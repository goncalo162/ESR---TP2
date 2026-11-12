---
stage: test
bolt: "035-overlay-extra"
created: "2026-11-12T00:39:35Z"
status: "approved-by-Luis"
---
# IMP-035 / V-034 — Test para repetição humana

Implement aprovado explicitamente; entrada em Test registada em2026-11-12T00:28:31Z.
Base main22622c1d7eb0fdab3df684f2fac10263cff11736. Só Test autorizado.
**Test aprovado explicitamente por Luis; fecho do bolt ainda pendente.**
Execução humana selecionada20261112T102931Z-pr_nynth,66 CHECK_PASS/MANUAL_PASS,
passed=true/no_processes_pending=true. Execução humana anterior preservada.
Commit e integração local em main autorizados; revisão humana final da implementação
ainda não foi registada. Não atribuir revisão ao revisor apenas proposto.

## Regressão completa e novo JAR

Com JDK21 em Projeto:

```sh
mvn -B -ntp clean verify
```

Execução real2026-11-12T00:28:35.345275+00:00 até
2026-11-12T00:28:51.832399+00:00, saída0/BUILD SUCCESS.
**1113 testes, zero falhas, erros ou skips**,11 XML Surefire da própria invocação.
Java release21, Surefire3.5.4/JUnitPlatformProvider. Não somar os175/368
incrementais nem usar1065/981/149 históricos como resultado atual.

| Classe | Testes | Falhas/erros/skips |
|---|---:|---|
| CandidateCacheTest | 9 |0/0/0|
| NeighborManagerTest | 54 |0/0/0|
| OverlayRecoveryTest | 29 |0/0/0|
| NeighborManagerLifecycleTest | 34 |0/0/0|
| NeighborDiscoveryTest | 20 |0/0/0|
| OttApplicationTest | 48 |0/0/0|
| TransportTest | 38 |0/0/0|
| ConfigurationLoaderTest | 741 |0/0/0|
| DiscoveryCodecTest | 26 |0/0/0|
| MessageCodecTest | 94 |0/0/0|
| BootstrapServerTest | 20 |0/0/0|

[Log Maven](../../../experiments/construction/035-overlay-extra/test-clean-verify-01.log),
[resultado/comando/hashes](../../../experiments/construction/035-overlay-extra/test-clean-verify-01-result.json),
[resumo/XML/JAR/CLI](../../../experiments/construction/035-overlay-extra/test-build-summary.json).
11 XML e109 artefactos CLI preservados fora de target; não foi alegada cópia de
relatórios TXT. POM/configuração/fixtures/código do resultado permanecem iguais;
apenas documentação é atualizada depois da execução.

[JAR preservado](../../../experiments/construction/035-overlay-extra/test-verified-jar/ott-streaming-1.0-SNAPSHOT.jar), SHA-256:

```text
dea01ffa2c501b9b948594005e7e8e9ee70b387981dc5b3d83f9d7c331cc2526
```

É o JAR novo desta execução, não o JAR histórico de IMP-034.

## Manual V-034 do agente — mortes reais

[Repetir pessoalmente / alcance do auxiliar](../../../experiments/overlay-recovery/README.md).
[Resultado Codex](../../../experiments/construction/035-overlay-extra/test-manual-runs/20261112T003609Z-sj9_ljkn/result.json),
[log68 CHECK_PASS/MANUAL_PASS](../../../experiments/construction/035-overlay-extra/test-manual-runs/20261112T003609Z-sj9_ljkn/manual.log)
e [ações/PIDs/SIGKILL](../../../experiments/construction/035-overlay-extra/test-manual-runs/20261112T003609Z-sj9_ljkn/actions.json).
Python saiu0; no_processes_pending=true. Não é execução nem aprovação de Luis.

| Grafo | Resultado real |
|---|---|
| S–R1–C e S–R2–C | Bootstrap0 antes das mortes; R1-9 seguido de R2-9. S–C único, sobreviventes S/C com mesmos PIDs/instanceIds. Sobreviventes encerraram0. |
| S–R1–R2–C e S–R1–R3–C | Bootstrap0; R2-9 seguido de R3-9. R1–C único, S/R1/C originais preservados; sessões S–R1 idênticas nos dois extremos. Sobreviventes encerraram0. |

Cada JVM carrega o mesmo JAR/hash; o auxiliar usa só APIs públicas/defaults,
sem fake clock ou transporte. Os snapshots02 e05 provam a aprendizagem/retenção
antes das falhas e a seleção posterior associa sourceKey/geração/queryStarted/TTL
à prova já observada. Nenhum candidato é inserido pelo manual. Após primeira
morte manteve-se alternativa/sem nova aresta durante a janela registada≈1s.
Após segunda, aresta reparada estável no intervalo observado≈2s; um Up por
extremo e zeroDown do peer reparado. Não extrapolar esses intervalos para SLA.

Unicidade física confirmada pelas tuplas TCP estabelecidas/inodes/FDs de ambos
os PIDs, nas tabelas tcp/tcp6; S–R1 também conferida no segundo grafo. Os UUID
connectionId de extremos diferentes são locais e não têm de coincidir.
Não ficaram JVMs/readers do ensaio; portas de listener reutilizáveis após shutdown.

O auxiliar expõe cache/snapshots pela API, não é a main OttApplication.
A CLI real/TCP real também foi exercitada na regressão; estes factos são separados.
Loopback não substitui CORE, vídeo/routing ou falhas IP silenciosas. Não há
resultados FPS/latência/vídeo atribuídos.

### Tentativas incompletas preservadas

- `20261112T003341Z-sx6crb8t`: 29 CHECK_PASS; failed em AssertionError('09-repaired: exactly one physical TCP between S,C'); no_processes_pending=True.
- `20261112T003506Z-add17tte`: 30 CHECK_PASS; failed em AssertionError('09-repaired: exactly one owned physical TCP between S,C'); no_processes_pending=True.

O observador inicial procurava apenas IPv4/tcp; sockets dual stack Java estavam
em tcp6. Foram preservadas saídas ss/tabelas, logs/resultados failed, limpeza e
sources exatos dos runners. Corrigiu-se só o observador Python, acrescentando
TCP6/mapeamento IPv4; não se alterou produção ou enfraqueceram oráculos.
[Inventário das três tentativas](../../../experiments/construction/035-overlay-extra/test-manual-evidence-inventory.json).

## D01–D20 / V-034

[Mapa detalhado dos casos](implementation-walkthrough.md) e
[complemento de grau](../../../experiments/construction/035-overlay-extra/implement-degree-walkthrough.md).
Todos os testes do mapa entram agora no clean verify atual.

| Oráculos | Prova atual |
|---|---|
| D01 | Codec/v2/19 tipos/masks3/78 fixtures independentes, bytes/caps e UDP; MessageCodec/DiscoveryCodec e V-ADD. |
| D02/D05/D06 | Gatilho só zeroACTIVE, mesma ligação20/6, maior/menor iniciador, admissão cooperativa saudável; oráculos controlados e TCP real. |
| D03/D04 | Manual agora com JVM por nó, quatro SIGKILL reais/sequenciais e bootstrap parado; S–C e S–R1–C, sobreviventes originais e S–R1 preservada. |
| D07/D08 | Claim/proveniência/TTL original, cache128x4, vazio/expirado/conflito/self/candidato indisponível; endpoint/identidade/caps/HELLO5 negativo. Cache e manager com portas/clock controlados; manual comprova aprendizagem real. |
| D09/D10/D11/D12 | Barreira native dial/opened, eleição só pendente, ACTIVE preservada, duplicatas/instâncias/callbacks e ERROR5/20/6; flush/ACK controlados. |
| D13/D14 | Grau total<=min(C,63) no commit comum: oito ordens62→63, sem Up/Down indevido. C64 físico/writer preso, FIFO/quotas/tickets/limites e liberação apenas após término. |
| D15 | TTL antes de envio/commit, DNS/dial/handshake/10s/backoff/jitter, callbacks tardios, justiça; relógio/escrita controlados. |
| D16/D17 | D07e em sessão reparada preserva query/cache/sessão perante token/query/geração antigos e aceita resposta válida posterior. Anunciar aresta só após Up. |
| D18/D19 | CLI/recovery, bootstrap rejeita20, isolamento local; shutdown global/reservas físicas e timeout explícito quando worker não termina. |
| D20 | Clean verify1113/XML/JARhash, manual do agente com mortes reais e evidências/diff executados. Repetição humana, aprovação Test, revisão final e integração ainda pendentes. |

## Repetição humana concluída e Test aprovado

Na raiz:

```sh
python3 -B experiments/overlay-recovery/run-manual.py --operator Luis
```

Luis executou o manual em2026-11-12T10:29:31.533818+00:00–10:29:42.179256+00:00,
66 CHECK_PASS, MANUAL_PASS, passed=true e no_processes_pending=true.
[Resultado humano aprovado](../../../experiments/construction/035-overlay-extra/test-manual-runs/20261112T102931Z-pr_nynth/result.json)
e [log](../../../experiments/construction/035-overlay-extra/test-manual-runs/20261112T102931Z-pr_nynth/manual.log).
A [execução humana anterior](../../../experiments/construction/035-overlay-extra/test-manual-runs/20261112T004311Z-ilbihqij/result.json),
também passada com66 checks, permanece intacta. Não foi repetido o manual.
[Aprovação explícita](../../../experiments/construction/035-overlay-extra/test-approval.json).
As cinco etapas estão concluídas; o bolt mantém in-progress/sem DONE enquanto
falta revisão humana final da implementação. Integração local autorizada.

## Diff e preservação

[Conferência final/hashes](../../../experiments/construction/035-overlay-extra/test-validation.json),
[V-ADD](../../../experiments/construction/035-overlay-extra/test-v-add-01.log),
[git diff --check](../../../experiments/construction/035-overlay-extra/test-diff-check.log).
[Delta de Projeto nesta etapa](../../../experiments/construction/035-overlay-extra/test-project-vs-before-Test.diff):
+4/−4 só em README; nenhum fonte/teste/configuração/fixture ou POM alterado.
[Diff completo de Projeto contra main22622c1](../../../experiments/construction/035-overlay-extra/test-project-vs-head.diff):
+1108/−140 e16 binários, incluindo a migração de fixtures já aprovada em Implement.
[Documentação de Test](../../../experiments/construction/035-overlay-extra/test-docs-vs-entry.diff)
e [auxiliares do manual](../../../experiments/construction/035-overlay-extra/test-manual-tools.diff)
separados. As listas geradas em target ficam
[separadas](../../../experiments/construction/035-overlay-extra/test-generated-target.numstat).

1042 artefactos anteriores conservam os hashes originais. O roteiro README de
experiments/overlay-recovery foi atualizado para Test e o conteúdo anterior
fica [arquivado](../../../experiments/construction/035-overlay-extra/test-previous-manual-readme.md)
com o hash original. Logs/resultados/JAR históricos não foram reescritos.
A conferência V-ADD final02 valida o estado documental apresentado;78 fixtures,
sem CORE/GUI ou aprovação humana inferida.

## Aprovação Test

Registo 2026-11-12T10:35:23Z. Luis aprovou Test/1113 testes e a execução humana selecionada,
autorizando commits e integração local em main. [Resumo para revisão final](final-review-summary.md).
Sem repetição do manual ou início de outro IMP.
