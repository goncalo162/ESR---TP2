---
unit: "005-video"
bolt: "026-video"
stage: "model"
status: "complete"
created: "2026-11-28T14:52:44Z"
updated: "2026-11-28T15:06:27Z"
---
# IMP-019 — Model: fontes sob procura, cadência e ciclo de vida

**Model e Q-C-48/49 aprovados explicitamente pelo utilizador**, registo 2026-11-28T15:06:27Z.
Apenas Design autorizado. O checkpoint/propostas abaixo documentam a fase de Model.
Não se leu/analisou/alterou código de produção/testes; nenhuma API Java real
foi conferida. Design/ADR/Implement/Test não autorizados. Sem Maven, execução
de vídeo, alteração de dados/configuração, commits/push ou outro IMP.

## 1. Contexto, âmbito e dependências

Fonte normativa: [ADD/IMP-019](../../../add.md#imp-019), ADR-006/009/021/024;
coordenação pelo owner/identidades/fatal conforme ADR-017/018/020 e qualidade
conforme ADR-026. [História008](../../intents/001-ott-streaming/units/005-video/stories/008-emitir-fontes-com-pacing-e-procura.md)
e [TASK-020/C](../../../docs/tasks/PHASE-05/TASK-020.md). [Contratos](../../../docs/07-API-AND-INTERFACES.md)
e [dados](../../../docs/06-DATA-MODEL.md) são detalhe/proveniência, não outra arquitetura.

Responsabilidade: produzir frames de fontes locais de SERVER, com posição,
sequência/timeline e cadência independentes, exclusivamente sob procura
confirmada. Compor esses resultados e o lifecycle com a aplicação existente.
Não é um leitor novo: reutilizar o domínio de VideoSource aceite em IMP-018.

Três caminhos futuros do ADD, sem ampliar âmbito por inferência:

- `server/StreamingServer.java`;
- `server/StreamingServerTest.java`;
- `app/OttApplication.java`.

Helper/porto que venha a ser necessário deve caber nesses caminhos; interfaces
existentes, construtores/factories e tratamento fatal serão conferidos emADR.
Se for necessário alterar VideoSource, OverlayNode, RoutingService, codec,
configuração/protocolo ou outro ficheiro, apresentar evidência e proposta antes
de editar. Não se presume que os portos conceituais abaixo já existem em Java.

009-arvores/IMP-015 e025-video/IMP-018 estão complete e integrados na base.
O fecho de025 comprova22687 JPEGs completos, hashes e cleanup no leitor; não
comprova a futura emissão/cadência.1582 testes/zero falhas são baseline histórico,
não resultado novo. Os binários de teste não são instalação runtime de media.base.

Fora do âmbito: StreamForwarder/replicação/socket UDP de IMP-020, reassemblagem,
player/play de IMP-022, GUI, integração ponta a ponta e CORE. A saída do produtor
é uma entrega local de frame ao porto de dados: não constitui entrega remota.
Não simular essas funcionalidades nem anunciar streaming reproduzido.

## 2. Entidades e valores

| Conceito | Identidade/conteúdo | Invariante |
|---|---|---|
| Conjunto de fontes locais | serverId, serverEpoch da execução, definições configuradas |2..256 streams locais; época comum à origem, não por worker/loop |
| Publicação de fonte | streamId + origem + época, definição, estado, cursor, contadores | Um agregado por stream; título não é chave |
| Definição de vídeo | path local admitido, FPS1..60, loop, maxFrameBytes | Imutável na execução; nenhum path da rede; sem reload nesta tarefa |
| Procura efetiva | projeção de routing para stream/época, referências/destinos confirmados válidos | Derivada da autoridade atual; não é um contador paralelo de leases |
| Fonte de leitura | recurso VideoSource, posição física e geração de leitura | Um proprietário e um leitor sequencial por publicação |
| Frame preparado | JPEG validado e localização física do registo | No máximo um à espera; não confundir index do ficheiro com frameSeq emitido |
| Posição de emissão | próximo ordinal k/frameSeq, mediaTimeUs | Não recua em pausa/loop; nunca faz wrap |
| Cadência | relógio monotónico, próxima elegibilidade, fase fracionária | Um vencimento lógico por stream; atraso não cria dívida de envios |
| Reserva física | trabalho I/O/decode/fecho admitido e ainda não terminado | Cancelamento lógico não a liberta antecipadamente |
| Identidade de trabalho | publicação, geração de recurso e token de operação | Resultado/timer antigo não ressuscita stop, EOF ou fonte substituída |
| Entrega de frame | streamId, serverEpoch, frameSeq, mediaTimeUs, JPEG imutável | Metadata consistente; nenhum prefixo do ficheiro no payload |
| Indisponibilidade local | stream, razão/categoria, causa local e recurso pendente | Nunca enviar paths/stack traces no protocolo |

serverEpoch é fornecido pelo ciclo de execução da origem; este componente não
inventa uma época por stream para contornar rollover. Estado operacional não
é persistido. Os UUID não são ordenados como contadores.

## 3. Agregados, autoridade e fronteiras

Raiz **Publicação de fonte** possui estado lógico, cursor/posição, frame preparado,
cadência e identidade dos seus trabalhos. Raiz de composição **Conjunto de fontes**
impõe capacidade total e shutdown global, sem misturar contadores por stream.

1. O owner serial decide qualificação, disponibilidade, procura, elegibilidade,
   emissão, retirada e encerramento. Worker/timer comunica resultado correlacionado;
   não altera rotas, catálogo ou estado de outra stream diretamente.
2. Paths, leitura, validação JPEG, reabertura e fecho potencialmente bloqueantes
   ficam fora do owner. A forma executável, executor/limites e integração com
   fila limitada existente pertencem aDesign/ADR, não são presumidos neste Model.
3. Routing continua a possuir ACK, leases, revisões e destinos confirmados.
   Anúncio, rota selecionada, ACTIVATE pendente, referência expirada ou mera
   intenção de consumo não autorizam produção de dados.
4. Procura positiva agregada dá **um** produtor por stream, independentemente
   de quantos clientes/ramos confirmados existam. A replicação pertence ao
   consumidor de dados/IMP-020; não criar leitura/scheduler por cliente.
5. Antes de cada nova leitura normal e de cada entrega, revalidar procura
   efetiva, stream/época e lifecycle no owner. Sem procura efetiva, não entregar.
6. Handoff aceite não comprova envio UDP ou receção. Uma retirada posterior
   não desfaz bytes que outro componente já aceitou; futuras filas/destinos
   respeitam o contrato de forwarding de IMP-020.

## 4. Estados e transições

| Estado | Significado/saídas possíveis |
|---|---|
| PREPARING | Admissão e qualificação inicial limitadas; sem oferta disponível ou emissão |
| SUSPENDED | Fonte qualificada, sem procura efetiva; cursor e eventual frame preparado retidos |
| RUNNING | Procura confirmada; ler/preparar/entregar apenas dentro da cadência |
| REOPENING | EOF com loop=true; fechar recurso anterior antes de abrir/qualificar o seguinte |
| ENDED | EOF com loop=false; retirar só esta oferta, sem retry por ticks/novos clientes |
| FAILED | Falha terminal da publicação; retirada local, restantes fontes preservadas |
| CLOSING | Permissão de produzir revogada; recursos/trabalhos físicos ainda podem terminar |
| CLOSED | Fecho verificado; nenhum trabalho físico ou recurso ainda possuído |

PREPARING termina em SUSPENDED (qualificação válida), ou falha de arranque.
SUSPENDED→RUNNING exige procura atual confirmada. RUNNING→SUSPENDED ocorre
na última referência efetiva removida/expirada. RUNNING→REOPENING só após EOF
real e loop=true. REOPENING não é retry cego: uma reabertura/primeiro frame
inválido ou vazio termina só aquela publicação. ENDED/FAILED não se rearmam
por novo tick, anúncio remoto ou procura; restart/reconfiguração não é inferido.

Stop da intenção do cliente é removido em routing e pode apenas suspender o
produtor; não equivale a destruir a publicação. Stop administrativo do componente
revoga definitivamente a geração e entra emCLOSING. Resultados antigos apenas
limpam recursos, sem renovar disponibilidade nem marcar CLOSED sem prova física.

## 5. Q-C-48 — proposta: cadência, pausas, timeline e pressão

**Proposta para aprovação no Model; não é contrato já aplicado.**

### 5.1. Sequência e timeline

Para FPS F e ordinal de produção k≥0:

- frameSeq=k, começando em0 na época nova;
- mediaTimeUs=floor(k×1000000/F), começando em0;
- cada tentativa de entrega de um frame válido consome exatamente um ordinal,
  mesmo se o porto recusar por pressão; a recusa fica contada como descarte.
- Timer sem frame pronto, qualificação, ausência de procura e leitura pendente
  não consomem ordinal. Todos os fragmentos futuros daquele frame partilham
  a mesma metadata; fragmentação não está a ser implementada aqui.

A fórmula é matemática exata, sem multiplicação com wrap ou acumulação float.
Antes de esgotar o intervalo representável, terminar apenas a publicação com
razão explícita; nunca resetar sequência sob a mesma época. Nova época exige
ciclo de origem explícito; não reiniciar automaticamente a época comum e
invalidar as outras streams para recuperar um único contador.

### 5.2. Ritmo sem dívida

Um frame pronto pode ser entregue imediatamente ao entrar emRUNNING. Depois,
a próxima elegibilidade é ancorada no instante real da tentativa anterior.
O intervalo racional de1/F segundo mantém o resto para alternar as durações
inteiras necessárias; no regime pontual a soma não deriva por arredondamento.
Relógio civil nunca governa o pacing.

Um vencimento atrasado pode produzir **no máximo um frame**, não tantos quantos
os períodos decorridos. Reancorar a próxima elegibilidade no instante atual,
um período depois; não ler/descartar dezenas de frames para recuperar atraso.
Atraso não avança timeline nem cursor sozinho. Pode haver um frame pré-preparado
para não somar sistematicamente decode ao período, mas nunca uma fila de histórico.
Nada aqui prova FPS real sem medir a implementação futura.

### 5.3. Suspender e retomar

Ao perder procura: cancelar vencimento lógico, não iniciar novas leituras,
não entregar. Uma leitura física já iniciada pode terminar; se a publicação
continua válida, conservar no máximo esse único frame para retoma. Não fingir
que cancelamento do callback interrompeu o I/O ou desfez o cursor.

Na retoma, preservar recurso/posição, frame preparado, sequência e timeline;
ancorar de novo a cadência ao instante da retoma, sem dívida do intervalo parado.
O primeiro frame retoma a posição seguinte, sem seek ou reinício do ficheiro.
Se a geração foi parada/substituída, o resultado antigo não é reaproveitado.

### 5.4. Pressão na entrega

Porto indisponível/fila cheia recusa explicitamente e não bloqueia owner ou
outras fontes. Descartar aquele frame, consumir seu ordinal e continuar apenas
no próximo período normal, sem retry em ciclo nem retenção de backlog. Lacuna
em frameSeq é legítima; não equivale a entrega remota nem a falha da origem.
Sem procura, ao contrário da recusa, nenhum frame é entregue/consumido logicamente.

## 6. Q-C-49 — proposta: qualificação e ownership físico

**Proposta para aprovação no Model; sem leitura de media nesta etapa.**

### 6.1. Qualificar antes de disponibilizar

A TASK-020 exige validar ficheiro/JPEG antes da oferta e suspender leitura/envio
sem procura. Propõe-se distinguir qualificação de arranque da leitura normal:

- Admitir o path e validar **um primeiro frame** por fonte, fora do owner,
  antes de anunciar disponibilidade. Isto é uma exceção inicial limitada à
  qualificação, não leitura contínua sem procura nem emissão.
- Reter esse frame como o primeiro a produzir; não o saltar, não avançar
  frameSeq/mediaTimeUs e não o reler para fabricar a prova. Sem procura, não
  ler o registo seguinte. O cursor físico já fica após esse primeiro registo.
- Ficheiro inicialmente vazio é rejeitado como fonte publicável, embora o
  leitor de IMP-018 aceite EOF limpo numa entrada vazia. Não enfraquecer M03.
- Qualificação não é auditoria integral do ficheiro; corrupção posterior é
  observada quando se pede esse registo. Não prometer que um frame bom valida
  todos os seguintes nem ignorar limites de VideoSource.
- Falha de admissão/qualificação inicial impede publicar o conjunto incompleto:
  reportar erro de arranque de recurso conforme CLI, tentar limpar todos os
  recursos já adquiridos, conservando o primeiro erro. Não abrir uma origem
  parcialmente válida como se o arranque tivesse sido integralmente aceite.

Esta distinção é material e precisa de aprovação. Se for rejeitada, resolver
explicitamente a compatibilidade entre pré-validação e proibição de leitura
sem procura; não escolher outra política silenciosamente emImplement.

### 6.2. Orçamento e posse

S≤256 publicações locais: no máximo um recurso sequencial e um frame preparado
por stream, uma leitura física emcurso por stream e um vencimento lógico.
O Design proporá um teto global finito W para trabalhos bloqueantes simultâneos,
com1≤W≤S, admissão/fairness e filas limitadas. W não é uma chave nova aprovada
nem autorização para criar threads por frame ou trabalho acumulado por tick.

Reserva física só termina quando o trabalho/recurso efetivamente terminou,
mesmo após timeout/cancelamento lógico, pausa, reabertura ou stop. Gerações novas
não contornam o orçamento partilhado nem admitem uma segunda leitura sobre o
mesmo VideoSource. Sem histórico crescente de frames, jobs ou gerações antigas.
As cópias comprimidas, raster e overhead não são uma quota total16MiB: esse limite
continua exclusivamente o raster destino de IMP-018, não toda a JVM/decoder.

### 6.3. EOF e falhas

loop=true: só reabrir depois de EOF ter fechado o recurso anterior e a sua tarefa
física terminar. Preservar frameSeq/mediaTimeUs e a época da origem; apenas o
índice/offset físico do ficheiro regressa a0. Reabertura tem nova identidade de
recurso para rejeitar callbacks/timers antigos. Se não há procura, deferir a
reabertura/leitura; não fazer loops de ficheiro no vazio. Um ficheiro reaberto
vazio/inválido termina com falha localizada, não spin de EOF/reopen.

loop=false: depois do último frame, EOF retira apenas a oferta daquela stream,
termina a sua cadência e fecha recursos. Não regenerar ANNOUNCE como disponível
por um timer antigo; outras streams da mesma serverEpoch permanecem saudáveis.
Se a descoberta do EOF ficou suspensa por ausência de procura, só a próxima
leitura autorizada o deteta; não sondar o ficheiro continuamente sem procura.

Erro checked esperado de leitura/formato emexecução retira só aquela oferta;
propagar contexto local limitado e não mascarar como EOF. Falha inesperada
RuntimeException/Error continua fatal pelo caminho existente da aplicação;
não convertê-la em JPEG inválido ou deixá-la escondida num resultado de worker.
Cleanup tenta cada recurso distinto uma vez, mantém o primário e os secundários,
sem autosupressão; reutiliza, não substitui, o contrato aceite de VideoSource.

### 6.4. Encerramento

Primeiro revogar permissão de leitura/entrega e cancelar agendamentos lógicos;
depois limpar/aguardar recursos fora do owner, dentro do **prazo global existente**
do shutdown, não um prazo completo por stream. Resultados tardios apenas libertam
suas reservas, nunca disponibilizam fonte após quit ou fecham uma fonte nova.

Não bloquear indefinidamente aguardando I/O/decoder que não retorna. Se o prazo
se esgotar com recursos ainda vivos, reportar terminação incompleta pelo lifecycle
existente; não declarar CLOSED ou capacidade libertada por simples cancelamento.
O mecanismo concreto e compatibilidade com tratamento fatal/CLI são para ADR.

## 7. Eventos, serviços e portos conceituais

| Evento | Informação/efeito |
|---|---|
| FonteQualificada | Definição, recurso/geração e primeiro frame retido; disponibilidade só no owner |
| ProcuraMudou | Snapshot atual confirmado; habilita ou suspende, não duplica produtor |
| Vencimento | Identidade, prazo monotónico e token; sozinho não autoriza dados |
| LeituraConcluída | Frame/EOF/falha e identidade física; owner revalida antes de usar |
| EntregaTentada | stream/época/k/timestamp e admitida/recusada; uma decisão por ordinal |
| FonteIndisponível | EOF/falha local; retirar só essa oferta e parar o seu trabalho |
| FechoPedido/Verificado | Geração revogada, recursos físicos pendentes/terminados e prazo global |

Serviços de domínio: qualificar uma fonte, projetar procura efetiva, decidir
próxima elegibilidade, reservar/produzir ordinal, tratar EOF/loop, reconciliar
resultados antigos e encerrar conjunto. Portos conceituais: leitura sequencial
VideoSource, relógio/scheduler controláveis, owner, projeção de procura,
disponibilidade local e entrega finita de frame. Sem repositório persistente.

Documentação anterior descreve projeção de forwarding e setLocalAvailable,
mas **não foram conferidas as APIs reais**. EmDesign/ADR verificar acesso ao
owner, snapshot confirmado, capacidade de adiar a oferta até qualificação,
retirada de só uma stream, propagação de fatal, lifecycle/construtores e fecho.
Se houver impedimento nos três caminhos, apresentar proposta antes de código.
A porta de entrega pode ser controlada nos testes; isso prova o produtor,
não um StreamForwarder/UDP ainda por implementar em outro IMP.

## 8. Oráculos propostos — P01–P20 / V-020-C, nenhum executado

| Caso | Observação requerida |
|---|---|
| P01 | Qualificação de fontes antes de oferta; um primeiro frame retido sem emissão/avanço lógico; papel nãoSERVER não cria produtor |
| P02 | ANNOUNCE/seleção/ACTIVATE pendente/lease expirado não permitem entrega; ACK/referência efetiva válida permite |
| P03 | Vários clientes/ramos da mesma stream mantêm um único cursor/produtor; retirada de um ramo não pausa se resta outro |
| P04 | Relógio controlado e FPS1/3/10/60 conferem cadência racional e timestamps sem drift acumulado |
| P05 | Vencimento cedo, duplicado ou de token antigo não emite nem cria nova leitura/job |
| P06 | Atraso por muitos períodos gera no máximo uma entrega e uma elegibilidade futura, sem burst ou fast-forward de ficheiro |
| P07 | Suspensão deixa de ler/entregar; conclusão física já iniciada cabe num único frame retido, sem consumir ordinal |
| P08 | Retoma mantém payload/posição/seq/timeline e reancora relógio, sem acumular tempo parado como dívida |
| P09 | Procura/época muda durante leitura ou antes da entrega; revalidação impede emissão nãoautorizada e callbacks não ressuscitam stop |
| P10 | Duas streams com FPS/loop/posições distintos progridem independentemente e preservam serverEpoch comum |
| P11 | Loop de um/dois frames conserva seq/timeline; EOF fecha antes de reabrir e não faz spin no vazio |
| P12 | loop=false retira só a oferta terminada; outra stream e o nó mantêm serviço |
| P13 | Vazio/prefixo/JPEG inválido na qualificação recusa arranque, com primário e recursos adquiridos preservados/limpos |
| P14 | Erro de leitura ou reabertura emexecução afeta só a publicação; não há EOF falso nem retry ilimitado |
| P15 | Porto de entrega recusa por pressão; uma tentativa, ordinal consumido, descarte contado e próxima tentativa só na cadência |
| P16 | Saturação do orçamento físico/callbacks/caches permanece finita, sem segundo leitor, fuga de reservas ou starvation causada por retry em ciclo |
| P17 | Stop com I/O/cleanup pendente não liberta capacidade antes de terminar; close único e resultado antigo não fecha recurso novo |
| P18 | Shutdown de várias fontes partilha prazo global; timeout lógico não é terminação física nem CLOSED falso |
| P19 | Erros checked/Runtime/Error e cleanup secundário conservam identidade/primário; fatal chega ao tratamento existente da aplicação |
| P20 | Overflow de seq/timeline/prazo termina sem wrap; composição do SERVER só entrega ao porto com procura; nenhum play/UDP/GUI é alegado |

Esperados calculados independentemente do scheduler/produtor a construir.
Relógio, owner, leitor e entrega controlados demonstram ordens/limites; a integração
com a aplicação usa os mesmos contratos e testes históricos preservados.
O teste de boundary de dados não substitui rede/vídeo real de IMP-020/023.

## 9. Questões e checkpoint

Q-C-48 e Q-C-49 são **propostas novas**, a aprovar/ajustar no Model; sem aprovação
presumida de semântica nova. Não criam gate externo. Design concretizará APIs,
capacidades/quotas, ownership, ordem dos callbacks e equações com overflow; ADR
conferirá esses elementos nos fontes reais só quando for autorizada.

Model preparado, current_stage=model, stages_completed=[], completed=null;
história008 planned. P01–P20/V-020-C não executados. Os1582 testes/22687 JPEGs
anteriores permanecem evidência de IMP-018, não verificação deste produtor.

Manter código/históricos/negativos, remoções da demo e R17 original com causa
ainda desconhecida. A exceção de execução delegada de025 é apenas daquele
fecho; não dispensa automaticamente manual humano aplicável deste bolt.
Fecho exige testes/regressãoJDK21, manual aplicável antes da aceitação deTest,
revisão final Denji e integração; TASK-020 só apósA/B/C. Sem novos gates externos,
Design/Implement, commits/push ou início de outro IMP nesta autorização.
