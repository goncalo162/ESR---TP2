---
stage: test
bolt: 020-overlay
created: "2026-11-10T22:13:26Z"
---
# IMP-005 — Test aprovado

O utilizador aprovou Test do IMP-005/020-overlay em 2026-11-10T22:22:56Z,
com V-008, os 981 testes registados, o manual de Luis, a verificação
complementar do agente e a resolução de Q-C-13. Confirmou uma nova execução
de V-ADD no repositório com saída 0, zero erros e 45 fixtures verificadas.
O bolt permanece in-progress até à revisão final de Waheed e à integração
da versão revista.

## V-008 e regressão

O [clean verify](../../../experiments/construction/020-overlay/test-clean-verify-01.log)
executou `mvn -B -ntp clean verify` em `Projeto/`, com Java 21.0.13 e
Surefire JUnitPlatformProvider. Terminou em 2026-11-10T18:43:51Z com BUILD
SUCCESS, saída 0: **981 testes, 0 falhas, 0 erros, 0 ignorados**. Os
[sete XML Surefire](../../../experiments/construction/020-overlay/test-clean-verify-01-xml/)
registam:

| Suite | Testes |
|---|---:|
| ConfigurationLoaderTest | 740 |
| MessageCodecTest | 66 |
| NeighborManagerTest | 47 |
| TransportTest | 31 |
| BootstrapServerTest | 19 |
| NeighborManagerLifecycleTest | 32 |
| OttApplicationTest | 46 |

O [resumo verificável](../../../experiments/construction/020-overlay/test-clean-verify-summary.json)
liga os XML, o log e os cinco hashes das fontes aprovadas. Os 981 da regressão
incremental de Implement são uma execução anterior; não se somam a este total.
O [JAR arquivado](../../../experiments/construction/020-overlay/verified-jar/ott-streaming-1.0-SNAPSHOT.jar)
usado nos manuais tem SHA-256
`303facafc148b96bd2cc0d98ea6afc7a773a50cb2b49d1d5c6c6c4c481de5b52`.
Para repetir o [roteiro](../../../experiments/construction/020-overlay/manual-instructions.md),
usar esse JAR verificado. O JAR local em `Projeto/target/` é anterior e ficou
intacto; os ficheiros gerados não entram no diff deste checkpoint.

O [mapa L01–L12/M01–M18](../../../experiments/construction/020-overlay/test-oracle-map.json)
liga os casos aos nomes exatos no XML e às execuções de M17–M18. A
[matriz de Implement](implement-oracles.md) conserva o estado histórico. V-008
tem prova dos quatro papéis em JVMs distintas, consulta de vizinhos, configuração
inválida sem bind, saídas de erro esperadas e continuidade dos sobreviventes
após saída ou morte de um peer. M06/M08 exercitam TCP com um peer controlado:
um nó já descoberto reconecta sem bootstrap e um ERROR legítimo fica observável.
L01–L12 verificam a saída v1, prazo global e isolamento de sessões. O resultado
de write+flush é local. M13 verifica joins antes da saída de uma JVM com
descritores reais e serviço controlado, incluindo falha e bloqueio da saída.

## Execuções manuais distintas

**Luis.** A [pasta pessoal](../../../experiments/construction/020-overlay/manual-Luis-retoma-02.X7LfJJ/)
preserva 38 ficheiros identificados e verificados pelo hash. O
[resumo de Luis](../../../experiments/construction/020-overlay/test-manual-luis.json)
liga os 16 códigos Java: nove 0, três 130 (interrupção), três 2 (entrada inválida)
e um 3 (bind ocupado). SERVER e RELAY mantiveram os PIDs 45010 e 45203 ao
observar a nova instância do CLIENT. A verificação pessoal registou sete portas
disponíveis e nenhuma correspondência na procura de processos do teste. As
saídas por sinal e os cenários negativos conservam os códigos originais. Os
[oito ficheiros preliminares de Francisco](../../../experiments/construction/020-overlay/manual-Francisco-execucao-01/)
continuam separados e não são contados como uma segunda execução humana completa.

**Agente.** O [relatório final](../../../experiments/construction/020-overlay/agent-verification/final/report.json)
é PASS, de 2026-11-10T21:57:09Z. Consolida dois cenários controlados e a
verificação final: 20 novas respostas a `neighbors` com CLIENT e BOOTSTRAP
desligados, dez por pipe e dez por terminal virtual (PTY). Para iniciar um
novo CLIENT, o bootstrap voltou a estar disponível para a descoberta inicial.
O cliente reconectou com nova identidade de instância, com os mesmos PIDs de
SERVER e RELAY em cada cenário. EOF e `quit` foram exercitados e as 12 JVM
criadas pelo agente terminaram com saída 0. A verificação final encontrou as
sete portas livres, nenhum processo do teste pendente e os hashes preservados.

Os relatórios preliminares mantêm o resultado original **FAIL**. A
[sondagem por pipe](../../../experiments/construction/020-overlay/agent-verification/pipe-preliminary/report.json)
concluiu os passos funcionais, mas a tentativa imediata de reutilizar as
portas devolveu `Address already in use`. A
[sondagem por PTY](../../../experiments/construction/020-overlay/agent-verification/pty-preliminary/report.json)
observou `TIME-WAIT` sem listener; o bind imediato falhou e a captura foi
interrompida por `BrokenPipeError`. A verificação final de portas passou depois.
Foram preservados os 36 ficheiros brutos, com
[origens e hashes](../../../experiments/construction/020-overlay/test-agent-archive.json),
e o [resumo complementar](../../../experiments/construction/020-overlay/test-agent-verification.json)
distingue os dois cenários da verificação posterior.

O volume de diagnósticos de transporte dificulta ler o resultado dos comandos
quando há retries. As respostas novas confirmam a utilização da CLI nos
cenários controlados; a evidência é local e não estabelece desempenho sob
carga, operação em CORE, GUI ou entrega de vídeo.

## Diff e condições de fecho

O [diff Java acumulado](../../../experiments/construction/020-overlay/test-code-vs-operational-base.diff)
contra a base operacional `90cded6` mantém cinco ficheiros (+1346/−14).
Nesta etapa não há diff de `Projeto/` face ao WIP `b425ad3`: os 157 hashes
fora de `target/` mantiveram-se. O
[diff documental](../../../experiments/construction/020-overlay/test-checkpoint.diff)
e a [verificação do checkpoint](../../../experiments/construction/020-overlay/test-consolidation-results.json)
distinguem as mudanças desta etapa das provas brutas. A
[primeira execução de V-ADD](../../../experiments/construction/020-overlay/test-consolidation-validation.md)
devolveu saída 1 por uma ligação local para `adr-template.md` em `.specsmd/`.
O [ensaio isolado do estado de entrada](../../../experiments/construction/020-overlay/test-vadd-entry-repro.log)
reproduziu o mesmo erro antes destas alterações. As 45 fixtures foram verificadas
nessa execução. A correção documental autorizada em
[Q-C-13](../../../docs/construction-duvidas.md#q-c-13--referência-a-template-inexistente-no-v-add-de-imp-005)
removeu as referências ao template inexistente. O
[novo V-ADD](../../../experiments/construction/020-overlay/q-c-13-v-add.log)
passou no repositório com saída 0, zero erros e 45 fixtures verificadas.
A [versão anterior deste relatório](../../../experiments/construction/020-overlay/test-walkthrough-before-approval.md)
foi arquivada com o hash registado no manifesto do checkpoint.
O [pacote para revisão final](closure-walkthrough.md) identifica as condições
de fecho ainda pendentes.
