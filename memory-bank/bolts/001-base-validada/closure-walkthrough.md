# IMP-001 — Checkpoint 5: fecho aprovado e aplicado

**Bolt, história e unidade complete em 2026-11-03T21:56:48Z**, por execução
real do mecanismo validado após integrar a versão revista em main. História
implemented=true; current_stage=null e completed real no bolt. Todos os
critérios de IMP-001 e Plan/Implement/Test concluídos; nenhum outro IMP iniciado.

O utilizador aprovou o fecho e autorizou commits/integração/push, com registo em
2026-11-03T21:51:13Z. [Autorização](../../../experiments/construction/001-base-validada/closure-authorization.json).
Luis executou o roteiro: [22 CHECK_PASS e MANUAL_PASS](../../../experiments/construction/001-base-validada/manual-smoke-luis.log),
com [origem/instantes/SHA-256](../../../experiments/construction/001-base-validada/manual-smoke-luis.json).
A cópia é idêntica a /tmp/ott-imp001-manual.ijH5h9.log, que foi mantido.

| Commit integrado | Âmbito |
|---|---|
| b603dc1dcef01a6cd38d0b89ee43695e686d5f49 | Inception: 20 ficheiros correspondem exatamente aos hashes anteriores ao bolt |
| d65f94740ef95ffc67d312f7fbccac7575c86f7a | IMP-001 revisto: documentação, evidência, execução por Luis e mecanismo de fecho |

[Proveniência](../../../experiments/construction/001-base-validada/inception-commit-provenance.json)
e [integração comprovada](../../../experiments/construction/001-base-validada/integration-proof.json):
main=Denji=d65f947 antes de fechar; tree 79710a1a693c841928fd197853b22e9f531ac7df,
ancestralidade confirmada e zero alterações em Projeto/ nos commits. main recebeu
git merge --ff-only Denji; não houve conflitos ou divergências remotas.

Depois dessa integração foi executado o comando validado documentado abaixo.
[Log real](../../../experiments/construction/001-base-validada/completion-real.log)
e [metadados](../../../experiments/construction/001-base-validada/completion-real-command.json):
código 0, uma história atualizada, unidade concluída e intent preservado por ter
seis unidades incompletas. A execução real, e não o ensaio temporário, forneceu
a data de conclusão. Script instalado/adaptador mantêm os hashes ensaiados.

Atualizados estados/datas reais, checkboxes, índices, rastreabilidade e progresso.
TASK-001–005 mantêm DONE; TASK-006–032 e os outros 32 bolts não foram iniciados.
Nenhum requisito global, fase, intent, GUI/CORE ou entrega foi concluído por esta
verificação. Projeto/target/ está excluído dos commits; a única alteração local
gerada foi preservada. Os outros 162 hashes rastreados de Projeto/ são iguais.

A evidência anterior permanece intacta. As regras locais de whitespace aplicam
exceção só a logs/patches/saídas brutas para preservar bytes/hash; fontes, scripts,
metadados e Markdown continuam com os checks normais. V-ADD é novamente executado
sobre os estados finais; publicação Denji/main autorizada sem force push, com
resultado e referências remotas a confirmar depois de executar.

[Conferência final dos estados e preservação](../../../experiments/construction/001-base-validada/closure-final-proof.json)
e [V-ADD final](../../../experiments/construction/001-base-validada/planning-validation-final.md),
com zero erros. O metadado derivado blocks de 019-overlay passou a false porque
a sua única dependência foi concluída; continua planned/started=null, sem iniciar
outro IMP. O marcador GENERATED permanece no índice como proveniência documental,
com estado complete explícito apenas na história concluída.

## Retrato histórico da preparação, anterior à integração e ao fecho

As secções seguintes preservam a revisão apresentada no checkpoint 5. As suas
pendências e instruções no futuro descrevem aquele momento; o estado final é
o registado acima, com os comprovativos de integração e execução real.

Checkpoint 4 e resultados aprovados explicitamente pelo utilizador, com registo
em 2026-11-03T21:40:56Z. Autorizado avançar apenas para o checkpoint 5.
Preparação inicial registada em 2026-11-03T21:42:46Z. O utilizador aprovou depois
o fecho de IMP-001 e autorizou commits separados de Inception/IMP-001,
integração em main e push de Denji/main para origin, sem force push, com registo
em 2026-11-03T21:51:13Z. Integração/fecho serão confirmados após execução real.

Execução manual por Luis preservada em [log](../../../experiments/construction/001-base-validada/manual-smoke-luis.log):
22 CHECK_PASS e MANUAL_PASS. Os apontamentos de pendência abaixo retratam a
preparação do checkpoint 5; a evidência de integração e os estados finais serão
atualizados depois de integrar a versão revista, antes de publicar.

## Critérios e revisão

| Critério | Evidência/estado |
|---|---|
| Componentes reais e TASK-001–005 DONE preservadas | Inspeção aprovada nos checkpoints 2/3; POM/fontes/configuração/fixtures/testes sem alterações |
| 45 fixtures, 16 tipos e ADD/specs/DAG conferidos | Auditoria independente e V-ADD aprovados no checkpoint 4 |
| Regressão existente com log novo | clean verify/JDK 21: 149 testes, zero falhas/erros/skips; relatórios Surefire novos |
| Teste manual adequado à base | 22 CHECK_PASS: configuração/codec/TCP/shutdown e reutilização de porta |
| Revisão dos resultados | Aprovação explícita do utilizador do checkpoint 4; não atribuída por inferência a outra pessoa |
| Revisão documental final e integração | Pendentes; os documentos do bolt continuam locais e não há evidência de integração na base comum |

Relatório: [test-walkthrough](test-walkthrough.md). Evidência aceita no checkpoint 4
em experiments/construction/001-base-validada/, preservada sem sobrescrever logs,
XML, auditoria ou manifesto desse checkpoint. Não se repetiu Maven/teste manual
por alterações documentais; a validação documental é executada novamente.
GUI/CORE/vídeo não foram executados nem são comprovados pelo loopback.

## Estados e alcance do checkpoint

Plan, Implement e Test concluídos; a última etapa tem como data de conclusão
o registo real da aprovação do checkpoint 4. Os três critérios técnicos do bolt
estão assinalados. O bolt continua in-progress/test, completed=null: não há etapa
"closure" no tipo local, pelo que o fecho pendente é explicado no corpo/registo.
Unidade in-progress. História conserva planned/sem implemented=true até a
cascata de fecho; o índice explicita esse estado e a execução do bolt.

TASK-001–005 mantêm DONE; TASK-006–032 e os outros 32 bolts não foram iniciados.
Nenhum requisito global, fase, intent, GUI/CORE ou entrega é declarado concluído.
Atualizados progresso, rastreabilidade, índice, registo e apontadores locais.
DoD e critérios dos TASK pais foram revistos sem alterar as regras ou o seu estado.
O próximo código pelo DAG é IMP-002/019-overlay/TASK-032, apenas após fecho da base
e pedido de execução; o seu revisor distinto mantém-se gate próprio.

## Integração e mecanismo de fecho

A [DoD](../../../docs/14-DEFINITION-OF-DONE.md) exige revisão/integração para DONE;
a [coordenação, secções 6–7](../../../docs/team/00-COORDINATION.md#6-ramos-ficheiros-partilhados-e-integração)
permite revisão por diff local e exige integração pela prática Git da equipa.
A aprovação dos resultados é real; integração ainda não foi comprovada.
Não se cria commit, branch ou push para presumir essa integração. Não é um
gate externo novo: preserva a condição de fecho já reiterada pelo utilizador.

O mecanismo obrigatório da skill bolt-start foi inspecionado e ensaiado em
cópias temporárias. O script instalado não tem fs-extra/js-yaml disponíveis;
o seu YAML genérico não respeita o subset JSON do validador local. [Q-C-02](../../../docs/construction-duvidas.md#q-c-02--compatibilidade-do-mecanismo-de-fecho)
regista a adaptação de ferramenta, sem nova ADR ou dependência runtime.

O [adaptador](../../../experiments/construction/001-base-validada/bolt-complete-json-frontmatter.cjs)
usa apenas Node builtin, restringe as substituições ao script instalado e mantém
a lógica de atualização de bolt/história/unidade/intent. O
[ensaio](../../../experiments/construction/001-base-validada/completion-tool-compatibility.json)
teve código 0 com metadados aceites pelo parser; o
[log](../../../experiments/construction/001-base-validada/completion-rehearsal-json-subset.log)
mostra uma história atualizada, unidade concluída e intent preservado por ter
seis unidades incompletas, exclusivamente na cópia. Não é prova de fecho real.
Os outros 32 bolts permaneceram intactos; o script instalado não foi editado.

Comando preparado para aplicar o fecho na raiz, apenas após revisão/integração
confirmadas, não executado neste repositório:

```sh
node --require ./experiments/construction/001-base-validada/bolt-complete-json-frontmatter.cjs /home/luis/.specsmd/aidlc/scripts/bolt-complete.cjs 001-base-validada
```

Depois, conferir cascata, checkboxes/índice/progresso e timestamps reais, validar
documentação e guardar a saída real. Não atribuir à execução em cópia o valor
de completed do bolt real. Não se pede nova aprovação dos resultados do checkpoint 4.

## Diff e evidência final desta preparação

Baseline antes do checkpoint 5: checkpoint-5-baseline.json, com textos e hashes
de toda a evidência anterior. Diff incremental: checkpoint-5.diff; cumulativo
do bolt: bolt-through-checkpoint-5.diff; scripts auxiliares: checkpoint-5-evidence-scripts.diff.
O pre-existing.diff continua a separar as alterações de Inception.
O manifesto de fontes atualiza apenas os hashes de docs/13 e docs/15, com
motivo e hashes anteriores preservados; nenhuma fonte de código é ressincronizada.

Os 162/163 hashes de Projeto/ iguais e a única lista gerada alterada pelo build
mantêm-se como no checkpoint 4. A preservação detalhada, git diff --check, estados
e numstat ficam em checkpoint-5-preservation.json. Nenhuma nova execução Java,
medição, data docente, aprovação pessoal, integração ou upload é inventado.

Validação documental desta preparação em 2026-11-03T21:45:46Z:
[V-ADD com snapshot](../../../experiments/construction/001-base-validada/planning-validation-checkpoint-5.md),
zero erros, 220 hashes e 45 fixtures conferidos. O relatório guarda a hora e
a contagem de referências da execução mais recente após os apontadores finais.

Parar no checkpoint 5 para revisão deste fecho e confirmação da integração na
base comum. O estado complete só será aplicado quando essa condição estiver
satisfeita; não iniciar outro bolt por inferência.
