---
id: "035-overlay-extra"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "in-progress"
stories: ["007-reparar-vizinhancas-com-candidatos-aprendidos"]
created: "2026-11-11T12:26:03Z"
updated: "2026-11-12T10:35:23Z"
started: "2026-11-11T22:20:02Z"
completed: null
current_stage: null
stages_completed: [{"name":"model","completed":"2026-11-11T22:35:39Z","artifact":"ddd-01-domain-model.md"},{"name":"design","completed":"2026-11-11T23:28:20Z","artifact":"ddd-02-technical-design.md"},{"name":"adr","completed":"2026-11-11T23:39:43Z","artifact":"adr-review.md"},{"name":"implement","completed":"2026-11-12T00:28:31Z","artifact":"implementation-walkthrough.md"},{"name":"test","completed":"2026-11-12T10:35:23Z","artifact":"ddd-03-test-report.md"}]
requires_bolts: ["034-overlay-extra"]
enables_bolts: ["005-rotas"]
requires_units: ["001-base-validada"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 3
  max_dependencies: 2
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-034"]
source_task_order: ["TASK-034"]
implementation_tasks: ["IMP-035"]
size: "M"
validation_ids: ["V-034"]
test_ids: ["V-034"]
external_gates: ["G-EXTRA-ADR"]
gate_status: {"G-EXTRA-ADR":"satisfied for IMP035; ADR035/partial substitution/contracts approved"}
adr_ids: ["ADR-013", "ADR-014", "ADR-017", "ADR-018", "ADR-019", "ADR-020", "ADR-024", "ADR-033", "ADR-034", "ADR-035"]
requirements: ["REQ-025"]
architecture_source: "add.md"
extra: true
---
# Bolt 035-overlay-extra — Extra — reparar ligações com candidatos aprendidos

EXTRA-01, parte B, reparação.
[IMP-035](../../../add.md#imp-035) é a fonte normativa.
[História](../../intents/001-ott-streaming/units/002-overlay/stories/007-reparar-vizinhancas-com-candidatos-aprendidos.md).

## Etapas

1. [x] **model** → `ddd-01-domain-model.md`.
2. [x] **design** → `ddd-02-technical-design.md`.
3. [x] **adr** → `adr-review.md`.
4. [x] **implement** → `implementation-walkthrough.md`.
5. [x] **test** → `ddd-03-test-report.md`.

## Critérios de aceitação

- [ ] Manter descoberta obrigatória e controlo exclusivamente TCP v2 em todos os nós, preservando as garantias e os testes existentes, sem modo base opcional ou fallback.
- [ ] Após aprendizagem pelos vizinhos e morte dos relays, formar S–C no primeiro cenário e R1–C no segundo, preservando S–R1.
- [ ] Candidatos aprendidos, grau, tentativas e temporizadores têm limites verificáveis e uma sessão confirmada por par.
- [ ] Callbacks antigos e reparações concorrentes ou assimétricas não apagam ligações novas nem ultrapassam os limites.
- [ ] Sem candidatos aprendidos válidos, manter estado explícito de isolamento e tentativas limitadas.
- [ ] V-034, regressão atual e ensaio de controlo executados com evidência. A prova de vídeo fica ligada à integração futura de routing e streaming.

## Estado e fronteiras

Model/Design/ADR aprovados; ADR-035 Aceite e G-EXTRA-ADR satisfeito para IMP-035.
Test aprovado por Luis, incluindo execução humana66/MANUAL_PASS.
As cinco etapas estão concluídas; fecho pendente de revisão humana final.
O âmbito corrente aprovado é21 caminhos.
Histórico das etapas abaixo preservado por ordem de registo. V-034 completo,
regressão atual, manual humano com mortes reais, revisão final e integração pendentes.

## Dependência integrada

2026-11-11T19:45:51Z: IMP-034 complete e integrado em main local. Pré-requisito disponível; este bolt permanece planned, sem etapas iniciadas ou aprovação presumida do seu gate.

## Checkpoint atual — Model para revisão

Registo de 2026-11-11T22:22:21Z. Início apenas de Model autorizado pelo utilizador (opção 1).
[Modelo de domínio](ddd-01-domain-model.md) com M01–M18 propostos para V-034;
Q-C-20 do gatilho decidida explicitamente. Dependência integrada em
22622c1d7eb0fdab3df684f2fac10263cff11736, árvore limpa à entrada. Os 206 caminhos
não gerados de Projeto e JAR do pré-requisito são conferidos por hash, sem
análise/alteração Java ou execução Maven. Model não aprovado, stages_completed
vazio; parar antes de Design. Gate próprio de intenção/admissão pendente;
contrato/âmbito adicional e eventual ADR nova só em revisão posterior.
Sem Test/fecho/commits/push ou início de outro IMP.

## Aprovação de Model e entrada apenas em Design

Registo de 2026-11-11T22:35:39Z (instante do registo). O utilizador aprovou Model e
clarificou atendimento: o destino pode estabelecer a ligação pedida pelo nó
isolado, conservando sessões existentes. Manter S–C e S–R1–C com candidatos
aprendidos antes das falhas e S–R1 preservada. G-EXTRA-ADR e revisão de
contratos/ADR continuam obrigatórios antes de Implement. Só Model concluído;
sem aprovação de Design/ADR/Implement/Test, commits ou push.

## Checkpoint atual — Design para revisão

Registo de 2026-11-11T22:41:27Z. [Design técnico](ddd-02-technical-design.md) com D01–D24
para M01–M18/V-034 e [ampliação proposta8→30](scope-change-proposal.md).
Q-C-21 apresenta permissões prévias, quatro tipos adicionais, snapshot com prova,
promoção/retorno conforme menor ID e terminated aditivo; tudo proposto, sem
alterar contratos operacionais. Só Model em stages_completed.
G-EXTRA-ADR, âmbito e revisão da futura ADR-035 pendentes antes de Implement.
Parar antes de ADR/Java/Test/fecho/commits/push.

## Checkpoint atual — Design simplificado para revisão

Registo de 2026-11-11T23:17:55Z. O utilizador não aprovou a primeira proposta e pediu
reparação na ligação iniciada pelo isolado, sem retorno. [Design revisto](ddd-02-technical-design.md),
[ADR-035 Proposta](adr-035-proposal.md) e [âmbito8→21](scope-change-proposal.md)
reduzem quatro tipos a um20 REPAIR_HELLO/ACK6, retiram permissões/registry e
propõem menor ID só como desempate pendente, nunca substituindo ACTIVE.
Q-C-22 explicita a confiança cooperativa unilateral. Só Model concluído;
Design/ADR/contratos/âmbito pendentes. Sem Java/Implement/Test/commits/push.

## Aprovação de Design e entrada apenas em revisão ADR

Registo de 2026-11-11T23:28:20Z (instante do registo, não data atribuída à mensagem).
O utilizador aprovou Design revisto, política cooperativa Q-C-22 e 21 caminhos.
Origem valida cache/proveniência/TTL; destino valida handshake/limites sem
comprovar aprendizagem. Mesma ligação, desempate só pendente e ACTIVE preservada.
ADR-035 formalizada como Proposta, substituição parcial019/033/034;
[revisão para aprovação](adr-review.md). Só Model/Design concluídos.
Parar antes de Implement. V-034/regressão/manual humano com mortes reais,
revisão final e integração continuam obrigatórios; sem commits/push/outro IMP.

## Aprovação ADR e entrada apenas em Implement

Registo de 2026-11-11T23:39:43Z. ADR-035/substituição parcial aprovadas; gate satisfeito.
21 caminhos/D01–D20 incrementais autorizados, preservar testes e correlação.
Parar antes de Test. V-034/regressão completa/manual humano com mortes reais/
revisão final/integração pendentes; sem commits/push/outro IMP.

## IMP-035 — Implement para revisão antes de Test

Registo de 2026-11-12T00:04:01Z. Revisão ADR/ADR-035/substituição parcial019/033/034 aprovadas
explicitamente; ADR-035 Aceite e G-EXTRA-ADR satisfeito para IMP-035.
[Walkthrough, mapa D01–D20 e evidência incremental](implementation-walkthrough.md).
Reparação20/6 no mesmo TCP v2, candidatos aprendidos, menor originator só pendente,
ACTIVE preservada e quotas existentes. 78 fixtures operacionais, snapshots/UDP intactos.
Sem clean verify ou manual humano de mortes reais nesta etapa; V-034 completo,
regressão, revisão final e integração pendentes. Não marcar DONE nem iniciar Test.
Sem commits, push ou outro IMP; 005-rotas continua bloqueado.

## Implement — correção de grau para revisão

Registo 2026-11-12T00:23:42Z. [Provas e diffs suplementares](../../../experiments/construction/035-overlay-extra/implement-degree-walkthrough.md).
Guarda comum no commit de ACTIVE≤min(C,63), sem retirar saudáveis.
175 incrementais passaram; as evidências anteriores preservadas. Implement
continua em revisão, sem Test/commits/push. Regressão completa, manual humano
com mortes reais, revisão final e integração pendentes.

## Implement aprovado — entrada apenas em Test

Aprovação explícita registada em 2026-11-12T00:28:31Z. Clean verify/JDK21/JAR novo e
manual V-034 com mortes reais nos dois grafos autorizados. Parar antes da
aprovação de Test para repetição humana. Sem fecho/commits/push/outro IMP.

## Checkpoint Test — repetição humana pendente

Registo 2026-11-12T00:39:35Z. [Relatório](ddd-03-test-report.md): clean verify/JDK21/1113 testes,
11 XML/JAR novo preservados; manual Codex68 CHECK_PASS/MANUAL_PASS, dois grafos
com SIGKILL real e S–R1 preservada. Duas tentativas incompletas arquivadas.
Aguardar execução pessoal de Luis antes da aprovação de Test. Sem DONE/fecho/
commits/push/outro IMP; revisão final e integração pendentes.
