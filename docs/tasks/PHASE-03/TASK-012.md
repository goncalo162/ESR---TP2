# TASK-012 — Difundir anúncios e construir catálogo e rotas

## 1. Identificação

- ID: TASK-012
- Fase: [PHASE-03 — Monitorização e descoberta de rotas](../../phases/PHASE-03-ROUTING.md)
- Prioridade: P0
- Estado: DONE
- Responsável: [Fillmore](../../team/FILLMORE.md).
- Revisor: [Denji](../../team/DENJI.md), antes de integrar e marcar DONE.
- Dependências: [TASK-007](../PHASE-02/TASK-007.md), [TASK-010](../PHASE-03/TASK-010.md), [TASK-011](../PHASE-03/TASK-011.md).
- Requisitos relacionados: [RF-005](../../02-REQUIREMENTS.md#rf-005), [RF-006](../../02-REQUIREMENTS.md#rf-006), [RF-009](../../02-REQUIREMENTS.md#rf-009), [RF-012](../../02-REQUIREMENTS.md#rf-012), [RF-014](../../02-REQUIREMENTS.md#rf-014), [RP-001](../../02-REQUIREMENTS.md#rp-001).

## 2. Objetivo

Descobrir todas as streams e caminhos sem flooding infinito.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Rotas para várias streams, alternativas sem ciclos e trace até à origem. A responsabilidade específica é difundir anúncios e construir catálogo e rotas. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Cada SERVER publica as streams configuradas, mesmo sem consumidores. Implementar ANNOUNCE/WITHDRAW com path vector, seq e epoch, cache limitada e reenvio de melhoria estrita. Verificar origem do StreamInfo, unicidade do path e sender. Candidatos expiram por relógio local; servidor reiniciado gera epoch novo. Metadados de catálogo são derivados de anúncios válidos e removidos quando não resta candidato.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Âmbito explícito de IMP-009, ampliado por Q-C-28 Opção1 aprovada: routing/RoutingService.java, streaming/StreamInfo.java, streaming/StreamCatalog.java, routing/RoutingServiceTest.java, overlay/OverlayNode.java e overlay/OverlayNodeRoutingTest.java (novo). Composição aditiva no owner/lifecycle existentes; conservar APIs/observadores. Grafos literais nos testes autorizados, sem ficheiros Java adicionais.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [x] Gerar anúncios periódicos para dois fluxos por origem.
- [x] Validar, somar custo local e guardar candidato por vizinho.
- [x] Reenviar primeira chegada e melhorias com rate limit.
- [x] Tratar expiração, retirada, epoch novo e catálogo consultável.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Um caminho melhor chega depois do primeiro anúncio; ciclo na malha; duplicados de outra conexão; época velha reaparece; conflito de origem para UUID. Não inventar catálogo central acessível por HTTP.

## 10. Testes necessários

V-012: grafo em losango e grafo cíclico convergem para custo esperado; segunda chegada melhor é aceite; quatro streams aparecem; retirar uma origem não remove streams da outra; flooding estabiliza.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [x] Catálogo inclui origem e validade dos quatro fluxos.
- [x] Melhores caminhos e alternativas ficam disponíveis.
- [x] Cache, seq, epoch e path impedem tempestade e regressão a época retirada.
- [x] Validação V-012 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.


## IMP-009 / 006-rotas — Model iniciado, para revisão

Registo 2026-11-14T11:54:45Z. Opção1 do utilizador autoriza apenas Model. Base limpa
164ac0dc477c02bc7fde816c761d137ee18ea55d, Denji/main alinhados; dependências
004-overlay/021-rotas e unidade002-overlay complete, IMP-007 transitivo complete.
JAR e seis hashes revistos do IMP-008 conferidos. Modelo de anúncios/catálogo,
validação/path, gerações, cache/quotas, épocas, callbacks e M01–M22/V-012 preparados.
Q-C-28 propõe composição em OverlayNode/OverlayNodeRoutingTest além dos quatro
caminhos; Q-C-29 propõe concretização de geração/retirada/publicidade/saturação.
Ambas pendentes, sem ADD/ADR/âmbito alterado por inferência. current_stage=model,
stages_completed=[],completed=null; critérios funcionais por cumprir. V-012,
regressão/manual atuais, revisão final Denji e integração pendentes. Sem gates
externos adicionais. Sem análise/alteração Java, Maven, sockets, commits/push
ou outro IMP. Unidade003-rotas/PHASE-03 permanecem em progresso.
Artefacto: memory-bank/bolts/006-rotas/ddd-01-domain-model.md; evidência em
experiments/construction/006-rotas/. Parar antes de Design.

## IMP-009 / 006-rotas — Model aprovado; Design para revisão

Registo 2026-11-14T12:31:20Z. Utilizador aprovou Model, Q-C-28 Opção1 e princípios Q-C-29.
ADD/IMP-009 agora explicita seis caminhos: os quatro originais mais OverlayNode
java e OverlayNodeRoutingTest java, compondo routing no owner/lifecycle existentes.
Design propõe limites de cache/pendências/quotas/tickets, ordens de retirada e
publicação por geração, preservação de TTL e tratamento de callbacks antigos.
D01–D32/V-012 planeados, nenhum executado. Só Model concluído; current_stage=design,
stages_completed=[model], completed=null. ADR/Implement/Test não iniciados.
V-012, regressão atual, manual humano antes de aprovar Test, revisão final atribuída
a Denji e integração pendentes; sem gates externos adicionais. Nenhum Java,
Maven, sockets, commit/push ou início de outro IMP. Parar antes de ADR.
Artefacto: memory-bank/bolts/006-rotas/ddd-02-technical-design.md; evidência em
experiments/construction/006-rotas/. Registos anteriores de Model pendente são histórico.

## IMP-009 / 006-rotas — Design aprovado; revisão ADR para aprovação

Registo 2026-11-14T12:53:08Z. Utilizador aprovou Design completo, limites/ordens Q-C-29 e
metadados Q-C-30. ADD e decisões registados; seis caminhos mantidos. Assinaturas
reais conferidas: quatro ficheiros vazios, OverlayNode com owner/monitor/lifecycle
existentes, OverlayNodeRoutingTest novo; dependências apenas lidas.
Revisão ADR preparada, incluindo Q-C-31 proposta para explicitar recusa fatal
real do owner.offer e OVERFLOW terminal só na ligação. Não inventar retoma nem
alterar fundações. Sem nova ADR/substituição aceite, sem Java/Maven/socket.
current_stage=adr; stages_completed=[model,design]; completed=null;32 oráculos
continuam por executar. Implement/Test/V-012/regressão atual/manual humano antes
de Test aprovado/revisão final Denji/integração pendentes. Sem gates externos
adicionais, commits/push ou outro IMP. Parar na revisão ADR.
Artefacto: memory-bank/bolts/006-rotas/adr-review.md; evidência em
experiments/construction/006-rotas/adr-review/. Estados anteriores de Design
pendente são histórico e não anulam esta aprovação explícita.

## IMP-009 / 006-rotas — Implement para revisão antes de Test

Registo 2026-11-14T14:36:19Z. Revisão ADR/Q-C-31 aprovadas explicitamente; Design6/7 e
D14/D15/D29 clarificados conforme contratos reais. Código nos seis caminhos:
RoutingService/StreamInfo/StreamCatalog, composição aditiva OverlayNode e dois
testes autorizados. APIs/testes antigos preservados, sem alterações a codec/
transporte/manager/monitor/tabela/config/POM/fixtures. Sem ampliar âmbito.
Regressão incremental JDK21:1254/0/0/0 em16 XML,43 testes novos. Falhas reais
anteriores e restrições/erro da bancada preservados no walkthrough. D32 parcial;
V-012 com prova incremental, Test/clean verify/JAR novo/manual humano antes da
aprovação Test/revisão final Denji/integração pendentes. D29/D31 incluem detalhes
estáticos ainda sujeitos à revisão; não alegar prova exaustiva em todos os caps.
current_stage=implement; stages_completed=[model,design,adr]; completed=null.
Implement por aprovar; nenhum fecho/commit/push ou outro IMP, sem gates externos.
Artefacto: memory-bank/bolts/006-rotas/implementation-walkthrough.md.
Evidência: experiments/construction/006-rotas/implement/.

## IMP-009 / 006-rotas — Test técnico, repetição humana pendente

Registo 2026-11-14T15:24:36Z. Implement e correções TTL/visibleAt aprovados explicitamente;
Model/Design/ADR/Implement concluídos, current_stage=test, completed=null.
Clean verify JDK21:1256 testes/0 falhas/0 erros/0 skips,16 XML,45 testes
novos e1211 anteriores. JAR novo411736bytes/SHA256
1a1beb8b849ccf505e4266dda199fe439f93e2a54e2297058a2d43eb2265af93.
237 hashes de Projeto/ preservados, nenhum Java/teste alterado nesta etapa.
Manual Codex53 checks/MANUAL_PASS/Java0/recursos terminados passou; ensaio inicial
falhado e ajuste de observação do auxiliar preservados, sem alterar JAR/fontes.
Roteiro humano pronto; nenhuma execução humana ou aprovação Test presumida.
D32 parcial; revisão final atribuída a Denji e integração pendentes. Sem gates
externos adicionais; sem fecho/commits/push ou início de outro IMP.
Relatório: memory-bank/bolts/006-rotas/ddd-03-test-report.md.
Manual: experiments/construction/006-rotas/manual-instructions.md.

## IMP-009 — Test/manual humano/revisão final aceites; integração pendente

Registo 2026-11-14T15:35:23Z. Utilizador aprovou Test, incluindo manual Luis da execução
20261114T152915Z-8s1k6ruu:53 CHECK_PASS/MANUAL_PASS/Java0,passed=true,
hashes corretos e recursos terminados. Report original preservado. Revisão final
atribuída a Denji confirmada concluída pelo utilizador; sem contacto inferido.
Clean verify1256/0/0/0 e JAR1a1beb8b... intactos. Fecho e commit para main
autorizados. Integração será comprovada antes de DONE; bolt/história/TASK ainda
em progresso, D32 parcial apenas até integração. Sem push/novo IMP.
Artefactos de aprovação em experiments/construction/006-rotas/closure/.

## IMP-009 / 006-rotas / TASK-012 — fecho após integração comprovada

Registo 2026-11-14T15:43:38Z. Versão revista integrada localmente em main: b7d74be5d9bad9fe7f598f29fa17188037647cf2;
268 caminhos e267 hashes do pacote conferidos,237 hashes de Projeto/ iguais ao
Test, V-ADD0/78 numa cópia extraída do commit. Fontes/JAR/manual humano imutáveis.
Clean verify JDK21:1256/0/0/0,16 XML; manual Luis53 CHECK_PASS/MANUAL_PASS/Java0/
passed=true e recursos terminados. Test aprovado; revisão final atribuída a Denji
confirmada pelo utilizador. D32 cumprido após integração; cinco etapas completas.
Bolt/história complete, implemented=true,TASK-012 DONE. Onze bolts complete,
24 planned, nenhum em execução. Unidade003-rotas/PHASE-03 continuam in-progress;
022-rotas apenas desimpedido,planned/não iniciado. Nenhum push solicitado.
Fontes/logs/XML/report humano originais preservados; target/classes/backups
transitórios fora dos commits, mantidos no disco. Commit de fecho posterior;
nenhum hash autorreferente ou resultado de publicação inventado.
Fecho: memory-bank/bolts/006-rotas/closure-walkthrough.md.
Prova: experiments/construction/006-rotas/closure/integration-proof.json.
