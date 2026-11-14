---
id: "022-rotas"
unit: "003-rotas"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "in-progress"
stories: ["004-diagnosticar-percursos-pela-tabela-de-rotas"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-14T17:53:17Z"
started: "2026-11-14T16:16:15Z"
completed: null
current_stage: "test"
stages_completed: [{"name": "plan", "completed": "2026-11-14T16:24:54Z", "artifact": "implementation-plan.md"}, {"name": "implement", "completed": "2026-11-14T17:24:31Z", "artifact": "implementation-walkthrough.md"}]
requires_bolts: ["004-overlay", "006-rotas", "020-overlay"]
enables_bolts: ["007-arvores", "008-arvores", "009-arvores", "023-arvores", "024-arvores"]
requires_units: ["002-overlay"]
blocks: false
complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-03"
source_tasks: ["TASK-013"]
source_task_order: ["TASK-013"]
implementation_tasks: ["IMP-010"]
size: "S"
validation_ids: ["V-013"]
test_ids: ["V-013"]
external_gates: []
adr_ids: ["ADR-004", "ADR-022", "ADR-024"]
requirements: ["INT-001", "INT-002", "REQ-010", "REQ-019", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 022-rotas — Diagnosticar percursos pela CLI

## Fonte normativa e âmbito

Ler [IMP-010](../../../add.md#imp-010) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-013](../../../docs/tasks/PHASE-03/TASK-013.md).

## História, dependências e gates

- [ ] [004-diagnosticar-percursos-pela-tabela-de-rotas](../../intents/001-ott-streaming/units/003-rotas/stories/004-diagnosticar-percursos-pela-tabela-de-rotas.md).
- Dependências: [004-overlay](../004-overlay/bolt.md), [006-rotas](../006-rotas/bolt.md), [020-overlay](../020-overlay/bolt.md).
- Unidade: [003-rotas](../../intents/001-ott-streaming/units/003-rotas/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **plan** → `implementation-plan.md`; validar a etapa contra IMP-010 e as ADR do ADD.
2. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-010 e as ADR do ADD.
3. [ ] **test** → `test-walkthrough.md`; validar a etapa contra IMP-010 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Trace reflete a tabela de cada nó.
- [ ] Falhas terminam por erro/timeout em prazo.
- [ ] Diagnóstico não altera subscrições.
- [ ] Validação V-013 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-013, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-010](../../../add.md#imp-010).
- Prova futura: `experiments/construction/022-rotas/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.


## Dependência concluída, sem iniciar execução

Registo 2026-11-14T15:43:38Z.006-rotas/IMP-009 complete e integrado em main b7d74be5d9bad9fe7f598f29fa17188037647cf2.
Apenas blocks=false; status=planned, started/completed/current_stage=null e
stages_completed=[] preservados. Não houve autorização de iniciar IMP-010.


## IMP-010 — Plan preparado, aprovação pendente

Registo 2026-11-14T16:16:15Z. Base operacional9144707, main, entrada limpa. Dependências
IMP-006/004-overlay, IMP-009/006-rotas e IMP-005/020-overlay complete;
unidade002-overlay complete. Apenas Plan do bolt Simple022-rotas iniciado,
current_stage=plan, stages_completed=[], completed=null.
Plan define APIs, consulta por salto/alternativas, correlação por Session completa,
visited/TTL/erros/prazos, reservas físicas, ownership e shutdown global.
Q-C-32(composição mínima em OverlayNode) e Q-C-33(detalhes/limites) abertas;
nenhum alargamento ou nova decisão aprovado. V-013/T01–T18 apenas propostos;
nenhum Java/Maven/teste de rede/manual executado nesta etapa.
Plan por aprovar; Implement/Test não iniciados, sem staging/commits/push.
Unidade003-rotas/PHASE-03 continuam em progresso; incrementos fechados intactos.
Manual humano, regressão, revisão final atribuída a Fillmore e integração futuros;
sem gates externos adicionais. Evidência em experiments/construction/022-rotas/.


## Plan aprovado — interfaces e testes vermelhos autorizados

Registo 2026-11-14T16:24:54Z. Plan e opções1 de Q-C-32/33 aprovados explicitamente pelo utilizador nesta sessão.
Extensão exclusivamente a OverlayNode.java para delegação mínima requestTrace(UUID).
TTL inicial32 incluindo iniciador; TTL_EXHAUSTED via TIMEOUT10/relatedType15;
prazo local3000ms; correlação/retorno por Sessions completas guardadas;
TraceTicket consultável, limites físicos/CLI do Plan aprovados. Concretizações
locais aprovadas, não imposições do enunciado nem alteração de wire/hard caps.
Apenas interfaces/tipos e testes inicialmente vermelhos autorizados, quatro
ficheiros. Comportamentos, Test, staging, commits/push e fecho não autorizados.
V-013/T01–T18 integral, regressão atual, manual humano, revisão final Fillmore
e integração continuam pendentes; sem gates externos adicionais.


## IMP-010 — interfaces/tipos e testes inicialmente vermelhos

Registo 2026-11-14T16:29:16Z. Plan e Q-C-32/33 opção1 aprovados; quatro caminhos autorizados,
OverlayNode exclusivamente fachada mínima. Apenas interfaces/stubs/tipos/testes:
13 selecionados,2 passam/11 falham por asserção/0 erros/0 ignorados, exit1 esperado;
compilação concluída, sem comportamentos. Algumas asserções ainda bloqueadas
pelo stub; V-013/T01–T18 integral e integração real não executados.
current_stage=implement; só Plan concluído. Parar antes dos comportamentos.
Test/regressão atual/manual humano/revisão final Fillmore/integração pendentes;
sem gates externos adicionais. Nenhum staging/commit/push/fecho ou outro IMP.
Evidência: experiments/construction/022-rotas/implement-interfaces/.


## IMP-010 — comportamentos de Implement para aprovação

Registo 2026-11-14T17:19:49Z. Utilizador autorizou comportamentos nos quatro caminhos Q-C-32/33
aprovados. Implement preparado; regressão final1296/0falhas/0erros/0ignorados,
17 XML frescos,40 novos testes e1256 históricos byte preservados. CLI/TCP reais,
limites/clock/callbacks/shutdown/ausência de subscrição e vídeo observados no
âmbito dos testes. Falha transitória do teste histórico de saída bloqueada na
regressão02 preservada; repetição isolada e regressão03 passaram sem alterações,
causa não estabelecida. Sem clean verify/JAR novo/manual humano nesta etapa.
current_stage=implement; só Plan concluído, Implement por aprovar, Test não iniciado.
V-013/T01–T18 consolidação formal, clean verify/manual humano, revisão final
Fillmore e integração pendentes; sem gates externos adicionais. target/ separado.
Sem staging/commit/push/fecho ou outro IMP. Evidência:
experiments/construction/022-rotas/implement-behavior/regression-03/result.json.
Walkthrough: memory-bank/bolts/022-rotas/implementation-walkthrough.md.


## IMP-010 — Test técnico executado, manual humano pendente

Registo 2026-11-14T17:45:23Z. Implement aprovado pelo utilizador; apenas Test autorizado.
clean verify em Projeto/:1296 testes/0falhas/0erros/0ignorados,17XML novos;
JAR436556bytes,SHA2565f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52.
40 oráculos de trace/1256 históricos executados; V-013/T01–T18 consolidados
tecnicamente com CLI/TCP reais,limites,ausência de11–14/vídeo no intervalo
observado e shutdown físico. Código/POM aprovados intactos; target/ separado.
Falha histórica m13 em Implement preservada sem causa estabelecida; não
reapareceu no clean verify,sem aumentar prazos ou enfraquecer testes.
Roteiro humano pronto; ensaio kind=agent34checks separado e não conta como
manual humano. Primeira cena do auxiliar corrigida após reparação legítima C–S,
sem mudar código/JAR. current_stage=test; só Plan/Implement concluídos.
Test por aprovar até manual humano; depois revisão final Fillmore/integração.
Sem gates externos adicionais,staging/commits/push/fecho ou outro IMP.
Relatório: memory-bank/bolts/022-rotas/test-walkthrough.md.
Evidência: experiments/construction/022-rotas/test-build-01/result.json.


## IMP-010 — pausa da validação e WIP autorizado

Registo 2026-11-14T17:53:17Z. Utilizador interrompeu a validação e autorizou commit WIP local
limitado ao incremento/documentação/evidência,sem target/ ou alterações alheias.
Plan/Implement aprovados; Test técnico1296/0/0/0,JAR/hash preservados.
Manual humano e aprovação Test pendentes; ensaios agent/parciais preservados e
não substituem execução humana. Falha histórica sem causa estabelecida mantém-se.
current_stage=test,status=in-progress,completed=null; só Plan/Implement concluídos.
TASK-013/história/unidade/fase em progresso; sem implemented=true/DONE/complete.
Sem push/revisão final presumida/integração final/novo IMP. Retoma:
experiments/construction/022-rotas/RESUME.md.
