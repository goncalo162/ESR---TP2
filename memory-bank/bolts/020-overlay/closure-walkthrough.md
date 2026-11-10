# IMP-005 / TASK-008 — Preparação do fecho

Test aprovado pelo utilizador, com registo em 2026-11-10T22:22:56Z. Denji é o
responsável pela implementação e Waheed é o revisor final atribuído.
A revisão incide sobre a versão publicada em Denji com este documento.
A TASK-008 e o bolt permanecem em progresso. O WIP `b425ad3` é a base
da publicação de revisão.

## Evidência para a revisão

- Os cinco ficheiros Java do IMP-005 têm +1346/−14 face à base operacional
  `90cded6`. O utilizador confirmou que as 14 remoções pertencem à adaptação
  de ciclo de vida autorizada em Q-C-09.
  [Diff Java](../../../experiments/construction/020-overlay/test-code-vs-operational-base.diff).
- V-008 e o clean verify de 981 testes foram aceites com os sete XML Surefire,
  o manual de Luis e a verificação complementar do agente.
  [Relatório de Test](test-walkthrough.md).
- Q-C-13 corrigiu a ligação quebrada em `.specsmd/`. O utilizador confirmou
  saída 0, zero erros e 45 fixtures numa repetição no repositório.
  [V-ADD aprovado](../../../experiments/construction/020-overlay/q-c-13-v-add.log).

## Condições de fecho

1. Waheed aceita a versão final e a revisão fica registada com o hash revisto.
2. A versão revista é integrada na base comum e a integração fica comprovada.
3. Bolt, história, TASK-008, índices e progresso são atualizados com a evidência
   da revisão e da integração, segundo a [Definition of Done](../../../docs/14-DEFINITION-OF-DONE.md).
