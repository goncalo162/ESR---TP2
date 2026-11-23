# IMP-016 / 010-video / TASK-019 — Pacote final para autorização

Test aprovado e revisão final Denji confirmada explicitamente pelo utilizador,
sem alterações pendentes. [Revisão e hashes revistos](final-review.json).
Bolt/história/unidade permanecem abertos até integração e fecho autorizados.

## Resumo final

Codec puro UDP OTT1 v1: record imutável,cópias defensivas,encode/decode da região
exata e fragmentação. Header64,datagrama≤1200,payload≤1136,frame≤2MiB,
count≤1847. Validação antes de cópias/alocações dependentes dos dados. Sem sockets
ou emissão de vídeo. TCP v2/19 tipos,caps,fixtures e testes históricos preservados.

Clean verify1537/0/0/0,22 XML;78 casos U01–U08/V-019. Manual humano Francisco24,
oito etapas,sem divergências observadas. Não repetir build/manual: código/JAR intactos.
[JAR/logs/XML](../test-01/README.md), [manual conferido](../manual-verification-01/README.md),
[oráculos Test](../../../../memory-bank/bolts/010-video/test-walkthrough.md).
JAR SHA-256:588bd8e5f85c880666beab1a2033c8f100f3bd37e79d2d83ed4d15ec322ac3a2.
Negativos,logs pessoais e evidências originais incluídos sem alterações.

## Conteúdo proposto

[Allowlist exata](allowlist.txt), [grupos por caminho](allowlist-groups.json),
[manifesto SHA-256](manifest.json), [exclusões/preservação](exclusions.json).
Manifesto lista todos os caminhos da allowlist exceto o seu próprio ficheiro,
para evitar auto-referência; o hash do manifesto é apresentado na resposta final.
A allowlist inclui o próprio manifesto. Nenhum caminho target/ ou demo/ é incluído.

[Diff Java](java.diff), [numstat Java](java.numstat),
[diff código/documentação](code-doc.diff), [numstat código/documentação](code-doc.numstat).
Os arquivos de execução têm inventário/hash no manifesto em vez de despejo XML no diff.
Java contra base b6de848:+491/−0,apenas StreamPacket.java e StreamPacketTest.java.

Patch separado [IMP-016-LOCAL-02](IMP-016-LOCAL-02.patch): só a linha do exemplo JWT
no template DDD,substituindo link fictício por caminho literal. Idêntico ao patch
previamente autorizado; não altera validador,ADR ou implementação. Não é código do codec.
IDs locais mantidos,nenhum Q-C global reservado.

[Estado dos ramos](branch-state.json): main local não avançou,HEAD/main b6de848;
nenhuma divergência atual. Remotos não consultados e trabalho do colega não integrado.
[Simulação/conferência](simulation.json) e [V-ADD na cópia](simulation-v-add.md).
[Plano de commit/integração](integration-plan.md).

Sem staging,commits,push,merge/rebase/checkout/reset ou fecho nesta preparação.
Falta autorização para executar o plano e comprovar integração. Sem gates externos adicionais.
