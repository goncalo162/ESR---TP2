---
unit: "005-video"
bolt: "010-video"
stage: "test"
status: "complete"
---
# IMP-016 — Checkpoint Test: Test aprovado e revisão final confirmada; integração pendente

## Resultado

Implement aprovado; clean verify JDK21 executado em Projeto/: **1537 testes,
0 falhas,0 erros,0 ignorados,22 XML**. Inclui78 casos U01–U08/V-019; os1459 testes
históricos restantes também passaram nesta execução. Nenhuma alteração ao código
aprovado de produção/testes. JAR e XML preservados fora de target/.

[Log/inventário/JAR/hashes/preservação](../../../experiments/construction/010-video/test-01/README.md).
JAR SHA-256:588bd8e5f85c880666beab1a2033c8f100f3bd37e79d2d83ed4d15ec322ac3a2.
Classe StreamPacket no JAR coincide por hash com a saída clean verify; major65.
Origem efetiva confirmada no manual humano: StreamPacket e MessageCodec carregados
do JAR fixado, pelo CHECK_PASS1 do auxiliar preservado.

## V-019 / oráculos independentes

| Oráculo | Casos reais | Prova |
|---|---:|---|
| U01 |9|16 referências íntegras; quatro golden encode/decode com expectativas independentes |
| U02 |12|Rejeição dos negativos fixados, sem alterar bytes |
| U03 |4|Offsets/big-endian, bits UUID, extremos de contadores e UDP/TCP separados |
| U04 |11|Fronteiras1..2MiB,1847 fragmentos/último96 e recombinação independente |
| U05 |1|1137 bytes: dois golden datagramas independentes |
| U06 |26|Metadados hostis, invariantes/nulos, payload exato e validação antes da cópia |
| U07 |11|Região exata/offset, buffer maior, caps e overflow |
| U08 |4|Cópias defensivas de entrada/saída/receção e lista imutável |

Fixtures independentes preservadas, incluindo16 UDP OTT1 e62 TCP v2 operacionais.
Ordem de validação antes de copiar/alocar revista em Implement; os testes hostis
não são medições de heap. Recombinação lê payloads/offsets independentemente,
sem usar round-trip como único oráculo. Sem sockets de streaming/vídeo/GUI/CORE.

## Manual humano e limites de aceitação

[Manual reproduzível](../../../experiments/construction/010-video/manual-instructions.md):
executado pessoalmente por Francisco, oito etapas/24 checks, sem divergências
observadas conforme confirmação explícita do utilizador. [Conferência e hashes](../../../experiments/construction/010-video/manual-verification-01/README.md).
Relatório original em test-manual-runs/20261123T153332Z-e5356b3d/report.json.
PROBE_PASS/MANUAL_PASS,CLEANUP_COMPLETE,PASSED=true,JAVA_EXIT=0 e WRAPPER_EXIT=0
conferidos nos originais; SHELL_EXIT=0 confirmado apenas pelo utilizador.
JVM terminada após wait real do runner; pasta temporária ausente. JAR/código/
fixtures/auxiliares intactos. Sem repetir build ou manual, pois o código não mudou.
**Test aprovado pelo utilizador**; revisão final Denji confirmada pelo utilizador,
sem alterações pendentes. [Confirmação vinculada aos hashes](../../../experiments/construction/010-video/final-package-01/final-review.json).
Integração pendente.
Sem gates externos adicionais.

## Estado

Test aprovado, bolt/história/unidade em progresso até integração e fecho autorizados. Negativos/logs anteriores,
fixtures, testes históricos e demo preservados por hash; código aprovado intacto.
V-ADD executado nesta etapa, relatório na evidência. Ramo imp-016-udp,
IDs locais,main intacto,target/ fora da entrega. Sem staging/commits/push/fecho.
