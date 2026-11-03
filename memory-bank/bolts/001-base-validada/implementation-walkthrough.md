# IMP-001 — Walkthrough da base verificada por leitura

Preparação do checkpoint 3: 2026-11-03T21:16:53Z.
Estado: checkpoint 3 aprovado explicitamente pelo utilizador, com registo em
2026-11-03T21:22:32Z. As pendências abaixo descrevem o estado apresentado no
checkpoint 3; a execução da validação foi autorizada depois dessa revisão.

Resultados posteriores: [validação do checkpoint 4](test-walkthrough.md),
executada após essa aprovação e aprovada pelo utilizador no checkpoint 4.
[Checkpoint 5](closure-walkthrough.md) preparado, sem integração presumida.
As secções seguintes são
o retrato do checkpoint 3; referências a pendências/autorização são desse momento.

## Autorização e estado na apresentação do checkpoint 3

O utilizador aprovou o checkpoint 1 e Q-C-01 exclusivamente para IMP-001,
e aprovou depois o checkpoint 2. A autorização então vigente permitia preparar
este walkthrough e os diffs, distinguindo as alterações preexistentes, e parar
no checkpoint 3. Não autoriza executar a validação ou concluir o bolt.

O bolt 001-base-validada estava in-progress, current_stage=implement. Apenas
plan está em stages_completed. Implement aguarda a revisão destes entregáveis;
Test e o fecho ainda não começaram. Não há dependências anteriores nem gates
externos adicionais no [IMP-001](../../../add.md#imp-001).

## O que foi produzido

O incremento verifica TASK-001–005 DONE sem as reimplementar. A entrega até
aqui é a inspeção e documentação da base existente, não uma funcionalidade
nova. Foram produzidos:

- [Plano](implementation-plan.md), aprovado no checkpoint 1.
- [Q-C-01](../../../docs/construction-duvidas.md#q-c-01--testes-primeiro-no-imp-001),
  resolvida: sem criar falhas artificiais para uma base DONE; a regressão
  continua obrigatória e qualquer lacuna real precede mudanças de código/testes.
- [Mapa de interfaces e oráculos](interfaces-and-test-map.md), aprovado no
  checkpoint 2, com assinaturas, records, enums e testes existentes para
  V-001–005/V-ADD. A aprovação é da revisão estática, não de execução de testes.
- Estados e registo de Construction com autorizações e pontos de paragem.
- Baseline, hashes e diffs em experiments/construction/001-base-validada/.

Não foi alterado Projeto/pom.xml, código Java, configuração, fixtures ou testes.
Não foram apagados/enfraquecidos testes, criadas APIs artificiais ou reabertas
TASK-001–005. Não há nova ADR ou dependência. Permanecem os bytes v1 e os
contratos de configuração/codec/transporte já existentes.

## Base de comparação e alterações preexistentes

Ramo: Denji. HEAD de referência:
4fe6791178828e72ea48c71d46146f9c8cd4c6f9. O arranque deste bolt encontrou
19 ficheiros rastreados alterados pelo fecho de Inception, mais o relatório
experiments/inception-closure-validation-2026-11-03.md não rastreado.

A [baseline de arranque](../../../experiments/construction/001-base-validada/baseline.json)
regista os hashes desses ficheiros e dos 163 ficheiros rastreados de Projeto/.
O [diff preexistente](../../../experiments/construction/001-base-validada/pre-existing.diff)
guarda o estado documental anterior ao bolt; não é atribuído a IMP-001.

Para distinguir as revisões:

- [Checkpoint 1](../../../experiments/construction/001-base-validada/checkpoint-1.diff):
  arranque, plano, dúvida e registos iniciais.
- [Checkpoint 2](../../../experiments/construction/001-base-validada/checkpoint-2.diff):
  aprovação do plano/Q-C-01 e mapeamento estático.
- [Checkpoint 3 incremental](../../../experiments/construction/001-base-validada/checkpoint-3.diff):
  aprovação do checkpoint 2, este walkthrough e atualização documental associada.
- [Diff cumulativo do bolt](../../../experiments/construction/001-base-validada/bolt-through-checkpoint-3.diff):
  conjunto dos entregáveis do bolt, contra a árvore existente ao seu arranque,
  descontando o fecho de Inception. Inclui os documentos novos não rastreados,
  que git diff isoladamente não apresenta.

O unit-brief de 001-base-validada participa nas duas fases: a alteração para
ready em Inception é preexistente; a passagem para construction/in-progress
e os timestamps de Construction pertencem ao bolt. O diff cumulativo usa a
baseline com hash para separar essas alterações no mesmo ficheiro.

Os diffs completos contra HEAD também contêm a documentação anterior de
Inception. Não os interpretar como autoria exclusiva de IMP-001 nem incluir
essa documentação num commit por inferência. Não foram criados ramos, commits,
push, deploy ou upload.

## Critérios e verificações ainda pendentes

| Critério de IMP-001 | Evidência disponível | Pendência |
|---|---|---|
| Confirmar componentes reais e preservar TASK-001–005 DONE | Código/POM/testes inspecionados, estados históricos preservados e revisão estática aprovada | Correlacionar com a execução nova e rever o fecho |
| Conferir 45 fixtures, 16 tipos e coerência ADD/specs/DAG | Inventário declarado e casos de teste mapeados; enumeração e contratos lidos | Recalcular binário/hex/hash, conferir offsets e executar V-ADD |
| Executar a regressão existente com log novo | Suites e comando identificados | Build completo e contagem/resultados novos ainda não executados |

Nenhum critério global foi marcado como concluído apenas por escrever este
walkthrough. O valor 149 continua histórico, sem representar um resultado novo.

| Oráculo / prova | Estado nesta execução do bolt |
|---|---|
| V-001: POM efetivo, release/provider e build limpo | Pendente de execução |
| V-002: fixtures/offsets e proveniência do recurso | Mapeado por leitura; verificação atual completa pendente |
| V-003: ConfigurationLoaderTest | Mapeado; suite não executada |
| V-004: MessageCodecTest | Mapeado; suite não executada |
| V-005: TransportTest | Mapeado; suite não executada |
| V-ADD: validação documental | Script inspecionado; não executado nesta retoma |
| Teste manual configuração/codec/TCP/shutdown | Roteiro definido; ainda não executado |
| Revisão humana e integração finais | Pendentes para fecho; aprovações dos checkpoints 1/2 não as substituem |

Os checks de hash/diff da árvore de trabalho demonstram preservação de ficheiros;
não substituem os oráculos funcionais/documentais acima. A existência de classes
vazias de bootstrap/overlay/routing/vídeo é trabalho futuro, não êxito funcional.
RuntimeConfiguration e sendAndFlush pertencem aos IMP seguintes. Nenhuma lacuna
estática que exija alterar fundações/testes foi identificada ou corrigida aqui.

## Validação proposta, a aguardar autorização

No checkpoint 4, depois de aprovação explícita deste checkpoint:

1. Confirmar ambiente e usar o JDK 21 instalado apenas no processo Maven;
   Java/Maven observados no arranque: JDK 21.0.12.1 e Maven 3.6.3.
2. Executar V-ADD e a verificação atual das fixtures/contratos aplicáveis,
   registando comando, ambiente, resultados e limitações.
3. Em Projeto/, executar env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
   mvn -B -ntp clean verify. Guardar log novo em
   experiments/construction/001-base-validada/ e extrair contagens reais dos
   relatórios Surefire produzidos pela execução, sem reutilizar target antigo.
4. Executar o teste manual já proposto no checkpoint 2: cinco perfis, uma
   fixture independente, troca PROBE_PING/PROBE_PONG entre dois transportes
   loopback com portas efémeras, esperas limitadas, receção efetiva e shutdown.
   Confirmar terminação/reservas e reutilização da porta; guardar comandos e
   saída reais. Não confundir o smoke com handshake, cálculo de RTT ou CORE.
5. Apresentar resultados, pendências e diff para revisão. Produzir
   test-walkthrough.md apenas depois das execuções; não declarar fecho antes
   de critérios cumpridos, verificações, revisão humana e integração.

A revisão do checkpoint 3 foi aprovada explicitamente. O roteiro acima foi
autorizado apenas até ao checkpoint 4; resultados devem ser registados num
test-walkthrough posterior às execuções. O fecho do bolt continua por autorizar.
