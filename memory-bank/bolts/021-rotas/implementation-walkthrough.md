---
unit: "003-rotas"
bolt: "021-rotas"
stage: "implement"
status: "complete"
created: "2026-11-13T17:17:41Z"
---
# IMP-008 — Implementation walkthrough para revisão

✅ Model/Design/Q-C-26/ADR/Implement aprovados pelo utilizador. Test técnico
executado; manual humano e aprovação de Test pendentes. O corpo abaixo conserva
o walkthrough apresentado em Implement. Só os seis caminhos autorizados foram alterados
em Projeto/src. Sem composição RoutingService/OverlayNode, commits/push ou outro IMP.

## 1. Base, aprovação e âmbito

Base HEAD/main/Denji cc952c5f558930f2c67e7dea8fe92612cd8cc705. Entrada suja apenas
com documentos/evidência dos checkpoints anteriores, preservada no
[manifesto de entrada](../../../experiments/construction/021-rotas/implement-entry.json).
[Autorização](../../../experiments/construction/021-rotas/adr-approval.json): Implement
nos seis caminhos e testes incrementais. Apenas Model/Design/ADR concluídos.

| Ficheiro | Linhas adicionadas/removidas contra a base | Entrega |
|---|---:|---|
| routing/RouteMetric.java | +20/−0 | Valor de custo/saltos e soma verificada |
| routing/RouteState.java | +4/−0 | Enum local; só INACTIVE/UNAVAILABLE produzidos |
| routing/Route.java | +48/−0 | Snapshot defensivo, sem autorização de dados |
| routing/RouteCandidate.java | +33/−0, novo | Candidato remoto, proveniência/path/validade |
| routing/RoutingTable.java | +361/−0 | Agregado, transações, retenção, limites e seleção |
| routing/RoutingTableTest.java | +385/−0, novo | 35 testes determinísticos de domínio |

Total **+851/−0**, seis caminhos main/test conforme ADD. Os quatro ficheiros
existiam vazios; os outros dois não existiam. **Nenhuma linha das fundações foi
removida ou substituída; nenhum teste anterior foi apagado, alterado ou enfraquecido.**
Os diffs completos e numstat são arquivados na evidência desta etapa; target fica
num inventário separado e não conta como alteração de fonte ou conteúdo aprovado.

## 2. Comportamentos implementados

1 - **Dados e seleção:** bindings UUID→origem persistem durante a execução;
    fonte local tem custo0 e upstream ausente. Candidatos por vizinho/stream,
    path simples fonte→emissor→local, limite32 já com local. Soma em µs por
    Math.addExact, ordenação custo/saltos/nodeId. Alternativas não são apagadas
    ao selecionar, retirar ou expirar o melhor.
2 - **Custo direto:** setLinkCost recebe OptionalLong, não EWMA. Teste com LinkMetrics
    real SUSPECT/EWMA1.2/cost2 demonstra consumo direto: anúncio10 produz12.
    Custo vazio mantém candidato pendente; atualização que cause overflow conserva
    proveniência/TTL mas não permite selecionar a soma impossível. Não cria ponte
    de eventos ou altera o monitor existente.
3 - **Sequência e tempo:** uma leitura monotónica por operação; ms→ns verificado,
    ticks negativos/wrap e fronteira age≥TTL. Só sequência nova renova o início.
    Melhoria de mesma geração, inclusive sem RTT, conserva prazo; watermark único
    por slot bloqueia ressurreição até cacheTtl. Retirada igual não renova; a nova
    instala watermark sem candidato. A proteção termina no horizonte aprovado.
4 - **Identidade:** CAS de sessão e época; callbacks de connectionId/instanceId
    antigos não afetam a nova. Substituição limpa amostra e candidatos antigos,
    repetição da sessão corrente não os reinicia. Sessão reparada confirmada é
    elegível sem consultar aresta inicial, endpoint/IP ou bootstrap.
5 - **Época por fonte:** mudança explícita, nunca inferida de UUID ordenável ou
    anúncio isolado. Invalida juntas todas as streams da fonte e ofertas locais,
    preservando outras fontes. Oito épocas retiradas protegidas por origem;
    nona mudança sem espaço recusa tudo, sem evicção protegida.
6 - **Limites e atomicidade:**64 origens×256 streams, N=min(C,63) sessões/slots por
    stream e um watermark por slot. Mapas crescem com entradas reais; não se
    prealocam produtos máximos. Cada operação prepara mapas/snapshots/revisões
    num draft privado e publica um único State só após todas as verificações.
    Rejeição não altera estado saudável. cacheCapacity4096 não é limite de rotas.
7 - **Leitura e ownership:** snapshots/paths/coleções/Outcome imutáveis, incluindo
    vista antiga após atualização. snapshot é chamado pelo owner e filtra expirados
    mesmo antes de expire físico. Não existe executor/timer/socket ou I/O novo.
    A classe exige confinamento pelo executor serial do chamador, sem criar outro.
    activeUpstream/pendingUpstream/downstreams ficam vazios; seleção não autoriza dados.

## 3. Execuções reais em Implement

JDK executado: /usr/lib/jvm/java-21-openjdk-amd64, OpenJDK21.0.13.0.3. O default
local era Java25; JAVA_HOME/PATH foram fixados no runner. POM sem alteração.
Comando de domínio: `mvn -o -B -ntp -Dtest=RoutingTableTest test`, em Projeto.
Regressão incremental: `mvn -o -B -ntp test`. Não foi executado clean verify,
package, manual offline ou etapa Test. Cada diretório contém comando/ambiente,
log, XML novos, contagens, hashes e ZIP dos seis fontes daquela execução.

| Execução | Testes/falhas/erros/skips | Resultado e alcance |
|---|---|---|
| implement-red-01 | 33/30/0/0 | Interfaces compilam; tabela ainda ausente. Três testes de valores/limites passam; restantes falham na preparação de bindings, não provam isoladamente todos os algoritmos |
| implement-state-02 | 33/27/0/0 | Bindings/sessões/épocas/ofertas reais; admissão/retirada/expiry ainda ausentes |
| implement-candidates-03 | 33/10/0/0 | Custos/seleção/correlação/TTL passam; dez falhas específicas nas operações withdraw/expire ainda ausentes |
| implement-green-04 | 33/0/0/0 | Núcleo completo aprovado pelos 33 casos iniciais |
| implement-record-red-05 | 35/1/0/0 | Novo negativo reproduz RouteCandidate direto aceitando custo anunciado positivo na origem; teste de leitura única passa |
| implement-green-06 | 35/0/0/0 | Invariante do record corrigido, sem enfraquecer teste |
| implement-regression-07 | 1211/0/72/0 | Sandbox:72 SocketException «Operação não permitida» em testes anteriores de rede; preservado, não declarado verde |
| implement-regression-08 | 1211/0/0/0 | Repetição autorizada fora da sandbox, mesmo código;14 XML, saída0 |

[Última regressão](../../../experiments/construction/021-rotas/implement-regression-08/result.json)
e [log](../../../experiments/construction/021-rotas/implement-regression-08/maven.log).
Regressão08 inclui35 novos+1176 anteriores. Não se somam execuções repetidas como
se fossem testes únicos. A mensagem de falha controlada do bootstrap no log
pertence aos testes negativos; o resultado final real é1211/0/0/0.

## 4. Correspondência D01–D22/V-011

Os métodos abaixo estão no XML de RoutingTableTest da execução08; prefixos D
são correspondência de Design, não identificadores de testes históricos.

| Design | Métodos de teste (prefixo) | Prova |
|---|---|---|
| D01 | d01FourStreams | Quatro UUIDs, mesma época por fonte e isolamento |
| D02 | d02LocalSource, d02RemoteNode | Custo local0, remoção correlacionada e nenhuma autorização de dados |
| D03 | d03KnownCosts | Fixtures literais140/130, ambas as ordens |
| D04 | d04Ties | Saltos/nodeId e alternativas preservadas |
| D05 | d05InvalidPaths, d05ThirtyOne | Caminhos inválidos, limite31+local/32+local |
| D06 | d06MetricArithmetic, d06Overflowing | Long.MAX_VALUE e rejeição atómica de overflow/negativos |
| D07 | d07Unmeasured, d07MeasuredSuspect | Custo opcional e consumo direto de LinkMetrics.costMicros |
| D08 | d08Expiry | Alternativa após expiry/remoção |
| D09 | d09SameGeneration, d09ImprovedPending, d09OnlyNewer | Só seq nova renova; melhoria sem amostra não estende TTL |
| D10 | d10ExpiredWatermark | Retenção após expiry, fronteira16s e reutilização limitada |
| D11 | d11Withdrawal, d11NewerWithdrawal | Retiradas iguais/novas/antigas, com e sem candidato |
| D12 | d12EpochChange, d12LocalOffers | Mudança explícita por fonte, CAS, isolamento e oferta local |
| D13 | d13EightRetired | Saturação sem invalidação parcial; proteção e expiração sem renovação por duplicado |
| D14 | d14OldCallbacks | Sessão nova na mesma instância; callbacks antigos ignorados e custo resetado |
| D15 | d15InputsNested | Cópias defensivas transitivas, outcomes e vista anterior |
| D16 | d16Bindings | UUID/origem persistente, stream desconhecida e conflito |
| D17 | d17ExactProduction, d17SmallBudgets | Valores/produtos exatos, limites pequenos, saturação, slots/watermarks e atualização sem evicção |
| D18 | d18ConfirmedRepair | Não exige grafo inicial; ACTIVE/parent confirmado impossível neste snapshot |
| D19 | d19MetricOverflow | Anúncio retido sem custo elegível e recuperação dentro do mesmo TTL |
| D20 | d20TickWrap, d20ClockRegression, d20Durations, d20EachOperation | Unidades, wrap/ticks negativos, relógio regressivo e leitura única |
| D21 | d21RevisionBounds, d21InvalidDomain, d21SourceCandidate | Revisões verificadas, rejeições e custo0 da origem |
| D22 | **PARCIAL** | Mapa/XML/regressão incremental feitos; clean verify, manual humano, revisão final e integração ainda futuros |

V-011 técnico exercitado nos35 testes: custos conhecidos/desempate, quatro streams,
alternativas e snapshots. Consolidação/aprovação de Test ainda pendentes. Não se
simularam bilhões de revisões: fronteira Long.MAX_VALUE é testada pelo helper
nextRevision aprovado; rollback por saturação/identidade/entrada é testado na API.
Os caps/produtos de produção são verificados sem alocar um milhão de slots;
Limits pequenos comprovam as mesmas guardas. Não há medição de memória máxima.

## 5. Preservação, limites e checkpoint

Código final corresponde aos seis hashes da execução08 e ao
[manifesto para revisão](../../../experiments/construction/021-rotas/implement-review-source-sha256.json),
que identifica o candidato, **não aprovação humana já recebida**.
Baseline tinha234 ficheiros fora de target: quatro vazios implementados,
230 preservados, dois caminhos novos autorizados. POM, configuração, monitor,
overlay, protocolo/fixtures e testes anteriores permanecem intactos.
As3392 evidências anteriores e os artefactos dos checkpoints deste bolt são
conferidos separadamente; target compilado/gerado não é evidência de integração.
O JAR anterior não foi reconstruído por `test` nem validado como este incremento.

1 - ⏳ Aprovação de Implement pendente; current_stage=implement.
2 - [ ] Test: clean verify JDK21 novo, XML/JAR/hash e V-011 consolidado.
3 - [ ] Manual offline preparado/executado tecnicamente e repetido pelo humano antes de aprovar Test.
4 - [ ] Revisão final atribuída a Waheed e integração antes de DONE.

Sem gates externos adicionais. Sem autorização de Test/fecho, commits/push ou
outro IMP. A atribuição do revisor não prova revisão efetuada. A composição de
métricas/rotas e o encaminhamento de vídeo continuam nos seus checkpoints futuros.

Registo 2026-11-13T17:43:51Z: Implement aprovado explicitamente; resultados atuais em
[ddd-03-test-report.md](ddd-03-test-report.md). Test continua em progresso, sem fecho.
