# Q-C-36 — exceção autorizada e verificação incremental de Implement

Retoma de 2026-11-17. O utilizador autorizou exclusivamente o patch apresentado
em `OverlayNodeMonitoringTest.java`, como exceção aos quatro caminhos originais
do IMP-011. A autorização não aceita Implement nem permite Test, fecho, commits,
push ou outro IMP. V-014 consolidado, regressão aprovada, manual humano,
revisão final Waheed e integração continuam pendentes; sem gates externos novos.

## Alteração exata

Foi aplicado, sem ajustes, o
[patch apresentado](../neighbor-down-investigation/proposed-test-synchronization.patch).
O `until` aguarda conjuntamente snapshot sem N1 e NeighborDown da Session antiga
esperada. Todas as asserções, timeout de 20 s, limite de 5 s/poll de 5 ms e
restantes testes mantêm-se. Nenhuma alteração adicional de Java ou produção.

## Resultados novos

Verificação incremental durante Implement, com Java 21.0.13 e Maven offline.
Os comandos exatos estão nos JSON; logs e XML são independentes da prova anterior.

| Execução | Testes/falhas/erros/ignorados | Evidência |
|---|---|---|
| NeighborDown focado, após o patch | 1/0/0/0 | [log](focused.log), [resultado e comando](focused-result.json), [XML](TEST-pt.uminho.esr.ott.overlay.OverlayNodeMonitoringTest.xml) |
| Regressão completa após o patch | 1324/1/0/0 | [log negativo](regression.log), [resultado/comando/18 XML](regression/result.json) |
| D11a repetido isoladamente, sem alterações | 1/0/0/0 | [log](d11a-isolated.log), [resultado/comando/XML](d11a-isolated/result.json) |
| Regressão completa repetida, sem alterações | 1324/1/0/0 | [segundo log negativo](regression-recheck.log), [resultado/comando/18 XML](regression-recheck/result.json) |

Nas duas regressões, OverlayNodeMonitoringTest passou com os seus 11 testes e
ActivationTest passou com os seus 28 testes, incluindo o diagnóstico controlado
`neighborDownSnapshotCanPrecedeExternalObserverDelivery`, inalterado.

A única falha de ambas é
`OttApplicationTest.D11aIncompatibleInputRejectsOnlyPeerWhileCliAndDiscoveryRemainUsable`,
em `OttApplicationTest.java:562`, via `LiveChild.await` na linha 156. A condição
exige conjuntamente REQUIRED_DISCOVERY_MISSING, NeighborUp e
DISCOVERY_CACHE_REJECTION no log de cada filho. O log negativo preservado contém
NeighborUp e DISCOVERY_CACHE_REJECTION, mas não REQUIRED_DISCOVERY_MISSING para
o filho N1 antes do prazo. Há diagnósticos TRANSPORT/TIMEOUT; isso não prova a
causa da ausência. O teste passou isoladamente entre os dois negativos.

A causa desta dependência do contexto de execução não está estabelecida.
Não se atribui a falha ao patch ou se declara que já existia sem prova. Nenhum
teste foi apagado, enfraquecido ou corrigido, nem se alterou handshake, transporte,
fundação, prazo ou configuração para obter um resultado verde. Não se repete a
suite até obter um sucesso; os dois negativos permanecem resultados de revisão.
Qualquer correção adicional exige apresentação prévia e autorização de âmbito.

## Preservação e estado para revisão

[before.json](before.json) inventaria os hashes dos 59 ficheiros de evidência
anteriores, incluindo os três negativos de NeighborDown, o diagnóstico/XML,
o relatório da investigação e o patch original. Esses ficheiros mantêm-se como
registos históricos, sem reescrever os seus estados anteriores à autorização.
[preservation-check.json](preservation-check.json) confere a preservação,
a igualdade exata do patch e o âmbito de cinco caminhos, sendo o quinto apenas
a exceção Q-C-36. As quatro fontes originais mantêm os hashes da regressão02.

Q-C-36 aplicada; Implement reapresentado para revisão, sem aprovação presumida.
A regressão incremental atual tem uma falha por resolver. A etapa permanece
`implement`, apenas Model/Design/ADR estão concluídos e V-014 não foi consolidado
ou aceite. A execução incremental não conclui os 14 cenários formais.

Conferência final: 59 ficheiros de evidência anterior byte a byte intactos,
250 ficheiros rastreados restantes de Projeto/CORE inalterados e os hashes dos
quatro caminhos originais preservados. As duas listas geradas rastreadas de
target foram restauradas à base. V-ADD documental passou com zero erros;
[log próprio](planning-validation.log). `git diff --check` passou. Sem mudanças
de HEAD, commits, push ou transição de etapa.
