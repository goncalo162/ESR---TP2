# Retoma — DEMO-001-checkpoint-phase04

**Paragem pedida pelo utilizador no checkpoint do protótipo.** Implement em
curso/pausado; a demonstração não está completa nem aceite para apresentação.
Não iniciar PHASE-05. Não apagar negativos ou repetir até obter verde.

## Estado entregue

- `demo/run.sh`, orquestrador Java21, proxy TCP real sem root, adaptador de
  snapshots/subscrições e HTML local. Compilação contra o mesmo JAR; zero
  alterações em fontes/testes de produção, CORE ou scripts anteriores.
- Descritores do enunciado: cenário1 quatro nós, cenário2 seis nós com
  triângulo R1/R2/R3 e cenário3 onze nós da Figura1. Bootstrap é processo
  adicional; no cenário3 O1 é servidor e O4/O8/O9/O10/O11 clientes.
- [Guião preliminar](../../../demo/DEMO.md), [matriz](../../../demo/MATRIX.md),
  [plano](implementation-plan.md), [validação](validation-plan.md).
- Build inicial: clean verify JDK21.0.13.1, **1459/0/0/0, 21 XML**.
  [Resultado](../../../experiments/checkpoint-phase04/build-start/result.json).
- Build final desta iteração: ver [Test](test-walkthrough.md) e pasta
  `experiments/checkpoint-phase04/build-end-iteration1/`; não confundir com
  aceitação final da ferramenta.

## Resultados que podem ser afirmados

**Cenário1: uma execução completa PASS**, run `20261122T233033Z-a3583f1e`,
31 verificações. [Resultado](../../../experiments/checkpoint-phase04/runs/cenario1/20261122T233033Z-a3583f1e/result.json).
Demonstrou validação, TCPv2/discovery, RTT real, CLI, ativação, isolamentoA/B,
uma ativação inicial no tronco, renovação real, trace, SIGKILL de R1 e reparação
S1–C1 com bootstrap parado, reinício e poda seletiva/remoção por morte de C2.
Todos os processos terminaram e as portas ficaram reutilizáveis.
Esta run pertence a uma versão anterior do protótipo: **não conta como uma das
três repetições da versão final**.

**Cenário2: última execução FAIL**, run `20261122T233636Z-e48c789f`.
[Resultado](../../../experiments/checkpoint-phase04/runs/cenario2/20261122T233636Z-e48c789f/result.json).
Confirmou arranque, CLI, rotas elegíveis, ACK/forwarding de C1/C2, agregação,
isolamentoA/B, trace positivo `[C1,R2,R1,S1]`, renovação além do grant inicial
e ausência de troca após pulso150ms. O custo real medido R2→R1 subiu para
965620µs (inclui filas); R2→R3 ficou49742µs. A seleção passou paraR3, custo
69692µs.

O oráculo de adaptação exige **evento SUCCESS**, não apenas mudança de seleção
ou de forwarding. Observou-se:

1. STARTED `SUSTAINED_IMPROVEMENT`, operação
   `05a0c597-98d9-4c8c-bb06-4aa44d669c35`, caminho anterior `[S1,R1,R2]`,
   proposto `[S1,R1,R3,R2]`, custos609166→57727µs.
2. FAILURE `PATH_CHANGED`, mesma operação, elapsed61451492ns.
3. Snapshot final com forwarding `[S1,R1,R3,R2]`, revisão39, mas **sem SUCCESS
   dessa troca**; isto não satisfaz o oráculo de troca por qualidade.
4. Prazo25s esgotado, resultado FAIL. Nenhum processo pendente, zero portas
   bloqueadas. Os passos de morte/reinício/poda não foram alcançados nesta run.

**Causa ainda não estabelecida.** Correlacionar ACTIVATE/ACK/ERROR, revisão e
path no wire.jsonl com STARTED/FAILURE/recuperação antes de propor qualquer
correção. O proxy atrasa todos os frames e introduz HOL; isso é uma característica
documentada, não uma causa atribuída sem prova. Não aumentar prazos nem remover
o requisito SUCCESS para declarar esta run verde.

## Negativos preservados e ajustes já feitos

- `cenario1/20261122T232712Z-37b7325b`: sandbox bloqueou sockets. Nova execução
  autorizada no host, sem alterar produção.
- `cenario1/20261122T232727Z-31c1ab55`: catálogo vazio na leitura da CLI e
  falha do observador; callbacks do proxy escreveram depois do fecho do log.
  O lifecycle do log da ferramenta foi corrigido. O catálogo passou a ter
  observação limitada de prontidão, sem inventar entradas.
- `cenario1/20261122T232853Z-8ba67ee2`: oráculo de renovação usava ACTIVATE;
  frames mostram20 SUBSCRIPTION_RENEW e25 ACTIVATE_ACK. Corrigido para o tipo14.
- `cenario2/20261122T233117Z-a9871c5f` e
  `cenario2/20261122T233325Z-bee35742`: CLI antes da ativação devolveu
  `TRACE_ERROR NO_ROUTE visited=[C2]`, sem frame ROUTE_TRACE emitido, apesar
  de uma listagem anterior mostrar seleção com métrica e mesma Session.
  **A causa desta divergência permanece por estabelecer.** A ferramenta agora
  preserva o terminal negativo da CLI e exige separadamente trace positivo
  após ACK. Esta alteração do oráculo não resolve nem elimina o negativo;
  deve ser revista antes da aceitação da demo.
- `cenario2/20261122T233224Z-866c7a83`: pedidos antes do novo adaptador ter
  catálogo completo: UNKNOWN_STREAM emC2; NO_ROUTE inicial emC1/R2, mas
  recuperação posterior com forwarding confirmado. Acrescentada pré-condição
  de rotas elegíveis/catálogo após repor o adaptador. A observação atual usa
  forwarding.localConsumers: o resultado terminal inicial de um ticket não
  é o estado vivo da árvore. Ambos permanecem nos snapshots.

Os controladores/logs/resultados anteriores não foram reescritos para PASS.
Nas runs recentes, tool-source/ e tool-sha256.json guardam os auxiliares usados;
as primeiras runs não tinham esse arquivo e não se lhes atribui a versão atual.

## Próxima iteração — ordem recomendada

1. Ler este handoff, `bolt.md`, DEMO.md e as duas limitações novas; confirmar
   branch/base/working tree, versões/JAR e hashes antes de executar.
2. Investigar com a run preservada a falha de troca por qualidade e o NO_ROUTE
   pré-ativação da CLI. Separar defeito da ferramenta de eventual defeito do
   sistema; apresentar reprodução e âmbito de qualquer correção de produção
   antes de a aplicar. Preservar testes e prazos históricos.
3. Rever a semântica dos oráculos e a interface: tickets iniciais versus
   forwarding vivo; negativo da CLI versus trace positivo por múltiplos saltos.
   Rever interrupção/SIGTERM, cleanup e saída da ferramenta com falha de portas.
4. Validar cenário2 completo, depois executar **três runs consecutivas de1 e
   três de2 sobre a mesma versão congelada**. Nenhuma série válida nesta iteração.
5. Executar cenário3 completo; se inviável, justificar redução a partir da
   Figura1 e guardar o negativo, sem alegar execução CORE.
6. Conferir HTML no browser, pré-requisitos/portas configuráveis e medir ensaio
   de10–15min. Executar clean verify de início/fim da nova iteração e documentar
   quaisquer diferenças face1459; preservar JAR/manifestos/source hashes.
7. Consolidar matriz, resultados, relatório final, critérios e commits pequenos.
   Só então propor aceitação da demonstração. Não marcar PHASE-05–07 iniciadas.

## Limitações históricas mantidas

R17 inicial continua sem causa estabelecida; V-018 com métricas controladas
não estabelece a sua causa. Os dois negativos novos são distintos de R17.
Sem UDP, fragmentação, MJPEG, player/GUI de vídeo ou CORE; `play` continua
NOT_IMPLEMENTED. A demonstração não é aprovação docente/submissão nem resolve
datas/conteúdo G-ASSESSMENTS. Não se inventa execução humana.
