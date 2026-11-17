# IMP-011 / 007-arvores / TASK-014 — fecho documental autorizado

Bolt/história **complete**, implemented=true e TASK-014 **DONE**, por autorização
explícita do utilizador após integração comprovada. Unidade004-arvores e
PHASE-04 continuam **in-progress**, com quatro incrementos/tarefas pendentes.

## Versão e aprovações cumpridas

Integração local em main **37fa863ed416017e733e692f9565899fb61285ae**, pai
9c5ad094e58313622a5a695e259cd91b15a71a72,568 caminhos iguais à allowlist/hash
aprovados. [Prova posterior ao commit](../../../experiments/construction/007-arvores/integration-proof-01/README.md),
[verificação de conteúdo](../../../experiments/construction/007-arvores/integration-proof-01/commit-verification.json) e
[V-ADD da cópia extraída: zero erros](../../../experiments/construction/007-arvores/integration-proof-01/v-add-extracted-commit.md).

Model/Design/ADR/Implement/Test aprovados pelo utilizador.
[Test/V-014 consolidado](ddd-03-test-report.md): clean verify com JDK21,
**1328 testes,0 falhas/erros/ignorados,18 suites**.
[Logs/XML/JAR](../../../experiments/construction/007-arvores/test-02/README.md).
JAR486734bytes, SHA-256
`f923f5b766f4ddcc3871c6339710a4a0e87a609aa6cb5ce1e6620c73ccc55314`.

Francisco executou pessoalmente as cinco etapas/24 checks, sem divergências:
[relatório original](../../../experiments/construction/007-arvores/test-manual-runs/20261117T233223Z-q2zk5pu4/report.json),
CLEANUP_COMPLETE, MANUAL_PASS CHECKS=24, JAVA_EXIT0 e PASSED=true. Hashes, origem
da classe no JAR e terminação recolhida pelo wrapper conferidos. A confirmação
humana provém do utilizador; o rótulo human não é verificação autónoma de identidade.

Revisão final de Waheed da implementação/evidência atuais confirmada explicitamente
pelo utilizador, sem alterações pendentes, vinculada aos hashes dos oito caminhos
Java e JAR no [registo de revisão](../../../experiments/construction/007-arvores/integration-01/manual-and-review-verification.json).
Nenhum contacto com pessoas ou aprovação presumida.

## Preservação e limites

Nenhuma alteração Java no fecho; não se repetem Maven/manual sem mudar código.
Q-C-36/37 mantidas, Q-C-38 exclusivamente lambda do leitor CLI, Q-C-39 apenas
whitespace das seis linhas autorizadas de CODESTYLE. Todos os negativos e
evidências anteriores preservados, incluindo a execução restrita com79 erros
de sockets e as falhas históricas NeighborDown/D11a/M07.

V-014 comprova controlo/ativação, agregação em dois relays, ACK/cancelamento,
streams independentes, leases e recursos físicos/shutdown. Roteiro humano usa
relógio/owner/port controlados; integração TCP loopback real é prova técnica
separada. Não se afirma streaming/GUI/CORE, FPS ou execução remota. Q-C-34 mantém
renovação/poda completas em IMP-012, sem prometer remoção remota antes do lease.

Os avisos de git diff --cached --check da integração pertencem aos bytes brutos
dos logs/XML/TXT/diffs/patches/relatórios aprovados, preservados por hash; não se
normaliza evidência nem se enfraquece o validador. target não integra o diff de
fecho; os nove caminhos target herdados da base não são removidos, e as duas
alterações locais geradas continuam excluídas/preservadas.

## Cascata e publicação

13 bolts/histórias complete,22 planned,nenhum em execução.023-arvores apenas
desimpedido por blocks=false, permanecendo planned, started=null e sem etapas.
Unidade004-arvores/PHASE-04 em progresso; nenhum requisito global promovido
a DONE apenas pelo fecho de TASK-014.

A evidência posterior à integração acompanha este commit exclusivamente de
fecho documental/evidência. V-ADD do fecho e preservação guardados em
experiments/construction/007-arvores/closure-01. Sem push ou novo IMP.
