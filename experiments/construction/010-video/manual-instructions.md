# IMP-016 / 010-video — Manual humano do codec UDP OTT1

Estado: preparado, **não executado/não aprovado**. Um terminal, oito etapas
interativas e24 checks. Não abrir nós, CORE, sockets ou perfis: este incremento é
um codec puro. Nenhuma porta é necessária. Não demonstra emissão/reprodução de vídeo.

## Construção fixada

- JDK21, clean verify1537/0/0/0,22 XML;78 casos U01–U08.
- JAR preservado: `experiments/construction/010-video/test-01/artifacts/ott-streaming-1.0-SNAPSHOT.jar`.
- SHA-256: `588bd8e5f85c880666beab1a2033c8f100f3bd37e79d2d83ed4d15ec322ac3a2`.
- Código aprovado/fixtures fixados em `test-manual-tools/approved-build.json`.
- Auxiliar compilado numa pasta temporária; StreamPacket e MessageCodec têm de
  ser carregados do JAR preservado, nunca de target/classes. A origem é impressa
  e registada. Nenhuma alteração ao código de produção/testes é necessária.

## Comando completo — Francisco

```sh
cd /Users/maria/Documentos/Francisco/ESR/ESR---TP2

shasum -a 256 experiments/construction/010-video/test-01/artifacts/ott-streaming-1.0-SNAPSHOT.jar

python3 -B experiments/construction/010-video/test-manual-tools/run-manual.py \
  --operator Francisco \
  --kind human \
  --java-home /Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
manual_exit=$?
printf 'SHELL_EXIT=%s\n' "$manual_exit"
```

Usar um terminal interativo. Não alimentar Enter por pipe: o modo human recusa
stdin não interativo. Confirmar o hash antes de continuar. Em cada etapa, ler o
objetivo, premir Enter e conferir os três CHECK_PASS/resultados antes de avançar.
Se houver divergência, interromper com Ctrl-C e enviar o log parcial; não repetir
para esconder o negativo. O runner preserva cada execução num diretório novo.

1. Origem das classes no JAR fixado; OTT1/v1 e TCP/v2 separados.
2. Mínimo65 bytes: literal independente, payload aa e metadados.
3. Golden small: seq7,mediaTime10000,payload ABC.
4. Fronteira1137:1136+1, golden bytes e recombinação independente.
5. Limite2MiB:1847 fragmentos, último96, datagramas≤1200 e recombinação.
6. Doze negativos independentes, u32 hostil e overflow de região rejeitados.
7. Região exata com offset17, buffer maior e payload extra rejeitado.
8. Cópias de entrada/saída/buffer de receção e lista imutável.

## Resultado a conferir e comunicar

Esperado, apenas depois de executar pessoalmente:

- 24 CHECK_PASS e8 OPERATOR_CONFIRM_STAGE.
- STREAM_PACKET_ORIGIN igual ao caminho absoluto do JAR acima.
- PROBE_PASS CHECKS=24, CLEANUP_COMPLETE e MANUAL_PASS CHECKS=24.
- JAVA_EXIT=0, PASSED=true, WRAPPER_EXIT=0 e SHELL_EXIT=0.
- REPORT aponta para `test-manual-runs/<execução-nova>/report.json`.

O log pessoal integral é manual.log nesse diretório, com compile.log e report.json.
JAVA_EXIT é o retorno real do processo Java depois de wait; WRAPPER_EXIT é o
retorno escolhido pelo runner; SHELL_EXIT é observado pelo shell. Não inferir um
código que não tenha sido registado. CLEANUP_COMPLETE indica JVM terminada e
pasta temporária removida; o teste não cria outros processos/serviços persistentes.

Enviar o resultado, o caminho de report.json e eventuais divergências. Um retorno
bem-sucedido não aprova Test automaticamente. Manual humano, aprovação de Test,
revisão final de Denji e integração continuam pendentes. Sem gates externos adicionais.
