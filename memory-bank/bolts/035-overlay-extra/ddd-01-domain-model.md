---
stage: model
bolt: "035-overlay-extra"
created: "2026-11-11T22:20:02Z"
status: "approved"
---
# Model — IMP-035 / EXTRA-01 B — reparar com candidatos aprendidos

**Clarificação posterior:** o utilizador pediu que o isolado complete na ligação
que inicia, mesmo sendo o maior ID. As referências ao menor ID no corpo deste
Model são histórico da regra então vigente; a revisão proposta e o novo
desempate estão no Design/ADR-035, sem alteração operacional antes de aprovação.
A validação remota da introdução também está em revisão: a proposta mínima
verifica aprendizagem na origem e usa admissão cooperativa no destino, sem
permissões. Essa política nova aguarda aprovação, não se presume aceite.

Model preparado para revisão; Design, ADR, Implement e Test não iniciados.
[IMP-035](../../../add.md#imp-035), [REQ-025](../../../specs.md#req-025),
[história](../../intents/001-ott-streaming/units/002-overlay/stories/007-reparar-vizinhancas-com-candidatos-aprendidos.md).
Uma tarefa M, unidade 002-overlay, TASK-034, oráculo V-034.

## 1. Fontes, dependência e âmbito

Enunciado ESR-TP2-26-27.pdf, pp. 1–5, specs/intent, ADD e contratos de descoberta.
As secções 2/5/6/8/9/10/11/12 do ADD governam esta etapa. As formulações
históricas de extra opt-in/TCP v1 cedem à decisão operacional posterior:
mesmo programa/JAR, todo controlo TCP v2, descoberta obrigatória, um codec e
um TCP persistente por par. UDP OTT1 mantém o seu formato de vídeo.

IMP-034/034-overlay-extra complete e integrado localmente: revisão funcional
f650be02f2af10b95328f5b50c3d6941e2e75c15 e fecho
22622c1d7eb0fdab3df684f2fac10263cff11736. main e Denji apontam à segunda revisão;
árvore limpa à entrada. A dependência transitiva 001-base-validada está complete.
[Prova do pré-requisito](../../../experiments/construction/034-overlay-extra/closure-integration.json).
Os 1065 testes e o manual Luis/53 checks são evidência histórica desse incremento;
não foram reexecutados neste Model e não demonstram reparação.

IMP-035 consome a cache de IMP-034 para formar arestas novas. Não aprende um
catálogo global nem altera a política de anúncios. Continua antes de 005-rotas.
Não implementa routing, escolha de parent, stream, failover de origem ou vídeo.
A aresta aplicacional precisa de conectividade IP entre endpoints sobreviventes;
este extra não repara a rede underlay nem prova alcançabilidade a partir da cache.

## 2. Decisão explícita de gatilho — Q-C-20

O utilizador escolheu **reparar quando o nó fica isolado**, entendido como zero
sessões vizinhas ACTIVE. Não se inicia reparação autónoma apenas por diminuir
o grau enquanto subsiste um vizinho ACTIVE. Um nó saudável pode atender a
intenção do isolado, sem perder as suas sessões, e sem ter de ficar isolado.
[Decisão e limites](../../../docs/construction-duvidas.md#q-c-20--gatilho-da-reparação-do-imp-035).

Isolamento é um estado local de vizinhança, não um diagnóstico de partição
global ou de disponibilidade de vídeo. READY/consulta bootstrap concluída não
significam presença de vizinhos. O arranque pode ficar sem ACTIVE enquanto
aguarda vizinhos configurados; Design deve coordenar essa janela, os retries
existentes e a elegibilidade de candidatos sem disparos/ciclos ilimitados.
Sem candidato aprendido utilizável, o estado isolado é explícito, sem inventar
endpoints ou voltar a consultar o bootstrap. Uma ligação configurada pode
recuperar em paralelo pelos mecanismos já existentes.

## 3. Entidades e objetos de valor

- **Nó overlay**: nodeId estável, instanceId desta execução, endpoint/papel,
  sessões atuais e ciclo de vida. Possui estado no executor serial (ADR-017).
- **Par de nós**: dois nodeIds ordenados. Identifica a unicidade da ligação,
  independentemente de quem detetou isolamento. O menor nodeId continua a ser
  o iniciador permitido da ligação TCP persistente (ADR-019 residual).
- **Candidato aprendido**: nodeId/endpoint/papel e pistas de instância, estado
  AVAILABLE ou CONFLICT e claims. É localização autorizável, não vizinho ACTIVE,
  rota, stream ou prova de que o destino está vivo.
- **Claim/proveniência**: anunciante, instanceId/connectionId/token HELLO, geração,
  instante monotónico local da QUERY e prazo. Mantém a definição do IMP-034:
  128 candidatos, quatro fontes por ID, TTL local 60 s. Morte do anunciante não
  apaga claims; reparação não fabrica ou renova esses prazos.
- **Necessidade de reparação**: isolamento local e respetivo contexto de execução.
  Desaparece quando surge a primeira sessão ACTIVE ou começa shutdown; não é
  uma quantidade de vizinhos a repor até ao grau inicial.
- **Intenção de reparação**: propósito efémero de ligar um par com candidato
  válido. Pode nascer em qualquer extremo. Transporta contexto suficiente para
  o iniciador permitido agir e para o recetor validar; não é autorização
  automática de um ID desconhecido. O formato/transporte concreto fica em Design.
- **Admissão de par aprendido**: autorização limitada e efémera para tentar esse
  par fora do grafo inicial, separada do grafo imutável do bootstrap e da cache.
  O contrato de confiança e prova verificável pelo outro extremo exige aprovação.
- **Tentativa de reparação**: identidade própria, par, execução, versão do candidato,
  prazo e reservas. Não é a sessão confirmada; callbacks só podem alterar a
  tentativa/contexto a que pertencem.
- **Sessão reparada**: mesmos invariantes de uma sessão configurada: identidade,
  endpoint, capacidade obrigatória, correlação HELLO/ACK, flush e unicidade.
  connectionId/instanceId reais substituem pistas do anúncio apenas nessa sessão.
- **Resultado/diagnóstico**: estados e razões finitos por nó/par, sem histórico
  ilimitado: isolamento, ausência de candidato, espera/backoff, falta de recurso,
  tentativa, rejeição, ACTIVE, shutdown. Ausência transitória não é incompatibilidade.

Os nomes são linguagem de domínio, não novas assinaturas/enums Java aprovadas.
Snapshots/publicações são imutáveis, com coleções copiadas e tamanho limitado.

## 4. Agregados e serviços de domínio

A **vizinhança do nó** é a raiz do agregado: sessões configuradas e reparadas,
necessidade local, admissões/tentativas, diagnósticos e reservas partilhadas.
OverlayRecovery coordena a política dentro do owner existente; NeighborManager
continua responsável pela sessão e pelo mecanismo de reconexão. OverlayNode
integra o ciclo de vida. Não criar um segundo proprietário do estado ou uma
rede paralela de sessões de descoberta/reparação.

A **cache de candidatos** conserva ownership e invariantes de IMP-034. A
reparação consulta snapshots e revalida a elegibilidade antes dos compromissos;
não altera gerações/watermarks, TTL ou fonte para tornar candidato utilizável.
Uma admissão pendente deixa de valer quando a prova necessária expira/conflita;
Design define o ponto de decisão e invalidação. Uma sessão já validada não
cai só porque o claim que permitiu descobri-la expira: tem identidade e vida
próprias. Se essa sessão cair, a elegibilidade de nova reparação é reavaliada,
sem converter a antiga sessão numa autorização eterna.

Serviços conceptuais: observar isolamento, consultar/selecionar candidatos,
coordenar intenção e iniciador, reservar/admitir, confirmar sessão, cancelar
operações lógicas e libertar recursos físicos quando terminam. Não se fixam
assinaturas, bytes, códigos novos ou algoritmos de temporização nesta etapa.

Repositórios conceptuais: leitura limitada da cache; visão das sessões atuais;
registo volátil e limitado de admissões/tentativas/reservas. Não acrescentar DB,
persistência ou pesquisa de membros no bootstrap.

## 5. Invariantes e corridas

1. Todos os nós exigem descoberta em TCP v2; não há extra desligado, ACTIVE_BASE,
   codec v1, fallback, outro JAR operacional ou novo listener de descoberta.
2. Apenas isolamento local inicia necessidade autónoma; nó saudável pode atender
   intenção válida. Não exigir isolamento simultâneo ou aprendizagem recíproca
   como condição universal: o contrato aprovado tem de suportar unilateralidade.
3. Só candidatos locais aprendidos válidos, não-self/não-conflitantes/não-expirados,
   podem motivar uma intenção. Uma intenção recebida exige validação própria;
   confiar cegamente no endpoint afirmado pelo requerente não satisfaz a admissão.
4. Configuração/topologia imutável não é reescrita por reparação; o bootstrap
   continua só inicial, sem listas suplementares, membership ou tráfego periódico.
5. Menor nodeId inicia o TCP persistente; qualquer extremo pode manifestar intenção.
   Não usar duas sessões concorrentes, regras de desempate acidentais ou colisões
   de sockets como substituto da coordenação. Unicidade é por par global de IDs.
6. Sender/recipient/endpoint/instanceId/connectionId e contexto da tentativa são
   validados. A instância observada pelo anunciante é pista, não identidade eterna.
7. Uma sessão só produz NeighborUp após validações e barreira de flush existentes.
   send=ACCEPTED e conclusão de um future não provam receção ou terminação física.
8. Uma sessão saudável não é fechada/substituída para admitir reparação, aumentar
   grau ou tratar peer inválido. Sem recurso, rejeitar/adiar com diagnóstico.
9. Grau total ACTIVE configurado+reparado <= min(C,63), coerente com C<=64 e
   snapshots de adjacência até 63. Reservas/dials/jobs/filas/timers físicos contam
   no orçamento comum existente, incluindo trabalhos cancelados ainda vivos.
10. Cache de 128 IDs não autoriza 128 dials/status/timers ativos. Design deve fixar
    caps de admissões, tentativas, retenção e intenções, e relação com C/B/R.
    Estados por peer limitados; uma tentativa e retry por par, sem busy retry.
11. Esperas/backoff e frequência globais são limitados. A eleição/ordenação de
    candidatos é determinística e justa dentro dos limites que Design concretiza;
    não usa RTT ainda não implementado, nem promete caminho mínimo/ótimo.
12. A primeira sessão ACTIVE satisfaz a necessidade autónoma. Operações concorrentes
    perdem a elegibilidade lógica; libertação física aguarda terminação. Design
    define o commit para impedir duas novas admissões por um único isolamento.
13. Reconexão configurada e reparação concorrem no mesmo agregado. Se uma resolver
    isolamento, a outra não pode publicar sessão extra a partir de estado antigo.
    Não interromper sessões que já tenham sido validamente confirmadas.
14. Tokens/queryId/gerações antigos em descoberta mantêm o descarte de D07e do
    IMP-034: não Down, não terminar consulta atual, não renovar cache/TTL/watermark.
    Contextos de reparação antigos só terminam os seus recursos.
15. Rejeição, timeout, cache vazia/expirada/conflitante e candidato indisponível
    deixam diagnóstico limitado e preservam CLI/aprendizagem/peers saudáveis.
    Nada disso provoca FAILED global por defeito ou alarga caps.
16. Aresta reparada ACTIVE é agora uma adjacência real, divulgável por IMP-034.
    A cache aprendida sozinha nunca é retransmitida como vizinhança direta.
17. Shutdown bloqueia novas intenções/admissões, cancela operações lógicas e usa
    o prazo global restante; owner/callbacks não bloqueiam nem fazem joins.
18. Não há garantia universal de recuperar conectividade: faltando candidato/prova,
    recurso ou alcance IP, registar isolamento/indisponibilidade e tentativas limitadas.
    Falha silenciosa depende da futura deteção do IMP-007, não de um timer inventado.

## 6. Eventos e transições conceptuais

Eventos: NeighborUp/Down corrente, isolamento observado, snapshot de candidatos
consultado/expirado/conflitante, intenção recebida, admissão concedida/recusada,
reserva obtida, dial/handshake/flush concluído, timeout/retry elegível, shutdown.
Cada evento transporta a identidade e a correlação mínimas; callbacks são curtos
e publicados no mesmo executor serial. Relógio é monotónico local.

Com vizinhos ACTIVE não há procura autónoma. Zero ACTIVE produz necessidade;
sem candidato válido há isolamento explícito. Candidato válido pode levar a
intenção/admissão e tentativa limitada. Falha regressa a espera/backoff enquanto
subsistir a necessidade; sucesso regressa à vizinhança ACTIVE. Receber intenção
válida também é possível quando já há sessões ACTIVE, sem criar necessidade local.
Shutdown é terminal para novas operações. Estados não implicam novos enums wire.

## 7. Cenários e critérios de sucesso

**S–R1/R2–C:** arrancar todos, confirmar sessões e aprendizagem de S por C e de C
por S; parar bootstrap. Matar R1: S/C conservam R2, sem procura de novas arestas.
Matar R2: sobreviventes originais isolados podem coordenar uma única S–C. Os
processos e instanceIds de S/C mantêm-se; connectionId pertence à nova sessão.

**S–R1–R2/R3–C:** aprender R1/C por R2/R3 e parar bootstrap. Matar R2: ainda há R3,
sem procura autónoma. Matar R3: C isolado pede reparação; R1 conserva S e pode
admitir C. Resultado S–R1–C com mesmo connectionId S–R1 e uma única R1–C.

**Unilateralidade e ordenação inversa:** o extremo que precisa de reparar pode
ter nodeId maior que o saudável. A intenção deve acionar o menor como iniciador
permitido sem exigir perda de todos os vizinhos no saudável. O contrato também
precisa de tratar cache/proveniência não recíprocas, saturação e expiração.

O ensaio V-034 terá mortes reais de processos (p. ex. SIGKILL), com mesmos
sobreviventes e bootstrap parado, logs/operador/hashes e limpeza verificáveis.
Teste com close/EOF em JVM embutida pode complementar, mas não será apresentado
como morte abrupta real, ensaio CORE, vídeo ou recuperação da rede IP.

## 8. Oráculos propostos de V-034 — M01–M18 (não executados)

| Caso | Oráculo a concretizar em Design/Implement/Test |
|---|---|
| M01 | Descoberta obrigatória/TCP v2, bytes/caps/tipo 3/UDP e testes anteriores preservados; nenhum fallback ou nova dependência. |
| M02 | Zero ACTIVE gera necessidade; perder só um relay mantendo alternativa não cria nova aresta; READY e isolamento são distintos. |
| M03 | Aprendizagem real e morte sequencial de R1/R2 forma S–C, mesmos sobreviventes e bootstrap parado. |
| M04 | Aprendizagem real e morte de R2/R3 forma R1–C, conservando S–R1 e respetivo connectionId. |
| M05 | Só extremo isolado manifesta intenção; menor nodeId saudável inicia o TCP quando a ordenação exige. Não exigir cache recíproca universal. |
| M06 | Intenção/admissão inválida, forjada, fora de contexto ou endpoint/identidade/capacidade incompatível não produz Up nem afeta saudável. |
| M07 | Cache vazia, expirada, self ou conflitante não produz dial inválido, consulta bootstrap ou aprendizagem fabricada; isolamento explícito. |
| M08 | Candidato indisponível implica backoff/tentativas e diagnóstico limitados; outro candidato elegível pode progredir segundo política justa. |
| M09 | Intenções simultâneas, duplicadas e dials cruzados conservam um iniciador e uma sessão por par; eventos finitos no owner. |
| M10 | Limites globais/por peer/grau e reservas comuns mantêm-se com saturação; não sacrificar sessão saudável para abrir espaço. |
| M11 | Expiração/conflito durante admissão/dial invalida a autorização pendente; sessão já confirmada sobrevive à expiração do claim. |
| M12 | Eventos de sessão/tentativa/execução antigas não apagam a sessão nova, mudam TTL ou libertam reservas físicas atuais. |
| M13 | Reconexão configurada e duas reparações concorrem: primeira confirmação satisfaz necessidade, elimina commits pendentes obsoletos e mantém unicidade. |
| M14 | Sessão reparada cumpre HELLO/ACK/flush/identidade v2 e passa a adjacência direta; cache sozinha não é anunciada como ligação. |
| M15 | CLI expõe vizinhança/isolamento/resultado com saída limitada; erro local preserva CLI, descoberta e peers saudáveis. |
| M16 | Cancelamento/timeout lógico não reutiliza reserva antes de terminação física; shutdown global com prazo e sem recursos pendentes. |
| M17 | Clock/transporte/escrita controlados para fronteiras/corridas e TCP real para estabelecimento/falhas; sem usar sleeps como único oráculo lógico. |
| M18 | Regressão clean verify JDK 21, fixtures/V-ADD, manual reproduzível, mortes reais e revisão/integração com comandos, XML/logs/hashes atuais. |

## 9. Handoff, âmbito e gate próprio

O âmbito autorizado do ADD conserva os oito caminhos: OverlayRecovery,
NeighborManager, OverlayNode, OttApplication, respetivos três testes e
experiments/overlay-recovery/. Os nomes/caminhos completos continuam no ADD;
não criar nem alterar Java nesta etapa.

O contrato TCP v2 integrado tem 18 tipos, HELLO sem contexto específico de
intenção/admissão reparadora e autorização inicial por grafo. Isso consta dos
contratos aprovados; **não se verificou código Java neste Model**. Ainda não
está demonstrado que os oito caminhos bastem para intenção unilateral segura,
particularmente sem cache recíproca. Não esconder uma operação em PROBE/ERROR,
REGISTER ou mensagens de routing, nem abrir canal paralelo por inferência.

Design deve propor contrato de intenção/admissão, confiança/proveniência,
correlação, iniciador, framing/versão se afetados, quotas/FIFO/flush, fair selection,
resolução de endpoint, reservas físicas e ciclo de vida. Se afetar protocolo,
transporte, configuração, cache ou seus testes fora dos oito caminhos, apresentar
ampliação do âmbito e eventual ADR-035 **Proposta**, sem editar esses ficheiros
antes da aprovação. Não modificar ADR-034 Aceite para fabricar compatibilidade.

G-EXTRA-ADR está **pendente para IMP-035**; a aprovação de IMP-034 não o satisfaz.
Q-C-20 aprova apenas o gatilho, não wire, admissão ou este Model. Nenhuma ADR nova
é emitida nesta etapa. Revisão de Design/ADR e âmbito são obrigatórios antes de
Implement; V-034/regressão/manual/revisão final/integração antes de DONE.

[PRESSUPOSTO] Laboratório controlado conforme ADR-021; proveniência limita
admissão, mas não fornece autenticação criptográfica. Nenhuma promessa de
reparação perante nós maliciosos, ausência de alcance IP ou falha exclusiva UDP.

## 10. Glossário

Isolado: zero vizinhos ACTIVE locais. Saudável: conserva sessão ACTIVE válida.
Candidato: localização aprendida com claims válidos. Intenção: pedido de
coordenação, não sessão/autorização por si. Admissão: autorização efémera limitada.
Tentativa: operação correlacionada com reservas. Reparação: nova aresta confirmada
fora do grafo inicial. Rota/fluxo: trabalho futuro; conectividade não comprova vídeo.

Parar para revisão de Model. Sem Design, Java, Maven, manual, commits ou push.

## Conferência documental desta etapa

[Entrada/hashes/base](../../../experiments/construction/035-overlay-extra/model-entry.json),
[V-ADD](../../../experiments/construction/035-overlay-extra/model-v-add-01.log),
[conferência final](../../../experiments/construction/035-overlay-extra/model-validation.json)
e [diff contra a base](../../../experiments/construction/035-overlay-extra/model-vs-base.diff).
Validação documental não executa V-034 nem confirma viabilidade do contrato wire.

## Aprovação de Model

Registo de 2026-11-11T22:35:39Z. Model aprovado explicitamente pelo utilizador, com
avanço apenas para Design. O destino pode estabelecer a ligação solicitada
pelo nó isolado, conservando sessões saudáveis; manter os dois cenários e
aprendizagem anterior às falhas. Contratos/ADR e G-EXTRA-ADR pendentes antes
de Implement. O histórico de revisão acima não constitui aprovação de Design.

## Revisão solicitada após o primeiro Design

Registo de 2026-11-11T23:17:55Z. Pedido explícito de simplificação: isolamento e cenários
mantidos, mas iniciar/completar no mesmo TCP independentemente do menor ID.
[Design revisto](ddd-02-technical-design.md) e [ADR-035 Proposta](adr-035-proposal.md)
concretizam a exceção à ADR-019, simultâneos e admissão cooperativa. A proposta
de permissões/retorno não foi aprovada. Só Model concluído, Design para revisão.
