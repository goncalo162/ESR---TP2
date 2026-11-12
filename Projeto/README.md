# ESR — Serviço OTT de entrega de multimédia

TP2 de ESR, Mestrado em Engenharia Informática, Universidade do Minho, 2026/2027.

## Estado

A base das fases 1/2 tem configuração, bootstrap inicial, vizinhos TCP persistentes e CLI. IMP-034 está concluído após Test, revisão técnica e integração local em main: todo o controlo TCP usa exclusivamente v2, com descoberta obrigatória e aprendizagem de candidatos. O mesmo programa/JAR serve BOOTSTRAP/SERVER/RELAY/CLIENT. Não há modo opcional, runtime TCP v1 ou fallback.

IMP-035 tem Test aprovado: quando fica sem vizinhos ACTIVE, usa candidatos aprendidos para formar uma ligação reparadora na mesma sessão TCP v2. A negociação exige DISCOVERY|REPAIR (máscara3). O clean verify atual passou1113 testes; o manual Codex passou68 checks com mortes reais dos relays nos dois grafos e S–R1 preservada. O JAR/hash está no relatório de Test. Luis repetiu o manual com66 checks/MANUAL_PASS, aprovou Test e a versão15efdf2 foi integrada em main local. Fecho e revisão humana final da implementação permanecem pendentes. Routing/streaming/vídeo permanecem nos seus incrementos.

## Compilar e testar

Usar JDK 21 e Maven. Exemplo numa bancada Linux com esse JDK instalado:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -ntp clean verify
sha256sum target/ott-streaming-1.0-SNAPSHOT.jar
```

Os testes TCP/CLI precisam de permissão para sockets locais e criação de JVM. Resultados em target/surefire-reports/. Usar o mesmo JAR/hash em todos os processos; não reutilizar um JAR histórico para validar os fontes v2. O total de testes deve vir da execução atual, sem tomar 149/981 como metas.

Testes selecionados do incremento:

```sh
mvn -B -ntp -Dtest=MessageCodecTest,DiscoveryCodecTest,NeighborDiscoveryTest,CandidateCacheTest,OverlayRecoveryTest test
mvn -B -ntp -Dtest=TransportTest,BootstrapServerTest,NeighborManagerTest,NeighborManagerLifecycleTest,OttApplicationTest test
```

## Arrancar a overlay e verificar a CLI

A partir de Projeto/, executar cada comando num terminal próprio, com JDK 21. Os cinco perfis correspondem ao grafo configurado; SERVER aqui ainda não implica vídeo implementado.

```sh
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role BOOTSTRAP --config src/main/resources/config/bootstrap.properties
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role SERVER --config src/main/resources/config/server-1.properties
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role SERVER --config src/main/resources/config/server-2.properties
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role RELAY --config src/main/resources/config/relay.properties
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role CLIENT --config src/main/resources/config/client.properties
```

Introduzir neighbors em cada nó e conferir as adjacências ACTIVE previstas; READY local por si só não prova todas as ligações. Depois de as estabelecer, quit no bootstrap não deve parar os nós: repetir neighbors e confirmar os sobreviventes. quit/EOF encerram o processo com prazo. A ajuda é uma invocação separada com --help; os comandos interativos implementados são neighbors e quit.

A cache de candidatos está disponível pela API assíncrona OverlayNode.candidates()/NeighborManager.candidates() e pelos testes; não existe comando CLI novo para reparar a overlay. A descoberta só usa sessões ACTIVE atuais. Os cenários S–R1/R2–C e S–R1–R2/R3–C são testados para aprendizagem/retenção, sem criar S–C/R1–C neste IMP.

Não é necessária chave de ativação. extra.discovery.enabled=true é apenas declaração redundante; false/vazio/inválido e Q=1 no overlay são erros agregados antes de recursos. Todo overlay exige control.queue.capacity>=2. Q=1 mantém-se na infraestrutura genérica/bootstrap que o suporte. O preflight conjunto rejeita capacidade de ligações inferior ao grau conhecido. Bootstrap só consulta o grafo inicial, sem membership ou consultas periódicas após sucesso.

## Contrato e evidência

- [Protocolo local](docs/protocolos.md) e [interfaces completas](../docs/07-API-AND-INTERFACES.md).
- [Design e revisão do incremento](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md).
- [Walkthrough e resultados reais](../memory-bank/bolts/034-overlay-extra/implementation-walkthrough.md).
- [Fixtures operacionais](src/test/resources/protocol/README.md): 50 TCP v2 independentes e 16 UDP OTT1 preservadas.
- [Limpeza autorizada TCP v1](../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md#limpeza-autorizada): retirada dos arquivos antigos, sem decoder legado.
- [Progresso](../docs/15-PROJECT-PROGRESS.md) e [roteiro do incremento](../experiments/overlay-discovery/README.md).

UDP de vídeo conserva OTT1/version 1, cabeçalho 64, total <=1200 e payload <=1136. Testes locais não são demonstração CORE, GUI, FPS ou vídeo real.

## Reparação após isolamento — checkpoint Test

Usar `neighbors` para sessões confirmadas e `recovery` para estado, motivo,
candidato selecionado e atraso de retry. O evento `RepairStarted` identifica
requestId/instância/sourceKey/geração/queryStarted/TTL da seleção. Não há nova
consulta ao bootstrap depois do arranque. Um destino saudável pode aceitar o
isolado, sem perder sessões ACTIVE. Ausência/expiração/conflito dos candidatos
mantém isolamento explícito e tentativas limitadas.

O [roteiro de mortes reais](../experiments/overlay-recovery/README.md) foi
executado pelo agente com JVMs separadas e SIGKILL, e foi repetido por Luis com Test aprovado.
A evidência incremental está no
[walkthrough](../memory-bank/bolts/035-overlay-extra/implementation-walkthrough.md).
