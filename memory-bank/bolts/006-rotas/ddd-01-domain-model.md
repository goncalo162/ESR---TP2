---
unit: "003-rotas"
bolt: "006-rotas"
stage: "model"
status: "complete"
created: "2026-11-14T11:47:13Z"
---
# IMP-009 — Model: anúncios, catálogo e rotas

✅ Model aprovado pelo utilizador; registo 2026-11-14T12:31:20Z. Q-C-28 Opção1 aprovada;
princípios Q-C-29 aprovados, concretização em Design para revisão. Só Model
concluído; V-012 não executado. O corpo abaixo preserva o estado histórico da
apresentação inicial; a decisão posterior encontra-se no fim deste documento.

## 1. Base, autoridade e dependências

Base limpa `164ac0dc477c02bc7fde816c761d137ee18ea55d`, ramo Denji; main e Denji
coincidem. [Inventário e hashes de entrada](../../../experiments/construction/006-rotas/model-entry.json).
Nenhuma aprovação/revisão do IMP-008 é transferida para este incremento.

1 - ✅ IMP-006/004-overlay e unidade002-overlay complete; overlay de controlo disponível.
2 - ✅ IMP-008/021-rotas complete, TASK-011 DONE; implementação3b7aa745 e fecho164ac0d
    ancestrais de main. Os seis hashes revistos e o JAR364704bytes/SHA256
    366210cf8c7656ad2948ae1c842796632085aaa4f09afcedcd00a49db0a0ecee conferem.
3 - ✅ IMP-007/005-rotas complete, dependência transitiva: contrato de métricas por sessão.
4 - ⏳ IMP-009/006-rotas e história003 iniciados só em Model; unidade003/PHASE-03
    permanecem em progresso. Os1211 testes do predecessor são resultado histórico.

Precedência: enunciado → specs → intents → documentação → código; ADD mestre.
Consultados PDF ESR-TP2-26-27, specs/REQ005/006/009/012/014/016, requisitos e
contexto do intent, ADD2/4–6/8–12, ADR004/018/024 e transversais017/020/021/026,
docs06/07/09/14/15, história/unidade/TASK-012 e a revisão ADR publicada do IMP-008.
O PDF exige tabela por fluxo e múltiplas fontes/conteúdos; este incremento aprende
e publica metadados/caminhos, sem afirmar entrega de vídeo ou ativação de árvores.

Respeitada a restrição DDD de Model: **nenhum texto de fonte Java lido/analisado**.
Apenas inventário/tamanhos e hashes de bytes. APIs concretas são referência dos
contratos e revisão anterior; verificação nova de código fica para etapa autorizada.

## 2. Âmbito autorizado e lacuna de composição

Os quatro caminhos do ADD/IMP-009 existem com zero bytes:

- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/streaming/StreamInfo.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/streaming/StreamCatalog.java`;
- `Projeto/src/test/java/pt/uminho/esr/ott/routing/RoutingServiceTest.java`.

Fixtures de grafos podem ser dados literais internos ao teste; esta frase não
autoriza um quinto caminho de recursos ou outro teste por inferência.

A [revisão do IMP-008](../021-rotas/adr-review.md) documentou que OverlayNode não
encaminha Updated/Invalidated do monitor para routing nem oferece uma subscrição
de métricas/owner pública. Não presumir que o serviço se liga sozinho ao programa.
Esta é uma lacuna documental de composição, não uma falha Java reproduzida agora.

**Q-C-28 propõe**, antes de Design/código, ampliar a allowlist com
`overlay/OverlayNode.java` e `overlay/OverlayNodeRoutingTest.java` (main/test), para
composição e integração testável no mesmo owner/lifecycle. Nenhum desses dois
caminhos é já autorizado; o ADD não foi alterado. A alternativa é manter quatro
caminhos para um núcleo com portas controladas e planear explicitamente a
composição posterior, sem declarar que a aplicação já anuncia streams.

Fora: CLI catalog/routes/trace (IMP-010), procura/ACK/leases/histerese/recuperação
da árvore, encaminhamento UDP/vídeo, player, novas arestas (IMP-035 já fechado),
persistência, novas dependências/configuração/wire ou alterações à RoutingTable.
Snapshots de seleção continuam sem upstream confirmado/destinos de dados.

## 3. Linguagem e entidades

| Conceito | Identidade/conteúdo | Invariante |
|---|---|---|
| StreamInfo de domínio | UUID da stream, serverId, título, codec, FPS e serverEpoch | Origem fixa por UUID; título não é chave; imutável e validado |
| Oferta local | StreamInfo configurado no SERVER | Publicada mesmo sem consumidores; path=[serverId], custo0 |
| Execução da fonte | serverId/serverEpoch | Época comum às duas streams da fonte; UUID não ordenável |
| Sessão vizinha | nodeId/instanceId/connectionId/endpoint validado | Só sessão confirmada atual; IP não identifica proveniência |
| Anúncio recebido | StreamInfo, sequência, custo anunciado e path até ao emissor | Contexto/path/fonte válidos antes de alterar estado |
| Candidato | stream/vizinho + sessão/época/seq/validade | Mantido pela RoutingTable aprovada, incluindo alternativas e pendentes sem RTT |
| Entrada de catálogo | Metadados e validade/estado de acessibilidade | Derivada de ofertas/candidatos vivos, nunca só de um binding histórico |
| Geração de flooding | streamId/serverEpoch/announceSeq | Distingue streams da mesma época; identidade da geração não é requestId |
| Registo de reenvio | geração, melhor informação publicada e destinatários/sessões | Retém comparação, não apenas seen booleano; limitado/expirável |
| Intenção de saída | stream/sessão/geração/payload corrente | Rate limit e armazenamento finitos; cancelamento lógico não prova término físico |
| Snapshot de serviço | Catálogo e vistas de seleção imutáveis | Uma vista anterior permanece estável; não dá autorização de dados |

Origem no catálogo é o servidor; Origem na tabela do enunciado corresponde ao
upstream conhecido, sem confundir qualquer deles com um upstream ativo de dados.

StreamInfo de domínio não é o record de payload `ControlMessage.StreamInfo` nem
obriga a copiá-lo no protocolo. O adaptador conserva os valores sem alterar bytes.
Título até128 pontos Unicode/512bytesUTF-8, codec/FPS e UUIDs seguem docs07;
nenhum path de ficheiro de vídeo entra no catálogo ou na rede.

## 4. Agregado e fronteiras

RoutingService coordena um contexto de routing no owner serial do nó: ofertas
locais, metadados por fluxo/fonte, cache de flooding, estado limitado por saída
e a RoutingTable já concluída. StreamCatalog é a projeção consultável em memória,
não um catálogo central HTTP, base de dados ou novo serviço distribuído.

O primeiro anúncio válido de um UUID remoto ainda desconhecido pode estabelecer
o binding à origem declarada, após validação de contexto/path; binding local já
configurado prevalece. Depois, UUID não é reatribuído nem por expiração do catálogo.
É uma confiança de laboratório em peers admitidos, não prova criptográfica da fonte.

[PRESSUPOSTO] Configuração não é recarregada durante a execução: título/codec/FPS
de uma oferta não mudam espontaneamente dentro da mesma época. Design deve
concretizar conflitos de metadados antes de implementar; não escolher silenciosamente
o último anúncio contraditório nem misturar campos de duas fontes/épocas.

O serviço reutiliza custos opcionais de LinkMetrics.costMicros() diretamente,
sem segunda EWMA/conversão. SUSPECT com amostra conserva custo; sem amostra não
há zero confiável. O owner traduz sessão completa e chama a tabela. No instante
de cada operação, a validade é monotónica/local; não depende de relógio civil.

Não se introduz outro executor de estado. Scheduler/escrita só devolvem eventos
ao owner emprestado, com callbacks limitados/não bloqueantes. Detalhes e injeções
ficam para Design. A composição real depende da decisão de Q-C-28.

## 5. Eventos e receção

Eventos de domínio: serviço iniciado; oferta local publicada/retirada; NeighborUp/
Down; métrica atualizada/invalidada; anúncio/retirada recebidos; prazo atingido;
publicação admitida/recusada; época substituída; serviço a parar/terminado.
São eventos locais, não novos tipos de protocolo.

Para ANNOUNCE9, validar sessão atual e emissor, UUID/origem/época, metadados,
seq não negativa, path simples que começa na fonte e termina no sender, sem nó
local e com espaço para o acrescentar dentro de32IDs. Custo anunciado inválido
ou soma não representável não substitui estado saudável. Root/table mantém as
guardas aprovadas; o serviço não as contorna nem recalcula custo com zero fictício.

**Deduplicar reenvio não é rejeitar candidato alternativo.** Mesmo quando a geração
já foi vista por outro vizinho, a entrada válida daquele emissor deve chegar à
tabela; só a decisão de publicidade pode ser suprimida. Uma segunda chegada
melhor pode alterar seleção/publicidade; uma alternativa pior pode ficar guardada.

WITHDRAW10 retira apenas o candidato do emissor correlacionado. Reavaliar as
alternativas antes de decidir publicidade; não remover a stream dos restantes
vizinhos ou a outra stream/fonte. Não reenviar cegamente uma retirada como se
fosse ordem de apagar toda a origem. A origem só deixa de aparecer no catálogo
quando não há oferta/candidato vivo que a sustente, conforme cada fluxo.

RoutingService consome os tipos9/10. Eventos/mensagens de outros protocolos,
incluindo ERROR legítimo destinado a outro consumidor, conservam a entrega ao
observador responsável. Nunca ERROR em resposta a ERROR nem fecho global por
conteúdo de routing rejeitado localmente. Identidade/framing e respetivos erros
terminais continuam sob os contratos já aceites.

## 6. Geração, publicidade e Q-C-29

As fontes exigem sequência crescente na origem e reenvio de primeira chegada/
melhorias estritas de cada geração (ADR004). Não há autorização para inventar uma
sequência global nova num relay só porque o custo mudou. Uma cache booleana ou
cache que se esqueça da sessão destinatária não satisfaz as entradas tardias.

**Q-C-29 propõe, para decisão:** conservar a geração da fonte nos relays;
publicar o path/custo realmente selecionados com a sequência desse candidato,
sem atribuir a um path antigo a sequência de outro anúncio; retirar publicidade
que perdeu validade/foi ultrapassada por pior informação na mesma geração e
aguardar geração mais recente da fonte para a repor. Alternativas locais permanecem.
Não enviar ANNOUNCE da mesma geração após WITHDRAW ao mesmo peer: Q-C-26 fixa
um watermark que o recusaria. Comparações e ordens de saída serão oráculos de Design.

Cache cheia, rate limit e fila de transporte cheia são fenómenos distintos.
Q-C-29 também propõe: conservar entradas protegidas até ao prazo, recusar reserva
nova de cache com diagnóstico explícito e preservar candidatos/catálogo válidos;
coalescer publicidade pendente num orçamento limitado e, se faltar reserva,
descartar apenas essa tentativa de publicação com diagnóstico. Nova geração pode
voltar a tentar, sem loop de retry imediato ou promessa de convergência sob overload.
Estes detalhes não estão aprovados; capacidades/algoritmo exatos ficam para Design.

Em carga admissível, a publicidade de uma geração estabiliza num grafo fixo.
Isso **não significa silêncio permanente**: as ofertas periódicas da fonte
renovam candidatos por gerações novas enquanto não houver retirada/paragem.

## 7. Catálogo, épocas e snapshots

Catálogo consultável contém UUID, título, origem, época, metadados e validade.
Binding preservado na tabela não mantém uma entrada visível para sempre. Um
candidato sem RTT pode sustentar metadados ainda válidos, com estado de rota
UNAVAILABLE/custo ausente, distinguindo conhecido de utilizável. Oferta local
válida tem custo0. Não expor referências mutáveis ou Connection em snapshots.

Nova época é validada pelo owner e aplicada explicitamente à tabela; só depois
de confirmada pode invalidar metadados/caches antigos de todas as streams daquela
fonte. Se a mudança for recusada por CAS/histórico/capacidade, preservar o conjunto
anterior, sem publicar catálogo novo com rotas antigas. Não ordenar UUIDs.
Depois de uma época nova legitimamente aceite, uma falha de admissão do seu
candidato não autoriza restaurar a época antiga: reportar indisponibilidade.

O catálogo publica uma vista coerente após o turno serial; Design deve conferir
as validações/reservas e APIs necessárias para este compromisso sem presumir uma
transação distribuída entre componentes. Época retirada é protegida no horizonte
limitado aprovado; não prometer proteção eterna contra mensagens arbitrariamente atrasadas.

## 8. Recursos, lifecycle e contratos preservados

Q-C-26 mantém64 origens×256 bindings, N=min(C,63) candidatos/stream, oito épocas
retiradas/origem, alocação por necessidade e saturação sem evicção protegida.
Defaults atuais: anúncio2000ms, candidato8000ms, cache16000ms, capacidade de
flooding4096, até10 reenvios/segundo/stream/vizinho. Overrides validados continuam
a ser parâmetros, não alteração de wire/política/hard caps. A cache de flooding
é distinta dos bindings e watermarks da tabela; não trocar as suas capacidades.

Design concretiza limites de metadados, cache, destinatários, pendências, tickets,
timers e trabalho por turno; não criar um timer ou fila ilimitada por mensagem.
Não reter uma lista de todas as gerações passadas ou todos os requestIds enviados.
Callback antigo compara sessão, stream/epoch/seq e geração de lifecycle antes
de tocar no estado atual. Reserva física de envio/timer não é libertada só por
cancelar intenção ou completar future lógico.

Na paragem, cessar novas publicações/renovações, invalidar callbacks antigos e
libertar os recursos próprios dentro do prazo global restante do nó. Não fazer
joins no owner nem fechar o owner emprestado. Ordem da composição/rollback e
retirada best effort ficam explícitas em Design se Q-C-28 for aprovada.
ACCEPTED continua a significar admissão, nunca entrega, flush ou ACK remoto.

Sem novo transporte, tipo ou byte: TCP v2/19 tipos1,2,4..20;3 rejeitado;
framing37..65536; UTF-8 estrito; HELLO5/20/ACK6 obrigatórios; UDP OTT1
64/1200/1136 intacto. Bootstrap apenas inicial; nenhuma consulta periódica,
registo vivo, lease ou tentativa de descobrir rotas nele.

## 9. Oráculos planeados — V-012, todos NÃO EXECUTADOS

Modela-se tempo/transporte/custos controlados e grafos literais, com expectativas
independentes da implementação. TCP real e composição (se autorizada), regressão
atual e manual reproduzível terão prova separada nas etapas próprias.

| Caso | Oráculo a concretizar |
|---|---|
| M01 | Duas fontes com duas ofertas cada anunciam sem consumidores; path/custo de origem corretos; quatro UUIDs no catálogo |
| M02 | Losango S1→R1→C com custos100+40=140 e S1→R2→C com80+50=130; segunda chegada melhor seleciona130, mantendo140 |
| M03 | Empates por saltos/nodeId e cache de geração não suprimem alternativa ou melhoria tardia |
| M04 | Grafo cíclico, paths com local/repetições rejeitados; após drenar uma geração estável não há reenvio infinito; períodos novos continuam distintos |
| M05 | Retirar um fluxo ou a fonteS1 não apaga metadados/candidatos válidos deS2; duas streams da mesma fonte partilham epoch, não estado por UUID |
| M06 | Geração já vista não impede admissão por outro vizinho; cache de flooding difere da coleção de candidatos |
| M07 | Candidato sem RTT permanece conhecido/não utilizável; custo direto atual habilita seleção sem renovar TTL |
| M08 | Origem/UUID/metadados/sender/path conflitantes não substituem estado saudável; binding persiste após desaparecer catálogo |
| M09 | Prazo candidato/cache, duplicados e melhorias não estendem geração; sem candidatos/oferta remove metadados visíveis |
| M10 | WITHDRAW só do emissor, seq antiga/sessão antiga ignoradas; preservar alternativa e respeitar watermark de geração retirada |
| M11 | Nova época explícita troca juntas as streams da fonte; antiga protegida não reverte; saturação do histórico mantém fonte anterior |
| M12 | Conexão nova recebe publicidade válida sem bloqueio pelo seen da anterior; callbacks antigos não alteram métricas/catálogo/saídas |
| M13 | Mais melhorias que o rate limit permitem só quota admissível e pendência coalescida limitada; payload expirado não é emitido depois |
| M14 | Cache/pendência/fila saturadas, custos com overflow e erro local produzem diagnósticos limitados, sem evicção saudável ou retry infinito |
| M15 | Caminho pior/invalidado após publicação e WITHDRAW seguido de ANNOUNCE: sequência/geração obedecem à decisão de Q-C-29 |
| M16 | Snapshot de catálogo/rotas imutável e coerente; vista antiga estável, sem estado de dados ACTIVE |
| M17 | Peers aprendidos/reparados são vizinhos correntes elegíveis, sem revalidar aresta inicial ou chamar bootstrap |
| M18 | Partida/paragem/rollback com relógio e envios controlados; callbacks/timers antigos inócuos e terminações físicas verificadas |
| M19 | Tipos9/10 usam codec/FIFO existentes;3 rejeitado, v2/UDP e testes anteriores preservados; outros eventos mantêm entrega ao consumidor |
| M20 | Composição conforme Q-C-28 (se aprovada): mesmo owner, métricas reais encaminhadas, TCP e shutdown dentro do budget global |
| M21 | Metadados e sessões respeitam os caps existentes; reservas antes de alocar, sem máximos pré-alocados |
| M22 | Consolidação futura de V-012, regressão atual, manual humano, revisão final atribuída a Denji e integração antes de DONE |

## 10. Decisões e checkpoint

ADR004/018/024 orientam path vector, identidades/volatilidade e âmbito atómico;
017/020/021/026 preservam ownership, falhas locais, fronteiras e qualidade.
Não foi demonstrada inviabilidade de uma ADR aceite nesta leitura documental.
Q-C-28 é uma lacuna de âmbito e Q-C-29 pede concretização de política, sem
aprovação ou nova ADR por inferência. Se for necessária substituição, aplicar ADD11.

1 - ⏳ Aprovar/ajustar Model e decidir Q-C-28/29; não ultrapassar a allowlist atual.
2 - [ ] Design: assinaturas/injeções/limites/ordens concretos e compatibilidade das APIs.
3 - [ ] Revisão ADR antes de Implement; nenhuma implementação iniciada por este modelo.
4 - [ ] V-012/regressão atual/manual humano/revisão final e integração para fecho.

Sem gates externos adicionais. Revisor de origem Denji atribuído, não revisão
realizada; a substituição por Luis no IMP-008 não se aplica automaticamente aqui.
Sem Java, Maven, sockets, commits, push ou início de outro IMP nesta etapa.

## Aprovação posterior e transição apenas para Design

Registo 2026-11-14T12:31:20Z; utilizador aprovou Model, Q-C-28 Opção1 e princípios Q-C-29.
ADD/IMP-009 agora autoriza seis caminhos com OverlayNode/OverlayNodeRoutingTest,
no owner/lifecycle existentes. [Design](ddd-02-technical-design.md) concretiza
limites/ordens, incluindo alternativa pior, retirada+geração nova, callbacks
antigos e saturação; propostas concretas ainda para revisão. Nenhum Java alterado.
ADR/Implement/Test, V-012, regressão atual, manual humano antes de aprovar Test,
revisão final Denji e integração permanecem pendentes. Não houve fecho.
