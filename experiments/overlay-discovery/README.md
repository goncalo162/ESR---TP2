# V-033 — manual de aprendizagem de candidatos no controlo v2

IMP-034 aprende e retém candidatos. A reparação pertence ao IMP-035, ainda não iniciado. Implement foi aprovado; Test aguarda repetição humana e aprovação, revisão final e integração.

## Repetir pessoalmente

Na raiz do repositório, numa bancada Linux autorizada para sockets locais, com Python 3 e JDK 21:

```sh
python3 -B experiments/overlay-discovery/run-manual.py --operator Luis
```

O comando usa o JAR preservado de clean verify, com SHA-256
`cff5db105bd1f3eca73aebc101724fccbdbd66645056ef6e3278018592bf8d48`.
Confere o hash antes/depois e cria um diretório novo em
experiments/construction/034-overlay-extra/test-manual-runs/.
O primeiro caminho impresso é o diretório real da execução; não reutiliza logs.
Não precisa de sudo. Se a bancada bloquear sockets, a tentativa falha e fica registada.

Aguardar cerca de 70–100 segundos: o TTL observado é o real de 60 segundos,
sem o encurtar por configuração ou usar relógio falso. Conferir a saída e os
ficheiros manual.log, result.json, snapshots learned/retained/expired e logs
cli/quit.log, cli/eof.log e cli/help.log. O sucesso exige JAVA_EXIT 0
(no JSON: java_exit=0), MANUAL_PASS, passed=true, hashes iguais e ausência
de traceback. Se falhar, preservar o diretório e apresentar o resultado.

## O que observar

1. Validação offline rejeita false/Q=1 com erros agregados. O codec operacional
   usa v2 e rejeita versões inválidas e o tipo retirado 3.
2. Uma entrada negativa de capacidade é rejeitada localmente com estado explícito
   por peer; a sessão saudável mantém connectionId e a API candidates continua útil.
   O input negativo é um HELLO v2 válido cujo último byte (requiredMask) é alterado
   no auxiliar. O codec de produção não codifica um modelo incompatível.
3. A CLI real, em JVM separada, responde a neighbors e termina por quit/EOF;
   --help é uma invocação separada. Cada JVM deve terminar com saída 0.
4. Nos grafos S–R1/R2–C e S–R1–R2/R3–C, todos os vizinhos admitidos exigem
   descoberta. Depois de parar o bootstrap, C aprende S ou R1 pelos vizinhos
   diretos. Os snapshots registam endpoint, instâncias, connectionId, token,
   geração, origem local do prazo e TTL.
5. Encerrar R1/R2 no primeiro grafo e R2/R3 no segundo remove as sessões, mas
   conserva os candidatos válidos. S–R1 mantém a mesma sessão no segundo grafo.
   Não surgem S–C/R1–C novas: este IMP só aprende.
6. Após o prazo local de 60 s, os candidatos retidos de C expiram em ambos os
   grafos. O encerramento final cumpre os prazos das APIs e permite voltar a
   abrir as portas dos listeners TCP usados pelo roteiro.

Este manual usa as APIs públicas do JAR com nós embutidos numa JVM e TCP real
em loopback; os ensaios da CLI usam processos próprios com o mesmo JAR.
Os grafos são construídos pela API, sem catálogo de streams/vídeo; os perfis
SERVER gerados servem de registo e não são perfis completos para arrancar a CLI.
A perda de anunciantes é close/EOF da API, não uma morte física, kill -9 ou CORE.
Não mede vídeo, FPS, perdas ou recuperação de rotas. Não substitui os oráculos
com relógio/transporte controlados para saturação, conflitos, callbacks antigos
ou a correlação D07e; estes são executados na regressão completa.

## Evidência separada

- [Regressão clean verify atual](../construction/034-overlay-extra/test-clean-verify-01.log).
- [XML, hashes, JAR e classificação da execução](../construction/034-overlay-extra/test-build-summary.json).
- [Relatório Test](../../memory-bank/bolts/034-overlay-extra/ddd-03-test-report.md).
- [Walkthrough incremental histórico](../../memory-bank/bolts/034-overlay-extra/implementation-walkthrough.md).

O operador Codex identifica a execução do agente. O comando acima identifica
Luis; só a execução real e a revisão de Luis podem satisfazer essa repetição.
O runner não inventa operador, data, aprovação ou estado DONE.
