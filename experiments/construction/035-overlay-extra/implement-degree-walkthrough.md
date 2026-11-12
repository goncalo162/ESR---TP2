# IMP-035 — correção da confirmação de grau / Implement para revisão

Requisito existente D13: ACTIVE total≤min(C,63), configurados e reparadores
em conjunto. Pedido explícito do utilizador: corrigir apenas Implement,
preservar sessões saudáveis/evidências e não avançar para Test/commits/push.

## Correção

activate(Context) revalida o grau no owner serial, depois de verificar o
contexto corrente e as barreiras de escrita/ACK, **antes** de mudar c.active,
retirar a tentativa, criar p.session ou chamar learner.up/publicar NeighborUp.
Se já há min(C,63) sessões, finish(RESOURCE_LIMIT) retira só o candidato pendente.
O mesmo ponto cobre HELLO5 e REPAIR_HELLO20, entrada e saída. Não se aumenta
qualquer cap e não se remove um ACTIVE para abrir espaço.

Como todas as confirmações são serializadas no mesmo owner, cada confirmação
vê a publicação anterior. O primeiro candidato pode ocupar a posição63; o
seguinte é recusado. Não há contador por finalidade nem reserva lógica capaz
de autorizar duas confirmações sobre a mesma posição. O fecho local mantém a
contabilidade de Transport até à terminação real, sem alteração no seu runtime.

## Provas determinísticas novas

| Teste | Ordens controladas e oráculo |
|---|---|
| OverlayRecoveryTest.D13_degreeConfirmationTwoRepairsCannotBothPassSixtyThree |62 configurados ACTIVE, C64, dois REPAIR_HELLO/ACK6 pendentes; ambas as ordens de flush. Após cada confirmação: grau63; excedente sem Up, diagnóstico RESOURCE_LIMIT; mesmos62 saudáveis, zeroDown. |
| OverlayRecoveryTest.D13_degreeConfiguredInboundAndRepairShareConfirmationLimit |62 ACTIVE e HELLO5 inbound/REPAIR_HELLO20 pendentes; ambas as ordens de flush; mesmo limite comum e preservação. |
| OverlayRecoveryTest.D13_degreeConfiguredOutboundAckAndFlushInterleaveWithRepair |HELLO5 outbound e20 inbound. Quatro ordens: ACK antes/depois do flush, ganhando configurado ou reparador; meia confirmação não publica Up; perdedor não troca as sessões. Barreira do dial associada antes das entradas. |
| TransportTest.D13_degreeLoserAtC64RetainsPhysicalSlotUntilBlockedWriterTerminates |64 sockets reais. Dois ACK6 com writers controlados; confirma um, fecha o excedente com writer preso. Contagem física64 e novo dial recusado; outros63 abertos. Só após libertar o writer passa a63 e o future falha. |

Os três testes de grau usam relógio/transporte/futures controlados, oito ordens
no total, sem sleeps para decidir o vencedor. O teste físico é uma prova
complementar do Transport real com64 slots, não uma simulação de todas as62
sessões do manager no mesmo teste. A composição usa Channel.close existente;
não se altera Connection/Transport nem se infere terminação de um future cancelado.

## Comandos e resultados preservados

JDK21; cada resultado contém comando, instantes reais, hashes de fontes/log e
cópia dos XML das classes efetivamente executadas.

- implement-degree-red-20:3 testes,3 falhas antes da correção. No código anterior,
  o candidato64 chegava a learner.up, cujo cap63 lançava exceção e provocava
  encerramento global/snapshot vazio. Não afirmar que foram publicados64 Up;
  a falha preservada comprova a perda indevida dos saudáveis por ausência da guarda.
- implement-degree-green-21:4 testes novos passaram, zero falhas/erros/skips.
- implement-degree-incremental-22:175 testes nas cinco classes afetadas passaram,
  zero falhas/erros/skips. OverlayRecoveryTest29, NeighborManagerTest54,
  NeighborManagerLifecycleTest34, NeighborDiscoveryTest20 e TransportTest38.
  Não é clean verify/regressão completa, não é execução manual.

```sh
# Dentro de Projeto, com JDK21; os testes de transporte precisam de sockets locais.
mvn -B -ntp '-Dtest=OverlayRecoveryTest#D13_degree*' test
mvn -B -ntp '-Dtest=OverlayRecoveryTest#D13_degree*,TransportTest#D13_degree*' test
mvn -B -ntp -Dtest=OverlayRecoveryTest,NeighborManagerTest,NeighborManagerLifecycleTest,NeighborDiscoveryTest,TransportTest test
```

[Entrada/hashes](implement-degree-entry.json),
[red](implement-degree-red-20-result.json), [green](implement-degree-green-21-result.json),
[incrementais175](implement-degree-incremental-22-result.json),
[log](implement-degree-incremental-22.log), [XML](implement-degree-incremental-22-xml/),
[V-ADD](implement-degree-v-add-01.log).

## Fronteira do checkpoint

Implement volta a revisão humana. ADR-035 Aceite/gate satisfeito permanecem;
a guarda concretiza o limite já aprovado, sem nova política/ADR/âmbito.
D13 suplementado; as provas anteriores ficam preservadas por hashes. Ainda
faltam V-034 completo, regressão completa, manual humano com mortes reais,
revisão final e integração. Sem Test/commits/push/outro IMP.

## Diff e conferência final

[Delta desta correção](implement-degree-project-vs-reviewed.diff),
[numstat](implement-degree-project-vs-reviewed.numstat):+98/−1 em4 caminhos
Projeto, incluindo README. Runtime apenas+5/−0 em NeighborManager.java;
restantes acréscimos são testes. Nenhum teste anterior foi removido/enfraquecido.
[Cumulativo contra main22622c1](implement-degree-project-vs-head.diff):
+1108/−140 e16 binários, incluindo fixtures anteriores do incremento.
[Documentação desta correção](implement-degree-docs-vs-reviewed.diff).

[Conferência/hashes/XML](implement-degree-validation.json) preserva1015 evidências
anteriores, JAR histórico e todos os fontes/fixtures fora dos quatro caminhos.
[Diff check](implement-degree-diff-check.log),
[listas geradas em target separadas](implement-degree-generated-target.numstat).
V-ADD01 passou com78 fixtures/zero erros;02 confere o estado documental final.
