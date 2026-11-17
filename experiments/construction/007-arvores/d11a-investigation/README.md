# D11a / Q-C-37 — diagnóstico correlacionado, patch por autorizar

Investigação de 2026-11-17 durante Implement de IMP-011. Nenhum Java, teste,
fundação, prazo ou configuração foi alterado nesta investigação. Os dois
negativos completos e o sucesso isolado continuam nos diretórios originais de
[Q-C-36](../q-c-36/README.md). [before.json](before.json) inventaria 110 artefactos
anteriores e 247 fontes; os dois deployments negativos encontrados em target
foram também copiados para `negative-01/deployment` e `negative-02/deployment`.

## Conclusão sustentada e limite histórico

Foi reproduzido um defeito de ordenação em `NeighborManager.write`, no callback
de `sendAndFlush`: uma continuação de HELLO já enfileirada pode encontrar o
socket fechado pelo reader e retirar o contexto com TRANSPORT antes de o owner
processar o erro de handshake que esse reader já capturou. O handler de
`protocolError` encontra então o contexto ausente e regressa sem emitir
REQUIRED_DISCOVERY_MISSING ou guardar a rejeição confirmada.

Não é uma inferência baseada apenas em resultados verdes: o
[ensaio de três ordens](owner-order-probe-02.log) usa o mesmo HELLO/HELLO_OK
correlacionado por requestId em cada caso e o codec/manager compilados intactos.
Só muda a ordem dos eventos. A primeira ordem dá TRANSPORT com remoteMessage
vazio; as outras duas dão REQUIRED_DISCOVERY_MISSING com a mensagem negativa.

Este defeito é um mecanismo demonstrado que produz o sintoma de D11a. Os dois
negativos históricos não gravaram os bytes nem a ordem de enfileiramento e têm
remoteMessage vazio no TRANSPORT. Não se pode reconstruir o requestId negativo
ou afirmar a ordem interna exata de cada um. A execução real instrumentada
atual passou; não é uma captura real da ordem negativa antiga. O diagnóstico
não declara essa limitação resolvida nem a regressão aprovada.

## 1. Evidência real de envio, fecho, receção e diagnóstico

Foi feita **uma** execução instrumentada da classe OttApplicationTest, com
`strace -f -ttt -T -yy -xx -s 1024 -e trace=network,read,write,close`, sem alterar
testes, prazos ou métodos. Resultado: 48/0 falhas/0 erros/0 ignorados.
[Comando/resultado](traced-class-result.json), [log Maven](traced-class.log),
[XML](TEST-pt.uminho.esr.ott.app.OttApplicationTest.xml).

O trace completo está em `traced-class.strace`; o
[extrato reduzido](wire-correlation.strace) preserva números de linha originais,
TID, timestamps, endpoints, syscalls e bytes. O
[parser reproduzível](correlate-trace.py) reconstrói syscalls suspensas,
valida a leitura completa dos 46 bytes de HELLO_OK e produz
[wire-correlation.json](wire-correlation.json).

| Nó | HELLO = HELLO_OK requestId | connectionId no diagnóstico | Session da negociação |
|---|---|---|---|
| N1 | 7b1d5628-65d4-499c-97db-aa7c43788d75 | 8c0f98e4-84af-4b7d-8359-a09894723fc7 | vazia |
| N0 | 66ee7f28-6801-4f10-b498-b98dfbd42bb8 | b0896912-cad6-45ac-9f56-63fda67717ad | vazia |

Para N1: HELLO escrito na linha23380; peer de teste escreve 46 bytes completos
de HELLO_OK/selectedMask=0 na23407 e executa shutdown/close na23409–23410;
o nó lê a resposta completa e fecha na23443–23444; o diagnóstico correlacionado
é escrito na23612. Para N0: HELLO23541, negativo23553, shutdown/close do peer
23555/23557, fecho do nó23588/23589 e diagnóstico23649. Os mesmos requestId,
senderId/instance e endpoints ligam os passos; os UUID de connectionId vêm do
diagnóstico, não são inventados a partir dos endpoints.

O teste original faz write, flush e close sequencialmente. O write do socket
retorna 46; `SocketOutputStream.flush` não tem syscall separada observável e
não é um ACK remoto. O trace não prova por si só em que momento corre a
continuação Java de sendAndFlush. O fechamento local do reader após
ProtocolException também existe quando o peer não causa um EOF antecipado:
não se atribui a divergência exclusivamente ao close do peer de teste.

Nesta fase ainda não existe Session para N2: o handshake negativo não chega a
ACTIVE. Os diagnósticos têm `session=Optional.empty`; a correlação usa
connectionId + requestId + identidade da mensagem. O ramo saudável N0–N1 tem
a sua própria Session em NeighborUp, preservada nos logs originais deste ensaio.

## 2. Contraprova determinística no owner

[owner-order-probe.jsh](owner-order-probe.jsh) usa por reflexão apenas o Fixture,
ManualOwner, FakeChannel e FakeIo já compilados de NeighborManagerTest; não
substitui classes nem injeta código no manager. O codec real transforma os bytes
com selectedMask=0 em ProtocolException/HandshakeFailure real. O relógio não
avança, portanto não há expiração ou aumento de prazo a explicar o resultado.

| Ordem controlada, mesma negociação em cada caso | Resultado observado |
|---|---|
| continuação de escrita enfileirada; protocolError enfileirado; canal fechado; owner processa ambos | TRANSPORT; contexto removido; remoteMessage vazio; rejeição não confirmada |
| protocolError enfileirado antes da continuação; canal fechado; owner processa ambos | REQUIRED_DISCOVERY_MISSING; requestId e mensagem negativa preservados |
| owner processa escrita ainda com canal aberto; depois protocolError e fecho | REQUIRED_DISCOVERY_MISSING; requestId e mensagem negativa preservados |

O ensaio confere uma única emissão por caso e zero ativações. O
[log](owner-order-probe-02.log) contém requestId do HELLO e HELLO_OK, instance,
connectionId, Session vazia, motivo e PeerStatus. Isto localiza a divergência
na continuação de escrita, não num UUID de pedido incorreto ou numa Session
ACTIVE antiga. As três ordens são fixas; não houve repetição até obter verde.

Os primeiros lançamentos do instrumento não chegaram a executar o ensaio:
`owner-order-probe.log` documenta a recusa do socket de JShell no sandbox;
`owner-order-probe-unrestricted.log` contém erros de acesso Java do instrumento.
O acesso foi corrigido só no `.jsh`, por reflexão a APIs não públicas. Nenhum
destes logs é classificado como falha de produto ou teste. O ensaio efetivo
está em `owner-order-probe-02.log`, terminado em PROBE_OK.

## 3. Patch concreto proposto — NÃO APLICADO

[proposed-hello-continuation.patch](proposed-hello-continuation.patch) tem
exclusivamente dois alvos, ambos fora dos cinco caminhos atualmente autorizados:

1. `NeighborManager.java`: em negociação HELLO de saída, sem ACTIVE e com canal
   já fechado, a continuação de escrita deixa a decisão ao protocolError/Closed
   ordenados do reader ou ao prazo de handshake existente. Não cria timers,
   não ativa canal fechado, não aceita máscara0 e conserva bootstrap, sessões
   ACTIVE, REPAIR_HELLO, entrada e tratamento de erro com canal ainda aberto.
2. `NeighborManagerTest.java`: acrescenta um teste determinístico das três ordens,
   com os bytes negativos reais, correlação requestId/connectionId, Session
   vazia, rejeição confirmada e nenhuma ativação. Nenhuma asserção/teste existente
   é removido ou alterado; os prazos existentes mantêm-se.

`git apply --check` passou contra a árvore atual. Isto verifica aplicabilidade,
não compilação ou sucesso do patch, que ainda não foi aplicado nem executado.
O âmbito é deliberadamente o HELLO inicial de saída observado em D11a; eventuais
casos análogos de entrada/REPAIR_HELLO não são corrigidos por inferência.

É necessária autorização específica para estes **dois** caminhos antes de
aplicar. Após autorização: executar o novo teste das três ordens, D11a original,
os testes de lifecycle/handshake pertinentes e uma regressão incremental,
preservando todos os resultados separados. Se houver divergências adicionais,
registar e apresentar antes de alargar a correção. Não repetir até obter verde.

## 4. Estado de Implement

Implement continua pendente. Q-C-36 aplicada; Q-C-37 é investigação concluída
com defeito de ordenação reproduzido, limites históricos explícitos e proposta
por autorizar. Não há aprovação de Implement, Test, fecho, commits, push ou outro
IMP. V-014 consolidado, regressão aprovada, manual humano, revisão final Waheed
e integração mantêm-se pendentes; sem gates externos adicionais.

[Conferência final](preservation-check.json): 247 fontes e 110 artefactos anteriores
byte a byte intactos; HEAD mantido. V-ADD documental passou com zero erros,
[log próprio](planning-validation.log); `git diff --check` e `git apply --check`
passaram. As duas listas geradas rastreadas de target foram restauradas à base.
Não se executou uma nova regressão completa nem a versão proposta do patch.
