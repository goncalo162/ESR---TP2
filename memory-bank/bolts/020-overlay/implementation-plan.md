---
stage: plan
bolt: 020-overlay
created: "2026-11-10T17:38:56Z"
---
# IMP-005 — Plan para aprovação

⏳ Plan preparado, não aprovado. ✅ Model aceite pelo utilizador como preparação
 de Plan. [ ] Implement e Test não iniciados. Tipo Simple preservado.

## 1. Objetivo, autoridade e dependências

Compor CLI Java 21 para BOOTSTRAP/SERVER/RELAY/CLIENT com serviços existentes,
entrada validada antes de sockets, neighbors/quit, logs e shutdown limitado.
SERVER/CLIENT ainda não emitem/reproduzem vídeo. Não antecipar outro IMP.

Base operacional autorizada: `90cded6b8d321cfcbe369dd4f8dbed9a01ddf430`, main.
Referência histórica: `2ea748efd61ba5bdea355d2b5a02de6f6bd9a8c5`.
Não resetar checkout. Preservar alterações documentais do Model e qualquer
alteração alheia. A fotografia de entrada deste Plan está em
[plan-entry.json](../../../experiments/construction/020-overlay/plan-entry.json).

Dependências conferidas no Model e mantidas: IMP-001/001-base-validada,
IMP-002/019-overlay e IMP-004/003-overlay complete, TASK-003/006/007 DONE.
Enunciado → specs → intents → documentação → código; ADD 2/5/6/8–12,
contrato 07 secções 3–7, estratégia 09/V-008, DoD 14 e progresso 15 aplicam-se.
A lista futura legada LifecycleTest/StreamingClient de TASK-008 é subordinada ao
ADD: sem implementar StreamingClient nesta tarefa.

## 2. Q-C-09 e allowlist futura de código

O utilizador aprovou extensão mínima aditiva de lifecycle no NeighborManager e
respetivos testes, mantendo send de aplicação inalterado. A lacuna de âmbito
fica resolvida por decisão explícita; conformidade técnica ainda por demonstrar.
O fecho histórico de IMP-004/003-overlay/TASK-007 não muda e os seus testes
D01–D22 e Q-C-05/Q-C-06 permanecem íntegros.

1 - **OttApplication.java**, main/app: parser, composição por papel, CLI e resultado.
2 - **OverlayNode.java**, main/overlay: owner único, manager emprestado e lifecycle.
3 - **OttApplicationTest.java**, test/app: M01–M18/V-008 e subprocessos reais.
4 - **NeighborManager.java**, main/overlay: única exceção de produção, operação
    lifecycle separada; adaptações internas mínimas de admissão/drenagem/cleanup.
5 - **NeighborManagerLifecycleTest.java**, test/overlay: ficheiro novo dedicado,
    fakes de relógio/transporte/owner e TCP real; não editar os testes históricos.

Allowlist exata para a etapa futura, ainda sem autorização de alteração Java:

- `Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java`
- `Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java`
- `Projeto/src/test/java/pt/uminho/esr/ott/app/OttApplicationTest.java`
- `Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java`
- `Projeto/src/test/java/pt/uminho/esr/ott/overlay/NeighborManagerLifecycleTest.java`

Todos os caminhos começam em `Projeto/src/` e nos namespaces existentes
`pt/uminho/esr/ott/`. Os ficheiros de produção são `main/java/`; testes em
`test/java/`. Não alterar Connection/Transport/BootstrapServer/configuração,
POM, validadores, fixtures v1 ou outras classes. Se a execução demonstrar
necessidade fora destes cinco ficheiros, parar a parte afetada e pedir decisão.
A documentação ADD/07/TASK/progresso/bolt/log e evidência do incremento é adicional
a esta allowlist de código; não constitui autorização de código nesta etapa.

## 3. Contrato público planeado de lifecycle

Assinatura futura em NeighborManager (ainda não implementada):

`public CompletionStage<ShutdownReport> requestShutdown(int reason, Duration remainingBudget)`

Tipos públicos aninhados, imutáveis:
`record LeaveAttempt(Neighbor.Session session, LeaveOutcome outcome)`;
`record ShutdownReport(List<LeaveAttempt> attempts, boolean notificationTimedOut)`;
`enum LeaveOutcome { LOCAL_WRITE_CONFIRMED, WRITE_FAILED, TIMED_OUT, ABORTED }`.
Outcomes são diagnósticos locais, sem códigos wire. O record copia defensivamente
a lista; entradas não nulas e sem duplicação por sessão. Lista no máximo igual
ao número de vizinhos diretos autorizados, limitada pelos caps já existentes.
Não expor Channel/Connection nem admitir payload de aplicação nesta operação.

reason apenas 0 normal ou 1 erro, exatamente como ControlMessage.Leave existente;
null, duração negativa, duração não representável ou reason inválido falham
sincronamente antes de alterar estado/alocar trabalhos. Duração zero é válida:
fecho imediato, sem aguardar notificação. Valor superior ao shutdownTimeout
configurado é limitado ao mesmo; não aumenta o cap. Primeiro pedido válido
fixa reason e início monotónico; pedidos seguintes devolvem a mesma vista do
resultado, sem renovar prazo, mudar reason ou repetir envios. A vista não permite
cancelamento externo do trabalho interno.

O future conclui quando a fase de notificação termina e o fecho foi solicitado,
**não** quando TCP/threads/owner terminaram fisicamente. A confirmação é write+flush
local de Channel.sendAndFlush, nunca ACK, entrega remota ou processamento do peer.
Cada tentativa guarda a sessão integral (connectionId e instanceId incluídos).
A composição chama awaitTermination fora do owner com o tempo global restante.
O manager não termina owner emprestado; OverlayNode é responsável por ele.

requestClose/close conservam o fecho imediato existente; não passam a aguardar
NODE_LEAVE. Um pedido imediato durante drenagem preempta-a e conclui os resultados
pendentes como ABORTED. Falha fatal nunca fica à espera do aviso de saída.
Antes de start ou sem sessões, devolver relatório vazio e solicitar fecho,
sem criar serviços; limpeza e start futuro respeitam o encerramento definitivo.

## 4. Algoritmo de drenagem e budget único

1 - **Admissão**: à entrada do primeiro pedido, fechar atomicamente a admissão
    externa. Novos start/send/disconnect não produzem trabalho de rede; send
    conserva CLOSED quando encerrado e a validação de aplicação 7–17 intacta.
    Comandos já na fila que ainda não executaram também não enviam aplicação.
2 - **Owner**: oferecer um único evento lifecycle limitado no owner existente.
    Separar estado de drenagem da flag de fecho imediato atual: ativar stopping
    cedo hoje destruiria os canais e bloquearia post(), impedindo o flush.
    A transição no owner cancela descoberta/dials/timers/retries e invalida suas
    gerações. Nenhum HELLO novo, nova sessão, tentativa ou reconexão é promovido.
    Uma operação anterior já linearizada pode concluir; não é tráfego novo.
3 - **Sessões**: capturar apenas contextos ACTIVE, diretos e ainda correntes,
    com associação autorizada a Peer/Session. Excluir bootstrap, descoberta,
    handshake incompleto e sessões substituídas. Revalidar contexto, connectionId
    e remote instanceId antes de emitir. Um NODE_LEAVE por sessão, com envelope
    v1 e identidade local reais; requestId novo não zero, payload reason 0/1.
4 - **Escrita**: usar sendAndFlush no Channel existente, paralelamente entre
    sessões e respeitando filas/caps atuais. Overflow/falha é WRITE_FAILED;
    não reenviar nem criar socket. Callbacks apenas oferecem conclusão no owner,
    sem esperar/logging bloqueante. Conclusões tardias/duplicadas/obsoletas não
    alteram resultado nem ressuscitam sessão. Eventos de fecho continuam válidos
    para libertar recursos, sem armar retry.
5 - **Prazo**: contar desde a entrada pública, incluindo espera no owner. A
    composição conserva t0/budget global desde a primeira paragem. [PRESSUPOSTO]
    reservar pelo menos metade do tempo restante para cleanup: limite da fase
    de notificação = metade do restante à entrada, sem propriedade nova ou
    alteração de cap/política wire. Todos os peers partilham esse limite,
    independentemente do grau. Timer usa infraestrutura existente e é cancelado
    ao terminar; não criar um scheduler por peer. O callback de prazo necessita
    de via lifecycle que permaneça admissível durante drenagem.
6 - **Fecho incondicional**: quando todos concluem, falham ou vence esse limite,
    fixar resultados restantes, solicitar requestClose imediato e publicar
    relatório. Timer indisponível, owner saturado/parado ou exceção no envio
    acionam a via de falha/fecho existente, sem deixar future pendente. A via de
    emergência só fecha recursos e publica falha: não muta estado de rede fora
    do owner. Um owner parado executa o cleanup registado.
7 - **Terminação**: coordenador fora do owner aguarda manager com o tempo restante,
    depois pede fecho e aguarda owner emprestado com o mesmo deadline. Nunca dar
    um timeout completo a cada recurso. Budget esgotado pede fecho a todos antes
    de reportar falha; timeout não equivale a recursos terminados.

A drenagem não muda a política de retry dos nós sobreviventes. O recetor conserva
o tratamento NODE_LEAVE existente. Alterações internas devem manter semântica das
APIs antigas; a nova rejeição de trabalho só se aplica após o novo pedido lifecycle.

## 5. CLI, composição e observabilidade

`--help` sozinho: ajuda/0 sem configuração ou serviços. Execução:
`--role BOOTSTRAP|SERVER|RELAY|CLIENT --config <ficheiro>`; ordem livre, cada opção
uma vez. [PRESSUPOSTO] só nomes de papel maiúsculos e opções separadas por espaço;
sem aliases, equals, argumentos posicionais ou tolerância silenciosa. Help
com outras opções é erro/2, tal como opção desconhecida/repetida/incompleta.
ConfigurationLoader valida tudo e coerência do papel antes de factories/sockets.
Não inventar DNS no loader; falha operacional de bind/recurso é 3, não erro de perfil.

OttApplication: main delega para execução testável com entradas/saídas/factories
injetáveis internas; apenas main traduz resultado em System.exit. Hooks, EOF e
quit convergem numa decisão idempotente; razão fatal não é apagada por EOF.
OverlayNode: configuração/UUID/SerialOwner/NeighborManager; um owner por nó,
construtor borrowedOwner. BOOTSTRAP cria apenas BootstrapServer, sem owner de nó,
manager, membership ou NODE_LEAVE. BootstrapServer fecha uma única vez, usando o
seu budget interno já existente; não o envolver em esperas que renovem o prazo.
A composição de um papel inicia só esse serviço, após validação.

Estados NEW/VALIDATING/STARTING/RUNNING/STOPPING/TERMINATED conforme Model.
READY indica lista inicial, não todos os peers online. neighbors deve funcionar
durante descoberta ou com bootstrap desligado depois dela. quit/EOF encerram;
catalog/play informam indisponibilidade; comando inválido é erro local sem frame.
Cada saída apresenta nodeId/papel/PID; UUID real do manager nos papéis overlay.
No bootstrap não inventar senderInstance: usar identidade de processo e indicar
explicitamente que a instância wire interna não é exposta pela API existente.
NeighborUp/Down/ERROR/Diagnostic tornam-se observáveis com connectionId/session e
razão. Falha de peer não mata todo o nó. Falha fatal local determina resultado 3.
A API bootstrap só expõe falhas de construção/close; não afirmar health contínuo
nem aceder a campos privados para o simular. Falha detetada em close é reportada.

I/O não corre no owner/callback. [PRESSUPOSTO] limites locais derivados, sem nova
configuração: linha CLI até 4096 caracteres antes de acumular mais, fila de
comandos até overlay.eventsCapacity, fila de saída até control.queueCapacity;
registo até 4096 caracteres, truncado com indicação. São limites de buffers CLI,
sem alterar hard caps/rede. Excesso de linha é erro explícito e consumo limitado;
fila saturada ou falha de consumidor produz diagnóstico/falha local e paragem,
sem wait por espaço no owner. Durante paragem não admitir novos comandos, mas
continuar a consumir conclusões lifecycle numa via limitada.

Leitor/escritor CLI têm ownership explícito e fecho de streams/descritores próprios;
as interfaces injetadas distinguem streams emprestados de possuídos. stdin e
stdout reais bloqueados exigem ensaio de fecho e join: daemon, interrupt/future
cancelado ou System.exit sozinho não provam cleanup. Se não for possível
terminar fisicamente no budget com os cinco ficheiros/APIs, parar e registar
evidência antes de enfraquecer o oráculo ou acrescentar dependência. Hooks não
escrevem em pipes possivelmente bloqueados; retirar hook no fim normal, tolerar
shutdown já em curso. Observações de terminação não podem esperar no próprio owner.

0: ajuda/fim normal com cleanup completo. 2: CLI/configuração inválida antes de
rede. 3: bind, recurso local ou terminação falhada. Falha de Leave isolada é
best effort e fica no relatório; não torna por si só cleanup bem-sucedido em erro.
SIGINT/SIGTERM: registar status real do SO, sem prometer exit 0; exigir fim limitado.
Rollback de arranque cobre todos os recursos parcialmente criados.

## 6. Testes primeiro e implementação incremental futura

1 - **Interfaces e vermelho**: apenas após aprovação de Plan/autorizar Implement,
    acrescentar assinaturas/tipos e testes que falham por comportamentos ausentes,
    não por dependências inválidas. Guardar nomes/razões e parar no checkpoint
    interfaces/testes primeiro previsto pelo utilizador, antes de implementar.
2 - **Lifecycle**: primeiro L01–L12, relógio/transporte/owner falsos, seguido de TCP
    real. Nenhum teste de IMP-004 é apagado/editado/enfraquecido.
3 - **Composição**: parser/configuração, bootstrap, owner/manager, CLI e shutdown,
    fazendo passar M01–M18 por partes. Preservar testes fundacionais TASK-001–005.
4 - **Checkpoint Implement**: apresentar diff/numstat contra 90cded6 e distinguir
    alterações documentais já presentes. Não avançar Test sem aprovação.
5 - **Test**: `cd Projeto` e `mvn -B -ntp clean verify`, log novo e saída real;
    XML Surefire, lista de testes, JAR/hash, perfis, comandos, ambiente e logs em
    experiments/construction/020-overlay/. 903 é histórico, nunca total presumido.

Oráculos adicionais lifecycle, todos planeados e ainda não executados:

| ID | Oráculo |
|---|---|
| L01 | reason/duração/null inválidos sem trabalho; zero fecha imediatamente |
| L02 | só sessões diretas ACTIVE atuais recebem Leave; bootstrap/HELLO pendente não |
| L03 | send(Leave) continua INVALID_MESSAGE em RUNNING; send aplicação inalterado |
| L04 | flush pendente impede concluir notificação; sucesso só após write+flush local |
| L05 | exceção/overflow/peer fechado concluem falha e fecho sem nova ligação |
| L06 | timer vence flush bloqueado, fecha e conclui; callbacks tardios ignorados |
| L07 | repetir pedido/hook/quit não duplica aviso nem renova deadline; requestClose preempta |
| L08 | novos comandos e comandos ainda na fila, retries/dials/HELLO tardios não criam tráfego |
| L09 | sessão trocada/mesmo nodeId com connectionId ou instanceId diferente não é notificada por engano |
| L10 | owner saturado/parado, consumidor que falha e timer rejeitado não retêm recursos/future |
| L11 | grau máximo mantém memória/tickets limitados; um prazo comum, sem N timeouts; cópias defensivas |
| L12 | TCP real recebe bytes v1 Leave reason 0/1; borrowed owner só termina pela composição; await físico |

M01–M18 permanecem conforme [Model](ddd-01-domain-model.md), com M11 desbloqueado
para L01–L12. V-008 exige JVMs distintas, não só serviços na mesma JVM:
BOOTSTRAP e três nós overlay em perfis temporários coerentes, stdin/stdout reais;
listar vizinhos, retirar bootstrap após descoberta, parar/reiniciar um peer sem
reiniciar sobreviventes, EOF/quit, sinal real, bind ocupado, configuração inválida,
stdout lento/fechado e prazo global. Ports temporários reservados e libertados
com tratamento de corrida, sem depender de sleeps como oráculo. Usar predicados,
deadlines e limpeza finally de todos os filhos, capturando exits e recursos.
Relógio falso prova budgets/lógicas; TCP/subprocessos provam integração efetiva.
Manter distinção entre falha remota e erro fatal do processo.

## 7. Aceitação, ADR e checkpoints

Aplicam-se ADR-013/014/016/017/019/020/022/024 e transversais
001/010/012/015/018/021/026. Sem ADR nova: extensão autorizada não substitui decisão
aceite, não muda wire, política de sessão, framing ou caps. Se for demonstrada
inviabilidade de ADR aceite, parar parte afetada e propor ADR-033 ou seguinte
livre, com aprovação, antes de a substituir. Nunca usar ObjectInputStream de rede.
Preservar framing 37..65536/UTF-8 estrito, 16 tipos/código 3 rejeitado e UDP OTT1
64/1200/1136 intacto; não há UDP neste incremento. JUnit só em testes; sem runtime
dependency nova, CI, linter, JaCoCo ou limiar de cobertura (ADR-026 prevalece
sobre sugestões genéricas da skill).

- [ ] Implementação restrita aos cinco ficheiros e APIs/limites acima.
- [ ] L01–L12 e M01–M18/V-008 satisfeitos com resultados reais e regressão executada.
- [ ] Sem sockets/threads pendentes; shutdown dentro do único budget ou falha explícita.
- [ ] V-ADD passa sem alterar validador nem reduzir alcance.
- [ ] Manual humano reproduzível executado antes da aprovação de Test.
- [ ] Revisão humana final de Waheed confirmada e versão revista integrada antes de DONE.

Nenhum gate externo adicional. GUI/CORE, vídeo e metas de outros IMP não são
executados nem declarados cumpridos por este Plan. Test/história implemented=true,
TASK-008 DONE e bolt complete não são presumidos. Unidade permanece in-progress.
Sem Java, build/testes Java, staging, commits ou push nesta autorização.

**Parar no checkpoint Plan para aprovação.** Model aceite não equivale a Plan
aprovado nem autoriza Implement. Após aprovação explícita seguir os checkpoints
incrementais indicados; não iniciar outro IMP.

## Aprovação do Plan e autorização limitada

Registada em 2026-11-10T17:41:41Z: Plan aprovado pelo utilizador, incluindo reserva de metade
do budget, gramática CLI e limites locais (Q-C-10/11/12). Os [PRESSUPOSTO] acima
passam a decisões aprovadas, preservados como histórico da apresentação.
Apenas interfaces/tipos e testes inicialmente vermelhos estão autorizados;
parar antes de implementar comportamentos, sem commits/push.
