---
id: "001-base-validada"
unit: "001-base-validada"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "in-progress"
stories: ["001-verificar-base"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-03T21:42:46Z"
started: "2026-11-03T21:03:35Z"
completed: null
current_stage: "test"
stages_completed: [{"name": "plan", "completed": "2026-11-03T21:08:48Z", "artifact": "implementation-plan.md"}, {"name": "implement", "completed": "2026-11-03T21:22:32Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-03T21:40:56Z", "artifact": "test-walkthrough.md"}]
requires_bolts: []
enables_bolts: ["002-overlay", "003-overlay", "004-overlay", "019-overlay", "020-overlay"]
requires_units: []
blocks: false
complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "BASELINE"
source_tasks: ["TASK-001", "TASK-002", "TASK-003", "TASK-004", "TASK-005"]
source_task_order: ["TASK-001", "TASK-002", "TASK-003", "TASK-004", "TASK-005"]
implementation_tasks: ["IMP-001"]
size: "S"
validation_ids: ["V-001", "V-002", "V-003", "V-004", "V-005"]
test_ids: ["V-001", "V-002", "V-003", "V-004", "V-005", "V-ADD"]
external_gates: []
adr_ids: ["ADR-001", "ADR-002", "ADR-007", "ADR-008", "ADR-009", "ADR-010", "ADR-012", "ADR-014", "ADR-020", "ADR-021", "ADR-024", "ADR-026"]
requirements: ["CON-009", "INT-001", "INT-002", "NFR-004", "NFR-005", "REQ-001", "REQ-003", "REQ-004", "REQ-012", "REQ-013", "REQ-016", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 001-base-validada — Verificar a base herdada

## Retoma — checkpoint 5

Planeamento iniciado em 2026-11-03T21:03:35Z, após leitura e comunicação do
estado ao utilizador. [Plano](implementation-plan.md) e
[Q-C-01](../../../docs/construction-duvidas.md#q-c-01--testes-primeiro-no-imp-001)
aprovados explicitamente pelo utilizador, com registo em 2026-11-03T21:08:48Z.
Etapa plan concluída. O utilizador aprovou também o checkpoint 2, com registo
em 2026-11-03T21:16:53Z, e autorizou preparar o checkpoint 3: walkthrough
e diffs. Esse checkpoint foi apresentado sem executar a validação.
O pedido do utilizador impõe cinco checkpoints, aplicados às etapas locais.

[Interfaces e mapeamento dos testes](interfaces-and-test-map.md) preparados
por inspeção em 2026-11-03T21:13:13Z e aprovados no checkpoint 2.
Nenhuma lacuna de âmbito que exija alterar fundações/testes identificada por
leitura. Nenhum build, suite, V-ADD ou teste manual foi executado no checkpoint 2.

Entregável do checkpoint 3: [implementation-walkthrough.md](implementation-walkthrough.md).
O utilizador aprovou o checkpoint 3, com registo em 2026-11-03T21:22:32Z,
e autorizou V-001–005/V-ADD, fixtures, POM/provider, clean verify com JDK 21
e teste manual. A execução parou no checkpoint 4 para revisão, sem fechar o bolt.

[Validação executada](test-walkthrough.md): regressão nova com 149 testes,
zero falhas/erros/skips; 45 fixtures conferidas; V-ADD sem erros; teste manual
com 22 verificações aprovadas. Resultados e limitações preparados em
2026-11-03T21:32:04Z e aprovados explicitamente pelo utilizador, com registo em
2026-11-03T21:40:56Z. Nenhuma alteração
das fundações/testes necessária. O build alterou apenas uma lista gerada
rastreada em Projeto/target/; os restantes 162 hashes de Projeto/ mantêm-se.
Test concluído após essa aprovação. Os critérios técnicos abaixo estão cumpridos;
o bolt permanece in-progress/test e completed=null enquanto o fecho aguarda
revisão do checkpoint 5 e evidência de integração na base comum. A documentação
local ainda não está integrada; não há autorização para commit/push.
[Preparação do fecho](closure-walkthrough.md), sem iniciar outro bolt.

## Fonte normativa e âmbito

Ler [IMP-001](../../../add.md#imp-001) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-001](../../../docs/tasks/PHASE-01/TASK-001.md), [TASK-002](../../../docs/tasks/PHASE-01/TASK-002.md), [TASK-003](../../../docs/tasks/PHASE-01/TASK-003.md), [TASK-004](../../../docs/tasks/PHASE-01/TASK-004.md), [TASK-005](../../../docs/tasks/PHASE-02/TASK-005.md).

## História, dependências e gates

- [ ] [001-verificar-base](../../intents/001-ott-streaming/units/001-base-validada/stories/001-verificar-base.md).
- Dependências: nenhuma.
- Unidade: [001-base-validada](../../intents/001-ott-streaming/units/001-base-validada/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **plan** → `implementation-plan.md`; validar a etapa contra IMP-001 e as ADR do ADD.
2. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-001 e as ADR do ADD.
3. [x] **test** → `test-walkthrough.md`; validar a etapa contra IMP-001 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] Confirmar componentes reais e manter TASK-001–005 DONE, sem os reimplementar.
- [x] Conferir 45 fixtures, 16 tipos ativos e o ADD/specs/DAG antes de libertar nova construção.
- [x] Executar a regressão existente no ambiente de Construction, guardando um novo log; 149 é total histórico, não resultado atual.

Os 149 testes do checkpoint 4 são resultado atual contado nos XML novos;
a formulação do critério preserva a obrigação de não reutilizar a contagem histórica.
Critérios técnicos cumpridos não substituem revisão/integração para estado complete.

## Evidência e fecho

- Testes: V-001, V-002, V-003, V-004, V-005, V-ADD, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-001](../../../add.md#imp-001).
- Prova atual: `experiments/construction/001-base-validada/` e [relatório de validação](test-walkthrough.md).
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.
