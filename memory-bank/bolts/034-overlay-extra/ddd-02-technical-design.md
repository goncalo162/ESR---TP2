---
stage: design
bolt: "034-overlay-extra"
created: "2026-11-11T12:41:00Z"
revision: "uniform-tcp-v2-proposed"
updated: "2026-11-11T17:43:46Z"
---
# Design revisto — IMP-034, descoberta no TCP de controlo

**Design aprovado explicitamente; registo 2026-11-11T17:55:50Z.** Retoma autorizada apenas de Implement. O texto de proposta abaixo identifica o contrato agora aprovado; as pendências de execução mantêm-se.
O transporte OTD1 separado foi suspenso. Este documento substitui a proposta
ativa de transporte; as aprovações anteriores referiam-se à variante arquivada,
não autorizam o contrato abaixo. [ADR-034 proposta](../../../add.md#adr-034)
propõe substituir o transporte/ativação opcional da ADR-033 e o contrato TCP v1 pelo contrato único v2; conserva as políticas de aprendizagem. [Q-C-18](../../../docs/construction-duvidas.md#q-c-18--descoberta-no-canal-de-controlo)
regista a mudança pedida e as escolhas novas ainda por aprovar.

O código parcial continua preservado, sem migração nesta revisão. O
[arquivo OTD1](closure-walkthrough.md#limpeza-autorizada)
contém código, Design/ADR anteriores, logs e XML com os seus resultados reais.
A compilação anterior terminou; não se iniciaram novos builds ou testes Java.

## 1. Âmbito e políticas conservadas

IMP-034/V-033 aprende candidatos; IMP-035/V-034 cria ligações novas depois,
com checkpoint próprio. Mantêm-se os dois grafos S–R1/R2–C e
S–R1–R2/R3–C, sem catálogo global e com bootstrap apenas no arranque.
Routing, recuperação de vídeo e deteção de máquina silenciosa continuam nos
incrementos previstos. Uma cache vazia/expirada não inventa um caminho.

| Política já aprovada | Mantida nesta revisão |
|---|---|
| Conteúdo anunciado | Só vizinhos diretos ACTIVE, snapshot completo até 63 entradas; nunca retransmitir hearsay da cache. |
| Proveniência | SourceKey com nodeId, instanceId, connectionId local e requestId do HELLO corrente; instância observada do candidato é pista. |
| Retenção | Morte do anunciante não apaga claims válidos; TTL monotónico local de 60 s desde a admissão da QUERY, sem TTL remoto. |
| Cache | 128 IDs, quatro fontes por ID; expirar primeiro e rejeitar novas entradas na saturação, sem desalojar claims válidos. |
| Conflitos | Configuração local autoritativa; self ignorado; divergências de endpoint/papel e colisões entre IDs tornam candidato indisponível. |
| Geração | Positiva, estritamente crescente por sessão emissora, sem wrap; sessão antiga não renova geração/TTL da nova. |
| Ritmo | Elegibilidade por vizinho 5 s, uma QUERY global por 250 ms, entrada até uma por segundo/sessão, pausa 30 s após falha. |
| Owner/callbacks | Estado no owner serial existente, callbacks curtos, runToken/sessão/consulta comparados antes de mutar estado. |
| Segurança | Laboratório sem criptografia; token HELLO correlaciona contexto, não prova honestidade, identidade criptográfica ou disponibilidade. |
| Fecho | V-033/D01–D12, regressão atual, manual antes da aprovação de Test, revisão humana e integração. |

### 1.1. Programa único e obrigatoriedade por papel

O cenário operacional é **um único programa e a mesma versão do JAR em todos
os nós**. SERVER/RELAY/CLIENT implementam e exigem a descoberta extra. Os
campos de versão/capacidade validam o contrato fixo do handshake; não definem
suporte operacional a versões antigas, atualização gradual ou matriz de
interoperabilidade. O bootstrap é o papel de consulta inicial do programa.
HELLO v1, versões desconhecidas, máscaras inválidas e identidades/endpoints
incoerentes são entradas negativas de protocolo, nunca um cenário de serviço
a acomodar. TCP v1 só existe no arquivo e nos negativos de versão; UDP OTT1 permanece inalterado.

**Mudança de requisito confirmada pelo utilizador:** todos os nós overlay da
aplicação, em qualquer API/perfil, suportam e exigem descoberta. “Extra” designa
o âmbito adicional do trabalho, não um modo runtime desligável. Não existe
ativação opcional, construtor de nó base, API alternativa sem capacidade ou
estado operacional ACTIVE_BASE. Bootstrap mantém apenas localização inicial v2;
é o papel de consulta inicial, sem aprendizagem ou membership, usando o mesmo codec TCP v2.

A configuração não seleciona presença/ausência da funcionalidade. Ausência da
antiga chave extra.discovery.enabled significa obrigatório. Para migração,
true é aceite como declaração redundante, sem efeito de ativação;
false, vazio ou outro valor são erro agregado antes de qualquer recurso.
BOOTSTRAP continua a recusar essa chave como inaplicável. Não converter false
em valor efetivo nem usá-lo para dispensar outras validações.

| Consumidor/perfil | Q e capacidade | Resultado offline |
|---|---|---|
| Qualquer SERVER/RELAY/CLIENT, chave ausente | Q=2..256, descoberta obrigatória | Perfil normal válido; não precisa de flag para ativar. |
| Overlay com antiga chave true | Q=2..256, descoberta obrigatória | Mesmo resultado efetivo do perfil sem chave. |
| Overlay com antiga chave false | Qualquer Q | Erro na chave retirada; nunca cria modo base. |
| Qualquer overlay, mesmo sem chave ou com false | Q=1 | Erro em control.queue.capacity, agregado com os demais. |
| BOOTSTRAP sem chave | Q=1..256 | Contrato próprio de consulta inicial, sem discovery/membership. |
| RuntimeConfiguration.Control / Transport.Options isolados | Q=1..256 | Infraestrutura genérica preservada; não representa um nó overlay operacional. |

Q>=2 é invariante do uso overlay, validado por load/loadDeployment e pelos
planos/construtores embutidos antes de criar sockets, owner ou workers. A
capacidade global da infraestrutura continua Q=1..256 e C=1..64; não se altera
o mínimo genérico para eliminar os testes Q=1 do transporte/bootstrap.
Mantêm-se defaults numéricos, hard caps, erros agregados e validação lexical.
As antigas restrições TCP/dataPort distinta e C>=8 continuam retiradas.
Validar também as capacidades/relações de configuração já previstas e os
orçamentos locais necessários antes de I/O. No preflight conjunto, conferir
as relações de grafo/capacidade conhecidas. Falha de bind/disponibilidade ou
informação remota posterior tem erro explícito e rollback limitado; nunca
é acomodada desligando descoberta. Validação offline não é prova de peer vivo.

NodeConfiguration conserva as assinaturas existentes de 13/14 argumentos;
ambas passam a construir um nó overlay obrigatório, sem atalho legado.
A configuração efetiva é derivada do papel, por exemplo discoveryRequired()
true para SERVER/RELAY/CLIENT e false apenas para BOOTSTRAP. O campo booleano
selecionável DiscoveryOptions.enabled do protótipo OTD1 não integra o modelo
final: será retirado/migrado com proveniência após aprovação. Não criar outro
booleano ou overload que permita construir um nó sem descoberta.

### 1.2. Configuração e arranque normais

Os perfis existentes com Q>=2 mantêm os valores/configuração de negócio e
passam a executar descoberta obrigatória sem acrescentar uma chave de ativação.
Perfis com Q=1 de papel overlay passam a ser inválidos; não são preservados como
um modo operacional alternativo. Podem conservar-se em testes como entradas
negativas. A antiga chave true é só compatibilidade de ficheiros, não um modo.

Exemplo do contrato normal (a atualizar após aprovação):

```properties
# Não é necessária qualquer chave para ativar descoberta.
control.queue.capacity=256
```

O default 256 já satisfaz a relação. O mesmo parser load/loadDeployment valida
individualmente/conjuntamente todos os perfis, agregando Q insuficiente, tentativa
de desativação e outros erros antes do arranque. Retirar a proposta de uma API
loadProjectDeployment separada para impor algo que todos os nós já têm de cumprir;
nenhuma API existente fica com uma exceção operacional base.

O roteiro normal usa loadDeployment para preflight do conjunto e os nós validam
o próprio perfil também no arranque isolado. O preflight não entrega catálogo
de candidatos; cada nó continua a aprender só pelos vizinhos. Roteiros/perfis/
README e a migração das entradas negativas só serão alterados na implementação
aprovada, sem modificar agora os ficheiros executáveis congelados.

## 2. Um contrato TCP v2 e um codec operacional

Decisão explícita do utilizador: **todo o controlo TCP usa versão 2**, incluindo
REGISTER/REGISTER_OK/ERROR do bootstrap, HELLO/HELLO_OK, NODE_LEAVE, sondas,
mensagens futuras de routing/subscrição e QUERY/SNAPSHOT de descoberta. Um
programa/versão de JAR em todos os processos. Não manter decoder, encoder, modo
de transporte, política CodecPolicy ou caminho operacional v1; não detetar a
versão para alternar parsers nem tentar fallback após rejeição.

Há uma ligação TCP persistente por par de vizinhos, com os mesmos connectionId,
reader/writer, FIFO e frame em escrita. O bootstrap continua a usar uma conexão
curta de consulta inicial v2, respondida com write/flush completo antes do fecho;
não passa a vizinho persistente. Não abrir canal TCP/dataPort, DNS/bind, listener
ou dial para descoberta. UDP OTT1 de vídeo permanece versão 1, sem alteração.

Framing único: u32 big-endian bodyLength, corpo 37..65536, prefixo excluído.
Mesmo UTF-8 estrito, limites antes de alocar, EOF limpo antes do prefixo,
truncamento como erro e ausência de bytes finais no frame. Discovery mantém
o cap adicional <=32768. Não há magic OTD1, sniffing ou outro enquadramento.

Conservar ControlMessage como envelope operacional para **todos os 18 tipos**;
acrescentar payloads imutáveis de handshake/descoberta com cópias defensivas.
A proposta anterior de ControlFrame/duplo namespace deixa de ser necessária:
retirar esse ficheiro do âmbito, não manter duas representações runtime de
versão. DiscoveryMessage representa apenas payloads tipados Query/Snapshot,
como subtipo permitido de ControlMessage.Payload, sem envelope/framing próprio.
DiscoveryCodec, se conservado, só contém helpers internos de payload usados
pelo MessageCodec único; não tem encode/decode/read/write de frames autónomos.

Assinaturas propostas (mesmos nomes/APIs públicas existentes, sem modo v1):

```java
record ControlMessage(int version, MessageType type, UUID requestId,
                      String senderId, UUID senderInstance, Payload payload) {
    // Construtor de cinco argumentos conserva assinatura e passa a emitir version=2.
    // O construtor canónico conserva forma; o codec rejeita version != 2 antes de escrita.
    record Hello(Endpoint endpoint, long supportedMask, long requiredMask) implements Payload { }
    record HelloOk(long selectedMask) implements Payload { }
}
// DiscoveryMessage.Query/Snapshot: payloads; correlação + geração/lista no Snapshot.
byte[] MessageCodec.encode(ControlMessage message);
ControlMessage MessageCodec.decode(byte[] singleFrame);
void MessageCodec.write(ControlMessage message, OutputStream destination);
Optional<ControlMessage> MessageCodec.read(InputStream source);
Optional<MessageCodec.FrameRead> MessageCodec.readFrame(InputStream source);
sealed interface FrameRead permits Decoded, RejectedExtraContent { }
record Decoded(ControlMessage message) implements FrameRead { }
record RejectedExtraContent(ExtraEnvelope envelope, ContentRejection reason) implements FrameRead { }
```

As quatro APIs existentes passam a v2; nomes e garantias de framing/validação
mantêm-se, sem overload v1. read/decode estritos podem converter conteúdo extra
rejeitado em ProtocolException para chamadores sem recuperação; **readFrame usa
o mesmo parser** e preserva o resultado tipado para o reader de transporte.
Transport.Listener.message continua a receber ControlMessage de todos os tipos;
adicionar callback curto rejectedExtraContent para publicar no owner e validar
identidade/sessão antes de recuperar. Não criar FrameListener/CodecPolicy por
versão nem outro Transport. Toda saída, incluindo ERROR, é v2.

Versão 1/0/desconhecida é rejeitada como UNSUPPORTED_VERSION sem interpretar
payload v1. Fechar só o handle afetado, com diagnóstico; sem resposta quando
o envelope não foi validado. Registos/handshakes v1 arquivados são exclusivamente
inputs negativos, nunca sessões a suportar. Uma mensagem extra de conteúdo
inválido completo continua a ser recuperável só nas condições da secção 5.2.

## 3. Versão, tipos e layout v2

Um enum MessageType com códigos explícitos, **18 tipos ativos**: os códigos
1,2,4..17 mantêm significado; acrescentar DISCOVERY_QUERY=18 e
DISCOVERY_SNAPSHOT=19. Código 3 continua retirado/rejeitado. Não usar ordinal,
enum alternativo por versão ou descoberta dentro de mensagens de routing.

| Versão única | Tipos | Utilização |
|---|---|---|
| 2 | REGISTER=1, REGISTER_OK=2, ERROR=17 | Consulta inicial curta do bootstrap; handler só admite REGISTER como pedido. |
| 2 | NODE_LEAVE=4, HELLO=5, HELLO_OK=6 | Sessões vizinhas, handshake obrigatório e saída após flush. |
| 2 | PROBE_PING=7, PROBE_PONG=8 e tipos 9..16 | Mensagens normais de controlo; os consumidores futuros continuam por implementar nos seus IMP. |
| 2 | DISCOVERY_QUERY=18, DISCOVERY_SNAPSHOT=19 | Só sessão ACTIVE_DISCOVERY com identidade/correlação atuais. |

Envelope comum: version u8 (=2), type u8, requestId UUID (16), senderId
u16+1..32 bytes e senderInstance UUID (16): corpo de envelope 37..68 bytes.
IDs [A-Za-z0-9_-]{1,32}, instâncias de execução não zero. Mesmos campos/payloads
de 1/2/4/7..17 do contrato arquivado, agora expressos exclusivamente em v2;
não mudar leases/path/UUID/relatedType/códigos de erro por esta migração.
A migração preserva a política de erro/retry da consulta inicial (Q-C-05),
sem confundi-la com estado de tentativa de um vizinho ou criar modo legado.
REGISTER leva Endpoint com instância não zero; REGISTER_OK leva só lista
configurada (até 64), instâncias zero, sem leases nem estado vivo. Mensagens
ERROR são v2 e relatedType mantém o octeto bruto, incluindo 18/19/desconhecido.

### 3.1. Handshake v2 obrigatório

HELLO contém Endpoint + supportedMask u32 + requiredMask u32;
HELLO_OK contém selectedMask u32. Exigir **supported=required=selected=1**,
com bits reservados zero. São validações do contrato único, sem adaptação para
outro JAR ou modo base. Q>=2 já foi validado offline. Máscara zero, reservada
ou combinação diferente é negativo do protocolo, não modalidade operacional.
RequestId ecoa o HELLO; identidade, endpoint, grafo, instância e menor iniciador
mantêm validação. Seleção só fica utilizável após as duas barreiras de
handshake/write/flush. Não autentica criptograficamente nem prova disponibilidade.

HELLO ocupa 72..386 bytes de corpo; HELLO_OK 41..72. Rejeição segura e
correlacionada pode usar **ERROR v2** INVALID_MESSAGE existente; input com
versão não suportada fecha com diagnóstico sem chamar decoder v1. Não responder
ERROR a ERROR, nem fechar sessões saudáveis por uma tentativa rejeitada.

### 3.2. QUERY/SNAPSHOT v2

O requestId comum é o queryId não zero. Payload comum da descoberta:
helloRequestId UUID (16), recipientId u16+1..32 bytes e recipientInstance UUID
(16). Estes campos preservam a correlação dos dois extremos já aprovada.
QUERY não acrescenta outros campos. SNAPSHOT acrescenta generation u64
(1..Long.MAX_VALUE), count u16 (0..63) e endpoints.

Cada endpoint conserva o formato: nodeId u16+1..32, UUID observado não zero,
host u16+1..253 bytes ASCII válido pela normalização lexical, controlPort u16,
dataPort u16 (ambas 1..65535), role u8 explícito (SERVER=1, RELAY=2, CLIENT=3).
Não admitir BOOTSTRAP, NUL, wildcard, ID repetido ou identidade do anunciante
na lista. O recetor ignora a sua própria entrada depois da validação completa.

QUERY ocupa 72..134 bytes; SNAPSHOT vazio 82..144; endpoint até 310 bytes;
SNAPSHOT completo até 19674 bytes de corpo, dentro do cap extra de 32768 e do
cap global de 65536. Estas são contas do formato proposto, não resultados de codec.
EOF não encerra uma consulta de descoberta: é perda do canal persistente e
segue o tratamento normal de NeighborDown. A correlação da resposta encerra
só a operação lógica; nunca fechar o socket depois de uma SNAPSHOT válida.

## 4. Contrato único e rejeição local de tentativas inválidas

Todos os nós operacionais usam o mesmo JAR e o mesmo contrato obrigatório.
Estado da tentativa/sessão: NEGOTIATING_REQUIRED ou ACTIVE_DISCOVERY; a falha
da tentativa fica num diagnóstico/estado **por peer**, nunca numa modalidade
alternativa do nó. Retirar ACTIVE_BASE do modelo operacional.
NeighborUp/sessão ACTIVE requer HELLO/HELLO_OK v2 e validação de versão,
identidade, endpoint/grafo, requestId, capacidade, iniciador e flush. Mensagens
normais também são v2 depois desse handshake. Nenhum handshake inválido permite
aprendizagem ou NeighborUp.

| Entrada/cenário | Comportamento exigido |
|---|---|
| Nós do mesmo JAR, Q>=2 | Menor ID envia HELLO v2 supported=required=1; selected=1 confirma o contrato; só então ACTIVE_DISCOVERY. |
| Entrada negativa: HELLO/HELLO_OK v1 ou versão não suportada | Rejeitar só a tentativa/handle, com diagnóstico; sem handshake de sucesso, NeighborUp ou fallback. Não suportar um nó operacional antigo. |
| Entrada negativa: bit obrigatório ausente, required=0 ou seleção inválida | Rejeitar só a tentativa, diagnosticando incompatibilidade quando identidade/correlação forem seguras. Não criar sessão base nem alterar sessões saudáveis. |
| Entrada negativa: identidade/endpoint/grafo/correlação inválidos | Rejeição local segundo as regras existentes; não atribuir ao peer legítimo um anúncio não autorizado nem sobrescrever o seu estado atual. |
| Processo temporariamente ausente, EOF/timeout sem resposta conclusiva | Falha transitória da tentativa, sem inferir incompatibilidade; retry apenas do contrato v2 obrigatório. |
| Perfil overlay/Q=1 ou tentativa de desativação | Recusado offline por todas as APIs de nó, antes de recursos. |

Não existe fallback v1, marcador V1_ONLY operacional, flag desligável, alteração
de requisitos em runtime ou construtor alternativo de nó sem extra. O codec/
transporte genérico também só usa TCP v2; o arquivo v1 serve apenas para
proveniência e testes negativos de versão, sem adaptador runtime.

### 4.1. Diagnóstico por peer, isolamento e tentativas posteriores

Uma capacidade ausente só é classificada como incompatibilidade confirmada
quando for legível e atribuível ao peer autorizado na tentativa atual,
comparando runToken, connectionId, requestId e identidade/instância quando
conhecida. Produz resultado local tipado RequiredDiscoveryIncompatible e razão
REQUIRED_DISCOVERY_MISSING, com estado por peer INCOMPATIBLE. Versão rejeitada
e identidade/endpoint inválidos mantêm razões próprias; não inferir capacidade
de texto livre nem de bytes que não possam ser validados. Não responder ERROR
a ERROR. Rejeição best effort usa apenas ERROR v2 quando seguro e
correlacionado, dentro do prazo de handshake, seguida de fecho só do handle
rejeitado. Não alterar o enum wire ErrorCode ou Neighbor.State para criar modo.

**Nenhuma dessas entradas negativas muda a fase do nó para FAILED, completa
node.failure()/o future de arranque com falha, emite FATAL, termina a CLI ou
fecha sessões saudáveis.** A conclusão com erro pertence só à tentativa.
Publicar Diagnostic pelo mecanismo de eventos existente, não por failure()
global. As falhas locais fatais reais da ADR-020 continuam a ter o tratamento
próprio; não se classificam inputs inválidos de um peer como tais falhas.
Preservar ACTIVE_DISCOVERY dos restantes peers, seus connectionId, consultas,
gerações e claims. Uma tentativa duplicada inválida também não retira uma
sessão saudável do mesmo peer. Callback antigo só trata os próprios recursos.

Estado de rejeição vive no owner serial: no máximo uma vista imutável por
peer configurado (até 63), com enum finito de estado/motivo, versão/máscaras
escalares, identificador da tentativa corrente e instante do próximo retry.
Reutilizar/substituir essa vista, sem guardar payload, texto remoto, lista de
tentativas ou histórico ilimitado. A consulta assíncrona/snapshot de diagnósticos
usa o mecanismo limitado existente; diagnóstico no máximo por motivo/5 s.
Peers desconhecidos não criam entradas persistentes. Validar identidade e
correlação antes de alterar o estado do peer; evento velho não o sobrescreve.

EOF, connect failure ou timeout sem resposta conclusiva dão estado por peer
TEMPORARILY_UNAVAILABLE/razão REQUIRED_NEGOTIATION_FAILED. Não provam versão
antiga ou incompatibilidade. Um novo insucesso de transporte não apaga a razão
da última rejeição confirmada: conservar separadamente o último motivo validado
e o resultado da tentativa atual, ambos escalares e limitados. A próxima
tentativa válida, com todas as barreiras cumpridas, substitui a rejeição por
ACTIVE_DISCOVERY; nenhuma aprovação manual ou caminho de compatibilidade.

Tentativas posteriores, após rejeição confirmada ou ausência temporária:

- Só o menor nodeId inicia, pela política existente; o outro pode receber nova
  tentativa válida sem iniciar um dial alternativo. Usar sempre HELLO v2 com
  supported=required=1, sem tentar outro formato/versão de programa.
- No máximo uma tentativa corrente e um agendamento de retry por peer; nada de
  fila adicional de rejeições. Usar overlay.retry.initial.ms/max.ms/jitter
  existentes (defaults 500/5000 ms/10%), com crescimento exponencial saturado,
  jitter limitado pelo teto e aritmética verificada. Não há loop imediato nem
  contador de tentativas sem limite; o índice satura no teto do backoff.
- Repetição pode continuar a cadência limitada enquanto o nó estiver a correr;
  não se acrescenta suporte a outra versão, sondagem de upgrade, burst ou novo
  parâmetro. Limites são uma tentativa/agendamento por peer e reservas físicas
  globais C<=64, deadlines e ledger existentes. Não liberar reserva por mero
  cancelamento/future concluído, nem iniciar o próximo dial sem capacidade real.
- Sucesso confirmado reinicia o backoff; shutdown invalida retry/runToken e
  aguarda terminação física dentro do orçamento global. A rejeição do peer não
  interrompe a manutenção e a aprendizagem nas sessões restantes.

READY de configuração/listener não prova que todas as arestas estejam prontas.
O roteiro normal só confirma arranque completo quando as arestas previstas
estão ACTIVE_DISCOVERY. Pending/rejeição/ausência ficam explícitos como estado
de conectividade parcial; a CLI e as sessões saudáveis continuam utilizáveis.

Bootstrap migra REGISTER/REGISTER_OK/ERROR para v2, com grafo e lifecycle preservados. HELLO/HELLO_OK v2 só são admissíveis
na tentativa atual. Retenção na janela legítima de flush continua limitada a
B=min(4,Q), R=min(N,C×B). Frames tardios não alteram a capacidade da sessão nova.
As reservas C incluem dials/workers em terminação, independentemente de futures.

ERROR v2 mantém os campos de envelope, requestId e relatedType bruto; erro corrente pertencente
a uma consulta extra é entregue intacto ao consumidor correspondente; outros
mantêm a entrega existente. Não responder ERROR a ERROR. Extra antes da seleção
ou em contexto inválido mantém rejeição, sem aprender. Identidade incompatível
fecha o handle afetado; eventos antigos nunca fecham a sessão nova.

## 5. Fila comum, limites e falhas

Há uma só fila de saída por Connection: Q configurado (1..256) mais um frame
inFlight. NODE_LEAVE, sondas, routing futuro, negociação e descoberta passam pelo
mesmo writer FIFO; sem prioridade, bypass, writer concorrente ou segunda fila.
Capturar o snapshot no owner, validar e codificar fora dos locks de I/O, dentro
dos limites, e fazer a admissão atómica na fila comum.

Nova operação interna proposta, distinta das APIs existentes agora v2 preservadas:

```java
ExtensionEnqueue trySendExtension(ControlMessage frame); // apenas tipos 18/19 autorizados
// Handshake v2 usa a admissão normal de handshake na mesma fila, não esta quota de descoberta.
// status: ACCEPTED / BACKPRESSURED / CLOSED / INVALID_MESSAGE
// ACCEPTED inclui WriteTicket com vista CompletionStage de write+flush.
```

### 5.1. Margem de fila e validação Q>=2 em todo nó overlay

Reservar **uma posição da fila para mensagens normais em cada admissão extra**.
Definir M=1 e E(Q)=min(1,max(0,Q-M)): quota de frames extra enfileirados por
ligação. O frame inFlight pertence ao writer existente e não aumenta Q.
A admissão de QUERY/SNAPSHOT só é aceite se, numa decisão atómica:

1. A ligação e a capacidade negociada atuais continuam válidas e Q>=2.
2. Existem pelo menos M+1=2 posições livres na fila comum.
3. Há menos de E(Q)=1 frames extra cobrados à parte enfileirada dessa ligação.
4. Há ticket extra global disponível, respeitando o teto de quatro e as operações 2+2.

Depois de inserir o frame extra fica pelo menos uma posição efetivamente livre.
A quota enfileirada inclui um frame retirado por take mas ainda não publicado
como inFlight: só se devolve essa quota na transição real do writer. O ticket
global continua cobrado até write/flush/falha terminal. Assim, pode haver no
máximo um extra enfileirado/em transferência e um extra inFlight por ligação,
sempre dentro de Q+inFlight e dos quatro tickets globais.

| Q/situação | Admissão da descoberta | Margem e efeito |
|---|---|---|
| Overlay/Q=1 | Configuração recusada antes de I/O, com ou sem antiga chave. | Corrigir Q para pelo menos 2; não desativar a funcionalidade. |
| Infraestrutura genérica/bootstrap Q=1 | Testar o contrato próprio, sem construir nó overlay base. | Manter Q=1 genérico e os caps; todos os frames TCP passam a v2. |
| Q=2, fila vazia, sem extra enfileirado | Pode admitir um extra. | Fica uma posição livre para mensagem normal. |
| Q=2, uma posição já ocupada | BACKPRESSURED para extra. | Não ocupa a última posição; mensagens normais mantêm a admissão existente. |
| Q>=2, apenas uma posição livre | BACKPRESSURED para extra. | Não consumir a margem. |
| Q>=2, dois ou mais lugares livres mas já existe extra enfileirado | BACKPRESSURED. | Limitar descoberta mesmo sem encher a fila. |
| Extra em transferência take→inFlight | Continua cobrado à quota enfileirada até publicar a transição. | A corrida não permite segundo extra prematuro. |

Q=1 só é válido em consumidores genéricos/bootstrap que o suportem; é sempre
erro agregado num perfil/construtor de nó overlay. Cache/TTL/proveniência
mantêm-se; não se fabricam candidatos para compensar falhas de negociação.
O limite global Q=1..256 mantém-se, com uso overlay obrigatoriamente Q>=2;
C continua 1..64. A nova guarda só afeta os tipos
18/19; HELLO/HELLO_OK e os envios normais usam a admissão existente.

Avaliar ocupação, quota e offer sob a sincronização comum dos produtores,
com contabilidade da transição do writer; uma leitura isolada de queuedMessages
não autoriza uma inserção posterior. Não reservar um frame fora da fila nem
criar lista de espera para o retry. A quota global é adquirida antes da
codificação/alocação e devolvida se a admissão final for recusada.

Nos nós overlay com configuração já válida, saturação transitória destas guardas
devolve BACKPRESSURED ou indisponibilidade da operação,
sem fechar socket, sem ERROR substituto e sem retenção para retry. QUERY recusada
falha localmente; resposta recusada pode levar a timeout remoto. Aplica-se a
pausa aprovada de 30 s. Não mudar o overflow das APIs existentes agora v2: um burst normal pode
continuar a saturar Q e fechar a ligação conforme o contrato. A margem é uma
propriedade da admissão extra, **não** garantia de capacidade para bursts
arbitrários, prioridade, largura de banda ou latência. FIFO permanece estrito.

Até duas consultas de saída e duas respostas de entrada logicamente pendentes,
e até **quatro tickets de escrita extra no total**, incluindo operações já
expiradas/sessões retiradas cujos frames ainda estejam enfileirados/em escrita.
Adquirir esta quota antes de codificar/alocar o frame. Uma consulta por vizinho,
no máximo 63 vínculos de origem ativos; estados retirados só na cache limitada.
Sem socket/dial/reserva TCP adicionais por consulta. O C<=64 existente continua
a contar dials/accepts/workers reais de controlo; descoberta não aumenta C.

Uma QUERY tem prazo lógico de 2 s desde antes da admissão à fila, incluindo
espera de escrita e resposta. O respondente tem 2 s desde a observação da QUERY
para processamento/admissão/flush; fila do owner e writer não reiniciam o prazo.
TTL conserva o início local da QUERY, não o momento tardio da receção da resposta.
Não há fases DNS/dial de descoberta. Os limites da negociação são os do HELLO.

Expirar/cancelar uma operação não remove bytes da fila nem liberta um ticket
físico antecipadamente. Expor apenas uma vista não mutável da conclusão;
cancelar um future derivado não cancela a confirmação interna. Libertar ticket
uma vez, após write/flush ou falha terminal decidida pelo writer; callbacks fora
dos locks. Um frame antigo pode chegar tarde, mas o recetor não o usa para
renovar TTL. Saturação persistente conserva a quota ocupada e recusa mais trabalho,
sem mapa de futures/tombstones ilimitado. O send normal ACCEPTED continua só a
significar enfileiramento; flush não é ACK remoto.

### 5.2. Framing, identidade e conteúdo recuperável

A decisão recuperável é limitada a **tipos v2 18/19 conhecidos**, sessão
ACTIVE_DISCOVERY atual, capacidade negociada e envelope/identidades legíveis.
Preservar a classificação de erros das APIs migradas para v2 nem recuperar handshake, versão/tipo desconhecido
ou código 3 por este mecanismo.

1. Validar comprimento do frame dentro dos caps, ler integralmente o corpo
   delimitado e isolar o decoder de payload nesse corpo. Nunca procurar o
   próximo prefixo dentro de dados malformados ou continuar após EOF parcial.
2. Ler integralmente cabeçalho, identidades e correlação. Truncamento ou identidade
   ilegível é terminal. Decodificar payload sem alterar cache, watermark ou TTL.
3. Para conteúdo semanticamente inválido, devolver RejectedExtraContent, com
   envelope limitado e motivo de enum, sem corpo/lista parcial. Não capturar
   genericamente IOException/ProtocolException e convertê-la em recuperável.
4. No owner, verificar primeiro o handle/connectionId atual. Callback de ligação
   retirada não fecha a nova. No handle atual, identidade sender/recipient
   incompatível é terminal **mesmo se o payload também for inválido**.
5. Só depois classificar a rejeição de conteúdo como recuperável: descartar o
   anúncio inteiro, sem aprender/apagar claims ou avançar watermark/TTL. Uma
   consulta corrente correlacionada pode terminar com CONTENT_REJECTED; não
   terminar consulta nova por requestId/token antigo. Sem ERROR de resposta.
6. O reader pode continuar no prefixo seguinte, já alinhado, e entregar uma
   mensagem normal v2 na mesma sessão. Descarte não é NeighborDown nem reconexão.

| Categoria/exemplo | Tratamento proposto |
|---|---|
| Prefixo/comprimento de frame fora do cap, ou comprimento de campo fora dos limites | Terminal no handle afetado, antes de alocação incompatível com os caps. |
| Truncamento do prefixo/corpo na stream ou EOF ao ler campo obrigatório | Terminal, mesmo que o corpo externo tenha sido recebido; não converter EOF em erro semântico. |
| Versão/tipo desconhecido, código 3, envelope/identidade ilegível ou estado de negociação inválido | Mantém o tratamento terminal existente/proposto. |
| Sender/recipient legíveis mas incompatíveis com a sessão atual | Terminal, antes de qualquer decisão de rejeição recuperável. |
| SNAPSHOT completa: geração zero/fora do domínio, ID repetido, papel desconhecido, endpoint/instância observada inválidos, host anunciado semanticamente inválido ou UTF-8 inválido só nesse conteúdo | Rejeição atómica de conteúdo, sem fechar controlo, sem mutar cache/watermark/TTL. |
| Cardinalidade anunciada >63, lida integralmente de uma frame delimitada | Rejeição de conteúdo antes de alocar a lista; não relaxar o cap nem tentar montar a quantidade declarada. |
| Bytes excedentes no payload v2 conhecido, dentro do corpo delimitado | Rejeitar esse conteúdo atomicamente; consumir apenas o corpo já delimitado, preservando o próximo prefixo. |
| QueryId/token/geração antigos numa identidade corrente | Descartar para a descoberta; não alterar consulta/sessão nova ou renovar cache. |
| Filtros de cache já aprovados: self recetor, ID configurado, conflitos entre fontes | Mantêm a política por candidato/proveniência; não reclassificar conflito de cache como falha de identidade do anunciante. |
| Frequência excedida, fila/quota/tickets sem margem ou prazo extra | Recusar/retirar só a operação lógica; o writer mantém os tickets/bytes já admitidos. |
| Falha TCP/write/EOF ou impossibilidade de publicar cleanup crítico | Mantém NeighborDown/falha terminal observada e shutdown limitado; claims retidos até TTL próprio. |

Uma recusa de conteúdo não pode mascarar identidade errada ou truncamento.
Os diagnósticos mantêm enum/counters limitados e throttle 5 s, sem reter corpos.
UTF-8 estrito continua obrigatório: recuperar significa rejeitar a frame extra,
não substituir caracteres ou aceitar parcialmente. Campos de identidade com
UTF-8 inválido são terminais; UTF-8 inválido no host/ID de um candidato anunciado,
com comprimento válido e bytes completos, é conteúdo recuperável.

A partilha mantém acoplamento de latência/framing/identidade e head-of-line,
mas isola anúncios completos semanticamente inválidos e callbacks antigos.
Não se promete imunidade a fila do owner saturada ou falhas reais de transporte.

## 6. Cache e lifecycle

Conservar SourceKey, CandidateEndpoint, Provenance e CandidateView imutáveis,
com cópias defensivas e as políticas da secção 1. A mesma entrada pode ter fontes
com TTL independente; omissão num snapshot posterior não renova nem elimina
antecipadamente uma observação válida. Normalização lexical de hosts igual à
base, sem DNS para comparação; instâncias observadas não autorizam migração.

O contador de geração vive na sessão ACTIVE_DISCOVERY, começa em zero e cresce
na captura de cada resposta admitida à operação. Gaps são permitidos; wrap não.
O recetor mantém watermark por sessão atual e só aceita geração superior com
consulta corrente antes do prazo. Down retira autorização de novos anúncios,
mas conserva os claims existentes até aos respetivos prazos. Campos old/new
do callback são comparados antes da mutação, mesmo que a escrita antiga complete
com sucesso. Uma futura ligação nova pode voltar a negociar a capacidade.

Manter tick de manutenção 100 ms, uma tarefa periódica física e um evento
pendente no máximo, sem timer por claim. Diagnósticos são contadores de enum
finito, com no máximo uma emissão por motivo/5 s. A consulta pública da cache
continua assíncrona, com até quatro comandos pendentes e expiração no owner.
A descoberta não possui nem encerra o owner partilhado.

Shutdown: fechar admissões e parar o tick antes da drenagem do nó; invalidar
operações lógicas, conservar tickets até decisão real, e coordenar NODE_LEAVE/
fecho do TCP com o mesmo orçamento global restante. Não fechar uma conexão
individual apenas para terminar uma QUERY. Aguardar workers e owner fora do
owner e dos locks; timeout de worker preso é explícito. Não criar resolver,
listener, pool ou prazo separado para esta funcionalidade.

## 7. Âmbito revisto e fronteira da aprovação

O âmbito normativo do IMP-034 no ADD inclui agora bootstrap e recursos de teste.
Remover protocol/ControlFrame.java proposto, pois ControlMessage já cobre o
contrato único. Não criar outro IMP: a migração é condição deste incremento,
sem reimplementar routing/subscrição ou reparação IMP-035.

| Ficheiros/grupo | Alteração futura justificada |
|---|---|
| protocol/MessageType.java, ControlMessage.java, MessageCodec.java | 18 códigos explícitos, envelope/payload v2 e parser único; construtores/APIs existentes passam a v2; rejeição v1. |
| protocol/DiscoveryMessage.java, DiscoveryCodec.java | Payloads da descoberta; retirar enquadramento/envelope OTD1 separado, sem outro codec operacional. |
| bootstrap/BootstrapServer.java, BootstrapServerTest.java | REGISTER/REGISTER_OK/ERROR v2, grafo imutável e offline, resposta completa antes de EOF, prazos/recursos/shutdown, negativos de versão sem derrubar serviço. |
| overlay/NeighborManager.java, NeighborDiscovery.java, OverlayNode.java | Cliente de bootstrap v2, handshake obrigatório, dispatch de 18/19 e diagnóstico local, owner/callbacks/cache/shutdown; não criar novas arestas. |
| overlay/CandidateCache.java | Mesmas políticas de cache/TTL/proveniência, sem migração semântica. |
| config/NodeConfiguration.java, ConfigurationLoader.java | Obrigatoriedade em todas as APIs/perfis, Q>=2 e false recusados offline; construtores mantidos sem modo base. |
| transport/Connection.java, Transport.java | Um reader/parser v2 e uma FIFO/writer, send/sendAndFlush, margem/tickets e callback tipado para conteúdo rejeitado; nenhum modo v1. |
| protocol/MessageCodecTest.java, DiscoveryCodecTest.java | Fixtures independentes v2; limites/UTF-8/tipos/roundtrip/EOF, conteúdo recuperável e rejeição de v1; sem usar encoder para fabricar o oráculo. |
| overlay/NeighborManagerTest.java, NeighborManagerLifecycleTest.java, NeighborDiscoveryTest.java, CandidateCacheTest.java | Bootstrap+handshake+mensagens normais v2; preservar ordens de arranque, identidade/unicidade, races, backoff e cache. |
| transport/TransportTest.java, config/ConfigurationLoaderTest.java | Testes de transporte passam a frames v2, mantendo Q=1 genérico, FIFO, flush, limites; overlay Q>=2/erros agregados. |
| app/OttApplicationTest.java | Mesma CLI/reconexão/shutdown, peers positivos v2; peer inválido enquanto outro ACTIVE, CLI/descoberta utilizáveis. |
| Projeto/src/test/resources/protocol/ | Futuro conjunto operacional: 50 fixtures TCP v2 propostas + 16 UDP OTT1 intactas, manifesto/README migrados; v1 arquivado, sem aceitação v1. |
| Projeto/README.md, Projeto/docs/protocolos.md | Um JAR/contrato, bootstrap e vizinhos v2, UDP OTT1 inalterado; instruções de teste/manual corrigidas. |
| scripts/validate-planning.py | Após aprovação, distinguir auditoria do conjunto operacional v2+UDP e arquivo histórico v1, sem alterar hashes/snapshots históricos nem esconder falhas. |
| experiments/overlay-discovery/ | Manual/preflight/captura do contrato uniforme; sem vídeo ou reparação antecipados. |

Neste checkpoint só mudam documentação e artefactos de referência em experiments/;
nenhum fonte, teste executável, recurso ou target de Projeto/ foi alterado.
Os 163 ficheiros da variante suspensa mantêm hashes; o JAR histórico não é JAR v2.

<a id="10-mapa-d01d12--v-033"></a>
## 8. Oráculos D01–D12 / V-033 revistos

| Caso | Prova requerida após aprovação de Implement |
|---|---|
| D01 | TCP exclusivamente v2 em todos os 18 tipos/bootstrap/APIs; v1 e código 3 rejeitados. Fixtures v2 independentes; arquivo v1/16 UDP OTT1 preservados. Todos os nós exigem descoberta/Q>=2; false offline; Q=1 só genérico/bootstrap. |
| D02 | Todo nó do mesmo JAR suporta/exige bit 0; inputs HELLO v1/required=0/selected inválido são negativos, rejeitados só na tentativa, sem Up/falha global/fallback. Versão/identidade/endpoint/flush e callbacks mantidos. |
| D03 | C aprende S via R1/R2 e S aprende C pelas sessões de controlo atuais, sem endpoints globais nem outra conexão. |
| D04 | C aprende R1 via R2/R3 e R1 aprende C, preservando S–R1; não reparar nesta prova. |
| D05 | Depois da morte dos anunciantes, claims sobrevivem até aos seus TTL independentes; EOF do controlo não é fim normal de QUERY. |
| D06 | Clock falso: 60 s exatos, wrap, geração velha/nova sessão, cache 128×4, saturação e expiração; resposta atrasada pela fila não rejuvenesce. |
| D07 | Codec comum distingue conteúdo extra recuperável de comprimento/truncamento/identidade terminais; D07a–D07e abaixo, preservando framing/caps e rejeitando v1. |
| D08 | FIFO comum, margem M=1 e quota E(Q), Q=1 rejeitado offline em todos os nós overlay, transição take→inFlight e tickets físicos: D08a–D08f abaixo; manter ritmo e ausência de hearsay. |
| D09 | Cache vazia/expirada ou ausência de aprendizagem antes das mortes é explícita; não há fallback ao bootstrap nem ligação direta inventada. |
| D10 | Clock/escrita controlados: timeout/cancelamento não libertam ticket; callback velho só libera o próprio recurso; owner saturado, shutdown global e ausência de threads/sockets novos. |
| D11 | TCP real entre nós do mesmo JAR, handshake obrigatório e unicidade; inputs inválidos injetados nas duas ordens de IDs são negativos, nunca cenário de versões mistas. D11a prova um peer rejeitado enquanto outro permanece ACTIVE, CLI/descoberta úteis e sem falha global; sem sessão overlay v1/dataPort separado. SNAPSHOT inválida completa seguida de mensagem normal v2 válida conserva conexão; framing/truncamento/identidade fecham só o handle afetado. |
| D12 | Regressão completa JDK 21, V-ADD, hashes/relatórios atuais e manual do novo contrato; logs OTD1 não provam este desenho. |

### Casos adicionais explícitos (subcasos dos D existentes)

D01e/D11b acrescentam a migração do bootstrap sem substituir as provas de
aprendizagem D03–D06 ou de isolamento D11a.

| Caso | Validação/requisito adicional proposto |
|---|---|
| D01a | load/loadDeployment e construtores 13/14 sem antiga chave criam sempre configuração overlay required; não há overload/campo false ou flag que dispense Q>=2. |
| D01b | Overlay/Q=1 gera problema em control.queue.capacity com chave ausente, true ou false; false gera ainda erro próprio. Agregar outros erros; socket/owner/worker em zero por todos os caminhos. |
| D01c | Overlay/Q=2/256 válido; false/vazio/inválido recusado; true redundante equivale a ausência. RuntimeConfiguration/Transport.Options e BOOTSTRAP mantêm testes Q=1 próprios. |
| D01d | Preflight loadDeployment aplica requisito a cada perfil overlay sem chave de ativação; perfis antigos Q>=2 válidos ficam required, Q=1/false falham. BOOTSTRAP mantém aplicabilidade e lookup inicial. |
| D02a | Ambos required: HELLO v2 supported=required=1, seleção com bit 0; nenhum NeighborUp antes de identidade/capacidade/flush válidos. Mensagens normais também são v2. |
| D02b | Injetar negativos HELLO/HELLO_OK v1, versão inválida, required=0, bit ausente/reservado não zero/seleção inválida, identidade e endpoint incoerentes nos papéis iniciador/respondente. Sem Up/cache nova, com diagnóstico e rejeição local; não executar JAR antigo ou modo opcional. |
| D02c | Relógio/transporte falsos: EOF/timeout/connect failure dá ausência temporária, distinta de rejeição confirmada. Retry sempre v2 pelo menor ID, uma tentativa/agendamento por peer, backoff/jitter saturados e reservas físicas respeitadas; último motivo confirmado não é apagado por timeout. Sucesso válido reinicia backoff. |
| D02d | Negativo de peer autorizado corrente gera diagnóstico/estado por peer limitado, mas não FAILED global, node.failure(), FATAL ou saída da CLI. Desconhecidos não aumentam a tabela (<=63); callbacks antigos e tentativa duplicada inválida não substituem estado/sessão saudáveis. Testar saturação/repetição sem histórico/timers acumulados. |
| D01e | Codec enumera exatamente 18 tipos v2 (1,2,4..19); versão 1/0/3/255 e código 3 rejeitados em todas as APIs. Cada payload tem vetor independente; lengths/UTF-8/EOF continuam com causas próprias, não mascaradas por byte de versão antigo. Arquivo v1 intacto, sem decoder legado. |
| D11b | Bootstrap/cliente reais v2: REGISTER→REGISTER_OK/ERROR v2, vizinhos configurados offline/instanceId zero, write/flush completo antes de EOF e retry inicial limitado. Pedido v1 é negativo, não derruba bootstrap; cliente rejeita resposta v1 sem handshake v1/fallback. Restantes peers/CLI conservados; shutdown e reservas físicas verificados com clock/escrita controlados. |
| D11a | Um nó com peer A ACTIVE e tentativa negativa de B: preservar connectionId de A e zero Down(A); zero Up(B); neighbors/help continuam a responder antes/depois da rejeição, sem FATAL/EOF induzido. QUERY/SNAPSHOT válidas de A continuam a aprender/renovar candidatos segundo TTL/geração; node.failure() não concluído por B. Depois B negocia validamente o mesmo contrato no retry e fica ACTIVE. Cobrir clock controlado e TCP/CLI reais; quit final termina normalmente. |

| Caso | Oráculo controlado/real a acrescentar |
|---|---|
| D07a | Em ACTIVE_DISCOVERY, frame completa com geração inválida/IDs repetidos/role inválido/endpoint inválido: com clock fixo, cache, watermark e deadlines de claims byte a byte/valor a valor intactos; consulta correlacionada falha só localmente. |
| D07b | Enviar SNAPSHOT semanticamente inválida concatenada com PROBE_PING v2 válida; depois responder a uma nova consulta com SNAPSHOT válida. Rejeição tipada, ping entregue, anúncio válido aceite, mesmo connectionId, sem Down/HELLO/dial. O watermark inválido não impede geração válida subsequente; incluir bytes excedentes e cardinalidade >63 sem alocação indevida. |
| D07c | Comprimento externo inválido, campo truncado e prefixo/corpo incompleto em todos os pontos relevantes produzem falha terminal; não emitir RejectedExtraContent nem tentar ressincronizar pelo próximo prefixo. |
| D07d | Payload inválido juntamente com sender/recipient incompatível é terminal; UTF-8 inválido na identidade terminal, em campo de candidato completo recuperável. Framing e classificação de erros preservados na migração v2; v1 rejeitado por versão. |
| D07e | Rejeição atrasada de ligação/token/consulta antigos não fecha a sessão nova, não avança watermark/TTL e não termina a consulta nova; libera apenas os recursos próprios. |
| D08a | Todo perfil/construtor overlay Q=1 falha offline, sem poder desativar; testes de Q=1 do transporte isolado/bootstrap preservados. Nunca criar ACTIVE_BASE. |
| D08b | Q=2 vazia admite um extra e depois uma mensagem normal sem overflow; com uma posição já ocupada, extra é recusado sem fecho. Projeto com Q=1 é recusado no preflight, não testado como sessão degradada. |
| D08c | Para Q=2, 3, 256: última posição livre nunca é consumida pela descoberta; quota extra enfileirada máxima 1, quatro tickets globais e Q+inFlight nunca ultrapassados. |
| D08d | Escrita controlada pausa take antes de publicar inFlight: quota continua cobrada e impede segunda admissão extra. Depois da transição pode haver um extra em escrita e um na fila, com margem normal e FIFO preservados. |
| D08e | Produtores normais/descoberta v2 concorrentes competem pela penúltima posição: decisão atómica mantém margem na admissão extra, não baseada em observação antiga de queuedMessages; burst só de mensagens normais v2 conserva a política de overflow anterior. |
| D08f | Timeout/cancelamento/backpressure não liberam ticket enfileirado/em escrita antecipadamente, não criam fila de retry nem fecham controlo; fechar o nó aguarda worker real no orçamento global. |

## 9. Aprovação e evidência pendente

G-EXTRA-ADR foi reaberto para IMP-034 pela mudança de transporte. É necessária
aprovação do Design revisto, ADR-034 e contratos de protocolo/configuração/
transporte/fila antes de retomar Java. ADR-033 mantém as políticas residuais;
a sua aprovação histórica não aprova o transporte agora proposto. IMP-035
conserva o seu checkpoint de intenção/admissão/reparação.

Não foram executados os testes, manual ou captura do novo protocolo. As execuções
anteriores, incluindo erros de compilação, sandbox, um teste novo falhado e
compilação sem testes, mantêm a classificação no registo de preservação.
Nenhum commit, push, integração ou outro IMP faz parte desta autorização.

## 10. Arquivo e fixtures independentes; migração de testes

[Contrato/fixtures v1 arquivados](closure-walkthrough.md#limpeza-autorizada),
com inventário de hashes e tar preservados. A cópia inclui 29 TCP v1 e 16 UDP;
UDP continua operacional e inalterado. Os originais em Projeto/ só serão
migrados após aprovação, sem apagar a evidência histórica.

[50 fixtures TCP v2 propostas](../../../experiments/construction/034-overlay-extra/protocol-v2-proposal/README.md)
cobrem 18 tipos: 22 aceites, 23 rejeições terminais e cinco rejeições de conteúdo
condicionadas à sessão. Construídas por campos literais/struct fora do Java,
sem alterar byte de versão de fixtures v1 nem usar encoder de produção; auditor
separado confere framing, campos, hashes e negativos. Isto é auditoria offline
do oráculo, não execução do codec Java, transporte ou V-033.

[Migração dos testes](test-contract-migration.md) identifica mudanças de versão,
HELLO payload/offsets, enum e fixtures, preservando garantias. Não conservar
um codec v1 para manter testes verdes; não excluir testes antigos nem produzir
valores esperados com o encoder. Q=1 genérico/bootstrap, erro agregado offline,
identidade/unicidade/FIFO/flush/EOF/deadlines/shutdown permanecem. Alterações
executáveis e red/green/regressão/manual só depois de aprovação.

## Atualização de fecho — Q-C-19

2026-11-11T19:42:24Z: Luis repetiu o manual V-033 com 53 checks/saída 0 e pediu revisão final, integração e fecho. A revisão técnica está concluída. Por instrução explícita foram retirados os arquivos TCP v1 e a variante abandonada; as referências anteriores a retenção de arquivos são históricas. Negativos de versão TCP v1 e todo UDP OTT1 continuam exigidos. [Estado e limpeza atuais](closure-walkthrough.md).
