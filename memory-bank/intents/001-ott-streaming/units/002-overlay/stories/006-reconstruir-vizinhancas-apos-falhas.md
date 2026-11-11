---
id: "006-reconstruir-vizinhancas-apos-falhas"
unit: "002-overlay"
intent: "001-ott-streaming"
status: "complete"
priority: "Must"
created: "2026-11-11T12:11:34Z"
source_tasks: ["TASK-033"]
implementation_tasks: ["IMP-034"]
requirements: ["REQ-025"]
adr_ids: ["ADR-007", "ADR-009", "ADR-012", "ADR-013", "ADR-014", "ADR-015", "ADR-017", "ADR-018", "ADR-019", "ADR-020", "ADR-024", "ADR-033", "ADR-034"]
bolt: "034-overlay-extra"
size: "M"
extra: true
updated: "2026-11-11T19:45:51Z"
implemented: true
completed: "2026-11-11T19:45:51Z"
---
# História — Extra — aprender candidatos através dos vizinhos

Como nó, quero aprender os endpoints dos vizinhos dos meus vizinhos durante a execução e retê-los por prazo limitado, para disponibilizar candidatos à reparação posterior.
[IMP-034](../../../../../../add.md#imp-034).

## Critérios de aceitação

- [x] Aprender endpoints a partir de anúncios limitados de vizinhos diretos em sessões confirmadas, sem pré-carregar um catálogo global.
- [x] Guardar proveniência e prazo local por candidato; a morte do anunciante não elimina automaticamente os candidatos ainda válidos.
- [x] Limitar anúncios, cache, filas e frequência; rejeitar informação inválida, obsoleta, conflitante e o próprio nó segundo política aprovada.
- [x] Migrar todo o controlo TCP para um único contrato/codec v2, incluindo bootstrap, mensagens normais e descoberta; manter framing/hard caps/código 3 rejeitado e garantias anteriores, validar fixtures v2 independentes, com retirada dos arquivos TCP v1 autorizada em Q-C-19, sem runtime v1/fallback ou canal separado.
- [x] V-033 comprova aprendizagem, retenção após falha do anunciante, expiração, limites e regressão atual.

## Histórico — validação e revisão

V-033. Responsável proposto Denji, revisor proposto Fillmore.
[Bolt](../../../../../bolts/034-overlay-extra/bolt.md).

Model/Design e Q-C-17 aprovados pelo utilizador. [Revisão ADR para aprovação](../../../../../bolts/034-overlay-extra/adr-review.md); ADR-033/contratos ainda propostos. V-033, regressão atual, manual, revisão humana e integração pendentes.

## Histórico — revisão de transporte

[Design revisto](../../../../../bolts/034-overlay-extra/ddd-02-technical-design.md) e ADR-034 propostos para TCP persistente de controlo,
codec/framing e fila comuns. OTD1 separado interrompido, com código/evidência
preservados. Cache/TTL/proveniência/callbacks mantêm-se. G-EXTRA-ADR reaberto;
não retomar Java, Test ou fecho antes de nova aprovação.

## Estado atual — Implement para revisão

Registo de 2026-11-11T19:06:59Z. Design/ADR-034/29 caminhos aprovados;
ADR-034 Aceite, G-EXTRA-ADR satisfeito para IMP-034. Implementação TCP v2 única
e aprendizagem com TTL/proveniência/limites concluídas para revisão.
[Walkthrough](../../../../../bolts/034-overlay-extra/implementation-walkthrough.md):
1062 testes incrementais aprovados; V-033 de fecho ainda parcial.
Test, clean verify, manual, revisão final e integração pendentes; sem DONE,
commits/push ou início de IMP-035. Os critérios acima aguardam validação final.

## Estado atual — correção D07e para revisão

Registo de 2026-11-11T19:18:38Z. Corrigidos o descarte de token HELLO antigo
e de geração antiga, preservando a sessão e a consulta corrente. 142 testes
incrementais afetados passaram com JDK 21/TCP local; os 1062 da apresentação
anterior ficam como histórico. [Walkthrough, red/green e evidência](../../../../../bolts/034-overlay-extra/implementation-walkthrough.md).
Implement aguarda revisão. Test/clean verify/manual humano/revisão final/integração
continuam pendentes; sem DONE, commits/push ou início de IMP-035.

## Estado atual — IMP-034 em Test para repetição humana

Registo de 2026-11-11T19:31:01Z. Implement aprovado; clean verify JDK 21 executado
com 1065 testes, zero falhas/erros/skips. Manual Codex: 53 CHECK_PASS,
MANUAL_PASS, saída Java 0, incluindo TTL real. [Relatório/evidência](../../../../../bolts/034-overlay-extra/ddd-03-test-report.md).
Repetição pessoal de Luis, aprovação de Test, revisão final e integração
pendentes. Bolt/história/unidade in-progress, sem DONE/commits/push/IMP-035.

## Fecho atual

2026-11-11T19:45:51Z: complete/implemented=true; V-033 e manual humano aceites, revisão técnica final concluída e versão f650be02f2af10b95328f5b50c3d6941e2e75c15 integrada em main local. [Fecho](../../../../../bolts/034-overlay-extra/closure-walkthrough.md). IMP-035 não iniciado.
