# Bolts Construction alinhados com o ADD

**Estado atual:** IMP-006/004-overlay complete e TASK-009 DONE após Test, revisão confirmada e integração de `cc478d1` em main. A unidade 002-overlay está completa. [Registo de fecho](../memory-bank/bolts/004-overlay/closure-walkthrough.md).

Arquitetura normativa: [add.md](../add.md). Requisitos: [specs.md](../specs.md).
Respostas 1A/2A/3B/4A incorporadas. Sete unidades, 33 tarefas IMP/histórias/bolts;
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
| IMP-007 | [005-rotas](../memory-bank/bolts/005-rotas/bolt.md) | TASK-010 | M | 004-overlay |
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
