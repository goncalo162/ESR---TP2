# Plano de implementação — ESR TP2 2026/2027

Este conjunto de documentos especifica um protótipo OTT que distribui vídeo em tempo real através de uma rede overlay com multicast aplicacional. A entrega final deve demonstrar pelo menos dois servidores, cada um com dois vídeos distintos, clientes simultâneos, caminhos alternativos e adaptação às condições de rede.

**Estado atual: PHASE-01 concluída; PHASE-02 IN PROGRESS.** TASK-001–005 DONE; TASK-006–031 TODO. Configuração, codec e transporte TCP implementados, com [149 testes aprovados](../experiments/task-005-transport-validation.md). Descoberta bootstrap, gestão de vizinhos e vídeo continuam por implementar. Material para o relatório: [fase 1](PHASE-01-REPORT-NOTES.md) e [fase 2/TASK-005](PHASE-02-REPORT-NOTES.md).

Correção atual / ADR-013: bootstrap só devolve a localização dos vizinhos configurados na consulta inicial; sem registo vivo, leases, renovações ou monitorização. O contrato e o codec foram ajustados antes de iniciar rede; [revalidação com 131 testes](../experiments/bootstrap-discovery-correction.md). A lista inicial permite depois manter/reconectar vizinhos sem bootstrap.

Correção atual / ADR-013: bootstrap só devolve a localização dos vizinhos configurados na consulta inicial; sem registo vivo, leases, renovações ou monitorização. O contrato e o codec foram ajustados antes de iniciar rede; [revalidação com 131 testes](../experiments/bootstrap-discovery-correction.md). A lista inicial permite depois manter/reconectar vizinhos sem bootstrap.

## Fonte de verdade e precedência

1. [Enunciado original, cinco páginas](../ESR-TP2-26-27.pdf).
2. Requisitos explícitos transcritos analiticamente em [02](02-REQUIREMENTS.md).
3. Decisões e hipóteses identificadas em [12](12-RISKS-AND-DECISIONS.md).
4. Contratos, tarefas e restantes documentos deste plano.

A cópia [em Projeto/docs](../Projeto/docs/ESR-TP2-26-27-1.pdf) tem o mesmo SHA-256: 34a0f7e493987e85c019d060a0196a0e66b55a2ff5de4f6b68a0468a23879342. A menção inicial a um servidor/fluxo é um exemplo simplificado; o mínimo explícito de dois servidores com dois fluxos cada prevalece no plano. A documentação antiga de Projeto/docs é contexto histórico, não uma especificação adicional. Divergências estão registadas em [12](12-RISKS-AND-DECISIONS.md).

## Solução proposta

Java 21 e Maven, mantendo a estrutura existente. Bibliotecas padrão Java para sockets, serialização binária e reprodução Swing; JUnit Jupiter apenas para testes. Controlo TCP, vídeo MJPEG fragmentado sobre UDP, bootstrap por topologia configurada, anúncios com caminho explícito, métrica RTT, subscrição e poda de árvores por fluxo. CORE é a bancada principal. Não há base de dados, serviços web nem autenticação de utilizadores no âmbito definido.

## Índice e leitura

| Documento | Utilização |
|---|---|
| [01 — Visão geral](01-PROJECT-OVERVIEW.md) | Contexto, âmbito, auditoria do repositório |
| [02 — Requisitos](02-REQUIREMENTS.md) | Obrigações, recomendações e aceitação |
| [03 — Tecnologias](03-TECH-STACK.md) | Escolhas e fontes técnicas |
| [04 — Arquitetura](04-ARCHITECTURE.md) | Componentes, concorrência e fluxos |
| [05 — Estrutura](05-PROJECT-STRUCTURE.md) | Caminhos existentes e planeados |
| [06 — Dados](06-DATA-MODEL.md) | Entidades, estados e invariantes |
| [07 — Interfaces](07-API-AND-INTERFACES.md) | Configuração, CLI, protocolos e erros |
| [08 — Roadmap](08-IMPLEMENTATION-ROADMAP.md) | Fases, dependências e marcos |
| [09 — Testes](09-TESTING-STRATEGY.md) | Catálogo de validações futuras |
| [10 — Segurança](10-SECURITY.md) | Limites e validação de entradas |
| [11 — Ambiente](11-SETUP-AND-ENVIRONMENT.md) | Preparação e comandos propostos |
| [12 — Riscos e decisões](12-RISKS-AND-DECISIONS.md) | ADR, hipóteses, questões e bloqueios |
| [13 — Rastreabilidade](13-REQUIREMENTS-TRACEABILITY.md) | Requisito → tarefa → validação |
| [14 — Definition of Done](14-DEFINITION-OF-DONE.md) | Condições de conclusão |
| [15 — Progresso](15-PROJECT-PROGRESS.md) | Estado e próxima tarefa |
| [16 — Validação do plano](16-PLAN-VALIDATION.md) | Revisão documental e limites |
| [Guia para IA](AI-IMPLEMENTATION-GUIDE.md) | Execução e retoma entre sessões |
| [Coordenação da equipa](team/00-COORDINATION.md) | Matriz completa das 31 tarefas, dependências, revisões e integração |
| [Waheed](team/WAHEED.md) | Oito implementações principais e dez revisões, com ordem e critérios |
| [Fillmore](team/FILLMORE.md) | Nove implementações principais e dez revisões, com ordem e critérios |
| [Denji](team/DENJI.md) | Dez implementações principais e sete revisões, com ordem e critérios |

Na primeira sessão ler 01, 02, 12, 04, 06, 07, 08 e o guia. Depois consultar 15 e a tarefa disponível. As referências específicas de cada tarefa reduzem o contexto necessário nas sessões seguintes.

## Ordem de execução

PHASE-01 → PHASE-02 → PHASE-03 → PHASE-04 → PHASE-05 → PHASE-06 → PHASE-07. O [roadmap](08-IMPLEMENTATION-ROADMAP.md) liga os sete ficheiros de fase e identifica todas as tarefas. A ordem numérica TASK-001 a TASK-031 é uma ordenação válida das dependências.

**Primeira tarefa, concluída:** [TASK-001 — Validar a base Java e configurar testes](tasks/PHASE-01/TASK-001.md). **TASK-002 concluída:** contratos/fixtures e recursos revistos. **TASK-003 concluída:** configuração implementada/testada. **TASK-004 e PHASE-01 concluídas:** codec e integração local testados. **TASK-005 concluída:** transporte TCP testado. **Próxima tarefa:** [TASK-006](tasks/PHASE-02/TASK-006.md), ainda TODO. Executar as tarefas seguintes apenas no âmbito solicitado pelo utilizador.

Ao concluir uma tarefa, atualizar simultaneamente o seu estado, o progresso, a rastreabilidade dos requisitos satisfeitos e as evidências. Não converter TODO em DONE por existirem ficheiros vazios, documentação ou artefactos antigos em target/.
