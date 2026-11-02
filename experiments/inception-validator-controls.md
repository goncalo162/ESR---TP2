# Controlos negativos do validador ADD

Execução: 2026-11-02T15:57:23Z. Baseline positivo e nove mutações rejeitadas em cópia temporária de /tmp.
Nenhuma mutação foi aplicada aos documentos de trabalho. Sem testes Java/GUI/CORE.

| Mutação | Rejeição observada |
|---|---|
| dependência necessária removida | lost TASK dependency |
| validação V-006 removida | missing V-XXX |
| etapa test alterada | execution stages |
| referência quebrada | missing target |
| critério de requisito alterado | canonical acceptance criterion changed |
| decisão divergente do índice | ADD/index mismatch in decision |
| default operacional divergente | parameter contract differs |
| ADR sem driver | orphan/unknown driver |
| requisito sem linha na matriz | traceability matrix missing |

O verificador foi importado com ROOT apontado para uma cópia completa sem .git/target.
Cada mutação foi revertida antes da seguinte. São controlos do checker documental,
sem novas dependências ou framework de testes. [Validação principal](inception-planning-validation.md).
