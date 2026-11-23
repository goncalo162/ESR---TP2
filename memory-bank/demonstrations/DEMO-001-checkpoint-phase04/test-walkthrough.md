# Test — checkpoint da primeira iteração, sem aceitação da demo

O utilizador pediu paragem no checkpoint do protótipo e preparação para
continuação. Não se concluem as etapas Implement/Test do bolt nem a demo.
Fases funcionais01–04 continuam DONE;05–07 continuam TODO.

## Regressão JDK21

Ambas as execuções usaram OpenJDK21.0.13.1 e `mvn -o -B -ntp clean verify`.

| Execução | Testes/falhas/erros/ignorados | XML | Resultado |
|---|---|---|---|
| Início |1459/0/0/0|21|PASS, saída0|
| Fim da iteração1 |1459/0/0/0|21|PASS, saída0, terminado2026-11-22T23:39:40Z|

[Baseline](../../../experiments/checkpoint-phase04/build-start/result.json) ·
[Resultado final](../../../experiments/checkpoint-phase04/build-end-iteration1/result.json) ·
[Âmbito/fontes](../../../experiments/checkpoint-phase04/build-end-iteration1/scope-check.json).

Nenhum teste acrescentado/retirado da regressão de produção: diferença0.
Fontes/testes, CORE e scripts anteriores preservados por hashes. Os testes
de integração da ferramenta são execuções externas e não entram no total1459.
JARs/logs/XML de ambos os builds congelados separadamente; hashes do arquivo
podem diferir por timestamps ZIP, sem diferenças das classes compiladas.
As listas Maven geradas foram guardadas e repostas nos bytes anteriores.

## Ferramenta e execuções reais

[Resumo estruturado de todas as tentativas](../../../experiments/checkpoint-phase04/iteration1-results.json).

- Cenário1: uma run completa PASS31/31, em versão anterior do protótipo.
  Não satisfaz três execuções consecutivas da versão final.
- Cenário2: última run FAIL na troca por qualidade. Houve STARTED e FAILURE
  PATH_CHANGED; apesar de o forwarding posterior usar R3, não houve SUCCESS
  da operação exigida. Não substituir o oráculo por mera mudança de rota.
- Cenário3: descritor completo criado, não executado.
- Negativos de catálogo/trace/renovação/prontidão preservados, com ajustes do
  observador e suas limitações explicitados no [handoff](HANDOFF.md).
- Todas as runs terminadas registam processos não pendentes e portas
  reutilizáveis. O cenário2 falhado também limpou completamente os recursos.

As causas do NO_ROUTE da CLI e do PATH_CHANGED da troca por qualidade ainda
não estão estabelecidas. Não foi feita correção de produção. R17 histórico
permanece desconhecido, distinto destes negativos.

## Iteração 2 (2026-11-23) — retoma e fecho dos critérios

Builds inicial e final `mvn -B -ntp clean verify` JDK21.0.13.0.3:
**1459/0/0/0, 21 XML** em ambos; JAR `dc192971…` idêntico
([início](../../../experiments/checkpoint-phase04/build-start-iteration2/result.json),
[fim](../../../experiments/checkpoint-phase04/build-end-iteration2/result.json)).

Ajustes só em `demo/` (commits `94300b2`, `91db5ea`, `afeb6b1`, `ec72427`,
`a416521`, `23ddc9a`): proxy com atraso por frame agendado (sem fila de
serialização), oráculos de trace com prazo, atomicidade do switch por
upstream/caminho publicados, restabilização após o pulso e prazo de arranque
escalado para >6 nós. Detalhes e âmbito em
[investigation-iteration2.md](investigation-iteration2.md).

Runs na versão congelada `23ddc9a` (JAR `dc192971…`):

- Cenário1: **3/3 PASS consecutivas** (33 checks cada) — série em
  [series-final2-iteration2.log](../../../experiments/checkpoint-phase04/series-final2-iteration2.log).
- Cenário2: **4 PASS consecutivas** (39 checks cada) em
  [reliability2-cenario2-iteration2.log](../../../experiments/checkpoint-phase04/reliability2-cenario2-iteration2.log),
  mais 2 PASS em séries posteriores; 2 FAIL posteriores preservados
  (`e63bfcf3`, `32a314b3`) com a corrida residual do 1.ª tentativa — ver
  handoff. Todas as runs: processos terminados, portas reutilizáveis.
- Cenário3 (Figura 1, 11 nós): **PASS completo** `20261123T115804Z-96352b48`
  (39 checks); FAIL de arranque anterior preservado (`9e1b0832`).
- Runs FAIL nunca apagadas; runs novas com `tool-source/`+`tool-sha256.json`.

Série final congelada `8303c0b` (JAR `dc192971…`): c1 3/3 PASS
(`ff3a5f83`,`9deaebcc`,`28c6b934`), c2 3/3 PASS (`4b4adbaf`,`67a5116e`,
`1b1daa64`) — [series-final-head.log](../../../experiments/checkpoint-phase04/series-final-head.log).
HEAD final `63d0c97`: c1 3/3 PASS, c2 2 PASS + 1 FAIL preservado, c3 2 PASS +
1 FAIL preservado — [series-final-head2.log](../../../experiments/checkpoint-phase04/series-final-head2.log).
Cenário3 completo PASS repetido (`96352b48`,`73408404`,`149ff3ed`,`e135a4d5`).

## Ainda obrigatório para aceitar a demonstração

Investigação dos negativos e revisão dos oráculos, cenário2 completo, três
runs consecutivas de1 e2 na mesma versão, tentativa de3, observação do HTML
no browser e ensaio cronometrado de10–15min. Validação de interrupção/SIGTERM
da ferramenta e revisão de robustez ainda pendentes. Build verde não substitui
esses critérios. Sem execução humana/CORE ou aceitação docente inventadas.

Validação documental desta iteração: V-ADD mantém apenas os três erros
preexistentes nos templates .specsmd (dois fences e uma ligação de exemplo),
sem erro novo. Links locais novos, sintaxe do shell/JavaScript do dashboard e
whitespace da documentação passaram; observação do HTML no browser pendente.
[Relatório](../../../experiments/checkpoint-phase04/v-add-iteration1.md).
