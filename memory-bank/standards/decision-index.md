# Índice derivado de decisões

Fonte normativa: [ADD](../../add.md).

- [ADR-001 — Java 21 e Maven](../../add.md#adr-001) — Aceite.
- [ADR-002 — TCP de controlo e UDP de vídeo](../../add.md#adr-002) — Aceite.
- [ADR-003 — Bootstrap por grafo permitido — registo histórico](../../add.md#adr-003) — Substituída.
- [ADR-004 — Anúncios path vector e custo RTT](../../add.md#adr-004) — Aceite.
- [ADR-005 — Árvores sob procura, ACK, revisões e leases](../../add.md#adr-005) — Aceite.
- [ADR-006 — MJPEG independente, UDP próprio e Swing](../../add.md#adr-006) — Aceite.
- [ADR-007 — Propriedades UTF-8 e binário sem dependências runtime](../../add.md#adr-007) — Aceite.
- [ADR-008 — Testes incrementais e aceitação no CORE](../../add.md#adr-008) — Aceite.
- [ADR-009 — Fixtures independentes e perfil confirmado](../../add.md#adr-009) — Aceite.
- [ADR-010 — Carga estrita e configuração imutável](../../add.md#adr-010) — Aceite.
- [ADR-011 — Reutilizar os XML CORE existentes](../../add.md#adr-011) — Aceite.
- [ADR-012 — Codec separado de transporte e contexto](../../add.md#adr-012) — Aceite.
- [ADR-013 — Bootstrap só para localização inicial](../../add.md#adr-013) — Aceite.
- [ADR-014 — TCP assíncrono com recursos limitados](../../add.md#adr-014) — Aceite.
- [ADR-015 — Confirmação assíncrona de escrita da resposta](../../add.md#adr-015) — Aceite.
- [ADR-016 — Parâmetros centrais e overrides validados](../../add.md#adr-016) — Aceite.
- [ADR-017 — Um projeto com componentes e proprietário serial do estado](../../add.md#adr-017) — Aceite.
- [ADR-018 — Estado volátil e identidades por stream/execução](../../add.md#adr-018) — Aceite.
- [ADR-019 — Handshake, iniciador determinístico e reconexão direta](../../add.md#adr-019) — Aceite.
- [ADR-020 — Erros tipados, falhas locais e shutdown limitado](../../add.md#adr-020) — Aceite.
- [ADR-021 — Segurança de laboratório e validação de fronteiras](../../add.md#adr-021) — Aceite.
- [ADR-022 — Observabilidade local e experiências reproduzíveis](../../add.md#adr-022) — Aceite.
- [ADR-023 — Entrega reproduzível, artigo e integração Git](../../add.md#adr-023) — Aceite.
- [ADR-024 — ADD mestre e incrementos atómicos rastreáveis](../../add.md#adr-024) — Aceite.
- [ADR-025 — Sem recuperação automática de falha exclusiva UDP](../../add.md#adr-025) — Aceite.
- [ADR-026 — Qualidade sem ferramentas ou limiar percentual novos](../../add.md#adr-026) — Aceite.
- [ADR-035 — Reparação direta na ligação iniciada pelo isolado](../../add.md#adr-035) — Aceite; substituição parcial019/033/034 aprovada, G-EXTRA-ADR satisfeito para IMP-035. [Revisão](../bolts/035-overlay-extra/adr-review.md).

## Clarificações aprovadas do checkpoint — 03-11-2026

Fonte normativa: [ADD, secção 13](../../add.md#13-questões-em-aberto-e-changelog).

- CP-01: manter nodeId e REGISTER/REGISTER_OK na consulta inicial; esclarece ADR-013/018.
- CP-02: regresso e entrada tardia de nós configurados, com nova instância após reinício; esclarece ADR-013/018/019 e TASK-007/016.
- CP-03: alternativas no grafo configurado; novas vizinhanças fora do grafo adiadas como extra; esclarece âmbito e ADR-013/019.

As CP confirmam decisões existentes; conservam-se as 26 ADR. A aprovação do
utilizador é documental, sem prova de implementação ou revisão individual de código.

## Detalhe de Construction aprovado — IMP-002

[Q-C-03 — gramática decimal de alpha](../../docs/construction-duvidas.md#q-c-03--gramática-decimal-de-alpha)
regista a aprovação explícita do detalhe lexical de monitor.ewma.alpha no Design.
As ADR-007/010/016/024/026 são reutilizadas sem substituição ou ADR nova;
[revisão de conformidade](../bolts/019-overlay/adr-review.md) aprovada antes de código.
Implement e [Test aprovados](../bolts/019-overlay/ddd-03-test-report.md): 824 testes
completos e repetição pessoal por Luis/25 checks confirmados. [Fecho concluído](../bolts/019-overlay/closure-walkthrough.md)
em 2026-11-04T10:33:12Z, revisão final confirmada por Luis e versão revista integrada
em main. G-REVIEW-032 cumprido; sem ADR nova.

## Concretizações de Model aprovadas — IMP-003

[Q-C-04](../../docs/construction-duvidas.md#q-c-04--coerência-da-instância-e-resposta-única-na-consulta-inicial),
aprovação explícita registada em 2026-11-04T10:50:52Z: igualdade de senderInstance e
Endpoint.instanceId no REGISTER e uma decisão de resposta por conexão. Aplicam
ADR-013/014/015; ADR-003 permanece substituída. [Design para revisão](../bolts/002-overlay/ddd-02-technical-design.md)
explicita também a normalização lexical de hosts já documentada em docs/07,
sem DNS. Design aprovado explicitamente em 2026-11-04T11:25:40Z, incluindo requestClose
como início de shutdown sem espera e preservação de close público. Autorizada
apenas ADR: [revisão de conformidade preparada](../bolts/002-overlay/adr-review.md)
em 2026-11-04T11:29:33Z; sem inviabilidade/divergência material identificada nesta inspeção.
ADR-013/014/015/017/020 e transversais reutilizadas; sem ADR nova ou alteração
do ADD. Aguardar revisão antes de Implement/Test.

Revisão ADR aprovada em 2026-11-04T11:37:45Z; autorizado apenas Implement. [Walkthrough](../bolts/002-overlay/implementation-walkthrough.md)
preparado em 2026-11-04T12:04:53Z: bootstrap e transporte nos cinco ficheiros, 856 testes
incrementais aprovados, red real guardado e M01–M18 mapeados. Sem ADR nova,
wire/caps alterados ou novo âmbito. Parar para revisão antes de Test/fecho;
clean verify/manual e revisão final por Waheed/integração pendentes.

Implement aprovado em 2026-11-04T12:18:01Z; autorizada apenas Test, com [relatório](../bolts/002-overlay/ddd-03-test-report.md)
preparado em 2026-11-04T12:29:39Z: clean verify/JDK21 com 856 testes e manual Codex/34 checks,
ambos saída 0. Sem código/ADR/dependência nova; cinco hashes de código iguais à
versão aprovada e 155 ficheiros congelados. V-006/M01–M18 executados tecnicamente;
repetição pessoal/aprovação Test, revisão final por Waheed e integração pendentes.

Test do IMP-003 aprovado explicitamente, com registo em 2026-11-04T14:05:14Z, incluindo
856 testes e execução pessoal por Luis em 2026-11-04T13:58:36Z–13:58:38Z:
34 CHECK_PASS/MANUAL_PASS/JAVA_EXIT=0, mesmo JAR. [Fecho preparado](../bolts/002-overlay/closure-walkthrough.md),
commits e push de Denji autorizados. Nenhuma nova decisão arquitetural ou ADR;
revisão final atribuída a Waheed e integração pendentes antes de DONE.

Fecho do IMP-003 confirmado pelo utilizador — «fecha o bolt, a revisão esta feita» —
com registo em 2026-11-04T14:18:23Z. Revisão final atribuída a Waheed declarada concluída;
versão revista 03345e9e1ab56ec04302676cca6fce32d168223d já integrada em main antes do fecho.
Cascata specsmd executada em 2026-11-04T14:18:23Z: bolt/história complete, implemented=true,
TASK-006 DONE, unidade in-progress. Sem nova decisão arquitetural, código ou ADR.
[Prova e fecho](../bolts/002-overlay/closure-walkthrough.md).

## Model para revisão — IMP-004

[Modelo](../bolts/003-overlay/ddd-01-domain-model.md) preparado em 2026-11-10T14:11:43Z, após seleção
explícita de 003-overlay/Model, iniciado em 2026-11-10T14:01:42Z. Reutilização prevista de
ADR-012/013/018/019/021/024 e contratos transversais, sem revisão ADR antecipada,
decisão arquitetural nova ou alteração dos bytes v1. ADR-003 continua substituída.
Preservados os 155 ficheiros de Projeto/; V-007/M01–M22 planeados, sem execução.
Aguardar aprovação de Model antes de Design/ADR/Implement/Test; revisão final
atribuída a Denji e integração exigidas antes de DONE.

Revisão do Model do IMP-004, preparada em 2026-11-10T14:24:46Z: lacuna ERROR (17) corrigida,
com correlação por fase, entrega integral ao consumidor ACTIVE e sem ERROR de
resposta. As concretizações não determinadas pelas fontes estão em
[Q-C-05](../../docs/construction-duvidas.md#q-c-05--receção-de-error-no-imp-004), **pendentes**, sem nova ADR aceite.
M02/M07/M11 ampliados; nenhuma aprovação de Model, Design ou Java inferida.

Model revisto/Q-C-05 aprovados explicitamente em 2026-11-10T14:48:50Z; apenas Design
autorizado. [Design para revisão](../bolts/003-overlay/ddd-02-technical-design.md) preparado em 2026-11-10T15:00:14Z,
com limites e assinaturas concretos, sem alterar os bytes v1 ou as APIs existentes.
Não é revisão ADR concluída; nenhuma ADR nova aceite ou implementação inferida.
Q-C-05 resolvida, V-007 e restantes verificações funcionais pendentes.

## Q-C-06 aprovada — revisão ADR do IMP-004

[Q-C-06](../../docs/construction-duvidas.md#q-c-06--prazo-próprio-de-resolução-e-bind-no-imp-004) resolvida por aprovação explícita
em 2026-11-10T15:13:50Z, juntamente com Design. Prazo resolução/bind igual a
control.connectTimeout, antes da fila e separado do dial TCP; sem inferir
terminação física de cancelamento/future concluído. Q-C-05 preservada.
[Revisão ADR](../bolts/003-overlay/adr-review.md) preparada em 2026-11-10T15:20:09Z: reutilizar ADR existentes,
sem ADR-033 necessária identificada. ADR-003 permanece substituída. Aguardar
revisão antes de Implement/Test; nenhuma alteração Java ou prova funcional nova.

## ADR do IMP-004 aprovada — Implement para revisão

ADR explicitamente aprovada; Q-C-07 resolvida com reposição externa autorizada
de EventLoop/RealDeadlines. [Implement para checkpoint](../bolts/003-overlay/implementation-walkthrough.md)
preparado em 2026-11-10T16:12:36Z. Regressão incremental: `mvn -B -ntp test`,
903 testes (740 configuração +66 codec +31 TCP +19 bootstrap +47 vizinhança),
zero falhas/erros/ignorados. V-007 tem execução automatizada TCP local;
Q-C-05/Q-C-06 e D01–D22 preservados. Três ficheiros do IMP +1745/−0;
BootstrapServer externo +482/−142 separado, 151 ficheiros restantes preservados.
Implement ainda aguarda aprovação; Test/clean verify/JAR/manual não iniciados,
revisão final atribuída a Denji e integração pendentes. TASK/bolt/história/unidade
continuam em progresso, sem implemented=true, commits/push ou fecho.

## IMP-004 — Test preparado, aceitação pendente

Implement aprovado explicitamente pelo utilizador; [relatório Test](../bolts/003-overlay/ddd-03-test-report.md)
preparado em 2026-11-10T16:26:35Z. Clean verify atual: 903 testes (47 de vizinhança),
zero falhas/erros/ignorados. Manual interativo executado pelo agente Codex no
JAR verificado: 49 CHECK_PASS, MANUAL_PASS/CLEANUP_COMPLETE, saída Java 0.
XML/JAR/hash/perfis/log e instruções de repetição pessoal preservados em
experiments/construction/003-overlay/. Código aprovado e ferramentas intactos
durante Test. V-007/D01–D22 têm consolidação técnica local, com limites explícitos.
Q-C-08 continua aberta: V-ADD reprovado com três erros, sem alteração de ferramentas
ou redução do alcance. Test ainda não aprovado; revisão final humana e integração
pendentes. TASK/bolt/história/unidade permanecem em progresso, sem implemented=true,
commits/push/fecho ou GUI/CORE presumidos.

## IMP-004 — Test aprovado; fecho pendente

Test aprovado explicitamente pelo utilizador, registo em 2026-11-10T16:44:29Z.
903 testes do clean verify aprovados, manual humano de Francisco cumprido
(49 checks/Java 0) e V-ADD aprovado; Q-C-08 resolvida.
[Resumo/diff para revisão humana final](../bolts/003-overlay/final-review-summary.md).
As cinco etapas estão concluídas; bolt in-progress, current_stage=null,
completed=null. TASK/história/unidade continuam em progresso, sem implemented=true
ou DONE. Não há gates externos adicionais; faltam revisão humana final e
integração. Não executar o fecho nem fazer commits/push nesta autorização.

## Estado atual — IMP-004 fechado

Registo documental em 2026-11-10T17:12:15Z.
IMP-004/003-overlay/TASK-007 fechado documentalmente por autorização explícita
do utilizador. Integração local comprovada em main: `8db7b78`
(`8db7b78950e444f3c2917844617be4b9b236ec0f`), com 145 caminhos e hashes conferidos.
Test aprovado: clean verify/903 testes, manual humano Francisco/49 checks/Java 0
confirmado pelo utilizador, V-ADD aprovado; revisão final de Denji aprovada sem
alterações pendentes. Q-C-08 resolvida e limitada a patch/evidência na integração.
Bolt complete; história complete/implemented=true; TASK-007 DONE. A unidade
002-overlay mantém in-progress: 020-overlay e 004-overlay continuam planned,
sem início de outro IMP. Fecho documental sem alteração Java ou repetição do manual.
Sem push ou alegação de integração remota/entrega do projeto.

[Fecho documental](../bolts/003-overlay/closure-walkthrough.md). As referências anteriores a estados
pendentes são históricas; não descrevem o estado atual. Não se promovem
requisitos globais, PHASE-02, unidade ou intent a concluídos por este fecho.

## Extra posterior à base

- [ADR-033 — Descoberta extra por divulgação de vizinhos e cache limitada](../../add.md#adr-033) — Proposta. Aplicável ao EXTRA-01 / IMP-034 e IMP-035.

## ADR-033 — concretização de Design proposta

Registo de 2026-11-11T12:49:29Z. [Design IMP-034](../bolts/034-overlay-extra/ddd-02-technical-design.md)
concretiza isolamento OTD1, confiança, geração, TTL, caps e conflitos. ADR-033
permanece Proposta; Model aprovado não é aprovação do protocolo. ADD explicita
as exceções futuras face a ADR-019/CP-02/03, preservando o modo base.
G-EXTRA-ADR exige aprovação da ADR e dos contratos afetados antes de Implement.
O contrato adicional de reparação de IMP-035 exige revisão nesse incremento.

## ADR-033 — revisão para aprovação após Design

Registo de 2026-11-11T13:44:19Z. Design e Q-C-17 aprovados explicitamente; somente ADR
foi autorizada. [Revisão](../bolts/034-overlay-extra/adr-review.md) confere
isolamento, confiança, retenção e recursos. A proposta explicita a chave
extra face à lista de ADR-010/016, mantendo as regras base. As 25 ADR aceites
e ADR-003 Substituída conservam-se; ADR-033 permanece Proposta. Aprovação
formal da ADR e contratos afetados pendente no G-EXTRA-ADR; Implement/Test
não iniciados. Consultar ao trabalhar em config, descoberta, codec ou transporte.

## ADR-034 — transporte revisto, proposta

Registo de 2026-11-11T14:26:58Z. [ADR-034](../../add.md#adr-034), Proposta, substitui apenas
a escolha de transporte da ADR-033: descoberta negociada no TCP persistente
de controlo, framing/codec e FIFO comuns. ADR-033 conserva as políticas residuais
aceites, com transporte OTD1 suspenso pelo utilizador. Aprovação antiga não
fecha o gate novo. Consultar ao trabalhar em protocolo, handshake, filas e cache.
G-EXTRA-ADR reaberto em IMP-034; IMP-035 mantém aprovação própria de reparação.

### ADR-034 — refinamento de margem/conteúdo para revisão

Registo de 2026-11-11T14:51:37Z. A proposta exige margem normal de uma posição e quota extra
por fila de um; Q=1 não negocia descoberta. Distingue conteúdo extra inválido,
completo e associado à sessão, de framing/truncamento/identidade terminais.
Resultado tipado é validado no owner antes de rejeição atómica sem fecho.
ADR-034 continua Proposta e G-EXTRA-ADR pendente. Ver Design secções 3, 5 e 8.

### ADR-034 — capacidade obrigatória na execução normal

Registo de 2026-11-11T15:15:24Z. O utilizador rejeitou as propostas de fallback e Q=1 degradado.
A proposta atual exige Q>=2 com flag ativo antes de sockets e supported=required=1;
seleção sem bit não cria sessão base. Perfis/APIs base e Q=1 antigos mantêm-se.
[Design revisto](../bolts/034-overlay-extra/ddd-02-technical-design.md), ADR-034
Proposta, gate pendente. Não confundir defaults base com os perfis normais required.

### ADR-034 — requisito intrínseco em todos os nós overlay

Registo de 2026-11-11T15:35:59Z. Substitui também ativação opcional/default false da proposta
anterior, não apenas transporte. O papel overlay implica descoberta/Q>=2 por
todas as APIs/perfis; sem ACTIVE_BASE ou API desligada. Peer deve suportar e
exigir capacidade; bootstrap só inicial e formatos v1 usados preservados.
[Plano dos testes](../bolts/034-overlay-extra/test-contract-migration.md).
ADR-034 continua Proposta; aprovação antecede qualquer alteração Java.

## ADR-034 — clarificação de operação e isolamento

Registo de 2026-11-11T17:22:59Z. Proposta revista, não aceite: programa único/mesma versão
de JAR, descoberta obrigatória e inputs de handshake inválidos rejeitados só
na tentativa. Sem compatibilidade operacional com versões antigas nem FAILED
global/fecho da CLI/sessões saudáveis. ADR-020 preservada; estado/retry por peer
limitados, backoff existente. [Revisão](../bolts/034-overlay-extra/adr-034-review.md)
e Design para aprovação; G-EXTRA-ADR pendente.

## ADR-034 — TCP v2 uniforme (proposta revista)

Registo de 2026-11-11T17:47:08Z. Decisão explícita: TCP exclusivamente v2 em bootstrap,
handshake, mensagens normais e descoberta; um contrato/codec, 18 tipos explícitos,
código 3 rejeitado, sem runtime v1/fallback. Arquivo v1 e fixtures v2 independentes
preparados; migração dos testes justificada com garantias preservadas. Bootstrap
continua só inicial, UDP OTT1 inalterado, descoberta obrigatória e isolamento por
peer/CLI saudável mantidos; cache/TTL/proveniência/shutdown conservados.
IMP-034 aprende, IMP-035 repara depois. Design/ADR-034/âmbito para aprovação;
Implement suspenso, sem execução Java, commits, push ou outro IMP.

## Aprovação e retoma de Implement — TCP v2

Registo de 2026-11-11T17:55:50Z; instante do registo, não data atribuída à mensagem humana.
O utilizador aprovou explicitamente Design revisto, ADR-034, âmbito dos 29
caminhos e migração justificada dos testes/recursos para TCP exclusivamente v2.
ADR-034 Aceite: substitui o contrato TCP v1 e as propostas OTD1/mista/ativação
opcional afetadas; v1/fixtures ficam arquivados, UDP OTT1 permanece inalterado.
G-EXTRA-ADR satisfeito para IMP-034; formalizada revisão ADR e retoma apenas
Implement. Um contrato/codec v2 no bootstrap e vizinhos, descoberta obrigatória,
com framing/caps/identidade/unicidade/FIFO/flush/isolamento/shutdown e cache/TTL/
proveniência preservados. Testes incrementais com logs reais autorizados.
Parar para revisão de Implement antes de Test/fecho; V-033, regressão atual,
manual, revisão humana e integração continuam exigidos. IMP-035 mantém checkpoint
próprio; sem commits/push ou início de outro IMP.

## IMP-034 — Implement pronto para revisão

Registo de 2026-11-11T18:57:34Z. ADR-034 Aceite e G-EXTRA-ADR satisfeito apenas para IMP-034.
Controlo TCP exclusivamente v2 implementado no bootstrap, handshake, mensagens
normais e descoberta obrigatória; um codec/FIFO/writer, sem canal OTD1/runtime
v1 ou fallback. Cache/TTL/proveniência, isolamento e shutdown preservados.
Conjunto incremental JDK 21: 1062 testes, zero falhas/erros; inclui TCP local e
JVM da CLI. Corrigida a corrida descoberta/shutdown detetada por teste existente,
com novo oráculo controlado; falhas/sondagens anteriores mantidas.
66 fixtures operacionais (50 TCP v2 independentes e 16 UDP intactas); arquivo
v1 de 45 fixtures verificado separadamente. Ver walkthrough do bolt para logs,
XML, hashes, diff e mapa D01–D12; não confundir teste incremental com clean verify.
Pendente aprovação humana de Implement antes de Test. V-033 completo, clean
verify/regressão atual, manual reproduzido, revisão final e integração continuam
necessários para fecho. Sem commits/push ou outro IMP; IMP-035 continua planeado.

## IMP-034 — fecho integrado

2026-11-11T19:45:51Z: V-033 executado (1065 testes), manual humano Luis/53 checks/saída 0 e Test aceites; revisão técnica final do validador concluída. Versão f650be02f2af10b95328f5b50c3d6941e2e75c15 integrada em main local antes do fecho. TASK-033/bolt/história DONE/complete; Q-C-19 retira arquivos TCP v1, preservando testes operacionais v2 e UDP OTT1. [Fecho](../bolts/034-overlay-extra/closure-walkthrough.md). Sem push. IMP-035 mantém planned e gate próprio; REQ-025 e unidade/fase continuam em curso.

## IMP-035 — revisão ADR para aprovação

Registo de 2026-11-11T23:28:20Z. Design/Q-C-22/política cooperativa e21 caminhos
aprovados pelo utilizador. ADR-035 formalizada como Proposta no ADD/catálogo,
com delta parcial019/033/034 e garantias preservadas. Nenhuma ADR aceite anterior
foi apagada ou alterada de estado. Gate próprio pendente apenas de aprovação
formal desta revisão antes de Implement; fontes e JAR intactos, sem Java/Maven.

## IMP-035 — Implement para revisão antes de Test

Registo de 2026-11-12T00:04:01Z. Revisão ADR/ADR-035/substituição parcial019/033/034 aprovadas
explicitamente; ADR-035 Aceite e G-EXTRA-ADR satisfeito para IMP-035.
[Walkthrough, mapa D01–D20 e evidência incremental](../bolts/035-overlay-extra/implementation-walkthrough.md).
Reparação20/6 no mesmo TCP v2, candidatos aprendidos, menor originator só pendente,
ACTIVE preservada e quotas existentes. 78 fixtures operacionais, snapshots/UDP intactos.
Sem clean verify ou manual humano de mortes reais nesta etapa; V-034 completo,
regressão, revisão final e integração pendentes. Não marcar DONE nem iniciar Test.
Sem commits, push ou outro IMP; 005-rotas continua bloqueado.

## IMP-035 — Test aprovado; integração autorizada

Registo 2026-11-12T10:35:23Z. Luis aprovou Test/1113 e o manual humano20261112T102931Z-pr_nynth:
66 CHECK_PASS/MANUAL_PASS, passed=true/no_processes_pending=true. Execução
humana anterior preservada; nenhuma repetição. Cinco etapas concluídas.
[Resumo para revisão humana final](<../bolts/035-overlay-extra/final-review-summary.md>). Commit/main autorizados;
revisão humana final da implementação e fecho pendentes. Bolt/história/unidade
continuam in-progress, sem implemented=true/DONE ou início de005-rotas.
