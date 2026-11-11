# TASK-034 — Extra — reparar ligações com candidatos aprendidos

## 1. Identificação

- ID: TASK-034
- Fase: PHASE-02, EXTRA-01 posterior ao fecho da base.
- Estado: TODO
- Natureza: Extra solicitado pelo utilizador.
- Responsável proposto: Denji.
- Revisor proposto: Fillmore.
- Dependências: [TASK-033](TASK-033.md).
- Fonte: [REQ-025](../../../specs.md#req-025), [IMP-035](../../../add.md#imp-035).

## 2. Objetivo e âmbito

Usar candidatos aprendidos no IMP-034 para criar as arestas S–C e R1–C dos dois cenários. O segundo cenário conserva S–R1 e permite reparação unilateral. A reconstrução precede 005-rotas e a prova de vídeo segue a implementação de routing/streaming.

## 3. Critérios de aceitação

- [ ] Com o extra desativado, conservar o comportamento e os testes da overlay base.
- [ ] Após aprendizagem pelos vizinhos e morte dos relays, formar S–C no primeiro cenário e R1–C no segundo, preservando S–R1.
- [ ] Candidatos aprendidos, grau, tentativas e temporizadores têm limites verificáveis e uma sessão confirmada por par.
- [ ] Callbacks antigos e reparações concorrentes ou assimétricas não apagam ligações novas nem ultrapassam os limites.
- [ ] Sem candidatos aprendidos válidos, manter estado explícito de isolamento e tentativas limitadas.
- [ ] V-034, regressão atual e ensaio de controlo executados com evidência. A prova de vídeo fica ligada à integração futura de routing e streaming.

## 4. Validação e fecho

V-034: após aprendizagem real, matar sequencialmente os relays nos grafos S–R1–C / S–R2–C e S–R1–R2–C / S–R1–R3–C forma respetivamente S–C e R1–C, com mesmos sobreviventes e bootstrap parado. Testar sessão única, reparação unilateral, callbacks antigos, candidato indisponível, cache vazia ou expirada e shutdown limitado.
Estado: NÃO EXECUTADO. Resolver ADR-033 e os contratos afetados antes
de Implement. Revisão e integração precedem DONE.
[Bolt](../../../memory-bank/bolts/035-overlay-extra/bolt.md).
