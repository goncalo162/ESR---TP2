---
id: "020-overlay"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "simple-construction-bolt"
status: "in-progress"
stories: ["003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-10T22:22:56Z"
started: "2026-11-10T17:27:22Z"
completed: null
current_stage: null
stages_completed: [{"name": "plan", "completed": "2026-11-10T17:41:41Z", "artifact": "implementation-plan.md"}, {"name": "implement", "completed": "2026-11-10T22:05:26Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-10T22:22:56Z", "artifact": "test-walkthrough.md"}]
requires_bolts: ["001-base-validada", "003-overlay", "019-overlay"]
enables_bolts: ["004-overlay", "022-rotas"]
requires_units: ["001-base-validada"]
blocks: false
complexity:
  avg_complexity: 2
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-008"]
source_task_order: ["TASK-008"]
implementation_tasks: ["IMP-005"]
size: "M"
validation_ids: ["V-008"]
test_ids: ["V-008"]
external_gates: []
adr_ids: ["ADR-013", "ADR-014", "ADR-016", "ADR-017", "ADR-019", "ADR-020", "ADR-022", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-003", "REQ-001", "REQ-002", "REQ-023"]
architecture_source: "add.md"
---
# Bolt 020-overlay — Compor CLI e ciclo de processo

## Fonte normativa e âmbito

Ler [IMP-005](../../../add.md#imp-005) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-008](../../../docs/tasks/PHASE-02/TASK-008.md).

## História, dependências e gates

- [ ] [003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel](../../intents/001-ott-streaming/units/002-overlay/stories/003-compor-o-processo-overlay-e-encerrar-de-forma-previsivel.md).
- Dependências: [001-base-validada](../001-base-validada/bolt.md), [003-overlay](../003-overlay/bolt.md), [019-overlay](../019-overlay/bolt.md).
- Unidade: [002-overlay](../../intents/001-ott-streaming/units/002-overlay/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **plan** → `implementation-plan.md`; validar a etapa contra IMP-005 e as ADR do ADD.
2. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-005 e as ADR do ADD.
3. [x] **test** → `test-walkthrough.md`; validar a etapa contra IMP-005 e as ADR do ADD.

Tipo: [simple-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/simple-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] BOOTSTRAP, SERVER, RELAY e CLIENT arrancam no papel pedido.
- [x] Códigos de erro e logs são coerentes.
- [x] O fim de um processo não exige reiniciar os restantes.
- [x] Validação V-008 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-008, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-005](../../../add.md#imp-005).
- Evidência: [relatório de Test](test-walkthrough.md) e `experiments/construction/020-overlay/`.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Prontidão derivada após fecho de IMP-004

003-overlay complete, TASK-007 DONE e integração local 8db7b78 comprovada.
Atualizado apenas blocks=false: dependências concluídas. Este bolt permanece
planned, started=null, current_stage=null, stages_completed=[]; nenhum IMP iniciado.

## Início autorizado — apenas Model, preparação de Plan

Iniciado em 2026-11-10T17:27:22Z por instrução explícita do utilizador. Base pedida
`2ea748efd61ba5bdea355d2b5a02de6f6bd9a8c5`; checkout limpo em `90cded6b8d321cfcbe369dd4f8dbed9a01ddf430`.
Fontes de Projeto/ e ADD iguais à base pedida; diferenças posteriores preservadas.
O tipo continua Simple: Model é artefacto preliminar dentro de Plan, sem completar
Plan nem alterar a sequência plan/implement/test. current_stage=plan,
stages_completed=[], completed=null. Parar no checkpoint Model.
Sem implementação, testes Java, commits/push ou início de outro IMP.

## IMP-005 — checkpoint Model, sem implementação

[Modelo preparado](ddd-01-domain-model.md) em 2026-11-10T17:31:12Z. Dependências
001-base-validada/003-overlay/019-overlay complete, presentes na base pedida
2ea748e. Checkout 90cded6 preservado; Projeto/ e ADD iguais à base. O tipo
continua Simple; apenas preparação Model de Plan, current_stage=plan,
stages_completed=[], completed=null. Q-C-09 regista a lacuna NODE_LEAVE
na API pública, sem alargar os três ficheiros do ADD ou alterar fundações.
Plan não concluído; parar no checkpoint Model. V-008/regressão/manual/revisão
Waheed/integração pendentes. Sem Java/build/testes, commits/push ou outro IMP.

## IMP-005 — Model aceite e Plan para aprovação

Registado em 2026-11-10T17:38:56Z por decisão explícita do utilizador. Model aceite como preparação
de Plan Simple; Q-C-09 resolvida quanto ao âmbito: extensão mínima de lifecycle
em NeighborManager e teste próprio, sem mudar send() de aplicação ou reabrir IMP-004.
Base operacional 90cded6; referência histórica 2ea748e. Plan preparado e ainda
por aprovar; current_stage=plan, stages_completed=[], sem Implement/Test iniciados.
V-008 com subprocessos reais, regressão, manual humano antes de aprovar Test,
revisão final de Waheed e integração continuam exigidos; sem gates externos adicionais.
Sem Java, staging, commits/push, DONE ou início de outro IMP.

## Plan aprovado — interfaces/tipos e testes vermelhos apenas

Registado em 2026-11-10T17:41:41Z. Aprovação explícita do Plan incluindo Q-C-10/11/12.
Implement autorizado só até ao checkpoint interfaces/testes inicialmente vermelhos.
Comportamentos permanecem por implementar; Test não iniciado. Sem commits/push.

## IMP-005 — Plan aprovado; Implement limitado ao checkpoint interfaces/testes

Registo em 2026-11-10T17:46:21Z. Plan aprovado explicitamente pelo utilizador, incluindo
Q-C-10 (budget), Q-C-11 (gramática) e Q-C-12 (buffers locais). current_stage=implement;
apenas Plan consta em stages_completed. Interfaces/tipos e testes inicialmente
vermelhos preparados; comportamentos novos continuam pendentes. Test não iniciado.
V-008 completo/L01–L12/M01–M18, regressão completa atual, manual humano antes de
aprovar Test, revisão final Waheed e integração continuam exigidos, sem gates
externos adicionais. Bolt aberto, história não implementada, TASK-008 IN PROGRESS.
Sem commits/push, DONE ou início de outro IMP.

[Checkpoint interfaces/testes vermelhos preparado](interfaces-test-checkpoint.md).
Implement continua incompleto e aguarda validação antes de comportamentos.

## IMP-005 — Implement preparado para aprovação

Registo em 2026-11-10T18:16:24Z. O utilizador aprovou interfaces/testes vermelhos e autorizou
comportamentos nos cinco ficheiros do Plan. Implement preparado, ainda por aprovar;
current_stage=implement, só Plan em stages_completed. Regressão atual Maven test:
979 testes, zero falhas/erros/ignorados, com 44 CLI e 32 lifecycle novos, TCP e
subprocessos reais. Test não iniciado; clean verify e manual humano antes da sua
aprovação continuam pendentes, tal como revisão final Waheed e integração.
Q-C-09–12 preservadas; send() e testes históricos intactos, sem alteração wire/caps
ou dependências. Não há gates externos adicionais. Bolt aberto, história ainda
não implementada, TASK-008 IN PROGRESS e unidade in-progress. Sem commits/push
ou outro IMP.

## IMP-005 — correção terminal no checkpoint Implement

Registo em 2026-11-10T18:39:44Z. Retirado anúncio prematuro STOPPED/exit: última mensagem
passa a progresso SHUTDOWN/closing-output, sem código final. Resultado terminal
só após conhecer o fecho do writer. Dois testes novos reproduzidos primeiro
vermelhos e depois verdes; regressão Maven test atual: 981 testes, zero falhas,
erros ou ignorados. Budget global, send(), manager, wire/caps e testes históricos
preservados. Implement reapresentado, por aprovar; Test não iniciado. Sem staging,
commits/push ou DONE. clean verify, manual humano, revisão Waheed e integração
continuam pendentes, sem gates externos adicionais.

## Preparação autorizada do manual de Francisco

Registada em 2026-11-10T18:53:31Z, por pedido explícito de preparar o manual após clean verify.
Executado mvn -B -ntp clean verify: 981 testes, zero falhas/erros/ignorados;
JAR novo SHA-256 303facafc148b96bd2cc0d98ea6afc7a773a50cb2b49d1d5c6c6c4c481de5b52. Perfis próprios validados offline e sete
portas de loopback disponíveis no instante registado, sem reserva mantida.
Roteiro com quatro terminais, logs pessoais e captura real JAVA_EXIT/TEE_EXIT
preparado em experiments/construction/020-overlay/manual-instructions.md.
Aguardar execução pessoal de Francisco; nenhum manual humano executado pelo
agente. Esta autorização limitada de preparação não presume aprovação de
Implement/Test nem revisão Waheed/integração. Bolt mantém-se aberto, sem staging,
commits/push ou DONE e sem gates externos adicionais.

## Validação interrompida — WIP e ponto de retoma

Registo em 2026-11-10T19:05:26Z, por pedido explícito do utilizador. Pausa da validação;
manual humano parcial, não aprovado, e Test por aprovar. Quatro logs pessoais
iniciais/ficheiros de saída preservados, sem inferir roteiro concluído.
clean verify já executado: 981/zero falhas/erros/ignorados. Ponto de retoma em
experiments/construction/020-overlay/RESUME.md. Guardar WIP local só deste
incremento/evidência; target/ e alterações alheias excluídos; sem push.
Bolt permanece in-progress/current_stage=implement/completed=null; só Plan em
stages_completed. História não implementada, TASK-008 IN PROGRESS. Revisão final
Waheed e integração continuam pendentes; sem gates externos adicionais ou DONE.

## IMP-005 — Implement aprovado; Test para revisão

Aprovação de Implement registada em 2026-11-10T22:05:26Z. O [relatório de Test](test-walkthrough.md)
consolida V-008, clean verify (981 testes, zero falhas/erros/ignorados), o manual
de Luis e a verificação complementar do agente, com as sondagens preliminares
preservadas. Test aguarda aprovação. A revisão final de Waheed e a integração
da versão revista continuam pendentes.

## IMP-005 — Test aprovado, revisão final pendente

A aprovação do utilizador foi registada em 2026-11-10T22:22:56Z. As três etapas
estão concluídas. V-008, o manual de Luis, a verificação complementar e
V-ADD após Q-C-13 estão aceites. O bolt e a TASK-008 ficam em progresso até à
revisão final de Waheed e à integração da versão revista.
[Pacote para revisão final](closure-walkthrough.md).
