# V-034 — reparação da overlay com mortes reais

Implement aprovado; Test executado pelo agente e à espera da repetição de Luis.
Não há aprovação de Test, fecho ou integração deste incremento.

## Repetir pessoalmente

Na raiz do repositório, numa bancada Linux autorizada para sockets locais,
com Python3, JDK21, javac, /proc e ss:

```sh
python3 -B experiments/overlay-recovery/run-manual.py --operator Luis
```

O runner usa o JAR preservado do clean verify1113, SHA-256:

```text
dea01ffa2c501b9b948594005e7e8e9ee70b387981dc5b3d83f9d7c331cc2526
```

Não precisa de sudo. Não reconstruir o JAR para repetir esta versão. O primeiro
caminho impresso identifica o novo diretório em
experiments/construction/035-overlay-extra/test-manual-runs/. Guardar esse caminho;
manual.log/result.json e subdiretórios dos dois grafos contêm os resultados reais.
O número de CHECK_PASS pode variar se ambos os extremos iniciarem uma tentativa;
o runner exige todos os oráculos, MANUAL_PASS, passed=true, saída Python0 e
no_processes_pending=true. O operador é declarado, não verificado como identidade
ou aprovação. Só a tua execução/revisão satisfaz a repetição humana.

O comando compila um auxiliar de observação fora de Projeto e arranca uma JVM
por nó, carregando as APIs públicas reais do mesmo JAR. Usa configuração validada,
clock/transporte/limites padrão e aprendizagem real. Não usa JUnit, nós embutidos
no mesmo processo, fake clock ou mensagens wire injetadas. O auxiliar só expõe
snapshots/cache que a CLI de produção não imprime. A CLI real foi exercitada
separadamente pelo clean verify (OttApplicationTest48); não se atribui ao auxiliar
uma execução da main OttApplication. Não altera fontes nem classes do JAR.

## Oráculos dos dois grafos

| Grafo inicial | Primeira morte | Segunda morte |
|---|---|---|
| S–R1–C; S–R2–C | SIGKILL R1: alternativa R2, sem S–C | SIGKILL R2: formar S–C |
| S–R1–R2–C; S–R1–R3–C | SIGKILL R2: alternativa R3, sem R1–C | SIGKILL R3: formar R1–C, manter S–R1 |

1. Conferir os perfis completos/validação offline, PIDs, instanceIds, grafo inicial
   e JAR/hash carregado por cada processo. Antes das falhas, os snapshots02
   devem mostrar candidatos aprendidos por vizinhos diretos, sourceKey, geração
   e TTL60s desde queryStarted, sem catálogo global de candidatos.
2. O bootstrap termina por quit/saída0 antes das mortes. Cada SIGKILL é precedido
   da confirmação de PID, starttime de /proc, cmdline/perfil/token deste ensaio;
   só morrem os relays criados pelo próprio runner. Saída esperada dos mortos:-9.
3. Após a primeira morte, observar alternativa existente e ausência da nova
   aresta durante a janela registada de cerca de1s. Antes da segunda, guardar
   candidatos retidos e os claims já observados. Não remover os sobreviventes.
4. Após a segunda, observar a sessão única nova nos dois extremos. RepairStarted
   deve corresponder a proveniência observada antes da morte, dentro do TTL
   original. PIDs/instanceIds dos sobreviventes permanecem iguais; no segundo
   grafo também as sessões/connectionIds S–R1 em ambos os extremos.
5. Os snapshots08 conferem estabilidade durante a janela registada de cerca de2s.
   Os eventos devem mostrar um NeighborUp do peer reparado por extremo e nenhum
   NeighborDown desse peer no intervalo. São observações limitadas ao ensaio,
   não promessa de estabilidade infinita ou SLA de recuperação.
6. A unicidade física usa os inodes dos FDs e as tabelas TCP/TCP6 em /proc dos
   próprios PIDs, incluindo IPv4 mapeado em sockets Java dual stack. Esperar uma
   única tupla TCP entre os extremos. ss fica como diagnóstico adicional.
   connectionId é local a cada extremo; não se exige o mesmo UUID nos dois lados.
7. Os sobreviventes terminam por shutdown público com prazo/saída0; bootstraps0,
   relays mortos-9. Conferir no_processes_pending, logs de shutdown e portas
   de listener reutilizáveis. Se falhar, manter o diretório e apresentar o erro.

## Execução do agente e histórico

Codex passou68 checks/MANUAL_PASS em20261112T003609Z-sj9_ljkn. As duas tentativas
anteriores permanecem: a reparação S–C ocorreu, mas o observador só verificava
IPv4/tcp e não encontrava os sockets dual stack em tcp6. Ambas foram marcadas
failed e limparam os processos. A correção foi só no observador Python; nenhum
fonte de produção mudou. Os sources desses runners estão arquivados nas tentativas.

Os snapshots/ações/logs comprovam controlo TCP em loopback e mortes de processos
reais. Não demonstram vídeo, routing, FPS, falhas de IP silenciosas ou CORE.
As entradas negativas, concorrência/saturação/TTL/fronteiras são cobertas pela
regressão com relógio/transporte/escrita controlados onde previsto.
