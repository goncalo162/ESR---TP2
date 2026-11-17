---
unit: "004-arvores"
bolt: "007-arvores"
stage: "model"
status: "complete"
created: "2026-11-17T15:31:05Z"
updated: "2026-11-17T17:02:14Z"
approved_at: "2026-11-17T17:02:14Z"
---
# IMP-011 — Model: ativação confirmada e procura agregada

✅ Model aprovado explicitamente pelo utilizador em 2026-11-17T17:02:14Z,
incluindo Q-C-34/35. Apenas Design autorizado; ADR/Implement/Test não iniciados.
M01–M14/V-014 continuam NÃO EXECUTADOS. As menções a propostas/aprovação pendente
abaixo conservam o conteúdo do checkpoint original; Q-C-34/35 estão aceites.

Retoma atual: Model/Design/ADR aprovados; apenas Implement autorizado em
2026-11-17T17:50:02Z. [Código/evidência incremental](implementation-walkthrough.md),
sem aceitação de Implement ou avanço a Test. Os parágrafos anteriores são históricos.
Os logs da secção 13 foram preservados em
[evidência duradoura](../../../experiments/construction/007-arvores/neighbor-down-baseline/README.md),
com SHA-256 e cópias byte a byte.
[Investigação consolidada e proposta](../../../experiments/construction/007-arvores/neighbor-down-investigation/README.md)
sem alteração do teste histórico; decisão de correção fora de âmbito pendente.

## 1. Base, fontes e decisões existentes

Início autorizado pela seleção explícita da opção 1 nesta sessão. Base do
checkout: `7164c9b`, com árvore limpa antes de iniciar este bolt. Inception
[concluído](../../intents/001-ott-streaming/inception-review.md);
[022-rotas](../022-rotas/closure-walkthrough.md) e unidade 003-rotas complete,
com implementação integrada em `3349d575ae38c772b38496cc08bc1d44ded764ff`.

1 - ✅ **Fonte normativa**: [ADD, IMP-011](../../../add.md#imp-011),
secções 6–9 e 11, com os requisitos de [specs.md](../../../specs.md).
2 - ✅ **ADR-005**: uma procura agregada, confirmação por ACK, revisões e
validade das subscrições; a implementação completa de leases/poda é IMP-012.
3 - ✅ **ADR-018**: estado volátil, separado por stream e época, associado à
sessão concreta do vizinho; reinício não recupera subscrições antigas.
4 - ✅ **ADR-024**: um IMP por bolt, âmbito rastreável e aprovação por etapa.
5 - ✅ **Restrições transversais**: ADR-004/014/015/016/017/020/021/026:
path vector, I/O assíncrono, aceitação de envio distinta de ACK, parâmetros
centrais, owner serial existente, erros tipados e validação de fronteiras.
ADR-034/035 mantêm TCP v2 uniforme e sessões reparadoras correntes.

Detalhe incorporado: [modelo de dados](../../../docs/06-DATA-MODEL.md),
[interfaces, tipos 11–14/17](../../../docs/07-API-AND-INTERFACES.md),
[TASK-014](../../../docs/tasks/PHASE-04/TASK-014.md), história 001 e brief da
[unidade](../../intents/001-ott-streaming/units/004-arvores/unit-brief.md).
Este documento deriva dessas fontes; não cria uma arquitetura concorrente.

Nenhum ficheiro de código-fonte foi lido, analisado ou alterado nesta etapa.
Os nomes seguintes são conceitos de domínio, não assinaturas Java aprovadas.

## 2. Contexto delimitado e fronteiras do incremento

O contexto é o controlo de procura de uma stream num nó overlay: receber
intenção local ou ACTIVATE de um filho, validar o caminho, agregar pedidos,
obter confirmação da origem e publicar apenas ramos confirmados.

Entradas: oferta/candidatos e época já mantidos pelo routing, sessão direta
corrente, intenção identificada de consumidor, mensagens de ativação e
ACK/ERROR correlacionados, cancelamento e eventos de prazo/fecho.
Saídas: alterações atómicas de estado, intenções de envio de controlo,
resultado local e snapshot imutável de confirmação.

Incluído: ACTIVATE/ACK/ERROR, primeira ativação e adesão compatível a ramo já
confirmado, idempotência, falha/timeout da tentativa, invalidação de ACK antigo
e representação de lease positivo/validade da confirmação. A origem confirma
a sua oferta local sem criar parent.

Excluído deste IMP: política completa de DEACTIVATE/RENEW, renovação periódica
e poda propagada (IMP-012), recuperação automática da árvore (IMP-013), troca
de parent por histerese (IMP-014), CLI play/stop, GUI e dados UDP/vídeo.
O cancelamento mínimo necessário para impedir uma ativação obsoleta está
explicitado como proposta Q-C-34 na secção 12.

Não há base de dados, HTTP, novo serviço de bootstrap, novo executor dono de
estado ou mudança de wire. Os quatro caminhos previstos em IMP-011 continuam
a ser os únicos alvos Java; a etapa ADR terá de conferir a viabilidade real
antes de Implement. Necessidade de outro caminho exige justificação de âmbito.

## 3. Entidades

1 - **Árvore local por stream (Route)**. Identidade: streamId, com serverId
fixo e época corrente. Contém seleção de caminho, confirmação a montante,
tentativa pendente, subscrições a jusante e conjunto de consumidores locais.
Seleção, pendência e confirmação são informações distintas. Um anúncio não
cria procura nem autorização de dados. A fonte não possui upstream.

2 - **Subscrição a jusante (Subscription)**. Identidade de contexto:
streamId, serverEpoch, sessão do filho, subscriptionId e revision. Contém
prefixo esperado, pedido/lease solicitado, correlação de resposta, estado
PENDING/ACTIVE e validade. Há no máximo uma subscrição efetiva por filho e
stream na sessão corrente. Não existe um destino por espectador final.

3 - **Referência agregada a montante**. Pertence à Route e representa toda a
procura local e dos filhos dessa stream. Usa subscriptionId e revision do
próprio nó, independentes das identidades dos pedidos dos filhos. Guarda a
sessão do parent e o path/época que foram efetivamente confirmados. Num relay,
dois consumidores não criam duas referências a montante.

4 - **Tentativa de ativação**. Pertence à referência agregada. Identificada
por requestId, subscriptionId, revision, streamId, época e sessão escolhida.
Guarda path esperado, início monotónico, prazo, estado terminal e número da
tentativa na ronda limitada. Só uma tentativa lógica corrente por stream na
ativação inicial; callbacks físicos de envios antigos não são uma segunda
tentativa válida nem podem alterar a atual.

5 - **Intenção de consumo local**. Identificada por um token estável dentro
da execução do nó, separada do streamId. Inscrever duas vezes o mesmo token
é idempotente; retirar um token conserva os outros. Intenção pode sobreviver
a NO_ROUTE, mas não equivale a uma subscrição confirmada. Stop remove a intenção
correspondente e impede que uma resposta antiga volte a criá-la.

6 - **Contexto de resposta de filho**. Associa a sessão admitida e requestId
original à subscrição/revisão que aguarda resultado. O ACK de montante não é
reenviado literalmente: cada filho recebe os seus próprios campos e requestId.
Duplicados não podem criar uma lista ilimitada de respostas ou reservas.

## 4. Objetos de valor e identidade

1 - **StreamKey**: UUID da stream; título e época isolados não são chaves.
S1/A e S1/B podem partilhar serverEpoch e continuam independentes.
2 - **SessionKey**: nodeId, instanceId e connectionId da sessão direta
confirmada. Um evento da conexão anterior não afeta uma conexão substituta.
3 - **Caminho esperado**: lista simples, origem → emissor imediato, até
32 IDs, com IDs válidos. A mensagem de C para P transporta [S,...,P,C];
P retira C e compara o prefixo com o seu caminho e a época correntes.
4 - **Correlação da tentativa**: requestId + sessão + streamId + época +
subscriptionId + revision. ERROR não contém todos esses campos no payload;
a sua correlação usa o contexto guardado e relatedType ACTIVATE.
5 - **Revisão**: versão crescente da subscrição no âmbito da sua identidade.
Repetição igual é idempotente; uma revisão anterior não cancela nem confirma
a atual. Overflow não dá a volta silenciosamente; deve terminar explicitamente.
6 - **Lease/validade**: duração positiva e horizonte monotónico local.
Concessão nunca excede o pedido ou o máximo local. Duplicados não renovam a
validade; uma confirmação expirada não aparece como ramo utilizável.
7 - **Resultado**: sucesso confirmado ou motivo tipado/cancelamento/fecho,
com um único terminal por tentativa; não representa entrega de vídeo.

instanceId e serverEpoch distinguem contextos diferentes; UUIDs não são
ordenados para inferir qual execução é mais recente. Tempos civis servem logs;
deadlines, expiração e comparação de validade usam relógio monotónico local.

## 5. Agregados e invariantes

A raiz conceptual é a Route por stream, pertencente ao owner serial existente.
Atualização de tentativa, filhos e snapshot ocorre numa única decisão desse
owner. I/O não altera mapas diretamente nem executa código arbitrário do
consumidor no owner. A projeção de seleção anterior conserva o seu significado.

1 - **Confirmação**: nó não fonte só publica upstream confirmado após ACK
válido da tentativa corrente. ACCEPTED e sendAndFlush não são ACK.
2 - **Agregação**: procura é a união de tokens locais e filhos válidos, com
PENDING distinto de ACTIVE. A passagem de zero para procura positiva inicia
uma referência; adesões compatíveis agregam-se à mesma referência.
3 - **Destinos**: apenas filhos confirmados, com sessão/época e validade
correntes. PENDING, próprio nó e upstream não entram nos destinos.
4 - **Isolamento**: toda mutação tem streamId; uma falha de A não remove B
da mesma origem. Mudança de época invalida o contexto antigo só dessa origem.
5 - **Caminho**: novo pedido incompatível é rejeitado antes de agregar
procura ou alterar um ramo saudável. Loops e caminho sem o destinatário na
posição correta não originam ativação.
6 - **Idempotência**: duplicar ACTIVATE não cria novo destino, nova referência
ou extensão de prazo. Mesma identidade/revisão com conteúdo incompatível não
substitui silenciosamente o estado existente.
7 - **Obsolescência**: ACK/ERROR/cancelamento de sessão, época, request ou
revisão anteriores não altera a tentativa atual. Uma tentativa terminal não
regressa a PENDING; a proteção contra replay tem de ter retenção limitada e
explicitada no Design, sem evicção de estado ainda protegido.
8 - **Prazos**: todos os PENDING têm prazo finito. Duplicados e callbacks não
reiniciam o prazo; a ronda usa no máximo routing.activate.attempts, sem
recriar rondas em ciclo por um erro ou callback antigo.
9 - **Falha localizada**: um pedido inválido, falta de capacidade ou filho
perdido não destrói procura confirmada dos restantes filhos/streams.
10 - **Validade**: nenhuma leitura do snapshot autoriza estado expirado;
a validade da confirmação é distinta do TTL dos anúncios e da cache de flooding.
11 - **Recursos físicos**: terminar a tentativa lógica não prova que o envio
ou callback terminou. As reservas físicas só são libertadas quando isso se
confirma, respeitando os contratos de transporte e shutdown anteriores.

## 6. Serviços de domínio e interfaces de repositório

1 - **Admissão de procura**: validar identidade, stream, sessão, epoch, path,
lease e revisão; agregar intenção/filho ou rejeitar antes da mutação.
2 - **Coordenação de ativação**: escolher candidato elegível do routing,
iniciar a tentativa agregada e tratar ACK/ERROR/prazo sem espera bloqueante.
3 - **Confirmação e publicação**: conferir a tentativa inteira, revalidar
procura/caminho/sessão e publicar upstream/filhos confirmados atomicamente;
produzir respostas com a correlação própria de cada filho.
4 - **Invalidação**: retirar provisórios e invalidar tokens/tentativas em
cancelamento, epoch/sessão substituída ou shutdown. Recuperação automática e
troca planeada de parent permanecem nos incrementos seguintes.
5 - **Repositório volátil de árvores**: obter estado de uma stream, aplicar
uma transição no owner, consultar pendências vencidas e publicar snapshot
imutável. É um contrato conceptual sobre memória já pertencente ao nó;
não implica criar uma classe Repository, persistência ou executor adicional.
6 - **Portas externas ao agregado**: consulta de candidatos/oferta/sessões,
relógio monotónico, agendamento e envio de controlo assíncrono. Interfaces
Java e números concretos de capacidade serão entregues no Design e revistos
contra as APIs reais na etapa permitida. Sem novos hard caps ou bibliotecas
introduzidos apenas por este Model.

## 7. Eventos de domínio

Os nomes abaixo descrevem transições observáveis, não tipos wire novos:

1 - **ProcuraAdmitida/Retirada**: token local ou contexto do filho e stream;
alteração idempotente do conjunto correspondente.
2 - **AtivacaoIniciada**: stream/época, sessão upstream, identidade/revisão,
requestId, path e prazo. Um evento por tentativa efetivamente iniciada.
3 - **AtivacaoConfirmada**: correlação válida e lease concedido; publicação
de confirmação e resolução dos filhos/consumidores ainda interessados.
4 - **AtivacaoTerminada**: erro, timeout, cancelamento, fecho ou obsolescência;
limpa os provisórios da tentativa e resolve pendências sem afetar outra stream.
5 - **FilhoConfirmado/Removido**: sessão e identidade/revisão próprias do
filho, nunca uma cópia da identidade da referência agregada do parent.
6 - **RespostaObsoletaIgnorada**: razão e correlação limitada para diagnóstico;
não altera procura, prazos ou confirmação.

Os motivos reutilizam UNKNOWN_STREAM, NO_ROUTE, PATH_CHANGED, LOOP_DETECTED,
STALE_REVISION, TIMEOUT, RESOURCE_LIMIT e SOURCE_UNAVAILABLE quando aplicáveis.
Não se muda ErrorCode nem se envia um ERROR em resposta a outro ERROR.
Falha local de envio e envelope sem contexto seguro conservam tratamento local.

## 8. Sequências e transições

### 8.1. Primeira procura e tronco comum

Na árvore S–R–{C1,C2}, C1 envia ACTIVATE para R com [S,R,C1]. R valida
[S,R], guarda C1 PENDING e envia um pedido próprio a S com [S,R]. Se C2 chega
antes do ACK, fica PENDING na mesma referência. S confirma a oferta e responde
com a identidade do pedido de R. R valida o ACK, confirma o seu upstream e
produz os ACK de C1/C2 com as identidades e requestId de cada um. Cada cliente
só confirma o seu upstream após o próprio ACK válido.

Se C2 chega depois da confirmação, R só usa a confirmação existente se a
epoch, o prefixo, a sessão e a validade forem compatíveis. Não envia uma nova
ativação a S apenas porque existe um segundo cliente. Em R, um consumidor
local adicional também partilha a mesma referência.

### 8.2. Fonte e ausência de caminho

Na origem, oferta local válida constitui a autoridade da confirmação; não
é necessário um ACK da própria origem para si mesma. Confirma cada filho
admitido, sem abrir upstream ou afetar outra stream. Ausência de oferta não
é sucesso só porque o processo tem papel SERVER.

Stream desconhecida distingue-se de stream conhecida sem candidato elegível.
Um candidato sem RTT não se transforma num caminho de custo zero. Intenção
local pode ficar registada como indisponível; filho que recebe erro terminal
não conserva uma ativação PENDING sem prazo ou uma subscrição ACTIVE fictícia.

### 8.3. Estados da Route e da Subscription

1 - UNAVAILABLE: sem confirmação utilizável nem candidato elegível; intenção
local pode existir sem dados autorizados.
2 - INACTIVE: candidato elegível sem ramo confirmado nem tentativa corrente.
3 - ACTIVATING: primeira procura com tentativa agregada PENDING.
4 - ACTIVE: confirmação utilizável para a procura admitida; a fonte tem
confirmação local e parent ausente.
5 - SWITCHING: estado reservado à troca confirmada de IMP-013/014;
este incremento não implementa o algoritmo de troca.

Subscription passa PENDING → ACTIVE somente por decisão válida da origem ou
ACK válido de montante. Erro, timeout ou cancelamento termina/remove PENDING.
Uma nova tentativa tem correlação nova; não ressuscita o registo terminal.
Resultado de transporte nunca faz a transição PENDING → ACTIVE.

### 8.4. Timeout, erro e cancelamento concorrentes

O prazo inclui a espera nas filas locais: não começa apenas quando o peer
responde ou o envio é aceite. Ao vencer, invalida-se a tentativa, removem-se
provisórios e resolve-se cada pedido com a sua correlação. Nova tentativa,
se elegível, respeita a ronda limitada e revalida caminho/epoch/sessão.

Se um filho cancela antes do ACK, esse filho não é confirmado. A outra procura
continua agregada; se não resta procura, a tentativa deixa de poder confirmar.
Se ACK e cancelamento competem, a ordem do owner define o estado: ACK após
cancelamento é obsoleto; cancelamento após confirmação retira a autorização
local correspondente. Não se presume que cancelar desfaça bytes já enviados.
Poda de recursos remotos e DEACTIVATE completo continuam em IMP-012.

Se a sessão/epoch deixa de ser corrente, nenhuma resposta anterior confirma
a substituta. Não se promete recuperar automaticamente a árvore neste IMP.
Um snapshot de seleção mais recente não substitui, por si só, o parent ativo.

## 9. Snapshots e compatibilidade

O snapshot conceptual de confirmação tem streamId/serverId/epoch, estado,
upstream confirmado opcional, path confirmado, destinos confirmados e
entrega local confirmada, com validade monotónica e revisão local.
Não expõe mapas mutáveis nem transforma PENDING em destino ativo.

A informação selecionada continua disponível separadamente para routing e
trace. Métrica, catálogo e ANNOUNCE não são confirmação da árvore. A forma de
compor projeções sem alterar o significado das APIs anteriores será definida
no Design. Não se alarga silenciosamente a validade do catálogo nem se muda
a política de gerações/retiradas aprovada em Q-C-29.

## 10. V-014 e casos críticos planeados

Todos os cenários seguintes estão NÃO EXECUTADOS; são oráculos de desenho,
não nomes ou quantidades de testes Java já existentes.

1 - **M01 / tronco partilhado**: S–R–C1/C2, adesão antes/depois do ACK;
uma referência agregada, identidades por filho e dois destinos confirmados.
2 - **M02 / consumidores locais**: tokens distintos e repetidos;
o segundo não duplica upstream, retirar um conserva o outro.
3 - **M03 / fonte**: confirmação direta da oferta, nenhum parent;
oferta ausente devolve indisponibilidade sem afetar outra oferta.
4 - **M04 / sem ACK**: envio ACCEPTED/flush concluído não ativa a árvore;
PENDING não aparece no snapshot confirmado.
5 - **M05 / prefixo**: caminho desatualizado ou incompatível produz
PATH_CHANGED antes de mutação; loop não dispara ativação a montante.
6 - **M06 / correlação**: ACK de outro request, stream, epoch,
subscriptionId, revision ou Session não confirma a tentativa atual.
7 - **M07 / cancelamento e ACK tardio**: cancelar filho/última procura,
timeout e confirmação em ordens opostas; terminal único e sem reativação.
8 - **M08 / revisões e duplicados**: repetição não duplica referência,
destino, prazo ou lease; revisão velha não remove a nova.
9 - **M09 / ERROR e prazo**: resposta traduzida para cada requestId dos
filhos; erro/prorrogação indevida não deixa PENDING eterno.
10 - **M10 / isolamento**: duas streams da mesma origem/epoch e streams
de origens diferentes; ativação, erro e cancelamento não cruzam estados.
11 - **M11 / sessão e epoch**: reconexão/época substituída, respostas
antigas, sessão reparadora corrente; sem herdar uma confirmação anterior.
12 - **M12 / validade**: lease nulo/excedido, duplicados e confirmação
expirada nunca autorizam dados; negociação/renovação completa é V-015.
13 - **M13 / capacidade**: admissões e respostas limitadas, ausência de
evicção protegida, fila cheia e child lento preservam estado saudável.
14 - **M14 / lifecycle**: callback tardio, shutdown com envio pendente,
libertação física comprovada e ausência de bloqueio do owner.

M01/M04/M06/M07/M05/M10 cobrem diretamente os quatro oráculos V-014 do ADD.
M02/M03/M08/M09/M11–M14 cobrem idempotência, recursos e casos limite da TASK.
REQ-005/007/011/017 só terão prova parcial de controlo; replicação real de
vídeo e cumprimento global continuam nos incrementos de streaming/CORE.

## 11. Linguagem ubíqua

1 - **Selecionado**: caminho preferido pelo routing, sem autorização de dados.
2 - **Confirmado**: ramo autorizado por confirmação contextual ainda válida.
3 - **Parent/upstream**: vizinho em direção à origem, ausente na fonte.
4 - **Filho/downstream**: vizinho com procura admitida nessa stream.
5 - **Procura agregada**: união de consumidores locais e filhos, com estados
pendente/confirmado preservados; não um contador incrementado por mensagens.
6 - **Tronco comum**: ligação que serve vários consumidores da mesma stream.
7 - **Tentativa**: pedido correlacionado com prazo finito dentro de uma ronda.
8 - **Ronda**: conjunto limitado de tentativas, sem retries infinitos.
9 - **Revisão**: versão da subscrição; não é sequência de anúncio nem epoch.
10 - **Confirmação local**: decisão da origem sobre oferta própria;
não é medição de FPS nem prova de receção de vídeo.

## 12. Propostas para o checkpoint e matérias de Design

**Q-C-34 — Fronteira com IMP-012 (proposta, não aprovada).** Incluir em
IMP-011 apenas cancelamento/invalidação lógicos e validade mínima necessários
para provar que ACK tardio/cancelado não ativa. Isto inclui retirar uma procura
local identificada e excluir confirmações expiradas da projeção utilizável.
Não inclui CLI stop, renovação periódica, algoritmo completo de DEACTIVATE
ou poda remota. Esses comportamentos mantêm-se em IMP-012; antes disso não
se promete uma transmissão duradoura ou ausência de ramos remotos até ao lease.

**Q-C-35 — Seleção e confirmação divergentes (proposta, não aprovada).**
Na ativação inicial revalidar o caminho/epoch antes de confirmar. Para adesão
a ramo já ativo, exigir compatibilidade com o prefixo selecionado e o prefixo
efetivamente confirmado. Se divergem, responder PATH_CHANGED sem trocar parent
ou remover procura confirmada saudável. IMP-013/014 tratarão a troca; não se
apresenta a atualização da seleção como uma troca de árvore já implementada.

Detalhes para Design: capacidades por stream/sessão/owner e tokens locais;
política de revisões/identidades retiradas e retenção contra replay; duplicados
com requestId distinto sem fila ilimitada; duração concedida ao filho quando
limitada pela validade remanescente a montante; novas tentativas elegíveis e
limite total da ronda; ordem de publicação/ACK/falha de saída; rejeição em
backpressure; tarefas físicas e integração com lifecycle/scheduler existentes.
Não se fixam valores novos nem assinaturas por inferência do código antigo.
Escolhas materiais serão propostas no Design antes de Implement.

## 13. Evidência anterior e condição de retoma

A análise anterior desta sessão, antes de iniciar Model, executou verify em
Java 21: 1296 testes, 1 falha, 0 erros, 0 ignorados. A repetição isolada falhou
na mesma asserção de observação de NeighborDown. Logs existentes nessa sessão:
`/tmp/esr-tp2-review-verify-unrestricted.log` e
`/tmp/esr-tp2-review-monitoring-recheck.log`; são ficheiros temporários locais,
não artefactos versionados ou garantia de disponibilidade futura.
O primeiro ensaio restrito teve erros de sockets e não serve como prova de
defeito. O resultado de 1 falha é do ensaio com sockets autorizados.

Nenhuma causa foi estabelecida. Não se declara a regressão verde nem se altera
o fecho histórico de 022-rotas. Esta etapa documental pode avançar; a falha
precisa de investigação na etapa que permita código e antes da aceitação de
Implement/Test. Qualquer correção fora dos quatro caminhos exige revisão de
âmbito, sem apagar/enfraquecer o teste ou mascarar o resultado anterior.

Verificação documental executada em 2026-11-17T15:36:08Z:
`python3 scripts/validate-planning.py`, saída 0, zero erros, 4843 referências
locais/âncoras verificadas, 35 IMP/bolts/histórias e DAG sem ciclos. O script
também conferiu as 78 fixtures e a sintaxe dos três XML, sem executar CORE.
`git diff --check` passou; o diff de Projeto/ e CORE/ está vazio. Não houve
build ou teste Java novo nesta etapa. Estes checks não executam V-014 nem
aprovam Model ou as propostas Q-C-34/35.

Retoma: rever este Model e Q-C-34/35. Só após aprovação explícita registar
model em stages_completed e avançar para Design. Os restantes bolts continuam
planned; revisão final atribuída a Waheed, V-014, integração e fecho pendentes.
