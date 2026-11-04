# IMP-002 — Fecho concluído após revisão e integração
Estado final: bolt/história **complete**, TASK-032 **DONE**, fecho em 2026-11-04T10:33:12Z.
Cinco etapas aprovadas, V-032/824 testes e manual pessoal de Luis confirmados.
G-REVIEW-032 cumprido: revisão final por **Luis**, distinta da implementação
produzida pelo agente **Codex**, e versão revista integrada em main por
fast-forward no commit `09a068f68eca473451e1e80a045c1cf9cf15a1b2`. Identidade Git é a configurada
pelo utilizador; não é prova de que o agente seja uma pessoa da equipa.

Mecanismo obrigatório executado com código 0, usando o adaptador já validado
para o frontmatter JSON/YAML local; ferramenta e adaptador mantêm os hashes.
[Comando e resultado](../../../experiments/construction/019-overlay/bolt-complete-command.json),
[saída original](../../../experiments/construction/019-overlay/bolt-complete.log) e
[prova de integração](../../../experiments/construction/019-overlay/integration-proof.json).

```text
Bolt status: in-progress → complete
005-implementar-parametrizacao-operacional: in-progress → complete
Stories: 1 updated, 0 skipped
Unit has 4 incomplete bolt(s), status unchanged
Intent has 6 incomplete unit(s), status unchanged
Bolt Complete: 019-overlay
```

Unidade 002-overlay continua in-progress. Os outros 31 bolts continuam planned;
002-overlay tem as dependências concluídas e blocks=false, sem ter sido iniciado.
Nenhum requisito global, GUI/CORE, streaming ou entrega é declarado concluído.
Listas geradas em Projeto/target/ excluídas de todos os commits deste incremento.
A publicação dos ramos será confirmada pelo resultado real do push.

## Validação final

V-ADD executado após a cascata: zero erros, 221 hashes, 45 fixtures e
3412 referências verificadas; [relatório](../../../experiments/construction/019-overlay/closure-validation.md).
`git diff --check` passou. Os XML preservados mantêm 824 testes sem falhas/erros/skips;
fontes e JAR iguais aos da execução aprovada. Integração por fast-forward de
conteúdo idêntico, seguida de alterações só documentais; sem nova execução Java.
[Conferência final de estados/hashes](../../../experiments/construction/019-overlay/closure-results.json).
Os logs/XML/diffs brutos preservam os bytes; exceções de whitespace limitadas
a esses artefactos no diretório de evidências, mantendo os checks de código/Markdown.

## Preparação anterior — registo histórico

O texto abaixo conserva o retrato anterior à autorização de fecho/publicação.
As pendências e proibições desse retrato foram resolvidas/substituídas acima.

Test e os 824 testes aprovados explicitamente pelo utilizador, incluindo a sua
execução manual como **Luis** em 2026-11-04T10:15:14Z: 25 CHECK_PASS, MANUAL_PASS
e saída Java 0 confirmada pelo utilizador. Aprovação registada em
2026-11-04T10:19:06Z. Autorizado concluir Test e preparar o fecho; **não autorizado
marcar DONE enquanto faltar G-REVIEW-032, fazer commits/push ou iniciar outro IMP**.

## Estado e critérios

As cinco etapas Model/Design/ADR/Implement/Test estão concluídas pelas aprovações
registadas. O tipo DDD não define uma sexta etapa: current_stage continua test
enquanto o fecho está pendente, com status=in-progress e completed=null.
História, unidade e TASK-032 continuam em progresso, sem implemented=true/DONE.
As três condições técnicas abaixo estão cumpridas; a condição de revisão/integração
permanece aberta no bolt/história.

| Critério técnico | Evidência aceite |
|---|---|
| 30 chaves, sete grupos, defaults/papéis/relações do ADD 6.3 | Model/Design/ADR aprovados; casos V-032 e Q-C-03 aprovados |
| Carga inválida antes de rede, sem configuração parcial | Casos de agregação/immutabilidade e manual com cinco problemas independentes |
| Perfis antigos válidos e construtor anterior preservado | Cinco perfis/defaults, overload público de 13 argumentos e testes antigos preservados |
| V-032 e regressão atual | clean verify/JDK21: 824 testes, zero falhas/erros/skips; 740 configuração +66 codec +18 TCP real |
| Manual do agente | Codex, 2026-11-03T22:58:31Z, 25 checks; [log](../../../experiments/construction/019-overlay/test-manual.log) |
| Repetição pessoal do utilizador | Luis, 2026-11-04T10:15:14Z, 25 checks/saída 0; [log separado](../../../experiments/construction/019-overlay/test-manual-luis.log) |

[Relatório Test aprovado](ddd-03-test-report.md) e
[walkthrough D01–D17](implementation-walkthrough.md). O código mantém exatamente
os quatro hashes da versão revista/executada; não se repetiram testes Java para
esta alteração exclusivamente documental. As execuções anteriores não foram
rebatizadas nem sobrescritas com datas/resultados novos.

## Proveniência da execução por Luis

Origem preservada: /tmp/ott-imp002-manual.tak20f.log, sem alterar/remover o original.
Cópia: experiments/construction/019-overlay/test-manual-luis.log, 4361 bytes,
SHA-256 `493522dbbfa85b3f8fe80f357759e4f1dd9b6bb444402d2e6b45d3d967ff55eb`.
[Metadados](../../../experiments/construction/019-overlay/test-manual-luis.json)
distinguem os instantes do log da data de preservação/aprovação registada.
As 25 etiquetas CHECK_PASS coincidem com as do roteiro executado pelo agente.
A saída Java 0 é a confirmação explícita do utilizador; não se inventa um comando
ou estado da shell ausente do log. Isto não designa Luis como revisor distinto.

O JAR da validação registada tem SHA-256
`b13a0bd6c928badfb0fd37cdb44d2e9699f683120b096490b60eed1a4ad43760`.
O log pessoal não inclui hash de JAR: a identificação do operador/resultados vem
do próprio log e da aprovação do utilizador, sem atribuir ao log metadados ausentes.

## G-REVIEW-032 — condições ainda pendentes

| Condição para DONE | Estado real |
|---|---|
| Revisor humano distinto do autor designado | Pendente; sem nome/designação confirmados |
| Revisão final da versão implementada | Pendente; aprovação de Test/manual não equivale à revisão distinta final |
| Integração na base comum | Pendente; alterações IMP-002 locais em Denji, HEAD 6bef742, sem commit deste incremento |

Estas condições vêm do [ADD](../../../add.md#10-riscos-pressupostos-e-dívida-técnica),
[TASK-032](../../../docs/tasks/PHASE-02/TASK-032.md),
[DoD](../../../docs/14-DEFINITION-OF-DONE.md) e do pedido explícito do utilizador.
Não se troca uma aprovação por outra nem se presume integração porque o build
passou. Manter a história/bolt e os dependentes em estados atuais; não iniciar
IMP-003/bolt 002 nem concluir unidade/requisitos/fase por inferência.

## Diff, preservação e ponto de retoma

Código de aplicação permanece o revisto: quatro ficheiros, +664/−2, com duas
substituições justificadas e nenhum teste antigo removido. POM, codec, transporte,
perfis e fixtures preservados. As duas listas Maven em Projeto/target/ ficam num
diff separado; não houve reposição artificial de resultados gerados.

Este checkpoint atualiza estados/aprovação/proveniência e prepara o fecho para
revisão. Os diffs documental/cumulativo e a conferência de hashes ficam em
experiments/construction/019-overlay/closure-preparation-*. A evidência do agente,
os XML e os logs anteriores permanecem intactos. Os relatórios/manifestos antigos
conservam o retrato anterior à aprovação; esta preparação regista o estado atual.

Não executar o mecanismo de fecho enquanto faltar alguma das três condições.
Sem fecho, commits, push ou outro IMP nesta continuação. Próxima ação: revisão
humana deste fecho e resolução explícita de G-REVIEW-032.

## Autorização de publicação e revisão final — 2026-11-04T10:31:39Z

O utilizador aprovou o fecho, commits e push e confirmou «sim já revi»
à pergunta sobre G-REVIEW-032. Revisor humano: Luis; implementação produzida
pelo agente Codex. Revisão final aceite sobre os quatro hashes de código
preservados em [final-review.json](../../../experiments/construction/019-overlay/final-review.json).
Esta autorização substitui a proibição anterior de commits/push. Integração
em main ainda por executar; só depois se aplica a cascata de fecho.
