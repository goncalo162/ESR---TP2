# Validação do ADD e planeamento Construction

Execução: 2026-11-25T15:47:53Z. Base: 85e1b33ff0a1de69116dfdf3e7ee2d2352cbf936.
Comando: python3 scripts/validate-planning.py --report experiments/construction/025-video/design-01/v-add.md

## Resultado

- Requisitos/constraints/intents: 41; 35 aliases e critérios legados preservados.
- ADD: 13 secções; 29 ADR com driver/alternativa; matriz 41/41 sem órfãos.
- Unidades: 7; histórias/IMP/bolts: 35/35/35; TASK pai: 34.
- DAG sem ciclos; dependências de todas as partes e gates de fase preservados; nenhum IMP L.
- Protocolo operacional: 62 fixtures TCP v2 independentes (19 tipos), 16 UDP OTT1 intactas face à base; arquivos TCP v1 retirados por decisão explícita Q-C-19.
- Fixtures verificadas: 78; XML CORE analisáveis: 3 (sem execução).
- Referências locais/âncoras verificadas: 5479.
- Erros encontrados: 6.

## Alcance

Validação estrutural/documental e de bytes de referência. Não executa Maven,
testes Java, rede entre processos, GUI, CORE ou submissão. Os 149 testes
continuam a ser evidência histórica, não resultado desta execução.
A validação não substitui a aprovação humana dos artefactos/tarefas; não executa bolts.

## Erros

- /home/luis/ESR/ESR---TP2/.specsmd/aidlc/skills/inception/bolt-plan.md: unclosed code fence
- /home/luis/ESR/ESR---TP2/.specsmd/aidlc/skills/operations/deploy.md: unclosed code fence
- docs/15-PROJECT-PROGRESS.md: missing target ../memory-bank/demonstrations/DEMO-001-checkpoint-phase04/bolt.md
- docs/15-PROJECT-PROGRESS.md: missing target ../demo/DEMO.md
- docs/15-PROJECT-PROGRESS.md: missing target ../demo/MATRIX.md
- docs/15-PROJECT-PROGRESS.md: missing target ../memory-bank/demonstrations/DEMO-001-checkpoint-phase04/HANDOFF.md
