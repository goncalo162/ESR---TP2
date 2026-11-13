---
unit: "003-rotas"
bolt: "021-rotas"
stage: "adr"
status: "complete"
created: "2026-11-13T16:37:24Z"
---
# IMP-008 — revisão ADR para aprovação

✅ Model, Design e valores/horizontes de Q-C-26 aprovados explicitamente pelo
utilizador. ✅ Revisão ADR aprovada pelo utilizador; Implement autorizado e
preparado para revisão. Test não iniciado. A aprovação ADR não é revisão final
da implementação; as pendências na análise original abaixo são históricas.

## 1. Base, fontes e método

HEAD/main/Denji: `cc952c5f558930f2c67e7dea8fe92612cd8cc705`. Dependências
004-overlay/005-rotas e unidade002-overlay complete; integração f990503 é ancestral
de main. Estados de dependências e testes anteriores são evidência histórica.

Consultados ADD2/5/6/8–12, specs/intents e standards, Model/Design aprovados,
Q-C-26, docs06/07/09/14/15 e APIs reais de monitorização, configuração,
Neighbor/owner, composição e DTOs de controlo. A leitura de fontes é agora uma
inspeção estática autorizada para esta revisão, não uma análise Java executada.
Não houve compilação, testes, sockets ou alteração Java. Inventário/hashes na
[entrada ADR](../../../experiments/construction/021-rotas/adr-entry.json).

## 2. Decisões reutilizadas

| ADR | Conformidade verificada | Prova futura |
|---|---|---|
| 004 | Um candidato por vizinho/stream; soma do custo RTT já medido; desempate custo/saltos/nodeId; melhoria tardia não suprimida por seen booleano | D01/D03–D10/D19, V-011 |
| 017 | Tabela mutável só no owner serial existente; snapshots imutáveis; nenhuma thread/timer/I/O próprio | D14/D15/D18/D21 e integração futura |
| 018 | UUID da stream fixa a origem; época comum por fonte; instanceId/connectionId isolam callbacks; estado só em memória | D01/D11–D14/D16 |
| 024 | Um IMP; os mesmos seis caminhos; revisão de contratos sem implementar consumidores posteriores | auditoria de hashes/diff e V-ADD |
| 016 | candidateTtl/cacheTtl/maxConnections efetivos; capacidades da tabela próprias; nenhuma nova propriedade ou override de wire/política | D17/D20 |
| 020/021 | Validação antes de retenção; saturação atómica/local; entrada inválida não apaga estado saudável nem fecha pares válidos | D05/D06/D12/D13/D16/D17/D21 |
| 005 | Selecionado não é confirmado para dados; nenhum ACK/ativação/lease neste incremento | D02/D18; ativação futura em IMP-011 |
| 025 | Métrica/invalidade pelo plano TCP; não concluir saúde UDP ou implementar watchdog/rerouting | V-011 não demonstra vídeo ou recuperação UDP |
| 001/007/026 | Java21/JDK, records/cópias defensivas, sem runtime novo/CI/linter/JaCoCo/cobertura percentual | compilação/regressão futuras, D15/D22 |
| 002/012/013/034/035 | Controlo v2/19 tipos,3 rejeitado; único TCP e bootstrap inicial; UDP OTT1 e reparação/discovery intactos | fixtures/regressão anteriores preservadas, sem novo wire |

Q-C-26 concretiza ADR-004/018 com aprovação humana, sem substituir as suas
decisões. Não se cria ADR-036 ou se altera o estado de uma ADR por inferência.

## 3. APIs reais e conversão do custo

1 - **Contrato de entrada existente:**
    [LinkMetrics.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/monitoring/LinkMetrics.java)
    é record com session, state, ewmaMicros OptionalDouble, costMicros OptionalLong,
    sampleCount, lastSampleAtNanos e contadores. Com sampleCount0, os opcionais de
    amostra/custo estão vazios; não há zero confiável. Com amostra, o construtor
    exige EWMA finita, ≥1 e <2^63, e custo exatamente `(long)Math.ceil(ewma)`.
2 - **Cálculo existente:**
    [NetworkMonitor.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/monitoring/NetworkMonitor.java)
    calcula amostra como max(1, elapsedNanos/1000.0); a primeira inicia EWMA e as
    seguintes aplicam alpha. Link.metrics publica o custo ceil. Portanto a futura
    ponte deve passar **metrics.costMicros() diretamente** para setLinkCost;
    não usar sampleCount como custo, dividir por1000 outra vez, recalcular alpha
    ou fazer novo ceil/cast de ewmaMicros. Math.addExact verifica apenas a soma
    custo anunciado+link em RoutingTable/RouteMetric.
3 - **SUSPECT não é ausência de amostra:** se já existia medição, o monitor mantém
    costMicros presente quando conta falhas abaixo do limiar. A tabela recebe esse
    valor, não zero ou OptionalLong.empty por dedução do enum. Invalidated significa
    retirar a sessão esperada; UNMEASURED significa custo vazio. O domínio admite
    setLinkCost vazio como entrada explícita; não se inventa esse evento no monitor.
4 - **Proveniência exata:**
    [Neighbor.Session](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/Neighbor.java)
    tem nodeId, instanceId, connectionId e endpoint validado. A projeção SessionKey
    usa os três primeiros, preservando ambos os UUIDs. Snapshot.links é indexado
    por nodeId, mas isso não autoriza ignorar metrics.session. Reconexão na mesma
    instância exige novo token e reset de amostra/candidatos antigos.
5 - **Eventos disponíveis:** NetworkMonitor.Observer.onEvent recebe Updated,
    Invalidated e LocalFailure. Updated contém LinkMetrics; Invalidated contém
    Session/Reason. Futura ponte no owner: conferir token corrente, mapear Updated
    para setLinkCost e Invalidated para removeSession da sessão exata. NeighborDown
    posterior é idempotente; LocalFailure segue o lifecycle do nó, não é inventado
    como nova época de servidor ou nova mensagem de rede.

Estes são mapeamentos estáticos verificáveis, não integração já implementada.
Os casos D07/D14/D19 cobrem o domínio injetando valores equivalentes; integração
dos eventos reais não é declarada concluída por esses testes offline futuros.

## 4. Horizonte temporal, dimensões e alocação

[RuntimeConfiguration.Routing](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/RuntimeConfiguration.java)
expõe candidateTtl()/cacheTtl() como Duration e cacheCapacity() como int separado.
Os defaults reais são8000ms/16000ms/4096. Validação existente exige duração inteira
em ms, entre1 e Integer.MAX_VALUE; cacheTtl≥candidateTtl e candidateTtl>announceInterval.
Logo `.toMillis()` é exato nesse contrato e o produto por1000000 cabe em long;
a tabela ainda verifica entrada/produto no construtor. Control.maxConnections()
já valida1..64. Nenhuma alteração ao loader/configuração é necessária aqui.

Valores aprovados:64 origens,256 streams/origem,16384 linhas; N=min(C,63)
sessões/slots por stream; até16384×N slots, um watermark por slot; oito épocas
retiradas por origem/512 totais. **Mapas crescem por admissão, sem prealocação dos
produtos máximos.** Testes com Limits menores exercitam a mesma política.
Um slot com watermark continua a contar mesmo sem candidato; bindings continuam
a contar sem rotas disponíveis. Não há LRU de estado protegido ou orçamento4096
reinterpretado como linhas. Não é medição de consumo nem garantia para qualquer heap.

candidateTtl parte da sequência nova. A melhoria na mesma geração preserva início
e prazo; duplicado, métrica, retirada de sequência igual e callbacks antigos não
os renovam. Watermark retém a maior sequência por cacheTtl desde essa admissão;
candidato expirado não ressuscita dentro desse horizonte. Após historyTtl a
proteção termina, inclusive se a limpeza física ocorrer depois: é memória limitada,
não promessa de replay protection infinito. Usa-se cacheTtl como **duração aprovada**,
nunca cacheCapacity como dimensão da tabela.

Época retirada fica protegida por cacheTtl desde a retirada. Antes de E1→E2,
confirmEpoch valida CAS e capacidade; depois aplica a todas as streams da fonte
num único commit serial. Nona retirada ainda protegida recusa a transição inteira.
Limpeza de épocas expiradas pode abrir espaço; não se expulsa uma ainda válida.
UUID não tem ordenação temporal. Anúncio não chama automaticamente confirmEpoch;
o futuro owner valida contexto e faz comando explícito, dentro da mesma execução
serial antes de admitir o anúncio correspondente.

Instantes seguem a mesma aritmética monotónica local do monitor: diferença de
ticks long, incluindo negativos/wrap válido, intervalos <2^63ns. now é lido uma
vez por operação; age≥duration expira. Nem publishedAtNanos do snapshot do monitor,
nem timestamp civil/remoto, nem um zero de snapshot NEW iniciam o TTL de rotas.

## 5. Compatibilidade com consumidores seguintes e limites da prova

| Consumidor | Contrato preparado pelo IMP-008 | O que permanece futuro |
|---|---|---|
| IMP-009 / RoutingService e catálogo | bindStream fixa UUID/origem; confirmEpoch explícito; announce/withdraw e expire; Outcome com alterações; snapshots por fluxo | Validar anúncios, catálogo, flooding, dedup e composição no owner |
| IMP-010 / trace/CLI | selectedCandidate/path/knownUpstream permitem trace sem ativação | Encaminhar trace/TTL/NO_ROUTE e expor comandos |
| IMP-011/012 / ativação e procura | Route separa selectedCandidate de activeUpstream/pendingUpstream/downstreams | ACK, revisões de subscrição, leases e estados ativos; só aqui pode surgir autorização de dados |
| IMP-013/014 / falha e histerese | Alternativas preservadas, remoção correlacionada e custo/saltos determinísticos | Escolher parent confirmado, troca com ACK/histerese e recuperação da árvore |
| Forwarding UDP | Nunca deriva autorização de selectedCandidate ou INACTIVE | Snapshot confirmado, publicação segura e leitura atómica não bloqueante |

DTOs reais em
[ControlMessage.java](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/ControlMessage.java):
Announcement.stream() fornece StreamInfo.streamId()/serverId()/serverEpoch();
announceSeq()/costMicros()/path() correspondem aos campos do Design. Withdrawal
fornece streamId()/serverEpoch()/announceSeq(). Session vem do canal confirmado,
não do payload; **serverEpoch não é instanceId do relay emissor**. Não converter
metadados recebidos em oferta local, nem contornar a validação de caminhos/caps.

RoutingService e StreamForwarder existem vazios; Route/RouteMetric/RouteState/
RoutingTable também. Não existem consumidores implementados cuja assinatura seja
quebrada pelos seis novos modelos. Isso não prova integração executada nem fecha
os contratos futuros. O conceptual forwardingSnapshot de docs07 pertence ao plano
de dados; o snapshot do IMP-008 é de seleção e não o substitui.

[OverlayNode](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java)
expõe monitoringSnapshot(). Atualmente o observer interno do monitor trata falha
fatal; não encaminha Updated/Invalidated para routing. O observer de vizinhança
recebe eventos no owner, mas não é por isso um observer de métricas. Não presumir
uma API de subscrição de métricas ou acesso público ao owner do OverlayNode.
NetworkMonitor dispõe de Observer na construção; a futura composição deve definir
como partilhar o owner e entregar eventos, com âmbito aprovado no seu checkpoint.
Se exigir OverlayNode fora da allowlist de um IMP futuro, apresentar a lacuna e
pedir extensão antes de editar. Não antecipar essa integração ou alargar os seis
caminhos deste IMP por inferência.

## 6. Conclusão da análise e checkpoint

Não identificada divergência entre o domínio do Design aprovado e as ADR aceites,
nem incompatibilidade concreta com as APIs de entrada verificadas. Há uma fronteira
de composição futura explícita, não uma ligação atual alegadamente funcional.
O custo opcional e a projeção SessionKey correspondem às APIs reais sem alterar
monitor, configuração ou transporte. O detalhe de consumo direto de costMicros
esclarece o Design; não muda a política aprovada.

Reutilizam-se ADR-004/017/018/024 e transversais acima; nenhuma ADR nova ou
substituição necessária nesta análise. Q-C-26 está decidida quanto a princípios,
valores e horizonte; não significa V-011 executado. ADD/contratos/índice recebem
o detalhe aprovado e a ligação a esta revisão, sem promover a revisão a aprovada.

1 - ✅ Model e Design concluídos; Q-C-26 aprovada integralmente.
2 - ⏳ Aprovação humana desta revisão ADR pendente; parar antes de Implement.
3 - [ ] V-011/D01–D22, regressão atual e manual offline antes de aprovar Test.
4 - [ ] Revisão final Waheed e integração antes de DONE; nenhuma revisão final presumida.

Sem gates externos adicionais, Java alterado, Maven, commits, push ou outro IMP.
Se a implementação revelar decisão aceite inviável, parar a parte afetada,
preservar evidência/impacto e propor substituição pelo ADD11 antes de continuar.

Aprovação explícita da revisão ADR registada em 2026-11-13T17:01:29Z. Apenas Implement autorizado;
pendências de aprovação anteriores são históricas. Test/fecho continuam não autorizados.
