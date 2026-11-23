# Construction log — unidade005-video

## IMP-016 / 010-video — Plan formal preparado

Registo 2026-11-23T14:41:11Z: utilizador aceitou tecnicamente proposta/API/imutabilidade/
validação/oráculos; autorizou gravação de Plan/documentação/evidência e patch
exato de uma linha no template,após confirmar colega fora deste diretório.
Ramo imp-016-udp criado explicitamente a partir de b6de848; main conserva esse ref.
[Plan](../../../../bolts/010-video/implementation-plan.md) formal por aprovar;
sem etapas concluídas,Implement/Test/Java/testes Java/staging/commits/push.
Dependência009-arvores/unidade004/PHASE-04 fechada; DEMO-001 é apoio separado,
não um gate. demo/artefactos/negativos preservados. IDs IMP-016-LOCAL,sem reserva
Q-C global. [V-ADD/diff/preservação](../../../../../experiments/construction/010-video/plan-01/README.md).
V-019 e regressão/manual/revisão/integração futuros,sem gates externos adicionais.

## 2026-11-23T14:54:00.061317+00:00 — Plan aprovado / primeiro passo Implement

Autorização do utilizador: tipos/interfaces e testes vermelhos, sem comportamentos.
Novos76/74/0/0; históricos TCP/discovery120/0/0/0. Compilação passou.
[Walkthrough](../../../../bolts/010-video/implementation-walkthrough.md).
Sem Test/staging/commits/push; critérios e fecho pendentes. IDs locais/demo preservados.

## 2026-11-23T15:02:58.277544+00:00 — Comportamentos autorizados / Implement por aceitar

Codec/fragmentação nos dois caminhos aprovados. Primeiro verde76/0/0/0; regressão
final JDK21 1537/0/0/0,22 XML, incluindo78 casos U01–U08/V-019. Execução restrita
com sockets bloqueados preservada; repetição fora da sandbox passou sem alterações.
[Walkthrough](../../../../bolts/010-video/implementation-walkthrough.md).
Main/demo/históricos/negativos preservados; IDs locais, target/ separado.
Test não autorizado; clean verify/JAR/manual/Denji/integração pendentes.
Sem staging/commits/push ou gates externos adicionais.

## 2026-11-23T15:16:20Z — Implement aprovado / checkpoint Test

Utilizador aprovou Implement e autorizou Test. Clean verify atual1537/0/0/0,
22 XML;78 U01–U08/V-019. JAR preservado SHA-256
588bd8e5f85c880666beab1a2033c8f100f3bd37e79d2d83ed4d15ec322ac3a2.
[Test](../../../../bolts/010-video/test-walkthrough.md). Código aprovado intacto;
negativos,históricos,fixtures,demo/main preservados. Manual preparado,não executado.
Test por aprovar; manual humano,Denji e integração pendentes. IDs locais,
target/ separado; sem staging/commits/push/fecho ou gates externos adicionais.

## 2026-11-23T15:35:42Z — Manual humano Francisco24 cumprido e conferido

Utilizador confirmou execução pessoal das oito etapas,sem divergências observadas.
24 CHECK_PASS,8 confirmações,PROBE_PASS/MANUAL_PASS,CLEANUP_COMPLETE,
JAVA_EXIT=0,PASSED=true,WRAPPER_EXIT=0 conferidos nos originais. SHELL_EXIT=0
é confirmação explícita do utilizador,não consta do log/JSON. Hashes JAR/código/
fixtures/auxiliares íntegros; origem das classes no JAR confirmada; JVM terminada
pelo wait do runner e pasta temporária ausente. Sem repetir build/manual.
[Conferência](../../../../../experiments/construction/010-video/manual-verification-01/README.md).
Test reapresentado por aprovar; Denji e integração pendentes. Sem staging/commits/
push/fecho,IDs locais,imp-016-udp,demo/main preservados,target/ excluído.

## 2026-11-23T15:39:17Z — Test aprovado / revisão final Denji confirmada

Utilizador aprovou clean verify1537/0/0/0,22 XML,V-ADD e manual Francisco24,
sem divergências observadas. Confirmou revisão final Denji da implementação atual,
sem alterações pendentes. [Confirmação e hashes código/JAR](../../../../../experiments/construction/010-video/final-package-01/final-review.json).
[Pacote/plano/simulação](../../../../../experiments/construction/010-video/final-package-01/README.md).
Main local continua b6de848,sem divergência entre ramos; remotos não consultados.
Sem repetir build/manual; integração e fecho pendentes. Bolt/história/unidade abertos,
IDs locais,demo preservada,target/ excluído,sem staging/commits/push/fecho.

## 2026-11-23T16:02:19Z — Fecho documental autorizado / IMP-016 concluído

Utilizador autorizou complete/implemented=true/TASK-019 DONE após commit e
fast-forward local em main72c9f92. Test1537/0/0/0,22 XML,V-019/U01–U08,
manual pessoal Francisco24 e revisão final Denji cumpridos/aprovados.
[Fecho](../../../../bolts/010-video/closure-walkthrough.md),
[commit](../../../../../experiments/construction/010-video/commit-verification-01/README.md),
[integração](../../../../../experiments/construction/010-video/integration-verification-01/README.md).
Unidade005/PHASE-05 em progresso;18 histórias/bolts complete,0 in-progress,17 planned.
Sem novo IMP,alterações Java,push ou target/ no commit. IDs locais,demo/negativos/
logs pessoais preservados; documentação/evidência apenas. V-ADD no fecho.
