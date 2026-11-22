---
unit: "004-arvores"
bolt: "024-arvores"
stage: "test"
status: "in-progress"
created: "2026-11-22T17:40:00Z"
---
# IMP-014 — Test executado com JAR novo / V-017 consolidada tecnicamente

Etapa Test do bolt 024-arvores (TASK-017, V-017) executada em 2026-11-22 com o
JAR novo do clean verify. **Manual humano e aprovação de Test continuam
pendentes**; este relatório consolida a prova técnica.

## 1. Clean verify e JAR novo

JDK OpenJDK 21.0.13.0.3 (Arch Linux), Maven 3.10.0. Em `Projeto/`:
`mvn -B -ntp clean verify` → exit 0, **1459 testes / 0 falhas / 0 erros /
0 ignorados**, **21 XML** Surefire frescos, terminado em 2026-11-22T17:35:12Z
(Maven 79 s). [Log](../../../experiments/construction/024-arvores/test-01/clean-verify.log) ·
[resultado](../../../experiments/construction/024-arvores/test-01/clean-verify-result.json).

JAR congelado: [ott-streaming-1.0-SNAPSHOT.jar](../../../experiments/construction/024-arvores/test-01/ott-streaming-1.0-SNAPSHOT.jar),
517927 bytes; SHA-256
`dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f`
([jar.sha256](../../../experiments/construction/024-arvores/test-01/jar.sha256)).
Composição: 1419 testes históricos (incl. 1379 anteriores + 40 novos do
IMP-014 em `RouteSwitchTest`). Nenhum teste histórico, asserção ou prazo foi
alterado; os negativos S13/S05-S06 reproduzidos no Implement foram corrigidos
e preservados com os seus controlos.

## 2. V-017 / S01–S18 consolidada tecnicamente

[Mapa grupo→métodos→XML](../../../experiments/construction/024-arvores/test-01/v017-oracles.json):
18 grupos com **34 métodos** mapeados em S01–S18, todos PASS; a classe
`RouteSwitchTest` tem no total **40 testcases** PASS no clean verify (os outros
6 são testes de compatibilidade/limites: construtores, payload, sink, fatal).
XML frescos preservados em `test-01/xml/` (21 ficheiros).

| Grupo | Oráculo V-017 | Métodos | Resultado |
|---|---|---|---|
| S01 | Ruído quebra sequência; sem oscilação no limiar | 1 | PASS |
| S02 | Dwell D−1ns não troca, D exacto troca; publicação atómica antes de SUCCESS/poda | 1 | PASS |
| S03 | P0/P100, empate, custo 0 da fonte; sem melhoria por hops/empate | 1 | PASS |
| S04 | Custo máximo sem overflow; fronteiras MAX | 1 | PASS |
| S05 | Amostras repetidas/stale não contam; watermark por contagem/instante; saturação | 9 | PASS |
| S06 | SUSPECT/inválida/Session nova; invalidação e watermark | 4 | PASS |
| S07 | Renovação não desloca a âncora D | 1 | PASS |
| S08 | Custo de selecção não substitui o confirmado | 1 | PASS |
| S09 | Poda captura revisão antiga; publicação antes de SUCCESS | 1 | PASS |
| S10 | ACK precoce espera ACCEPTED; cancelamento vence | 1 | PASS |
| S11 | Falha preserva grant antigo; um terminal; sem retry espontâneo | 1 | PASS |
| S12 | Métrica recuperada sem ANN não rearma; ANN novo rearma sob T/cadência | 1 | PASS |
| S13 | Perda efectiva ignora dwell; deadline global; crédito de incidente | 4 | PASS |
| S14 | Alvo obsoleto não confirma | 1 | PASS |
| S15 | Um de dois consumidores sai sem cancelar a troca | 1 | PASS |
| S16 | Filho incompatível suspenso; ACTIVATE fresco dá prefixo correto | 1 | PASS |
| S17 | Envio físico retido até callback; await(0)=false | 1 | PASS |
| S18 | TCP real S–(A,B)–R com métricas controladas; outra origem/stream isoladas; erros não completam | 3 | PASS |

## 3. Limites

- Métricas controladas nos testes de integração (RTT real validado em V-010);
  não se afirma medição de ruído real nem explicação do negativo histórico R17
  (continua sem causa estabelecida).
- Sem vídeo/GUI/CORE/UDP; sem reroute por perda exclusiva UDP.
- **Manual humano pendente** (obrigação humana, não substituível por ensaio do
  agente); **aprovação de Test pelo utilizador pendente**; revisão final
  **Waheed** e integração em main pendentes. Sem fecho/commits/push.
