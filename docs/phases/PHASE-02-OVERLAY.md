# PHASE-02 — Overlay, bootstrap e ciclo de vida

## 1. Objetivo

Conectar processos reais por TCP full-duplex, descobrir vizinhos e preparar a primeira demonstração intermédia.

## 2. Contexto

Incremento 2 de sete, segundo [o roadmap](../08-IMPLEMENTATION-ROADMAP.md). O resultado é verificável antes da fase seguinte. Estado atual: IN PROGRESS para EXTRA-01. A base foi concluída e validada pela equipa. TASK-005 DONE com transporte TCP validado localmente; TASK-032 DONE/integrada; TASK-006 DONE, Test aprovado com clean verify/856 testes e manuais separados Codex/Luis/34 checks/saída 0 cada, revisão final atribuída a Waheed confirmada pelo utilizador e versão revista integrada em main; TASK-007 DONE após Test/manual humano, revisão Denji aprovada e integração local 8db7b78; TASK-008 DONE, com [fecho e integração](../../memory-bank/bolts/020-overlay/closure-walkthrough.md); TASK-009 DONE, com [marco técnico e integração](../../memory-bank/bolts/004-overlay/closure-walkthrough.md). O utilizador confirmou a validação da equipa e autorizou o fecho, registado em 2026-11-10T23:39:09Z sobre a base integrada `223ea6f`. [Dossier de fecho](PHASE-02-CLOSURE-REVIEW.md).

## 3. Pré-requisitos

PHASE-01 concluída, com evidência do seu entregável; TASK-004 está DONE. Ler os contratos de [07](../07-API-AND-INTERFACES.md), a [arquitetura](../04-ARCHITECTURE.md) e os gates em [12](../12-RISKS-AND-DECISIONS.md). Dependências específicas estão na tabela e em cada tarefa.

## 4. Requisitos abrangidos

RF-001, RF-002, RF-003, RF-004, RNF-003, RNF-004, RNF-005, RC-007, RP-006, RP-008. As descrições e critérios estão em [02](../02-REQUIREMENTS.md); esta fase pode implementar apenas parte de um requisito transversal, como mostra [13](../13-REQUIREMENTS-TRACEABILITY.md).

## 5. Tarefas

| Tarefa | Objetivo concreto | Dependências | Validação | Estado |
|---|---|---|---|---|
| [TASK-005](../tasks/PHASE-02/TASK-005.md) | Estabelecer transporte TCP full-duplex com recursos limitados | TASK-004 | V-005 | DONE |
| [TASK-032](../tasks/PHASE-02/TASK-032.md) | Parametrização operacional validada | TASK-003, TASK-005 | V-032 | DONE |
| [TASK-006](../tasks/PHASE-02/TASK-006.md) | Fornecer localização inicial dos vizinhos configurados | TASK-003, TASK-005, TASK-032 | V-006 | DONE |
| [TASK-007](../tasks/PHASE-02/TASK-007.md) | Gerir handshake, unicidade e reconexão de vizinhos | TASK-005, TASK-006 | V-007 | DONE |
| [TASK-008](../tasks/PHASE-02/TASK-008.md) | Compor o processo overlay e encerrar de forma previsível | TASK-003, TASK-006, TASK-007 | V-008 | DONE |
| [TASK-009](../tasks/PHASE-02/TASK-009.md) | Preparar evidência do primeiro marco intermédio | TASK-008 | V-009 | DONE |
| [TASK-033 — Extra A](../tasks/PHASE-02/TASK-033.md) | Aprender candidatos por divulgação de vizinhos | TASK-009 | V-033 | IN PROGRESS |
| [TASK-034 — Extra B](../tasks/PHASE-02/TASK-034.md) | Reparar ligações com candidatos aprendidos | TASK-033 | V-034 | TODO |

## 6. Ordem de execução

TASK-005 (DONE) → TASK-032 → TASK-006 → TASK-007 → TASK-008 → TASK-009 → TASK-033 → TASK-034 (EXTRA-01). Esta ordem é segura; não iniciar a fase seguinte antes de cumprir o gate desta fase. A estrutura permite trabalho independente dentro da fase se as dependências explícitas estiverem concluídas e o progresso ficar coerente.

## 7. Dependências

O gate anterior e as dependências de cada tarefa são cumulativos. A tarefa inicial depende do fecho da fase anterior, direta ou transitivamente. Não há dependências em tarefas ainda não identificadas. Resolver recursos externos apenas no gate documentado, sem inventar resultados.

## 8. Componentes afetados

transport; bootstrap de consulta inicial; overlay; app; logs e roteiro da primeira avaliação intermédia. O bootstrap consulta apenas a configuração: sem registo vivo, leases, renovações, monitorização ou avisos de saída. Os caminhos precisos e a distinção existente/planeado estão nas tarefas e em [05](../05-PROJECT-STRUCTURE.md).

## 9. Entregáveis

Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. Preservar configurações, comandos e evidência de V-005 a V-009 nas tarefas e no progresso. Artefactos de testes não são prova de submissão ou de fases seguintes.

## 10. Estratégia de testes

Executar as validações da tabela nas tarefas correspondentes e a demonstração integrada da fase. Usar [09](../09-TESTING-STRATEGY.md) para oráculos, ambiente, evidência e casos de erro. Não adiar os unitários desta fase para PHASE-06.

## 11. Critérios de aceitação

- [ ] Cada tarefa da tabela está DONE com critérios individuais satisfeitos.
- [x] Pelo menos três nós iniciam fora de ordem, trocam HELLO e reconectam sem canais duplicados.
- [x] Bootstrap só fornece localização dos vizinhos a partir da configuração; não mantém disponibilidade ou registos com renovação.
- [x] Nós já descobertos reconectam diretamente com bootstrap parado, sem consultas periódicas ou avisos de saída para ele.
- [x] O resultado «Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego.» é demonstrável no ambiente da fase.
- [x] Requisitos cobertos, limitações e recursos pendentes estão coerentes em 13 e 15.
- [x] Não há regressão nos componentes anteriores afetados pelas alterações.

- [ ] EXTRA-01 aprende candidatos por divulgação e cria S–C ou R1–C após as falhas, com V-033/V-034 e integração confirmados.

## 12. Definition of Done

Coordenação do fecho: [Denji](../team/DENJI.md), com revisão dos três membros segundo a [coordenação da equipa](../team/00-COORDINATION.md). Autor e revisor de cada TASK constam na identificação; a atribuição não altera dependências ou estados.

Cumprir [14](../14-DEFINITION-OF-DONE.md), incluindo evidência real e passagem de contexto. Atualizar [15](../15-PROJECT-PROGRESS.md); marcar a fase DONE apenas depois de todas as tarefas e critérios.

## 13. Riscos e observações

Início simultâneo, conexões duplicadas e endereços publicitados incorretos são os principais riscos. Consultar [12](../12-RISKS-AND-DECISIONS.md) para mitigação e responsável. Metas numéricas do plano são propostas; obrigações do PDF continuam vinculativas.
