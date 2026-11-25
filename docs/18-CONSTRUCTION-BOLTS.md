# Bolts Construction alinhados com o ADD

**IMP-018 / 025-video — concluído (2026-11-25T17:11:00Z):**
Leitor local MJPEG entregue e integrado em maine896643; [fecho](../memory-bank/bolts/025-video/closure-walkthrough.md).
1582/0/0/0 conferido, sem rebuild. JAR fixado e22687 frames completos verificados
(hashes,500/5965/8880/7342, comparação por frame, EOF e fecho único da entrada).
Negativo humano preservado; por instrução posterior do utilizador a execução
pessoal foi substituída por delegação em Codex neste fecho, nova run saída0.
Test aceite por essa condição; Denji reviu/aprovou a versão, confirmado pelo
utilizador. V-ADD0 erros, validador/demo/código aprovados preservados.
20 bolts/histórias complete,0 in-progress,15 planned. TASK-020 apenasA/B,
parteC/IMP-019 não iniciada; pai e PHASE-05 IN PROGRESS, PHASE-06/07 TODO.
Sem push;16MiB apenas raster e R17 original sem causa estabelecida.

**Checkpoint anterior, histórico:**


**IMP-018 — Test aceite por condição expressa; integração local a concluir (2026-11-25T17:06:50Z):**
Quatro binários completos com hashes aprovados,500/5965/8880/7342 frames,
cada Frame comparado, EOF/END e fecho único da entrada. [Prova nova](../experiments/construction/025-video/test-02/full-content-consolidation.json).
Sem rebuild: clean verify1582/0/0/0, JAR fixado e V-ADD conferidos pelo utilizador.
Negativo humano preservado (path sem /media, resposta sisim). O utilizador delegou
nova execução em Codex e autorizou commit/fecho se correto; run automática
saída0 satisfaz a condição. Não se afirma manual humano concluído. Denji reviu
e aprovou esta versão, confirmado explicitamente pelo utilizador.
[Autorização](../experiments/construction/025-video/test-02/acceptance-and-review.json).
Código/negativos/remoções da demo preservados; sem push ou IMP-019.
TASK-020 permanece IN PROGRESS atéA/B/C; integração local antes de marcar025 DONE.

**Checkpoint anterior de Test (histórico):**


**IMP-018 / 025-video — Test em curso, prova integral/manual pendentes (2026-11-25T16:50:39Z):**
Implement aprovado; apenas Test autorizado. [Relatório](../memory-bank/bolts/025-video/ddd-03-test-report.md).
Clean verify JDK21: **1582/0/0/0,23 XML,saída0**; novo JAR preservado com
SHA-256 `f9a3cb9cad6166d13e244a772398179731952fd8eb8ff0efe7b16a6f67b9787f`.
45 testes locais M01–M14 passam; V-020-B continua pendente da leitura dos
quatro conteúdos completos pelo novo VideoSource e do manual humano aplicável.
Ponteiros LFS/cache ausente: sem novos resultados integrais de hash/count/EOF/
cleanup; fixtures equivalentes não os substituem. [Manual preparado](../experiments/construction/025-video/manual-instructions.md),
ensaio técnico pendente por media ausente, sem execução humana presumida.

Os seis problemas V-ADD foram resolvidos pelo patch de3 paths explicitamente
autorizado: fences dos dois templates e referências históricas removidas da
demo. [Validação atual](../experiments/construction/025-video/test-01/v-add-after-patch.md):
**zero erros**, sem alterar/enfraquecer o validador nem repor a demo.
Código aprovado, históricos/negativos e limitação R17 preservados;16MiB
apenas raster. Sem pedido de aprovação Test, fecho, commits/push ou IMP-019.
Revisão final Denji e integração pendentes; TASK-020 só apósA/B/C. Sem gates
externos adicionais; PHASE-05 IN PROGRESS, PHASE-06/07 TODO mantidas.

**Checkpoint anterior de Implement (histórico):**


**IMP-018 / 025-video — Implement pendente de revisão (2026-11-25T16:28:55Z):**
ADR e A05/A06 aprovados; implementados apenas VideoSource.java e
VideoSourceTest.java. [Walkthrough](../memory-bank/bolts/025-video/implementation-walkthrough.md): leitura pull de5ASCII+LJPEG,
paths admitidos, JPEG validado, Frame imutável, guardas antes de decode,
cleanup de cada recurso distinto e erro primário preservado. Raster destino
≤16MiB; não é limite da memória total do decoder/JVM.

Verificações de Implement JDK21: **45 novos testes e regressão incremental
1582/0/0/0, 23 XML (1537 históricos +45 novos)**. Negativo inicial de compilação
preservado e corrigido só no teste. M01–M14 exercitados; V-020-B consolidado
e etapa Test pendentes. Os quatro vídeos estão como ponteiros LFS nesta
checkout: não se executou a leitura integral dos22687 JPEGs com o novo leitor.
Dimensões aprovadas verificadas com fixtures independentes e decoder real.

Seis problemas V-ADD continuam pendentes antes do fecho, sem validação
aprovada; remoções da demo preservadas. Sem Test, fecho, commits/push,
configuração/protocolo, pacing/loop/emissão ou outro IMP. Manual humano
aplicável, revisão final Denji e integração continuam por cumprir;
TASK-020 apenas fecha com A/B/C. Sem gates externos adicionais.
19 bolts complete,1 in-progress,15 planned; histórias19 complete,1 in-progress,
15 planned. R17 inicial continua sem causa estabelecida.

**Checkpoint anterior de ADR (histórico):**


**IMP-018 / 025-video — revisão ADR para aprovação (2026-11-25T16:00:22Z):**
Model eDesign/guardas aprovados; apenas revisão ADR autorizada.
[Revisão](../memory-bank/bolts/025-video/adr-review.md) confere fontes e APIs
JDK21 reais:paths,ownership,ImageReader/ImageReadParam,destino ecleanup.
Sem impedimento de API/ampliação identificado;A01–A08 concretizam o Design.
A05/A06 apresentam duas precisões antes de código:retorno nulo/distinto de
ImageReader com destino explícito é falha inesperada,e imagem extra também
exige cleanup (teto conservador6,no ramo anormal). Sem Implement/aprovaçãoADR.
16MiB limitam raster,não heap total;M01–M14/V-020-B continuam por executar.
Seis problemas V-ADD pendentes antes do fecho,validação não aprovada;remoção
da demo preservada. Dois caminhos VideoSource.java/VideoSourceTest.java.
Sem alteração Java,configuração/protocolo,pacing/loop/emissão,commits ou push.
Fecho:regressãoJDK21,manual aplicável,Denji e integração;TASK-020 só apósA/B/C.
19 bolts complete,1 in-progress,15 planned;nenhum novo gate externo.

**Checkpoint anterior de Design (histórico):**


**IMP-018 / 025-video — Design para revisão (2026-11-25T15:46:47Z):**
Model eIMP-018-LOCAL-01/02 aprovados; apenas Design autorizado.
[Design](../memory-bank/bolts/025-video/ddd-02-technical-design.md): API pull/síncrona,ownership/fecho/erro primário,
zero progresso com falha imediata e limites4096×4096/4194304pixels antes
de decode. Raster de destino≤16MiB;compatível com os22687 registos da
auditoria anterior (A384×288,B/C/D640×360),sem novo decode ou teste.
M01–M14/V-020-B continuam por executar. Dois caminhos VideoSource.java/
VideoSourceTest.java,sem configuração/protocolo,pacing,loop ou emissão.
[Seis pendências V-ADD](../experiments/construction/025-video/design-01/v-add-pendencias.md)
a resolver antes do fecho;validação não aprovada. Remoções da demo preservadas.
19 bolts complete,1 in-progress,15 planned;apenas Model concluído.
Sem leitura/análise/alteração de fontes,Implement,Java/Maven,commits ou push.
Fecho exige testes/regressãoJDK21,manual humano aplicável,Denji e integração.
TASK-020 só fecha apósA/B/C;sem gates externos adicionais.

**Checkpoint anterior de Model (histórico):**


**IMP-018 / 025-video — Model para revisão (2026-11-25T15:32:21Z):**
iniciado por instrução do utilizador após009-arvores/011-video complete.
[Modelo](../memory-bank/bolts/025-video/ddd-01-domain-model.md): leitor sequencial
5ASCII+LJPEG,limites,EOF/erro terminal,fecho e isolamento; propostas locais01/02
por aprovar. M01–M14/V-020-B por executar. Nenhuma etapa aprovada;sem leitura/
análise de fontes,Java,Maven,Design/ADR/Implement/Test,commits ou push.
Totais atuais:19 bolts complete,1 in-progress,15 planned;histórias19 complete/
16 planned. TASK-020 pai e PHASE-05 continuam IN PROGRESS,PHASE-06/07 TODO.
LOCAL-03 sem cópia runtime,LOCAL-04/G-MEDIA-DISTRIBUTION permanecem pendentes
como antes;sem gates externos adicionais. Remoções locais da demo preservadas.

**Reconciliação anterior (histórico):**


Estado reconciliado em 2026-11-22T23:10:04Z: **024-arvores/IMP-014 e 009-arvores/IMP-015 complete; TASK-017/018 DONE**, com V-017/S01–S18 e V-018 executadas. Manual humano e Test aprovados; revisão final de Waheed das duas versões confirmada pelo utilizador. Integração em main comprovada pelos PRs #5 (ce76726) e #6 (1ee8db6). Regressão atual JDK21: **1459/0/0/0, 21 XML**. PHASE-04/unidade004 **DONE/complete**, com coordenação de Fillmore e revisão dos três membros confirmadas pelo utilizador. Total: **17 bolts complete, 0 in-progress, 18 planned**; nenhum novo IMP iniciado. Negativos e hashes preservados. R17 inicial continua sem causa estabelecida; a integração com métricas controladas não demonstra a sua causa. G-ASSESSMENTS mantém a pendência de informação docente sobre data/conteúdo; o marco técnico concluído não prova avaliação ou submissão externa. [Evidência e proveniência das confirmações](../experiments/construction/024-arvores/closure-reconciliation-01/README.md).

## Histórico de preparação e execução

Arquitetura normativa: [add.md](../add.md). Requisitos: [specs.md](../specs.md).
Respostas 1A/2A/3B/4A incorporadas. Sete unidades, 35 tarefas IMP/histórias/bolts, incluindo EXTRA-01;
18 IDs antigos mantidos e 15 acrescentados. Na preparação nenhum bolt estava
iniciado. Estado Construction atual: IMP-001/001-base-validada complete em
2026-11-03T21:56:48Z, após revisão e integração em main; 019-overlay/IMP-002
complete em 2026-11-04T10:33:12Z, com [fecho concluído](../memory-bank/bolts/019-overlay/closure-walkthrough.md).
G-REVIEW-032 cumprido: revisão final por Luis e versão revista integrada em main.
002-overlay/IMP-003 com Model aprovado em 2026-11-04T10:50:52Z/Q-C-04;
[Design preparado para revisão](../memory-bank/bolts/002-overlay/ddd-02-technical-design.md) em 2026-11-04T11:04:53Z.
Design aprovado em 2026-11-04T11:25:40Z, incluindo requestClose; autorizada apenas ADR,
[revisão preparada](../memory-bank/bolts/002-overlay/adr-review.md) em 2026-11-04T11:29:33Z.
ADR aprovada em 2026-11-04T11:37:45Z; apenas Implement autorizado, com [walkthrough](../memory-bank/bolts/002-overlay/implementation-walkthrough.md)
preparado em 2026-11-04T12:08:01Z. 856 testes incrementais aprovados; cinco ficheiros +1226/−35.
Implement aprovado em 2026-11-04T12:18:01Z; apenas Test autorizado, [relatório preparado](../memory-bank/bolts/002-overlay/ddd-03-test-report.md)
em 2026-11-04T12:29:39Z: clean verify com 856 testes e manual Codex/34 checks, saídas 0.
V-006/M01–M18 executados. Test aprovado e concluído, com registo em 2026-11-04T14:05:14Z,
incluindo repetição pessoal por Luis em 2026-11-04T13:58:36Z–13:58:38Z, 34 checks/
MANUAL_PASS/JAVA_EXIT=0, mesmo JAR. [Fecho concluído](../memory-bank/bolts/002-overlay/closure-walkthrough.md)
em 2026-11-04T14:18:23Z: revisão final atribuída a Waheed confirmada pelo utilizador,
registada em 2026-11-04T14:18:23Z; versão revista 03345e9e1ab56ec04302676cca6fce32d168223d integrada em main.
002-overlay/história complete, implemented=true, TASK-006 DONE. Unidade continua in-progress.
003-overlay/IMP-004 iniciado em 2026-11-10T14:01:42Z, apenas Model por seleção explícita.
[Modelo para revisão](../memory-bank/bolts/003-overlay/ddd-01-domain-model.md) preparado em 2026-11-10T14:11:43Z:
155 hashes preservados e M01–M22/V-007 planeados. Nenhuma execução Java nova;
parar antes de Design/ADR/Implement/Test. Revisão final atribuída a Denji e
integração exigidas antes de DONE. Os restantes 29 bolts permanecem planned. [Fecho e prova](../memory-bank/bolts/001-base-validada/closure-walkthrough.md).

## Ordem de execução

| Ordem/IMP | Bolt | Origem | Tamanho | Dependências de bolt |
|---|---|---|---|---|
| IMP-001 | [001-base-validada](../memory-bank/bolts/001-base-validada/bolt.md) | TASK-001, TASK-002, TASK-003, TASK-004, TASK-005 | S | nenhuma |
| IMP-002 | [019-overlay](../memory-bank/bolts/019-overlay/bolt.md) | TASK-032 | M | 001-base-validada |
| IMP-003 | [002-overlay](../memory-bank/bolts/002-overlay/bolt.md) | TASK-006 | M | 001-base-validada, 019-overlay |
| IMP-004 | [003-overlay](../memory-bank/bolts/003-overlay/bolt.md) | TASK-007 | M | 001-base-validada, 002-overlay |
| IMP-005 | [020-overlay](../memory-bank/bolts/020-overlay/bolt.md) | TASK-008 | M | 001-base-validada, 003-overlay, 019-overlay |
| IMP-006 | [004-overlay](../memory-bank/bolts/004-overlay/bolt.md) | TASK-009 | S | 001-base-validada, 020-overlay |
| IMP-034 (extra A) | [034-overlay-extra](../memory-bank/bolts/034-overlay-extra/bolt.md) | TASK-033 | M | 004-overlay |
| IMP-035 (extra B) | [035-overlay-extra](../memory-bank/bolts/035-overlay-extra/bolt.md) | TASK-034 | M | 034-overlay-extra |
| IMP-007 | [005-rotas](../memory-bank/bolts/005-rotas/bolt.md) | TASK-010 | M | 004-overlay, 035-overlay-extra |
| IMP-008 | [021-rotas](../memory-bank/bolts/021-rotas/bolt.md) | TASK-011 | M | 004-overlay, 005-rotas |
| IMP-009 | [006-rotas](../memory-bank/bolts/006-rotas/bolt.md) | TASK-012 | M | 004-overlay, 021-rotas |
| IMP-010 | [022-rotas](../memory-bank/bolts/022-rotas/bolt.md) | TASK-013 | S | 004-overlay, 006-rotas, 020-overlay |
| IMP-011 | [007-arvores](../memory-bank/bolts/007-arvores/bolt.md) | TASK-014 | M | 022-rotas |
| IMP-012 | [023-arvores](../memory-bank/bolts/023-arvores/bolt.md) | TASK-015 | M | 007-arvores, 022-rotas |
| IMP-013 | [008-arvores](../memory-bank/bolts/008-arvores/bolt.md) | TASK-016 | M | 022-rotas, 023-arvores |
| IMP-014 | [024-arvores](../memory-bank/bolts/024-arvores/bolt.md) | TASK-017 | M | 008-arvores, 022-rotas |
| IMP-015 | [009-arvores](../memory-bank/bolts/009-arvores/bolt.md) | TASK-018 | S | 022-rotas, 024-arvores |
| IMP-016 | [010-video](../memory-bank/bolts/010-video/bolt.md) | TASK-019 | M | 009-arvores |
| IMP-017 | [011-video](../memory-bank/bolts/011-video/bolt.md) | TASK-020/A | M | 009-arvores, 010-video |
| IMP-018 | [025-video](../memory-bank/bolts/025-video/bolt.md) | TASK-020/B | M | 009-arvores, 011-video |
| IMP-019 | [026-video](../memory-bank/bolts/026-video/bolt.md) | TASK-020/C | M | 009-arvores, 025-video |
| IMP-020 | [012-video](../memory-bank/bolts/012-video/bolt.md) | TASK-021 | M | 009-arvores, 010-video |
| IMP-021 | [027-video](../memory-bank/bolts/027-video/bolt.md) | TASK-022 | M | 009-arvores, 012-video |
| IMP-022 | [013-video](../memory-bank/bolts/013-video/bolt.md) | TASK-023 | M | 009-arvores, 027-video |
| IMP-023 | [028-video](../memory-bank/bolts/028-video/bolt.md) | TASK-024 | M | 009-arvores, 013-video, 026-video |
| IMP-024 | [014-core](../memory-bank/bolts/014-core/bolt.md) | TASK-025/A | M | 028-video |
| IMP-025 | [029-core](../memory-bank/bolts/029-core/bolt.md) | TASK-025/B | M | 014-core, 028-video |
| IMP-026 | [015-core](../memory-bank/bolts/015-core/bolt.md) | TASK-026/A | M | 028-video, 029-core |
| IMP-027 | [030-core](../memory-bank/bolts/030-core/bolt.md) | TASK-026/B | M | 015-core, 028-video |
| IMP-028 | [016-core](../memory-bank/bolts/016-core/bolt.md) | TASK-027 | M | 028-video, 030-core |
| IMP-029 | [031-core](../memory-bank/bolts/031-core/bolt.md) | TASK-028 | M | 016-core, 028-video |
| IMP-030 | [017-entrega](../memory-bank/bolts/017-entrega/bolt.md) | TASK-029 | M | 031-core |
| IMP-031 | [018-entrega](../memory-bank/bolts/018-entrega/bolt.md) | TASK-030 | M | 017-entrega, 031-core |
| IMP-032 | [032-entrega](../memory-bank/bolts/032-entrega/bolt.md) | TASK-031/A | S | 018-entrega, 031-core |
| IMP-033 | [033-entrega](../memory-bank/bolts/033-entrega/bolt.md) | TASK-031/B | S | 031-core, 032-entrega |

Um IMP por bolt; os critérios/ficheiros/testes estão no ADD secção 8. Não ordenar
bolts/TASK numericamente. TASK-032 vem antes de TASK-006. Pais 020/025/026/031
só fecham após todas as partes, revisão e integração exigidas.

## Iniciar quando solicitado

```text
/specsmd-construction-agent --unit="001-base-validada" --bolt-id="001-base-validada"
```

Depois da base, o primeiro código novo é 019-overlay/TASK-032.

## Verificação documental

```sh
python3 scripts/validate-planning.py
```

O --check-snapshot compara a identidade das fontes no último snapshot, não restaura
estado antigo após execução legítima. Testes Java, GUI e CORE são provas separadas.
3B: UDP-only com TCP saudável é limitação; 4A: sem CI/linter/limiar percentual novos.

## Entradas de revisão

- [ADD e matriz normativa](../add.md).
- [Especificação](../specs.md).
- [Índice de histórias](../memory-bank/story-index.md).
- [Validação](../experiments/inception-planning-validation.md).

Ajuste do Model 003-overlay preparado em 2026-11-10T14:24:46Z: ERROR (17) explícito por fase,
M02/M07/M11 ampliados e Q-C-05 pendente de decisão. Model ainda não aprovado;
Design/ADR/Implement/Test não iniciados, sem código ou provas Java novos.

Continuação 003-overlay em 2026-11-10T15:00:14Z: Model revisto/Q-C-05 aprovados em 2026-11-10T14:48:50Z,
[Design preparado para revisão](../memory-bank/bolts/003-overlay/ddd-02-technical-design.md).
Apenas Model em stages_completed; current_stage=design, completed=null. D01–D22
previstos, V-007 NÃO EXECUTADO; sem Java/teste/build/commit/push ou início de outro IMP.
Revisão final por Denji e integração continuam exigidas; parar antes de ADR.

Continuação 003-overlay em 2026-11-10T15:20:09Z: Design/Q-C-06 aprovados em 2026-11-10T15:13:50Z;
[revisão ADR para validação](../memory-bank/bolts/003-overlay/adr-review.md). Sem divergência material
identificada no desenho; nenhuma ADR nova criada. Apenas Model/Design em
stages_completed, current_stage=adr. D01–D22 mantidos e por executar, sem Java.
Revisão final/integração antes de DONE; parar antes de Implement/Test.

## Fecho anterior — IMP-004

Registo documental em 2026-11-10T17:12:15Z.
IMP-004/003-overlay/TASK-007 fechado documentalmente por autorização explícita
do utilizador. Integração local comprovada em main: `8db7b78`
(`8db7b78950e444f3c2917844617be4b9b236ec0f`), com 145 caminhos e hashes conferidos.
Test aprovado: clean verify/903 testes, manual humano Francisco/49 checks/Java 0
confirmado pelo utilizador, V-ADD aprovado; revisão final de Denji aprovada sem
alterações pendentes. Q-C-08 resolvida e limitada a patch/evidência na integração.
Bolt complete; história complete/implemented=true; TASK-007 DONE. A unidade
002-overlay mantém in-progress: 020-overlay e 004-overlay continuam planned,
sem início de outro IMP. Fecho documental sem alteração Java ou repetição do manual.
Sem push ou alegação de integração remota/entrega do projeto.

[Fecho documental](../memory-bank/bolts/003-overlay/closure-walkthrough.md). As referências anteriores a estados
pendentes são históricas; não descrevem o estado atual. Não se promovem
requisitos globais, PHASE-02, unidade ou intent a concluídos por este fecho.

## IMP-005 — Implement aprovado; Test para revisão

Aprovação de Implement registada em 2026-11-10T22:05:26Z. O [relatório de Test](../memory-bank/bolts/020-overlay/test-walkthrough.md)
consolida V-008, clean verify (981 testes, zero falhas/erros/ignorados), o manual
de Luis e a verificação complementar do agente, com as sondagens preliminares
preservadas. Test aguarda aprovação. A revisão final de Waheed e a integração
da versão revista continuam pendentes.

## Ordem revista — extra antes de 005-rotas

004-overlay → 034-overlay-extra → 035-overlay-extra → 005-rotas. O último está bloqueado até
o extra estar concluído e integrado. [Model](../memory-bank/bolts/034-overlay-extra/ddd-01-domain-model.md).

## Revisão atual do IMP-034 — migração TCP v2

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
MANUAL_PASS, saída Java 0, incluindo TTL real. [Relatório/evidência](../memory-bank/bolts/034-overlay-extra/ddd-03-test-report.md).
Repetição pessoal de Luis, aprovação de Test, revisão final e integração
pendentes. Bolt/história/unidade in-progress, sem DONE/commits/push/IMP-035.

## IMP-034 — fecho integrado

2026-11-11T19:45:51Z: V-033 executado (1065 testes), manual humano Luis/53 checks/saída 0 e Test aceites; revisão técnica final do validador concluída. Versão f650be02f2af10b95328f5b50c3d6941e2e75c15 integrada em main local antes do fecho. TASK-033/bolt/história DONE/complete; Q-C-19 retira arquivos TCP v1, preservando testes operacionais v2 e UDP OTT1. [Fecho](../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md). Sem push. IMP-035 mantém planned e gate próprio; REQ-025 e unidade/fase continuam em curso.

## IMP-035 — Model para revisão

Registo de 2026-11-11T22:22:21Z. Apenas Model iniciado por autorização do utilizador.
Pré-requisito IMP-034 complete e integrado localmente na base 22622c1.
Q-C-20: reparação autónoma só ao ficar isolado (zero ACTIVE), atendimento
possível por peer saudável. Descoberta obrigatória/TCP v2, sem modo opcional.
[Model e M01–M18 propostos](../memory-bank/bolts/035-overlay-extra/ddd-01-domain-model.md). G-EXTRA-ADR próprio pendente: contrato
de intenção/admissão e eventual âmbito adicional exigem revisão antes de código.
Model não aprovado; V-034 não executado, sem Java/Maven/manual/commits/push.
Parar antes de Design. Unidade/REQ-025/extra continuam em curso; 005-rotas não iniciado.

## IMP-035 — Model aprovado; Design para revisão

Registo de 2026-11-11T22:41:27Z. O utilizador aprovou Model e clarificou que o destino
pode estabelecer a ligação pedida pelo isolado, conservando sessões saudáveis.
[Design proposto/Q-C-21](../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md) concretiza intenção/admissão unilateral,
permissões anteriores às falhas, promoção/retorno pelo menor ID, limites e
D01–D24. Âmbito8→30 e minuta para futuraADR-035 ainda propostos; o contrato
integrado/206 hashes de Projeto/JAR continuam intactos.
Só Model concluído, current_stage=design. Gate próprio, Design/ADR/Implement/
V-034/manual/revisão final/integração pendentes. Sem Java/Maven/commits/push.

## IMP-035 — Design direto simplificado para revisão

Registo de 2026-11-11T23:17:55Z. Revisão pedida pelo utilizador: isolado inicia e completa
na mesma ligação mesmo com ID maior, sem retorno. [Design/Q-C-22](../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md)
propõe um tipo20/ACK6, admissão cooperativa sem permissões/registry e eleição
do menor originator só entre saídas pendentes; ACTIVE nunca substituída.
Âmbito8→21/ADR-035/contratos ainda propostos; versão anterior preservada nas
evidências. Só Model concluído; nenhum Java/Maven/V-034/Implement/Test/commit/push.

## IMP-035 — revisão ADR para aprovação antes de Implement

Registo de 2026-11-11T23:28:20Z. Model/Design concluídos; Design revisto, Q-C-22/
política cooperativa e21 caminhos aprovados explicitamente. [RevisãoADR](../memory-bank/bolts/035-overlay-extra/adr-review.md)
formaliza ADR-035 Proposta e substituição parcial019/033/034, mesma ligação e
ACTIVE preservada. APIs reais conferidas sem alteração de fontes.
Só revisão ADR autorizada; aprovação formalADR/G-EXTRA-ADR ainda pendentes.
V-034/regressão/manual humano com mortes reais/revisão final/integração obrigatórios.
Sem Implement/Test/fecho/commits/push/outro IMP.

## IMP-035 — Implement para revisão antes de Test

Registo de 2026-11-12T00:04:01Z. Revisão ADR/ADR-035/substituição parcial019/033/034 aprovadas
explicitamente; ADR-035 Aceite e G-EXTRA-ADR satisfeito para IMP-035.
[Walkthrough, mapa D01–D20 e evidência incremental](../memory-bank/bolts/035-overlay-extra/implementation-walkthrough.md).
Reparação20/6 no mesmo TCP v2, candidatos aprendidos, menor originator só pendente,
ACTIVE preservada e quotas existentes. 78 fixtures operacionais, snapshots/UDP intactos.
Sem clean verify ou manual humano de mortes reais nesta etapa; V-034 completo,
regressão, revisão final e integração pendentes. Não marcar DONE nem iniciar Test.
Sem commits, push ou outro IMP; 005-rotas continua bloqueado.

## IMP-035 — Test à espera de repetição humana

Registo 2026-11-12T00:39:35Z. Implement aprovado; [Test](<../memory-bank/bolts/035-overlay-extra/ddd-03-test-report.md>) executou clean verify
JDK21/1113 testes sem falhas e preservou11 XML/JAR novo. Manual Codex68
CHECK_PASS/MANUAL_PASS: JVM por nó, mortes SIGKILL sequenciais, bootstrap
parado, S–C/R1–C únicos, sobreviventes originais e S–R1 preservada. Duas tentativas
incompletas mantidas. Execução pessoal de Luis/aprovação de Test/revisão final/
integração pendentes. Sem DONE/commits/push ou início de outro IMP.

## IMP-035 — Test aprovado; integração autorizada

Registo 2026-11-12T10:35:23Z. Luis aprovou Test/1113 e o manual humano20261112T102931Z-pr_nynth:
66 CHECK_PASS/MANUAL_PASS, passed=true/no_processes_pending=true. Execução
humana anterior preservada; nenhuma repetição. Cinco etapas concluídas.
[Resumo para revisão humana final](<../memory-bank/bolts/035-overlay-extra/final-review-summary.md>). Commit/main autorizados;
revisão humana final da implementação e fecho pendentes. Bolt/história/unidade
continuam in-progress, sem implemented=true/DONE ou início de005-rotas.

Integração local comprovada:15efdf280b97327cfdc73e9040b9dfed4d4a5658 em main.
Test aprovado; revisão humana final da implementação/fecho pendentes.

## IMP-035 e PHASE-02 concluídos

Registo 2026-11-12T10:46:24Z. Aceitação explícita de fecho pelo utilizador após Test1113/
manual Luis66 e versão integrada em mainff0ef0b. [Fecho e evidência](<../memory-bank/bolts/035-overlay-extra/closure-walkthrough.md>).
Bolt/história/unidade002-overlay complete; TASK-034 e PHASE-02 DONE no âmbito
de controlo incluindo EXTRA-01. Oito bolts complete no projeto;27 planned.
005-rotas desimpedido e planned, sem início. Continuidade de vídeo nas novas
arestas fica para routing/streaming; projeto/intent não concluídos.


## IMP-007 / 005-rotas / TASK-010 — fecho documental autorizado

Registo 2026-11-12T22:37:24Z. Integração local comprovada em main: f9905038c614612b6f2798d4eca46dd72934743f;
348 caminhos/hashes conferidos e V-ADD0 na cópia extraída do commit. Evidência
posterior incluída neste fecho em experiments/construction/005-rotas/integration-proof/,
integration-proof.json e integration-proof.md, preservada byte a byte.
Clean verify1176/0falhas/0erros/0ignorados, manual humano Francisco execução02
com limites documentados, revisão final Fillmore incluindo Q-C-25 e integração
cumpridos. Fecho explicitamente autorizado pelo utilizador: bolt/história
complete, implemented=true, TASK-010 DONE. Unidade003-rotas e PHASE-03 mantêm
in-progress;26 outros bolts planned, nenhum novo IMP iniciado. Código/JAR/logs
intactos; Q-C-23/24 e os dois patches Q-C-25 preservados. Sem Java/push/entrega.
Históricos de pendências acima descrevem os checkpoints anteriores; estado atual
neste fecho e em closure-walkthrough.md. V-ADD documental novo, sem repetir Maven
ou o manual. Commit local limitado a documentação e evidência autorizadas.


## IMP-010 — Plan preparado, aprovação pendente

Registo 2026-11-14T16:16:15Z. Base operacional9144707, main, entrada limpa. Dependências
IMP-006/004-overlay, IMP-009/006-rotas e IMP-005/020-overlay complete;
unidade002-overlay complete. Apenas Plan do bolt Simple022-rotas iniciado,
current_stage=plan, stages_completed=[], completed=null.
Plan define APIs, consulta por salto/alternativas, correlação por Session completa,
visited/TTL/erros/prazos, reservas físicas, ownership e shutdown global.
Q-C-32(composição mínima em OverlayNode) e Q-C-33(detalhes/limites) abertas;
nenhum alargamento ou nova decisão aprovado. V-013/T01–T18 apenas propostos;
nenhum Java/Maven/teste de rede/manual executado nesta etapa.
Plan por aprovar; Implement/Test não iniciados, sem staging/commits/push.
Unidade003-rotas/PHASE-03 continuam em progresso; incrementos fechados intactos.
Manual humano, regressão, revisão final atribuída a Fillmore e integração futuros;
sem gates externos adicionais. Evidência em experiments/construction/022-rotas/.


## IMP-010 — interfaces/tipos e testes inicialmente vermelhos

Registo 2026-11-14T16:29:16Z. Plan e Q-C-32/33 opção1 aprovados; quatro caminhos autorizados,
OverlayNode exclusivamente fachada mínima. Apenas interfaces/stubs/tipos/testes:
13 selecionados,2 passam/11 falham por asserção/0 erros/0 ignorados, exit1 esperado;
compilação concluída, sem comportamentos. Algumas asserções ainda bloqueadas
pelo stub; V-013/T01–T18 integral e integração real não executados.
current_stage=implement; só Plan concluído. Parar antes dos comportamentos.
Test/regressão atual/manual humano/revisão final Fillmore/integração pendentes;
sem gates externos adicionais. Nenhum staging/commit/push/fecho ou outro IMP.
Evidência: experiments/construction/022-rotas/implement-interfaces/.


## IMP-010 — comportamentos de Implement para aprovação

Registo 2026-11-14T17:19:49Z. Utilizador autorizou comportamentos nos quatro caminhos Q-C-32/33
aprovados. Implement preparado; regressão final1296/0falhas/0erros/0ignorados,
17 XML frescos,40 novos testes e1256 históricos byte preservados. CLI/TCP reais,
limites/clock/callbacks/shutdown/ausência de subscrição e vídeo observados no
âmbito dos testes. Falha transitória do teste histórico de saída bloqueada na
regressão02 preservada; repetição isolada e regressão03 passaram sem alterações,
causa não estabelecida. Sem clean verify/JAR novo/manual humano nesta etapa.
current_stage=implement; só Plan concluído, Implement por aprovar, Test não iniciado.
V-013/T01–T18 consolidação formal, clean verify/manual humano, revisão final
Fillmore e integração pendentes; sem gates externos adicionais. target/ separado.
Sem staging/commit/push/fecho ou outro IMP. Evidência:
experiments/construction/022-rotas/implement-behavior/regression-03/result.json.
Walkthrough: memory-bank/bolts/022-rotas/implementation-walkthrough.md.


## IMP-010 — Test técnico executado, manual humano pendente

Registo 2026-11-14T17:45:23Z. Implement aprovado pelo utilizador; apenas Test autorizado.
clean verify em Projeto/:1296 testes/0falhas/0erros/0ignorados,17XML novos;
JAR436556bytes,SHA2565f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52.
40 oráculos de trace/1256 históricos executados; V-013/T01–T18 consolidados
tecnicamente com CLI/TCP reais,limites,ausência de11–14/vídeo no intervalo
observado e shutdown físico. Código/POM aprovados intactos; target/ separado.
Falha histórica m13 em Implement preservada sem causa estabelecida; não
reapareceu no clean verify,sem aumentar prazos ou enfraquecer testes.
Roteiro humano pronto; ensaio kind=agent34checks separado e não conta como
manual humano. Primeira cena do auxiliar corrigida após reparação legítima C–S,
sem mudar código/JAR. current_stage=test; só Plan/Implement concluídos.
Test por aprovar até manual humano; depois revisão final Fillmore/integração.
Sem gates externos adicionais,staging/commits/push/fecho ou outro IMP.
Relatório: memory-bank/bolts/022-rotas/test-walkthrough.md.
Evidência: experiments/construction/022-rotas/test-build-01/result.json.


## IMP-010 — pausa da validação e WIP autorizado

Registo 2026-11-14T17:53:17Z. Utilizador interrompeu a validação e autorizou commit WIP local
limitado ao incremento/documentação/evidência,sem target/ ou alterações alheias.
Plan/Implement aprovados; Test técnico1296/0/0/0,JAR/hash preservados.
Manual humano e aprovação Test pendentes; ensaios agent/parciais preservados e
não substituem execução humana. Falha histórica sem causa estabelecida mantém-se.
current_stage=test,status=in-progress,completed=null; só Plan/Implement concluídos.
TASK-013/história/unidade/fase em progresso; sem implemented=true/DONE/complete.
Sem push/revisão final presumida/integração final/novo IMP. Retoma:
experiments/construction/022-rotas/RESUME.md.
