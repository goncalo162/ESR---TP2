---
unit: "003-rotas"
bolt: "005-rotas"
stage: "model"
status: "in-progress"
created: "2026-11-12T14:25:37Z"
---
# IMP-007 — Model: RTT e falhas nas sessões TCP v2

⏳ Model para aprovação. Só esta etapa foi autorizada. Design, ADR, Implement e
Test não iniciados. V-010 está planeado, não executado. Nenhuma aprovação da
retoma cancelada é transportada para este documento.

## 1. Base, fontes e dependências confirmadas

HEAD/main e base operacional: `3dedf007f52f43f53eaf254bef7c25e35f986e5a`.
Entrada limpa; inventário/hashes em experiments/construction/005-rotas/model-entry.json.
Não houve checkout/reset nem modificação de fontes Java, testes ou artefactos anteriores.

Precedência: enunciado → specs → intents → documentação → código; ADD é o mestre
arquitetural. Consultados ADD 2/5/6/8–12, specs, requirements/system-context do intent,
unidade/história/bolt/TASK-010, contrato 07/3–7 e deltas ADR-033/034/035,
estratégia 09, DoD 14, progresso 15, standards e APIs reais de configuração,
protocolo, transporte, vizinhança/composição. A instrução explícita de inspecionar
código prevalece sobre a proibição genérica da skill em Model, sem autorizar escrita.

1 - ✅ IMP-006/004-overlay complete e integrado; TASK-009 DONE.
2 - ✅ IMP-035/035-overlay-extra complete e integrado; TASK-034 DONE. A aprendizagem
    IMP-034/034-overlay-extra também está complete na cadeia de dependências.
3 - ✅ Unidade 002-overlay complete, PHASE-02 DONE. Registos de aceitação/revisão/
    integração existentes são evidência histórica; não são execuções feitas neste Model.
4 - ⏳ 005-rotas e história estavam planned; unidade 003-rotas ready/inception concluída.
    Agora apenas Model em curso; nenhum critério funcional foi promovido a cumprido.

## 2. Âmbito, contexto e contrato operacional

O contexto de monitorização produz medição da ligação de controlo e identifica
sessões sem resposta. Não calcula rotas, não cria arestas nem substitui a reparação.
Únicos ficheiros Java previstos no ADD 8 para fases futuras:

- `Projeto/src/main/java/pt/uminho/esr/ott/monitoring/NetworkMonitor.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/monitoring/LinkMetrics.java`;
- `Projeto/src/test/java/pt/uminho/esr/ott/monitoring/NetworkMonitorTest.java`.

Os dois main existem com zero bytes; o teste ainda não existe. TimeSource na
TASK-010 representa injeção de tempo, não autorização para um quarto ficheiro.
A composição real é uma questão de âmbito aberta em Q-C-23; não se acrescenta
OverlayNode/NeighborManager/teste de integração fora desta lista por inferência.

Contrato atual: exclusivamente **TCP v2**, envelope version=2 e **19 tipos ativos**
1,2,4..20; **3 rejeitado**. PROBE_PING=7 e PROBE_PONG=8 conservam payload Probe:
probeId i64 não negativo, incluindo zero. Respostas preservam requestId por regra
expressa de docs/07 §4: PONG ecoa requestId e probeId do PING. O emissor do envelope
é o nó que responde, com a sua instanceId, nunca uma cópia da identidade do pedido.
Mensagens periódicas locais usam requestId novo. Não acrescentar timestamp no wire.

ADR-035 substitui apenas as cláusulas de reparação/admissão/máscaras identificadas
nas ADR-019/033/034: HELLO5/REPAIR_HELLO20/ACK6 exigem máscaras3. Descoberta18/19 e
reparação20 pertencem aos componentes já fechados; monitor não os reinterpreta.
Há um TCP persistente e um FIFO/writer por par; sondas usam a via normal do manager.
Framing 37..65536, UTF-8 estrito, códigos explícitos, limites v2/extra e UDP OTT1
64/1200/1136 permanecem intactos. TCP v1/OTD1 são história, não fallback operacional.

Fora: catálogo, candidatos/rotas por stream, anúncios/histerese/ativação, vídeo,
UDP/watchdog, CLI metrics, novos transportes, bootstrap periódico, persistência,
novos parâmetros e dependências runtime. RTT não prova capacidade de banda,
perda de vídeo ou atraso unidirecional. ADR-025 mantém a limitação UDP-only.

## 3. Entidades, objetos de valor e agregado

| Conceito | Identidade/dados | Invariante |
|---|---|---|
| Sessão monitorizada | Neighbor.Session(nodeId, instanceId, connectionId, endpoint) | Uma sessão confirmada atual por nodeId; não usar só ID ou IP |
| Sonda local | sessão completa, probeId, requestId, início monotónico e prazo | No máximo uma pendente por sessão, incluindo admissão ainda não concluída |
| Amostra RTT | duração local e µs explícitos | Valor não negativo, convertido sem overflow; custo mínimo positivo 1 µs |
| Histórico corrente de métrica | EWMA, sampleCount, último instante válido, missedCount | Sem amostra não há RTT confiável ou timestamp fictício |
| Política de monitorização | intervalo, timeout, limiar, alpha | RuntimeConfiguration.Monitoring já validado; defaults não são hard caps |
| Token de callback | sessão/sonda/geração/tipo de prazo | Timer/conclusão tardia não altera uma nova sessão ou sonda |
| Vista LinkMetrics | sessão, disponibilidade de amostra, EWMA/custo, idade e contadores | Objeto imutável; mapa/listas publicados com cópias defensivas |

O agregado é o monitor do nó, com coleção limitada de estados por sessão. O nó e
o manager continuam donos das conexões; o monitor não possui nem expõe Connection.
NodeId é estável; instanceId distingue arranque e connectionId distingue canal.
A identidade do emissor local vem da composição, não de valores remotos da sonda.

Sessões resultantes de HELLO configurado e de REPAIR_HELLO admitido pelo IMP-035
têm a mesma elegibilidade depois de confirmadas. Candidato aprendido, claim ou
vizinho anunciado por terceiros **não** é sessão direta e não recebe PING. Não
filtrar só pelo grafo inicial, nem criar estado para IDs arbitrários recebidos.

Repositório: apenas estado volátil limitado, sem DB/ficheiro persistente. Leituras
usam snapshots imutáveis; API Java concreta e representação numérica pertencem
a Design, após resolver as dúvidas e validar este Model.

## 4. Ownership e eventos de domínio

Um único executor serial por nó possui estado de vizinhança/monitorização
(ADR-017). Scheduler, transporte e conclusões de futures só oferecem eventos
limitados e não bloqueantes. Snapshot não é autorização suficiente para agir
mais tarde: revalidar Session/geração no dono serial antes de alterar/fechar.

| Entrada | Efeito conceptual / saída |
|---|---|
| NeighborUp confirmado | instalar sessão sem amostra, preparar elegibilidade de sonda |
| Momento elegível | criar uma pendência local e solicitar PING pela sessão atual |
| Resultado de envio | ACCEPTED confirma só enfileiramento; erros locais não viram amostra |
| PING válido recebido | responder PONG na mesma sessão, ecoando probeId/requestId; não criar sonda local |
| PONG correlacionado no prazo | concluir uma pendência, medir RTT, atualizar EWMA e publicar métrica |
| Prazo de sonda admitida | concluir uma perda, atualizar missedCount, eventualmente pedir fecho |
| NeighborDown/EOF/erro TCP | invalidar imediatamente a métrica corrente e cancelar a sonda |
| Sessão substituída | retirar tokens antigos; nova instância obriga reset de toda a métrica |
| Paragem local | retirar admissão de sondas/eco, cancelar compromissos e libertar recursos possuídos |

Eventos conceptuais de saída: MetricUpdated, ProbeTimedOut, MetricInvalidated,
SessionUnresponsive e pedido de disconnect da sessão exata. Não se introduzem
novos subtipos no Event sealed de NeighborManager nesta etapa. Demultiplexação e
observação mantêm Received/Diagnostic/RepairStarted e ERROR intactos para os seus
consumidores; não responder ERROR a ERROR nem descartar eventos alheios.

## 5. Máquina de estado da sonda e contagem de falhas

Estado conceptual do registo: SEM_AMOSTRA, MEDIDO, SUSPEITO, INDISPONÍVEL.
Pendência da sonda é ortogonal: sem sonda → admissão pendente → aguardando PONG
→ resposta/timeout/aborto. Não são enums Java nem novos estados do manager.

1 - Uma sessão tem no máximo uma sonda criada/admitida/aguardando; ticks repetidos
    não criam outra, nem contam perdas. PING remoto e seu eco são independentes.
2 - O início local tem de ser guardado antes de poder receber a resposta. A corrida
    PONG versus conclusão assíncrona de admissão não pode perder uma resposta
    válida nem publicar amostra de uma tentativa recusada; oráculo específico M12.
3 - Só PONG da Session corrente, com probeId e requestId esperados e prazo válido,
    altera sampleCount/EWMA/idade. Duplicado, desconhecido, de outro canal/arranque
    ou fora do prazo é ignorado para métricas e não reseta missedCount.
4 - Uma sonda admitida expirada conta **uma** falha, mesmo com vários callbacks.
    PONG válido põe missedCount a zero; mensagens de descoberta/ERROR/outros tipos
    não satisfazem PONG nem apagam as perdas da sonda.
5 - Default três timeouts consecutivos provoca uma invalidação/evento de falha e
    um pedido de fecho da sessão exata. Um timeout isolado não provoca neighborDown.
    RuntimeConfiguration permite outro limiar validado (1..Integer.MAX_VALUE):
    testar overrides; não fixar três como política não configurável.
6 - O pedido de fecho é assíncrono e condicionado à Session. Se entretanto já
    mudou, não fechar o substituto. Down posterior torna a invalidação idempotente.
    Novas tentativas e eventual reparação competem ao manager existente.
7 - EOF/Down da sessão corrente invalida assim que o evento é tratado no owner,
    sem aguardar três perdas ou o timer seguinte; callback Down antigo é inócuo.
    A palavra imediato refere-se ao processamento serial do evento, não a latência
    física zero ou deteção de silêncio UDP.
8 - Reinício (instanceId nova) apaga EWMA, sampleCount, missedCount e pendência;
    não restaurar RTT de outra execução. Reset também em nova conexão da mesma
    instância é proposta nova nesta Model, ainda por decidir em Q-C-24.

Sem amostra: sampleCount=0, EWMA/custo ausentes e idade da amostra ausente. Não usar
zero como substituto de desconhecido. Com amostra, SUSPEITO pode conservar o último
RTT datado e o número de perdas sem o apresentar como medição nova/saudável.
Após invalidação, custo não é utilizável por routing; não inventar valor alternativo.

## 6. Tempo, EWMA e cadência

Relógio monotónico local do emissor: RTT = tPong − tInicio. Não comparar timestamps
entre JVMs nem usar UTC/mediaTimeUs para RTT. A definição exata de tInicio e a
inclusão de filas estão propostas, não aprovadas, em Q-C-24. Tempo de tratamento
local pode incluir espera no owner; não chamar automaticamente RTT no fio a esse valor.

Defaults: intervalo 1000 ms, timeout 1000 ms, limiar 3, alpha 0.2. As durações
permitem 1..2147483647 ms; alpha finito em (0,1]. Usar os valores efetivos do
RuntimeConfiguration, já validados antes de sockets, sem nova propriedade.
Intervalo/timeout são independentes: mesmo intervalo menor que timeout não cria
mais de uma pendência. A próxima sonda só pode ocorrer depois da conclusão e de
atingida a elegibilidade do intervalo (ADD 6.3), sem bursts para recuperar ticks.

Primeira amostra r inicializa EWMA=r. Nas seguintes:
EWMA_n = alpha × r_n + (1−alpha) × EWMA_(n−1).
Oráculo independente: 1000 µs → 2000 µs → 3000 µs, alpha 0.2, produz
1000 → 1200 → 1560 µs. Nenhum destes valores é medição executada nesta sessão.
RTT zero por resolução tem custo mínimo 1 µs; valor negativo/incoerente não é
amostra. Design fixará precisão/rounding para o custo long sem NaN/overflow,
sem amostra negativa ou zero confiável. Idade é tempo local desde último PONG válido.

ProbeId admite 0..Long.MAX_VALUE pelo codec v2. Não embrulhar para negativo nem
reutilizar ID de modo a aceitar resposta antiga. RequestId UUID correlaciona
adicionalmente, com o eco já determinado por docs/07; não é aprovação herdada.
[PRESSUPOSTO, novo] Primeira sonda pode ser elegível ao Up; cadência seguinte
baseada no último início, sempre subordinada à conclusão da sonda corrente.
Esta concretização menor exige validação do Model antes de ser convertida em código.

## 7. Envio, limites e shutdown

As portas de domínio necessárias são: tempo/scheduler falsificáveis, despacho no
owner partilhado, eventos de sessão/mensagens, envio assíncrono normal, fecho
condicionado e publicação não bloqueante. APIs Java concretas não são desenhadas aqui.

Send ACCEPTED não é write/flush nem ACK. CLOSED/OVERFLOW/INVALID_MESSAGE/future
excecional não são resposta nem três perdas remotas: classificar e tornar a falha
local observável. Um overflow do transporte pode gerar Down real do handle, que
invalida normalmente; INVALID_MESSAGE local não deve culpar um peer saudável.
Não adicionar retry imediato de envio, outra Connection ou forma de obter o canal.
Design deverá fixar o desfecho de cada erro e de admission/PONG/deadline em corrida.

Estado: no máximo 63 sessões diretas e nunca mais que o limite efetivo de conexões;
no máximo uma sonda e compromissos lógicos limitados por sessão. Não usar a cache
128×4 de candidatos como se fossem 128 vizinhos monitorizáveis. Timers fisicamente
cancelados/em execução, eventos e confirmações contam até libertação real;
sem scheduler por peer, histórico de sondas ilimitado ou fila ilimitada de callbacks.
O desenho do orçamento físico de timers e de publicação ficará para Design.

PING/PONG são tráfego normal 7/8 no FIFO existente. Não mudar margem M=1, quota
extra de um frame enfileirado ou quatro tickets físicos, Q>=2 dos overlay,
backpressure/isolamento, máscaras3 ou caps por meio de configuração de monitor.
Componente não bloqueia callbacks com DNS, flush, espera de future ou logging.

Na paragem local: parar admissão antes de NODE_LEAVE/fecho, cancelar sondas e
invalidar tokens; não emitir falsas mortes de peers nem pedir disconnect por
cada sessão só porque o monitor para. Participar no deadline global restante,
sem renovar budget por recurso. Terminar fisicamente workers possuídos e reports
pendentes antes de alegar cleanup; daemon/cancel de future não provam terminação.
Owner/manager/transport emprestados não são fechados pelo monitor. A ordem de
instalação/remoção no nó atual depende da decisão de composição Q-C-23.

## 8. Composição encontrada e decisões em aberto

O código real oferece NeighborManager com Observer e Up/Down/Received/Diagnostic/
RepairStarted, snapshot imutável, send(Session, message), disconnect(Session)
e construtor com SerialOwner emprestado. A via normal aceita as sondas 7/8;
os tipos18/19 são tratados internamente pelo learner, sem serem apropriados pelo monitor.

OverlayNode cria owner e manager privados, compõe o observer existente e expõe
snapshots/discoveryStatus/candidates/recoveryStatus/failure e lifecycle. Não expõe
send/disconnect nem porta para instalar participante de monitorização com o mesmo
owner. OttApplication fornece um observer de logs. Não existe NetworkMonitor instalado.
São capacidades suficientes no nível manager para provar um componente embutível,
mas não uma ligação disponível à composição CLI nos três ficheiros do ADD.

[Q-C-23](../../../docs/construction-duvidas.md#q-c-23--composição-do-monitor-na-base-v2)
apresenta duas opções novas: extensão mínima e explicitamente autorizada de
composição/testes, ou componente embutível com integração posterior atribuída por
decisão. Nenhuma adotada. Não recorrer a reflexão, novo transporte ou owner paralelo;
não reabrir IMP-005/034/035, alterar APIs fechadas ou promover um teste com fakes
a demonstração de monitorização já ativa no programa.

[Q-C-24](../../../docs/construction-duvidas.md#q-c-24--origem-temporal-e-reset-da-métrica)
regista propostas novas de início local incluindo filas e reset por nova conexão,
e as alternativas com impacto. A resposta/aceitação cancelada não é autoridade.
Q-C-16/17 já existentes pertencem a EXTRA-01, não a esta retoma; não são renumerados.
Nenhuma ADR nova aceite/criada: se for preciso substituir decisão aceite, usar a
próxima ADR livre (após ADR-035) com aprovação, não reutilizar ADR-033.

## 9. Oráculos V-010 planeados — não executados

| ID | Resultado esperado / meio futuro |
|---|---|
| M01 | relógio falso: subtração local, unidade µs, sem relógio remoto/UTC |
| M02 | EWMA 1000/2000/3000→1000/1200/1560 com alpha0.2; alpha1 e overrides válidos |
| M03 | sem amostra não publica custo/idade fictícios; resolução zero custa pelo menos1 µs |
| M04 | uma pendência por sessão; ticks repetidos/atrasados não geram bursts/perdas múltiplas |
| M05 | PONG ignora probeId/requestId/canal/instância errados, duplicados e atrasados |
| M06 | três sondas expiradas geram uma falha/fecho, um timeout não; outros limiares configurados |
| M07 | PONG válido reseta perdas consecutivas; outros frames não as disfarçam |
| M08 | EOF/Down corrente invalida no evento, antes do próximo tick; Down antigo não afeta substituto |
| M09 | nova instanceId reseta tudo; PONG/timer/conclusão velha não fecha/apaga a nova sessão |
| M10 | reconexão mesma instância respeita decisão Q-C-24, sem apresentar medição antiga como nova |
| M11 | PING recebido tem eco de requestId/probeId corretos; sondas bidirecionais independentes |
| M12 | corrida PONG/admissão/deadline não perde resposta válida nem confirma tentativa recusada |
| M13 | ACCEPTED não prova escrita; CLOSED/OVERFLOW/INVALID_MESSAGE/exceção sem falsas perdas remotas |
| M14 | monotónico com wrap, conversão/overflow, limite do probeId e fronteira exata do prazo |
| M15 | 63 sessões, filas/timers/tickets físicos limitados e rejeição observável sem novas dependências |
| M16 | shutdown durante sonda/eco/fecho cancela gerações, preserva owner emprestado e budget restante |
| M17 | snapshots/cópias defensivas, sampleCount/idade/EWMA consistentes e sem NaN/negativos |
| M18 | ERROR/Diagnostic/RepairStarted e tráfego alheio preservados, sem ERROR de resposta a ERROR |
| M19 | sessões admitidas por HELLO5 e REPAIR_HELLO20 monitorizadas por igual, candidato sem sessão excluído |
| M20 | wire v2/19 tipos/3 rejeitado e UDP intactos; FIFO/quota extra/isolamento não regressam |
| M21 | composição e teardown conforme Q-C-23; sem segundo executor de estado/socket/CLI inventada |
| M22 | regressão/clean verify novos e teste manual/revisão Fillmore/integração nas etapas próprias |

Fakes de relógio, scheduler, transporte e resultados de envio permitem provar
EWMA, deadlines e corridas sem sleeps como oráculo. Testes reais futuros têm de
identificar se exercitam componente ou CLI; GUI/CORE/medições não são inferidos.
As execuções antigas de IMP-035 (1113 testes/manual) são históricas e não V-010.

## 10. Checkpoint

ADRs de monitorização 004/016/022/024/025, transversais de ownership/identidade/
recursos/erros e as substituições específicas 033/034/035 orientam o Model. Não
modificar ADR aceite, testes ou caps; não criar dependência runtime, CI, linter,
JaCoCo ou percentagem de cobertura (ADR-026), apesar de sugestões genéricas da skill.

Model preparado com entidades, valores, agregado, eventos, serviços/portas,
repositório volátil e vocabulário. **Parar para aprovação e decisões Q-C-23/24.**
V-010/regressão/manual/revisão final de Fillmore/integração continuam futuros;
responsável previsto Waheed. Não há gates externos adicionais para este IMP.
Sem Java, staging, commits, push ou avanço Design/ADR/Implement/Test.

## Model aprovado e decisões confirmadas

Registo em 2026-11-12T14:36:45Z. Model aprovado explicitamente; Q-C-23 opção1 e Q-C-24
aprovadas, incluindo elegibilidade no Up/cadência sem bursts. Os estados abertos
e propostas do corpo acima são o histórico apresentado no checkpoint anterior.
Só Design autorizado; ver decisões atuais em construction-duvidas.md.
Sem aprovação de Design/ADR/Implement/Test nem extensão além da composição/testes
ora autorizados. Nenhum fecho anterior é reaberto.
