# PHASE-03 — fecho de monitorização e descoberta de rotas

Registo 2026-11-15T16:36:52Z. **DONE**,por autorização explícita do utilizador,após Test e
revisão final de IMP-010 por Luis,substituindo Fillmore. Não se inventam revisões
dos restantes membros. Versão revista integrada em main 3349d575ae38c772b38496cc08bc1d44ded764ff.
[Prova de integração](../../experiments/construction/022-rotas/closure/integration-proof.json).

## Critérios e evidência

- TASK-010/V-010: RTT,EWMA,custos e falhas por sessão;
  [fecho IMP-007](../../memory-bank/bolts/005-rotas/closure-walkthrough.md).
- TASK-011/V-011: candidatos/expiração/épocas/limites e snapshots imutáveis;
  [fecho IMP-008](../../memory-bank/bolts/021-rotas/closure-walkthrough.md).
- TASK-012/V-012: quatro streams/duas origens,catálogo,difusão,ciclos e alternativas;
  [fecho IMP-009](../../memory-bank/bolts/006-rotas/closure-walkthrough.md).
- TASK-013/V-013: traces por salto,correlação,prazos,TTL,erros e sem subscrição;
  [fecho IMP-010](../../memory-bank/bolts/022-rotas/closure-walkthrough.md).
- Regressão atual preservada:1296/0/0/0,17XML;
  [resultados](../../experiments/construction/022-rotas/test-build-01/result.json).
  RoutingServiceTest.d05TwoOriginsFourStreamsAndWithdrawalIndependence verifica
  quatro streams de duas origens; d02D03DiamondBetterArrivalAndAlternativeDespiteCacheHit
  verifica alternativa e melhoria tardia140→130; d04CyclicDiamondConvergesAndFixedGenerationStopsFlooding
  verifica ausência de ciclos/flooding ilimitado com tempo/transporte controlados.
- Demonstração humana integrada com TCP real e dois streams,sem bootstrap durante
  a falha: trace por ambos os relays e alternativa após SIGKILL de R1,UUID inválido,
  UNKNOWN_STREAM,NO_ROUTE e shutdown. [Report Luis34 checks](../../experiments/construction/022-rotas/manual-runs/20261115T160738Z-me3p9b08/report.json).
  Não se transforma o ensaio de dois streams numa alegação de quatro streams
  humanas/simultâneas. A prova de quatro é automatizada/controlada,acima.

## Limites e retoma

NO_ROUTE transitórios com peers ACTIVE,retiradas127/anúncios128 e falha histórica
m13 de shutdown preservados e aceites na revisão. Sem causa exata inventada,
sem disponibilidade contínua,vídeo,GUI,CORE ou desempenho alegados.
RF-010 eRP-008 continuam parciais; RP-004 concluído.

Próximo bolt:007-arvores/IMP-011,planned e não iniciado. PHASE-04 permanece TODO.
O controlo já permite overlay,reparação,métricas,catálogo,rotas e trace; a
ativação de árvores e entrega de vídeo continuam nas fases seguintes.
