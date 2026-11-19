---
id: "002-podar-ramos-renovar-leases-e-remover-procura-abandonada"
unit: "004-arvores"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-015"]
implementation_tasks: ["IMP-012"]
requirements: ["INT-001", "INT-002", "NFR-003", "REQ-002", "REQ-005", "REQ-007", "REQ-009", "REQ-011", "REQ-017"]
adr_ids: ["ADR-005", "ADR-024"]
bolt: "023-arvores"
size: "M"
implemented: true
completed: "2026-11-19T22:02:54Z"
---
# História — Renovar e podar subscrições

## Valor e âmbito

Como responsável pelo serviço OTT, quero renovar e podar subscrições, para cumprir os drivers INT-001, INT-002, NFR-003, REQ-002, REQ-005, REQ-007, REQ-009, REQ-011, REQ-017 com validação concreta.

A tarefa normativa é [IMP-012](../../../../../../add.md#imp-012); o bolt só organiza a execução. A origem legada TASK-015 mantém os seus critérios e revisão.

## Critérios de aceitação

- [x] Vídeo só permanece autorizado enquanto há procura efetiva.
- [x] Ramos órfãos desaparecem por timeout.
- [x] Outros consumidores e fluxos mantêm-se ativos.
- [x] Validação V-015 executada e evidência ligada no progresso.

## Testes

- V-015: sair um de dois consumidores mantém tronco ativo; sair o último remove; relógio falso elimina lease órfão; cancelamento velho não remove ramo novo.

## Fonte, dependências e revisão

- [TASK-015](../../../../../../docs/tasks/PHASE-04/TASK-015.md).
- Bolts anteriores: 007-arvores, 022-rotas.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: nenhum específico adicional.

## Estado de construção

Histórico das etapas; o checkpoint atual consta no final.

Model aprovado em023-arvores, base operacional b273b258; apenas Design autorizado.
Q-C-40/41/42 preservadas; tipos/testes vermelhos autorizados, comportamentos não autorizados.
V-015 não executado, nenhum critério marcado como cumprido.
[Model](../../../../../bolts/023-arvores/ddd-01-domain-model.md).

Model aprovado pelo utilizador;
[Design por aprovar](../../../../../bolts/023-arvores/ddd-02-technical-design.md).
Somente Design autorizado; V-015/L01–L24 por executar, sem ADR/Implement/Java.

Design aprovado pelo utilizador;
[revisão ADR para aprovação](../../../../../bolts/023-arvores/adr-review.md).
Sem nova ADR ou alteração de âmbito; Implement não autorizado, V-015 pendente.

[Checkpoint de tipos/testes vermelhos](../../../../../bolts/023-arvores/implementation-walkthrough.md):
22 novos/20 falhas esperadas,30 históricos selecionados verdes. Nenhum critério
marcado como concluído; V-015 integral e comportamento pendentes.

## Checkpoint atual — Implement dos comportamentos

Primeiro passo aprovado; renovação e poda autorizadas/implementadas nos três
ficheiros. [Walkthrough e resultados atuais](../../../../../bolts/023-arvores/implementation-walkthrough.md),
com preservação de Q-C-40–42/M07 e TCP real. História em progresso,
implemented=false; Implement por aceitar e Test não autorizado. Clean verify/JAR,
manual humano, revisão final Denji e integração pendentes. Sem staging/commit/push.

## Checkpoint atual — Test autorizado, por aprovar

Implement aprovado pelo utilizador. [Relatório Test](../../../../../bolts/023-arvores/ddd-03-test-report.md):
clean verify1379/0/0/0,19 XML,V-015/L01–L24,JAR/hash e código aprovado intacto.
[Roteiro humano](../../../../../../experiments/construction/023-arvores/manual-instructions.md)
preparado; execução humana pendente. Ensaio kind=agent não conta como manual.
Depois faltam aprovação Test, revisão final Denji e integração. História permanece
in-progress, sem implemented=true/complete/staging/commits/push/fecho.

## Test reapresentado — manual humano Francisco cumprido

Seis etapas pessoais confirmadas sem divergências,30 checks,JAVA_EXIT0,
hashes/origem/JVM conferidos. [Evidência](../../../../../../experiments/construction/023-arvores/manual-human-verification-01/verification.json).
WRAPPER_EXIT não fornecido/não inferido. Test continua por aprovar; revisão final
Denji e integração pendentes. História in-progress,sem implemented=true/complete.
Sem staging/commits/push/fecho; código/JAR e evidências anteriores intactos.

## Test aprovado — aguarda revisão final e integração

Utilizador aprovou Test1379/0/0/0,19 XML,V-ADD e Francisco30.
[Resumo final para Denji](../../../../../bolts/023-arvores/final-review-summary.md)
e allowlist/diffs preparados; revisão final não confirmada e integração pendente.
História in-progress, sem implemented=true/complete; código/JAR/logs/negativos
preservados. Sem staging/commits/push/fecho/outro IMP.

## Revisão final Denji confirmada — integração pendente

Utilizador confirmou aprovação final da implementação atual sem alterações pendentes.
[Hashes/simulação/plano](../../../../../bolts/023-arvores/integration-plan.md),
mesmo código/JAR/Test/manual. História in-progress,sem implemented=true/complete,
aguardando integração comprovada. Sem staging/commits/push/fecho/outro IMP.

## Fecho autorizado — estado atual

História complete,implemented=true,TASK-015 DONE após autorização explícita.
Integração main4d18d1dd8fb50d14ace02609a9133733c20cabfc comprovada;
[Test/V-015](../../../../../bolts/023-arvores/ddd-03-test-report.md),1379/0/0/0,
19 XML,manual humano Francisco30,revisão final Denji e V-ADD cumpridos.
[Fecho e limitações](../../../../../bolts/023-arvores/closure-walkthrough.md).
Unidade004-arvores/PHASE-04 em progresso,sem Java/push/novo IMP no fecho.
Registos anteriores são snapshots históricos,sem reescrita de evidências.
