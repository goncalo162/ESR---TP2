---
id: "006-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "in-progress"
stories: ["003-difundir-anuncios-e-construir-catalogo-e-rotas"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-14T15:35:23Z"
started: "2026-11-14T11:47:13Z"
completed: null
current_stage: "test"
stages_completed: ["model", "design", "adr", "implement", "test"]
requires_bolts: ["004-overlay", "021-rotas"]
enables_bolts: ["022-rotas"]
requires_units: ["002-overlay"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-03"
source_tasks: ["TASK-012"]
source_task_order: ["TASK-012"]
implementation_tasks: ["IMP-009"]
size: "M"
validation_ids: ["V-012"]
test_ids: ["V-012"]
external_gates: []
adr_ids: ["ADR-004", "ADR-016", "ADR-017", "ADR-018", "ADR-020", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-006", "REQ-009", "REQ-012", "REQ-014", "REQ-016"]
architecture_source: "add.md"
---
# Bolt 006-rotas — Difundir anúncios e catálogo

## Fonte normativa e âmbito

Ler [IMP-009](../../../add.md#imp-009) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-012](../../../docs/tasks/PHASE-03/TASK-012.md).

## História, dependências e gates

- [ ] [003-difundir-anuncios-e-construir-catalogo-e-rotas](../../intents/001-ott-streaming/units/003-rotas/stories/003-difundir-anuncios-e-construir-catalogo-e-rotas.md).
- Dependências: [004-overlay](../004-overlay/bolt.md), [021-rotas](../021-rotas/bolt.md).
- Unidade: [003-rotas](../../intents/001-ott-streaming/units/003-rotas/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-009 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-009 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-009 e as ADR do ADD.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-009 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-009 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Catálogo inclui origem e validade dos quatro fluxos.
- [ ] Melhores caminhos e alternativas ficam disponíveis.
- [ ] Cache, seq, epoch e path impedem tempestade e regressão a época retirada.
- [ ] Validação V-012 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-012, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-009](../../../add.md#imp-009).
- Prova futura: `experiments/construction/006-rotas/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Histórico — Model apresentado antes da aprovação

⏳ Apenas Model autorizado. [Modelo](ddd-01-domain-model.md) preparado; Q-C-28
(composição/âmbito) e Q-C-29 (publicidade/geração/saturação) propostas, não aprovadas.
Dependências complete e integradas em main164ac0d; quatro caminhos atuais mantidos.
V-012/M01–M22 planeados, sem execução. Nenhum critério/etapa marcado concluído;
Design/ADR/Implement/Test não iniciados, nenhum Java/Maven/socket/commit/push.
Não alterar OverlayNode/novo teste por inferência; parar para decisão antes de Design.
Revisão final atribuída a Denji e integração continuam futuras; sem gates externos.
Evidência em [model-entry.json](../../../experiments/construction/006-rotas/model-entry.json).

## Histórico — Design apresentado antes da aprovação

Registo 2026-11-14T12:31:20Z. Model aprovado; Q-C-28 Opção1 decidida, ADD ampliado para seis
caminhos incluindo OverlayNode e OverlayNodeRoutingTest. Q-C-29 princípios
aprovados; valores/ordens concretos no [Design](ddd-02-technical-design.md), pendentes.
✅ Model concluído. ⏳ Design preparado. [ ] ADR/Implement/Test não iniciados.
P=min(K,E), B=min(E,2N), quota deslizante por stream/nodeId e barreira W/ANN
são propostas desta etapa, não aprovação implícita. Callbacks antigos distinguem
contabilidade de admissão física de mutação do domínio; sem seq/TTL inventados.
V-012/D01–D32, regressão atual, manual humano antes de aprovar Test, revisão final
atribuída a Denji e integração pendentes. Sem gates externos adicionais.
Nenhum Java, Maven, sockets, commit/push ou outro IMP. Parar antes de ADR.
Evidência: [entrada](../../../experiments/construction/006-rotas/design-entry.json)
e [aprovação](../../../experiments/construction/006-rotas/model-approval.json).

Verificações documentais: [V-ADD](../../../experiments/construction/006-rotas/design-v-add.md)
com zero erros/78 fixtures; [resultado e preservação de hashes](../../../experiments/construction/006-rotas/design-results.json).
O relatório inicial com duas divergências de metadados foi preservado; índice
de arquitetura sincronizado sem alterar o validador. [Diff incremental de Design](../../../experiments/construction/006-rotas/design-documents.diff)
separado do [acumulado](../../../experiments/construction/006-rotas/design-cumulative-documents.diff).

## Histórico — revisão ADR apresentada antes da aprovação

Registo 2026-11-14T12:53:08Z. Design completo, Q-C-29 limites/ordens e Q-C-30 aprovados.
✅ Model/Design concluídos. ⏳ [Revisão ADR](adr-review.md) preparada, incluindo
Q-C-31 pendente: preservar owner.offer fatal por fila cheia e ligação fechada
por OVERFLOW, sem interpretar ambos como backpressure recuperável.
Seis caminhos mantidos; quatro vazios, OverlayNode existente e novo teste do nó.
APIs reais/dependências conferidas sem alteração Java. Nenhuma ADR nova aceite
ou substituição necessária identificada; ADR-004/016/017/018/020/024 reutilizadas.
D01–D32/V-012 por executar. Implement/Test não iniciados, completed=null.
Sem gates externos adicionais. Regressão atual, manual humano antes de aprovar
Test, revisão final Denji e integração continuam obrigatórios.
Apenas revisão ADR autorizada; sem Maven/sockets/commits/push ou outro IMP.
[Entrada e aprovação](../../../experiments/construction/006-rotas/adr-review/entry.json).


## Entrada — Implement autorizado

Registo 2026-11-14T12:58:05Z. Revisão ADR/Q-C-31 aprovadas explicitamente. Apenas Implement
nos seis caminhos autorizado; preservar APIs/testes e reservas físicas. Sem
Test/fecho/commits/push.32 oráculos por executar à entrada; resultado só após
execução real. V-012/regressão/manual humano/revisão Denji/integração pendentes.

## Histórico — Implement apresentado antes da correção TTL

Registo 2026-11-14T14:36:19Z. Revisão ADR/Q-C-31 aprovadas; Model/Design/ADR concluídos.
[Walkthrough e D01–D32](implementation-walkthrough.md) preparados nos seis caminhos.
Regressão incremental JDK21:1254 testes/0 falhas/0 erros/0 skips,16 XML,43 testes
novos e1211 anteriores intactos. Red inicial, falhas de fronteira/correções,
restrição de sockets e erro de compilação do teste novo preservados. Nenhum
resultado de XML antigo conta como execução nova (correção documentada).
Implement por aprovar; Test não iniciado. D32 parcial; D29/D31 incluem verificação
estática de limites, sem alegar ensaio exaustivo/desempenho. V-012 tem prova
incremental, consolidação formal pendente com clean verify/JAR novo/manual humano.
Revisão final Denji e integração pendentes. Sem gates externos adicionais.
Fontes só nos seis caminhos; listas target geradas separadas. Sem commits/push
ou outro IMP; completed=null e critérios de fecho por cumprir.
Evidência em experiments/construction/006-rotas/implement/.


[Resultados/hashes atuais](../../../experiments/construction/006-rotas/implement/results.json),
[diff de fonte](../../../experiments/construction/006-rotas/implement/source.diff) e
[V-ADD](../../../experiments/construction/006-rotas/implement/v-add.md) ligados ao walkthrough.

## Histórico — Implement revisto antes da aprovação

Registo 2026-11-14T15:00:17Z. Desvios TTL/visibleAt reproduzidos em red-13 (39 testes,2 falhas),
corrigidos nos três caminhos autorizados e reapresentados no walkthrough.
Regressão incremental final15/JDK21:1256/0/0/0,16 XML,45 testes novos/1211
anteriores. Evidências1254 e anteriores preservadas; não confundir hashes.
current_stage=implement, só Model/Design/ADR concluídos. Implement por aprovar;
D32 parcial, sem Test/fecho/commits/push. Clean verify/JAR novo/manual humano/
revisão final Denji/integração pendentes, sem gates externos adicionais.
Evidência em experiments/construction/006-rotas/implement-revision-ttl/.


## Entrada de Test — Implement aprovado

Registo 2026-11-14T15:08:34Z. Utilizador aprovou Implement, incluindo TTL/visibleAt.
Apenas Test autorizado; clean verify JDK21/JAR novo/manual a preparar.
Repetição humana, aprovação Test, revisão final Denji/integração pendentes.
D32 parcial; sem fecho/commits/push ou outro IMP.

## Histórico — Test técnico antes da aprovação humana

Registo 2026-11-14T15:24:36Z. Implement/TTL/visibleAt aprovados. [Relatório Test](ddd-03-test-report.md)
com clean verify JDK21:1256/0/0/0,16 XML e novo JAR411736bytes/SHA256
1a1beb8b849ccf505e4266dda199fe439f93e2a54e2297058a2d43eb2265af93.
53 checks do manual técnico Codex passaram no JAR, Java0/JVM terminada/recursos
encerrados; primeiro ensaio falhado preservado e observação corrigida só no helper.
237 hashes de Projeto/ estáveis. [Roteiro humano](../../../experiments/construction/006-rotas/manual-instructions.md)
pronto; execução humana/aprovação Test não recebidas. D32 parcial; revisão final
Denji e integração pendentes. current_stage=test; só Model/Design/ADR/Implement
concluídos, completed=null. Sem gates externos adicionais. Sem fecho/commits/push
ou outro IMP. Código e testes de Projeto/ não alterados nesta etapa.

## Preparação de fecho — Test e revisão final aceites

Registo 2026-11-14T15:35:23Z. Test aprovado com manual Luis53/Java0 e revisão final atribuída
a Denji confirmada pelo utilizador. Cinco etapas aceites; integração local em
main ainda por comprovar antes de complete/DONE. D32 pendente só dessa prova.
Fontes/JAR validados preservados. Fecho/commits para main autorizados, sem push
ou novo IMP. Estado do bolt ainda in-progress nesta preparação.
