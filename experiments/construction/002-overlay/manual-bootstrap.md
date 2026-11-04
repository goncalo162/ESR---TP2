# IMP-003 — Roteiro manual de consulta bootstrap

Roteiro executado pelo agente Codex em 2026-11-04T12:22:51Z → 2026-11-04T12:22:53Z,
saída Java 0, 34 CHECK_PASS e MANUAL_PASS.
[Log do agente](test-manual.log) e [comando/hashes](test-manual-command.json).
A tua repetição pessoal continua pendente; não equivale à revisão final atribuída
a Waheed. Esta documentação não aprova Test nem fecha TASK-006/bolt.

## Versão e âmbito

JAR de `mvn -B -ntp clean verify`/JDK21, saída 0, 856 testes atuais aprovados.
SHA-256 obrigatório para o roteiro:

`2c3c063ac58692d1fd813d7e7564b00af7068906d156cc99126827847d2a9ccc`

O launcher [ManualBootstrapCheck.java](ManualBootstrapCheck.java) fica fora de
Projeto/src, Maven/JUnit e JAR. Usa somente a API pública do serviço/loader/codec,
sem reflection ou seams de teste. Cria perfil/grafo temporários, escolhe cinco
portas loopback, reserva-as/liberta-as e comprova recusa nas quatro de vizinhos.
Os endpoints UDP são apenas configuração; não há tráfego de vídeo ou UDP.
Não usa nem altera os perfis do projeto. No fim remove somente o diretório
temporário que criou. Portas, UUID e instantes variam entre execuções.

## Repetição pessoal — Bash, a partir da raiz

Usa o JAR validado já disponível. O próprio roteiro verifica o hash antes de
abrir rede. Os comandos guardam a saída e o exit code Java num log pessoal novo:

```bash
cd /home/luis/ESR/ESR---TP2
set -o pipefail
ott_manual_log=$(mktemp /tmp/ott-imp003-manual.XXXXXX.log)
/usr/lib/jvm/java-21-openjdk-amd64/bin/java --class-path Projeto/target/ott-streaming-1.0-SNAPSHOT.jar experiments/construction/002-overlay/ManualBootstrapCheck.java Luis 2c3c063ac58692d1fd813d7e7564b00af7068906d156cc99126827847d2a9ccc 2>&1 | tee "$ott_manual_log"
ott_java_exit=${PIPESTATUS[0]}
printf 'JAVA_EXIT=%s\n' "$ott_java_exit" | tee -a "$ott_manual_log"
printf 'LOG=%s\n' "$ott_manual_log"
```

Resultado esperado: **34 CHECK_PASS**, uma linha **MANUAL_PASS**, nenhuma linha
MANUAL_FAIL, **JAVA_EXIT=0**. Se houver falha, conservar o log e o erro, sem o
sobrescrever com uma execução posterior. Para a revisão, indica o caminho LOG
e o resultado; a execução por Luis será preservada separadamente da do agente.
O operador pode ter outro nome real, substituindo Luis no argumento.

## O que os 34 checks verificam

- JDK21 e JAR com hash esperado; perfil BOOTSTRAP e quatro nós, overrides válidos.
- Quatro portas de vizinhos efetivamente recusam TCP; bootstrap liga-se à sua
  porta configurada. A reserva é preparação do harness, não um nó vizinho ativo.
- REGISTER válido devolve os adjacentes de R1, endpoints/papéis configurados,
  instâncias zero, correlação e identidade da execução bootstrap.
- Frame completo seguido de EOF; nova instância do mesmo nodeId recebe a mesma
  lista e não muda a instância do bootstrap.
- Endpoint errado → UNAUTHORIZED_NEIGHBOR; PING → INVALID_MESSAGE; correlação
  e resposta completa/EOF também no erro. Dois pedidos na mesma conexão têm
  apenas uma resposta, correlacionada com o primeiro.
- Pedidos silencioso e parcial: leitura com guarda de 250 ms ainda aguarda;
  depois fecho por prazo de consulta configurado em 500 ms, com guarda de leitura
  de 5000 ms. São leituras TCP temporizadas, sem Thread.sleep.
- Após esses timeouts o listener continua funcional; grafo/perfis ficam intactos.
- Shutdown com uma consulta à espera, retorno normal após waits internos,
  encerramento do peer, idempotência e reutilização da porta do listener.
- Diretório temporário removido no fim. Hashes dos dois ficheiros temporários
  são conferidos durante a execução e impressos no log; os bytes temporários
  são eliminados, sendo o gerador/protocolo do roteiro a receita de repetição.

Observações reais do agente: silent_elapsed_ms=504, partial_elapsed_ms=503,
shutdown_elapsed_ms=4. São observações de uma execução local e do relógio
monotónico do cliente; não são metas de overlay, garantia de desempenho ou
latência unidirecional. Uma máquina sobrecarregada pode ultrapassar a guarda;
a falha deve ser preservada como resultado dessa execução.

TCP real local e API embutível não demonstram CLI/HELLO/processos overlay
independentes, GUI, CORE, vídeo ou recuperação. As provas de write/flush,
janela take/fecho, cancelamento e saturação usam os testes controlados da suíte,
não a suposição de que um frame pequeno bloqueia um socket loopback.
