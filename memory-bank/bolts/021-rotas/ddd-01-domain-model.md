---
unit: "003-rotas"
bolt: "021-rotas"
stage: "model"
status: "complete"
created: "2026-11-13T16:04:28Z"
---
# IMP-008 — Model: candidatos, custos e snapshots por stream

✅ Model e os três princípios de Q-C-26 aprovados explicitamente pelo utilizador;
registo em 2026-11-13T16:19:45Z. Apenas Design autorizado em seguida. V-011 não executado.
As secções abaixo preservam o modelo apresentado no checkpoint; as menções a
propostas pendentes descrevem esse checkpoint anterior. Estado atual: princípios
aprovados, valores/retenção concretos propostos no Design ainda para revisão.

## 1. Base, fontes e dependências

Base limpa `cc952c5f558930f2c67e7dea8fe92612cd8cc705`, ramo Denji;
main e Denji apontam para a mesma base. Inventário de entrada e hashes em
[model-entry.json](../../../experiments/construction/021-rotas/model-entry.json).

1 - ✅ IMP-006/004-overlay complete; unidade 002-overlay complete.
2 - ✅ IMP-007/005-rotas complete; integração `f9905038c614612b6f2798d4eca46dd72934743f`
    é ancestral de main. Os seis hashes de código e o JAR da prova de integração
    foram conferidos. Os 1176 testes são resultado histórico, não execução deste Model.
3 - ⏳ IMP-008/021-rotas, história 002 e TASK-011 passam a em progresso apenas em Model.
    Unidade 003-rotas/PHASE-03 continuam em progresso; nenhum critério funcional concluído.

Precedência: enunciado → specs → intents → documentação → código. Consultados
enunciado ESR-TP2-26-27, specs/REQ-005/006/014/021, requisitos/contexto do intent,
ADD 2/5/6/8–12, ADR-004/017/018/024, história/unidade/TASK-011, docs 06/07/09/14/15
e contratos documentados da monitorização concluída. O tipo DDD proíbe leitura de
código em Model: apenas inventário de caminhos/tamanhos e hashes de bytes, sem
analisar fontes Java. Compatibilidade com APIs concretas será conferida quando
autorizada na etapa própria; não é alegada por esta revisão documental.

## 2. Âmbito e linguagem

O domínio guarda caminhos candidatos, calcula a preferência determinística e
publica vistas imutáveis por fluxo. Não difunde anúncios nem ativa uma árvore.
O campo **Origem** do enunciado representa o próximo upstream, distinguindo-se
do **servidor de origem** que é proprietário do UUID da stream.

Ficheiros autorizados pelo ADD para implementação futura:

- `Projeto/src/main/java/pt/uminho/esr/ott/routing/Route.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RouteCandidate.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RouteState.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RouteMetric.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingTable.java`;
- `Projeto/src/test/java/pt/uminho/esr/ott/routing/RoutingTableTest.java`.

Route/RouteState/RouteMetric/RoutingTable existem vazios; os outros dois não existem.
Isto é inventário, não verificação de implementação. StreamKey, token de sessão,
limites e outros valores necessários devem caber nestes caminhos, por tipos internos
ou reutilização autorizada; não criar ficheiros adicionais por inferência.

Fora do incremento: RoutingService, catálogo recebido da rede, flooding/deduplicação
de anúncios, handlers TCP, CLI routes/trace, subscrições/ACK, histerese, forwarding,
UDP, vídeo, reparação da overlay, persistência e alterações de configuração.
IMP-009 integra os anúncios; incrementos posteriores ativam dados. O contrato
operacional existente permanece TCP v2/19 tipos, código 3 rejeitado e UDP OTT1;
nenhum formato, limite, fixture ou API de transporte é alterado neste Model.

## 3. Entidades e objetos de valor

| Conceito | Identidade e conteúdo | Invariante |
|---|---|---|
| Fluxo | streamId UUID e serverId esperado | UUID é chave; título não é chave; origem não muda por anúncio conflitante |
| Execução da fonte | serverId e serverEpoch UUID | Época partilhada pelas streams dessa fonte; UUID não ordenável |
| Proveniência de candidato | nodeId, instanceId e connectionId do vizinho | Substituição de sessão invalida callbacks antigos; IP não identifica candidato |
| RouteCandidate | fluxo, origem, época, proveniência, path, sequência, custo, saltos, prazo | Um candidato corrente por vizinho/stream; path simples fonte→nó local |
| RouteMetric | custo acumulado em µs e hopCount | Soma verificada; desempate por saltos e depois nodeId do upstream |
| Route | fluxo, candidatos e selecionado | Alternativas independentes; selecionado não significa confirmado para dados |
| Snapshot | fluxo, custo, upstream conhecido, destinos, estado e alternativas | Cópias defensivas transitivas; ausência de custo não é zero |
| Tempo local | instante monotónico e prazo de validade | Expiração local, testável sem sleeps; relógio civil apenas para evidência |

Uma sessão reparada e confirmada pelo overlay é elegível como proveniência.
A tabela não volta a exigir aresta no grafo inicial nem implementa admissão de peers.
Uma medição pertence à mesma sessão do candidato: nova conexão não herda RTT antigo.

## 4. Agregado e propriedade

RoutingTable é a raiz do agregado em memória do nó; contém rotas isoladas por
stream e metadados limitados de época por fonte. Cada Route conserva os candidatos
dos vários vizinhos, incluindo os não selecionados. Uma alteração de S1/A não
altera S1/B, S2/A ou S2/B, salvo a invalidação deliberada da época comum de S1.

Todas as mutações pertencem ao executor serial já existente (ADR-017). Não criar
outro executor, sockets, timers autónomos ou base de dados. O domínio recebe tempo,
métricas e eventos já correlacionados pelo owner; consumidores recebem snapshots,
nunca os mapas internos. A composição em OverlayNode/RoutingService não faz parte
dos seis ficheiros autorizados neste incremento.

## 5. Caminho, custo e seleção

1 - **Validar antes de alterar:** stream/origem/época, sessão atual, sequência válida,
    path simples e custo. Um anúncio termina no vizinho emissor; o armazenamento
    acrescenta o nó local. Rejeitar próprio ID no path recebido, repetições,
    início diferente da origem, fim diferente do emissor e limite excedido.
2 - **Limite do path:** no máximo 32 IDs no candidato já com o nó local; um
    anúncio com 32 IDs não pode originar candidato local com 33. Saltos são
    número de arestas do path, não número de IDs.
3 - **Fonte local:** path `[local]`, zero saltos/custo e upstream ausente. Origem
    remota não adquire custo zero por falta de medição.
4 - **Custo remoto:** custo anunciado + custo da ligação medido para a sessão
    atual. O monitor fornece EWMA; a tabela não volta a aplicar alpha. Sem amostra,
    candidato pendente/não selecionável. Com amostra, custo local mínimo 1 µs,
    conversão verificada e soma sem overflow de long.
5 - **Preferência:** menor custo acumulado, menos saltos, menor nodeId do próximo
    upstream. Ordem lexical determinística dos IDs ASCII, sem DNS ou IP. Não
    depende da ordem de chegada de candidatos distintos e conserva alternativas.
6 - **Mudança:** remoção/expiração do melhor reavalia os restantes válidos; não
    apaga a Route inteira nem os candidatos de outro vizinho ou stream.

Exemplos de oráculo, ainda NÃO executados: 100+40=140 contra 80+50=130 escolhe o
segundo; custo igual compara saltos e depois R1 antes de R2. Long.MAX_VALUE−1+1
é representável; +2 é rejeitado. Uma atualização inválida preserva a vista anterior.

## 6. Validade, sequência e épocas

Validade usa `routing.candidate.ttl.ms` existente (default 8000 ms), com tempo
monotónico fornecido ao domínio. Candidato expirado não é elegível, mesmo se a
limpeza física ocorrer depois. O Design deve definir a fronteira exata do prazo,
aritmética sem overflow e a forma de passagem do relógio, sem timer próprio.

Sequência, melhoria tardia e renovação precisam de contrato explícito: uma cache
booleana seen não pode impedir melhoria na mesma geração (ADR-004). As propostas
de Q-C-26 distinguem avanço de sequência, duplicado, melhoria e sequência antiga;
não são decisões aceites nem autorização para implementar flooding neste bolt.

Nova época validada da fonte invalida candidatos de todas as suas streams, sem
afetar outras fontes. Época retirada não pode reverter o estado por mensagem
atrasada durante o horizonte de anúncios. A tabela não deduz qual UUID é mais
recente. O futuro owner de routing valida o evento de mudança; o domínio aplica
a transição atómica e a memória limitada correspondente. Limites/retirada e
política de saturação são propostas pendentes de Q-C-26.

## 7. Estados e snapshots

Neste incremento só há transições efetivas UNAVAILABLE ↔ INACTIVE: sem candidato
selecionável fica UNAVAILABLE; com candidato válido ou oferta local fica INACTIVE.
Uma origem local tem custo zero e upstream nulo. Não inventar ligação local como
vizinho, prazo remoto para a oferta local ou atividade de vídeo.

O snapshot expõe o fluxo, custo e upstream conhecido quando existem, destinos
vazios e o estado. Preserva a distinção entre candidato selecionado e upstream
ativo/pendente: neste incremento os últimos não existem. Um futuro snapshot de
forwarding nunca deve autorizar dados só porque existe candidato. Estados
ACTIVATING/ACTIVE/SWITCHING pertencem ao modelo global, sem transições implementadas
aqui. O Design decidirá a forma Java compatível, mantendo este recorte explícito.

Paths, listas de alternativas e mapas publicados são imutáveis, inclusive objetos
aninhados. Uma vista obtida antes de outra atualização conserva os seus valores.
Não expor Connection, contadores mutáveis ou coleções internas num snapshot.

## 8. Operações, eventos e repositório

Contratos de domínio, sem fixar ainda assinaturas Java:

- registar/remover oferta local; admitir/substituir candidato validado;
- aplicar métrica da sessão atual; remover candidato por fluxo e sessão;
- invalidar sessão terminada; expirar candidatos no instante fornecido;
- aplicar mudança de época validada, com invalidação comum à origem;
- consultar rota/snapshot e alternativas sem mutação pelo consumidor.

Eventos lógicos: candidato admitido/atualizado/retirado/expirado, seleção alterada,
disponibilidade alterada e época substituída. São resultados de operações do owner,
não novos tipos wire, callbacks em threads ou um event bus. Um evento antigo de
sessão não remove o candidato da conexão substituta.

O único repositório é a tabela em memória; não há DAO, ficheiros de rotas nem
restauro no arranque. Leitura de snapshot não inicia rede, DNS ou trabalho assíncrono.

## 9. Recursos e falhas

Validações antecedem alocação/retenção. Os caps existentes de deployment (64 nós),
streams por origem (256) e path (32 IDs) continuam aplicáveis. O grafo não é uma
lista global de autorização para candidatos aprendidos; proveniência é a sessão.
Há no máximo um candidato por vizinho/stream. Todas as coleções, inclusive chaves
sem candidato e histórico de épocas, precisam de capacidade e política de limpeza.

`routing.cache.capacity=4096` limita chaves de flooding; não é automaticamente
limite de linhas da tabela. Q-C-26 propõe orçamento explícito e finito para o
agregado, detalhado em Design dentro dos ficheiros autorizados, sem nova chave
de configuração ou alteração de hard caps. Saturação deve ser visível e atómica,
preservando alternativas saudáveis e proteção de épocas ainda válidas.

## 10. Oráculos planeados — V-011

Todos os casos abaixo estão **NÃO EXECUTADOS**. Concretizar em RoutingTableTest,
com relógio/tempo controlado, métricas fornecidas e tokens de sessão; não precisam
de TCP real para provar o domínio. Não substituem a regressão futura do projeto.

| Caso | Prova e resultado esperado |
|---|---|
| M01 | Quatro streams de duas fontes, incluindo época comum por fonte; operações de uma stream não afetam as restantes |
| M02 | Fonte local: custo zero, sem upstream, destinos vazios, INACTIVE |
| M03 | Fixtures de custo conhecidas; escolha por soma e independência da ordem de inserção |
| M04 | Empate por saltos e nodeId, incluindo alternativa de igual custo |
| M05 | Path correto fonte→emissor→local; detetar loops, identidade/fonte/fim incorretos |
| M06 | Fronteiras de 32/33 IDs, custos negativos e soma Long.MAX_VALUE/overflow |
| M07 | Sem amostra não seleciona; chegada de métrica da sessão atual permite escolher; nunca zero remoto fictício |
| M08 | Atualizar candidato não apaga alternativa; remoção do melhor seleciona a alternativa válida |
| M09 | Expiração na fronteira do prazo; expirar um não remove outro; nenhum válido implica UNAVAILABLE |
| M10 | Nova sequência, duplicado e melhoria na mesma geração conforme Q-C-26; replay antigo não renova TTL |
| M11 | Epoch nova invalida só streams da mesma fonte; epoch retirada não reverte; UUIDs não são ordenados |
| M12 | Down/métrica/update da conexão antiga não altera candidato da nova; nova conexão não herda RTT |
| M13 | Snapshots e paths imutáveis, cópias de inputs; vista antiga estável após mutação da tabela |
| M14 | UUID com origem conflitante e entrada inválida rejeitados atomicamente, sem perda da seleção anterior |
| M15 | Limites de candidatos/chaves/histórico e saturação conforme Q-C-26, sem coleções ilimitadas |
| M16 | Seleção não cria upstream ativo, destinos, ACK, dados ou socket; só INACTIVE/UNAVAILABLE |
| M17 | Candidato de sessão reparada elegível sem exigir aresta inicial; remoção correlacionada por sessão |
| M18 | Consolidação futura: V-011, regressão atual, manual offline reproduzível, revisão humana e integração |

## 11. ADR, pendências e checkpoint

ADR-004 justifica path vector/custos/desempate; ADR-017, o owner serial e snapshots;
ADR-018, estado volátil e identidades; ADR-024, seis ficheiros e um IMP de cada vez.
Não foi demonstrada inviabilidade de uma decisão aceite nesta análise documental.
Q-C-26 regista concretizações ainda propostas, sem nova ADR aceite por inferência.

1 - ⏳ Aprovar/ajustar Model e decidir Q-C-26 antes de Design.
2 - [ ] Design: assinaturas/tipos, limites exatos, tempo/overflow, sem integração fora do âmbito.
3 - [ ] Revisão ADR antes de Implement; divergência material volta a decisão humana.
4 - [ ] Implement/Test: falhas reais, incrementais, clean verify JDK 21 e manual atual.
5 - [ ] Revisão final atribuída a Waheed e integração comprovada antes de DONE.

Sem gates externos adicionais. A atribuição de revisor não prova revisão efetuada.
V-ADD e diff-check desta etapa são apenas validação documental; não contam como
V-011, regressão Java, teste manual, aprovação do modelo ou fecho do bolt.
