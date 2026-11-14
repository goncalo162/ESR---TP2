---
unit: "003-rotas"
bolt: "006-rotas"
stage: "implement"
status: "complete"
---
# IMP-009 — Implement para revisão

✅ Model/Design/ADR e Q-C-28/29/30/31 aprovados. ⏳ Implement preparado para
revisão; não aprovado por este documento. Test não iniciado, bolt não fechado.
Base de comparação164ac0dc477c02bc7fde816c761d137ee18ea55d, ramo Denji.
[Autorização/estado de entrada](../../../experiments/construction/006-rotas/implement/entry.json).
Nenhum commit, staging, push ou outro IMP.

## 1. Comportamento implementado nos seis caminhos

1 - **streaming/StreamInfo:** record validado, tuplo imutável por UUID/origem/época,
    conversão explícita para StreamInfo wire atual. Título UTF-8 estrito sem NUL/
    surrogate isolado, codec1 e FPS1..60; não lê vídeo.
2 - **streaming/StreamCatalog:** Entry/Snapshot imutáveis com custo/upstream
    opcionais e validade no instante da captura; visibleAt puro não altera Root
    ou renova TTL. Capacidade própria derivada de Q-C-26, não de flooding.
3 - **routing/RoutingService:** reutiliza RoutingTable no owner emprestado;
    ofertas configuradas periódicas, admissão por vizinho, referência de metadados
    persistente por época, cache K, coalescer P, quota deslizante por stream/nodeId,
    ledger por stream/Session e tickets físicos B. Sem nova ligação ou codec.
4 - **overlay/OverlayNode:** compõe factory/eventos de routing e métricas no owner
    existente, preservando APIs e observadores; routingSnapshot novo e fecho/
    rollback no prazo global anterior, sem alterar o resultado NODE_LEAVE.
5 - **routing/RoutingServiceTest:**39 testes determinísticos com relógio,
    scheduler, owner e futuras admissões controlados, mais grafos literais.
6 - **overlay/OverlayNodeRoutingTest:**6 testes novos de integração/lifecycle,
    incluindo TCP real ANNOUNCE/WITHDRAW/ERROR, bootstrap desligado após início,
    codec existente, mesmo owner, rollback e deadline partilhado.

Recusa local K/P/quota é recuperável. OVERFLOW termina apenas a Session afetada;
sem retry nessa conexão. owner.offer recusado inicia stop: cleanup por hook, sem
esperar wake novo. Callback/continuação mantêm reserva física até ambos terminarem.
A libertação final sinaliza trabalho ainda pendente quando o owner continua vivo.

Metadados são fixados pela oferta local ou primeiro candidato admitido, incluindo
sem RTT; mensagem inválida/recusada não fixa referência. Contradição na mesma
época é rejeitada sem consumir seq, alterar candidatos saudáveis ou renovar TTL.
Referência persiste sem candidato e só muda após época confirmada/reinício.

Publicação usa seq/path/custo do candidato selecionado. Alternativa pior, troca
igual de custo/saltos ou geração selecionada anterior à já publicada exigem W e
aguardam geração suficiente. W pendente não é coalescido por ANN novo; ACCEPTED
antigo na sessão corrente gera recibo/retirada coerente, sem restaurar Root antigo.
Callback de sessão antiga só termina a sua reserva. Seleção não autoriza dados.

## 2. Evidência incremental preservada

Os comandos usam JDK21 via JAVA_HOME, Maven sem mudar POM/dependências. O helper
[run-incremental.py](../../../experiments/construction/006-rotas/implement/run-incremental.py)
regista comando, instantes reais, exit, hashes da versão e copia os XML atuais.
Cada diretório é novo; não substituir resultados anteriores por uma repetição.

1 - **red-01:**17 testes,16 erros UnsupportedOperationException nos comportamentos
    ausentes,1 teste de metadados passou. Scaffold compilável; falhas reais de
    execução e XML preservados, sem provocar falhas de fundações já completas.
2 - **green-01:**17 testes,1 falha no gatilho da bancada de saturação: nó vazio
    não tinha timer para provocar a recusa. Corrigido o cenário para fonte com
    publicação agendada; oráculo de cleanup não foi removido/enfraquecido.
3 - **green-02:**17 testes novos passaram;9 erros de abertura de sockets em testes
    anteriores de monitor, por sandbox. Execução restrita preservada.
4 - **green-03:** repetição autorizada com TCP loopback,28 testes passaram,
    incluindo os11 anteriores de OverlayNodeMonitoringTest intactos.
5 - **incremental-04:**30 testes determinísticos passaram, incluindo ciclos,
    duas fontes/quatro streams, saturação e gerações/callbacks antigos.
6 - **incremental-05:** falhou a compilação do teste novo: BootstrapServer já
    inicia no construtor, não existe start(). Nenhum teste executado nessa
    tentativa. O primeiro helper copiou XML antigos de30 testes; o
    [result-correction.json](../../../experiments/construction/006-rotas/implement/incremental-05/result-correction.json)
    corrige explicitamente essa contagem, preservando log/JSON/XML brutos.
    O helper passou a filtrar XML pelo instante de início da execução.
7 - **incremental-06:**47 testes passaram:30 routing+6 integração nova+11 monitor.
8 - **red-07:**35 testes,1 falha real: anúncio de UUID novo em capacidade cheia
    podia confirmar época antes de recusar o binding, invalidando estado saudável.
9 - **red-08:**36 testes,2 falhas reais: a anterior e uma corrida em que a
    continuação do owner acabava antes do callback, faltando sinalizar trabalho
    pendente ao libertar o último ticket físico.
10 - **green-09:**36 testes determinísticos passaram após pré-validar capacidade
     antes da mudança de época e sinalizar libertação física fora do lock.
11 - **incremental-regression-10:** `mvn -B -ntp '-Dtest=*' test`, JDK21,
     **1253 testes,0 falhas,0 erros,0 skips,16 XML**. Inclui42 novos e1211 anteriores.
     [Comando, instantes e hashes](../../../experiments/construction/006-rotas/implement/incremental-regression-10/result.json),
     [log](../../../experiments/construction/006-rotas/implement/incremental-regression-10/maven.log).

12 - **red-11:**37 testes,1 falha real no cleanup do construtor se o relógio
     injetado lança exceção. A leitura inicial foi movida para antes do helper/hook;
     falha posterior à inscrição remove o hook e pede fecho ao scheduler.
13 - **incremental-regression-12:** regressão final da versão atual, mesmo comando
     JDK21, **1254 testes,0 falhas,0 erros,0 skips,16 XML** (43 novos/1211 anteriores).
     [Resultado/hash da versão final](../../../experiments/construction/006-rotas/implement/incremental-regression-12/result.json),
     [log final](../../../experiments/construction/006-rotas/implement/incremental-regression-12/maven.log).

A regressão incremental é real, mas não substitui clean verify/Test. Nenhum JAR
novo foi produzido neste checkpoint; o JAR histórico de IMP-008 permanece
preservado e não representa o código novo. Sem manual humano desta versão.

## 3. Correspondência D01–D32

Os nomes são prefixos de métodos nos testes autorizados; detalhes dos contratos
herdados também são exercitados pelos35 testes anteriores de RoutingTableTest,
inalterados e incluídos na regressão1254. Não transformar estes casos planeados
em aprovação formal de Test ou conclusão de V-012.

| Oráculo | Evidência incremental / limite |
|---|---|
| D01 | d01SourcePublishes...: duas ofertas sem consumidores, seq0/época da fonte, geração periódica sem catch-up; d05TwoOrigins...: quatro streams |
| D02 | d02D03Diamond...:140→130µs; d04CyclicDiamond... convergência do grafo |
| D03 | d02D03Diamond...: alternativa apesar de cache hit; d17EqualCostPath... protege desempate visto pelo recetor |
| D04 | d04CyclicDiamond...: ciclo, path único e estabilização da geração; gerações periódicas normais |
| D05 | d05TwoOrigins... e d09Withdrawal...: origem/stream independente e retirada por sender |
| D06 | d06Unmeasured...: ausência de RTT, custo direto, SUSPECT e prazo original; contratos de Invalidated também pela regressão monitor/tabela |
| D07 | d07ConflictingMetadata..., d07InvalidBeforeFirst..., d07IdentityAndCostOverflow...; domínio UTF-8/codecs em d31 |
| D08 | d08AnnounceRevalidatesExpiryBetweenTwoSendsInOneTurn e d08ReferenceSurvives...: além de candidate/cache/watermark TTL, conflito tardio, seq rejeitada reutilizável e época nova; referências não expulsas em d28 |
| D09 | d09Withdrawal... mais testes anteriores d11* de RoutingTableTest para watermarks/seq antiga e nova |
| D10 | d08D10EpochChange... mais testes anteriores d12*/d13* da tabela para atomicidade por fonte e oitava/nona retirada |
| D11 | d11OldSession...: Down, métricas e anúncio antigo não alteram substituta |
| D12 | d12SlidingWindow... e d12ClosedAttempt...: fronteira1s, reconexão, sem refund de tentativa CLOSED |
| D13 | d13FullFloodCache... e d16Worse...: recusa de cache conserva candidato, floor impede reposição da geração |
| D14 | d14LocalPending...: P limitado, dívida W sem célula preservada, retoma justa e peers saudáveis |
| D15 | d15BlockedPhysical..., d15OutputOverflow..., d15PhysicalRelease...: físicos retirados, timeout, isolamento e corrida callback/continuação |
| D16 | d16WorseSameGeneration...: W/espera/geração nova, sem ressuscitar melhoria tardia |
| D17 | d17EqualCostPath...: path diferente com igual custo/saltos não é melhoria para recetor |
| D18 | d18SelectedGeneration...: não empresta seq de alternativa nem regride face à publicada |
| D19 | d19WithdrawalCannot...: W antes de ANN novo, futures/quota controlados |
| D20 | d20LateAccepted...: E2 permanece no domínio e recibo E1 ordena W antigo/ANN novo |
| D21 | d21LateRevision...: admissão antiga não confirma payload novo; sessão antiga não afeta nova |
| D22 | d22VisibleAtNeverRelabelsExpiredSelectionAsCurrentWhileAlternativeLives e d22ImmutableSnapshots...: mapas defensivos, validade pura e nenhuma autorização de dados |
| D23 | d23NewRepairPeer...; testes anteriores de reparação/tabela/monitor: NeighborUp de nova aresta é consumido sem bootstrap periódico |
| D24 | d24ClockFailureDuringConstruction..., d24CloseBeforeStart..., d24StopBeforeStart..., d24RoutingFactoryFailure..., d24SharedDeadline... e11 testes anteriores do nó |
| D25 | d25UnrelatedEvents... e d27OriginalObserver...: ERROR intacto/uma vez; codec/fixtures preservados e reexecutados na regressão |
| D26 | d26RealTcpSharesOwner...: owner idêntico, monitor→routing, TCP/codec atuais, W com bootstrap parado |
| D27 | d27OldConstructor..., d27OriginalObserver... e testes anteriores de finally/erro do monitor |
| D28 | d28BindingSaturation..., d28NewStreamAtCapacity...; limites exatos Q-C-26 e alocação lazy nos testes anteriores d17* da tabela |
| D29 | d29RunningTimer... e d29RejectedCompletionWake...: cancelamento não prova fim físico, hook sem wake novo; turnos64/16 e timers2 explícitos no código, prova mais ampla fica na revisão/validação |
| D30 | d01SourcePublishes... e d30SourceSequenceExhaustion...: atraso, withdraw/resume, saturação sem wrap |
| D31 | d31MetadataUnicode..., d31InvalidSource... e snapshots puros; diagnósticos saturantes por implementação, sem medição de heap |
| D32 | **PARCIAL:** regressão incremental/log/XML/hashes disponíveis; clean verify, novo JAR, manual humano, aprovação Test, revisão Denji e integração pendentes |

V-012 tem ensaios técnicos incrementais em losango/ciclo/quatro streams/retirada;
consolidação de V-012 e decisão de aprovação pertencem à etapa Test futura.
D29/D31 incluem detalhes verificados estaticamente, sem alegar ensaio exaustivo
em todos os máximos ou resultados de desempenho/heap/CORE/GUI/vídeo.

## 4. Oito linhas substituídas na fundação OverlayNode

Somente OverlayNode tinha implementação anterior nos seis caminhos. Nenhum teste
anterior foi apagado/modificado. As oito linhas removidas no diff têm substituição
que mantém o comportamento anterior e acrescenta a composição autorizada:

1 - **2 linhas** do observer de LocalFailure do monitor: preservadas dentro de
    finally depois da entrega de métricas ao routing.
2 - **1 linha** monitor.start: mantém a chamada e acrescenta routing.start no owner.
3 - **1 linha** m.onEvent: mantém chamada em try/finally com routing, sem perder o
    finally exterior que entrega o evento original ao consumidor.
4 - **3 linhas** stop de monitor/manager/owner: mantidas em finally encadeados,
    acrescentando o gate de routing e garantindo pedidos de cleanup a todos.
5 - **1 linha** allOf da notificação: conserva monitoring/leave e inclui o future
    de cleanup lógico de routing; tipo/resultado de ShutdownReport intactos.

As restantes mudanças são adições. Nenhum codec, transporte, POM, configuração,
RoutingTable, NetworkMonitor ou NeighborManager foi editado. Nenhuma ampliação de
âmbito necessária. Listas geradas em target/ aparecem separadas no pacote de diff;
não as tratar como alterações aprovadas de fonte.

## 5. Checkpoint e pendências

⏳ Implement para revisão; Model/Design/ADR concluídos. Sem avanço automático a
Test. Não fechar bolt/TASK/unidade/fase nem marcar critérios finais cumpridos.
Sem gates externos adicionais. Antes de fecho: V-012 consolidado, clean verify
JDK21 com log/XML/JAR novo, manual humano antes de aprovar Test, revisão final
atribuída a Denji e integração. Sem commits/push ou início de outro IMP.


## Pacote anterior de revisão preservado (1254 testes)

[Diff de fonte contra a base](../../../experiments/construction/006-rotas/implement/source.diff),
[diff documental desta etapa](../../../experiments/construction/006-rotas/implement/documents.diff)
e [acumulado documental](../../../experiments/construction/006-rotas/implement/cumulative-documents.diff).
[Listas target geradas separadas](../../../experiments/construction/006-rotas/implement/target-generated.numstat).
[V-ADD](../../../experiments/construction/006-rotas/implement/v-add.md) com zero erros/78 fixtures;
[resultados/hashes e preservação](../../../experiments/construction/006-rotas/implement/results.json).

## Correção TTL/visibleAt — Implement reapresentado para revisão

Registo 2026-11-14T15:00:17Z. Pedido explícito do utilizador: corrigir dois desvios e repetir
regressão incremental, sem avançar a Test/fecho. O pacote1254 acima é histórico,
preservado byte a byte nas evidências; não representa os hashes da versão atual.

1 - **Antes de cada ANNOUNCE:** attempt já não recebe o instante do início do
    turno. Lê now(), aplica expiração e reavalia seleção/intenção quando a versão
    muda; valida conteúdo, Session e TTL com tempo atual depois de preparar a
    mensagem e novamente após reservar ticket/quota, imediatamente antes da porta.
    Uma intenção já expirada não chega à porta. Reservas sem chamada/future são
    descartadas com sinalização; uma tentativa já debitada não recebe refund de
    quota. Tickets com trabalho efetivamente iniciado continuam sujeitos à
    terminação física de callback+continuação. Não alterar seq/TTL para enviar.
    O fim do turno também expira/publica/agenda usando um instante novo, sem
    voltar a publicar o instante antigo depois de um envio que demorou.
2 - **visibleAt puro:** remainingNanos protege os metadados enquanto há algum
    candidato válido; não representa o prazo do upstream selecionado. Como Entry
    não transporta o conjunto de candidatos nem o prazo específico da seleção,
    não é possível recalcular uma alternativa a partir deste snapshot. Mantém-se
    a API: ao avançar o instante de uma entrada remota, projetar UNAVAILABLE com
    metric/knownUpstream ausentes. Aos mesmos capturedAtNanos conserva a seleção;
    ofertas locais sem TTL conservam custo0. Isto é deliberadamente conservador,
    inclusive quando a seleção poderia ainda estar viva. Não consulta nem altera
    Root, relógio, catálogo original ou snapshots anteriores; não estende prazo.
3 - **Sem ampliação de âmbito:** só RoutingService.java, StreamCatalog.java e
    RoutingServiceTest.java mudaram nesta revisão; os outros três caminhos e
    todos os testes anteriores desta implementação estão preservados. A opção de
    projeção sem custo foi expressamente autorizada no pedido, sem nova ADR/Q.

**Reprodução determinística:** red-13,39 testes,2 falhas/0 erros/0 skips. O
primeiro teste avança8s dentro da porta do primeiro ANNOUNCE, antes do segundo
envio do mesmo turno: antes saíam2 anúncios, após a correção só1, sem segundo
consumo de quota/ticket para a intenção que já está expirada. Mantém3 sessões;
a projeção final do turno já tem instante8s e catálogo vazio. O segundo teste
captura A selecionado com custo110 expirando aos8s e B válido até9s; visibleAt(8s)
conservava o custo/upstream de A. Agora preserva metadados por mais1s com ambos
os campos ausentes; projeção encadeada reduz prazo e desaparece aos9s, sem mudar
o snapshot original nem o relógio do domínio.

**Resultados preservados:**

- [red-13](../../../experiments/construction/006-rotas/implement-revision-ttl/red-13/result.json):39 testes,2 falhas reais.
- [green-14](../../../experiments/construction/006-rotas/implement-revision-ttl/green-14/result.json):39/0/0/0 após correção inicial.
- [Regressão final15](../../../experiments/construction/006-rotas/implement-revision-ttl/incremental-regression-15/result.json):
  `mvn -B -ntp '-Dtest=*' test`, JDK21, **1256/0/0/0 em16 XML**,45 testes novos e1211
  anteriores; fonte final identificada por hashes, sem reutilizar XML antigos.
  [Log](../../../experiments/construction/006-rotas/implement-revision-ttl/incremental-regression-15/maven.log).

D08/D22 reforçados sem alterar os32 IDs de oráculo. D32 permanece parcial;
clean verify, novo JAR, manual humano, revisão final Denji e integração pendentes.
Sem Test/fecho/commits/push ou outro IMP. Implement continua para revisão.


Pacote atual: [diff desta correção](../../../experiments/construction/006-rotas/implement-revision-ttl/source.diff),
[fonte acumulada contra a base](../../../experiments/construction/006-rotas/implement-revision-ttl/cumulative-source.diff),
[verificações/hashes](../../../experiments/construction/006-rotas/implement-revision-ttl/results.json)
e [V-ADD](../../../experiments/construction/006-rotas/implement-revision-ttl/v-add.md).
Listas geradas em target continuam separadas e não fazem parte da fonte aprovada.


## Aprovação posterior de Implement

Registo 2026-11-14T15:08:34Z. Utilizador aprovou Implement e as correções TTL/visibleAt,
autorizando apenas Test. Estado anterior de revisão é histórico. D32/repetição
humana/revisão final Denji/integração continuam pendentes; sem fecho ou commits.
