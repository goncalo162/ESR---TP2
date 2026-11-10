# IMP-004 — roteiro manual reproduzível

Requer JDK 21 e autorização local para sockets TCP loopback. Executar na raiz do
repositório. O driver usa apenas APIs públicas do JAR verificado; não acrescenta
uma CLI ao produto. São três RELAY e um bootstrap embutidos numa JVM de ensaio.
Não demonstra vídeo, GUI, CORE ou processos finais da CLI. Os perfis gerados são
fixtures deste roteiro, não um deployment que cumpra todos os mínimos do projeto.

## Artefacto verificado

JAR preservado em
`experiments/construction/003-overlay/test-artifacts-20261110T161706Z/ott-streaming-1.0-SNAPSHOT.jar`.

SHA-256:
`1d56888b8d3db1ca8d12d45621e14783c45bb7f0b7c8b4a0d0945deb77644d17`.

É o mesmo hash de `Projeto/target/ott-streaming-1.0-SNAPSHOT.jar` após o clean verify
registado. Não é necessário reconstruir para repetir este manual. Se reconstruir,
registar separadamente o novo hash/build; não o atribuir à execução preservada.

```sh
shasum -a 256 experiments/construction/003-overlay/test-artifacts-20261110T161706Z/ott-streaming-1.0-SNAPSHOT.jar
```

## Execução pessoal

Substituir `TEU_NOME` pelo nome real do operador. Escolher sempre um diretório
novo para cada execução; o driver recusa sobrescrever um `manual.log` existente.
O comando abaixo usa a execução por source file de Java 21, sem Maven/JUnit,
sem classes de teste no classpath e sem modificar fontes em Projeto/.

```sh
java --class-path experiments/construction/003-overlay/test-artifacts-20261110T161706Z/ott-streaming-1.0-SNAPSHOT.jar \
  experiments/construction/003-overlay/ManualNeighborSmoke.java \
  experiments/construction/003-overlay/test-artifacts-20261110T161706Z/ott-streaming-1.0-SNAPSHOT.jar \
  experiments/construction/003-overlay/manual-TEU_NOME-execucao-01 \
  TEU_NOME --interactive
```

Depois da saída do Java, guardar o código real no mesmo terminal:

```sh
manual_java_exit=$?
printf 'JAVA_EXIT=%s\n' "$manual_java_exit"
```

Não executar outro comando entre Java e a captura de `$?`. Juntar o código ao
registo pessoal; não editar o log do agente para representar uma execução humana.

## Passos do operador e oráculos

1. Confirmar SHA, JDK, operador, sete portas efémeras e perfis carregados. O driver
   valida o deployment e verifica que os construtores dos managers não fizeram bind.
   No primeiro PRESS_ENTER, iniciar bootstrap e managers pela ordem N2/N0/N1.
2. Ler os três snapshots: grau dois, IDs esperados e uma sessão por vizinho. As
   instâncias confirmadas são não zero e a configuração desejada mantém instâncias
   zero. Observar entrega de ERROR NO_ROUTE/relatedType 255 com a sessão completa,
   sem retirar a sessão; a ausência de ERROR de resposta é observada numa janela
   explícita de 200 ms. HELLO de desconhecido e de ID vivo duplicado recebem as
   rejeições esperadas e EOF, preservando o grafo.
3. No segundo PRESS_ENTER, encerrar bootstrap e provocar a perda do vínculo N0/N1
   com `disconnect` da sessão corrente. Confirmar Connection refused no bootstrap,
   Down da sessão antiga e Up com connectionId novo/mesma instanceId. Comandos com
   a Session antiga devolvem false/CLOSED e preservam a conexão nova. É perda TCP
   induzida pela API local; não é falha CORE ou prova de rede física.
4. No terceiro PRESS_ENTER, encerrar N1 e criar um novo manager com nova instanceId.
   O novo arranque não completa a descoberta nos 200 ms observados sem bootstrap;
   N0/N2 mantêm a sua aresta. Repor bootstrap para a descoberta do processo novo e
   verificar convergência e nova instância, sem permitir que a Session antiga a remova.
5. No quarto PRESS_ENTER, solicitar fecho e verificar `awaitTermination` de todos
   os managers, snapshots vazios, close idempotente e reutilização das quatro portas
   de controlo TCP. Só considerar sucesso com CHECK_PASS em todos os pontos,
   MANUAL_PASS CHECKS=49, CLEANUP_COMPLETE e saída Java 0.

Os eventos são recolhidos num coletor limitado a 256 e apresentados pela thread
principal, sem I/O de logs nos callbacks do manager. Cada espera de convergência
é limitada a oito segundos; os valores são limites do ensaio, não metas medidas
do projeto. Os perfis de runtime, UUID, portas e hashes ficam no diretório da
execução. Jitter é zero neste ensaio; não foi usada seed aleatória de jitter.

## Execução já efetuada

Operador: agente Codex, em modo interativo, avançando os quatro prompts pelo
terminal da ferramenta. [Log original](manual-codex-20261110T161706Z/manual.log):
49 CHECK_PASS, zero CHECK_FAIL, MANUAL_PASS, CLEANUP_COMPLETE, saída real Java 0.
[Resultado e proveniência](test-manual-result.json). Esta execução não é atribuída
a uma pessoa nem substitui revisão humana. Não há execução pessoal humana
registada nesta continuação; usar diretório e log próprios quando a realizar.
