# TASK-034 — Extra — reparar ligações com candidatos aprendidos

## 1. Identificação

- ID: TASK-034
- Fase: PHASE-02, EXTRA-01 posterior ao fecho da base.
- Estado: IN PROGRESS
- Natureza: Extra solicitado pelo utilizador.
- Responsável proposto: Denji.
- Revisor proposto: Fillmore.
- Dependências: [TASK-033](TASK-033.md).
- Fonte: [REQ-025](../../../specs.md#req-025), [IMP-035](../../../add.md#imp-035).

## 2. Objetivo e âmbito

Usar candidatos aprendidos no IMP-034 para criar as arestas S–C e R1–C dos dois cenários. O segundo cenário conserva S–R1 e permite reparação unilateral. A reconstrução precede 005-rotas e a prova de vídeo segue a implementação de routing/streaming.

## 3. Critérios de aceitação

- [ ] Manter descoberta obrigatória e controlo exclusivamente TCP v2 em todos os nós, preservando as garantias e os testes existentes, sem modo base opcional ou fallback.
- [ ] Após aprendizagem pelos vizinhos e morte dos relays, formar S–C no primeiro cenário e R1–C no segundo, preservando S–R1.
- [ ] Candidatos aprendidos, grau, tentativas e temporizadores têm limites verificáveis e uma sessão confirmada por par.
- [ ] Callbacks antigos e reparações concorrentes ou assimétricas não apagam ligações novas nem ultrapassam os limites.
- [ ] Sem candidatos aprendidos válidos, manter estado explícito de isolamento e tentativas limitadas.
- [ ] V-034, regressão atual e ensaio de controlo executados com evidência. A prova de vídeo fica ligada à integração futura de routing e streaming.

## 4. Validação e fecho

V-034: após aprendizagem real, matar sequencialmente os relays nos grafos S–R1–C / S–R2–C e S–R1–R2–C / S–R1–R3–C forma respetivamente S–C e R1–C, com mesmos sobreviventes e bootstrap parado. Testar sessão única, reparação unilateral, callbacks antigos, candidato indisponível, cache vazia ou expirada e shutdown limitado.
Estado: V-034 executado, Test aprovado por Luis; revisão humana final/fecho pendentes.
ADR-033 residual/ADR-034 aceites não dispensam G-EXTRA-ADR próprio do IMP-035:
aprovar intenção/admissão/reparação e âmbito antes de Implement.
Revisão e integração precedem DONE.
[Bolt](../../../memory-bank/bolts/035-overlay-extra/bolt.md).

## 5. Entrada em Model

Registo de 2026-11-11T22:22:21Z. Pré-requisito integrado em main local, base 22622c1.
Apenas documentação de Model; reparação ao ficar isolado, sem desativação de
descoberta ou fallback v1. [Model/M01–M18](../../../memory-bank/bolts/035-overlay-extra/ddd-01-domain-model.md).
Nenhuma execução V-034/Java/Maven/manual nova. Parar antes de Design.

## IMP-035 — Model aprovado; Design para revisão

Registo de 2026-11-11T22:41:27Z. O utilizador aprovou Model e clarificou que o destino
pode estabelecer a ligação pedida pelo isolado, conservando sessões saudáveis.
[Design proposto/Q-C-21](../../../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md) concretiza intenção/admissão unilateral,
permissões anteriores às falhas, promoção/retorno pelo menor ID, limites e
D01–D24. Âmbito8→30 e minuta para futuraADR-035 ainda propostos; o contrato
integrado/206 hashes de Projeto/JAR continuam intactos.
Só Model concluído, current_stage=design. Gate próprio, Design/ADR/Implement/
V-034/manual/revisão final/integração pendentes. Sem Java/Maven/commits/push.

## IMP-035 — Design direto simplificado para revisão

Registo de 2026-11-11T23:17:55Z. Revisão pedida pelo utilizador: isolado inicia e completa
na mesma ligação mesmo com ID maior, sem retorno. [Design/Q-C-22](../../../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md)
propõe um tipo20/ACK6, admissão cooperativa sem permissões/registry e eleição
do menor originator só entre saídas pendentes; ACTIVE nunca substituída.
Âmbito8→21/ADR-035/contratos ainda propostos; versão anterior preservada nas
evidências. Só Model concluído; nenhum Java/Maven/V-034/Implement/Test/commit/push.

## IMP-035 — revisão ADR para aprovação antes de Implement

Registo de 2026-11-11T23:28:20Z. Model/Design concluídos; Design revisto, Q-C-22/
política cooperativa e21 caminhos aprovados explicitamente. [RevisãoADR](../../../memory-bank/bolts/035-overlay-extra/adr-review.md)
formaliza ADR-035 Proposta e substituição parcial019/033/034, mesma ligação e
ACTIVE preservada. APIs reais conferidas sem alteração de fontes.
Só revisão ADR autorizada; aprovação formalADR/G-EXTRA-ADR ainda pendentes.
V-034/regressão/manual humano com mortes reais/revisão final/integração obrigatórios.
Sem Implement/Test/fecho/commits/push/outro IMP.

## IMP-035 — Implement para revisão antes de Test

Registo de 2026-11-12T00:04:01Z. Revisão ADR/ADR-035/substituição parcial019/033/034 aprovadas
explicitamente; ADR-035 Aceite e G-EXTRA-ADR satisfeito para IMP-035.
[Walkthrough, mapa D01–D20 e evidência incremental](../../../memory-bank/bolts/035-overlay-extra/implementation-walkthrough.md).
Reparação20/6 no mesmo TCP v2, candidatos aprendidos, menor originator só pendente,
ACTIVE preservada e quotas existentes. 78 fixtures operacionais, snapshots/UDP intactos.
Sem clean verify ou manual humano de mortes reais nesta etapa; V-034 completo,
regressão, revisão final e integração pendentes. Não marcar DONE nem iniciar Test.
Sem commits, push ou outro IMP; 005-rotas continua bloqueado.

## IMP-035 — Test à espera de repetição humana

Registo 2026-11-12T00:39:35Z. Implement aprovado; [Test](<../../../memory-bank/bolts/035-overlay-extra/ddd-03-test-report.md>) executou clean verify
JDK21/1113 testes sem falhas e preservou11 XML/JAR novo. Manual Codex68
CHECK_PASS/MANUAL_PASS: JVM por nó, mortes SIGKILL sequenciais, bootstrap
parado, S–C/R1–C únicos, sobreviventes originais e S–R1 preservada. Duas tentativas
incompletas mantidas. Execução pessoal de Luis/aprovação de Test/revisão final/
integração pendentes. Sem DONE/commits/push ou início de outro IMP.

## IMP-035 — Test aprovado; integração autorizada

Registo 2026-11-12T10:35:23Z. Luis aprovou Test/1113 e o manual humano20261112T102931Z-pr_nynth:
66 CHECK_PASS/MANUAL_PASS, passed=true/no_processes_pending=true. Execução
humana anterior preservada; nenhuma repetição. Cinco etapas concluídas.
[Resumo para revisão humana final](<../../../memory-bank/bolts/035-overlay-extra/final-review-summary.md>). Commit/main autorizados;
revisão humana final da implementação e fecho pendentes. Bolt/história/unidade
continuam in-progress, sem implemented=true/DONE ou início de005-rotas.
