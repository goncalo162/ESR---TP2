# NeighborDown — investigação consolidada durante Implement

Registo 2026-11-17T18:31:55Z. Nenhum teste histórico ou fundação foi alterado.
Os negativos anteriores continuam em ../neighbor-down-baseline, com os hashes
originais. [report.json](report.json) liga logs, XML, teste controlado e proposta.

## Factos estabelecidos

1. O teste histórico espera apenas que monitoringSnapshot().links deixe de conter
N1 (linha95). Na instrução seguinte exige que a lista do observador externo já
contenha NeighborDown com a Session antiga (linha96).
2. OverlayNode.onNeighborEvent chama monitor.onEvent, depois routing.onNeighborEvent
e só no finally chama observer.onEvent. O monitor publica o snapshot durante o
seu processamento. Logo, a condição esperada pelo teste pode ficar verdadeira
antes da entrega ao observador. Não existe relação de happens-before que a
condição do teste estabeleça entre essas duas observações.
3. O novo diagnóstico ActivationTest.neighborDownSnapshotCanPrecedeExternalObserverDelivery
usa OverlayNode/TCP reais, uma barreira limitada no observador e as APIs públicas.
Com o mesmo NeighborDown retido antes de adicionar à lista, o snapshot já não
contém N1 e o evento ainda não está observado; ao libertar a barreira, o evento
da **mesma Session esperada** chega e o nó não apresenta falha. Não se mudou a
ordem dos eventos na produção nem se contornou a API por reflexão.
4. A repetição isolada original, inalterada, passou em2026-11-17T17:54:54Z:
[log preservado](original-isolated-reproduction.log). Nas regressões incrementais
1322 e1324 o teste original também passou, com os mesmos prazos e asserções.
Estes sucessos não apagam os dois negativos anteriores.

Conclusão: há uma corrida de observação reproduzida no contrato que o teste
assume. Não é necessário nem correto reordenar o monitor/observador na produção
para satisfazer essa suposição. Os logs históricos não contêm uma cronologia
que permita reconstruir a interleaving exata de cada execução negativa; não
se atribui a todas uma sequência concreta que não foi medida. O diagnóstico
comprova a lacuna de sincronização e a eventual entrega, não vídeo ou desempenho.

## Correção mínima proposta — NÃO APLICADA

[Patch para revisão](proposed-test-synchronization.patch): alterar apenas a
condição do until para esperar conjuntamente pela remoção no snapshot **e**
pelo NeighborDown da Session esperada. A assertTrue original mantém-se, tal como
o timeout20s do teste e o limite5s/poll5ms do until. Sem sleeps arbitrários,
asserções removidas, skips, aumento de prazo ou alterações de produção.

O alvo é Projeto/src/test/java/pt/uminho/esr/ott/overlay/OverlayNodeMonitoringTest.java,
fora dos quatro caminhos autorizados de IMP-011. É necessária autorização
específica antes de aplicar; o patch é só um artefacto de apresentação.
A eventual aplicação deve ser seguida da verificação pertinente, mantendo
os negativos e os logs novos separados. Não está declarada uma correção aplicada
ou uma dispensa dessa apresentação prévia por a regressão atual estar verde.

## Estado de aceitação

Investigação consolidada e proposta concreta apresentável. A decisão sobre a
correção fora de âmbito permanece pendente; não se apresenta Implement como
aceite nem se avança para Test/fecho. Esta é a restrição de âmbito pedida pelo
utilizador, não um gate externo novo. V-014 consolidado, regressão aprovada,
manual humano, revisão final Waheed e integração continuam exigidos para fecho.
