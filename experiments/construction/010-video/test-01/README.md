# IMP-016 / 010-video — Checkpoint Test

Implement aprovado,Test autorizado; Test/manual humano por aprovar.

## Construção atual

Em Projeto/, com JAVA_HOME=/Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home:

`mvn -B -ntp clean verify`

Exit0, **1537/0/0/0,22 XML**, incluindo78 casos U01–U08/V-019.
Execução com sockets locais permitidos para a regressão histórica.
[Log](clean-verify.log), [resultado/inventário/oráculos](clean-verify-result.json),
[ambiente](environment.txt). XML e relatórios texto em xml/.
Os1459 históricos passaram nesta execução; contagens antigas não foram reutilizadas.

[JAR preservado](artifacts/ott-streaming-1.0-SNAPSHOT.jar),
[SHA-256](artifacts/ott-streaming-1.0-SNAPSHOT.jar.sha256),
[inventário de classes](jar-info.json).
SHA-256:588bd8e5f85c880666beab1a2033c8f100f3bd37e79d2d83ed4d15ec322ac3a2.
StreamPacket.class major65 coincide com a saída de clean verify por hash.
Origem em execução será confirmada pelo manual, não presumida aqui.

## V-019 / independência

U01:9,U02:12,U03:4,U04:11,U05:1,U06:26,U07:11,U08:4 (total78).
Golden bin/hex independentes e literais,offsets/caps,1..2MiB,1137/múltiplos,
recombinação,regiões exatas,metadados inválidos e imutabilidade.
Sem enfraquecer testes históricos ou alterar fixtures; sem vídeo/sockets de streaming.
Revisão da ordem de validação em ../implement-behavior-01/review.md.
Não se alega medição de heap,vídeo,GUI ou CORE.

## Manual humano

[Instruções completas](../manual-instructions.md). Um terminal,8 etapas interativas,
24 checks, sobre o JAR acima. [Configuração fixada](../test-manual-tools/approved-build.json).
[Sintaxe/compilação do auxiliar](manual-tools-check.json),
[log de compilação](manual-probe-compile.log).
Auxiliar/manual **não executados**; nenhum resultado humano ou JVM de manual presumido.
Não existe ainda report humano para esta versão.

## Preservação e revisão

[Código/fixtures/históricos/demo/negativos preservados](preservation.json).
[V-ADD](v-add.md), validador inalterado. IDs locais; LOCAL-02 resolvida no Plan.
[Diff Java](java.diff), [numstat Java](java.numstat), [diff código/documentação](cumulative.diff),
[numstat código/documentação](cumulative.numstat). Inventário de evidência/hashes
em manifest.json; target/ separado em target.numstat,fora da entrega.
Manifesto é snapshot de checkpoint,não autorização de integração.

Ramo imp-016-udp,main/HEAD b6de848 preservados. Test por aprovar;
manual humano,revisão final Denji e integração pendentes. Sem staging/commits/push/fecho
ou gates externos adicionais. Bolt/história/unidade permanecem em progresso.
