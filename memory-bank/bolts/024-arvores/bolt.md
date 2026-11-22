---
id: "024-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "complete"
stories: ["004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-21T16:01:32Z"
started: "2026-11-21T14:10:07Z"
completed: "2026-11-22T15:56:23Z"
current_stage: "null"
stages_completed: [{"name": "model", "completed": "2026-11-21T14:21:52Z", "artifact": "ddd-01-domain-model.md"}, {"name": "design", "completed": "2026-11-21T14:46:03Z", "artifact": "ddd-02-technical-design.md"}, {"name": "adr", "completed": "2026-11-21T15:03:15Z", "artifact": "adr-review.md"}, {"name": "implement", "completed": "2026-11-22T15:23:20Z", "artifact": "implementation-walkthrough.md"},{"name": "test", "completed": "2026-11-21T15:56:23Z"}]
requires_bolts: ["008-arvores", "022-rotas"]
enables_bolts: ["009-arvores"]
requires_units: ["003-rotas"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-017"]
source_task_order: ["TASK-017"]
implementation_tasks: ["IMP-014"]
size: "M"
validation_ids: ["V-017"]
test_ids: ["V-017"]
external_gates: []
adr_ids: ["ADR-005", "ADR-016", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-001", "NFR-003", "REQ-009", "REQ-014"]
architecture_source: "add.md"
---
# Bolt 024-arvores — Trocar parent com histerese

## Fonte normativa e âmbito

Ler [IMP-014](../../../add.md#imp-014) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-017](../../../docs/tasks/PHASE-04/TASK-017.md).

## História, dependências e gates

- [ ] [004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada](../../intents/001-ott-streaming/units/004-arvores/stories/004-adaptar-rota-por-rtt-com-histerese-e-troca-confirmada.md).
- Dependências: [008-arvores](../008-arvores/bolt.md), [022-rotas](../022-rotas/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-014 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-014 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-014 e as ADR do ADD.
4. [ ] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-014 e as ADR do ADD.
5. [ ] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-014 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Política corresponde aos parâmetros documentados.
- [ ] Um upstream é confirmado no snapshot.
- [ ] Tráfego transitório e limites da troca são explicitados.
- [ ] Validação V-017 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-017, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-014](../../../add.md#imp-014).
- Prova futura: `experiments/construction/024-arvores/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Início autorizado — apenas Model

Registo 2026-11-21T14:10:07Z: seleção explícita da opção1 pelo utilizador para024-arvores/
IMP-014, apenas Model. Sem leitura/análise/alteração de Java/testes/fundações,
Design/ADR/Implement/Test, Maven, commits/push ou início de outro IMP.
008-arvores/IMP-013 e022-rotas/IMP-010 concluídos. R17 inicial de IMP-013
permanece sem causa estabelecida; evidência e limitação preservadas.

## Model proposto — aguardar revisão humana

[Model](ddd-01-domain-model.md) preparado em2026-11-21T14:13:30Z.
Q-C-45/46 propostas em [construction-duvidas](../../../docs/construction-duvidas.md),
sem aprovação presumida. Evidência documental em
[model-01](../../../experiments/construction/024-arvores/model-01/README.md).
V-017/S01–S18 planeados/não executados. Nenhuma etapa concluída; apenas
Model autorizado, história planned. Sem alteração de fontes/testes/fundações,
Maven, Design/ADR/Implement/Test, commits/push ou outro IMP.

## Model/Q-C-45/46 aprovados — apenas Design

Registo 2026-11-21T14:21:52Z: aprovação explícita do utilizador; Model concluído.
Apenas Design autorizado. V-017/S01–S18 por executar; sem Implement, commits,
push ou outro IMP. Três caminhos do ADD e testes históricos preservados.
Regressão JDK21, manual humano antes da aprovação Test, revisão final Waheed e
integração necessários para fecho; sem gate externo adicional. R17 histórico
continua sem causa estabelecida.

## Design proposto — checkpoint humano

[Design](ddd-02-technical-design.md) preparado em2026-11-21T14:29:02Z.
Identidades M/A, cálculo inteiro exacto, âncora D/revalidação, publicação atómica,
poda capturada, filhos e coordenação14/11/recuperação concretizados. T/A e
reservas partilhados; sem novas propriedades, caminhos ou gates externos.
[Evidência documental](../../../experiments/construction/024-arvores/design-01/README.md).
Só Model concluído, Design por aprovar. V-017/S01–S18 não executados; sem
fontes Java/Maven, ADR/Implement/Test, commits/push ou outro IMP. R17 preservado.

## Design aprovado — apenas revisão ADR

Registo 2026-11-21T14:46:03Z: aprovação explícita do utilizador. Model/Design concluídos,
current_stage=adr. Conferir APIs reais, sem Implement/commits/push ou outro IMP.
Três caminhos do ADD preservados; impedimento/alteração de contrato/âmbito será
apresentado antes de código. V-017/S01–S18 não executados. Preservar R17 histórico.

## Revisão ADR preparada — impedimento Q-C-47

[Revisão ADR](adr-review.md), registo 2026-11-21T14:54:50Z: APIs reais conferidas;
A01–A07 são adaptações internas propostas nos três caminhos. Observabilidade
prevista não está disponível no contrato/composição actual; Q-C-47 propõe
alternativas antes de alterar contrato/âmbito. Nenhuma está aprovada/aplicada.
Só Model/Design concluídos; ADR pendente, sem Implement/Test/commit/push.
V-017/S01–S18 não executados; R17 histórico sem causa estabelecida.

## ADR/Q-C-47-B aprovadas — apenas Implement

Registo2026-11-21T15:03:15Z: cinco caminhos no ADD/Design/contratos antes de código.
Preservar Snapshot/constructores/factories/fatal; testar sink até console e
limites/compatibilidade, sem alterar históricos. Sem Test/fecho/commit/push.

## Implement preparado — revisão humana pendente

Registo 2026-11-21T15:36:31Z / actualização 2026-11-21T16:01:32Z. [Walkthrough](implementation-walkthrough.md).
Regressão incremental actual 1459/0/0/0 JDK21.0.13.1, 21 XML: 1419 históricos
intactos + 40 novos. Q-C-47-B/ADR aprovadas; Implement em cinco caminhos para revisão,
sem avanço a Test, fecho, commits/push ou outro IMP. Snapshot/construtores/factories
preservados; payload/sink e fila limitada verificados até à saída real. Negativos
S13-transfer e S05/S06 stale metrics reproduzidos/corrigidos no âmbito e preservados com
os seus controlos e oráculos (SUSPECT, invalidação, saturação estrita e Session substituta);
R17 histórico sem causa estabelecida, não explicada pela integração controlada. V-017 formal,
clean verify/JAR de entrega, regressão aprovada, manual humano antes de aprovar
Test, Waheed e integração pendentes; sem gate externo adicional.
