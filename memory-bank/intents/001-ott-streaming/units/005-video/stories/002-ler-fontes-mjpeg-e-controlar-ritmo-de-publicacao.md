---
id: "002-ler-fontes-mjpeg-e-controlar-ritmo-de-publicacao"
unit: "005-video"
intent: "001-ott-streaming"
status: "complete"
implemented: true
priority: "Must"
created: "2026-11-02T15:36:48Z"
source_tasks: ["TASK-020"]
implementation_tasks: ["IMP-017"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
bolt: "011-video"
size: "M"
---
# História — Preparar quatro conteúdos distintos

## Valor e âmbito

Como responsável pelo serviço OTT, quero preparar quatro conteúdos distintos, para cumprir os drivers INT-001, INT-002, NFR-001, REQ-012, REQ-013, REQ-015, REQ-018 com validação concreta.

A tarefa normativa é [IMP-017](../../../../../../add.md#imp-017); o bolt só organiza a execução. A origem legada TASK-020 é parcial (A); só fecha quando todas as partes e critérios do pai estiverem satisfeitos.

## Critérios de aceitação

- [x] Quatro conteúdos identificáveis, dois por origem, preparados no perfil de cinco dígitos/JPEG.
- [x] Proveniência, parâmetros de preparação, hashes e FPS registados; cópias idênticas não contam como vídeos distintos.
- [x] Cada JPEG respeita o limite e é descodificável; condições de redistribuição ainda pendentes ficam explícitas para TASK-030.

## Testes

- V-020-A: inspeção de todos os registos/JPEG e comparação de conteúdos/hashes; recurso indisponível mantém gate G-MEDIA.

## Fonte, dependências e revisão

- [TASK-020](../../../../../../docs/tasks/PHASE-05/TASK-020.md).
- Bolts anteriores: 009-arvores, 010-video.
- Responsável de origem: Fillmore; revisor: Denji.
- Gates: G-MEDIA.

## Histórico — checkpoint Test antes da revisão/integração

Implement aprovado;Test técnico consolidado sobre os quatro ficheiros finais,
manifesto/binários intactos:22687 JPEGs headless/sem avisos e16 fronteiras/negativos.
[Test](../../../../../bolts/011-video/test-walkthrough.md).
Manual Francisco13/7 etapas conferido,quatro conteúdos distintos;créditos B ilegíveis,
C/D legíveis nas amostras. Test/G-MEDIA técnicos aprovados pelo utilizador com
limitação B aceite;LOCAL-04/G-MEDIA-DISTRIBUTION abertos para TASK-030,
sem presumir legibilidade ou licença de A. LOCAL-03/cópia posterior explícita.
459820128 bytes de media considerados,sem escolha inferida de armazenamento.
Revisão final/integração pendentes,história/pai/unidade em progresso;limitação B explícita.
Não aprova atribuição adequada,legibilidade ou redistribuição.
Sem Java/perfis/POM/fixtures/demo alterados,staging/commits/push/fecho.

Git LFS/exceção .gitattributes aprovados e aplicados apenas aos quatro media,
sem alteração de conteúdo,Java/perfis/POM/fixtures/demo. Validação local materializa
4/4 objetos com hashes aprovados;sem index/commits/uploads/push. Pacote distingue
ponteiros Git de binários reais. Efeitos da tentativa Homebrew registados.
Revisão final/integração/fecho e distribuição continuam pendentes.

## Estado final — complete / implemented=true

Preparação dos quatro conteúdos (IMP-017/TASK-020 parte A) concluída e integrada
localmente em main8a91d9f. Test/G-MEDIA técnicos e manual Francisco13 aprovados;
revisão final Denji confirmada explicitamente pelo utilizador,sem pendências.
[Fecho](../../../../../bolts/011-video/closure-walkthrough.md).
TASK-020 inteira,unidade005 e PHASE-05 permanecem em progresso. Leitor/pacing/
emissão não são entregues por esta parte. LOCAL-03 não instalou Projeto/media;
LOCAL-04 e G-MEDIA-DISTRIBUTION abertos. B com créditos ilegíveis,sem aprovação
de atribuição/legibilidade/redistribuição/publicação. Sem push ou novo IMP.
