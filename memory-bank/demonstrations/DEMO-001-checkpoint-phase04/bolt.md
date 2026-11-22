---
id: "DEMO-001-checkpoint-phase04"
type: "simple-construction-bolt"
category: "demonstration-support"
status: "in-progress"
functional_increment: false
source_phase: null
demonstrated_phases: ["PHASE-01", "PHASE-02", "PHASE-03", "PHASE-04"]
requires_bolts: ["009-arvores", "024-arvores", "035-overlay-extra"]
current_stage: "implement"
created: "2026-11-22T23:42:27Z"
updated: "2026-11-22T23:40:32Z"
paused: true
pause_reason: "User requested stopping at the prototype checkpoint and resuming in another iteration"
completed: null
---
# DEMO-001 — Checkpoint do fecho da PHASE-04

## Objetivo e organização

Preparar uma demonstração reproduzível do plano de controlo concluído para o
docente, sem iniciar vídeo/PHASE-05 ou avaliação CORE/PHASE-06. Autorização:
pedido explícito do utilizador para planear, implementar, validar e criar
commits pequenos, terminando com working tree limpo na branch Denji.

O catálogo `memory-bank/bolts/` tem correspondência normativa 1:1 com os 35
IMPs/histórias do ADD. Escolhe-se **DEMO-001-checkpoint-phase04**, na coleção
separada `memory-bank/demonstrations/`, com a mesma estrutura de artefactos
de um bolt simples. Não se renumeram os 35 bolts nem se acrescenta um requisito
funcional ou dependência à PHASE-05. Este é trabalho de apoio entre fases.

## Âmbito e decisões

- Opção **C**: orquestrador Java 21, scripts shell e HTML estático regenerado
  a partir de snapshots. Sem bibliotecas, servidor HTTP, player ou dependências
  novas. O HTML pode ser aberto como ficheiro local; a consola controla o guião.
- JVM separada por nó; bootstrap e verificação CLI executam `OttApplication`
  do JAR. Para ativação e snapshots adicionais, um adaptador externo usa
  `OverlayNode`, `NetworkMonitor` e `RoutingService` reais, com factory de
  composição existente. O adaptador reside no package overlay apenas para
  capturar o router através dessa factory; não substitui classes do JAR.
- A CLI de produção não oferece subscribe/cancel. São comandos **da ferramenta**
  sobre `requestSubscription`/`SubscriptionTicket.cancel`, como em V-018.
  Nunca se afirma que `play` ativa uma árvore ou envia dados.
- Atraso real sem root: proxy TCP por endpoint, identifica a ligação pelo HELLO
  v2 e atrasa os frames nos dois sentidos. Os bytes originais são preservados.
  Routing/monitorização usam clock, transporte e amostras reais. Não se chama
  `onMetrics` para fabricar custos. Fila e retenção são limitadas pelo TCP e
  pelas capacidades da ferramenta (64 conexões/proxy, um frame <=65536 bytes
  por direção, comandos 4096 chars, 16 tickets/nó, janela 512 eventos/nó).
- Grafo de quatro/seis nós do enunciado; bootstrap é infraestrutura adicional.
  Tentar os onze nós da Figura 1, cinco clientes. Configurações por nó são
  produzidas e validadas por `ConfigurationLoader.loadDeployment` antes dos
  processos; ficam preservadas em cada run.
- Reutilizar oráculos C1–C4 de [V-018](../../../experiments/milestone-2/README.md),
  sem reescrever a evidência 44/44 anterior. Aqui morte significa SIGKILL de
  processo real, não fecho de TRANSPORT numa JVM única.
- Bootstrap termina antes de uma falha para demonstrar reparação sem nova
  consulta. Ao reiniciar uma JVM, o seu bootstrap inicial volta a ser legítimo
  e é apresentado separadamente da reparação dos sobreviventes.
- Sem alterações a produção, testes históricos, política P/K/D, leases ou
  fundações para fazer a demo passar. Perfis de demo documentam intervalos,
  portas e atrasos. Não se promete estabilidade infinita ou SLA.

## Critérios de aceitação

- [ ] Clean verify inicial e final com JDK21 e números reais preservados.
- [ ] Cenários 1 e 2 completos, três execuções consecutivas PASS de cada um,
  com processos terminados e listeners reutilizáveis.
- [ ] Arranque validado, HELLO/discovery TCP v2, ACTIVE, RTT e anúncios reais.
- [ ] Catálogo, rotas, alternativas e trace observáveis; CLI real e `play`
  deliberadamente NOT_IMPLEMENTED.
- [ ] Dois clientes e dois streams: ACK, intenção isolada, tronco agregado,
  renovação, cancelamento de um sem afetar o outro e poda do último.
- [ ] Morte de relay real, NeighborDown, cache e recuperação automática da
  intenção, sem bootstrap nem reinício do consumidor.
- [ ] Atraso TCP real produz melhoria sustentada e troca confirmada; pulso
  transitório não oscila; evento SUCCESS observa revisão já publicada.
- [ ] Procura abandonada após morte real de cliente é removida. Expiração
  isolada de leases e casos negativos são ligados à evidência histórica,
  sem atribuir ao transporte fechado uma prova de expiração pura.
- [ ] Cenário 3 tentado; resultado completo ou redução explicitamente justificada.
- [ ] DEMO.md, matriz para artigo, resultados e limitações entregues; fases
  05–07 preservadas; working tree limpo com commits locais na Denji.

## Plano e riscos

[Plano](implementation-plan.md) · [Validação](validation-plan.md) ·
[Implement pendente](implementation-walkthrough.md) · [Handoff de retoma](HANDOFF.md).

**Checkpoint atual: protótipo pausado por pedido do utilizador.** Cenário1
com uma run PASS; cenário2 FAIL na adaptação por qualidade; cenário3 e as
três repetições por cenário ainda por executar. Sem fecho/aceitação da demo.

RTT localhost tem jitter: os oráculos observam estado, amostras e eventos,
não esperas cegas. Delay afeta todos os frames da ligação e pode causar HOL;
não é emulação de banda/perdas ou de uma underlay CORE. Portas em 127/8 e
permissões para sockets locais/JVMs são pré-requisitos da bancada Linux.
SIGKILL apenas em processos criados pela ferramenta. R17 histórico continua
sem causa estabelecida; métricas controladas anteriores e esta demonstração
não estabelecem a sua causa. G-ASSESSMENTS não é inventado como satisfeito;
datas/conteúdo docente e submissão externa permanecem fora desta preparação.
