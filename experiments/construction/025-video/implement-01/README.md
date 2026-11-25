# IMP-018 / 025-video — Evidência de Implement

Checkpoint 2026-11-25T16:29:54Z. ADR/A05/A06 aprovados; **Implement pendente de
aceitação**. Sem avanço a Test, fecho, commits/push ou outro IMP.
[Walkthrough](../../../../memory-bank/bolts/025-video/implementation-walkthrough.md).

## Resultados e reprodução

JDK fixado em `/usr/lib/jvm/java-21-openjdk-amd64` (21.0.13.1+2), Maven offline.
Em `Projeto/`, com JAVA_HOME e PATH a apontar para esse JDK:

```sh
mvn -o -B -ntp -Dtest=VideoSourceTest -DargLine=-Djava.awt.headless=true test
mvn -o -B -ntp -DargLine=-Djava.awt.headless=true test
```

A regressão requer permissão para TCP local real; foi executada fora da
sandbox após autorização. Não é clean verify nem aceitação da etapa Test.

| Execução | Resultado observado | Evidência |
|---|---|---|
| focused-01 | FAIL de compilação, dois erros de inferência Class em assertThrows; nenhum teste executado | [log](focused-01/maven.log), [comando/hashes](focused-01/result.json), [fonte](focused-01/source-snapshot.txt), [teste](focused-01/test-snapshot.txt) |
| focused-02 | 42/0/0/0 após corrigir declarações de tipo, sem alterar expectativas | [log](focused-02/maven.log), [resultado](focused-02/result.json), XML separado |
| focused-03 | 45/0/0/0; cobertura acrescentada para consultas checked, cleanup e dimensões aprovadas | [log](focused-03/maven.log), [resultado](focused-03/result.json), XML separado |
| regression-01 | 1582/0/0/0,23 XML,41.942s;1537 históricos+45 novos | [log](regression-01/maven.log), [resultado/ambiente](regression-01/result.json), [XML](regression-01/surefire-reports) |

Negativo de compilação não apagado nem substituído por verde. Rejeições de
inputs/providers defeituosos permanecem como casos negativos esperados nos
testes, com controlos positivos. Nenhum teste histórico alterado/removido.
M01–M14 exercitados; V-020-B consolidado e aprovação Test continuam pendentes.

## Fixtures, media e limites

[Proveniência](fixture-provenance.json): cinco JPEGs literais independentes,
gerados uma vez com Pillow9.0.1 já instalado. Testes não executam Pillow nem
acrescentam dependência; SHA-256 conferido em JUnit. Campos esperados de
índice/offset/payload vêm de fixtures, não de outra execução do VideoSource.
1032 cortes de corpo, oito prefixos parciais e50 casos de byte inválido ficam
agrupados nos métodos JUnit; não são métodos adicionais à contagem45.

Os testes de estrutura/caps com reader controlado demonstram fronteiras,
ordem de admissão e orçamento; não demonstram entropia JPEG válida. Fixtures
progressiva/COM/dimensões384×288 e640×360 também passam no JPEG reader real.
16MiB limitam o raster destino, não heap/nativo total do provider ou JVM.

[Disponibilidade dos quatro conteúdos](media-availability.json): apenas
ponteiros LFS nos paths autorizados, OIDs coincidem com os hashes aprovados.
Nenhum fetch/materialização/cópia/conversão/instalação. A leitura dos22687
JPEGs completos pelo novo VideoSource não foi executada. Auditoria histórica
de IMP-017 não é resultado deste leitor. R17 inicial permanece com causa
não estabelecida; os testes com métricas controladas não a demonstram.

## Preservação e pendências

[Entrada](entry-state.json), [âmbito/hashes](scope-check.json):257 ficheiros
protegidos; só mudou o placeholder VideoSource.java e foi criado o teste
aprovado. Restantes fontes, configuração/protocolo, POM, recursos e scripts
inalterados.18 paths removidos da demo continuam ausentes. HEAD inalterado.
Listas geradas pelo compilador, limpas à entrada, foram guardadas e repostas
byte a byte de HEAD; [registo](generated-build-metadata.json). Sem operação
no index, commit ou push. Nenhum histórico/evidência anterior foi apagado.

[V-ADD atual](v-add.log): saída1, exatamente os mesmos seis erros da revisão
ADR, sem erros adicionais. Continuam pendentes antes do fecho; validação não
aprovada, demo não restaurada, templates/validador inalterados. Manual humano aplicável, regressão
JDK21 de aceitação, revisão final Denji e integração mantêm-se obrigatórios.
TASK-020 só fecha após A/B/C; sem gates externos adicionais.

[Resumo verificável](verification.json) e [conferência de âmbito](scope-check.json).
Hashes dos artefactos em `artifact-sha256.json` (não inclui a si próprio).
