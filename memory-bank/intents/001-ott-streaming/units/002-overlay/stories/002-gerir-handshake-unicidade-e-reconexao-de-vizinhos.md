---
id: "002-gerir-handshake-unicidade-e-reconexao-de-vizinhos"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "in-progress"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-007"]
implementation_tasks: ["IMP-004"]
requirements: ["INT-001", "INT-002", "NFR-004", "REQ-002", "REQ-003", "REQ-004", "REQ-021"]
adr_ids: ["ADR-012", "ADR-013", "ADR-018", "ADR-019", "ADR-021", "ADR-024"]
bolt: "003-overlay"
size: "M"
---
# História — Estabelecer e reconectar vizinhos

## Model preparado para revisão

Iniciado em 2026-11-10T14:01:42Z; [modelo concreto](../../../../../bolts/003-overlay/ddd-01-domain-model.md)
preparado em 2026-11-10T14:11:43Z, base Denji 26aaa4ae6775e9c78e1d7658472ccaca0b376427.
Dependências complete/integradas; três ficheiros autorizados vazios e 155 hashes
preservados. Estados/sessões/handshake/reconexão e M01–M22 para V-007 planeados,
sem implementação ou execução Java. Model ainda não aprovado; parar antes de
Design/ADR/Implement/Test. Revisão final atribuída a Denji e integração antes
de DONE, sem implemented=true, commits/push ou outro IMP.

## Valor e âmbito

Como responsável pelo serviço OTT, quero estabelecer e reconectar vizinhos, para cumprir os drivers INT-001, INT-002, NFR-004, REQ-002, REQ-003, REQ-004, REQ-021 com validação concreta.

A tarefa normativa é [IMP-004](../../../../../../add.md#imp-004); o bolt só organiza a execução. A origem legada TASK-007 mantém os seus critérios e revisão.

## Critérios de aceitação

- [ ] Uma conexão confirmada por vizinho.
- [ ] Entrada tardia e reconexão funcionam.
- [ ] Endereço UDP só é aceite após handshake.
- [ ] Validação V-007 executada e evidência ligada no progresso.

## Testes

- V-007: três nós iniciados em ordens diferentes convergem para o grafo esperado sem duplicados; novo instanceId após reinício não é apagado por callback antigo; vizinho desconhecido é rejeitado; após descoberta, desligar bootstrap e comprovar reconexão direta sem novas consultas.

## Fonte, dependências e revisão

- [TASK-007](../../../../../../docs/tasks/PHASE-02/TASK-007.md).
- Bolts anteriores: 001-base-validada, 002-overlay.
- Responsável de origem: Waheed; revisor: Denji.
- Gates: nenhum específico adicional.

## Ajuste de Model — ERROR (17)

Em 2026-11-10T14:24:46Z, preparado o ajuste pedido pelo utilizador, sem aprovação de Model:
[tratamento por fase](../../../../../bolts/003-overlay/ddd-01-domain-model.md#8-autorização-error-17-e-entrega-ao-consumidor)
e M02/M07/M11 ampliados; entrega intacta de erros ACTIVE ao consumidor e proibição
de ERROR em resposta a ERROR. Políticas não fixadas pelas fontes em
[Q-C-05](../../../../../../docs/construction-duvidas.md#q-c-05--receção-de-error-no-imp-004), pendentes.
Manter Model para revisão; nenhum Java/Design, teste, commit ou push novo.

## Model aprovado — Design para revisão

Model revisto e propostas da tabela Q-C-05 aprovados explicitamente em 2026-11-10T14:48:50Z.
[Design](../../../../../bolts/003-overlay/ddd-02-technical-design.md) preparado em 2026-11-10T15:00:14Z:
assinaturas/tipos, retenção limitada, DNS/correlação de dials, dono serial partilhável,
ERROR por fase e D01–D22. Esta continuação supera as pendências históricas acima.
Nenhum Java/teste/build novo; parar antes de ADR/Implement/Test. Revisor final
atribuído Denji e integração antes de DONE; sem commits/push/fecho.

## Design aprovado — ADR para revisão

Design/Q-C-06 aprovados explicitamente em 2026-11-10T15:13:50Z, preservando Q-C-05.
[Revisão ADR](../../../../../bolts/003-overlay/adr-review.md) preparada em 2026-11-10T15:20:09Z:
owner/limites físicos/retenção/correlação conformes no desenho e sem nova ADR
necessária identificada. D01–D22 mantidos; V-007 e restantes provas funcionais
pendentes. Nenhum Java, teste/build, commit/push ou fecho; parar antes de Implement/Test.
