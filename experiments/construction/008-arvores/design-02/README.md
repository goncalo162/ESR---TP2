# IMP-013 — Design reapresentado: RESOURCE_LIMIT temporário

Alteração exclusivamente documental pedida pelo utilizador. [Autorização/entrada](entry.json)
e [Design anterior preservado](design-input.md), sem reescrever model-01/design-01.
[Design revisto](../../../../memory-bank/bolts/008-arvores/ddd-02-technical-design.md).

Ajustes limitados à secção4 e à variante R13: pressão temporária de admissão pode
terminar em UNAVAILABLE; libertar callback/capacidade sozinho não reabre terminal.
ANN validado recebido depois da falha e com geração superior ao watermark pode
fornecer um crédito coalescido mesmo com Session/época/path iguais, sujeito a
capacidade, T/A, cadência e validade. O terminal bloqueia a prova já observada,
mesmo quando não houve envio, impedindo reutilização por timer. Duplicados não
rearmam e não se exige novo pedido humano. Capacidade ainda indisponível não é
sucesso nem permite ignorar limites; falha nova atualiza a proteção.

Q-C-44 regista a clarificação aprovada; Design global continua por aprovar.
Três caminhos do ADD e restantes políticas preservados. V-016/R01–R18 NÃO
EXECUTADOS, sem leitura/análise/alteração de fontes, Maven, ADR/Implement,
commits, push, outro IMP ou gate externo adicional. Fecho exige testes,
regressão JDK21, manual humano, revisão Fillmore e integração.

[Validação documental](v-add.md), [conferência de âmbito](status-and-scope.json).
