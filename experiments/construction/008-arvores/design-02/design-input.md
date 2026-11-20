---
unit: "004-arvores"
bolt: "008-arvores"
stage: "design"
status: "in-progress"
created: "2026-11-20T11:07:54Z"
updated: "2026-11-20T11:21:05Z"
---
# IMP-013 — Design de recuperação limitada e recomposição automática

✅ Model e Q-C-43/44 aprovados em2026-11-20T11:07:54Z. Apenas Design autorizado.
⏳ Este Design é proposta completa para revisão, não aprovação ADR/Implement.
V-016/R01–R18 continuam NÃO EXECUTADOS. Nenhuma fonte foi lida/analisada/alterada
nesta etapa; contratos abaixo provêm da documentação aprovada, sem inspeção Java.
Compatibilidade real será conferida na revisão ADR autorizada, antes de Implement.

## 1. Arquitetura, fontes e âmbito exato

Composição no RoutingService existente, no mesmo owner serial. Estado volátil
por stream, seleção/catálogo separados de forwarding confirmado, mensagens
assíncronas via ControlPort e scheduler comum. Sem HTTP/DB, dependência, executor,
socket, timer por operação ou serviço de recuperação concorrente.

Fontes: [Model aprovado](ddd-01-domain-model.md), [ADD](../../../add.md),
IMP-013/ADR-005/020/024/025, CP-02/03, Q-C-35/40/41/43/44 e
[Design IMP-012](../023-arvores/ddd-02-technical-design.md).
Preservar ADR-014/017/018/019/021/026 e contratos TCPv2/ADR-034/035.

Os três únicos caminhos Java previstos para Implement permanecem:

1 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`.
2 - `Projeto/src/main/java/pt/uminho/esr/ott/routing/Route.java`.
3 - `Projeto/src/test/java/pt/uminho/esr/ott/routing/RecoveryTest.java`.

Auxiliares novos ficam aninhados nos dois ficheiros de produção. Não alterar
Subscription.java, RoutingTable.java, NeighborManager, OverlayNode, monitor,
codec/wire/enums de protocolo, transporte, configuração/POM, CLI ou testes
históricos. Nenhum caminho é acrescentado por alegada necessidade técnica.
Se a revisão das APIs revelar impedimento real, apresentar causa/impacto e
proposta concreta antes de alterar código ou ampliar âmbito.

## 2. Contratos locais e projeções propostas

Preservar requestSubscription/retrySubscription, SubscriptionTicket e cancel,
receção de eventos do manager/métricas, ControlPort.send, scheduler e lifecycle.
Não obter automatismo criando tickets/consumidores falsos ou chamando o comando
de play. Uma intenção local já admitida e não cancelada pertence ao Tree enquanto
vive, mesmo se o resultado da sua operação inicial já é terminal.

O resultado de um ticket continua imutável: SUCCESS/erro antigos não são
reescritos quando a recuperação se inicia/termina. Recuperação automática usa
uma operação interna da árvore, não duplica a reserva L ou o consumerToken.
Um pedido recusado antes de admissão/capacidade não cria intenção recuperável.
Cancelamento identifica a intenção/operação corrente; ticket antigo não retira
uma intenção substituta explicitamente criada por retrySubscription.

Propostas a conferir em ADR, sem afirmar assinaturas já existentes:

- Em Route.java, acrescentar valor aninhado imutável `Route.Recovery`, com fase
  local, razão, geração, intenção presente, tentativa corrente/total, parent/path
  pendentes opcionais e validade remanescente da operação. Não alterar componentes,
  construtor ou invariantes do record Route que representa seleção.
- Em RoutingService.Snapshot, acrescentar mapa imutável `recoveries` por stream,
  limitado por C. Preservar os construtores públicos anteriores de quatro e cinco
  argumentos como overloads com esse mapa vazio, além do novo construtor completo.
  Catálogo/rotas/forwarding/recoveries são capturados no mesmo turno/instante.
- As fases locais de recuperação distinguem WAITING_FOR_ROUTE, WAITING_FOR_BUDGET,
  ACTIVATING, UNAVAILABLE e CONFIRMED; não são mensagens wire nem novos valores
  no estado legado de seleção/Subscription.State. Sem candidato ou ronda terminal,
  o resultado da recuperação é UNAVAILABLE, mesmo que catálogo ainda exista.
- Expor restantes relativos ao instante do snapshot, nunca renovar deadline ao
  ler. Uma projeção posterior filtra confirmação expirada, sem mutar owner ou
  declarar caminho/custo selecionado que não possa recomputar.

Nomes internos de responsabilidades, não APIs adicionais fora do âmbito:
`reconcileRecovery`, `observeRecoveryProof`, `tryRecoveryAttempt`,
`retireRecovery`, `rebindDesiredBranch` e `publishRecovery`. Podem ser métodos
do engine existente; não se exige classe/ficheiro novo de infraestrutura.

## 3. Estado limitado e identidades

Por Tree/stream, incluindo períodos UNAVAILABLE com intenção ainda admitida:

- intenção local e interesses filhos já limitados por L/D, confirmação opcional;
- geração lógica corrente; no máximo uma ronda, uma tentativa corrente e um ACK
  antecipado, usando os mesmos contextos de ativação inicial;
- um crédito de novidade coalescido para a visão elegível mais recente; sem fila
  de anúncios, pedidos ou candidatos por falha;
- início da última ronda automática e janela de admissão T, independentes de
  trocar objeto/generation ou parent; não reutilizar criação de Tree para obter
  nova quota enquanto a mesma intenção automática sobrevive;
- indicação de rebind necessário no próximo fim de grant, sem prolongar o grant;
- estado/razão de recuperação para a projeção, dentro do slot C existente.

Por Line upstream já limitada por H, guardar no máximo a última prova estrutural
consumida, watermark de sequência de anúncio consumido/bloqueado, razão e instante
da última falha. Uma prova é um valor limitado; não manter lista de incidentes,
revisões, paths ou requestId. Retenção/retirada de Session/época seguem H existente;
não evictar proteção saudável nem criar segundo ledger sem teto.

Fingerprint estrutural da seleção elegível:
`F = (streamId, serverId, serverEpoch, Session completa, path origem→nó)`.
Path tem no máximo32 IDs válidos e é copiado só após validar/reservar capacidade.
`G = announceSeq` é sequência da geração da stream/época, obtida da vista de
anúncios validada, nunca uma ordenação de UUID. RTT, cost, capturedAt, idade,
instante de publicação, tick e versão global do snapshot não pertencem a F/G.

Elegível significa conjuntamente: vínculo stream/origem corrente, época admitida,
Session corrente, candidato fresco, amostra/métrica válida, path simples sem
retorno ao nó local e prefixo que pode ser solicitado. Não se inventam candidatos
se faltar acesso a esta informação: a revisão ADR verificará as portas disponíveis.

## 4. Novidade útil verificável para rearme

Um evento só dá um crédito se existe intenção legítima, nenhuma confirmação
saudável que deva ser preservada e a seleção é elegível. Crédito não é autorização,
reserva física ou promessa de ACK; é apenas permissão de tentar quando há budget.

| Entrada observada | Novidade útil? | Regra concreta |
|---|---|---|
| Perda da confirmação corrente | Sim, uma vez por identidade/grant perdido | criar incidente; Down duplicado da mesma Session não cria outro |
| Session nova, época corrente admitida diferente ou path estrutural diferente | Sim, se elegível | F difere da última prova consumida; revalidar proteção de época/lineagem |
| Candidato passa de inelegível a elegível | Sim, com causa validada | por Session admitida, geração nova após TTL expirado ou primeira amostra válida; não por ler o mesmo snapshot noutro instante |
| F igual após PATH_CHANGED, NO_ROUTE, SOURCE_UNAVAILABLE ou TIMEOUT | Só com geração nova posterior à falha | ANN validado recebido depois da falha, G estritamente maior que o watermark bloqueado/consumido; dá no máximo um crédito coalescido |
| Mesmo F/G repetido, melhoria de custo/RTT, refresh da projeção | Não | não altera watermark nem cria crédito/ronda |
| ANN de outra stream/origem ou época retirada | Não | isolamento; rejeição existente antes de recuperação |
| Callback/libertação física antiga | Não cria crédito | pode servir a tentativa/ronda ainda vigente, nunca reabrir terminal |
| Erro de formato, revisão esgotada, cancelamento ou shutdown | Não por ANN periódico | corrigir contexto admissível ou criar Session legítima nova; nunca contornar com outro UUID |

Um G maior com F igual é permitido apenas nas quatro razões da tabela: significa
um anúncio realmente novo da fonte após o fracasso, não simples refresh/duplicado
da mesma geração. É oportunidade limitada para verificar se o parent já pode
confirmar, não prova de que está confirmado. Se voltar a falhar, consumir a prova
usada e atualizar o watermark; o mesmo G não alimenta rondas sucessivas.
No instante de falha, o watermark bloqueado dessa Line/F captura o maior G
validado já observado para ela, incluindo anúncios recebidos durante a espera.
Um ANN anterior à falha não se torna novidade apenas porque o future terminou
mais tarde. Para F igual, exigir também instante de receção posterior ao erro;
receber de novo os mesmos bytes não muda esse instante/prova de geração.
Isto concretiza o caso TASK-016 de aguardar anúncio novo após PATH_CHANGED sem
ficar permanentemente bloqueado quando o path correto reaparece igual.

Na mesma ronda, novidade pode substituir a próxima tentativa ainda não submetida;
não renova T/A. Guardar apenas a prova elegível mais recente ainda não consumida.
Novidades antigas que se tornaram inválidas/expiradas são descartadas, sem
reancorar TTL. Uma prova nova não usada pode ficar como único crédito para a
próxima janela, desde que permaneça válida; não acumular trabalho de catch-up.
Revisitar um path após mudança real pode dar nova oportunidade, mas continua
sujeito à cadência e aos tetos abaixo; oscilação não ganha tentativas ilimitadas.

Saturação antes de emitir não consome prova/tentativa nem cria nova ronda: a
operação vigente pode aguardar capacidade dentro do seu deadline. Se termina
por RESOURCE_LIMIT, libertar callback sozinho não reabre o terminal. Nova
demanda explicitamente admitida ou mudança estrutural elegível pode ser reavaliada;
não transformar incapacidade local em falha global ou retry por timer sem evento.

## 5. Limites das rondas e admissão automática

Usar os parâmetros existentes, sem propriedade nova:
`T = routing.activate.timeout.ms`, `A = routing.activate.attempts`;
defaults T3000ms/A3. A é teto, não promessa de A esperas completas.

1 - Admissão lógica captura t0 antes de fila/envio. Ronda expira quando
`age(now,t0) >= T`, por subtração monotónica no horizonte suportado. Cada
tentativa tem deadline no máximo igual ao restante dessa mesma ronda.
Se depende apenas de interesses remotos, estes podem expirar antes; recalcular
procura e terminar imediatamente quando deixa de existir algum interesse válido.
2 - No máximo uma tentativa lógica por Tree. Consumir tentativa quando o envio
é emitido ou admitido para emissão após reserva física, como IMP-011. Uma recusa
antes de B/quota não consome tentativa, mas o tempo continua. Erro depois da
admissão consome; não devolver contagem para escapar ao teto.
3 - Após erro antecipado, tentar de novo apenas se houver candidato/prova
admissível, n<A, restante>0 e procura. Uma tentativa que espera T inteiro esgota
a ronda. Filhos adicionais, ACK precoce, atualizações e troca de candidato não
reiniciam t0 ou contador; nenhuma thread fica bloqueada à espera.
4 - A=limite ou prazo/no candidate terminal torna a ronda UNAVAILABLE com razão.
Fechar futures pendentes segundo o seu próprio prazo; conservar intenção local
admitida, não o future indefinidamente PENDING. Callback antigo permanece físico.
5 - **Cadência de rearme:** rondas automáticas da mesma stream começam no máximo
uma vez por janela T desde a última admissão. Próxima admissão exige crédito útil,
visão ainda elegível e `age(now,lastRoundStart) >= T`; ao iniciar, capturar agora,
sem executar janelas perdidas. Perda efetiva retira autoridade imediatamente;
esperar capacidade/orçamento é diferente de esperar histerese de melhoria.
6 - Durante a ronda corrente, uma alternativa nova pode usar as tentativas
restantes imediatamente, sem essa espera adicional. Depois de terminal, mesmo
Session/path/época novos não fornecem um orçamento automático paralelo antes
da janela. Assim, ANNs em rajada e alternância de paths não dão mais de A
submissões por janela de ronda T. O intervalo não é novo parâmetro configurável.
7 - O alarme comum pode acordar no fim da janela se já existe crédito útil válido;
o timer não cria esse crédito. Sem crédito/candidato, permanece UNAVAILABLE e
não agenda cadeia de retries. Sem intenção, retirar estado lógico elegível;
sem novo pedido/admissão legítima, callback/tick não o recria.

Exemplos de oráculo, não medições:

- T3000/A3, três recusas imediatas: não há quarta tentativa nem nova ronda aos
  100ms. Um ANN novo útil pode ficar coalescido; a próxima ronda começa apenas
  ao atingir a janela e só se ainda é válido. Repetir o mesmo ANN não basta.
- T3000/A3, primeiro ACK ausente durante3000ms: uma tentativa consumiu toda a
  ronda; não se acrescentam outros6000ms para cumprir três esperas.
- Um crédito aos2500ms não estende um interesse filho que expira aos2600ms.
  Sem intenção local/outro filho, remover o crédito quando a procura expira.
- Novo grant confirmado cedo não altera t0 da ronda que o criou. Se volta a
  falhar dentro da mesma janela, retirar a confirmação logo; nova tentativa
  automática respeita o orçamento. Não prometer tempo máximo de recuperação
  sob falhas repetidas, saturação ou partição.

## 6. Execução da recuperação e correlação

Na perda efetiva da confirmação, no owner e no mesmo turno:

1 - Comparar Session/geração/grant corrente; evento antigo não toca na substituta.
2 - Retirar forwarding dependente, terminar renovação/pedidos antigos logicamente
e tornar filhos dependentes suspensos, mantendo só o interesse permitido por Q-C-43.
Não eliminar intenção local, alternativas de outras Sessions ou outra origem.
3 - Reavaliar seleção pelas regras existentes; não fabricar um segundo algoritmo
de rotas, tocar no record de seleção para fingir confirmação ou usar outro servidor.
4 - Coalescer crédito/projeção e admitir uma ronda quando elegível. Sem rota,
publicar UNAVAILABLE; sem orçamento ainda, WAITING_FOR_BUDGET sem grant ou future novo.
5 - Submeter ACTIVATE11 com Session/path/época atuais, requestId novo, revisão
estritamente maior e subscriptionId da lineagem aplicável. Nova Session/época
tem contexto próprio; Session igual não ganha ID novo para esquecer histórico.
Revisão máxima sem sucessor é REVISION_EXHAUSTED, sem wrap ou reset oculto.

ACK12 só confirma se coincidem Session completa, stream/epoch, subscriptionId,
revision, requestId, espécie11 e geração/operação atuais. Guardar no máximo um
ACK antecipado até ACCEPTED; ACCEPTED sozinho não confirma. Cancelamento,
deadline exato, perda de intenção/Session ou prova de caminho entretanto inválida
impedem instalação. ERROR11/14/15 desmultiplexam por espécie e contexto; um erro
de trace/renovação não encerra a recuperação errada ou remove grant saudável.

O início da tentativa é capturado antes da fila. Lease recebido G deve ser
positivo e não exceder pedido/máximo local. Remanescente local é G menos idade
desde esse início, não G desde ACK/ACCEPTED. A duração concedida ao filho limita-se também
ao restante upstream, com floor em ms e sem elevar <1ms. A resposta deve ocorrer
dentro do prazo pendente; isso não corta artificialmente a duração do grant para
o restante T nem permite responder depois do prazo.
Revalidar antes de ControlPort.send; após submissão os bytes são imutáveis.
Snapshot só publica a nova confirmação utilizável, uma por stream, com destinos
compatíveis já renegociados. Não copiar todos os filhos antigos para a nova árvore.

Poda de ramo antigo usa identidade/revisão capturadas e semântica exata de
IMP-012. Não atrasar a retirada local esperando13, não inventar ACK de13 e não
cancelar aproximadamente uma revisão cuja submissão é desconhecida. Lease
remoto continua o fallback; falha lógica não liberta o envio físico em voo.

## 7. Renegociação automática dos filhos — Q-C-43 concretizada

Todos os nós executam o mesmo coordenador no seu RoutingService; não há callback
humano, CLI stop/play, watcher externo ou mensagem nova de recuperação.

### 7.1. No relay que perdeu o seu parent

Guardar no mesmo slot D o interesse do filho vivo com identidade/época e o
prazo **original** admitido. Publicar zero autoridade dependente enquanto falta
confirmação upstream. Suspensão não renova lease, não acrescenta timeout T ao
prazo antigo e não mantém filho morto. O fim desse prazo retira o interesse,
mesmo se a recuperação upstream acabou de ter sucesso.

Se houver confirmação nova mas o path do filho é antigo/incompatível, não
reconstruir grant por cópia nem enviar ACK12 espontâneo. Só o pedido fresco
do filho pode conceder autoridade nova com prefixo compatível. O parent pode
responder NO_ROUTE/PATH_CHANGED aos pedidos antigos, dentro de quota/prazo,
sem provocar queda da Session TCP saudável.

### 7.2. No nó filho, sem intervenção humana

- A sua própria perda de Session/grant confirmado inicia recuperação pela sua
  intenção local ou pelos interesses descendentes ainda admissíveis.
- ANN novo que altera o prefixo selecionado guarda uma indicação de rebind na
  mesma Tree. **Se o grant antigo ainda é saudável, conservá-lo só até ao prazo
  original:** seleção divergente ou ERROR14/PATH_CHANGED recusa a renovação,
  não prova revogação antecipada do grant. Não executar troca por melhoria de RTT.
- Enquanto o prefixo é conhecido incompatível, não enviar indefinidamente RENEW
  repetido do mesmo caminho. Manter a indicação de rebind e o deadline antigo,
  sem estender validade. Se uma renovação legítima do caminho ainda corrente
  confirma antes da perda efetiva, segue a regra normal de IMP-012.
- Limpar a indicação de incompatibilidade se uma visão nova validada volta a
  coincidir com Session/época/path confirmados, sem deslocar o deadline antigo.
  Depois de ERROR14/PATH_CHANGED, uma repetição da mesma geração não basta para
  retentar RENEW: exigir anúncio de geração nova posterior ao erro, respeitando
  a cadência P constante e o prazo original do grant. Só um ACK de renovação
  legitimamente admitida pode conceder lease novo, ancorado nessa operação.
- Ao terminar a autoridade antiga, retirar forwarding e, automaticamente,
  reavaliar a seleção atual. Com procura e crédito admissíveis, emitir **ACTIVATE11**
  fresco para o novo path. Não usar RENEW14 para criar/substituir grant expirado.
  Na mesma Session/época, manter o ID de lineagem e avançar revisão acima de todas
  as revisões já submetidas, incluindo renovação antiga em voo; requestId novo.
- Se a Session/época muda, usar a negociação própria do contexto corrente;
  nenhuma intenção/subscrição de uma conexão morta é transferida silenciosamente.
  O consumidor local já admitido mantém intenção; a operação de rede é nova.
- Sem rota, ficar UNAVAILABLE até crédito útil, não pedir ao utilizador novo play.
  Após ACK atual/barreira/validade, publicar grant novo e voltar à cadência normal
  de renovação de IMP-012. Isto não reescreve o resultado terminal de tickets antigos.

Não se consegue notificar revogação imediata de todos os descendentes através
de um novo tipo inexistente. O nó que conhece a perda retira autoridade logo;
um filho que só observa TCP saudável e recusa de renovação pode conservar o
grant antigo até à sua expiração local conservadora. Esse é um atraso limitado
pelo lease antigo, não uma extensão dele. O sucesso futuro continua dependente
de novos pedidos/ACK e das quotas; não se promete recuperação imediata em toda a árvore.

### 7.3. Ramos sem consumidores locais e corridas de expiração

Num relay cuja única procura eram filhos e todos os interesses antigos expiraram,
não conservar procura fantasma para forçar recuperação. Os descendentes com
intenção local persistente emitem os seus próprios pedidos frescos e reconstroem
os saltos de baixo para cima automaticamente. Novo ACTIVATE de filho é uma
admissão real em D/pending/T, não renovação do prazo do contexto antigo.

Se o novo pedido chega enquanto a poda do antigo está física, partilhar o
contexto estável e tetos existentes; poda/ACK/callback velhos não removem a
reativação nova. Revisão a podar é capturada na retirada, não consultada depois.
Se o pedido novo ou a ronda termina antes do ACK, não conservar future ou grant
antigo; o novo crédito/pedido posterior terá outra correlação e o seu próprio prazo.
Assim, a conservação limitada de Q-C-43 não exige conservar toda a árvore antiga
até o último descendente responder.

Sequência ilustrativa de um filho C através de dois relays; todos os tipos já
existem. O ACK do relay R1 depende da sua confirmação upstream, omitida do desenho:

~~~mermaid
sequenceDiagram
  participant R1 as Relay R1
  participant R2 as Relay R2
  participant C as Consumidor C
  R1->>R2: ANN9 de caminho novo
  R2->>C: ANN9 de caminho novo
  Note over R2,C: Grants antigos conservam os deadlines originais
  Note over C: Perda efetiva / expiração e intenção local viva
  C->>R2: ACTIVATE11 fresco, revisão maior, novo requestId
  R2->>R1: ACTIVATE11 agregado compatível
  R1-->>R2: ACK12 após confirmação a montante
  R2-->>C: ACK12 limitado pelo grant remanescente
~~~

## 8. Eventos de retorno, épocas, métricas e UDP

NeighborUp/HELLO_OK não confirma árvore. Regresso de relay com nova Session só
permite considerar candidato fresco anunciado; Down antigo não invalida esse
contexto. Uma instância reiniciada faz a consulta inicial ao bootstrap; processos
que conservaram a lista podem reconectar diretamente sem nova consulta.
Disponibilidade criada pela reparação IMP-035 pode ser consumida como entrada,
sem reimplementar repair ou criar arestas no routing.

Mudança de época é admitida pela política existente da origem. Retirar grants/
pedidos antigos dessa fonte e conservar intenção local vinculada à stream/origem;
os interesses remotos da época retirada não são autoridade/procura na nova.
Sem rollback a época retirada, substituição de origem ou ordenação de UUIDs.
Streams da outra origem e confirmações independentes permanecem intactas.

Q-C-35/41 mantêm-se: RTT/custo melhor, seleção divergente, withdraw isolado ou
renovação recusada não destroem por si só um grant saudável. Só perda efetiva
leva ao algoritmo de recuperação; melhoria com parent saudável é IMP-014.
UDP sem dados/perdido não produz crédito nem falha TCP. Não adicionar watchdog,
feedback ou nova mensagem; bloqueio UDP real e metas/FPS continuam avaliações futuras.

## 9. Capacidades, trabalho e shutdown

Com E=eventsCapacity e N=min(control.maxConnections,63), preservar os tetos
aprovados, partilhados com ativação/renovação/poda, sem pool paralelo:

| Recurso | Limite e uso pela recuperação |
|---|---|
| Trees C | min(E,256); RecoveryCursor/crédito/projeção dentro do mesmo slot |
| Children D | min(E,256), até N/stream; interesse suspenso usa o slot antigo e o prazo antigo |
| Pendentes child | até4/Session, somando11/14; rebind não cria fila extra |
| Tokens L | min(E,64), até4/stream; recuperação automática não adquire token duplicado |
| Históricos H | min(E,1024), até2N/stream; última prova/watermark por Line, sem lista de paths/rondas |
| Envios B | min(E,64), até4/Session e2/contexto, incluindo envios retirados físicos |
| Erros J | min(E,16), até1/Session, incluídos em B; sem backlog ilimitado |
| Respostas | ACK/ERROR até16/janela fixa1s/Session; recuperação não aumenta quota |
| Trabalho | passe comum até16 ações:4 conclusões+4 trees+6 children+2 locais |
| Scheduler | mesmo alarme, até2 jobs físicos e1 wake; prazo/rearme são instantes no estado, não timers dedicados |

Não pré-alocar A operações ou matriz de incidentes. Cada turno trata no máximo
o trabalho do cursor corrente e cede; scans/seleção/cópias respeitam os limites
já existentes e ausência de ciclos. Reservar antes de alocar/publicar novos
contextos, e recusar atomicamente sem perder grant/histórico saudável.

Envios upstream, inclusive os de gerações antigas, usam referência canónica
estável da stream; ACK/ERROR de filho usam Line estável. Nova geração/requestId
não muda a chave de quota física. Callback publica resultado e agenda continuação;
esta valida identidades e marca conclusão em finally. Release exige callbackDone
e continuationDone. Cancelar future, expirar ronda ou limpar mapa não é release.
CLOSED/OVERFLOW afeta só a Session corrente correspondente; antigo nunca derruba nova.

requestClose impede novas rondas/rebinds/envios, termina operações lógicas e
remove grants/interesses/créditos. Não adicionar DEACTIVATE após stopping nem
fase/budget extra de recuperação. awaitTermination continua fora do owner,
incluindo scheduler e todos os envios físicos no restante do primeiro shutdown.
Com callback/job retido, await(0) é false; só true quando terminaram fisicamente.
Excesso do prazo é falha explícita, não aumento de teste/budget ou simulação de fecho.

## 10. Segurança, erros e dependências

Preservar envelope/Session de handshake, vínculo de origem/época, validação de
path/tamanhos/caps antes de copiar e política de replay antes de admitir.
Q-C-43/44 não autenticam criptograficamente vizinhos nem alteram o modelo de
laboratório. Sem DNS no owner, stack trace/path local na rede ou resposta ERROR
a envelope inseguro. Snapshot é cópia defensiva; nenhum leitor obtém Tree/Line mutável.

NO_ROUTE/PATH_CHANGED/SOURCE_UNAVAILABLE/TIMEOUT e razões locais explicam a
indisponibilidade; ERROR14 não é ERROR11 nem trace15. Rejeição lógica isolada
não fecha nó saudável. INVALID_MESSAGE/revisão esgotada não autorizam reset de
lineagem. Saturação do owner segue hooks operacionais existentes; nenhuma espera
bloqueante para contornar recusa. Não se corrige fundação por inferência.

Não há persistência, serviço externo, REST, migration ou nova biblioteca.
Configuração, wire TCPv2/19 tipos/código3 rejeitado e UDP OTT1 permanecem intactos.
Compatibilidade de construtores/projeções e lifecycle será verificada em ADR,
nos três caminhos e dependências somente em leitura quando essa etapa for autorizada.

## 11. V-016/R01–R18 — concretização dos oráculos, todos por executar

1 - **R01:** losango, Down da Session atual retira autoridade no mesmo turno;
alternativa até à mesma origem só confirma com11/12/barreira e um upstream.
2 - **R02:** sem alternativa, partição/fonte única indisponível: operação terminal,
UNAVAILABLE explícito e intenção local persistente, sem loop/retry por tick.
3 - **R03:** relay regressa/Session nova e ANN válido: rearme automático respeita
T/A; consumidores não fazem play/restart. Reiniciado precisa bootstrap inicial;
os nós que mantiveram lista reconectam sem consulta repetida.
4 - **R04:** Down/ERROR/ACK/callback de Session/geração retirada não remove
substituta ou estende grants; reserva antiga só é libertada ao concluir físico.
5 - **R05:** duas origens e várias streams: perder parent/época de uma não muda
as confirmações/leases/intenções independentes da outra.
6 - **R06:** loop/prefixo/época inválidos recusados; PATH_CHANGED só permite
mesmo F voltar a tentar com G posterior ao erro, não com replay/RTT/refresh.
7 - **R07:** última intenção cancelada antes/depois de submit/ACK/rearme ganha;
cancelamento antigo não apaga retry explícito substituto ou consumidor distinto.
8 - **R08:** Session do filho morta, lease/cancelamento/época retirada elimina
interesse; mesmo nodeId não herda estado numa conexão nova.
9 - **R09:** árvore com dois relays e consumidores: mudança de prefixo, RENEW
antigo rejeitado, grant antigo não estendido; ao expirar, ACTIVATE automático
com mesmo ID de Line/revisão maior/requestId novo. Nenhum comando humano.
Incluir casos de relay só com filhos, interesse expirado, nova admissão e poda física antiga.
10 - **R10:** T/A=3000/3 e variantes pequenas; A erros rápidos não geram quarta
tentativa, timeout usa T total, nova adesão/ANN não reinicia orçamento. Rajada de
F/G novos coalesce um crédito; no máximo uma ronda por T, sem catch-up. Mesmo
F/G, ANN anterior à falha, métrica e callback não rearmam; G novo útil pode rearmar
uma vez se ainda válido. Crédito expira com candidato/interesse sem extensão.
11 - **R11:** ACK antes de ACCEPTED, no deadline exato e com grant excessivo/
fração<1ms não confirma; depois de confirmar, validade usa início do pedido.
12 - **R12:** época nova admitida conserva intenção local e exige ACK novo;
época retirada e pedidos antigos não reativam autoridade nem catálogo.
13 - **R13:** E/N pequenos e fronteiras C/D/L/H/B/J/quotas; recurso ocupado não
é tentativa antes de admissão, mas consome tempo; recusa/replay não expulsa
estado saudável. ERROR11/14/15 e falhas físicas isoladas por Session.
14 - **R14:** callback/continuação retidos, cancelamento/rebind/novo Tree não
furam2/contexto ou libertam B cedo; guardas M07 e todos os testes históricos mantidos.
15 - **R15:** shutdown em WAIT_BUDGET/envio/ACK, sem novas submissões após
stopping, await(0)=false até terminar físico, orçamento global nunca reiniciado.
16 - **R16:** TCP/sondas saudáveis e zero bytes UDP não criam incidente/crédito;
não se inventa vídeo/CORE, watchdog ou meta de recuperação automática UDP-only.
17 - **R17:** consumir reparação S–C existente após falhas sequenciais, identificar
stream/origem/Session/path/ACK e libertar recursos; sem reimplementar repair.
18 - **R18:** path/métrica melhores com grant saudável e ERROR14/PATH_CHANGED
preservam confirmação até perda efetiva; não iniciar troca de IMP-014.

Acrescentar as variantes dentro destes grupos sem declarar execução por escrever
nomes. JDK21/regressão e testes históricos são obrigatórios na etapa autorizada.
TCP real com dois relays e renegociação automática deve complementar relógio/
owner/port controlados; demonstração de autorização não equivale a vídeo recebido.

## 12. Riscos, limites e checkpoint

A recuperação pode esperar o fim conservador de um lease descendente ou da janela
de admissão automática. Grants curtos, quotas baixas, rounds esgotados, partição
e anúncios stale podem impedir continuidade; não aumentar limites ou prometer7s/FPS.
G novo é crédito de tentativa, não prova remota de disponibilidade. As variantes
R06/R09/R10 devem demonstrar segurança e rearme sem confundir esses conceitos.

Design mantém três caminhos. Em ADR conferir APIs reais: prova F/G acessível,
admissão11 com revisão nova após expiração, manutenção da intenção admitida,
construtores4/5 de Snapshot e projeções compatíveis, e ligação do lifecycle.
Se algum contrato exigir alteração fora desses caminhos, apresentar o impedimento
e o patch/âmbito propostos antes de código. Não contornar com reflexão em produção,
infraestrutura alternativa, enfraquecimento de testes ou alteração silenciosa de ADR.

Parar para aprovação de Design; revisão ADR/Implement/Test não autorizadas por
este documento. Nenhum Java lido/alterado ou Maven executado. V-016/R01–R18 por
executar. Fecho exige testes, regressão JDK21, manual humano, revisão final Fillmore
e integração. Sem gate externo novo, commits, push ou outro IMP.
