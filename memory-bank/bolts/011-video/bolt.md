---
id: "011-video"
unit: "005-video"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "complete"
stories: ["002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-24T20:49:09Z"
started: "2026-11-24T14:23:59Z"
completed: "2026-11-24T20:49:09Z"
current_stage: null
stages_completed: [{"name": "plan", "completed": "2026-11-24T14:37:54Z", "artifact": "implementation-plan.md"}, {"name": "implement", "completed": "2026-11-24T14:48:45Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-24T15:43:59Z", "artifact": "test-walkthrough.md"}]
requires_bolts: ["009-arvores", "010-video"]
enables_bolts: ["025-video"]
requires_units: ["004-arvores"]
blocks: false
complexity:
  avg_complexity: 2
  avg_uncertainty: 2
  max_dependencies: 3
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-05"
source_tasks: ["TASK-020"]
source_task_order: ["TASK-020"]
implementation_tasks: ["IMP-017"]
size: "M"
validation_ids: ["V-020"]
test_ids: ["V-020-A"]
external_gates: ["G-MEDIA"]
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
architecture_source: "add.md"
---
# Bolt 011-video — Preparar quatro conteúdos distintos

## Fonte normativa e âmbito

Ler [IMP-017](../../../add.md#imp-017) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md).

## História, dependências e gates

- [x] [002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao](../../intents/001-ott-streaming/units/005-video/stories/002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao.md).
- Dependências: [009-arvores](../009-arvores/bolt.md), [010-video](../010-video/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: G-MEDIA.

## Etapas e validação

1. [x] **plan** → `implementation-plan.md`; validar a etapa contra IMP-017 e as ADR do ADD.
2. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-017 e as ADR do ADD.
3. [x] **test** → `test-walkthrough.md`; validar a etapa contra IMP-017 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] Quatro conteúdos identificáveis, dois por origem, preparados no perfil de cinco dígitos/JPEG.
- [x] Proveniência, parâmetros de preparação, hashes e FPS registados; cópias idênticas não contam como vídeos distintos.
- [x] Cada JPEG respeita o limite e é descodificável; condições de redistribuição ainda pendentes ficam explícitas para TASK-030.

## Evidência e fecho

- Testes: V-020-A, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-017](../../../add.md#imp-017).
- Prova futura: `experiments/construction/011-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Histórico — Test/G-MEDIA técnicos aprovados antes da revisão/integração

Utilizador aprovou22687 JPEGs,16 fronteiras,quatro conteúdos distintos e manual
Francisco13. Aceitou B com créditos presentes mas ilegíveis só para Test técnico:
nenhuma aprovação de atribuição adequada,legibilidade ou redistribuição.
LOCAL-04/G-MEDIA-DISTRIBUTION abertos para TASK-030;originais e parcial preservados.
[Confirmação ligada a hashes](../../../experiments/construction/011-video/review-package-01/technical-approval.json),
[pacote para revisão](../../../experiments/construction/011-video/review-package-01/README.md).
Revisão final e integração pendentes;bolt/história/pai/unidade em progresso.
LOCAL-03:cópia/hash runtime não instalada. Volume459820128 bytes,sem LFS/externo
ou conteúdo alterado por inferência. Sem staging/commits/push/fecho.

## Histórico — Git LFS aplicado antes da revisão final

Git LFS3.8.1 oficial instalado;local --skip-smudge,13 hooks anteriores preservados,
quatro hooks LFS novos. .gitattributes exatamente4 regras aprovadas,sem staging.
Clean/cache/smudge materializaram4 objetos reais com hashes aprovados;ZIP temporário
de dados validado,não Codigo.zip final. Sem rede LFS/upload/push/index/histórico.
[Execução/efeitos Homebrew](../../../experiments/construction/011-video/lfs-implementation-01/README.md),
[pacote atualizado](../../../experiments/construction/011-video/lfs-review-package-01/README.md).
Manifesto distingue binários no disco de ponteiros Git esperados. Antes de publicar,
proprietário confirma quota/budget;sem custo zero presumido ou billing alterado.
LOCAL-04/distribuição/revisão final/integração pendentes,LOCAL-03 não instalada.

## Estado final — complete,apenas TASK-020 parte A

Fecho documental autorizado após Test/manual Francisco13,revisão final Denji e
integração local comprovada em main8a91d9f. [Fecho](closure-walkthrough.md).
V-020-A:22687 JPEGs,16 fronteiras,quatro conteúdos distintos. História complete/
implemented=true. TASK-020 inteira,unidade005 e PHASE-05 continuam em progresso.
LOCAL-03:cópia para Projeto/media não instalada. LOCAL-04 e
G-MEDIA-DISTRIBUTION abertos para TASK-030;créditos B presentes mas ilegíveis.
Sem atribuição adequada,legibilidade,redistribuição ou publicação aprovadas.
Quota/orçamento pendentes antes de publicar;Homebrew separado,sem intervenção.
Os checkpoints acima são históricos;nenhum novo IMP iniciado. Sem push.
