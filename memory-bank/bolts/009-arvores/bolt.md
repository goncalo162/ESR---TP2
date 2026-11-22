---
id: "009-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "complete"
stories: ["005-preparar-o-segundo-marco-com-arvores-e-adaptacao"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-22T23:10:04Z"
started: "2026-11-22T16:20:00Z"
completed: "2026-11-22T17:12:18Z"
current_stage: null
stages_completed: [{"name": "plan", "completed": "2026-11-22T16:35:00Z", "artifact": "implementation-plan.md"}, {"name": "implement", "completed": "2026-11-22T16:50:00Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-22T17:00:00Z", "artifact": "test-walkthrough.md"}]
requires_bolts: ["022-rotas", "024-arvores"]
enables_bolts: ["010-video", "011-video", "012-video", "013-video", "025-video", "026-video", "027-video", "028-video"]
requires_units: ["003-rotas"]
blocks: false
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-018"]
source_task_order: ["TASK-018"]
implementation_tasks: ["IMP-015"]
size: "S"
validation_ids: ["V-018"]
test_ids: ["V-018"]
external_gates: ["G-ASSESSMENTS"]
adr_ids: ["ADR-008", "ADR-023", "ADR-024"]
requirements: ["CON-007", "INT-001", "INT-002", "NFR-005"]
architecture_source: "add.md"
---
# Bolt 009-arvores — Demonstrar o segundo marco

## Fonte normativa e âmbito

Ler [IMP-015](../../../add.md#imp-015) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-018](../../../docs/tasks/PHASE-04/TASK-018.md).

## História, dependências e gates

- [x] [005-preparar-o-segundo-marco-com-arvores-e-adaptacao](../../intents/001-ott-streaming/units/004-arvores/stories/005-preparar-o-segundo-marco-com-arvores-e-adaptacao.md).
- Dependências: [022-rotas](../022-rotas/bolt.md), [024-arvores](../024-arvores/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: G-ASSESSMENTS.

## Etapas e validação

1. [x] **plan** → `implementation-plan.md` (2026-11-22T16:35:00Z); validar a etapa contra IMP-015 e as ADR do ADD.
2. [x] **implement** → `implementation-walkthrough.md` (run PASS 20261122T164428Z-f6n15e, 44/44 checks); validar a etapa contra IMP-015 e as ADR do ADD.
3. [x] **test** → `test-walkthrough.md` (V-018 consolidado, 44/44, mvn test 1459/0/0/0). Validador-bolt independente devolveu **PASS global** em 2026-11-22 (reexecução própria 20261122T165306Z-2hy8eu, 44/44), com 7 achados documentais menores corrigidos neste bolt; revisão final Waheed confirmada pelo utilizador e integração em main comprovada na reconciliação abaixo.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] Demonstração mostra isolamento entre fluxos e procura agregada.
- [x] Falha e alteração de RTT têm evidência verificável.
- [x] Conteúdo e estado do marco estão registados.
- [x] Validação V-018 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-018, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-015](../../../add.md#imp-015).
- Evidência executada: [runs do marco](../../../experiments/milestone-2/README.md) e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Estado reconciliado de fecho

Estado reconciliado em 2026-11-22T23:10:04Z: **024-arvores/IMP-014 e 009-arvores/IMP-015 complete; TASK-017/018 DONE**, com V-017/S01–S18 e V-018 executadas. Manual humano e Test aprovados; revisão final de Waheed das duas versões confirmada pelo utilizador. Integração em main comprovada pelos PRs #5 (ce76726) e #6 (1ee8db6). Regressão atual JDK21: **1459/0/0/0, 21 XML**. PHASE-04/unidade004 **DONE/complete**, com coordenação de Fillmore e revisão dos três membros confirmadas pelo utilizador. Total: **17 bolts complete, 0 in-progress, 18 planned**; nenhum novo IMP iniciado. Negativos e hashes preservados. R17 inicial continua sem causa estabelecida; a integração com métricas controladas não demonstra a sua causa. G-ASSESSMENTS mantém a pendência de informação docente sobre data/conteúdo; o marco técnico concluído não prova avaliação ou submissão externa. [Evidência e proveniência das confirmações](../../../experiments/construction/024-arvores/closure-reconciliation-01/README.md).

Os checkpoints datados anteriores são históricos. Datas originais de conclusão preservadas; ratificação atual sem retrodatação.
