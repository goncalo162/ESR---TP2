# IMP-004 — Revisão ADR para aprovação

Revisão preparada em 2026-11-10T15:20:09Z. Análise estática do [Design](ddd-02-technical-design.md) aprovado pelo utilizador,
incluindo [Q-C-06](../../../docs/construction-duvidas.md#q-c-06--prazo-próprio-de-resolução-e-bind-no-imp-004).
Apenas a etapa ADR está autorizada. Não há código, build, testes, manual ou
aprovação humana desta revisão inferidos pelo presente relatório.

## 1. Conclusão e autoridade

O desenho é compatível com as decisões aceites analisadas. Não foi identificada
uma decisão aceite inviável, alteração necessária das fundações ou nova decisão
arquitetural que exija ADR-033. Reutilizar as ADR abaixo; **ADR-003 permanece
Substituída por ADR-013**, sem reabrir membership/lease no bootstrap.

Esta é uma conclusão de desenho, não prova de execução. D01–D22/V-007 e a
regressão/manual continuam por executar. Se a implementação revelar uma
contradição real ou exigir ficheiros fora dos três autorizados, aplicar ADD 11
e parar a parte afetada antes de alterar uma decisão/API aceite.

Precedência: enunciado → specs → intents → documentação → código. O ADD governa
as decisões derivadas; Q-C-05 e Q-C-06 conservam a aprovação explícita das suas
concretizações, sem as atribuir retroativamente ao contrato inicial. Fonte de
âmbito: [IMP-004](../../../add.md#imp-004), TASK-007, Neighbor/NeighborManager e
NeighborManagerTest, sem serviços de routing, UDP, CLI ou outros incrementos.

## 2. Matriz de conformidade

| ADR aceite do ADD | Verificação do desenho | Evidência/oráculo ainda exigido |
|---|---|---|
| ADR-012 — Codec/contexto | Codec/formatos intocados; handlers validam sessão/estado/identidade. ERROR preserva payload, envelope e relatedType bruto | D02/D06/D07/D11, regressão do codec |
| ADR-013 — Descoberta inicial | Lista desejada imutável, incluindo offline/instâncias zero; reconexão direta após READY; processo novo descobre de novo | D02–D04/D21; bootstrap encerrado depois da descoberta |
| ADR-018 — Identidade volátil | nodeId estável; instanceId/connectionId/attemptId cercam eventos. Não ordenar UUID por idade, persistir sessões ou restaurar subscrições | D09/D10/D15/D16 |
| ADR-019 — Iniciador/reconexão | Só menor ID faz dial; HELLO validado e HELLO_OK vazio; candidato/vínculo único, backoff/jitter, lista conservada | D05–D10/D13/D14/D21 |
| ADR-021 — Fronteiras | Perfil/entrada/capacidade validados; endpoint lexical, sem DNS do HELLO ou inferência de identidade pelo IP; UDP só após ACTIVE | D01/D03/D06/D08/D17 |
| ADR-024 — Incremento rastreável | Um IMP, três ficheiros autorizados e tipos internos; docs/índices/progresso/evidência sincronizados, sem fonte arquitetural paralela | V-ADD e diff; testes em Implement/Test |
| ADR-014 — Transporte limitado | Transport público mantido; callbacks curtos, C inclui dials/terminação; send mantém ACCEPTED=enfileiramento | D14/D15/D17–D19 e regressão de transporte |
| ADR-015 — Escrita confirmada | Usar sendAndFlush existente; Up exige as barreiras locais. Fila vazia não prova escrita; flush não é ACK remoto | D07/D08/D12/D19; bytes/API existentes preservados |
| ADR-016 — Runtime central | Usar Options/hello/retry/eventos efetivos; Q-C-06 fixa orçamento local separado, sem propriedade nova ou aumento de cap | D01/D12/D13/D20 |
| ADR-017 — Dono serial | Um owner por nó, próprio ou recebido; estado de Peer apenas nele, snapshots imutáveis, fila limitada/fail explícito | D18/D20; não criar outro owner na composição futura |
| ADR-020 — Falhas/shutdown | Separar rejeição remota/contexto/recurso local; isolar canal; um budget global, rollback e await físico; timeout explícito se preso | D02/D07/D15/D17–D19 |
| ADR-026 — Ferramentas | Maven/JUnit/validador existentes; sem dependência runtime, CI, linter, JaCoCo ou percentagem nova | POM/fontes preservados nesta etapa; regressão futura |

## 3. Owner próprio/emprestado

O manager autónomo é o nó embutível deste incremento e cria um único owner.
Quando a composição do nó já tiver um owner, usa a variante emprestada; não
instanciar a variante própria em paralelo para o mesmo estado de nó. O owner
externo tem capacidade igual a eventsCapacity e serializa os componentes futuros.

Mapas de peers, candidatos, sessão, retry e snapshots são alterados apenas no
owner. Threads de I/O/agenda publicam eventos; contadores/handles thread-safe
de lifecycle permitem solicitar fecho/contabilizar recursos físicos, sem
autorizar vizinhos ou publicar estado de Peer fora desse executor.

Fecho normal do manager não fecha nem espera a thread do owner emprestado, que
continua a pertencer ao chamador; espera o cleanup do componente e os seus
recursos. A fixture/caller encerra esse owner no momento apropriado. Owner próprio
também é aguardado fisicamente. A distinção não dispensa verificar os recursos
do nó completo quando IMP-005 fizer a composição.

Fila saturada/falha do consumidor é erro operacional observado. Flag terminal
e hook no owner permitem cleanup sem oferecer mais um evento à fila cheia.
Não usar CallerRuns para mutar Peer numa thread de I/O. Nenhum hook/callback
completa futures de terceiros ou chama consumidores segurando locks de I/O.
Callback que bloqueia continua a violar o contrato e pode produzir timeout
explícito; não é promovido a shutdown bem-sucedido por ter sido interrompido.

## 4. Contabilização física e retenção

Os limites internos detalham recursos do componente, não alteram os hard caps
de nó/stream/wire estabelecidos no ADD. Com D≤63, C≤64, Q≤256 e N≤4096:

| Recurso | Limite/retenção da reserva | Prova pendente |
|---|---|---|
| TCP | C reservas do Transport, incluindo tentativa e I/O em terminação; o manager não as liberta ao retirar Peer ou completar um future | D14/D15/D17/D19 |
| Contextos | ≤C; anónimos e em fecho também contam enquanto registados | D06/D17 |
| Barreira | ≤C tokens por Context, ≤C²≤4096 referências; NativeDial retirado logicamente permanece até correlação nativa | D14/D15/D17 |
| Frames retidos | B=min(4,Q) por canal; R=min(N,C×B) no total, R≤256; cobrar antes de inserir e libertar uma vez | D08/D17 |
| DNS/bind | 1 worker local, fila 65, ≤65 jobs físicos totais; ≤1 job por host, destinos configurados e waiters limitados | D12/D15/D17/D19 |
| Tickets | Limite ativo C+1 antes da lista e D+C+1 após a lista; inclui callback retirado ainda a executar, sem exceder o máximo provisionado | D12/D17/D19 |
| Eventos/hooks | N eventos; registos de cleanup finitos; uma falha da fila não cria outra fila ilimitada | D18/D20 |

Future.isDone/isCancelled refere-se ao estado do future, não à morte de um worker,
fim de I/O ou devolução de todas as reservas. Pode existir resultado de dial com
workers TCP vivos, waiter DNS terminado com resolver ainda bloqueado ou callback
de timer cancelado ainda a sair. A revisão não usa esses indicadores como prova
física. São necessários contabilização própria e awaits de cada recurso real.

Retenção B/R cobre Context UNCLASSIFIED e janela legítima de flush. Up só depois
das condições de handshake; Received só depois de Up, na ordem da conexão.
Falha/prazo liberta os retidos e não os transfere para outra instância.
Overflow afeta o Context/candidato corrente com diagnóstico, sem elevar o limite
ou transformar ERROR recebido em ERROR de resposta. Os dados retidos são mensagens
imutáveis já limitadas pelo codec; R não é uma medição de heap ou desempenho.

## 5. Barreira de correlação e APIs existentes

O código atual de Transport inicia Connection.start dentro de attach antes de
completar o future de connect. Logo, callback de Open/Message/Closed pode chegar
antes de DialResult. A interface não identifica direção; resolver isso por
IP/porta ou pelo primeiro socket contrariaria ADR-018/019 e o desenho aprovado.

Registar NativeDial antes de connect permite obter um conjunto conservador de
possíveis donos de cada Open não classificado. O canal só é INBOUND depois de
os resultados desses dials excluírem a associação; resultado com handle é
correlacionado por connectionId/token, mesmo que a tentativa já tenha sido
retirada. Validar direção/alvo/identidade antes de confirmar a sessão.

O wrapper não cancela individualmente o future nativo, para não perder o handle
numa corrida com attach; cerca a tentativa, recebe a conclusão e fecha o canal
tardio. Não inicia outro dial desse peer enquanto o anterior está nativamente
pendente. Transport.cancel continua disponível com a semântica anterior para
os seus chamadores; nenhuma alteração interna/pública de Transport é exigida.

Em shutdown, requestClose fecha todos os sockets e dials independentemente da
barreira; await verifica os workers. Um resultado de canal tardio após Stop ou
falha de oferta à fila é fechado, sem Up. Não apagar uma sessão nova pelo nodeId
de um evento antigo. A espera por outros dials tem custo de latência e pode
consumir o deadline HELLO; D14 testa essa consequência sem renovar o prazo.

## 6. Q-C-05 e Q-C-06 preservadas

Q-C-05: rejeição REGISTER corrente com code 10/11 permite retry com backoff;
outros codes → FAILED. Rejeição HELLO/HELLO_OK correlacionada retira só o candidato;
somente o iniciador permitido faz retry. ERROR inesperado antes de ACTIVE, fora
da janela legítima, gera diagnóstico e fecho **local** do canal ofensor. ERROR
ACTIVE é entregue intacto com sessão; não exige pedido próprio do manager, não
é filtrado pelo code/relatedType e não gera ERROR de resposta.

FAILED por erro da consulta aplica-se ao **canal da consulta corrente**. Um peer
desconhecido que envie ERROR enquanto a Phase global é DISCOVERING não se torna
esse canal: isolar o peer sem terminar uma consulta bootstrap legítima. Esta
precisão é derivada de correlação por conexão/Q-C-05 e isolamento ADR-020;
não é uma nova classificação de codes ou ampliação de FAILED a terceiros.

Q-C-06: usar control.connectTimeout em cada operação local de resolução/bind,
desde antes de entrar na fila. Depois, o dial TCP tem o seu próprio timeout nas
Options, sem herdar um tempo restante de DNS nem obter tempo extra por mudar de
fila. Validar o elapsed quando o resultado é processado; conclusão tardia não
autoriza ressuscitar tentativa ou prolongar deadline. O budget de shutdown é
outro parâmetro, global, aplicado aos awaits físicos.

Se o resolver do sistema ignorar interrupção, pedir cancelamento não o torna
terminado. Reter a reserva até ao fim físico e impedir duplicação de job por
host mantém o limite. Se o worker continuar vivo depois do prazo global, devolver
erro explícito de shutdown; libertação posterior do fake e nova observação não
reescrevem o timeout anterior. Não prometer uma API JDK que mate DNS nativo.

## 7. Wire, âmbito e prova por executar

Continuam framing 37..65536, UTF-8 estrito, 16 tipos ativos, código 3 rejeitado,
HELLO_OK vazio e ERROR com relatedType bruto. Receber ERROR relacionado a tipo
17/desconhecido não obriga a gerar outro erro. UDP OTT1 64/1200/1136 não muda;
não se implementa transporte UDP aqui. NodeRole e nomes Java existentes mantêm-se.

As novas assinaturas e tipos imutáveis pertencem a Neighbor/NeighborManager,
atualmente vazios; fakes/fixtures temporárias ficam em NeighborManagerTest.
Não modificar configuração/POM/codec/transporte/BootstrapServer, fundações ou
testes existentes para acomodar o componente. Sem membership, DNS de entrada
remota, reload do grafo, watchdog UDP, novas arestas ou extras inferidos.

As linhas **D01–D22** do Design permanecem intactas e planeadas. Em particular:
D08 verifica limites/drain/flush; D12 o instante antes da fila e budgets separados;
D14 as intercalações de dial permitidas; D15/D17 as reservas físicas; D18/D20 o
owner partilhado/saturação; D19 o shutdown com recursos bloqueados. D02/D07/D11
mantêm os oráculos ERROR aprovados. D21 é TCP real local; D22 exige regressão
atual/log/XML/JAR, manual repetido pelo humano e revisão/integração para DONE.

## 8. Checkpoint ADR

Resultado desta etapa: revisão de conformidade preparada para aprovação,
reutilização justificada das ADR, Q-C-06 registada e limites/correlação expostos.
Não há divergência material ou pedido de substituição de ADR nesta inspeção;
isso não dispensa testes de implementação nem antecipa a aprovação desta etapa.

Permanecem V-007/D01–D22/regressão/manual, revisão final atribuída a Denji e
integração em main. Sem gates externos adicionais. Bolt/TASK/história continuam
in-progress, completed=null; só Model/Design em stages_completed. Parar para
revisão **antes de Implement/Test**, sem Java, commits/push ou outro IMP.
