# Q-C-37 — patch autorizado aplicado e Implement reapresentado

Retoma de 2026-11-17. O utilizador autorizou exclusivamente o
[patch apresentado](../d11a-investigation/proposed-hello-continuation.patch) em
`Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java` e
`Projeto/src/test/java/pt/uminho/esr/ott/overlay/NeighborManagerTest.java`, como
dois caminhos adicionais. Os quatro caminhos originais, Q-C-36 e estes dois
perfazem sete caminhos autorizados. Qualquer outra correção exige apresentação
prévia. Esta autorização não aprova Implement, Test ou fecho.

## Aplicação e resultados novos

Patch aplicado sem ajustes: apenas cinco linhas acrescentadas na continuação
de HELLO inicial de saída e um teste adicional das três ordens. Não se alteraram
asserções/testes existentes, prazos, configuração, dependências ou protocolo.
O ramo fechado permanece não ACTIVE; a rejeição capturada pelo reader conserva
o contexto até ao evento ordenado. Bootstrap, entrada, REPAIR_HELLO e ACTIVE
mantêm os seus caminhos anteriores.

| Verificação incremental de Implement | Testes/falhas/erros/ignorados | Evidência |
|---|---|---|
| Novo D11b, três ordens no mesmo teste | 1/0/0/0 | [log](three-orders.log), [comando/resultado/XML](three-orders/result.json) |
| D11a original, sem alteração | 1/0/0/0 | [log](d11a-original.log), [comando/resultado/XML](d11a-original/result.json) |
| Handshake/lifecycle, seis suites | 202/0/0/0 | [log](handshake-lifecycle.log), [comando/resultado/XML](handshake-lifecycle/result.json) |
| Regressão incremental completa, 18 suites | 1325/0/0/0 | [log](regression.log), [comando/resultado/XML](regression/result.json) |

Ambiente: Java 21.0.13, Maven offline; comandos exatos e hashes dos sete caminhos
em cada JSON. `record-run.py` confere os totais finais do Maven contra os XML das
suites efetivamente executadas; não soma XML antigos de outras execuções.
Os deployments dos testes CLI estão em `d11a-deployments` e
`regression-deployments`, separados dos negativos e do sucesso isolado anteriores.

As seis suites focadas são NeighborManagerTest, NeighborManagerLifecycleTest,
NeighborDiscoveryTest, OverlayRecoveryTest, TransportTest e DiscoveryCodecTest.
Incluem flush/ACK, rejeições, reconexão, EOF, shutdown, recursos e TCP real.
Na regressão completa, D11a original, D11b, os 11 testes de
OverlayNodeMonitoringTest e os 28 de ActivationTest passaram, incluindo o
diagnóstico controlado de NeighborDown inalterado.

## Canal fechado sem resposta: terminação e libertação

[closed-without-response.jsh](closed-without-response.jsh) é instrumento externo
de diagnóstico, não uma alteração de testes ou fundações. Usa classes compiladas
existentes e o patch autorizado. [Log efetivo](closed-without-response-02.log),
[resultado estruturado](closed-without-response-result.json).

1. **Closed, sem resposta:** a continuação de escrita já enfileirada encontra
   o canal fechado e adia a decisão. O evento Closed/PEER_EOF retira exatamente
   esse contexto com TRANSPORT, sem avançar o relógio.
2. **Sem resposta e sem evento Closed:** a mesma continuação adia a decisão;
   o prazo existente de HELLO, 3 s, retira o contexto com TIMEOUT. Só se avança
   o relógio controlado exatamente pelo budget configurado; nenhum prazo aumenta.

Em ambos os casos controlados: contextos, frames retidos e nativeDials a zero;
tentativa antiga retirada; timer antigo cancelado; peer sem contexto, tentativa
ou Session; nenhuma ativação ou falha global. Só resta o retry limitado já
existente. O FakeIo não representa reservas físicas: a prova física é separada.

O terceiro ensaio usa BootstrapServer, NeighborManager, Transport e sockets TCP
reais. Dois peers recebem HELLO e nunca escrevem resposta: um fecha; o outro
permanece aberto. O primeiro termina por TRANSPORT/Closed; o segundo por TIMEOUT
no HELLO de 3 s e observa EOF enviado pelo nó. Os listeners são fechados após
accept para impedir novas conexões estabelecidas por retries. Sem alterar os
perfis gerados pelo fixture histórico, `Transport.connectionCount()` chega a
zero e `NeighborManager.awaitTermination` confirma owner, timers, I/O e transporte
terminados dentro do shutdown configurado de 1,5 s. Há zero reservas após fecho.

O primeiro lançamento do instrumento, preservado em
`closed-without-response.log`, concluiu os dois casos controlados, mas a parte
TCP parou antes de arrancar por um cwd incorreto para o perfil de streams.
Corrigiu-se somente o lançamento do instrumento e a sua diretoria de evidência,
executando a partir de Projeto. Não é falha de produto; não se alterou Java,
teste, perfil ou prazo para obter sucesso. O resultado efetivo termina em
`NO_RESPONSE_PROBE_OK` no segundo log. Comando exato no JSON.

## Preservação e estado para revisão

[before.json](before.json) inventaria 257 artefactos anteriores e 247 fontes.
[preservation-check.json](preservation-check.json) confere que só os dois caminhos
autorizados nesta retoma mudaram e que os restantes ficheiros/evidências mantêm
hashes. Os dois negativos D11a, sucesso isolado, trace, ensaio de ordens anterior,
negativos NeighborDown e respetivo diagnóstico continuam intactos.

A sequência exata dos negativos históricos continua não reconstruível; estes
sucessos não reescrevem a evidência anterior. Não houve repetição da regressão
completa até obter verde: foi executada uma vez após aplicar Q-C-37.

Implement reapresentado para revisão, não aprovado/concluído. Só Model/Design/ADR
estão concluídos, `current_stage=implement`, `external_gates=[]`. Sem etapa Test,
fecho, commits, push ou outro IMP. Permanecem pendentes V-014 consolidado,
regressão aprovada, manual humano, revisão final Waheed e integração; a prova
incremental não conclui formalmente os 14 cenários.

Conferência final: patch exato de 5+35 linhas; 245 outras fontes desta retoma
byte a byte intactas, 248 ficheiros rastreados restantes de Projeto/CORE
inalterados face à base ADR e 257 artefactos anteriores preservados. Listas
geradas rastreadas de target restauradas à base; HEAD mantido. V-ADD documental
passou com zero erros, [log próprio](planning-validation.log), e
`git diff --check` passou. Nenhuma outra correção foi aplicada.
