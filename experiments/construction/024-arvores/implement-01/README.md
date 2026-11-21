# IMP-014 — Implement preparado para revisão

Q-C-47-B/revisão ADR aprovadas; registo2026-11-21T15:03:15Z. ADD, Design e
contratos registaram cinco caminhos antes de editar fontes. Apenas Implement
autorizado. [Walkthrough](../../../../memory-bank/bolts/024-arvores/implementation-walkthrough.md).

Regressão incremental final: **1449/0/0/0**,21 XML, JDK21.0.13.1 —1419 históricos
preservados+30 novos. `regression-02/` conserva XML e suites.json; os hashes dos
cinco fontes desse resultado estão em `regression-02-source.json`. Não é
aprovação de Implement/Test nem consolidação formal V-017. Sem clean verify,
JAR novo de entrega, fecho, commit/push ou outro IMP.

`authorization.json` e `approved-adr-input.tar.gz` preservam a autorização e os
22 artefactos/manifesto anteriores à transição. Os directórios model-01,
design-01 e adr-review-01 permanecem intactos; manifests antigos descrevem as
versões arquivadas. `source-before.json` e os dois java.input iniciais guardam
baseline; `verification.json` e `document-hashes.sha256` identificam o resultado.

Âmbito exacto: RoutingService.java, RouteMetric.java, OverlayNode.java,
OttApplication.java e novo RouteSwitchTest.java, paths completos no ADD e
authorization.json. Snapshot4/5/6 e factories/construtores anteriores preservados;
Event2 compatível e payload imutável. Console.log e writer limitados existentes,
sem thread/fila/logger/dependência nova, sem I/O de saída no owner. Logs só de
início/sucesso/falha; publicação antes de sucesso e poda.

Os logs focused-01–07, XML correspondentes disponíveis, duas regressões e o
negativo transfer-negative-01 ficam separados. O walkthrough distingue falhas
de invocação, sandbox e novos fixtures de uma falha funcional reproduzida.
Nenhuma asserção/prazo/histórico/fundação foi enfraquecida.

Negativo funcional novo: uma ronda transferida para recuperação deixava vivo
lossCredit; depois de A+3B e T, aparecia quinta ACTIVATE sem ANN novo. Código
anterior em before-transfer-fix.java.input; negativo1/1/0/0 e teste corrigido
preservados. Transferência ainda válida consome o incidente; perda após a troca
já expirar conserva crédito novo. Verificação pertinente4/0/0/0 e regressão
final1449/0/0/0. Não confundir este S13 com R17 histórico.

**R17 inicial de IMP-013 continua sem causa estabelecida.** Integração com
métricas controladas não demonstra a causa desse negativo. Evidências, manual,
JAR/hashes e negativos anteriores permanecem intactos.

Pendentes: revisão/aceitação de Implement; Test/V-017 consolidado, clean verify/
JAR, regressão aprovada, manual humano antes de aprovar Test, revisão final
Waheed e integração. Nenhum gate externo adicional foi introduzido.

As duas listas Maven rastreadas em target/ foram preservadas em generated-target/
e repostas no estado HEAD após a execução, sem alterar fontes ou XML/resultados.
