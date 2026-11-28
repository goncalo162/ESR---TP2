# TASK-020 — Ler fontes MJPEG e controlar ritmo de publicação

## 1. Identificação

- ID: TASK-020
- Fase: [PHASE-05 — Vídeo, replicação e reprodução](../../phases/PHASE-05-STREAMING.md)
- Prioridade: P0
- Estado: IN PROGRESS
- Responsável: [Fillmore](../../team/FILLMORE.md).
- Revisor: [Denji](../../team/DENJI.md), antes de integrar e marcar DONE.
- Dependências: [TASK-002](../PHASE-01/TASK-002.md), [TASK-003](../PHASE-01/TASK-003.md), [TASK-019](../PHASE-05/TASK-019.md).
- Requisitos relacionados: [RF-012](../../02-REQUIREMENTS.md#rf-012), [RF-013](../../02-REQUIREMENTS.md#rf-013), [RF-015](../../02-REQUIREMENTS.md#rf-015), [RNF-001](../../02-REQUIREMENTS.md#rnf-001), [RP-003](../../02-REQUIREMENTS.md#rp-003).

## 2. Objetivo

Produzir frames válidos de vídeos distintos com pacing e procura controlada.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. A responsabilidade específica é ler fontes mjpeg e controlar ritmo de publicação. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

A TASK-002 confirmou o perfil docente: cinco bytes de dígitos decimais ASCII com L, seguidos de L bytes JPEG, limite de 99999 bytes por frame; ver secção 7 de [07](../../07-API-AND-INTERFACES.md) e [revisão V-002](../../../experiments/task-002-contract-review.md). Reobter o recurso por URL/hash se a cópia transitória de /tmp não existir. Preparar os conteúdos A/B e C/D identificados nessa revisão; Na revisão histórica,B/C/D eram fontes MP4/MKV/MOV por descarregar/converter;preparação atual está documentada no checkpoint abaixo. Não basta alterar a extensão. Registar parâmetros da preparação offline, créditos e hashes; sem acesso ao conteúdo ou formato compatível, o gate continua por resolver. Validar ficheiros e JPEG com limites antes de anunciar disponibilidade. Cada stream tem scheduler, sequência e posição independentes. Sem procura, parar leitura/envio; em loop manter seq e timeline; loop=false termina apenas essa stream.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: server/VideoSource.java e StreamingServer.java; planeados: server/VideoSourceTest.java e fixtures MJPEG pequenas autorizadas.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [x] Parte A/IMP-017:recurso docente preservado por hash,quatro conteúdos preparados,proveniência/créditos/condições pendentes registados no manifesto.
- [x] Parte B/IMP-018:leitura de um frame com EOF e truncatura explícitos.
- [ ] Configurar dois vídeos distintos por cada servidor e registar hashes.
- [ ] Agendar emissão ao FPS sem catch-up ilimitado e só com procura confirmada.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Fonte ausente, cabeçalho inválido, frame JPEG ilegível, leitura parcial, vídeo termina durante subscrição e loop curto. Se recurso estiver indisponível, marcar esta tarefa BLOCKED na construção; não inventar leitor.

## 10. Testes necessários

V-020: fixture confirmada com dois frames produz bytes esperados; ficheiro truncado falha de forma localizada; relógio falso confirma pacing/ausência de envio sem procura; quatro conteúdos são identificáveis.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Formato real está documentado e testado.
- [ ] Há dois conteúdos diferentes por origem na configuração de entrega.
- [ ] Ritmo, EOF e loop têm comportamento determinado.
- [ ] Validação V-020 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.

## IMP-017 — parte A com Test/G-MEDIA técnicos aprovados,limitação B aceite

Implement aprovado. Nova auditoria final:22687 JPEGs headless/sem avisos,
16 fronteiras/negativos e6 pares distintos. Binários/manifesto aprovados intactos.
[Test](../../../memory-bank/bolts/011-video/test-walkthrough.md),
[manual](../../../experiments/construction/011-video/manual-instructions.md).
Manual pessoal Francisco13/7 etapas conferido;quatro conteúdos distintos. Créditos
B observados mas ilegíveis,C/D legíveis nas amostras. Test/G-MEDIA técnicos aprovados
pelo utilizador com limitação B aceite;LOCAL-04/G-MEDIA-DISTRIBUTION abertos para
TASK-030,sem aprovação de atribuição adequada,legibilidade/redistribuição ou licença inventada de A.
[Avaliação LOCAL-04](../../../experiments/construction/011-video/manual-verification-01/B-readability-assessment.md).
LOCAL-03:cópia/hash para Projeto/media posterior,perfis inalterados/não prontos.
459820128 bytes considerados no plano posterior,sem escolha de LFS/externo/conteúdo
por inferência. Revisão final/integração pendentes. Parte A não fecha
pai nem leitor/emissão/pacing B/C. Sem staging/commits/push/fecho ou alteração
Java/perfis/POM/fixtures/demo.

Estratégia Git LFS foi posteriormente aprovada e aplicada só aos quatro media:
.gitattributes4 regras,configuração local,materialização real4/4 com hashes aprovados.
[Execução/efeitos de instalação](../../../experiments/construction/011-video/lfs-implementation-01/README.md).
Nenhum conteúdo/Java/perfil alterado,index/histórico intactos,sem upload/push/billing.
Revisão final/integração/LOCAL-04/G-MEDIA-DISTRIBUTION pendentes;proprietário deve
confirmar quota/budget antes de publicar,não é pressuposto custo zero.

## Fecho de IMP-017 — parte A concluída;TASK-020 IN PROGRESS

IMP-017/011-video complete,história implemented=true por autorização explícita.
Integração local em main8a91d9fbf0bd1de60f1d29e5576625c9b8320c81 comprovada:
598 caminhos/hashes/modos,quatro ponteiros LFS e vídeos reais materializados
exclusivamente do cache local,V-ADD extraído0 erros. Test/G-MEDIA técnicos,
22687 JPEGs/16 fronteiras,manual Francisco13 e revisão final Denji cumpridos.
[Fecho](../../../memory-bank/bolts/011-video/closure-walkthrough.md).
Só preparação de conteúdos concluída;partes B/C (IMP-018/019),leitura/pacing/emissão
pendentes. Não se marca o pai DONE nem a configuração de entrega instalada.
LOCAL-03 permanece sem cópia para Projeto/media;LOCAL-04 e G-MEDIA-DISTRIBUTION
abertos para TASK-030. Limitação B ilegível aceite só tecnicamente,não aprova
atribuição adequada,legibilidade,redistribuição/publicação ou licença de A.
Unidade005/PHASE-05 em progresso;Homebrew separado,sem intervenção;sem push.

## IMP-018 — parte B iniciada apenas em Model

Registo 2026-11-25T15:32:21Z: [Model de025-video](../../../memory-bank/bolts/025-video/ddd-01-domain-model.md)
preparado para revisão,LOCAL-01/02 propostos. Leitor com validação de registo/
JPEG,EOF/erros/ownership e M01–M14/V-020-B planeados;nenhuma execução nova.
Sem leitura/análise de fontes,Java/Design/ADR/Implement/Test,Maven,commit ou
push. Parte A permanece concluída;parte B não implementada;parte C não iniciada.
Estado pai IN PROGRESS,sem ampliar dois caminhos do ADD ou gates externos.

## IMP-018 — Model aprovado, Design apenas para revisão

Registo 2026-11-25T15:46:47Z: Model/LOCAL-01/02 aprovados; [Design](../../../memory-bank/bolts/025-video/ddd-02-technical-design.md)
concretiza API,ownership/erro/fecho,sem progresso e caps compatíveis com os
quatro conteúdos antes do decode. Não é leitor implementado ou V-020-B
executado. Seis problemas V-ADD pendentes antes do fecho,validação não aprovada.
Parte A concluída,B apenasDesign,C não iniciada;TASK-020 pai IN PROGRESS.
Sem Java/Implement/commits/push,config/protocolo ou gates externos adicionais.

## IMP-018 — Design aprovado; apenas revisão ADR

Registo 2026-11-25T16:00:22Z: [revisão sobre APIs reais](../../../memory-bank/bolts/025-video/adr-review.md)
para aprovação,sem impedimento de âmbito identificado. A05/A06 apresentam
precisões de falha inesperada ecleanup antes de Implement. B não implementada,
A concluída,C não iniciada;TASK-020 IN PROGRESS atéA/B/C. M01–M14/V-020-B
por executar;seis problemas V-ADD pendentes,sem validação aprovada.
Sem alteraçãoJava,config/protocolo,commits/push ou gates externos adicionais.

## IMP-018 — Implement pendente de revisão, 2026-11-25T16:28:55Z

[Leitor e walkthrough](../../../memory-bank/bolts/025-video/implementation-walkthrough.md) apenas nos dois caminhos autorizados.
ADR/A05/A06 aprovados.45 testes novos M01–M14 passam; regressão incremental
JDK21 com1582/0/0/0,23 XML. Negativo de compilação preservado; testes históricos
inalterados. Guardas/erro primário/cleanup mantidos,16MiB apenas raster.

Quatro vídeos como ponteiros LFS nesta checkout: sem materialização e sem
nova leitura integral dos22687 JPEGs pelo VideoSource. Fixtures independentes
nas dimensões aprovadas passam no decoder real; não substituem os conteúdos.
Seis problemas V-ADD ainda pendentes antes do fecho; demo não reposta.
ParteA concluída; B/Implement para revisão, C não iniciada; pai IN PROGRESS.
Sem Test, fecho, commits/push. V-020-B consolidado, manual humano aplicável,
regressão de aceitação JDK21, revisão final Denji e integração pendentes;
fecho do pai só apósA/B/C, sem gates externos adicionais. R17 preservado.

## IMP-018 — Implement aprovado, Test em curso (2026-11-25T16:50:39Z)

[Relatório](../../../memory-bank/bolts/025-video/ddd-03-test-report.md), clean verify JDK21 com1582/0/0/0,23 XML,
JAR novo e SHA-256 `f9a3cb9cad6166d13e244a772398179731952fd8eb8ff0efe7b16a6f67b9787f`.
M01–M14 locais consolidados; V-020-B integral pendente por falta dos quatro
binários completos (paths são LFS; objetos ausentes), sem falsos resultados
de hash/count/EOF/cleanup ou substituição por fixtures. Manual preparado,
ainda não executado por humano. Sem pedir aprovação Test nesta condição.

Seis problemas V-ADD resolvidos pelo patch de3 paths explicitamente autorizado;
validação0 erros, sem alterar o validador/restaurar demo. Código aprovado e
históricos/negativos preservados. ParteA concluída; B emTest não aprovado,
C não iniciada. Pai IN PROGRESS; só fecha apósA/B/C, manual aplicável, Denji
e integração; sem gates externos adicionais. Sem fecho/commits/push/IMP-019.

## Test aceite por condição expressa; revisão confirmada — 2026-11-25T17:06:50Z

[Relatório](../../../memory-bank/bolts/025-video/ddd-03-test-report.md):22687 frames reais, hashes aprovados, comparação de cada
Frame,500/5965/8880/7342, EOF/END e fecho único. Sem rebuild ou alteração Java.
Negativo pessoal preservado; posterior delegação de execução em Codex e
commit/fecho condicional ao resultado correto. Nova run automática saída0;
Test aceite por essa condição, sem afirmar manual humano concluído. Denji
reviu e aprovou a versão, confirmado pelo utilizador. Integrar localmente antes
de concluir025; sem push/IMP-019. TASK-020 apenas fecha apósA/B/C; R17 preservado.

## Parte B concluída — 2026-11-25T17:11:00Z

[Fecho](../../../memory-bank/bolts/025-video/closure-walkthrough.md):025-video complete, leitor e prova integral
22687 frames; Test aceite após delegação expressa em Codex, negativo humano
preservado sem afirmar manual pessoal concluído. Denji aprovado e integração
local maine896643. Sem rebuild/push/IMP-019. PartesA/B concluídas; parteC
pendente, TASK-020 continua IN PROGRESS e só fecha comA/B/C.

## Parte C/IMP-019 — Model para revisão (2026-11-28T14:54:27Z)

[026-video/Model](../../../memory-bank/bolts/026-video/ddd-01-domain-model.md) iniciado por seleção do utilizador.
Q-C-48/49 propostas: cadência/retoma/timeline e qualificação/ownership/fecho.
P01–P20/V-020-C não executados. Sem fontes/Java, Design/Implement, Maven,
commits/push. Três caminhos futuros do ADD, sem configurar/instalar media
ou antecipar reencaminhamento/playback. A/B concluídas, C apenasModel;
TASK-020 pai IN PROGRESS e só fecha apósA/B/C, revisão e integração.
Manual aplicável de019 não dispensado pela exceção de025. R17 preservado.

## Parte C/IMP-019 — Design para revisão (2026-11-28T15:18:50Z)

Model/Q-C-48/49 aprovados; [Design](../../../memory-bank/bolts/026-video/ddd-02-technical-design.md) apenas documental.
Cadência exata, procura, pausa/retoma, W2+coordenador1, fairness/filas finitas,
reservas físicas e prazo global. Qualificar antes do nó/routing publicar;
integração real por conferir emADR, sem presumir API ou ampliar âmbito.
P01–P20/V-020-C não executados. Sem fontes/Java/Implement/Maven, commits/push.
A/B concluídas, C apenasDesign, pai IN PROGRESS; fecho só apósA/B/C,
regressãoJDK21, manual humano aplicável, Denji e integração. Sem novos gates.

## Parte C/IMP-019 — revisão ADR com impedimento (2026-11-28T15:37:32Z)

Design aprovado; [A01–A08](../../../memory-bank/bolts/026-video/adr-review.md) conferidos nos fontes reais.
Faltam pontes públicas de owner/procura/revogação/disponibilidade. Q-C-50 propõe
8 caminhos, sem ampliar os3 do ADD até autorização. Qualificação antes do nó
cabe no App; UNWIRED recusado e prazo/cleanup honestos. Sem código alterado,
Maven/testes/aplicações/Implement/commits/push. P01–P20/V-020-C por executar;
ADR por aprovar, apenas Model/Design aceites. A/B concluídas, C pendente;
manual humano aplicável/regressão JDK21/Denji/integração mantidos, sem novos gates.

## Parte C/IMP-019 — Test aceite e bolt 026-video fechado (2026-11-28T19:55:07Z)

O utilizador aprovou explicitamente a validação/fecho de Test e do bolt. Em
OpenJDK 21.0.13.1/Maven 3.6.3, a seleção focal passou 5/5, a suíte pertinente
150/150 e a suíte Maven completa 1599/1599, sem falhas/erros/skips. JaCoCo 0.8.12:
bundle 7573/8252 (91,77%) e linhas executáveis novas 673/825 (81,58%), ambos >80%.
[Relatório](../../../memory-bank/bolts/026-video/ddd-03-test-report.md).

`026-video` complete e história 008 complete/implemented=true. Isto conclui o
bolt da parte C, **não** a TASK-020: A/B/C, revisão/integração e restantes
critérios do pai permanecem sujeitos ao fluxo próprio. Não se marca TASK-020
DONE, PHASE-05 concluída ou unidade 005-video completa.
