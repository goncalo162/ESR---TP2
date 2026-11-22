---
stage: plan
bolt: 009-arvores
created: "2026-11-22T16:35:00Z"
---
# IMP-015 — Plan do segundo marco (árvores e adaptação)

Bolt `009-arvores` / TASK-018 / V-018, tipo simple-construction-bolt, tamanho S.
Demonstração, não código novo de produção: o deliverable é
`experiments/milestone-2/` com roteiro, auxiliar de execução e runs com
evidência. [ADD §IMP-015](../../../add.md#imp-015) ·
[TASK-018](../../../docs/tasks/PHASE-04/TASK-018.md) ·
[V-018](../../../docs/09-TESTING-STRATEGY.md) ·
[Roteiro](../../../experiments/milestone-2/README.md).

## Base e decisões aplicáveis

- Dependências verificadas: 022-rotas (IMP-010) **complete** e 024-arvores
  (IMP-014) **complete**; unidade 003-rotas satisfeita. A base comum é o
  checkout atual (branch Waheed, HEAD `15c74b1`).
- ADR-008: evidência incremental com oráculo próprio. ADR-023: MSP/reprodução
  com configurações e evidência identificadas. ADR-024: ADD como autoridade,
  incremento limitado a V-018. ADR-013: bootstrap só localização inicial.
- V-018 pede **reprodução independente da ativação/poda/failover usando logs e
  snapshots** e revisão do material contra os critérios de gestão de fluxos do
  PDF. A CLI não expõe subscrição (`play` = NOT_IMPLEMENTED), pelo que o
  consumidor é um auxiliar Java contra o JAR congelado, usando só API pública
  (`NeighborManager` + `RoutingService` + `BootstrapServer` em TCP loopback
  real, portas efémeras) — o mesmo padrão do S18 do `RouteSwitchTest` e do
  manual do 008-arvores.
- Métricas de custo controladas via `onMetrics` no fio do owner (RTT real já
  validado em V-010); a lógica de árvore, o protocolo v2 e o TCP são reais.

## Cenários planeados (detalhe no roteiro)

| Cenário | Topologia | Oráculos principais |
|---|---|---|
| C1 isolamento entre fluxos | S1,S2 (streams A,B) — R — C1..C4 | 4 subscrições SUCCESS em streams distintos; cancelar C1 não afeta C3(A)/C2/C4(B); S1 abaixo deixa A indisponível e preserva B |
| C2 procura agregada | S — R — C1,C2 (mesmo stream A) | exatamente 1 ACTIVATE R→S com 2 consumidores (tap de mensagens); um sai → outro continua; último sai → DEACTIVATE e poda |
| C3 falha e recuperação | losango S—R1/R2—C; variante 3b cadeia S—R1—C | morte de R1 → comuta para R2 com evento registado e ticket SUCCESS; sem alternativa → UNAVAILABLE explícito |
| C4 alteração de RTT | S—A/B—R (como S18) | custo B melhorado sustentado → ROUTE_SWITCH SUCCESS reason=CONFIRMED, upstream passa a B; ruído não comuta |

## Etapas seguintes

1. **implement** → `implementation-walkthrough.md`: construir
   `experiments/milestone-2/Milestone2.java` + `run-milestone2.py`, executar os
   4 cenários e guardar `runs/<run-id>/` (manifest, run.log, result.json, taps de
   mensagens). Regressão `mvn -B -ntp test` preservada.
2. **test** → `test-walkthrough.md`: consolidar V-018 com a run e rever o
   material contra os critérios do PDF; ligar evidência em docs/13/15.

## Gates e limites

- G-ASSESSMENTS pendente (data/conteúdo da 2.ª avaliação por confirmar).
- Sem vídeo/GUI/CORE/UDP; `play` permanece NOT_IMPLEMENTED; a morte de relay é
  fecho abrupto em JVM única, não SIGKILL de processo SO.
- Sem alteração de código de produção neste bolt; sem commits/push sem
  autorização; revisão humana atribuída (Waheed) não é presumida.

- [ ] Roteiro repetível com os 4 cenários e oráculos.
- [ ] Runs com logs, snapshots e tap de mensagens preservados.
- [ ] V-018 executada e evidência ligada no progresso.
