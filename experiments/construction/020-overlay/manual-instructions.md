# Manual humano de Francisco — IMP-005 / 020-overlay

**Pronto; ainda não executado por Francisco.** O agente executou clean verify e
validações mecânicas; não executou este manual nem criou logs pessoais.
Esta preparação não presume aprovação de Implement/Test, revisão final ou integração.

## Versão conferida

`mvn -B -ntp clean verify`, em Projeto/: **981 testes, zero falhas/erros/ignorados,
saída 0**. [Log novo](test-clean-verify-01.log) e [XML](test-clean-verify-01-xml/).

JAR novo: `/Users/maria/Documentos/Francisco/ESR/ESR---TP2/Projeto/target/ott-streaming-1.0-SNAPSHOT.jar` (216087 bytes).
SHA-256:

```text
303facafc148b96bd2cc0d98ea6afc7a773a50cb2b49d1d5c6c6c4c481de5b52
```

[Cópia do mesmo JAR](verified-jar/ott-streaming-1.0-SNAPSHOT.jar) e
[manifesto de JAR/perfis](manual-preparation.json). O helper verifica os hashes do
JAR e dos seis ficheiros de configuração antes de cada execução, recusando
alterações. Não reconstruir/alterar a versão durante o manual.

## Perfis e portas

[manual-profiles](manual-profiles/) é uma cópia própria, com caminhos relativos.
ConfigurationLoader do JAR novo validou conjuntamente os quatro perfis, sem
iniciar aplicações/sockets: [log offline](manual-profile-validation.log),
`DEPLOYMENT_VALID profiles=4`. Grafo triangular N0–N1–N2, duas sessões por nó;
BOOT apenas localiza vizinhos. SERVER tem dois metadados de stream; este IMP
não demonstra vídeo/GUI/CORE. Shutdown configurado: 1500 ms por processo.

| Papel | node.id | Perfil | TCP controlo | UDP configurado |
|---|---|---|---:|---:|
| BOOTSTRAP | BOOT | bootstrap.properties | 56100 | — |
| SERVER | N0 | N0.properties | 56110 | 56111 |
| RELAY | N1 | N1.properties | 56120 | 56121 |
| CLIENT | N2 | N2.properties | 56130 | 56131 |

Host: `127.0.0.1`. As sete portas estavam disponíveis em
`2026-11-10T18:47:54.860630+00:00`: [bind temporário TCP/UDP](manual-port-check-agent.json),
sem listen nem reserva mantida. Reconfirmar antes de iniciar os processos.

## Preparação no terminal 1

Usar quatro terminais **zsh interativos**, com Java 21 no PATH. Source apenas
define o helper; ott_manual_run inicia a aplicação. Não sobrescreve logs existentes.

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2/Projeto
source /Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-session.zsh
java -version
shasum -a 256 "$OTT_MANUAL_JAR"
mkdir -p "$OTT_MANUAL_PERSONAL_DIR"
python3 /Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-port-check.py | tee "$OTT_MANUAL_PERSONAL_DIR/ports-before.log"
```

Confirmar todas as entradas available=true. Se houver conflito, guardar o resultado
sem editar os perfis de evidência. Logs pessoais em
`/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-Francisco-execucao-01`; esse diretório ainda não tem execução humana.

O helper grava comando completo, hash, stdout/stderr e instantes UTC reais.
Depois de Java terminar, grava **JAVA_EXIT e TEE_EXIT separadamente**, em .exit e
no log. Assim tee não mascara o código Java. Ctrl-C conserva os códigos reais;
o logger foi testado com shell/sleep/cat, sem manual/Java:
[validação](manual-logger-validation.json). O tee captura saídas; anotar também
as ações/resultados pessoais. Cada rótulo deve ser novo para não sobrescrever logs.

SHUTDOWN/closing-output é progresso da aplicação. JAVA_EXIT é registado pelo
shell após a JVM terminar; não é anúncio antecipado nem exige nova escrita da
aplicação depois de fechar stdout/stderr.

## Terminal 1 — BOOTSTRAP

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2/Projeto
source /Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-session.zsh
ott_manual_run bootstrap-01 --role BOOTSTRAP --config "$OTT_MANUAL_PROFILES/bootstrap.properties"
```

## Terminal 2 — SERVER

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2/Projeto
source /Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-session.zsh
ott_manual_run server-01 --role SERVER --config "$OTT_MANUAL_PROFILES/N0.properties"
```

## Terminal 3 — RELAY

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2/Projeto
source /Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-session.zsh
ott_manual_run relay-01 --role RELAY --config "$OTT_MANUAL_PROFILES/N1.properties"
```

## Terminal 4 — CLIENT

```zsh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2/Projeto
source /Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-session.zsh
ott_manual_run client-01 --role CLIENT --config "$OTT_MANUAL_PROFILES/N2.properties"
```

## Ações de Francisco

1. Arrancar na ordem dos terminais e confirmar STARTED/READY/papéis. Nos terminais
   2/3/4 escrever neighbors; esperar duas sessões ACTIVE por nó, sem duplicados.
   No bootstrap não esperar membership.
2. No relay escrever catalog, play example, um comando desconhecido e neighbors;
   confirmar indisponibilidade/erro local e CLI ainda utilizável.
3. No bootstrap escrever quit: confirmar bootstrap-01.exit com JAVA_EXIT=0.
   Os três nós devem continuar a responder a neighbors sem bootstrap.
4. No CLIENT escrever quit: confirmar client-01.exit com JAVA_EXIT=0 e eventos
   reason=LEAVE nos sobreviventes, que mantêm PID e continuam utilizáveis.
5. Reiniciar bootstrap e CLIENT com rótulos novos, nos respetivos terminais:

```zsh
# Terminal 1
ott_manual_run bootstrap-02 --role BOOTSTRAP --config "$OTT_MANUAL_PROFILES/bootstrap.properties"
```

```zsh
# Terminal 4, após READY do bootstrap-02
ott_manual_run client-02 --role CLIENT --config "$OTT_MANUAL_PROFILES/N2.properties"
```

6. Confirmar nova instanceId do CLIENT, reconexão e PIDs preservados dos
   sobreviventes. Encerrar SERVER por EOF (Ctrl-D em linha vazia), RELAY por quit,
   CLIENT por Ctrl-C e o bootstrap reiniciado por quit. EOF/quit normais devem dar
   JAVA_EXIT=0; **guardar o código real do sinal, sem o converter para zero**.
   tee pode receber também Ctrl-C; guardar TEE_EXIT e verificar completude do log.
7. Depois de terminar todos, no terminal 1, testar ajuda e erros com captura:

```zsh
ott_manual_run help-01 --help
ott_manual_run invalid-role-01 --role relay --config "$OTT_MANUAL_PROFILES/N1.properties"
ott_manual_run missing-config-01 --role RELAY --config "$OTT_MANUAL_PROFILES/missing.properties"
printf 'node.id=BAD\nnode.role=RELAY\ncontrol.port=999999\n' > "$OTT_MANUAL_PERSONAL_DIR/invalid.properties"
ott_manual_run invalid-profile-01 --role RELAY --config "$OTT_MANUAL_PERSONAL_DIR/invalid.properties"
```

Esperado: ajuda 0 e os três erros 2. Para bind ocupado, no terminal 3 iniciar:

```zsh
ott_manual_run relay-occupied-01 --role RELAY --config "$OTT_MANUAL_PROFILES/N1.properties"
```

Após STARTED, no terminal 1:

```zsh
ott_manual_run bind-error-01 --role RELAY --config "$OTT_MANUAL_PROFILES/N1.properties"
```

Confirmar JAVA_EXIT=3, sem processo preso; no terminal 3 encerrar a instância
relay-occupied-01 com quit e confirmar JAVA_EXIT=0.

## Recolha final e confirmação

No terminal 1, depois de todos terminarem:

```zsh
cat "$OTT_MANUAL_PERSONAL_DIR/"*.exit | tee "$OTT_MANUAL_PERSONAL_DIR/resumo-exits.log"
pgrep -fl 'pt[.]uminho[.]esr[.]ott[.]app[.]OttApplication' > "$OTT_MANUAL_PERSONAL_DIR/processes-after.log"
python3 /Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/020-overlay/manual-port-check.py | tee "$OTT_MANUAL_PERSONAL_DIR/ports-after.log"
```

Guardar observacoes.md nesse diretório: Francisco, versão/hash conferidos,
ações e resultados observados, falhas/passos não executados e cleanup/pendências.
Não copiar contagens CHECK_PASS/MANUAL_PASS dos testes automáticos nem inventar
resultados. Comunicar depois o caminho dos logs e a conclusão pessoal.

**Aguardar execução de Francisco.** Manual humano, aprovação Test, revisão final
de Waheed e integração continuam pendentes; sem gates externos adicionais.
Sem fecho, commits/push ou DONE por efeito desta preparação.
