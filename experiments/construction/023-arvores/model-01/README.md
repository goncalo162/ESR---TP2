# IMP-012 / 023-arvores — checkpoint Model

[Model](../../../../memory-bank/bolts/023-arvores/ddd-01-domain-model.md), por aprovar.
Base main/b273b258a4642f94f8614ae00f060a46318d77a5, entrada limpa.
Dependências007-arvores/IMP-011 e022-rotas/IMP-010 complete, fechos preservados.

[Entrada e hashes](entry.json), [inspeção estática](source-inspection.json),
[preservação](preservation.json), [V-ADD inicial](v-add-initial.md).

Q-C-40/41/42 decididas explicitamente: revisão crescente por renovação com
mesmo ID/pedido novo; recusa de RENEW divergente mantendo grant vigente; três
ficheiros, cancelamento por ticket/DEACTIVATE e composição CLI posterior.
24 grupos L01–L24/V-015 planeados, nenhum teste Java executado. Não se contam
os1328 testes históricos de IMP-011 como resultado atual.

Somente documentação e metadados de Model. Sem Design/ADR/Implement/Test,
staging, commits/push ou outro IMP. Diff/numstat contra a base e relatório
final de validação documental guardados nesta pasta.

[Diff contra a base, incluindo Model novo](model.diff),
[numstat](model.numstat), [inventário de ficheiros novos](new-files.json),
[V-ADD final, zero erros](v-add-final.md).
