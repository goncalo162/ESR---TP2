# Simple Construction Bolt — ESR OTT

Etapas locais Plan → Implement → Test, preservando o workflow specsmd e aplicando
o âmbito do projeto. Não importar DB, HTTP ou metas de cobertura do template.

## 1. Plan

Ler bolt/unit/histórias/TASK, verificar dependências e fixar interfaces, ordem,
outputs, riscos e oráculos. Output: `implementation-plan.md` na pasta do bolt.
Checkpoint humano: apresentar o plano concreto, salvo autorização explícita já
dada pelo utilizador para as etapas abrangidas.

## 2. Implement

Executar um IMP de cada vez, com os testes relevantes. Em bolts de evidência
ou documentação, produzir esses artefactos sem código artificial. Output:
alterações e `implementation-walkthrough.md`. Checkpoint: rever os entregáveis
concretos; não inventar aprovação humana.

## 3. Test

Executar V-XXX/regressão apropriada e provas GUI/CORE exigidas; registar comandos,
ambiente, resultados e limites em `test-walkthrough.md`. Checkpoint: apresentar
critérios satisfeitos/pendentes para fecho.

## Estado

current_stage: plan, implement ou test. started/completed/stages_completed usam
timestamps ISO 8601 UTC e artefactos reais. planned → in-progress → complete,
ou blocked perante impedimento real. Não fabricar relatórios na preparação.
Autorização explícita de execução prevalece sobre checkpoints genéricos;
revisão atribuída da equipa e confirmação de upload mantêm-se gates próprios.


Fonte de arquitetura: add.md. source_tasks são pais de rastreabilidade; implementation_tasks define o único incremento executável deste bolt.
