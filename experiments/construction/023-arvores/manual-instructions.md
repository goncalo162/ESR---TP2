# IMP-012 / V-015 — manual humano reproduzível

**Execução humana pendente; Test por aprovar.** O ensaio técnico Codex está
identificado como kind=agent e não comprova execução/aceitação humana.

## Construção congelada

JAR: [ott-streaming-1.0-SNAPSHOT.jar](test-01/ott-streaming-1.0-SNAPSHOT.jar),
495568 bytes. SHA-256:
`e2a41113b8dbb4d3d68024f546bb570e7509fdd47a2d09366c4b96ef907a1188`.
Clean verify1379/0/0/0,JDK21.0.13+9/Maven3.9.9,19 XML reais.
[Manifesto](test-manual-manifest.json) fixa JAR, fontes aprovadas e auxiliares;
[proveniência](test-01/clean-verify-result.json) e classes do JAR conferidas.
Não usar um JAR de target nem recompilar o produto para executar este manual.

## Um terminal — comando completo

```bash
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2

shasum -a 256 experiments/construction/023-arvores/test-01/ott-streaming-1.0-SNAPSHOT.jar

python3 -B experiments/construction/023-arvores/test-manual-tools/run-manual.py \
  --operator Francisco \
  --kind human \
  --java-home /Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
ott_manual_exit=$?
printf 'WRAPPER_EXIT=%s\n' "$ott_manual_exit"
```

O caminho JDK21 foi conferido nesta máquina; noutra máquina indicar o seu JDK21.
Usar o nome efetivo do operador. O wrapper cria uma pasta pessoal nova em
`test-manual-runs/`, imprime o caminho e guarda `entry.json`, `compile.log`,
`manual.log`, `java-pid.json` e `report.json`, sem sobrescrever execuções.
Compila só o auxiliar numa pasta da execução, fora de target, usando o JAR fixo
no classpath. Verifica fontes/JAR/ferramentas antes/depois, origem RoutingService
no JAR e recolha da JVM. A declaração kind=human exige confirmação pessoal posterior.

Premir Enter em **cada uma das seis etapas**, conferir o texto ESPERADO, os
snapshots e CHECK_PASS. Não usar entrada automática para uma execução humana.
EOF/Ctrl+C ficam como interrupção, sem aprovação; try-with-resources fecha rigs,
serviços e sockets e o wrapper termina/recolhe a JVM se necessário.
Não há processos de aplicação separados nem comando quit neste roteiro.

| Etapa | Resultado a conferir |
|---|---|
| 1 — Renovação agregada | Uma ACTIVATE no tronco, C1/C2 ativos; três ciclos14, mesmo subscriptionId/revisão crescente/request novo, ambos ativos após prazo original |
| 2 — Saída parcial/final | C1 não reaparece após ACCEPTED tardio; C2 e lease exatos intactos; B retido até concluir; fonte mantém catálogo ao sair último |
| 3 — Poda/substituta |13 usa maior revisão14 submetida; forwarding retirado antes de acabar envio; ACK/callback/ticket antigos não removem nova subscrição |
| 4 — Órfão/grant curto | G−1 válido/G expirado sem rede; grant1ms, ciclo em333333ns, sem ACK expira e não há burst |
| 5 — Físico/shutdown | Dois envios ocupam2/Line e terceira geração aguarda; await(0)=false antes de callbacks; nenhum envio após stopping; sends/timers terminam |
| 6 — TCP real | S–R1–R2–(C1,C2), lease3000ms/renew200ms, ambos ativos por3300ms; retirada parcial/final, reativação e órfão com TCP vivo; seis portas TCP disponíveis após fecho |

## Perfis e portas

Etapas1–5: relógio, owner, scheduler e transporte controlados; sem sockets.
Etapa6: bootstrap mais cinco managers/monitores/routing reais, um owner por nó,
na mesma JVM, em127.0.0.1. Consumidores pela API SubscriptionTicket; não CLI
stop/play. São configurados fonte S, relays R1/R2 e consumidores C1/C2.
Lease3000ms, renew200ms, announce200ms e candidateTTL3000ms. Sem ficheiros de
perfil externos/media: catálogo é metadado, vídeo não é aberto/transmitido.

As **seis portas TCP de controlo são efémeras**, escolhidas localmente e registadas
em `TCP_CONTROL_PORTS=[bootstrap,S,R1,R2,C1,C2]`. `DATA_ENDPOINT_PORTS` são cinco
números anunciados, sem sockets UDP ou reivindicação de teste UDP. Não é preciso
configurar portas fixas. O teste verifica rebind das seis TCP após shutdown;
não transforma isso em prova GUI/CORE, vídeo, processos distribuídos ou desempenho.

## Resultado e retorno

Esperado: **30 CHECK_PASS**, `CLEANUP_COMPLETE`, `MANUAL_PASS CHECKS=30`,
`JAVA_EXIT=0`, `PASSED=true` e `WRAPPER_EXIT=0`.
No report.json conferir `frozen_hashes_match=true`, `code_source_matches_jar=true`
e `java_process_terminated=true`. Guardar também eventuais falhas/parciais;
não apagar negativos nem tratá-los como aprovação.

Depois envia o caminho do `report.json` e confirma pessoalmente as seis etapas,
indicando divergências. Não aprovar Test antes desse manual humano.
Depois continuam necessárias revisão final de Denji e integração, sem gates externos.

Ensaio técnico separado: [Codex30](test-manual-runs/20261118T174421Z-clqz57m8/report.json),kind=agent,
exit0, hashes/origem corretos e JVM terminada. Não é execução humana.
