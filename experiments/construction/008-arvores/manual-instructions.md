# IMP-013 / V-016 — roteiro humano; Luis concluído

Manual Luis concluído e confirmado pessoalmente pelo utilizador; Test aprovado,
Fillmore confirmado e integração447ae4b comprovada. O roteiro original abaixo é
reproduzível e conserva a preparação histórica; o ensaio kind=agent permanece
separado e não é a prova humana. [Conferência Luis](manual-human-verification-01/verification.json).

## Versão congelada

[JAR do clean verify](test-01/ott-streaming-1.0-SNAPSHOT.jar), 504997 bytes.
SHA-256: `81779be388778fd7dfacba5957e74ccffe5da45e86a403ea1ec5626f52c8f49d`.
Clean verify JDK21.0.13.1:1419 testes/0falhas/0erros/0ignorados,20 XML.
[Resultado](test-01/clean-verify-result.json), [V-016 consolidado](test-01/v016-oracles.json)
e [manifesto do manual](test-manual-manifest.json).

## Um terminal — execução pessoal

Na máquina Linux atual, a partir da raiz do repositório:

```bash
sha256sum experiments/construction/008-arvores/test-01/ott-streaming-1.0-SNAPSHOT.jar

python3 -B experiments/construction/008-arvores/test-manual-tools/run-manual.py \
  --operator Luis \
  --kind human \
  --java-home /usr/lib/jvm/java-21-openjdk-amd64
ott_manual_exit=$?
printf 'WRAPPER_EXIT=%s\n' "$ott_manual_exit"
```

Usar o nome real do operador. Noutra máquina indicar o caminho do seu JDK21.
Não reconstruir o produto nem usar JAR/classes de target. O wrapper verifica
os hashes, compila somente o auxiliar numa pasta nova da execução e executa
com o JAR congelado primeiro no classpath. As fixtures aprovadas e as oito
bibliotecas de teste existentes foram congeladas fora de target; nenhuma nova
dependência de produção foi acrescentada. Não é necessário Maven para o manual.

Premir Enter em cada uma das sete etapas. Ler ESPERADO e OBSERVADO, conferir
os CHECK_PASS e guardar qualquer divergência. Não alimentar Enter automaticamente
numa execução humana. EOF/Ctrl+C registam interrupção; os try-with-resources e o
wrapper recolhem as rigs/serviços/JVM, sem tratar parcial como sucesso.

| Etapa | O que observar |
|---|---|
|1 — Perda/alternativa | Down(A) retira forwarding já; até T só1 ACTIVATE; B confirma apenas após ACK novo, com o mesmo token e resultado antigo imutável |
|2 — R10/watermark | G2/custo Long.MAX_VALUE-1000, PATH_CHANGED, T3s, métrica10→2000→10: continua UNAVAILABLE/1 ACTIVATE; duplicado não rearma; G3 dá nova revisão/request e permite confirmar |
|3 — Novidade legítima | Primeira medição válida funciona; anúncio anterior à falha continua bloqueado; anúncio posterior sem métrica mantém oportunidade legítima |
|4 — Época/intenção | Época nova exige ACK; ACK antigo ignorado; consumidor conservado até cancelamento; outra origem permanece intacta |
|5 — Físico/shutdown | Duas reservas bloqueiam terceira emissão; libertação não cria retry terminal; ANN novo admite; await(0) falso enquanto há callback, mesmo após continuação |
|6 — TCP/prefixo | Dois relays; mesmo ID de Line no filho, revisão/request novos e recuperação automática do prefixo; recursos e portas fechados ao regressar do teste |
|7 — TCP/reparação | Bootstrap fechado; falhas sequenciais dos relays; Session S–C e path[S,C] novos, sem retry humano; recursos/portas recolhidos |

## Âmbito e limites

Etapas1–5 usam relógio/owner/scheduler/port controlados. Etapas6–7 executam os
métodos TCP aprovados da RecoveryTest congelada, com sockets loopback reais,
handshake/NeighborManager/reparação reais e métricas fixas. O clock TCP é real.
Portas de controlo efémeras em127.0.0.1 são escolhidas pelas fixtures; endpoints
aparecem nas linhas TCP_PREFIX_RECOVERY/TCP_REPAIR_RECOVERY. As asserções verificam
rebind das portas de controlo após fecho. Os números dataPort anunciados não são
prova de sockets UDP, vídeo ou tráfego de dados. Não é CLI stop/play, GUI ou CORE.

**O negativo inicial R17 permanece sem causa estabelecida. A integração atual
com métricas controladas não demonstra a causa dessa falha.** Conservar qualquer
falha nova e apresentar reprodução/âmbito antes de propor correção de código.

## Resultado a devolver

Esperado:28 CHECK_PASS, CLEANUP_COMPLETE, MANUAL_PASS CHECKS=28, JAVA_EXIT=0,
PASSED=true e WRAPPER_EXIT=0. O wrapper imprime REPORT e cria uma pasta nova em
`test-manual-runs/` com entry.json, compile.log, manual.log, java-pid.json,
report.json e classes do auxiliar. Nunca sobrescreve execuções anteriores.

No report.json conferir frozen_hashes_match, code_source_matches_jar,
frozen_test_code_source_matches e java_process_terminated, todos true.
A declaração kind=human não comprova, por si só, identidade ou revisão humana.

Devolver o caminho do report.json e confirmar pessoalmente as sete etapas,
indicando divergências e a saída WRAPPER_EXIT. Aguardar essa repetição antes de
pedir aprovação de Test. Depois continuam necessárias revisão final Fillmore
e integração. Sem fecho, commits, push, outro IMP ou gates externos adicionais.

[Ensaio técnico Codex](test-01/manual-agent-verification.json):28 checks, JVM
terminada, kind=agent. Serve para verificar o roteiro, não a obrigação humana.
