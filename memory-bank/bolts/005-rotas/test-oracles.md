# IMP-007 — V-010 / M01–M22 / D01–D16 em Test

Execução de clean verify do código aprovado:1176 testes,0falhas/0erros/0ignorados,
exit0; todos os métodos mapeados abaixo constam dos XML novos. M01–M21 e D01–D16
exercitados tecnicamente, sem inferir execução humana. M22 cumprido após integração f990503; revisão final Fillmore confirmada e Test aprovado e execução humana02
confirmada e conferida nos logs. O mapa de Implement mantém o seu contexto histórico.

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
- **M22**: clean verify novo EXECUTADO (1176/0/0/0); manual humano execução02 cumprido e Test aprovado; revisão final do código Fillmore cumprida; integração f990503 cumprida e fecho documental autorizado.

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


Integração real própria:52 testes monitoring e11 OverlayNodeMonitoringTest no
clean verify; TCP v2 loopback, HELLO5/REPAIR_HELLO20, EOF, ERROR/ordem/identidade,
subprocesso CLI, callbacks/timers retidos, shutdown físico e primeiro prazo global.
XML completos em experiments/construction/005-rotas/test-clean-verify-xml/.
Nenhum teste foi removido, ignorado ou afrouxado em Test. Q-C-25 permaneceu
exclusivamente na cadeia já aprovada, byte a byte igual à entrada Test.
