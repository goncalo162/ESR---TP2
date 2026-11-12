# Plano de integração pendente — IMP-007 / 005-rotas

Preparado por autorização do utilizador; **não executar staging/commit/push**.
Base/HEAD main:3dedf007f52f43f53eaf254bef7c25e35f986e5a. Test e revisão final da implementação/evidência por Fillmore aprovados; falta
integração comprovada. Confirmação inclui Q-C-25 e não tem alterações pendentes.

1. Revisão final cumprida: Fillmore reviu a versão atual do código e evidência,
   incluindo Q-C-25, sem pendências, conforme confirmação explícita do utilizador.
   A confirmação vincula os seis hashes, JAR e manifesto do pacote revisto.
2. Incorporar apenas alterações que venham a ser autorizadas por essa revisão.
   Se houver mudança de código, revalidar testes e evidência afetados e rever a
   nova versão; se o código não mudar, não repetir o manual humano já cumprido.
3. Só com autorização de integração: reconferir HEAD/branch/status e que todos
   os hashes/listas ainda correspondem. A allowlist contém **348 caminhos**:
   5IMP-007 +1Q-C-25 +20documentais +322evidência.
   O manifesto lista a si próprio, mas não tenta hash recursivo de si mesmo.
4. Fazer staging explícito apenas dos caminhos aprovados na versão vigente de
   integration-allowlist.txt, sem git add . nem seleção recursiva. Excluir sempre
   target/ e alterações alheias; não modificar/limpar esses outputs por inferência.
5. Antes de commit: mostrar staged diff/numstat e confrontar árvore staged, nomes
   e bytes/hash com a lista autorizada. Verificar código/documentos e conservar
   logs/XML/diffs brutos, sem normalização que destrua a evidência. V-ADD mantém
   alcance/ferramenta intactos. Índice vazio na preparação atual.
6. Só com commit expressamente autorizado, criar integração local identificando
   TASK-010/IMP-007 e a ação; sem push ou alteração forçada de histórico. Não criar
   branch por inferência. Apresentar hash, caminhos/bytes efetivamente integrados
   e prova de integração antes de solicitar/realizar fecho documental autorizado.
7. Manter bolt/story/TASK/unidade em progresso até integração comprovada; a unidade mantém progresso enquanto outras histórias estiverem
   pendentes. Nenhum outro IMP é iniciado neste plano.

Os ficheiros originais pessoais e evidência de tentativas falhadas/ensaios do agente
são preservados. O pacote distingue resultados humanos de dados técnicos e não
alega GUI/CORE, medições de vídeo, entrega ou upload. A revisão final confirmada
é registada com o âmbito exato, sem atribuir qualquer ação Git já executada.
Sem gates externos adicionais. Este plano é preparação revisável, não autorização
nem evidência de execução das ações Git acima.

## Simulação do conteúdo exato

Reconstruir a base Git e sobrepor exclusivamente a allowlist. Conferir todos os
bytes selecionados e que os restantes ficheiros da base não mudaram; executar
o validador original da árvore simulada com o alcance integral. Verificação
suplementar de links Markdown de todo o pacote, sem substituir V-ADD. Target
tracked já existente na base permanece histórico e inalterado; nenhum caminho
ou mudança de target pertence ao payload. Não se apaga conteúdo da base por
inferência. A simulação não usa índice alternativo, staging, commit ou worktree.

O log/prova de preparação são incluídos; depois de fechar o manifesto, executar
novamente sobre cópia final congelada com stdout/recibo externo ao payload, sem
mutar a árvore nem o pacote após validação. Recibo temporário identificado na
prova e resultado final registado na sessão; script permite reproduzir tudo.
Só após autorização de integração aplicar os348 caminhos explícitos e apresentar
staged diff/hash antes do commit local. Não repetir manual/build se código/JAR
continuarem iguais; não marcar DONE até apresentar integração comprovada.
