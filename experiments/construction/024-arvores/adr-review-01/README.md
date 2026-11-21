# IMP-014 — revisão ADR, decisão Q-C-47 pendente

Design aprovado; registo2026-11-21T14:46:03Z. Autorizada apenas revisão ADR com
leitura de APIs reais. [Relatório](../../../../memory-bank/bolts/024-arvores/adr-review.md)
e [Q-C-47](../../../../docs/construction-duvidas.md) preparados para decisão humana.
Só Model/Design concluídos; ADR não aprovado, história planned/não implementada.

A01–A07 identificam adaptações internas nos três caminhos do ADD: proveniência,
custo do ramo confirmado, ronda única T/A, captura antes da submissão, ordem de
publicação/poda, suspensão dos filhos e reservas/lifecycle existentes.
São propostas conferidas estaticamente, não implementação/testes aprovados.

A08 é impedimento de observabilidade: Event não tem dados de troca e OverlayNode
ignora eventos não fatais. Q-C-47-A propõe manter os três caminhos com projecção
consultável e alteração explícita do contrato; Q-C-47-B mantém logs automáticos,
mas exigiria excepção de dois caminhos adicionais. **Nenhuma foi autorizada ou
aplicada.** O ADD/allowlist continuam com três caminhos. Não rebaixar o critério
de logs nem alterar o Design aprovado silenciosamente.

`authorization.json` identifica a autorização e o hash de
`approved-design-input.tar.gz`. Esse arquivo conserva os20 documentos/artefactos
do manifesto design-01 mais o manifesto, verificados antes de actualizar estados.
O arquivo anterior do Model mantém-se dentro da evidência congelada. Os manifests
históricos descrevem essas versões, não os documentos correntes actualizados.

`source-inventory.json` guarda hashes dos fontes Java rastreados, igualdade com
HEAD e inventário dos ficheiros inspeccionados. `verification.json`, `v-add.md` e
`document-hashes.sha256` preservam verificações actuais. Não substituem os hashes
históricos nem afirmam ter executado V-017. Nenhum fonte/teste/fundação/fixture
foi alterado; leitura de dependências não é ampliação de âmbito de escrita.

Sem Implement/Test/Maven, commit/push, fecho ou outro IMP. V-017/S01–S18 continuam
**não executados**. Regressão JDK21, manual humano **antes de aprovar Test**,
revisão final **Waheed** e integração continuam obrigatórios. Sem gate externo
adicional: Q-C-47 é a apresentação prévia de impedimento exigida pelo utilizador
e ADR-024, não um gate externo novo.

R17 inicial de IMP-013 permanece **sem causa estabelecida**. Integração com
métricas controladas não demonstra a causa do negativo histórico. Logs, negativos,
sucessos, manual/JAR/hashes anteriores permanecem intactos.
