import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.*;
import org.junit.platform.engine.discovery.DiscoverySelectors;
var listener = new SummaryGeneratingListener();
var launcher = LauncherFactory.create();
launcher.registerTestExecutionListeners(listener);
var request = LauncherDiscoveryRequestBuilder.request().selectors(DiscoverySelectors.selectMethod("pt.uminho.esr.ott.routing.ActivationTest", "m07CancelledChildCannotBeReauthorizedByLateAckAcceptance")).build();
launcher.execute(request);
var summary = listener.getSummary();
summary.printTo(new java.io.PrintWriter(System.out, true));
summary.printFailuresTo(new java.io.PrintWriter(System.out, true));
System.out.println("M07_COUNTS tests=" + summary.getTestsFoundCount() + " failed=" + summary.getTestsFailedCount() + " skipped=" + summary.getTestsSkippedCount() + " aborted=" + summary.getTestsAbortedCount());
/exit (summary.getTestsFailedCount()==0L && summary.getTestsFoundCount()>0L ? 0 : 1)
