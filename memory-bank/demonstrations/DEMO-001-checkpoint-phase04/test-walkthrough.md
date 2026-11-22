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
