# IMP-007 — checkpoint Implement para aprovação

Registo 2026-11-12T15:56:53.776762+00:00. Base/HEAD:3dedf007f52f43f53eaf254bef7c25e35f986e5a, main.
Model/Design/ADR e primeiro passo de interfaces/vermelhos aprovados pelo utilizador.
Comportamentos autorizados nos cinco caminhos do Design; Q-C-25 acrescentou apenas
o teste histórico indicado, exclusivamente pelos dois patches aprovados.
Implement preparado para aprovação; Test não iniciado. Nenhum commit/staging/push.

## Resultado e alterações

NetworkMonitor implementa sondas7/8 v2 por Session corrente, primeira elegível Up,
um pending antes de send, earlyPong limitado, correlação completa, timeout de
sondas admitidas, RTT local monotónico incluindo filas, EWMA/custo ceil mínimo1,
reset completo a cada nova conexão e EOF imediato. IDs/contadores não embrulham;
wrap de nanoTime é tratado por subtração. Candidatos sem sessão não são sondados.
LinkMetrics valida invariantes/idade; snapshots defensivos e diagnóstico limitado.

Composição no mesmo owner/manager, sem transporte/CLI/executor de estado novo.
Helper de timers JDK limitado: dois jobs físicos incluindo canceled-running;
N=min(63,maxConnections),B=min(eventsCapacity,2*N), atéB sends e um close físico
reservado. Reservas incluem callbacks ainda em execução/continuções queued,
mesmo depois de retirar estado lógico. Libertação física acorda fechos pendentes,
sem assumir que a continuação serial foi o último participante a terminar.
Decisão de despachar a continuação e flag queued protegidas pelo mesmo ledger da
retirada: stop não pode libertar ticket e deixar depois um callback por contar.

CLOSED/OVERFLOW não contam como perda remota; invalidação/desconexão usa Session
esperada. INVALID_MESSAGE/erro local inesperado/rejeição/consumidor falhado são
falhas locais observáveis. Falta de quota própria adia sonda sem perda; echo
excedente descartado com motivo. Sem ERROR a ERROR, retries próprios ou histórico
ilimitado. Churn que esgote a fila bounded de fechos torna-se RESOURCE/local
LOCAL_CAPACITY fatal observável em vez de abandonar silenciosamente um fecho.

Paragem fecha admissão antes do graceful leave; primeiro prazo preservado, cleanup
lógico distinto de físico, waits fora do owner/locks e sem renovar timeout por
participante ou repetição. Hook de cleanup do monitor é instalado já no construtor
validado (antes de start/worker), para cobrir close entre composição e primeiro
despacho; start permanece só owner. É antecipação de proteção de rollback, não
segundo proprietário. Nenhum socket/worker é aberto no construtor. Hook fica
registado enquanto houver callbacks queued por retirar e é removido após retirada
física/cleanup. Owner emprestado só é fechado pelo OverlayNode, seu proprietário.

## Allowlist efetiva e justificação de substituições em OverlayNode

Cinco ficheiros do Design:

1. Projeto/src/main/java/pt/uminho/esr/ott/monitoring/NetworkMonitor.java.
2. Projeto/src/main/java/pt/uminho/esr/ott/monitoring/LinkMetrics.java.
3. Projeto/src/test/java/pt/uminho/esr/ott/monitoring/NetworkMonitorTest.java.
4. Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java.
5. Projeto/src/test/java/pt/uminho/esr/ott/overlay/OverlayNodeMonitoringTest.java.

Exceção Q-C-25: Projeto/src/test/java/pt/uminho/esr/ott/app/OttApplicationTest.java,
limitada à cadeia exata q-c-25-proposed.patch +q-c-25-discovery-proposed.patch,
autorizados explicitamente pelo utilizador após falhas reais. Nada mais desse
ficheiro alterado; hashes antes/depois e patch reverse-check em evidência.

Cada substituição de OverlayNode responde a um critério concreto:

- **Construtores/campos:** delegação para Factory e referência ao monitor são
  necessários à composição Q-C-23 e teste próprio. Conserva validação offline
  via NeighborManager, configuração e instanceId únicas. Sem segundo manager
  operacional ou owner; construtor de validação anterior já existia e não arranca.
- **Snapshot novo:** substitui placeholder de interface por vista vazia antes de
  start e leitura imutável do monitor, para observabilidade ADR-022 sem CLI nova.
- **start:** substitui arranque isolado do manager por construção do monitor e
  adaptador que delega send/disconnect existentes, enfileirando monitor.start
  antes do bind do manager. Manager instala rollback hook antes de close poder
  adquirir o lock; não atribui manager operacional em falha da factory anterior
  ao arranque. Completions para consumidores/rollback saem do lock. Exige-o
  não perder o primeiro Up e terminar em falhas parciais (Design4/D01/D15).
- **Lambda de eventos:** extraída para onNeighborEvent para adicionar tap sem
  alterar o Event sealed do manager. Mantém a condição histórica que regista
  Diagnostic FAILED/CLOSED crítico; finally entrega exatamente o objeto original,
  uma vez/ordem, mesmo se o tap falhar. Critério M18 e preservação de ERROR/restantes.
- **requestShutdown:** retira synchronized da assinatura e usa lock curto só
  para primeiro t0/budget/future idempotente. Pede fecho do monitor antes do
  manager, usa restante global e combina resultados sem bloquear. Falha num
  pedido não evita o outro. Conserva ShutdownReport/NODE_LEAVE/razão e deadline
  original; exige-o Design9/D15, sem espera ou consumidor externo sob lock.
- **awaitTermination:** acrescenta await físico do monitor com restante e
  conserva manager antes/owner no fim, usando &= sem short-circuit para ainda
  pedir todos os fechos. Critério: lógico não prova físico; budget não renova.
- **close/falhas:** extrai stopImmediately comum para parar monitor antes de
  manager e solicitar fecho do owner pertencente ao nó; guarda primeiro prazo,
  não faz self-join, espera externa fora de lock. Necessário a fatal do tap,
  rollback/close-before-start/callback e deadline único. Outros getters/APIs e
  send/NeighborManager não são substituídos nem reimplementados.

## Testes e evidência real

Regressão incremental em Projeto: `mvn -B -ntp -Dott.test.evidence=<pasta absoluta>
test`, implement-regression-04.log, exit0: **1176 testes,0falhas/0erros/0ignorados**.
52 monitoring+11 integração própria. XML/TXT completos copiados para
experiments/construction/005-rotas/implement-regression-04-surefire/.
Os1113 do fecho anterior são históricos; 1176 vem desta execução, não de soma presumida.

[Mapa M01–M22/D01–D16 e métodos reais](implementation-oracles.md). Inclui relógio,
owner/scheduler/transportes falsos, valores EWMA independentes, corridas de admissão,
recusa/timeout/IDs antigos,63 sessões/churn/caps, callback ainda em execução,
close reservado/serializado, contadores/clock no limite e cleanup lógico/físico.
O teste fake antigo informava isOwnerThread=true até ao await externo: passou a
representar explicitamente esse contexto sem retirar a asserção de terminação;
há agora também teste que exige rejeitar self-join.

Integração: bootstrap/três nós TCP reais loopback, Session/error/EOF, reparação
HELLO20 depois de fechar bootstrap e relays, mesmo owner e sessões, eventos
originais por identidade/ordem/falha do tap, shutdown dentro de callback, factory
falhada, close imediato, prazo global com admissão retida e JVM real da CLI.
Log/perfil/JAVA_EXIT=0 do smoke em implement-real-regression04/, sem declarar
manual humano, GUI, CORE ou medições de desempenho.

Tentativas preservadas: erros de compilação em testes novos (enum MALFORMED/
SourceKey aridade) corrigidos sem tocar produção histórica; sandbox negou sockets
em05/07 e na primeira tentativa focada, depois executados fora dela com aprovação.
Reboot interrompeu apenas o pedido de execução06, sem log: a execução06 posterior
à retoma é real.06 falhou em expectativa STOPPED no novo smoke: corrigida para
SHUTDOWN phase=closing-output conforme CLI existente, preservando exit0.
Regressão01 teve2falhas (identidade do novo teste e peer histórico);02 teve1
histórica;03 teve1por Query18;04 passou após os dois patches autorizados. XML
anteriores de01/02/03 preservados separadamente. A procura global de processos na
sandbox falhou: não se infere daí ausência de processos; a prova de término é a
join/exit/assert dos recursos pertencentes aos testes e ao subprocesso.

## Estado e limites

V-010 exercitado tecnicamente nesta fase, sem aprovação de Test. M22 conserva
clean verify novo, manual humano, revisão final do código e integração pendentes.
Fillmore reviu apenas Design, conforme confirmação; não se atribui revisão do
código. Nenhum JAR novo foi produzido por mvn test nem se reutiliza hash antigo
como resultado deste incremento. Sem nova ADR/dependência/CI/linter/cobertura.

Target/ contém outputs gerados (incluindo maven-status tracked), separados dos
diffs de fontes/allowlist de entrega e listados na auditoria. Nenhum cleanup
ou reposição global. Fechos anteriores e ficheiros fora da allowlist preservados.

**Parar no checkpoint Implement para aprovação**, sem Test, staging, commits,
push, DONE ou início de outro IMP. Sem gates externos adicionais.
