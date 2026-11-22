# Segundo marco da gestão de fluxos — IMP-015 / V-018

Demonstração do segundo marco intermédio (M2): árvores de distribuição e
adaptação do plano de controlo, sem vídeo. Os dados são mensagens sintéticas
de diagnóstico claramente identificadas; nenhuma prova de vídeo, GUI ou CORE
é alegada. O bolt é `memory-bank/bolts/009-arvores` (IMP-015, TASK-018);
dependências: 022-rotas (IMP-010) e 024-arvores (IMP-014), ambos complete.

[ADD §IMP-015](../../add.md#imp-015) ·
[TASK-018](../../docs/tasks/PHASE-04/TASK-018.md) ·
[V-018](../../docs/09-TESTING-STRATEGY.md#v-018--preparar-o-segundo-marco-com-árvores-e-adaptação).

**Estado da segunda avaliação (G-ASSESSMENTS)** — data e conteúdo pendentes
de informação docente; não inventar datas.

## Abordagem

Ao contrário do primeiro marco (processos CLI com `neighbors`/`trace`), o
segundo marco precisa de consumidores que pedem subscrição de streams, o que a
CLI ainda não expõe (`play` está `NOT_IMPLEMENTED`). O roteiro usa portanto um
auxiliar Java (`Milestone2.java`) executado em modo fonte contra o JAR
congelado (`java --class-path <JAR> Milestone2.java`), como o `Inspect.java` do
primeiro marco e o `ManualRecovery.java` do bolt 008-arvores. O auxiliar usa
apenas API pública do JAR: `BootstrapServer`, `NeighborManager`,
`RoutingService`, `NodeConfiguration`/`TopologyConfiguration`/`StreamConfiguration`.

Cada nó corre com TCP real em loopback (portas efémeras), bootstrap real para
localização inicial (ADR-013) e métricas de custo controladas injetadas via
`RoutingService.onMetrics` no fio do owner — o mesmo padrão "TCP real +
métricas controladas" do S18 do `RouteSwitchTest`. A medição RTT em si foi
validada em V-010; aqui o que se demonstra é a reação da árvore à mudança de
custo. Um tap no `ControlPort` de cada nó regista todas as mensagens de
controlo enviadas (tipo, stream, direção), e os snapshots
(`RoutingService.Snapshot`: routes, forwarding, recoveries, diagnostics) são
registados antes/depois de cada passo. Esta é a evidência exigida por V-018:
logs e snapshots que permitem a um terceiro reproduzir ativação, poda e
failover.

## Preparar a execução

Da raiz do repositório, com JDK 21 e Python 3.9+:

```bash
export M="$PWD/experiments/milestone-2"
export JAR="$PWD/Projeto/target/ott-streaming-1.0-SNAPSHOT.jar"
sha256sum "$JAR"   # registar no manifesto da run
python3 -B "$M/run-milestone2.py" --jar "$JAR" --java "$(command -v java)"
```

O wrapper cria uma pasta nova em `runs/<utc>-<rand>/`, grava `manifest.json`
(data UTC, commit git, versão Java, SHA-256 do JAR, comandos), executa o
auxiliar com saída em `run.log` e escreve `result.json` (totais por cenário).
Nunca sobrescreve execuções anteriores. A execução termina com código 0 se
todos os cenários passam e 1 caso contrário; cada verificação imprime
`CHECK_PASS`/`CHECK_FAIL` em `run.log`.

## Cenários e oráculos

### C1 — Isolamento entre fluxos (2 servidores, 4 consumidores)

Topologia: S1 e S2 (SERVER, cada um com um stream distinto: A e B), relay R
ligado a S1 e S2, e C1..C4 (CLIENT) ligados a R. S1 anuncia o stream A, S2 o
stream B.

| Passo | Oráculo |
|---|---|
| C1 e C3 pedem `requestSubscription(A)`; C2 e C4 pedem `requestSubscription(B)` | Os quatro tickets terminam em `SUCCESS`; o snapshot de R mostra duas rotas (A→S1, B→S2) com origens/épocas distintas |
| `routes`/`forwarding` de C1 contém apenas A; de C2 apenas B | Isolamento por stream: nenhuma rota da outra origem no consumidor |
| C1 cancela a subscrição (`ticket.cancel()`) | C3 (mesmo stream A) permanece `SUCCESS`; C2 e C4 (stream B) permanecem `SUCCESS`; o tap mostra DEACTIVATE de R para jusante apenas no ramo de C1, sem afetar B |
| S1 deixa de estar alcançável (fecha-se S1) | A fica indisponível para C3; B continua `SUCCESS` em C2/C4 — isolamento entre fluxos preservado |

### C2 — Procura agregada (1 servidor, 2 consumidores no mesmo stream)

Topologia: S (SERVER, stream A), relay R, C1 e C2 ligados a R (S–R–C1, S–R–C2).

| Passo | Oráculo |
|---|---|
| C1 e C2 pedem subscrição de A com tokens distintos | Ambos `SUCCESS` |
| Contagem de mensagens ACTIVATE no tap | Exatamente **1** ACTIVATE de R→S para o stream A durante a ativação, apesar de 2 consumidores (RF-011: uma subscrição no tronco) |
| Snapshot de S (`forwarding(A).downstreams`) | Um único ramo de consumo agregado; `localConsumers` de R contém os dois tokens |
| C1 sai (cancel) | C2 permanece `SUCCESS`; ainda sem novo ACTIVATE upstream (o ramo continua por C2) |
| C2 sai (cancel) | Último consumidor sai: R envia DEACTIVATE a S; o snapshot de R deixa de ter `forwarding(A)` após o prazo de poda |

### C3 — Falha e recuperação (morte de um relay, switch para alternativa)

Topologia em losango: S ligado a R1 e R2; C ligado a R1 e R2.

| Passo | Oráculo |
|---|---|
| C subscreve A; métricas controladas fazem o caminho via R1 ser o confirmado | `forwarding(A).upstream == R1`; ticket `SUCCESS` |
| R1 morre (fecha-se o `NeighborManager`/ligações sem NODE_LEAVE — simula morte abrupta) | `NeighborDown` em S e C; C comuta para R2: `forwarding(A).upstream == R2`, evento ROUTE_SWITCH/razão de falha registado; o ticket permanece `SUCCESS` (recuperação sem nova subscrição humana) |
| Variante 3b: cadeia S–R1–C sem alternativa; R1 morre | C fica `UNAVAILABLE`/sem forwarding após o prazo, com evento de falha explícito — fim sem caminho alternativo é explícito, não silencioso |

### C4 — Alteração de RTT (mudança de custo, comuta para o melhor caminho)

Topologia: S ligado a A e B; R (consumidor) ligado a A e B — idêntica ao S18.

| Passo | Oráculo |
|---|---|
| Custos iniciais: A=10, B=1000 (µs) | Subscrição de R confirma via A (`forwarding(A).upstream == A`) |
| Injetam-se 3+ amostras novas em B com custo 1 (melhoria sustentada >20% durante a janela de délivrance) | Ocorre comuta confirmada: evento `ROUTE_SWITCH phase=SUCCESS reason=CONFIRMED`, `previousCostMicros`/`replacementCostMicros` registados, `forwarding(A).upstream == B` |
| Ruído à volta do limiar não comuta | Amostras que não sustentam a melhoria não geram segundo evento SUCCESS (sem oscilação) |

## Evidência por run

`experiments/milestone-2/runs/<run-id>/`:

- `manifest.json` — data UTC, commit, hash do JAR, versão Java, operador, comandos.
- `run.log` — saída do auxiliar com linhas `CHECK_PASS`/`CHECK_FAIL`, eventos
  ROUTE_SWITCH, e extratos de snapshots (routes/forwarding) por passo.
- `result.json` — por cenário: nome, controlos, pass/fail, duração; total.
- `messages-<node>.jsonl` — tap de mensagens de controlo enviadas por nó
  (tipo, requestId, stream, direção), para auditoria da agregação e da poda.

## Limites

- Sem vídeo, GUI, CORE ou datagramas UDP; `play` continua `NOT_IMPLEMENTED`.
- As métricas de custo são controladas (RTT real validado em V-010); a lógica
  de árvore, protocolos e TCP são reais.
- A morte de relay é fecho abrupto de processo/componente em JVM única; não é
  SIGKILL de processo SO (isso foi provado no manual do IMP-035).
- G-ASSESSMENTS permanece pendente de informação docente.

## Limpeza

Cada run usa portas efémeras libertadas no fim; o auxiliar termina todos os
owner/manager/router com `requestClose`+`awaitTermination` e verifica que não
ficam threads vivas. O wrapper termina com o código de saída do auxiliar.
