# TASK-016 — Recuperar árvores após falha de upstream

## 1. Identificação

- ID: TASK-016
- Fase: [PHASE-04 — Árvores de distribuição e adaptação](../../phases/PHASE-04-FLOW-MANAGEMENT.md)
- Prioridade: P0
- Estado: DONE
- Responsável: [Waheed](../../team/WAHEED.md).
- Revisor: [Fillmore](../../team/FILLMORE.md), antes de integrar e marcar DONE.
- Dependências: [TASK-012](../PHASE-03/TASK-012.md), [TASK-014](../PHASE-04/TASK-014.md), [TASK-015](../PHASE-04/TASK-015.md).
- Requisitos relacionados: [RF-002](../../02-REQUIREMENTS.md#rf-002), [RF-006](../../02-REQUIREMENTS.md#rf-006), [RF-009](../../02-REQUIREMENTS.md#rf-009), [RNF-003](../../02-REQUIREMENTS.md#rnf-003).

## 2. Objetivo

Reativar procura num caminho alternativo válido após falha.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Dois consumidores partilham ativação, remoção de um preserva o outro, falha troca upstream e segundo marco fica reproduzível. A responsabilidade específica é recuperar árvores após falha de upstream. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Invalidar candidatos do vizinho morto e selecionar alternativa cujo path não contém o nó local. Renegociar ACTIVATE pelo caminho consistente, trocando upstream após ACK. Se o caminho estiver desatualizado, pedir/aguardar anúncio novo e repetir em prazo limitado. Preservar intenção de consumo, não subscrições de conexões mortas. Sem caminho válido usar UNAVAILABLE; quando regressar conectividade, voltar a ativar sem exigir reinício do cliente.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: routing/RoutingService.java, RoutingTable.java; planeado: routing/FailoverTest.java.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [x] Ligar neighborDown à invalidação de candidatos e snapshot.
- [x] Selecionar alternativa e iniciar ativação correlacionada.
- [x] Confirmar troca ou reportar indisponibilidade.
- [x] Testar retorno de relay e rejeição de dados/ACK antigos.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Partição, morte da fonte única, alternativa que contém o próprio nó, falha durante ativação, bootstrap indisponível e regresso de nó com instanceId novo.

## 10. Testes necessários

V-016: cortar upstream num losango leva a caminho alternativo sem ciclo; sem alternativa termina UNAVAILABLE; regresso permite recuperar; streams da outra origem continuam.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [x] Há recuperação quando existe caminho até à mesma fonte.
- [x] Ausência de caminho é visível e não produz laço.
- [x] Estado antigo não é reativado por mensagens atrasadas.
- [x] Validação V-016 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

## Integração futura de EXTRA-01

O utilizador aprovou construir primeiro a aresta S–C no
[IMP-034/035](../../../add.md#imp-035), antes de 005-rotas. Quando os componentes
desta tarefa estiverem disponíveis, ligar a evidência de recuperação do
caminho e do vídeo ao cenário S–R1–C / S–R2–C, com morte sequencial dos
relays e formação de S–C. A aprendizagem e a reparação da overlay demonstram a conectividade.
O teste integrado deve identificar stream, origem, nova aresta, percurso
selecionado, confirmação de ativação e resultado observado.

## Construction — apenas Model em 2026-11-20T11:02:52Z

Seleção explícita do utilizador para008-arvores/IMP-013; dependências concluídas.
[Modelo proposto](../../../memory-bank/bolts/008-arvores/ddd-01-domain-model.md)
e Q-C-43/44 aguardam aprovação. Âmbito futuro normativo: Route.java,
RoutingService.java e RecoveryTest.java; RoutingTable/FailoverTest citados acima
são detalhe legado, não autorização de ampliação. V-016/R01–R18 não executados;
recuperação de controlo não comprova vídeo. Sem fontes lidas/analisadas/alteradas,
Maven, Design/ADR/Implement/Test, commits ou push. Fecho futuro mantém V-016,
regressão aprovada, manual humano, revisão final Fillmore e integração.

## Model aprovado; Design para revisão em 2026-11-20T11:21:05Z

Model/Q-C-43/44 aprovados explicitamente; apenas Design autorizado.
[Design proposto](../../../memory-bank/bolts/008-arvores/ddd-02-technical-design.md)
concretiza novidade útil F/G, rondas T/A com cadência limitada e renegociação
filhos automática por11, sem intervenção humana/extensão de grants antigos.
Três caminhos do ADD preservados, compatibilidade real por conferir em ADR.
V-016/R01–R18 por executar; sem fontes lidas/alteradas, Implement, Maven, commits
ou push. Fecho mantém testes, regressão JDK21, manual humano, Fillmore e integração;
sem gates externos adicionais.

## IMP-013 — Design revisto aprovado; revisão ADR preparada

Registo 2026-11-20T11:45:38Z. Design revisto e clarificação de Q-C-44 aprovados
pelo utilizador em2026-11-20T11:41:00Z; apenas revisão ADR autorizada.
APIs reais conferidas estaticamente: announceSeq/Outcome disponíveis, watermark
interno privado sem necessidade de API nova; intenção distinta de resultado do
ticket, mudança de época, ACTIVATE/revisões, construtores4/5/projeções e lifecycle.
Revisão para aprovação, sem impedimento de âmbito identificado ou nova ADR.
A01–A08 são trabalho futuro nos mesmos três caminhos; nenhuma correção aplicada.
V-016/R01–R18 não executados; fontes só consultadas, sem Java alterado, Maven,
Implement/Test, fecho, staging, commits, push ou outro IMP. Negativos/provas
anteriores preservados. Fecho exige testes, regressão JDK21, manual humano,
revisão final Fillmore e integração, sem gates externos adicionais.
[Revisão completa](../../../memory-bank/bolts/008-arvores/adr-review.md).

## IMP-013 — Implement reapresentado, sem aceitação/Test

Registo 2026-11-20T12:27:18Z. Utilizador aprovou ADR e autorizou apenas Implement A01–A08
nos três caminhos do ADD, com CODESTYLE, históricos/correlações/construtores e
reservas físicas preservados. Implement preparado para revisão: regressão JDK21
incremental final1414/0/0/0,20 XML,35 testes novos e1379 históricos byte intactos.
Duas integrações TCP reais com métricas controladas passaram; negativos guardados
separadamente, incluindo R13, prazo de fila, R18/fixture, R17 inicial sem causa
estabelecida e alternativa consumida R10. Remoções/substituições justificadas no
walkthrough; sem teste/asserção/prazo histórico retirado ou fundação alterada.
Só Model/Design/ADR em stages_completed; current_stage=implement, completed=null.
Implement ainda por aceitar; sem etapa Test, fecho, commits/push ou outro IMP.
V-016 consolidado, regressão da etapa Test, manual humano antes de aprovar Test,
revisão final Fillmore e integração continuam obrigatórios; sem gates novos.
[Walkthrough e prova](../../../memory-bank/bolts/008-arvores/implementation-walkthrough.md).

## IMP-013 — R10 corrigido; Implement permanece pendente

Registo 2026-11-20T15:44:31Z. Implement continua pendente. Reprodução exata com G2,
Long.MAX_VALUE-1000, métrica10→2000→10 após PATH_CHANGED/T3s: negativo preservado
esperado1/observado2 ACTIVATEs. A transição previous==null agora confere blocked
antes de conceder crédito; métrica isolada não reutiliza a prova bloqueada.
Primeira medição válida, anúncio novo posterior e T/A/cadência preservados.
11 testes R10 focados passaram; regressão JDK21 atual1419/0/0/0,20 XML,
40 RecoveryTest (35 anteriores intactos+5 novos),1379 históricos intactos.
Delta Java só em RoutingService e RecoveryTest; Route/fundações inalterados.
R17 inicial mantém causa não estabelecida. Sem aceitação de Implement, Test,
fecho, commits ou push. V-016 consolidado, manual humano, Fillmore e integração
pendentes; sem gates externos adicionais.
[Walkthrough atualizado](../../../memory-bank/bolts/008-arvores/implementation-walkthrough.md).

## IMP-013 — Test técnico, aguardar manual humano

Registo 2026-11-20T16:07:46Z. Implement/R10 aprovados; apenas Test autorizado.
Clean verify JDK21 novo1419/0/0/0,20 XML e JAR504997bytes/SHA256
81779be388778fd7dfacba5957e74ccffe5da45e86a403ea1ec5626f52c8f49d.
V-016/R01–R18 consolidado tecnicamente; fontes/históricos/evidências intactos.
Manual humano preparado:7 etapas/28 checks. Ensaio kind=agent passou no JAR
congelado, hashes/origem/JVM conferidos; não satisfaz obrigação humana.
Aguardar repetição pessoal antes de pedir aprovação Test. Test não aprovado;
R17 inicial sem causa estabelecida, integração com métricas controladas não
prova a causa. Nova correção exige reprodução/âmbito apresentados.
Sem fecho/staging/commits/push/outro IMP; Fillmore/integração pendentes,
sem gates externos adicionais.
[Relatório Test](../../../memory-bank/bolts/008-arvores/ddd-03-test-report.md).

## Fecho após integração comprovada

Registo 2026-11-20T16:23:16Z: Test/manual Luis7/28 e revisão Fillmore aprovados;
integração main 447ae4bf68bf96329bd1a0c7e16ed20df06bc55b comprovada.
Bolt/história complete,implemented=true,TASK-016 DONE; unidade/fase em progresso.
Negativos e limitação R17 preservados,sem causa estabelecida. Nenhum outro IMP
iniciado;024 apenas desimpedido. Commit/push autorizados,sem Java/build/manual novo.
[Fecho](../../../memory-bank/bolts/008-arvores/closure-walkthrough.md).
