# IMP-003 — Etapa ADR: revisão do Design aprovado

Design aprovado explicitamente pelo utilizador, com registo em
2026-11-04T11:25:40Z, incluindo Transport.requestClose() como início de shutdown
sem espera e preservação do comportamento público anterior de close().
Revisão ADR aprovada explicitamente em 2026-11-04T11:37:45Z; a continuação autorizada
é apenas Implement, com [walkthrough para revisão](implementation-walkthrough.md).
Esta análise documental não é a prova de implementação/execução posterior.

Fontes: [ADD secção 5](../../../add.md#5-registo-de-decisões),
[interfaces/invariantes](../../../add.md#6-modelo-de-dados-e-contratos),
[IMP-003](../../../add.md#imp-003), [regras ADD 11](../../../add.md#11-regras-para-os-agentes-de-implementação),
[Model aprovado](ddd-01-domain-model.md), [Design aprovado](ddd-02-technical-design.md),
[Q-C-04](../../../docs/construction-duvidas.md#q-c-04--coerência-da-instância-e-resposta-única-na-consulta-inicial),
[contrato v1](../../../docs/07-API-AND-INTERFACES.md),
[TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md) e código atual de
Connection/Transport/configuração/codec. Base: Denji
4ac5808dacf6e97bfd3636d2788db363e926bc95; IMP-001/002 complete e integrados.

## 1. Resultado da revisão

As decisões aceites são reutilizadas. **ADR-003 permanece Substituída pela
ADR-013** e não autoriza registo vivo ou leases. Não foi identificada decisão
aceite inviável ou divergência material entre as obrigações do ADD, o Design
aprovado e a base inspecionada. Não é necessária ADR nova/substituição nesta
análise; não criar ADR-033 para duplicar estas decisões.

A conformidade aqui é do desenho. V-006/M01–M18, regressão e manual continuam
por executar; resultados antigos não são convertidos em prova deste incremento.
As lacunas de código atuais (bootstrap só documental, sendAndFlush ausente)
são precisamente a entrega planeada no IMP-003, não motivo para reabrir TASK-005
ou dispensar testes.

## 2. Decisões principais e conformidade

| Decisão | Conformidade do Design aprovado | Prova futura e limite |
|---|---|---|
| [ADR-003](../../../add.md#adr-003) — substituída | Consultada só como histórico; ADR-013 governa o comportamento | M02–M10 não admitem renovação/estado vivo nem uma segunda arquitetura de bootstrap |
| [ADR-013](../../../add.md#adr-013) — só localização inicial | REGISTER consulta o grafo imutável por nodeId. Resposta com adjacentes configurados, incluindo offline, instanceId zero e sem lease. Q-C-04 valida a instância do pedido e limita a decisão de resposta por conexão; não cria exclusividade por ID | M02–M10/M15: repetição, concorrência, duas instâncias do mesmo ID, resposta completa/EOF e ausência de alterações ao grafo. HELLO/reconexão/saída ficam nos IMP próprios |
| [ADR-014](../../../add.md#adr-014) — transporte assíncrono limitado | send mantém ACCEPTED como admissão. send/sendAndFlush partilham ArrayBlockingQueue, escritor único e ordem de admissão. Não há I/O de saída no chamador ou callbacks à espera de escrita. Overflow TCP afeta só a conexão; reservas persistem até ambos os workers terminarem | M11–M17 e os 18 testes TT existentes: ordem, cancelamento, overflow localizado, callbacks e terminação. requestClose não muda close nem antecipa release |
| [ADR-015](../../../add.md#adr-015) — confirmação de escrita | Future de sendAndFlush só tem sucesso após write e flush completos no escritor. O prazo total da consulta fecha socket e termina a pendência. Conjunto de admitidos inclui o intervalo entre take e inFlight; queuedMessages=0 não é prova | M09/M12–M16: driver bloqueia write/flush/dequeued, simula IOException e força corrida com fecho. Cancelamento terminal não promete retirar bytes. Sucesso local não é ACK remoto |
| [ADR-017](../../../add.md#adr-017) — proprietário serial do estado | Core possui mapa/contextos/tickets; bridge, timer e whenComplete publicam eventos. connectionId e contexto esperado protegem callbacks tardios. Grafo é imutável; flag atómica de paragem é lifecycle, sem mutação concorrente do mapa | M04/M10/M13–M17: relógio/eventos manuais, consultas distintas do mesmo ID, eventos antigos e saturação. Fila serial ≤4096; cleanup permanece no owner |
| [ADR-020](../../../add.md#adr-020) — erros e shutdown limitado | Erros/correlação existentes, sem detalhes sensíveis ou ERROR para envelope inseguro. Configurar antes de rede; timeout/erro normal de peer isolado. requestClose inicia fecho sem join; serviço coordena um único deadline e aguarda só o restante | M01/M05–M09/M12–M17: erro seguro, EOF/truncatura, peer lento, confirmações terminais, porta/threads/timers libertos. Callback que viola contrato rápido deve produzir timeout explícito, não sucesso inventado |

## 3. Decisões transversais preservadas

| Decisão | Aplicação neste incremento |
|---|---|
| [ADR-010](../../../add.md#adr-010) | Loader/records existentes permanecem. Fronteira do novo serviço verifica os dados consumidos antes do bind, incluindo configuração construída diretamente; erros agregados da carga não são substituídos. Normalização lexical de host acompanha o loader/contrato, sem DNS ou inferência por IP remoto (M01/M05/M06) |
| [ADR-012](../../../add.md#adr-012) | Codec separado de autorização. Preservar bytes TCP 37..65536, UTF-8 estrito, 16 tipos e rejeição do código 3; UDP OTT1 64/1200/1136 intocado. Não adicionar tipo, payload, ACK ou ObjectInputStream. Fixtures independentes mantidas (M07–M09/M17/M18) |
| [ADR-016](../../../add.md#adr-016) | Consumir apenas Control aplicável ao bootstrap, passando limites/timeouts a Options e à consulta; não inventar propriedade ou permitir override overlay no BOOTSTRAP. Options anterior de quatro argumentos preservado (M13/M14/M16/M18) |
| [ADR-018](../../../add.md#adr-018) | nodeId estável, instanceId de execução e connectionId distintos. Nenhuma persistência de consultas/IDs vivos. A instância viva de um vizinho continua a ser conhecida por HELLO, não pelo bootstrap (M04/M05/M08/M10/M15) |
| [ADR-021](../../../add.md#adr-021) | Validação contextual de allowlist/endpoints não é autenticação criptográfica. Nenhuma resolução DNS do pedido nem confiança no endereço remoto para reescrever a topologia; preservar validação/limites antes de alocar (M01/M05–M08/M17) |
| [ADR-024](../../../add.md#adr-024) | Um IMP por bolt, apenas cinco ficheiros Java futuros e documentação pertinente; dependências integradas, índices/estados coerentes. Se inviável, aplicar secção 11 antes de intervir. Nenhuma fundação, fase ou requisito global reaberto/concluído por inferência |
| [ADR-026](../../../add.md#adr-026) | Maven/JUnit/validador existentes. Sem dependência runtime, CI/linter/JaCoCo/limiar novos. Casos críticos/relógio/escrita controláveis e TCP real são provas distintas, com logs novos quando autorizados |

## 4. Necessidade e limites das alterações internas ao transporte

| Alteração futura | Limitação observada na base | Necessidade e compatibilidade |
|---|---|---|
| PendingWrite na fila comum | ArrayBlockingQueue<byte[]> não associa uma confirmação ao frame | Acrescentar associação privada, preservando a fila e capacidade. send sem future continua igual; sem fila prioritária/separada. Nenhum byte v1 muda |
| inFlight e conjunto limitado de confirmações | writeLoop retira o frame com take; close limpa a fila, que já não contém esse frame | Cobrir fila + retirado/em escrita, incluindo a janela take/publicação. No máximo capacidade+1, teto 257, sem histórico. Remoção/publicação pelo escritor antes de workerFinished |
| Região crítica curta em Connection.close | Método inteiro synchronized faz clear/close/interrupt no mesmo monitor | Proteger decisão/reason/admissão; fechar/interromper fora do monitor. complete* fora do monitor e de lifecycle, evitando executar continuations sob esses locks. Primeira razão de fecho mantém-se |
| Snapshot e fecho fora de Transport.lifecycle | Transport.close chama Connection.close enquanto detém lifecycle | Copiar recursos e impedir novas admissões sob lifecycle; fechar recursos depois de o libertar. Não adquirir lifecycle dentro do monitor da conexão nem executar conclusões/join sob locks |
| Transport.requestClose | close externo espera o budget do Transport; bootstrap precisa também coordenar core/timer | Extensão aditiva aprovada explicitamente: só inicia shutdown. close preserva espera externa e evita joins quando chamado por worker. awaitTermination continua a verificar terminação; requestClose não a prova |
| Driver de escrita/injeções de pacote | Socket de loopback pequeno não controla o instante de write/flush nem a janela take/fecho | Overloads/tipos aninhados nos ficheiros autorizados; defaults públicos usam OutputStream real. Sem tornar classes extensíveis, dependência, reflexão ou alteração de codec/POM |

Estas mudanças concretizam ADR-014/015/020. Não são uma política de transporte
nova nem uma exceção para reescrever TASK-005: preservar construtores, Options,
send, métricas existentes, reasons, contagem de reservas e todos os seus testes.
O mecanismo de sucesso continua no escritor exclusivo, não num executor de
conclusões/commonPool nem numa thread por future.

**Reservas:** nem requestClose, nem limpeza da fila, conclusão do future ou
callback connectionClosed libertam sozinhos a reserva. O release continua após
remainingWorkers chegar a zero; o finally do escritor termina as confirmações
antes de sinalizar o fim desse worker. A fase de erro do dial mantém também o
seu cleanup/future. Snapshot de shutdown e attach/reserve precisam da verificação
closed existente para cobrir sockets aceites/dials concorrentes.

**Corridas:** o desenho fixa uma decisão terminal sob o monitor; publicação é
posterior e fora dos locks. Fecho que ganha antes da decisão pós-flush pode
produzir erro mesmo com bytes enviados. Decisão de sucesso ganha não é revertida
por close posterior; cancel bem-sucedido do future permanece cancelamento mesmo
se o escritor tentar complete depois. Não alegar rollback/entrega remota.

## 5. Prazos, saturação e recursos

Três prazos distintos de Control: dial (default 2000 ms), consulta total após
connectionOpen (3000 ms) e shutdown global (3000 ms). Consulta conserva t0 antes
da fila serial e nunca renova prazo por pedido adicional/bytes parciais. O timer
fecha o canal por decisão serial; isso interrompe I/O e permite ao escritor
terminar pendências. Future.orTimeout sem fechar socket seria insuficiente.
O timeout de consulta não se torna watchdog de vizinhos ou timer de membership.

sendAndFlush mantém a assinatura do ADD, sem novo parâmetro/propriedade de timeout.
O prazo finito desta operação no bootstrap pertence à consulta; outros consumidores
têm de coordenar o respetivo prazo/fecho. Isto não dispensa término das confirmações
em qualquer fecho/shutdown. O serviço usa requestClose e esperas com o tempo
restante de um único deadline real de shutdown, sem somar três budgets ou fazer
join de si próprio. Tempo falso controla a semântica do Core; não congela waits
de workers reais.

Limites a conservar:

- Reservas TCP ≤64, fila por conexão ≤256 e um frame retirado/em escrita;
  no máximo 257 confirmações admitidas, sem lista histórica ilimitada.
- Fila serial ≤4096, callbacks sem espera por espaço. Saturação da saída TCP
  fecha só o peer; saturação da fila serial é o erro operacional do nó previsto
  na ADR-017, com paragem observável/cleanup. Não confundir esse caso com timeout
  normal de um peer, que deixa o listener e outras consultas operacionais.
- Um ticket por contexto admitido, ≤64; cancelamento/removeOnCancelPolicy
  evita acumular timers cancelados na implementação JDK. Não expor scheduler
  para outras tarefas, criar tarefas periódicas ou aceitar chaves overlay.
- Grafo e snapshots imutáveis; contextos/tickets removidos pelo owner em todos
  os caminhos de fecho. Limites de wire/configuração não viram tuning por nó.

## 6. Provas M01–M18 e condições da implementação futura

M01–M18 estão definidos no Design e continuam obrigatórios, sem declarar nenhum
executado por esta revisão. BootstrapServerTest terá Core/Channel/clock/deadlines/
eventos controláveis; TransportTest terá driver write/flush/dequeued com barreiras
e falhas controladas. Todos esses tipos ficam aninhados nos cinco ficheiros
autorizados. TCP real complementa as provas: resposta completa seguida de EOF,
isolamento de erro e libertação de socket/porta/workers. Fila vazia, sleeps ou
loopback sem pressão não substituem as provas do instante de escrita ou timeout.

Prioridades verificáveis para Implement, só depois de autorização:

1. Introduzir casos novos e falhas reais dos comportamentos em falta; não criar
   falhas artificiais nos 18 testes de transporte ou nos restantes testes existentes.
2. Demonstrar write regressado/flush ainda bloqueado com future pendente e
   fecho entre take e inFlight, sem future perdido (M09/M12/M15).
3. Demonstrar overflow, cancelamento e shutdown com confirmações na fila e em
   escrita, continuations de close sem lock/join circular e reservas só libertas
   após ambos os workers terminarem (M11–M16).
4. Demonstrar prazo total, eventos atrasados de outra consulta do mesmo ID,
   saturação controlada e remoção de tickets/contextos (M04/M10/M13–M17).
5. Executar regressão atual/JDK21 e conferir 45 fixtures, com novos logs/XML e
   contagens reais; manual deste incremento e comandos repetíveis (M18).

Nenhuma linha de teste pode ser apagada/enfraquecida para obter verde. As únicas
alterações internas à base permitidas são as exigidas pelo IMP-003 e descritas
no Design; necessidade de outro ficheiro ou decisão inviável exige parar e
apresentar a lacuna antes de intervir.

## 7. Aplicação da secção 11 do ADD e checkpoint

Não se encontrou condição para propor ADR de substituição nesta inspeção.
Se a implementação futura demonstrar que uma decisão aceite não é cumprível,
parar a parte afetada, preservar comando/log/código e impacto, registar a próxima
Q-C em docs/construction-duvidas.md, propor ADR-033 em diante e aguardar aprovação.
Não reduzir hard caps/provas, deslocar conclusões para dentro de locks ou
desmarcar obrigações para contornar essa condição.

Checkpoint ADR aprovado em 2026-11-04T11:37:45Z, incluído em stages_completed.
A execução incremental posterior está no walkthrough: 856 testes aprovados;
V-006 parcial, Test/clean verify/manual/revisão final/integração pendentes.
O bolt continua in-progress/implement, completed=null. A análise acima é o retrato
ADR; não converte os resultados históricos de IMP-002 em testes deste incremento.

Sem gate externo adicional no IMP-003; a revisão final atribuída a **Waheed**,
os critérios/testes aplicáveis e a integração continuam exigidos antes de DONE.
Não atribuir aceitação/contactar o revisor por inferência. Nesta etapa não há
alteração de Java ou execução de testes na etapa ADR. Na continuação, Implement
foi aprovado em 2026-11-04T12:18:01Z; [Test preparado para revisão](ddd-03-test-report.md).
Sem commits/push ou fecho.
