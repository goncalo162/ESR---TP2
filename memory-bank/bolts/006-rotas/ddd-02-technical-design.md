---
unit: "003-rotas"
bolt: "006-rotas"
stage: "design"
status: "complete"
---
# IMP-009 — Design: anúncios, catálogo e composição no nó

✅ Model e Design completo aprovados pelo utilizador; registo 2026-11-14T12:53:08Z.
Q-C-28 Opção1 e Q-C-29/30, incluindo limites, ordens e política de metadados,
aprovados integralmente. Apenas revisão ADR autorizada; Implement/Test não
iniciados. V-012/D01–D32 não executados. As formulações de proposta no corpo
seguinte são histórico do documento aprovado; ver registo final e revisão ADR.
Registo real e autorização: [model-approval.json](../../../experiments/construction/006-rotas/model-approval.json).

## 1. Autoridade, âmbito e garantias

ADD/IMP-009, ADR-004/016/017/018/020/024, Q-C-26 e Model são a base.
As APIs de dependências provêm dos contratos documentados e da revisão ADR do
IMP-008. Nenhuma análise nova de fontes Java nesta etapa documental. A conferência
de assinaturas reais fica para ADR; uma divergência material para antes de código.

Q-C-28 amplia explicitamente o ADD de quatro para seis caminhos:

1 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java` — owner emprestado, propagação e integração do domínio.
2 - `Projeto/src/main/java/pt/uminho/esr/ott/streaming/StreamInfo.java` — metadados imutáveis.
3 - `Projeto/src/main/java/pt/uminho/esr/ott/streaming/StreamCatalog.java` — projeção imutável do catálogo.
4 - `Projeto/src/test/java/pt/uminho/esr/ott/routing/RoutingServiceTest.java` — relógio, scheduler, sessões e escritas controlados; grafos literais neste ficheiro.
5 - `Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java` — composição aditiva no owner e lifecycle existentes.
6 - `Projeto/src/test/java/pt/uminho/esr/ott/overlay/OverlayNodeRoutingTest.java` — novo teste de composição, incluindo TCP real.

Tipos auxiliares ficam aninhados nestes ficheiros. Não editar RoutingTable,
NetworkMonitor, NeighborManager, transporte, codec, configuração ou testes antigos
por inferência. Preservar construtores e observadores existentes. Se uma garantia
exigir outro caminho, apresentar a lacuna antes de intervir (ADD11/ADR-024).

Continuam TCP v2 único, 19 tipos ativos, código3 rejeitado, framing37..65536,
UTF-8 estrito e UDP OTT1 intocado. ANNOUNCE9/WITHDRAW10 usam o codec atual;
sem novo tipo, propriedade ou bootstrap periódico. Sem CLI nova, subscrições,
ativação de dados, sockets próprios de routing ou dependências runtime.

## 2. Propriedade e interfaces propostas

Todas as mutações de RoutingService, RoutingTable e StreamCatalog ocorrem no
`NeighborManager.SerialOwner` já pertencente a OverlayNode. O serviço nunca
cria outro owner. Snapshot público é imutável; tabela mutável não é exposta.

Assinaturas propostas, com auxiliares públicos aninhados onde a injeção entre
pacotes o exige; não são declarações Java já implementadas:

```java
RoutingService(NodeConfiguration config, UUID instanceId,
    NeighborManager.SerialOwner owner, ControlPort control, Observer observer)
RoutingService(NodeConfiguration config, UUID instanceId,
    NeighborManager.SerialOwner owner, ControlPort control, Observer observer,
    LongSupplier nanoTime, Scheduler scheduler)
void start()                              // owner
void onNeighborEvent(NeighborManager.Event event) // owner
void onMetrics(NetworkMonitor.Event event) // owner
void setLocalAvailable(UUID streamId, boolean available) // owner, só oferta local configurada
Snapshot snapshot()                       // qualquer thread; projeção publicada
CompletionStage<Void> requestClose()       // qualquer thread, sem espera
boolean awaitTermination(Duration timeout) // nunca bloquear no owner
void close()                              // delega no mesmo prazo de fecho
```

1 - `ControlPort.send(Neighbor.Session, ControlMessage)` devolve
    `CompletionStage<Connection.SendResult>`; `Factory.create(config, instanceId,
    owner, control, observer)` cria RoutingService. Port apenas adapta manager.send.
2 - `Scheduler` agenda por prazo monotónico; `Ticket` distingue cancelamento lógico
    de terminação física e permite aguardar esta última com prazo. A implementação
    normal é aninhada; a controlada não inicia threads. Construtor normal usa
    System.nanoTime. Validação ocorre antes de iniciar helper ou sockets.
3 - `Observer` recebe eventos locais imutáveis de diagnóstico/falha; não amplia
    os eventos selados de NeighborManager nem intercepta ERROR para consumidores futuros.
4 - OverlayNode conserva as APIs e o construtor de teste com NetworkMonitor.Factory.
    Acrescenta overload de teste com essa factory e RoutingService.Factory, e
    `routingSnapshot()` público imutável. Não publica owner, manager ou Connection.
5 - StreamInfo é record com `UUID streamId, String serverId, String title,
    int codec, int fps, UUID serverEpoch`. Conversão explícita para/de StreamInfo
    do wire, sem criar segundo codec. Validar ID, UUID, codec1/MJPEG, fps1..60,
    título1..128 codepoints/512 bytes UTF-8, sem NUL ou surrogate isolado.
6 - StreamCatalog mantém `Entry`/`Snapshot` aninhados e substituição da projeção
    pelo serviço no owner. Entry inclui info, disponibilidade, métrica/upstream
    opcionais, oferta local e validade remanescente no instante de captura.
    Snapshot inclui capturedAtNanos e mapa copiado; sem setters públicos do nó.

RoutingService.Snapshot inclui catálogo e mapa de Route da mesma transação do
owner, instante de captura e Diagnostics com contadores saturantes/última falha.
Nada disso autoriza vídeo: selectedCandidate/knownUpstream continuam distintos
de activeUpstream/pendingUpstream/downstreams, vazios neste incremento.

Um snapshot é válido **no instante indicado**, não é relógio vivo nem autorização
de dados. Uma projeção pura `visibleAt(now)` pode ocultar entradas entretanto
expiradas e reduzir a duração remanescente, sem renovar ou alterar o domínio.
Sem RTT há candidato conhecido, catálogo UNAVAILABLE e métrica vazia; custo zero
nunca substitui OptionalLong.empty. Streams sem oferta/candidato vivo desaparecem
do catálogo; bindings e históricos protegidos continuam na tabela.

## 3. Admissão, origem e transações no owner

1 - Identificar Session completa `(nodeId, instanceId, connectionId)` atualmente
    ACTIVE. Eventos de conexão/instância antiga não atingem Root, catálogo ou cache.
2 - Validar conteúdo e contexto antes de mutar: origem do path=serverId, último
    elemento=sender imediato, path único e sem localId, máximo32 IDs após append,
    metadados consistentes por UUID/época e custo somável sem overflow. A identidade
    do envelope é a do relay; serverEpoch pertence à fonte, nunca ao relay.
3 - Aplicar bindStream/confirmEpoch explícitos. UUID não pode mudar de origem.
    Para origem conhecida, confirmar mudança de época antes de consumir binding
    novo: saturação do histórico não deve deixar UUID adicional reservado. Para
    origem nova, reservar primeiro as capacidades necessárias ao primeiro binding.
4 - Encaminhar `LinkMetrics.costMicros()` diretamente a setLinkCost; já contém a
    conversão ceil da EWMA em µs. SUSPECT com amostra mantém custo; UNMEASURED fica
    vazio. Somar custos com operação verificada, sem dupla conversão ou clamp.
5 - Admitir candidato por vizinho **antes da decisão de flooding**. Cache hit,
    quota ou fila cheia não impedem guardar segunda chegada/alternativa legítima.
6 - Produzir uma projeção coerente e só então publicá-la. Uma mudança de época
    invalida todos os streams dessa fonte na mesma transação do owner, limpa
    metadados/intenções antigos e prepara retiradas de publicidade efetiva antiga;
    outras fontes não mudam. Não ordenar UUIDs para inferir época mais recente.

### 3.1. Referência de metadados por UUID/época — Q-C-30 proposta

A referência é o tuplo imutável `(title, codec, fps)` associado ao binding
`(streamId, serverId)` e à época corrente dessa fonte. Não se deduz do candidato
selecionado nem se escolhe novamente quando muda o melhor caminho.

1 - **Estabelecimento local:** a configuração validada da oferta SERVER fixa o
    tuplo quando essa oferta é instalada na época local. Suspender/retomar a
    oferta na mesma época não permite alterar o tuplo; não há reload neste IMP.
2 - **Estabelecimento remoto:** sem referência nessa época, o primeiro ANNOUNCE
    que passa validação completa de sessão/identidade/origem/path/conteúdo e é
    efetivamente admitido pelo Root como candidato vivo fixa o tuplo no mesmo
    turno do owner, antes de publicar o snapshot. Candidato sem RTT também pode
    fixá-lo: é conhecido e válido, apesar de ainda UNAVAILABLE para seleção.
    Mensagem rejeitada, época não confirmada ou candidato não admitido não fixa
    referência. Reservar capacidade antes da admissão; um binding eventualmente
    reservado sem candidato não é, por si só, referência de metadados.
3 - **Comparação:** exigir igualdade exata dos três campos em todos os anúncios
    seguintes desse UUID/época, qualquer que seja o caminho ou a sequência.
    Título compara a sequência Unicode validada, sem trim, case-fold ou
    normalização implícita; codec/FPS comparam os valores numéricos. No protocolo
    atual só codec1 é válido; outro codec já falha a validação estrutural.
4 - **Contradição:** rejeitar o anúncio inteiro antes de alterar Root, watermarks,
    referência, cache, ledger ou catálogo. Não instalar/remover candidatos,
    renovar TTL, retirar publicidade saudável ou encerrar a sessão por esse
    conflito de conteúdo; apenas diagnóstico limitado. Inclui contradição no
    mesmo sender de um candidato saudável. Seq maior não autoriza mudar título/
    codec/FPS. Anúncio coerente posterior é avaliado normalmente, inclusive com a
    mesma seq rejeitada: o conflito não consumiu geração nem instalou watermark.
5 - **Retenção e limite:** a referência permanece mesmo após WITHDRAW, NeighborDown
    ou expiração de **todos** os candidatos, watermarks e cache de flooding.
    Catálogo visível fica vazio, mas a referência não é TTL de rota nem oferta.
    Há no máximo uma referência da época corrente por binding: 256 por origem,
    64 origens, total S=16384, alocadas por necessidade. Não há referência por
    candidato/seq/caminho, arquivo de tuplos de épocas antigas, LRU ou novo limite
    baseado em routing.cache.capacity. Saturação recusa novo binding/referência
    explicitamente, sem expulsar os existentes ou deixar candidato sem a sua
    referência. Não se liberta uma referência só para aceitar conteúdo diferente.
6 - **Mudança permitida:** só a transação confirmEpoch aceite pelas regras Q-C-26
    elimina as referências da época anterior dessa fonte, juntamente com a
    invalidação dos seus candidatos. O primeiro candidato admitido na nova época
    estabelece o novo tuplo; mudança de época rejeitada preserva tudo. Outras
    fontes não mudam. Reiniciar o processo também elimina este estado volátil.
    Uma nova seq, expiry ou reconexão na mesma época nunca bastam. Épocas retiradas
    continuam protegidas apenas pelo histórico limitado Q-C-26 (oito/cacheTtl);
    esta política não acrescenta proteção eterna nem ordenação temporal de UUID.

É confiança de laboratório na primeira oferta/admissão validada, não autenticação
criptográfica dos metadados da fonte nem consenso entre caminhos. Não votar,
misturar campos ou substituir a referência pelo último anúncio recebido.
O catálogo usa sempre esse tuplo enquanto há oferta/candidato vivo coerente.
A retenção sem TTL e o critério de primeira admissão são proposta nova Q-C-30,
pendente desta revisão; não alteram as restantes propostas de Design/Q-C-29.

Cada operação Root preserva a atomicidade já contratada. Reservar recursos de
metadados antes de mutar; não prometer transação distribuída entre métodos Root.
Após confirmEpoch aceite, uma falha posterior de admissão é UNAVAILABLE explícito,
sem restaurar época retirada. Erro local de relógio/invariante gera falha tipada
e cleanup, não rollback inventado. Leitores externos só veem projeções completas.

Mantêm-se Q-C-26: 64 origens,256 streams/origem, S=16384 bindings persistentes,
N=min(C,63) sessões/slots por stream, oito épocas retiradas/origem. Alocação por
necessidade, sem LRU de bindings, candidatos/históricos protegidos. Não reutilizar
routing.cache.capacity como limite de linhas. Retenção de épocas/watermarks usa
cacheTtl; candidatos usam candidateTtl, sem renovar por duplicados/métricas.

## 4. Fontes, sequência e validade

SERVER publica todas as streams configuradas mesmo sem consumidores. Proposta:
serverEpoch=instanceId do processo fonte, comum a todos os seus streams; sequência
independente por stream começa em0. Cada período announceInterval produz uma
sequência realmente nova com incremento verificado, path=[source], custo0.
Períodos perdidos não geram rajada de catch-up: no máximo uma geração nova por
stream quando o owner processa a publicação. Não ler ficheiros de vídeo agora.

Relay conserva stream/epoch/seq do **candidato selecionado**, acrescenta localId
ao path e usa a métrica desse candidato. Uma alternativa não selecionada com seq
maior não empresta a geração ao caminho antigo. Overflow de sequência local é
falha explícita, sem wrap nem mudança silenciosa de época; relay nunca incrementa.

setLocalAvailable(false) retira apenas a oferta configurada indicada, suspende
as suas publicações periódicas e prepara WITHDRAW por destinatário efetivamente
anunciado, usando a seq publicada nesse destinatário. true cria geração nova.
Esta porta serve testes e futuros consumidores, sem comando CLI neste IMP.

Somente admissão Root de sequência mais recente renova candidato/histórico.
Melhoria na mesma geração preserva os prazos; ausência de métricas, reenvio,
cache hit, reenvio pendente, callback e nova sessão de destino não os renovam.
Não enviar um anúncio cujo candidato já expirou quando a tentativa é iniciada.
Bytes já admitidos no TCP não são revogáveis: não alegar garantia de entrega
antes da expiração; o recetor aplica os seus próprios prazos locais.

## 5. Publicação por destinatário e ordem de retirada

Ledger lazy por `(streamId, Session)` guarda época, última publicidade realmente
admitida, floor retirado e **uma dívida de WITHDRAW**; nunca lista de mensagens.
Máximo S×N ledgers de sessões correntes. Floor persiste até fim da sessão/época;
expirar cache de flooding não permite ressuscitar geração retirada nesse peer.
Anúncio não é enviado a destinatário que já pertence ao path selecionado.

1 - Sem publicidade anterior nesse destinatário: primeira geração corrente pode
    ser enviada se ainda válida e houver reservas. Destinatário novo não é
    confundido com sessão anterior pelo cache hit global.
2 - Mesmo epoch/seq e mesmo payload: não enviar. Melhoria publicável na mesma
    geração exige custo menor, ou custo igual e menos saltos, comparados **como
    esse destinatário os verá**, sempre com o relay como mesmo sender imediato.
3 - Mudança local apenas de nextHop/path com custo e saltos iguais não satisfaz
    esse predicado do recetor. Assim como alternativa pior, exige retirar a
    publicidade anterior e aguardar geração estritamente nova para esse peer.
    O desempate local por nextHopId não cria melhoria de custo/saltos no recetor.
4 - Candidato expirado/retirado, exclusão pelo path ou época substituída criam
    dívida da publicidade admitida. Alternativas locais sobrevivem. Não anunciar
    novamente seq≤floor retirado, mesmo que depois surja melhoria nessa geração.
5 - Geração nova pode substituir diretamente publicidade antiga se nenhuma
    retirada estiver pendente. **Uma dívida já criada não é apagada/coalescida
    por ANNOUNCE novo**: primeiro WITHDRAW antigo admitido; só depois tentar novo
    ANNOUNCE no mesmo TCP FIFO. WITHDRAW não limpa candidatos de outros senders.
6 - Na mesma época, geração selecionada menor que a maior já admitida nesse
    destinatário nunca é publicada, mesmo com custo melhor. Se substitui um
    anúncio que perdeu validade, retirar a geração efetivamente publicada e
    aguardar seq maior que esse floor. O ledger não regride ao candidato antigo.

Exemplo A: relay publicou E/7/custo100; perde esse candidato e seleciona custo150
da mesma geração. Envia W(E,7), preserva a alternativa local, aguarda E/8 do path
selecionado para anunciar150. Receber E/8 só numa alternativa não selecionada não
autoriza publicar o path E/7 com seq8. Uma melhoria E/7 após a retirada também espera.

Exemplo B: W(E,7) está pendente por quota; chega E/8. A pendência conserva W(E,7)
e coalesce apenas a intenção ANNOUNCE. Na janela seguinte tenta W; após ACCEPTED,
na próxima oportunidade de quota tenta A(E,8). A ordem não depende de ACK de vídeo.

Exemplo C: muda apenas path/tie-break com igual custo/saltos em E/7. W+esperaE/8;
não afirmar que o recetor aceita a troca na mesma geração. Em ciclo, geração fixa
termina sem flooding infinito; pode haver indisponibilidade até geração nova.
Convergência de V-012 usa as gerações periódicas normais e carga comportável;
estabilização de uma geração não significa parar anúncios periódicos da fonte.

## 6. Capacidades propostas para Q-C-29

Valores derivados, sem novas propriedades/caps de deployment. C=control.max.connections,
E=node.events.capacity, K=routing.cache.capacity, F=routing.flood.max.per.sec.
Configuração existente valida domínios; produção N=min(C,63), K/E≤4096 e F≤10.

1 - **Cache de flooding: K chaves** `(streamId,epoch,seq)`, horizonte H=cacheTtl
    desde primeira reserva. Guarda melhor informação de reenvio e estado limitado
    a N destinatários por chave, no máximo K×N=258048 células. Nenhuma melhoria,
    duplicado ou callback estende H. Só remover expirados; cheia recusa nova chave
    com diagnóstico, sem desfazer Root/catálogo. W de publicidade existente não
    depende de reservar nova chave de cache.
2 - **Ledger: S×N células**, máximo1032192, lazy. Uma dívida W/célula, sem histórico
    de payloads; contém apenas dados suficientes para correlação/rank/barreira.
    Retirar sessão remove estado lógico; tickets físicos sobrevivem separadamente.
3 - **Coalescer: P=min(K,E) células ativas**, cada uma com no máximo duas intenções:
    dívida W + último ANNOUNCE, máximo8192 intenções. ANN novo substitui apenas
    ANN ainda não tentado, revalidando seq/path/TTL. Se P está cheio, dívida W fica
    sinalizada no ledger existente e é descoberta por varrimento justo; nova
    tentativa de anúncio sem reserva é descartada/diagnosticada. Não criar fila
    adicional. Geração nova pode voltar a tentar; sem promessa em overload.
4 - **Quotas: S×N chaves `(streamId,nodeId)`**, incluindo IDs recentemente retirados;
    cada uma guarda no máximo F instantes. Janela deslizante exata de1s; expurgar
    instante com idade≥1s. Manter quota ainda viva após Down; reconectar o mesmo
    nodeId não a reinicia. Saturação por churn recusa quota nova sem expulsar
    timestamps protegidos nem fechar sessões saudáveis. Máximo10321920 instantes,
    nunca pré-alocados. ANN e W contam juntos, por tentativa antes de control.send;
    CLOSED/OVERFLOW/erro também consomem quota, sem refund ou retry imediato.
5 - **Tickets físicos: B=min(E,2N)≤126**, incluindo retirados. Uma admissão em curso
    por Session corrente; reservar ticket, quota e intenção antes de chamar port.
    Mantê-lo até terminação real do future/callback, não até cancelamento lógico.
    Ticket retirado não ocupa ledger corrente, mas continua em B.
6 - **Agendamento:** um helper normal, sem sockets/estado de rede; no máximo dois
    trabalhos físicos de timer (corrente e anterior a terminar), um wake enfileirado
    e um em execução. Callbacks apenas sinalizam trabalho. Por turno do owner,
    varrer até64 células e iniciar até16 envios; cursores cíclicos em índices
    limitados a S e N, sem pré-alocar produto/lista de tarefas. Uma continuação
    coalescida quando necessária; sem timer por stream/peer.

Quota vazia e cache expirados são removidos oportunisticamente; varrimento de
retiradas é justo e não fica atrás de anúncios sempre novos. Prazo do próximo wake
é o mínimo de publicação de fonte, expiração e disponibilidade de quota. Não
agendar wake imediato repetido quando a causa é port/owner saturado: manter sinal
limitado e retomar por conclusão ou próximo prazo útil apenas na recusa local
recuperável K/P/quota. owner.offer(false) em fila cheia inicia fecho: não esperar
outro wake. Guardar resultados físicos limitados e recolhê-los pelo cleanup/hook
de terminação do owner, sem libertar reservas antes de callback/continuação
terminarem. Saturação do owner é falha operacional explícita (Q-C-31 aprovada).

Monotonia/deadlines usam diferenças verificadas de nanoTime e ms→ns exatos;
valores máximos validados cabem em long. Sem wall clock para TTL. Clock que viola
o contrato controlado é erro local. Valores máximos teóricos acima são limites
de contagem, não medições de heap/latência. Operações Root que percorrem S linhas
continuam limitadas por Q-C-26; não alegar custo constante por evento.

## 7. Futures, callbacks antigos e falhas

ACCEPTED prova só admissão na fila TCP, não flush/entrega/ACK. A fila/writer comuns
garantem FIFO dos bytes admitidos. Completion fora dos locks do transporte; toda
consequência de domínio volta ao owner. Ticket inclui lifecycle, Session, epoch,
seq, revisão da intenção e resultado imutável.

1 - Callback de Session/lifecycle antigo: libertar só a reserva física quando
    terminou realmente. Não alterar consulta/estado novo, cache, quota ou TTL.
2 - Callback da Session corrente mas intenção substituída: nunca confirmar a
    intenção nova como enviada nem restaurar Root/catálogo antigo. Se ACCEPTED,
    registar **o recibo da publicidade antiga efetivamente admitida** no ledger
    limitado. Se já inválida, criar dívida W e barreira antes de anúncio novo.
    Ignorar esse recibo deixaria publicidade fantasma. Contabilidade de bytes
    antigos não é aceitação da geração antiga no domínio nem renovação de TTL.
3 - Exemplo: A(E1,7) recebe ACCEPTED depois da mudança paraE2. Root/catálogo ficam
    emE2; sessão corrente em RUNNING envia W(E1,7), depois A(E2,q). Callback do
    connectionId anterior só liberta ticket, sem W na conexão nova.
4 - CLOSED afeta só contexto da Session exata; OVERFLOW já fecha a ligação afetada,
    diagnostica e invalida as suas pendências, acompanhando NeighborDown sem retry
    nessa conexão. Não confundir com recusa local recuperável K/P/quota; sessões
    saudáveis ficam preservadas. owner saturado limpa pelos hooks, não por wake
    futuro. INVALID_MESSAGE interno, exceção de
    codec para mensagem construída localmente ou invariante quebrada são falha
    local tipada, com cleanup coordenado, não acusação genérica ao peer.
5 - ANN/W inválido no contexto é rejeitado atomicamente com diagnóstico limitado;
    não inventar resposta ERROR nem alterar política terminal de framing/identidade
    do manager. ERROR legítimo e mensagens alheias continuam para o observador
    original exatamente uma vez, intactos e com identidade/correlação da sessão.

Métrica tardia de sessão retirada não remove custo da sessão nova. Updated/
Invalidated correntes recalculam Root e publicidade sem mexer nos prazos de
candidatos. Falha remota isolada não fecha CLI, outras sessões ou outras fontes.
Future que nunca termina conserva reserva; shutdown reporta timeout, não sucesso.

## 8. Composição, publicação e shutdown

1 - Preservar criação do owner existente e configuração validada antes dos sockets.
    Construir manager, monitor e routing com o mesmo owner. Inicializar monitor
    e routing antes de permitir eventos NeighborUp do manager; preparar ofertas
    locais no owner. Não introduzir transporte de routing nem refazer bootstrap.
2 - NeighborEvent passa pelos diagnósticos existentes, monitor e routing; observer
    original recebe uma vez mesmo se um componente lança exceção (finally
    encadeado). Updated/Invalidated do monitor são encaminhados internamente para
    routing, mantendo o tratamento de falhas anterior. Não publicar duas vezes
    nem esconder eventos usados por consumidores futuros.
3 - Para cada evento, validar sessão, aplicar transação Root, projetar catálogo/
    rotas e publicar Snapshot único antes dos envios consequentes. Estes derivam
    dessa revisão, mas revalidam no início da tentativa. Snapshot/diagnósticos de
    outros componentes mantêm os contratos públicos anteriores.
4 - Falha parcial em factory/start pede cleanup a todos os componentes já criados;
    não deixar manager vivo por o routing ter falhado. Preservar causa tipada.
5 - Primeiro requestClose fecha gate de novos envios de routing, invalida intenções
    e cancela timers logicamente. Solicitar fecho do monitor e manager mesmo se
    outro falha. Usar o primeiro T0 e um só shutdownTimeout; awaits fora do owner/
    locks, passando prazo remanescente a cada componente, sem short-circuit.
6 - Fecho do nó não tenta escoar W de todos os streams depois do gate fechado:
    NODE_LEAVE/EOF existentes invalidam candidatos remotos. W de oferta removida
    em RUNNING segue secção5. Routing nunca fecha sozinho o owner emprestado.
7 - requestClose termina cleanup lógico; awaitTermination exige também helper,
    timers e tickets físicos terminados. Cancelar Future não é essa prova. Com
    worker/callback preso, devolver timeout explícito; não alegar zero recursos.

## 9. Oráculos propostos — todos por executar

Relógio manual, scheduler controlado, port com futures completáveis e harness de
owner serial determinístico em RoutingServiceTest. OverlayNodeRoutingTest usa as
factories para comprovar owner/lifecycle e um ensaio TCP real para a composição.
As fixtures de grafos são dados literais dos testes, sem caminhos adicionais.

1 - **D01 / M01:** duas fontes×dois streams, seq0/época por fonte, anúncio sem consumidores e sem leitura de média.
2 - **D02 / M02:** losango, primeira rota140µs seguida de130µs; aceita melhoria, preserva alternativa.
3 - **D03 / M03/M06:** cache hit não elimina candidato de outro vizinho nem melhoria posterior; empate local determinístico.
4 - **D04 / M04:** ciclo rejeita path com repetição/local, orçamento finito por geração; convergência com gerações periódicas e carga controlada.
5 - **D05 / M05:** retirar um stream/origem preserva os dois streams da outra; só remove candidato do sender correto.
6 - **D06 / M07:** UNMEASURED não custa0; Updated usa costMicros direto; SUSPECT conserva amostra; Invalidated sem renovar TTL.
7 - **D07 / M08:** origem/UUID/path/overflow inválidos não deixam mutação parcial ou cache nova. Antes da primeira referência, anúncio inválido não a fixa; o primeiro admitido, incluindo sem RTT, fixa M=("Video A",1,25). Outro caminho anuncia o mesmo UUID/época com título diferente ou FPS30, mesmo com seq maior/custo melhor: rejeição integral conserva referência, candidatos saudáveis, seleção, projeção de catálogo/rotas, watermarks e prazos; sem W/NeighborDown. Repetir pelo sender do candidato saudável, e com codec inválido. Anúncio posterior coerente com a mesma seq rejeitada é admitido normalmente; oferta local usa o tuplo configurado, não o de outro caminho.
8 - **D08 / M09:** sequência nova renova; duplicado/melhoria/quotas/callbacks não renovam. Com relógio controlado, estabelecer M na épocaE1, expirar todos os candidatos e avançar além de cacheTtl para expirar também cache/watermarks: catálogo vazio, referência M ainda reservada. Outro caminho com seq100/título ou FPS contraditório é rejeitado sem repor catálogo/candidato nem renovar prazo; o anúncio seq100 coerente é depois admitido e repõe M com prazo apenas dessa admissão. Reconexão/retirada não libertam M. Mudança E1→E2 aceite permite M2 novo; mudança rejeitada preserva M; E1 reaparecida dentro do histórico é rejeitada. Verificar limite de uma referência por binding, sem crescer por seq/caminho nem expulsar referências na saturação.
9 - **D09 / M10:** WITHDRAW seq antiga/futura/contexto errado conforme Root; nunca elimina candidatos de outros senders.
10 - **D10 / M11:** mudança E1→E2 atómica por fonte, outras preservadas, época retirada rejeitada, oitava/nona fronteiras de Q-C-26.
11 - **D11 / M12:** old Session/instance/connectionId, Down e métricas atrasadas não afetam nova sessão/TTL; reservas antigas continuam físicas.
12 - **D12 / M13:** F tentativas em qualquer janela deslizante de1s, ANN+W, fronteira exata, erros sem refund e reconexão sem reset.
13 - **D13 / M14:** K cheio só expurga expirados, recusa explícita preservando Root/catálogo; expiração não apaga floor por peer.
14 - **D14 / M14:** P cheio, no máximo2P intenções; recusa local recuperável não fecha sessões; W preservado no ledger e retomado justamente, anúncio recusado sem fila oculta.
15 - **D15 / M14/M18:** B físico inclui retirados; future bloqueado impede reserva extra e produz timeout explícito de fecho. OVERFLOW termina só a ligação afetada, limpa pendências da Session e não tenta de novo nessa conexão.
16 - **D16 / M15:** alternativa pior E/7→W(E,7); melhoria E/7 posterior não ressuscita; E/8 selecionado permite publicar.
17 - **D17 / M03/M15:** troca apenas de path/nextHop com igual custo/saltos exige W+geração nova, sem falsa melhoria no recetor.
18 - **D18 / M15:** alternativa E/8 não selecionada não empresta seq ao path E/7; candidato selecionado de geração anterior à publicada não regride ledger; retirar e esperar seq>floor.
19 - **D19 / M13/M15:** W pendente não é apagado por ANN de geração nova; controlar futures/quota e conferir FIFO W antes de ANN.
20 - **D20 / M12/M15:** ACCEPTED tardio na mesma sessão após nova época conserva Root novo, regista recibo velho e ordena W velho/ANN novo.
21 - **D21 / M12:** ACCEPTED de revisão antiga não confirma intenção nova; callback de sessão antiga só liberta ticket físico.
22 - **D22 / M16:** catálogo/rotas publicados na mesma revisão, cópias defensivas, validade as-of, nenhum activeUpstream/destino/UDP.
23 - **D23 / M17:** sessão criada pela reparação recebe geração corrente; não depende de grafo inicial nem consulta bootstrap de novo.
24 - **D24 / M18:** start/factory falha a meio, rollback de todos; gate antes de shutdown, prazo único, owner emprestado não fechado pelo serviço.
25 - **D25 / M19:** bytes/tipos/caps atuais e observadores anteriores preservados; ERROR e outros eventos entregues uma vez intactos.
26 - **D26 / M20:** factories recebem mesmo owner; monitor Updated chega a routing, vizinho TCP real recebe ANN/W pelo manager atual.
27 - **D27 / M20:** API antiga de OverlayNode e constructor de monitor preservados; observer original recebe evento mesmo perante exceção.
28 - **D28 / M21:** S/N/64/256/8, ledger/quota em fronteiras pequenas injetadas, saturação atómica, alocação lazy e sem expulsar protegido.
29 - **D29 / M13/M18:** máximo2 timers físicos, wake coalescido, turnos64/16; owner.offer cheio inicia fecho e cleanup pelos hooks, sem depender de outro wake, sem continuations ilimitadas/busy-loop nem libertação física antecipada.
30 - **D30 / M01/M09:** atraso em vários períodos produz só geração nova útil; source withdraw/resume aumenta seq real, sem wrap.
31 - **D31 / M16/M21:** snapshot puro envelhece sem mutar Root; título UTF-8/codec/fps válidos e fronteiras, diagnóstico limitado.
32 - **D32 / M22:** V-012 completo, regressão JDK21/log/XML/JAR atual, manual humano reproduzido antes de aprovar Test, revisão final Denji e integração.

Não alegar cobertura executada. Na implementação, introduzir casos e guardar
falhas reais antes de completar comportamentos novos; não provocar falhas em
fundações completas. Se testes antigos pressupõem ausência de tráfego de routing,
apresentar a incompatibilidade e o âmbito antes de os alterar/enfraquecer.

## 10. Revisão e próximo checkpoint

Proposta concreta de Q-C-29: K cache, P=min(K,E), B=min(E,2N), quota deslizante
com retenção após Down, ledger S×N, timers2/turnos64/16, ordens W/ANN e recibos
antigos. Estes valores e o uso de instanceId como época local aguardam esta revisão.
Q-C-28 já aprovada autoriza o âmbito documental de seis caminhos, não código agora.
Q-C-30 propõe a referência de metadados da secção3.1: primeira admissão/oferta,
retenção na época corrente mesmo após expiração total e rejeição atómica de
contradições. D07/D08 concretizados; continuam 32 oráculos, todos por executar.

Após aprovação de Design, avançar **apenas para ADR**, conferir contratos/APIs e
conformidade ADR-004/016/017/018/020/024. Divergência aceite inviável aplica ADD11.
Nenhuma ADR nova aceite por este documento. Sem gates externos adicionais.

V-012, regressão atual, manual humano antes de Test aprovado, revisão final
atribuída a Denji e integração permanecem obrigatórios. Nenhum fecho, Java, Maven,
socket, commit/push ou início de outro IMP nesta etapa.

## Aprovação integral e revisão ADR — estado atual

Registo 2026-11-14T12:53:08Z. Utilizador aprovou expressamente todo o Design, limites/ordens
Q-C-29 e política Q-C-30. Apenas revisão ADR autorizada. Nenhum Java alterado.
A [revisão dos contratos reais](adr-review.md) está para aprovação; assinala em
Q-C-31 que o owner.offer recusa por fila cheia iniciando fecho, e send devolve
OVERFLOW depois de fechar a ligação. Clarificação proposta contra essas APIs,
sem alterar por inferência a política aprovada ou as fundações. Restantes
limites/ordens/metadados aprovados mantidos. Nenhuma proposta Q-C-31 aprovada
por esta transição. Os32 oráculos continuam por executar, sem gates externos.
V-012, regressão atual, manual humano antes de aprovar Test, revisão final Denji
e integração mantêm-se obrigatórios; sem Implement, commits/push ou outro IMP.


## Aprovação ADR/Q-C-31 e início apenas de Implement

Registo 2026-11-14T12:58:05Z. Revisão ADR/Q-C-31 explicitamente aprovadas; secções6/7 e
D14/D15/D29 clarificadas conforme contratos reais. Apenas Implement nos seis
caminhos autorizado; testes incrementais/evidências, sem avançar a Test/fecho,
commits ou push. Os32 oráculos estavam por executar à entrada desta etapa.
