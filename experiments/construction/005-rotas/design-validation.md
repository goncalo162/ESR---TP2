# IMP-007 — validação documental de Design

Base/HEAD: 3dedf007f52f43f53eaf254bef7c25e35f986e5a. Model e Q-C-23/24 aprovados; Design para aprovação.

- `python3 scripts/validate-planning.py`: exit 0, log design-v-add.log.
- `git diff --check`: exit 0.
- SHA256: todos os 626 ficheiros de Projeto, incluindo ignorados,
  preservados desde a entrada Model; nenhum ficheiro acrescentado/removido.
- Todos os restantes ficheiros tracked fora da allowlist documental preservados;
  incrementos fechados, ferramentas, testes e unidade002-overlay intactos.
- Índice vazio; HEAD/base preservado; sem staging, commit ou push.
- Diff documental acumulado contra a base: 13 ficheiros,
  +1034/-19, incluindo documentos novos sem staging; exclui
  a própria evidência experiments/, listada pelo inventário separado.
- V-010, Maven, manual e medições não executados nesta etapa.

Ver design-preservation.json, design-vs-base.diff e design-vs-base.numstat.
