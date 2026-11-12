---
unit: "003-rotas"
bolt: "005-rotas"
stage: "adr"
status: "in-progress"
created: "2026-11-12T14:48:27Z"
---
# IMP-007 — análise ADR para aprovação

## Âmbito e autoridade

Registo efetuado em 2026-11-12T14:48:27Z, não data atribuída à revisão humana.
O utilizador aprovou o Design atual e a allowlist exata de cinco ficheiros e
confirmou que Fillmore reviu **esse Design**. Esta confirmação não é revisão
final do código: implementação ainda não iniciada. Q-C-23 opção1 e Q-C-24
mantêm a aprovação desta retoma, sem reutilizar decisões da retoma cancelada.
Base e HEAD: 3dedf007f52f43f53eaf254bef7c25e35f986e5a, main.

Fontes: ADD secções2/5/6/8–12; Model e Design aprovados; docs/07 contrato atual;
índice de decisões (registos históricos subordinados às substituições aceites
no ADD). IMP-006/IMP-035 e unidade002-overlay mantêm complete. A análise é
estática/documental; não executa V-010, Java, Maven, serviços ou medições.

## Reutilização das decisões aceites

| Decisão | Conformidade do Design aprovado | Prova futura exigida |
|---|---|---|
| ADR-001/007 | Java21/Maven, JDK apenas, records/cópias defensivas; nenhum runtime novo | compilação/regressão; D01/D16 |
| ADR-002/012/021 | mesmo TCP/codec; Probe7/8 existente; validação de contexto/identidade, sem ObjectInputStream ou mensagem nova | M01–M22, D08, integração v2 |
| ADR-004 | RTT monotónico local/EWMA; custo mínimo1µs só com amostra, ceil da EWMA sem arredondar passos; não implementa routing | D02/D09/D14, V-010 |
| ADR-016 | I/T/alpha/F efetivos validados; N/B derivados de caps existentes, sem chave ou override de wire/política | D01/D04/D09/D11 |
| ADR-014/015 | ACCEPTED só admissão, nunca write/flush ou ACK; uma pendência antes de send, earlyPong só vira amostra após ACCEPTED; cancelamento não desfaz bytes | D05–D07/D13 |
| ADR-017 | mesmo owner/manager; helper só oferece wakeup genérico; atomics só para reservas físicas; callbacks não bloqueiam e snapshots imutáveis | D11–D13/D16 e integração |
| ADR-018/019, com delta035 | Session integral e geração; reset por connectionId mesmo instanceId; callbacks velhos não contaminam nova sessão; manager conserva admissão/reconexão | D03/D08 e integração HELLO5/REPAIR_HELLO20 |
| ADR-020 | CLOSED/OVERFLOW e falhas locais não incrementam perdas remotas; INVALID_MESSAGE local/erro inesperado são diagnósticos fatais locais; EOF invalida imediatamente; não gera ERROR novo | D06/D09/D10/D13 |
| ADR-020 + lifecycle aceite IMP-005 | parar admissão monitor antes de graceful leave; primeiro t0/budget único, nenhuma renovação; pedir todos os fechos mesmo se um falhar, esperar fora do owner e sem locks | D15 e integração shutdown parcial/callback |
| ADR-022 | snapshot programático e diagnóstico bounded; timestamp/idade monotónicos locais, sem atraso unidirecional presumido nem CLI novo | D02/D14/D16; manual posterior |
| ADR-024 | um IMP, allowlist aprovada, dependências satisfeitas, revisão separada e checkpoint ADR; não reabre fechos nem alarga âmbito | V-ADD e auditoria SHA256 |
| ADR-025 | deteção TCP/sondas, sem watchdog UDP, feedback ou rerouting novo | V-010 não prova saúde UDP; limitação conservada |
| ADR-026 | sem CI/linter/JaCoCo/limiar, regressão e testes significativos continuam exigidos | clean verify futuro, sem total presumido |
| ADR-033 residual/034/035 | controlo TCP operacional exclusivamente v2; 19 códigos ativos1,2,4..20, código3 rejeitado; HELLO5/20/ACK6 máscaras3; cache/repair/FIFO/writer e isolamento por peer intactos | fixtures históricas/regressão e integração próprias preservadas |

## Verificações concretas de compatibilidade

1 - **Composição Q-C-23:** apenas NetworkMonitor.java, LinkMetrics.java,
NetworkMonitorTest.java, OverlayNode.java e OverlayNodeMonitoringTest.java, nos
[caminhos exatos do Design](ddd-02-technical-design.md#1-âmbito-e-allowlist-exata-de-implementação-futura).
Não altera NeighborManager/send, transporte, configuração, CLI ou testes históricos.
Tap entrega o mesmo evento original uma vez/por ordem, via finally mesmo se falhar;
ERROR/Diagnostic/RepairStarted e restantes tipos continuam no observador original.
Falha do tap é erro local e não oculta o evento corrente.

2 - **Limites físicos:** N=min(63,maxConnections), B=min(eventsCapacity,2*N),
validados antes de alocar; N estados/pendências/fechos lógicos, um earlyPong por
pendência, B tickets send e um close reservado. Tickets retirados logicamente
continuam contados até terminar callback físico. Um helper, até dois jobs físicos
(timer corrente e substituto), um wakeup queued e um running; nenhum histórico
ilimitado. Não aumenta filas, slots ou reservas físicas do manager/transporte.
Snapshot<=N e diagnóstico limitado. Saturação global continua erro operacional
observado; as quotas próprias não prometem impedir saturação dos serviços existentes.

3 - **Corridas Q-C-24:** guardar pending/t0 antes de send; RTT inclui filas até
tratamento serial do PONG; earlyPong captura no máximo um instante. Recusa descarta
a amostra; deadline em ADMITTING não conta perda antes de ACCEPTED; aceite tardio
não renova T. PONG no limite elapsed>=T vence uma vez. Session/geração/IDs protegem
callbacks velhos. Up dá elegibilidade inicial; uma pendência e cadência sem bursts.
Não comparar relógios de máquinas diferentes nem timestamps absolutos nanoTime.

4 - **Falhas locais:** own-capacity cheia antes do send adia sem perda remota;
echo excedente é descartado com diagnóstico limitado. CLOSED/OVERFLOW invalidam
só a Session esperada; INVALID_MESSAGE local e exceção inesperada apontam bug/recurso
local e acionam cleanup normal, nunca castigo remoto ou ERROR em resposta a ERROR.
Falha/timeout remoto é uma sonda admitida sem PONG no prazo; F consecutivas, não F
ticks. EOF/Down imediato. Falha exclusivamente UDP continua fora deste mecanismo.

5 - **Shutdown:** requestClose lógico não prova join físico; callbacks/timers
retidos mantêm tickets e podem levar awaitTermination a devolver false. Monitor
não fecha owner emprestado. Node pede stop monitor e manager com restante do prazo
original; joins externos usam só restante, sem self-join, locks ou short-circuit
que deixe recursos por solicitar. NODE_LEAVE e reserva de cleanup anteriores
preservados. Hook do owner cobre rejeição/arranque parcial; repetição é idempotente.

6 - **Wire/caps:** framing37..65536 e UTF-8 estrito, UDP OTT1 cabeçalho64/total<=1200/
payload<=1136 intactos. Nenhuma codificação de Reason/State local na rede ou enum
wire por ordinal. Não interpreta referências históricas a TCPv1/18tipos no índice
como contrato operacional atual: prevalecem ADR-034/035 e 19tipos do ADD atual.

## Divergências e decisão desta análise

Nenhuma divergência concreta face a ADR aceite ou inviabilidade identificada na
análise documental. Q-C-23 é a exceção de composição já autorizada e incorporada
no ADD/contratos; Q-C-24 concretiza tempo/reset/cadência aprovados, não requisito
inventado do enunciado. Limites derivados e política de falhas concretizam o
Design aprovado dentro dos mecanismos existentes. Não há nova tecnologia,
transporte, comando, mensagem, hard cap ou política arquitetural substituída.
**Nenhuma ADR nova criada, substituída ou promovida por inferência.** Índice de
decisões recebe apenas referência de reutilização, sem incrementar catálogo.

Conformidade documental não é prova de viabilidade executada: D01–D16/M01–M22,
integração própria e regressão têm de provar as garantias. Se Implement revelar
inviabilidade real, parar a parte afetada, guardar evidência/impacto e propor
substituição segundo ADD11; aguardar aprovação sem contornar a decisão aceite.

## Checkpoint ADR

Model/Design aprovados; revisão de Fillmore limitada ao Design registada.
ADR analisada, **aprovação desta etapa pendente**; Implement/Test não iniciados.
V-010, regressão atual, manual humano antes de aprovar Test, revisão final do
código e integração continuam pendentes. Sem gates externos adicionais.
Sem Java, staging, commits, push ou outro IMP. Parar aqui para aprovação.
