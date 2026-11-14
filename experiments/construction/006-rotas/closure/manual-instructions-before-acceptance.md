# IMP-009 / V-012 — manual para repetição humana

**Repetição humana pendente.** O ensaio Codex é separado e não aprova Test.
O comando abaixo usa exclusivamente o JAR fixo do clean verify JDK21 e as APIs
públicas de RoutingService/OverlayNode. Não usa os classfiles mutáveis de target.
As etapas1–5 usam relógio/transporte controlados;6–7 abrem TCP loopback real com
cinco nós normais. Não reproduz vídeo, não executa GUI/CORE e não acrescenta CLI.

## Artefacto e comando

JAR: [ott-streaming-1.0-SNAPSHOT.jar](test-build-01/ott-streaming-1.0-SNAPSHOT.jar),
411736 bytes. SHA-256:

```text
1a1beb8b849ccf505e4266dda199fe439f93e2a54e2297058a2d43eb2265af93
```

Precisas de JDK21 com java/javac e permissão normal para sockets em localhost;
não é necessário sudo. A partir da raiz do repositório:

```bash
python3 -B experiments/construction/006-rotas/test-manual-tools/run-manual.py --operator Luis --kind human --java-home /usr/lib/jvm/java-21-openjdk-amd64
```

É criado e apresentado um diretório **novo** em test-manual-runs/. O wrapper
confere os hashes do JAR, das237 entradas não geradas de Projeto/ e dos dois
auxiliares antes/depois. Compila ManualAnnouncements.java contra esse JAR fora
de Projeto/. CodeSource confirma a origem real das classes de produto.

Lê o ESPERADO e prime Enter antes de cada uma das sete etapas. EOF/Ctrl+C abortam
com relatório de falha; não contam como manual completo. O wrapper só termina a
JVM que criou; o helper pede shutdown limitado dos nós. Não editar o JAR/fontes/
auxiliares entre as verificações de hash. Resultados parciais não são aprovação.

## O que conferir em cada etapa

| Etapa | Conferência esperada |
|---|---|
| 1 — Grafo em losango | Duas fontes S1/S2, dois streams cada, R1/R2 e C. Em C: A/B deS1=130µs viaR2, alternativa140 viaR1; C/D deS2=170. Primeira chegada140 seguida de130. Snapshots defensivos e nenhum upstream ativo/destino de dados |
| 2 — Ciclo | Aresta R1–R2 com custo200 conserva seleção130. Paths sem IDs repetidos; uma geração estabiliza. Duplicado não renova TTL nem causa reenvio; aos2s **simulados** há geração nova, não fim da publicidade periódica |
| 3 — Retirada | Suspender ofertas deS1 retira apenas A/B; S2 mantém C/D e custo170. Snapshot anterior continua com quatro streams. Retomar S1 produz seq nova e catálogo com quatro |
| 4 — TTL entre envios | O relógio avança8s **dentro da primeira chamada de envio**: apenas1 ANNOUNCE; o segundo é impedido por expiração no mesmo turno. Três sessões continuam saudáveis; referência persiste, catálogo/candidato expiram |
| 5 — Metadados/projeção | A selecionado=110 expira aos8s; B permite metadados até9s. visibleAt(8s) mostra metadados por1s e custo/upstream ausentes; projeção encadeada expira aos9s sem alterar Root/relógio/snapshot anterior. Aos17s referência persiste; título contraditório seq100 é rejeitado, seq100 coerente aceite, só época nova muda título |
| 6 — TCP real | Cinco OverlayNode normais e bootstrap, mesmo JAR, dois servidores/quatro streams, RTT real/codec v2. Bootstrap é encerrado; é capturado snapshot com quatro streams **e** geração nova, mantendo as mesmas sessões físicas. Saída graciosa deS1 deixa os streams deS2 e sobreviventes saudáveis |
| 7 — Terminação | Notificação NODE_LEAVE deS1 com escrita local confirmada, todos os componentes terminados, portas TCP reutilizáveis/portas de dados livres e schedulers controlados terminados |

Os custos/segundos das etapas controladas são **entradas de teste**, não medições
de uma bancada real. No TCP não se exige um RTT numérico inventado. Sob mudança
de métricas pode haver indisponibilidade transitória e espera por geração nova,
conforme Q-C-29. A observação verifica convergência num snapshot que satisfaz as
condições, não disponibilidade contínua. Não é prova de tráfego zero por captura.
Escrita NODE_LEAVE confirmada é local, não ACK remoto. Verificação de portas
reutilizáveis não afirma ausência de TIME_WAIT. Não há tráfego de vídeo.

## Saída e relatório

Resultado completo esperado: **53 CHECK_PASS**, `CHECK_COUNT=53`,
`NO_RESOURCES_PENDING`, `MANUAL_PASS`, `JAVA_EXIT=0`, `PASSED=true`.
O report.json regista operador/tipo declarados, comandos, JAR/hash, fontes/hash,
origem de classe, contagem, saída e terminação da JVM. Não autentica a pessoa;
a tua confirmação humana deve referir esse report.json e o resultado observado.

Quando terminares, indica o caminho do **novo report.json** e qualquer divergência.
Ainda não existe execução humana registada deste manual. Test, revisão final
Denji, integração e D32 não estão concluídos; sem fecho/commits/push autorizados.

## Ensaios técnicos do agente preservados

- [Primeiro ensaio](test-manual-runs/20261114T151721Z-4t1kaj5v/report.json):45 checks,
  falha na observação46 e Java1. O helper esperava só a seq avançar e depois lia
  outro snapshot vivo de catálogo. Log/resultados e versão do auxiliar preservados.
- [Correção de observação](test-manual-observation-fix.json): espera conjunta por
  geração nova e quatro streams, capturando esse snapshot imutável; mesma condição
  após saída da fonte. Nenhuma asserção retirada, prazo inalterado,53 checks.
  Fontes de Projeto/ e JAR **não mudaram**.
- [Ensaio final Codex](test-manual-runs/20261114T151924Z-_00ejxkk/report.json):
  **53 CHECK_PASS, MANUAL_PASS, Java0, passed=true**, hashes/origem do JAR corretos,
  JVM terminada e recursos encerrados. É kind=agent, não execução humana.
