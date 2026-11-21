# Experiência: implement-02-stale-metrics (024-arvores / IMP-014)

## Âmbito e Autorização
Autorização: Manter Implement pendente. Reproduzir em S05/S06 o tratamento de medições ultrapassadas com o rig de `RouteSwitchTest` (controlo sem replay e negativo com replay), validar proveniência antes de substituir custos/projeções no grafo e preservar tratamento legítimo de SUSPECT, invalidação, amostras novas e Session substituta. Executar testes pertinentes e regressão completa JDK21.

## Reprodução do Negativo
Sequência no rig:
1. `longGrant()`, ficando em D=4 s.
2. Ligar B, medir custo 10000 (count=1, sampledAt=4 s) e anunciar geração 1/custo 790.
3. Entregar três medições novas de B com custo 10, contagens 2/3/4.
4. Reentregar exatamente a medição antiga: custo 10000, contagem 1, instante 4 s.
5. Avançar 1 s até D=5 s.

- **Esperado:** B qualificado preserva seleção; emite segundo ACTIVATE para B.
- **Observado no negativo:** Seleção regressa a A e fica apenas o ACTIVATE inicial (`expected: <2> but was: <1>`).
- **Controlo sem repetição antiga (`s05StaleMeasurementControlWithoutReplayKeepsQualification`):** PASSOU (2 ACTIVATE).
- **Negativo com repetição (`s05SupersededMeasurementCannotReplaceCurrentCostOrQualification`):** FALHOU (`expected: <2> but was: <1>`), capturado em `negative-and-control-01.log` e XML.

## Causa Raiz
Em `RoutingService.onMetrics`: embora a amostra repetida não incrementasse a contagem de observações de qualidade (distinct=false), o custo antigo de 10000 substituía imediatamente o custo ativo de 10 na tabela de custos (`costs`) e no grafo (`root.setLinkCost(key(session), cost)`). Com o recálculo via `activation.kick()` e `drive()`, o upstream selecionado revertia de B para A, desqualificando B antes do instante de troca D.

## Resolução Implementada
Implementada validação estrita de proveniência antes de qualquer mutação de custos ou projeções:
- Método `supersededMeasurement(metrics, previous)` em `RoutingService`:
  - Rejeita amostras com `sampleCount` inferior ao registado.
  - Rejeita amostras sem `lastSampleAtNanos`.
  - Rejeita amostras com timestamp em recuo temporal (`elapsed < 0`).
  - Rejeita des-saturação inválida (`previous.saturated() && !metrics.sampleCountSaturated()`).
  - Rejeita repetição de métrica antiga após evento de invalidação (`previous.invalidated() && metrics.sampleCount() == previous.count() && elapsed == 0`).
- Casos legítimos preservados:
  - Estado SUSPECT corrente preserva custo e qualificação sem contar como nova observação (`s06CurrentSuspectStatePreservesCostAndQualificationWithoutAnObservation`).
  - Invalidação não ressuscita por repetição ultrapassada (`s06InvalidationFollowedBySupersededReplayDoesNotResurrectCost`).
  - Amostras novas e timestamps que avançam contam normalmente.
  - Saturação do contador (`Long.MAX_VALUE`) exige avanço estrito do instante temporal.
  - Session substituta descarta marcas anteriores e aceita amostras da nova sessão, rejeitando a antiga.

## Resultados
1. **Reprodução corrigida (`reproduction-fixed-01.log`):**
   - `RouteSwitchTest#s05StaleMeasurementControlWithoutReplayKeepsQualification` e `#s05SupersededMeasurementCannotReplaceCurrentCostOrQualification`: 2/2 passaram.
2. **Testes pertinentes (`targeted-01.log`):**
   - 63 testes executados (`RouteSwitchTest#s05*+s06*`, `RecoveryTest#r10*`, `RoutingServiceTest`): 63 passaram, 0 falhas, 0 erros.
3. **Regressão JDK21 completa (`regression-01.log`):**
   - 1459 testes executados: 1459 passaram, 0 falhas, 0 erros, 0 skips.
   - Build Success (41.4 s).

Implement permanece PENDENTE de revisão humana.
