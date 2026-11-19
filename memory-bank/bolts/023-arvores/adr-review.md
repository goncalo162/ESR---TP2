---
unit: "004-arvores"
bolt: "023-arvores"
stage: "adr"
created: "2026-11-18T16:15:43Z"
status: "complete"
---
# IMP-012 — revisão ADR, para aprovação

Model e Design aprovados pelo utilizador; Q-C-40–42 reconfirmadas. Apenas revisão
ADR autorizada. **Revisão ADR aprovada pelo utilizador. Apenas interfaces/tipos e testes
vermelhos autorizados; comportamentos e etapa Test não autorizados.**
Base main/b273b258a4642f94f8614ae00f060a46318d77a5, sem alteração Java/testes Java,
staging, commits/push ou outro IMP. Os1328 testes anteriores continuam históricos.

## 1. Conclusão e âmbito

A comparação do [Design aprovado](ddd-02-technical-design.md), ADD/contratos,
Q-C-34–42 e código real encontra **conformidade arquitetural estática** com as
ADR aceites. Nenhuma divergência exige alterar uma decisão aceite, âmbito,
wire, hard cap, biblioteca ou transporte. Não se cria ADR nova/substituta.
Isto não comprova implementação, segurança funcional em execução ou V-015.

Os handlers/estados atuais ainda não implementam renovação/poda: a diferença
entre código atual e entrega futura é trabalho previsto, não aprovação automática
de código ou motivo para reabrir as fundações fechadas. Os pontos concretos de
adaptação estão nas secções3–6; os três caminhos continuam a ser:

1 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/Subscription.java`.
2 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`.
3 - `Projeto/src/test/java/pt/uminho/esr/ott/routing/SubscriptionLeaseTest.java`.

O teste é novo, não criado nesta etapa. Não alterar Route, manager, monitor,
OverlayNode, CLI, codec/transporte/configuração/POM ou testes históricos.
A leitura de fonte nesta revisão foi estática; nenhum código foi modificado ou
executado. Evidência e hashes em experiments/construction/023-arvores/adr-01.

## 2. Reutilização das decisões aceites

| ADR / decisão | Confronto e condição de preservação |
|---|---|
| ADR-001/002/007 | Java21/Maven/biblioteca padrão, controlo TCP existente; nenhuma dependência runtime, serialização Java de rede ou novo transporte |
| ADR-004/005 | Um upstream confirmado e procura agregada por stream;14 renova explicitamente,13 poda com identidade exata e lease como salvaguarda; seleção e autorização continuam distintas |
| ADR-008/009/012 | Oráculos L01–L24 com relógio/port falsos e integração real posterior; codecs/fixtures/caps permanecem; nenhum resultado GUI/CORE presumido |
| ADR-013 | Bootstrap continua só descoberta inicial, sem membership/leases/renovações de registo;13/14 operam entre Sessions confirmadas |
| ADR-014/015 | ACCEPTED só enfileira; não é flush, ACK ou admissão remota. Poda não inventa confirmação13 nem usa outro port/writer; reservas e callbacks mantêm semântica |
| ADR-016 | subscription.lease/renew e activateTimeout existentes; P=min(renew,G/3), nanos positivos e budget restante concretizam o contrato, sem tornar caps configuráveis |
| ADR-017 | Mutação/remoção/publicação no owner existente, callbacks só publicam resultado e wake; sem executor/timer por pedido ou joins no owner |
| ADR-018 | Estado volátil e isolamento por stream/epoch/Session/ID/revisão/request/geração; reconexão não herda subscrição nem lease |
| ADR-019/033 | Não altera handshake, iniciador, aprendizagem/cache ou reconexão; códigos13/14 já passam pelo transporte de aplicação confirmado |
| ADR-020/021 | Erros tipados/limitados, validação antes de alocar, contexto seguro antes de responder, quota e shutdown global; sem ERROR sobre ERROR |
| ADR-022/023/024 | Evidência/limites honestos, incremento atómico, revisão Denji/integração futuras; sem contacto, publicação ou extensão de âmbito inferidos |
| ADR-025 | Nenhum watchdog ou recuperação por falha exclusiva UDP; indisponibilidade por falta de renovação não implementa troca de parent |
| ADR-026 | Sem CI/linter/JaCoCo/limiar percentual; regressão/documentação e oráculos pertinentes, sem executar Java nesta etapa |
| ADR-034/035 | Contrato operacional final v2,19 tipos1,2,4..20 e3 rejeitado, máscaras3; detalhes históricos18tipos/máscaras1 da ADR-034 não prevalecem sobre o delta aceite ADR-035 |
| Q-C-34–39 | Fronteira e fecho IMP-011 mantidos; M07, correções36/37, lambda38 e whitespace39 preservados. O esclarecimento CLI é o de Q-C-42, não uma extensão implícita |
| Q-C-40 | Mesmo subscriptionId, revisão crescente/request novo; poda da maior revisão submetida da geração retirada, correspondência exata remota, sem cancelar por aproximação |
| Q-C-41 | RENEW divergente recusa PATH_CHANGED e preserva grant anterior válido; não troca parent nem aumenta prazo por erro |
| Q-C-42 | Três ficheiros; ticket local/13 de rede, sem CLI ou porta nova em OverlayNode |

Nenhum estado de ADR é promovido/alterado nesta revisão. O índice derivado apenas
regista a reutilização. O Design foi aprovado; esta revisão ADR ainda aguarda
aceitação e não autoriza Implement.

## 3. Grant anterior e terminais M07

O código atual separa Tree.confirmed/Child.active de pedido pendente e usa
Line.terminal/identidade de Child para ignorar a conclusão de ACK cancelado.
O Design permite transferir o valor saudável anterior para o contexto de revisão
nova, sem copiar mapas ou tornar PENDING em destino. Essa transferência não
liberta a reserva do contexto físico antigo.

**Adaptações previstas no RoutingService, não alterações de ADR:**

- F01 — `wants(Tree)` atual serve ativação inicial (operação ainda pendente),
  não toda a procura contínua. Manter esse uso e acrescentar o predicado de
  procura efetiva para renovação, evitando renovar por terminal/histórico/send.
  startRound atual não inicia com confirmação saudável: criar a operação de
  renovação sem apagar Tree.confirmed/grant para contornar essa condição.
- F02 — Preparação de ACK atual limita o grant ao Child.active anterior para que
  ACTIVATE/replay não renove. Um RENEW novo válido precisa de distinguir a espécie14:
  pode exceder o restante antigo, mas respeita pedido/máximo/upstream e a validade
  do contexto anterior até confirmar. Duplicado14 continua sem renovar; tipo11
  conserva o cap anterior. Reusar o cap antigo em todos os casos impediria a
  entrega de IMP-012; removê-lo globalmente enfraqueceria IMP-011. A distinção
  está no Design aprovado e cabe no ficheiro autorizado.
- F03 — Um terminal de operação RENEW falhada não é cancelamento da geração:
  bloqueia o seu ACK tardio, preservando apenas o grant anterior ainda válido.
  DEACTIVATE exato/cancelamento do último remove também esse grant e impede replay.
  Falha de renovação não pode chamar um caminho que apague antecipadamente a
  confirmação saudável nem limpar um terminal para instalar ACK antigo.

A guarda de identidade `detached/line.child` e a guarda terminal M07 devem manter
o efeito em todos os caminhos. Nova geração/revisão pode substituir o contexto,
mas callback antigo continua ligado ao contexto capturado; não se decide apenas
por ID de stream ou pelo valor atual consultado depois. Não trocar a guarda por
!pending genérico. ContinuationDone continua no finally, incluindo retornos por
cancelamento, deadline, Session/epoch antiga, substituição e shutdown.

L07–L13/L15/L23 e os testes históricos de ActivationTest devem demonstrar esses
casos posteriormente. Não foram executados nesta revisão; os negativos M07
originais e a correção aceite permanecem intactos.

## 4. Correlação, revisão a podar e limites de entrega

O codec atual já representa14 com Activation,13 com Deactivation e ACK12 sem
novo campo de espécie. RoutingService precisa de encaminhar13/14/ERROR13/14 antes
de trace e correlacionar ACK12 pelo contexto local completo. ERROR11/15 e restantes
eventos são preservados; não se edita manager/codec ou se cria wire novo.

**F04 — Adaptação prevista:** Line/fingerprint de11/14 e terminais devem distinguir
espécie, revisão e request canónicos. DEACTIVATE compara Session/stream/epoch/ID/
revisão admitida exatos. O que foi reservado mas não submetido não é revisão
emitida; marcar submissão antes de chamar port protege callback síncrono.

A maior revisão submetida pode não ter sido admitida no parent; o Design escolheu
explicitamente preservar identidade exata e terminar pelo lease, em vez de
cancelar r quando foi enviado13(r+1) ou enviar nova poda de fallback. Esta escolha
é compatível com ADR-005 e REQ-017 (poda por cancelamento ou expiração). Não prova
remoção remota imediata nem meta7s: bytes14 já aceites podem originar grant remoto
posterior ao cancelamento. Não há novos pedidos após retirada, mas a expiração
remota segue o último grant válido efetivamente concedido.

O novo pedido explícito usa revisão maior, não reset de UUID para contornar
watermark. MAX_VALUE não faz wrap. Poda/ACK antigos não removem substituta, mesmo
quando a nova ativação está na mesma Session; Session antiga nunca retira a nova.
A matriz aprovada do Design5 e L09–L11/L15/L18 cobrem os limites dessa escolha.
Nenhuma garantia de ACK de DEACTIVATE é acrescentada por inferência de ADR-015.

## 5. Cadência, quota e recursos físicos

Confrontado com RuntimeConfiguration: durações são positivas, em milissegundos
inteiros, com comparação antes de conversão; renew<lease. Para G>=1ms, o P
aprovado é positivo (>=333333ns), constante por grant e sem ceil para1ms.
P não é recalculado por restante/3; atraso de timer/ACK admite um ciclo corrente,
com início real novo, sem fila de ticks perdidos. Após falha, ciclo periódico só
é elegível com procura/grant anterior ainda vivo; não reinicia ACTIVATE por tick.

**F05 — Adaptação prevista:** a operação14 e a resposta do child precisam de
budget min(T, restante do grant anterior na admissão), incluindo filas, B/quota,
ACK e callback. Os testes de limites exatos devem provar que prazo/lease expirado
não reinstala autorização. O início da concessão nova é o início local do pedido,
não receção de ACK/ACCEPTED; floor é revalidado na fronteira do port.

**F06 — Adaptação prevista:** no código atual `kind != REQUEST` identifica respostas
porque só existem REQUEST/ACK/ERROR/UNBOUND nesse engine. Ao acrescentar13/14,
usar classificação explícita pedido/resposta: ACK/ERROR continuam16/janela fixa1s,
pedidos RENEW/DEACTIVATE não contornam nem alargam essa quota de respostas.
Não criar outro contador/janela/pool em paralelo; todos partilham B e os limites
por Session/contexto. Erro sem slot continua em J, com descarte limitado.

C/D/L/H/B/J existentes,4 PENDING/Session,4 sends/Session e2/contexto,16 ações/turno
e<=2 jobs/1 wake permanecem. O contexto upstream deve continuar canónico e estável
entre gerações, para que um objeto novo não esconda envio antigo perante2/contexto.
Retirada lógica não evicta H protegido ou devolve B/J antes do físico.

O ActSend atual liberta só após callbackDone && continuationDone e notifica
resources. O Design preserva essa regra para11/12/13/14/ERROR. Callback que ainda
está em owner.offer não terminou só porque ready=true ou a continuação já correu.
Pelo inverso, callback terminado não dispensa a continuação. Não libertar por
future.cancel, timeout lógico, mapa vazio ou ACCEPTED. L12/L19/L20 são obrigatórios
na validação posterior; nenhuma prova de runtime é inferida desta inspeção.

Grants muito curtos ou carga acima das quotas podem impedir continuidade. A resposta
segura é recusa/expiração documentada, não aumentar caps, simular sucesso ou
considerar qualquer configuração/carga um SLA de disponibilidade. Isto é limite
já explícito no Design aprovado, não decisão aceite inviável identificada agora.

## 6. Shutdown e owner

requestClose atual usa firstStop, stopping, scheduler/cleanup e awaitTermination
com o budget restante mais restritivo. Inclui admissions de anúncios, traceSends
e activation.sends. A renovação/poda pertence a esse último orçamento, sem fase
adicional por ramo, novos sockets ou espera de ACK13.

**F07 — Adaptação prevista:** depois de stopping não admitir nem enviar novos13/14;
cancelar intenções não submetidas. Cleanup no owner pode terminar a continuação
lógica cancelada após realizar a remoção real, porque o estado de domínio é
serializado; não pode antecipar a conclusão de callback ainda em execução.
A reserva física só sai quando ambos terminam. Await(0) false enquanto houver
físico; budget excedido é erro explícito e não prazo novo. L22 valida depois.

CLOSED/OVERFLOW da Session corrente continua a retirar essa conexão e as streams
dela dependentes; não é uma falha lógica apenas do pedido14. Callback de Session
antiga não remove Session nova. INVALID_MESSAGE/exceção sem perda física deve
terminar só a operação corrente, conservando concessão anterior saudável.
owner.offer(false) mantém os hooks existentes de cleanup, sem espera bloqueante.

## 7. Divergências e decisão

Nenhuma divergência concreta entre Design aprovado e ADR aceite foi identificada.
F01–F07 são adaptações ainda necessárias no código para realizar esse Design
nos três ficheiros autorizados. Não são código já corrigido, nova fundação,
autorização de Implement ou nova ADR. A política CLI de Q-C-42 é decisão explícita
do utilizador; não se reinterpreta Q-C-34 para editar a aplicação.

Não se propõe ADR de substituição por inferência. Se Implement revelar uma
inviabilidade real, aplicar ADD11: parar a parte afetada, preservar evidência,
explicar impacto e aguardar aprovação da substituição antes de a executar.

## 8. Evidência e checkpoint

Validação documental aplicável e hashes serão guardados em adr-01. V-015/L01–L24,
regressão atual/clean verify/JAR final, manual humano, revisão final Denji e
integração continuam pendentes. Sem gates externos adicionais.

Parar no checkpoint ADR. Só Model e Design em stages_completed; current_stage=adr.
Revisão preparada para aprovação, sem Java/testes Java/staging/commits/push ou
início de outro IMP. ADR novas criadas:0; estados de ADR aceites alterados:0.

## Aprovação ADR e autorização limitada

Registo 2026-11-18T16:41:21Z: utilizador aprovou revisão ADR/reutilização e autorizou
só primeiro passo de Implement, nos três ficheiros, com APIs/testes históricos/
M07/Q-C-40–42/wire/caps/físico preservados. Parar antes dos comportamentos.
