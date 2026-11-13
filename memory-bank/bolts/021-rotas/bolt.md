---
id: "021-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "complete"
stories: ["002-modelar-candidatos-custos-e-snapshots-por-stream"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-13T18:09:37Z"
started: "2026-11-13T16:04:28Z"
completed: "2026-11-13T18:09:37Z"
current_stage: "test"
stages_completed: ["model", "design", "adr", "implement", "test"]
requires_bolts: ["004-overlay", "005-rotas"]
enables_bolts: ["006-rotas"]
requires_units: ["002-overlay"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-03"
source_tasks: ["TASK-011"]
source_task_order: ["TASK-011"]
implementation_tasks: ["IMP-008"]
size: "M"
validation_ids: ["V-011"]
test_ids: ["V-011"]
external_gates: []
adr_ids: ["ADR-004", "ADR-017", "ADR-018", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-005", "REQ-006", "REQ-014", "REQ-021"]
architecture_source: "add.md"
---
# Bolt 021-rotas — Modelar candidatos e snapshots

## Fonte normativa e âmbito

Ler [IMP-008](../../../add.md#imp-008) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-011](../../../docs/tasks/PHASE-03/TASK-011.md).

## História, dependências e gates

- [x] [002-modelar-candidatos-custos-e-snapshots-por-stream](../../intents/001-ott-streaming/units/003-rotas/stories/002-modelar-candidatos-custos-e-snapshots-por-stream.md).
- Dependências: [004-overlay](../004-overlay/bolt.md), [005-rotas](../005-rotas/bolt.md).
- Unidade: [003-rotas](../../intents/001-ott-streaming/units/003-rotas/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-008 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-008 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-008 e as ADR do ADD.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-008 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-008 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] Cada fluxo tem custo, origem, destinos e estado.
- [x] Empates são reproduzíveis e caminhos inválidos rejeitados.
- [x] Expirar um candidato não apaga alternativas válidas.
- [x] Validação V-011 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-011, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-008](../../../add.md#imp-008).
- Prova futura: `experiments/construction/021-rotas/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Histórico — Model preparado para aprovação

⏳ Apenas Model autorizado pelo utilizador (opção 1). Dependências complete e
integradas na base cc952c5; IMP-007/f990503 é ancestral de main. Modelo em
[ddd-01-domain-model.md](ddd-01-domain-model.md); Q-C-26 proposta, não aprovada.
Nenhum critério de aceitação ou etapa concluído. V-011/regressão/manual/revisão
final Waheed/integração pendentes; sem gates externos adicionais. Sem leitura
semântica ou alteração Java, Maven, sockets, commits/push ou outro IMP.
Evidência: [entrada](../../../experiments/construction/021-rotas/model-entry.json).

## Histórico — Design para revisão

Registo 2026-11-13T16:19:45Z: ✅ Model e os três princípios de Q-C-26 aprovados pelo utilizador.
⏳ [Design](ddd-02-technical-design.md) preparado, incluindo APIs/tipos, capacidades
exatas propostas, TTL/watermarks, época atómica por fonte e D01–D22. Só Model
concluído; Design ainda não aprovado. Valores e retenção continuam para revisão.
Âmbito preservado nos seis caminhos; sem código, Maven, sockets, commits/push ou
outro IMP. V-011/regressão atual/manual offline antes de aprovar Test/revisão final
Waheed/integração pendentes. Sem gates externos adicionais. Prova da aprovação em
[model-approval.json](../../../experiments/construction/021-rotas/model-approval.json).

## Histórico — revisão ADR para aprovação

Registo 2026-11-13T16:41:04Z. ✅ Model/Design e Q-C-26 integralmente aprovados.
⏳ [Revisão ADR](adr-review.md) preparada; só Model/Design concluídos. APIs reais,
custo já convertido pelo monitor, limites/horizontes e consumidores futuros
conferidos estaticamente. Sem divergência arquitetural identificada; sem ADR nova.
Futura composição requer checkpoint próprio. Seis caminhos preservados; sem
Java alterado, Maven, commits/push ou outro IMP. V-011/regressão/manual offline
antes de aprovar Test/revisão final Waheed/integração continuam pendentes.
Sem gates externos adicionais; parar antes de Implement.

## Implement autorizado

Revisão ADR aprovada explicitamente; apenas Implement nos seis caminhos e
testes incrementais autorizado. Test/fecho, commits/push e outro IMP não autorizados.

## Histórico — Implement para aprovação

✅ Model/Design/ADR aprovados; ⏳ Implement preparado para revisão, não aprovado.
[Walkthrough](implementation-walkthrough.md), D01–D22 e evidência real em
experiments/construction/021-rotas/. Seis caminhos +851/−0,35 testes novos e
regressão incremental08:1211/0/0/0,JDK21; execução07 restrita preservada.
D22 parcial: clean verify, JAR/manual humano, revisão final e integração pendentes.
Test/fecho não iniciados; nenhum commit/push ou outro IMP. Sem gates adicionais.

## Histórico — Test técnico executado, manual humano pendente

Registo 2026-11-13T17:43:51Z. ✅ Model/Design/ADR/Implement aprovados; ⏳ Test em progresso.
[Relatório](ddd-03-test-report.md): clean verify JDK21,1211/0/0/0,14 XML;
novo JAR364704 bytes/SHA256 366210cf8c7656ad2948ae1c842796632085aaa4f09afcedcd00a49db0a0ecee.
Manual técnico Codex43 CHECK_PASS/MANUAL_PASS/Java0; execução humana não recebida.
[Roteiro para Luis](../../../experiments/construction/021-rotas/manual-instructions.md).
D22 parcial até manual humano, aprovação de Test, revisão final e integração.
Fonte aprovada intacta, delta Java deste Test0; target separado. Sem fecho,
commits/push ou outro IMP. Sem gates externos adicionais.

## Histórico — Test e revisão final aprovados; integração pendente

Cinco etapas concluídas; Test/manual humano Luis43 aprovados e revisão final por
Luis confirmada, substituindo Waheed. Commits/integração/push main e fecho
explicitamente autorizados. Bolt ainda in-progress/completed=null até integração
comprovada. D22 parcial só por integração. Nenhum outro IMP iniciado.


## IMP-008 / 021-rotas / TASK-011 — fecho após integração comprovada

Registo 2026-11-13T18:09:37Z. Versão revista integrada em main: 3b7aa745cac62b71ac8ce7e036656bf7ab21692f;192 caminhos/hashes
iguais ao pacote, V-ADD0/78 numa cópia extraída do commit. Fontes/JAR e relatório
humano Luis conferidos no commit, sem overlay do diretório de trabalho.
Test1211/0/0/0,JDK21/14XML; manual humano Luis43 CHECK_PASS/MANUAL_PASS/Java0/
passed=true/JVM terminada aceites. Revisão final Luis confirmada pelo utilizador,
substituindo Waheed. D22 cumprido; fecho explicitamente autorizado.
Bolt/história complete, implemented=true, TASK-011 DONE. Dez bolts complete,
25 planned, nenhum em execução. Unidade003-rotas/PHASE-03 continuam em progresso;
006-rotas apenas desimpedido, sem início ou autorização para outro IMP.
Código/JAR/logs originais preservados, sem repetição de build/manual. EXTRA-02
preexistente e target gerado fora dos commits. Publicação main autorizada,
resultado só será declarado após push efetivo; nenhum force push.

[Fecho e prova](closure-walkthrough.md).
