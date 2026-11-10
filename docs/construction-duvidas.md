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
