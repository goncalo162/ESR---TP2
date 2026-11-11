---
stage: test
bolt: "034-overlay-extra"
created: "2026-11-11T19:31:01Z"
status: "approved-by-Luis-closure-request"
updated: "2026-11-11T19:40:54Z"
---
# IMP-034 / V-033 — Test para repetição humana e revisão

Implement aprovado explicitamente pelo utilizador; entrada em Test registada
em 2026-11-11T19:22:10Z. Este relatório não aprova Test nem fecha o bolt.
Main/base integrada: 17c1ca27673930bfbf82078522059a243d96ccdb; ramo Denji.
Sem commits/push ou início de IMP-035.

## Regressão completa atual

Com JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64, em Projeto/:

```sh
mvn -B -ntp clean verify
```

Execução real de 2026-11-11T19:22:14.950327+00:00 a
2026-11-11T19:22:31.842099+00:00, saída 0 e BUILD SUCCESS.
**1065 testes, zero falhas, erros ou skips**, dez XML Surefire da própria
invocação. JDK 21, compilação release 21, JUnitPlatformProvider 3.5.4.
São resultados atuais; os 1062/142 incrementais permanecem separados e históricos.

| Classe | Testes | Falhas/erros/skips |
|---|---:|---|
| CandidateCacheTest | 9 | 0/0/0 |
| NeighborManagerTest | 54 | 0/0/0 |
| NeighborManagerLifecycleTest | 33 | 0/0/0 |
| NeighborDiscoveryTest | 20 | 0/0/0 |
| OttApplicationTest | 47 | 0/0/0 |
| TransportTest | 36 | 0/0/0 |
| ConfigurationLoaderTest | 741 | 0/0/0 |
| DiscoveryCodecTest | 26 | 0/0/0 |
| MessageCodecTest | 79 | 0/0/0 |
| BootstrapServerTest | 20 | 0/0/0 |

[Log Maven](../../../experiments/construction/034-overlay-extra/test-clean-verify-01.log),
[comando, hashes e lista exata dos XML](../../../experiments/construction/034-overlay-extra/test-clean-verify-01-result.json),
[resumo correto da execução e hashes dos XML/JAR/CLI](../../../experiments/construction/034-overlay-extra/test-build-summary.json).
Os XML e TXT foram preservados fora de target/, assim como os 109 artefactos
CLI gerados por esta execução. A flag not_a_clean_verify_or_manual do runner
incremental genérico é constante e incorreta nesta invocação; o comando/log e
o resumo classificam-na corretamente como clean verify completo. O resultado
bruto foi preservado, sem o reescrever. POM e dependências mantidos.

JAR construído/preservado em test-verified-jar/ott-streaming-1.0-SNAPSHOT.jar:

```text
cff5db105bd1f3eca73aebc101724fccbdbd66645056ef6e3278018592bf8d48
```

Este é o JAR TCP v2 desta execução, diferente do JAR histórico. Os fontes,
testes, configuração e fixtures não mudaram durante/depois da execução;
apenas README de Projeto foi atualizado posteriormente para indicar Test.

## V-033 / D01–D12

[Mapa completo de implementação e oráculos](implementation-walkthrough.md).
Os casos correspondentes foram incluídos na regressão atual:

| Oráculos | Evidência atual e alcance |
|---|---|
| D01/a–e | Configuração obrigatória Q>=2/false agregado/preflight, APIs preservadas, 18 tipos/versões/código 3; fixtures independentes comparadas pelo codec Java. |
| D02/a–d | Capacidade/identidade/endpoint/versão, tentativa rejeitada localmente, diagnóstico/retry por peer, saudável ACTIVE/CLI/aprendizagem preservados. |
| D03–D04 | Ambos os grafos aprovados aprendem com bootstrap parado; não criam arestas novas. TCP real na regressão e manual API. |
| D05–D06 | TTL desde QUERY, morte do anunciante, fonte/geração/sessão, retenção/expiração/conflitos/caps. Relógio controlado para fronteiras; manual observa TTL real. |
| D07/a–e | Conteúdo recuperável versus framing/identidade terminais; token antigo em mensagens/rejeições e geração antiga preservam query/sessão/cache/TTL; resposta válida posterior aceite sem Down. |
| D08/a–f | FIFO comum, margem Q2, quota/tickets físicos, cadência/operações limitadas, sem hearsay. Fakes de clock/escrita e transporte real onde exigido. |
| D09–D10 | Ausência de reparação, coalescência, callbacks antigos, shutdown global e reservas físicas; sem recursos artificiais ilimitados. |
| D11/a–b | TCP/CLI reais, bootstrap v2 flush/EOF, grafos/reconexão e isolamento de peer incompatível. Manual público API e JVM CLI. |
| D12 | Regressão completa/fixtures/V-ADD/diffs/hashes e manual do agente executados. Repetição humana, aprovação Test, revisão final e integração pendentes. |

## Manual do agente e repetição humana

[Roteiro/comando e alcance](../../../experiments/overlay-discovery/README.md).
Manual autónomo contra o JAR, sem JUnit nem alterações ao runtime.
Execução Codex de 2026-11-11T19:28:21.125978+00:00 a
2026-11-11T19:29:24.035030+00:00: **53 CHECK_PASS, MANUAL_PASS, JAVA_EXIT=0**.
[Log](../../../experiments/construction/034-overlay-extra/test-manual-runs/20261111T192821Z-o453w0bk/manual.log)
e [resultado/hashes](../../../experiments/construction/034-overlay-extra/test-manual-runs/20261111T192821Z-o453w0bk/result.json).

Confirma offline Q1/false, codec v2/negativos, peer incompatível sem queda de
saudável, CLI neighbors/quit/EOF/help em JVM separada, ambos os grafos,
bootstrap parado, proveniência, retenção após encerramento dos anunciantes,
TTL real de 60 s, ausência de reparação e shutdown/portas dos listeners.
Nós API dos grafos embutidos numa JVM; perda por close/EOF. Não é kill -9,
morte física, CORE, GUI, routing/streaming, vídeo/FPS ou reparação IMP-035.
A consulta normal do bootstrap não foi capturada em PCAP neste roteiro;
fecho real do bootstrap e continuidade são observados pelas APIs/TCP.

Tentativas anteriores do auxiliar, todas preservadas e sem MANUAL_PASS:

- 20261111T192613Z-nr7eem_9: compilação do auxiliar falhou por leitura Optional;
  zero checks. Não foi falha do código Java do projeto.
- 20261111T192635Z-yrtieyw_: 12 checks; o encoder recusou corretamente o modelo
  HELLO inválido antes da rede. Auxiliar passou a alterar só o byte da máscara
  de um HELLO v2 válido para a entrada negativa de protocolo.
- 20261111T192757Z-glsj42qv: 17 checks; o cenário CLI com grau zero foi recusado
  corretamente pelo bootstrap. Roteiro corrigido para o grafo válido C–R.

**Luis executou este manual nesta etapa:** 20261111T193418Z-xj_bfaej, 53 checks e saída 0. Comando para nova repetição:

```sh
python3 -B experiments/overlay-discovery/run-manual.py --operator Luis
```

Cria um novo diretório, identifica operador e guarda comando/log/result.json
sem sobrescrever os anteriores. Aguardar o TTL real e conferir passed=true,
53 CHECK_PASS, um MANUAL_PASS, java_exit=0 e hashes iguais. Rever também os
snapshots learned/retained/expired e logs CLI. Não inferir aprovação pela saída.

## Conferências e estado final

[V-ADD atual](../../../experiments/construction/034-overlay-extra/test-v-add-01.log),
[conferência final](../../../experiments/construction/034-overlay-extra/test-validation.json),
[diff de Projeto contra HEAD](../../../experiments/construction/034-overlay-extra/test-project-vs-head.diff)
e [numstat](../../../experiments/construction/034-overlay-extra/test-project-vs-head.numstat).
Documentação deste checkpoint separada das alterações aprovadas de Implement;
recursos gerados de target/ excluídos dos patches fonte.

Test fica em revisão, **sem conclusão de Test/DONE**. Pendentes: repetição
pessoal de Luis, aprovação de Test, critérios finais, revisão humana final e
integração. Não se atribui revisão/operador/datas a terceiros. G-EXTRA-ADR
permanece satisfeito só para IMP-034; IMP-035 mantém checkpoint próprio.

## Aceitação humana e revisão final

Luis executou em 2026-11-11T19:34:18Z–19:35:20Z: 53 CHECK_PASS, MANUAL_PASS, java_exit=0, passed=true e mesmo JAR. [Resultado pessoal](../../../experiments/construction/034-overlay-extra/test-manual-runs/20261111T193418Z-xj_bfaej/result.json). A mensagem seguinte pediu revisão final, integração e fecho; regista-se essa aceitação de Test, sem atribuir revisão a terceiros. [Revisão final](closure-walkthrough.md). A limpeza Q-C-19 não altera código Java nem bytes/expectativas dos vetores; os resultados originais não são reescritos.
