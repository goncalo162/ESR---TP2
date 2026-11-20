---
id: "008-arvores"
unit: "004-arvores"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "in-progress"
stories: ["003-recuperar-arvores-apos-falha-de-upstream"]
created: "2026-11-02T15:02:08Z"
updated: "2026-11-20T16:07:46Z"
started: "2026-11-20T10:52:56Z"
completed: null
current_stage: "test"
stages_completed: [{"name": "model", "completed": "2026-11-20T11:07:54Z", "artifact": "ddd-01-domain-model.md"}, {"name": "design", "completed": "2026-11-20T11:41:00Z", "artifact": "ddd-02-technical-design.md"}, {"name": "adr", "completed": "2026-11-20T11:55:33Z", "artifact": "adr-review.md"}, {"name": "implement", "completed": "2026-11-20T15:59:41Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-20T16:17:51Z", "artifact": "ddd-03-test-report.md"}]
requires_bolts: ["022-rotas", "023-arvores"]
enables_bolts: ["024-arvores"]
requires_units: ["003-rotas"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-04"
source_tasks: ["TASK-016"]
source_task_order: ["TASK-016"]
implementation_tasks: ["IMP-013"]
size: "M"
validation_ids: ["V-016"]
test_ids: ["V-016"]
external_gates: []
adr_ids: ["ADR-005", "ADR-020", "ADR-024", "ADR-025"]
requirements: ["CON-008", "INT-001", "INT-002", "NFR-003", "REQ-002", "REQ-006", "REQ-009"]
architecture_source: "add.md"
---
# Bolt 008-arvores — Recuperar falhas do upstream TCP

## Fonte normativa e âmbito

Ler [IMP-013](../../../add.md#imp-013) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-016](../../../docs/tasks/PHASE-04/TASK-016.md).

## História, dependências e gates

- [ ] [003-recuperar-arvores-apos-falha-de-upstream](../../intents/001-ott-streaming/units/004-arvores/stories/003-recuperar-arvores-apos-falha-de-upstream.md).
- Dependências: [022-rotas](../022-rotas/bolt.md), [023-arvores](../023-arvores/bolt.md).
- Unidade: [004-arvores](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-013 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-013 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-013 e as ADR do ADD.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-013 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-013 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [ ] Há recuperação quando existe caminho até à mesma fonte.
- [ ] Ausência de caminho é visível e não produz laço.
- [ ] Estado antigo não é reativado por mensagens atrasadas.
- [ ] Validação V-016 executada e evidência ligada no progresso.

## Evidência e fecho

- Testes: V-016, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-013](../../../add.md#imp-013).
- Prova futura: `experiments/construction/008-arvores/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Model preparado — aprovação pendente

Registo 2026-11-20T11:02:06Z, após início explícito em2026-11-20T10:52:56Z.
[Modelo de domínio](ddd-01-domain-model.md): intenção/autoridade por stream,
identidades, épocas, ACK/barreira de envio, alternativas até à mesma fonte,
isolamento e recursos físicos. Q-C-43/44 propostas, aguardando decisão.
R01–R18/V-016 planeados/não executados;1379 testes do IMP-012 apenas históricos.
Nenhuma fonte Java/teste/fundação lida, analisada ou alterada; sem Maven,
Design/ADR/Implement/Test, staging, commits, push ou outro IMP.
current_stage=model,stages_completed=[],sem aprovação ou conclusão presumida.
Fillmore mantém revisão final atribuída; sem gate externo adicional.


## Model e Q-C-43/44 aprovados — apenas Design autorizado

Registo 2026-11-20T11:07:54Z: aprovação explícita do utilizador. Concretizar novidade útil
para rearme, limites de rondas e renegociação automática dos filhos quando muda
o caminho, sem intervenção humana ou extensão dos prazos antigos. Preservar
os três caminhos do ADD; qualquer ampliação exige apresentação prévia antes de
alterar código. V-016/R01–R18 não executados. Sem Implement, commits ou push.
Fecho exige testes, regressão JDK21, manual humano, Fillmore e integração,
sem gates externos adicionais. As pendências de Model acima são histórico.

## Design preparado — aprovação pendente em 2026-11-20T11:21:05Z

[Design técnico](ddd-02-technical-design.md): novidade estrutural e geração ANN
posterior a certas falhas, watermark e crédito coalescido; T/A existentes,
no máximo uma ronda automática por T, sem catch-up. Filhos renegociam sozinhos
por ACTIVATE fresco/revisão maior/requestId novo após perda efetiva; nenhum ACK
espontâneo ou extensão do grant antigo. Grant saudável preservado até perder
validade, sem antecipar troca por melhoria de IMP-014.
Três caminhos mantidos; propostas de projeção/construtores4/5/correlação e
lifecycle por verificar nas APIs reais durante futura revisão ADR autorizada.
V-016/R01–R18 por executar. Só Model em stages_completed; Design não aceite,
sem ADR/Implement/Test, fontes lidas/alteradas, Maven, commits ou push.
Fecho: testes, regressão JDK21, manual humano, Fillmore e integração, sem gates novos.


## Design revisto — apenas RESOURCE_LIMIT temporário em 2026-11-20T11:34:53Z

Clarificação explícita do utilizador incorporada na secção4/R13 do Design e
Q-C-44: ANN validado posterior à falha pode dar crédito de admissão com o mesmo
F, sem callback/duplicado reabrir terminal. Watermark bloqueia prova anterior
mesmo sem envio; capacidade/T/A/cadência e restantes políticas mantidos.
Sem novo pedido humano, extensão de prazo antigo, âmbito/Java/ADR/Implement.
Design reapresentado e ainda por aprovar; só Model concluído. V-016/R01–R18
por executar; testes/JDK21/manual/Fillmore/integração e external_gates=[] mantidos.


## Design revisto aprovado — apenas revisão ADR em 2026-11-20T11:41:00Z

Utilizador aprovou Design e clarificação Q-C-44. Conferir APIs reais de geração/
watermark, intenção, revisões ACTIVATE, construtores/projeções e lifecycle.
Três caminhos do ADD preservados; qualquer impedimento/ampliação apresentado
antes de código. Sem Implement, commits ou push. V-016/R01–R18 não executados;
regressão JDK21, manual humano, Fillmore e integração para fecho; sem gates novos.

## IMP-013 — Design revisto aprovado; revisão ADR preparada

Registo 2026-11-20T11:45:38Z. Design revisto e clarificação de Q-C-44 aprovados
pelo utilizador em2026-11-20T11:41:00Z; apenas revisão ADR autorizada.
APIs reais conferidas estaticamente: announceSeq/Outcome disponíveis, watermark
interno privado sem necessidade de API nova; intenção distinta de resultado do
ticket, mudança de época, ACTIVATE/revisões, construtores4/5/projeções e lifecycle.
Revisão para aprovação, sem impedimento de âmbito identificado ou nova ADR.
A01–A08 são trabalho futuro nos mesmos três caminhos; nenhuma correção aplicada.
V-016/R01–R18 não executados; fontes só consultadas, sem Java alterado, Maven,
Implement/Test, fecho, staging, commits, push ou outro IMP. Negativos/provas
anteriores preservados. Fecho exige testes, regressão JDK21, manual humano,
revisão final Fillmore e integração, sem gates externos adicionais.
[Revisão completa](adr-review.md).

## Revisão ADR aprovada — apenas Implement

Registo 2026-11-20T11:55:33Z: autorização explícita para A01–A08 nos três caminhos do ADD.
Preservar testes históricos, intenção/correlações/construtores e reservas físicas
entre gerações. Oráculos controlados e integração TCP pertinente autorizados
como verificação incremental. Seguir docs/style/CODESTYLE.md. Sem etapa Test,
fecho, commits, push ou outro IMP; qualquer ampliação será apresentada antes de
alterar ficheiros. V-016 consolidado, regressão JDK21, manual humano antes de
aprovar Test, Fillmore e integração continuam obrigatórios; sem gates novos.

## IMP-013 — Implement reapresentado, sem aceitação/Test

Registo 2026-11-20T12:27:18Z. Utilizador aprovou ADR e autorizou apenas Implement A01–A08
nos três caminhos do ADD, com CODESTYLE, históricos/correlações/construtores e
reservas físicas preservados. Implement preparado para revisão: regressão JDK21
incremental final1414/0/0/0,20 XML,35 testes novos e1379 históricos byte intactos.
Duas integrações TCP reais com métricas controladas passaram; negativos guardados
separadamente, incluindo R13, prazo de fila, R18/fixture, R17 inicial sem causa
estabelecida e alternativa consumida R10. Remoções/substituições justificadas no
walkthrough; sem teste/asserção/prazo histórico retirado ou fundação alterada.
Só Model/Design/ADR em stages_completed; current_stage=implement, completed=null.
Implement ainda por aceitar; sem etapa Test, fecho, commits/push ou outro IMP.
V-016 consolidado, regressão da etapa Test, manual humano antes de aprovar Test,
revisão final Fillmore e integração continuam obrigatórios; sem gates novos.
[Walkthrough e prova](implementation-walkthrough.md).

## IMP-013 — R10 corrigido; Implement permanece pendente

Registo 2026-11-20T15:44:31Z. Implement continua pendente. Reprodução exata com G2,
Long.MAX_VALUE-1000, métrica10→2000→10 após PATH_CHANGED/T3s: negativo preservado
esperado1/observado2 ACTIVATEs. A transição previous==null agora confere blocked
antes de conceder crédito; métrica isolada não reutiliza a prova bloqueada.
Primeira medição válida, anúncio novo posterior e T/A/cadência preservados.
11 testes R10 focados passaram; regressão JDK21 atual1419/0/0/0,20 XML,
40 RecoveryTest (35 anteriores intactos+5 novos),1379 históricos intactos.
Delta Java só em RoutingService e RecoveryTest; Route/fundações inalterados.
R17 inicial mantém causa não estabelecida. Sem aceitação de Implement, Test,
fecho, commits ou push. V-016 consolidado, manual humano, Fillmore e integração
pendentes; sem gates externos adicionais.
[Walkthrough atualizado](implementation-walkthrough.md).

## Implement aprovado — apenas Test autorizado

Registo 2026-11-20T15:59:41Z: aprovação explícita de Implement, incluindo R10. Executar
clean verify JDK21/JAR SHA256 e consolidar V-016/R01–R18, preservar código e
evidência, preparar manual humano e aguardar repetição pessoal antes de pedir
aprovação Test. R17 inicial continua sem causa estabelecida; métricas
controladas não a demonstram. Correção nova exige reprodução/âmbito apresentados.
Sem fecho/commits/push/outro IMP; Fillmore/integração pendentes, sem gates novos.

## IMP-013 — Test técnico, aguardar manual humano

Registo 2026-11-20T16:07:46Z. Implement/R10 aprovados; apenas Test autorizado.
Clean verify JDK21 novo1419/0/0/0,20 XML e JAR504997bytes/SHA256
81779be388778fd7dfacba5957e74ccffe5da45e86a403ea1ec5626f52c8f49d.
V-016/R01–R18 consolidado tecnicamente; fontes/históricos/evidências intactos.
Manual humano preparado:7 etapas/28 checks. Ensaio kind=agent passou no JAR
congelado, hashes/origem/JVM conferidos; não satisfaz obrigação humana.
Aguardar repetição pessoal antes de pedir aprovação Test. Test não aprovado;
R17 inicial sem causa estabelecida, integração com métricas controladas não
prova a causa. Nova correção exige reprodução/âmbito apresentados.
Sem fecho/staging/commits/push/outro IMP; Fillmore/integração pendentes,
sem gates externos adicionais.
[Relatório Test](ddd-03-test-report.md).

## Test/manual Luis e revisão Fillmore aprovados

Registo 2026-11-20T16:17:51Z: utilizador confirmou execução pessoal das sete etapas/28 checks
e aprovou Test. Relatório161206Z-w1jlv9q6 conferido por hash/origem/JVM.
Fillmore reviu e aprovou a mesma versão sem alterações, por confirmação explícita
do utilizador. Commit/push autorizados; integrar e comprovar antes de marcar
complete/DONE. R17 inicial sem causa estabelecida; negativos preservados.
Nenhum outro IMP iniciado.
