# Bolts Construction alinhados com o ADD

Arquitetura normativa: [add.md](../add.md). Requisitos: [specs.md](../specs.md).
Respostas 1A/2A/3B/4A incorporadas. Sete unidades, 33 tarefas IMP/histórias/bolts;
18 IDs antigos mantidos e 15 acrescentados. Nenhum bolt iniciado.

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
