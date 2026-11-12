---
stage: implement
bolt: "035-overlay-extra"
status: "ready-for-review"
---
# IMP-035 — Implement para revisão, antes de Test

**Atualização 2026-11-12T00:23:42Z: correção de grau concorrente concluída em Implement,
175 testes afetados aprovados.** [Complemento D13](../../../experiments/construction/035-overlay-extra/implement-degree-walkthrough.md).
As contagens/evidências anteriores abaixo são históricas desta etapa e permanecem
preservadas; não substituem a regressão completa ainda pendente.

ADR-035/revisão e substituição parcial019/033/034 aprovadas explicitamente;
registo2026-11-11T23:39:43Z. ADR-035 Aceite e G-EXTRA-ADR satisfeito **para IMP-035**.
Âmbito21 aprovado; Model/Design/ADR concluídos. Só Implement autorizado.
Não foi iniciado Test, feito clean verify, criado JAR novo, executado manual
SIGKILL, feito commit/push/integração ou iniciado outro IMP.

## Resultado e fronteira

OverlayRecovery seleciona candidatos aprendidos quando zeroACTIVE. Origem
fixa/revalida endpoint/proveniência/TTL e orçamento10s. REPAIR_HELLO20/ACK6
completa na mesma ligação, sem regra de menor ID para iniciar. Menor originator
só desempata saídas sobrepostas pendentes; sessão ACTIVE não é substituída.
O destino cooperativo valida handshake/limites sem provar a aprendizagem remota.
NeighborManager preserva barreira opened/future, UNCLASSIFIED/B/R, correlação
ERROR5/20/6, instanceId/connectionId e contextos perdedores separados.

Tick100ms partilhado com descoberta, sem pools/listeners/timers por candidato.
C/grau/dinâmicos16/admissões2/saída1 e quotas extra18/19 preservados. Handshakes
usam FIFO/sendAndFlush normal; Transport/Connection não foram alterados.
Cancelamento não liberta reserva física; teste com writer bloqueado confirma-o.
RecoveryStatus/CLI recovery limitados, RepairStarted uma vez por seleção com
record imutável de proveniência. Shutdown não regressa de STOPPING a ISOLATED.

Um codec operacional TCP v2 com19 tipos, máscaras3 e código3 rejeitado. Quatro
fixtures de máscara migradas e12 independentes acrescentadas;46TCP anteriores,
snapshots e16UDP intactos. Referência literal Python, sem encoder Java. Bytes
UDP OTT1, framing/caps, POM e recursos de configuração não foram alterados.
ControlMessage existente serve o payload20 sem exigir alteração desse caminho.

## Testes primeiro e execução incremental

Cada `*-result.json` conserva comando, JDK, instantes reais, hashes dos fontes,
log/hash e cópia dos XML executados. Nenhuma soma de execuções repetidas é
apresentada como total de regressão. Falhas/sandbox/compilação foram preservadas.

| Evidência | Resultado real | Leitura |
|---|---|---|
| implement-red-01 |2 testes,2 erros| Tipo20 ausente/capacidade3 rejeitada antes de implementar. |
| implement-protocol-green-02 |2 passaram| Codec mínimo verde. |
| implement-admission-red-03 |2 falhas| Falta de admissão; uma fixture ainda tinha dial configurado pendente. |
| implement-admission-red-04 |0 testes, compilação falhou| Acesso incorreto no teste novo, corrigido antes de usar o red como oráculo. |
| implement-admission-red-05 |2 falhas| Fixture corrigida, admissão20 realmente ausente. |
| implement-admission-green-06 |2 passaram| Admissão na mesma ligação. |
| implement-protocol-07 |120 passaram| Codec comum e descoberta. |
| implement-recovery-08 |15 passaram| Política/ordens controladas. |
| implement-cli-red-09 |1 falha| Comando recovery ausente. |
| implement-tcp-10 |228 passaram| Incrementais com TCP/CLI existentes. |
| implement-timer-red-11 |1 falha| Timer adicional indevido na fase DNS reparadora. |
| implement-timer-green-12 |1 falha,2 erros em18| Motivo TIMEOUT sobrescrito por BACKOFF e sockets bloqueados pela sandbox; não é sucesso. |
| implement-recovery-13 |18 passaram| Motivo corrigido e execução com sockets autorizados. |
| implement-incremental-14 |354 passaram| Saturação, dial tardio e reserva física. |
| implement-incremental-15 |358,1 falha| Callback Down alterava STOPPING após shutdown. |
| implement-incremental-16 |358 passaram| Correção de estado, D07e em sessão reparada, anúncio/CLI/shutdown. |
| implement-cache-17 |9 passaram| Cache existente sem alteração, em execução separada. |
| implement-claim-red-18 |1 falha| TTL podia expirar entre dial e envio antes do tick. |
| implement-incremental-19 |368 passaram| Dez classes, incluindo cache e guarda de TTL imediatamente antes do envio. |

[368 testes: resultado](../../../experiments/construction/035-overlay-extra/implement-incremental-19-result.json),
[log](../../../experiments/construction/035-overlay-extra/implement-incremental-19.log),
[XML](../../../experiments/construction/035-overlay-extra/implement-incremental-19-xml/).
[Cache: resultado](../../../experiments/construction/035-overlay-extra/implement-cache-17-result.json).
JDK21. As dez classes finais são OverlayRecoveryTest26, NeighborManagerTest54,
NeighborManagerLifecycleTest34, NeighborDiscoveryTest20, OttApplicationTest48,
TransportTest37, MessageCodecTest94, DiscoveryCodecTest26, BootstrapServerTest20
e CandidateCacheTest9. A execução17 da cache fica preservada separadamente. Não executar Test sem aprovação.

Reprodução incremental desde Projeto/, com JDK21 e sockets locais autorizados:

```sh
mvn -B -ntp -Dtest=OverlayRecoveryTest,NeighborManagerTest,NeighborManagerLifecycleTest,NeighborDiscoveryTest,TransportTest,BootstrapServerTest,OttApplicationTest,MessageCodecTest,DiscoveryCodecTest,CandidateCacheTest test
```

## Correspondência D01–D20

| Oráculo | Prova incremental / limite |
|---|---|
| D01 | MessageCodecTest94 compara bytes/modelos às fixtures independentes; DiscoveryCodecTest26; V-ADD. |
| D02 | D02_onlyIsolationTriggersAndOneOperationIsLogical e D03/D04: alternativa antes da segunda perda. |
| D03 | D03_realTcpLearnsBeforeFailureAndFormsSCOnSameSurvivors; close de nós embutidos. SIGKILL/manual pendentes. |
| D04 | D04_realTcpFormsR1CAndPreservesHealthySR1; mesma sessão S–R1. Mortes reais pendentes. |
| D05 | Três testes D05: isolado maior, destino saudável, mesma conexão, sem retorno/cache recíproca. |
| D06 | D03/D04 TCP real incluem C menor iniciando para S/R1 e confirmação dos dois extremos. |
| D07 | Política empty/expired/conflict/self, claim original fixo e D07_selectionRecordsPinnedProvenanceOnce. |
| D08 | D08_badEndpointIdentityCapabilitiesAndConfiguredHelloDoNotAdmit; codec/todos negativos anteriores preservados. |
| D09 | Duas ordens de saídas/flush; opened antes do resultado retido; perdedor tardio não remove vencedor. |
| D10 | D10_activeGreaterInitiatorIsNotReplacedBySmallerLateAttemptOrNewInstance; slot inbound bloqueia segunda saída pelo testeD11. |
| D11 | D11_sameOriginDuplicatePendingDoesNotReplaceCurrentRequest e D09callbacks; não trocar request/contexto. |
| D12 | D08/D10, ERROR relatedType5 não reconhecido como rejeição20 e correlação anterior de NeighborManagerTest. |
| D13 | Dois testes pendentes2/dinâmicos16 e tick único; cache9, grau63/B/R anteriores e reserva física de TransportTest. |
| D14 | TransportTest.D14_repairHandshakeConfirmationAndLosingPhysicalReservationUseNormalFifo e testes FIFO/quotas anteriores; ACK pendente em D11/D17. |
| D15 | TTL original não renovado,10s/backoff/justiça, DNS timeout, native dial tardio e claim expirado antes do envio sem novo HELLO; callbacks antigos ignorados. |
| D16 | D16_staleDiscoveryOnRepairedSessionPreservesQueryCacheAndConnection: tokens18/19, rejeitados tipados, query/geração velhas; resposta válida posterior mesma consulta/connectionId, zeroDown. D07e anteriores intactos. |
| D17 | D17_onlyConfirmedRepairedAdjacencyIsAdvertised: antes de flush sem aresta, depois Up anúncio só de ACTIVE. |
| D18 | Bootstrap recusa20; ERROR20 correlacionado; CLI recovery em JVM com peer rejeitado/sessão saudável. |
| D19 | Política encerra admissão, lifecycle de sessão reparada e worker físico preso/timeout explícito; suite anterior de shutdown preservada. |
| D20 | TCP real incremental/JDK21/XML/hashes executados. Clean verify completo, manual humano SIGKILL, revisão final/integração pendentes. |

D01–D20 estão mapeados; isso **não conclui V-034**. D03/D04/D20 têm provas
operacionais deliberadamente pendentes na etapa Test.

## Justificação das remoções/substituições nas fundações

Nenhum método de teste anterior foi apagado. Migrações abaixo correspondem ao
contrato obrigatório aprovado, sem alterar testes para esconder falhas:

- MessageType acrescenta20 à enum explícita; MessageCodec partilha os ramos de
  Hello e troca masks1 por3, mantendo offsets/envelope/framing/caps. Não criar
  segundo decoder. Os testes positivos mudam1→3; apenasDISCOVERY/zero continuam
  negativos. Loops de tipos passam a19, bootstrap recusa mais um tipo. Quatro
  fixtures alteram só a expectativa/campos de capacidade autorizados.
- NeighborManager remove hardcodes de tipo5 na correlação/retém o tipo original
  por Context, partilha construção Hello5/20, evita retry configurado para
  dinâmicos e mantém reservas dos native dials. Acrescenta seleção/admissão sem
  substituir o transporte. Casas de terminal/ativação/shutdown passam também
  pelo controlador reparador; callbacks vencidos não tocam no escolhido.
- NeighborDiscovery só compõe o tick para Recovery. Dois e2e de aplicação que
  afirmavam não existir S–C/R1–C deixam de poder exigir ausência **depois** de
  ambas as perdas. Passam a conferir retirada dos relays/aprendizagem e sessão
  saudável; D03/D04 novos exigem a aresta nova. Os oráculos de aprendizagem
  isolada sem dial permanecem. Não alterar cache, TTL ou regrasD07e.
- OttApplication acrescenta recovery/help/delegação; testes CLI verificam o
  comando real. Mask migrations em Manager/Lifecycle/App preservam os casos.
- Validador passa50→62TCP/tipos19, confere referência anterior protegida, os46
  vetores não migrados e16UDP. README de fixtures descreve25 aceites pelo codec,
  32 rejeições terminais e5 de conteúdo; duplicata20 wireválida é rejeitada no owner.

## Pendências antes do fecho

Revisão humana de Implement antes de autorizar Test. Na etapa seguinte:
clean verify completo JDK21/TCP, contagens/XML atuais, JAR novo/hash e manual
com mortes reais nos dois grafos, executado e repetido pelo humano antes de
aprovar Test. [Roteiro preparado](../../../experiments/overlay-recovery/README.md).
Depois revisão final/integração. Sem vídeo/routing/CORE/GUI/FPS comprovados.
O JAR preservado do IMP-034 não é uma build desta implementação.

## Diff e preservação

Base main22622c1d7eb0fdab3df684f2fac10263cff11736. O diff de Projeto inclui
ficheiros novos e fixtures binárias; exclui todo target. [Numstat](../../../experiments/construction/035-overlay-extra/implement-project-vs-head.numstat)
e [patch completo](../../../experiments/construction/035-overlay-extra/implement-project-vs-head.diff).
[Documentação preexistente](../../../experiments/construction/035-overlay-extra/implement-docs-preexisting.diff)
separada do [delta desta etapa](../../../experiments/construction/035-overlay-extra/implement-docs-vs-entry.diff).
[Conferência final/hashes](../../../experiments/construction/035-overlay-extra/implement-validation.json),
[V-ADD](../../../experiments/construction/035-overlay-extra/implement-v-add-03.log),
[git diff --check](../../../experiments/construction/035-overlay-extra/implement-diff-check.log).

181 dos206 ficheiros originais de Projeto preservados;26 novos. Os25 originais
alterados estão nos21 caminhos aprovados. As662 evidências anteriores à entrada
mantêm os hashes, incluindo o JAR histórico cff5db105bd1f3eca73aebc101724fccbdbd66645056ef6e3278018592bf8d48.
109 artefactos CLI gerados na execução19 foram copiados fora de target com
[inventário](../../../experiments/construction/035-overlay-extra/implement-incremental-19-cli-inventory.json).
Listas Maven geradas em target ficam [separadas](../../../experiments/construction/035-overlay-extra/implement-generated-target.numstat).
V-ADD final: zero erros,78 fixtures; não é execução CORE nem regressão Java.

A execução V-ADD02 foi preservada com uma referência ao próprio relatório ainda
inexistente no início da chamada. V-ADD03 passou após o ficheiro existir; a
conferência04 volta a validar o estado documental final, sem apagar02.

## Correção de grau concorrente — novo checkpoint Implement

activate() aplica ACTIVE≤min(C,63) antes de qualquer mutação, para5/20 e ambos
os sentidos. Recusa só o excedente com RESOURCE_LIMIT, sem Up/Down dos saudáveis.
Três testes determinísticos/oito ordens e um teste físico com64 sockets passaram;
175 incrementais afetados sem falhas. [Walkthrough suplementar](../../../experiments/construction/035-overlay-extra/implement-degree-walkthrough.md).
Sem Test/fecho/commits/push; restantes critérios mantêm-se pendentes.

[Diff da correção](../../../experiments/construction/035-overlay-extra/implement-degree-project-vs-reviewed.diff),
[numstat](../../../experiments/construction/035-overlay-extra/implement-degree-project-vs-reviewed.numstat)
e [conferência de preservação atual](../../../experiments/construction/035-overlay-extra/implement-degree-validation.json):
+98/−1 em Projeto, runtime+5/−0,1015 evidências anteriores preservadas.
