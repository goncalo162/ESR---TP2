# IMP-005 — Model: processo, composição por papel e ciclo de vida

Modelo preparado para o checkpoint pedido pelo utilizador, **sem implementação**.
O bolt é Simple: este artefacto é preparação limitada da etapa Plan, que não fica
concluída nem aprovada por existir o modelo. Não se mudou o tipo, a sequência
Plan/Implement/Test ou a arquitetura. Só Model foi autorizado nesta continuação.

## 1. Base, fontes e dependências

Base normativa pedida: `2ea748efd61ba5bdea355d2b5a02de6f6bd9a8c5`.
Checkout real: `90cded6b8d321cfcbe369dd4f8dbed9a01ddf430`, ramo `main`, inicialmente
limpo. A base é ancestral de HEAD. As diferenças posteriores são ferramentas,
saídas antigas de build e evidência do fecho anterior; foram preservadas. ADD,
specs, este bolt e os 155 ficheiros de Projeto/ fora de target são iguais à base.
Não se fez checkout/reset nem se atribui HEAD à base pedida.

Fontes: enunciado ESR-TP2-26-27.pdf, em especial pp. 2–3 (processos manuais e
entrada/saída); ADD 2/5/6/8–12, IMP-005; specs REQ-001/002/023 e NFR-003;
intent/unidade/história; TASK-008; contrato 07, secções 3–7; estratégia 09/V-008;
DoD 14, progresso 15 e contratos públicos já disponíveis em Projeto/.
O texto do PDF foi extraído localmente, sem consulta externa.

Dependências fechadas e integradas na base:

| Incremento / bolt | Estado | Reutilização |
|---|---|---|
| IMP-001 / 001-base-validada | complete | configuração, codec e transporte de base |
| IMP-002 / 019-overlay | complete | RuntimeConfiguration validada, defaults e overrides |
| IMP-004 / 003-overlay | complete | NeighborManager, Session e SerialOwner; TASK-007 DONE |

TASK-003/006/007 exigidas pela TASK-008 estão DONE. A descoberta inicial de
IMP-003 é herdada por IMP-004; não se reimplementa bootstrap. O último build
aprovado foi o de IMP-004, com 903 testes: **histórico, sem nova execução aqui**.

## 2. Fronteira do incremento

Únicos ficheiros de código previstos pelo ADD 8:

- `Projeto/src/main/java/pt/uminho/esr/ott/app/OttApplication.java` — CLI e composição;
- `Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java` — ciclo do nó;
- `Projeto/src/test/java/pt/uminho/esr/ott/app/OttApplicationTest.java` — V-008.

Os dois ficheiros main existem vazios na base. O terceiro ainda não existe.
A referência legada a LifecycleTest/StreamingClient na TASK-008 não autoriza
mudar essa lista: prevalece o ADD. Tipos auxiliares podem ser internos aos três
ficheiros quando a implementação for autorizada, sem nova dependência.

Dentro: selecionar BOOTSTRAP/SERVER/RELAY/CLIENT; argumentos e configuração antes
de rede; identidade volátil; composição dos serviços existentes; comando neighbors,
quit/EOF; logs e códigos de saída; shutdown com prazo; subprocessos reais de V-008.
SERVER/CLIENT são papéis overlay nesta fase, sem fingir emissão/reprodução.

Fora: vídeo/UDP, GUI, catálogo dinâmico, métricas/sondas/rotas/subscrições,
CLI futura completa, HTTP, sockets paralelos ao manager, novas arestas, membership,
novas propriedades/caps, CI/linter/cobertura percentual. catalog/play explicam
explicitamente indisponibilidade nesta fase; não produzem dados fictícios.

## 3. Entidades e invariantes

| Conceito | Identidade/dados | Invariante |
|---|---|---|
| Invocação | argumentos originais, papel e caminho de configuração | sem serviços enquanto argumentos/perfil não forem válidos |
| Plano local validado | NodeConfiguration/RuntimeConfiguration imutáveis | defaults e caps herdados, sem DNS no loader ou configuração parcial |
| Execução do processo | nodeId, papel, PID e identidade de execução quando disponível | identidade de processo não é connectionId nem pode ser confundida com UUID wire de outro componente |
| Nó overlay composto | configuração, uma instanceId não zero, owner e NeighborManager | uma instância por arranque; um único dono serial do estado do nó |
| Serviço bootstrap | BootstrapServer existente e o seu lifecycle | só consulta inicial; sem membros, leases, renovações, sondas ou NODE_LEAVE |
| Vista de vizinhos | cópia de Session/diagnóstico publicado pelo manager | leitura não altera peers, não autoriza UDP antes de ACTIVE e não expõe Connection |
| Pedido de paragem | primeira razão, origem e início monotónico do budget | idempotente; pedidos repetidos não renovam prazo nem repetem cleanup |
| Resultado terminal | código local 0/2/3 e razão preservada | causa fatal não é substituída por EOF/erro induzido pelo próprio shutdown |

A CLI não gera identidade no loader. SERVER/RELAY/CLIENT recebem uma instanceId
única na composição, reutilizada em logs e envelopes do manager. BootstrapServer
já gera a sua identidade interna; o wrapper não deve inventar outro UUID e
apresentá-lo como senderInstance desse serviço. A exposição/log dessa identidade
é uma fronteira a esclarecer em Plan, sem alterar BootstrapServer neste Model.

## 4. Estados e eventos do processo

Estados conceptuais, não enums Java introduzidos nesta etapa:

`NEW → VALIDATING → STARTING → RUNNING → STOPPING → TERMINATED`.
Validação inválida termina sem recursos; falha de arranque passa por rollback
antes de terminar. READY do manager significa lista inicial instalada, não todos
os vizinhos online. RUNNING não exige grau desejado já alcançado. Bootstrap pode
estar ainda indisponível: não confundir retry de descoberta com configuração
inválida, sucesso fictício ou morte de todos os processos.

Eventos: configuração válida/inválida; arranque concluído/falhado; NeighborUp,
NeighborDown, Received e Diagnostic; linha/comando local; EOF; sinal/hook;
paragem pedida/concluída; budget esgotado. Entradas assíncronas conservam geração
e sessão quando aplicável. Estado de rede só muda no owner; callbacks capturam
e oferecem eventos sem DNS, leitura de stdin, logging bloqueante ou espera por espaço.

Saídas: criar serviços permitidos, publicar diagnóstico/snapshot, apresentar
vizinhos, solicitar paragem, cancelar elegibilidade de tentativas, aguardar fora
do owner com tempo restante e devolver resultado terminal. Sem consumidores que
descartem ERROR válido silenciosamente; a composição tem de o tornar observável
sem implementar agora routing.

## 5. CLI e política por papel

| Entrada/papel | Comportamento a construir e provar |
|---|---|
| Ajuda | terminar com 0, sem carregar perfil ou abrir socket |
| --role/--config | obrigatórios para execução, coerentes com perfil; opções desconhecidas/repetidas/incompletas rejeitadas |
| BOOTSTRAP | validar perfil/topologia e iniciar só BootstrapServer |
| SERVER/RELAY/CLIENT | validar perfil completo e criar OverlayNode/manager com owner partilhável |
| neighbors | apresentar desired/estado/sessões confirmadas ou descoberta pendente; não exigir bootstrap disponível depois de READY |
| quit ou EOF | pedir paragem normal uma vez e aguardar recursos com prazo |
| catalog/play | explicar que a função não está implementada nesta fase, sem criar stream/rota/reprodução |
| comando não reconhecido | erro local de utilização, sem frame de rede nem fechar peer saudável |

[PRESSUPOSTO] O Model não fixa aliases, abreviaturas ou sintaxe `--opção=valor`
que o contrato não exige. Gramática exata e apresentação ficam para Plan, com
casos negativos; não se cria parser permissivo por inferência. Help combinado
com argumentos inválidos também precisa de regra explícita antes dos testes.

BOOTSTRAP não é um nó de encaminhamento. neighbors nele pode explicar ausência
de sessões overlay; não deve fabricar uma lista de membros vivos. A utilização
de SERVER/CLIENT não abre media/UDP/Swing neste incremento.

## 6. Ownership, recursos e observabilidade

OverlayNode deve usar o SerialOwner emprestado ao NeighborManager: a API própria
é útil ao serviço embutível, mas criar um segundo owner para o mesmo nó violaria
ADR-017. Configuração/runtime efectivos chegam aos construtores existentes.
BootstrapServer conserva o seu proprietário interno; não se instancia um manager
para esse papel. Não se usa um executor comum ilimitado para CLI, logs ou callbacks.

Input, comandos e logs têm de ser limitados antes de acumular; saturação é
observável e termina a parte afetada. Os limites concretos derivados, gestão de
stdin e I/O de saída ficam para Plan. Um leitor daemon ou um future cancelado
não provam terminação física: V-008 tem de verificar subprocesso e recursos.

Logs distinguem processo/papel/instância conhecida, requestId/connectionId/sessão,
fase, motivo e código de saída. Não expõem paths privados/stack traces na rede.
Uma falha de peer gera indisponibilidade/retry local, sem ser tratada como falha
fatal de todo o nó; uma Diagnostic de saturação/falha fatal de serviço não pode
ser ignorada porque start já teve sucesso. A classificação da causa e a
observação de falhas do bootstrap precisam de portas reais, não de campos privados.

## 7. Paragem e códigos de saída

0: ajuda ou fim normal de quit/EOF com recursos terminados. 2: argumentos/perfil
inválidos antes de sockets. 3: falha local fatal de bind/recurso/cleanup. Não
prometer 0 para SIGINT/SIGTERM: o SO/JVM determina o status de sinal; o teste
regista o resultado real e verifica que o hook não fica pendente.

Uma primeira paragem fecha a admissão de comandos, retira novos retries e usa
control.shutdownTimeout como budget global do processo. O tempo gasto a notificar
vizinhos/fechar manager/terminar owner não se soma em budgets completos por recurso.
Hook e quit partilham a mesma decisão terminal; repetir Ctrl-C não reinicia o prazo.
Nunca aguardar no owner/callback nem confundir requestClose com terminação física.

TASK-008 exige NODE_LEAVE para vizinhos diretos quando possível, nunca para
bootstrap. Não deve ser convertido em garantia de entrega nem novo ACK. A
capacidade de envio é uma lacuna real descrita abaixo; não se modela como existente.
stdin EOF, pipe de logs bloqueado, bootstrap ausente e owner falhado são casos
separados de shutdown. Rollback cobre recursos parcialmente criados.

## 8. Q-C-09 — fronteira real de NODE_LEAVE

Inspeção da base: NeighborManager.send() aceita apenas mensagens de aplicação
7..17; NODE_LEAVE tem código 4 e devolve INVALID_MESSAGE. snapshot não expõe canal;
disconnect/requestClose fecham sem transmitir Leave. Criar outro Transport para
essa notificação, usar reflexão ou representar Leave como ERROR violaria o
modelo de sessão/wire. Não se alarga send() por configuração.

Assim, **não está demonstrado ser possível cumprir a notificação normal da
TASK-008 só nos três ficheiros de IMP-005**. O carácter best effort não permite
apagar silenciosamente a operação por faltar uma porta pública na composição.
[Q-C-09](../../../docs/construction-duvidas.md#q-c-09--node_leave-na-composição-do-imp-005)
regista evidência e opções para decisão: extensão aditiva de lifecycle no manager
com âmbito explicitamente aprovado, ou incremento separado que forneça essa
capacidade. Nenhuma foi adotada; não se reabre IMP-004 nem se cria ADR-033 por
inferência. Não foi provada inviabilidade de uma ADR aceite: é uma lacuna de
contrato/âmbito que deve ser resolvida antes de Plan/Implement da parte afetada.

## 9. Oráculos M01–M18 / V-008 — planeados, não executados

| ID | Resultado exigido no futuro | Meio |
|---|---|---|
| M01 | argumentos/perfil inválidos e ajuda não criam serviços/sockets | factories espiadas + subprocesso/porta de controlo |
| M02 | cada um dos quatro papéis arranca com identidade/papel coerentes | perfis reais e quatro subprocessos por casos |
| M03 | só bootstrap consulta grafo; overlay compõe um owner e manager | factories/owner controlados; logs por papel |
| M04 | READY não significa peers online; neighbors responde durante descoberta | bootstrap/peers controlados, stdin/stdout reais |
| M05 | três nós por CLI convergem; neighbors mostra sessões sem duplicados | processos JVM distintos, TCP local real |
| M06 | bootstrap desligado após descoberta não impede comandos/reconexão | subprocessos e callbacks/IDs observados |
| M07 | um peer sair não exige reiniciar os restantes | fim normal/abrupto e reconexão entre sobreviventes |
| M08 | ERROR/Diagnostic são observáveis; erro de peer não mata nó saudável | observer/saída espiados + TCP real |
| M09 | bind/recurso fatal produz 3 e rollback sem porta/worker pendente | serviço falso e porta ocupada real |
| M10 | quit/EOF/hook concorrentes fazem um cleanup e não renovam budget | clock/fakes e subprocesso com sinal real |
| M11 | NODE_LEAVE só para sessões diretas correntes, antes do fecho quando possível | pendente Q-C-09; transporte/flush controlados |
| M12 | tempo global inclui notificação, serviços e owner; prazo dá erro explícito | relógio falso, recursos bloqueáveis e awaits com tempo restante |
| M13 | stdin/stdout lento/fechado e consumidor que falha não deixam processo preso | pipes reais e I/O controlado, sem alegar thread morta por daemon |
| M14 | queues/linhas/logs limitados e saturação observável | fronteiras com fakes, sem filas ilimitadas |
| M15 | owner emprestado termina no fim; nenhum callback ressuscita nó fechado | owner/futures controlados, gerações e snapshots |
| M16 | catalog/play/command inválido não fingem streaming nem enviam frames novos | entrada/saída e contadores de rede |
| M17 | regressão preserva codec/framing, configuração, manager e fundações | testes existentes + clean verify futuro, sem os enfraquecer |
| M18 | roteiro CLI reproduzível, revisão Waheed e integração antes de DONE | Test futuro com JAR/hash/logs e operador identificado |

Os fakes não substituem M02/M05/M07/M10 com subprocessos reais. A regressão de
903 testes anterior não é execução de V-008. CORE, GUI e metas só contam depois
de execuções próprias, fora deste Model.

## 10. ADR, gates e checkpoint

Aplicam-se ADR-013/014/016/017/019/020/022/024; transversais 001/010/012/015/018/
021/026 preservadas. ADR-003 continua substituída. Sem nova dependência, propriedade,
tipo wire, política ou hard cap. Sem gates externos adicionais; permanecem V-008,
regressão, manual/revisão humana final atribuída a **Waheed** e integração antes
de DONE. A atribuição histórica de execução da TASK é Denji; autoria do agente
não substitui nem presume o revisor.

[PRESSUPOSTO] O pedido «Model» é tratado como preparação de Plan do bolt Simple:
`current_stage=plan`, `stages_completed=[]`, sem fechar Plan. Este documento não
é um desenho técnico nem uma autorização para construir. Se o utilizador quiser
converter o fluxo para DDD, isso requer uma alteração explícita posterior do
plano, não uma mudança silenciosa deste artefacto.

Parar no checkpoint Model. Validar entidades/estados/fronteiras e resolver a
questão material Q-C-09 antes de decidir a próxima parte do planeamento.
Nenhum Java, teste, build, socket de serviço, commit ou push foi executado/criado
para este Model. A validação documental e hashes são evidência de preservação,
não V-008 ou aprovação do modelo.

## IMP-005 — Model aceite e Plan para aprovação

Registado em 2026-11-10T17:38:56Z por decisão explícita do utilizador. Model aceite como preparação
de Plan Simple; Q-C-09 resolvida quanto ao âmbito: extensão mínima de lifecycle
em NeighborManager e teste próprio, sem mudar send() de aplicação ou reabrir IMP-004.
Base operacional 90cded6; referência histórica 2ea748e. Plan preparado e ainda
por aprovar; current_stage=plan, stages_completed=[], sem Implement/Test iniciados.
V-008 com subprocessos reais, regressão, manual humano antes de aprovar Test,
revisão final de Waheed e integração continuam exigidos; sem gates externos adicionais.
Sem Java, staging, commits/push, DONE ou início de outro IMP.

O [Plan](implementation-plan.md) sucede este checkpoint; descrições anteriores de
Q-C-09 aberta e autorização limitada a Model são o estado histórico da apresentação.
