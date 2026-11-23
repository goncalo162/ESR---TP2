---
unit: "005-video"
bolt: "010-video"
stage: "implement"
status: "complete"
---
# IMP-016 — Checkpoint Implement

## Resumo

Codec UDP OTT1 v1 e fragmentação implementados nos dois ficheiros aprovados,
sem sockets ou emissão de vídeo. Plan e checkpoint de testes vermelhos aprovados;
Implement aprovado pelo utilizador; Test autorizado e em curso.

## Estrutura e trabalho realizado

- [x] StreamPacket.java: valor validado e imutável, cópias defensivas, codificação/descodificação de datagramas exatos e fragmentação limitada.
- [x] StreamPacketTest.java: U01–U08 com bytes/expectativas independentes, fronteiras, negativos, regiões e propriedade dos buffers.

As substituições limitam-se aos stubs: invariantes/cópias cumprem U06/U08;
encode/decode cumprem U01–U03/U06/U07; fragmentação cumpre U04/U05/U08.
Nenhum comportamento de componentes anteriores foi substituído. Os testes
iniciais foram preservados integralmente e dois testes adicionais cobrem nulos e
região máxima/exata versus buffer inteiro. Sem desvio do Plan nem dependências novas.

## Decisões e limites

Caps fixos64/1200/1136 e2MiB/1847; UDP v1 separado do TCP v2/19 tipos.
Região, metadados e tamanhos são validados antes de cópias dependentes dos dados.
Decode nunca aloca frame por comprimento declarado; encode usa valor validado.
Fragmentação retém no máximo2MiB de payload em1847 fragmentos. Diagnósticos locais
ProtocolException não emitem ERROR TCP. Arrays não escapam ao valor; lista imutável.
Sem filas, threads, sockets ou estado de rede novo. Sem medição de heap alegada.
[Revisão da ordem de validação](../../../experiments/construction/010-video/implement-behavior-01/review.md).

## Resultados reais

Primeiro incremento:76/0/0/0 após implementar os stubs. Versão final:78 casos U01–U08
passam na regressão completa JDK21: **1537/0/0/0,22 XML**, Maven test (não clean verify).
A primeira regressão restrita falhou por sockets bloqueados; log/XML preservados.
Repetição com sockets locais permitidos passou sem mudar código/testes/prazos.
Negativo anterior76/74/0/0 e todas as evidências anteriores preservados.
[V-019/U01–U08, logs, hashes, XML e diffs](../../../experiments/construction/010-video/implement-behavior-01/README.md).

## Preservação e pendências

234 ficheiros anteriores de código/recursos,380 ficheiros protegidos da demo e
artefactos e36 ficheiros de evidência anterior intactos por hash. Testes históricos,
fixtures, ADD/specs, POM, validador e Q-C global preservados. IDs apenas locais.
Main/HEAD b6de848, ramo imp-016-udp; target/ separado, sem staging/commits/push.
Implement aprovado. Clean verify/JAR final e consolidação técnica Test executados
posteriormente no test-walkthrough. Manual humano Francisco24 cumprido e conferido; Test aprovado e revisão final
Denji confirmada pelo utilizador. Integração local72c9f92 e fecho autorizado;
bolt/história complete,TASK-019 DONE,unidade005/PHASE-05 em progresso. Sem gates externos
adicionais. Não se alega vídeo, GUI/CORE ou aceitação final por estes testes.
