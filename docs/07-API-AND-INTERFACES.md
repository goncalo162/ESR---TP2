# Interfaces e contratos propostos

A API de configuração foi implementada na TASK-003 e o codec do formato de controlo v1 na TASK-004. O transporte TCP foi implementado na TASK-005; handlers e restantes interfaces de rede continuam por implementar. O enunciado obriga ao comportamento, mas não especifica nomes de mensagens, formato binário, portas, REST ou bibliotecas. Não há API HTTP nem serviço externo inventado.

Correção explícita do utilizador / ADR-013: bootstrap é exclusivamente descoberta inicial de vizinhos configurados, sem registo vivo, leases, renovações, monitorização ou avisos de saída. O contrato v1 foi corrigido antes da implementação de qualquer transporte: REGISTER_OK contém apenas a lista de vizinhos, e o código 3 está retirado/reservado. Os bytes antigos de REGISTER_OK não são compatíveis nem aceites; não existe fallback para a renovação antiga.

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

Valores abaixo são **hipóteses configuráveis**, sujeitos a validação experimental em TASK-028; nunca requisitos numéricos do enunciado.

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

Envelope, por ordem: version u8 (=1), type u8, requestId UUID (16 bytes), senderId string, senderInstance UUID (16 bytes). Comprimento mínimo com ID de um byte: 1+1+16+2+1+16=37 bytes. Não tratar IDs de um byte como malformados.

Primitivas: u8/u16/u32 sem sinal na rede e lidos em tipos Java suficientes; i64 não negativo para sequências/custos/tempos de media; UUID em dois longs big-endian, bits preservados; bool u8 0/1. String = u16 de comprimento de bytes + UTF-8 estrito sem NUL; nodeId até 32 bytes ASCII, title até 512 bytes e 128 pontos de código Unicode, mensagens de erro até 256 bytes. Lista = u16 count + elementos; node list até 64, path até 32. Não usar writeUTF/readUTF como se fossem UTF-8 comum. Bytes extra no fim do payload são erro de versão/formato. Todas as listas abaixo usam essa contagem.

StreamInfo na rede: streamId UUID, serverId string, title string, codec u8 (1=MJPEG), fps u16 (1–60), serverEpoch UUID. Endpoint: nodeId string, instanceId UUID (obrigatoriamente zero em REGISTER_OK, não zero em REGISTER/HELLO), host string ASCII de 1–253 bytes, controlPort u16 e dataPort u16 (ambos 1–65535), role u8 (1 SERVER, 2 RELAY, 3 CLIENT). O bootstrap fornece endpoints configurados, nunca a instância viva de um vizinho; esta é conhecida no HELLO direto. senderId, serverId e nodeId obedecem a [A-Za-z0-9_-]{1,32}; título tem 1–128 pontos de código Unicode e até 512 bytes UTF-8. A identidade no envelope é sempre a do emissor imediato, inclusive quando reencaminha anúncio de outra origem. Um elemento não válido rejeita a mensagem inteira.

### API do codec implementada na TASK-004

MessageCodec.encode(ControlMessage) devolve os bytes de exatamente um frame; decode(byte[]) exige exatamente um frame e rejeita bytes adicionais. write(message, OutputStream) valida/serializa antes de escrever no destino; read(InputStream) lê um frame completo e devolve Optional.empty apenas perante EOF limpo antes do prefixo. EOF a meio do prefixo/corpo é EOFException; campos incompletos num corpo já recebido são ProtocolException INVALID_MESSAGE. Comprimento superior ao máximo, incluindo u32 com bit de sinal, é rejeitado antes da alocação do corpo. Strings e listas também verificam máximos antes de alocar.

ControlMessage contém envelope, payloads tipados/imutáveis e Endpoint/StreamInfo do contrato. NeighborList(List<Endpoint>) é o payload de REGISTER_OK e não tem leaseMs. MessageType tem 16 tipos ativos e não inclui renovação de registo; o código 3 é rejeitado como INVALID_MESSAGE. MessageType e ErrorCode usam números explícitos; ProtocolException expõe errorCode sem enviar resposta nem fechar a conexão. Versão desconhecida é UNSUPPORTED_VERSION, formato/domínio incorreto é INVALID_MESSAGE e limites de tamanho excedidos são RESOURCE_LIMIT. Payload incompatível com o tipo também é rejeitado.

O chamador gere flush/close e o único escritor por conexão. O codec não abre sockets. UUID são opacos e preservam todos os bits; a exceção de Endpoint.instanceId desconhecido aplica-se apenas a REGISTER_OK. u32 aceita 0..4294967295 e é exposto como long. Listas vazias, TTL zero ou percursos repetidos podem ter representação sintática; os handlers têm de verificar a validade operacional, origem/prefixo, ciclos, frescura e leases antes de agir. Host no Endpoint tem validação de domínio ASCII/comprimento; o handshake terá de o comparar com a configuração autorizada.

[Implementação e validação original do codec](../experiments/task-004-codec-validation.md); [correção e revalidação do bootstrap](../experiments/bootstrap-discovery-correction.md). As 29 fixtures TCP incluem agora 17 casos aceites e 12 rejeitados, incluindo o código 3 retirado. As 16 fixtures UDP permanecem dados de referência; o codec de dados é da TASK-019.

### API de transporte implementada na TASK-005

`new Transport(bind, listener[, options])` abre o listener TCP; porta 0 permite testes efémeros. `localAddress()` devolve o endpoint real. `connect(remote)` devolve `CompletableFuture<Connection>` e faz o dial fora do chamador, com timeout default de 2 s. Endpoints devem estar resolvidos: resolver DNS fora do executor serial do nó. Limite/falha/shutdown completa o futuro excecionalmente; cancelamento fecha o socket da tentativa, inclusive numa corrida com estabelecimento. Não há retries automáticos.

`Connection.id()` devolve UUID único por ligação, ainda sem associação a nodeId. `send(message)` valida/serializa e devolve `ACCEPTED`, `CLOSED`, `OVERFLOW` ou `INVALID_MESSAGE`; a thread chamadora nunca escreve na rede. ACCEPTED significa enfileiramento, não entrega/ACK. O escritor único preserva frames e ordem da fila, inclusive com envios concorrentes. Fila default de 256 frames; overflow limpa a fila e fecha apenas a ligação saturada com OUTPUT_OVERFLOW. Uma mensagem inválida de saída não fecha uma ligação saudável.

`Listener.connectionOpen`, `message` e `connectionClosed` executam sequencialmente no leitor da mesma ligação; fecho tem razão explícita e evento único. Ligações diferentes produzem callbacks em paralelo. Callbacks e continuations síncronas do futuro de dial devem terminar rapidamente, publicando no futuro executor do nó sem esperar por ACK ou por filas cheias. RuntimeException em open/message fecha a ligação com CALLBACK_ERROR; exceção no callback de fecho é isolada. EOF limpo, truncatura, protocolo e I/O têm razões distintas. Não se envia ERROR automaticamente sem contexto seguro.

`Options` limita queueCapacity a 1–256 e maxConnections a 1–64 (defaults 256/64). Reservas incluem dials e permanecem até ambos os workers I/O terminarem. Um worker accept, um por dial pendente e dois por ligação; threads virtuais Java 21 sem fila de executor ilimitada. `connectionCount()` conta reservas pendentes/em terminação; `queuedMessages()` devolve profundidade da fila. Cada frame tem até 65540 bytes com prefixo; existe ainda um frame em escrita e serializações concorrentes dos chamadores. São limites de frames, sem medição de consumo máximo de memória.

`close()` fecha listener/sockets, limpa filas e interrompe workers. Chamador externo aguarda um deadline global default de 3 s; timeout lança erro explícito se um callback bloquear. Chamado por callback, só inicia shutdown para evitar joins circulares. `awaitTermination(duration)` externo confirma terminação. Close é idempotente, sem acesso a bootstrap. `Connection.close()` solicita fecho local e notificação assíncrona. Atenção: ACCEPTED seguido imediatamente de close não garante envio; o futuro bootstrap da TASK-006 terá de confirmar a escrita completa da resposta antes de fechar, mediante mecanismo apropriado nessa tarefa.

A assinatura conceptual `sendControl(neighborId, message)` depende do mapa futuro de vizinhos; nesta tarefa o envio usa Connection diretamente. Sem handshake, autorização, retries, CLI ou processos JVM independentes demonstrados. Os testes usam TCP real dentro da JVM de testes. Contrato bootstrap/NeighborList corrigido por ADR-013 preservado. [V-005](../experiments/task-005-transport-validation.md).

## 4. Mensagens de controlo v1

Os números são fixados por este contrato. Campos do envelope não são repetidos nos payloads. Respostas preservam requestId do pedido; mensagens periódicas usam um UUID novo.

| type | Nome | Payload por ordem / efeito |
|---:|---|---|
| 1 | REGISTER | Endpoint local; pedido de consulta, valida identidade/endpoint/papel contra configuração sem guardar registo vivo |
| 2 | REGISTER_OK | apenas lista Endpoint dos vizinhos configurados, todos com instanceId zero; sem lease ou disponibilidade |
| 3 | Retirado/reservado | rejeitar como INVALID_MESSAGE; não implementar renovação de registo |
| 4 | NODE_LEAVE | reason u8 (0 normal, 1 erro); notifica exclusivamente vizinhos diretos, nunca bootstrap |
| 5 | HELLO | Endpoint local; permite validar conexão, identidade e UDP publicitado |
| 6 | HELLO_OK | sem campos; confirma handshake |
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

Bootstrap usa uma conexão TCP curta por consulta inicial: REGISTER → REGISTER_OK ou ERROR, seguido de fecho após envio completo. O requerente usa os endpoints configurados devolvidos para falar diretamente com os vizinhos. Falhas antes de obter resposta permitem retry limitado por tentativa; depois do sucesso não existem renovação, consultas periódicas, heartbeats ou NODE_LEAVE para bootstrap. Consultas repetidas não alteram a configuração; IDs duplicados/instâncias e saídas são geridos entre vizinhos. O handler de bootstrap só aceita REGISTER e responde da configuração, mesmo que os vizinhos estejam offline. O codec comum pode representar outras mensagens; isso não autoriza o handler de bootstrap a processá-las como operações suas.

O transporte de vizinhos mantém conexão full-duplex persistente. Handshake precede mensagens 7–16, com 3 retirado. TYPE desconhecido ou versão diferente fecha ligação após ERROR se for seguro responder. TYPE do envelope só admite 1, 2 e 4–17; relatedType no payload ERROR é o octeto bruto do tipo que falhou, podendo conter um valor desconhecido. reason em NODE_LEAVE só admite 0/1 e code em ERROR só admite 1–12. Um decoder rejeita formato, limites e valores enumerados inválidos; autorização do vizinho, correspondência HELLO/envelope, frescura, caminhos e subscrições são verificações de contexto nos componentes responsáveis. Uma fixture rejeitada não obriga a enviar ERROR quando o envelope não é seguro para resposta.

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

fragmentCount = ceil(frameLength/1136); cada fragmento exceto o último tem 1136 bytes, último tem o resto não nulo. O datagrama é no máximo 1200 bytes de aplicação. Não contar IP/UDP nesses 1200; verificar MTU efetivo da bancada e reduzir o limite se necessário. O relay reenvia os mesmos bytes sem alterar sequência, stream ou origem temporal.

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
