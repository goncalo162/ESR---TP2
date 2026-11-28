# Conferência estática A01–A08 — fontes reais

Excerto numerado da working tree, sem executar código. Hashes integrais em project-api-inspection.json.

## A01/A05: Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java

```text
239:         var console = new Console(input, output, error, config, clock);
240: 
241:         var hook = new Thread(() ->
242:         {
243:             console.stop(false);
244: 
245:             try
246:             {
247:                 console.finished.await(config.runtime().control().shutdownTimeout().toNanos(), TimeUnit.NANOSECONDS);
248:             }
249:             catch(InterruptedException e)
250:             {
251:                 Thread.currentThread().interrupt();
252:             }
253:         }, "ott-shutdown-hook");
254: 
255:         RunningService service = null;
256:         var diagnostic = new DiagnosticConsole(console, clock);
257:         boolean interrupted = false;
258:         boolean inputDone = true;
259:         boolean outputDone = true;
260:         boolean serviceDone = true;
261: 
262:         try
263:         {
264:             Runtime.getRuntime().addShutdownHook(hook);
265:             console.writer.start();
266:             service = factory == null ? open(config, console) : factory.open(config);
267:             console.log("STARTED node=" + config.nodeId() + " role=" + config.role() + " pid=" + ProcessHandle.current().pid(), false);
```

```text
320:                 else
321:                     console.log("COMMAND_ERROR unknown command", true);
322:             }
323:         }
324:         catch(InterruptedException e)
325:         {
326:             console.stop(true);
327:             interrupted = true; // Restore after bounded cleanup.
328:         }
329:         catch(Exception e)
330:         {
331:             console.log("FATAL " + bounded(e.getMessage()), true);
332:             console.stop(true);
333:         }
334:         finally
335:         {
336:             console.stop(false);
337:             diagnostic.cancel();
338:             // Close/cancel the owned input before waiting on the network budget.
339:             console.reader.interrupt();
340:             closeInput.run();
341: 
342:             if(service != null)
343:             {
344:                 try
345:                 {
346:                     service.requestShutdown(console.failed.get() ? 1 : 0, console.left());
347:                     serviceDone = service.awaitTermination(console.left());
348: 
349:                     if(!serviceDone)
350:                     {
351:                         console.failed.set(true);
352:                         service.close();
353:                     }
354: 
355:                     if(service.failure().isPresent())
356:                         console.failed.set(true);
357:                 }
358:                 catch(RuntimeException e)
359:                 {
360:                     serviceDone = false;
361:                     console.failed.set(true);
362:                     console.log("CLEANUP_ERROR " + e.getMessage(), true);
363: 
364:                     try
365:                     {
366:                         service.close();
367:                     }
368:                     catch(RuntimeException ignored)
369:                     {
370:                         // Preserve the first cleanup failure.
371:                     }
372:                 }
373:             }
374: 
```

```text
472:         var reference = new AtomicReference<OverlayNode>();
473: 
474:         var node = OverlayNode.withRoutingObserver(config, instance, event ->
475:         {
476:             console.log("EVENT node=" + config.nodeId() + " instance=" + instance + " " + event,
477:                     event instanceof NeighborManager.Received r && r.message().type() == pt.uminho.esr.ott.protocol.MessageType.ERROR);
478: 
479:             if(event instanceof NeighborManager.Received received)
480:             {
481:                 var m = received.message();
482:                 UUID stream = m.payload() instanceof pt.uminho.esr.ott.protocol.ControlMessage.Trace t ? t.streamId()
483:                         : m.payload() instanceof pt.uminho.esr.ott.protocol.ControlMessage.TraceResult t ? t.streamId() : null;
484:                 var n = reference.get();
485: 
486:                 if(stream != null && n != null)
487:                 {
488:                     var snapshot = n.routingSnapshot();
489:                     var route = snapshot.routes().get(stream);
490:                     console.log("TRACE_HOP node=" + config.nodeId() + " instance=" + instance + " stream=" + stream
491:                             + " request=" + m.requestId() + " direction=" + (m.type() == pt.uminho.esr.ott.protocol.MessageType.ROUTE_TRACE ? "request" : "response")
492:                             + " session=" + received.session() + " capturedAt=" + snapshot.capturedAtNanos() + " costMicros="
493:                             + (route == null ? "UNAVAILABLE" : route.selectedMetric().map(x -> Long.toString(x.costMicros())).orElse("UNAVAILABLE")), false);
494:                 }
495:             }
496:         }, event -> event.routeSwitch().ifPresent(change ->
497:                 console.log(switchLog(config.nodeId(), instance, change), false)));
498: 
499:         reference.set(node);
500: 
501:         node.start().whenComplete((v, error) ->
502:         {
503:             if(error == null)
504:                 console.log("READY node=" + config.nodeId() + " instance=" + instance, false);
505:         });
```

```text
892:             budget = config.runtime().control().shutdownTimeout().toNanos();
893:             commands = new ArrayBlockingQueue<>(config.runtime().overlay().eventsCapacity());
894:             outputs = new ArrayBlockingQueue<>(config.runtime().control().queueCapacity());
895:             reader = Thread.ofVirtual().name("ott-cli-input").unstarted(() -> read(input));
896:             writer = Thread.ofVirtual().name("ott-cli-output").unstarted(() -> write(output, error));
897:         }
898: 
899: 
900: 
901:         //* === Métodos === *
902: 
903:         synchronized void stop(boolean fatal)
904:         {
905:             if(fatal)
906:                 failed.set(true);
907: 
908:             if(stopped.compareAndSet(false, true))
909:                 stopAt = clock.getAsLong();
910:         }
911: 
912:         Duration left()
913:         {
914:             return Duration.ofNanos(Math.max(0, budget - (clock.getAsLong() - stopAt)));
915:         }
916: 
917:         void log(String text, boolean error)
918:         {
919:             if(!outputs.offer(new OutputEvent(oneLine(text), error)))
920:                 stop(true);
921:         }
922: 
```

## A02/A03/A05: Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java

```text
24:     private final NetworkMonitor.Factory monitorFactory;
25:     private volatile NetworkMonitor monitor;
26:     private final RoutingService.Factory routingFactory;
27:     private final RoutingService.Observer routingObserver;
28:     private volatile RoutingService routing;
29:     private final AtomicReference<Throwable> failure = new AtomicReference<>();
30:     private final CompletableFuture<Void> started = new CompletableFuture<>();
31:     private NeighborManager.SerialOwner owner;
32:     private volatile NeighborManager manager;
33:     private CompletionStage<NeighborManager.ShutdownReport> shutdown;
34:     private boolean closed;
```

```text
55:             NetworkMonitor.Factory monitorFactory, RoutingService.Factory routingFactory)
56:     {
57:         this(configuration, instanceId, observer, monitorFactory, routingFactory, event -> { });
58:     }
59: 
60: 
61:     private OverlayNode(NodeConfiguration configuration, UUID instanceId, NeighborManager.Observer observer,
62:             NetworkMonitor.Factory monitorFactory, RoutingService.Factory routingFactory,
63:             RoutingService.Observer routingObserver)
64:     {
65:         this.routingObserver = Objects.requireNonNull(routingObserver);
66:         this.configuration = Objects.requireNonNull(configuration);
67:         instance = Objects.requireNonNull(instanceId);
68:         this.observer = Objects.requireNonNull(observer);
69:         this.monitorFactory = Objects.requireNonNull(monitorFactory);
70:         this.routingFactory = Objects.requireNonNull(routingFactory);
71:         RoutingService.validateConfiguration(configuration, instanceId);
72:         new NeighborManager(configuration, instance, observer);
73:     }
74: 
75: 
76: 
77:     //* === Métodos Públicos === *
78: 
79:     public static OverlayNode withRoutingObserver(NodeConfiguration configuration, UUID instanceId,
80:             NeighborManager.Observer observer, RoutingService.Observer routingObserver)
81:     {
82:         return new OverlayNode(configuration, instanceId, observer, NetworkMonitor::new,
83:                 RoutingService::new, routingObserver);
84:     }
85: 
86: 
87:     /** Q-C-32: diagnostic delegation only, no new owner or transport. */
88:     public RoutingService.TraceTicket requestTrace(UUID streamId)
89:     {
90:         Objects.requireNonNull(streamId);
91:         var r = routing;
92:         return r == null ? RoutingService.unavailableTrace(streamId) : r.requestTrace(streamId);
93:     }
94: 
95:     public RoutingService.Snapshot routingSnapshot()
96:     {
97:         var r = routing;
98:         return r == null ? RoutingService.emptySnapshot() : r.snapshot();
99:     }
100: 
101:     public NetworkMonitor.Snapshot monitoringSnapshot()
102:     {
103:         var m = monitor;
104:         return m == null ? new NetworkMonitor.Snapshot(0, Map.of(), new NetworkMonitor.Diagnostics(
```

```text
153:                 routing = Objects.requireNonNull(routingFactory.create(configuration, instance, owner, service::send,
154:                         event ->
155:                         {
156:                             try
157:                             {
158:                                 routingObserver.onEvent(event);
159: 
160:                             }finally{
161:                                 if(event.fatal())
162:                                     failMonitoring(new IllegalStateException("Routing: " + event.reason()));
163:                             }
164:                         }));
165: 
166:                 if(!owner.offer(() ->
167:                 {
168:                     try
169:                     {
170:                         monitor.start();
171:                         routing.start();
172:                     }
173:                     catch(RuntimeException e)
174:                     {
175:                         failMonitoring(e);
176:                     }
177:                 }))
178:                     throw new RejectedExecutionException("Node start owner rejected");
```

```text
256:         failure.compareAndSet(null, error);
257:         stopImmediately();
258:     }
259: 
260: 
261: 
262:     //* === Shutdown === *
263: 
264:     private void stopImmediately()
265:     {
266:         synchronized(this)
267:         {
268:             if(!closed)
269:             {
270:                 closed = true;
271:                 stopStarted = System.nanoTime();
272:                 stopBudget = configuration.runtime().control().shutdownTimeout().toNanos();
273:             }
274:         }
275: 
276:         started.completeExceptionally(new IllegalStateException("Node closed"));
277: 
278:         try
279:         {
280:             var r = routing;
281:             if(r != null)
282:                 r.requestClose();
283:         }
284:         finally
285:         {
286:             try
287:             {
288:                 var mon = monitor;
289:                 if(mon != null)
290:                     mon.requestClose();
291:             }
292:             finally
293:             {
294:                 try
295:                 {
296:                     var service = manager;
297:                     if(service != null)
298:                         service.requestClose();
299:                 }
300:                 finally
301:                 {
302:                     var serial = owner;
303:                     if(serial != null)
304:                         serial.requestClose();
305:                 }
```

## A06: Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java

```text
347:     public CompletionStage<NeighborManager.ShutdownReport> requestShutdown(int reason, Duration remainingBudget)
348:     {
349:         long nanos = budget(remainingBudget);
350: 
351:         if(reason < 0 || reason > 1)
352:             throw new IllegalArgumentException("Leave reason 0/1 required");
353: 
354:         final CompletableFuture<NeighborManager.ShutdownReport> result;
355: 
356:         synchronized(this)
357:         {
358:             if(shutdown != null)
359:                 return shutdown;
360: 
361:             if(!closed)
362:             {
363:                 closed = true;
364:                 stopStarted = System.nanoTime();
365:                 stopBudget = Math.min(nanos, configuration.runtime().control().shutdownTimeout().toNanos());
366:             }
367: 
368:             result = new CompletableFuture<>();
369:             shutdown = result.minimalCompletionStage();
370:         }
371: 
372:         started.completeExceptionally(new IllegalStateException("Node shutting down"));
373: 
374:         CompletionStage<Void> routeCleanup;
375:         try
376:         {
377:             var r = routing;
378:             routeCleanup = r == null ? CompletableFuture.completedStage(null) : r.requestClose();
379:         }
380:         catch(RuntimeException e)
381:         {
382:             routeCleanup = CompletableFuture.failedStage(e);
383:         }
384: 
385:         CompletionStage<Void> monitoring;
386:         try
387:         {
388:             var mon = monitor;
389:             monitoring = mon == null ? CompletableFuture.completedStage(null) : mon.requestClose();
390:         }
391:         catch(RuntimeException e)
392:         {
393:             monitoring = CompletableFuture.failedStage(e);
394:         }
395: 
396:         CompletionStage<NeighborManager.ShutdownReport> leaving;
397:         try
398:         {
399:             var service = manager;
400:             leaving = service == null ? CompletableFuture.completedStage(new NeighborManager.ShutdownReport(java.util.List.of(), false))
401:                     : service.requestShutdown(reason, Duration.ofNanos(left(stopBudget, stopStarted)));
402:         }
403:         catch(RuntimeException e)
404:         {
405:             leaving = CompletableFuture.failedStage(e);
406:         }
407: 
408:         var leave = leaving;
409:         CompletableFuture.allOf(routeCleanup.toCompletableFuture(), monitoring.toCompletableFuture(), leave.toCompletableFuture()).whenComplete((v, error) ->
410:         {
411:             if(error != null)
412:                 result.completeExceptionally(error);
413:             else
414:                 result.complete(leave.toCompletableFuture().join());
415:         });
416: 
417:         return shutdown;
```

```text
420:     public boolean awaitTermination(Duration remainingBudget)
421:     {
422:         long nanos = budget(remainingBudget);
423:         long began = System.nanoTime();
424:         final CompletionStage<NeighborManager.ShutdownReport> stopping;
425:         final NeighborManager.SerialOwner serial;
426: 
427:         synchronized(this)
428:         {
429:             if(!closed)
430:                 return false;
431: 
432:             stopping = shutdown;
433:             serial = owner;
434:             nanos = Math.min(nanos, left(stopBudget, stopStarted));
435:         }
436: 
437:         if(serial != null && serial.isOwnerThread())
438:             return false;
439: 
440:         boolean done = true;
441: 
442:         try
443:         {
444:             if(stopping != null)
445:                 stopping.toCompletableFuture().get(left(nanos, began), TimeUnit.NANOSECONDS);
446:         }
447:         catch(InterruptedException e)
448:         {
449:             Thread.currentThread().interrupt();
450:             done = false;
451:         }
452:         catch(ExecutionException | TimeoutException e)
453:         {
454:             done = false;
455:         }
456: 
457:         var m = manager;
458:         if(!done && m != null)
459:             m.requestClose();
460: 
461:         if(m != null)
462:             done &= m.awaitTermination(Duration.ofNanos(left(nanos, began)));
463: 
464:         var mon = monitor;
465:         if(mon != null)
466:         {
467:             if(!done)
468:                 mon.requestClose();
469: 
470:             done &= mon.awaitTermination(Duration.ofNanos(left(nanos, began)));
471:         }
472: 
473:         var r = routing;
474:         if(r != null)
475:         {
476:             if(!done)
477:                 r.requestClose();
478: 
479:             done &= r.awaitTermination(Duration.ofNanos(left(nanos, began)));
480:         }
481: 
482:         if(serial != null)
483:         {
```

```text
491:     @Override
492:     public void close()
493:     {
494:         stopImmediately();
495:         final NeighborManager.SerialOwner serial;
496:         final long nanos;
497: 
498:         synchronized(this)
499:         {
500:             serial = owner;
501:             nanos = left(stopBudget, stopStarted);
502:         }
503: 
504:         if(serial != null && serial.isOwnerThread())
505:             return;
506: 
507:         if(!awaitTermination(Duration.ofNanos(nanos)))
508:             throw new IllegalStateException("Node shutdown deadline");
509:     }
```

## A01/A02/A03/A04/A07: Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java

```text
18:     public interface ControlPort { CompletionStage<Connection.SendResult> send(Neighbor.Session session, ControlMessage message); }
19:     public interface Observer { void onEvent(Event event); }
20:     public interface Factory { RoutingService create(NodeConfiguration config, UUID instanceId, NeighborManager.SerialOwner owner, ControlPort port, Observer observer); }
21:     public interface Ticket { void cancel(); }
22:     public interface Scheduler {
23:         Ticket schedule(Runnable task, long delayNanos);
24:         void requestClose(); boolean awaitTermination(Duration budget); int physicalJobs();
25:     }
```

```text
168:     public record Event(Reason reason, boolean fatal, Optional<SwitchEvent> routeSwitch)
169:     {
170:         public Event
171:         {
172:             Objects.requireNonNull(reason);
173:             Objects.requireNonNull(routeSwitch);
174:         }
175: 
176: 
177:         public Event(Reason reason, boolean fatal)
178:         {
179:             this(reason, fatal, Optional.empty());
180:         }
181:     }
182:     public record Diagnostics(Phase phase, int sessions, int bindings, int references, int cacheEntries,
183:             int ledgers, int pendingCells, int pendingIntents, int quotas, int sendTickets, int timers,
184:             long failures, boolean countersSaturated, Reason lastReason) { }
185:     public record Snapshot(long capturedAtNanos, StreamCatalog.Snapshot catalog,
186:             Map<UUID,Route> routes, Diagnostics diagnostics, Map<UUID,Route.Confirmation> forwarding,
187:             Map<UUID,Route.Recovery> recoveries)
188:     {
189:         //* === Construtores === *
190: 
191:         public Snapshot
192:         {
193:             Objects.requireNonNull(catalog);
194:             Objects.requireNonNull(diagnostics);
195:             routes = Map.copyOf(routes);
196:             forwarding = Map.copyOf(forwarding);
197:             recoveries = Map.copyOf(recoveries);
198:         }
199: 
200: 
201:         public Snapshot(long capturedAtNanos, StreamCatalog.Snapshot catalog,
202:                 Map<UUID,Route> routes, Diagnostics diagnostics,
203:                 Map<UUID,Route.Confirmation> forwarding)
204:         {
205:             this(capturedAtNanos, catalog, routes, diagnostics, forwarding, Map.of());
206:         }
207: 
208: 
209:         public Snapshot(long capturedAtNanos, StreamCatalog.Snapshot catalog,
210:                 Map<UUID,Route> routes, Diagnostics diagnostics)
211:         {
212:             this(capturedAtNanos, catalog, routes, diagnostics, Map.of(), Map.of());
213:         }
214:     }
215:     private static final long WINDOW=1_000_000_000L;
216:     private final NodeConfiguration config;
```

```text
378:     public void start(){
379:         requireOwner();if(phase!=Phase.NEW||stopping.get())return;phase=Phase.RUNNING;
380:         try{
381:             long t=now();
382:             for(var s:config.streams()){
383:                 var row=bind(s.streamId(),s.serverId());if(row==null)throw new IllegalStateException("Configured source capacity");
384:                 if(!epoch(s.serverId(),instance))throw new IllegalStateException("Configured source epoch");
385:                 row.info=new StreamInfo(s.streamId(),s.serverId(),s.title(),1,s.fps(),instance);row.local=true;row.offered=true;row.nextAt=t+period;
386:                 applied(root.offerLocal(row.id,instance));
387:             }
388:             drive();
389:         }catch(RuntimeException e){note(Reason.INVALID_LOCAL_MESSAGE,true);}
390:     }
```

```text
531:     public void setLocalAvailable(UUID id,boolean available){
532:         requireOwner();if(stopping.get())return;var row=rows.get(Objects.requireNonNull(id));
533:         if(row==null||!row.local)throw new IllegalArgumentException("Configured local stream required");
534:         if(row.offered==available)return;row.offered=available;
535:         try{
536:             activation.kick();
537:             if(available){row.sequence=Math.incrementExact(row.sequence);row.nextAt=now()+period;applied(root.offerLocal(id,instance));}
538:             else applied(root.removeLocalOffer(id,instance));
539:             row.version=Math.incrementExact(row.version);dirty();drive();
540:         }catch(ArithmeticException e){note(Reason.SEQUENCE_EXHAUSTED,true);}
541:     }
```

```text
700:     }
701:     private void drive(){
702:         requireOwner();if(driving||stopping.get()||phase!=Phase.RUNNING)return;driving=true;
703:         try{
704:             long t=now();completions();if(stopping.get())return;
705:             applied(root.expire());cache.entrySet().removeIf(e->t-e.getValue().began>=history);
706:             for(var row:rows.values())if(row.offered&&t-row.nextAt>=0){
707:                 try{row.sequence=Math.incrementExact(row.sequence);row.version=Math.incrementExact(row.version);}
708:                 catch(ArithmeticException e){note(Reason.SEQUENCE_EXHAUSTED,true);return;}
709:                 row.nextAt=t+period;dirty();
710:             }
711:             // All owner projections precede the resulting send attempts.
712:             publish(t); traceDrive(); activation.drive(); int swept=sweepQuotas(t);scan(64-swept);
713:             // A slow/control-port callback may also advance time during this turn.
714:             long finished=now();applied(root.expire());publish(finished);
715:             if(scanRemaining>0)wake();
716:             schedule(finished);
717:         }catch(RuntimeException e){note(Reason.CLOCK_ERROR,true);}
718:         finally{driving=false;}
```

```text
746:         }
747:     }
748:     private void publish(long t){
749:         var routes=new LinkedHashMap<UUID,Route>();var entries=new LinkedHashMap<UUID,StreamCatalog.Entry>();int refs=0,intents=0;
750:         for(var row:rows.values()){
751:             if(row.route!=null)routes.put(row.id,row.route);if(row.info==null)continue;refs++;
752:             if(row.offered)entries.put(row.id,new StreamCatalog.Entry(row.info,row.route.state(),row.route.selectedMetric(),row.route.knownUpstream(),true,OptionalLong.empty()));
753:             else{
754:                 long remaining=0;for(var c:row.route.candidates())remaining=Math.max(remaining,c.candidateTtlNanos()-(t-c.generationStartedAtNanos()));
755:                 if(remaining>0)entries.put(row.id,new StreamCatalog.Entry(row.info,row.route.state(),row.route.selectedMetric(),row.route.knownUpstream(),false,OptionalLong.of(remaining)));
756:             }
757:         }
758:         for(var l:ledgers.values())if(l.cell){if(l.debt!=null)intents++;if(l.want!=null)intents++;}
759:         var projection=new StreamCatalog.Snapshot(t,entries);catalog.replace(projection);
760:         int physical;synchronized(resources){physical=admissions.size();}
761:         view=new Snapshot(t,projection,routes,new Diagnostics(phase,sessions.size(),rows.size(),refs,cache.size(),ledgers.size(),pending,intents,quotas.size(),physical,scheduler.physicalJobs(),failures,countersSaturated,lastReason),activation.forwarding(t),activation.recoveries(t));
762:     }
```

```text
997:     private void note(Reason reason,boolean fatal){
998:         lastReason=reason;if(failures==Long.MAX_VALUE)countersSaturated=true;else failures++;
999:         if(fatal)requestClose();
1000:         try{observer.onEvent(new Event(reason,fatal));}catch(RuntimeException e){requestClose();}
1001:     }
1002:     public Snapshot snapshot(){return view;}
1003:     private void ownerStopped(){firstStop.compareAndSet(null,System.nanoTime());stopping.set(true);scheduler.requestClose();lastReason=Reason.OWNER_REJECTED;cleanup();}
1004:     public CompletionStage<Void> requestClose(){
1005:         firstStop.compareAndSet(null,System.nanoTime());if(stopping.compareAndSet(false,true)){
1006:             scheduler.requestClose();if(owner.isOwnerThread())cleanup();else owner.offer(this::cleanup);
1007:         }return cleaned.minimalCompletionStage();
1008:     }
1009:     private void cleanup(){
1010:         requireOwner();if(cleaned.isDone())return;stopping.set(true);scheduler.requestClose();if(alarm!=null)alarm.cancel();alarm=null;
1011:         closeTraces();
1012:         activation.close();
1013:         rows.clear();streamIndex.clear();originCounts.clear();epochs.clear();sessions.clear();peerIndex.clear();costs.clear();metricStamps.clear();ledgers.clear();cache.clear();quotas.clear();quotaSweep.clear();pending=0;scanRemaining=0;phase=Phase.STOPPED;
1014:         synchronized(resources){for(var a:List.copyOf(admissions)){a.continuationDone=true;release(a);}}
1015:         publish(clock.getAsLong());cleaned.complete(null);
1016:         if(registration!=null){registration.remove();registration=null;}
1017:     }
1018:     private static long nanos(Duration d){Objects.requireNonNull(d);if(d.isNegative())throw new IllegalArgumentException("Negative budget");try{return d.toNanos();}catch(ArithmeticException e){throw new IllegalArgumentException("Budget overflow",e);}}
1019:     private static long left(long budget,long began){return Math.max(0,budget-(System.nanoTime()-began));}
1020:     public boolean awaitTermination(Duration timeout){
1021:         long budget=nanos(timeout),began=System.nanoTime();if(owner.isOwnerThread()||!stopping.get())return false;
1022:         budget=Math.min(budget,left(shutdownNanos,firstStop.get()));
1023:         try{cleaned.get(left(budget,began),TimeUnit.NANOSECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}catch(ExecutionException|TimeoutException e){return false;}
1024:         if(!scheduler.awaitTermination(Duration.ofNanos(left(budget,began))))return false;
1025:         synchronized(resources){while(!admissions.isEmpty() || !traceSends.isEmpty() || !activation.sends.isEmpty()){
1026:             long rest=left(budget,began);if(rest==0)return false;
1027:             try{TimeUnit.NANOSECONDS.timedWait(resources,rest);}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}
1028:         }}return true;
1029:     }
1030:     public void close(){requestClose();if(!owner.isOwnerThread()&&!awaitTermination(Duration.ofNanos(left(shutdownNanos,firstStop.get()))))throw new IllegalStateException("Routing physical shutdown deadline");}
```

```text
1657:         boolean healthy(Tree t,long at){
1658:             var c=t.confirmed;if(c==null||!epochCurrent(t))return false;
1659:             return c.source?row(t.id).offered:current(c.parent)&&c.lease.remainingNanos(at)>0;
1660:         }
1661:         boolean compatible(Tree t,List<String> prefix,long at){
```

```text
2396:         Map<UUID,Route.Confirmation> forwarding(long at){
2397:             var result=new LinkedHashMap<UUID,Route.Confirmation>();
2398:             for(var t:trees.values())if(healthy(t,at)){
2399:                 var c=t.confirmed;var children=new LinkedHashMap<String,Subscription>();
2400:                 for(var child:t.children.values())if(child.active!=null&&child.active.validity().orElseThrow().remainingNanos(at)>0)
2401:                     children.put(child.line.key.session.nodeId(),child.active);
2402:                 var locals=new HashSet<UUID>();
2403:                 for(var token:t.confirmedLocals){var l=t.locals.get(token);if(l!=null&&!l.cancelled.get())locals.add(token);}
2404:                 if(!children.isEmpty()||!locals.isEmpty())result.put(t.id,new Route.Confirmation(t.id,t.origin,t.epoch,c.source,
2405:                         Optional.ofNullable(c.parent),c.path,children,locals,Optional.ofNullable(c.lease),t.version));
2406:             }
2407:             return result;
2408:         }
2409:         void sessionDown(Neighbor.Session session)
2410:         {
```

## A06: Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java

```text
41:     public synchronized CompletionStage<ShutdownReport> requestShutdown(int reason, Duration remainingBudget) {
42:         Objects.requireNonNull(remainingBudget);
43:         if (reason < 0 || reason > 1 || remainingBudget.isNegative())
44:             throw new IllegalArgumentException("Leave reason 0/1 and nonnegative budget required");
45:         final long nanos;
46:         try { nanos = remainingBudget.toNanos(); }
47:         catch (ArithmeticException e) { throw new IllegalArgumentException("Budget overflow", e); }
48:         if (shutdownResult != null) return shutdownView;
49:         shutdownResult = new CompletableFuture<>(); shutdownView = shutdownResult.minimalCompletionStage();
50:         notificationStarted = now();
51:         notificationTotalBudget = Math.min(nanos, plan.config.runtime().control().shutdownTimeout().toNanos());
52:         notificationBudget = notificationTotalBudget / 2;
53:         draining.set(true);
54:         if (!launched || stopping.get() || notificationBudget == 0) {
55:             requestClose(); publishShutdown(LeaveOutcome.ABORTED); return shutdownView;
56:         }
57:         if (!post(() -> beginShutdown(reason), true)) {
58:             requestClose();
59:             // No notification event was admitted, so there is no owner-owned attempt map to read.
60:             shutdownResult.complete(new ShutdownReport(List.of(), false));
61:         }
```

```text
955:     public synchronized void requestClose() {
956:         if (!stopping.compareAndSet(false, true)) return;
957:         closeStarted = draining.get() ? notificationStarted : now();
958:         var services = io; if (services != null) services.requestClose();
959:         var timers = deadlines; if (timers != null) timers.requestClose();
960:         if (owner == null) { started.completeExceptionally(new IllegalStateException("Manager closed")); cleaned.complete(null); publishShutdown(LeaveOutcome.ABORTED); return; }
961:         if (owner.isOwnerThread()) cleanup();
962:         else if (!owner.offer(this::cleanup)) owner.fail(new RejectedExecutionException("Stop could not enter owner"));
963:     }
964:     private void ownerStopped() {
965:         stopping.set(true);
966:         if (closeStarted == 0) closeStarted = draining.get() ? notificationStarted : now();
967:         if (io != null) io.requestClose(); if (deadlines != null) deadlines.requestClose(); cleanup();
968:     }
969:     private void cleanup() {
970:         if (cleanupDone) return; cleanupDone = true;
971:         if (phase != Phase.FAILED) phase = Phase.CLOSED;
```

```text
989:     public boolean awaitTermination(Duration timeout) {
990:         Objects.requireNonNull(timeout);
991:         if (timeout.isNegative()) throw new IllegalArgumentException("Negative timeout");
992:         if (terminationObserved.get()) return true;
993:         if (owner != null && owner.isOwnerThread()) return cleaned.isDone();
994:         long began = now(), nanos = timeout.toNanos();
995:         try {
996:             if (!cleaned.isDone()) cleaned.get(Math.max(0, remainingNanos(nanos, began)), TimeUnit.NANOSECONDS);
997:         } catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
998:         catch (ExecutionException | TimeoutException e) { return false; }
999:         Duration left = remainingMillis(nanos, began);
1000:         if (io != null && (left.isZero() || !io.network().awaitTermination(left))) return false;
1001:         left = remainingMillis(nanos, began);
1002:         if (deadlines != null && (left.isZero() || !deadlines.awaitTermination(left))) return false;
1003:         left = remainingMillis(nanos, began);
1004:         if (io != null && (left.isZero() || !io.awaitTermination(left))) return false;
1005:         left = remainingMillis(nanos, began);
1006:         boolean done = borrowed || owner == null || !left.isZero() && owner.awaitTermination(left);
1007:         if (done) terminationObserved.set(true);
1008:         return done;
1009:     }
1010:     private long remainingNanos(long budget, long began) { return Math.max(0, budget - (now() - began)); }
1011:     private Duration remainingMillis(long budget, long began) { return Duration.ofMillis(TimeUnit.NANOSECONDS.toMillis(remainingNanos(budget, began))); }
1012:     @Override public void close() {
1013:         requestClose(); if (owner != null && owner.isOwnerThread()) return;
1014:         long total = draining.get() ? notificationTotalBudget : plan.config.runtime().control().shutdownTimeout().toNanos();
1015:         long left = remainingNanos(total, closeStarted);
1016:         if (!awaitTermination(Duration.ofNanos(left))) throw new IllegalStateException("Neighbor shutdown deadline: owner, TCP, timers or I/O worker still pending", failure.get());
1017:     }
```

```text
1081:         private final Thread thread;
1082:         Owner(int capacity) {
1083:             if (capacity < 1 || capacity > 4096) throw new IllegalArgumentException("Owner capacity 1..4096");
1084:             this.capacity = capacity; queue = new ArrayBlockingQueue<>(capacity);
1085:             thread = Thread.ofVirtual().name("ott-neighbor-events").unstarted(this::loop); thread.start();
1086:         }
1087:         private void loop() {
1088:             try {
1089:                 while (!stopped.get()) {
1090:                     try { var action = queue.take(); if (!stopped.get()) action.run(); }
1091:                     catch (InterruptedException e) { if (!stopped.get()) fail(e); }
1092:                     catch (RuntimeException e) { fail(e); }
1093:                 }
1094:             } finally {
1095:                 queue.clear(); List<Runnable> cleanups;
1096:                 synchronized (hooks) { cleanups = List.copyOf(hooks.values()); hooks.clear(); }
```

```text
1110:             synchronized (hooks) {
1111:                 if (stopped.get() || hooks.size() >= 16) throw new RejectedExecutionException("Owner stopped or cleanup hook capacity");
1112:                 hooks.put(token, cleanup);
1113:             }
1114:             return () -> { synchronized (hooks) { hooks.remove(token); } };
1115:         }
1116:         public void fail(Throwable cause) { failure.compareAndSet(null, Objects.requireNonNull(cause)); requestClose(); }
1117:         public void requestClose() { stopped.set(true); if (!isOwnerThread()) thread.interrupt(); }
1118:         public boolean awaitTermination(Duration timeout) { return joinThread(thread, timeout); }
1119:         public void close() {
1120:             requestClose(); if (!isOwnerThread() && !awaitTermination(Duration.ofSeconds(3))) throw new IllegalStateException("Serial owner did not terminate");
1121:         }
1122:     }
```

## A06: Projeto/src/main/java/pt/uminho/esr/ott/monitoring/NetworkMonitor.java

```text
921:     public CompletionStage<Void> requestClose()
922:     {
923:         firstStop.compareAndSet(null, System.nanoTime());
924: 
925:         if(stopping.compareAndSet(false, true))
926:         {
927:             scheduler.requestClose();
928: 
929:             if(owner.isOwnerThread())
930:                 cleanup();
931:             else if(!owner.offer(this::cleanup))
932:                 emergency(Reason.OWNER_REJECTED);
933:         }
934: 
935:         return cleaned.minimalCompletionStage();
936:     }
```

```text
972:     public boolean awaitTermination(Duration budget)
973:     {
974:         long nanos = budgetNanos(budget);
975:         long began = System.nanoTime();
976: 
977:         if(owner.isOwnerThread() || !stopping.get())
978:             return false;
979: 
980:         nanos = Math.min(nanos, left(shutdownNanos, firstStop.get()));
981: 
982:         try
983:         {
984:             cleaned.get(left(nanos, began), TimeUnit.NANOSECONDS);
985:         }
986:         catch(InterruptedException e)
987:         {
988:             Thread.currentThread().interrupt();
989:             return false;
990:         }
991:         catch(ExecutionException | TimeoutException e)
992:         {
993:             return false;
994:         }
995: 
996:         if(!scheduler.awaitTermination(Duration.ofNanos(left(nanos, began))))
997:             return false;
998: 
```

## A07: Projeto/src/main/java/pt/uminho/esr/ott/server/VideoSource.java

```text
60:     public static VideoSource open(Path mediaBase, Path file, int maxFrameBytes)
61:             throws SourceException
62:     {
63:         Objects.requireNonNull(mediaBase, "mediaBase");
64:         Objects.requireNonNull(file, "file");
65:         validateLimit(maxFrameBytes);
66:         String label = bounded(file.toString());
67:         InputStream opened;
68:         try
69:         {
70:             Path base = mediaBase.toRealPath();
71:             Path candidate = file.isAbsolute() ? file : base.resolve(file);
72:             Path real = candidate.toRealPath();
73:             if(real.equals(base) || !real.startsWith(base))
74:                 throw new IOException("Source is not contained in mediaBase");
75: 
76:             BasicFileAttributes attributes = Files.readAttributes(real,
77:                     BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
78:             if(!attributes.isRegularFile())
79:                 throw new IOException("Source is not a regular file");
80:             opened = Files.newInputStream(real, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
81: 
82:         }catch(IOException failure){
83:             throw new SourceException(new Diagnostic(label, 0, 0, Stage.OPEN,
84:                     Reason.OPEN_IO, -1, -1, 0, "PATH_ADMISSION"), failure);
85:         }
```

```text
124:     public State state()
125:     {
126:         return state;
127:     }
128: 
129: 
130: 
131:     //* === Lógica === *
132: 
133:     public Optional<Frame> nextFrame() throws SourceException
134:     {
135:         if(state == State.END)
136:             return Optional.empty();
137:         if(state == State.FAILED)
138:             throw propagate(terminal);
139:         if(state == State.CLOSED)
140:             throw failure(Reason.CLOSED_USE, -1, -1, "SOURCE_CLOSED", null);
141: 
142:         try
143:         {
144:             stage = Stage.PREFIX;
145:             consumedInStage = 0;
146:             byte[] prefix = new byte[PREFIX_BYTES];
147:             if(read(prefix, 0, 1) == -1)
148:             {
149:                 Throwable closeFailure = releaseInput(null);
150:                 if(closeFailure != null)
151:                     throw propagate(closeFailure);
152:                 state = State.END;
153:                 return Optional.empty();
154:             }
155: 
```

```text
215:         }catch(SourceException | RuntimeException | Error primary){
216:             terminal = primary;
217:             state = State.FAILED;
218:             releaseInput(primary);
219:             throw propagate(primary);
220:         }
221:     }
222: 
223: 
224:     @Override
225:     public void close() throws SourceException
226:     {
227:         if(state != State.OPEN)
228:             return;
229:         Throwable closeFailure = releaseInput(null);
230:         if(closeFailure != null)
231:         {
232:             terminal = closeFailure;
233:             state = State.FAILED;
234:             throw propagate(closeFailure);
235:         }
236:         state = State.CLOSED;
237:     }
```

```text
509: 
510:     private Throwable releaseInput(Throwable primary)
511:     {
512:         InputStream owned = input;
513:         input = null;
514:         if(owned == null)
515:             return primary;
516:         try
517:         {
518:             owned.close();
519: 
520:         }catch(IOException | RuntimeException | Error secondary){
521:             return mergeCleanup(primary, secondary);
522:         }
523:         return primary;
524:     }
525: 
526: 
527:     private Throwable mergeCleanup(Throwable primary, Throwable secondary)
528:     {
529:         if(primary != null)
530:         {
531:             suppress(primary, secondary);
532:             return primary;
533:         }
534:         if(secondary instanceof IOException)
535:             return new SourceException(new Diagnostic(sourceId, recordIndex, prefixOffset,
536:                     Stage.CLOSE, Reason.CLOSE_IO, -1, -1, 0, "RESOURCE_CLEANUP"), secondary);
537:         return secondary;
538:     }
```

## A07: Projeto/src/main/java/pt/uminho/esr/ott/config/StreamConfiguration.java

```text
8: public record StreamConfiguration(Path sourceFile, UUID streamId, String serverId,
9:                                   String title, Path mediaFile, int fps, boolean loop)
10: {
11: 
12:     //* === Construtor === *
13: 
14:     public StreamConfiguration
15:     {
16:         Objects.requireNonNull(sourceFile);
17:         Objects.requireNonNull(streamId);
18:         Objects.requireNonNull(serverId);
19:         Objects.requireNonNull(title);
20:         Objects.requireNonNull(mediaFile);
21:     }
22: }
```

## A07: Projeto/src/main/java/pt/uminho/esr/ott/config/NodeConfiguration.java

```text
14: public record NodeConfiguration(
15:         Path sourceFile, String nodeId, NodeRole role, String bindHost,
16:         String advertisedHost, int controlPort, int dataPort,
17:         Optional<BootstrapContact> bootstrap, Optional<Path> topologyFile,
18:         Optional<Path> streamsFile, Optional<Path> mediaBase,
19:         Optional<TopologyConfiguration> topology, List<StreamConfiguration> streams,
20:         RuntimeConfiguration runtime)
21: {
22: 
23:     //* === Construtor === *
```

```text
50:     public NodeConfiguration(
51:             Path sourceFile, String nodeId, NodeRole role, String bindHost,
52:             String advertisedHost, int controlPort, int dataPort,
53:             Optional<BootstrapContact> bootstrap, Optional<Path> topologyFile,
54:             Optional<Path> streamsFile, Optional<Path> mediaBase,
55:             Optional<TopologyConfiguration> topology, List<StreamConfiguration> streams)
56:     {
57:         this(sourceFile, nodeId, role, bindHost, advertisedHost, controlPort, dataPort,
58:                 bootstrap, topologyFile, streamsFile, mediaBase, topology, streams,
59:                 RuntimeConfiguration.defaults());
60:     }
61: 
62: 
```

## A07: Projeto/src/main/java/pt/uminho/esr/ott/config/RuntimeConfiguration.java

```text
186:     public record Source(int maxFrameBytes)
187:     {
188: 
189:         //* === Construtor === *
190: 
191:         public Source
192:         {
193:             range(maxFrameBytes, 1, 99999, "maxFrameBytes");
194:         }
195:     }
```

## A01/A07: Projeto/src/main/java/pt/uminho/esr/ott/config/ConfigurationLoader.java

```text
465:         List<String> ids = csv(p, file, "stream.ids", 256, errors);
466: 
467:         if(ids.size() < 2)
468:             error(errors, file, "stream.ids", "cada servidor deve configurar pelo menos dois fluxos");
469: 
470:         var allowed = new HashSet<>(Set.of("stream.ids"));
```

```text
503:             Path mediaFile = resolve(mediaBase, media, file, prefix + "file", errors);
504: 
505:             if(mediaFile != null)
506:             {
507:                 if(!mediaFile.startsWith(mediaBase) || mediaFile.equals(mediaBase))
508:                     error(errors, file, prefix + "file", "ficheiro deve estar dentro de media.base");
509: 
510:                 if(!mediaFiles.add(mediaFile))
511:                     error(errors, file, prefix + "file", "dois fluxos não podem ser aliases do mesmo ficheiro");
512:             }
513: 
514:             int fps = integer(p, file, prefix + "fps", 1, 60, errors);
515:             String loop = required(p, file, prefix + "loop", errors);
516: 
517:             if(loop != null && !loop.equals("true") && !loop.equals("false"))
518:                 error(errors, file, prefix + "loop", "booleano deve ser true ou false");
519: 
520:             if(errors.size() == before)
521:                 result.add(new StreamConfiguration(file, id, server,
522:                         title, mediaFile, fps, Boolean.parseBoolean(loop)));
523:         }
```

## A08: Projeto/src/main/java/pt/uminho/esr/ott/server/StreamingServer.java

Placeholder: 0 bytes.


## A02/A07: Projeto/src/main/java/pt/uminho/esr/ott/routing/Route.java

```text
114:     public record Confirmation(UUID streamId, String serverId, UUID serverEpoch,
115:             boolean localSource, Optional<Neighbor.Session> upstream, List<String> path,
116:             Map<String, Subscription> downstreams, Set<UUID> localConsumers,
117:             Optional<Subscription.Lease> upstreamLease, long revision) {
118:         public Confirmation {
119:             Objects.requireNonNull(streamId); RoutingTable.nodeId(serverId);
120:             Objects.requireNonNull(serverEpoch); Objects.requireNonNull(upstream);
121:             Objects.requireNonNull(upstreamLease);
122:             path = List.copyOf(path); downstreams = Map.copyOf(downstreams);
123:             localConsumers = Set.copyOf(localConsumers);
124:             if (revision < 0 || path.isEmpty() || path.size() > 32
125:                     || !path.getFirst().equals(serverId) || Set.copyOf(path).size() != path.size()
126:                     || localSource != upstream.isEmpty() || localSource != upstreamLease.isEmpty()
127:                     || localSource && path.size() != 1 || !localSource && path.size() < 2
128:                     || downstreams.size() > 63 || localConsumers.size() > 4)
129:                 throw new IllegalArgumentException("Inconsistent forwarding confirmation");
130:             for (String id : path) RoutingTable.nodeId(id);
131:             if (upstream.isPresent() && !path.get(path.size()-2).equals(upstream.orElseThrow().nodeId()))
132:                 throw new IllegalArgumentException("Upstream does not match confirmed path");
133:             for (var e : downstreams.entrySet()) {
134:                 var child = e.getValue();
135:                 if (!e.getKey().equals(child.child().nodeId()) || !streamId.equals(child.streamId())
136:                         || !serverEpoch.equals(child.serverEpoch()) || child.state() != Subscription.State.ACTIVE
137:                         || path.contains(e.getKey()) || child.expectedPath().size() != path.size()+1
138:                         || !child.expectedPath().subList(0,path.size()).equals(path))
139:                     throw new IllegalArgumentException("Inconsistent forwarding child");
140:             }
141:         }
142:         /** Ages immutable grants; never renews them or mutates the owner's state. */
143:         public Optional<Confirmation> visibleAt(long now) {
144:             if (upstreamLease.isPresent() && upstreamLease.orElseThrow().remainingNanos(now) == 0)
145:                 return Optional.empty();
146:             var children = new LinkedHashMap<String,Subscription>();
147:             downstreams.forEach((id,s) -> {
148:                 if (s.validity().orElseThrow().remainingNanos(now)>0) children.put(id,s);
149:             });
150:             if (children.isEmpty() && localConsumers.isEmpty()) return Optional.empty();
151:             return Optional.of(new Confirmation(streamId,serverId,serverEpoch,localSource,upstream,path,
152:                     children,localConsumers,upstreamLease,revision));
153:         }
154:     }
```


## A02/A07: Projeto/src/main/java/pt/uminho/esr/ott/routing/Subscription.java

```text
28:             if(revision<0 || generation<0) throw new IllegalArgumentException("Invalid branch identity");
29:         }
30:     }
31:     // End IMP-012 types.
32: 
33:     /** A duration anchored before the request queue; safe across nanoTime wrap. */
34:     public record Lease(long beganNanos, long durationNanos) {
35:         public Lease {
36:             if (durationNanos < 1 || durationNanos > 0xffff_ffffL * 1_000_000L)
37:                 throw new IllegalArgumentException("Invalid lease duration");
38:         }
39:         public long remainingNanos(long now) {
40:             long age=now-beganNanos;
41:             if(age<0) throw new IllegalArgumentException("Monotonic lease horizon");
42:             return age>=durationNanos ? 0 : durationNanos-age;
43:         }
44:         public long remainingMillis(long now) { return remainingNanos(now)/1_000_000L; }
```


## A02/A06/A07: Projeto/src/test/java/pt/uminho/esr/ott/overlay/OverlayNodeRoutingTest.java

```text
30:     static final class Rig implements AutoCloseable {
31:         final NeighborManagerTest.Deployment ports=new NeighborManagerTest.Deployment();
32:         final List<OverlayNode> nodes=new ArrayList<>();final List<RoutingService> routers=new ArrayList<>();
33:         final List<NeighborManager.SerialOwner> owners=new ArrayList<>();
34:         final List<RoutingService.ControlPort> controls=new ArrayList<>();final List<UUID> instances=new ArrayList<>();
35:         final List<List<NeighborManager.Event>> events=new ArrayList<>();
36:         final AtomicReference<NeighborManager.SerialOwner> creatingOwner=new AtomicReference<>();
37:         final BootstrapServer bootstrap;
38:         Rig()throws Exception{
39:             ports.bootstrap.close();var bc=ports.bootstrapConfig;var topology=new LinkedHashMap<String,TopologyConfiguration.Node>();
40:             bc.topology().orElseThrow().nodes().forEach((id,n)->topology.put(id,new TopologyConfiguration.Node(id,n.host(),n.controlPort(),n.dataPort(),id.equals("N0")?NodeRole.SERVER:n.role(),n.neighbors())));
41:             bootstrap=new BootstrapServer(new NodeConfiguration(bc.sourceFile(),bc.nodeId(),bc.role(),bc.bindHost(),bc.advertisedHost(),bc.controlPort(),0,bc.bootstrap(),bc.topologyFile(),bc.streamsFile(),bc.mediaBase(),Optional.of(new TopologyConfiguration(Path.of("graph"),topology)),bc.streams(),bc.runtime()));
42:             for(int i=0;i<3;i++){
43:                 var c=i==0?source(ports.configs.get(i)):ports.configs.get(i);var log=new CopyOnWriteArrayList<NeighborManager.Event>();events.add(log);
44:                 nodes.add(new OverlayNode(c,UUID.randomUUID(),log::add,(cfg,id,owner,port,observer)->{
45:                     creatingOwner.set(owner);owners.add(owner);return new NetworkMonitor(cfg,id,owner,port,observer);
46:                 },(cfg,id,owner,port,observer)->{
47:                     controls.add(port);instances.add(id);assertSame(creatingOwner.get(),owner);assertEquals(cfg.runtime().overlay().eventsCapacity(),owner.capacity());
48:                     var r=new RoutingService(cfg,id,owner,(session,message)->{assertTrue(owner.isOwnerThread());return port.send(session,message);},observer);routers.add(r);return r;
49:                 }));
```

```text
93:     @Test void d24RoutingFactoryFailureRollsBackPreviousComponents()throws Exception{
94:         var owner=new AtomicReference<NeighborManager.SerialOwner>();var node=new OverlayNode(NeighborManagerTest.config("A",19001),UUID.randomUUID(),e->{},
95:             (c,id,o,p,v)->{owner.set(o);return new NetworkMonitor(c,id,o,p,v);},(c,id,o,p,v)->{throw new IllegalStateException("routing factory fixture");});
96:         assertThrows(ExecutionException.class,()->node.start().toCompletableFuture().get(1,TimeUnit.SECONDS));assertTrue(node.failure().isPresent());assertTrue(node.awaitTermination(Duration.ofSeconds(2)));assertTrue(owner.get().awaitTermination(Duration.ZERO));node.close();
97:     }
98:     @Test void d27OldConstructorAndSnapshotsBeforeStartAndAfterClose(){
99:         var node=new OverlayNode(NeighborManagerTest.config("A",19001),UUID.randomUUID(),e->{});assertTrue(node.routingSnapshot().routes().isEmpty());node.close();assertTrue(node.routingSnapshot().catalog().entries().isEmpty());
100:     }
101:     @Test void d24SharedDeadlineIncludesRoutingPhysicalResourcesAndGracefulLeave()throws Exception{
102:         try(var rig=new Rig()){
103:             rig.start();var node=rig.nodes.get(0);var result=node.requestShutdown(0,Duration.ofSeconds(2));assertSame(result,node.requestShutdown(1,Duration.ofSeconds(30)));
104:             var report=result.toCompletableFuture().get(2,TimeUnit.SECONDS);assertEquals(2,report.attempts().size());assertTrue(report.attempts().stream().allMatch(a->a.outcome()==NeighborManager.LeaveOutcome.LOCAL_WRITE_CONFIRMED));
105:             assertTrue(node.awaitTermination(Duration.ofSeconds(2)));assertTrue(rig.routers.get(0).awaitTermination(Duration.ZERO));assertEquals(RoutingService.Phase.STOPPED,node.routingSnapshot().diagnostics().phase());
106:         }
107:     }
```


## A03/A04/A07: Projeto/src/test/java/pt/uminho/esr/ott/routing/RoutingServiceTest.java

```text
36:     static final class Owner implements NeighborManager.SerialOwner {
37:         final ArrayDeque<Runnable> queue=new ArrayDeque<>(); final List<Runnable> hooks=new ArrayList<>();
38:         final int capacity; boolean inside,stopped,reject,inline; Throwable failure;
39:         Owner(int capacity){this.capacity=capacity;}
40:         public int capacity(){return capacity;} public boolean isOwnerThread(){return inside;}
41:         public boolean offer(Runnable r){if(stopped)return false;if(reject||queue.size()==capacity){fail(new IllegalStateException("full"));return false;}queue.add(r);if(inline)drain();return true;}
42:         public NeighborManager.Registration onStop(Runnable r){hooks.add(r);return ()->hooks.remove(r);}
43:         public void fail(Throwable t){failure=t;requestClose();}
44:         public void requestClose(){if(stopped)return;stopped=true;queue.clear();run(()->{for(var h:List.copyOf(hooks))h.run();});}
45:         public boolean awaitTermination(Duration d){return stopped;} public void close(){requestClose();}
46:         void run(Runnable r){boolean old=inside;inside=true;try{r.run();}finally{inside=old;}}
47:         void drain(){int n=0;while(!queue.isEmpty()){assertTrue(n++<10000,"owner busy loop");run(queue.remove());}}
```

```text
86:         try(var r=new Rig("S",true,4096,4096,10,new RoutingTable.Limits(64,256,63,8))){
87:             var p=session("P");r.up(p);assertEquals(2,r.to(p).size());assertEquals(2,r.router.snapshot().catalog().entries().size());
88:             var a=(ControlMessage.Announcement)r.to(p).getFirst().payload();assertEquals(0,a.announceSeq());assertEquals(LOCAL,a.stream().serverEpoch());assertEquals(List.of("S"),a.path());
89:             r.tick(20000);assertEquals(4,r.to(p).size());assertTrue(r.to(p).subList(2,4).stream().allMatch(m->((ControlMessage.Announcement)m.payload()).announceSeq()==1));
90:             r.act(()->r.router.setLocalAvailable(STREAM,false));assertTrue(r.to(p).stream().anyMatch(m->m.type()==MessageType.STREAM_WITHDRAW));
91:             r.act(()->r.router.setLocalAvailable(STREAM,true));assertTrue(r.to(p).stream().filter(m->m.payload() instanceof ControlMessage.Announcement a2&&a2.stream().streamId().equals(STREAM)).map(m->((ControlMessage.Announcement)m.payload()).announceSeq()).max(Long::compare).orElseThrow()>1);
92:         }
93:     }
94:     @Test void d02D03DiamondBetterArrivalAndAlternativeDespiteCacheHit(){try(var r=new Rig()){
```


## A06/A07: Projeto/src/test/java/pt/uminho/esr/ott/app/OttApplicationTest.java

```text
21:     static final class FakeService implements OttApplication.RunningService {
22:         final List<Duration> budgets = new ArrayList<>();
23:         int stops, closes, snapshots; boolean terminated = true; int reason = -1;
24:         public Map<String, Neighbor> neighbors() { snapshots++; return Map.of(); }
25:         public void requestShutdown(int reason, Duration budget) { stops++; this.reason = reason; budgets.add(budget); }
26:         public boolean awaitTermination(Duration budget) { budgets.add(budget); return terminated; }
27:         public void close() { closes++; }
28:     }
29:     static final class Harness {
30:         final AtomicLong clock = new AtomicLong(1000);
31:         final AtomicInteger opens = new AtomicInteger();
32:         final List<NodeConfiguration> configs = new ArrayList<>();
33:         final FakeService service = new FakeService();
34:         final ByteArrayOutputStream output = new ByteArrayOutputStream(), error = new ByteArrayOutputStream();
35:         boolean failOpen;
36:         int run(String input, String... arguments) {
37:             return assertDoesNotThrow(() -> OttApplication.run(arguments,
38:                 new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
39:                 new PrintStream(output, true, StandardCharsets.UTF_8), new PrintStream(error, true, StandardCharsets.UTF_8),
40:                 config -> { opens.incrementAndGet(); configs.add(config); if (failOpen) throw new IOException("controlled resource failure"); return service; }, clock::get));
41:         }
```

```text
455:         }
456:     }
457: 
458:     @Test void m12ServiceWaitReceivesOnlyTheRemainingGlobalBudget() {
459:         var clock = new AtomicLong(1000); var budgets = new ArrayList<Duration>();
460:         var service = new OttApplication.RunningService() {
461:             public Map<String, Neighbor> neighbors() { return Map.of(); }
462:             public void requestShutdown(int reason, Duration budget) { budgets.add(budget); clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1000)); }
463:             public boolean awaitTermination(Duration budget) { budgets.add(budget); clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(500)); return true; }
464:             public void close() { fail("No second complete timeout or duplicate close"); }
465:         };
466:         var output = new ByteArrayOutputStream();
467:         var result = OttApplication.execute(new String[]{"--role", "RELAY", "--config", "src/main/resources/config/relay.properties"},
468:             new ByteArrayInputStream("quit\n".getBytes(StandardCharsets.UTF_8)), new PrintStream(output), new PrintStream(output),
469:             c -> service, clock::get, () -> {}, () -> {});
470:         assertEquals(0, result.exitCode()); assertTrue(result.inputTerminated()); assertTrue(result.outputTerminated()); assertTrue(result.serviceTerminated());
471:         assertEquals(List.of(Duration.ofMillis(3000), Duration.ofMillis(2000)), budgets);
472:     }
473: 
474:     @Test @Timeout(15) void m15ClosingFromSerialCallbackNeverWaitsForItsOwnOwner() throws Exception {
475:         try (var d = new LiveDeployment(); var boot = new pt.uminho.esr.ott.bootstrap.BootstrapServer(
476:                 pt.uminho.esr.ott.config.ConfigurationLoader.load(d.profiles.getFirst(), NodeRole.BOOTSTRAP))) {
477:             var reference = new AtomicReference<OverlayNode>(); var once = new AtomicBoolean();
```

