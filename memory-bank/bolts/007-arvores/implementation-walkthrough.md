---
unit: "004-arvores"
bolt: "007-arvores"
stage: "implement"
status: "in-progress"
created: "2026-11-17T18:31:55Z"
updated: "2026-11-17T21:49:25Z"
---
# IMP-011 — Implement: ativação confirmada e investigação de NeighborDown

✅ Model/Design/ADR aprovados; autorização de Implement em2026-11-17T17:50:02Z.
⏳ Código nos quatro caminhos e exceções Q-C-36/Q-C-37 aplicadas; regressão
incremental atual 1325/0/0/0, após correção Q-C-37 expressamente autorizada;
aceitação de Implement ainda pendente. Test/fecho/outro IMP não iniciados.
Não houve commits, push ou contacto com revisores.

## 1. Alterações concretas

1 - **Route.java**: o construtor e as invariantes do record de seleção mantêm-se.
Novo tipo aninhado Confirmation separa o ramo confirmado, Session/endpoint,
path, destinos, consumidores e validade. visibleAt filtra leases expirados sem
renovar prazos nem mutar o owner. O catálogo e RoutingTable não foram alterados.
2 - **Subscription.java**: novo valor imutável por filho/stream/epoch/revisão,
com path, correlação e Lease ancorado em início/duração monotónicos. Aritmética
suporta wrap de nanoTime dentro do horizonte e floor de ms; não transforma
restante submilissegundo em grant positivo.
3 - **RoutingService.java**: ActivationEngine aninhado, owner/port/scheduler
existentes. Agrega tokens/filhos, ACTIVATE/ACK, erros, rondas e lineagens;
adiciona forwarding a Snapshot e conserva o construtor anterior de quatro
argumentos. ERROR relacionado11 e trace15 têm correlação separada.
4 - **ActivationTest.java**: harness próprio,28 testes JUnit incrementais,
27 de comportamento/contratos de ativação e1 de diagnóstico NeighborDown com
OverlayNode/TCP reais. Este ficheiro e o diagnóstico mantêm-se inalterados na retoma Q-C-36.
5 - **OverlayNodeMonitoringTest.java — exceção Q-C-36**: exclusivamente o patch
autorizado pelo utilizador, que espera conjuntamente snapshot e evento da Session
esperada. Asserções, prazos, restantes testes e produção preservados.

APIs locais adicionadas: requestSubscription(streamId,consumerToken), ticket
consultável/cancelável e retrySubscription explícito. Cancelamento lógico de
filho é operação interna no owner, não um handler completo de DEACTIVATE.
Não existe nova CLI de reprodução ou delegação em OverlayNode neste IMP.

## 2. Garantias implementadas e fronteiras

Procura agregada usa conjuntos/identidades, não contagem de mensagens.
Primeiro pedido emite uma referência upstream; adesões compatíveis partilham-na.
Confirmação depende de ACK contextual e da admissão do pedido; ACK antecipado
fica retido uma vez até ACCEPTED. PENDING não autoriza dados. Fonte confirma
oferta local sem parent. IDs/revisões/requestId dos filhos não são os do parent.

Lease a montante é ancorado no início da tentativa, incluindo fila local.
Concessão a filho é limitada pelo pedido, máximo local e restante de montante,
com nova verificação no limite da chamada ao port. Duplicados/revisões superiores
em ramo já confirmado não renovam leases. A validade local não começa na
receção tardia de ACK. Anúncio/TTL e confirmação continuam informações distintas.

Q-C-35: novas adesões divergentes devolvem PATH_CHANGED sem trocar parent ou
alterar ramos saudáveis. Cancelamento e retry imediato respeitam a geração do
consumidor: cancelar um token antigo não remove o novo nem restaura autorização
de um token cancelado quando o novo pedido é recusado.

T limita a ronda inteira; no máximo A emissões, sem reset por duplicado/adesão.
Intenção local pode ficar indisponível depois do terminal, sem loop automático
de novas rondas. Lineagens guardam maior revisão por Session/epoch, sem evicção
protegida. C/D/L/H/B/J, quotas por Session e orçamento de16 ações de ativação por
turno seguem o Design. Os valores e a negociação aprovados não foram alterados.

Callback e continuação físicos são acompanhados separadamente. Cancelar/expirar
a operação lógica não liberta B/J. A libertação física acorda pedidos bloqueados;
awaitTermination inclui todos os envios de ativação dentro do prazo global
existente. Owner cheio mantém cleanup fatal pelos hooks; OVERFLOW/CLOSED afetam
a Session corrente, preservando as demais. Não há executor/timer por pedido.

RENEW, poda remota, DEACTIVATE completo, failover de árvore, histerese, UDP,
vídeo, GUI e CLI play/stop continuam nos incrementos seguintes. Não se alega
transmissão duradoura sem renovação ou disponibilidade contínua dos percursos.

## 3. Verificação incremental de Implement

Ambiente: JDK21.0.13.0.3, Maven3.6.3, release21, offline. Comandos no diretório
Projeto, com JAVA_HOME/PATH apontados ao JDK21. Sockets locais autorizados para
o diagnóstico e a regressão; nenhuma alteração dos prazos dos testes históricos.

| Execução | Resultado real | Evidência |
|---|---|---|
| compile com -DskipTests | Sucesso; não é teste funcional | experiments/construction/007-arvores/implement-compile-01.log |
| -Dtest=ActivationTest test, primeira versão |23/0falhas/0erros/0ignorados | implement-activation-01.log |
| -Dtest=ActivationTest test, com limites/callback |26/0/0/0 | implement-activation-02.log |
| mvn -o -B -ntp test, regressão01 |1322/0/0/0,18 suites | implement-regression-01.log e diretório implement-regression-01 |
| mvn -o -B -ntp test, regressão02 após casos de corrida |1324/0/0/0,18 suites | implement-regression-02.log e diretório implement-regression-02 |

Logs/XML/contagens e hashes dos quatro fontes foram preservados separadamente
por regressão. [Resultado final incremental](../../../experiments/construction/007-arvores/implement-regression-02/result.json).
Os1296 testes históricos e28 novos correram na regressão02, sem falhas/erros/skips.
Não se reutilizou um resultado anterior como se fosse execução desta versão.

São verificações incrementais de Implement exigidas pelo ADD; **a etapa Test
não foi iniciada**. Não houve clean verify/JAR novo, manual humano ou revisão
final Waheed desta implementação. Resultado técnico verde não é regressão
aceite pelo utilizador nem fecho de V-014.

## 4. M01–M14: cobertura incremental e o que continua pendente

Os14 grupos de cenários do Design não equivalem ao número de métodos JUnit.
Há checks incrementais nos14 grupos: agregação, tokens, fonte, ACK antecipado,
divergência, correlação, cancelamento, revisões, T/A, isolamento, Session/epoch,
leases, capacidades/quotas e lifecycle. Os novos métodos estão em ActivationTest.

Não se declara execução completa/aceitação formal dos14 cenários ou V-014 por
esses nomes aparecerem em testes. Continuam pendentes a consolidação integral
das variantes do Design na etapa Test, incluindo cadeia de dois relays,
ensaios integrados de ativação e avaliação dos limites em cenários adicionais,
manual humano e aceitação da regressão. Fonte/catálogo não são reprodução de vídeo.

## 5. NeighborDown — investigação consolidada, correção fora de âmbito proposta

[Investigação e proposta](../../../experiments/construction/007-arvores/neighbor-down-investigation/README.md)
consolidam o diagnóstico. O teste original espera apenas o snapshot sem N1 e
imediatamente exige o evento externo. O dispatcher real publica o monitor antes
de entregar ao observador; essa espera não estabelece a sincronização exigida.
O novo ensaio controlado com TCP real reproduz a janela e comprova que a
Session esperada é entregue depois de libertar a barreira. Não usa reflexão
nem altera produção para introduzir a ordem desejada pelo teste.

Os negativos anteriores e a repetição isolada original que passou estão
preservados; as duas regressões atuais também passaram com o teste original
inalterado. Não se reconstrói uma cronologia exata que os logs históricos não
contêm nem se apagam os negativos em função dos sucessos atuais.

Q-C-36 foi explicitamente autorizada pelo utilizador nesta retoma e aplicada
exclusivamente como apresentada em OverlayNodeMonitoringTest.java, quinto caminho
limitado à sincronização do teste. O until espera as duas condições; assertTrue,
prazos e restantes testes mantêm-se. [Prova nova](../../../experiments/construction/007-arvores/q-c-36/README.md)
separada dos negativos e diagnóstico controlado anteriores, preservados.

## 6. Estado e próxima ação

Implement fica em curso, sem marcar etapa concluída/aceite ou promover história/
TASK/requisitos a DONE. A exceção Q-C-36 está autorizada e aplicada; a aceitação
de Implement depende da revisão dos resultados e limites, incluindo a falha D11a
observada nas duas regressões atuais. Nenhuma correção adicional foi aplicada.
Sem avanço automático a Test, fecho, commits, push ou outro IMP.
Fecho exige V-014, regressão aprovada, manual humano, revisão final Waheed
e integração; nenhum desses gates é dispensado pelo código ou pelos unitários.

## 7. Conferência histórica anterior à autorização Q-C-36

Em2026-11-17T18:42:01Z, implement-preservation-check.json confirmou: apenas
Route.java/RoutingService.java alterados e Subscription.java/ActivationTest.java
adicionados em Projeto/CORE;251 ficheiros rastreados restantes intactos, incluindo
todos os testes históricos, configuração, codec, POM e CORE. Os quatro hashes
atuais coincidem com os da regressão02 preservada. As listas geradas de target
foram restauradas à versão inicial, sem restaurar/apagar qualquer fonte.
Os três negativos de NeighborDown mantêm dimensões/hashes e o patch Q-C-36
não foi aplicado. HEAD7164c9ba111268dff59d847982b517e6e41c7a98 preservado.

V-ADD documental em2026-11-17T18:41:23Z passou: zero erros,4898 referências,
35 IMP/bolts/histórias e DAG sem ciclos. Log em
experiments/construction/007-arvores/implement-validation.log; git diff --check
passou. Só Model/Design/ADR concluídos; current_stage=implement e external_gates=[].
Não houve Test/fecho/commits/push/outro IMP; aceitação e decisão Q-C-36 pendentes.

## 8. Retoma Q-C-36 — Implement reapresentado em 2026-11-17T18:56:41Z

O utilizador autorizou apenas o patch apresentado no teste histórico, como exceção
aos quatro caminhos. Aplicação exata e âmbito conferidos; nenhuma outra fonte
mudou. [Registo completo, comandos, logs, XML e preservação](../../../experiments/construction/007-arvores/q-c-36/README.md).

- NeighborDown focado: 1/0 falhas/0 erros/0 ignorados.
- Regressão após o patch: 1324/1/0/0; repetição completa: 1324/1/0/0.
- Única falha em ambas: OttApplicationTest.D11a… na linha 562, à espera conjunta
  de REQUIRED_DISCOVERY_MISSING/NeighborUp/DISCOVERY_CACHE_REJECTION. D11a passou
  isoladamente (1/0/0/0). Causa ainda não estabelecida; teste e produção intactos.
- OverlayNodeMonitoringTest (11) e ActivationTest (28), incluindo o diagnóstico
  controlado, passaram em ambas. Os resultados negativos novos e antigos mantêm-se.

Não se declara regressão aprovada nem Implement aceite. V-014 consolidado,
14 cenários formais completos, manual humano, revisão final Waheed e integração
pendentes; sem Test, fecho, commits, push, outro IMP ou gates externos adicionais.

Conferência desta retoma: patch exato e quatro fontes originais intactas;
250 restantes ficheiros rastreados de Projeto/CORE e 59 artefactos de evidência
anterior preservados byte a byte. V-ADD documental e git diff --check passaram.

## 9. Q-C-37 — investigação D11a em 2026-11-17T19:13:11Z

[Relatório correlacionado e patch proposto](../../../experiments/construction/007-arvores/d11a-investigation/README.md).
110 artefactos anteriores preservados; nenhuma das 247 fontes alterada nesta
investigação. Uma execução fixa instrumentada da classe original passou48/0/0/0;
não substitui os dois negativos completos ou o sucesso isolado anterior.

A corrida na continuação de escrita de HELLO foi reproduzida com três ordens
controladas e codec/manager/fixture intactos: write enfileirado antes de
protocolError, mas processado após fecho físico, remove o contexto como TRANSPORT
e pretere a rejeição capturada. As outras duas ordens preservam
REQUIRED_DISCOVERY_MISSING e requestId/connectionId. Session da negociação vazia;
trace real correlaciona HELLO, negativo completo, fecho e diagnóstico.

O mecanismo está provado no ensaio; a sequência exata dos negativos históricos
não pode ser reconstruída. Correção proposta em NeighborManager.java mais teste
novo em NeighborManagerTest.java, ambos fora do âmbito autorizado e ainda não
aplicados. Só git apply --check; compilação/verificação do patch aguardam autorização
específica. Implement pendente, cinco caminhos autorizados mantidos. Sem Test,
fecho, commits/push/outro IMP; todos os requisitos de fecho mantidos.

## 10. Q-C-37 autorizada/aplicada — revisão de Implement em 2026-11-17T21:49:25Z

O utilizador autorizou exclusivamente o patch apresentado em NeighborManager.java
e NeighborManagerTest.java. Dois caminhos adicionais, sete autorizados no total.
Patch aplicado sem ajustes; asserções, testes existentes, prazos e prova anterior
mantidos. Nenhuma outra correção de fundação ou ativação nesta retoma.

[Logs, comandos, XML, diagnóstico sem resposta e preservação](../../../experiments/construction/007-arvores/q-c-37/README.md):

- Novo D11b, três ordens: 1/0/0/0.
- D11a original: 1/0/0/0.
- Handshake/lifecycle: 202/0/0/0, seis suites.
- Uma regressão incremental completa: 1325/0/0/0,18 suites;29 testes novos face
  à base1296. ActivationTest/NeighborDown e todos os testes históricos passaram.
- Sem resposta: Closed ou prazo HELLO existente retiram contexto, tentativa e
  timer antigos sem ativar; só permanece o retry limitado existente. TCP real
  confirma EOF/fecho, zero reservas nativas e terminação completa no orçamento
  configurado de shutdown. Nenhum prazo foi aumentado.

O primeiro lançamento do instrumento externo teve erro de cwd antes de iniciar
TCP; log preservado, corrigido somente o lançamento. O segundo ensaio concluiu
os dois casos controlados e o TCP real. Não é uma falha de produto escondida.

Negativos completos D11a/sucesso isolado, trace/contraprova anterior e negativos/
diagnóstico NeighborDown preservados. A sequência histórica exata não foi
reconstruída por estes sucessos. Implement reapresentado para revisão, não aceite
ou concluído; sem etapa Test, fecho, commits, push ou outro IMP. V-014 consolidado,
regressão aprovada, manual humano, revisão final Waheed e integração pendentes;
sem gates externos adicionais.
