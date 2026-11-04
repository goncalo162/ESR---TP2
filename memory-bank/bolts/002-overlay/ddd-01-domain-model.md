# IMP-003 — Model: descoberta inicial e escrita confirmada

Bolt 002-overlay; unidade 002-overlay; origem TASK-006. O utilizador selecionou
este bolt e autorizou inicialmente apenas a primeira etapa. Model aprovado
explicitamente em 2026-11-04T10:50:52Z (registo), incluindo coerência de instância
e uma decisão de resposta por conexão, em [Q-C-04](../../../docs/construction-duvidas.md#q-c-04--coerência-da-instância-e-resposta-única-na-consulta-inicial).
[Design aprovado](ddd-02-technical-design.md) em 2026-11-04T11:25:40Z; continuação
[revisão ADR aprovada](adr-review.md) em 2026-11-04T11:37:45Z, autorizando inicialmente
Implement, depois aprovado em 2026-11-04T12:18:01Z; [Test para revisão](ddd-03-test-report.md).
As descrições da fronteira/casos abaixo são o modelo, não implementação ou V-006 executado.

Fontes, pela precedência aprovada: [enunciado, pp. 2–3](../../../ESR-TP2-26-27.pdf),
[REQ-002–004](../../../specs.md#req-002),
[intent](../../intents/001-ott-streaming/requirements.md),
[ADD 6](../../../add.md#6-modelo-de-dados-e-contratos),
[IMP-003](../../../add.md#imp-003),
[TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md),
[contrato v1](../../../docs/07-API-AND-INTERFACES.md#3-tcp-framing-e-tipos-primitivos)
e [V-006](../../../docs/09-TESTING-STRATEGY.md#v-006--fornecer-localização-inicial-dos-vizinhos-configurados).
ADR aplicáveis: 003 (histórica/substituída), 013, 014, 015 e 024. O modelo
reutiliza também as invariantes transversais 010/012/016/017/018/020/021/026;
a revisão formal das ADR permanece na terceira etapa.

## 1. Base real, dependências e fronteira

Base de partida: Denji, HEAD 4ac5808dacf6e97bfd3636d2788db363e926bc95,
igual a main e às referências locais de origin; árvore inicialmente limpa.
Inception está concluído para este âmbito. Os bolts 001-base-validada e
019-overlay estão complete e integrados. TASK-003/005/032 estão DONE.
Os 824 testes e os 25 checks manuais de Luis pertencem à validação aprovada
do IMP-002; não foram reexecutados para preparar este modelo.

Observação do código:

- ConfigurationLoader fornece NodeConfiguration, RuntimeConfiguration e
  TopologyConfiguration imutáveis, com grafo/endpoints/papéis validados antes
  de rede. Os construtores públicos dos records não substituem a validação
  semântica do loader; não assumir que qualquer record construído manualmente
  representa uma configuração válida para abrir sockets.
- BootstrapServer.java contém apenas um comentário; BootstrapServerTest.java
  está vazio. Não existe NodeRegistry nem serviço bootstrap executável.
- Transport já oferece connect assíncrono, callbacks ordenados por ligação,
  limites, cancelamento de dial e shutdown com prazo. Connection tem um leitor,
  um escritor e uma fila limitada; send significa apenas enfileiramento.
- Connection.close limpa a fila e interrompe I/O; writeLoop escreve e faz flush,
  mas não expõe confirmação. Falta sendAndFlush, prevista pela ADR-015.
  Esta lacuna é o trabalho autorizado do IMP-003, não uma contradição do ADD.
- ControlMessage já contém EndpointPayload, Endpoint, NeighborList e ErrorPayload;
  codec/enums/fixtures do wire v1 são reutilizados sem alteração.

Ficheiros de aplicação autorizados para Implement, ainda sem alterações:

| Ficheiro relativo a Projeto/src | Intervenção futura exigida pelo IMP |
|---|---|
| main/java/pt/uminho/esr/ott/bootstrap/BootstrapServer.java | Consulta e ciclo de vida limitados do bootstrap |
| main/java/pt/uminho/esr/ott/transport/Connection.java | Acrescentar sendAndFlush e terminar confirmações em todos os fechos |
| main/java/pt/uminho/esr/ott/transport/Transport.java | Integração de lifecycle/recursos estritamente necessária à extensão |
| test/java/pt/uminho/esr/ott/bootstrap/BootstrapServerTest.java | Casos V-006 e isolamento das consultas |
| test/java/pt/uminho/esr/ott/transport/TransportTest.java | Casos novos da escrita confirmada, preservando os 18 existentes |

Não se implementam CLI/OttApplication, NeighborManager/HELLO, reconexão direta,
monitorização, rotas, streams ou novas vizinhanças neste incremento. A composição
do processo pertence ao IMP-005; testes futuros podem compor bootstrap/transporte
diretamente nos ficheiros autorizados, sem fingir essa aplicação pronta.

## 2. Entidades, valores e identidades

| Elemento | Identidade e proprietário | Invariantes |
|---|---|---|
| Configuração bootstrap | NodeConfiguration de papel BOOTSTRAP | Topologia presente, dados e opções válidos antes do bind; não tem endpoint UDP |
| Grafo permitido | TopologyConfiguration.nodes: nodeId → Node | Imutável; IDs, endpoints, papéis e arestas autorizados; sem disponibilidade |
| Node configurado | nodeId estável; valor do grafo | SERVER/RELAY/CLIENT; conjunto de vizinhos define X daquele nó |
| Endpoint do pedido | Endpoint existente, pertencente ao requerente | Identidade/host/portas/papel coerentes com o nó configurado; instanceId não zero |
| Consulta | connectionId local e, após descodificação, requestId | Contexto efémero por ligação; não representa inscrição ou membro vivo |
| Instância requerente | senderInstance/Endpoint.instanceId | Distingue execuções no pedido, sem exclusividade ou atualização do grafo |
| Lista devolvida | NeighborList com Endpoint dos adjacentes | Cópia imutável da configuração; cada instanceId é UUID zero; sem lease |
| Confirmação de saída | Uma tentativa local de enfileirar/escrever um frame | Future termina uma vez em sucesso/erro/cancelamento; sucesso só após write/flush |
| Erro | ErrorPayload ou razão local existente | Correlação segura, sem stack trace/path; falha limitada à ligação |

nodeId é a chave da pesquisa, conforme CP-01. O IP observado no socket serve
para diagnóstico e não substitui o envelope ou o endpoint autorizado. S1, S2,
R1 e C1 do perfil local partilham 127.0.0.1 e têm identidades/portas diferentes.
Não se faz descoberta por IP, sondagem dos vizinhos ou resolução DNS a partir
dos campos recebidos para escolher/reescrever a configuração.

Concretização aprovada em Q-C-04: a coerência da identidade inclui igualdade de
senderInstance e Endpoint.instanceId: ambos descrevem o mesmo emissor imediato.
Isto é validação contextual, pois o codec valida os campos separadamente.
A comparação do host é lexical **após normalização**, conforme docs/07 §1:
ConfigurationLoader.loadDeployment compara valores já normalizados na carga.
O host bruto do pedido também deve respeitar caixa/ponto final/IPv4 decimal;
[Design](ddd-02-technical-design.md#2-validação-antes-do-bind-e-comparação-lexical-de-hosts)
explicita essa regra existente, sem DNS ou aliases baseados no IP remoto.

## 3. Consulta como função do grafo

Para um pedido REGISTER válido do nó N, a resposta contém exatamente
G.nodes[N].neighbors, convertidos em endpoints a partir de G.nodes. O pedido
nunca fornece a lista, não altera endpoints e não cria arestas. X é a dimensão
da lista daquele nó, não um valor global fixo em três. O loader limita a
topologia a 64 nós, sem auto-vizinhança; numa topologia válida X ≤ 63.
O limite binário existente da lista continua 64; não mudar o codec para impor
uma regra de grafo que pertence à configuração/handler.

Exemplos do grafo local carregado:

| Requerente | Resultado configurado, independentemente de processos ativos |
|---|---|
| S1 | S2, R1 |
| S2 | S1, R1 |
| R1 | S1, S2, C1 |
| C1 | R1 |

Uma nova consulta com o mesmo nodeId, outro requestId, outra ligação ou outra
instanceId válida devolve o mesmo conteúdo configurado. A ordem de chegada
dos pedidos e o histórico de consultas não modificam o resultado. A correlação
requestId é preservada em REGISTER_OK ou ERROR; o emissor da resposta é o
bootstrap, não o requerente nem um dos vizinhos.

Cada vizinho devolvido usa instanceId zero, incluindo se estiver ativo e mesmo
que tenha consultado antes. A instância viva só será conhecida no HELLO direto
do IMP-004. Não criar cache de consultas, registo vivo, lease, monitorização,
renovação ou processamento de NODE_LEAVE pelo bootstrap.

## 4. Fronteiras de validação e erros

1. Configuração: validar o perfil, o papel, o grafo e as opções antes de abrir
   listener/threads. O bootstrap consome apenas o grupo Control aplicável ao papel.
   Topologia assimétrica ou inválida não é reparada por pedidos recebidos.
2. Transporte/codec: validar framing, UTF-8, payloads, limites e domínios existentes.
   Uma conexão truncada/malformada não fornece necessariamente um envelope seguro;
   nesse caso fechar com razão local, sem inventar ERROR nem alterar MessageCodec.
3. Contexto: aceitar somente REGISTER; verificar senderId=Endpoint.nodeId,
   existência desse ID e coerência de instância/host/portas/papel com a configuração.
   As portas do endpoint são as publicitadas, não a porta efémera de origem TCP.
4. Tipos v1 válidos mas alheios, incluindo NODE_LEAVE, HELLO, sondas, renovação
   de subscrição, anúncios e ERROR, não executam operações no bootstrap. Quando
   o pedido tem contexto seguro, responder ERROR INVALID_MESSAGE e fechar após
   escrita completa; não entrar num ciclo de respostas ERROR.
5. ID/endpoint não autorizado produz rejeição contextual, com código existente
   apropriado e sem mutação. Design fixará o mapeamento entre erros de coerência
   e UNAUTHORIZED_NEIGHBOR, sem criar códigos/formatos nem alegar autenticação.

Os detalhes de erro são curtos e seguros, relacionados com o tipo bruto do pedido
e o seu requestId quando disponíveis. Falha/timeout/fecho de uma consulta não
derruba o listener nem impede consultas independentes. Duas instâncias do mesmo
nodeId não geram DUPLICATE_ID no bootstrap; essa arbitragem pertence ao HELLO.

## 5. Eventos, estados e propriedade

Estados abaixo são conceitos do contexto de consulta, não novos enums no wire
nem estados da futura tabela de vizinhos.

| Estado | Evento | Efeito/transição exigidos |
|---|---|---|
| AWAITING_REQUEST | Conexão aberta | Criar contexto limitado e prazo total da consulta |
| AWAITING_REQUEST | REGISTER válido | Derivar resposta do grafo, iniciar escrita; WRITING_RESPONSE |
| AWAITING_REQUEST | Operação/contexto rejeitado e resposta segura | Preparar ERROR, iniciar escrita; WRITING_RESPONSE |
| AWAITING_REQUEST | Protocolo inseguro, EOF ou prazo | Fechar/remover contexto; CLOSED |
| WRITING_RESPONSE | Escritor confirma frame completo e flush | Fechar a conexão de consulta; CLOSED |
| WRITING_RESPONSE | Escrita falha/cancela, EOF, timeout ou shutdown | Fechar e terminar confirmações/contexto; CLOSED |
| WRITING_RESPONSE | Outro pedido na mesma ligação | Não iniciar outra operação nem acumular respostas; preservar limite e prazo |
| CLOSED | Evento atrasado/duplicado | Nenhuma nova resposta, inscrição ou remoção de outro contexto |

Concretização aprovada em Q-C-04: uma decisão de resposta por conexão. Após iniciar
a resposta, mensagens adicionais não geram novas consultas nessa conexão.
Design definirá o descarte/fecho compatível com o envio completo já pendente;
uma requisição extra nunca prolonga o prazo nem transforma o canal em sessão.

Estado operacional do bootstrap: somente contextos efémeros de ligação/escrita,
num proprietário serial, separado do grafo imutável. Callbacks do leitor,
escritor/future e timer publicam eventos e regressam sem aguardar escrita,
ACK, joins ou espaço em filas. Eventos identificam connectionId e contexto
esperado; não usar apenas nodeId para apagar/substituir contexto, pois duas
consultas legítimas do mesmo ID podem coexistir. Instância do pedido é dado
de coerência, não índice de disponibilidade.

Connection mantém a propriedade de socket/fila/frame em escrita no transporte;
somente o escritor resolve o sucesso de escrita. Design terá de explicitar
ordem de locks e entrega de conclusões/callbacks, evitando continuations que
bloqueiem ou executem shutdown sob locks incompatíveis. Este modelo não cria
executor público, classe de registry ou novo componente fora dos cinco ficheiros.

## 6. Escrita confirmada, prazo e recursos

A interface imposta pela ADR-015 é
Connection.sendAndFlush(ControlMessage): CompletableFuture<Void>.
Connection.send e os seus resultados permanecem compatíveis. Não se usa
queuedMessages=0, ACCEPTED, sleep ou um novo ACK remoto para decidir o fecho.

Uma confirmação distingue fila de frame em escrita. O future só conclui com
sucesso quando o escritor termina write e flush desse frame. Sucesso significa
escrita local concluída, sem garantir receção/consumo remoto. Mensagem de saída
inválida produz conclusão excecional de formato e preserva ligação saudável;
overflow/fecho/I/O/shutdown terminam confirmações com erro/razão explícita.
Cancelamento é terminal para o future, sem prometer retirar bytes já escritos.
Após fechar, não podem ficar futures pendentes, nem para o frame já retirado
da fila pelo escritor. Corridas não podem converter erro/cancelamento em sucesso.

Prazo da consulta: bootstrap.query.timeout.ms, default 3000 ms, total após a
ligação, sem reinício por bytes parciais ou mensagens repetidas. Expiração fecha
o socket, termina a escrita pendente e liberta o contexto; não bloqueia callbacks.
Design explicitará como aplicar/testar o prazo ao contexto e à confirmação sem
introduzir propriedades ou timers de membership. O timeout de dial (2000 ms)
e o deadline de shutdown (3000 ms) têm funções distintas.

Limites existentes a preservar: control.max.connections ≤ 64 (reservas também
em terminação), control.queue.capacity ≤ 256 por saída, corpo TCP 37..65536
e prefixo de quatro bytes. Há no máximo a fila admitida mais um frame em escrita
por conexão; o bookkeeping das confirmações tem de respeitar esse limite,
sem lista histórica de futures. Contextos/timers acompanham apenas ligações
admitidas e são removidos em fecho; filas de eventos também têm limite explícito
compatível com o ADD (teto 4096), sem aceitar overrides overlay num BOOTSTRAP.
Design fixará organização/injeção e limpeza de timers cancelados.

Shutdown fecha listener, consultas e escritores, termina pendências e aguarda
terminação com prazo global configurado quando chamado externamente. Chamado
por callback, inicia fecho sem joins circulares. Não persistir topologia
alterada, IDs vivos, respostas, timers ou subscrições após o shutdown.

## 7. Correspondência dos invariantes com V-006 — ainda não executado

| Caso | Oráculo para Design/Implement/Test |
|---|---|
| M01 — configuração antes de rede | Perfil/papel/grafo/opções inválidos são rejeitados antes de abrir socket; topologia assimétrica não é reparada |
| M02 — conteúdo/X | Para cada nó local, exatamente os adjacentes/endpoints/papéis do grafo; nenhum próprio ID ou vizinho extra |
| M03 — offline | Mesma resposta com todos os vizinhos desligados; sem dial/sondas aos endpoints devolvidos |
| M04 — repetição/concorrência | Pedidos repetidos e concorrentes, de IDs diferentes e duas instâncias do mesmo ID, não alteram a lista nem criam exclusividade |
| M05 — identidade | Rejeitar ID desconhecido, senderId diferente de Endpoint.nodeId e incoerência de instância; IP partilhado não confunde IDs |
| M06 — endpoint/papel | Rejeitar host, portas ou papel incompatíveis; uma consulta válida posterior continua a funcionar |
| M07 — operações/contexto | Tipos v1 válidos alheios são rejeitados com INVALID_MESSAGE quando seguro, sem efeitos; código 3 é rejeitado pelo codec preservado |
| M08 — resposta/correlação | REGISTER_OK usa requestId do pedido, identidade do bootstrap, instanceId zero nos vizinhos e NeighborList sem lease |
| M09 — escrita antes de EOF | Ler frame completo e depois EOF em TCP real; confirmar também o caminho ERROR seguro; não aceitar fila vazia como prova |
| M10 — ausência de estado vivo | Respostas independentes do histórico; nenhuma renovação, aviso de saída, alteração do grafo ou consulta periódica após sucesso |
| M11 — send preservado/ordem | 18 testes anteriores intactos; send e sendAndFlush respeitam a fila comum e o escritor único, inclusive com concorrência |
| M12 — sucesso/erro de confirmação | Sucesso depois de write/flush; formato inválido, I/O e ligação fechada têm término real e razões coerentes |
| M13 — prazo/peer lento | Relógio e escrita controláveis: timeout total, pedido incompleto, silêncio/peer lento; fechar socket/contexto e terminar pendentes sem bloquear outras consultas |
| M14 — overflow/recursos | Fila cheia afeta só a conexão saturada; futures na fila e em escrita terminam, limites/contextos/timers não crescem após fecho |
| M15 — corridas/cancelamento | Cancelamento, conclusão, fecho e eventos atrasados são terminais uma vez; uma consulta antiga não apaga outra do mesmo ID |
| M16 — shutdown | Shutdown com confirmação pendente liberta socket, workers, timers e contextos dentro do prazo; nenhuma confirmação pendente |
| M17 — entrada/isolamento | Frame inválido/truncado/extra e encerramento precoce afetam só a ligação; listener aceita novo requerente |
| M18 — regressão/overrides | Consumir Control validado, incluindo overrides; executar regressão completa/JDK21 e conferir bytes/45 fixtures sem regenerar expected bytes |

Casos de prazo/ordem/erro de escrita exigem relógio e escrita/transporte
controláveis quando o oráculo depende dessa condição; sleep ou um peer loopback
pequeno não provam socket bloqueado. Design definirá os pontos de injeção dentro
dos ficheiros autorizados. Testes TCP reais complementam essas provas e verificam
resposta/EOF e libertação de recursos. A contagem anterior de 824 testes é
baseline histórica, nunca o resultado futuro deste incremento.

## 8. Rastreabilidade, revisão e ponto de paragem

REQ-004 recebe a parte de consulta; REQ-002/003 mantêm entrada, saída, HELLO,
reconexão e monitorização para os IMP respetivos. INT-001/002 exigem preservar
a base e a evidência, sem declarar overlay/streaming ou requisito global DONE.
Não alterar bytes v1, UDP OTT1 64/1200/1136, enums/códigos, POM ou dependências.

Não foi identificada contradição material entre ADD e código que impeça este
Model. Os pressupostos explícitos acima detalham coerência/consulta curta;
assinaturas, organização da fila, conclusão segura de futures, injeções e
política concreta de erros pertencem a Design. Se esse trabalho revelar uma
decisão inviável ou ambiguidade material, registar a próxima Q-C em
docs/construction-duvidas.md e parar a parte afetada; ADR nova só a partir de 033.

Não há gate externo adicional no IMP-003. A DoD exige revisão da versão final
pelo revisor atribuído **Waheed**, critérios/V-006/regressão/manual executados
e integração antes de DONE. A aprovação de Model não é essa revisão final.
Não contactar o revisor ou atribuir-lhe aceitação por inferência.

Checkpoint Model aprovado em 2026-11-04T10:50:52Z; Model registado em stages_completed.
Continuação autorizada apenas em Test, com current_stage=test, status=in-progress
e completed=null. Execuções estão no relatório Test; aprovação/repetição pessoal e
fecho continuam pendentes. Sem commits/push ou outro IMP.
