# IMP-003 — Implement: walkthrough para revisão

Preparado em 2026-11-04T12:08:01Z. Revisão ADR aprovada explicitamente pelo utilizador,
com registo em 2026-11-04T11:37:45Z. Autorizado inicialmente somente Implement nos cinco ficheiros previstos e
incrementais. Implement aprovado em 2026-11-04T12:18:01Z, autorizando apenas Test;
[relatório posterior para revisão](ddd-03-test-report.md), sem fecho.
Base Denji `4ac5808dacf6e97bfd3636d2788db363e926bc95`; não houve commit, integração ou push deste incremento.

[Design aprovado](ddd-02-technical-design.md), [revisão ADR aprovada](adr-review.md),
[Q-C-04](../../../docs/construction-duvidas.md#q-c-04--coerência-da-instância-e-resposta-única-na-consulta-inicial),
[IMP-003](../../../add.md#imp-003) e [V-006](../../../docs/09-TESTING-STRATEGY.md#v-006--fornecer-localização-inicial-dos-vizinhos-configurados).

## O que está implementado

BootstrapServer é um serviço embutível, com configuração validada antes do bind,
core serial e grafo imutável. REGISTER consulta por nodeId e valida identidade,
instância e endpoint normalizado lexicalmente, sem DNS do pedido ou inferência
por IP remoto. Resposta conserva requestId, devolve apenas adjacentes configurados
com instanceId zero, incluindo offline, e fecha após confirmação de escrita.
Não há membership, lease, renovação, monitorização ou novas arestas.

Connection.sendAndFlush acrescenta confirmação local sobre a mesma fila limitada
que send. PendingWrite associa bytes/future, inFlight identifica o frame em escrita
e o conjunto de admitidos cobre também a janela take/publicação. O escritor publica
as conclusões fora dos locks e termina as pendências antes de workerFinished;
release continua condicionado aos dois workers. Cancelamento não desfaz bytes.

Transport.requestClose inicia shutdown sem espera; close mantém o comportamento
anterior, aguardando só quando chamado externamente. Snapshot sob lifecycle e
fecho fora dele evitam continuations sob locks incompatíveis. Bootstrap coordena
um deadline global para transporte/core/scheduler, sem somar três budgets.

Limites preservados: reservas ≤64, saída ≤256 + um frame em escrita, confirmações
admitidas ≤257, eventos ≤4096 e um timer por contexto admitido (≤64), com remoção
no cancelamento. Tempo/escrita controláveis ficam em tipos aninhados de pacote;
construtores públicos usam I/O e scheduler reais. A saturação serial regista erro
operacional e para o serviço; erro/timeout normal de um peer não mata o listener.

## Testes primeiro e resultados reais

Antes de alterar produção, acrescentaram-se cinco casos contra APIs ausentes:
red real com 23 testes, 5 erros novos; os 18 testes anteriores passaram. As chamadas
iniciais verificavam APIs ainda inexistentes por reflection; após a sua existência,
foram trocadas por chamadas diretas, mantendo os oráculos. O código final não usa
reflection/setAccessible como seam. Não se enfraqueceram testes das fundações.

A segunda ronda introduziu Core/canais/relógio/scheduler/eventos falsos e os casos
antes dos handlers: 10 testes, 7 falhas e 2 erros reais, um caso de validação já
aprovado. O shell de API não respondia nem aplicava deadlines; os handlers foram
implementados só depois desse red. Nenhum shell/TODO ficou na versão apresentada.

Contagens: testes / falhas / erros / skips; instantes UTC extraídos dos metadados
novos de cada execução, sem reutilizar resultados históricos.

| Execução | Início → fim | Saída Maven | Contagens |
|---|---|---:|---|
| implement-api-red | 2026-11-04T11:39:08Z → 2026-11-04T11:39:11Z | 1 | 23 / 0 / 5 / 0 |
| implement-transport-green | 2026-11-04T11:42:42Z → 2026-11-04T11:42:46Z | 0 | 29 / 0 / 0 / 0 |
| implement-core-red | 2026-11-04T11:50:08Z → 2026-11-04T11:50:11Z | 1 | 10 / 7 / 2 / 0 |
| implement-bootstrap-green | 2026-11-04T11:51:36Z → 2026-11-04T11:51:39Z | 0 | 41 / 0 / 0 / 0 |
| implement-oracles | 2026-11-04T11:55:09Z → 2026-11-04T11:55:13Z | 0 | 48 / 0 / 0 / 0 |
| implement-incremental-final | 2026-11-04T11:58:05Z → 2026-11-04T11:58:10Z | 0 | 856 / 0 / 0 / 0 |
| implement-offline-final | 2026-11-04T12:06:51Z → 2026-11-04T12:06:56Z | 0 | 856 / 0 / 0 / 0 |

Comando final incremental (em Projeto/, JDK 21):

```sh
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -ntp -Dtest=ConfigurationLoaderTest,MessageCodecTest,TransportTest,BootstrapServerTest test
```

Resultado: **856 testes**, zero falhas/erros/skips: configuração 740, codec 66,
transporte 31 (18 anteriores +13 novos) e bootstrap 19 novos. Os 824 testes
anteriores continuam representados; são 32 novos casos JUnit neste incremento.
Os hashes dos cinco ficheiros antes/depois da execução final coincidem com a versão
apresentada. [Comando/metadados](../../../experiments/construction/002-overlay/implement-offline-final-command.json),
[log](../../../experiments/construction/002-overlay/implement-offline-final.log) e
[XML/txt novos](../../../experiments/construction/002-overlay/implement-offline-final-surefire/TEST-pt.uminho.esr.ott.bootstrap.BootstrapServerTest.xml).
O auxiliar run-incremental.py identifica versões/seletores, remove apenas relatórios
selecionados antigos e arquiva resultados atuais, saída e hashes.

As execuções usam permissões alargadas autorizadas para TCP loopback real.
A etapa Test não começou: não foi executado clean verify nem o manual deste IMP.
A execução incremental de quatro classes não é apresentada como validação final.

## Mapa M01–M18

BS = BootstrapServerTest; TT = TransportTest. Todos os métodos da tabela existem
na versão apresentada e constam dos XML atuais; os blocos com vários cenários
são testes JUnit com loops, sem inflacionar a contagem de casos executados.

| Model | Métodos efetivos / prova | Resultado incremental e limite |
|---|---|---|
| M01 | BS coreInvalidDirectConfigurationsAreRejectedBeforeTransportFactory; additionalDirectTopologyViolationsFailBeforeNetworkOrTimers | Papel/grafo/endpoints inválidos sem chamada à factory, timer ou evento |
| M02 | BS coreConfiguredNeighborsAndCorrelationAreIndependentOfHistoryAndOfflineState | Quatro listas/endpoints/papéis e X, grafo intacto |
| M03 | BS queryReturnsConfiguredOfflineNeighborsThenEof | TCP real com portas reservadas/fechadas e recusa TCP comprovada; lista sem filtrar disponibilidade |
| M04 | BS coreDifferentInstancesOfSameIdCoexistAndOnlyOneResponseIsChosen; simultaneousRealTcpQueriesFromDifferentInstancesReturnOnlyConfiguredNeighbors | 16 canais controlados e 16 consultas TCP concorrentes; sem exclusividade por ID |
| M05 | BS coreIdentityInstanceAndUnknownIdAreRejectedWithoutPoisoningLaterQueries | Coerência de ID/instância, ID desconhecido e consulta seguinte válida |
| M06 | BS coreEndpointHostPortsAndRoleAreValidatedLexicallyWithoutDns; lexicalNormalizationMatchesExistingLoaderAndRejectsInvalidHosts | Host lexical/portas/papel; confronto com loader offline e rejeição de aliases físicos |
| M07 | BS coreAllFifteenOtherActiveTypesAreRejectedWithoutSideEffects; malformedFramesAndEarlyCloseDoNotKillBootstrapListener | 15 tipos ativos de fixtures, erro correlacionado, código 3 rejeitado no codec |
| M08 | BS coreConfiguredNeighborsAndCorrelationAreIndependentOfHistoryAndOfflineState; queryReturnsConfiguredOfflineNeighborsThenEof | requestId/emissor/instância zero, sem lease; leitura pelo codec real |
| M09 | BS completeErrorThenEofAndTwoValidRequestsChooseOnlyOneResponse; TT confirmedResponseCanBeReadCompletelyBeforeLocalClose | REGISTER_OK/ERROR completos antes de EOF; uma resposta por ligação |
| M10 | BS coreConfiguredNeighborsAndCorrelationAreIndependentOfHistoryAndOfflineState | Avanço do relógio um dia após fecho sem timers ou alterações; sem renovação |
| M11 | TT mixedPlainAndConfirmedFramesUseTheSameWriterOrder; concurrentPlainAndConfirmedSendersPreserveFramesAndPerLaneOrder | Fila/escritor comuns, quatro lanes concorrentes, 18 testes antigos preservados |
| M12 | TT confirmationRemainsPendingUntilFlushAndContinuationHoldsNoLocks; controlledWriteAndFlushErrorsTerminateAllAdmittedFutures; writerFactoryFailureIsLocalAndCannotLeaveWorkersOrReservations; invalidConfirmedMessagePreservesHealthyConnectionAndClosedSendFails | Driver/barreiras provam flush, falhas e formato inválido sem matar ligação saudável |
| M13 | BS coreTotalDeadlineClosesPendingWriteAndLateEventsCannotAffectAnotherInstance; coreQueueDelayAndNoRequestDoNotExtendDeadlineAndWrapIsSupported; silentAndPartialRealTcpPeersExpireAndShutdownReleasesListenerPort | Prazo total controlado, fila/bytes adicionais não renovam, wrap e TCP silencioso/parcial |
| M14 | TT overflowFailsQueuedAndInFlightConfirmationsButPreservesOtherPeer; BS coreControlOverridesLimitContextsTimersAndTotalQueryDeadline | Fila 1 + frame em escrita; todos os futures terminam, outro peer progride; contextos/timers limitados |
| M15 | TT closeBetweenTakeAndInFlightPublicationDoesNotLoseConfirmation; cancellationBeforeAndDuringWritingRemainsTerminalAfterClose; BS coreTotalDeadlineClosesPendingWriteAndLateEventsCannotAffectAnotherInstance | Janela take/close forçada, cancel terminal e eventos antigos de outra instância |
| M16 | TT requestCloseDoesNotAwaitBlockedWriterAndReservationRemainsUntilBothWorkersEnd; blockedFutureContinuationProducesExplicitShutdownTimeoutThenReleasesAllPendingWrites; BS coreWriteFailureCancellationAndShutdownRemoveEveryContextAndTicket | Reservas só após ambos workers, continuation sem locks, timeout explícito de callback que viola contrato, contextos/tickets limpos |
| M17 | BS malformedFramesAndEarlyCloseDoNotKillBootstrapListener; coreSaturationFailsExplicitlyAndCleanupDoesNotNeedAnotherQueueSlot | Erros locais com listener operacional; saturação serial distinta, erro/paragem e cleanup explícitos |
| M18 | BS coreControlOverridesLimitContextsTimersAndTotalQueryDeadline; execução incremental das quatro classes | Overrides chegam a Options/deadline; 856 testes aprovados; clean verify/validação final e manual pendentes |

M01–M17 têm prova incremental. M18 fica **parcial**: consumo dos overrides,
regressão incremental e preservação de fixtures comprovados; clean verify com
log/XML novos e manual continuam para Test. TCP real verifica resposta/EOF,
concorrência, entradas inválidas e cleanup; relógio/driver falsos controlam o
instante de flush, a janela take/close e deadline/ordens que loopback não prova.
Shutdown com escrita pendente é coberto no transporte real com driver controlado
e no Core com Channel falso; não alegar bloqueio do buffer do kernel no bootstrap.

## Diff e todas as remoções

Apenas cinco ficheiros de fonte/teste: **+1226/−35**, contra a base 4ac5808.
[Diff completo de aplicação](../../../experiments/construction/002-overlay/implement-code.diff),
[numstat](../../../experiments/construction/002-overlay/implement-code.diff.numstat) e
[inventário linha a linha das remoções](../../../experiments/construction/002-overlay/implement-removals.json).

| Ficheiro | + / − | Justificação exaustiva das linhas originais substituídas |
|---|---:|---|
| BootstrapServer.java | 368 / 11 | Comentário-placeholder de 11 linhas substituído por package/classe/Javadoc e implementação da mesma restrição de bootstrap; nenhum comportamento anterior existia |
| Connection.java | 124 / 10 | L24: entrada privada passa a PendingWrite. L56–58: admissão/overflow conservados, fecho de I/O fora do monitor. L85: factory de escrita real/controlada. L87–88: frame tipado/inFlight antes de escrever. L93: finally publica erros antes de workerFinished. L102–103: separar decisão atómica de close dos efeitos fora do lock |
| Transport.java | 27 / 14 | L121: passar factory mantendo construtores anteriores. L171–172: extrair requestClose, manter close público abaixo. L176–180: snapshot sob lifecycle e fecho fora. L183–188: interrupções em requestClose e identificação de leitores/escritores em close, sem joins circulares |
| BootstrapServerTest.java | 452 / 0 | Placeholder vazio preenchido com testes/fixtures e seams aninhados |
| TransportTest.java | 255 / 0 | Só acréscimos antes da chave final; conteúdo dos 18 testes anteriores preservado byte a byte |

Os outros **150 ficheiros** de Projeto/ fora de target mantêm os hashes e não
há ficheiros novos fora desses cinco caminhos existentes. POM, configuração,
RuntimeConfiguration, codec, enums, 45 fixtures e CORE preservados. As duas listas
Maven em target são geradas e ficam em diff separado, sem reposição artificial:
[listas geradas](../../../experiments/construction/002-overlay/implement-generated.diff).

## Pendências e checkpoint

Implement e incrementais aprovados explicitamente em 2026-11-04T12:18:01Z; etapa incluída
em stages_completed. Bolt/TASK-006/história/unidade continuam em progresso, agora
em Test. O texto acima conserva a prova incremental; a validação posterior está
no relatório Test. Sem implemented=true ou DONE.

Antes de fechar: aprovação deste checkpoint; etapa Test autorizada com clean
verify/JDK21 e log/XML/contagens novos, consolidação V-006 e roteiro/manual
adequado ao serviço embutível; revisão final atribuída a **Waheed** e integração.
Não presumir aceitação do revisor pela aprovação dos checkpoints do utilizador.
Nenhum gate externo adicional, commit/push ou outro IMP nesta continuação.
CLI/HELLO/overlay completa, GUI, vídeo e CORE continuam fora desta prova.

Nenhuma decisão aceite inviável ou necessidade de alterar ficheiro de aplicação
fora do IMP foi identificada nesta implementação. Conservar a regra ADD 11 se
surgir evidência nova. Continuação Test autorizada; parar agora para revisão antes do fecho.
