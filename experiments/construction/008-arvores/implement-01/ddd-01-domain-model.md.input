---
unit: "004-arvores"
bolt: "008-arvores"
stage: "model"
status: "complete"
created: "2026-11-20T10:52:56Z"
updated: "2026-11-20T11:07:54Z"
---
# IMP-013 — Model de recuperação após perda do upstream TCP

✅ Model e Q-C-43/44 aprovados explicitamente pelo utilizador em 2026-11-20T11:07:54Z.
Apenas Design autorizado; texto de propostas/checkpoint abaixo conserva a
apresentação anterior. Não é aprovação de Implement nem execução V-016.

⏳ Model preparado para revisão; Q-C-43/44 propostas, não aprovadas.
A seleção explícita da opção 1 autoriza apenas Model. Nenhuma etapa está
concluída; Design/ADR/Implement/Test não iniciados. Este documento é uma proposta
de domínio, não evidência de implementação, APIs verificadas ou execução V-016.

## 1. Contexto delimitado e fontes

Recuperação da árvore de procura de uma stream quando o seu upstream confirmado
deixa de ser utilizável. Preservar intenção legítima, selecionar um caminho
confirmável até à mesma fonte e restabelecer autorização após ACK válido.
Sem alternativa, publicar indisponibilidade e conservar apenas a intenção que
continua legítima. Não pressupor réplica do conteúdo noutra origem.

Fonte normativa: [ADD](../../../add.md), IMP-013, secções6/9/11 e ADR-005/020/024/025.
Foram consultados [specs](../../../specs.md), requisitos do intent, unidade004,
história003, [TASK-016](../../../docs/tasks/PHASE-04/TASK-016.md),
[modelo geral](../../../docs/06-DATA-MODEL.md),
[contratos](../../../docs/07-API-AND-INTERFACES.md), V-016/DoD e índice de decisões.
ADR-004/017/018/019 e CP-02/03 complementam seleção, owner, épocas e regresso.
As decisões Q-C-34/35/40/41/42 e os fechos anteriores continuam válidos.
Nenhuma ADR é criada, substituída ou considerada inviável por esta proposta.

Dependências022-rotas/IMP-010 e023-arvores/IMP-012 concluídas e integradas;
último fecho [IMP-012](../023-arvores/closure-walkthrough.md).
Base observada40592da, árvore/índice limpos antes do início. Os1379 testes e o
manual Francisco30 são evidência histórica de IMP-012, não testes deste Model.

Só documentação foi lida nesta etapa. A regra Model da skill proíbe leitura,
análise e alteração de fontes: não se inspecionaram Java, testes ou fundações.
Os nomes de campos e operações abaixo são conceitos de domínio; compatibilidade
real e assinaturas terão de ser conferidas na etapa autorizada para tal.
O template foi localizado no diretório global de specsmd e adaptado a este
projeto sem HTTP, DB ou persistência de rotas.

Âmbito futuro previsto pelo ADD, sem o executar nem ampliar:

1 - routing/RoutingService.java: coordenar recuperação no proprietário serial.
2 - routing/Route.java: valores/projeções compatíveis do domínio de routing.
3 - routing/RecoveryTest.java: oráculos novos de V-016.

TASK-016 menciona RoutingTable.java/FailoverTest.java como detalhe legado;
prevalecem os três caminhos do ADD. Qualquer necessidade de alterar codec,
NeighborManager, monitor, transporte, CLI, configuração, testes históricos ou
outro caminho será apresentada antes de aplicar; não é autorizada por inferência.
Não se inicia adaptação por melhoria de RTT/IMP-014, vídeo, GUI ou CORE.

## 2. Entidades de domínio

1 - **Árvore de procura por stream:** raiz do agregado operacional; contém
intenções locais, ramos filhos, eventual confirmação a montante e uma operação
lógica de recuperação corrente. Uma única referência agregada upstream serve
todos os consumidores legítimos da mesma stream. A oferta da fonte e o catálogo
não criam procura por si só.
2 - **Intenção local de consumo:** identidade do consumidor e da sua operação
para um streamId/origem fixos. Sobrevive à indisponibilidade dentro da execução
até cancelamento explícito. Um resultado SUCCESS passado não é autorização
atual; o resultado de um ticket não é reescrito para simular a recuperação.
3 - **Interesse de ramo descendente:** pedido de um filho ligado a uma Session,
stream/época e lineagem de subscrição. Distingue interesse ainda admissível de
grant autorizado. Session morta, cancelamento ou prazo esgotado retiram o
interesse remoto; nunca é intenção duradoura equivalente ao consumidor local.
A conservação limitada após perda do parent é a proposta Q-C-43, secção5.
4 - **Confirmação upstream:** identifica Session/endpoint, origem/época, path,
correlação e lease que autorizam o ramo agora. A perda desta autoridade torna
inutilizável a confirmação publicada, independentemente do resultado de tickets
antigos ou da presença de um candidato no catálogo.
5 - **Incidente de recuperação:** perda efetiva da autoridade upstream de uma
árvore com procura legítima. Guarda a identidade local da geração afetada e a
razão; eventos repetidos da mesma falha não criam incidentes/rounds paralelos.
É uma identidade local opaca, não ordenação de UUID remotos ou nova mensagem.
6 - **Ronda de recuperação:** operação lógica com início, prazo total finito,
limite de tentativas, tentativa corrente e resultado terminal. Uma tentativa
captura um candidato/Session/path/época; a seguinte não herda o seu ACK.
Não se define aqui algoritmo de agendamento ou valores novos de capacidade.
7 - **Envio físico pendente:** reserva separada da operação lógica. Cancelar,
expirar ou substituir a recuperação não significa que bytes/callback deixaram
de existir. A reserva só se liberta ao terminar callback e continuação, nos
limites e no shutdown existentes, preservando M07 e IMP-012.

## 3. Objetos de valor e identidades

- **Chave de fluxo e origem:** streamId com vínculo ao serverId. Outra origem
  não é alternativa para o mesmo UUID, mesmo que tenha título/conteúdo igual.
- **Época de origem:** serverEpoch admitida pela política existente de épocas;
  nova época requer confirmação própria e invalida autoridade antiga daquela
  fonte. Nunca comparar UUIDs numericamente, aceitar época retirada ou fabricar
  epoch no relay. As restantes origens mantêm o seu estado.
- **Session de vizinho:** nodeId + instanceId + connectionId + endpoint confirmado.
  Mesmo nodeId após reconexão/reinício não conserva subscrição, grant ou recibos
  de uma Session anterior. O estado da negociação sem Session não é ACTIVE.
- **Caminho esperado:** sequência simples desde serverId até ao nó solicitante,
  com prefixo compatível em cada salto. Exclui repetição de IDs, retorno ao nó
  local e candidato cuja Session, época ou validade já não são correntes.
- **Identidade de operação:** stream/época, Session, espécie de pedido,
  subscriptionId/revisão/requestId e geração local corrente. Reutiliza as regras
  de lineagem de IMP-011/012: não rodar IDs para contornar replay, não aceitar
  revisão antiga, não confundir ACK12 de ACTIVATE11 com renovação14 ou trace15.
- **Validade:** início e duração monotónicos locais; grants novos ancorados no
  pedido, limitados pelo remanescente a montante. ACK tardio, duplicado, replay,
  callback ou espera em fila não renovam a validade nem arredondam fração de
  milissegundo para um grant indevido.
- **Resultado de recuperação:** confirmado; indisponível por ausência de rota,
  rejeição, prazo ou recursos; cancelado; terminado por shutdown. Razões locais
  distinguem estes resultados sem novos códigos ERROR ou estados wire.

## 4. Agregados, autoridade e invariantes

A árvore por stream é o limite de consistência. Seleção/catálogo, confirmação e
operação pendente são visões diferentes; um candidato selecionado não autoriza
forwarding. As decisões relativas a uma árvore são serializadas pelo owner
existente; eventos vindos de I/O/timer só produzem efeitos quando admitidos nesse
contexto. Não há outro executor, serviço concorrente ou proprietário de rotas.

1 - Ao perder a Session confirmada ou expirar efetivamente a sua autoridade,
retirar a autorização dependente imediatamente na decisão do owner. Não esperar
histerese, sucesso de DEACTIVATE ou libertação de um envio antigo.
2 - Um anúncio retirado/expirado, seleção divergente, melhoria de métrica,
PATH_CHANGED/timeout de renovação ou ausência de bytes UDP não retiram por si
só um grant ainda saudável e válido. Preservar Q-C-35/41; a troca por melhoria
com parent saudável pertence a IMP-014.
3 - Candidatos da Session perdida deixam de ser elegíveis; não apagar alternativas
de outras Sessions nem fontes independentes. Os mecanismos de anúncios, TTL,
métricas, sessões e épocas existentes continuam a ser a autoridade para esses dados.
4 - Uma tentativa de recuperação confirma só após ACK totalmente correlacionado
e a barreira de envio exigida por IMP-011/012, com intenção, Session e validade
ainda correntes. ACCEPTED, HELLO_OK, NeighborUp, anúncio ou flush isolados não
equivalem a confirmação da árvore.
5 - Recuperação confirmada tem um único upstream por stream. A confirmação velha
não continua ativa depois da perda; a confirmação nova não renova/copia grants
filhos cujo prefixo ou época ficaram incompatíveis.
6 - Sem procura legítima, não iniciar/manter uma ativação de recuperação. Oferta,
catálogo, histórico terminal e callback tardio não são procura. Última retirada
ganha à recuperação; cancelar operação antiga não remove intenção substituta.
7 - Sem alternativa confirmável, publicar UNAVAILABLE com razão observável;
não fabricar ciclo, conteúdo replicado, autorização ou espera PENDING infinita.
Conservar a intenção admissível é diferente de conservar um future sem prazo.
8 - Nova Session/época exige nova negociação segundo a lineagem aplicável. Eventos
da Session/geração retirada só podem concluir limpeza física correspondente;
nunca reativar árvore, repor consumidor cancelado ou apagar o ramo substituto.
9 - Manter isolamento por stream/origem. A perda de um vizinho afeta só árvores
que dele dependiam; streams confirmadas por outras Sessions mantêm ramo e leases.
10 - Preservar recursos e memória limitados. Não criar pool de retries/históricos
ilimitado, evictar proteção de replay saudável para recuperar, nem libertar uma
reserva física antes de callback e continuação terminarem. Saturação é explícita
e localizada, não motivo para derrubar o nó ou outras árvores.
11 - Shutdown cancela a recuperação lógica e impede novos envios, preservando o
primeiro prazo global e as reservas físicas até terminação. Sem novo orçamento
por tentativa, por stream ou por subsistema.
12 - Na fonte local não há upstream para recuperar. Perder um filho retira o seu
ramo segundo IMP-012; não elimina catálogo/oferta ou consumidores independentes.

## 5. Q-C-43 proposta — interesse dos filhos durante a recuperação

O ADD exige preservar intenção de consumo, não subscrição de conexão morta,
mas é necessário explicitar o filho cuja Session continua viva quando o parent
do relay falha. Proposta para aprovação no Model:

- Conservar intenções locais até cancelamento, sem exigir reinício do cliente.
- Conservar o interesse de filho ainda na mesma Session/época apenas até ao
  prazo original do pedido/grant já admitido. Se a autorização a montante se
  perdeu, este interesse fica suspenso para encaminhamento, não ganha lease
  novo e não prolonga o prazo por ficar à espera de recuperação.
- Tal interesse limitado pode justificar a referência upstream agregada em
  recuperação. Não autoriza a instalar silenciosamente um path novo no filho.
- Se o novo path/época é diferente, exigir pedido fresco correlacionado do filho,
  compatível com o novo prefixo e com a lineagem aplicável, antes de voltar a
  conceder autoridade a esse ramo. ACTIVATE/RENEW/ACK existentes bastam como
  conceitos; a escolha de APIs e sequência concreta fica para Design.
- Com Session do filho morta, lease expirado, cancelamento ou época antiga
  retirada, remover esse interesse. Mesmo nodeId noutra conexão não o herda.
  A intenção local do próprio filho é que permitirá a renegociação automática
  nesse nó; não se exige novo comando humano para reconstituir o percurso.

Esta regra não altera a imutabilidade do path de um grant nem converte uma
expiração em renovação implícita. Se não houver interesse legítimo restante,
termina a recuperação. A alternativa de apagar todo o interesse descendente
imediatamente seria mais conservadora, mas pode adiar a recomposição agregada;
a escolha não é assumida aprovada. Ver registo em construction-duvidas.

## 6. Q-C-44 proposta — rondas limitadas e regresso de conectividade

Separar uma ronda finita do tempo durante o qual a intenção pode permanecer
UNAVAILABLE. Proposta para aprovação no Model:

- Uma ronda tem prazo total e número máximo de tentativas. Perder a alternativa,
  receber PATH_CHANGED ou aderir outro consumidor durante a ronda não reinicia
  o prazo/counter. Duplicados, callback antigo e tick não criam outra ronda.
- Reutilizar o contrato de ativação limitada existente como referência; os
  valores, elegibilidade de alternativas e integração com os tetos físicos serão
  concretizados em Design. Nenhuma propriedade ou recurso novo é aprovado aqui.
- Se faltar candidato fresco/Session válida, esgotar prazo/tentativas ou cancelar
  a intenção, terminar a operação com estado/razão explícitos. Não bloquear o owner
  aguardando anúncio, handshake, future ou nova sessão.
- Enquanto houver intenção admissível, uma mudança relevante de disponibilidade
  pode voltar a tornar a stream elegível: novo candidato/anúncio válido, Session
  nova ou época corrente admitida. Reavaliar/coalescer a visão; não criar um retry
  a cada notificação ou eternizar a ronda anterior. Repetir a mesma visão já
  esgotada sem novidade útil não basta. A distinção entre atualização útil e
  mero refresh/métrica/duplicado deverá ter critério verificável em Design.
- Recusa temporária de admissão física pode ser reavaliada dentro da ronda ainda
  válida quando houver capacidade; libertar callback velho não reabre uma ronda
  terminal nem consome outra reserva para restaurar estado cancelado.
- A intenção local permite recuperar após regresso de relay sem restart/play
  manual. Se o parent atual entretanto continua saudável, o regresso de um caminho
  melhor não força troca; IMP-014 tratará a política de melhoria com histerese.

Não se promete tempo de recuperação finito durante partição permanente, nem
recuperação de vídeo bloqueado apenas em UDP. A proposta não adiciona um gate
externo; é uma precisão do comportamento do incremento, pendente de decisão.

## 7. Estados e eventos de domínio

Estados conceptuais de recuperação, sem criar enums Java ou wire:

~~~mermaid
stateDiagram-v2
  state "Sem procura" as SemProcura
  state "Confirmado" as Confirmado
  state "Ativação de recuperação pendente" as Recuperacao
  state "Indisponível com intenção" as Indisponivel
  Confirmado --> Recuperacao: autoridade perdida + alternativa + procura
  Confirmado --> Indisponivel: autoridade perdida sem alternativa
  Recuperacao --> Confirmado: ACK corrente + barreira de envio + validade
  Recuperacao --> Indisponivel: ronda termina sem confirmação
  Indisponivel --> Recuperacao: visão elegível nova + intenção corrente
  Recuperacao --> SemProcura: cancelamento final / shutdown
  Indisponivel --> SemProcura: cancelamento final / shutdown
  Confirmado --> SemProcura: última procura retirada
~~~

ACTIVATING/ACTIVE/UNAVAILABLE/INACTIVE continuam a fornecer o vocabulário de
projeção aprovado; RECOVERING não é um enum/API novo implicitamente autorizado.
Uma árvore pode estar logicamente sem operação enquanto envios antigos continuam
fisicamente reservados. Projeções de leitura filtram expiração sem mutar o owner,
renovar prazos ou apresentar uma confirmação perdida como utilizável.

Eventos internos propostos, não novas mensagens de rede:

| Evento | Gatilho/identidade | Efeito admissível |
|---|---|---|
| AutoridadeUpstreamPerdida | Session/grant corrente deixou de ser válido | retirar confirmação dependente, avaliar procura/alternativa |
| ProcuraRetirada | consumidor/ramo e operação exatos | retirar apenas essa intenção; terminar se última |
| VisãoDeRotasAtualizada | estado fresco admitido por seleção/sessões/época | reavaliar elegibilidade, preservando ramo saudável |
| TentativaSubmetida | incidente/ronda/operação e candidato capturados | consumir tentativa segundo regra de admissão, guardar correlação |
| ACKCapturado / EnvioConcluído | correlação e reserva física exatas | confirmar só quando ambas as condições atuais se cumprem |
| TentativaRejeitada / PrazoEsgotado | ronda corrente e motivo local/ERROR correlacionado | retry dentro dos limites ou UNAVAILABLE |
| ÉpocaAdmitida | política existente da fonte confirma nova execução | retirar autoridade antiga dessa origem, renegociar se intenção |
| CallbackRetiradoConcluído | reserva antiga identificada | apenas libertação física correspondente |
| ShutdownIniciado | primeiro budget global | parar admissões, cancelar lógico e aguardar físico limitado |

## 8. Serviços de domínio e repositórios conceptuais

1 - **Coordenador de recuperação:** identifica a perda corrente, separa intenção
de grant, agrega procura, conduz uma ronda e publica resultado. Depende das visões
de seleção/sessões/época e do serviço de ativação já definido documentalmente.
2 - **Validador de candidato:** exige mesma origem, época admitida, Session viva,
path simples/prefixo consistente, metadados/TTL/métrica válidos. Não consulta
bootstrap como membership, não resolve DNS nem cria novas arestas por conta própria.
3 - **Confirmador de tentativa:** cruza ACK, barreira de envio, intenção corrente,
revisão/Session/época e validade remanescente. Mantém ERROR11/14/trace15 isolados.
4 - **Retirador de geração:** termina operações lógicas antigas, mantém proteção
de replay e recursos físicos; DEACTIVATE é best effort com lease fallback, sem
cancelamento aproximado de revisão desconhecida ou dependência de ACK inexistente.

Repositórios são papéis conceptuais em memória, não novos ficheiros/classes/DB:

- Consulta de candidatos/seleção por stream e de Sessions correntes, somente
  através do estado validado existente.
- Estado volátil da árvore/intenção/ronda no owner, com consulta por identidade
  e alteração serializada; não devolve coleções mutáveis a leitores.
- Projeção imutável de confirmação/indisponibilidade/razão por stream, separada
  do catálogo/seleção. Um snapshot antigo não é autorização de validade renovada.
- Registo limitado de lineagens/reservas, segundo os limites anteriores; reinício
  não carrega rotas ou intenções persistidas de outra execução.

O sucesso/fracasso da reparação de vizinhança existente em IMP-035 é apenas
entrada de disponibilidade para routing. Não se reimplementa overlay repair
nem se reabre o bootstrap/transporte para satisfazer uma prova de recuperação.

## 9. Linguagem ubíqua

| Termo | Significado neste incremento |
|---|---|
| Mesma fonte | serverId vinculado à stream, com época corrente admitida |
| Intenção | vontade local vigente ou interesse remoto limitado, não grant |
| Confirmação / grant | autoridade atual de Session/path/época durante lease |
| Perda efetiva | fim da autoridade corrente, não mera melhoria/ausência de seleção |
| Incidente | perda identificada de uma geração, coalescida por árvore |
| Ronda | operação finita; não toda a vida da intenção indisponível |
| Tentativa | pedido correlacionado contra um candidato capturado |
| Recuperado | autoridade confirmada de novo; não prova de frames/FPS |
| Indisponível | sem autoridade confirmável agora; intenção admissível pode continuar |
| Ramo suspenso | interesse ainda limitado à Session/lease, sem forwarding autorizado |
| Trabalho retirado | lógico sem efeito atual; pode reter recurso físico até terminar |

## 10. Cobertura proposta da história / V-016

Todos os grupos abaixo são **PLANEADOS / NÃO EXECUTADOS**. São obrigações de
domínio para concretizar em Design e validar depois, não testes Java escritos.
Os cenários de controlo não substituem prova futura de vídeo/GUI/CORE ou metas.

1 - **R01:** losango S–A–C / S–B–C; perda TCP da Session confirmada leva a outro
path até S, só após ACK correto, sem ciclo ou duplicação estável do upstream.
2 - **R02:** sem alternativa/partição/morte da fonte única termina UNAVAILABLE,
sem PENDING infinito ou fallback para outra origem; intenção local conserva-se.
3 - **R03:** regresso de relay com Session/instance nova e anúncio válido permite
reativar a mesma intenção sem novo play/restart do consumidor. Uma instância
reiniciada continua a exigir consulta inicial ao bootstrap; os processos que
conservaram a lista podem reconectar diretamente sem repetir essa consulta.
4 - **R04:** Down/ACK/ERROR/callback tardios de Session ou geração antiga não
retiram a nova confirmação, prolongam lease nem restauram a antiga.
5 - **R05:** streams da outra origem e streams com upstream independente ficam
intactas quando uma Session/época concreta se perde.
6 - **R06:** alternativa com nó local/repetição, época retirada ou prefixo errado
não confirma; PATH_CHANGED/LOOP_DETECTED/indisponibilidade são observáveis e limitados.
7 - **R07:** cancelar a última intenção antes/depois de enfileirar ou capturar ACK
ganha; cancellation antigo não apaga operação substituta do mesmo consumidor.
8 - **R08:** Session de filho morta ou lease expirado não se conserva como procura;
reconexão de mesmo nodeId não herda subscrição anterior. Validar Q-C-43 aprovada.
9 - **R09:** filho ainda ligado com path antigo não recebe novo grant implícito;
novo prefixo exige pedido fresco/ACK, prazos antigos não são estendidos pela espera.
10 - **R10:** duplicados, eventos repetidos, novas adesões e falha da alternativa
não renovam T/A; ronda esgotada é terminal e novidade útil permite reavaliar sem spin.
11 - **R11:** ACK precoce espera a barreira de envio; ACK no/apos prazo, incompatível
ou com grant excessivo não confirma; fila não acrescenta tempo de lease.
12 - **R12:** nova época da mesma fonte só é usada após admissão existente; época
retirada/ACK antigo não reverte catálogo/árvore. Intenção local persiste, grants não.
13 - **R13:** saturação, recusas de envio e ERROR11/14/15 são isolados por contexto;
não removem ramos saudáveis ou furam quotas/histórico para recuperar.
14 - **R14:** cancelamento/revisão/retirada de uma tentativa não liberta reserva
física cedo; callback e continuação antigos só concluem limpeza, preservando M07.
15 - **R15:** shutdown durante espera/envio/ACK termina pendências e recursos no
budget global original; nenhum envio novo após fecho lógico.
16 - **R16:** ausência de bytes/atividade UDP com TCP saudável não inicia failover.
Bloqueio UDP real/efeito em vídeo permanece limitação a medir nos IMP de experiência.
17 - **R17:** integrar disponibilidade da aresta S–C já criada pela reparação
existente após perdas sequenciais de relays; ligar stream/origem/caminho/ACK e
recursos observados, sem reimplementar repair ou afirmar vídeo recebido.
18 - **R18:** regresso de caminho melhor/alteração RTT/renovação rejeitada com grant
atual saudável preserva o confirmado; não antecipar IMP-014 nem perder Q-C-35/41.

R01/R03/R06 suportam recuperação até à mesma fonte; R02/R10 a indisponibilidade
limitada; R04/R07–R09/R11/R12 a impossibilidade de restaurar estado velho;
R05/R13–R18 isolamento, recursos e fronteira de âmbito. V-016 só ficará cumprido
com execução/evidência, regressão e aceitação pertinentes, não por este mapa.

## 11. Checkpoint e trabalho seguinte

Model inclui entidades, valores, invariantes, agregados, eventos, serviços,
repositórios voláteis e vocabulário; cobre a história sem prometer implementação.
Q-C-43/44 são escolhas explicitamente propostas e devem ser respondidas antes
de depender delas em Design. Se forem alteradas, rever o modelo, não o código.

Após aprovação/autorização apenas de Design: concretizar elegibilidade/rearme,
correlação e capacidades nos três caminhos previstos. A inspeção real das APIs
fica para a revisão ADR autorizada, respeitando a proibição de leitura de fontes
em Model/Design. Não se pede gate externo, ADR nova, quota ou propriedade nova.
Compatibilidade real permanece por conferir; não se conclui impossibilidade
ou necessidade de alteração de fundação sem inspeção autorizada e evidência.

V-016/R01–R18 não executados. Sem Maven, Java, Design/ADR/Implement/Test,
staging, commits ou push. Fecho futuro exige critérios/V-016, regressão aprovada,
manual humano, revisão final atribuída a Fillmore e integração comprovada;
responsável de origem Waheed. Unidade004/PHASE-04 continuam em progresso.
