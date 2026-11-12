# Dúvidas de Construction

## Q-C-07 — Base de Implement do IMP-004 não compila após alteração externa

- Estado: resolvida por autorização explícita do utilizador para alterar o ficheiro
  e repor as classes, prosseguindo Implement. A suspensão abaixo é histórica.
- O utilizador aprovou ADR e autorizou Implement de 003-overlay, limitado a
  Neighbor.java, NeighborManager.java e NeighborManagerTest.java, preservando
  Q-C-05/Q-C-06 e D01–D22. Pediu identificar separadamente a alteração externa
  de legibilidade em BootstrapServer.java e confirmar equivalência.
- Evidência: contra a base aprovada `26aaa4ae6775e9c78e1d7658472ccaca0b376427`,
  BootstrapServer.java tem +481/−164 linhas. A alteração externa removeu as
  classes internas EventLoop e RealDeadlines, ainda usadas nas linhas 102/107.
  Não é possível confirmar equivalência: a base atual não compila.
- Compilação de diagnóstico com Java 21: `mvn -B -ntp -DskipTests compile`, em
  Projeto/, falhou com quatro erros cannot find symbol para essas classes.
  [Log novo](../experiments/construction/003-overlay/implement-entry-compile.log),
  [diff externo](../experiments/construction/003-overlay/implement-entry-bootstrap-external.diff)
  e [numstat acumulado de Projeto/](../experiments/construction/003-overlay/implement-entry-project.diff.numstat).
  Uma confirmação offline também falhou pelos mesmos quatro erros.
  Não houve execução V-007/regressão/manual nem implementação nos três ficheiros.
- Impacto: não se consegue executar os oráculos/regressão sobre a base atual;
  corrigir BootstrapServer está fora dos três ficheiros autorizados. A regra do
  utilizador exige parar quando o código contradiz o ADD ou exige intervenção
  fora do âmbito aditivo autorizado.
- Proposta para aprovação: restaurar exclusivamente EventLoop e RealDeadlines
  da base aprovada, como correção externa separada, preservando a formatação
  restante; compilar e voltar a verificar a equivalência antes de Implement.
  Não exige substituir uma decisão arquitetural nem criar ADR: é reposição de
  código removido numa alteração externa. Nenhuma correção aplicada nesta inspeção.

- Resolução executada: EventLoop e RealDeadlines repostas da base aprovada;
  compilação passou. Auditoria javac encontrou 102 declarações normalizadas
  equivalentes; 19 testes existentes de bootstrap passaram na regressão final
  de 903 testes. Evidência e limites em
  [Implement](../memory-bank/bolts/003-overlay/implementation-walkthrough.md#correção-externa--q-c-07).

## Q-C-01 — Testes primeiro no IMP-001

- Registada: 2026-11-03T21:03:35Z.
- Estado: resolvida; adaptação aprovada exclusivamente para IMP-001.
- Âmbito: IMP-001, bolt/unidade 001-base-validada.
- Evidência: [IMP-001](../add.md#imp-001) exige verificar TASK-001–005 DONE sem
  reimplementar; o pedido desta execução exige interfaces e testes primeiro,
  com testes a falhar antes de implementar. Não há nova funcionalidade ou API
  prevista neste incremento. O tipo Simple e a DoD excluem código/testes artificiais.
- Proposta: no checkpoint 2 auditar assinaturas/records/enums e correspondência
  entre os testes existentes e V-001–005/V-ADD; não acrescentar ou provocar uma
  falha artificial só para imitar um ciclo de funcionalidade nova. Executar a
  regressão com evidência nova na validação autorizada. Qualquer lacuna real
  deve ser apresentada ao utilizador antes de alterar fundações/testes.
- Impacto: nenhuma mudança de bytes, arquitetura, requisitos ou dependências;
  adapta o procedimento de testes primeiro à natureza de verificação de IMP-001.
- Aprovação: explícita pelo utilizador nesta sessão, registada em
  2026-11-03T21:08:48Z. Apenas conferir interfaces e mapear testes no checkpoint 2,
  apresentando lacunas antes de alterar fundações/testes e parando para revisão.
  Não se autoriza omitir a regressão com log novo ou o teste manual na validação.
  Não altera o procedimento dos restantes IMP nem requer nova ADR.

## Q-C-02 — Compatibilidade do mecanismo de fecho

- Registo do diagnóstico/ensaio: 2026-11-03T21:42:27Z; apenas IMP-001/checkpoint 5.
- Estado: resolvida; ensaio em cópia temporária e fecho real aplicado após
  aprovação/integração em 2026-11-03T21:56:48Z. [Saída real](../experiments/construction/001-base-validada/completion-real.log).
- O script local não existe; o instalado em /home/luis/.specsmd/aidlc/scripts/bolt-complete.cjs
  falha por falta de fs-extra. Também não está disponível js-yaml. A inspeção
  do serializer mostra YAML genérico, incompatível com os valores JSON do
  parser local; não se alterou esse parser nem se instalaram dependências.
- Solução de ferramenta: [preload](../experiments/construction/001-base-validada/bolt-complete-json-frontmatter.cjs)
  que fornece apenas as quatro operações fs-extra utilizadas a partir de Node
  builtin e load/dump do subset local, preservando a lógica/cascata instalada.
- [Ensaio isolado](../experiments/construction/001-base-validada/completion-tool-compatibility.json):
  execução nativa falha antes de escrever; com adaptação, código 0, metadados
  aceites pelo parser local, bolt/história/unidade completos só na cópia,
  intent e os outros 32 bolts preservados. Sem fecho real ou história editada
  manualmente. A primeira tentativa do auxiliar de ensaio assumia a presença
  das dependências; o diagnóstico e o log foram preservados.
- Impacto: nenhum no runtime Java, wire, âmbito ou ADR; é adaptação de ferramenta
  ao esquema já existente. Não dispensa revisão final nem integração, exigidas
  pelo utilizador/DoD antes do estado complete.

## Q-C-03 — Gramática decimal de alpha

- Estado: resolvida; aprovação explícita do utilizador registada em
  2026-11-03T22:25:30Z, juntamente com a aprovação de Design do IMP-002.
- Âmbito: monitor.ewma.alpha, bolt 019-overlay / TASK-032. O
  [ADD 6.3](../add.md#63-parâmetros-operacionais-opcionais) exige representação
  decimal com ponto e valor finito em (0,1]; a gramática exata é o detalhe
  especificado no [Design, secção 4](../memory-bank/bolts/019-overlay/ddd-02-technical-design.md#4-unidades-gramática-e-conversões-verificadas).
- Decisão aprovada: depois de strip, exigir correspondência da string inteira
  com `(?:[0-9]+(?:\.[0-9]+)?|\.[0-9]+)`. Aceitar, por exemplo, `1`, `1.0`,
  `0.2` e `.2`; os exemplos continuam sujeitos ao domínio numérico.
- Rejeitar expoente, hexadecimal, sufixo d/f, vírgula, sinal, unidades, valor
  vazio e decimal sem dígitos depois do ponto, como `1.`. Após o teste lexical,
  usar Double.parseDouble, exigir Double.isFinite e 0 < alpha ≤ 1; overflow
  não finito e underflow para zero são inválidos. Não arredondar/clamp nem
  aplicar o default 0.2 a um valor presente inválido.
- Erros de propriedades entram na ConfigurationException agregada com fonte e
  chave; criação direta do record também mantém finitude e domínio. A ausência
  da chave usa o default existente. Os casos D03/D04/D10/D12 de V-032 verificam
  fronteiras, gramática, agregação e construção direta quando forem executados.
- Impacto: esclarece a representação de uma propriedade nova; preserva valor
  default, papel, domínio de ADR-016, bytes v1, hard caps e perfis existentes.
  Sem substituição de ADR Aceite, dependência ou âmbito adicional; não requer
  ADR nova. A revisão das cinco ADR está em
  [adr-review](../memory-bank/bolts/019-overlay/adr-review.md).
- A aprovação abrange Design/gramática e autoriza somente a etapa ADR nesta
  continuação; não declara implementação, testes, revisão distinta ou integração.

## Q-C-04 — Coerência da instância e resposta única na consulta inicial

- Estado: resolvida; concretizações de Model aprovadas explicitamente pelo
  utilizador nesta sessão, com registo em 2026-11-04T10:50:52Z.
- Âmbito: IMP-003 / bolt 002-overlay / TASK-006, sem alterar o envelope v1.
- Decisões aprovadas: no REGISTER, senderId corresponde a Endpoint.nodeId e
  senderInstance corresponde a Endpoint.instanceId, não zero. Ambos descrevem
  a execução do emissor imediato; esta validação de contexto não cria registo
  vivo ou exclusividade entre pedidos de instâncias diferentes do mesmo nodeId.
- Uma decisão de resposta por conexão de consulta. Depois de escolhida a
  resposta, pedidos adicionais não iniciam outra operação/resposta nem renovam
  o prazo. Design deve especificar como preservar o envio completo já pendente.
- Fonte: pressupostos explícitos do [Model](../memory-bank/bolts/002-overlay/ddd-01-domain-model.md)
  e aprovação do utilizador; aplica ADR-013/014/015. Não acrescenta tipos,
  campos, ACK, lease, membership, hard caps, dependência ou âmbito.
- Clarificação pedida para Design: comparar hosts pela normalização lexical
  documentada (caixa/ponto final/IPv4 decimal), sem DNS nem inferência pelo IP
  remoto; os detalhes devem respeitar o loader e o contrato existente.
- Autorizado apenas Design: fila comum send/sendAndFlush, frame em escrita,
  conclusões fora de locks incompatíveis, prazos/saturação/shutdown e injeções
  de relógio/escrita com oráculos M01–M18 nos cinco ficheiros do IMP. Parar
  antes de ADR/Implement/Test. Esta aprovação não aceita código ou fecha TASK;
  a revisão final atribuída a Waheed e integração continuam exigidas.
- Sem divergência arquitetural identificada por estas concretizações; nenhuma
  ADR nova é criada neste registo.
- Continuação posterior: Design aprovado explicitamente pelo utilizador, com
  registo em 2026-11-04T11:25:40Z, incluindo Transport.requestClose() como início de
  shutdown sem espera, preservando o comportamento público anterior de close().
  Autorizada apenas a etapa ADR, com [revisão para aprovação](../memory-bank/bolts/002-overlay/adr-review.md).
  Esta aprovação posterior substitui o limite de avanço anterior para Design;
  Implement/Test, revisão final e integração continuam pendentes.
- Continuação Implement: revisão ADR aprovada em 2026-11-04T11:37:45Z; autorizado
  apenas Implement nos cinco ficheiros e documentação, com testes novos/red
  real primeiro e incrementais controlados/TCP. Prova no [walkthrough](../memory-bank/bolts/002-overlay/implementation-walkthrough.md).
  Sem decisão nova; parar antes da etapa Test ou fecho, mantendo revisão final
  atribuída a Waheed e integração pendentes.
- Continuação Test: Implement aprovado em 2026-11-04T12:18:01Z; autorizado apenas
  clean verify/JDK21/TCP e manual reproduzível, com comandos para repetição
  pessoal. [Relatório Test](../memory-bank/bolts/002-overlay/ddd-03-test-report.md)
  com 856 testes e 34 checks do agente, sem alteração Java. Manter aprovação
  de Test, repetição pessoal, revisão final e integração pendentes; sem fecho.

- Continuação após Test: aprovação explícita registada em 2026-11-04T14:05:14Z, incluindo
  856 testes e execução pessoal por Luis em 2026-11-04T13:58:36Z–13:58:38Z,
  34 CHECK_PASS/MANUAL_PASS/JAVA_EXIT=0, mesmo JAR. Log pessoal preservado e
  separado do agente. Autorizados preparação do fecho, commits e push de Denji;
  limite anterior sem commits/push substituído. Mantêm-se revisão final atribuída
  a Waheed e integração antes de DONE; nenhuma decisão arquitetural nova.

- Fecho posterior do IMP-003: o utilizador confirmou «fecha o bolt, a revisão esta feita»,
  com registo em 2026-11-04T14:18:23Z, concluindo a revisão final atribuída a Waheed.
  Versão revista 03345e9e1ab56ec04302676cca6fce32d168223d já integrada em main antes do fecho.
  Mecanismo specsmd executado em 2026-11-04T14:18:23Z: bolt/história complete, TASK-006 DONE;
  unidade in-progress. Esta confirmação supera as pendências dos checkpoints
  anteriores. Sem decisão arquitetural nova ou início de outro IMP.

## Q-C-05 — Receção de ERROR no IMP-004

- Registada em 2026-11-10T14:24:46Z, após pedido explícito de ajustes ao Model; IMP-004 /
  bolt 003-overlay / TASK-007. **Estado: resolvida; propostas da tabela aprovadas**.
  Aprovação explícita do utilizador registada em 2026-11-10T14:48:50Z, juntamente com o
  Model revisto. Autorizado apenas Design, sem Java/ADR/Implement/Test.
- Lacuna identificada na revisão humana: a secção 8 do Model referia tipos 7–16,
  sem explicitar ERROR (17). Correção no
  [Model, secção 8](../memory-bank/bolts/003-overlay/ddd-01-domain-model.md#8-autorização-error-17-e-entrega-ao-consumidor).
- Regras já determinadas: [contrato 07, secção 4](07-API-AND-INTERFACES.md#4-mensagens-de-controlo-v1)
  fixa ERROR por requestId, relatedType como octeto bruto e uso pelo routing;
  ADD 6.2/ADR-012/018/020/021 exigem identidade/contexto, falhas locais e recursos
  limitados. O utilizador instruiu expressamente não responder ERROR a ERROR
  e não descartar erros legítimos necessários aos consumidores futuros.
  Essas obrigações não são alternativas em votação.
- O que falta concretizar nas fontes: reação a ERROR inesperado antes de ACTIVE,
  rejeição remota de HELLO_OK ainda em confirmação local e classificação de retry
  após ERROR remoto. As concretizações abaixo foram **aprovadas explicitamente**
  pelo utilizador; não eram regras inferidas das fontes no registo inicial.

| Situação | Decisão aprovada | Razão/limite |
|---|---|---|
| Descoberta: ERROR da tentativa corrente (requestId REGISTER, relatedType=1, geração/connectionId/prazo certos e envelope válido) | Terminar a consulta e guardar a rejeição remota completa; TIMEOUT(10)/RESOURCE_LIMIT(11) permitem nova tentativa com backoff; restantes codes terminam a descoberta em FAILED com evidência para intervenção | Não confundir resposta negativa com NeighborList, não fazer retry imediato e não mascarar erro incompatível como sucesso; codes próprios de routing nesta fase têm diagnóstico de resposta inesperada |
| Handshake: ERROR do HELLO pendente (relatedType=5 e identidade do alvo) | Retirar só o candidato, informar a rejeição completa e voltar a DISCONNECTED; menor ID repete com backoff, maior espera por novo HELLO, para qualquer code válido | Um ID desejado continua desejado, incluindo DUPLICATE_ID transitório; sem nova consulta bootstrap, alteração de grafo ou Up/UDP |
| Recetor de HELLO: ERROR sobre HELLO_OK já enfileirado, ainda HANDSHAKING | Tratar como rejeição dessa tentativa apenas com o requestId do HELLO recebido, relatedType=6 e emissor/instância do HELLO validado | HELLO_OK tem payload vazio; não criar terceiro ACK nem espera adicional. Depois de ACTIVE, ERROR é entregue como mensagem da sessão, não reabre handshake concluído |
| ERROR não correlacionado num canal corrente de descoberta/handshake, fora de uma janela legítima de aplicação | Diagnóstico local com mensagem/sessão/fase e motivo; fechar só o canal ofensor, classificando falha local de contexto, sem atribuir o payload remoto ao pedido corrente | Descoberta com resposta inesperada vai a FAILED; handshake retoma DISCONNECTED/backoff. Nunca emitir ERROR de resposta ou afetar outro vínculo |
| ERROR antigo, de geração/connectionId retirados ou após prazo já terminado | Observar/contabilizar localmente sem reaplicar o efeito à tentativa nova ou mudar a causa terminal anterior | Sem history/cache de tamanho ilimitado e sem descartar erro legítimo de uma sessão corrente |

Na janela legítima de confirmação/flush, um ERROR de aplicação que não pertença
à operação de handshake fica sujeito à retenção limitada e barreira ACTIVE da
secção 6, tal como os restantes frames de aplicação. Não o consumir como rejeição
de HELLO nem o encaminhar antes da confirmação. Se a confirmação falhar, libertar
com diagnóstico, sem o transportar para outra sessão.

Em ACTIVE, encaminhar o ControlMessage/ErrorPayload e a sessão imutável
(nodeId, instanceId, connectionId) intactos ao consumidor. O manager não exige
pedido próprio pendente, não filtra code/relatedType e não fecha um canal saudável
apenas por NO_ROUTE/PATH_CHANGED/etc. O consumidor futuro correlaciona ou produz
diagnóstico local; não se implementa routing neste IMP. relatedType=3/17/desconhecido
continua octeto bruto e nunca dá origem a ERROR de resposta. Identidade inválida
não é uma mensagem legítima dessa sessão: isolar localmente, sem ERROR de resposta.

- Alternativa não adotada: manter a tentativa até ao deadline após ERROR não
  correlacionado antes de ACTIVE. O utilizador escolheu diagnóstico e fecho local
  do canal, fora da janela legítima de aplicação, sem ERROR de resposta.
- Impacto: só políticas de contexto/terminação e entrega de evento nos três
  ficheiros autorizados. Nenhum byte, código, payload, hard cap, dependência ou
  âmbito de routing novo. Não altera fundações nem cria ADR aceite por inferência.
- Oráculos planeados M02/M07/M11 sincronizados com esta decisão, sem execução.
  Model concluído em 2026-11-10T14:48:50Z; autorizada apenas a preparação de Design, com
  retenção/DNS/correlação de dial/executor partilhável concretos. Parar antes
  de ADR/Implement/Test. Revisão final e integração continuam exigidas para DONE.

## Q-C-06 — Prazo próprio de resolução e bind no IMP-004

- Estado: resolvida; concretização e Design aprovados explicitamente pelo
  utilizador, com registo em 2026-11-10T15:13:50Z. Âmbito: IMP-004 / 003-overlay / TASK-007.
- Origem: o [Design, secção 6](../memory-bank/bolts/003-overlay/ddd-02-technical-design.md#6-dns-e-trabalho-físico-limitado)
  propôs usar control.connectTimeout nas operações locais sem propriedade própria.
  Esta regra não era uma imposição do enunciado nem um novo campo wire.
- Decisão aprovada: cada operação de resolução ou bind tem prazo próprio igual
  ao control.connectTimeout efetivo. Capturar o início monotónico **antes da
  entrada na fila**; espera na fila e processamento do resultado não renovam o
  prazo. Uma conclusão que já chega fora do prazo não reativa a tentativa.
- Esse prazo é separado do timeout do dial TCP. Depois da resolução válida,
  Transport.connect continua com o seu próprio connectTimeout nas Options;
  não retirar tempo de DNS a esse timeout nem mudar a API do transporte.
- Aplicar o prazo ao waiter/tentativa lógica; não usar cancelamento, isCancelled
  ou future concluído como prova de terminação física. Job/reserva em execução
  permanece contabilizado até terminar fisicamente. Resultado/handle tardio é
  cercado pela geração e fechado/retirado quando necessário.
- Shutdown usa o budget global independente de control.shutdownTimeout e awaits
  dos recursos reais. Se o worker DNS não terminar no prazo, reportar timeout
  explícito com recurso pendente, sem alegar encerramento bem-sucedido.
- Preservados Q-C-05, defaults/chaves/papéis, bytes v1, hard caps e APIs existentes.
  Não se introduz propriedade, dependência ou ADR nova; reutilizar ADR-014/016/
  017/020/024 e confirmar formalmente na etapa ADR.
- Oráculos D12/D15/D17/D19/D20 concretizam esta decisão; D01–D22 mantidos e ainda
  não executados. Autorizada **apenas ADR** após Design: parar para revisão antes
  de Implement/Test, sem Java/build/testes, commits/push ou outro IMP.

## Q-C-08 — Validação global inclui exemplos de ferramentas preexistentes

- Estado: resolvida em 2026-11-10T16:41:30Z, após aplicação exclusiva do patch
  aprovado e execução real de V-ADD com saída 0/zero erros.
  As falhas/propostas abaixo são histórico anterior a esta resolução.
- A execução real de `python3 scripts/validate-planning.py` nesta continuação
  devolveu três erros fora dos ficheiros implementados: fence não fechado em
  `.specsmd/aidlc/skills/inception/bolt-plan.md` e
  `.specsmd/aidlc/skills/operations/deploy.md`; referência de exemplo inexistente
  `bolts/001-auth-service/adr-001-jwt-auth.md` em
  `.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md`.
- Estes ficheiros estavam presentes antes de Implement; o template já tinha
  alterações locais. Não foram modificados nesta execução.
- [Log real](../experiments/construction/003-overlay/implement-planning-validation.log).
  Não alegar V-ADD aprovado: o resultado global é falha, embora o relatório
  confirme a DAG e as contagens de arquitetura. A regressão Java é independente.
- Impacto/proposta: apresentar no checkpoint Implement; corrigir os exemplos
  de ferramentas separadamente se autorizado, ou definir explicitamente o
  âmbito aplicável do validador. Nenhuma exceção foi aprovada por inferência.
  Não altera ADR, wire ou código Java; não impede preparar a revisão autorizada.

### Continuação Test de Q-C-08

O utilizador autorizou apenas Test e determinou manter esta pendência explícita,
sem corrigir ferramentas ou reduzir alcance por inferência. Validador novamente
executado em Test: saída 1 e os mesmos três erros.
[Log](../experiments/construction/003-overlay/test-planning-validation.log).
Auditoria: 67 ficheiros de ferramentas preservados nesta etapa. O build clean
verify (903 testes) e o manual do agente (49 checks/Java 0) passaram, mas não
constituem aprovação de V-ADD ou resolução de Q-C-08.

### Q-C-08 — patch concreto para aprovação

Manual humano de Francisco conferido e aprovado pelo utilizador, com 49 checks
e Java 0 confirmado por ele; não aprova Test ou V-ADD.
[Proposta mínima](../experiments/construction/003-overlay/q-c-08-proposal.md):
três ficheiros, +5/−3 linhas contra o conteúdo local atual, com cópias e hashes
dos inputs. Ensaio isolado: três erros antes, zero depois, 3688 ligações/âncoras
em ambos; validador intacto. Patch ainda não aplicado no repositório.
Q-C-08 permanece por resolver até aprovação/aplicação e execução real de V-ADD.
Sem fecho, commits ou push; revisão humana final e integração pendentes.

### Q-C-08 — resolução autorizada e executada

O utilizador autorizou exclusivamente `q-c-08-proposed.patch` e nova execução
de V-ADD. Hashes de entrada conferidos; diff aplicado exatamente igual à proposta:
+5/−3 em três ficheiros de ferramentas, preservando o restante conteúdo local.
O validador mantém o mesmo SHA-256/alcance. Execução real no repositório:
`python3 scripts/validate-planning.py`, saída 0 e zero erros.
[Diff aplicado](../experiments/construction/003-overlay/q-c-08-applied.diff),
[numstat](../experiments/construction/003-overlay/q-c-08-applied.diff.numstat),
[log](../experiments/construction/003-overlay/q-c-08-v-add-applied.log) e
[auditoria](../experiments/construction/003-overlay/q-c-08-applied.json).
Q-C-08 resolvida; Test continua por aprovar. Manual humano de Francisco cumprido;
revisão humana final e integração pendentes. Sem fecho, commits ou push.

### Confirmação da resolução de Q-C-08 na aprovação de Test

Em 2026-11-10T16:44:29Z (registo), o utilizador aprovou Test incluindo V-ADD e
mandou manter Q-C-08 resolvida. Resolução técnica já suportada pelo patch
aplicado e logs com saída 0/zero erros. Revisão humana final e integração
continuam pendentes; não há autorização de fecho, commits ou push.

## Q-C-09 — NODE_LEAVE na composição do IMP-005

- Estado atual: decisão de âmbito resolvida por autorização explícita do utilizador;
  extensão ainda não implementada nem validada. Model aceite; checkpoint Plan por aprovar.
  O registo abaixo conserva a evidência e proposta originais. Não alterar Java nesta etapa.
- TASK-008, secção 5, exige notificar NODE_LEAVE aos vizinhos diretos quando
  possível, antes de fechar. ADD 8 limita o incremento a OttApplication,
  OverlayNode e OttApplicationTest.
- Evidência da base 2ea748e: NeighborManager.send(), linhas 177–185, rejeita tipo
  que não seja application; application(), linha 430, aceita apenas 7..17.
  NODE_LEAVE é 4. requestClose/close/disconnect não têm emissão de Leave; snapshots
  não expõem canais. O comportamento é igual no checkout atual.
- Impacto: não é legítimo prometer essa operação nos três ficheiros de composição
  sem uma porta pública adequada. Best effort não é autorização para eliminar
  silenciosamente uma operação prevista, criar outro socket ou contornar sessão.
- Proposta para decisão, não aplicada: fornecer uma operação lifecycle aditiva no
  manager, sob extensão de âmbito explicitamente aprovada, ou planear um incremento
  separado que a disponibilize. Preservar send de aplicação, identidade/sessão,
  bytes v1, caps e todos os testes de IMP-004. A confirmação local de escrita,
  o tempo restante e a rejeição de novos comandos devem ser definidos antes de código.
- Não foi provada uma ADR aceite inviável; não se cria ADR-033 por inferência.
  Se a solução exigir mudar política/decisão aceite, aplicar ADD 11 e obter a
  aprovação correspondente. Parar a parte afetada; nenhuma opção autorizada aqui.
- [Modelo](../memory-bank/bolts/020-overlay/ddd-01-domain-model.md). Sem nova
  execução Java/Maven/V-008 ou alteração das fundações.

### Decisão autorizada de Q-C-09 — Model aceite, Plan preparado

Registada em 2026-11-10T17:38:56Z. O utilizador aprovou a extensão mínima aditiva de lifecycle
em NeighborManager e testes próprios, preservando send() de aplicação e o fecho
histórico de IMP-004. A alternativa de incremento separado não foi adotada.
ADD/IMP-005 e contrato 07 passam a documentar essa exceção. Decisão de âmbito
resolvida; a capacidade permanece por implementar e provar com L01–L12/V-008.
Base operacional 90cded6; 2ea748e é referência histórica.
[Plan](../memory-bank/bolts/020-overlay/implementation-plan.md) define operação,
sessões diretas, confirmação local, bloqueio de trabalho/retries, orçamento e
fecho incondicional. Sem alteração Java, nova ADR por inferência, commits ou push.

## Q-C-10 — Reserva do budget de cleanup aprovada

Estado: decisão aprovada explicitamente pelo utilizador, registada em 2026-11-10T17:41:41Z.
A fase best effort de NODE_LEAVE usa no máximo metade do budget global restante
à entrada do primeiro pedido; a outra metade fica reservada para cleanup.
Pedidos repetidos não renovam o prazo; não há timeout completo por vizinho/recurso.
Concretização de Plan, não exigência textual do enunciado nem novo hard cap wire.
Prova futura L04/L06/L07/L11 e M10/M12; sem execução inferida pela aprovação.

## Q-C-11 — Gramática CLI aprovada

Estado: decisão aprovada explicitamente pelo utilizador, registada em 2026-11-10T17:41:41Z.
`--help` apenas sozinho; execução com `--role BOOTSTRAP|SERVER|RELAY|CLIENT` e
`--config <ficheiro>`, ordem livre e cada opção exatamente uma vez. Papéis
maiúsculos; sem aliases, equals ou posicionais. Ajuda combinada, duplicação,
opção desconhecida ou valor ausente: erro 2 antes de abrir sockets.
Escolha de interface do Plan, não transcrição do enunciado. M01/M02/M16 futuros.

## Q-C-12 — Limites locais de I/O aprovados

Estado: decisão aprovada explicitamente pelo utilizador, registada em 2026-11-10T17:41:41Z.
Linha CLI até 4096 caracteres antes de acumular mais; fila de comandos até
runtime.overlay.eventsCapacity, fila de saída até runtime.control.queueCapacity;
registo até 4096 caracteres com truncagem indicada. Excesso de linha tem erro
explícito e consumo limitado; saturação/falha de consumidor é observável e
solicita paragem sem bloquear o owner. São buffers locais, sem novas propriedades,
política wire ou alteração dos hard caps existentes; decisão do utilizador,
não atribuída ao enunciado. M13/M14 futuros, com terminação física a demonstrar.

### Evidência de Implement para Q-C-09–12

Registada em 2026-11-10T18:16:24Z, ainda sujeita à aprovação de Implement. As decisões aprovadas
foram implementadas nos cinco ficheiros, sem alterar send()/wire/caps ou reabrir
IMP-004. L01–L12/M01–M17 têm prova técnica na regressão atual de 979 testes; M18,
clean verify de Test, manual humano, revisão final Waheed e integração pendentes.
O scope Q-C-09 permanece resolvido; as três concretizações Q-C-10–12 continuam
atribuídas à aprovação do utilizador, não ao enunciado. Nenhuma ADR foi substituída.
[Walkthrough e limites](../memory-bank/bolts/020-overlay/implementation-walkthrough.md).

## Q-C-13 — Referência a template inexistente no V-ADD de IMP-005

O V-ADD do checkpoint Test devolveu saída 1 por uma ligação quebrada para
`adr-template.md` no template DDD de `.specsmd/`. As duas referências já
constavam da base operacional `90cded6`. O mesmo erro foi reproduzido no
estado de entrada. O [precedente Q-C-08](#q-c-08--validação-global-inclui-exemplos-de-ferramentas-preexistentes)
regista uma correção documental de ferramentas com validação no repositório.
[Relatório inicial](../experiments/construction/020-overlay/test-consolidation-validation.md).
[Reprodução no estado de entrada](../experiments/construction/020-overlay/test-vadd-entry-repro.log).

O utilizador aceitou a evidência de V-008 e do manual e autorizou a correção
das duas referências. A instrução aponta para o exemplo da etapa 3 e para o
formato de ADR da secção 5 do ADD. A ligação quebrada foi retirada do exemplo
de apresentação. Test permanece em revisão.

Q-C-13 resolvida na execução de 2026-11-10T22:18:21Z. O V-ADD passou no
repositório com saída 0, zero erros e 45 fixtures verificadas.
[Novo relatório V-ADD](../experiments/construction/020-overlay/q-c-13-v-add.log).

## Q-C-14 — Permissões da bancada Linux para V-009

O Plan do IMP-006 foi aprovado com a abordagem de captura no loopback e
corte seletivo de uma sessão TCP. No diagnóstico de 2026-11-10, `tcpdump`
não conseguiu abrir `lo` com a conta atual. Fora do sandbox, `sudo -n`
pediu autenticação. A sondagem de `ss -K` selecionou um único socket
descartável do próprio diagnóstico. O kernel recusou `SOCK_DESTROY`,
apesar do código de saída 0, e o socket continuou a transmitir dados.
[Diagnóstico no anfitrião](../experiments/milestone-1/preflight/permissions-host.json) ·
[Diagnóstico no sandbox](../experiments/milestone-1/preflight/permissions-sandbox.json).

Estado pendente. Para executar o roteiro aprovado é necessário um operador
ou bancada Linux com permissão de captura em `lo` e de corte do socket
selecionado. Uma mudança de abordagem fica sujeita a revisão desta dúvida.
O diagnóstico não abriu sessões da overlay. V-009 aguarda o ensaio e a
repetição por outro elemento.

### Q-C-14 — Permissões confirmadas na bancada local

Registo de 2026-11-10T23:09:30Z. A sondagem de Luis no anfitrião local, com a sua
sessão autenticada, registou `passed: true`. O PCAP contém um pacote do
par descartável e o seletor exato devolveu uma linha antes do corte e
zero depois. Q-C-14 fica resolvida para essa sessão autorizada.
A tentativa anterior do agente conserva o resultado de falha.
[Sondagem de Luis](../experiments/milestone-1/runs/20261110T230622Z-pax81tgv/permission-probe.json) ·
[PCAP da sondagem](../experiments/milestone-1/runs/20261110T230622Z-pax81tgv/probe.pcap) ·
[Sondagem do agente](../experiments/milestone-1/runs/20261110T230236Z-1oy34xz9/permission-probe.json).

## Q-C-15 — Evidência aceite no fecho de V-009

Registo de 2026-11-10T23:25:25Z. O utilizador aceitou o ensaio técnico validado
de V-009 como evidência única de execução e dispensou a repetição por
outro elemento para este fecho. O roteiro continua disponível e a ausência
de repetição independente é uma limitação registada. A tentativa incompleta
e as sondagens mantêm a sua classificação. A revisão final atribuída a
Fillmore foi dada como concluída por indicação do utilizador.
[Registo de Test](../memory-bank/bolts/004-overlay/test-walkthrough.md).

## Q-C-16 — Retoma de Inception para EXTRA-01

Registo de 2026-11-11T12:11:34Z. O utilizador pediu o extra antes de 005-rotas e
confirmou implementar agora a reconstrução da overlay, com vídeo após
routing/streaming. Aprovou o catálogo inicial de candidatos autorizados.

O grafo do exemplo é S–R1–C e S–R2–C, sem S–C inicial. A nova sessão deve
ser criada após a perda dos relays. ADR-019 admite hoje só vizinhos do
grafo inicial. [ADR-033](../add.md#adr-033) propõe a extensão. A política
de gatilho, iniciativa e admissão concorrente aguarda revisão de Model/Design,
antes de alterar Java. A fronteira de validação evita declarar vídeo
entregue quando só a conectividade foi reconstruída.
[Model](../memory-bank/bolts/034-overlay-extra/ddd-01-domain-model.md).

### EXTRA-01 — Cenário assimétrico acrescentado ao Model

O utilizador acrescentou S–R1–R2–C e S–R1–R3–C. A morte de R2/R3
isola C, enquanto R1 continua ligado a S. V-033 inclui a criação de R1–C
e a preservação de S–R1. A política de iniciativa/admissão deve permitir
essa reparação unilateral, dentro dos limites, sem esperar pela falha
do vizinho que R1 ainda conserva. Catálogo inicial e bootstrap apenas
na descoberta de arranque continuam como âmbito aprovado.

### Q-C-16 — Decisão posterior, descoberta por vizinhos

Registo de 2026-11-11T12:26:03Z. O utilizador mandou substituir o catálogo inicial
pela divulgação de vizinhos diretos e pela conservação limitada de
candidatos. O texto anterior conserva a escolha histórica entretanto
substituída. A decisão atual é a aprendizagem durante a execução.

A cache conserva candidatos aprendidos após a morte do anunciante até ao
prazo próprio. Listas divulgam apenas adjacências diretas confirmadas,
com frequência, tamanho e recursos limitados. A ausência de candidatos
aprendidos antes das falhas deve ficar explícita. A extensão de protocolo,
a confiança, o TTL e a admissão aguardam Design/ADR-033. O extra foi
dividido em IMP-034 (aprendizagem) e IMP-035 (reparação), antes de 005-rotas.

<a id="q-c-17--design-da-descoberta-extra-proposta"></a>
## Q-C-17 — Design da descoberta extra (aprovado em Design)

Proposta registada em 2026-11-11T12:49:29Z. **Estado: escolhas aprovadas
explicitamente em Design; aprovação registada em 2026-11-11T13:38:55Z.**
O utilizador declarou «Aprovo Design do IMP-034 e as escolhas de Q-C-17» e
autorizou apenas ADR. A aprovação do Model isoladamente não fixava as escolhas
seguintes; a aprovação posterior de Design inclui a tabela completa.
ADR-033 e contratos afetados aguardam o checkpoint formal G-EXTRA-ADR, antes
de Implement; não se infere autorização de código ou de fecho.
[Design completo](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md)
e [ADR-033 Proposta](../add.md#adr-033).

| Ponto material | Proposta concreta para revisão |
|---|---|
| Isolamento/localização | TCP separado na dataPort já anunciada; UDP mantém OTT1. Sem alteração do HELLO/control v1, sem fallback para essa porta de controlo. |
| Ativação | extra.discovery.enabled=false por default; só SERVER/RELAY/CLIENT. Modo ativo exige portas TCP distintas e C>=8; erro agregado antes de I/O local. |
| Confiança | Só sessão ACTIVE, identidades dos dois extremos, token requestId do HELLO e consulta/canal correntes. Laboratório sem criptografia; candidato continua não admitido. |
| Formato | OTD1 v1, QUERY=1/SNAPSHOT=2, corpo limitado a 32768 bytes, até 63 adjacências diretas, UTF-8 estrito e papéis com códigos explícitos. |
| Validade | generation positiva por sessão, estritamente crescente; TTL local 60 s desde início da consulta, sem renovar por callback/sessão/geração antigos. Morte da fonte conserva claims até ao prazo. |
| Cache/conflitos | 128 IDs × 4 claims; self ignorado; configuração local autoritativa; divergência de endpoint/papel ou colisão entre IDs torna candidato CONFLICT até desaparecer a divergência. |
| Ritmo/recursos | 5 s de elegibilidade por vizinho, 4 consultas/s globais, 1/s recebida por sessão, 30 s após falha; quatro sockets/dials extra sujeitos ao mesmo C<=64, filas 1+1, DNS físico partilhado/limitado. |
| Exceção futura de admissão | IMP-035 poderá criar par fora do grafo apenas com cache válida e contrato aprovado. Menor nodeId continua a iniciar controlo; intenção pode partir de qualquer extremo, permitindo reparação unilateral. |

Não se encontrou uma decisão aceite inviável que exija alterar código nesta
etapa. Estas escolhas foram aprovadas em Design e continuam sujeitas à formalização
ADR/contratos no gate, não a um [PRESSUPOSTO] usado para contornar aprovação. A porta TCP precisa de estar livre e o catálogo aprendido
não garante disponibilidade. Sem cache válida não se inventa reparação.
G-EXTRA-ADR exige ADR-033 e contratos afetados aprovados antes de Implement.
O contrato adicional de reparação pertence ao Design/ADR do IMP-035/V-034.
V-033 e os testes D01–D12 ainda não foram executados.

### Q-C-17 — Entrada na revisão ADR

Registo de 2026-11-11T13:44:19Z. A [revisão ADR](../memory-bank/bolts/034-overlay-extra/adr-review.md)
confirma o âmbito aprovado. Explicita em ADR-033 que extra.discovery.enabled
é a única chave adicional à lista de ADR-010/016, sem alteração das escolhas
aprovadas. Não abre nova decisão de política nem nova Q-C. G-EXTRA-ADR e
aprovação da revisão permanecem pendentes.

<a id="q-c-18--descoberta-no-canal-de-controlo"></a>
## Q-C-18 — Descoberta no canal de controlo

Mudança pedida pelo utilizador, registada em 2026-11-11T14:17:12Z. **Decidido:** parar
OTD1 separado e usar a ligação TCP persistente de controlo, com framing/codec
partilhados, preservando bytes/tipos/caps v1 e as políticas de aprendizagem.
**Ainda proposto:** o contrato concreto da [ADR-034](../add.md#adr-034) e do
[Design revisto](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md).

A proposta usa HELLO/HELLO_OK v2 para seleção de capacidade e tipos 18/19 só
em v2; mensagens normais continuam v1 no mesmo TCP. Incompatibilidade permite
um fallback de handshake para v1, sem upgrade numa sessão ACTIVE saudável.
A fila comum Q+inFlight admite descoberta sem fecho quando cheia e limita
quatro tickets reais de escrita; não existe fila paralela. Frame malformada
pode fechar o controlo, uma consequência nova que exige aprovação explícita.
Deixam de ser necessárias TCP/dataPort, portas distintas ou C>=8 para o extra.
Cache 128×4, TTL 60 s, fontes, gerações e isolamento de callbacks mantêm-se.

ADR-033 conserva as políticas residuais e a aprovação histórica; a escolha
OTD1 foi suspensa, com substituição parcial proposta por ADR-034. G-EXTRA-ADR
reaberto para IMP-034. O âmbito do ADD inclui os ficheiros comuns de protocolo
e testes necessários; IMP-035 mantém o checkpoint próprio, sem início.
[Arquivo preservado](../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md#limpeza-autorizada).
Nenhum Java/teste/target foi alterado nesta revisão; a compilação anterior já
terminara, não é validação da nova proposta. Sem commits, push ou outro IMP.

### Q-C-18 — Refinamento de margem e falhas

> Histórico: a proposta de Q=1 com extra ativo degradar para base não foi
> aprovada. Aplica-se a decisão posterior de extra requerido abaixo.

Registo de 2026-11-11T14:51:37Z. O utilizador pediu margem de fila, comportamento explícito
com Q=1 e rejeição recuperável de conteúdo, mantendo framing/identidade terminais.
A [proposta revista](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md)
define M=1 e E(Q)=min(1,max(0,Q-1)), admissão com duas posições livres e quota
extra enfileirada de um (incluindo transferência take→inFlight). Quatro tickets
globais e Q+inFlight mantêm-se. Q=1 continua válido, mas não anuncia/seleciona
descoberta e usa modo base, sem bypass. Esta indisponibilidade é explícita.

Um anúncio extra completo/delimitado com identidades atuais pode produzir
rejeição tipada de conteúdo, sem fecho ou alteração de cache/watermark/TTL;
o owner confirma o handle e a identidade antes de a considerar recuperável.
Comprimentos inválidos, truncamento, tipo/versão inválidos e identidade
ilegível/incompatível mantêm-se terminais. Não absorver IOException genérica.
D07a–D07e e D08a–D08f cobrem fronteiras, continuidade v1 e corridas.
ADR-034 e contratos continuam Proposta/para revisão, G-EXTRA-ADR pendente;
nenhum código, build, teste Java ou outro IMP foi continuado.

### Q-C-18 — Decisão posterior: extra requerido no projeto

> Histórico: as exceções opcionais/base e eventual falha global foram
> substituídas pelas clarificações posteriores de obrigatoriedade e isolamento.

Registo de 2026-11-11T15:15:24Z. O utilizador **não aprovou** Q=1/incompatibilidade passarem
automaticamente a base. A execução normal exige extra ativo em todos os nós
overlay. As propostas anteriores de fallback e Q=1 em base com flag ativo ficam
rejeitadas, mantidas apenas como histórico nas evidências.

A [revisão](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md)
define enabled⇒Q>=2 validado com erros agregados antes de I/O, supported=required=1
e seleção obrigatória. Incompatibilidade confirmada de peer autorizado/corrente
é erro explícito RequiredDiscoveryIncompatible/FAILED; ausência de resposta é
falha transitória diagnosticada, com retry apenas v2. Nunca ACTIVE_BASE required.
Perfis/API base continuam flag false/default e Q=1 válidos. Bootstrap mantém
consulta inicial. O preflight normal loadProjectDeployment exige todos os perfis
overlay true; não é um catálogo runtime. A margem e os erros de conteúdo da
revisão anterior mantêm-se. ADR-034/contratos por aprovar, sem continuação Java.

### Q-C-18 — Mudança de requisito: nenhuma ativação opcional

Registo de 2026-11-11T15:35:59Z. O utilizador confirmou extra obrigatório em todos os nós
overlay e mandou retirar a ativação opcional/ACTIVE_BASE. Esta decisão substitui
as propostas anteriores de APIs/perfis base de nó, default false e validação
apenas quando enabled. Não existe caminho de construção/carregamento desligado.

A proposta atual deriva o requisito do papel: ausência da antiga chave já é
obrigatório; true é no máximo declaração redundante, false é erro. Todo overlay
exige Q>=2 antes de recursos, mesmo sem chave ou com false; bootstrap mantém
só lookup inicial. Load/loadDeployment e construtores aplicam a mesma relação,
sem loadProjectDeployment paralelo. Formats/transporte genérico v1 não são um
modo de nó base. Peer required=0/sem capacidade é rejeitado explicitamente.

[Justificação dos testes](../memory-bank/bolts/034-overlay-extra/test-contract-migration.md)
separa bytes v1/Q=1 genérico preservados das expectativas operacionais alteradas.
Fonte anterior e evidências mantidas; não houve alteração Java/testes/target.
ADR-034/contratos pendentes antes de Implement; IMP-034 aprende, IMP-035 repara.

## Q-C-18 — Clarificação operacional e isolamento por peer

Registo de 2026-11-11T17:22:59Z; instante de documentação, não atribuído à mensagem humana.
O utilizador fixa um único programa e a mesma versão do JAR em todos os nós.
Todos os overlay implementam/exigem descoberta; não desenvolver funcionamento
com versões antigas, modo base opcional ou fallback. Manter validação de versão,
identidade, endpoint e capacidade. Inputs incompatíveis são negativos do
protocolo; conservar os testes dos formatos v1 que continuam usados.

A consequência FAILED global/failure()/FATAL/saída da CLI proposta acima foi
rejeitada e substituída por rejeição **só da tentativa**, sem NeighborUp e sem
afetar sessões saudáveis (ADR-020). Estado por peer limitado a configurados,
uma tentativa/agendamento e backoff/jitter/deadlines/reservas existentes;
ausência temporária distingue-se de rejeição confirmada, sem trocar protocolo.
D02b–d/D11a exigem B inválido enquanto A permanece ACTIVE, CLI utilizável e
QUERY/SNAPSHOT válidas de A continuam a alimentar a cache.

false/Q=1 continuam erros agregados offline no overlay; não há desligamento
para acomodar recursos insuficientes. Cache/TTL/proveniência/callbacks e fila
comum mantêm-se. [Design](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md)
e [revisão ADR-034](../memory-bank/bolts/034-overlay-extra/adr-034-review.md)
apresentados para aprovação, sem continuar código. IMP-034 aprende; IMP-035
repara, com checkpoint próprio. Registos anteriores são históricos, não modos
operacionais alternativos.

### Q-C-18 — Decisão de controlo TCP uniforme v2

Registo de 2026-11-11T17:47:08Z. Decisão explícita: TCP exclusivamente v2 em bootstrap,
handshake, mensagens normais e descoberta; um contrato/codec, 18 tipos explícitos,
código 3 rejeitado, sem runtime v1/fallback. Arquivo v1 e fixtures v2 independentes
preparados; migração dos testes justificada com garantias preservadas. Bootstrap
continua só inicial, UDP OTT1 inalterado, descoberta obrigatória e isolamento por
peer/CLI saudável mantidos; cache/TTL/proveniência/shutdown conservados.
IMP-034 aprende, IMP-035 repara depois. Design/ADR-034/âmbito para aprovação;
Implement suspenso, sem execução Java, commits, push ou outro IMP.

## Aprovação e retoma de Implement — TCP v2

Registo de 2026-11-11T17:55:50Z; instante do registo, não data atribuída à mensagem humana.
O utilizador aprovou explicitamente Design revisto, ADR-034, âmbito dos 29
caminhos e migração justificada dos testes/recursos para TCP exclusivamente v2.
ADR-034 Aceite: substitui o contrato TCP v1 e as propostas OTD1/mista/ativação
opcional afetadas; v1/fixtures ficam arquivados, UDP OTT1 permanece inalterado.
G-EXTRA-ADR satisfeito para IMP-034; formalizada revisão ADR e retoma apenas
Implement. Um contrato/codec v2 no bootstrap e vizinhos, descoberta obrigatória,
com framing/caps/identidade/unicidade/FIFO/flush/isolamento/shutdown e cache/TTL/
proveniência preservados. Testes incrementais com logs reais autorizados.
Parar para revisão de Implement antes de Test/fecho; V-033, regressão atual,
manual, revisão humana e integração continuam exigidos. IMP-035 mantém checkpoint
próprio; sem commits/push ou início de outro IMP.

## Q-C-19 — Limpeza TCP v1 e fecho solicitado por Luis

Registo de 2026-11-11T19:39:07Z. Após a execução humana V-033 (53 checks, MANUAL_PASS e saída 0), Luis pediu revisão final, integração e fecho do IMP-034, retirando os arquivos/testes da versão TCP v1 e conservando a versão v2. Esta instrução substitui a obrigação documental anterior de conservar o arquivo TCP v1 na árvore entregue. A revisão técnica final é do agente validador, solicitada por Luis; não se atribui revisão a Fillmore ou Waheed. Luis é o operador humano e autor da autorização de fecho.

Conservam-se os negativos que rejeitam TCP v1, todos os testes operacionais TCP v2 e UDP OTT1 inalterado. UDP versão 1 é o formato de vídeo atual, não o codec TCP retirado. A limpeza não altera runtime, framing, caps ou garantias e não inicia IMP-035. O manifesto da limpeza lista paths/hashes retirados; execuções v2, manual humano, regressão e JAR final permanecem. Integração local e publicação remota são registadas separadamente.

## Q-C-20 — Gatilho da reparação do IMP-035

Registo de 2026-11-11T22:22:21Z. **Decidido explicitamente pelo utilizador:**
«Reparar quando fica isolado (recomendado)», em resposta à escolha entre zero
vizinhos ACTIVE e reparar após qualquer perda.

A necessidade autónoma nasce com zero sessões ACTIVE locais. Perder um relay
mantendo outro não inicia uma aresta extra. Um nó saudável pode atender a
intenção do isolado sem perder as suas sessões; não se exige isolamento nos
dois extremos. O menor nodeId conserva a iniciativa do TCP persistente.
Isolamento local não prova partição global, falha física ou ausência de rota.
Sem candidato válido ou recurso, mantém-se diagnóstico e tentativas limitadas.

O critério antigo «extra desativado» de IMP-035/TASK-034 foi alinhado com REQ-025
/ADR-034 já aprovados: todos os nós exigem descoberta/TCP v2, sem modo base
opcional ou fallback. Não há novo formato, alteração de cap ou ADR aceite.

[Model para revisão](../memory-bank/bolts/035-overlay-extra/ddd-01-domain-model.md).
O gatilho está decidido; Model ainda não aprovado. Design deverá concretizar
intenção, admissão e confiança unilaterais, incluindo cache não recíproca,
iniciador menor, limites e corridas. Os oito caminhos atuais do ADD não incluem
protocolo/transporte/cache: apresentar ampliação e eventual ADR-035 Proposta se
esses contratos forem afetados. G-EXTRA-ADR próprio de IMP-035 permanece pendente
antes de Implement. Esta decisão não autoriza Design/Java/Test/fecho/commits/push.

### Q-C-20 — Aprovação de Model e clarificação de atendimento

Registo de 2026-11-11T22:41:27Z. O utilizador aprovou Model e o avanço apenas para Design.
Clarificação: o destino pode estabelecer a ligação pedida pelo nó isolado,
conservando sessões existentes. Manter S–C e S–R1–C, candidatos aprendidos antes
das falhas e S–R1 preservada. Gate próprio/contratos/ADR continuam obrigatórios
antes de Implement. Não é aprovação da proposta técnica Q-C-21 abaixo.

## Q-C-21 — Proposta de intenção, admissão e retorno do IMP-035

**Histórico: proposta não aprovada, substituída pela revisão Q-C-22 a pedido do utilizador.**

Registo de 2026-11-11T22:41:27Z. **Proposto para revisão de Design; não aprovado.**
[Design](../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md)
e [âmbito proposto](../memory-bank/bolts/035-overlay-extra/scope-change-proposal.md).

- Permissões opacas emitidas pelo destino aos vizinhos ACTIVE antes das falhas,
  divulgadas só junto de adjacências diretas e retidas para validação após Down.
  Registry256, até quatro por sessão, validade local120 s, renovação elegível30 s;
  geração crescente por sessão e janela30 s de divulgação no anunciante.
  Cache128x4/TTL60 s/proveniência não mudam. Permissão não é autenticação
  criptográfica; confiança em introdução de laboratório é explícita.
- Mesmo TCP/codec v2, quatro tipos20..23 (GRANT/INTENT/RESULT/BIND); 22 tipos ativos.
  SNAPSHOT19 ganha presença/prova por Endpoint, máximo21753 dentro do cap32768.
  HELLO/ACK mantêm formato e exigem máscaras3 (DISCOVERY|REPAIR); rejeitar
  só DISCOVERY sem fallback. Preservar bootstrap/restantes payloads e16UDP;
  migrar sete fixtures e
  preparar24 novas independentes, total alvo90. Sem runtime/fallback de JAR antigo.
- Canal provisório no listener de controlo: isolado menor promove o canal;
  isolado maior instala espera e ACK, destino menor fecha/aguarda terminação
  física e faz dial/BIND de retorno. Uma sessão persistente por par, sem dataPort
  ou listener auxiliar; sessões saudáveis preservadas. Resultado/flush não é ACK.
- Uma tentativa autónoma, duas admissões recebidas, dinâmica<=min(16,C), grau
  total<=min(63,C), orçamento total15 s/subfases2 s, sempre limitado por claim/
  permissão e restante global de shutdown. Backoff/jitter já configurados.
  Quotas/FIFO/Q>=2/tickets físicos mantidos, sem fila/pool/timer ilimitado.
- Connection.terminated() é proposta aditiva que observa fim físico dos workers
  e libertação da reserva, com conclusão fora dos locks, sem bloquear owner.
- Âmbito8→30 e minuta para futura ADR-035 exigem revisão explícita. Não inserir
  ainda tipos/records/fixtures no projeto, nem alterar ADR-034 Aceite por esta
  proposta. Os oito caminhos do ADD continuam o âmbito normativo atual.

G-EXTRA-ADR continua pendente para IMP-035. Este registo não antecipa a etapa ADR
nem autoriza Implement/Test, commits, push ou outro IMP. Todos os D01–D24/V-034
são oráculos planeados; codec Java/mortes reais/manual ainda não executados.

## Q-C-22 — Simplificação da reparação na mesma ligação

Registo de 2026-11-11T23:17:55Z. **Pedido explícito do utilizador:** apresentar proposta mais
simples; o isolado inicia e completa na mesma ligação mesmo tendo o maior ID,
sem dial de retorno. Rever ADR-019 e tentativas simultâneas; justificar o mínimo
de admissão unilateral no laboratório. Não avançar para código antes de contratos/ADR.

[Design revisto](../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md),
[ADR-035 Proposta](../memory-bank/bolts/035-overlay-extra/adr-035-proposal.md)
e [âmbito8→21 proposto](../memory-bank/bolts/035-overlay-extra/scope-change-proposal.md).

**Proposta técnica para revisão, não aprovada:**

- Um tipo20 REPAIR_HELLO, payload Hello existente; resposta6 HELLO_OK no mesmo
  TCP. Erros17 existentes, sem códigos novos. Retirar GRANT/INTENT/RESULT/BIND,
  permissões/registry/provas, retorno e API Connection.terminated.
- Origem verifica/regista cache válida/proveniência/TTL. Destino admite o pedido
  unilateral após validação de identidade/endpoint/capacidade/colisões/limites,
  sem exigir cache recíproca. Não comprova a aprendizagem da origem. Esta é a
  confiança cooperativa proposta para o laboratório; não alegar autenticação
  ou rejeição de toda autoafirmação maliciosa com campos válidos.
- HELLO5 configurado conserva grafo e iniciador menor. Exceção20 permite qualquer
  isolado iniciar. Duas saídas pendentes elegem menor originatorId; entrada já
  escolhida bloqueia nova saída para o par; ACTIVE nunca substituída por menor
  ID tardio. Associação de dials/UNCLASSIFIED/B/R e cobrança física preservadas.
- Mesma versão/codec v2;19 tipos1/2/4..20, tipo3 retirado. Capacidades3
  DISCOVERY|REPAIR obrigatórias em HELLO5/20/ACK6, sem subconjunto/fallback.
  Quatro fixtures de máscara migradas e12 novas independentes, alvo78.
  Restantes46TCP/16UDP intactas, incluindo snapshots. Nenhuma fixture alterada agora.
- Cache128x4/TTL60s, C<=64, grau<=min(C,63), Q>=2/quotas da descoberta e owner/
  shutdown preservados. Uma tentativa autónoma, até duas admissões recebidas,
  dinâmica<=min(16,C), backoff existente e orçamento total10s proposto (não SLA).

Decidido pelo pedido: preparar Design direto sem retorno. Ainda propostos:
confiança de admissão, exceção/desempate detalhado, tipo20/máscaras/fixtures/
âmbito e ADR-035. G-EXTRA-ADR próprio permanece pendente. Só Model consta como
concluído; continuar em Design para revisão antes de ADR/Implement/Test.

### Q-C-22 — Aprovação de Design/política/âmbito e revisão ADR

Registo de 2026-11-11T23:28:20Z. O utilizador aprovou explicitamente Design revisto,
política cooperativa e21 caminhos. Aceitou que a origem valide cache/proveniência/
TTL e o destino handshake/limites sem comprovar a aprendizagem. Mantém mesma
ligação, desempate só entre pendentes e ACTIVE preservada. Autorizou apenas
revisão ADR: [ADR-035 Proposta](../add.md#adr-035) formaliza delta parcial019/033/034,
com [revisão antes de Implement](../memory-bank/bolts/035-overlay-extra/adr-review.md).
Não voltar a pedir confirmação da política ou âmbito já aprovados. G-EXTRA-ADR
aguarda aprovação formal desta revisão; não há autorização de Implement.
V-034, regressão, manual humano com mortes reais, revisão final e integração
continuam obrigatórios; sem commits/push/outro IMP.

### Q-C-22 — revisão ADR aprovada, Implement autorizado

Aprovação explícita registada em2026-11-11T23:39:43Z: ADR-035 Aceite, incluindo
substituição parcial019/033/034 e contratos/âmbito21. G-EXTRA-ADR satisfeito para
IMP-035. Só Implement autorizado; Test/V-034 completo/regressão/manual humano
com mortes reais/revisão final/integração pendentes. Sem commits/push/outro IMP.

## Q-C-23 — Composição do monitor na base v2

- Estado atual: opção 1 aprovada explicitamente pelo utilizador; decisão de âmbito resolvida.
  A evidência e opções seguintes preservam o registo original de Model, em 2026-11-12T14:25:37Z; base 3dedf007.
- ADD8 autoriza só NetworkMonitor.java, LinkMetrics.java e NetworkMonitorTest.java.
  Os dois main são placeholders; teste ainda não existe.
- Evidência: NeighborManager fornece Up/Down/Received/Diagnostic/RepairStarted,
  send(Session,...) para 7/8, disconnect(Session) e owner emprestado. OverlayNode
  cria owner/manager privados; APIs de snapshots/descoberta/reparação/lifecycle
  não permitem instalar o monitor com acesso às mesmas sessões/envio/owner.
  OttApplication apenas compõe o observer de logs; não há monitor instalado.
- Impacto: fakes podem provar componente embutível; não demonstram monitor a operar
  na CLI. Owner paralelo, reflexão ou novo transporte contornariam ADR-017/contratos.
- Opção 1: autorizar extensão mínima de composição em OverlayNode e respetivos
  testes, documentando exceção de âmbito e lifecycle antes de implementar.
- Opção 2: confirmar entrega embutível neste IMP, atribuindo explicitamente onde
  e quando a integração real será feita; não declarar monitorização da CLI pronta.
- Nenhuma opção aprovada nesta retoma. Preservar fechos IMP-005/034/035; não alterar
  ADD/contratos/Java por inferência. Não foi demonstrada ADR aceite inviável;
  eventual substituição exigirá próxima ADR livre e aprovação, não reutilizar033.
- [Model](../memory-bank/bolts/005-rotas/ddd-01-domain-model.md), secção8. Parar em Model.

## Q-C-24 — Origem temporal e reset da métrica

- Estado atual: concretizações aprovadas explicitamente pelo utilizador; decisão resolvida.
  O registo seguinte preserva as propostas de Model de 2026-11-12T14:25:37Z.
  Aprovações da retoma cancelada não contam. Não altera Java nem contrato aceite.
- Fontes exigem RTT no mesmo relógio monotónico e reset quando muda instanceId;
  send() existente só confirma enfileiramento. Não fixam um instante de escrita
  observável no manager nem a conservação da EWMA numa nova conexão da mesma instância.
- Proposta nova: guardar início na criação/admissão local da sonda, incluindo filas
  locais até tratamento do PONG. Não chamar RTT no fio a essa medição. Alternativa:
  timestamp de escrita capturado na infraestrutura exigiria porta/âmbito adicional
  e tratamento do PONG que chega antes da conclusão local; não supor flush exposto.
- Proposta nova: qualquer nova Session/connectionId inicia sem amostra, mesmo com
  mesma instanceId, evitando vender RTT de outro canal como atual. Alternativa:
  conservar histórico identificado, limitado e sem custo utilizável antes de nova
  medição; decidir explicitamente se tal histórico influencia ou não a nova EWMA.
- Preservar requestId no PONG já é exigido por docs/07 secção4, que manda respostas
  ecoarem requestId; não necessita nem herda a aprovação cancelada desse detalhe.
- Recomenda-se validar as duas propostas simples antes de Design; nenhuma está
  marcada como aprovada. Oráculos M01/M09/M10/M12/M14 tornam as decisões verificáveis.
- [Model](../memory-bank/bolts/005-rotas/ddd-01-domain-model.md), secções5–8.

### Aprovação de Model/Q-C-23/24 e autorização limitada de Design

Registo em 2026-11-12T14:36:45Z. O utilizador aprovou este Model e Q-C-23 opção1: documentar
extensão mínima de composição em OverlayNode e testes próprios, com allowlist
exata apresentada em Design. Usar mesmo owner/manager/sessões, preservar eventos
alheios e integrar shutdown no budget global; sem transporte ou comando CLI novo.
Aprovou Q-C-24: início na criação/admissão local incluindo filas até tratar PONG;
reset completo por nova Session/connectionId mesmo com mesma instanceId; primeira
sonda elegível no Up e cadência subordinada à conclusão, sem bursts. São decisões
desta retoma, não aprovação reaproveitada da retoma cancelada nem texto do PDF.
Aprovação resolve decisões de âmbito/semântica, não valida comportamento ainda
inexistente. Apenas Design autorizado, parar antes de ADR/Implement.
Sem Java/staging/commits/push; V-010/regressão/manual antes de aprovar Test, revisão
Fillmore e integração obrigatórios, sem gates externos adicionais.


## IMP-007 — Design aprovado; checkpoint ADR

Registo 2026-11-12T14:48:27Z: utilizador aprovou Design e allowlist exata dos cinco ficheiros.
Confirmou revisão de Fillmore do Design atual; não constitui revisão final do
código ainda não implementado. Q-C-23/24 preservadas. Análise ADR preparada para
aprovação, sem divergência/inviabilidade concreta identificada documentalmente;
reutiliza decisões aceites, sem ADR nova ou alteração de estado das existentes.
current_stage=adr; só Model/Design concluídos. Implement/Test não iniciados.
V-010, regressão atual, manual humano antes de aprovar Test, revisão final do
código e integração pendentes; sem gates externos adicionais. Sem Java, staging,
commits, push ou fecho. Relatório: memory-bank/bolts/005-rotas/adr-review.md.


## Q-C-25 — Leitura histórica de NODE_LEAVE face à primeira sonda aprovada

- **Estado:** RESOLVIDA após os dois patches autorizados e regressão04 aprovada tecnicamente (1176/0/0/0); aprovação de Implement/Test ainda pendente.
- **Registo:** 2026-11-12T15:44:13.032355+00:00; evidência real de regressão Implement, não aprovação de Test.
- **Fontes:** Q-C-24/Design aprovado (elegibilidade da primeira sonda no Up),
  preservação expressa de testes históricos e allowlist de cinco caminhos.
- **Evidência:** implement-regression-01.log:1176 testes/2falhas/0erros/0ignorados.
  Uma falha é do novo teste de identidade/ordem e foi corrigida no seu ficheiro
  autorizado (indexOf por igualdade encontrava um Up anterior igual; agora localiza
  a mesma referência). A outra, histórica, é OttApplicationTest.
  m06AndM08ActiveErrorIsObservedAndReconnectNeedsNoBootstrap:342:
  esperava NODE_LEAVE, recebeu PROBE_PING. Os originais/teste histórico estão intactos.
- **Causa:** após nova sessão HELLO5/6, o monitor envia a primeira sonda aprovada;
  o peer controlado histórico lê só um frame após quit e presume que será Leave.
  As mensagens Probe7/8 já são wire v2; esta alteração operacional é precisamente
  o IMP-007 autorizado. Não é uma falha de encode/identidade/flush demonstrada.
- **Impacto:** não se pode declarar regressão verde nem Implement aprovado. Não
  alterar este teste fora da allowlist, desligar monitor, atrasar artificialmente
  a sonda ou esconder/excluir a falha para obter verde. Test continua por iniciar.
- **Opção recomendada:** autorizar exclusivamente o patch de teste
  experiments/construction/005-rotas/q-c-25-proposed.patch, acrescentando o sexto
  caminho Projeto/src/test/java/pt/uminho/esr/ott/app/OttApplicationTest.java só para
  esta adaptação. O peer drena no máximo8 frames/2500ms, aceita apenas PING válido
  v2/identidade da nova sessão/IDs válidos, responde com PONG correlacionado, e
  continua a exigir NODE_LEAVE payload0 e saída0. As garantias históricas de ERROR,
  reconexão sem bootstrap, nova instância e shutdown permanecem; nada é apagado.
- **Alternativa:** manter o teste histórico literalmente intacto, documentando a
  incompatibilidade e a regressão reprovada; sem fecho ou promoção a Test aprovado.
- **ADR:** nenhuma decisão aceite se mostrou inviável; não se propõe ADR nova por
  inferência. O pedido é extensão explícita e localizada de âmbito de teste, não
  substituição de wire, caps, Q-C-23/24 ou política de sondagem aprovada.
- **Estado da proposta antes da resposta humana:** NÃO AUTORIZADA/NÃO APLICADA.


### Q-C-25 — autorização explícita e aplicação exclusiva

Registo 2026-11-12T15:48:58.371559+00:00. Resposta do utilizador: «Autorizar o patch e repetir a regressão
em Implement». Patch exato aplicado após git apply --check, sem staging; hashes
antes/depois registados em q-c-25-application.json. Nova execução focada e regressão
atuais a executar antes de resolver a questão. Sem outro teste histórico alterado,
Test, commits ou push; allowlist ampliada só para esta adaptação concreta.


### Q-C-25 — resultado real e complemento proposto

Registo 2026-11-12T15:51:21.802346+00:00. Teste focado do
patch inicial passou (1/0falhas/0erros/0ignorados). Regressão03:1176 testes,
1falha/0erros/0ignorados; 52monitoring+11integração novos passaram. A única falha
continua no mesmo peer histórico: esperava PROBE_PING durante a drenagem, recebeu
DISCOVERY_QUERY. Não se resolveu a questão só pelo sucesso focado.

Complemento concreto q-c-25-discovery-proposed.patch, NÃO APLICADO: mesmo caminho
e mesmo limite8frames/2500ms. Aceita apenas Query18 com helloRequestId/recipiente/
instanceId exatos da nova Session e devolve Snapshot19 vazio, requestId correlacionado
e geração local crescente. Todos os outros frames continuam a exigir Ping7 ou
Leave4, com identidade v2 correta; mantém Leave payload0 e saída0. Não altera
transporte, tipos, quotas, produção ou outro teste. Autorização inicial era só
o primeiro patch; requer aprovação explícita do complemento antes de aplicar.


### Q-C-25 — complemento aprovado e resolução com evidência

Registo 2026-11-12T15:55:04.977106+00:00. Utilizador respondeu
«Autorizar o complemento e repetir a regressão». Aplicado exclusivamente
q-c-25-discovery-proposed.patch após git apply --check; cadeia de hashes em
q-c-25-application.json e q-c-25-discovery-application.json. Regressão04 executada:
1176 testes,0falhas/0erros/0ignorados,exit0. XML/log preservados em experiments/
construction/005-rotas. Resolve-se a questão de adaptação do peer controlado;
não se promove Implement/Test, fecho, manual ou revisão final a aprovados.
