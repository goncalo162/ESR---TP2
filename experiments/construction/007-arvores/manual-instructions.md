# IMP-011 / V-014 — manual humano reproduzível

**Pendente de execução humana e de aprovação de Test.** O ensaio Codex está
identificado como `kind=agent`; não substitui a execução de uma pessoa.

## Construção e âmbito

JAR fixo: [ott-streaming-1.0-SNAPSHOT.jar](test-02/ott-streaming-1.0-SNAPSHOT.jar),
486734 bytes. SHA-256:
`f923f5b766f4ddcc3871c6339710a4a0e87a609aa6cb5ce1e6620c73ccc55314`.
Foi produzido pelo clean verify1328/0/0/0, JDK Temurin21.0.13+9/Maven3.9.9.
[Manifesto congelado](test-manual-manifest.json): fontes aprovadas, JAR e ferramentas.

Este roteiro usa o RoutingService real do JAR, com owner, relógio e porta de
envio controlados. Cinco etapas interativas, sem JUnit, sem reflexão e sem usar
Projeto/target. A operação interna de cancelamento corre no owner do harness.
Não há sockets nem portas/perfis a configurar neste roteiro. Não é CLI play/stop,
vídeo, GUI, CORE, medição de RTT/FPS ou poda/renovação completa (IMP-012).
A integração em TCP real foi executada separadamente pelo agente e está em
[test-02](test-02/README.md); não se apresenta esse ensaio como manual humano.

## Comando completo (um terminal, a partir da raiz do repositório)

Confirma JDK21 e substitui Francisco pelo nome efetivo do operador, se diferente.
O caminho de JDK abaixo foi conferido nesta máquina; noutra máquina usa o seu JDK21.

```bash
python3 -B experiments/construction/007-arvores/test-manual-tools/run-manual.py --operator Francisco --kind human --java-home /Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
```

O wrapper cria uma pasta **nova** em test-manual-runs/ e mostra o seu caminho.
Não reutilizes a pasta do agente. Antes/depois confere hashes de fontes, JAR e
auxiliares, compila fora de target, regista o PID e confirma a origem da classe
RoutingService no JAR fixo. Guarda compile.log, manual.log e report.json com
operador/tipo declarados, comandos e JAVA_EXIT. A identidade humana exige a tua
confirmação posterior; a etiqueta `human` não a comprova por si só.

Prima Enter antes de cada etapa e compare os snapshots/checks com o esperado.
EOF ou Ctrl+C interrompem e não contam como aprovação. Os rigs são fechados em
try-with-resources; o wrapper termina/recolhe a JVM se houver erro/interrupção.
Não precisa de quit: este auxiliar não abre processos do produto nem sockets.

| Etapa | Conferir na saída |
|---|---|
| 1 — S-A-R, C1/C2 | Pending fora de forwarding; uma ACTIVATE em cada tronco; ACK correlacionado por filho; grants6000→5500→4500 com tempo simulado; C3 reutiliza troncos |
| 2 — M07 | Antes/depois do cancelamento e do ACCEPTED tardio, só C2 autorizado; lease exato igual; send1 até conclusão, depois0; replay STALE_REVISION |
| 3 — Streams/validade | Cancelar A preserva B; grants5000/4000 não renovam;999999ns não concede1ms; expiração impede snapshots antigos de autorizar |
| 4 — Divergência | Dois prefixos devolvem PATH_CHANGED; ramo e lease iguais; zero ACTIVATE/DEACTIVATE adicional |
| 5 — Limites/fecho | Dois ACKs retidos; timeout não recicla reservas; await(0)=false até completar; depois zero sends/timers/forwarding |

Resultado técnico esperado: **24 CHECK_PASS**, `CLEANUP_COMPLETE`,
`MANUAL_PASS CHECKS=24`, `JAVA_EXIT=0`, `PASSED=true`. Verifica em report.json
`frozen_hashes_match=true`, `code_source_matches_jar=true` e
`java_process_terminated=true`. Qualquer falha ou etapa parcial fica registada,
sem aprovar manual/Test. Envia o caminho do report.json e a confirmação humana
com as divergências que observaste; não existe ainda execução humana a registar.

## Ensaio do agente, preservado separadamente

[Relatório Codex](test-manual-runs/20261117T230134Z-afcwucrm/report.json):24 checks,
JAVA_EXIT0, hashes/origem corretos e JVM terminada, kind=agent. Não é manual humano.
Depois da execução humana continuam necessárias aprovação de Test, revisão
final de Waheed e integração comprovada. Sem gates externos adicionais.

## Execução humana recebida e conferida

Francisco confirmou pessoalmente as cinco etapas, sem divergências, no
[relatório humano](test-manual-runs/20261117T233223Z-q2zk5pu4/report.json).
Conferidos24 CHECK_PASS, CLEANUP_COMPLETE, MANUAL_PASS CHECKS=24, JAVA_EXIT0,
PASSED=true, hashes/origem do JAR e terminação recolhida pelo wrapper.
[Verificação e revisão final](integration-01/manual-and-review-verification.json).
O utilizador confirmou também a revisão final de Waheed desta implementação,
sem alterações pendentes. Código e JAR iguais aos aprovados tecnicamente.
Test continua por aprovar; integração ainda não autorizada nem executada.
As menções anteriores a manual/revisão pendentes são histórico preservado.
