# IMP-010 — Implement para aprovação

Registo 2026-11-14T17:19:49Z. ✅ Plan/Q-C-32/33 aprovados. ⏳ Comportamentos de Implement
preparados nos quatro caminhos autorizados; Implement por aprovar. [ ] Test
não iniciado. Nenhum staging, commit, push, fecho ou outro IMP.

## Alterações e motivo

- [x] RoutingService.java: TraceTicket consultável, resultados imutáveis, handlers
  TRACE15/RESULT16/ERROR relacionado15, consulta da seleção em cada salto,
  contexts/sessões guardadas, TTL32/prazo3000ms, cancelamento e limites físicos.
- [x] OverlayNode.java: apenas requestTrace(UUID), null-check, delegação ao
  serviço existente ou ticket terminal NOT_RUNNING. Nenhum acesso novo ao owner,
  manager, mapa ou transporte; restantes métodos/lifecycle byte preservados.
- [x] OttApplication.java: trace UUID/routes/catalog/metrics/ajuda, adaptador de
  snapshots/tickets, até4 tickets e1 cursor incremental; logger de uma linha
  limitada e observação local de salto/custo, sem callback cliente no owner.
- [x] RouteTraceTest.java:40 oráculos automatizados com relógio/owner/scheduler/
  ControlPort falsos, processos CLI/TCP reais, falhas e terminação física.

Trace não cria consumidor/downstream, active/pending upstream, subscrição,
leitura de media, socket UDP ou vídeo; nenhum envio11–14 como efeito diagnóstico.
NeighborManager/send(), monitor/config/tabela/codec/transporte, testes históricos,
TCPv2/19 tipos/3 rejeitado/framing/caps e UDP OTT1 intactos por hashes.
Sem dependência runtime, CI, linter/JaCoCo ou limiar percentual novos.

## Substituições de linhas preexistentes, contra9144707

Cada uma conserva o comportamento anterior fora do critério indicado:

1. Ajuda CLI: acrescentar comandos de TASK-013/V-013(T14), sem mudar parse/config.
2. Ramo catalog/NOT_IMPLEMENTED: retirar só catalog do placeholder e tratá-lo
   com routes/metrics/trace(T14/T15); play continua placeholder.
3. Duas linhas de composição do observador: conservar EVENT e ERROR originais,
   acrescentar TRACE_HOP com snapshot/custo local(T02/T13/T15); observer mantém
   fila não bloqueante, sem acesso direto a estado mutável.
4. Console.log: oneLine substitui bounded puro para impedir injeção de linhas
   por títulos nos eventos, preservando fila/cap4096/truncation/erro fatal e
   shutdown do writer(Q-C-33/T14 e regressão do logger).
5. onMetrics: antes da atualização original, marcar sweep de diagnósticos para
   observar mudança de epoch/sessão/validade(T08/T13), sem mudar custo ou RTT.
6. drive: inserir passagem limitada de diagnóstico entre publish e trabalho
   de flooding original(T06/T11/T12); quotas e scan de anúncios conservados.
7. awaitTermination: esperar também traceSends, no mesmo prazo global restante,
   pois timeout lógico não prova callback físico concluído(T17).

OverlayNode não remove/substitui linhas herdadas. Stubs do checkpoint anterior
substituídos só nas APIs aprovadas; TraceOutcome passou a validar limites/UTF-8
antes da cópia. Todo o restante é acréscimo. As8 linhas removidas da base são
as5 linhas CLI e3 de RoutingService enumeradas; testes históricos não alterados.

## Recursos, correlação e política de falhas

T=min(E,64) inclui reservas locais antes da fila e contextos/terminais protegidos;
máximo4 locais/4 por Session. S=min(E,64) envia diagnósticos em reservas adicionais,
sem reduzir o orçamento de9/10; até2 tickets por contexto e1 resposta pendente.
Reserva só liberta após continuation e callback terminarem; liberta imediatamente
se falhar a validação antes de chamar o transporte, quando não existe callback.
Passagens limitadas a16, wake coalescido, alarme do scheduler existente/até2 jobs;
nenhum timer/thread/executor por pedido. Saturação local explícita; resposta ERROR
sem reserva pode ser descartada, preservando prazo do iniciador. OVERFLOW mantém
semântica real de fecho de ligação; owner.offer cheio continua fatal(Q-C-31).

Tempo na fila conta para3000ms; t>=deadline vence. IDs/prefixo/época/Session
(nodeId,instanceId,connectionId) correlacionados; resultado antes de ACCEPTED
aceite, callback tardio não substitui terminal. Resposta usa Session guardada,
não a tabela atual ou uma conexão substituta. TTL_EXHAUSTED usa TIMEOUT10/
relatedType15; sem código/campo wire novo. Custos só em logs/snapshots locais.
Shutdown conserva owner/manager e budget global; jobs/callbacks ativos não são
anunciados terminados. Não se promete tempo real sob falha/bloqueio do owner.

## Resultados atuais

Regressão final em Implement: `mvn -B -ntp -f Projeto/pom.xml -Dott.trace.evidence=... test`.
**1296 testes:1256 históricos +40 novos;0 falhas/0 erros/0 ignorados,exit0,17 XML
frescos; fontes iguais aos hashes do início/fim.** JDK21.0.13/Maven3.9.9.
Não é clean verify, nova construção de JAR ou aprovação da etapa Test.
[Resultado final](../../../experiments/construction/022-rotas/implement-behavior/regression-03/result.json)
e [log](../../../experiments/construction/022-rotas/implement-behavior/regression-03/maven.log).

A primeira regressão1291/0/0/0 precede os últimos oráculos/correções. A segunda,
já1296, teve1 falha no teste histórico m13PhysicalIoJoinsAreProvedBeforeJvmExitWithBlockedOutput:
JVM não terminou no prazo6s; stderr vazio e sem resultado final escrito antes do
cleanup do teste. Mesmo teste inalterado passou isolado e na regressão final;
causa do timeout não estabelecida. Não alterar teste/prazo nem apagar a falha.
[Execução falhada](../../../experiments/construction/022-rotas/implement-behavior/regression-02/result.json),
[log da repetição isolada](../../../experiments/construction/022-rotas/implement-behavior/blocked-output-recheck.log).

## Progressão e evidência dos oráculos

- Interfaces red13:2pass/11fail. incremental01:13/1fail(a ajuda ainda ausente),
  incremental02:13/0, incremental03:28/0. incremental04:29/1erro por socket
  bloqueado na sandbox, não falha funcional; repetição autorizada05:29/0.
- incremental06:35/0; teste de clock red:1fail(exceção escapava da admissão),
  red02:1fail(reserva sem callback impedia awaitTermination), green:2/0.
  Correções limitadas à captura da falha/rollback de reserva antes do port.
- incremental07:37/0, incluindo TCP pendente/origem suspensa e logger; últimos
 3 oráculos de callbacks/reconexão:3/0. Regressão final executa todos40 e1256 antigos.
- Cinco JVMs reais BOOT/S/R1/R2/C: trace e alternativa após SIGKILL; UUID
  desconhecido e gramática inválida explícitos; shutdown normal dos sobreviventes,
  processo morto recolhido, portas TCP/UDP libertas. Outro ensaio suspende S,
  observa pedido num relay, mata esse relay, recebe NO_ROUTE no iniciador e
  retoma S. Terminação e códigos de saída reais nos logs por processo
  do ensaio automatizado; não são manual humano nem prova CORE.
- Socket sentinela UDP não recebeu datagramas durante o intervalo observado;
  ausência de11–14 nos observadores/logs. Não se alega captura contínua, vídeo,
  FPS, latência medida ou avaliação de desempenho.
- SIGKILL demonstra NeighborDown/TRANSPORT. Não inferir FIN versus RST desses
  logs; PEER_EOF é assertado pelo TransportTest histórico reexecutado. Fakes
  comprovam a ligação desse Down à conclusão do trace e isolamento das sessões.
- CLI com256 entradas: cursor cede a quit, segunda listagem BUSY, títulos
  escapados e linhas<=4096. Sem alterações aos testes históricos para obter verde.

[Mapa T01–T18](../../../experiments/construction/022-rotas/implement-behavior/oracle-map.json).
T01–T17 têm prova automatizada incremental combinada; T18 tem regressão atual,
mas clean verify/XML/JAR/hash novos e consolidação formal V-013 pertencem ao
checkpoint Test futuro. Manual humano antes de aprovar Test, revisão final
atribuída a Fillmore e integração continuam obrigatórios. Sem gate externo novo.

## Diff, preservação e checkpoint

[Diff Java](../../../experiments/construction/022-rotas/implement-behavior/source-vs-base.diff),
[numstat Java](../../../experiments/construction/022-rotas/implement-behavior/source-vs-base.numstat),
[acumulado sem target/evidência](../../../experiments/construction/022-rotas/implement-behavior/cumulative-review.numstat),
[target gerado separado](../../../experiments/construction/022-rotas/implement-behavior/target-generated.numstat),
[preservação](../../../experiments/construction/022-rotas/implement-behavior/preservation.json) e
[V-ADD](../../../experiments/construction/022-rotas/implement-behavior/v-add.md).
Os acumulados brutos do checkpoint anterior conservam as listas tracked de
compilação; o acumulado atual de revisão exclui-as explicitamente. target/ não
é entrega e não foi staged. Evidência relevante em experiments/construction/022-rotas/.

**Parar em Implement para aprovação.** Plan é a única etapa concluída.
Test não iniciado/aprovado; bolt/história/TASK/unidade/fase permanecem abertos,
sem implemented=true, DONE, commits, push ou início de outro incremento.


## Aprovação posterior de Implement — Test autorizado

Registo 2026-11-14T17:45:23Z. Utilizador aprovou Implement e autorizou Test, sem Java/staging/
commits/push/fecho. [Relatório atual](test-walkthrough.md); o texto acima preserva
o checkpoint histórico. Test continua por aprovar até manual humano; revisão
final Fillmore e integração posteriores.
