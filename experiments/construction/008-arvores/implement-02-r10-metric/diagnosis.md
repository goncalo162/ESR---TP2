# R10 — recuperação da elegibilidade não é prova nova

## Reprodução sustentada

negative-01 reproduz exatamente Rig("R",false,4096,1), parent A, métrica10,
ANN geração2/custo Long.MAX_VALUE-1000, ACTIVATE seguido de PATH_CHANGED,
tick de3s, métrica2000 (soma não representável), métrica10 sem ANN novo.
Uma execução/um teste: falha expected1/observed2 no número de ACTIVATEs.
Antes da reposição da métrica, a asserção UNAVAILABLE e o único ACTIVATE passam.
Log/XML e os dois ficheiros de fonte da reprodução foram preservados.

## Causa e correção autorizada

RoutingTable mantém o candidato/geração/TTL e torna só a sua métrica ausente
quando Math.addExact deixa de representar a soma. eligibleProof passa a null.
reconcileRecovery guarda view=null; ao repor métrica10, previous==null atribuía
crédito incondicional, apesar de used/blocked ainda identificarem a geração2.
O erro é no rearme do RoutingService, não na fundação de cálculo da métrica.

A transição de elegibilidade agora confere o watermark: só é novidade se não
há prova bloqueada para esse ramo, se muda o ramo, ou se a geração validada é
estritamente superior à bloqueada. Uma prova estrutural diferente conserva a
regra anterior. O watermark capturado na falha já inclui os ANN observados
antes dela, inclusive durante o pedido; estes não podem ser reutilizados.
Primeira medição válida de prova ainda não usada e ANN novo posterior continuam
admissíveis. Teste adicional cobre ANN novo recebido sem métrica, seguido da
primeira observação elegível; o crédito não depende de nova intervenção humana.

Não alterar RoutingTable/monitor/codec/Route, T/A/cadência ou históricos.
Nenhum teste/asserção/prazo é removido. R17 inicial permanece sem causa
estabelecida; esta correção R10 não o diagnostica nem o resolve por inferência.
