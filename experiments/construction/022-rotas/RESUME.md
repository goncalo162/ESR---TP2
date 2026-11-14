# Retoma — IMP-010 / 022-rotas / TASK-013

Validação interrompida por pedido explícito do utilizador, registado em 2026-11-14T17:53:17Z.
Autorizado apenas commit WIP local em main; sem push, fecho ou outro IMP.

## Estado guardado

- Base/HEAD antes do WIP:9144707eb5622d9da43b3b07354bc838c2797ef8, main.
  WIP não é integração final nem revisão de Fillmore; não inventar o hash do
  próprio commit neste documento. Obtê-lo por git log/mostrar ao utilizador.
- Plan e Implement aprovados. current_stage=test, stages_completed só
  Plan/Implement; bolt in-progress, completed=null. Test **não aprovado**.
- História in-progress, sem implemented=true; TASK-013 IN PROGRESS.
  Unidade003-rotas e PHASE-03 mantêm-se em progresso.
- clean verify em Projeto/ executado: **1296 testes/0falhas/0erros/0ignorados,
  exit0;17 XML novos**,1256 históricos +40 de trace. [Resultados](test-build-01/result.json)
  e [log](test-build-01/maven.log). É execução técnica, não manual humano.
- [JAR fixo](test-build-01/ott-streaming-1.0-SNAPSHOT.jar):436556bytes,238 classes
  Java21/major65, sem testes empacotados. SHA-256:

  ```text
  5f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52
  ```

- Código aprovado e POM iguais aos hashes de Implement; sem alterações Java em
  Test/WIP. Quatro caminhos autorizados: RoutingService.java, OttApplication.java,
  OverlayNode.java (só delegação mínima), RouteTraceTest.java. Preservar Q-C-32/33,
  NeighborManager/send(), testes históricos,TCPv2/19 tipos/código3 rejeitado,
  framing/caps e UDP. [Prova](test-preservation.json).
- target/ excluído do WIP, incluindo listas tracked geradas; não remover nem
  reverter alterações alheias. Cópias de JAR/XML/logs em experiments/ são evidência.

## Falha histórica e execuções parciais

A regressão02 de Implement teve1 falha em
m13PhysicalIoJoinsAreProvedBeforeJvmExitWithBlockedOutput: a JVM não terminou
no prazo6s. **Causa não estabelecida.** Preservar [log/XML/artefactos](implement-behavior/regression-02/result.json),
sem atribuir causa ou aumentar prazos/enfraquecer testes. Passou depois isolada,
na regressão03 e no clean verify deste JAR. Se reaparecer, investigar antes
de apresentar Test para aprovação.

- [Primeiro ensaio do agente](manual-runs/20261114T173427Z-ni76vufu/agent-driver-error.json):
  parcial/falhado, preservado integralmente. Esperava NO_ROUTE com S vivo após
  perder relays; reparação IMP-035 criou C–S e trace [C,S] correto.
- [Correção apenas da sequência](manual-observation-fix.json): sair de S antes
  do último relay, mantendo NO_ROUTE, código/JAR/config/prazos inalterados.
- [Segundo ensaio kind=agent](manual-runs/20261114T174144Z-am0q0t63/report.json):
 34 CHECK_PASS/MANUAL_PASS, quatro Java0 e um SIGKILL intencional
 (return=-9/sinal9/shell137),processos recolhidos,portas livres,0UDP no intervalo.
 **Não substitui o manual humano nem aprova Test.**
- Nesta pausa foram encontrados apenas estes dois runs kind=agent; não foi
  recebida execução humana. Preservar qualquer run pessoal/parcial que apareça,
  sem editar logs nem inferir aprovação a partir de saídas0 ou de MANUAL_PASS.
- [Verificação de processos](wip/process-check.json): nenhum processo próprio
  do manual ativo nesta observação; sidecars de saída e watcher preservados.
  Não foi necessário enviar quit adicional; não se terminaram processos alheios.

## Passos para retomar

1. Ler este RESUME, [Test](../../../memory-bank/bolts/022-rotas/test-walkthrough.md)
   e [manual](manual-instructions.md); conferir HEAD/main, git status e hashes.
   Ler o commit WIP, sem o confundir com integração final ou aprovação de Test.
2. Conferir o JAR fixo e fontes contra test-preservation.json/manual-tools/frozen.json.
   Não reconstruir/copiar de target alegando nova evidência. Se código/JAR mudar,
   obter a autorização aplicável e revalidar essa versão antes de aceitar Test.
3. Executar manual **humano** num RUN novo com operador real e --kind human.
   Não sobrescrever os runs anteriores. Reconfirmar portas20880..20884 TCP e
   20981..20984 UDP. O roteiro inclui logs próprios,códigos de saída,watcher UDP
   e verificação de hashes/portas. Matar só o relay escolhido do próprio run.
   Retirar S antes do último relay para NO_ROUTE, sem desativar IMP-035.
4. Se houver execução parcial, confirmar os passos em falta com o operador e
   preservar os ficheiros originais; encerrar os seus processos com quit antes
   de abandonar. Sem aprovação por inferência.
5. Conferir report.json/logs e obter confirmação humana, depois aprovação de Test.
6. Só depois: revisão final de Fillmore e integração da versão revista, com
   autorização própria de staging/commit. Não marcar TASK/bolt/história DONE ou
   complete enquanto faltar revisão/integração e fecho documental autorizado.

Sem gates externos adicionais. Sem push nem início de outro incremento.
Mensagem autorizada do WIP:
`wip(IMP-010/TASK-013): guardar trace e validacao tecnica para retoma`
