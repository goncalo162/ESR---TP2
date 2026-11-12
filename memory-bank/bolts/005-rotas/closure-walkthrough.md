# IMP-007 / 005-rotas / TASK-010 — fecho documental

Registo 2026-11-12T22:37:24Z. Fecho autorizado explicitamente pelo utilizador após prova da
integração local em main: `f9905038c614612b6f2798d4eca46dd72934743f`. Não se inventa entrega, upload ou aprovação.
A unidade003-rotas e a PHASE-03 continuam em progresso; outros IMP não iniciados.

## Critérios e evidência cumpridos

- Test aprovado: clean verify1176 testes,0falhas/0erros/0ignorados;
  [resultado/JAR](../../../experiments/construction/005-rotas/test-build.json),
  [log](../../../experiments/construction/005-rotas/test-clean-verify.log) e
  [XML](../../../experiments/construction/005-rotas/test-clean-verify-xml/).
- V-010/M01–M22/D01–D16 exercitados e [mapeados](test-oracles.md), incluindo
  relógio/transporte falsos, TCP/JVM/reparação20 reais e shutdown físico limitado.
- Manual humano Francisco execução02 cumprido com os limites aceites: quatro
  saídas0, SIGKILL9/137 intencional, processos registados terminados/logger sem
  erros e cinco portas disponíveis no bind final; PING/PONG/identidades conferidos.
  [Conferência/limites](../../../experiments/construction/005-rotas/manual-human-02-review.md).
- Fillmore realizou e aprovou a revisão final da implementação e evidência atuais,
  incluindo Q-C-25, sem alterações pendentes, por confirmação explícita do utilizador.
  [Confirmação vinculada aos hashes](../../../experiments/construction/005-rotas/fillmore-final-review-confirmation.json).
- Integração:348 caminhos e hashes staged/commit iguais ao pacote revisto,
  árvorec8976e7287a0185b601806313a18c4884d7d54ee. V-ADD0 em cópia extraída
  do commit efetivo, sem overlay do working tree;3880 ficheiros verificados.
  [Prova estruturada](../../../experiments/construction/005-rotas/integration-proof.json),
  [resumo](../../../experiments/construction/005-rotas/integration-proof.md),
  [V-ADD do commit](../../../experiments/construction/005-rotas/integration-proof/commit-v-add.log).

Esta evidência posterior à integração é incluída agora no commit documental,
sem alterar os bytes originais da prova, logs, XML, JAR ou fontes aprovadas.
JAR330491bytes, SHA2563f39cab445ca05360525691b15c8f7193b7e0186b8033ef8cbd86d50690cfb15.
Q-C-23/24 preservadas; Q-C-25 só a cadeia exata dos dois patches autorizados.
Sem mudança de Java/NeighborManager/send/wire/caps, dependência ou ferramenta.

## Estados de fecho

Bolt complete, cinco etapas aprovadas; história complete/implemented=true,
TASK-010 DONE. Nove bolts/histórias complete e26 planned no projeto, nenhum
outro incremento iniciado. Unidade003-rotas permanece in-progress porque IMP-008,
009 e010 continuam planned; PHASE-03 IN PROGRESS/TASK-011–013 TODO. Não se fecha
a fase, intent, projeto ou requisitos globais sem a sua própria evidência.

Os registos anteriores de revisão/integração/fecho pendentes são históricos;
este fecho e metadados atuais prevalecem. [Autorização de fecho](../../../experiments/construction/005-rotas/closure-authorization.json).
Sem gates externos adicionais, push ou início de outro IMP.

## Commit documental e preservação

Apenas documentação e evidência, com staging explícito da
[allowlist de fecho](../../../experiments/construction/005-rotas/closure-allowlist.txt).
[Diff contra f990503](../../../experiments/construction/005-rotas/closure-vs-integrated-base.diff),
[numstat](../../../experiments/construction/005-rotas/closure-vs-integrated-base.numstat),
[V-ADD novo](../../../experiments/construction/005-rotas/closure-v-add.log) e
[conferência](../../../experiments/construction/005-rotas/closure-validation.json).
Target/ e alterações alheias excluídos; outputs target existentes mantidos no disco.
Nenhum Maven, manual ou ensaio de rede repetido, pois o código não mudou.
O hash do próprio commit de fecho só pode ser apresentado depois de criado;
não é antecipado nem inserido por amend nesta documentação.

## Coerência derivada das dependências

Primeiro V-ADD de fecho encontrou unicamente 021-rotas:blocks flag mismatch.
[Log original](../../../experiments/construction/005-rotas/closure-v-add-attempt01.log)
preservado. Corrigido apenas blocks=true→false em021-rotas, pois004-overlay e
005-rotas estão complete. Status planned, started/current_stage/completed null
e stages_completed=[] mantidos; isto não inicia IMP-008 nem concede autorização
para iniciar outro incremento. Validador/alcance permanecem intactos.
