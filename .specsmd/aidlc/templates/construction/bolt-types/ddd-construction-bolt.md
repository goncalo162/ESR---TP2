# DDD Construction Bolt — estado distribuído ESR OTT

Cinco etapas specsmd: Model → Design → ADR → Implement → Test. Modelar vizinhos,
rotas, árvores e pacotes no layout Java atual, sem DB, HTTP ou microserviços.

## 1. Model

Entidades, identidades, eventos, estados, invariantes e proprietário do estado,
reutilizando add.md secção 6. Output: `ddd-01-domain-model.md`. Checkpoint: modelo concreto.

## 2. Design

Interfaces, mensagens, concorrência, limites, timeouts, falhas e testes, separando
serviços disponíveis/futuros. Output: `ddd-02-technical-design.md`. Checkpoint: desenho.

## 3. ADR

Rever add.md secção 5 e registar reutilização em `adr-review.md`. Análise obrigatória;
nova ADR só se necessária, atualizando índice/contratos antes da implementação.
Checkpoint: decisões novas ou razão da reutilização.

## 4. Implement

Executar implementation_tasks, um IMP de cada vez com testes relevantes e revisões
atribuídas. Output: código nos pacotes atuais e `implementation-walkthrough.md`.
Checkpoint: alterações concretas e verificações autorizadas, sem revisão inventada.

## 5. Test

Consolidar V-XXX, regressão, integração, casos atrasados/repetidos/inválidos,
falhas e limites. Output: `ddd-03-test-report.md`, com comandos, ambiente,
resultados e pendências. Checkpoint: resultados e fecho quando aplicável.

## Estado e checkpoints

current_stage: model, design, adr, implement ou test. Atualizar timestamps UTC,
stages_completed e artefactos apenas após execução. Não adiar todos os testes
para o fim. Os checkpoints exigem revisão, salvo autorização explícita do
utilizador para as etapas abrangidas; tal autorização não substitui revisores
TASK nem confirmação humana de submissão.


Fonte de arquitetura: add.md. source_tasks são pais de rastreabilidade; implementation_tasks define o único incremento executável deste bolt.
