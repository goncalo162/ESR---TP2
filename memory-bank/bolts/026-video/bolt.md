---
id: 026-video
unit: 005-video
intent: 001-ott-streaming
type: ddd-construction-bolt
status: complete
stories:
  - 008-emitir-fontes-com-pacing-e-procura
created: "2026-11-02T15:36:48Z"
updated: "2026-11-28T19:55:07Z"
started: "2026-11-28T14:47:06Z"
completed: "2026-11-28T19:55:07Z"
current_stage: null
stages_completed:
  - name: model
    completed: "2026-11-28T15:06:27Z"
    artifact: ddd-01-domain-model.md
  - name: design
    completed: "2026-11-28T15:28:10Z"
    artifact: ddd-02-technical-design.md
  - name: adr
    completed: "2026-11-28T15:48:00Z"
    artifact: adr-review.md
  - name: implement
    completed: "2026-11-28T19:11:45Z"
    artifact: implementation-walkthrough.md
  - name: test
    completed: "2026-11-28T19:54:45Z"
    artifact: ddd-03-test-report.md
requires_bolts:
  - 009-arvores
  - 025-video
enables_bolts:
  - 028-video
requires_units:
  - 004-arvores
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: construction
source_phase: PHASE-05
source_tasks:
  - TASK-020
source_task_order:
  - TASK-020
implementation_tasks:
  - IMP-019
size: M
validation_ids:
  - V-020
test_ids:
  - V-020-C
external_gates: []
adr_ids:
  - ADR-006
  - ADR-009
  - ADR-021
  - ADR-024
requirements:
  - INT-001
  - INT-002
  - NFR-001
  - REQ-012
  - REQ-013
  - REQ-015
  - REQ-018
architecture_source: add.md
---
# Bolt 026-video — Emitir fontes com pacing e procura

## Fonte normativa e âmbito

Ler [IMP-019](../../../add.md#imp-019) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md).

## História, dependências e gates

- [ ] [008-emitir-fontes-com-pacing-e-procura](../../intents/001-ott-streaming/units/005-video/stories/008-emitir-fontes-com-pacing-e-procura.md).
- Dependências: [009-arvores](../009-arvores/bolt.md), [025-video](../025-video/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-019 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-019 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-019 e as ADR do ADD.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-019 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-019 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] Emitir só com procura confirmada, ao FPS configurado, sem catch-up ilimitado.
- [x] Streams mantêm posição/seq/timeline independentes; loop preserva monotonia e EOF sem loop retira só essa stream.
- [x] Stop e shutdown cancelam emissão e fecham fontes sem afetar a outra origem.

## Evidência e fecho

- Testes: V-020-C, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-019](../../../add.md#imp-019).
- Prova futura: `experiments/construction/026-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Model preparado para revisão — 2026-11-28T14:54:27Z

Utilizador selecionou opção1: iniciar apenas Model. [Modelo](ddd-01-domain-model.md)
com publicações por stream, procura confirmada, cursor/seq/timeline, cadência,
qualificação, EOF/loop, ownership físico e fecho. Q-C-48/49 são propostas novas,
registadas em construction-duvidas, sem aprovação presumida. P01–P20/V-020-C
planeados, não executados;1582 testes e22687 JPEGs de025 são históricos.

Três caminhos futuros preservados, sem adiantar StreamForwarder/UDP/player,
configuração/protocolo ou alterações a fundações. Nenhum fonte lido/analisado,
Java/Maven/teste executado, Design/ADR/Implement, commit/push ou outro IMP.
current_stage=model, stages_completed=[], completed=null, história008 planned.
RegressãoJDK21, manual aplicável, Denji e integração para fecho; a exceção de
execução delegada em025 não é transposta aeste bolt. TASK-020 só apósA/B/C,
sem gates externos adicionais; remoções da demo/R17/negativos preservados.

## Model/Q-C-48/49 aprovados; Design para revisão — 2026-11-28T15:18:50Z

[Design](ddd-02-technical-design.md) concretiza aritmética exata, fronteiras
claim/pausa/retoma/entrega e qualificação antes de criar/start com routing.
W2+coordenador1, filas/células/heap finitos, fairness e reservas reais; prazo
global único. Integrações Owner/Demand/Availability são propostas a conferir
emADR A01–A08, sem pressupor APIs nem ampliar os três caminhos. Sem fontes
lidas/analisadas/alteradas, Java/Maven/testes, Implement, commits/push ou outroIMP.
current_stage=design, só Model emstages_completed; história008 planned,
completed=null. P01–P20/V-020-C continuam por executar. Fecho:regressãoJDK21,
manual humano aplicável antes deTest, Denji e integração; sem gate externo novo.

## Design aprovado; revisão ADR com impedimento — 2026-11-28T15:37:32Z

[Revisão A01–A08](adr-review.md) preparada sobre fontes e APIs reais. Qualificação
antes da factory/start cabe no App. Impedimento de owner/procura/revogação e
retirada local: Q-C-50 propõe oito caminhos totais, sem alterar os três do ADD
até autorização. Nenhum substituto por polling/owner paralelo/reflexão/sucesso
fictício; UNWIRED recusado. Não houve alteração Java, teste/Maven/aplicação.
Assinaturas JDK21 inspecionadas com javap; evidência estática não é teste.
current_stage=adr; só Model/Design aceites. ADR/Q-C-50 por aprovar;
completed=null, história008 planned e P01–P20/V-020-C por executar. Sem Implement,
commits/push/fecho/outro IMP. Manual aplicável/regressão JDK21/Denji/integração
mantidos, sem gates externos novos. TASK-020 só após A/B/C; R17 preservado.

## ADR e Q-C-50 aprovados; Implement iniciado — 2026-11-28T15:48:00Z

Revisão ADR e Q-C-50 (8 caminhos) aprovadas pelo utilizador. `add.md`,
`ddd-02-technical-design.md`, `docs/07-API-AND-INTERFACES.md`, `construction-duvidas.md`
e `bolt.md` atualizados antes de qualquer alteração de código. `current_stage=implement`,
com paragem obrigatória antes de validar `implement`.
Em 2026-11-28T16:15:00Z, `RouteTraceTest.java` foi autorizado como 9.º caminho
estritamente para fornecer `.Mjpeg` mínimos aos fixtures de `CliGraph`.

## Implement aprovado; Test iniciado — 2026-11-28T19:11:45Z

O utilizador aprovou explicitamente: “Aprovo implement e avança para Test”.
Implement foi registado como concluído no checkpoint humano; `current_stage=test`.
Test/V-020-C passa agora à execução, incluindo `RouteTraceTest.t14`–`t16` e
a menor suíte Maven pertinente. `completed=null`; a história não é fechada
antes da aprovação final do Test.

## Test executado; gate de cobertura pendente — 2026-11-28T19:21:16Z

Em JDK 21, `RouteTraceTest.t14`–`t16` passou (5/5 métodos selecionados) e a
menor suíte Maven pertinente passou (143/143). Nenhuma falha funcional exige
correção de produção. A cobertura medida no bundle com essa suíte foi 60,7%;
nas linhas executáveis adicionadas pela implementação, 69,1%, abaixo do
critério genérico de >80% definido pelo tipo DDD. `current_stage=test`;
Test não foi aceite nem o bolt/história foram fechados. Ver
`ddd-03-test-report.md` para âmbito, limitações e próxima decisão.

## Test validado; gates atingidos — 2026-11-28T19:45:47Z

Testes focados adicionados para composição Server/Relay real com readiness e
trace TCP, cleanup na falha de composição, binding/versões/confirmations de
fontes locais, e limites dos value objects/preparação. A suíte pertinente em
JDK 21 passou **150/150**; a suíte Maven completa passou **1599/1599**, sem
falhas/erros/skips. Relatório JaCoCo 0.8.12 gerado após a suíte completa:
bundle **91,72%** (7569/8252 linhas) e linhas executáveis adicionadas nos
quatro ficheiros de produção **81,58%** (673/825); ambos estritamente acima
de >80%. Não foram necessárias correções de produção.

`current_stage=test`, `completed=null`, `status=in-progress`; história 008
continua aberta. Evidência de Test pronta para checkpoint e aprovação humana.
Não marcar Test aceite, não fechar a história/bolt e não avançar para DONE sem
essa aprovação. Relatório: `ddd-03-test-report.md`.

## Test aceite pelo utilizador — 2026-11-28T19:54:45Z

O utilizador aprovou explicitamente: “Podes validar e fechar test. Podes também
fechar o bolt e fazer commit do mesmo”. Reexecução em OpenJDK 21.0.13.1 confirmou
5/5 testes focados, 150/150 pertinentes e 1599/1599 na suíte completa, sem
falhas/erros/skips. JaCoCo 0.8.12: bundle 7573/8252 (91,77%); linhas executáveis
adicionadas nos quatro ficheiros de produção 673/825 (81,58%). Critérios de
aceitação e etapa Test aceites. O script oficial de conclusão atualiza o estado
final e a história vinculada; a unidade permanece em progresso enquanto outros
bolts estiverem incompletos.

## Bolt fechado pelo fluxo oficial — 2026-11-28T19:55:07Z

`node .specsmd/aidlc/scripts/bolt-complete.cjs 026-video` concluiu com sucesso:
`status=complete`, `current_stage=null`, `completed=2026-11-28T19:55:07Z`,
e etapa Test associada a `ddd-03-test-report.md`. A história 008 foi atualizada
para `complete`/`implemented=true`. Unidade 005-video permanece em progresso
porque há outros quatro bolts incompletos; TASK-020 continua aberta e não foi
marcada DONE. Não se afirma que a validação executou manual humano, revisão
Denji ou integração.
