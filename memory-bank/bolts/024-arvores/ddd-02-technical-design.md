---
unit: "004-arvores"
bolt: "024-arvores"
stage: "design"
status: "complete"
created: "2026-11-21T14:21:52Z"
updated: "2026-11-21T15:03:15Z"
---

# IMP-014 — Design de histerese e troca confirmada

**Design proposto para revisão, não aprovado.** Model e Q-C-45/46 aprovados
explicitamente pelo utilizador; registo2026-11-21T14:21:52Z. Apenas Design
autorizado. Sem revisão ADR, Implement/Test, Maven, commits ou push.
V-017/S01–S18 continuam **NÃO EXECUTADOS**.

## Aprovação do Design

Design aprovado explicitamente pelo utilizador; registo 2026-11-21T14:46:03Z. Apenas
revisão ADR autorizada. Texto de proposta e checkpoint abaixo preservado como
histórico da etapa, sem aprovação Implement ou execução de V-017. Versão
aprovada original congelada em adr-review-01/approved-design-input.tar.gz.

## 1. Fontes, arquitetura e âmbito

[Model aprovado](ddd-01-domain-model.md), [ADD/IMP-014](../../../add.md#imp-014),
ADR-005/016/024 e [Q-C-45/46](../../../docs/construction-duvidas.md) definem
o incremento. Preservar ADR-014/017/018/020/021/025/026, contratos de
[renovação/poda](../023-arvores/ddd-02-technical-design.md) e de
[recuperação](../008-arvores/ddd-02-technical-design.md), incluindo R10.
Os detalhes de interfaces anteriores provêm de documentação aprovada, não de
inspeção de fontes nesta etapa. A compatibilidade real será conferida numa
revisão ADR posteriormente autorizada; nomes internos abaixo são propostas.

A política pura cabe em RouteMetric; a coordenação cabe no RoutingService,
no owner serial existente. Auxiliares/valores ficam aninhados nesses ficheiros.
Não criar tabela, serviço, executor, socket, wire, propriedade ou timer novo.
Estado operacional volátil; sem base de dados, migração ou API HTTP.

Âmbito futuro exclusivo de código:

1. `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`.
2. `Projeto/src/main/java/pt/uminho/esr/ott/routing/RouteMetric.java`.
3. `Projeto/src/test/java/pt/uminho/esr/ott/routing/RouteSwitchTest.java`.
4. `Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java` (Q-C-47-B).
5. `Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java` (Q-C-47-B).

Não ler/analisar/alterar fontes Java em Design. Não alterar NetworkMonitor,
LinkMetrics, RoutingTable, Route, Subscription, NeighborManager,
protocolo/configuração/POM nem testes históricos por inferência. A menção
legada a NetworkMonitor em TASK-017 não amplia o ADD. Um impedimento real
será apresentado com impacto e proposta antes de ampliar âmbito.

## 2. Responsabilidades e contratos preservados

| Responsabilidade | Proposta / contrato |
|---|---|
| Política pura em RouteMetric | Comparação exata de custos válidos/P; preservar componentes, construtores, validade, comparação e operações anteriores |
| Owner do RoutingService | Validar entradas, qualificar alternativa, coordenar operação única, confirmar e publicar, depois podar contexto capturado |
| Transporte/manager existentes | ControlPort.send assíncrono e Session completa; ACCEPTED não significa ACK remoto nem aplicação de DEACTIVATE |
| Scheduler existente | Mesmo alarme/wake/sweep; D/backoff são instantes no cursor, não tickets por observação |
| Snapshot | Route/catálogo continuam seleção; forwarding continua confirmação; recuperação mantém projeção própria existente |
| Teste novo RouteSwitchTest | Relógio/owner/transporte controlados e integração TCP pertinente; nenhuma remoção/substituição de testes anteriores |

Preservar requestSubscription/retrySubscription, SubscriptionTicket/result/cancel,
onNeighborEvent/onMetrics, snapshots, construtores públicos de Snapshot de
quatro/cinco/seis argumentos documentados por IMP-013 e lifecycle. Não acrescentar
componente obrigatório ao Snapshot, enum wire, estado público de Route ou DTO
em Route.java. Os estados OBSERVING/QUALIFIED/SWITCHING/BACKOFF abaixo são internos;
não alteram o significado legado de seleção INACTIVE/UNAVAILABLE.
Observabilidade usa diagnóstico/log existente: stream, operação, identidades,
custos, P/O/D, início/fim, motivo e duração. Não se propõe API pública adicional
ou histórico ilimitado para expor cada amostra.

## 3. Estado e identidade das observações distintas — Q-C-45

### 3.1. Estado limitado

Dentro do slot C da Tree: identidade da última confirmação para D e instante
da sua instalação; no máximo um cursor de qualificação (F alvo, contagem,
proveniência corrente de comparação), um motivo/instante de backoff e a
operação upstream já existente. O contexto antigo capturado é um valor limitado,
não cópia de tabela, procura ou fila de operações. Contagem satura em O.

F=(stream, origem fixa, época admitida, Session completa e path origem→nó).
Usar IDs de nó/instância/ligação, não só nodeId/IP. O path simples continua
limitado a32 IDs; validar tamanho antes de copiar. G é announceSeq validado.
Épocas não são ordenadas por UUID. Campo de custo/RTT e capturedAt não são F/G.

### 3.2. Proveniência verificável, sem IDs inventados por releitura

| Entrada | Identidade de prova e regra de distinção |
|---|---|
| Medição local | M=(Session completa, sampleCount, sampleCountSaturated, lastSampleAtNanos), campos documentados de LinkMetrics. A Session tem de ser corrente, com amostra/custo válidos e instante de amostra presente. Contagem crescente identifica amostra nova mesmo que custo e instante coincidam |
| Contagem de amostras saturada | Com count=MAX e flag de saturação, só lastSampleAtNanos que avança monotonicamente prova amostra distinta. Mesmo token/instante não prova outra medição: não contar, sem wrap, contador artificial ou alteração do monitor |
| Anúncio | A=(F,G), observado depois de validação/aplicação no owner; G estritamente novo perante máximo observado da lineagem. APPLIED com mesmo G por melhoria de custo não é geração nova |
| Primeira comparação | Uma única observação inicial da Tree com os tokens atuais e custos válidos. Se ainda falta métrica, a primeira entrada real que os torna comparáveis conta uma vez. Recriar cursor, restaurar custo ou reler snapshot não recria esta exceção |
| Invalidação | Não é observação positiva. Reinicia consecutividade quando afecta a comparação corrente; conservar marcas de prova já vista/bloqueada |
| Timer/callback/publicação/duplicado | Não é observação. Pode executar revalidação ou libertar recurso, sem incrementar contador ou criar crédito |

A distinção de M usa diferença monotónica no horizonte do relógio local;
timestamp civil, identidade de objeto e número de publicação não servem.
Sem amostra, estado SUSPECT só conserva custo se o contrato anterior o permitir;
missedCount/estado por si só não tornam a amostra nova. Snapshot repetido com
o mesmo M não conta; entrada atrasada de Session antiga ou token ultrapassado
não pode reverter a prova corrente nem prejudicar a substituta.

O RoutingService compara a proveniência recebida com a última validada antes
de substituir a sua vista de métricas. Guardar no máximo um token por Session
no estado de sessões/métricas já limitado por N; não criar ledger por amostra.
Para ANN, reutilizar máximo G observado e proteção da Line/H já existentes;
acrescentar campos de qualidade no contexto existente, não outro mapa de
históricos. Sem slot admissível, conservar grant saudável e recusar nova troca.

Um evento genuíno pode afectar várias streams dependentes e contar uma vez
em cada uma. Na mesma Tree, se altera as duas pernas/projeções, avaliar uma
vez após aplicar todas as alterações desse evento. Duas entradas reais podem
produzir duas observações; releituras da mesma entrada não. O passe limitado
pode coalescer entradas numa única prova mais recente por Tree: isso pode
subcontar sob carga, nunca fabricar O observações a partir de uma só. Não
guardar backlog para recuperar amostras perdidas. Conferir em ADR a entrega e
proveniência nas APIs reais sem alterar o monitor para obter uma sequência nova.

### 3.3. Formação e interrupção da sequência

Comparar o candidato alternativo seleccionado pelas regras existentes com o
caminho **confirmado**, usando os anúncios/métricas actuais de cada um no mesmo
instante now. Não confundir o custo histórico do ACK ou a Route já seleccionada
para B com o custo actual do ramo A. Sem custo actual do prefixo confirmado,
não contar nem trocar por qualidade; manter a sua autoridade se continuar válida.
Somar apenas custos representáveis. Não recalcular EWMA no routing.

Para o mesmo F alvo e comparação ainda válida: evento distinto que satisfaz
a política incrementa até O; insuficiência, métrica ausente/overflow, TTL ou
invalidação reinicia em0. Mudança do alvo/F reinicia; uma entrada real nova
que já valida o novo F pode iniciar a nova sequência em1. Não transportar a
contagem de B para C. Mesmo G não é evento ANN novo; novo G do mesmo F pode
continuar a sequência. Releituras/duplicados não incrementam nem quebram
uma sequência saudável. Mudança irrelevante noutra stream não a altera.
Só contar entradas que afectem uma das duas pernas da comparação: métrica da
Session confirmada/alvo ou ANN pertinente a um desses caminhos. Actualização
de vizinho sem relação com eles não é observação desta Tree.

Após terminal, nenhum caminho de primeira elegibilidade pode ignorar o watermark
bloqueado. Em particular, overflow por métrica e reposição dessa métrica sem ANN
novo não restauram prova. Mantém-se a correcção R10 de IMP-013; observação de
qualidade não fornece crédito à recuperação nem limpa o seu bloqueio.

## 4. Cálculo exacto de melhoria e permanência

Para C actual e N novo inteiros não negativos representáveis, P inteiro0..100:
exigir C>N e C−N ≥ ceil(P×C/100). Evitar os produtos P×C e100×(C−N) em long.
Proposta de decomposição inteira na função pura de RouteMetric:

```text
q = C / 100; r = C % 100
minimumReduction = q*P + (r*P + 99)/100
qualifies = (N < C) and (C - N >= minimumReduction)
```

q×P≤C; r×P+99≤9999 e o resultado total≤C. Logo a decomposição não transborda
para C≤Long.MAX_VALUE e P≤100. Não usar double, epsilon, percentagem truncada
ou subtracção antes de validar os domínios. A soma do custo local ao anunciado
continua a exigir a validação de representabilidade já existente.

P=0 exige N<C; P=100 exige N=0<C. C=0 não tem melhoria possível, e a fonte
continua sem upstream. Hops/nodeId só desempata selecção, nunca satisfaz melhoria.
Exemplos de oráculo: C1000/N800/P20 passa; N801 falha. Com C=Long.MAX_VALUE/P20,
a redução mínima é1844674407370955162, sem produto intermédio inválido.

D usa `routing.switch.min.dwell.ms`, default5000ms; O/P preservam defaults3/20.
Guardar início monotónico+duração, comparar age(now,start)≥D sem somar timestamps.
Capturar a âncora no turno de publicação da primeira confirmação e de cada troca
efectiva de identidade do ramo (Session/época/path), incluindo recuperação que
mudou ramo. Renovação/revisão nova do mesmo ramo, observação, falha da tentativa
ou mudança de selecção não reancoram D. Reaquisição do mesmo ramo após uma lacuna
não inventa mudança de parent; preserva a âncora existente enquanto a Tree vive.

Pode alcançar O antes de D. No despertar em D, validar novamente F, custos,
TTL/Session/época, procura e grants: timer não acrescenta observação. Se a
qualificação deixou de valer, limpar contagem e aguardar entradas reais. No
instante exacto D, permanência cumprida; no deadline exacto, grant/operação
expirados. Falha efectiva nunca espera D, O, P ou backoff de qualidade.

## 5. Admissão, revalidação e T/A partilhados

Admissão por qualidade exige procura legítima, não fonte, confirmação antiga
saudável, alvo distinto e elegível, sequência O válida, D cumprido, ausência
de operação upstream lógica concorrente e capacidade/H/cadência disponíveis.
Não criar consumidores/tickets fictícios; resultados antigos de tickets ficam
imutáveis e intenção admitida mantém identidade/cancelamento.

Usar o mesmo cursor de ronda de activação/recuperação: T=activateTimeout
(default3000ms), A=activateAttempts (default3), t0 capturado na admissão antes
da fila; até A envios admitidos dentro de T global. Recusa antes da reserva
física não consome tentativa, mas não reinicia tempo. Nova adesão, nova geração,
troca de alvo, callback ou passagem qualidade→recuperação não muda t0/A.
Uma tentativa que espera T inteiro pode esgotar a ronda com apenas um envio.
Não pré-alocar A operações ou prometer A esperas completas.

Ao admitir: congelar contexto do ramo antigo, incluindo maior revisão já
submetida11/14 **antes** de preparar/submeter a nova11, ainda que seja a mesma
Line. Contexto antigo contém Session/stream/época/subscriptionId/revisão máxima
submetida e grant; o alvo tem operação/requestId próprios. Revisão só reservada
sem chamada ao port não entra na revisão de poda. Marcar submissão no contexto
capturado antes da chamada ao port, tolerando callbacks síncronos.

Revalidar imediatamente antes de cada envio e antes de efectivar o ACK:

1. Stopping/cancelamento, geração e operação correntes, procura ainda admitida.
2. Session completa, vínculo de origem/época, anúncio vivo, path simples e
   compatível com o pedido e lineagem/revisão actuais; não basta igualdade de nó.
3. Tempo restante da mesma ronda, n<A para novo envio e capacidade canónica.
4. Em qualidade, ramo antigo saudável, mesmo F qualificado, comparação actual
   ainda suficiente, O e D válidos. Revalidação não conta nova observação.
5. Se houve perda efectiva do antigo, retirar a sua autoridade e aplicar já a
   recuperação; a tentativa útil pode continuar no orçamento original sem P/O/D.

Alvo que deixou de ser seleccionável/compatível ou perdeu a melhoria não se
confirma por qualidade com um ACK antigo. Tornar a tentativa terminal, preservar
o ramo antigo válido e bloquear prova/backoff. Um novo candidato pode ocupar
a visão coalescida, não uma segunda operação. Dentro da ronda de recuperação,
alternativa admissível usa apenas T/A remanescentes, segundo IMP-013.

## 6. Confirmação atómica e poda exacta — Q-C-46

ACK12 só corresponde se coincidem espécie11, requestId, Session completa,
stream/origem/época, subscriptionId/revisão e geração/operação. ERROR11/14/15
continuam separados. Guardar no máximo um ACK antecipado; confirmação exige
também ACCEPTED do envio corrente. ACCEPTED só enfileira, não concede grant.
ACK duplicado não reancora lease; callback antigo não publica sobre substituta.

Lease wire G é positivo, dentro do pedido/máximo; restante conservador é
G menos idade desde o início local da tentativa, capturado antes da fila,
não desde ACK ou callback. Restante≤0 não confirma. Aplicar as revalidações da
secção5 no instante de commit, inclusive cancelamento e deadline exacto.

Preparar validações e cópias defensivas antes de alterar a confirmação. No mesmo
turno serial, sem I/O ou callbacks de utilizador entre as mutações:

1. Preparar o valor da nova confirmação e os destinos **já compatíveis**;
   não copiar grants dependentes do prefixo antigo.
2. Substituir a referência lógica do ramo confirmado; retirar autoridade antiga,
   actualizar a âncora D se houve mudança efectiva e terminar a operação actual.
3. Capturar/publicar snapshot imutável de catálogo/selecção/forwarding/recuperação
   no mesmo instante. Há zero ou um parent utilizável por stream; leitores
   não observam uma confirmação parcialmente mutada.
4. Só depois oferecer a poda best effort do contexto antigo congelado. Falha
   da poda não faz rollback para A nem transforma B em dois parents activos.

Um snapshot antigo é uma observação as-of; visibleAt filtra expiração e não
renova prazos. A sobreposição de ramos remotos/controlo pode produzir tráfego
transitório antes da poda/lease, sem autorizar saídas duplicadas estáveis. Não se
afirma continuidade de vídeo sem medição nem se adiciona deduplicação UDP aqui.

DEACTIVATE13 usa Session/stream/época/subscriptionId e a revisão máxima submetida
**da geração antiga capturada**, nunca a Line actual consultada após a troca.
No receptor, só a revisão exactamente admitida pode ser removida. Revisão maior
desconhecida/menor não cancela a nova; se o parent só conhecia r e foi submetida
r+1 sem prova remota, a poda r+1 pode ser ignorada e o lease finito é fallback.
Não enviar fallback aproximado r, não usar >=, não inventar ACK13 ou esperar
pela poda para retirar autoridade local.

Reutilizar a intenção de poda existente, uma tentativa dentro do seu T original
capturado na retirada, sem fila por candidato/filho ou reenvio ilimitado. Esse
prazo de limpeza não prolonga a ronda de activação nem o shutdown. No contexto
de mesma Session/Line, r novo>r antigo impede poda antiga de remover B; conservar
esta guarda e nunca usar a revisão nova como revisão de limpeza de A.

Se a activação do alvo falha, limpeza do alvo não pode cancelar o ramo antigo
que se pretende preservar. Para alvo em outro contexto, só limpar a sua revisão
submetida exacta; se partilha Line com o grant antigo e a limpeza o poderia
afectar, conservar a confirmação antiga válida e usar expiração remota finita
como fallback. Não adivinhar admissão remota a partir de SendResult.

## 7. Renovação, recuperação e filhos

### 7.1. Renovação do ramo em serviço

Uma operação upstream lógica por Tree. Se14 está pendente, a qualificação pode
ser observada mas11 aguarda a sua conclusão/prazo original; não a cancelar para
obter artificialmente um T/A novo. Renovação necessária do ramo saudável tem
prioridade no passe existente. Depois de admitir11 por qualidade, não abrir14
paralela para A: o grant de A continua só pelo remanescente conhecido.

Renovação continua uma operação finita com deadline cortado por T e pelo grant
antigo; não ganha um segundo conjunto A de retries por activar histerese.
ACTIVATE por qualidade e recuperação usam literalmente a mesma ronda/T/A;
RENEW conserva a cadência e o orçamento da sua operação já existente, sem
duplicação ou sobreposição lógica. Todos usam as mesmas reservas físicas.

Uma14 anterior fisicamente pendente após conclusão lógica conserva B/quota;
seu ACK/callback tardio só pode afectar o contexto ainda corrente. Após troca
para B, não renova A, não publica A nem reancora D. A revisão submetida dessa14
continua incluída na captura de poda de A. Falha lógica14/PATH_CHANGED recusa
renovação, sem revogar por inferência o grant antigo válido.

### 7.2. Falha efectiva durante a observação/troca

Down da Session actual, época retirada, indisponibilidade efectiva ou expiração
retiram autoridade dependente imediatamente, sem esperar P/O/D ou cooldown.
Um Down antigo não derruba Session nova. Transferir tentativa útil para modo
recuperação mantém t0, contador, requestId/revisão e reservas; apenas deixa de
exigir melhoria/dwell. Sem tentativa útil, terminar contexto e reconciliar a
recuperação existente, com os seus créditos/watermarks e cadência mínima T.
Não abrir outra ronda em paralelo nem prometer envio quando não há capacidade.

Backoff de qualidade não pode impedir retirada/recuperação. Espera pelo limite
físico ou pela cadência de IMP-013 não é espera pela histerese. Sem parent válido,
publicar ausência de forwarding e UNAVAILABLE/espera de orçamento conforme
projecção existente, preservando intenção e prazos próprios dos interesses.

### 7.3. Prefixo novo e renegociação automática

No relay que troca: manter apenas destinos cujo grant continua compatível com
a nova confirmação; filhos do prefixo antigo tornam-se interesses suspensos
no mesmo slot D, só até aos deadlines originalmente admitidos. Não publicar
esses destinos em B, criar ACK12 espontâneo ou transformar suspensão em lease.
Novos pedidos frescos serão avaliados pelo prefixo/Session/época actuais.

No filho: anúncio validado de prefixo diferente ou ERROR14/PATH_CHANGED marca
rebind sem revogar antecipadamente o grant ainda válido por inferência. Não
renovar indefinidamente o path incompatível, não prolongar o prazo antigo e
não trocar por qualidade sem a política aprovada. Ao terminar a autoridade
antiga, recuperação automática usa intenção já admitida, emite ACTIVATE11
fresco e ignora histerese de qualidade. Sem novo play humano.

Na mesma Session/época, preservar subscriptionId da Line e usar revisão maior
que todas as11/14 já submetidas, requestId novo; não reutilizar UUID para escapar
ao watermark nem fazer wrap em MAX. Nova Session/época exige contexto próprio.
Conceder novo grant apenas por pedido/ACK corrente e barreira de envio; validade
ao filho é min(pedido, máximo local, remanescente confirmado a montante), com
floor em ms imediatamente antes de enviar e sem elevar resto<1ms.

Um filho que só vê TCP saudável/recusa14 pode conservar a sua autoridade local
até expirar o lease antigo conservador. Não há mensagem de revogação nova nem
promessa de renegociação instantânea. No relay, não há forwarding por grant
incompatível entretanto. Cancelamento/exaustão do interesse remove o slot, não
é restaurado por ACK/callback atrasado; outros consumidores/ramos mantêm-se.

## 8. Backoff de qualidade, capacidade e lifecycle

No terminal falhado de11 por qualidade: capturar instante e o maior G observado
até esse terminal para o F/Line; bloquear também em RESOURCE_LIMIT temporário
antes de envio. Limpar sequência; não reancorar D. Próxima admissão por qualidade
exige age(now,terminal)≥T, cadência age(now,lastRoundStart)≥T, O observações novas
e prova útil validada posterior ao terminal: F novo elegível ou G superior ao
watermark bloqueado do mesmo F. O ANN que dá crédito pode ser observação1.
Após terminal, a sequência nova começa apenas com prova de rearme útil;
medições distintas posteriores podem completá-la. Não acumular O com métricas
isoladas enquanto a prova permanece bloqueada.

Um ANN posterior recebido antes de acabar o backoff pode ficar como único
crédito coalescido e iniciar a sequência nova; não inicia11 cedo. Ao despertar,
revalidar tudo. Sem crédito/qualificação não agendar cadeia de retries. Métrica
isolada, capacidade libertada, callback, tick e ANNs anteriores/duplicados não
desbloqueiam prova. Erro permanente, revisão esgotada, cancelamento e stopping
mantêm o tratamento terminal anterior; não viram falhas temporárias.

Com E=eventsCapacity e N=min(control.maxConnections,63), manter limites existentes:

| Recurso | Limite partilhado e extensão interna |
|---|---|
| Trees C | min(E,256); cursor/qualificação/cooldown dentro do slot, sem Tree por candidato |
| Children D | min(E,256), até N/stream; suspenso usa slot/prazo antigo |
| Pendentes child | até4/Session, somando11/14; nenhum slot adicional de troca |
| Tokens L | min(E,64), até4/stream; automatismo não cria token/consumidor novo |
| Históricos H | min(E,1024), até2N/stream; prova/watermark na Line existente, sem lista de amostras/revisões |
| Tokens de métrica | até N, uma proveniência no estado da Session existente; descartar ao retirar contexto, sem aceitar eventos da Session antiga |
| Envios B | min(E,64), até4/Session e2/contexto, somando11/12/13/14/ERROR, inclusive gerações retiradas ainda físicas |
| Erros J / respostas | min(E,16), até1/Session, incluídos em B; ACK/ERROR até16/janela fixa1s/Session |
| Trabalho | passe comum≤16 acções:4 conclusões+4 trees+6 children+2 locais; qualificação/revalidação usa visita da Tree, sem scan ilimitado por callback |
| Scheduler | mesmo alarme, até2 jobs físicos e1 wake; D/backoff/deadlines no estado |

Enviar upstream com referência canónica estável da stream, não um objeto por
geração; respostas child usam Line estável. Reservar antes de invocar port.
Troca/recuperação/poda/renovação não ganham pools próprios. Saturação preserva
grant/histórico saudável; nenhuma evicção protegida ou libertação antecipada.

Callback publica resultado e agenda continuação; esta valida contexto e marca
conclusão em finally. Libertar B/J só com callbackDone **e** continuationDone,
incluindo quando a continuação é ignorada. Timeout/cancel/future.cancel/limpeza
de mapa não são conclusão física. Gerações novas continuam sujeitas a2/contexto.
Falha de offer no owner mantém o hook de shutdown existente; não descartar uma
continuação e libertar o seu ticket por aproximação. Cleanup só assume a parte
lógica depois de a executar no owner, preservando a conclusão física pendente.

requestClose impede novas admissões/envios, termina lógica e retira autoridade;
não cria DEACTIVATE tardio nem fase extra de drenagem. awaitTermination fora do
owner inclui jobs e reservas físicas de todas as gerações no restante do primeiro
budget global. Callback/job retido implica await(0)=false. Falha de prazo é
explícita; não aumentar prazos/testes para obter conclusão.

## 9. Segurança, erros e observabilidade

Validar antes de copiar/alocar: Session corrente/envelope, origem/época fixa,
IDs/revisões, path simples/tamanho e valores representáveis. TCP é laboratório
controlado, não autenticação criptográfica. Não usar bytes UDP, trace, catálogo
ou simples selecção como prova de autoridade/procura. Silêncio exclusivo UDP
continua limitação assumida, sem watchdog/re-route novo.

Recusa lógica do alvo preserva A enquanto válido. CLOSED/OVERFLOW da Session
corrente segue a perda física já existente; erro de sessão antiga não afecta
substituta. ERROR de trace/renovação não termina11 de outro contexto. Sem ERROR
sobre ERROR, retries infinitos ou manutenção de future lógico indefinido.
Diagnósticos distinguem qualificação insuficiente, dwell/backoff, orçamento,
mudança de alvo, timeout, cancelamento e falha física; dados limitados aos
contadores/último motivo existentes, sem amostras completas em memória.

## 10. V-017/S01–S18 — oráculos concretos, todos por executar

Relógio monotónico e owner drenados explicitamente; transportes permitem reter
ACCEPTED/ACK/callback/continuação e controlar Session. Não usar sleeps para
simular D/T nos unitários. Integração TCP usa métricas controladas para testar
o coordenador, sem afirmar que isso mede ruído real ou explica R17.

| ID | Preparação e oráculo observável |
|---|---|
| S01 | A1000, B alterna799/801; cada801 quebra sequência; contar11/snapshot para provar ausência de oscilação |
| S02 | B800, três tokens distintos; D−1ns sem11, D exacto permite uma; antes de ACK fica A, depois fica B |
| S03 | P0/P100/O1, empate e custo0 da fonte; nenhuma melhoria por hops/empate ou activação na fonte |
| S04 | MAX com P20 exige redução1844674407370955162; vizinhos na fronteira, soma inválida e reposição sem ANN; sem overflow/crédito reciclado |
| S05 | Mesmo token M/A repetido não conta; três medições com custo igual contam; count distinto no mesmo instante conta; count saturado/instante igual não prova nova amostra; um evento em duas projecções conta uma vez |
| S06 | F/Session/path/época/TTL e métrica inválida reiniciam; duplicado não quebra; G novo do mesmo F continua; replay depois de reset não recria observação inicial |
| S07 | Primeira confirmação ancora D;14 do mesmo ramo não desloca; troca real/recuperação que muda ramo ancora; fronteiras de deadline/D separadas |
| S08 | Selecção B com forwarding A: comparar custo actual de A; sem custo de A conservar grant sem troca; sem procura/oferta local sem11 |
| S09 | Reter ACK/envio e snapshots: A até ambas as condições, B numa publicação,13 antiga só depois; falha de13 não restaura A |
| S10 | Permutar ACK/ACCEPTED/cancelamento/erro e deadline exacto; requestId/Session/época/revisão/path errados não confirmam nem libertam reserva cedo |
| S11 | Negar11 de B mantendo A saudável; nenhuma quarta tentativa com activateAttempts=3 nem T novo por callback; snapshot A, cooldown desde terminal, watermark inclui ANN vistos durante tentativa |
| S12 | Após S11, libertar capacidade e invalidar/restaurar métrica sem ANN não geram11; ANN posterior G maior fornece observação1/crédito; O novo e T/cadência permitem recuperação limitada; duplicados não rearmam |
| S13 | Down/expiração de A antes de D, em backoff e durante11; retirar A logo, continuar tentativa útil com t0/A originais ou usar recuperação existente, sem espera por histerese/segundo pool |
| S14 | B/C melhoram durante troca; no máximo uma operação; perda de melhoria/path actual antes de ACK invalida commit por qualidade, sem mudar t0 ou confirmar alvo obsoleto |
| S15 | Cancelar um de dois consumidores/child e depois último nas ordens ACK/envio; preservar outro, impedir ressurreição;13 antiga não remove revisão nova |
| S16 | Cadeia de filhos com prefixo antigo; snapshot novo exclui incompatíveis, nenhum ACK espontâneo/lease alargado, ACTIVATE fresco automático após perda efectiva, floor/remanescente/revisão incluindo14 em voo correctos |
| S17 | Reter callback e continuação em ambas as ordens através de gerações; B e2/contexto mantidos; shutdown sem prazo extra, await(0) false até término físico; M07/históricos preservados |
| S18 | TCP real A/B e duas streams/origens com métricas controladas: troca confirmada e falha limitada isoladas,14/15 ERROR não atingem11 alheia,13 só revisão capturada; sem alegar reroute por perda exclusiva UDP |

Acrescentar a estes grupos as combinações de renovação14→11, ramo antigo com
maior revisão apenas reservada versus submetida, mesma Line e ACK14 tardio
como instâncias de S09/S10/S13/S15. Preservar ActivationTest, RecoveryTest,
testes de lifecycle e restantes testes históricos integralmente. Não retirar asserções,
alargar prazos ou alterar fundações para fazer passar a integração.

Os oráculos serão implementados apenas com autorização de Implement. Test terá
de consolidar V-017, clean verify/regressão JDK21 e novo JAR/hashes conforme o
contrato de entrega. Manual humano deve ocorrer **antes de aprovar Test**;
revisão final **Waheed** e integração continuam obrigatórias para fecho. Não
se introduz gate externo adicional nem aprovação presumida de qualquer fase.

## 11. Conferência futura e checkpoint

Na revisão ADR autorizada conferir: proveniência M/A e coalescência no owner;
acesso ao custo do caminho confirmado e G sem expor privados; semântica/revisões
de11/14/13 e contexto antigo; construtores/projecções preservados; adaptação
da operação/cursor único sem duplicar budgets; reservas canónicas entre gerações,
work quota e lifecycle. Estes são pontos de compatibilidade local a verificar,
não gates externos ou prova obtida pela leitura de código nesta etapa.

**R17 inicial de IMP-013 continua sem causa estabelecida.** A integração com
métricas controladas não demonstra a causa do negativo histórico; nenhuma
promessa de correcção dessa falha faz parte deste Design. Logs, negativos,
sucessos, manual e hashes anteriores são preservados.

Parar neste Design para revisão humana. `current_stage=design`; só Model
concluído. História continua planned/não implementada. Sem ADR/Implement/Test,
fontes lidas/analisadas/alteradas, Maven, commit/push ou outro IMP.

## Resolução aprovada Q-C-47-B — contrato e âmbito antes de Implement

Registo2026-11-21T15:03:15Z: utilizador aprova alternativa B e revisão ADR.
A allowlist efectiva tem os três caminhos da secção1 mais
`Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java` e
`Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java`.
As restrições anteriores a esses dois caminhos ficam substituídas apenas para
esta integração. Não alterar outros fontes/testes históricos.

RoutingService.Event passa a transportar Optional<SwitchEvent> imutável, mantendo
Event(Reason,boolean). SwitchEvent contém stream/origem/época/operação, fase
STARTED/SUCCESS/FAILURE, Sessions/paths antigo e alvo, revisões, custos, motivo
local e duração monotónica. Paths copiados/limitados32; motivo≤256. Snapshot
e seus construtores4/5/6 ficam exactamente com o contrato anterior.

OverlayNode ganha factory nomeada com sink RoutingService.Observer, preservando
todos os construtores/factories anteriores. Fatal continua a passar pelo
tratamento existente, mesmo se o sink falha; apenas payloads de troca geram
logs normais. OttApplication liga o sink exclusivamente a console.log/fila
limitada existente; nenhuma thread/fila/logger/dependência adicional.
Um início e um terminal por operação de qualidade; sem log por amostra/duplicado.
Publicar nova confirmação antes do evento SUCCESS e antes de submeter13 antiga;
rollback/falha preservam grant antigo somente enquanto válido. A01–A07 da ADR
continuam aplicáveis; nenhuma ronda/budget/reserva adicional.

Apenas Implement autorizado, a reapresentar com verificações correntes; sem
avançar a Test/fecho/commits/push. Preservar negativos/limitação histórica R17.
