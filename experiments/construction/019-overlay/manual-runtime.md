# IMP-002 — Roteiro manual reproduzível

Executar depois de clean verify com JDK 21, na raiz do repositório. O launcher
Java source-file usa o JAR produzido; este ficheiro experimental não pertence
ao código de produção, não entra no Maven/JUnit nem é incluído no JAR.
Não precisa de dependências adicionais ou de permissões de sockets.

O roteiro carrega os cinco perfis reais e verifica 30 defaults em cada um,
papéis/streams e os 30 overrides num SERVER temporário. Confere quatro sentinelas
no record Transport.Options, o construtor antigo e cópias imutáveis. Rejeita um
CLIENT com cinco erros independentes agregados, expoente em alpha e override
overlay em BOOTSTRAP igual ao default. Não faz new Transport, bind/dial ou DNS.
Cria apenas cópias temporárias em /tmp, remove-as em finally e confere os hashes
dos perfis originais. Não valida vídeo, GUI, CORE ou composição dos serviços futuros.

## Repetição pessoal pelo utilizador

O último argumento regista o operador real; o exemplo usa Luis. A partir da raiz:

```sh
cd /home/luis/ESR/ESR---TP2
manual_log=$(mktemp /tmp/ott-imp002-manual.XXXXXX.log)
set -o pipefail
/usr/lib/jvm/java-21-openjdk-amd64/bin/java --class-path Projeto/target/ott-streaming-1.0-SNAPSHOT.jar experiments/construction/019-overlay/ManualRuntimeCheck.java Projeto/src/main/resources/config Luis | tee "$manual_log"
printf '%s\n' "$manual_log"
```

Resultado esperado: código 0, **25 linhas CHECK_PASS**, uma linha final
MANUAL_PASS com operador/contagem/instante e nenhuma MANUAL_FAIL. Os cinco
EXPECTED_PROBLEM são rejeições esperadas, não falha do roteiro. Defaults e
overrides são impressos para inspeção, assim como os valores de Transport.Options.
O log preserva o resultado real; se falhar, não declarar aprovação.

Se o JAR já não existir, primeiro, em Projeto/:

```sh
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -ntp clean verify
```

Esse comando inclui TCP loopback real e exige que o ambiente o permita. Não
alterar fontes/perfis para contornar falhas; uma reconstrução pode ter outro
hash de JAR por metadados de empacotamento, que deve ser registado separadamente.
Para repetir sobre o artefacto desta validação, conferir o hash antes:

```sh
sha256sum Projeto/target/ott-streaming-1.0-SNAPSHOT.jar
```

SHA-256 do JAR testado nesta etapa:
`b13a0bd6c928badfb0fd37cdb44d2e9699f683120b096490b60eed1a4ad43760`.

## Execução já realizada e limite da aprovação

O agente executou [ManualRuntimeCheck.java](ManualRuntimeCheck.java) em
2026-11-03T22:58:30Z–22:58:31Z: código 0, 25 CHECK_PASS e MANUAL_PASS,
operador **Codex**. [Log](test-manual.log) e [comando/hashes](test-manual-command.json).
Isto não é prova de repetição pessoal por Luis ou de revisão humana independente.

Antes de aprovar Test, o utilizador deve repetir pessoalmente o roteiro e indicar
o resultado/log. Essa repetição, a designação de revisor humano distinto,
revisão final e integração continuam pendentes; não há fecho automático do bolt.
