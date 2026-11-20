# IMP-013 / TASK-016 — revisão final Fillmore confirmada

Test1419/0/0/0,20 XML,V-016/R01–R18 e manual humano Luis7etapas/28checks
aprovados explicitamente pelo utilizador. Fillmore reviu e aprovou a mesma
versão sem alterações, conforme confirmação explícita. Não se contactou pessoas
nem se presumiu aprovação pela etiqueta do relatório.

Três caminhos: Route.java,RoutingService.java,RecoveryTest.java. A01–A08 e
correção R10 preservam intenção admitida, novidade G/watermark, T/A/cadência,
correlação/lineagens/revisões, construtores4/5 e reservas físicas entre gerações.
Métrica10→2000→10 não reutiliza prova bloqueada; primeiro valor elegível e ANN
novo posterior à falha continuam válidos.40 testes novos,1379 históricos intactos.

[Versão vinculada à revisão](../../../experiments/construction/008-arvores/integration-01/review-confirmation.json),
[manual Luis original](../../../experiments/construction/008-arvores/test-manual-runs/20261120T161206Z-w1jlv9q6/report.json)
e [conferência humana](../../../experiments/construction/008-arvores/manual-human-verification-01/verification.json).
28 CHECK_PASS,CLEANUP_COMPLETE,MANUAL_PASS28,JAVA_EXIT0,passed=true;
fontes/JAR/ferramentas/origem/JVM conferidos. WRAPPER_EXIT não fornecido,não inferido.

JAR504997bytes,SHA25681779be388778fd7dfacba5957e74ccffe5da45e86a403ea1ec5626f52c8f49d,
267 classes do clean build. Código/JAR/manuais/negativos não serão alterados nem
reexecutados nesta integração. R17 inicial continua sem causa estabelecida;
o sucesso com métricas controladas não demonstra a causa. Sem vídeo/GUI/CORE,
SLA/FPS ou recuperação de falha exclusiva UDP alegados.

Commit/push autorizados. Integrar por avanço rápido de main/Denji a partir de
40592da, comprovar o commit exportado e só depois marcar bolt/história/TASK DONE.
Unidade004/PHASE-04 continuam em progresso e nenhum outro IMP é iniciado.

## Fecho após integração comprovada

Registo 2026-11-20T16:23:16Z: Test/manual Luis7/28 e revisão Fillmore aprovados;
integração main 447ae4bf68bf96329bd1a0c7e16ed20df06bc55b comprovada.
Bolt/história complete,implemented=true,TASK-016 DONE; unidade/fase em progresso.
Negativos e limitação R17 preservados,sem causa estabelecida. Nenhum outro IMP
iniciado;024 apenas desimpedido. Commit/push autorizados,sem Java/build/manual novo.
[Fecho](closure-walkthrough.md).
