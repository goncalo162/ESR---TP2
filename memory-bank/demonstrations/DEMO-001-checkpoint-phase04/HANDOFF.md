# Retoma — DEMO-001-checkpoint-phase04 (iteração 2 concluída)

**Estado: critérios de aceitação do bolt cumpridos na série formal congelada
`8303c0b`;demonstração pronta para validação/aceitação. PHASE-05–07
continuam TODO — não foram iniciadas.** Todas as runs FAIL (antigas e novas)
preservadas; nenhum resultado antigo foi reescrito para PASS.

## Ambiente e builds (iteração 2)

- Branch `Waheed`, JDK `21.0.13.0.3` (`/usr/lib/jvm/java-21-openjdk` nesta
  máquina), Maven 3.10.0. Working tree limpo no início (só `Projeto/.idea/`
  untracked).
- Clean verify inicial e final: `cd Projeto && mvn -B -ntp clean verify` →
  **1459/0/0/0, 21 XML, BUILD SUCCESS** em ambos
  ([início](../../../experiments/checkpoint-phase04/build-start-iteration2/result.json),
  [fim](../../../experiments/checkpoint-phase04/build-end-iteration2/result.json)).
  JAR `dc192971c9ba3a7f66df0e20b3851b4c176c8f4d34158267610b4d7eebab920f`
  idêntico no início e no fim (`jarSameAsStart: true`). Zero alterações em
  fontes/testes de produção, CORE ou scripts anteriores.
- Investigação completa em [investigation-iteration2.md](investigation-iteration2.md).

## Ajustes justificados à ferramenta (só `demo/`, commits `94300b2`…`23ddc9a`)

1. `DelayProxy`: cada frame agendado para `chegada + atraso` por direção
   (leitor/escritor desacoplados), removendo a fila de serialização do
   `Thread.sleep` no ciclo de leitura que inflava o RTT medido (>900ms com
   120ms configurado). Bytes e ordem TCP preservados.
2. Oráculo de trace `trace-before`/`trace-after`: observação com prazo (15s)
   de trace positivo multi-salto após ACK; terminais negativos preservados nos
   ficheiros de evidência.
3. Oráculo de atomicidade do switch: no evento SUCCESS, a forwarding publicada
   no instante do evento tem de mostrar o novo upstream/caminho com revisão
   superior (`replacementRevision` é a revisão do ACTIVATE na nova linha —
   espaço próprio — e não podia comparar-se com a revisão da árvore).
4. Após o pulso transitório, espera que o caminho anterior volte a confirmar e
   permaneça estável por janela > dwell (5s) antes do atraso sustentado.
5. Prazo de convergência inicial do arranque escala com o nº de nós (60s para
   >6 nós; 20s inalterado nos cenários 1/2). Prazos do guião (ex.: switch 25s)
   intocados.

## Resultados (JAR `dc192971…` em todas as runs novas)

**Série congelada `8303c0b` (3 consecutivas por cenário, todas PASS):**
- Cenário1: `20261123T151115Z-ff3a5f83`, `20261123T151147Z-9deaebcc`,
  `20261123T152225Z-28c6b934` (33 checks cada).
- Cenário2: `20261123T151257Z-4b4adbaf`, `20261123T151421Z-67a5116e`,
  `20261123T151525Z-1b1daa64` (39 checks cada), com troca por qualidade
  **STARTED→SUCCESS** (custos reais ~250ms→~60ms), publicação atómica
  confirmada, pulso de 150ms sem oscilação, SIGKILL real de R2 com recuperação
  sem bootstrap, reinício, poda e procura abandonada.
  [Série final em HEAD](../../../experiments/checkpoint-phase04/series-final-head.log).
- Séries anteriores na mesma base de código (23ddc9a, idêntica em ficheiros de
  código): c1 3/3 PASS (`2a2d667c`,`0b6a21a5`,`4fec984a`); c2 4 PASS consecutivas
  (`b7363015`,`f91d87a2`,`049d5119`,`6913a902`) + 4 PASS adicionais.

**HEAD final `63d0c97`** (escala de prazos de convergência para >6 nós e
espera de 1s no oráculo de trace; inerte para 4/6 nós):
[series-final-head2.log](../../../experiments/checkpoint-phase04/series-final-head2.log)
— c1 3/3 PASS (`505e78d5`,`a7cbb419`,`8ba7e4cd`); c2 2 PASS + 1 FAIL preservado
(`3cc7f000`, corrida residual do switch); c3 2 PASS (`149ff3ed`,`e135a4d5`) +
1 FAIL preservado (`07e916d7`).

**Cenário3 (11 nós da Figura 1): completo, PASS repetido** —
`20261123T115804Z-96352b48`, `20261123T151646Z-73408404`,
`20261123T152742Z-149ff3ed`, `20261123T152923Z-e135a4d5` (39 checks cada; 17
processos terminados; portas reutilizáveis), com switch por qualidade em O3,
morte real de O3 e recuperação sem bootstrap. FAILs de arranque/CLI
preservados (`9e1b0832`, `6d8b9caf`, `807586fb`, `7e5ca65b`, `1ae53dad`,
`07e916d7`).
- Todas as runs desta iteração incluyen `tool-source/` e `tool-sha256.json`;
  as 6 runs das séries formais partilham o mesmo hash da ferramenta (1 valor
  distinto). Dashboard HTML regenerado com dados reais (sem placeholder).
- Ensaio: guião automático completo ~59s (cenario2) / ~73s (cenario3);
  guião de apresentação com os tempos por passo do DEMO.md → 10–15 min.

## Negativos preservados e causa / âmbito

- `cenario2/20261122T233636Z-e48c789f` (e repro `…T114247Z-7e4c6be5`): switch
  STARTED→FAILURE (PATH_CHANGED na 1.ª tentativa / QUALITY_CHANGED borderline
  no dwell) sem SUCCESS; convergência posterior via rondas simples. **Âmbito:
  comportamento do sistema (JAR) — não alterado; a ferramenta só registas.**
- `cenario2/20261123T142314Z-e63bfcf3`, `…T143102Z-32a314b3`: mesma corrida
  residual na 1.ª tentativa do switch (~2 em 10 runs nesta máquina). Pass rate
  ~80% por run no cenário2; preservados como evidência de que não se promete
  estabilidade infinita.
- `cenario1/20261123T120158Z-76021040`: FAIL `lease-renewed-past-original-6s`
  durante uma pausa de ~1h59m do sandbox (relógio/nanoTime avançou com a
  máquina parada); renewals presentes no wire; artefacto de ambiente, não do
  sistema.
- NO_ROUTE pré-ativação da CLI (`…T233117Z`, `…T233325Z`, repro
  `…T112915Z-c8130075`): corrida de prontidão — a CLI substitui o adaptador de
  C2 e o vizinho retira/reanuncia; no instante do trace o candidato estava
  vazio → NO_ROUTE legítimo. Oráculo agora preserva o negativo terminal e exige
  trace positivo multi-salto após ACK com prazo. Causa estabelecida — sem defeito de produção.
- R17 histórico continua sem causa estabelecida; V-018 (44/44) intacto.

## Limitações actuais

- Sem UDP, fragmentação, MJPEG, player/GUI, vídeo ou CORE; `play` continua
  NOT_IMPLEMENTED. A demonstração não é aprovação docente/submissão.
- O proxy atrasa por frame (agendado), sem fila artificial de serialização;
  TCP real mantém ordenação. Atraso configurável 0..250ms/direção.
- Corrida residual na 1.ª tentativa do switch de qualidade em ~20% das runs
  do cenário2 (rejeição PATH_CHANGED no alvo); o sistema converge de qualquer
  forma, mas sem evento SUCCESS nesse caso. Não corrigido (âmbito do JAR).

## Próximos passos

1. Validação externa do bolt (validador-bolt) segundo DEMO.md/MATRIX.md/
   09-TESTING-STRATEGY.md.
2. Se aceite, propor ao utilizador a aceitação da demonstração; PHASE-05–07
   permanecem TODO e não são iniciadas por esta ferramenta.
