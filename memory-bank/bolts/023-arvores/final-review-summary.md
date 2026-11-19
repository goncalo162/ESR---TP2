# IMP-012 / TASK-015 — resumo final para Denji

**Test e revisão final Denji aprovados, por confirmação explícita do utilizador.** Integração
não autorizada/executada. Bolt023-arvores, história/TASK-015, unidade004 e PHASE-04
mantêm-se em progresso, sem fecho. Base operacional main/b273b258a4642f94f8614ae00f060a46318d77a5.

## Problema e comportamento entregue

Subscrições confirmadas precisam de renovar enquanto há procura e de podar só
o ramo que deixou de a ter. Agora14 renova a mesma linhagem com revisão crescente
requestId novo e prazo conservador;13 só retira a revisão exata admitida.
Sair um consumidor preserva os restantes e o tronco comum; sair o último retira
a autorização e envia uma poda best effort. Órfãos expiram pelo lease finito.
Nenhuma entrega de vídeo é presumida a partir da autorização de forwarding.

Durante RENEW, o grant saudável anterior mantém-se até sucesso correlacionado ou
prazo original. ACK/ACCEPTED/ticket de operação antiga não reinstalam cancelados
nem removem substitutas. M07, quotas e reservas até callback+continuação são
preservados; P=min(renew,G/3) é constante por grant, sem bursts/remaining/3.
Shutdown mantém o owner e o budget global existentes.

Q-C-40–42 permanecem aprovadas: revisão crescente com mesmo subscriptionId,
recusa de prefixo divergente preservando grant e âmbito dos três ficheiros, sem
CLI stop/play. Não há nova dependência, executor/transporte, wire/cap ou ADR.
TCPv2/19 tipos/código3 rejeitado, framing/UTF-8 e UDP intactos.

## Três ficheiros Java para revisão

- `Projeto/src/main/java/pt/uminho/esr/ott/routing/Subscription.java`: valida cap
  antes de copiar path, mantendo record/Lease/State/API e cópias defensivas.
- `Projeto/src/main/java/pt/uminho/esr/ott/routing/RoutingService.java`: composição
  de renovação/poda no ActivationEngine/owner existentes, correlação/prazos,
  grant anterior e limites físicos partilhados.
- `Projeto/src/test/java/pt/uminho/esr/ott/routing/SubscriptionLeaseTest.java`:
  51 testes novos,24 grupos L01–L24 e composição TCP real.

[Justificação F01–F07 e de cada substituição](implementation-walkthrough.md).
[Diff Java](../../../experiments/construction/023-arvores/integration-01/java.diff)
e [numstat](../../../experiments/construction/023-arvores/integration-01/java.numstat):
RoutingService+141/−53,Subscription+23/−0,SubscriptionLeaseTest+574/−0.
19 testes históricos e230 outros ficheiros src anteriores permanecem intactos.
NeighborManager/send(), OverlayNode, protocolo/transporte/configuração/POM intactos.

## Versão e prova aceite em Test

[Relatório Test aprovado](ddd-03-test-report.md): clean verify **1379/0/0/0**,
**19 XML**,51 novos e6 M07 históricos; [V-015/L01–L24](../../../experiments/construction/023-arvores/test-01/v015-oracles.json).
TCP real S–R1–R2–(C1,C2): ambos ativos além do lease, saída parcial/final,
órfão com sessão viva, shutdown físico e rebind das seis TCP.

Manual humano Francisco: seis etapas/30 checks,sem divergências declaradas,
CLEANUP_COMPLETE,MANUAL_PASS CHECKS=30,JAVA_EXIT0,passed=true.
[Conferência de hashes/origem/JVM](../../../experiments/construction/023-arvores/manual-human-verification-01/verification.json).
WRAPPER_EXIT não fornecido,sem valor inventado. kind=human não foi usado por si
como prova de identidade; há confirmação pessoal explícita do utilizador.
Ensaio Codex anterior permanece separado como kind=agent.

JAR congelado:495568 bytes,SHA-256
`e2a41113b8dbb4d3d68024f546bb570e7509fdd47a2d09366c4b96ef907a1188`.
264 classes correspondem ao clean build; RoutingService foi carregado deste JAR.
[Hashes de fonte/JAR/XML](../../../experiments/construction/023-arvores/test-01/clean-verify-result.json).
Não se repetiu Java/Maven nesta preparação documental porque o código não mudou.

Negativos de tipos/vermelhos/replay/compilação auxiliar/sandbox/V-ADD e snapshots
anteriores preservados. Errata20→19 XML mantém os originais intactos. Sem alegar
vídeo/GUI/CORE, UDP medido ou metas experimentais7s/FPS.

## Pacote revisto e integração pendente

Denji realizou e aprovou a revisão final desta implementação sem alterações
pendentes, conforme confirmação explícita do utilizador. [Confirmação vinculada
à versão](../../../experiments/construction/023-arvores/integration-01/review-confirmation.json)
regista os três hashes de fonte e o JAR revistos. Não se contactou o revisor.
A data de execução da revisão não é inferida; recorded_at é o registo da confirmação.

[Allowlist exata atualizada](../../../experiments/construction/023-arvores/integration-01/allowlist.txt),
[manifesto](../../../experiments/construction/023-arvores/integration-01/manifest.json),
[diff acumulado](../../../experiments/construction/023-arvores/integration-01/cumulative.diff),
[numstat](../../../experiments/construction/023-arvores/integration-01/numstat.txt)
e [exclusões](../../../experiments/construction/023-arvores/integration-01/excluded-paths.json).
O pacote review-01 anterior mantém-se imutável como snapshot anterior à confirmação.

[Simulação da integração](../../../experiments/construction/023-arvores/integration-01/simulation.json)
parte do git archive da base e sobrepõe somente a allowlist, sem index/staging.
Confere bytes/paths, referências atuais, V-ADD e exclusão de alterações target/alheias.
Ficheiros target já versionados na base permanecem com bytes da base; nenhum
caminho target pertence à allowlist nem recebe o delta local de Maven.

Código/JAR/históricos/negativos e Q-C-40–42 preservados. Sem mudança de código,
sem repetir build/manual. [Plano final de integração](integration-plan.md) para
autorização; sem staging/commits/push/fecho/outro IMP. Falta integrar e comprovar
a versão revista, mantendo bolt/história/TASK/unidade/fase abertos até ao fecho.
Nenhum gate externo adicional.

## Fecho documental autorizado — estado atual

IMP-012/023-arvores/TASK-015 concluído após integração local4d18d1d comprovada,
Test1379/0/0/0,19 XML,V-ADD,Francisco30 e revisão final Denji.
[Fecho e evidência](closure-walkthrough.md): bolt/história complete,
implemented=true,TASK-015 DONE. Unidade004/PHASE-04 em progresso.
Texto anterior preserva as etapas/planos históricos; sem Java/push/novo IMP no fecho.
