# TASK-032 — Parametrização operacional validada

## 1. Identificação

- ID: TASK-032
- Fase: PHASE-02
- Prioridade: P1
- Estado: DONE
- Responsável: [Construction](../../../memory-bank/standards/execution-contract.md), trabalho técnico transversal; implementação produzida pelo agente Codex.
- Revisor: Luis, humano distinto do agente Codex que produziu a implementação; revisão final confirmada nesta sessão («sim já revi»).
- Dependências: [TASK-003](../PHASE-01/TASK-003.md), [TASK-005](TASK-005.md).
- Requisitos relacionados: [REQ-024](../../../specs.md#req-024), [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Fornecer defaults centrais e propriedades opcionais estritamente validadas aos componentes.

## 3. Contexto

Resposta 2A do utilizador e ADR-016 do [ADD](../../../add.md). Os parâmetros antes
descritos como configuráveis ainda não são aceites pelo loader existente.

## 4. Pré-requisitos

Base implementada verificada; respeitar os bytes v1, os perfis existentes e a
imutabilidade. Não reabrir TASK-003/005 por acrescentar esta extensão.

## 5. Especificação técnica

Implementar RuntimeConfiguration imutável com defaults e chaves do ADD, secção 6.3.
O loader aplica defaults apenas às chaves operacionais opcionais. Identidades,
hosts e portas continuam obrigatórios. Rejeitar desconhecidas, inaplicáveis,
duplicadas, NaN/infinito, overflow e relações temporais inválidas antes de sockets.
BOOTSTRAP usa só parâmetros de controlo/consulta; restantes papéis aceitam os
parâmetros overlay/vídeo aplicáveis. A runtime composition recebe o objeto validado.

## 6. Ficheiros e componentes envolvidos

config/RuntimeConfiguration.java (novo), NodeConfiguration.java,
ConfigurationLoader.java, ConfigurationLoaderTest.java e perfis .properties
apenas quando útil para exemplificar overrides. Wiring de cada serviço ocorre
na tarefa que o implementa, conservando a origem dos valores.

## 7. Passos de implementação

- [x] Modelar as opções, defaults, limites e regras cruzadas do ADD.
- [x] Integrar carga estrita e erros agregados sem estado parcial.
- [x] Testar defaults antigos, overrides válidos e rejeições/casos limite.
- [x] Confirmar que um consumidor recebe os valores validados sem abrir rede.

## 8. Regras e restrições

Não alterar IDs, formatos TCP/UDP, hard limits de proteção ou políticas aprovadas
por uma propriedade. Aumentar esses limites exige ADR e compatibilidade explícita.

## 9. Casos limite e tratamento de erros

Valores zero/negativos/excessivos, alpha fora de (0,1], NaN/infinito,
renovação ≥lease, validade ≤período de anúncio, retry inicial >máximo e chave
overlay em BOOTSTRAP. Defaults não mascaram configurações obrigatórias ausentes.

## 10. Testes necessários

V-032: perfis atuais mantêm os defaults; overrides válidos são preservados;
valores/papéis/relações inválidos produzem ConfigurationException agregada,
sem sockets/estado parcial; regressão de configuração/codec/transporte preservada.

## 11. Critérios de aceitação

- [x] Chaves, defaults, papéis e relações correspondem ao ADD, secção 6.3.
- [x] Carregamento inválido falha antes de rede e sem configuração parcial.
- [x] Perfis existentes continuam válidos sem propriedades novas.
- [x] V-032 e revisão/integração exigidas têm evidência real.

## 12. Definition of Done

Cumprir [DoD](../../14-DEFINITION-OF-DONE.md), registar autor/revisor e versão
revista, atualizar docs/15 e o bolt; nenhum DONE sem testes/revisão/integração.

## 13. Notas de retoma

Arranque em 2026-11-03T22:09:01Z, apenas Model do bolt 019-overlay/IMP-002.
[Modelo preparado](../../../memory-bank/bolts/019-overlay/ddd-01-domain-model.md)
aprovado pelo utilizador em 2026-11-03T22:16:54Z. [Design aprovado](../../../memory-bank/bolts/019-overlay/ddd-02-technical-design.md),
com tipos/assinaturas, conversões, compatibilidade, agregação de erros/casos V-032 e
prova de consumidor sem sockets. Estas eram pendências na apresentação de Design;
Design/alpha aprovados em 2026-11-03T22:25:30Z, registados em
[Q-C-03](../../construction-duvidas.md#q-c-03--gramática-decimal-de-alpha).
Revisão ADR e Implement aprovados; [Test aprovado](../../../memory-bank/bolts/019-overlay/ddd-03-test-report.md).
Clean verify/JDK21 em 2026-11-03T22:56:28Z: 824 testes aprovados, zero falhas/erros/
skips (740 configuração, 66 codec, 18 TCP real). Manual do agente: 25 checks e
MANUAL_PASS. Código igual à versão revista de Implement. Luis repetiu pessoalmente em
2026-11-04T10:15:14Z: 25 CHECK_PASS/MANUAL_PASS, saída Java 0 confirmada. Test
aprovado explicitamente, com registo em 2026-11-04T10:19:06Z; etapas concluídas.
[Fecho concluído](../../../memory-bank/bolts/019-overlay/closure-walkthrough.md) em 2026-11-04T10:33:12Z.
G-REVIEW-032 cumprido: revisão final por Luis confirmada («sim já revi»), distinta
da autoria do agente, e integração em main do commit 09a068f68eca473451e1e80a045c1cf9cf15a1b2.
Estado DONE após executar cascata specsmd; unit 002-overlay permanece in-progress.

Esta tarefa passa antes de TASK-006 no DAG, apesar do ID posterior. O ADD contém
a ordem normativa; não selecionar trabalho apenas por ordem numérica de TASK.
