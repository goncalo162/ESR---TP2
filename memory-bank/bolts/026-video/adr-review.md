---
unit: "005-video"
bolt: "026-video"
stage: "adr"
status: "completed"
created: "2026-11-28T15:33:45Z"
updated: "2026-11-28T15:48:00Z"
---
# IMP-019 — Revisão ADR e impedimento de integração

**Design aprovado; revisão preparada, com Q-C-50 por decidir. Não autoriza
Implement.** A01–A08 foram conferidos nos fontes reais. A integração completa
**não cabe nos três caminhos atuais**: faltam acesso ao owner, observação atual
de procura e notificações de revogação, além da delegação de disponibilidade.
Não se substituem por polling, outro owner, reflexão ou sucesso fictício.

O ADD conserva os três caminhos autorizados. A ampliação para oito caminhos
na secção 4 é uma **proposta**, sem alteração normativa ou código aplicado.
Model/Design são as únicas etapas aceites; current_stage=adr, completed=null.
P01–P20/V-020-C não executados. Os 1582 testes e a prova integral dos conteúdos
de IMP-018 são históricos, não validação de StreamingServer.

## 1. Método e base real

Base HEAD cba7ea393b5e9028a7a753d91450c378d640a357, branch Denji. Inspeção
estática da working tree, com [hashes de entrada](../../../experiments/construction/026-video/adr-review-01/entry-state.json),
[inventário de APIs](../../../experiments/construction/026-video/adr-review-01/project-api-inspection.json)
e [excertos numerados](../../../experiments/construction/026-video/adr-review-01/source-excerpts.md).
Nenhuma aplicação, Maven, teste, socket ou leitura dos vídeos executada.
`javap` inspecionou apenas [assinaturas do JDK21 instalado](../../../experiments/construction/026-video/adr-review-01/jdk21-public-apis.txt).

Reutilizam-se ADR-006/009/021/024 e as restrições de owner, época e fatal de
ADR-017/018/020. Não se altera a decisão de proprietário serial nem se propõe
um serviço paralelo. Q-C-50 trata da ponte ausente e do âmbito necessário;
nenhuma ADR global é unilateralmente alterada ou declarada aceite.

## 2. A01–A08: resultado sobre as APIs existentes

| Item | Conferência estática | Resultado e consequência |
|---|---|---|
| A01 | OttApplication:239–267/472–505 cria hook antes de open; open cria/start OverlayNode. OverlayNode:65–75 só valida; RoutingService:378–389 instala ofertas em start | **Compatível mediante implementação no App**: preparar todas as fontes antes da factory/criação do nó. O hook atual não conhece uma preparação em curso; falta ligar-lhe o scope de cancelamento antes dos workers |
| A02 | OverlayNode:31/28 guarda owner/routing privados; API pública só expõe snapshots/trace. RoutingService:350 exige owner. Route.Confirmation/Subscription.Lease já preservam identidades, revisões e validade | **Impedimento**: não há caminho público para consultar procura atual no owner. Snapshot antigo e seleção não substituem autorização |
| A03 | RoutingService.Observer recebe Event(reason,fatal,routeSwitch); callbacks em note/switchEvent não representam alterações gerais de procura. ActivationEngine é privado | **Impedimento**: não há listener de confirmação/cancelamento/expiração/Session/revogação nem bind inicial para o produtor |
| A04 | RoutingService:531–540 tem setLocalAvailable(UUID,boolean), owner-only/void, sem guard de época. drive:706 só renova row.offered=true; healthy:1657 e admissão:1832 respeitam indisponibilidade | **Mecanismo interno existente, ponte ausente**: OverlayNode não delega a mutação. É preciso resultado local explícito e correlação de binding/época antes de afirmar retirada |
| A05 | Console.log/stop são limitados e não bloqueantes; OverlayNode.failMonitoring é privado. Owner.loop:1081–1096 só apanha RuntimeException, embora finally execute hooks; App:329 só catch(Exception) | **Integração fatal incompleta**: não presumir captura/registo de Error por essas fronteiras. Novo adaptador deve preservar o objeto e iniciar fecho real também em preparação/bind/offer rejeitado |
| A06 | Node.requestShutdown/awaitTermination aceitam Duration restante e fazem clamp; manager divide restante para LEAVE; routing/monitor requestClose são não bloqueantes e await limita ao restante | **Compatível com adaptação**, mas requer o scope global antes de prepare e fecho físico independente do owner. Fallbacks close() com prazo próprio não servem para o novo composto |
| A07 | Getters reais de NodeConfiguration/StreamConfiguration/RuntimeConfiguration.Source; VideoSource.open/nextFrame/state/close públicos. END/CLOSED provam fecho normal; FAILED+close quieto não prova cleanup | **Compatível com diagnóstico conservador**: sem mudar VideoSource, FAILED conserva CLEANUP_UNCERTAIN. Construtores de Snapshot de4/5/6 argumentos e factories históricas mantêm-se |
| A08 | StreamingServer.java tem0 bytes; StreamingServerTest.java ausente. StreamForwarder continua placeholder. Primitivas JDK21 finitas estão disponíveis | **Implementação futura** com FramePort explícito e saída UNWIRED recusada. Não há ligação de dados/UDP a alegar |

### A01: qualificação anterior a qualquer oferta

O instanceId gerado no App é passado ao nó; RoutingService.start usa esse mesmo
UUID como serverEpoch de todas as streams locais. Não é preciso inventar uma
época por fonte. Depois de validar configuração e criar scope/hook, o App pode
qualificar os S ficheiros antes de chamar qualquer factory de nó. Sem essas
fontes qualificadas não se chama start, nem sequer para apenas uma delas.
O binding posterior deve conferir o epoch real e os metadados, não fabricar
uma confirmação de routing. Falha de preparação fecha aquisições e resultados
tardios com os mesmos W workers, mesmo quando ainda não há nó.

O hook atual só chama console.stop e espera finished com o timeout completo.
Enquanto open prepara, ainda não existe RunningService para finalmente fechar.
Logo, o scope de preparação deve ser acessível ao hook desde antes do primeiro
worker; o hook sinaliza cancelamento imediatamente e espera apenas o restante
global. Uma flag de console sozinha não cancela uma leitura física.

### A02/A03: informação existente não equivale a acesso/notificação

RoutingService.snapshot() é uma projeção imutável publicada, não uma chamada
ao owner que atualiza a idade e o estado. Confirmation.visibleAt(now) envelhece
leases, mas não sabe de uma nova Session, cancelamento/revogação ainda não
presente na cópia ou retirada local. ActivationEngine.forwarding(at) filtra
confirmação saudável/leases/consumidores, mas é privado e usado em publish.
Não há como instalar o DemandPort aprovado apenas a partir de OttApplication.

Os testes existentes de OverlayNodeRoutingTest acedem à factory package-private
e capturam owner/router para testes. Não é uma API pública disponível ao App.
Mover o produtor de pacote, refletir campos privados ou imitar esse owner
fora do nó não é uma solução de integração admissível.

O listener proposto deve invalidar permits não claimed no próprio turno do
owner que revoga a procura. O pump pode ser coalescido; a revogação não fica
adiada até esse pump. Pedido externo de cancelamento e transição processada
pelo owner são momentos distintos. Um READ já claimed conserva a reserva até
retorno, sem transformar cancelamento lógico em fecho físico.

### A04: retirada local efetiva e limites da confirmação

setLocalAvailable(false) altera row.offered, remove oferta local e faz drive;
anúncios periódicos só avançam a sequência quando offered é true. Não foi
encontrado rearmamento periódico incondicional dessa oferta. A admissão de
um filho numa origem local indisponível produz SOURCE_UNAVAILABLE.
Isto é evidência estática, não um teste novo de retirada executado.

O método existente devolve void e pode simplesmente retornar quando stopping.
Não aceita a época/binding esperados nem está exposto pelo nó. A ponte deve
confirmar a mudança autoritativa local antes de devolver APPLIED, preservando
o comportamento de propagação já existente. APPLIED **não** confirma que um
peer recebeu WITHDRAW ou deixou de enviar: o transporte continua assíncrono.

### A05/A06: fatal e orçamento global

As fronteiras novas apanham RuntimeException/Error para guardar o mesmo
primário, invalidar gates e iniciar o shutdown; não os convertem em erro
checked de media. Quando há chamador síncrono, relançam o mesmo objeto após
iniciar a limpeza. Nas callbacks sem chamador, o objeto fica no FatalPort e
no diagnóstico de falha, nunca num Future ignorado. Fecho secundário não
substitui o primário nem provoca autosupressão.

O owner real tem offer/onStop/fail e fila finita; offer rejeitado inicia stop.
Essas capacidades existem em SerialOwner, mas o App não possui a instância.
A ponte deve guardar a primeira causa e sinalizar o fecho fora da fila quando
ela rejeita. A captura genérica existente de RuntimeException no loop não
justifica afirmar que Error já fica registado; tratar as novas fronteiras
explicitamente, sem alterar NeighborManager por inferência.

RoutingService/NetworkMonitor iniciam firstStop com timeout configurado, mas
requestClose não espera esse prazo: só agenda/sinaliza cleanup. awaitTermination
recebe e limita-se ao Duration restante, incluindo scheduler/reservas físicas.
NeighborManager.requestShutdown usa min(restante,D), metade para notificação;
o nó recalcula restante entre cada await. Não há evidência de necessidade de
alterar esses três componentes de lifecycle para um prazo novo neste bolt.

Há, porém, uma armadilha no fallback: o App chama service.close() após um await
incompleto. OverlayNode.close() usa o prazo capturado no nó; se o nó entrou em
stop fatal depois da âncora global do App, o prazo local pode acabar mais tarde.
No composto SERVER novo, close/requestShutdown/await reutilizam sempre o scope
global: iniciar o fecho das fontes **e** do nó antes de esperar; nunca renovar D
ou chamar um close bloqueante de prazo próprio depois de o orçamento acabar.
Não se alteram por inferência os outros papéis/transportes.

Thread.join(Duration), timedWait/await em ns e queues finitas estão disponíveis
no JDK21 instalado. A [inspeção do bytecode de join(Duration)](../../../experiments/construction/026-video/adr-review-01/jdk21-thread-join-duration.txt)
mostra retorno imediato para duração não positiva numa thread já iniciada,
mas arredondamento para cima em ms no caminho de thread de plataforma.
Não usar esse arredondamento nos novos workers/coordenador: calcular restante,
arredondar para baixo para join(long), esperar só se houver pelo menos1ms e
conferir isAlive sem espera positiva no resto. Não chamar join(0), nem
join(Duration) numa thread ainda não iniciada (lança IllegalThreadStateException).
O código existente que converte o restante para ms recusa espera quando
obtém zero; não precisa de correção inferida neste incremento.

Um worker preso continua a ocupar W e a posse do handle. Daemon/interrupção,
requestClose concluído ou cleaned lógico não provam terminação. A prova futura
exige retorno físico, recolha por opToken, threads terminadas e handles sem
incerteza. O coordenador fecha sem depender da fila/vida do owner; se exceder
o único prazo, Termination fica incompleta e a saída de aplicação é de erro.

### A07/A08: compatibilidade e limites honestos

SourcePlan pode usar mediaBase(), mediaFile(), streamId(), serverId(), fps(),
loop() e runtime().source().maxFrameBytes() existentes. O loader exige2..256
streams e FPS1..60; não são precisas chaves/getters novos. StreamingServer não
tem consumidor/construtor anterior no placeholder, mas o App e routing têm:
preservar factories/constructores públicos e package-private usados nos testes,
Snapshot de4/5/6 argumentos, Observer/Event e CLI, incluindo play não implementado.

VideoSource.END só é instalado após releaseInput sem erro; CLOSED também.
Em FAILED, o input foi destacado e o fecho foi tentado, mas o resultado não
é público; a repetição close retorna sem atuar. Suprimidos ausentes não provam
sucesso de cleanup. A proposta **não amplia VideoSource/VideoSourceTest**:
o adapter regista tentativa/retorno e CLEANUP_UNCERTAIN quando FAILED não
permite prova. Jobs podem chegar a zero com Termination.complete=false.
Não chamar isso handle comprovadamente fechado, nem repetir o fecho do input.

O adapter de produção FramePort.UNWIRED devolve REFUSED/UNAVAILABLE, com counters
separados de tentativa/admissão/recusa. Só um porto controlado de teste poderá
admitir frames neste incremento. Esse teste não prova entrega UDP nem reprodução.
16MiB continuam a limitar apenas raster de destino, não toda a memória da JVM.

## 3. Impedimento sustentado

Faltam duas pontes de produção: **OverlayNode → owner/routing** e
**RoutingService → procura/revogação/disponibilidade correlacionada**. Nenhum
dos três caminhos autorizados pode acrescentar métodos aos dois tipos finais
privados do nó, nem observar todas as transições da ActivationEngine privada.
Logo, a implementação integral não pode ser aceite como ligada sob esse âmbito.

Manter os três caminhos permite apenas preparar o produtor/portos/testes
isolados, deixando a integração recusada. Não satisfaz A02/A03/A04, P12/P20
ou V-020-C completo; não é uma alternativa para declarar sucesso.

## 4. Q-C-50 — proposta concreta de âmbito/contrato, por aprovar

Recomendação: ampliar de **3 para8 caminhos**, mantendo os três atuais:

1. `Projeto/src/main/java/pt/uminho/esr/ott/server/StreamingServer.java` — produtor e tipos aninhados aprovados.
2. `Projeto/src/test/java/pt/uminho/esr/ott/server/StreamingServerTest.java` — P01–P20 e oráculos controlados.
3. `Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java` — qualificação antes da factory, composição UNWIRED/fatal e scope global.
4. **Adicional:** `Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java` — ponte pública específica de fontes, usando o owner real e o fecho existente.
5. **Adicional:** `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java` — observação autoritativa de procura, notificações e resultado de disponibilidade local.
6. **Adicional:** `Projeto/src/test/java/pt/uminho/esr/ott/overlay/OverlayNodeRoutingTest.java` — bind/owner/callbacks/rollback/rejeição e ciclo TCP pertinente.
7. **Adicional:** `Projeto/src/test/java/pt/uminho/esr/ott/routing/RoutingServiceTest.java` — notificações completas, snapshot/compatibilidade e retirada sem ressurreição.
8. **Adicional:** `Projeto/src/test/java/pt/uminho/esr/ott/app/OttApplicationTest.java` — ordem de startup, cancelamento durante prepare, fatal e orçamento comum na saída real do App.

Os cinco adicionais não estão autorizados por este documento. Não alterar
NeighborManager, NetworkMonitor, transporte, VideoSource, configuração, wire,
fundamentos de árvore ou testes históricos. Se outro caminho vier a ser
necessário, apresentar reprodução/impedimento antes de o alterar.

Para o teste de App, propor seams package-private de composição que controlem
leitor/factory do nó, exercitando o caminho real de prepare/open. A factory
histórica de serviço falso usada para testar a gramática não prova que a
qualificação precede uma factory real; não a usar como substituto desse oráculo.
Preservar os overloads/ensaios históricos e acrescentar os casos específicos.

### Contrato proposto da ponte

Tipos novos de integração ficam aninhados em RoutingService/OverlayNode,
sem dependência desses pacotes para server. Tipos internos do produtor
continuam aninhados em StreamingServer. Assinaturas abaixo são **propostas**,
não a descrição de APIs existentes:

```text
OverlayNode.withLocalSources(config, instanceId, neighborObserver,
                             routingObserver, localSourceBinding)
LocalSourceBinding.bindOnOwner(localSourceAccess, ownerAccess)
OwnerAccess.isOwnerThread() / offer(Runnable) / fail(Throwable)
RoutingService.bindLocalSourcesOnOwner(listener) -> LocalSourceAccess
LocalSourceAccess.serverEpoch()
LocalSourceAccess.observeOnOwner(streamId) -> SourceDemand
LocalSourceAccess.setAvailableOnOwner(streamId, expectedEpoch, available)
    -> APPLIED / ALREADY_APPLIED / STALE / STOPPED
LocalSourceListener.onDemandChangedOnOwner(streamId)
LocalSourceListener.onStoppedOnOwner()
```

- Uma única ligação SERVER por nó, sem listeners ilimitados; outros papéis e
  factories antigas continuam com o comportamento atual. OwnerAccess delega
  ao SerialOwner já criado; não oferece um segundo executor ou acesso a I/O.
- Instalar o hook de stop e listener antes de expor/start do produtor. Epoch
  vem da instância real. Após routing.start, observar inicialmente todas as
  fontes; uma procura chegada no startup não depende de evento futuro.
- SourceDemand imutável contém stream/época, instante monotónico e versão
  local da autorização, disponibilidade efetiva e confirmação opcional com
  referências/Sessions/revisões/leases. É calculado diretamente no owner,
  a tempo atual, não devolve apenas o snapshot antigo. Exige origem local
  corrente, Session atual, referência confirmada não cancelada e lease positivo.
- A versão muda em alteração relevante de autorização, não em cada leitura
  do relógio. Permits incluem identidade de binding e horizonte: máximo das
  validades remanescentes de referências efetivas; consumidor local exige
  revogação explícita. Seleção/intenções/ACK pendente não fornecem crédito.
- Notificar alterações após publicar a projeção correspondente e no mesmo
  turno do owner: ACK/renovação, cancelamento/DEACTIVATE, expiração, Session
  substituída/fechada, perda/mudança de confirmação, disponibilidade e stop.
  A callback só revoga/assinala dirty; nunca faz I/O nem reentra na árvore.
  Continuam um wake pendente, um pump e as células S aprovadas; sem nova fila.
- Mutação de disponibilidade valida binding/época/owner e devolve resultado
  depois da aplicação autoritativa local. Preservar setLocalAvailable antigo
  como API de compatibilidade. Não gerar um WITHDRAW avulso no App. Callback
  antigo é descartado no produtor antes de invocar a ponte; recusa inesperada
  de retirada corrente inicia fatal/fecho do nó, sem sucesso fictício.
- Rejeição de offer e Runtime/Error nas fronteiras novas iniciam o fecho
  real fora da fila, preservando o primário. Callback de stop revoga gates;
  cleanup físico não aguarda essa callback nem depende de um owner vivo.
- Registrar/remover a única ligação no lifecycle do nó. Sem histórico de
  bindings/gerações, sem timer auxiliar em routing e sem mudar os limites W/S.

Se Q-C-50 for aprovado, registar **primeiro** a ampliação no ADD, Design,
contratos e bolt, antes de código. A aprovação desta proposta não substitui
autorização explícita de Implement nem aprova Test.

## 5. Verificação futura e checkpoint

P01/P13 testam zero criação/start/oferta antes de todos os primeiros frames,
incluindo cancelamento com prepare ainda sem RunningService. P02/P03/P07/P09
forçam confirmação/revogação/claim/Session/expiração nas duas fronteiras.
P12/P14 verificam indisponibilidade local após EOF/erro através da API real,
avançam anúncios periódicos e preservam a outra stream. P18/P19 verificam um
único orçamento, owner morto, W ocupado, fatal/Error e cleanup incerto sem
sucesso falso. P20 inclui a ponte real SERVER e o caminho App com UNWIRED
recusado. São concretizações futuras, não testes executados nesta revisão.

Nenhuma correção Java aplicada. Aprovação de Q-C-50 e desta revisão é necessária
antes de implementar a ligação. Sem autorização, manter ADR pendente e os
três caminhos atuais; não construir um substituto que esconda o impedimento.

Conferência documental/imutabilidade em
[evidência desta revisão](../../../experiments/construction/026-video/adr-review-01/README.md).
Continuam obrigatórios P01–P20/V-020-C, regressão JDK21, manual humano aplicável
antes de aprovar Test, revisão Denji e integração. TASK-020 só fecha após A/B/C;
sem gates externos adicionais. Preservados negativos e remoção da demo;
R17 inicial continua sem causa estabelecida, sem usar integração com métricas
controladas como prova de causa. Não iniciar outro IMP, fechar, fazer commit/push.
