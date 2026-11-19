---
id: "023-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "in-progress"
stories: ["002-podar-ramos-renovar-leases-e-remover-procura-abandonada"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-19T21:48:56Z"
started: "2026-11-18T15:40:16Z"
completed: null
current_stage: "test"
stages_completed: [{"name": "model", "completed": "2026-11-18T15:58:39Z", "artifact": "ddd-01-domain-model.md"}, {"name": "design", "completed": "2026-11-18T16:12:04Z", "artifact": "ddd-02-technical-design.md"}, {"name": "adr", "completed": "2026-11-18T16:41:21Z", "artifact": "adr-review.md"}, {"name": "implement", "completed": "2026-11-18T17:46:39Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-18T18:00:35Z", "artifact": "ddd-03-test-report.md"}]
requires_bolts: ["007-arvores", "022-rotas"]
enables_bolts: ["008-arvores"]
requires_units: ["003-rotas"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-015"]
source_task_order: ["TASK-015"]
implementation_tasks: ["IMP-012"]
size: "M"
validation_ids: ["V-015"]
test_ids: ["V-015"]
external_gates: []
adr_ids: ["ADR-005", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-003", "REQ-002", "REQ-005", "REQ-007", "REQ-009", "REQ-011", "REQ-017"]
architecture_source: "add.md"
---
# Bolt 023-arvores — Renovar e podar subscrições

## Fonte normativa e âmbito

Ler [IMP-012](../../../add.md#imp-012) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-015](../../../docs/tasks/PHASE-04/TASK-015.md).

## História, dependências e gates

- [ ] [002-podar-ramos-renovar-leases-e-remover-procura-abandonada](../../intents/001-ott-streaming/units/004-arvores/stories/002-podar-ramos-renovar-leases-e-remover-procura-abandonada.md).
- Dependências: [007-arvores](../007-arvores/bolt.md), [022-rotas](../022-rotas/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-012 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-012 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-012 e as ADR do ADD.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-012 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-012 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Vídeo só permanece autorizado enquanto há procura efetiva.
- [ ] Ramos órfãos desaparecem por timeout.
- [ ] Outros consumidores e fluxos mantêm-se ativos.
- [ ] Validação V-015 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-015, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-012](../../../add.md#imp-012).
- Prova futura: `experiments/construction/023-arvores/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Checkpoint Design

Model aprovado pelo utilizador, Q-C-40–42 reconfirmadas;
[Design preparado](ddd-02-technical-design.md) para aprovação. Somente Model
em stages_completed. Sem ADR/Implement/Test, Java/testes Java, staging, commits
ou push. Fecho exige V-015/L01–L24, regressão, manual humano, revisão Denji e
integração; nenhum gate externo adicional.

## Checkpoint ADR

Design aprovado pelo utilizador; apenas revisão ADR autorizada.
[Revisão preparada](adr-review.md), reutilização de decisões aceites sem nova ADR
ou divergência concreta; F01–F07 registam adaptações previstas dentro do âmbito.
Só Model/Design em stages_completed. ADR por aprovar, Implement não autorizado.
V-015/L01–L24, regressão, manual humano, revisão Denji e integração pendentes.
Sem Java/testes Java/staging/commits/push ou outro IMP.

## Checkpoint Implement — tipos e testes vermelhos

Revisão ADR aprovada; somente primeiro passo de Implement autorizado.
[Walkthrough](implementation-walkthrough.md), tipos internos não ligados ao engine
e22 testes novos:20 falhas esperadas/0 erros/0 ignorados; compilação passou.
30 históricos de ativação, incluindo6 M07, passaram. Nenhum histórico alterado.
Parar antes dos comportamentos; Implement por aceitar, Test não iniciado.
Sem staging/commits/push/outro IMP.

## Checkpoint Implement — comportamentos para revisão

Utilizador aprovou tipos/testes vermelhos e autorizou comportamentos nos três
ficheiros. [Walkthrough atualizado](implementation-walkthrough.md): renovação,
poda, grants, correlação e limites com testes incrementais e TCP real.
Implement continua por aceitar; apenas Model/Design/ADR em stages_completed.
Test não autorizado; nenhum critério de fecho marcado como cumprido.
Pendentes V-015 consolidado, clean verify/JAR, manual humano, revisão final Denji
e integração. Sem staging/commits/push/fecho/outro IMP; sem gates adicionais.

## Checkpoint Test — manual humano pendente

Implement aprovado; Test autorizado pelo utilizador. [Relatório](ddd-03-test-report.md)
consolida V-015/L01–L24: clean verify1379/0/0/0,19 XML,JAR/hash, TCP real,
callbacks/limites/shutdown e preservação dos históricos/Q-C-40–42.
[Roteiro humano](../../../experiments/construction/023-arvores/manual-instructions.md)
preparado; ensaio kind=agent não é manual humano. Test permanece por aprovar.
Bolt/história/TASK-015/unidade004/PHASE-04 em progresso, sem fecho, staging,
commits/push/outro IMP. Depois faltam revisão final Denji e integração.

## Checkpoint Test reapresentado — manual humano cumprido

Francisco confirmou pessoalmente as seis etapas sem divergências. [Conferência](../../../experiments/construction/023-arvores/manual-human-verification-01/verification.json)
valida30 checks,CLEANUP_COMPLETE,MANUAL_PASS CHECKS=30,JAVA_EXIT0,passed=true,
hashes, origem da classe no JAR e terminação da JVM. WRAPPER_EXIT não fornecido,
sem resultado inferido. Código/JAR iguais ao clean verify1379/0/0/0,19 XML.
Test por aprovar, sem marcar test em stages_completed. Revisão final Denji e
integração pendentes; sem complete/DONE/staging/commits/push/fecho/outro IMP.

## Test aprovado — revisão final e integração pendentes

Utilizador aprovou Test: clean verify1379/0/0/0,19 XML,V-ADD aprovado e manual
humano Francisco30 cumprido. [Resumo para Denji](final-review-summary.md),
[plano de integração](integration-plan.md) e allowlist exata preparados.
Revisão final Denji ainda não confirmada; integração não autorizada/executada.
Bolt permanece in-progress,completed=null; história/TASK/unidade/fase abertas.
Sem staging/commits/push/fecho/outro IMP; target e alterações alheias excluídos.

## Revisão final Denji confirmada — plano final de integração

Utilizador confirmou revisão final aprovada da implementação atual, sem alterações
pendentes. [Confirmação/hashes](../../../experiments/construction/023-arvores/integration-01/review-confirmation.json)
e [plano final](integration-plan.md) atualizados. Simulação do pacote exato com
hashes/referências/V-ADD e exclusões; sem repetir build/manual, mesmos código/JAR.
Integração não autorizada/executada; bolt in-progress,completed=null,sem fecho.
Sem staging/commits/push/outro IMP; unidade004/PHASE-04 em progresso.
