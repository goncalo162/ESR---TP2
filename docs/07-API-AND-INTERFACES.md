# Interfaces e contratos propostos

> Arquitetura normativa: [add.md](../add.md). Requisitos consolidados: [specs.md](../specs.md). Este documento mantém detalhe técnico/proveniência e deve ser interpretado pela precedência do ADD; não é uma autoridade arquitetural concorrente.

**Contrato aprovado ADR-034/035: controlo TCP uniforme v2,19 tipos e capacidades3.**
IMP-034 está integrado; IMP-035 está implementado para revisão antes de Test.
Bootstrap, handshake, mensagens normais e descoberta usam o mesmo codec, sem
runtime v1/fallback. Mesmo programa/versão de JAR em todos os processos.
A regressão completa/manual humano desta versão continuam pendentes.

[Contrato e fixtures v1 arquivados](../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md#limpeza-autorizada).
Bootstrap mantém só consulta do grafo imutável inicial, sem membership,
renovação, leases de registo ou notificações de saída; muda apenas o contrato
TCP para v2. UDP OTT1/version 1 de vídeo permanece inalterado. Não há HTTP,
dependências runtime novas ou serviço externo.

## 1. CLI e configuração

Sintaxe futura: java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role BOOTSTRAP|SERVER|RELAY|CLIENT --config CAMINHO. Caminho e papel são obrigatórios e devem coincidir com o ficheiro; opções desconhecidas são erro. --help apresenta utilização. BOOTSTRAP é um processo de controlo, separado do processo SERVER mesmo quando partilham host.

Para CLIENT propor --stream UUID opcional para seleção inicial e --headless para testes de receção sem janela; a demonstração de reprodução exige GUI. Comandos no stdin local: catalog, routes, neighbors, metrics, play UUID, stop, trace UUID e quit. Um cliente consome uma stream de cada vez; vários processos permitem clientes simultâneos. play de outra stream cancela a anterior após validar a nova escolha. Não implementar pausa/seek nem controlo remoto.

Códigos de saída propostos: 0 fim normal/help; 2 configuração/CLI inválida; 3 erro fatal de recursos locais, bind ou ficheiro de origem inválido. Indisponibilidade temporária de bootstrap, vizinho ou stream mantém o processo com evento explícito e retries limitados por tentativa.

| Propriedades | Tipo / comportamento |
|---|---|
| node.id, node.role, bind.host, advertised.host | obrigatórias; bind pode ser 0.0.0.0, advertised não pode ser wildcard |
| control.port, data.port | inteiros obrigatórios 1–65535 nos nós overlay; BOOTSTRAP exige control.port e não aceita data.port; não há defaults implícitos no loader. Exemplos locais usam 5002 no bootstrap e 5100/5101 a 5400/5401 nos outros perfis |
| bootstrap.host, bootstrap.port | obrigatórias para nós overlay; porta explícita, 5002 nos exemplos locais. Não se aplicam ao perfil BOOTSTRAP |
| topology.file | só bootstrap; resolve relativamente ao ficheiro .properties, não ao cwd |
| streams.file | só SERVER; resolve relativamente à configuração |
| media.base | diretório local do servidor para resolver os paths autorizados |
| node.ids | em topology.properties: lista CSV de 1–64 IDs, sem campos vazios/duplicados; cada nó exige pelo menos um vizinho permitido |
| node.ID.host, node.ID.controlPort, node.ID.dataPort, node.ID.role | endpoints/papel de cada nó permitido |
| node.ID.neighbors | lista CSV de IDs existentes; grafo simétrico, sem auto-vizinhança; X é o comprimento desta lista |
| stream.ids | no catálogo de cada origem: 2–256 UUID canónicos separados por vírgula; únicos no catálogo e no conjunto validado por loadDeployment |
| stream.UUID.title, .file, .fps, .loop | título; path local; fps inteiro 1–60; loop true/false; mínimo final de dois ficheiros distintos por origem |

O listener TCP pode usar bind.host wildcard. Para preservar a validação da origem UDP em nós multihomed, o socket UDP deve ser ligado ao endereço local advertised.host e data.port, enviando e recebendo no mesmo socket. Validar que esse endereço pertence ao nó e é alcançável pelos seus vizinhos. Não aceitar host/port incompatíveis para a identidade registada. Não deduzir IP publicitado de localhost nem do primeiro interface num nó CORE multihomed. O operador atribui IP roteável e confirma reachability no cenário. Não há variáveis de ambiente da aplicação obrigatórias; DISPLAY é do ambiente gráfico e JAVA_HOME do runtime.

### Regras de configuração implementadas na TASK-003

Ficheiros UTF-8 com sintaxe de java.util.Properties, incluindo escaping e continuação de linha. Rejeitar chaves desconhecidas, duplicadas após descodificar escapes, vazias ou incompatíveis com o papel. IDs/papéis/booleanos são estritos; loop só admite true/false. UUID deve ter 36 caracteres na forma canónica, sem versões abreviadas. Listas CSV não podem conter campos vazios ou duplicados. O limite inicial de 256 streams por catálogo é uma escolha de proteção do protótipo, não do enunciado.

Paths de topology.file, streams.file e media.base são relativos ao ficheiro de configuração do nó; stream.UUID.file é relativo a media.base. Normalizar para paths absolutos e rejeitar escape lexical de media.base, referência à própria base e aliases do mesmo path dentro de uma origem. Os vídeos não têm de existir nesta tarefa: bytes, symlinks reais e diferença do conteúdo são validados em TASK-020 antes de abrir os media.

Hosts são verificados sintaticamente, sem resolver DNS ou abrir sockets. Aceitam-se IPv4, IPv6 literal e hostnames ASCII; wildcard é exclusivo de bind.host. Comparação de endpoints é lexical após normalização simples (caixa, ponto final e representação decimal IPv4), por transporte/host/porto. Aliases DNS, identidade física do namespace, pertença do IP e bind/reachability são verificações futuras de transporte/CORE, não garantias da carga offline.

load(path, expectedRole) valida um perfil, incluindo a topologia ou catálogo dependente. loadDeployment(paths) deve receber exatamente um perfil BOOTSTRAP e todos os perfis do seu grafo; verifica papéis/endpoints/contacto bootstrap e unicidade dos UUID entre as origens fornecidas. A configuração do servidor fixa o serverId dos seus fluxos. Não exigir duas origens em todos os cenários simples; os exemplos finais desta tarefa incluem duas, cada uma com dois UUID. instanceId e serverEpoch são gerados em execução, não pelo parser.

A API devolve modelos imutáveis; em qualquer erro lança ConfigurationException com lista imutável de ConfigurationProblem(file, key, message), sem devolver configuração parcial. Não há CLI de execução nesta tarefa; OttApplication será implementada na TASK-008. [Código, testes e resultados V-003](../experiments/task-003-configuration-validation.md).

### Perfis mínimos de conteúdos

Usar ficheiros separados por origem: streams-s1.properties lista apenas A/B; streams-s2.properties lista apenas C/D. O streams.properties vazio existente não é um catálogo global interpretado por ambos os servidores. server-1.properties e server-2.properties referenciam o respetivo ficheiro. O serverId é sempre o node.id do processo de origem, não é escolhido pelo cliente.

| Origem | Alias visual | UUID proposto estável | Ficheiro proposto, ainda por obter/validar |
|---|---|---|---|
| S1 | A | 00000000-0000-4000-8000-000000000001 | a.Mjpeg |
| S1 | B | 00000000-0000-4000-8000-000000000002 | b.Mjpeg |
| S2 | C | 00000000-0000-4000-8000-000000000003 | c.Mjpeg |
| S2 | D | 00000000-0000-4000-8000-000000000004 | d.Mjpeg |

Os aliases são rótulos do plano; a chave na rede é o UUID. Os nomes de ficheiro e UUID são exemplos técnicos fixados para os perfis propostos, não recursos existentes nem valores do enunciado. Os ficheiros reais têm de representar vídeos distintos por origem e ter proveniência documentada. Um cliente que entra a meio recebe a transmissão corrente; não há sessão de reprodução independente, seek ou retransmissão do início por cliente. Parar a última procura suspende a fonte; retomar continua a sequência e a posição, salvo reinício explícito de época.

## 2. Parâmetros e limites iniciais

A tabela conserva os valores de partida do plano. O ADD, secção 6.3, distingue parâmetros operacionais opcionais e hard caps binários/de proteção. TASK-032 implementará apenas as chaves operacionais aprovadas. Não tornar formatos/fragmentos v1 configuráveis por inferência desta tabela.

| Parâmetro | Inicial |
|---|---:|
| TCP connect / HELLO timeout | 2 s / 3 s |
| Sonda / timeout / falhas consecutivas | 1 s / 1 s / 3 |
| EWMA alpha | 0,2 |
| Anúncio / validade de candidato | 2 s / 8 s |
| Lease de subscrição / renovação | 6 s / 2 s |
| Timeout de ACTIVATE / tentativas por ronda | 3 s / 3 |
| Retry de vizinho/bootstrap | backoff 0,5 s até 5 s, jitter até 10% |
| Melhoria para troca / observações / permanência | 20% / 3 / 5 s |
| IDs máximos no path / nós por cenário | 32 (até 31 arestas) / 64 |
| Frame TCP máximo, sem prefixo | 65536 bytes |
| Fila de saída TCP por vizinho | 256 mensagens; overflow fecha conexão com razão |
| Fila de eventos do nó | 4096; overflow de eventos de controlo gera erro operacional explícito |
| Datagrama de aplicação máximo | 1200 bytes, incluindo cabeçalho de 64 |
| Frame JPEG máximo | 2 MiB; máximo de fragmentos ceil(2 MiB / 1136)=1847 |
| Montagens simultâneas por stream / deadline | 8 / 250 ms desde primeiro fragmento |
| Janela de deduplicação de vídeo | 512 frames por stream/session, com bitsets de fragmentos |
| Fila UDP egress por vizinho | 256 datagramas; descartar o mais antigo e contar |
| Flooding cache / validade | 4096 chaves / 16 s; rate limit 10 reenvios/s por stream/vizinho |

Ajustar prazos em conjunto: candidato válido por mais de um período de anúncio, lease maior que renovação, capacidade de buffer compatível com taxa do vídeo. Não aumentar buffers para esconder congestionamento.

## 3. TCP: framing e tipos primitivos

Cada mensagem: uint32 big-endian length, seguido de exatamente length bytes; length inclui envelope e payload, exclui o próprio prefixo. Validar 37 <= length <= 65536 antes de alocar. Ler prefixo/corpo com leitura completa; TCP pode fragmentar ou juntar escritas. Um único escritor por conexão preserva frames.

Envelope, por ordem: version u8 (=2), type u8, requestId UUID (16 bytes), senderId string, senderInstance UUID (16 bytes). Comprimento mínimo com ID de um byte: 1+1+16+2+1+16=37 bytes. Não tratar IDs de um byte como malformados.

Primitivas: u8/u16/u32 sem sinal na rede e lidos em tipos Java suficientes; i64 não negativo para sequências/custos/tempos de media; UUID em dois longs big-endian, bits preservados; bool u8 0/1. String = u16 de comprimento de bytes + UTF-8 estrito sem NUL; nodeId até 32 bytes ASCII, title até 512 bytes e 128 pontos de código Unicode, mensagens de erro até 256 bytes. Lista = u16 count + elementos; node list até 64, path até 32. Não usar writeUTF/readUTF como se fossem UTF-8 comum. Bytes extra no fim do payload são erro de versão/formato. Todas as listas abaixo usam essa contagem.

StreamInfo na rede: streamId UUID, serverId string, title string, codec u8 (1=MJPEG), fps u16 (1–60), serverEpoch UUID. Endpoint: nodeId string, instanceId UUID (obrigatoriamente zero em REGISTER_OK, não zero em REGISTER/HELLO), host string ASCII de 1–253 bytes, controlPort u16 e dataPort u16 (ambos 1–65535), role u8 (1 SERVER, 2 RELAY, 3 CLIENT). O bootstrap fornece endpoints configurados, nunca a instância viva de um vizinho; esta é conhecida no HELLO direto. senderId, serverId e nodeId obedecem a [A-Za-z0-9_-]{1,32}; título tem 1–128 pontos de código Unicode e até 512 bytes UTF-8. A identidade no envelope é sempre a do emissor imediato, inclusive quando reencaminha anúncio de outra origem. Um elemento não válido rejeita a mensagem inteira.

### API alvo do codec único v2 (migração proposta)

MessageCodec.encode(ControlMessage) devolve os bytes de exatamente um frame; decode(byte[]) exige exatamente um frame e rejeita bytes adicionais. write(message, OutputStream) valida/serializa antes de escrever no destino; read(InputStream) lê um frame completo e devolve Optional.empty apenas perante EOF limpo antes do prefixo. EOF a meio do prefixo/corpo é EOFException; campos incompletos num corpo já recebido são ProtocolException INVALID_MESSAGE. Comprimento superior ao máximo, incluindo u32 com bit de sinal, é rejeitado antes da alocação do corpo. Strings e listas também verificam máximos antes de alocar.

ControlMessage contém envelope, payloads tipados/imutáveis e Endpoint/StreamInfo do contrato. NeighborList(List<Endpoint>) é o payload de REGISTER_OK e não tem leaseMs. MessageType tem19 tipos ativos (1,2,4..20) e não inclui renovação de registo; o código 3 é rejeitado como INVALID_MESSAGE. MessageType e ErrorCode usam números explícitos; ProtocolException expõe errorCode sem enviar resposta nem fechar a conexão. Versão desconhecida é UNSUPPORTED_VERSION, formato/domínio incorreto é INVALID_MESSAGE e limites de tamanho excedidos são RESOURCE_LIMIT. Payload incompatível com o tipo também é rejeitado.

O chamador gere flush/close e o único escritor por conexão. O codec não abre sockets. UUID são opacos e preservam todos os bits; a exceção de Endpoint.instanceId desconhecido aplica-se apenas a REGISTER_OK. u32 aceita 0..4294967295 e é exposto como long. Listas vazias, TTL zero ou percursos repetidos podem ter representação sintática; os handlers têm de verificar a validade operacional, origem/prefixo, ciclos, frescura e leases antes de agir. Host no Endpoint tem validação de domínio ASCII/comprimento; o handshake terá de o comparar com a configuração autorizada.

[Implementação/validação original v1](../experiments/task-004-codec-validation.md)
e [correção histórica do bootstrap](../experiments/bootstrap-discovery-correction.md)
são proveniência, não execução v2. O arquivo conserva 29 fixtures TCP v1 e
16 UDP; [50 novos vetores TCP v2 independentes](../experiments/construction/034-overlay-extra/protocol-v2-proposal/README.md)
estão propostos e auditados offline, sem produção Java. A futura migração de
recursos/testes conserva 16 UDP e troca os vetores TCP operacionais por v2.
Versão !=2 é UNSUPPORTED_VERSION, sem decoder v1. readFrame partilha o mesmo
parser e devolve Decoded(ControlMessage) ou RejectedExtraContent tipado; read
estrito preserva assinatura/erros sem oferecer outra versão. Ver secção extra.

### API de transporte implementada na TASK-005

`new Transport(bind, listener[, options])` abre o listener TCP; porta 0 permite testes efémeros. `localAddress()` devolve o endpoint real. `connect(remote)` devolve `CompletableFuture<Connection>` e faz o dial fora do chamador, com timeout default de 2 s. Endpoints devem estar resolvidos: resolver DNS fora do executor serial do nó. Limite/falha/shutdown completa o futuro excecionalmente; cancelamento fecha o socket da tentativa, inclusive numa corrida com estabelecimento. Não há retries automáticos.

`Connection.id()` devolve UUID único por ligação, ainda sem associação a nodeId. `send(message)` valida/serializa e devolve `ACCEPTED`, `CLOSED`, `OVERFLOW` ou `INVALID_MESSAGE`; a thread chamadora nunca escreve na rede. ACCEPTED significa enfileiramento, não entrega/ACK. O escritor único preserva frames e ordem da fila, inclusive com envios concorrentes. Fila default de 256 frames; overflow limpa a fila e fecha apenas a ligação saturada com OUTPUT_OVERFLOW. Uma mensagem inválida de saída não fecha uma ligação saudável.

`Listener.connectionOpen`, `message` e `connectionClosed` executam sequencialmente no leitor da mesma ligação; fecho tem razão explícita e evento único. Ligações diferentes produzem callbacks em paralelo. Callbacks e continuations síncronas dos futuros de dial/escrita devem terminar rapidamente, publicando no futuro executor do nó sem esperar por ACK ou por filas cheias. RuntimeException em open/message fecha a ligação com CALLBACK_ERROR; exceção no callback de fecho é isolada. EOF limpo, truncatura, protocolo e I/O têm razões distintas. Não se envia ERROR automaticamente sem contexto seguro.

`Options` limita queueCapacity a 1–256 e maxConnections a 1–64 (defaults 256/64). Reservas incluem dials e permanecem até ambos os workers I/O terminarem. Um worker accept, um por dial pendente e dois por ligação; threads virtuais Java 21 sem fila de executor ilimitada. `connectionCount()` conta reservas pendentes/em terminação; `queuedMessages()` devolve profundidade da fila. Cada frame tem até 65540 bytes com prefixo; existe ainda um frame em escrita e serializações concorrentes dos chamadores. São limites de frames, sem medição de consumo máximo de memória.

`close()` fecha listener/sockets, limpa filas e interrompe workers. Chamador externo aguarda um deadline global default de 3 s; timeout lança erro explícito se um callback bloquear. Chamado por callback, só inicia shutdown para evitar joins circulares. `awaitTermination(duration)` externo confirma terminação. Close é idempotente, sem acesso a bootstrap. `Connection.close()` solicita fecho local e notificação assíncrona. Atenção: ACCEPTED seguido imediatamente de close não garante envio; o bootstrap do IMP-003 usa sendAndFlush para confirmar a escrita completa antes de fechar. requestClose inicia shutdown sem espera; close mantém a espera externa/atalho dos workers. As reservas continuam até ambos os workers terminarem.

Extensão local implementada em TASK-006 / IMP-003 / ADR-015: Connection.sendAndFlush(ControlMessage) devolve CompletableFuture<Void>, com sucesso no escritor após frame inteiro e flush. Partilha a fila de send e acompanha também o frame retirado/em escrita; fecho/overflow/I/O/shutdown terminam confirmações fora dos locks, antes de release. Cancelamento não promete desfazer bytes. Fila vazia não prova escrita completa e flush não prova entrega remota/ACK; send mantém a semântica anterior. O deadline total da consulta fecha o socket e termina pendências; não se cria timeout genérico por frame. [Walkthrough e incrementais](../memory-bank/bolts/002-overlay/implementation-walkthrough.md): 856 testes incrementais aprovados; [Test com clean verify/JDK21 e manual Codex/34 checks](../memory-bank/bolts/002-overlay/ddd-03-test-report.md) executados e Test aprovado, sem alteração da implementação. Luis repetiu em 2026-11-04T13:58:36Z–13:58:38Z, 34 checks/MANUAL_PASS/JAVA_EXIT=0, mesmo JAR; log pessoal separado do agente. Revisão final atribuída a Waheed confirmada pelo utilizador e versão revista integrada em main; TASK-006 DONE, bolt 002-overlay complete.

BootstrapServer(NodeConfiguration) é serviço embutível com localAddress()/close(), validando antes de bind e normalizando lexicalmente hosts sem DNS do pedido. Só REGISTER, uma decisão por conexão, grafo imutável/instâncias zero/sem lease. Core serial/contextos limitados, timers canceláveis e shutdown com budget global. Composição CLI/HELLO continua no IMP-005/004, sem processos overlay completos demonstrados.

A assinatura conceptual `sendControl(neighborId, message)` depende do mapa futuro de vizinhos; nesta tarefa o envio usa Connection diretamente. Sem handshake, autorização, retries, CLI ou processos JVM independentes demonstrados. Os testes usam TCP real dentro da JVM de testes. Contrato bootstrap/NeighborList corrigido por ADR-013 preservado. [V-005](../experiments/task-005-transport-validation.md).

## 4. Mensagens de controlo v1

Os números são fixados por este contrato. Campos do envelope não são repetidos nos payloads. Respostas preservam requestId do pedido; mensagens periódicas usam um UUID novo.

| type | Nome | Payload por ordem / efeito |
|---:|---|---|
| 1 | REGISTER | Endpoint local; pedido de consulta, valida identidade/endpoint/papel contra configuração sem guardar registo vivo |
| 2 | REGISTER_OK | apenas lista Endpoint dos vizinhos configurados, todos com instanceId zero; sem lease ou disponibilidade |
| 3 | Retirado/reservado | rejeitar como INVALID_MESSAGE; não implementar renovação de registo |
| 4 | NODE_LEAVE | reason u8 (0 normal, 1 erro); notifica exclusivamente vizinhos diretos, nunca bootstrap |
| 5 | HELLO | Endpoint local + supportedMask u32 + requiredMask u32, ambos3; identidade/endpoint/grafo/instância/iniciador validados |
| 6 | HELLO_OK | selectedMask u32=3; confirma o contrato obrigatório após as barreiras de handshake/flush |
| 7 | PROBE_PING | probeId i64 |
| 8 | PROBE_PONG | probeId i64, eco da sonda; RTT calculado com relógio local |
| 9 | STREAM_ANNOUNCE | StreamInfo, announceSeq i64, costMicros i64, path lista nodeId; path termina no sender |
| 10 | STREAM_WITHDRAW | streamId UUID, serverEpoch UUID, announceSeq i64; invalida candidato do sender, sem eliminar alternativas de outros vizinhos |
| 11 | ACTIVATE | streamId UUID, serverEpoch UUID, subscriptionId UUID, revision i64, leaseMs u32, expectedPath lista nodeId; path da fonte até ao sender |
| 12 | ACTIVATE_ACK | streamId UUID, serverEpoch UUID, subscriptionId UUID, revision i64, grantedLeaseMs u32 |
| 13 | DEACTIVATE | streamId UUID, serverEpoch UUID, subscriptionId UUID, revision i64 |
| 14 | SUBSCRIPTION_RENEW | mesmos campos de ACTIVATE; revalida prefixo/epoch e prolonga lease, respondendo com ACTIVATE_ACK |
| 15 | ROUTE_TRACE | streamId UUID, serverEpoch UUID, traceId UUID, ttl u8, visited lista nodeId; encaminhado a montante conforme upstream selecionado |
| 16 | TRACE_RESULT | streamId UUID, traceId UUID, visited lista nodeId; resposta pelo caminho inverso do pedido |
| 17 | ERROR | code u16, relatedType u8, detail string; correlacionado por requestId |
| 18 | DISCOVERY_QUERY | helloRequestId UUID, recipientId string, recipientInstance UUID; só sessão ACTIVE_DISCOVERY |
| 19 | DISCOVERY_SNAPSHOT | campos da QUERY + generation u64 positiva + count u16 (0..63) e endpoints diretos; cap extra 32768 |
| 20 | REPAIR_HELLO | payload Hello comum, máscaras3; admissão reparadora cooperativa ADR-035, mesma conexão/ACK6 |

Bootstrap usa uma conexão TCP curta **v2** por consulta inicial: REGISTER → REGISTER_OK ou ERROR, seguido de fecho após envio completo. O requerente usa os endpoints configurados devolvidos para falar diretamente com os vizinhos. Falhas antes de obter resposta permitem retry limitado por tentativa; depois do sucesso não existem renovação, consultas periódicas, heartbeats ou NODE_LEAVE para bootstrap. Consultas repetidas não alteram a configuração; IDs duplicados/instâncias e saídas são geridos entre vizinhos. O handler de bootstrap só aceita REGISTER e responde da configuração, mesmo que os vizinhos estejam offline. O codec comum pode representar outras mensagens; isso não autoriza o handler de bootstrap a processá-las como operações suas.

O transporte de vizinhos mantém conexão full-duplex persistente. Handshake precede as mensagens de aplicação e descoberta; 3 continua retirado. TYPE desconhecido ou versão diferente fecha ligação após ERROR se for seguro responder. TYPE do envelope só admite1,2 e4–20, sempre version=2; relatedType no payload ERROR é o octeto bruto do tipo que falhou, podendo conter um valor desconhecido. reason em NODE_LEAVE só admite 0/1 e code em ERROR só admite 1–12. Um decoder rejeita formato, limites e valores enumerados inválidos; autorização do vizinho, correspondência HELLO/envelope, frescura, caminhos e subscrições são verificações de contexto nos componentes responsáveis. Uma fixture rejeitada não obriga a enviar ERROR quando o envelope não é seguro para resposta.

STREAM_ANNOUNCE de uma fonte tem path=[serverId], custo=0 e sequência estritamente crescente por stream/epoch. Cada relay publica o seu path e custo acumulado; o recetor acrescenta-se antes de guardar/reenviar. Epoch muda a cada arranque do SERVER e coincide com sessionId dos datagramas. Ao ver nova época confirmada por anúncios de caminhos válidos, retirar a antiga até expirar o horizonte; não aceitar regresso a uma época retirada por anúncio atrasado.

ACTIVATE do cliente C para P leva path=[S,...,P,C]. P retira o último ID, verifica prefixo igual ao seu selecionado e regista a procura de C. Se já está ACTIVE com esse prefixo/epoch, confirma imediatamente; caso contrário ativa o seu upstream com subscriptionId própria. O ACK só é propagado para os filhos pendentes após confirmação a montante. Um ERROR que vem de montante é correlacionado ao pedido local, limpa PENDING e é traduzido para os requestId dos filhos pendentes. Nenhuma thread bloqueia à espera do ACK.

Renovação ou cancelamento só pode alterar a subscrição da conexão/identidade autenticada pelo handshake local e com ID/revisão correspondentes; isto é validação de consistência, não autenticação criptográfica. Após reconexão renegociar subscrições. Pedidos expirados ou cancelados ignoram ACK atrasado. Prazo esgotado permite nova tentativa com outro candidato/caminho atualizado.

Erros: 1 INVALID_MESSAGE; 2 UNSUPPORTED_VERSION; 3 UNAUTHORIZED_NEIGHBOR; 4 DUPLICATE_ID; 5 UNKNOWN_STREAM; 6 NO_ROUTE; 7 PATH_CHANGED; 8 LOOP_DETECTED; 9 STALE_REVISION; 10 TIMEOUT; 11 RESOURCE_LIMIT; 12 SOURCE_UNAVAILABLE. Não enviar paths de ficheiros locais ou stack traces ao vizinho.

## 5. UDP: cabeçalho e fragmentação

Formato próprio v1; **não é RTP**, não usa multicast IP e não assume compatibilidade com players externos. Cada datagrama tem cabeçalho fixo de 64 bytes em big-endian:

| Offset | Bytes | Campo |
|---:|---:|---|
| 0 | 4 | magic ASCII OTT1 |
| 4 | 1 | version=1 |
| 5 | 1 | flags=0, bits reservados rejeitados |
| 6 | 2 | headerLength=64 |
| 8 | 16 | streamId UUID |
| 24 | 16 | sessionId UUID = serverEpoch |
| 40 | 8 | frameSeq i64, começa em 0, cresce por frame |
| 48 | 8 | mediaTimeUs i64, relativo à timeline da fonte |
| 56 | 4 | frameLength u32, 1..2097152 |
| 60 | 2 | fragmentIndex u16, começa em 0 |
| 62 | 2 | fragmentCount u16, 1..1847 |
| 64 | variável | payload, 1..1136 bytes |

fragmentCount = ceil(frameLength/1136); cada fragmento exceto o último tem 1136 bytes, último tem o resto não nulo. O datagrama é no máximo 1200 bytes de aplicação. Não contar IP/UDP nesses 1200; verificar MTU efetivo da bancada; se incompatível, documentar o gate e propor revisão do contrato/fixtures por ADR, sem reduzir silenciosamente o fragmento v1 por nó. O relay reenvia os mesmos bytes sem alterar sequência, stream ou origem temporal.

Aceitação: endpoint UDP recebido coincide com HELLO do upstream confirmado; stream/epoch ativas; índice, tamanho, magic e versão válidos. Metadados são consistentes durante a montagem. Duplicados são ignorados. Frame incompleto ao deadline é descartado; não há retransmissão. A janela dedup de 512 frames é limitada, rejeita frames mais antigos que a janela e não renova leases. Nenhum datagrama por si só cria ou ativa rota.

mediaTimeUs não é horário UTC e não permite medir atraso unidirecional entre máquinas sem outra referência de relógio. Ao repetir o ficheiro, manter sequência e timeline monotónicas. Antes de atingir Long.MAX_VALUE reiniciar sessão de forma explícita com nova época. Não prometer continuidade por EOF quando loop=false; notificar indisponibilidade da origem e preservar os outros fluxos.

## 6. Contratos internos

| Interface conceptual | Entrada / saída | Garantia |
|---|---|---|
| ConfigurationLoader.load(path, role) | propriedades → NodeConfiguration ou ConfigurationException com problems | implementado na TASK-003, sem efeitos de rede |
| ConfigurationLoader.loadDeployment(paths) | perfis completos → lista imutável ou problemas agregados | implementado na TASK-003; verifica grafo, endpoints, contacto bootstrap e unicidade global dos UUID |
| MessageCodec.read/write e encode/decode | ControlMessage ↔ frame binário v1 | implementado na TASK-004; framing, EOF, UTF-8 e limites, sem sockets |
| Connection.send(message), Transport.connect(remote) | SendResult; CompletableFuture de dial | TASK-005 implementada; framing/backpressure, sem ACK; associação a neighborId em TASK-007 |
| NeighborManager.snapshot() | nenhum → vizinhos imutáveis | não expor mapas mutáveis |
| NetworkMonitor.onPong(id, now) | sonda correlacionada → atualização de métrica/evento | relógio monotónico do emissor |
| RoutingService.onAnnouncement/activate/deactivate | evento → novo estado + mensagens | executado no proprietário serial do estado |
| RoutingTable.forwardingSnapshot(streamId) | StreamKey → upstream/epoch/downstream ativos | leitura atómica sem bloquear UDP |
| StreamForwarder.accept(packet, sourceEndpoint) | datagrama → envios/local delivery/descartes | não descodifica JPEG |
| VideoSource.nextFrame() | → JPEG válido ou EOF/erro | formato real confirmado; limite antes de alocação |
| FrameAssembler.accept(fragment, now) | → frame completo ou nenhum | armazenamento limitado e timeout |
| VideoPlayer.showFrame(image) | imagem pronta → atualização visual | executa atualização Swing no EDT |

As APIs de configuração, codec e transporte acima já existem. As restantes assinaturas Java serão criadas nas tarefas correspondentes; a presença no desenho não significa implementação.

## 7. Perfil de ficheiro MJPEG confirmado na TASK-002

O recurso docente movie.Mjpeg usa registos consecutivos: exatamente cinco bytes ASCII de dígitos decimais (0–9), com zeros à esquerda quando necessário e sem newline/separador, que indicam o tamanho L, seguidos de exatamente L bytes JPEG. O tamanho não inclui os cinco bytes do prefixo. Exemplo real: os primeiros bytes são 30 36 30 31 34, texto 06014, seguidos de 6014 bytes JPEG; o segundo registo começa no offset 6019. Não é MP4, AVI, multipart HTTP ou uma sequência de JPEG sem prefixos.

EOF é limpo apenas entre registos. Prefixo parcial, caractere não decimal, comprimento zero ou JPEG truncado são erros. Neste perfil, L está entre 1 e 99999; o limite efetivo da fonte é o menor entre 99999, maxFrameBytes configurado e o máximo de 2 MiB do protocolo UDP. O limite UDP continua a servir outros tamanhos sintéticos nos testes de fragmentação; não altera a capacidade de cinco dígitos do ficheiro adotado. FPS não está codificado no ficheiro: continua a vir da configuração. O exemplo Java docente usa período de 100 ms (10 FPS), valor observado e não requisito obrigatório.

A TASK-020 implementará o leitor robusto e validará descodificação JPEG; nesta tarefa houve apenas inspeção dos bytes e dos exemplos, sem copiar/implementar o leitor. Hashes, conteúdos identificados, proveniência e gates estão no [registo V-002](../experiments/task-002-contract-review.md). Referências binárias/hexadecimais de controlo e UDP: [fixtures](../Projeto/src/test/resources/protocol/README.md).


## 8. Parametrização e falha exclusiva UDP

Resposta 2A: chaves/defaults/papéis/relações de RuntimeConfiguration estão no
[ADD, secção 6.3](../add.md). A carga offline mantém defaults dos perfis atuais
e valida antes de sockets. Não muda os bytes v1 ou a semântica de send.

Resposta 3B / ADR-025: deteção automática continua a usar TCP/sondas. Bloqueio
apenas de UDP com controlo saudável não garante troca de parent/recuperação.
O teste deve documentar interrupção/counters e essa limitação sem criar watchdog.

## Estado incremental de NeighborManager — IMP-004

Implementação local preparada, ainda sujeita ao checkpoint Implement.
`NeighborManager(NodeConfiguration, UUID, Observer)` fornece `start()`,
`snapshot()`, `diagnostics()`, `send(Session, ControlMessage)`, `disconnect(Session)`,
`requestClose()`, `awaitTermination(Duration)` e `close()`. O construtor com
`SerialOwner` permite partilhar o dono serial do nó. Eventos tipados incluem
NeighborUp/NeighborDown/Received/Diagnostic; snapshots contêm Session imutável.
READY confirma a instalação da lista, sem prometer todos os peers online.
ERROR ACTIVE é entregue intacto ao consumidor; envio ACCEPTED só enfileira.
[Implement e limites](../memory-bank/bolts/003-overlay/implementation-walkthrough.md).
As referências anteriores a HELLO por implementar são históricas; CLI continua
por implementar. Formatos v1 não foram alterados. Test/manual/revisão final e
integração permanecem pendentes; não se declara API aprovada ou serviço entregue.

## IMP-004 — Test preparado, aceitação pendente

Implement aprovado explicitamente pelo utilizador; [relatório Test](../memory-bank/bolts/003-overlay/ddd-03-test-report.md)
preparado em 2026-11-10T16:26:35Z. Clean verify atual: 903 testes (47 de vizinhança),
zero falhas/erros/ignorados. Manual interativo executado pelo agente Codex no
JAR verificado: 49 CHECK_PASS, MANUAL_PASS/CLEANUP_COMPLETE, saída Java 0.
XML/JAR/hash/perfis/log e instruções de repetição pessoal preservados em
experiments/construction/003-overlay/. Código aprovado e ferramentas intactos
durante Test. V-007/D01–D22 têm consolidação técnica local, com limites explícitos.
Q-C-08 continua aberta: V-ADD reprovado com três erros, sem alteração de ferramentas
ou redução do alcance. Test ainda não aprovado; revisão final humana e integração
pendentes. TASK/bolt/história/unidade permanecem em progresso, sem implemented=true,
commits/push/fecho ou GUI/CORE presumidos.

## Estado atual — IMP-004 fechado

Registo documental em 2026-11-10T17:12:15Z.
IMP-004/003-overlay/TASK-007 fechado documentalmente por autorização explícita
do utilizador. Integração local comprovada em main: `8db7b78`
(`8db7b78950e444f3c2917844617be4b9b236ec0f`), com 145 caminhos e hashes conferidos.
Test aprovado: clean verify/903 testes, manual humano Francisco/49 checks/Java 0
confirmado pelo utilizador, V-ADD aprovado; revisão final de Denji aprovada sem
alterações pendentes. Q-C-08 resolvida e limitada a patch/evidência na integração.
Bolt complete; história complete/implemented=true; TASK-007 DONE. A unidade
002-overlay mantém in-progress: 020-overlay e 004-overlay continuam planned,
sem início de outro IMP. Fecho documental sem alteração Java ou repetição do manual.
Sem push ou alegação de integração remota/entrega do projeto.

[Fecho documental](../memory-bank/bolts/003-overlay/closure-walkthrough.md). As referências anteriores a estados
pendentes são históricas; não descrevem o estado atual. Não se promovem
requisitos globais, PHASE-02, unidade ou intent a concluídos por este fecho.

## IMP-005 — extensão lifecycle planeada (Q-C-09)

Model aceite e exceção de âmbito autorizada explicitamente pelo utilizador;
Plan ainda por aprovar. Contrato futuro, sem Java alterado:
`NeighborManager.requestShutdown(int reason, Duration remainingBudget)` devolve
`CompletionStage<ShutdownReport>`; reason v1 0/1. Relatório imutável com tentativas
por Session e resultado LOCAL_WRITE_CONFIRMED/WRITE_FAILED/TIMED_OUT/ABORTED,
mais notificationTimedOut. Sucesso confirma apenas write+flush local, nunca ACK.

O primeiro pedido fecha admissão de comandos/retries; no owner captura apenas
sessões diretas ACTIVE correntes, revalidando connectionId e instanceId. Nunca
envia para bootstrap/handshakes/sessões obsoletas nem cria outro transporte.
Notificação paralela limitada ao budget restante global, com reserva para cleanup;
falha/timeout obrigam a fechar. Repetições não renovam prazo nem repetem envios.
requestClose imediato preempta drenagem; awaitTermination físico continua separado,
fora do owner. send de aplicação mantém filtro 7–17 e semântica existente.

Exceção autorizada: NeighborManager.java e novo NeighborManagerLifecycleTest.java
somam-se aos três ficheiros de IMP-005. Não reabre nem altera o fecho histórico de
IMP-004; testes D01–D22 preservados. Sem nova ADR/política/bytes/caps/dependências.
[Plan completo](../memory-bank/bolts/020-overlay/implementation-plan.md), incluindo
assinaturas, validações, orçamento, I/O e L01–L12/M01–M18. A operação não existe
ainda na implementação; Q-C-09 resolve a decisão de âmbito, não comprova execução.

## IMP-005 — interfaces introduzidas, comportamento pendente

Plan aprovado explicitamente, incluindo Q-C-10/11/12. A assinatura requestShutdown
com ShutdownReport/LeaveAttempt/LeaveOutcome já foi acrescentada; a operação
lança UnsupportedOperationException até à autorização de implementar comportamento.
OttApplication e OverlayNode têm igualmente contratos/stubs sem serviços ativos.
Não confundir compilação das interfaces com API funcional ou validação V-008.
send() e todos os restantes comportamentos históricos de NeighborManager foram
preservados. O checkpoint interfaces/testes vermelhos precede a implementação.

## IMP-005 — comportamento de Implement preparado

Registo em 2026-11-10T18:16:24Z, sujeito ao checkpoint Implement. A autorização posterior às
interfaces permitiu implementar o Plan nos cinco ficheiros. As descrições anteriores
de stubs/contrato futuro são históricas; requestShutdown já implementa Q-C-09/10,
com relatório limitado a 63 sessões e escrita local best effort. requestClose
continua imediato e preempta drenagem; close depois de um aviso não renova o budget.
send() de aplicação é idêntico à base. Sem alteração de bytes, caps ou transportes.

OttApplication parse/run/main concretizam Q-C-11/12. O main possui canais dos
descritores da consola, fecha-os diretamente e verifica joins físicos; run com
streams emprestados não assume ownership desses streams. A vista ExecutionResult
é interna para observar joins nos testes, sem comando público adicional.
OverlayNode cria um owner, empresta-o ao manager e aguarda a sua terminação fora
do owner; close em callback só solicita paragem. failure() expõe falha local
observada como Optional. Bootstrap conserva o seu lifecycle existente,
sem membership ou exposição de UUID wire fictício.

Regressão de Implement: 979 testes, zero falhas/erros/ignorados, incluindo TCP e
subprocessos reais. Não é clean verify/Test aprovado nem manual humano.
[Walkthrough](../memory-bank/bolts/020-overlay/implementation-walkthrough.md) e
[oráculos](../memory-bank/bolts/020-overlay/implement-oracles.md). Sem fecho,
commits/push ou integração presumida.

## IMP-005 — coerência do resultado terminal

Correção registada em 2026-11-10T18:39:44Z, sujeita à aprovação de Implement. A mensagem
SHUTDOWN node=... phase=closing-output assinala apenas progresso de cleanup,
sem declarar STOPPED ou exit. O código final é o devolvido por ExecutionResult/run
e usado pelo main como status do processo, depois de conhecer o resultado do
writer. Não se exige um log final depois de fechar stdout/stderr. Falha ou
timeout do writer pode determinar 3, conservando o budget global restante.
Dois testes de regressão dirigidos cobrem falha e bloqueio precisamente nessa
fase, após serviço terminado com sucesso; regressão atual de 981 testes passou.
Test/clean verify/manual humano/revisão final Waheed/integração continuam pendentes.

## EXTRA-01 — Contrato OTD1 proposto

> Histórico: transporte suspenso pelo utilizador. Proposta atual em
> [contrato v2 no controlo](#extra-01-control-v2); não implementar OTD1 separado.

**Estado: Design/Q-C-17 aprovados; contrato em revisão ADR, G-EXTRA-ADR pendente.**
Aprovação de Design registada em 2026-11-11T13:38:55Z. ADR-033 continua Proposta;
[revisão ADR](../memory-bank/bolts/034-overlay-extra/adr-review.md) e aprovação
formal dos contratos afetados ainda necessárias antes de Implement.
[Design IMP-034](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md)
contém a sequência binária completa e os oráculos D01–D12.
[Q-C-17](construction-duvidas.md#q-c-17--design-da-descoberta-extra-proposta).

| Serviço | Endereço e mensagens |
|---|---|
| Controlo base | TCP/controlPort, 16 tipos v1 existentes, código 3 rejeitado, framing 37..65536. |
| Dados | UDP/dataPort, OTT1 inalterado (64 bytes de cabeçalho, total <=1200). |
| Descoberta extra | TCP/dataPort, listener separado, apenas OTD1 v1. Desativado por default. |

Uma ligação de descoberta realiza QUERY=1 → SNAPSHOT=2 → flush completo → EOF.
Não é uma nova ligação overlay, não tem ERROR e não transporta routing/vídeo.
Framing big-endian u32 bodyLength, corpo 76..32768, magic ASCII OTD1, version=1,
kind explícito, queryId UUID, helloRequestId UUID e identidades/instâncias dos
extremos. SNAPSHOT acrescenta generation u64 positiva, count u16 até 63 e
endpoints dos vizinhos diretos ACTIVE. Cada endpoint contém nodeId, UUID
observado, host lexical, controlPort, dataPort e role com código explícito.
Não enviar cache aprendida como adjacência. Não acrescentar campos ao HELLO,
REGISTER_OK ou datagrama UDP, nem escolher codec por bytes numa sessão base.

QUERY exige sessão ACTIVE atual; SNAPSHOT exige também consulta/canal correntes.
O token do HELLO é comum aos dois extremos; connectionId é comparado apenas
localmente. Sem sessão/correlação, ou com versão/tipo/comprimento inválidos,
fechar o canal extra sem resposta. O extra não autentica criptograficamente
anunciantes ou candidatos e não autoriza por IP remoto. Não há fallback ao
bootstrap nem à porta de controlo se o extra do vizinho estiver indisponível.
Se dataPort=controlPort no endpoint remoto, ou o TCP alvo coincide com algum
controlo/bootstrap conhecido, não abrir a consulta. O perfil base mantém-se
válido; só o extra desse vizinho fica indisponível.

Proposta fixa: TTL local 60 s desde a QUERY, cache 128 IDs/quatro fontes, geração
crescente por sessão e filtragem de self/IDs configurados/conflitos. Fontes
retiradas conservam claims até expirar; callbacks antigos não os renovam.
Conflitos de endpoint/papel e colisões entre IDs tornam candidatos indisponíveis.
Elegibilidade de refresh 5 s, teto global 4 queries/s, entrada 1/s por sessão,
30 s de pausa após falha; todos os detalhes e limites físicos estão no Design.

extra.discovery.enabled opt-in só nos três papéis overlay; conservar os dois
construtores anteriores de NodeConfiguration. Recursos TCP comuns com C<=64,
no máximo quatro canais/dials extra (2+2), fila de um frame mais o frame em escrita.
Estado no owner serial partilhado; resolver/bind fora dele, limitados e observados
até terminação física. Flush não é ACK e cancelamento não prova terminação.
Shutdown usa o prazo global restante, sem joins no owner nem callbacks sob locks.

OverlayNode terá consulta assíncrona limitada da cache; não é uma tabela de
rotas, um comando CLI novo ou prova de reparação. IMP-035 precisa de contrato
próprio para intenção/admissão de arestas aprendidas, com aprovação do gate.
V-033 cobre aprendizagem; V-034 cobre reparação. Nenhum deles foi executado.

### Alcance da revisão ADR do contrato OTD1

As consultas OTD1 podem ser iniciadas por qualquer extremo de uma sessão
base ACTIVE; não criam outra sessão vizinha de controlo nem mudam a regra
do menor nodeId dessa sessão. RESOURCE_LIMIT/EXTRA_UNAVAILABLE são razões
locais da descoberta, não novas mensagens/códigos v1 enviados pela extensão.
A configuração mantém a lista base das ADR-010/016, com a única chave extra
explicitamente definida na ADR-033. DNS/bind usam o serviço físico comum
existente (um worker, até 65 jobs totais; até dois da descoberta dentro desse
limite). As demais capacidades e escolhas aprovadas do Design mantêm-se.
[Conferência dos contratos e recursos](../memory-bank/bolts/034-overlay-extra/adr-review.md).

<a id="extra-01-control-v2"></a>
## EXTRA-01 — Contrato v2 único no controlo (proposto)

G-EXTRA-ADR satisfeito para IMP-034; [Design completo](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md)
e [revisão ADR-034](../memory-bank/bolts/034-overlay-extra/adr-034-review.md)
estão aprovados. OTD1 e a mistura TCP v1/v2 são históricos, arquivados.
Implement pronto para revisão, sem autorização inferida para Test/fecho.

Todo o TCP é v2: bootstrap REGISTER/REGISTER_OK/ERROR, handshake, mensagens
normais e descoberta. Um enum MessageType, 19 tipos explícitos (1,2,4..20),
código 3 rejeitado. Um ControlMessage, um MessageCodec, um parser de framing/
strings/UUIDs/endpoints/buffer; mesmas APIs encode/decode/read/write agora v2.
Não criar ControlFrame, CodecPolicy, FrameListener, decoder legado ou fallback.
DiscoveryMessage só representa payloads; eventual DiscoveryCodec é helper
interno de payload, sem framing/codec operacional autónomo.

Version!=2 é UNSUPPORTED_VERSION, sem interpretar o payload v1. Toda resposta
emitida, incluindo ERROR, é v2; responder apenas quando envelope/contexto são
seguros e correlacionados, nunca ERROR a ERROR. Bootstrap mantém grafo imutável,
instâncias zero em REGISTER_OK e resposta completa antes de EOF, sem operações
de descoberta extra. A migração não altera a política do bootstrap (ADR-013/015).

Framing u32/body 37..65536, UTF-8 estrito e hard caps preservados. HELLO contém
HELLO5/REPAIR_HELLO20: Endpoint + supportedMask/requiredMask u32, ambos3, bits reservados zero;
HELLO_OK selectedMask u32=3. Corpo HELLO 72..386 e ACK 41..72. Todos os nós
overlay exigem esta capacidade, versão/identidade/endpoint/grafo/instância/
requestId/iniciador/flush válidos antes de NeighborUp. Não há modo base.

QUERY/SNAPSHOT usam o envelope único v2 e payload helloRequestId UUID,
recipientId string e recipientInstance UUID. SNAPSHOT acrescenta generation
positiva 1..Long.MAX_VALUE, count 0..63 e endpoints diretos; sem hearsay/TTL
remoto. QUERY 72..134, SNAPSHOT até 19674, cap adicional extra <=32768.

## Rejeição local e recuperação de conteúdo no contrato v2

Inputs inválidos de versão/identidade/endpoint/capacidade rejeitam apenas a
tentativa/handle. Sem NeighborUp, FAILED global, node.failure(), FATAL, fim da
CLI ou fecho de sessões saudáveis. Estado por peer configurado limitado (<=63),
enum/motivos/escalares, sem payload/histórico ilimitado; uma tentativa e um retry
por peer, menor ID iniciador, backoff/jitter/deadlines e reservas físicas C<=64
existentes. Ausência temporária/EOF/timeout não prova incompatibilidade;
conservar último motivo confirmado separado do resultado atual. Apenas handshake
válido v2 restaura ACTIVE_DISCOVERY. D11a mantém A ACTIVE/CLI/descoberta úteis
enquanto B é rejeitado. READY local não prova todas as arestas confirmadas.

FIFO única Q+inFlight/writer comum; descoberta só entra atomicamente com duas
posições livres e nenhum extra enfileirado. Margem M=1, quota E(Q)=min(1,max(0,Q-1)),
no máximo um extra enfileirado e um em escrita por ligação e quatro tickets
globais até terminação física. A transição take→inFlight continua cobrada.
BACKPRESSURED não fecha controlo nem cria fila de retry. Mensagens normais v2
conservam a política de overflow. Margem não promete bursts/latência arbitrários.

readFrame, no mesmo parser operacional, devolve FrameRead.Decoded(ControlMessage)
ou RejectedExtraContent(ExtraEnvelope, ContentRejection). O resultado inválido
retém só envelope/correlação legíveis e enum de motivo, sem corpo/lista parcial.
Transport.Listener recebe ControlMessage; callback adicional publica rejeição
no owner, que compara runToken/connectionId/instanceId/queryId/token e identidade
antes de a classificar recuperável. Não há parser alternativo por versão.

Um anúncio completo/delimitado de sessão atual pode ser rejeitado atomicamente
por geração/IDs/role/endpoint/UTF-8 de candidato/count/bytes excedentes inválidos,
sem alterar cache/watermark/TTL. Mensagem normal v2 seguinte é entregue no mesmo
TCP. Comprimento inválido, truncamento, versão/tipo/código 3 ou identidade
ilegível/incompatível continuam terminais no handle afetado. Callback antigo não
fecha sessão nova. Nunca capturar EOF/framing como mero erro de conteúdo.

## Configuração, cache e shutdown preservados

Todo SERVER/RELAY/CLIENT, em cada loader/construtor/perfil, exige descoberta/Q>=2
antes de sockets/owner/workers. Ausência da antiga chave já exige, true é só
declaração redundante, false/vazio/inválido são erros agregados. BOOTSTRAP recusa
a chave como inaplicável e mantém Q=1 válido quando suportado, tal como transporte
genérico; ambos usam TCP v2, sem constituir modo base. Não criar API alternativa.

Cache 128 IDs x quatro fontes, TTL monotónico local 60 s desde QUERY admitida,
geração por sessão, proveniência e retenção após morte do anunciante preservados.
Owner serial partilhado, callbacks curtos, filas/timers físicos limitados e
shutdown global com prazo, sem liberar recursos pela mera conclusão de future.
UDP OTT1/version 1 de vídeo mantém bytes/caps e as 16 fixtures. IMP-034 aprende;
IMP-035 repara após falhas, com checkpoint próprio; routing/vídeo continuam depois.


<a id="extra-01-recovery-proposed"></a>
## EXTRA-01 B — reparação direta aprovada, Implement para revisão

ADR-035 Aceite por aprovação explícita registada em 2026-11-11T23:39:43Z;
G-EXTRA-ADR satisfeito para IMP-035. [Design](../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md),
[ADR](../add.md#adr-035), [âmbito21](../memory-bank/bolts/035-overlay-extra/scope-change-proposal.md)
e [walkthrough](../memory-bank/bolts/035-overlay-extra/implementation-walkthrough.md).

O isolado inicia REPAIR_HELLO20 com payload Hello existente e qualquer ordem
nodeId. Destino cooperativo responde HELLO_OK6 no mesmo TCP sem cache recíproca.
Origem verifica cache/proveniência/TTL; destino valida identidade/endpoints/caps/
colisões/limites sem comprovar aprendizagem remota. HELLO5 conserva grafo e menor
iniciador. Só saídas pendentes sobrepostas elegem menor originator; ACTIVE é
preservada. Losers continuam cobrados até terminação física; barreira UNCLASSIFIED
não infere direção de opened antes da associação do dial.

Codec/versão2 comum, 19 tipos1/2/4..20, código3 rejeitado, máscaras3 obrigatórias
em5/20/6 sem fallback. Framing37..65536; corpo20 de72..386 e6 de41..72.
Snapshot19, cache,TTL e UDP OTT1 não mudam. 78 fixtures operacionais62TCP+16UDP;
quatro masks migradas,46TCP anteriores preservadas,12 novas independentes.
Nenhum contrato de JAR antigo é suportado operacionalmente.

20/6/ERROR usam FIFO/sendAndFlush normal; quotas/tickets extra só18/19. C≤64,
grau≤min(C,63), dinâmica≤min(C,16), admissões20 pendentes≤2, saída autónoma≤1.
Seleção só com zeroACTIVE, tick comum100ms, orçamento10s limitado ao TTL original,
DNS/dial com prazos separados e backoff existente. Shutdown usa prazo global.

ERROR mantém requestId/relatedType5/20/6 e identidade da sessão. D07e preservado
em sessões configuradas/reparadas: token/query/geração antigos não terminam query
nem provocam Down ou alteram cache/TTL. Aresta nova só é anunciada após Up.
RepairStarted regista record imutável de proveniência por seleção admitida;
recoveryStatus() é vista limitada, CLI recovery imprime uma linha limitada.

A implementação e fixtures foram exercitadas incrementalmente; ainda faltam
Test com clean verify, manual humano com mortes reais, revisão final e integração.


## IMP-007 — composição e monitorização: decisões aprovadas, Design proposto

Q-C-23 opção 1 e Q-C-24 aprovadas pelo utilizador nesta retoma. O
[Design](../memory-bank/bolts/005-rotas/ddd-02-technical-design.md) apresenta
assinaturas, records, política de falhas e oráculos; ainda aguarda aprovação.
Não existem APIs novas implementadas nesta etapa.

Exceção mínima aos três ficheiros monitoring do ADD: OverlayNode.java e novo
OverlayNodeMonitoringTest.java, com os caminhos completos na allowlist do Design.
Composição com o mesmo SerialOwner, NeighborManager e Session; nenhum transporte,
executor de estado ou comando CLI adicional. O tap recebe Up/Down/Received, trata
PROBE_PING/PONG e entrega todos os eventos originais uma vez e por ordem ao
observador existente, incluindo ERROR, Diagnostic e RepairStarted.

RTT monotónico local desde criação/admissão até tratamento do PONG, filas incluídas.
Reset completo para qualquer nova Session/connectionId mesmo instanceId; Up torna
a primeira sonda elegível, apenas uma pendência por sessão, sem bursts. PONG
preserva requestId e probeId; nenhum relógio remoto entra no cálculo. Sem amostra
não há custo zero confiável; EWMA mantém precisão e custo usa ceil mínimo 1.

Proposta de APIs: LinkMetrics imutável; NetworkMonitor com portas de controlo,
relógio/scheduler injetáveis, snapshot defensivo e requestClose/awaitTermination
separando término lógico de físico; OverlayNode.monitoringSnapshot(). Pending
existe antes de send: um PONG antecipado guarda apenas o instante local e só
produz amostra após ACCEPTED. Recusa descarta-o; callbacks antigos nunca alteram
sessões novas. CLOSED/OVERFLOW não contam como perda remota; INVALID_MESSAGE ou
falha inesperada de admissão são falhas locais observáveis e encerram normalmente
o nó. Recursos esgotados antes da admissão adiam a sonda sem penalizar o vizinho.

Limites propostos: N=min(63,maxConnections), B=min(eventsCapacity,2*N);
N entradas/pendências, B tickets físicos de send e um ticket reservado de close,
N pedidos lógicos de close, um PONG antecipado por pendência, dois trabalhos
físicos de timer no máximo e um wakeup enfileirado mais um em execução. Recursos
retirados logicamente continuam contados até término físico. Paragem fecha
admissão antes do graceful leave e partilha o prazo global original do nó, sem
renovação nem espera bloqueante no owner. Detalhes e testes no Design.

TCP operacional v2/19 tipos ativos/código 3 rejeitado e caps existentes preservados
(ADR-033/034/035); nenhuma propriedade de configuração altera wire ou hard caps.
Decisões da retoma cancelada não são tomadas como aprovação desta retoma.


## IMP-007 — Design aprovado; checkpoint ADR

Registo 2026-11-12T14:48:27Z: utilizador aprovou Design e allowlist exata dos cinco ficheiros.
Confirmou revisão de Fillmore do Design atual; não constitui revisão final do
código ainda não implementado. Q-C-23/24 preservadas. Análise ADR preparada para
aprovação, sem divergência/inviabilidade concreta identificada documentalmente;
reutiliza decisões aceites, sem ADR nova ou alteração de estado das existentes.
current_stage=adr; só Model/Design concluídos. Implement/Test não iniciados.
V-010, regressão atual, manual humano antes de aprovar Test, revisão final do
código e integração pendentes; sem gates externos adicionais. Sem Java, staging,
commits, push ou fecho. Relatório: memory-bank/bolts/005-rotas/adr-review.md.


### IMP-007 — Q-C-25: adaptação autorizada do peer de teste

Registo 2026-11-12T15:48:58.371559+00:00. Utilizador autorizou exclusivamente q-c-25-proposed.patch e
repetição da regressão em Implement. Sexto caminho: OttApplicationTest.java,
só o teste m06AndM08ActiveErrorIsObservedAndReconnectNeedsNoBootstrap. Primeira
PROBE_PING elegível no Up e FIFO existentes preservados; peer controlado responde
com PONG correlacionado antes de exigir NODE_LEAVE, limitado a8frames/2500ms.
Continua a exigir identidade v2 da nova Session, payload0 e saída0. Não modifica
contrato operacional, wire/caps ou decisões Q-C-23/24. Não é nova ADR ou revisão
final do código. Aplicação/hashes em experiments/construction/005-rotas/q-c-25-application.json.

Complemento q-c-25-discovery-proposed.patch igualmente autorizado e aplicado:
Query18 exige helloRequestId/recipientId/recipientInstance da Session nova e recebe
Snapshot19 vazio, requestId correlacionado e geração local crescente. Mantém
8frames/2500ms e as restantes exigências, sem aceitar mensagens arbitrárias.
Regressão04:1176/0falhas/0erros/0ignorados,exit0. Q-C-25 resolvida neste âmbito;
Implement/Test ainda por aprovar.


## IMP-008 — Design aprovado e checkpoint ADR

Registo 2026-11-13T16:41:04Z. Q-C-26 aprovada integralmente: capacidades e retenção no
[ADD6.1](../add.md#61-entidades-e-invariantes) e
[Design](../memory-bank/bolts/021-rotas/ddd-02-technical-design.md).
[Revisão ADR](../memory-bank/bolts/021-rotas/adr-review.md) preparada, para aprovação.
LinkMetrics.costMicros() fornece o custo opcional em µs já arredondado pelo monitor;
projetar nodeId/instanceId/connectionId para SessionKey e consumir o valor diretamente.
SUSPECT com amostra mantém o custo; ausência não é zero. O domínio não recalcula RTT.
Tabela cresce por admissão, sem máximos pré-alocados, cacheCapacity reutilizada ou
evição de histórico protegido. Snapshot de seleção não é forwarding confirmado.
A ponte de eventos de métricas para routing ainda não existe em OverlayNode;
composição futura requer checkpoint/âmbito próprio. Só Model/Design concluídos;
Implement/Test, V-011, regressão atual, manual offline, revisão final e integração
pendentes. Nenhuma alteração Java, teste executado ou ADR nova neste checkpoint.


## IMP-008 — domínio implementado para revisão, sem composição de rede

Registo 2026-11-13T17:18:48Z. [Walkthrough](../memory-bank/bolts/021-rotas/implementation-walkthrough.md):
RouteMetric/RouteCandidate/RouteState/Route/RoutingTable disponíveis nos seis
caminhos do IMP-008, conforme Design e Q-C-26 aprovados. Snapshot só de seleção,
sem parent confirmado ou destinos. Todos os métodos da tabela exigem o owner
serial do chamador; nenhuma thread, socket ou timer criado pelo domínio.
35 testes novos e regressão incremental1211/0/0/0 com JDK21; execução restrita
anterior preservada. Test/clean verify/manual humano/revisão final/integração
pendentes. RoutingService/OverlayNode e bytes operacionais preservados.


## IMP-008 — Test técnico, manual humano pendente

Registo 2026-11-13T17:43:51Z. [Relatório](../memory-bank/bolts/021-rotas/ddd-03-test-report.md):
clean verify JDK21:1211/0/0/0; novo JAR e35 testes do domínio confirmados, sem
alteração de fonte nesta etapa. Manual Codex43checks passou contra esse JAR;
[roteiro offline humano](../experiments/construction/021-rotas/manual-instructions.md)
preparado, execução humana não recebida. D22 parcial/Test por aprovar, revisão
final e integração pendentes. Sem composição de rede ou autorização de dados nova.
