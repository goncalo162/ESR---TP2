---
unit: "004-arvores"
bolt: "024-arvores"
stage: "model"
status: "complete"
created: "2026-11-21T14:13:30Z"
updated: "2026-11-21T14:21:52Z"
---

# Model — IMP-014: troca confirmada de parent com histerese

## Aprovação do Model

Model e Q-C-45/46 aprovados explicitamente pelo utilizador; registo 2026-11-21T14:21:52Z.
Autorizada apenas a etapa Design. Texto de proposta/checkpoint abaixo preservado
como histórico da revisão; não declara Implement ou V-017 executado. A versão
original e os hashes estão congelados em design-01/approved-model-input.tar.gz.

## Estado, fontes e fronteira

**Proposta para revisão humana; Model não aprovado.** Apenas esta etapa foi
autorizada pela opção1. Q-C-45/46 abaixo são propostas, não decisões aceites.
Design/ADR/Implement/Test não iniciados; V-017 não executado.

Fonte normativa: [ADD/IMP-014](../../../add.md#imp-014), ADR-005/016/024 e
parâmetros da secção6.3. Complementos: [TASK-017](../../../docs/tasks/PHASE-04/TASK-017.md),
[dados](../../../docs/06-DATA-MODEL.md), [interfaces](../../../docs/07-API-AND-INTERFACES.md)
e [validação V-017](../../../docs/09-TESTING-STRATEGY.md).
008-arvores/IMP-013 e022-rotas/IMP-010 estão concluídos; os seus contratos de
recuperação, intenção, métricas, lineagens e reservas físicas são preservados.

O contexto é a árvore de uma stream já pedida, com parent confirmado ainda
válido e uma alternativa cuja melhoria de custo persiste. Selecionar uma rota
ou anunciar uma melhoria não autoriza forwarding. Falha efetiva do ramo atual
continua a pertencer à recuperação de IMP-013 e não espera histerese.
Sem streaming UDP novo, GUI, watchdog UDP, bootstrap adicional, persistência,
protocolo/configuração nova ou início de IMP-015.

O âmbito futuro de código conserva exclusivamente:

- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`;
- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RouteMetric.java`;
- `Projeto/src/test/java/pt/uminho/esr/ott/routing/RouteSwitchTest.java`.

A referência legada a NetworkMonitor em TASK-017 não amplia o ADD. Não foram
lidas, analisadas ou alteradas fontes Java, nem executado Maven. A viabilidade
nas APIs reais será conferida apenas na etapa autorizada para essa leitura;
um impedimento será apresentado antes de qualquer ampliação de âmbito.

## Entidades e raiz do agregado

| Entidade | Informação de domínio | Regras |
|---|---|---|
| Árvore por stream, raiz | origem fixa, época corrente, intenção admitida, ramo confirmado, qualificação e eventual troca pendente | Um owner serial; isolamento entre streams; uma única operação de ativação da árvore, coordenada com recuperação/renovação já existentes |
| Ramo confirmado | Session completa, endpoint, caminho confirmado, subscrição/revisão e prazo do grant | Autoridade para forwarding, distinta da seleção; expiração, morte de Session ou revogação efetiva não podem ser ocultadas pela troca |
| Alternativa observada | identidade de caminho, geração de anúncio, custo válido e proveniência da observação | Sem autoridade de dados; não usar anúncio expirado, ciclo, sessão antiga, custo ausente ou soma não representável |
| Qualificação da alternativa | identidade estável, última prova contada, contador e âncora de permanência | Um registo coalescido por árvore; consecutividade não é uma lista ilimitada de amostras |
| Troca planeada | identidade lógica, ramo antigo capturado, alternativa, prazo global e tentativas consumidas | Até uma pendente; não substitui a confirmação antiga antes de confirmação nova válida |
| Interesses e grants dos filhos | intenção local, consumidor local, filho/Session/subscrição/revisão e validade remanescente | Preservar intenção admitida; não transferir uma confirmação incompatível para outro prefixo por cópia |
| Reserva física | envio/continuação ainda não concluído, incluindo gerações anteriores | Partilhada entre troca e recuperação; cancelamento e timeout lógicos não libertam capacidade física |

Os membros pertencem à mesma árvore; não se propõem uma segunda tabela de
forwarding, um segundo motor de retry ou um armazenamento duradouro de rotas.
No máximo um ramo é publicado como confirmado por stream. O ramo novo pode
estar em ativação enquanto o antigo ainda fornece serviço: essa sobreposição
temporária de controlo/tráfego não representa dois parents confirmados no
snapshot local nem permissão para duplicar saídas estáveis para filhos.

## Objetos de valor

| Objeto | Igualdade e limites |
|---|---|
| Identidade de caminho F | stream, origem, época, Session completa (nó/instância/ligação) e path simples; mudar qualquer componente invalida a sequência de qualificação |
| Geração G | announceSeq validado dentro de F; não ordenar UUID de época ou usar versão global de snapshot como novidade |
| Custo comparável | custo acumulado inteiro em µs e proveniência válida ao mesmo instante monotónico; ausência/overflow não são zero nem infinito elegível |
| Política P/O/D | percentagem mínima, número de observações consecutivas, permanência mínima; parâmetros centrais já documentados |
| Prova de observação | atualização real e validada de métrica ou geração nova de anúncio; identidade suficiente para não contar a mesma entrada duas vezes |
| Confirmação | Session/path/subscrição/revisão/requestId e validade remanescente; ACK novo não confirma pedido antigo ou outro trace |
| Orçamento de operação | T/A existentes e deadline monotónico ancorado na admissão; não renovado por callback, nova adesão, ANN ou troca de candidato |
| Prova bloqueada | F/G e watermark consumidos no terminal de uma troca falhada; libertação de capacidade ou oscilação de elegibilidade não apagam o bloqueio |

## Política de melhoria — proposta Q-C-45

Defaults normativos: `routing.switch.improvement.percent=20`,
`routing.switch.observations=3`, `routing.switch.min.dwell.ms=5000`.
Domínios: P inteiro0..100, O inteiro1..2147483647, D1..2147483647ms.
Não criar overrides novos ou alterar defaults.

1. Comparar a alternativa elegível com o custo **atual do caminho confirmado**,
   incluindo a métrica local atual de cada ligação. Não comparar duas vezes a
   rota selecionada, nem usar o custo histórico do ACK como custo atual.
   Ambos os custos têm de estar disponíveis e válidos no mesmo instante de
   avaliação. Se não for possível obter essa comparação, não iniciar troca por
   qualidade nem deduzir perda do grant saudável.
2. Com custos C atual e N novo, exigir N<C e `100×(C−N) ≥ P×C`, como relação
   matemática exata, sem overflow ou arredondamento de percentagens. P=0 ainda
   exige melhoria estrita; P=100 só aceita N=0 com C>0. A fonte de custo0 não
   tem parent a melhorar. Hops/nodeId apenas desempata candidatos; empate de
   custo não satisfaz histerese.
3. Contar uma observação quando uma entrada genuína de métrica validada ou
   um ANN validado de geração nova permita a comparação atual. Valores iguais
   em medições realmente distintas podem contar; uma leitura repetida, tick,
   callback, duplicado de ANN ou reapresentação do mesmo evento não conta.
   Um evento que atualiza mais de uma projeção só conta uma vez por árvore.
   A primeira comparação válida conta uma vez, sem exigir alteração numérica.
4. O mesmo F alternativo deve cumprir o limiar em O observações consecutivas.
   Mudança de F/alternativa, insuficiência da melhoria, custo ausente/inválido,
   expiração ou invalidação reiniciam a sequência. G novo do mesmo F pode
   continuar a sequência. Regressar à elegibilidade após invalidação não
   ressuscita provas antigas. O contador satura em O, sem guardar histórico
   ilimitado, incrementar por timer ou transbordar.
5. Proposta explícita: ancorar D na primeira confirmação e em cada troca
   confirmada, incluindo recuperação que efetivamente mudou parent. Não
   reancorar em renovação do mesmo ramo, começo/falha da tentativa ou mudança
   de seleção. Antes de D, observações podem qualificar a alternativa; ao
   atingir D, só usar essa qualificação se continuar válida e contemporânea.
   O despertar não cria amostras. No instante exato D, a permanência está
   satisfeita; um grant expirado nesse instante já não é saudável.

Exemplo futuro de oráculo: C=1000, N=801 não qualifica; N=800 qualifica no
limiar de20%. Três entradas distintas qualificam, mas não produzem ACTIVATE
antes de D e da admissão limitada. O retorno isolado de custo inválido para
válido não reutiliza uma prova já bloqueada após falha.

## Troca, falha e procura — proposta Q-C-46

| Situação | Efeito de domínio |
|---|---|
| Sem procura admitida ou fonte local | Não ativar alternativa; seleção/catálogo não são procura |
| Parent saudável; melhoria ainda não qualificada | Conservar confirmação; atualizar apenas observação limitada |
| O/D satisfeitos e admissão possível | Iniciar uma troca limitada; conservar ramo antigo enquanto realmente válido |
| ACK novo compatível e envio admitido | Revalidar operação, procura, Session/época/path, deadline e grant; trocar confirmação atomicamente e só depois podar revisão antiga capturada |
| ACK antecipado, atrasado ou de outro pedido | Não publicar confirmação antes da barreira de envio/admissão; após cancelamento/terminal não ressuscitar operação |
| Erro/timeout da alternativa; parent antigo válido | Terminar troca, conservar serviço/intenção no ramo antigo e aplicar backoff/prova bloqueada |
| Parent antigo deixa efetivamente de ser válido | Remover a sua autoridade; recuperação IMP-013 ignora P/O/D, preservando T/A/capacidade e intenção |
| Última procura cancelada | Não efetivar troca; cancelar lógica e podar o que foi submetido, conservando reservas físicas até conclusão |
| Shutdown | Não admitir nova troca; incluir pendências físicas no fecho existente, sem novo executor ou prazo global extra |

Uma tentativa de ativação nova pode consumir até A envios dentro de T global,
reutilizando a disciplina de IMP-013 (defaults T=3000ms/A=3). Uma confirmação
não pode estender retroativamente o prazo global nem o lease que suportou um
filho. ERROR de ativação continua separado de trace e de renovação; recusa de
seleção divergente não é revogação automática da confirmação antiga.

**Backoff proposto para qualidade:** após falha terminal, reiniciar qualificação,
bloquear a prova F/G já observada e impedir nova troca por qualidade durante T
a contar do terminal. Depois disso, exigir prova útil posterior (novo F válido
ou ANN validado com G acima do watermark bloqueado) e nova sequência de O
observações; o novo ANN pode ser a primeira. Capacidade disponível, métrica que
fica inválida e volta, callbacks e timers não desbloqueiam essa prova. Sem
fila de retries, novo parâmetro ou ronda espontânea ao terminar o backoff.
A cadência mínima existente por stream também se mantém. Este backoff não
atrasa recuperação por perda real do parent; os seus bloqueios e créditos
continuam regidos por IMP-013/Q-C-44 e pela correção R10 já aprovada.

Uma segunda melhoria durante a troca não abre outra operação nem renova T/A.
Conservar apenas a visão atual necessária à revalidação, sem fila de candidatos.
Se o alvo deixa de ser compatível, não efetivar confirmação obsoleta; manter o
ramo antigo válido e reavaliar posteriormente segundo as mesmas regras. A
política exata de correlação/revisões fica para Design, sem cancelar grants de
outros contextos. Se o parent antigo falhar durante a troca, aproveitar uma
tentativa pendente só se ainda cumprir o contexto e orçamento originais;
qualquer recuperação subsequente usa a disciplina existente, nunca dois
conjuntos de reservas nem reinício artificial dos prazos dessa tentativa.

Ao mudar o prefixo confirmado, os filhos afetados têm de renegociar
automaticamente a partir da intenção ainda admitida. Não copiar uma confirmação
de prefixo antigo para um novo, prometer continuidade para além do prazo antigo
ou exigir novo pedido humano. Cada grant antigo só vale enquanto todas as suas
condições continuam verdadeiras; o novo tem confirmação própria e validade
limitada pelo remanescente a montante. Child stop, cancelamento antigo e ACK de
revisão anterior não removem o ramo/revisão nova nem reabrem a procura cancelada.

## Eventos, serviços de domínio e acesso ao estado

| Evento conceptual | Informação necessária / efeito |
|---|---|
| Observação válida/invalidada | stream, F/G, custos e proveniência; continuar ou reiniciar qualificação |
| Alternativa qualificada | política, contagem e âncora; possibilidade de admissão, sem autoridade de dados |
| Troca iniciada | operação, antigo/alvo, custos, razão, T/A; correlação auditável |
| Troca confirmada | confirmação nova e contexto antigo capturado; publicação única e poda posterior |
| Troca recusada/expirada | contexto, razão e watermark; preservação condicionada do ramo antigo e backoff |
| Parent perdido / procura removida | recuperação imediata ou poda conforme intenção remanescente |
| Envio físico concluído | libertar apenas a reserva correspondente; nunca criar procura/crédito por conclusão |

Política de melhoria avalia elegibilidade/P/O/D; coordenação da árvore realiza
transições no owner serial; projeção produz snapshot imutável de seleção e
forwarding separados. São responsabilidades conceptuais, não novas classes ou
assinaturas Java aprovadas. Custos comparados, motivo, início/fim e duração da
troca devem ser observáveis com correlação, usando os mecanismos existentes.

Contratos conceptuais de acesso: consultar candidatos e respectivas gerações/
validades, confirmação corrente, procura e reservas; publicar confirmação
atómica; observar relógio monotónico; enviar controlo assíncrono correlacionado.
O estado permanece volátil e limitado pelas capacidades já aprovadas. Não se
propõe repository persistente, thread, monitor, transporte, fila ou histórico
global adicional. Design concretizará armazenamento/custos e ADR conferirá APIs,
construtores/projeções e lifecycle nos três caminhos do ADD.

## Vocabulário

**Seleção** é preferência calculada, não grant. **Confirmação** é autoridade
temporária correlacionada. **Qualificação** é melhoria sustentada demonstrada
por observações distintas. **Permanência** limita trocas por qualidade após
confirmação; **backoff** limita nova tentativa depois de falha. **Recuperação**
responde a perda efetiva e ignora histerese. **Prova bloqueada** não volta a ser
nova por uma oscilação de métrica. **Poda** remove o ramo/revisão capturado,
nunca uma confirmação posterior por coincidência de stream ou vizinho.

## Oráculos planeados — V-017/S01–S18, não executados

| ID | Cenário e resultado esperado |
|---|---|
| S01 | RTT ruidoso junto de20% interrompe consecutividade; sem oscilação |
| S02 | Limiar exato20%, O=3 e D=5s: troca apenas quando todas as condições se verificam |
| S03 | P=0/100, O=1 e fonte custo0; melhoria sempre estrita, sem parent artificial |
| S04 | Custos grandes/soma inválida: comparação exata sem overflow, custo ausente não tratado como zero |
| S05 | Medições distintas de mesmo valor contam; duplicados/leituras/ticks/callbacks não multiplicam observações |
| S06 | Mudança F, TTL, falta de métrica ou melhoria insuficiente reinicia sequência; G novo válido do mesmo F pode continuá-la |
| S07 | Primeira confirmação/troca ancoram D; renovação/falha não reancoram; fronteiras exatas de tempo |
| S08 | Confirmado difere da seleção: custo contemporâneo do ramo certo; sem procura/fonte não há troca |
| S09 | Até ACK novo válido, snapshot conserva parent antigo saudável; depois troca atómica seguida de poda capturada |
| S10 | ACK antes da conclusão do envio, erro de envio, requestId/Session/época/path errado ou deadline atingido não confirmam indevidamente |
| S11 | Nova ativação falha: mantém ramo antigo válido, bloqueia prova e respeita backoff/T/A |
| S12 | Após S11, invalidar/restaurar métrica ou libertar callback não reabre; ANN posterior útil e nova qualificação permitem retry limitado |
| S13 | Falha real do parent antes de D ou durante troca: recuperação sem esperar histerese, sem duplo orçamento ou extensão da tentativa antiga |
| S14 | Dois candidatos melhores concorrentes: uma troca por árvore; atualização/ACK obsoleto não troca para contexto inválido |
| S15 | Child stop/último consumidor durante troca: sem ressurreição; outros interesses e revisões novas preservados |
| S16 | Prefixo novo: filhos renegociam sem ação humana e sem estender grants antigos; não há confirmação copiada |
| S17 | Reservas físicas presas entre gerações, cancelamento e shutdown: capacidade partilhada e terminação limitada pelo lifecycle existente |
| S18 | Streams/origens isoladas; integração TCP pertinente, ERROR de ativação/trace/renovação separados; silêncio UDP não cria reroute |

Esta tabela define cobertura de domínio a concretizar; não são testes escritos,
execuções ou aprovação. Futuras verificações devem usar relógio/owner/transporte
controlados e integração TCP pertinente, preservando testes históricos, negativos,
asserções e prazos. Regressão JDK21, manual humano, revisão final **Waheed** e
integração continuam necessários antes de fecho. Não há gate externo adicional.

R17 inicial de IMP-013 continua **sem causa estabelecida**. O sucesso da
integração com métricas controladas não demonstra a causa desse negativo.
Artefactos, hashes e resultados anteriores não são alterados por este Model.

## Checkpoint

Rever este Model e [Q-C-45/46](../../../docs/construction-duvidas.md).
Apenas após aprovação humana e autorização da etapa seguinte se poderá passar
a Design. Até lá: `current_stage=model`, `stages_completed=[]`, história planned/
implemented=false, V-017/S01–S18 não executados; sem Java/Maven/commit/push.
