---
id: "035-overlay-extra"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "planned"
stories: ["007-reparar-vizinhancas-com-candidatos-aprendidos"]
created: "2026-11-11T12:26:03Z"
updated: "2026-11-11T14:33:20Z"
started: null
completed: null
current_stage: null
stages_completed: []
requires_bolts: ["034-overlay-extra"]
enables_bolts: ["005-rotas"]
requires_units: ["001-base-validada"]
blocks: true
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
adr_ids: ["ADR-013", "ADR-014", "ADR-017", "ADR-018", "ADR-019", "ADR-020", "ADR-024", "ADR-033", "ADR-034"]
requirements: ["REQ-025"]
architecture_source: "add.md"
extra: true
---
# Bolt 035-overlay-extra — Extra — reparar ligações com candidatos aprendidos

EXTRA-01, parte B, reparação.
[IMP-035](../../../add.md#imp-035) é a fonte normativa.
[História](../../intents/001-ott-streaming/units/002-overlay/stories/007-reparar-vizinhancas-com-candidatos-aprendidos.md).

## Etapas

1. [ ] **model** → `ddd-01-domain-model.md`.
2. [ ] **design** → `ddd-02-technical-design.md`.
3. [ ] **adr** → `adr-review.md`.
4. [ ] **implement** → `implementation-walkthrough.md`.
5. [ ] **test** → `ddd-03-test-report.md`.

## Critérios de aceitação

- [ ] Com o extra desativado, conservar o comportamento e os testes da overlay base.
- [ ] Após aprendizagem pelos vizinhos e morte dos relays, formar S–C no primeiro cenário e R1–C no segundo, preservando S–R1.
- [ ] Candidatos aprendidos, grau, tentativas e temporizadores têm limites verificáveis e uma sessão confirmada por par.
- [ ] Callbacks antigos e reparações concorrentes ou assimétricas não apagam ligações novas nem ultrapassam os limites.
- [ ] Sem candidatos aprendidos válidos, manter estado explícito de isolamento e tentativas limitadas.
- [ ] V-034, regressão atual e ensaio de controlo executados com evidência. A prova de vídeo fica ligada à integração futura de routing e streaming.

## Estado e fronteiras

Planeado. Aguardar a conclusão e integração de 034-overlay-extra antes de iniciar.
O contrato de divulgação, a confiança, os limites e a negociação da extensão
aguardam Design/ADR-033. A base v1 conserva os bytes e os códigos.
V-034 por executar. A prova de vídeo ocorre após routing/streaming.
