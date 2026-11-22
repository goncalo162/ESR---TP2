# Implement — protótipo pausado

A implementação desta iteração está em `demo/`, fora das fases funcionais e
de Projeto/src. O pedido de paragem do utilizador interrompe a evolução no
checkpoint do protótipo; não há aprovação Implement/Test da demo ou fecho.

Arquitetura: controller Java21 + processos reais, um adaptador por nó que usa a
factory de composição de OverlayNode para capturar o RoutingService existente;
NetworkMonitor real, sem métricas injetadas. Proxy por endpoint inspeciona HELLO,
encaminha frames originais com delay por aresta e escreve evidência de wire.
O HTML deriva dos snapshots, sem lógica de escolha/ativação ou player.

Lançamento único `./demo/run.sh cenario2`; `next` conduz os passos, `--auto`
automatiza. Perfis por nó, validação, PID/instance, CLI real, snapshots e cleanup
integram os oráculos. As capacidades e os comandos do auxiliar estão no
[bolt](bolt.md); não são novos comandos da CLI de produção.

O protótipo compilou e o cenário1 teve uma run31/31; não existe ainda série
de três. Cenário2 terminou com negativo na troca por qualidade; cenário3 não
executado. Oráculos ajustados e limitações estão integralmente no
[handoff](HANDOFF.md); não se atribui resolução funcional aos ajustes do observador.

Nenhum teste histórico, asserção ou prazo foi alterado. Nenhuma correção de
produção foi aplicada. Estilo de referência: docs/style/CODESTYLE.md. Revisão
da ferramenta, robustez e interface continuam tarefas da próxima iteração.
