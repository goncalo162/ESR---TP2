# Evidência — IMP-001

Os checkpoints aprovados conservam os seus logs, relatórios, hashes e diffs;
os ficheiros anteriores não são regenerados ao integrar ou fechar o bolt.
As tentativas falhadas de sandbox/JShell e os ensaios isolados são preservados
com o seu contexto; não constituem sucesso da execução real de fecho.

Execução manual por **Luis**: [log original preservado](manual-smoke-luis.log),
22 CHECK_PASS e MANUAL_PASS; [origem/instantes/SHA-256](manual-smoke-luis.json).
O original em /tmp/ott-imp001-manual.ijH5h9.log não foi alterado.

[Proveniência Inception](inception-commit-provenance.json): os 20 ficheiros do
commit de Inception correspondem aos hashes da baseline anterior ao bolt.
As alterações posteriores de IMP-001, inclusive em ficheiros partilhados,
ficam nos seus próprios commits. Projeto/target/ é excluído desses commits;
as cópias dos relatórios Surefire em experiments são evidência aprovada.

O .gitattributes desta pasta preserva o whitespace original de patches, logs
e saídas Surefire brutas. O espaço de contexto dos patches e as linhas Maven
não são reformatação de código; os seus hashes devem manter-se. As verificações
de whitespace continuam normais para fontes, scripts, metadados e Markdown.
