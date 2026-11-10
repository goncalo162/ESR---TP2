# IMP-004 / TASK-007 — plano de integração para aprovação

**Revisão final de Denji: realizada e aprovada, sem alterações pendentes,
conforme confirmação explícita do utilizador.**
[Registo](../../../experiments/construction/003-overlay/denji-final-review-confirmation.json). Test foi aprovado, o manual humano de Francisco está
cumprido e Q-C-08 resolvida. Bolt in-progress/completed=null, sem DONE.

## Lista exata e exclusões

A [lista JSON por grupo](../../../experiments/construction/003-overlay/integration-file-list.json)
e a [lista simples, um caminho por linha](../../../experiments/construction/003-overlay/integration-file-list.txt)
são a allowlist completa proposta, sem globs nem git add global. Contêm apenas
ficheiros novos/alterados em relação a HEAD, não documentos anteriores já integrados.
Contagens desta proposta:

- bootstrap-restoration: 9 ficheiros.
- imp-004: 106 ficheiros.
- q-c-08-patch-and-evidence: 20 ficheiros.
- shared-traceability: 10 ficheiros.

Código de IMP-004:

- `Projeto/src/main/java/pt/uminho/esr/ott/overlay/Neighbor.java`
- `Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java`
- `Projeto/src/test/java/pt/uminho/esr/ott/overlay/NeighborManagerTest.java`

Reposição externa:

- `Projeto/src/main/java/pt/uminho/esr/ott/bootstrap/BootstrapServer.java`

O delta a integrar do bootstrap é **+24/−1 contra HEAD atual** (classes repostas
e newline final). O +482/−142 do resumo final é o acumulado contra a base aprovada
26aaa4a e inclui a legibilidade já integrada antes desta execução; não é novamente
uma mudança a integrar. IMP-004 mantém +1745/−0 nos três ficheiros.

O grupo de rastreabilidade contém as versões finais dos documentos partilhados,
sem as repartir artificialmente entre grupos. Os outros grupos enumeram os logs,
XML, JAR/hash, perfis/manuais, diffs e auditorias reais. O JAR preservado fica em
experiments/, **não em target/**. São incluídas evidências negativas/históricas
necessárias para a proveniência, sem as apresentar como resultados atuais.

Excluídos: qualquer mudança em `Projeto/target/`, `.gitignore`, configurações locais
`.specsmd/aidlc/context-config.yaml`/`memory-bank.yaml`, o template Simple,
`.codex/`, `.vscode/` e restantes ficheiros fora da allowlist. Não se removem nem
revertem essas alterações. Arquivos já tracked em target/ na base não são limpos
por esta proposta; nenhuma mudança neles entra no delta de integração.

## Q-C-08 — delimitação necessária para preservar alterações alheias

Os três alvos corrigidos localmente foram:

- `.specsmd/aidlc/skills/inception/bolt-plan.md`
- `.specsmd/aidlc/skills/operations/deploy.md`
- `.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md`

Os dois primeiros não estão tracked; o terceiro está tracked, mas o conteúdo local
é uma substituição genérica extensa anterior a Q-C-08. HEAD tem o template OTT de
43 linhas. Fazer staging dos três ficheiros completos importaria ferramentas e
alterações locais alheias, contrariando a exclusão pedida.

**Proposta para aprovação:** integrar Q-C-08 apenas como patch exato +5/−3 e
respetiva evidência/cópias de entrada, preservando as correções locais em vigor;
não instalar o conjunto genérico de ferramentas na base comum. As cópias em
experiments/ são dados de auditoria, não ferramentas ativadas. O template tracked
OTT da base comum permanece como está em HEAD. Não se altera o validador, não se
adicionam exclusões e não se apagam ficheiros para o fazer passar.

A [simulação final](../../../experiments/construction/003-overlay/integration-final-preview-result.json)
parte do conteúdo tracked de HEAD, sobrepõe exatamente a allowlist e executa o
validador original; [log final](../../../experiments/construction/003-overlay/integration-final-preview-v-add.log).
Isso verifica o conteúdo efetivamente proposto para a base comum, em separado da
validação já aprovada do checkout local. Importar as ferramentas completas seria
outra proposta, com âmbito próprio; não faz parte deste plano.

## Sequência proposta — sem execução autorizada

1. **Cumprido:** revisão final de Denji aprovada e registada por confirmação
   explícita do utilizador, sem alterações pendentes. Os hashes das quatro fontes
   revistas permanecem iguais; não se contactou nem falou em nome do revisor.
2. Aprovar este plano/allowlist, incluindo a delimitação de Q-C-08. Atualizar a
   referência local da base comum quando for autorizada a integração; neste momento
   `main` e `origin/main` locais apontam a 06dc037. Não se consultou o remoto nesta
   preparação, nem se afirma que o servidor remoto continua nesse estado.
3. Após autorização própria para integração/commit, preparar um único conjunto
   atómico com estes grupos, usando caminhos explícitos. Não fazer git add -A,
   não usar reset e não incluir alterações alheias. Conferir staged diff/numstat
   e lista de caminhos contra a allowlist antes de criar qualquer commit.
4. Com HEAD e código iguais aos agora revistos, preservar clean verify/903 testes,
   JAR/hash e manual humano/49 checks. **Não repetir o manual se o código não mudar.**
   Se a integração alterar código ou resolver conflitos de código, validar as
   alterações efetivas e voltar a obter a revisão aplicável antes de as integrar.
5. Mensagem sugerida, apenas para futura autorização:
   `feat(IMP-004/TASK-007): integrar vizinhanca revista e evidencias`.
   Um commit atómico evita estados intermédios com referências documentais quebradas.
   Commit/push não estão autorizados por este plano; o destino remoto e eventual
   publicação exigem a autorização correspondente.
6. Com revisão final confirmada e integração efetivamente comprovada, apresentar
   evidência da versão/referência integrada. Só depois tratar o fecho specsmd e
   cascata de estados, sem marcar DONE antecipadamente.

## Estado desta preparação

Nenhum ficheiro foi staged, nenhum branch/worktree/commit/push criado e nenhuma
integração executada. O [log final de validação desta documentação](../../../experiments/construction/003-overlay/integration-final-planning-v-add.log)
não é uma aprovação humana do plano. Falta autorização de integração e a sua execução/comprovação para
poder tratar DONE. A lista contém preparação documental/evidência, sem alteração ao código.

## Allowlist final completada

A lista passa de 137 a **145 ficheiros**. Além das duas listas e dos primeiros
outputs de simulação já enumerados, contém agora estes oito caminhos explícitos:

- `experiments/construction/003-overlay/denji-final-review-confirmation.json`
- `experiments/construction/003-overlay/integration-product-to-head.diff`
- `experiments/construction/003-overlay/integration-product-to-head.diff.numstat`
- `experiments/construction/003-overlay/integration-preparation-audit.json`
- `experiments/construction/003-overlay/integration-final-preview-v-add.log`
- `experiments/construction/003-overlay/integration-final-preview-result.json`
- `experiments/construction/003-overlay/integration-final-planning-v-add.log`
- `experiments/construction/003-overlay/integration-final-audit.json`

Os outputs anteriores preservam o estado histórico da preparação. A confirmação
atual de Denji e os outputs finais acima superam a indicação anterior de revisão
pendente. [Auditoria final do pacote](../../../experiments/construction/003-overlay/integration-final-audit.json):
conferência da allowlist, referências, fontes/JAR, exclusões e ausência de staging.
Os outputs desta validação são verificados como evidência no pacote local;
não se pretende obter um hash autorreferente do próprio relatório.

**Só falta autorizar e executar a integração da versão revista.** Este plano
atualizado não autoriza staging, commits/push, integração ou fecho. Não houve
mudança de código; não se repetiu o manual humano nem se inventaram novas execuções
Java. O bolt permanece aberto e não está DONE.

## Estado atual — IMP-004 fechado

Registo documental em 2026-11-10T17:12:15Z.
IMP-004/003-overlay/TASK-007 fechado documentalmente por autorização explícita
do utilizador. Integração local comprovada em main: `8db7b78`
(`8db7b78950e444f3c2917844617be4b9b236ec0f`), com 145 caminhos e hashes conferidos.
Test aprovado: clean verify/903 testes, manual humano Francisco/49 checks/Java 0
confirmado pelo utilizador, V-ADD aprovado; revisão final de Denji aprovada sem
alterações pendentes. Q-C-08 resolvida e limitada a patch/evidência na integração.
Bolt complete; história complete/implemented=true; TASK-007 DONE. A unidade
002-overlay mantém in-progress: 020-overlay e 004-overlay continuam planned,
sem início de outro IMP. Fecho documental sem alteração Java ou repetição do manual.
Sem push ou alegação de integração remota/entrega do projeto.

[Fecho documental](closure-walkthrough.md). As referências anteriores a estados
pendentes são históricas; não descrevem o estado atual. Não se promovem
requisitos globais, PHASE-02, unidade ou intent a concluídos por este fecho.
