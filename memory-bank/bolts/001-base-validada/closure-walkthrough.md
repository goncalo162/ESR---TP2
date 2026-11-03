# IMP-001 — Checkpoint 5: preparação do fecho

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
