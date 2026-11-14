# IMP-010 / V-013 — manual humano sobre o JAR de Test

**Execução humana pendente. Test não aprovado.** Este roteiro prepara uma execução
nova, sem usar as classes mutáveis de target/. Não substitui os40 testes de trace,
relógio/transporte falsos ou a regressão. Não demonstra CORE/GUI ou desempenho.

## Construção fixa

JAR: [ott-streaming-1.0-SNAPSHOT.jar](test-build-01/ott-streaming-1.0-SNAPSHOT.jar),
436556 bytes, Java21/class major65,238 classes de produto e nenhum teste empacotado.
SHA-256:

```text
5f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52
```

O helper confere JAR/fontes/POM/auxiliar contra os hashes aprovados antes/depois;
usa java -cp <cópia-do-JAR-fixo> pt.uminho.esr.ott.app.OttApplication.
Precisa de JDK21 no PATH, Python3 e sinais POSIX (Linux/macOS, como os ensaios
locais). Não usa sudo, tcpdump, ferramentas de cobertura ou dependências Java novas.

## Perfis e portas

Cinco processos: BOOT, S, R1, R2 e C; losango S–R1/R2–C, sem aresta direta S–C.
S tem dois UUID distintos, fps25 de configuração e ficheiros deliberadamente não
lidos; não executar play. Não é prova de entrega de media ou de dois servidores.

| Processo | Papel | TCP | UDP anunciado |
|---|---|---|---|
| BOOT | BOOTSTRAP | 20880 | nenhum |
| S | SERVER | 20881 | 20981 |
| R1 | RELAY | 20882 | 20982 |
| R2 | RELAY | 20883 | 20983 |
| C | CLIENT | 20884 | 20984 |

Shutdown1500ms, connect300ms, retries50..200ms/jitter0, como os perfis CLI testados.
TRACE mantém TTL32 e3000ms aprovados. Limites/bytes/políticas de produto intactos.
prepare testa disponibilidade das cinco portas TCP e quatro UDP; isso é uma
observação inicial, não reserva até ao arranque. Se uma estiver ocupada, não
matar processos alheios: terminar a execução e identificar o conflito.

## Preparar — terminal de controlo

Todos os comandos partem da raiz deste repositório. Substituir TEU_NOME pelo
operador real. Para escolher outro JDK21, acrescentar --java-home <diretório-real>.

```bash
python3 -B experiments/construction/022-rotas/manual-tools/manual.py prepare --operator "TEU_NOME" --kind human
```

O comando imprime RUN=<diretório novo>. Copia esse caminho para OTT_RUN em todos
os seis terminais (um de controlo, cinco dos nós); não reutilizar diretório antigo.

```bash
export OTT_RUN="CAMINHO_ABSOLUTO_IMPRESSO_EM_RUN"
python3 -B experiments/construction/022-rotas/manual-tools/manual.py watch --run "$OTT_RUN" > "$OTT_RUN/watch.log" 2>&1 &
OTT_WATCH_PID=$!
cat "$OTT_RUN/watch.log"
```

Confirmar UDP_WATCH_READY antes dos traces. Se ainda não houver essa linha,
voltar a ler o log; se houver erro, abortar. O watcher ocupa as quatro portas
UDP anunciadas e grava qualquer datagrama observado, sem o descodificar.

## Arrancar os cinco terminais

Em cada terminal, definir OTT_RUN com o mesmo caminho. Primeiro BOOT e esperar
READY; depois S, R1, R2 e C. O wrapper imprime os logs enquanto aceita comandos
no stdin, guardando <nó>.log, comandos com instantes, PID e saída real da JVM.

Terminal BOOT:

```bash
python3 -B experiments/construction/022-rotas/manual-tools/manual.py node --run "$OTT_RUN" --node BOOT
OTT_STATUS=$?; printf 'WRAPPER_EXIT=%s\n' "$OTT_STATUS"
```

Terminal S:

```bash
python3 -B experiments/construction/022-rotas/manual-tools/manual.py node --run "$OTT_RUN" --node S
OTT_STATUS=$?; printf 'WRAPPER_EXIT=%s\n' "$OTT_STATUS"
```

Terminal R1:

```bash
python3 -B experiments/construction/022-rotas/manual-tools/manual.py node --run "$OTT_RUN" --node R1
OTT_STATUS=$?; printf 'WRAPPER_EXIT=%s\n' "$OTT_STATUS"
```

Terminal R2:

```bash
python3 -B experiments/construction/022-rotas/manual-tools/manual.py node --run "$OTT_RUN" --node R2
OTT_STATUS=$?; printf 'WRAPPER_EXIT=%s\n' "$OTT_STATUS"
```

Terminal C:

```bash
python3 -B experiments/construction/022-rotas/manual-tools/manual.py node --run "$OTT_RUN" --node C
OTT_STATUS=$?; printf 'WRAPPER_EXIT=%s\n' "$OTT_STATUS"
```

## Conferência humana e comandos da aplicação

1. Nos nós normais, usar neighbors e esperar duas sessões ACTIVE por nó. Em C:

   ```text
   routes
   catalog
   metrics
   ```

   Esperar cada listagem terminar (_END) antes da seguinte. Conferir dois UUID,
   origemS, candidatos por R1/R2, custos/idade com unidades e snapshots rotulados.
   Não atribuir um custo numérico esperado ao RTT real. Se ainda não convergiu,
   guardar a observação e repetir a listagem, sem alterar prazos/configurações.

2. Em S, executar o diagnóstico local e confirmar TRACE_RESULT com visited=[S]:

   ```text
   trace 00000000-0000-4000-8000-000000000001
   ```

3. Em C, confirmar TRACE_STARTED seguido de TRACE_RESULT correlacionado e
   visited=[C, R1, S] **ou** [C, R2, S]. Anotar qual relay aparece:

   ```text
   trace 00000000-0000-4000-8000-000000000001
   ```

   TRACE_HOP nos relays/fonte distingue request/response e custo observado local.
   Visited mantém ordem cliente→origem; a resposta volta em sentido inverso.
   Não inferir delay unidirecional, custo remoto ou ACK a partir de ACCEPTED.

4. Em BOOT, escrever quit e confirmar JAVA_EXIT=0. O resto continua sem bootstrap.
   No terminal de controlo matar **só o relay indicado no resultado do passo3**.
   Se foi R1:

   ```bash
   python3 -B experiments/construction/022-rotas/manual-tools/manual.py signal --run "$OTT_RUN" --node R1 --signal KILL
   ```

   Se foi R2, substituir --node R1 por --node R2. Não executar ambos os comandos.
   No terminal morto: PROCESS_RETURN=-9, SHELL_STATUS=137 e WRAPPER_EXIT=137 são
   o SIGKILL intencional; não são saída Java normal. Conferir NeighborDown em C.

5. Em C, repetir routes até observar a alternativa e executar novamente o trace
   do passo3. Conferir TRACE_RESULT pelo **outro** relay. Se houver transição com
   erro explícito, preservar o resultado e repetir após convergência; nunca
   apagar logs ou aumentar TTL/timeouts. Cada pedido mantém o seu prazo3000ms.

6. Em C, conferir erros locais e stream desconhecida:

   ```text
   trace bad
   trace 11111111-1111-4111-8111-111111111111
   ```

   Esperados: COMMAND_ERROR trace requires... e TRACE_ERROR/UNKNOWN_STREAM.
   Até4 contextos locais ficam protegidos até3s: aguardar3s após o último
   resultado antes do próximo bloco evita saturação involuntária; não altera o
   prazo de qualquer pedido. RESOURCE_LIMIT/CLI_BUSY é explícito, não sucesso.

7. Primeiro em **S**, escrever quit e confirmar JAVA_EXIT=0; depois no relay
   sobrevivente, escrever quit e confirmar JAVA_EXIT=0. Retirar a origem é
   necessário: com S ainda vivo, a reparação IMP-035 pode criar uma ligação
   direta C–S após perder ambos os relays, e um trace bem-sucedido [C,S] seria
   correto. Não desativar essa reparação nem mudar configurações. Em C:

   ```text
   trace 00000000-0000-4000-8000-000000000001
   ```

   Esperar TRACE_ERROR/NO_ROUTE, sem espera ilimitada nem ativação de dados.
   TTL_EXHAUSTED e a fronteira de3000ms são provados pelos testes controlados;
   este grafo manual não pretende fabricar um percurso de32 nós.

8. Em C, escrever quit e confirmar JAVA_EXIT=0 (S já terminou no passo7). Há quatro saídas normais0
   e um SIGKILL intencional. Confirmar ausência de FATAL/erros do logger/cleanup.
   Fechar o watcher só depois dos cinco processos terminarem:

   ```bash
   python3 -B experiments/construction/022-rotas/manual-tools/manual.py stop-watch --run "$OTT_RUN"
   wait "$OTT_WATCH_PID"
   OTT_WATCH_STATUS=$?; printf 'WATCH_EXIT=%s\n' "$OTT_WATCH_STATUS"
   cat "$OTT_RUN/watch.log"
   python3 -B experiments/construction/022-rotas/manual-tools/manual.py verify --run "$OTT_RUN"
   ```

   Esperado: WATCH_EXIT=0, UDP_OBSERVED=0, todos CHECK_PASS e MANUAL_PASS.
   O verificador confirma os caminhos nas linhas TRACE_RESULT da CLI, erros,
   ausência de11–14 nos logs, saídas/recolha dos filhos, hashes e portas livres.
   Gera report.json com operador/kind declarados; não autentica a identidade.

Se interromperes: registar a interrupção, retomar S com CONT caso o tenhas
suspendido, terminar os processos desta execução e o watcher; execução parcial
não conta como manual aprovado. Não usar --signal KILL num PID externo/antigo.
A observação UDP tem o intervalo guardado; não é captura CORE, medição de FPS
ou prova de classificação FIN/RST. Saída0 e portas reutilizáveis não prometem
inexistência de TIME_WAIT.

## O que devolver

Enviar o caminho do **novo report.json**, resultado do manual e qualquer
observação divergente. O helper não aprova Test, não fecha o bolt, não contacta
Fillmore e não integra código. Falta manual humano antes de aprovar Test;
depois continuam necessárias a revisão final de Fillmore e a integração.
A falha histórica de saída bloqueada permanece registada sem causa estabelecida;
não reapareceu no clean verify deste JAR e não autoriza aumentar prazos/testes.
EOF/Ctrl+C/erro não contam como aprovação nem devem ser ocultados numa repetição.
