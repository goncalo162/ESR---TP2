# TASK-033 — Extra — aprender candidatos através dos vizinhos

## 1. Identificação

- ID: TASK-033
- Fase: PHASE-02, EXTRA-01 posterior ao fecho da base.
- Estado: DONE
- Natureza: Extra solicitado pelo utilizador.
- Responsável proposto: Denji.
- Revisão técnica final: Codex validador, solicitada por Luis; aceitação humana e manual: Luis (Q-C-19).
- Dependências: [TASK-009](TASK-009.md).
- Fonte: [REQ-025](../../../specs.md#req-025), [IMP-034](../../../add.md#imp-034).

## 2. Objetivo e âmbito

Divulgar vizinhos diretos e aprender candidatos com proveniência, TTL e limites. TCP v2 único no bootstrap e vizinhos; descoberta obrigatória/Q>=2 offline. ADR-034 Aceite e G-EXTRA-ADR satisfeito para este IMP. Test e manual humano aprovados; revisão técnica final concluída, integração local concluída. IMP-035 não iniciado.

## 3. Critérios de aceitação

- [x] Aprender endpoints a partir de anúncios limitados de vizinhos diretos em sessões confirmadas, sem pré-carregar um catálogo global.
- [x] Guardar proveniência e prazo local por candidato; a morte do anunciante não elimina automaticamente os candidatos ainda válidos.
- [x] Limitar anúncios, cache, filas e frequência; rejeitar informação inválida, obsoleta, conflitante e o próprio nó segundo política aprovada.
- [x] Migrar todo o controlo TCP para um único contrato/codec v2, incluindo bootstrap, mensagens normais e descoberta; manter framing/hard caps/código 3 rejeitado e garantias anteriores, validar fixtures v2 independentes, com retirada dos arquivos TCP v1 autorizada em Q-C-19, sem runtime v1/fallback ou canal separado.
- [x] V-033 comprova aprendizagem, retenção após falha do anunciante, expiração, limites e regressão atual.

## 4. Validação e fecho

V-033: TCP v2 em todos os 18 tipos, APIs, bootstrap servidor/cliente, handshake, mensagens normais e descoberta; código 3 e versões antigas rejeitados sem parser/fallback v1. Fixtures v2 independentes, sem gerar o esperado pelo codec de produção; UDP OTT1/16 fixtures intactos face à base integrada. Arquivos TCP v1 retirados por decisão explícita Q-C-19. Todos os overlay exigem descoberta/Q>=2, ausência/true redundante obrigatórios e false/Q=1 agregados offline; genérico/bootstrap Q=1 preservado. Validar versão/identidade/endpoint/capacidade/flush/unicidade, rejeição só da tentativa e estado/retry por peer limitados com backoff; A ACTIVE/CLI/aprendizagem utilizáveis quando B é rejeitado. Bootstrap v2 conserva grafo imutável/offline, instâncias zero na resposta, flush antes de EOF, prazos e shutdown. Cache/TTL/proveniência/gerações/callbacks antigos/conflitos/caps com relógio/transporte/escrita controlados; FIFO/margem/tickets físicos, conteúdo extra recuperável e framing/identidade terminais. Migrar testes sem eliminar as garantias; executar regressão JDK 21, V-ADD, TCP/CLI reais e manual após aprovação.
Estado: V-033 executado, Test aceite por Luis após manual pessoal, revisão técnica final concluída. Versão revista integrada em main local antes de DONE.
[Bolt](../../../memory-bank/bolts/034-overlay-extra/bolt.md).

## Revisão de transporte em curso

[Design revisto](../../../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md) e ADR-034 propostos para TCP persistente de controlo,
codec/framing e fila comuns. OTD1 separado interrompido, com código/evidência
preservados. Cache/TTL/proveniência/callbacks mantêm-se. G-EXTRA-ADR reaberto;
não retomar Java, Test ou fecho antes de nova aprovação.

## Clarificação operacional da revisão pendente

Registo de 2026-11-11T17:22:59Z. Um programa/versão de JAR; todos exigem descoberta.
Inputs inválidos são negativos do handshake, sem suporte a versões antigas.
Rejeitar só a tentativa, sem NeighborUp/falha global/fecho da CLI ou das sessões
saudáveis; estado/retry limitados com backoff existente. D02b–d/D11a verificam
um peer inválido enquanto outro permanece ACTIVE e alimenta candidatos.
[Revisão ADR-034](../../../memory-bank/bolts/034-overlay-extra/adr-034-review.md)
pendente de aprovação, sem continuar Java. IMP-035 permanece para reparação.

## Revisão de âmbito — bootstrap e controlo v2

Registo de 2026-11-11T17:47:08Z. Decisão explícita: TCP exclusivamente v2 em bootstrap,
handshake, mensagens normais e descoberta; um contrato/codec, 18 tipos explícitos,
código 3 rejeitado, sem runtime v1/fallback. Arquivo v1 e fixtures v2 independentes
preparados; migração dos testes justificada com garantias preservadas. Bootstrap
continua só inicial, UDP OTT1 inalterado, descoberta obrigatória e isolamento por
peer/CLI saudável mantidos; cache/TTL/proveniência/shutdown conservados.
IMP-034 aprende, IMP-035 repara depois. Design/ADR-034/âmbito para aprovação;
Implement suspenso, sem execução Java, commits, push ou outro IMP.

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

## IMP-034 — Implement pronto para revisão

Registo de 2026-11-11T18:57:34Z. ADR-034 Aceite e G-EXTRA-ADR satisfeito apenas para IMP-034.
Controlo TCP exclusivamente v2 implementado no bootstrap, handshake, mensagens
normais e descoberta obrigatória; um codec/FIFO/writer, sem canal OTD1/runtime
v1 ou fallback. Cache/TTL/proveniência, isolamento e shutdown preservados.
Conjunto incremental JDK 21: 1062 testes, zero falhas/erros; inclui TCP local e
JVM da CLI. Corrigida a corrida descoberta/shutdown detetada por teste existente,
com novo oráculo controlado; falhas/sondagens anteriores mantidas.
66 fixtures operacionais (50 TCP v2 independentes e 16 UDP intactas); arquivo
v1 de 45 fixtures verificado separadamente. Ver walkthrough do bolt para logs,
XML, hashes, diff e mapa D01–D12; não confundir teste incremental com clean verify.
Pendente aprovação humana de Implement antes de Test. V-033 completo, clean
verify/regressão atual, manual reproduzido, revisão final e integração continuam
necessários para fecho. Sem commits/push ou outro IMP; IMP-035 continua planeado.

## Estado atual — IMP-034 em Test para repetição humana

Registo de 2026-11-11T19:31:01Z. Implement aprovado; clean verify JDK 21 executado
com 1065 testes, zero falhas/erros/skips. Manual Codex: 53 CHECK_PASS,
MANUAL_PASS, saída Java 0, incluindo TTL real. [Relatório/evidência](../../../memory-bank/bolts/034-overlay-extra/ddd-03-test-report.md).
Repetição pessoal de Luis, aprovação de Test, revisão final e integração
pendentes. Bolt/história/unidade in-progress, sem DONE/commits/push/IMP-035.

## Revisão final atual

[Fecho e evidência](../../../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md). Manual Luis/53 checks e clean verify/1065 testes aprovados. Estado atual: DONE após integração local; as secções datadas anteriores são histórico.

Fecho registado em 2026-11-11T19:45:51Z, revisão f650be02f2af10b95328f5b50c3d6941e2e75c15 integrada em main local. [Prova](../../../experiments/construction/034-overlay-extra/closure-integration.json). Nenhuma publicação remota nesta retoma.
