# Reconciliação ADD e bolts

Data: 2026-11-02T15:50:12Z. Respostas recebidas: 1A/2A/3B/4A.
Normativo: [ADD](../add.md) e [specs](../specs.md).

- 40 entradas: 24 REQ, cinco NFR, nove CON e dois INT.
- 26 ADR; ADR-003 Substituída por ADR-013, restantes Aceites como desenho.
- Sete unidades, 33 histórias/IMP/bolts; 18 IDs preservados e 15 acrescentados.
- TASK-032/V-032 introduz defaults/overrides antes do bootstrap.
- TASK-020 divide-se em conteúdos/leitor/pacing; 025 em preflight/partilha;
  026 em falhas/qualidade; 031 em defesa/submissão humana.
- Nenhum watchdog UDP ou ferramenta nova de qualidade; limitação UDP explícita.
- Java, POM, fixtures e XML não foram alterados. Nenhum bolt/build Java/GUI/CORE
  executado; os 149 testes mantêm a sua natureza histórica.

O validador verifica matriz/drivers/alternativas/etapas/DAG e identidade das
fontes. O índice JSON é derivado e não decide arquitetura. Os gates de media,
GUI/CORE, revisão TASK-032 e entrega continuam abertos na fase responsável.
