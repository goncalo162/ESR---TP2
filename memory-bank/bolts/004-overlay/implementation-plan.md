---
stage: plan
bolt: 004-overlay
created: "2026-11-10T22:38:06Z"
---
# IMP-006 — Plan do primeiro marco intermédio

O IMP-006 / TASK-009 prepara uma demonstração curta da descoberta, do handshake,
da saída e da reconexão entre nós da overlay. Outro elemento deve conseguir
repetir o roteiro, localizar os eventos nos logs e verificar os critérios V-009.
O incremento incide sobre `experiments/milestone-1/`. As alterações de controlo
da etapa pertencem ao memory-bank e aos índices de progresso.
[ADD §8, IMP-006](../../../add.md#imp-006) ·
[TASK-009](../../../docs/tasks/PHASE-02/TASK-009.md) ·
[Estratégia de testes](../../../docs/09-TESTING-STRATEGY.md#v-009--preparar-evidência-do-primeiro-marco-intermédio).

## Base e decisões aplicáveis

- IMP-001 / 001-base-validada e IMP-005 / 020-overlay estão fechados. A base
  comum verificada é `89d02014b919290db8c619748973d12ebee246a6`. A CLI da TASK-008
  e o seu clean verify de 981 testes fornecem a base operacional.
  [Fecho IMP-001](../001-base-validada/closure-walkthrough.md) ·
  [Fecho IMP-005](../020-overlay/closure-walkthrough.md).
- A demonstração pode partir do JAR validado do IMP-005 com Java 21. O operador
  confirma o SHA-256 e os perfis antes da execução. O JAR local gerado em
  `Projeto/target/` não é a referência.
  [JAR de referência](../../../experiments/construction/020-overlay/verified-jar/ott-streaming-1.0-SNAPSHOT.jar) ·
  [hash](../../../experiments/construction/020-overlay/verified-jar.sha256).
- ADR-008 exige evidência incremental com oráculo próprio. Loopback comprova
  este marco de controlo e a validação dos cenários CORE pertence aos incrementos
  previstos. ADR-013 reserva o bootstrap à descoberta inicial. ADR-023 pede
  reprodução com configurações e evidência identificadas. ADR-024 mantém o ADD
  como autoridade e limita o incremento a V-009.
  [Registo de decisões do ADD](../../../add.md#5-registo-de-decisões).

## Entregáveis previstos para Implement e Test

- `experiments/milestone-1/README.md` com pré-requisitos, comandos por terminal,
  topologia, sequência esperada, verificação de cada resultado, limitações e
  procedimento de limpeza.
- `experiments/milestone-1/profiles/` com perfis relativos e grafo triangular
  para BOOT e três nós overlay. Reutilizar os perfis do IMP-005 como referência.
  Validar a implantação e as portas antes de iniciar os processos. As portas e
  tempos efetivos ficam registados por execução.
  [Perfis do IMP-005](../../../experiments/construction/020-overlay/manual-profiles/).
- `experiments/milestone-1/runs/<identificador>/` com manifesto datado, ambiente,
  versão e hashes, comandos reais, cronologia UTC, logs por processo, estados de
  saída, captura de controlo, resumo de frames e resultados. Usar um diretório
  novo por execução e conservar os resultados parciais. Os logs e capturas de
  revisão são evidência desta execução, com identificação do operador.
- Acompanhamento em dois passos. Implement entrega o roteiro e pode fazer um
  ensaio técnico. Test recolhe e confronta a repetição efetiva por outro
  elemento. O resumo indica que execução sustenta cada condição de V-009.

## Roteiro previsto e oráculos de V-009

| Passo | Ação | Evidência esperada |
|---|---|---|
| 1 | Começar captura de controlo, arrancar BOOT e depois CLIENT e RELAY. Aguardar a descoberta desses dois nós antes de arrancar o SERVER. | Consultas REGISTER/REGISTER_OK correlacionadas e conexões de consulta fechadas. O endpoint do SERVER consta das respostas enquanto o processo está ausente. |
| 2 | Iniciar o SERVER e pedir `neighbors` nos três nós após a convergência. | A consulta inicial do SERVER fica registada. HELLO/HELLO_OK em ligações diretas. Cada nó mostra dois vizinhos ACTIVE sem duplicados. Guardar PIDs, instanceIds e connectionIds de referência. |
| 3 | Observar a overlay estável ainda com BOOT em execução e depois encerrar BOOT com `quit`. | Registar o intervalo observado, o fim da JVM BOOT e a ausência de listener. Após as consultas iniciais e durante a observação, verificar que os nós estabilizados não iniciam outras consultas, renovações, sondas ou avisos ao bootstrap. |
| 4 | Com BOOT parado, fechar apenas o socket estabelecido entre SERVER e RELAY, mantendo ambos os processos em execução. | `NeighborDown` por transporte seguido de novo HELLO/HELLO_OK e `NeighborUp` nos mesmos processos. Confirmar os mesmos PIDs e instanceIds, novos connectionIds e as demais arestas preservadas. Durante a reconexão, a captura continua a abranger a porta de BOOT para detetar tentativas. |
| 5 | Ainda com BOOT parado, encerrar o CLIENT com `quit`. | Capturar NODE_LEAVE nos canais diretos e eventos `NeighborDown` com motivo LEAVE nos sobreviventes. Confirmar a saída normal do CLIENT. SERVER e RELAY continuam em execução com a ligação reconstituída. |
| 6 | Se for demonstrado um novo CLIENT, voltar a iniciar BOOT antes da nova descoberta. Encerrar os processos do ensaio e fechar a captura. | A nova JVM tem novo instanceId e efetua a própria consulta. A verificação final regista os códigos de saída, os processos do ensaio terminados e o estado das portas. |

O passo 4 usa, como abordagem planeada, Linux com `ss -K` limitado a uma sessão
TCP própria do ensaio. Pré-verificar a disponibilidade e permissão. Identificar
um único socket estabelecido pela família, endereços e portas de origem e
destino, e confirmar a pertença ao PID observado. Abortar o corte se a seleção
for ambígua. A ferramenta de bancada não exige mudanças nos componentes da
aplicação. Se a bancada não permitir este corte, a reconexão entre processos
já descobertos fica por demonstrar e a alternativa deve ser revista.

A captura prevista usa `tcpdump` no loopback, limitada às portas TCP do ensaio
e a uma janela com fim definido. Registar os limites e eventuais perdas da
captura. Analisar por conexão e direção com reconstituição TCP para identificar
os tipos, emissores e requestIds. Usar os bytes de controlo v1 e o codec existente
como referência. Distinguir ACK/FIN de transporte de frames da aplicação.
A ausência de consultas periódicas é avaliada na janela capturada, incluindo
um intervalo estável antes do encerramento de BOOT. A aprovação do Plan exige
uma justificação independente do backoff dos vizinhos. Definir a janela,
registar os limites e restringir a conclusão ao intervalo capturado. Uma
captura interrompida ou incompleta fica registada como tal.

Antes de marcar V-009 como executado, confrontar a repetição com RF-001–004:
CLI e papéis para RF-001, saída e continuidade para RF-002, adjacências TCP
para RF-003, consulta inicial e reconexão sem bootstrap para RF-004. O roteiro
distingue estas observações de encaminhamento, streaming, GUI, medidas de
desempenho e validação dos cenários CORE.

## Gate e checkpoint

A situação da primeira avaliação permanece **pendente de informação docente**
sobre data e conteúdo. O enunciado enumera as duas avaliações intermédias e
o ADD permite registar a pendência em G-ASSESSMENTS. Atualizar o roteiro quando
houver informação confirmada.
[Enunciado](../../../ESR-TP2-26-27.pdf) ·
[Gates do ADD](../../../add.md#gates-e-momento-de-resolução).

- [ ] Roteiro repetível e logs datados disponíveis.
- [ ] Limitações da versão intermédia explicitadas.
- [ ] Situação da avaliação registada sem datas inventadas.
- [ ] Validação V-009 executada e evidência ligada no progresso.

Plan aprovado pelo utilizador, com registo em 2026-11-10T22:50:50Z. Os comandos do cenário e a intervenção de bancada ainda
não foram executados. Para fechar a TASK-009, o resultado de V-009 fica ligado
ao progresso, Fillmore revê a versão final e a versão revista é integrada.
[Definition of Done](../../../docs/14-DEFINITION-OF-DONE.md).
