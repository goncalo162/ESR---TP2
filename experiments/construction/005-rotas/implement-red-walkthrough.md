# IMP-007 — interfaces/tipos e testes inicialmente vermelhos

Registo 2026-11-12T14:56:34Z. Base operacional3dedf007f52f43f53eaf254bef7c25e35f986e5a.
ADR aprovada pelo utilizador; autorização limitada ao primeiro passo Implement.

## Alterações nos cinco ficheiros autorizados

- monitoring/LinkMetrics.java: record, State local, opcionais e assinatura de idade;
  verificações null estruturais; validação cruzada/idade ficam por implementar.
- monitoring/NetworkMonitor.java: ControlPort/Observer/Factory/Scheduler/Ticket,
  eventos/razões locais, Diagnostics/Snapshot, construtores público/injetável,
  assinaturas start/onEvent/snapshot/requestClose/awaitTermination/close.
  Snapshot defensivo com cap63 antes de copiar. Sem scheduler real, threads,
  sockets, envio, mutação de sessões, timers ou cleanup operacional.
  start/onEvent são placeholders sem efeito, snapshot vazio NEW; operações de
  cleanup lançam UnsupportedOperationException explícita, não simulam sucesso.
- overlay/OverlayNode.java: import, overload package-private Factory e assinatura
  monitoringSnapshot com placeholder. Métodos históricos intactos; factory ainda
  não é ligada ao arranque (nem retida para sugerir integração pronta).
- monitoring/NetworkMonitorTest.java: 14 testes; owner manual com fila limitada,
  relógio/scheduler controlado e ControlPort com futures de admissão retidos.
- overlay/OverlayNodeMonitoringTest.java: 2 testes de contrato snapshot sem rede,
  antes de start e após close-before-start; integração TCP/eventos/budget futura.

Nenhuma remoção ou alteração em NeighborManager, send, testes históricos,
configuração, protocolo, transportes, POM ou recursos. Q-C-23/24 e Design intactos.

## Execução real e causas

Comando: mvn -B -ntp -f Projeto/pom.xml
-Dtest=NetworkMonitorTest,OverlayNodeMonitoringTest test (JDK21).
Resultado exit1: 16 testes,15 falhas de asserção,0 erros,0 ignorados; 1 passou
(cópia defensiva/impossibilidade de mutar snapshot).

- d02Age: assertDoesNotThrow falha porque idade é placeholder explícito.
- d16Rejects: falta validação cruzada que rejeite custo confiável zero.
- d09Down: falta evento Invalidated imediato.
- Dez casos de Up/RTT/reset/earlyPong/recusa/deadline/correlação/F/echo/cleanup
  falham na pré-condição observável: Up ainda não produz primeira sonda.
  As asserções seguintes estão escritas mas não foram atingidas. Não são prova
  isolada das corridas/cleanup/EWMA nem cobertura integral D01–D16/M01–M22.
- Os dois testes OverlayNode falham por monitoringSnapshot ainda não implementado.

Primeira tentativa: compilação falhou por usar Reason.CLOSED inexistente no teste;
corrigido para Reason.TRANSPORT existente, sem modificar enum histórico. Log
implement-red-attempt-01-compile.log preservado e excluído do resultado vermelho.
Segunda tentativa compila e executa JUnit, resultados acima; XML/TXT copiados de
target para experiments/construction/005-rotas/implement-red-surefire/.

Não executar clean verify ou declarar regressão/V-010/manual/revisão final nesta
etapa. Build criou/alterou artefactos em target, inclusive ficheiros tracked de
maven-status preexistentes: são gerados, listados na auditoria e excluídos do diff
de fontes/pacote. Não apagar/repor globalmente target nem alegar imutabilidade
byte a byte dos outputs após Maven. Evidência anterior não sobrescrita.

## Checkpoint

Interfaces/tipos e primeiros testes vermelhos prontos para revisão; comportamentos
não autorizados ainda. Implement/Test não aprovados; bolt aberto, história não
implementada. Fillmore reviu Design, não código final. V-010 integral, regressão,
manual humano antes de aprovar Test, revisão final do código e integração pendentes.
Sem gates externos adicionais, staging, commits, push ou outro IMP.
