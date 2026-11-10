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

## Implement preparado para validação

ADR explicitamente aprovada; Q-C-07 resolvida com reposição externa autorizada
de EventLoop/RealDeadlines. [Implement para checkpoint](../../../../../bolts/003-overlay/implementation-walkthrough.md)
preparado em 2026-11-10T16:12:36Z. Regressão incremental: `mvn -B -ntp test`,
903 testes (740 configuração +66 codec +31 TCP +19 bootstrap +47 vizinhança),
zero falhas/erros/ignorados. V-007 tem execução automatizada TCP local;
Q-C-05/Q-C-06 e D01–D22 preservados. Três ficheiros do IMP +1745/−0;
BootstrapServer externo +482/−142 separado, 151 ficheiros restantes preservados.
Implement ainda aguarda aprovação; Test/clean verify/JAR/manual não iniciados,
revisão final atribuída a Denji e integração pendentes. TASK/bolt/história/unidade
continuam em progresso, sem implemented=true, commits/push ou fecho.

## IMP-004 — Test preparado, aceitação pendente

Implement aprovado explicitamente pelo utilizador; [relatório Test](../../../../../bolts/003-overlay/ddd-03-test-report.md)
preparado em 2026-11-10T16:26:35Z. Clean verify atual: 903 testes (47 de vizinhança),
zero falhas/erros/ignorados. Manual interativo executado pelo agente Codex no
JAR verificado: 49 CHECK_PASS, MANUAL_PASS/CLEANUP_COMPLETE, saída Java 0.
XML/JAR/hash/perfis/log e instruções de repetição pessoal preservados em
experiments/construction/003-overlay/. Código aprovado e ferramentas intactos
durante Test. V-007/D01–D22 têm consolidação técnica local, com limites explícitos.
Q-C-08 continua aberta: V-ADD reprovado com três erros, sem alteração de ferramentas
ou redução do alcance. Test ainda não aprovado; revisão final humana e integração
pendentes. TASK/bolt/história/unidade permanecem em progresso, sem implemented=true,
commits/push/fecho ou GUI/CORE presumidos.

## Manual humano aprovado — Test ainda aberto

Registo de conferência em 2026-11-10T16:38:44Z.
Manual humano executado e aprovado por Francisco, confirmado pelo utilizador.
Conferência do log original: 49 CHECK_PASS distintos, zero CHECK_FAIL,
MANUAL_PASS CHECKS=49 e CLEANUP_COMPLETE; hash do JAR coincide com o clean verify
e os cinco hashes de configuração coincidem com os ficheiros da execução.
JAVA_EXIT=0 é confirmação explícita do utilizador; esse marcador não consta
do log original do driver, que foi preservado sem edição.
Test continua por aprovar até tratar V-ADD. Q-C-08 continua aberta; correção
mínima preparada e ensaiada apenas em cópia isolada, não aplicada às ferramentas.
Sem fecho/commits/push; revisão humana final e integração continuam pendentes.

[Log Francisco](../../../../../../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log) · [Conferência](../../../../../../experiments/construction/003-overlay/manual-Francisco-evidence.json) · [Proposta Q-C-08](../../../../../../experiments/construction/003-overlay/q-c-08-proposal.md).

## Test para aprovação — Q-C-08 resolvida

Estado registado em 2026-11-10T16:41:30Z. Patch Q-C-08 aplicado exclusivamente
como autorizado: [diff +5/−3](../../../../../../experiments/construction/003-overlay/q-c-08-applied.diff), três ficheiros, restante conteúdo
local preservado. [V-ADD real](../../../../../../experiments/construction/003-overlay/q-c-08-v-add-applied.log) passou: saída 0, zero erros;
validador/alcance intactos. Q-C-08 resolvida. As anteriores referências a
V-ADD reprovado são históricas. Código Java/testes/configurações sem alteração;
clean verify de 903 testes e JAR/hash/XML preservados. Manual humano Francisco
cumprido e aprovado, 49 checks/Java 0. [Checkpoint Test atualizado](../../../../../bolts/003-overlay/ddd-03-test-report.md).
Test ainda por aprovar, current_stage=test, completed=null; não há fecho,
commits/push ou nova integração. Revisão humana final e integração pendentes.

## IMP-004 — Test aprovado; fecho pendente

Test aprovado explicitamente pelo utilizador, registo em 2026-11-10T16:44:29Z.
903 testes do clean verify aprovados, manual humano de Francisco cumprido
(49 checks/Java 0) e V-ADD aprovado; Q-C-08 resolvida.
[Resumo/diff para revisão humana final](../../../../../bolts/003-overlay/final-review-summary.md).
As cinco etapas estão concluídas; bolt in-progress, current_stage=null,
completed=null. TASK/história/unidade continuam em progresso, sem implemented=true
ou DONE. Não há gates externos adicionais; faltam revisão humana final e
integração. Não executar o fecho nem fazer commits/push nesta autorização.

## Revisão final Denji aprovada — integração por autorizar

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Registo da confirmação](../../../../../../experiments/construction/003-overlay/denji-final-review-confirmation.json).
