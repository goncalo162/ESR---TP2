# IMP-003 — Test aprovado e concluído

Preparado em 2026-11-04T12:27:23Z. Implement aprovado explicitamente pelo utilizador,
com registo em 2026-11-04T12:18:01Z; autorizada apenas Test,
limite vigente nessa execução, antes da aprovação posterior de Test. Bolt 002-overlay/TASK-006, unidade 002-overlay;
base Denji 4ac5808dacf6e97bfd3636d2788db363e926bc95 com cinco ficheiros Java revistos.

## Resultado atual

`mvn -B -ntp clean verify` com JDK **21.0.12.1**, TCP loopback real e provider
JUnitPlatform: **856 testes**, zero falhas/erros/skips, saída Maven **0**.
Execução 2026-11-04T12:18:05Z → 2026-11-04T12:18:10Z. Os 856 incrementais de Implement
não foram reutilizados como este resultado: clean eliminou target e foram
arquivados quatro XML/txt novos da execução completa.

| Suíte | Testes | Falhas | Erros | Skips |
|---|---:|---:|---:|---:|
| BootstrapServerTest | 19 | 0 | 0 | 0 |
| ConfigurationLoaderTest | 740 | 0 | 0 | 0 |
| MessageCodecTest | 66 | 0 | 0 | 0 |
| TransportTest | 31 | 0 | 0 | 0 |

[Comando, ambiente, contagens e hashes](../../../experiments/construction/002-overlay/test-clean-verify-command.json),
[log completo](../../../experiments/construction/002-overlay/test-clean-verify.log),
[XML bootstrap](../../../experiments/construction/002-overlay/test-clean-verify-surefire/TEST-pt.uminho.esr.ott.bootstrap.BootstrapServerTest.xml),
[transporte](../../../experiments/construction/002-overlay/test-clean-verify-surefire/TEST-pt.uminho.esr.ott.transport.TransportTest.xml),
[configuração](../../../experiments/construction/002-overlay/test-clean-verify-surefire/TEST-pt.uminho.esr.ott.config.ConfigurationLoaderTest.xml)
e [codec](../../../experiments/construction/002-overlay/test-clean-verify-surefire/TEST-pt.uminho.esr.ott.protocol.MessageCodecTest.xml).
A mensagem de paragem do bootstrap no log vem do teste deliberado de saturação
serial, que espera esse erro e verifica cleanup; não é uma falha Maven omitida.

JAR: Projeto/target/ott-streaming-1.0-SNAPSHOT.jar, SHA-256
`2c3c063ac58692d1fd813d7e7564b00af7068906d156cc99126827847d2a9ccc`.
Os cinco hashes antes/depois do build coincidem com a versão aprovada em
Implement. Nenhuma alteração Java/teste ou dependência foi feita nesta etapa;
os 155 ficheiros de Projeto/ fora de target estão congelados desde a aprovação.
Comparada à base do bolt, a aplicação continua em cinco ficheiros **+1226/−35**,
com outros 150 hashes e os 18 testes anteriores de transporte preservados.

## Manual executado e comandos pessoais

Manual executado pelo agente **Codex** sobre esse JAR e APIs públicas, sem hooks
ou reflection: 2026-11-04T12:22:51Z → 2026-11-04T12:22:53Z, saída Java **0**,
**34 CHECK_PASS** e **MANUAL_PASS**. [Log separado](../../../experiments/construction/002-overlay/test-manual.log)
e [metadados/hashes](../../../experiments/construction/002-overlay/test-manual-command.json).
SHA-256 do roteiro:
`e9c0d1f96a5eb87ff87f8308ebc36457e6869e1e5adcfce6994bd7f222144ff4`.

[Manual e comandos para repetição pessoal](../../../experiments/construction/002-overlay/manual-bootstrap.md):
consulta válida/erro, uma resposta por conexão, instâncias distintas, resposta
completa antes de EOF, vizinhos com recusa TCP comprovada, prazo total e shutdown
com consulta pendente/reutilização da porta. Cria/remove apenas configurações
temporárias, preservando os perfis do projeto. Esperado na repetição: 34 CHECK_PASS,
MANUAL_PASS e JAVA_EXIT=0, num log novo com operador real.

O agente observou 504 ms/503 ms para os dois fechos de consulta com prazo de
500 ms, e 4 ms para o shutdown, numa única execução local. Não são SLA/medição de
latência entre máquinas; instantes/durações vêm do log, sem valores inventados.
Luis executou pessoalmente o mesmo roteiro em **2026-11-04T13:58:36.658445362Z →
2026-11-04T13:58:38.039499140Z**: **34 CHECK_PASS, MANUAL_PASS e JAVA_EXIT=0**.
[Log pessoal](../../../experiments/construction/002-overlay/test-manual-luis.log) copiado
byte a byte de /tmp/ott-imp003-manual.sivs66.log; original preservado e execução
separada da de Codex. [Metadados](../../../experiments/construction/002-overlay/test-manual-luis.json).
SHA-256 do log: `6d7cc4eb76bc0487128c685f8143be1497255a9b7048aa655424a3a9781bcaa9`.
O log identifica Luis e o JAR com SHA-256 `2c3c063ac58692d1fd813d7e7564b00af7068906d156cc99126827847d2a9ccc`.
Observações pessoais: silêncio 501 ms, parcial 503 ms e shutdown 5 ms;
continuam observações locais, separadas das durações do agente.
Test aprovado explicitamente pelo utilizador; aprovação registada em 2026-11-04T14:05:14Z.

## Consolidação V-006 / M01–M18

[Mapa estruturado](../../../experiments/construction/002-overlay/test-case-map.json).
Nomes abaixo estão nos XML novos (com nomes normalizados só para relacionar
parâmetros de método); não se contam loops internos como testes JUnit adicionais.

| Model | Métodos efetivos na execução completa | Resultado técnico |
|---|---|---|
| M01 | additionalDirectTopologyViolationsFailBeforeNetworkOrTimers; coreInvalidDirectConfigurationsAreRejectedBeforeTransportFactory | Executado; sem falhas |
| M02 | coreConfiguredNeighborsAndCorrelationAreIndependentOfHistoryAndOfflineState | Executado; sem falhas |
| M03 | queryReturnsConfiguredOfflineNeighborsThenEof | Executado; sem falhas |
| M04 | coreDifferentInstancesOfSameIdCoexistAndOnlyOneResponseIsChosen; simultaneousRealTcpQueriesFromDifferentInstancesReturnOnlyConfiguredNeighbors | Executado; sem falhas |
| M05 | coreIdentityInstanceAndUnknownIdAreRejectedWithoutPoisoningLaterQueries | Executado; sem falhas |
| M06 | coreEndpointHostPortsAndRoleAreValidatedLexicallyWithoutDns; lexicalNormalizationMatchesExistingLoaderAndRejectsInvalidHosts | Executado; sem falhas |
| M07 | coreAllFifteenOtherActiveTypesAreRejectedWithoutSideEffects; malformedFramesAndEarlyCloseDoNotKillBootstrapListener | Executado; sem falhas |
| M08 | coreConfiguredNeighborsAndCorrelationAreIndependentOfHistoryAndOfflineState; queryReturnsConfiguredOfflineNeighborsThenEof | Executado; sem falhas |
| M09 | completeErrorThenEofAndTwoValidRequestsChooseOnlyOneResponse; confirmedResponseCanBeReadCompletelyBeforeLocalClose | Executado; sem falhas |
| M10 | coreConfiguredNeighborsAndCorrelationAreIndependentOfHistoryAndOfflineState | Executado; sem falhas |
| M11 | concurrentPlainAndConfirmedSendersPreserveFramesAndPerLaneOrder; mixedPlainAndConfirmedFramesUseTheSameWriterOrder | Executado; sem falhas |
| M12 | confirmationRemainsPendingUntilFlushAndContinuationHoldsNoLocks; controlledWriteAndFlushErrorsTerminateAllAdmittedFutures; invalidConfirmedMessagePreservesHealthyConnectionAndClosedSendFails; writerFactoryFailureIsLocalAndCannotLeaveWorkersOrReservations | Executado; sem falhas |
| M13 | coreQueueDelayAndNoRequestDoNotExtendDeadlineAndWrapIsSupported; coreTotalDeadlineClosesPendingWriteAndLateEventsCannotAffectAnotherInstance; silentAndPartialRealTcpPeersExpireAndShutdownReleasesListenerPort | Executado; sem falhas |
| M14 | coreControlOverridesLimitContextsTimersAndTotalQueryDeadline; overflowFailsQueuedAndInFlightConfirmationsButPreservesOtherPeer | Executado; sem falhas |
| M15 | cancellationBeforeAndDuringWritingRemainsTerminalAfterClose; closeBetweenTakeAndInFlightPublicationDoesNotLoseConfirmation; coreTotalDeadlineClosesPendingWriteAndLateEventsCannotAffectAnotherInstance | Executado; sem falhas |
| M16 | blockedFutureContinuationProducesExplicitShutdownTimeoutThenReleasesAllPendingWrites; coreWriteFailureCancellationAndShutdownRemoveEveryContextAndTicket; requestCloseDoesNotAwaitBlockedWriterAndReservationRemainsUntilBothWorkersEnd | Executado; sem falhas |
| M17 | coreSaturationFailsExplicitlyAndCleanupDoesNotNeedAnotherQueueSlot; malformedFramesAndEarlyCloseDoNotKillBootstrapListener | Executado; sem falhas |
| M18 | coreControlOverridesLimitContextsTimersAndTotalQueryDeadline | Clean verify e manuais Codex/Luis executados; Test aprovado |

M01–M18 têm agora execução técnica: M18 inclui clean verify e manual do agente,
antes pendentes. V-006 está **executado localmente e aprovado em Test**, incluindo
a repetição pessoal por Luis. A revisão final atribuída a Waheed foi confirmada
pelo utilizador e a versão revista integrada em main; fecho registado abaixo. Relógio e
escrita controláveis comprovam deadlines/flush/janela take/fecho/overflow;
TCP real comprova framing/EOF/isolamento/concorrência/terminação. Nenhum sleep
ou fila vazia é aceite como prova de escrita.

## Preservação e diff

Fontes/testes, POM, configuração, codec/enums, fixtures e CORE mantêm os hashes
congelados no início de Test. [V-ADD executado](../../../experiments/construction/002-overlay/test-validation.md):
45 fixtures, 221 hashes do snapshot importado e zero erros. É uma verificação
documental/estrutural distinta de Maven.
Preservar as evidências e aprovações anteriores em vez de as sobrescrever.

Esta etapa acrescenta relatório/roteiro/launcher experimental, logs/XML e estados.
O launcher fica em experiments/construction/002-overlay/, não em Projeto/src ou
no JAR. Listas geradas em Projeto/target/ são apresentadas num diff separado;
não foram restauradas para fabricar preservação, nem são novas funcionalidades.
Os diffs e a conferência final de hashes ficam nos artefactos test-*.diff/json.

## Limitações e checkpoint

- TCP é real, em loopback, dentro das JVM de teste/launcher. Não foram demonstrados
  CLI/HELLO, nós overlay em processos independentes, vídeo, GUI, CORE ou recuperação.
- O manual usa o serviço embutível disponível e perfis temporários; não é prova
  da composição futura de OttApplication. Portas/UUID variam entre repetições.
- Escrita confirmada é write/flush local, sem ACK de receção remota. Cancelamento
  não retira bytes. Callback que bloqueia viola o contrato e tem teste de timeout
  explícito; não se promete terminação normal desse consumidor incorreto.
- O bootstrap só é usado na descoberta inicial de cada processo; processos novos
  ou reiniciados voltam a consultá-lo. O overlay direto após descoberta pertence
  aos incrementos seguintes, não foi demonstrado por este bolt.

Test aprovado e concluído em stages_completed, com registo em 2026-11-04T14:05:14Z.
[Fecho concluído](closure-walkthrough.md) em 2026-11-04T14:18:23Z: revisão final atribuída
a Waheed confirmada pelo utilizador, com registo em 2026-11-04T14:18:23Z, e versão
revista 03345e9e1ab56ec04302676cca6fce32d168223d já integrada em main antes do fecho.
Mecanismo specsmd executado com saída 0: bolt/história complete, implemented=true,
TASK-006 DONE; unidade permanece in-progress, três bolts planned. Nenhum outro
IMP iniciado. Os resultados Java são os aprovados e preservados acima;
este fecho altera documentação/evidência, sem nova execução Java.
