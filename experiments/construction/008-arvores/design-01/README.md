# IMP-013 / 008-arvores — Design para revisão

Model/Q-C-43/44 aprovados; apenas Design autorizado. [Entrada](entry.json) e
[snapshot exato do Model apresentado](model-input.md), cujo hash coincide com o
manifesto original de model-01. Os artefactos originais de model-01 permanecem
byte a byte intactos; atualizar a aprovação no Model vivo não reescreve essa prova.

[Design proposto](../../../../memory-bank/bolts/008-arvores/ddd-02-technical-design.md):
novidade útil F/G, watermark/crédito, T/A e uma ronda automática por T; renegociação
automática dos filhos com pedido11 fresco, sem intervenção humana ou extensão
dos prazos antigos. Três caminhos do ADD preservados. Construtores/projeções e
lifecycle serão confrontados com APIs reais na revisão ADR autorizada, não aqui.

V-016/R01–R18 NÃO EXECUTADOS. Sem leitura/análise/alteração de fontes, Maven,
ADR/Implement/Test, commits, push ou outro IMP. Nenhum gate externo novo.
Fecho mantém testes, regressão JDK21, manual humano, Fillmore e integração.

[Validação documental](v-add.md) e [estado/âmbito](status-and-scope.json).
O validador não executa oráculos de recuperação; propostas não são implementação.
