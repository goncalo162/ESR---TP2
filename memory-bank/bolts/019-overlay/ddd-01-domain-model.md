# IMP-002 — Model: parametrização operacional

Bolt 019-overlay; unidade 002-overlay; origem TASK-032. Autorizado pelo utilizador
iniciar o próximo bolt e executar somente a primeira etapa. Model aprovado
explicitamente pelo utilizador, com registo em 2026-11-03T22:16:54Z. A continuação
posterior está no [Design aprovado](ddd-02-technical-design.md) e na
[revisão ADR aprovada](adr-review.md). Implement autorizado, com
[walkthrough aprovado](implementation-walkthrough.md), [Test aprovado](ddd-03-test-report.md)
e [fecho preparado](closure-walkthrough.md).
As descrições de pendências abaixo retratam o checkpoint Model.

Fontes: [ADD 6.1–6.3](../../../add.md#6-modelo-de-dados-e-contratos),
[IMP-002](../../../add.md#imp-002), [REQ-024](../../../specs.md#req-024),
[TASK-032](../../../docs/tasks/PHASE-02/TASK-032.md) e
[história](../../intents/001-ott-streaming/units/002-overlay/stories/005-implementar-parametrizacao-operacional.md).
ADR aplicáveis: 007, 010, 016, 024 e 026. A revisão formal das ADR é a etapa 3;
este modelo reutiliza as decisões aceites e não propõe nova ADR.

## Base real e fronteira do incremento

IMP-001/001-base-validada está complete e integrado em main/Denji, HEAD de partida
6bef7425a1f9237f9f744f26f3aa7e6067778025. A árvore inicial está limpa.
NodeConfiguration é um record com 13 componentes; ainda não tem runtime().
ConfigurationLoader.load/loadDeployment validam propriedades, papéis e deployment
offline, agregam ConfigurationProblem e devolvem dados imutáveis ou lançam
ConfigurationException. RuntimeConfiguration ainda não existe.

O ponto de extensão autorizado é RuntimeConfiguration, NodeConfiguration,
ConfigurationLoader e ConfigurationLoaderTest. Não se substitui o loader nem se
reabre TASK-003/005. A futura extensão deve preservar nomes/APIs existentes;
o Design deve explicitar como manter a construção anterior de NodeConfiguration
ao acrescentar o componente runtime. Nenhuma assinatura Java é alterada em Model.

Os testes existentes de chaves inválidas usam control.prt e streams.file num
CLIENT; continuam inválidos. Aceitar as 30 chaves novas não exige enfraquecer esses
casos. O único new NodeConfiguration encontrado está no loader. Transport.Options
já recebe capacidades/timeouts, com defaults 256/64/2000 ms/3000 ms; não é preciso
abrir um Transport para provar passagem de valores a um consumidor em V-032.

## Entidades, identidade e propriedade

| Elemento | Natureza e identidade | Invariantes |
|---|---|---|
| NodeConfiguration | Configuração do nó identificado pelo nodeId existente; sourceFile é proveniência | Identidade, papel, endpoints e paths obrigatórios mantêm a validação atual |
| RuntimeConfiguration | Objeto de valor composto, pertencente à configuração do nó; sem UUID/identidade própria | Imutável, grupos não nulos, valores tipados e já validados |
| Control | Conexão, shutdown, limites TCP e consulta bootstrap | Aplicável aos quatro papéis |
| Overlay | HELLO, retry/jitter e capacidade de eventos | Overrides só SERVER/RELAY/CLIENT |
| Monitoring | Intervalo/timeout/falhas de sondas e EWMA | Overrides só SERVER/RELAY/CLIENT; alpha finito |
| Routing | Anúncios, candidatos, cache, flooding, ativação e troca | Overrides só SERVER/RELAY/CLIENT; relações e tetos explícitos |
| Subscription | Lease máximo e período máximo de renovação | Overrides só SERVER/RELAY/CLIENT; renew estritamente menor que lease |
| Video | Egress e reassemblagem | Overrides só SERVER/RELAY/CLIENT, conforme o ADD, mesmo que um consumidor futuro não use todos os campos |
| Source | Limite local do frame de ficheiro | Override só SERVER; não muda o máximo do protocolo UDP |
| ConfigurationProblem | Diagnóstico existente, com file/key/message | Erros atribuíveis à fonte/chave; coleção de problemas copiada defensivamente |

Os sete grupos são valores de configuração, não serviços, atores ou máquinas de
estado de rede. É permitido manter defaults dos grupos não aplicáveis no record;
isso não autoriza um override nem faz o BOOTSTRAP consumir opções de overlay.
Não há mutação global, geração de instanceId, DNS, sockets, criação de diretórios
ou leitura de media nesta carga. O resultado pode ser partilhado como valor
imutável. O estado de rede dos consumidores continua sujeito à ADR-017, sem
introduzir executor serial, filas ou timers reais na configuração.

## Catálogo completo dos valores

Correspondência de modelo com o ADD 6.3. "Overlay" no papel significa
SERVER/RELAY/CLIENT; "todos" inclui BOOTSTRAP. Durações da tabela são milissegundos.
Inteiros positivos sem outro teto usam 1..2147483647. Os tipos Java concretos e
conversões verificadas serão fixados em Design; não são 30 strings livres.

| Grupo | Chave | Default | Papel | Domínio |
|---|---|---:|---|---|
| Control | control.connect.timeout.ms | 2000 | todos | 1..2147483647 ms |
| Control | control.shutdown.timeout.ms | 3000 | todos | 1..2147483647 ms |
| Control | control.max.connections | 64 | todos | 1..64 |
| Control | control.queue.capacity | 256 | todos | 1..256 |
| Control | bootstrap.query.timeout.ms | 3000 | todos | 1..2147483647 ms |
| Overlay | overlay.hello.timeout.ms | 3000 | overlay | 1..2147483647 ms |
| Overlay | overlay.retry.initial.ms | 500 | overlay | 1..2147483647 ms |
| Overlay | overlay.retry.max.ms | 5000 | overlay | 1..2147483647 ms |
| Overlay | overlay.retry.jitter.percent | 10 | overlay | inteiro 0..100 |
| Overlay | node.events.capacity | 4096 | overlay | 1..4096 |
| Monitoring | monitor.probe.interval.ms | 1000 | overlay | 1..2147483647 ms |
| Monitoring | monitor.probe.timeout.ms | 1000 | overlay | 1..2147483647 ms |
| Monitoring | monitor.probe.failures | 3 | overlay | 1..2147483647 |
| Monitoring | monitor.ewma.alpha | 0.2 | overlay | finito, 0 < alpha ≤ 1 |
| Routing | routing.announce.interval.ms | 2000 | overlay | 1..2147483647 ms |
| Routing | routing.candidate.ttl.ms | 8000 | overlay | 1..2147483647 ms |
| Routing | routing.cache.capacity | 4096 | overlay | 1..4096 |
| Routing | routing.cache.ttl.ms | 16000 | overlay | 1..2147483647 ms |
| Routing | routing.flood.max.per.sec | 10 | overlay | 1..10 |
| Routing | routing.activate.timeout.ms | 3000 | overlay | 1..2147483647 ms |
| Routing | routing.activate.attempts | 3 | overlay | 1..2147483647 |
| Routing | routing.switch.improvement.percent | 20 | overlay | inteiro 0..100 |
| Routing | routing.switch.observations | 3 | overlay | 1..2147483647 |
| Routing | routing.switch.min.dwell.ms | 5000 | overlay | 1..2147483647 ms |
| Subscription | subscription.lease.ms | 6000 | overlay | 1..2147483647 ms |
| Subscription | subscription.renew.ms | 2000 | overlay | 1..2147483647 ms |
| Video | video.egress.capacity | 256 | overlay | 1..256 |
| Video | video.reassembly.max.frames | 8 | overlay | 1..8 |
| Video | video.reassembly.deadline.ms | 250 | overlay | 1..2147483647 ms |
| Source | source.max.frame.bytes | 99999 | SERVER | 1..99999 |

São 5+5+4+10+2+3+1=30 chaves. A consulta bootstrap fica em Control, pois não
existe grupo Bootstrap entre os sete definidos pelo ADD. A capacidade de eventos
fica em Overlay; não se cria um oitavo grupo Node por causa do prefixo da chave.
Esta é uma organização do modelo, sem mudar nomes de propriedades/contratos.

## Invariantes de validação e compatibilidade

1. Só a ausência de uma chave operacional permite o default. Valor presente
   inválido/vazio não é ausência. Identidades, hosts/portas e paths existentes
   continuam obrigatórios; os defaults não os substituem.
2. Chaves desconhecidas, duplicadas após descodificação Properties e inaplicáveis
   ao papel são rejeitadas. BOOTSTRAP só admite as cinco chaves operacionais de
   Control; source.max.frame.bytes só pode ser fornecida por SERVER.
3. Números usam representação decimal sem unidades/separadores de milhares;
   alpha usa ponto. Rejeitar NaN/infinito, overflow, tipos incompatíveis e valores
   fora do domínio. Conversões de duração são verificadas antes do consumo.
4. overlay.retry.initial.ms ≤ overlay.retry.max.ms.
5. routing.candidate.ttl.ms > routing.announce.interval.ms.
6. routing.cache.ttl.ms ≥ routing.candidate.ttl.ms.
7. subscription.renew.ms < subscription.lease.ms.
8. Validar o conjunto efetivo de defaults+overrides: um override individualmente
   válido pode violar uma relação com o default de outra chave. Não acrescentar
   relações que o ADD não exige, como probe.timeout < probe.interval.
9. O loader agrega erros pelo mecanismo atual e não publica NodeConfiguration
   nem deployment parcial. Dados temporários de validação não são estado ativo.
10. Filas/caches/montagens configuráveis só podem descer dos tetos da tabela;
    não se alteram 64 nós/deployment, 256 streams/origem, 32 IDs/path, TCP
    37..65536, UDP 1200/64/1136, 2 MiB/frame de protocolo ou dedup de 512 frames.

O construtor/validador dos valores não pode permitir que um consumidor receba um
RuntimeConfiguration inválido por uma via alternativa de criação. O Design
definirá a divisão de validações entre construção e loader, preservando o erro
agregado na API de carga. Não se decide aqui um novo tipo público de erro.

## Estados e resultados da carga

Fluxo conceptual: entrada → leitura estrita → defaults/tipos/papel → relações e
deployment → resultado imutável **ou** ConfigurationException agregada. Estes
nomes são passos de análise, não novos enums/eventos Java.

Uma carga válida termina com um valor local; uma carga inválida termina sem
publicação parcial. loadDeployment não devolve subconjunto se algum perfil falhar.
Não há hot reload, negociação de parâmetros entre nós ou protocolo de atualização.
Sucesso/erro são o retorno/exception existentes; não se cria event bus.

Os consumidores futuros recebem os valores na composição: Control alimenta
Transport.Options e o prazo de consulta; os outros grupos alimentam os serviços
nos IMP que os implementam. O modelo preserva as políticas já aprovadas: retry
com jitter limitado a 1 ms..retry.max, uma sonda pendente por vizinho, renovação
com lease concedido e melhoria estrita mesmo com 0%. TASK-032 fornece os valores;
não implementa esses algoritmos, watchdog UDP ou recuperação extraordinária.

## Rastreabilidade para a prova futura V-032

| Obrigação | Casos a preparar em Design/Implement, ainda não executados |
|---|---|
| Defaults/compatibilidade | Cinco perfis existentes; sete grupos/defaults corretos sem chaves novas; identidade/endpoints/paths sem defaults adicionais |
| Overrides e consumidor | Valores válidos por papel e fronteiras; um consumidor offline observa exatamente os valores fornecidos |
| Domínios e erros agregados | Vazio, zero/negativos/excesso, NaN/infinito, overflow, unknown/duplicada/inaplicável e várias falhas numa carga |
| Relações | Igualdade permitida em retry/cache e proibida em candidate/announce e renew/lease; combinações de overrides com defaults |
| Publicação/imutabilidade | Nenhum resultado parcial ou socket; cópias defensivas; rejeição de construção inválida por vias públicas |
| Preservação | Não apagar/enfraquecer testes existentes; regressão configuração/codec/transporte e novo log quando autorizados |

Model não executa V-032, Maven, GUI/CORE ou o teste manual do novo incremento.
Os 149 testes de IMP-001 são evidência anterior, não resultado deste bolt.
O gate G-REVIEW-032 exige revisor humano distinto designado e revisão/integração
antes de DONE; ainda não há designação ou aprovação dessa revisão.

## Checkpoint e próxima etapa

Modelo concreto preparado para aprovação. Nenhuma lacuna material de fundações
ou decisão arquitetural inviável identificada nesta leitura. Design deverá fixar
assinaturas/records, compatibilidade do construtor existente, unidades/conversões,
origem dos diagnósticos e oráculos detalhados, antes de escrever a implementação.
Parar aqui: o pedido só autoriza Model. Os critérios funcionais do bolt/história
continuam pendentes até implementação, provas, revisão e integração reais.
