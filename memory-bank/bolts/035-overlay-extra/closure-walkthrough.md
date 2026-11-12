# IMP-035 / 035-overlay-extra — fecho e cascata da fase2

Fecho registado em 2026-11-12T10:46:24Z. O utilizador autorizou: «Podes fechar o bolt e e
esta fase», após Test/manual humano aprovados e integração da versão em main.
A aceitação final que permanecia pendente está satisfeita por essa instrução;
não se inventa revisão de código realizada por Fillmore ou outro colega.
[Aceitação e base integrada](../../../experiments/construction/035-overlay-extra/completion-authorization.json).

## Evidência aceite, sem repetir execuções

- Clean verify/JDK21:1113 testes, zero falhas/erros/skips,11 XML. [Resumo/JAR](../../../experiments/construction/035-overlay-extra/test-build-summary.json).
- Manual humano selecionado: Luis,20261112T102931Z-pr_nynth,66 CHECK_PASS,
  MANUAL_PASS/passed=true/no_processes_pending=true. [Resultado](../../../experiments/construction/035-overlay-extra/test-manual-runs/20261112T102931Z-pr_nynth/result.json).
  A execução humana anterior20261112T004311Z-ilbihqij e todas as tentativas do
  agente ficam preservadas, incluindo as incompletas do observador IPv4.
- Os dois grafos foram reparados após SIGKILL reais/sequenciais dos relays,
  bootstrap parado, mesmos processos sobreviventes, uma ligação física reparada
  e S–R1 preservada no segundo grafo. Auxiliar usa APIs públicas/JVM por nó e
  relógio/transporte reais; CLI de produção exercitada separadamente na regressão.
- Hash JAR: dea01ffa2c501b9b948594005e7e8e9ee70b387981dc5b3d83f9d7c331cc2526.
  Código/testes/configuração/fixtures coincidem com a execução validada.
- Versão15efdf280b97327cfdc73e9040b9dfed4d4a5658 e documentação
  ff0ef0b15f1366272493d62cfaaa4ede9bd0ddd1 estão em main antes do fecho.
  [Prova anterior](../../../experiments/construction/035-overlay-extra/integration-proof.json).

## Cascata concluída

035-overlay-extra complete, cinco etapas concluídas; história007 complete e
implemented=true; TASK-034 DONE. Todos os sete bolts da unidade002-overlay
complete, unidade complete. Todas as oito tarefas da PHASE-02 DONE, incluindo
as partes A/B de EXTRA-01; fase DONE por aceitação explícita do utilizador.
[Progresso](../../../docs/15-PROJECT-PROGRESS.md) e
[dossier da fase](../../../docs/phases/PHASE-02-CLOSURE-REVIEW.md).

005-rotas/IMP-007 deixa de estar bloqueado por dependências, mas permanece
planned/sem início. Nenhum novo IMP, manual/build ou teste de rede foi iniciado.
Este fecho é documental, com V-ADD e integridade dos artefactos.

## Limites mantidos

REQ-025 está concluído na aprendizagem/reparação do plano de controlo. A
continuidade de vídeo nessas arestas permanece para a integração posterior
com routing/streaming; o requisito global não é promovido a satisfeitos sem
essa prova futura. CORE/GUI/FPS/entrega do projeto não são inferidos.
G-ASSESSMENTS conserva a informação docente pendente aceite no marco anterior.
Intent/projeto continuam em construção. Sem push por este pedido.

## Verificação documental de fecho

[Conferência de estados/hashes](../../../experiments/construction/035-overlay-extra/completion-validation.json),
[diff contra a base integrada](../../../experiments/construction/035-overlay-extra/completion-vs-integrated-base.diff),
[V-ADD](../../../experiments/construction/035-overlay-extra/completion-v-add-02.log)
e [diff check](../../../experiments/construction/035-overlay-extra/completion-diff-check.log).
1478 artefactos anteriores mantêm hashes; a versão anterior do roteiro README
fica arquivada. Alterações de Projeto limitam-se ao README; nenhum código,
teste, configuração, POM, fixture ou JAR foi alterado. Não se repete o manual.
