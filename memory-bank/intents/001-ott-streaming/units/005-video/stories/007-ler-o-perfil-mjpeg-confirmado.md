---
id: "007-ler-o-perfil-mjpeg-confirmado"
unit: "005-video"
intent: "001-ott-streaming"
status: "in-progress"
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-020"]
implementation_tasks: ["IMP-018"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
bolt: "025-video"
size: "M"
updated: "2026-11-25T16:50:39Z"
---
# História — Ler o perfil MJPEG confirmado

## Valor e âmbito

Como responsável pelo serviço OTT, quero ler o perfil mjpeg confirmado, para cumprir os drivers INT-001, INT-002, NFR-001, REQ-012, REQ-013, REQ-015, REQ-018 com validação concreta.

A tarefa normativa é [IMP-018](../../../../../../add.md#imp-018); o bolt só organiza a execução. A origem legada TASK-020 é parcial (B); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [ ] Ler L bytes após cinco dígitos ASCII; EOF só é limpo entre registos.
- [ ] Prefixo inválido/parcial, zero, excesso de tamanho, truncatura e JPEG inválido têm erro localizado antes de publicação.
- [ ] Fixtures independentes produzem os bytes esperados e o leitor fecha recursos.

## Testes

- V-020-B: ficheiros de dois frames, leituras parciais, cada truncatura e limite; teste de ImageIO sem GUI.

## Fonte, dependências e revisão

- [TASK-020](../../../../../../docs/tasks/PHASE-05/TASK-020.md).
- Bolts anteriores: 009-arvores, 011-video.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: nenhum específico adicional.

## Implement para revisão — 2026-11-25T16:28:55Z

[Walkthrough](../../../../../../memory-bank/bolts/025-video/implementation-walkthrough.md):45 testes novos e regressão incremental
JDK21 com1582/0/0/0,23 XML. M01–M14 exercitados; V-020-B consolidado e Test
pendentes, critérios ainda não aceites. Sem leitura integral dos quatro vídeos
com o novo leitor: checkout contém ponteiros LFS, não materializados.
Seis erros V-ADD pendentes antes do fecho; demo não reposta. Sem Test/fecho,
commits/push; manual humano aplicável, Denji e integração mantidos.

## Implement aprovado; Test técnico — 2026-11-25T16:50:39Z

[Relatório](../../../../../../memory-bank/bolts/025-video/ddd-03-test-report.md): clean verify JDK21 com1582/0/0/0,
23 XML e JAR novo/SHA-256. M01–M14 locais passam; V-020-B/Test pendentes
por binários completos A/B/C/D ausentes e manual humano ainda não executado.
Ponteiros/fixtures equivalentes não satisfazem essa prova. V-ADD0 erros após
patch autorizado, validador/demo/código preservados. História in-progress,
sem aceitação/fecho/commits/push; Denji e integração continuam obrigatórios.

## Test aceite por condição expressa; revisão confirmada — 2026-11-25T17:06:50Z

[Relatório](../../../../../../memory-bank/bolts/025-video/ddd-03-test-report.md):22687 frames reais, hashes aprovados, comparação de cada
Frame,500/5965/8880/7342, EOF/END e fecho único. Sem rebuild ou alteração Java.
Negativo pessoal preservado; posterior delegação de execução em Codex e
commit/fecho condicional ao resultado correto. Nova run automática saída0;
Test aceite por essa condição, sem afirmar manual humano concluído. Denji
reviu e aprovou a versão, confirmado pelo utilizador. Integrar localmente antes
de concluir025; sem push/IMP-019. TASK-020 apenas fecha apósA/B/C; R17 preservado.
