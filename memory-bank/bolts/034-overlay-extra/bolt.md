---
id: "034-overlay-extra"
unit: "002-overlay"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "in-progress"
stories: ["006-reconstruir-vizinhancas-apos-falhas"]
created: "2026-11-11T12:11:34Z"
updated: "2026-11-11T19:40:54Z"
started: "2026-11-11T12:11:34Z"
completed: null
current_stage: "test"
stages_completed: [{"name": "model", "completed": "2026-11-11T12:41:00Z", "artifact": "ddd-01-domain-model.md"}, {"name": "design", "completed": "2026-11-11T17:55:50Z", "artifact": "ddd-02-technical-design.md"}, {"name": "adr", "completed": "2026-11-11T17:55:50Z", "artifact": "adr-034-review.md"}, {"name": "implement", "completed": "2026-11-11T19:22:10Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-11T19:40:54Z", "artifact": "ddd-03-test-report.md"}]
stage_history: [{"name":"design","completed":"2026-11-11T13:38:55Z","artifact":"ddd-02-technical-design.md","revision":"OTD1 separate, suspended by user","archive_removed":"Q-C-19; explicit user cleanup","artifact_sha256":"1df2c1572282173ec5d04ac7312e6d3a49a3a57af2124101d11b6804b50cd82d"},{"name":"adr","completed":"2026-11-11T13:57:14Z","artifact":"adr-review.md","revision":"OTD1 separate, suspended by user","archive_removed":"Q-C-19; explicit user cleanup","artifact_sha256":"5c42483e2d61b199105548603cde7552174ce070f6d02da2d1e91630bef1aa92"}]
suspended_stage: null
gate_status: {"G-EXTRA-ADR":"satisfied for IMP-034 only; revised TCP v2 contracts approved"}
requires_bolts: ["004-overlay"]
enables_bolts: ["035-overlay-extra"]
requires_units: ["001-base-validada"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 3
  max_dependencies: 2
  testing_scope: 3
execution_agent: "construction"
source_phase: "PHASE-02"
source_tasks: ["TASK-033"]
source_task_order: ["TASK-033"]
implementation_tasks: ["IMP-034"]
size: "M"
validation_ids: ["V-033"]
test_ids: ["V-033"]
external_gates: ["G-EXTRA-ADR"]
adr_ids: ["ADR-007", "ADR-009", "ADR-012", "ADR-013", "ADR-014", "ADR-015", "ADR-017", "ADR-018", "ADR-019", "ADR-020", "ADR-024", "ADR-033", "ADR-034"]
requirements: ["REQ-025"]
architecture_source: "add.md"
extra: true
human_test_operator: "Luis"
final_technical_reviewer: "Codex validator"
closure_authorized_by: "Luis"
---
# Bolt 034-overlay-extra — Extra — aprender candidatos através dos vizinhos

EXTRA-01, parte A, aprendizagem.
[IMP-034](../../../add.md#imp-034) é a fonte normativa.
[História](../../intents/001-ott-streaming/units/002-overlay/stories/006-reconstruir-vizinhancas-apos-falhas.md).

## Etapas

1. [x] **model** → `ddd-01-domain-model.md`.
2. [x] **design** → `ddd-02-technical-design.md`.
3. [x] **adr** → `adr-034-review.md` (adr-review.md conserva a revisão OTD1 histórica).
4. [x] **implement** → `implementation-walkthrough.md`.
5. [x] **test** → `ddd-03-test-report.md`.

## Critérios de aceitação

- [ ] Aprender endpoints a partir de anúncios limitados de vizinhos diretos em sessões confirmadas, sem pré-carregar um catálogo global.
- [ ] Guardar proveniência e prazo local por candidato; a morte do anunciante não elimina automaticamente os candidatos ainda válidos.
- [ ] Limitar anúncios, cache, filas e frequência; rejeitar informação inválida, obsoleta, conflitante e o próprio nó segundo política aprovada.
- [ ] Migrar todo o controlo TCP para um único contrato/codec v2, incluindo bootstrap, mensagens normais e descoberta; manter framing/hard caps/código 3 rejeitado e garantias anteriores, validar fixtures v2 independentes, com retirada dos arquivos TCP v1 autorizada em Q-C-19, sem runtime v1/fallback ou canal separado.
- [ ] V-033 comprova aprendizagem, retenção após falha do anunciante, expiração, limites e regressão atual.

## Histórico — revisão de transporte interrompida

Implement OTD1 interrompido por decisão explícita do utilizador; código e
execuções preservados. [Design revisto](ddd-02-technical-design.md) e
[ADR-034 Proposta](../../../add.md#adr-034) usam TCP persistente de controlo,
codec/framing e FIFO comuns; revisão atual migra todo TCP para v2 e mantém as políticas de cache aprovadas.
G-EXTRA-ADR reaberto; apenas Model conta como concluído para o contrato atual.
Aprovações anteriores de Design/ADR ficam em stage_history e no arquivo.
Sem continuação de Java, commits/push, Test ou outro IMP antes de aprovação.

## Histórico das aprovações e entrada na variante interrompida

### Estado registado antes da revisão

Model aprovado explicitamente pelo utilizador, registado em 2026-11-11T12:41:00Z.
[Design aprovado](ddd-02-technical-design.md): canal TCP isolado
OTD1 em dataPort, vínculo ao HELLO atual, geração por sessão, TTL local de 60 s,
cache 128×4 e limites físicos comuns com o controlo.
[Q-C-17](../../../docs/construction-duvidas.md#q-c-17--design-da-descoberta-extra-proposta)
aprovada explicitamente com Design; aprovação registada em 2026-11-11T13:38:55Z.
[Revisão ADR para aprovação](adr-review.md): coerência dos contratos, retenção,
confiança e recursos comuns; explicitar em ADR-033 a chave adicional face a
ADR-010/016. ADR-033 continua Proposta e G-EXTRA-ADR pendente de aprovação
formal da ADR e dos contratos afetados. Parar antes de Implement/Test.
V-033, regressão nova,
manual, revisão humana final e integração por executar. IMP-035/V-034 trata
a reparação; a prova de vídeo ocorre após routing/streaming.

## Aprovação ADR e entrada em Implement

Registo de 2026-11-11T13:57:14Z: utilizador aprovou revisão ADR, ADR-033 e contratos
de descoberta/configuração/transporte do IMP-034. ADR-033 Aceite, G-EXTRA-ADR
satisfeito apenas para este IMP. Implement autorizado nos ficheiros do ADD,
com testes D01–D12 e preservação dos anteriores. Parar antes de Test/fecho.
V-033, regressão atual, manual, revisão humana e integração continuam obrigatórios.
Sem commits, push ou outro IMP. IMP-035 mantém checkpoint próprio.

## Ajuste de fila/conteúdo ainda para aprovação

> Histórico: as exceções opcionais/base e eventual falha global foram
> substituídas pelas clarificações posteriores de obrigatoriedade e isolamento.

Design/ADR-034 revistos com margem M=1, Q=1 em base, quota extra de um
enfileirado e rejeição atómica de conteúdo corrente. Framing/truncamento/
identidade mantêm tratamento terminal. D07a–e/D08a–f pendentes, sem retoma Java.

## Revisão atual — extra requerido

> Histórico: as exceções opcionais/base e eventual falha global foram
> substituídas pelas clarificações posteriores de obrigatoriedade e isolamento.

Q=1 degradado/fallback foram rejeitados pelo utilizador. Normal exige extra
ativo em todos os overlay, Q>=2 offline e required=1 na negociação. Base/API
Q=1 preservados; erros explícitos sem ACTIVE_BASE required. Design/ADR-034
pendentes; código interrompido. README normal incluído no âmbito futuro.

## Revisão atual — sem ativação opcional

Requisito obrigatório em todos os nós/API/perfis overlay, sem ACTIVE_BASE.
Ausência de chave exige, false é erro, Q>=2 antes de recursos.
[Justificação dos testes](test-contract-migration.md) para aprovação; todos os
bytes/formatos v1 usados e invariantes preservados. Código congelado, gate pendente.

## Revisão atual — programa único e isolamento de falhas

Registo de 2026-11-11T17:22:59Z. Mesmo programa/versão do JAR em todos os nós; descoberta
obrigatória e validação estrita do handshake. Incompatibilidade é input negativo,
rejeitado só na tentativa, sem NeighborUp/FAILED global/FATAL/fim da CLI ou
queda de sessões saudáveis. Estado por peer e retry limitados, com backoff
existente, sem suporte a versões antigas/fallback. D02b–d/D11a acrescentados.
[Revisão Design/ADR-034](adr-034-review.md) para aprovação; Implement suspenso,
G-EXTRA-ADR pendente. IMP-034 aprende/retém; IMP-035 repara depois.

## Revisão atual — todo o controlo TCP v2

Registo de 2026-11-11T17:47:08Z. Utilizador decidiu v2 também em REGISTER/REGISTER_OK/ERROR do
bootstrap, mensagens normais e descoberta. Um contrato/MessageCodec/ControlMessage,
18 tipos explícitos e código 3 rejeitado; sem runtime v1/fallback. Âmbito proposto
29 caminhos, incluindo bootstrap/testes/recursos e removendo ControlFrame.
Arquivo v1 preservado e 50 vetores TCP v2 preparados/auditados offline; UDP
OTT1/16 fixtures permanecem. Migração de testes justificada, sem execução Java.
Design/ADR-034 ainda para aprovação, G-EXTRA-ADR pendente, Implement suspenso.
Cache/TTL/proveniência/filas/shutdown/isolamento preservados. IMP-035 não iniciado.

## Aprovação e retoma de Implement — TCP v2

Registo de 2026-11-11T17:55:50Z; instante do registo, não data atribuída à mensagem humana.
O utilizador aprovou explicitamente Design revisto, ADR-034, âmbito dos 29
caminhos e migração justificada dos testes/recursos para TCP exclusivamente v2.
ADR-034 Aceite: substitui o contrato TCP v1 e as propostas OTD1/mista/ativação
opcional afetadas; v1/fixtures ficam arquivados, UDP OTT1 permanece inalterado.
G-EXTRA-ADR satisfeito para IMP-034; formalizada revisão ADR e retoma apenas
Implement. Um contrato/codec v2 no bootstrap e vizinhos, descoberta obrigatória,
com framing/caps/identidade/unicidade/FIFO/flush/isolamento/shutdown e cache/TTL/
proveniência preservados. Testes incrementais com logs reais autorizados.
Parar para revisão de Implement antes de Test/fecho; V-033, regressão atual,
manual, revisão humana e integração continuam exigidos. IMP-035 mantém checkpoint
próprio; sem commits/push ou início de outro IMP.

## Checkpoint atual — Implement para revisão

Registo de 2026-11-11T18:57:34Z. Código v2/aprendizagem implementados, com 1062 testes
incrementais aprovados (JDK 21, TCP local e CLI reais). Ver
[walkthrough](implementation-walkthrough.md) para mapa/evidência/remoções/diff.
Implement ainda aguarda revisão; current_stage permanece implement e Test não
foi iniciado. clean verify, manual, revisão humana final e integração pendentes;
sem DONE/commits/push. IMP-035 não iniciado.

## Checkpoint atual — correção D07e para revisão de Implement

Registo de 2026-11-11T19:18:38Z. Token HELLO antigo e geração antiga são descartados
sem fechar a sessão nem terminar a consulta atual; identidade incompatível
continua terminal. Três testes novos reproduziram e verificaram as correções.
142 testes incrementais afetados passaram com JDK 21/TCP local. Os 1062 da
apresentação anterior permanecem evidência histórica, sem alegar nova regressão
completa. [Walkthrough atualizado](implementation-walkthrough.md).
Implement aguarda aprovação; Test, clean verify, manual humano, revisão final
e integração pendentes. Sem commits/push, DONE ou início de IMP-035.

## Aprovação de Implement e entrada em Test

Registo de 2026-11-11T19:22:10Z (instante do registo). O utilizador aprovou Implement,
incluindo as correções D07e, e autorizou apenas Test: clean verify completo
com JDK 21 e manual V-033 reproduzível. Evidência incremental preservada e
separada da regressão atual. Parar antes da aprovação de Test para repetição
humana; fecho exige critérios, revisão final e integração. Sem commits/push
ou início do IMP-035.

## Estado atual — IMP-034 em Test para repetição humana

Registo de 2026-11-11T19:31:01Z. Implement aprovado; clean verify JDK 21 executado
com 1065 testes, zero falhas/erros/skips. Manual Codex: 53 CHECK_PASS,
MANUAL_PASS, saída Java 0, incluindo TTL real. [Relatório/evidência](ddd-03-test-report.md).
Repetição pessoal de Luis, aprovação de Test, revisão final e integração
pendentes. Bolt/história/unidade in-progress, sem DONE/commits/push/IMP-035.

## Estado de fecho

Test aceite pela instrução de Luis após execução pessoal de 53 checks/MANUAL_PASS/saída 0. Revisão técnica final do agente validador concluída; integração local pendente. Q-C-19 autoriza retirar os arquivos TCP v1. O histórico acima não descreve o estado atual. IMP-035 permanece planned.
