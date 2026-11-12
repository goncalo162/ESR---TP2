# IMP-007 — validação documental de Model na base v2

Base/HEAD: 3dedf007f52f43f53eaf254bef7c25e35f986e5a, main; entrada limpa.
Model preparado por autorização explícita, por aprovar. Nenhuma decisão da retoma
cancelada recuperada. Q-C-23/24 abertas, sem extensão de âmbito por inferência.

- model-entry.json: hashes anteriores de todos os tracked e de todo Projeto/,
  incluindo ficheiros ignorados de build. Nenhum foi alterado, criado ou removido
  em Projeto/. Incrementos fechados, unidade002, ferramentas e fixtures intactos.
- model-dependencies.json: IMP-006/IMP-035 e cadeia IMP-034 complete;
  unidade002 complete. Não foram repetidos testes para conferir esses estados.
- model-sources.json: identidades das fontes lidas e contrato operacional atual;
  TCP v2, 19 tipos, 3 rejeitado, sem fallback v1 ou alteração UDP OTT1.
- model-preservation.json: alterações documentais permitidas, hashes preservados,
  HEAD inalterado, índice vazio, current_stage=model e stages_completed=[].
- model-v-add.log: python3 scripts/validate-planning.py, saída 0/zero erros.
  O campo Base do validador é a sua referência interna histórica, não HEAD.
- model-vs-base.diff / model-vs-base.numstat: diferenças documentais contra a
  base operacional, incluindo Model e construction-log novos sem staging.
- git diff --check: sem erros nos ficheiros tracked alterados.

Não foram executados Maven/JUnit, V-010, serviços/sockets, manual, GUI/CORE ou
medições nesta etapa. Os valores numéricos dos oráculos são exemplos de resultados
esperados, não medições. Test/revisão final Fillmore/integração continuam futuros.
Parar em Model, sem Design/ADR/Implement/Test, commit ou push.
