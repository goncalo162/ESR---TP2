# Arquitetura

## Estrutura geral

Um único projeto Java, executado em processos separados. SERVER, RELAY e CLIENT partilham o núcleo OverlayNode. BOOTSTRAP é um serviço de descoberta autónomo, que pode correr na mesma máquina de um servidor sem ser um nó de encaminhamento adicional. Não existe coordenador central das rotas nem base de dados.

~~~mermaid
flowchart LR
  B[Bootstrap e topologia permitida]
  S1[Servidor S1: A e B]
  S2[Servidor S2: C e D]
  R1[Relay R1]
  R2[Relay R2]
  R3[Relay R3]
  C1[Cliente C1]
  C2[Cliente C2]
  S1 -. registo TCP .-> B
  R1 -. registo TCP .-> B
  S2 -. registo TCP .-> B
  C1 -. registo TCP .-> B
  S1 <--> R1
  S1 <--> R2
  S2 <--> R2
  S2 <--> R3
  R1 <--> R3
  R2 <--> R3
  R3 <--> C1
  R3 <--> C2
~~~

As linhas sólidas representam vizinhanças lógicas com TCP de controlo e endpoint UDP associado; o tráfego de vídeo usa somente a árvore ativa de cada stream. O desenho exemplifica caminhos alternativos e partilha, não substitui o mapeamento IP de cada experiência. Todos os nós usam bootstrap; só alguns registos são desenhados para legibilidade.

## Componentes e responsabilidades

| Componente | Responsabilidade e limites |
|---|---|
| app | CLI, composição de serviços, início ordenado, comandos locais e encerramento |
| config | Ler propriedades, validar IDs, endpoints, limites e relações; nenhum socket antes da validação |
| bootstrap | Ler grafo permitido, devolver vizinhos e manter registo com lease; não anuncia vídeos ou calcula percursos |
| transport | Conexões TCP, framing, filas e eventos; UDP de dados separado; não decide destinos |
| overlay | Handshake, unicidade da ligação, reconexão e publicação de eventos de vizinhança |
| monitoring | Sondas TCP correlacionadas, RTT EWMA e deteção de falhas; sem inferir bandwidth a partir de RTT |
| routing | Candidatos com caminho explícito, seleção de parent, ativação/leases/poda e mudança dinâmica |
| streaming | Metadados, codificação dos datagramas, snapshots de encaminhamento e contadores |
| server | Validar ficheiros MJPEG, ler frames, controlar ritmo e publicar apenas quando há procura |
| client | Escolha de stream, reassemblagem, descarte de frames e reprodução Swing |

Interfaces internas e mensagens em [07](07-API-AND-INTERFACES.md); entidades e estados em [06](06-DATA-MODEL.md). Não criar camadas web, repositórios de base de dados ou microserviços.

## Construção da overlay

O grafo permitido é estático, simétrico e configurado pelo operador; disponibilidade e conexões são dinâmicas. Cada nó conhece apenas o bootstrap e a sua própria identidade no arranque. O bootstrap devolve todos os vizinhos permitidos, incluindo os ainda offline, para permitir início em qualquer ordem. X corresponde ao grau pretendido desse nó; os clientes folha podem ter X=1. O registo de nós vivos serve diagnóstico, não impede tentar ligar a um vizinho que ainda não se registou.

Entre cada par só o nodeId lexicograficamente menor inicia o canal; ambos aceitam HELLO e rejeitam duplicados. Uma nova instanceId invalida eventos antigos da mesma identidade. O iniciador repete tentativas com backoff limitado e jitter. As tentativas não alteram o grafo permitido. Após falha, reconecta ao mesmo vizinho e permite escolher outra rota já existente.

## Descoberta de fluxos e prevenção de ciclos

Cada origem anuncia periodicamente cada StreamKey, com serverEpoch, sequência de anúncio, caminho iniciado na origem e custo acumulado. Ao receber de P, o nó N verifica: caminho termina em P, começa no serverId anunciado, não contém N nem IDs repetidos e respeita o máximo de saltos. O candidato de N é esse caminho acrescido de N, com custo recebido + RTT EWMA(N,P), sempre positivo.

Guardar um candidato por vizinho/fluxo. A seleção usa custo, depois saltos, depois nodeId. A cache de flooding identifica (stream, epoch, announceSeq) e permite reenviar a primeira chegada e melhorias estritas nessa geração, com intervalo mínimo; rejeitar apenas por já ter visto a sequência esconderia um caminho melhor. Validar loops pelo vetor de caminho e limitar path a 32 IDs, cache e taxa. O custo local alterado só é propagado numa nova geração ou melhoria permitida, evitando reenviar a mesma geração indefinidamente.

Os anúncios expiram. Quando o melhor caminho desaparece, selecionar candidato válido; sem candidato, marcar UNAVAILABLE e anunciar retirada. É proibido reutilizar alternativas cujo vetor inclua o próprio nó. Novos candidatos ficam disponíveis após novas ligações. Isto evita depender de Dijkstra global ou de um controlador sempre ativo.

## Ativação e poda

Cada nó agrega a procura local e dos seus downstream. No primeiro pedido cria uma subscrição a montante. ACTIVATE percorre o vetor inverso; cada nó verifica que o prefixo até si coincide com o seu próprio caminho selecionado para essa época. Se o percurso mudou, responde PATH_CHANGED, permitindo nova tentativa com anúncio atualizado. O pedido fica provisório até ACK da origem ou de um ramo que já tem ativação confirmada e prefixo compatível.

A fonte ativa uma saída por vizinho e não uma por cliente final. Pedidos repetidos com a mesma identidade/revisão são idempotentes. Cada nó tem no máximo um upstream confirmado por fluxo; consumidores locais e downstream são conjuntos, nunca contadores incrementados cegamente.

A última remoção de procura gera DEACTIVATE. Queda abrupta de cliente/relay é resolvida por fechamento da ligação e por lease de subscrição, independentemente de mensagens de saída. Um cancelamento antigo só remove a mesma subscriptionId/revision, sem atingir subscrição nova. Só a receção de uma renovação válida prolonga o lease a jusante; um nó continua a renovar para montante enquanto tiver procura efetiva.

## Falhas e alterações de custo

Falha de transporte ou três sondas sem resposta invalida o vizinho e os candidatos associados. Selecionar alternativa, ativar novo upstream e confirmar ACK; sem caminho, manter intenção do cliente e estado UNAVAILABLE para retomar quando surgir um anúncio válido. Não existe failover de conteúdo para outra fonte: cada stream pertence a uma origem neste plano.

Uma melhoria sustentada de custo exige redução relativa de pelo menos 20%, três observações e permanência mínima de cinco segundos. Estes valores são hipóteses de tuning. A falha física ignora histerese. Numa troca planeada, manter upstream antigo até confirmação do novo, trocar snapshot atomicamente e podar o antigo. Só o upstream confirmado alimenta dados; deduplicação por época/frame/fragmento protege a transição. Pode existir tráfego temporário nos dois ramos, limitado pelo timeout da ativação e poda; a propriedade de cópia única é exigida em estado estável.

## Caminho de dados

VideoSource → pacing na origem → fragmentação → StreamForwarder → UDP de cada downstream confirmado → StreamForwarder seguinte → reassemblagem local → ImageIO → VideoPlayer. O relay não descodifica JPEG nem reagrupa frames. Cada datagrama é reenviado segundo snapshot imutável da tabela, sem consulta ao bootstrap. Uma stream inativa não envia vídeo; mensagens de controlo e sondas continuam.

## Concorrência e recursos

- Um executor serial de eventos por nó é proprietário de vizinhos, rotas e subscrições; handlers publicam eventos sem editar esse estado diretamente.
- Um leitor e uma fila/escritor por ligação TCP; a thread de leitura nunca espera por ACK que ela própria terá de receber.
- UDP ingress e egress usam filas limitadas e snapshots de rotas; saturação descarta dados e incrementa contador, sem bloquear indefinidamente o controlo.
- Fonte usa scheduler com relógio monotónico, sem catch-up ilimitado após atraso. O cliente tem máximo de frames em montagem e deadline por frame.
- Swing só recebe imagens prontas no EDT. A janela não executa rede, leitura de vídeo ou descodificação.
- Shutdown cancela tarefas, fecha sockets, limpa subscrições e termina executores com prazo, mesmo sem acesso ao bootstrap.

## Erros, logging e testabilidade

Falha de configuração é fatal antes de abrir sockets. Falha de um vizinho é recuperável e localizada; mensagens malformadas fecham a conexão ofensora sem matar o nó. Logs incluem timestamp, nodeId, instanceId, tipo de evento, StreamKey e requestId quando aplicável. Registar alterações de rota e razões, nunca bytes completos de vídeo. Injetar relógio, transporte e coletor de eventos para testar timeouts, leases, histerese e concorrência sem sleeps arbitrários. Mais detalhes em [09](09-TESTING-STRATEGY.md) e [10](10-SECURITY.md).
