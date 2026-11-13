---
unit: "003-rotas"
bolt: "021-rotas"
stage: "design"
status: "complete"
created: "2026-11-13T16:14:58Z"
---
# IMP-008 — Design de candidatos e snapshots

✅ Model e os três princípios de Q-C-26 aprovados pelo utilizador.
✅ Design, capacidades e horizontes aprovados pelo utilizador, com registo em
2026-11-13T16:41:04Z. Revisão ADR autorizada e preparada; aprovação dessa etapa pendente.
Implement/Test não iniciados. Nenhum Java alterado. A proibição de leitura de
fontes desta etapa foi respeitada; a inspeção das APIs reais ocorreu só em ADR.

## 1. Base, arquitetura e fronteira

Base `cc952c5f558930f2c67e7dea8fe92612cd8cc705`, Denji, igual a main.
Dependências confirmadas no [Model](ddd-01-domain-model.md); entrada deste
checkpoint preservada em [design-entry.json](../../../experiments/construction/021-rotas/design-entry.json).
Os documentos e evidências do Model são anteriores a esta aprovação.

Tabela em memória com owner serial (ADR-017); operações síncronas e sem I/O.
Projeções imutáveis para consumidores; nenhum executor, socket, future, timer,
repositório persistente ou dependência runtime nova. O catálogo e o owner futuros
validam o contexto de rede; a tabela volta a verificar os seus invariantes locais.

Mantêm-se exatamente os seis caminhos do IMP-008:

| Ficheiro | Responsabilidade futura |
|---|---|
| routing/RouteMetric.java | Valor de custo/saltos, soma e ordem verificadas |
| routing/RouteCandidate.java | Candidato imutável com proveniência, path e validade |
| routing/RouteState.java | Estados do modelo; apenas INACTIVE/UNAVAILABLE produzidos neste incremento |
| routing/Route.java | Snapshot imutável da rota, seleção e campos de dados explicitamente vazios |
| routing/RoutingTable.java | Agregado, tipos auxiliares internos, limites, tempo, transações e projeções |
| routing/RoutingTableTest.java | V-011/M01–M18, casos D01–D22 abaixo |

Caminhos main/test relativos a Projeto/src/main/java/pt/uminho/esr/ott e
Projeto/src/test/java/pt/uminho/esr/ott. Quatro main estão vazios e dois caminhos
ausentes no inventário; nenhum deles precisa de substituir implementação existente.
Não se afirma compatibilidade com assinaturas de fontes não inspecionadas. Tipos
internos de entrada evitam depender de APIs Java não verificadas; a composição
com Neighbor/LinkMetrics será explícita no IMP-009, sem alterar esses componentes aqui.

Não criar StreamKey.java, Limits.java ou Clock.java separados. Não alterar
RoutingService, OverlayNode, protocolo, configuração, fixtures, POM ou testes
anteriores. Sem flooding, subscrições, CLI, histerese, UDP ou vídeo neste incremento.
TCP v2/19 tipos, código3 rejeitado, bootstrap inicial e UDP OTT1 ficam preservados.

## 2. Tipos públicos propostos

Assinaturas de Design, **não código implementado**. Records fazem cópias defensivas
e validam campos antes de reter dados. UUIDs/IDs não nulos; nodeId ASCII1–32 conforme
o contrato existente. SessionKey tem nodeId diferente do local, instanceId não
nula/não-zero e connectionId não nula; sessão só existe depois da admissão do
overlay. Sem IDs ou códigos novos de protocolo.

```java
// RouteMetric.java
public record RouteMetric(long costMicros, int hopCount)
        implements Comparable<RouteMetric> {
    public static RouteMetric accumulate(long advertisedCostMicros,
                                         long linkCostMicros, int hopCount);
}

// Tipos aninhados em RoutingTable.java
public record SessionKey(String nodeId, UUID instanceId, UUID connectionId) {}
public record Advertisement(UUID streamId, String serverId, UUID serverEpoch,
                            SessionKey session, long announceSeq,
                            long advertisedCostMicros, List<String> advertisedPath) {}
public record Limits(int origins, int streamsPerOrigin, int neighbors,
                     int retiredEpochsPerOrigin) {
    public static Limits forNode(int maxConnections);
}
public record Outcome(Status status, Reason reason, Set<UUID> changedStreams) {}
public enum Status { APPLIED, IGNORED, REJECTED }
public enum Reason {
    NONE, DUPLICATE, STALE_SEQUENCE, GENERATION_EXPIRED, WITHDRAWN_GENERATION,
    STALE_SESSION, STALE_EPOCH, RETIRED_EPOCH, UNKNOWN_STREAM,
    ORIGIN_CONFLICT, INVALID_INPUT, CAPACITY_EXCEEDED, REVISION_EXHAUSTED
}

// RouteCandidate.java; SessionKey é RoutingTable.SessionKey
public record RouteCandidate(UUID streamId, String serverId, UUID serverEpoch,
    SessionKey upstream, List<String> path, long announceSeq,
    long advertisedCostMicros, Optional<RouteMetric> metric,
    long generationStartedAtNanos, long candidateTtlNanos) {}

// Route.java
public record Route(UUID streamId, String serverId, Optional<UUID> serverEpoch,
    boolean localSource, Optional<RouteCandidate> selectedCandidate,
    Optional<RouteMetric> selectedMetric, Optional<SessionKey> knownUpstream,
    Optional<SessionKey> activeUpstream, Optional<SessionKey> pendingUpstream,
    Set<String> downstreams, List<RouteCandidate> candidates,
    RouteState state, long revision) {}

// RouteState.java: modelo global, sem transições futuras neste IMP
public enum RouteState { UNAVAILABLE, INACTIVE, ACTIVATING, ACTIVE, SWITCHING }
```

`metric.empty()` significa falta de custo utilizável (sem amostra ou soma tornada
irrepresentável por uma atualização da métrica); não significa zero. `candidates`
inclui candidatos vivos não selecionáveis e alternativas; não inclui tombstones.
HopCount deriva do path guardado: size−1, entre1 e31 no remoto; local0.
RouteCandidate só representa remoto; fonte local tem flag própria, selectedMetric
(0,0), selectedCandidate/knownUpstream vazios. O upstream conhecido selecionado
não preenche activeUpstream ou pendingUpstream. Estes e downstreams são sempre
vazios neste incremento; estado nunca ACTIVE, mesmo para fonte local.

Comparar RouteMetric por custo e saltos, e na seleção da tabela por nodeId ASCII
do upstream. Sem desempate por instanceId, UUID da época, IP ou ordem de chegada.
IDs de enum acima são locais, não novos códigos wire; preservar convenções Java
existentes quando a análise de fontes for autorizada antes de Implement.

## 3. API do agregado e contratos

```java
public RoutingTable(String localNodeId, Limits limits,
                    long candidateTtlMillis, long historyTtlMillis,
                    LongSupplier nanoTime);
public Outcome bindStream(UUID streamId, String expectedServerId);
public Outcome replaceSession(Optional<SessionKey> expected, SessionKey replacement);
public Outcome removeSession(SessionKey session);
public Outcome setLinkCost(SessionKey session, OptionalLong costMicros);
public Outcome confirmEpoch(String serverId, Optional<UUID> expectedCurrent,
                            UUID replacement);
public Outcome offerLocal(UUID streamId, UUID epoch);
public Outcome removeLocalOffer(UUID streamId, UUID epoch);
public Outcome announce(Advertisement advertisement);
public Outcome withdraw(UUID streamId, UUID epoch, SessionKey session, long sequence);
public Outcome expire();
public Optional<Route> snapshot(UUID streamId);
```

1 - **bindStream:** comando do catálogo local validado; fixa UUID→serverId durante
    toda a vida da tabela. Idempotente se igual; origem conflitante rejeitada.
    Reserva linha mesmo UNAVAILABLE, evitando reatribuição após expiração. Sem
    expulsão LRU ou criação de stream desconhecida por announce/withdraw.
2 - **replaceSession:** compare-and-set da sessão esperada por nodeId. Ausência
    esperada só aceita se não houver sessão; repetir a mesma sessão é idempotente.
    Repetição idêntica nunca limpa métricas ou reinicia validade. Substituição
    válida elimina candidatos/watermarks da sessão antiga e a sua
    métrica; callbacks antigos falham a comparação completa. O futuro owner só
    encaminha eventos NeighborUp correntes, não usa esta API para autorizar peers.
3 - **removeSession/setLinkCost:** exigem token atual completo. Down antigo é
    ignorado e não remove substituto. Nova sessão começa sem medição. A remoção
    não fecha transportes, cria ligações ou modifica sessões saudáveis.
4 - **confirmEpoch:** operação explícita com compare-and-set da época esperada
    da fonte; não pode ser disparada implicitamente por announce. Época igual
    não renova históricos; UUID retirado ainda protegido é rejeitado.
5 - **offerLocal:** só para stream ligada ao localNodeId e época corrente da
    fonte; sem prazo remoto. removeLocalOffer exige mesma época. Remover oferta
    não liberta a associação UUID→origem. Época nova exige nova oferta explícita.
6 - **announce/withdraw:** só stream ligada, fonte/época correntes, sessão atual e
    sequência não negativa. Rejeição preserva o estado. Detalhes nas secções5–6.
7 - **expire:** manutenção explícita chamada pelo owner; remove entradas cujo
    prazo chegou. Não abre timer nem lê relógio civil. Sem trabalho assíncrono.
8 - **snapshot:** só pelo owner, obtém uma vista imutável no instante observado.
    Filtra candidatos expirados mesmo antes da limpeza física; desconhecido dá
    Optional.empty, fluxo conhecido sem caminho dá Route UNAVAILABLE. Atualização
    da projeção/revision por expiração lógica pertence ao owner, nunca ao leitor UDP.

Outcomes têm motivos explícitos e conjunto imutável de streams cuja projeção
mudou; tamanho máximo limitado pelo número de linhas. Não são eventos de rede.
APPLIED pode ter conjunto vazio (ex.: watermark sem candidato, custo igual).
IGNORED não renova prazo. Inputs estruturalmente inválidos nos records/construtor
lançam IllegalArgumentException/NullPointerException antes de mutação; rejeições
de contexto/recursos usam Outcome. Relógio inválido produz IllegalStateException
antes de alterar o estado. Revisão cresce por mudança da projeção, com incremento
verificado; esgotamento rejeita a transação inteira, sem wrap ou alteração parcial.
Um helper package-private `nextRevision(long)` em RoutingTable permite testar
diretamente a fronteira Long.MAX_VALUE sem bilhões de operações ou reflexão.

Todas as chamadas são do mesmo executor serial. Os snapshots devolvidos podem
ser publicados por referência segura pelo futuro owner e lidos noutras threads;
a tabela mutável não passa para essas threads. Não implementar já
forwardingSnapshot que autorize dados. Leitura futura de dados depende de upstream
confirmado/época e é um contrato de incrementos posteriores.

## 4. Capacidades exatas aprovadas — Q-C-26

Não se altera qualquer hard cap do deployment ou propriedade de configuração.
`Limits.forNode(C)` recebe o maxConnections validado já existente (1..64).
N=min(C,63), independente de queueCapacity e de routing.cache.capacity.

| Coleção | Limite de produção | Justificação |
|---|---:|---|
| Origens ligadas | 64 | Cap existente de nós/deployment; não inferir só dois servidores |
| Linhas por origem | 256 | Cap existente de streams/origem |
| Linhas totais, incluindo UNAVAILABLE | 16384 | 64×256; binding persiste durante a execução |
| Sessões/métricas correntes | N≤63 | Grau máximo já imposto pelo overlay; nenhuma reserva física nova |
| Slots por stream | N≤63 | Um slot por vizinho; candidato e watermark usam a mesma entrada |
| Slots/candidatos máximos totais | 16384×N≤1032192 | Produto verificado; tombstone ocupa slot tal como candidato |
| Watermarks por slot | 1 | Apenas maior sequência da sessão/época; sem lista por anúncio |
| Épocas retiradas por origem | 8 | Orçamento local proposto para churn; saturação explícita |
| Épocas retiradas totais | 512 | 64×8; época corrente é um campo separado por origem |
| Path por candidato | 32 IDs | Inclui local; cap existente, cópia limitada antes de reter |

Valores de Limits para testes podem ser menores, entre1 e os limites de produção
(origens≤64, streams/origem≤256, vizinhos≤63, épocas/origem≤8); dimensões e produtos
verificados no construtor. Não se cria configuração runtime para estes budgets.
A produção usa64/256/N/8; testes pequenos exercitam saturação sem alocar um milhão
de entradas. Não prealocar matrizes/produtos máximos: mapas crescem só com admissões.
O limite é finito, mas o extremo pode consumir memória substancial; não é uma
medição ou garantia de adequação a qualquer heap. O orçamento8 e o horizonte
abaixo foram aprovados no Design; Implement ainda requer o checkpoint ADR.

Não usar4096 para limitar rotas ou slots. routing.cache.capacity permanece dedicado
ao flooding futuro. Cada linha sem candidatos continua a contar para as256/16384;
não há tombstones de UUID ilimitados nem evicção de associações saudáveis.

Se uma admissão precisar de linha/slot/época adicional sem capacidade, devolve
REJECTED/CAPACITY_EXCEEDED, sem alteração parcial nem expulsão de estado vivo.
Atualizar um slot existente não necessita de outro; remover sessão/expire liberta
slots. Épocas protegidas e linhas ligadas não são expulsas para aceitar entradas.
Capacidade pode exigir expire explícito antes de repetir uma admissão; a limpeza
nunca é prova de terminação física de transportes (que não pertencem à tabela).

## 5. Tempo, sequência e histórico de gerações

candidateTtlMillis vem do parâmetro existente routing.candidate.ttl.ms (default8000).
historyTtlMillis usa **routing.cache.ttl.ms**, default16000, como horizonte temporal
já previsto; usa a duração, nunca a capacidade4096. Validar1..2147483647ms e
history≥candidate; conversão `Math.multiplyExact(ms, 1000000L)` para nanos.
Nenhuma alteração ao ConfigurationLoader é necessária neste incremento.

Ler nanoTime uma vez por operação. Instantes são ticks long locais, incluindo
valores negativos/wrap de System.nanoTime; guardar início+duração, não somar
deadline com saturação silenciosa. Idade é `now−start` em aritmética modular,
válida para intervalos inferiores a2^63ns; durações configuráveis são muito menores.
Chegada ao prazo: age≥duration significa expirado. Detetar recuo do relógio entre
operações (delta negativo) antes de mutar; wrap normal com delta positivo é válido.
Não suportar lacuna de observação de2^63ns nem alegar relógio remoto sincronizado.

Cada slot `(streamId,nodeId)` contém época/sessão, maxSequence, instante de início
da geração, watermark até historyTtl e candidato opcional. Não guardar um objeto
por sequência passada. A validade do candidato e a do watermark partem da mesma
admissão de sequência mais recente; melhorias/métricas/duplicados não movem o início.

| Entrada no slot atual | Efeito |
|---|---|
| Seq maior, conteúdo válido | Substituir candidato, guardar maxSequence, iniciar candidateTtl/historyTtl novos, mesmo se custo piorou |
| Mesma seq, candidato vivo, melhoria estrita | Atualizar conteúdo apenas se custo/saltos melhoram; manter ambos os prazos originais |
| Mesma seq, igual/pior | IGNORE/DUPLICATE; não renovar nem substituir |
| Seq menor | IGNORE/STALE_SEQUENCE; não renovar nem retirar candidato |
| Mesma seq com candidato expirado/retirado e watermark vivo | IGNORE/GENERATION_EXPIRED ou WITHDRAWN_GENERATION; nunca ressuscitar candidato |
| Seq nova, sem amostra RTT | Guardar candidato pendente e prazos normais; seleção espera amostra atual |

Na mesma geração do mesmo vizinho, comparar custo anunciado e saltos: a parcela
da ligação atual é comum aos dois anúncios, logo essa ordem também prova melhoria
do custo acumulado quando representável. Upstream igual não oferece terceiro
desempate. Path diferente de igual métrica não substitui o anterior. Mesmo sem
amostra, uma melhoria comprovável do anúncio pode substituir o conteúdo pendente,
sem renovar prazo e sem tornar o candidato selecionável antes de setLinkCost.
Isto não compara custos desconhecidos de vizinhos distintos. Anúncio novo com
soma já impossível perante a métrica atual é rejeitado antes de substituir estado válido.

Na ausência de slot, cria-se slot se houver capacidade. expire remove candidato
ao candidateTtl, mas só remove slot/watermark ao historyTtl. Nesse intervalo uma
melhoria atrasada não reinicia a geração. Após remoção do watermark, um anúncio
da mesma sequência pode voltar a ser admitido: proteção é limitada ao horizonte,
não memória infinita nem substituto da deduplicação/flooding de IMP-009.
No instante exato historyTtl a proteção já terminou; expire pode libertar o slot.
Uma admissão no mesmo slot logicamente expirado trata-o como ausente e pode
reutilizar a sua reserva num único commit, iniciando novo intervalo local. Não
reutilizar um watermark expirado como se ainda tivesse frescura válida.

Withdraw só afeta o slot do emissor daquela stream/época. Seq antiga é ignorada;
seq igual remove candidato e marca a geração retirada sem prolongar prazos;
seq maior instala watermark de retirada com novo início, sem candidato. Repetição
igual não renova. Uma retirada válida sem slot pode reservar só watermark sujeito
aos mesmos limites. Retirada/métrica de sessão antiga não modifica slot corrente.

## 6. Mudança de época atómica por fonte

Origens ligadas começam sem época; confirmEpoch com expected vazio instala a
primeira, sem retirar época inexistente. Anúncios não criam ou mudam a época.
Ao mudar E1→E2, o owner já validou a nova execução pelos contratos futuros de
routing, e a tabela executa, sem chamadas externas intercaladas:

1 - **Preparar:** verificar origem/expectedCurrent, rejeitar E2 se retirada ainda
    protegida; calcular entradas de histórico expiradas e capacidade após limpeza.
2 - **Reservar:** garantir espaço para tombstone de E1, com início agora e duração
    historyTtl; garantir revisões/estruturas necessárias sem alterar o estado vivo.
3 - **Aplicar:** trocar época da fonte; remover candidatos/watermarks/ofertas locais
    de todas as suas streams; manter UUID→origem, outras fontes e sessões/métricas.
4 - **Publicar:** snapshots dessas streams passam juntos à época nova sem seleção,
    UNAVAILABLE, sem upstream ativo. Retornar streams afetadas de forma imutável.

Qualquer rejeição preserva época corrente, candidatos, ofertas, tombstones vivos
e snapshots. Não publicar metade das streams da fonte. Épocas UUID não ordenáveis;
CAS evita callback de transição antigo. As oito entradas protegem as oito últimas
retiradas ainda no horizonte: nona retirada simultaneamente protegida é recusada
até expirar alguma; não expulsar a mais antiga ainda válida. Operação repetida
para a época corrente é idempotente e não estende qualquer tombstone.
Depois do horizonte, o mesmo UUID pode ser confirmado por evento explícito: não
existe proteção ilimitada. Anúncio isolado nunca é esse evento, mesmo após expiry.

## 7. Métricas, seleção, transação e publicação

setLinkCost recebe custo medido já convertido em µs pelo contrato do monitor,
não reaplica EWMA. OptionalLong vazio é ausência de amostra; presente exige≥1.
A tabela não aceita zero como medida confiável. A inspeção posterior em ADR confirmou que LinkMetrics.costMicros() já fornece
o custo inteiro (ceil, mínimo1, finito/representável). O adaptador futuro passa
esse OptionalLong diretamente, sem segundo arredondamento ou cálculo de RTT.

Anúncio recebido `[S,...,P]` exige origemS, últimoP=session.nodeId, IDs únicos e
local ausente. Lista de entrada é limitada a31 IDs para acrescentar local dentro
do cap32; copiar só após validar comprimento. Custo anunciado≥0, com zero apenas
no anúncio de origem com path `[S]`. Candidato armazena `[S,...,P,local]`.

Soma por Math.addExact(advertised,link). Entrada que já produz overflow é
REJECTED/INVALID_INPUT sem substituir candidato anterior. Se uma métrica posterior
maior causar overflow num candidato existente, conservar o anúncio/prazo mas
marcar métrica indisponível/não selecionável, escolhendo alternativa válida. Outra
medição representável pode voltar a torná-lo elegível dentro do mesmo TTL.
Atualizar métrica não renova geração ou TTL, mesmo quando melhora a seleção.

Cada operação valida tokens, domínios, limites e revisões antes de commit; calcula
mudanças e snapshots afetados, depois troca o estado no owner sem callbacks. O
commit de mudança de fonte pode afetar até256 streams; mudança de métrica/sessão
pode afetar16384, sempre limitado. Não há locks/futures para concluir dentro deles.
Inputs rejeitados não alteram estado saudável; snapshot/expire pode refletir o
passar legítimo do tempo, sem atribuir essa expiração à entrada rejeitada.

Snapshot constrói Map/Set/List imutáveis onde aplicável, incluindo paths e
Outcome.changedStreams; DTOs não expõem estruturas do agregado. Sem amostra,
sem candidatos vivos ou só candidatos irrepresentáveis dá UNAVAILABLE. Fonte local
oferecida ou melhor candidato utilizável dá INACTIVE. Destinos e upstreams de
dados permanecem vazios. Ordenação estável das alternativas por elegibilidade,
custo/saltos/nodeId; pendentes depois, por nodeId. Nenhuma referência a Connection.

## 8. Oráculos de Design — todos NÃO EXECUTADOS

Um relógio long controlado, custos injetados e SessionKeys explícitas bastam;
nenhum fake de TCP ou sleep é necessário. Dados de custo esperados são literais,
não calculados reutilizando a implementação sob teste. Mantêm-se todos os testes
anteriores; primeiro escrever os casos novos e registar falhas reais em Implement.

| Caso | Model/V-011 | Prova concreta |
|---|---|---|
| D01 | M01 | Quatro UUIDs, duas fontes, época comum por fonte; atualizar/retirar um não altera os outros |
| D02 | M02/M16 | Oferta local0/0, sem candidate/upstream de dados, destinos vazios; remover exige época corrente |
| D03 | M03 | 100+40=140 contra80+50=130; inserir nas duas ordens seleciona130 |
| D04 | M04 | Custos iguais: menos saltos; depois R1 antes deR2; manter ambos candidatos |
| D05 | M05/M06 | Loops, origem/emissor incorretos, próprio ID,31+local aceites e32+local rejeitados |
| D06 | M06/M14 | Long.MAX_VALUE−1+1 aceite; +2 rejeitado preserva snapshot; negativos/zero remoto irregular inválidos |
| D07 | M07 | Sem amostra pendente; medição válida permite escolher; perder amostra torna inelegível sem renovar TTL |
| D08 | M08/M09 | Expirar/retirar melhor preserva alternativa; expires no instante≥prazo; depois UNAVAILABLE |
| D09 | M10 | Nova seq aos7s renova; mesma seq melhor aos7s expira aos8s originais, inclusive se pendente sem RTT; duplicado/pior/antigo não renovam |
| D10 | M10/M15 | Slot candidato expira aos8s, watermark protege até16s; melhoria aos9s não ressuscita; nova seq aceita |
| D11 | M10 | Withdraw igual/mais novo, repetido/antigo, com e sem slot; não remover alternativa nem prolongar igual |
| D12 | M11 | E1→E2 altera juntas as duas streams deS1, nãoS2; CAS errado e E1 protegida preservam tudo |
| D13 | M11/M15 | Oito épocas retiradas protegidas, nona recusa sem evicção; após16s liberta; repetição não renova; UUIDs arbitrários |
| D14 | M12/M17 | Substituir conexão, ignorar métrica/Down/announce/withdraw antigos; mesma instanceId não basta |
| D15 | M13 | Mutar inputs e tentar mutar snapshot/path/listas/sets falha ou não altera domínio; vista anterior estável |
| D16 | M14 | Origem conflitante/stream desconhecida rejeitada; bindings persistem sem candidatos; nenhuma mutação parcial |
| D17 | M15 | Limites de produção/produtos e dimensões inválidas; Limits pequenos saturam origem/stream/sessão/slot/histórico; update em slot cheio aceita |
| D18 | M16/M17 | Sessão reparada não precisa de aresta inicial; seleção não produz ACTIVE/ACK/dados, nem qualquer socket |
| D19 | M07/M14 | Atualização de métrica causa overflow, preserva anúncio e escolhe alternativa; volta a ser elegível sem renovar TTL |
| D20 | M09/M10 | ms→ns verificado; fronteira exata, ticks negativos e wrap válidos; relógio regressivo rejeita antes de alterar estado |
| D21 | M15/M16 | Revisões verificadas, resultado imutável, transações rejeitadas preservam snapshots e reservas; limpeza de histórico no prazo |
| D22 | M18 | V-011 mapeado nos XML futuros, clean verify JDK21, manual offline humano, revisão final e integração comprovadas |

O manual futuro exercita API pública offline com duas fontes/quatro streams,
custos/empates, expiração, nova época, saturação e snapshots. Preparar comando
repetível e executar tecnicamente e pelo humano antes de aprovar Test. Não requer
ou demonstra vídeo, CORE, alteração da topologia ou rede entre processos.

## 9. ADR e checkpoint

ADR-004/017/018/024 continuam a fundamentar seleção, owner, identidades e âmbito.
Não há nova ADR aceite ou revisão ADR concluída por efeito deste Design. Os
princípios, números e retenção de Q-C-26 foram aprovados, incluindo orçamento8
e historyTtl=cache.ttl; o checkpoint ADR continua para aprovação.

1 - ✅ Design e limites/horizontes aprovados pelo utilizador.
2 - ⏳ Revisão ADR preparada para aprovação; apresentar divergências antes de código.
3 - [ ] Implement nos seis caminhos, com falhas reais e incrementais.
4 - [ ] Test: V-011, regressão atual e manual offline antes de aprovação.
5 - [ ] Revisão final Waheed e integração antes do fecho; atribuição não é evidência de revisão.

Sem gates externos adicionais; sem Implement, Maven, commits, push ou outro IMP.

Registo da aprovação: [design-approval.json](../../../experiments/construction/021-rotas/design-approval.json).
Inspeção de APIs e fronteiras futuras: [revisão ADR](adr-review.md).
