# IMP-009 / 006-rotas / TASK-012 — fecho autorizado

Registo 2026-11-14T15:43:38Z. Versão revista integrada **localmente em main**:
`b7d74be5d9bad9fe7f598f29fa17188037647cf2`; árvore `227e24c1efad5c47fd9015af2bf675c9de28964a`.
[Prova estruturada](../../../experiments/construction/006-rotas/closure/integration-proof.json)
e [V-ADD extraído do commit](../../../experiments/construction/006-rotas/closure/integration-commit-v-add.log):
268 caminhos iguais ao pacote,267 hashes do manifesto e237 entradas de Projeto/
conferidos. O commit de fecho documental é posterior; não inventar hash próprio.

## Critérios e aceitação

- V-012/D01–D31: losango/ciclo, primeira chegada140→130, quatro streams,
  retirada por origem, alternativas, gerações/path/cache/quotas, metadados,
  TTL revalidado antes de cada envio e visibleAt puro/conservador.
- Clean verify JDK21:1256/0falhas/0erros/0skips,16XML; [resultado](../../../experiments/construction/006-rotas/test-build-01/result.json).
- JAR411736bytes,SHA2561a1beb8b849ccf505e4266dda199fe439f93e2a54e2297058a2d43eb2265af93,
  major65 e sem testes empacotados. Fontes e JAR não mudaram após validação/revisão.
- Manual humano Luis:53 CHECK_PASS,MANUAL_PASS,Java0,passed=true,hashes corretos,
  JVM terminada/no_resources_pending=true. [Report original](../../../experiments/construction/006-rotas/test-manual-runs/20261114T152915Z-8s1k6ruu/report.json),
  SHA256f3b3d2102fe887f42cda588663ade110c2b97f6fc436ddb9c90d5c0188671005;
  separado dos ensaios Codex, incluindo o primeiro falhado preservado.
- [Test aprovado](../../../experiments/construction/006-rotas/closure/test-acceptance.json)
  e [revisão final atribuída a Denji confirmada concluída pelo utilizador](../../../experiments/construction/006-rotas/closure/final-review-confirmation.json),
  sem alegar contacto independente ou data de revisão não fornecida.
- [Fecho/commit main autorizados](../../../experiments/construction/006-rotas/closure/authorization.json);
  integração agora comprovada e D32 cumprido. Sem gate externo adicional.

## Cascata e limites

Cinco etapas completas, bolt complete/completed real, história complete/
implemented=true e TASK-012 DONE. Critérios do ADD e TASK assinalados. Índices,
progresso e unidade atualizados. Onze bolts complete,24 planned, nenhum em
execução; unidade003-rotas/PHASE-03 continuam in-progress por TASK-013.
022-rotas apenas blocks=false, mantendo planned e started/completed/current_stage
null; nenhum IMP novo iniciado. Requisitos globais de dados/UI/CORE não passam
DONE por este fecho. Sem vídeo/GUI/CORE ou entrega/submissão alegados.

D29/D31 incluem testes e inspeção estática dos limites, sem inventar alocação de
máximos, heap ou desempenho. A espera por geração nova e a projeção conservadora
são limites aprovados, não disponibilidade contínua. Não é prova de PCAP sem
tráfego nem ausência de TIME_WAIT. Os resultados e limites já aceites mantêm-se.

## Preservação e Git

Seis caminhos de fonte +1080/−8 contra a base164ac0d; substituições do nó
justificadas no walkthrough. Testes anteriores intactos. Logs/XML/report humano,
JAR e provas de falha preservados. Espaços de patches/logs brutos mantidos por
atributos locais, conforme precedente021-rotas; fontes/Markdown/auxiliares
continuam sujeitos ao check normal.

Nenhuma alteração em Projeto/target integra os commits. Backup pré-clean/classes
manuais temporárias ficam no disco e fora de Git por regras locais explícitas;
fontes dos auxiliares, logs, reports e JAR validado são versionados. Nenhum build
ou manual repetido porque não houve alteração de fonte. Referência remota main
confirmada164ac0d antes de integrar, sem divergência. Nenhum push foi pedido ou
executado; origin/main permanece no estado anteriormente observado.
