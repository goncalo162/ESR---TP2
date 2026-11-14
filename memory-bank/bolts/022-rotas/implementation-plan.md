---
stage: plan
bolt: "022-rotas"
implementation_task: "IMP-010"
created: "2026-11-14T16:16:15Z"
status: "approved"
---
# IMP-010 — Plan: diagnosticar percursos pela CLI

✅ Plan aprovado em 2026-11-14T16:24:54Z. ⏳ Implement apenas interfaces/testes vermelhos. [ ] Test não iniciado.
Tipo Simple: Plan → Implement → Test, com aprovação em cada checkpoint.
Base operacional/HEAD: `9144707eb5622d9da43b3b07354bc838c2797ef8`, main;
árvore limpa na entrada. Nenhum checkout/reset, alteração Java ou teste Java.

## 1. Fontes, dependências e critérios

[ADD](../../../add.md), secções2/5/6/8–12, em particular IMP-010;
[specs](../../../specs.md), REQ-010/019/023 e INT-001/002;
[intent](../../intents/001-ott-streaming/requirements.md),
[unidade](../../intents/001-ott-streaming/units/003-rotas/unit-brief.md),
[história](../../intents/001-ott-streaming/units/003-rotas/stories/004-diagnosticar-percursos-pela-tabela-de-rotas.md),
[TASK-013](../../../docs/tasks/PHASE-03/TASK-013.md),
[contratos07](../../../docs/07-API-AND-INTERFACES.md), secções3–7 e adendas v2/IMP-009,
[testes09](../../../docs/09-TESTING-STRATEGY.md),
[DoD14](../../../docs/14-DEFINITION-OF-DONE.md) e
[progresso15](../../../docs/15-PROJECT-PROGRESS.md).
Enunciado → specs → intent → documentação → código; o ADD rege decisões derivadas.

✅ Dependências completas e presentes na base: IMP-006/004-overlay,
IMP-009/006-rotas e IMP-005/020-overlay; unidade002-overlay complete.
IMP-009 integrou routing em `b7d74be5d9bad9fe7f598f29fa17188037647cf2`,
com fecho em9144707. Fechos anteriores são evidência de dependência, não
resultados de execução deste Plan. Unidade003-rotas/PHASE-03 continuam in-progress.

Critérios: consultar a seleção real em cada salto; terminar falhas em prazo;
mostrar NO_ROUTE e TTL explicitamente; não criar procura/subscrições/vídeo;
V-013 executado futuramente, com regressão, manual humano, revisão final e integração.
Responsável atribuído Denji; revisor atribuído Fillmore. Atribuição não é revisão feita.
Sem gates externos adicionais.

## 2. Composição real e allowlist

1. `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`:
   existente, não vazio. Acrescentar operação, tipos internos de diagnóstico,
   contextos limitados, handlers15/16/ERROR relacionado15, prazos e cleanup.
2. `Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java`:
   existente. Comandos trace UUID, routes/catalog/metrics exigidos por TASK-013,
   ajuda, leitura de snapshots e apresentação limitada assíncrona ao owner.
3. `Projeto/src/test/java/pt/uminho/esr/ott/routing/RouteTraceTest.java`:
   novo. Oráculos determinísticos, TCP real e subprocessos da CLI pública;
   helpers/tipos de teste aninhados, sem alterar testes históricos.

Esta é a allowlist do ADD, não autorização para implementar agora.
**Lacuna Q-C-32:** OverlayNode tem routing privado e apenas routingSnapshot();
não existe porta pública para iniciar operação no serviço. O construtor que
injeta RoutingService.Factory é package-private e o construtor público cria o
serviço internamente. Snapshot não contém owner/port nem permite enviar.
Recomenda-se, apenas se aprovado, um quarto caminho:
`Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java`,
com uma delegação pública mínima e resposta NOT_RUNNING antes de start/após stop.
Testar a delegação no próprio RouteTraceTest e pela CLI real. Não expor manager,
owner, mapas internos ou serviço mutável; não criar registry, reflexão, outro
RoutingService, transporte, executor de estado ou comando adicional.
Sem Q-C-32 decidida, a composição/integração fica pendente; não é possível
apresentar a entrega operacional completa dentro dos três caminhos por inferência.
ADD/contratos não alterados para fingir esta aprovação.

## 3. APIs propostas, ainda sem Java

Dentro de RoutingService, nomes de detalhe propostos:

- `public TraceTicket requestTrace(UUID streamId)`: não bloqueia; valida argumento,
  reserva admissão local antes de oferecer evento ao owner e devolve ticket limitado.
- `TraceTicket.traceId()`, `TraceTicket.result(): Optional<TraceOutcome>` e
  `TraceTicket.cancel()`: consulta de valor imutável e pedido de cancelamento,
  sem continuations arbitrárias executadas no owner. Resultado publicado uma vez.
- `TraceOutcome`: streamId, traceId, Optional serverEpoch, estado local,
  Optional ErrorCode, detalhe limitado, visited imutável e duração monotónica local.
  Estados de diagnóstico locais com nomes explícitos, não códigos wire novos:
  SUCCESS, ERROR, TIMEOUT, CANCELLED, CLOSED, RESOURCE_LIMIT, NOT_RUNNING.
- Porta proposta no nó, condicionada a Q-C-32:
  `public RoutingService.TraceTicket requestTrace(UUID streamId)`;
  serviço ainda ausente devolve ticket terminal sem alocar helper.
- RunningService (interface interna de OttApplication) recebe defaults de
  snapshots de routing/métricas e requestTrace; adaptador overlay delega para node.
  BOOTSTRAP devolve NOT_APPLICABLE/NOT_RUNNING, nunca envia15.

Manter construtores/APIs, RoutingService.Factory/ControlPort/Scheduler, snapshots
existentes, NeighborManager e send() de aplicação. A extensão não requer novas
classes top-level ou mudanças no codec/config/monitor/tabela/transporte.
Records/listas defensivos; enums Java existentes e números wire preservados.

## 4. Tabela de cada salto e sentido dos percursos

No owner, obter now monotónico, aplicar a projeção de expiração existente da
tabela e consultar `root.snapshot(streamId)` antes de escolher. Não encaminhar
pelo antigo snapshot volátil de UI nem apenas inverter o path do anúncio.
A projeção pode retirar candidatos expirados, mas trace não insere anúncios,
renova TTL/seq/epoch, altera custos ou seleciona um upstream de dados ativo.
Preservar a publicação/flooding que o ciclo normal faria por expiração.

Fonte válida: route.localSource da época pedida e oferta local disponível;
terminar com sucesso local sem rede se o iniciador for a fonte.
Remoto: usar selectedCandidate/knownUpstream da tabela, com custo conhecido,
candidato ainda válido e Session atual completa. INACTIVE é elegível para
trace; UNAVAILABLE não. Alternatives são mostradas em routes, mas o pedido
segue só a seleção atual: não difundir por todos os candidatos nem fazer retry
silencioso. Dois testes mudam a seleção entre pedidos e demonstram ambos os caminhos.
Mudança de tabela durante o percurso pode produzir observações de instantes
locais diferentes; não prometer snapshot global nem impor o path anunciado inteiro.

Visited tem ordem iniciador→origem. Pedido inicial acrescenta o iniciador;
cada recetor valida prefixo sem repetições, último ID igual ao sender imediato,
IDs válidos, ausência do próprio ID e limite antes de copiar/acrescentar-se.
Guardar o prefixo original e a Session de jusante; encaminhar uma única vez à
Session de montante selecionada. Exemplo: C→R2→S produz [C,R2,S]; retorno de
TRACE_RESULT passa S→R2→C mantendo a lista [C,R2,S], não a invertendo no payload.
Logs locais identificam direction=request/response, nó, instância, stream,
traceId/requestId, sessão e custo observado em µs; custo não existe no payload16
nem permite inventar soma dos custos remotos ou atraso unidirecional.

## 5. Correlação e corridas

Proposta Q-C-33: requestId=traceId novo na origem e preservado por todos os saltos;
envelope senderId/senderInstance refeito pelo emissor imediato. Guardar stream,
época, traceId/requestId, prefixo, Session completa de entrada (ou LOCAL),
Session completa de saída, deadline local e token único da admissão.
Session=(nodeId,instanceId,connectionId); identidade do envelope e sessão têm de
coincidir. Resultado16 exige requestId/traceId/stream e Session de montante
esperada, prefixo guardado, ausência de ciclos e último ID igual ao serverId
esperado. ERROR só é consumido quando relatedType=15 e requestId/Session
correspondem. Restantes ERROR/eventos continuam observáveis por handlers anteriores.

Reservar/publicar contexto antes de chamar ControlPort.send(): uma resposta
válida pode chegar antes da continuation ACCEPTED. A resposta constitui decisão
remota e conclui uma vez; callback tardio só liberta reserva física, nunca
reescreve terminal nem contexto de outro pedido. Falha local anterior ao resultado
termina o contexto; resposta subsequente é ignorada. Se ambos chegam antes do
owner tratar, aplicar a ordem serial documentada no teste; nenhum falso sucesso
é inferido só de ACCEPTED. Exceção/CLOSED/OVERFLOW/INVALID_MESSAGE não é ACK.

Callbacks/timers apenas publicam resultado físico limitado/coalescem wake;
nenhuma mutação de rede fora do owner e nenhuma chamada a código cliente nele.
NeighborDown retira operações da Session exata; Up da mesma instanceId com nova
connectionId não herda contextos. Token impede resultado antigo de afetar nova
reserva; IDs terminais não são reutilizados. Epoch diferente cancela operação
incompatível; não reenviar automaticamente pela nova geração.
Duplicado em contexto vivo não renova deadline nem cria segundo envio; colisão
incompatível é rejeitada. Estado terminal protegido só até deadline/cap existente,
sem dedup infinito; qualquer replay posterior continua sujeito a TTL e limites.

## 6. Erros, TTL e prazo — proposta Q-C-33

Não existe código wire TTL_EXCEEDED. Propor usar ERROR TIMEOUT(10), relatedType15,
detail=TTL_EXHAUSTED, e mostrar razão TTL distinta de timeout de espera na CLI.
Visited wire admite lista até32; propor TTL inicial32 como orçamento de nós,
consumindo1 ao admitir cada nó, incluindo o iniciador. Exemplo direto C→S:
saída C leva31; S valida ttl>0, acrescenta-se e termina. TTL=0 termina antes de
acrescentar/encaminhar, mesmo se o recetor for fonte. Fonte no32.º nó pode
responder; um33.º nunca é admitido. Não criar flags/opções CLI para TTL neste IMP.
Listas32 recebidas que exigem append excedem o limite e não são copiadas/reenviadas.

- UUID sintaticamente inválido: COMMAND_ERROR local, sem ticket/rede.
- Stream nunca conhecida: UNKNOWN_STREAM(5); binding conhecido sem candidato
  atual/localOffer: NO_ROUTE(6), incluindo falta de RTT ou expiração.
- Época divergente: PATH_CHANGED(7); ciclo: LOOP_DETECTED(8).
- Prazo: TIMEOUT(10); saturação: RESOURCE_LIMIT(11).
- Envelope/contexto inseguro: ignorar ou erro apenas com identidade segura;
  nunca responder a ERROR nem criar erro recursivo.
- Falha de envio/NeighborDown: resultado local explícito NO_ROUTE ou CLOSED
  conforme origem/cleanup; tentar ERROR seguro a jusante sem criar retries.

Cada salto guarda a Session de entrada e devolve sucesso/ERROR por ela, não
consulta rotas para devolver resposta. Se essa Session já não existe, não envia
à reconectada; origem termina no seu próprio prazo. Não prometer erro entregue
se transporte/fila falhar. Falta de admissão para um ERROR permite descartar a
resposta remota: o pedido original continua coberto pelo prazo local do iniciador.

Propor prazo de3000ms por contexto e total no iniciador, desde a admissão local
antes da fila até à decisão; sem renovação por duplicado/retry/resultado parcial.
Sem relógio partilhado, deadlines locais por salto não prometem deadline global
remoto, mas o ticket inicial termina até ao seu prazo quando owner progride.
Verificar tempo antes de cada envio e de aceitar resultado; t>=deadline vence.
Se evento de admissão já expirou, não enviar. Owner bloqueado/saturado é falha
operacional, não garantia de tempo real: preservar owner.offer que é fatal por
fila cheia (Q-C-31). Não reinterpretar como backpressure recuperável.
Novos valores locais não são requisitos do PDF nem propriedades que mudem wire.

## 7. Limites físicos e shutdown — proposta Q-C-33

E=owner.capacity. Contextos diagnósticos globais T=min(E,64), incluindo terminais
retidos/tombstones, máximos4 locais e4 por Session de entrada, sem prealocação
máxima. Reservar local antes de offer, não alocar uma future/timer por tentativa
recusada. CLI mantém até4 tickets, com polling no seu ciclo existente; sem espera
por ACK nem threads/executores por pedido. Busy/erro imediato é valor terminal
limitado, sem retenção pelo serviço. Não evictar contexto protegido para admitir outro.

Reservas físicas de sends trace S=min(E,64), separadas e explicitamente limitadas,
sem reduzir/alterar o limite aprovado das admissões9/10. Cada request/result/error
reserva antes de port.send; callback ainda vivo mantém reserva mesmo após timeout
lógico ou consumo do resultado. No máximo2 tickets de envio por contexto para
corrida request/resposta; saturação não cria fila ilimitada de erros. Falta de
reserva deixa no próprio contexto no máximo1 intenção de resposta até deadline;
sem retry de pedido. Erros sem contexto só usam reserva livre, senão descarte.
Tratar envio e liberação física sem que uma admissão antiga liberte a nova.
Até16 ações de diagnóstico por passagem, cursor para progresso justo; não
alterar quotas/fairness/caps de flooding aceites no IMP-009.

Timers: integrar o próximo deadline no alarme existente de RoutingService;
um scheduler, no máximo2 jobs físicos incluindo job em execução/cancelamento,
um wake coalescido e fila owner E já limitada. Não criar um timer por contexto;
S reservas com no máximoS callbacks vivos e dados imutáveis de tamanho limitado.
Memória O(T×32 IDs + S×frame limitado), mais índices de tamanhoT/S e4 tickets
CLI. Não reter snapshots de tabelas inteiras por pedido; só sessão/prefixo/custo
observado/epoch. Não alegar máximo de bytes JVM medido neste Plan.

requestClose existente impede novas admissões; no owner terminar/cancelar tickets,
limpar contextos/intentos e cancelar alarme; hooks onStop cobrem recusa de owner.
Cancelamento não prova terminação de callback físico. awaitTermination existente
passa a incluir as reservas trace até callback/continuação realmente terminados,
no mesmo budget global restante e sem novo prazo por pedido, peer ou helper.
OverlayNode já solicita routing antes de parar manager/owner e espera routing no
restante: conservar esta ordem. Nenhum join no owner. CLI quit/EOF/falha cancela
tickets e usa cleanup e resultado terminal autoritativo do IMP-005; não voltar a
anunciar código de saída final antes do writer terminar.

## 8. CLI e snapshots sem efeitos de procura

Gramática proposta: `trace UUID` (um UUID canónico, sem argumentos extra),
`routes`, `catalog`, `metrics`; preservar neighbors/recovery/quit/ajuda e restantes
comandos ainda não implementados. TRACE_STARTED com ID seguido exatamente de
um TRACE_RESULT ou TRACE_ERROR terminal no ticket consumido. Incluir stream,
traceId, visited/código/razão e duração local; não anunciar sucesso no enqueue.

Snapshots de routing e monitoring já expostos pelo nó, imutáveis. routes mostra
origem, época, seleção, candidatos/alternativas, custoµs, validade observada,
estado/revisão; catalog mostra metadados e validade; metrics mostra Session,
EWMA/custo/idade e estado monitor. Não renovar candidato por leitura.
Rotular capturedAt/idade e observar validade local: não apresentar snapshot antigo
como seleção atual, nem chamar root fora do owner. Usar projeção visibleAt do
catálogo conservadora; routes rotula snapshot e candidato expirado, sem fingir
reseleção global. Clock da CLI é do mesmo processo; testes injetam relógio comum.

Saída até4096 caracteres/linha; título/strings escapados para não injetar linhas
ou comandos. Uma listagem incremental por vez, um cursor e uma referência ao
snapshot imutável; candidato por linha, sem concatenar16384 rotas/63 alternativas.
Até16 linhas/iteração, apenas enquanto há espaço na fila de saída; manter
COMMAND_OVERFLOW/OUTPUT_ERROR e política fatal de saturação histórica para eventos.
Nova listagem quando existe uma devolve BUSY; quit/falha descarta cursor. Polling
intercalado mantém comandos responsivos; não bloquear callbacks nem o owner.

Trace apenas lê seleção e cria contextos diagnósticos. Proibidos ACTIVATE11,
ACK12, DEACTIVATE13, RENEW14 como efeito de trace, registo de consumidor,
downstream, active/pending upstream, sockets UDP, leitura de media ou emissão
de vídeo. Anúncios/sondas/descoberta normais continuam independentes.

## 9. V-013: mapa de oráculos propostos, todos NÃO EXECUTADOS

1. T01 — Fonte local retorna [S], custo0, nenhum15/11–14/UDP.
2. T02 — Grafo C–R1/R2–S: custos escolhidos levam [C,R1,S]; nova seleção
   pela alternativa leva [C,R2,S]; tabela de cada relay pode divergir do path
   originalmente anunciado, demonstrando consulta real por salto.
3. T03 — Retorno inverso por Sessions guardadas com visited invariável; não
   consultar tabela modificada para rotear resposta.
4. T04 — UNKNOWN_STREAM e NO_ROUTE sem RTT, expirado ou retirado; sem envio15 indevido.
5. T05 — TTL0,1 e32; origem no último salto, path32/33, repetição e sender incorreto;
   TTL explícito separado do timeout sem novos códigos/bytes.
6. T06 — Clock/scheduler falsos: admissão em fila já expirada, t=deadline−1/t=deadline,
   resultado tardio e progresso da CLI sem sleeps de parede como oráculo.
7. T07 — Resultado válido antes de ACCEPTED, conclusão síncrona/reentrante,
   exceção/null, CLOSED/OVERFLOW/INVALID_MESSAGE; terminal único sem fuga física.
8. T08 — Troca connectionId com instanceId igual; nova instanceId; Down/resultado/
   callback antigo não altera contexto novo nem envia à Session substituta.
9. T09 — traceId/requestId/stream/epoch/prefixo/sender incompatíveis e resultado
   da Session errada; ERROR relatedType alheio preservado, sem erro sobre erro.
10. T10 — Duplicado/cancelamento/timeout/fecho concorrentes, sem prazo renovado,
    segunda resposta ou retry automático pela alternativa.
11. T11 — T/S e quotas local/Session nos limites e limite+1; contexto protegido,
    callback não resolvido após timeout conserva reserva física; capacidade
    não é reposta prematuramente, intenção/erro limitado e falha isolada.
12. T12 — Até2 jobs físicos, alarme compartilhado, wake coalescido e owner cheio
    fatal/onStop, sem tentar mudar a semântica aprovada em Q-C-31.
13. T13 — Eventos9/10/7/8/18/19/ERROR preservados, flooding/quotas/TTL/Q-C-26–31
    intactos; nenhuma variação de procura/active/pending/downstream ou envio11–14.
14. T14 — routes/catalog/metrics refletem snapshots, custo/idade/unavailable
    honesto, linhas/título limitados e defensivos; listagem grande cede a quit.
15. T15 — Processo CLI real (ProcessBuilder, sem acesso a interfaces package-private):
    perfis temporários validados, bootstrap/servidor/relays/cliente e porta TCP
    efémera/reservada com verificação de bind. Esperar readiness/convergência por
    evidência, enviar trace UUID pelo stdin, exigir visited esperado e erros
    explícitos, preservar stdout/stderr e exit. Alternativa real após retirar
    relay/alterar seleção de forma observável; não alegar teste de custos exatos
    com RTT de parede não controlado.
16. T16 — TCP real multi-hop, reconexão/EOF no meio; request/result e retorno
    pertencem às Sessions atuais e origem termina em prazo. UDP de dados ausente
    comprovado por observador de controlo e captura/socket sentinela nas portas
    de dados do perfil, separando controlo/perfis de media ainda não lida.
17. T17 — Shutdown durante admissão/espera/resposta e callback bloqueado:
    falha física explícita até callback libertado, budget único sem multiplicação;
    no nominal todos os processos/threads/helpers/sockets terminam, portas libertas.
18. T18 — Regressão completa futura `mvn -B -ntp clean verify`, XML/log/JAR/hash
    novos; comparação literal de testes históricos/bytes wire/UDP e de caminhos
    autorizados. Não usar1256/1211/1176 históricos como resultados atuais.

Implement deverá começar com interfaces e testes significativos vermelhos se
assim autorizado no checkpoint; evoluir por partes sem reduzir testes existentes.
Test consolida execução V-013/T01–T18/regressão e prepara manual humano sobre
JAR/hash novos; repetir por pessoa identificada antes de aprovar Test, sem
atribuir execução ao utilizador por inferência. Revisão final de Fillmore e
integração da versão revista antes de DONE; sem contacto/commit/push inferidos.

## 10. Conformidade e checkpoint

ADR-004/022/024 diretamente aplicáveis; ADR-014/017/018/020/021/026 e033–035
preservadas. TCP exclusivamente v2,19 tipos1/2/4..20; código3 rejeitado,
framing37..65536, UTF-8 estrito, listas/path/caps existentes. UDP OTT1 continua
64/1200/1136. Nenhum campo novo nos payloads15/16/17; custos por salto só em log
local e snapshots, não em TRACE_RESULT. Sem runtime dependency, CI, linter,
JaCoCo ou percentagem de cobertura. Sem ADR nova/substituição por inferência.

[Q-C-32/33](../../../docs/construction-duvidas.md) precisam de decisão humana:
Q-C-32 sobre quarto caminho/porta; Q-C-33 sobre semântica TTL/prazos/recursos/API
propostos. Nenhuma proposta está implicitamente aprovada pelo início de Plan.
Se for identificado conflito inviável com decisão aceite, parar a parte afetada,
recolher prova e propor substituição segundo ADD11, sem implementar contorno.

⏳ Parar no checkpoint Plan. Plan por aprovar; Implement/Test não iniciados;
bolt/história em progresso, implemented não true e TASK-013 não DONE.
Validação desta etapa apenas documental, reportada em
[plan-validation.md](../../../experiments/construction/022-rotas/plan-validation.md).


## Decisão posterior — Plan aprovado

Registo 2026-11-14T16:24:54Z. Plan e opções1 de Q-C-32/33 aprovados explicitamente pelo utilizador nesta sessão.
Extensão exclusivamente a OverlayNode.java para delegação mínima requestTrace(UUID).
TTL inicial32 incluindo iniciador; TTL_EXHAUSTED via TIMEOUT10/relatedType15;
prazo local3000ms; correlação/retorno por Sessions completas guardadas;
TraceTicket consultável, limites físicos/CLI do Plan aprovados. Concretizações
locais aprovadas, não imposições do enunciado nem alteração de wire/hard caps.
Apenas interfaces/tipos e testes inicialmente vermelhos autorizados, quatro
ficheiros. Comportamentos, Test, staging, commits/push e fecho não autorizados.
V-013/T01–T18 integral, regressão atual, manual humano, revisão final Fillmore
e integração continuam pendentes; sem gates externos adicionais.
As expressões proposta/pendente acima conservam a redação do checkpoint original;
esta decisão resolve Q-C-32/33 e aprova a allowlist de quatro caminhos.
