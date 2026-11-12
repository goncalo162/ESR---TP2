# Primeiro passo Implement — evidência

Testes selecionados:16/1pass/15fail/0error/0skip, exit1, XML/log preservados.
V-ADD: exit0. git diff --check: exit0. HEAD/base 3dedf007f52f43f53eaf254bef7c25e35f986e5a; índice vazio.
Cinco fontes autorizadas: +321/-0; nenhum comportamento
operacional implementado. Diff acumulado documental/fontes exclui target/ e
experiments/: +1785/-21, 21 ficheiros.
Ver numstat correspondente, inclusive documentos/testes novos sem staging.
Git diff --numstat bruto guardado separadamente; Git não inclui untracked nele.
Todos os 229 ficheiros tracked protegidos de Projeto fora de target/
e allowlist conservam SHA256 da entrada Model; todos os restantes ficheiros
preexistentes da entrada Implement fora da allowlist documental/Java e outputs
de Maven conservam SHA256. Target foi alterado pelo build: não alegar preservação
dos outputs; lista explícita em implement-red-preservation.json. Nenhum cleanup
global, staging, commit ou push. Regressão integral, V-010 e fases posteriores pendentes.
