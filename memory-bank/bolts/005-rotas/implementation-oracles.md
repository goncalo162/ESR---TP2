# IMP-007 — mapa de oráculos executados em Implement

Execução corrente: implement-regression-04.log/XML,1176/0falhas/0erros/0ignorados.
52 testes monitoring +11 integração própria. Nenhum substitui manual humano ou Test.

## M01–M22

- **M01**: `d02AgeUsesLocalSubtractionAndAllowsNegativeTimestamp`; `m01m02ThirdEwmaAndAlphaOneOverride`
- **M02**: `d02FirstRttAndEwmaUseIndependentExpectedValues`; `m01m02ThirdEwmaAndAlphaOneOverride`
- **M03**: `d16UnmeasuredLossAndMeasuredSuspectKeepStateAndAgeConsistent`; `m03SubMicrosecondAndFractionalSamplesNeverPublishReliableZero`
- **M04**: `d04m04CadenceHandlesBothIntervalRelationsAndDelayedTicksWithoutBurst`
- **M05**: `d08WrongRequestCannotCompleteCurrentProbe`; `m05WrongTokensDuplicatesAndOldDownCannotModifyCurrentMeasurement`
- **M06**: `d09ThreeAdmittedTimeoutsCloseOnlyExpectedSession`; `m06ConfiguredFailureThresholdOneClosesExactlyOnce`
- **M07**: `m07SuccessResetsConsecutiveLossesAndForeignTrafficDoesNot`
- **M08**: `d09DownInvalidatesImmediatelyWithoutThreeTimeouts`; `realTcpUsesSameOwnerV2ProbesAndEofInvalidatesCurrentLink`
- **M09**: `m09InstanceChangeAndRetiredAdmissionCannotCloseReplacement`
- **M10**: `d03NewConnectionSameInstanceResetsAllMetrics`
- **M11**: `d10EchoPreservesRequestAndProbeIds`; `realTcpUsesSameOwnerV2ProbesAndEofInvalidatesCurrentLink`
- **M12**: `d05EarliestValidReplyWinsEvenIfAdmissionArrivesAfterDeadline`; `d05EarlyPongWaitsForAdmissionAndKeepsCapturedTime`; `d07AdmissionCanOutliveDeadlineWithoutFabricatingLossOrSpinningTimer`; `d07ExactDeadlineCannotBeReclassifiedAsGoodPong`; `d07LateEarlyReplyCannotRescueAnAdmittedExpiredProbe`
- **M13**: `d06AllRefusalsAndExceptionalAdmissionsDiscardEarlyReplies`; `d06RefusedAdmissionDiscardsEarlyPongWithoutRemoteLoss`; `d10EchoClosedOverflowAndExceptionalResultAreNotRemoteTimeouts`; `d10LocalSyncThrowFutureFailureAndInvalidMessageAreFatalNotPeerLoss`
- **M14**: `d14CountersSaturateWithoutWrappingAndKeepExactSampleIdentity`; `d14LongestConfiguredTimeoutStillConvertsWithoutOverflow`; `d14NanoTimeWrapAndNegativeInstantsAreValidButBackwardElapsedIsFatal`; `d14ProbeIdMaximumDoesNotWrapOrRepeat`
- **M15**: `d11CloseHasAnIndependentReservationAndRetiredSendsKeepTheirTickets`; `d11OnlyOnePhysicalCloseIsAdmittedAndFailureIsLocalFatal`; `d11ReservedCloseProgressesWhileSendCapacityIsFull`; `d11SixtyThreeSessionsAndChurnStayWithinPhysicalSendBudget`; `d12CanceledRunningTimerStillUsesPhysicalSlotAndThirdJobIsRejected`
- **M16**: `d15CloseBeforeStartIsIdempotentAndBorrowedOwnerIsNeverClosed`; `d15HeldEchoAndHeldCloseAreCountedUntilTheirRealCompletion`; `d15LogicalCleanupDoesNotClaimPhysicalCompletionWithHeldAdmission`; `d15StopRejectsNewWorkDoesNotStopBorrowedOwnerAndRejectsSelfJoin`; `heldMonitorAdmissionUsesOnlyTheOriginalGlobalCleanupBudget`
- **M17**: `d16CopiesAndMeasurementInvariantsRejectNaNOverflowAndMismatchedKeys`; `d16RejectsAFalseReliableZeroCost`; `d16SnapshotMakesADefensiveCopy`; `d16SnapshotRejectsOverCapBeforeIteratingOrAllocatingCopy`; `d16UnmeasuredLossAndMeasuredSuspectKeepStateAndAgeConsistent`
- **M18**: `allOriginalEventsKeepIdentityAndOrderIncludingErrorAndRepair`; `tapFailureStillDeliversTheOriginalEventAndRollsBackResources`
- **M19**: `repairedHello20SessionGetsFreshMetricsWithoutBootstrapOrNewTransport`
- **M20**: `m20WireEnumAndCodecRetainV2AndRejectCodeThree`
- **M21**: `closeInsideOriginalObserverDoesNotSelfJoinOrHideItsEvent`; `partialFactoryFailureAndImmediateCloseCannotLeaveOwnerAlive`; `realCliSubprocessUsesTheNormalCompositionAndExitsCleanly`; `realTcpUsesSameOwnerV2ProbesAndEofInvalidatesCurrentLink`; `shutdownSharesOriginalDeadlineAndPreservesGracefulNodeLeave`
- **M22**: **PENDENTE nas etapas próprias:** regressão incremental executada; clean verify novo, manual humano, revisão final do código e integração ainda não executados/aprovados.

## D01–D16

- **D01**: `d01StateMutationOffOwnerIsRejectedWithoutTouchingTransport`; `d01ValidatesOfflineIdentityAndBorrowedOwnerCapacity`
- **D02**: `d02AgeUsesLocalSubtractionAndAllowsNegativeTimestamp`; `d02FirstRttAndEwmaUseIndependentExpectedValues`
- **D03**: `d03NewConnectionSameInstanceResetsAllMetrics`
- **D04**: `d04m04CadenceHandlesBothIntervalRelationsAndDelayedTicksWithoutBurst`; `d04UpStartsOneV2ProbeAndNoReliableZero`
- **D05**: `d05EarliestValidReplyWinsEvenIfAdmissionArrivesAfterDeadline`; `d05EarlyPongWaitsForAdmissionAndKeepsCapturedTime`
- **D06**: `d06AllRefusalsAndExceptionalAdmissionsDiscardEarlyReplies`; `d06RefusedAdmissionDiscardsEarlyPongWithoutRemoteLoss`
- **D07**: `d07LateEarlyReplyCannotRescueAnAdmittedExpiredProbe`; `d07ExactDeadlineCannotBeReclassifiedAsGoodPong`; `d07AdmissionCanOutliveDeadlineWithoutFabricatingLossOrSpinningTimer`
- **D08**: `d08WrongRequestCannotCompleteCurrentProbe`
- **D09**: `d09ThreeAdmittedTimeoutsCloseOnlyExpectedSession`; `d09DownInvalidatesImmediatelyWithoutThreeTimeouts`
- **D10**: `d10LocalSyncThrowFutureFailureAndInvalidMessageAreFatalNotPeerLoss`; `d10EchoPreservesRequestAndProbeIds`; `d10EchoFloodIsBoundedAndEchoFailureFollowsLocalPolicy`; `d10EchoClosedOverflowAndExceptionalResultAreNotRemoteTimeouts`; `d10ObserverFailureIsObservableAndStopsWithoutCallingItForever`
- **D11**: `d11ReservedCloseProgressesWhileSendCapacityIsFull`; `d11OnlyOnePhysicalCloseIsAdmittedAndFailureIsLocalFatal`; `d11CloseHasAnIndependentReservationAndRetiredSendsKeepTheirTickets`; `d11SixtyThreeSessionsAndChurnStayWithinPhysicalSendBudget`
- **D12**: `d12CanceledRunningTimerStillUsesPhysicalSlotAndThirdJobIsRejected`; `d12GenericOldWakeupDoesNotExpireNewPendingOrLoseNextDeadline`
- **D13**: `d13CallbackStillUnwindingCannotBeMistakenForPhysicalTermination`; `d13PhysicalReleaseWakesAQueuedCloseAfterItsContinuationFinished`; `d13RejectedOwnerOrSchedulerStopsWithVisibleLocalReason`; `d13QueuedCompletionKeepsPhysicalReservationUntilItRunsOrOwnerStops`
- **D14**: `d14CountersSaturateWithoutWrappingAndKeepExactSampleIdentity`; `d14NanoTimeWrapAndNegativeInstantsAreValidButBackwardElapsedIsFatal`; `d14ProbeIdMaximumDoesNotWrapOrRepeat`; `d14LongestConfiguredTimeoutStillConvertsWithoutOverflow`
- **D15**: `d15LogicalCleanupDoesNotClaimPhysicalCompletionWithHeldAdmission`; `d15StopRejectsNewWorkDoesNotStopBorrowedOwnerAndRejectsSelfJoin`; `d15CloseBeforeStartIsIdempotentAndBorrowedOwnerIsNeverClosed`; `d15HeldEchoAndHeldCloseAreCountedUntilTheirRealCompletion`
- **D16**: `d16SnapshotRejectsOverCapBeforeIteratingOrAllocatingCopy`; `d16SnapshotMakesADefensiveCopy`; `d16CopiesAndMeasurementInvariantsRejectNaNOverflowAndMismatchedKeys`; `d16UnmeasuredLossAndMeasuredSuspectKeepStateAndAgeConsistent`; `d16RejectsAFalseReliableZeroCost`

## Integração própria

- `partialFactoryFailureAndImmediateCloseCannotLeaveOwnerAlive`
- `realTcpUsesSameOwnerV2ProbesAndEofInvalidatesCurrentLink`
- `snapshotBeforeStartIsEmptyAndDoesNotConstructTheMonitor`
- `realCliSubprocessUsesTheNormalCompositionAndExitsCleanly`
- `tapFailureStillDeliversTheOriginalEventAndRollsBackResources`
- `allOriginalEventsKeepIdentityAndOrderIncludingErrorAndRepair`
- `shutdownSharesOriginalDeadlineAndPreservesGracefulNodeLeave`
- `closeInsideOriginalObserverDoesNotSelfJoinOrHideItsEvent`
- `heldMonitorAdmissionUsesOnlyTheOriginalGlobalCleanupBudget`
- `repairedHello20SessionGetsFreshMetricsWithoutBootstrapOrNewTransport`
- `snapshotAfterCloseBeforeStartRemainsReadableAndEmpty`

M20 inclui também os testes históricos de codec, UTF-8/framing, transporte, FIFO,
quotas, fixtures v2/UDP e configuração executados na regressão, sem os substituir
pelo smoke do enum. M18 usa identidade do objeto/ordem, ERROR real em TCP e falha
do tap controlada; M19 usa reparação real depois de fechar bootstrap/relays.
M16/D15 incluem future retido, eco/fecho em shutdown, callback ainda em execução,
reserva física e deadline global sem renovação. Nenhuma execução constitui GUI,
CORE, manual humano ou revisão final de Fillmore. V-010 exercitado tecnicamente,
sem declarar a etapa Test aprovada.
