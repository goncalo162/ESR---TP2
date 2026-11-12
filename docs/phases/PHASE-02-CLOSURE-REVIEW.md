# PHASE-02 — Dossier de fecho

**Estado atual: DONE, incluindo EXTRA-01 de controlo.** Aceitação final explícita
registada em 2026-11-12T10:46:24Z. O dossier original abaixo descreve a base da fase;
a extensão concluída está no suplemento final, sem substituir a sua evidência.


Preparado em 2026-11-10T23:34:59Z sobre a base comum `223ea6ff342525fdd211445753b47129494f2805`.
A evidência técnica da fase está reunida e as seis tarefas estão DONE e
integradas. A PHASE-02 está DONE após confirmação da validação pela equipa.
A unidade de construção 002-overlay está completa.
[Fase 02](PHASE-02-OVERLAY.md) · [Progresso](../15-PROJECT-PROGRESS.md) ·
[Unidade 002-overlay](../../memory-bank/intents/001-ott-streaming/units/002-overlay/unit-brief.md).

## Tarefas integradas

Os commits abaixo foram verificados como antepassados da base comum.

| Tarefa | Prova preservada | Revisão integrada |
|---|---|---|
| TASK-005 | V-005. TCP full-duplex, limites, callbacks e encerramento. 18 testes TCP na revisão de 149 testes. [Evidência](../../experiments/task-005-transport-validation.md) | `6bef742` |
| TASK-032 | V-032. Configuração, consumo offline, compatibilidade e erros agregados. 824 testes e manual de Luis. [Fecho](../../memory-bank/bolts/019-overlay/closure-walkthrough.md) | `09a068f` |
| TASK-006 | V-006. Consulta inicial, resposta completa antes de EOF e shutdown. 856 testes e execução manual. [Fecho](../../memory-bank/bolts/002-overlay/closure-walkthrough.md) | `03345e9` |
| TASK-007 | V-007. HELLO, unicidade, instâncias e reconexão. 903 testes e manual. [Fecho](../../memory-bank/bolts/003-overlay/closure-walkthrough.md) | `8db7b78` |
| TASK-008 | V-008. Quatro papéis, comandos CLI e ciclo de vida. 981 testes, manual de Luis e verificação complementar. [Fecho](../../memory-bank/bolts/020-overlay/closure-walkthrough.md) | `8c30b04` |
| TASK-009 | V-009. Demonstração integrada em loopback com PCAP e logs. Q-C-15 regista a evidência aceite e a dispensa de repetição independente. [Fecho](../../memory-bank/bolts/004-overlay/closure-walkthrough.md) | `cc478d1` |

## Critérios da fase

| Critério de PHASE-02 §11 | Evidência disponível para a revisão |
|---|---|
| Cada tarefa da fase está DONE | Os seis estados foram conferidos nos ficheiros de tarefa e os commits de integração constam de `main`. |
| Três nós iniciam fora de ordem, fazem handshake e reconectam sem duplicados | N2 e N1 obtiveram a lista antes de N0 arrancar. Três pares HELLO/HELLO_OK formaram o triângulo, com dois vizinhos ACTIVE por nó. Apenas a aresta N0–N1 mudou de conexão no corte. [V-007](../../memory-bank/bolts/003-overlay/ddd-03-test-report.md) · [Resultado V-009](../../experiments/milestone-1/runs/20261110T231137Z-9917llvm/analysis/result.json) |
| Bootstrap fornece os endpoints configurados | Três pares REGISTER/REGISTER_OK foram correlacionados por requestId. N0 consta das respostas a N1 e N2 enquanto está offline. A implementação lê o grafo configurado, responde e fecha a consulta. [ADR-013](../../add.md#adr-013) · [V-006](../../memory-bank/bolts/002-overlay/ddd-03-test-report.md) |
| Nós já descobertos reconectam com bootstrap parado | A nova ligação N0–N1 teve novo HELLO/HELLO_OK com os mesmos PIDs e instanceIds. O PCAP tem zero pacotes para BOOT na janela estável de 30,030 s e entre a paragem de BOOT e a marca de reconexão. O CLIENT enviou NODE_LEAVE aos dois vizinhos diretos. [Frames](../../experiments/milestone-1/runs/20261110T231137Z-9917llvm/analysis/frames.json) · [Ações](../../experiments/milestone-1/runs/20261110T231137Z-9917llvm/technical-actions.jsonl) |
| Entregável integrado demonstrável | BOOT, SERVER, RELAY e CLIENT foram executados como JVMs distintas em loopback. A captura fechou com saída 0, 89 pacotes gravados e zero perdas reportadas pelo kernel. As quatro JVM saíram com código 0 e as sete portas ficaram disponíveis. [Roteiro](../../experiments/milestone-1/README.md) · [PCAP](../../experiments/milestone-1/runs/20261110T231137Z-9917llvm/control.pcap) · [Relatório de Test](../../memory-bank/bolts/004-overlay/test-walkthrough.md) |
| Requisitos, limitações e recursos pendentes rastreados | A fase cobre CLI, descoberta, vizinhanças TCP, saída e reconexão. A matriz conserva as obrigações das fases seguintes e o progresso assinala a pendência docente. [Rastreabilidade](../13-REQUIREMENTS-TRACEABILITY.md) · [Progresso](../15-PROJECT-PROGRESS.md) |
| Regressão dos componentes afetados | Os sete XML Surefire da regressão de V-008 totalizam 981 testes, zero falhas, erros ou ignorados. Foram conferidos os 157 hashes de `Projeto/` fora de `target/` contra a versão dessa regressão. O SHA-256 do JAR confere com o manifesto do ensaio V-009. [XML](../../experiments/construction/020-overlay/test-clean-verify-01-xml/) · [Regressão de referência](../../experiments/construction/020-overlay/test-clean-verify-01.log) |

## Limites e decisão da revisão

- O ensaio integrado aceite é V-009 no diretório `20261110T231137Z-9917llvm`.
  A tentativa anterior ficou incompleta no fecho da captura. Q-C-15 conserva
  a dispensa de repetição independente. As conclusões sobre tráfego para
  BOOT aplicam-se às duas janelas observadas no PCAP.
  [Q-C-15](../construction-duvidas.md#q-c-15--evidência-aceite-no-fecho-de-v-009).
- G-ASSESSMENTS regista a data e o conteúdo como pendentes de informação
  docente. O ADD admite esse estado. A evidência desta fase cobre a overlay
  de controlo em loopback. Streaming, GUI, cenários CORE e medições de
  desempenho têm os seus próprios oráculos nas fases seguintes.
  [Gates do ADD](../../add.md#gates-e-momento-de-resolução).
- A conferência de preparação incluiu os estados, a ancestralidade Git,
  os XML arquivados e os hashes do código, JAR e provas de V-009. O V-ADD
  é uma execução documental desta preparação. Os testes Java e o ensaio
  integrado referem-se aos resultados arquivados.
  [V-ADD da preparação](../../experiments/construction/phase-02-review-v-add.log).

Em 2026-11-10T23:39:09Z foi registada a confirmação do utilizador de que a equipa
validou a fase e autorizou o fecho. O registo usa a data da confirmação e a
base integrada `223ea6f`. A data e o conteúdo oficiais da avaliação
mantêm-se pendentes em G-ASSESSMENTS. A PHASE-03 está TODO com a dependência
da PHASE-02 cumprida. O ponto de retoma é **Model do 005-rotas / IMP-007 /
TASK-010**, para RTT e falhas TCP.
[V-ADD do fecho](../../experiments/construction/phase-02-closure-v-add.log).
[Coordenação](../team/00-COORDINATION.md#5-quatro-momentos-de-revisão-por-tarefa) ·
[Definition of Done](../14-DEFINITION-OF-DONE.md#fase) ·
[Próximo bolt](../../memory-bank/bolts/005-rotas/bolt.md).

## Suplemento — fecho de EXTRA-01

Aceitação explícita de Luis em registo 2026-11-12T10:46:24Z: «Podes fechar o bolt e e esta fase».
TASK-033/IMP-034 e TASK-034/IMP-035 DONE; as oito tarefas da tabela de PHASE-02
estão DONE e os sete bolts da unidade002-overlay complete. Fonte/modelo de
reparação: [ADR-035](../../add.md#adr-035).

- Aprendizagem V-033:1065 testes/manuais Luis53, revisão e integração f650be0.
- Reparação V-034:1113 testes/11 XML e manual humano selecionado66 checks,
  MANUAL_PASS/passed/no_processes_pending. Dois grafos com SIGKILL sequencial,
  bootstrap parado, mesmos sobreviventes, ligação física única e S–R1 preservada.
- Versão15efdf2 integrada em main e documentaçãoff0ef0b já integradas antes do
  fecho. [Fecho do bolt](../../memory-bank/bolts/035-overlay-extra/closure-walkthrough.md)
  e [aceitação/integridade](../../experiments/construction/035-overlay-extra/completion-authorization.json).
- G-ASSESSMENTS continua com data/conteúdo docentes pendentes. A fase está
  concluída no âmbito de controlo; continuidade de vídeo via novas arestas fica
  para routing/streaming. Nenhum resultado GUI/CORE/vídeo é presumido.

005-rotas/IMP-007 tem pré-requisitos satisfeitos e permanece planned. Nenhuma
etapa da fase3 foi iniciada por este fecho; o intent/projeto continuam em curso.
