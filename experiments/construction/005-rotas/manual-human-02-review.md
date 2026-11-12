# IMP-007 — conferência do manual humano execução02

Registo da conferência: 2026-11-12T22:03:45Z. Operador declarado nos metadados: Francisco;
execução humana e ações confirmadas explicitamente pelo utilizador nesta conversa.
Logs originais em manual-humano-execucao-02/, preservados sem qualquer escrita.
Não foi repetida a execução, nem criados CHECK_PASS/MANUAL_PASS humanos artificiais.

## Factos diretamente conferidos

- BOOT-01/BOOT-02/R1-01/R2-02: quatro JAVA_EXIT=0,returncode0,sem sinal.
- R2-01: returncode=-9,signal9,shell_status137,JAVA_EXIT=null. O utilizador
  confirmou que foi SIGKILL intencional; não é saída normal0.
- Cinco .exit.json: process_terminated=true,logger_error=null; PROCESS_RESULT
  no último registo coincide com cada ficheiro. Os PID/metadados RUN e STARTED
  correspondem; não se presume uma varredura global atual de processos.
- As cinco portas TCP58020/58021/58022 e UDP58121/58122 estão available=true no
  ficheiro ports-after.json, na observação 2026-11-12T21:59:11.940888+00:00.
  É bind temporário, não reserva nem garantia permanente; ficheiro before também
  regista as cinco disponíveis antes do teste.
- Todos os comandos/manifestos de execução referem o JAR SHA256
  3f39cab445ca05360525691b15c8f7193b7e0186b8033ef8cbd86d50690cfb15; hash-check.log e cópia atual correspondem. Perfis atuais
  mantêm os hashes do manifesto de preparação.
- PING/PONG v2 recebidos em R1/R2 e requestId/probeId cruzados entre os logs
  confirmam eco bidirecional. Há359 pares correlacionados por direção para R2-01
  e78 para R2-02; são contagens de registos correlacionados, não medições de RTT,
  perda, FPS ou transmissões underlay. Valores completos em manual-human-02-review.json.
- Nenhuma linha FATAL ou CLEANUP_ERROR encontrada. Existem diagnósticos
  TRANSPORT,TIMEOUT,STALE,DISCOVERY_CACHE_REJECTION e de shutdown/leave: foram
  conservados e contados no JSON. Não se declara ausência de diagnósticos.

## Identidades e ordem observáveis em R1-01.log

| Evento | Linha | instanceId de R2 | connectionId local em R1 |
|---|---:|---|---|
| Up inicial | 43 | 106207f4-fe7f-457c-a6f8-cb219c4382b4 | b51d2924-447d-4da9-bf99-bf8eab454e0e |
| Down TRANSPORT | 625 | 106207f4-fe7f-457c-a6f8-cb219c4382b4 | b51d2924-447d-4da9-bf99-bf8eab454e0e |
| Up reconexão | 668 | 106207f4-fe7f-457c-a6f8-cb219c4382b4 | e63ce25e-47d7-40a7-8475-f162917953b7 |
| Down TRANSPORT | 849 | 106207f4-fe7f-457c-a6f8-cb219c4382b4 | e63ce25e-47d7-40a7-8475-f162917953b7 |
| Up instância nova | 964 | 9ef26219-fa64-4e71-9ece-3e8f5a538fc4 | e4c9c27a-5212-4a9a-8022-e4c177170020 |
| Down LEAVE | 1132 | 9ef26219-fa64-4e71-9ece-3e8f5a538fc4 | e4c9c27a-5212-4a9a-8022-e4c177170020 |

A primeira reconexão conserva instanceId e muda connectionId; o reinício muda
ambas. Não comparar UUID de conexão entre extremos: são identidades locais.
READY de R2-01/R2-02 confirma as respetivas instâncias e os logs de ambos contêm
sondas e vizinho ACTIVE. R1 preserva a sua própria instância durante a sequência.

## Contexto confirmado pelo operador e limites

O utilizador confirmou retirada durante STOP, CONT depois de terminar bootstrap,
EOF após KILL e novo arranque. A sequência acima é compatível com esse relato.
Os logs da CLI não gravam os comandos de sinal nem timestamps por evento: por si
só não provam o instante STOP/CONT, nem a ordenação global do segundo Up face ao
fim do bootstrap. Por isso a ação e a condição «sem bootstrap» são registadas
como confirmação humana, apoiada pelos eventos, não como medição independente.

TRANSPORT não distingue PEER_EOF de timeout/fecho de transporte. Não se atribui
PEER_EOF literal à linha nem se mede latência/contagem exata de três sondas a partir
deste log. EOF imediato, reset integral de métricas, EWMA e terminação física dos
recursos internos permanecem sustentados pelos oráculos V-010 e XML clean verify.
Sem prova/alegação nova de GUI, CORE, vídeo ou revisão final do código.

## Resultado e checkpoint

Execução humana e observações conferidas no âmbito acima; requisito de manual
humano cumprido pela confirmação do utilizador e evidência consistente.
Test apresentado para aprovação, **ainda não aprovado**. clean verify1176/0/0/0,
V-ADD documental atual e código aprovado preservado. Revisão final do código e
integração pendentes; Fillmore confirmado só no Design. Nenhum commit/push/fecho.
