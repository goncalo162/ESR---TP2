---
id: "025-video"
unit: "005-video"
intent: "001-ott-streaming"
type: "ddd-construction-bolt"
status: "complete"
stories: ["007-ler-o-perfil-mjpeg-confirmado"]
created: "2026-11-02T15:36:48Z"
updated: "2026-11-25T17:11:45Z"
started: "2026-11-25T15:28:08Z"
completed: "2026-11-25T17:11:00Z"
current_stage: "test"
stages_completed: [{"name": "model", "completed": "2026-11-25T15:40:54Z", "artifact": "ddd-01-domain-model.md"}, {"name": "design", "completed": "2026-11-25T15:52:32Z", "artifact": "ddd-02-technical-design.md"}, {"name": "adr", "completed": "2026-11-25T16:10:17Z", "artifact": "adr-review.md"}, {"name": "implement", "completed": "2026-11-25T16:37:27Z", "artifact": "implementation-walkthrough.md"}, {"name": "test", "completed": "2026-11-25T17:05:33Z", "artifact": "ddd-03-test-report.md"}]
requires_bolts: ["009-arvores", "011-video"]
enables_bolts: ["026-video"]
requires_units: ["004-arvores"]
blocks: false
complexity:
  avg_complexity: 3
  avg_uncertainty: 1
  max_dependencies: 2
  testing_scope: 2
execution_agent: "construction"
source_phase: "PHASE-05"
source_tasks: ["TASK-020"]
source_task_order: ["TASK-020"]
implementation_tasks: ["IMP-018"]
size: "M"
validation_ids: ["V-020"]
test_ids: ["V-020-B"]
external_gates: []
adr_ids: ["ADR-006", "ADR-009", "ADR-021", "ADR-024"]
requirements: ["INT-001", "INT-002", "NFR-001", "REQ-012", "REQ-013", "REQ-015", "REQ-018"]
architecture_source: "add.md"
---
# Bolt 025-video — Ler o perfil MJPEG confirmado

## Fonte normativa e âmbito

Ler [IMP-018](../../../add.md#imp-018) e as ADR aí indicadas antes de qualquer etapa. Este bolt executa **uma** tarefa atómica; o ADD define o que construir, como e porquê. Fonte legada: [TASK-020](../../../docs/tasks/PHASE-05/TASK-020.md).

## História, dependências e gates

- [x] [007-ler-o-perfil-mjpeg-confirmado](../../intents/001-ott-streaming/units/005-video/stories/007-ler-o-perfil-mjpeg-confirmado.md).
- Dependências: [009-arvores](../009-arvores/bolt.md), [011-video](../011-video/bolt.md).
- Unidade: [005-video](../../intents/001-ott-streaming/units/005-video/unit-brief.md).
- Gates: nenhum adicional.

## Etapas e validação

1. [x] **model** → `ddd-01-domain-model.md`; validar a etapa contra IMP-018 e as ADR do ADD.
2. [x] **design** → `ddd-02-technical-design.md`; validar a etapa contra IMP-018 e as ADR do ADD.
3. [x] **adr** → `adr-review.md`; validar a etapa contra IMP-018 e as ADR do ADD.
4. [x] **implement** → `implementation-walkthrough.md`; validar a etapa contra IMP-018 e as ADR do ADD.
5. [x] **test** → `ddd-03-test-report.md`; validar a etapa contra IMP-018 e as ADR do ADD.

Tipo: [ddd-construction-bolt](../../../.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md). Não gerar relatórios de execução só por criar este ficheiro. Testes são incrementais e a etapa Test consolida a prova.

## Critérios de aceitação

- [x] Ler L bytes após cinco dígitos ASCII; EOF só é limpo entre registos.
- [x] Prefixo inválido/parcial, zero, excesso de tamanho, truncatura e JPEG inválido têm erro localizado antes de publicação.
- [x] Fixtures independentes produzem os bytes esperados e o leitor fecha recursos.

## Evidência e fecho

- Testes: V-020-B, detalhados no ADD; guardar comandos, ambiente, resultados e limites.
- Outputs e ficheiros: consultar [IMP-018](../../../add.md#imp-018).
- Prova futura: `experiments/construction/025-video/` e relatório local do bolt.
- Rever/atualizar história, TASK pai, docs/13–15 e construction-log; uma parte não conclui o pai.
- A revisão humana atribuída não é presumida. Antes de declarar decisão inviável ou alterar âmbito, aplicar ADD secção 11.

## Model preparado para revisão — 2026-11-25T15:32:21Z

Próximo bolt iniciado por instrução explícita do utilizador,apenas Model.
[Modelo estático](ddd-01-domain-model.md): cursor por fonte,registos5ASCII+LJPEG,
limite antes de alocar,entrega atómica,EOF/erros/fecho e isolamento.
IMP-018-LOCAL-01/02 são propostas de ownership/EOF e validação JPEG,sem novos
IDs Q-C globais ou alteração normativa. M01–M14/V-020-B planeados,não executados.
current_stage=model,stages_completed=[],completed=null;história planned.
Sem leitura/análise/alteração de fontes,Design/ADR/Implement/Test,Maven,commits
ou push. Dois caminhos futuros do ADD preservados;TASK-020 pai IN PROGRESS.
LOCAL-03/04/G-MEDIA-DISTRIBUTION e R17 mantêm as limitações anteriores.
Remoções locais da demo e seus documentos não são repostas.

## Model aprovado e Design preparado — 2026-11-25T15:46:47Z

Model/IMP-018-LOCAL-01/02 aprovados explicitamente;apenas Design autorizado.
[Design para revisão](ddd-02-technical-design.md): assinaturas propostas,
entrada própria,fecho idempotente,erro primário e zero progresso;caps4096
por dimensão/4194304pixels/16MiB de raster antes de decode,compatíveis com
os quatro conteúdos pelos registos de auditoria aprovados. Sem nova auditoria
de media/Java/Maven ou execução M01–M14/V-020-B. Nenhum fonte lido/analisado.
Dois caminhos do ADD,sem configuração/protocolo,pacing,loop,emissão ou Implement.
[Seis problemas V-ADD](../../../experiments/construction/025-video/design-01/v-add-pendencias.md)
pendentes antes do fecho,não aprovados;remoções da demo preservadas.
current_stage=design,apenas Model emstages_completed,história planned.
Sem commits/push/outro IMP. Fecho requer testes/regressãoJDK21,manual humano
aplicável,revisão Denji e integração;TASK-020 só fecha apósA/B/C.

## Design aprovado; revisão ADR preparada — 2026-11-25T16:00:22Z

Design/guardas aprovados. [Revisão ADR](adr-review.md) sobre fontes/APIs reais
para aprovação,sem impedimento de âmbito identificado. A01–A08 concretizam
paths,ownership,decoder/scanner,destino ecleanup. A05/A06 apresentam precisões
na classificação de retorno ImageReader inválido ecleanup de imagem distinta,
a aprovar antes de código. ADR-006/009/021/024 reutilizadas,sem nova ADR.
VideoSource.java real vazio/sem consumidores,VideoSourceTest inexistente;
nenhum fonte alterado,compilação/teste/leitor executado. M01–M14/V-020-B
continuam por executar. current_stage=adr,apenas Model/Design concluídos.
Seis problemas V-ADD pendentes antes do fecho,não aprovados;demo não reposta.
Sem Implement,config/protocolo,commits/push ou outro IMP. Fecho:JDK21,manual
aplicável,Denji e integração;TASK-020 só fecha apósA/B/C,sem gate externo novo.

## ADR aprovada; Implement para revisão — 2026-11-25T16:28:55Z

Revisão incluindo A05/A06 aprovada pelo utilizador. Apenas os dois caminhos
Java autorizados foram implementados; [walkthrough](implementation-walkthrough.md).
M01–M14 exercitados em45 testes novos, JDK21/headless; regressão incremental
1582/0/0/0,23 XML. Negativo de compilação inicial e snapshots preservados.
Guardas e pós-condições de decoder/cleanup mantidas;16MiB apenas para raster.

Quatro recursos locais são ponteiros LFS, sem materialização. Leitura integral
dos22687 JPEGs com o novo leitor não demonstrada; fixtures independentes nas
dimensões aprovadas passam. Seis pendências V-ADD mantidas antes do fecho,
validação não aprovada; remoções da demo e históricos preservados.
current_stage=implement; apenas Model/Design/ADR em stages_completed,
completed=null; história007 in-progress. Implement aguarda aceitação;
V-020-B consolidado e Test pendentes. Sem Test, fecho, commits/push ou IMP-019.
Manual humano aplicável, Denji e integração mantidos; TASK-020 só apósA/B/C.

## Implement aprovado; Test em curso — 2026-11-25T16:50:39Z

Aprovação explícita de Implement registada na entrada de Test; código aprovado
sem alterações. [Relatório Test](ddd-03-test-report.md): clean verify JDK21 com
1582/0/0/0,23 XML, saída0 e JAR novo/SHA-256 guardados. M01–M14 locais passam;
V-020-B e M13 integral pendentes: quatro ponteiros LFS, binários/cache ausentes.
Não se pede aprovação Test sem prova completa/hash/count/EOF/cleanup e manual
humano aplicável; guião preparado, ensaio técnico não substitui execução pessoal.

Patch V-ADD de3 paths apresentado e autorizado exclusivamente pelo utilizador,
aplicado exatamente; validação atual0 erros, sem enfraquecer validador ou repor
a demo. Negativos de Implement e ensaio do lançador manual preservados.
current_stage=test, stages_completed apenas Model/Design/ADR/Implement,
completed=null, história007 in-progress. Sem fecho/commits/push ou IMP-019.
Denji/integração obrigatórios; TASK-020 só apósA/B/C; R17 inicial sem causa.

## Test aceite condicionalmente; revisão Denji confirmada — 2026-11-25T17:05:33Z

Prova integral nova:22687 frames dos quatro binários com hashes aprovados,
contagens500/5965/8880/7342, cada JPEG comparado, EOF/END e fecho único.
Negativo humano preservado; posterior instrução do utilizador delega execução
em Codex e autoriza commit/fecho se correto. Nova run automática saída0,
sem declarar manual humano concluído. Denji reviu e aprovou esta versão,
confirmado pelo utilizador. Test emstages_completed, bolt ainda in-progress
até integração local real. Sem rebuild, alteração Java, push ou IMP-019.

## Fecho efetivo — 2026-11-25T17:11:00Z

[Fecho](closure-walkthrough.md): cinco etapas concluídas, Test aceite por
condição expressa após execução delegada, revisão Denji confirmada e código
integrado localmente em maine896643. Bolt complete, completed definido,
história007 complete/implemented=true. TASK-020 apenasA/B; C não iniciada.
Sem rebuild, push ou outro IMP. Negativos e R17 preservados.
