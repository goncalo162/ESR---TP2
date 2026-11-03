# IMP-001 — Checkpoint 2: interfaces e testes existentes

Inspeção estática preparada em 2026-11-03T21:11:16Z.
Estado: checkpoint 2 aprovado explicitamente pelo utilizador, com registo em
2026-11-03T21:16:53Z. Nenhuma suite, build, validação V-ADD ou teste manual
executados nesse checkpoint. As pendências/estados abaixo retratam a revisão
estática; a execução posterior está no [relatório do checkpoint 4](test-walkthrough.md),
com resultados aprovados pelo utilizador no checkpoint 4. O
[checkpoint 5](closure-walkthrough.md) foi aprovado e o fecho aplicado depois da
integração em main, em 2026-11-03T21:56:48Z. Os estados abaixo são históricos.

## Autorização e posição no bolt

O utilizador aprovou explicitamente o checkpoint 1 e a adaptação Q-C-01 apenas
para IMP-001. Autorizou conferir interfaces e mapear os testes existentes,
apresentar qualquer lacuna real antes de alterar fundações/testes e parar no
checkpoint 2. Regressão com log novo e teste manual continuam obrigatórios
na validação. A autorização foi registada em 2026-11-03T21:08:48Z.

| Etapa Simple | Checkpoint do utilizador | Estado |
|---|---|---|
| Plan | 1 — estado e plano | Concluído e aprovado |
| Implement | 2 — interfaces e mapeamento | Inspeção aprovada pelo utilizador |
| Implement | 3 — entregáveis e diff | Preparação autorizada; aguardar validação antes dos testes |
| Test | 4 — regressão e teste manual | Ainda não executado |
| Fecho do bolt | 5 — estados, revisão e integração | Ainda não executado |

A etapa implement não está concluída pela aprovação isolada deste mapeamento.
Fonte do incremento: [ADD/IMP-001](../../../add.md#imp-001).

## Interfaces e modelos conferidos

| Componente | API/modelo existente | Contrato observado |
|---|---|---|
| ConfigurationLoader | load(Path, NodeRole): NodeConfiguration; loadDeployment(Collection<Path>): List<NodeConfiguration>; ambas lançam ConfigurationException | Carga de propriedades/ficheiros e validação agregada; sem DNS, sockets ou leitura de bytes de media |
| NodeConfiguration | Record com sourceFile, nodeId, role, bindHost, advertisedHost, controlPort, dataPort, bootstrap, topologyFile, streamsFile, mediaBase, topology e streams; BootstrapContact(host, port) | Campos opcionais explícitos; cópia defensiva de streams; dataPort zero apenas no perfil BOOTSTRAP validado |
| TopologyConfiguration | Record (sourceFile, nodes); Node(nodeId, host, controlPort, dataPort, role, neighbors) | Cópias de mapa e conjuntos não modificáveis; o loader valida grafo, papéis e endpoints |
| StreamConfiguration | Record (sourceFile, streamId, serverId, title, mediaFile, fps, loop) | Descritor local, sem ler vídeo; validação de domínio feita pelo loader |
| ConfigurationException/Problem | problems(): List<ConfigurationProblem>; record (file, key, message) | Erros agregados com origem/propriedade e lista defensiva; não devolver configuração parcial |
| NodeRole | BOOTSTRAP, SERVER, RELAY, CLIENT | Papéis de configuração; códigos de Endpoint no fio são explícitos no codec |
| ControlMessage | Record (version, type, requestId, senderId, senderInstance, payload); construtor de versão 1 | Envelope existente; payload selado e tipado |
| Payloads e descritores | Endpoint, StreamInfo, EndpointPayload, NeighborList, Leave, Probe, Announcement, Withdrawal, Activation, ActivationAck, Deactivation, Trace, TraceResult, ErrorPayload; Empty enum | NeighborList, Announcement, Activation, Trace e TraceResult copiam as listas; NeighborList não contém lease |
| MessageType/ErrorCode | code(); fromCode(int) | 16 tipos ativos 1, 2 e 4–17; código 3 rejeitado; erros explícitos 1–12, sem ordinal no fio |
| MessageCodec | encode(ControlMessage): byte[]; decode(byte[]): ControlMessage; write(ControlMessage, OutputStream): void; read(InputStream): Optional<ControlMessage> | Framing 37..65536 antes de alocar; UTF-8 estrito; EOF/truncatura/formato distintos; não abre sockets nem fecha/flush streams do chamador |
| ProtocolException | IOException com errorCode() | Classifica erro de formato sem enviar resposta ou decidir política de ligação |
| Transport | Transport(InetSocketAddress, Listener[, Options]); localAddress(); connect(InetSocketAddress): CompletableFuture<Connection>; connectionCount(); close(); awaitTermination(Duration): boolean | Dial assíncrono, endpoints resolvidos, reservas incluindo dials, sockets TCP reais, terminação com prazo |
| Transport.Options | Record (queueCapacity, maxConnections, connectTimeout, shutdownTimeout); defaults() | Filas 1..256, conexões 1..64; defaults 256/64 e 2 s/3 s; durações positivas em ms limitadas a int |
| Transport.Listener | connectionOpen(Connection); message(Connection, ControlMessage); connectionClosed(Connection, CloseReason) | Callbacks sequenciais por ligação no leitor; ligações distintas podem chamar em paralelo; publicar trabalho sem bloquear |
| Connection | id(): UUID; remoteAddress(): InetSocketAddress; isOpen(): boolean; queuedMessages(): int; send(ControlMessage): SendResult; close() | send=ACCEPTED confirma enfileiramento; escritor exclusivo; fecho limpa fila e liberta reserva após ambos os workers |
| Connection enums | SendResult: ACCEPTED/CLOSED/OVERFLOW/INVALID_MESSAGE; CloseReason: LOCAL_CLOSE/SHUTDOWN/PEER_EOF/TRUNCATED_FRAME/PROTOCOL_ERROR/IO_ERROR/OUTPUT_OVERFLOW/CALLBACK_ERROR | Overflow localizado e razões de fecho observáveis |

Implementações consultadas: [configuração](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/ConfigurationLoader.java),
[records de configuração](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/NodeConfiguration.java),
[grafo](../../../Projeto/src/main/java/pt/uminho/esr/ott/config/TopologyConfiguration.java),
[mensagens](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/ControlMessage.java),
[codec](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/MessageCodec.java),
[tipos](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/MessageType.java),
[transporte](../../../Projeto/src/main/java/pt/uminho/esr/ott/transport/Transport.java) e
[ligação](../../../Projeto/src/main/java/pt/uminho/esr/ott/transport/Connection.java).
As assinaturas concordam com o âmbito já disponível em docs/07; esta inspeção
não demonstra execução bem-sucedida ou cobertura exaustiva.

## Correspondência entre oráculos e verificações

| Oráculo | Evidência existente identificada por leitura | O que fica por executar |
|---|---|---|
| V-001 | Projeto/pom.xml: release=21, JUnit Jupiter 5.13.4 apenas test, Surefire 3.5.4, failIfNoTests=true; plugins fixados | POM efetivo/provider realmente resolvido e build limpo com JDK 21 |
| V-002 | Manifesto e README de fixtures: 45 casos declarados, 29 TCP e 16 UDP, binário/hex/SHA-256 e interpretação de offsets; notas de acesso ao recurso docente e correção ADR-013 | Recalcular os hashes e correspondência hex/bin; conferir limites/offsets, manter a distinção entre acesso histórico ao recurso e nova obtenção |
| V-003 | ConfigurationLoaderTest: métodos abaixo, com @TempDir, exemplos reais e assertThrows para entradas inválidas | Descoberta e execução de ConfigurationLoaderTest no novo build; a contagem de 65 é histórica |
| V-004 | MessageCodecTest: bytes independentes para 16 tipos mais variante UTF-8, 12 fixtures TCP inválidas e testes de fronteira/contexto abaixo | Execução nova da classe; a contagem de 66 é histórica |
| V-005 | TransportTest: 18 métodos @Test visíveis, sockets loopback/portas efémeras, deadlines e verificação de terminação | Execução nova da classe; 18 métodos identificados não são 18 testes agora aprovados |
| V-ADD | scripts/validate-planning.py: secções/requisitos/aliases, ADR/matriz, IMP/histórias/bolts, etapas/dependências, fixtures/XML e referências | Executar o validador na fase de validação; não substituir testes Java, revisão humana ou CORE |

Testes consultados: [ConfigurationLoaderTest](../../../Projeto/src/test/java/pt/uminho/esr/ott/config/ConfigurationLoaderTest.java),
[MessageCodecTest](../../../Projeto/src/test/java/pt/uminho/esr/ott/protocol/MessageCodecTest.java) e
[TransportTest](../../../Projeto/src/test/java/pt/uminho/esr/ott/transport/TransportTest.java).
POM: [Projeto/pom.xml](../../../Projeto/pom.xml).
Referências documentais: [manifesto de fixtures](../../../Projeto/src/test/resources/protocol/manifest.json),
[interpretação das fixtures](../../../Projeto/src/test/resources/protocol/README.md),
[acesso/formato histórico](../../../experiments/task-002-contract-review.md),
[correção do bootstrap](../../../experiments/bootstrap-discovery-correction.md) e
[validador V-ADD](../../../scripts/validate-planning.py).

## V-003 — Casos localizados em ConfigurationLoaderTest

| Parte do oráculo | Métodos existentes |
|---|---|
| Cinco perfis, duas origens, quatro UUID e ownership | loadsFiveRealProfilesAndFourDistinctOwnedStreams; deploymentRejectsUuidOwnedByTwoServers |
| Imutabilidade e cópias | collectionsAreImmutable; loadsFiveRealProfilesAndFourDistinctOwnedStreams; aggregatesMissingFieldsWithFileAndProperty |
| Paths fora do cwd e ID de um carácter | resolvesPathsAgainstConfigurationNotWorkingDirectory; acceptsOneCharacterIdAndUnresolvedHostnameWithoutNetwork |
| Campos/ficheiros ausentes e erros agregados | aggregatesMissingFieldsWithFileAndProperty; missingFileIsAnExplicitConfigurationProblem; missingTopologyFileDoesNotReturnPartialNode |
| Chaves duplicadas/inaplicáveis e UTF-8 | rejectsDuplicateDecodedProperties; rejectsUnknownAndRoleInapplicableKeys; rejectsInvalidUtf8AndMalformedUnicodeEscapes; acceptsPropertyContinuationAndUtf8Titles |
| Papéis, IDs, portas e endereços | rejectsExpectedRoleMismatch; rejectsInvalidRoles; rejectsInvalidIds; rejectsInvalidPorts; rejectsUnusableAdvertisedHosts; permitsWildcardBindAndLiteralIpv6WithoutLookup |
| Grafo e unicidade de endpoints | rejectsMalformedNodeLists; rejectsSelfUnknownAndAsymmetricNeighbors; rejectsDuplicateNeighborAndUnknownTopologyKey; rejectsDuplicateEndpointsWithinSameTransport; allowsSameNumericPortForTcpAndUdp; rejectsBootstrapAsForwardingNode |
| Catálogos, títulos, UUID, FPS, loop e media.base | rejectsNonCanonicalUuid; rejectsRepeatedUuid; rejectsServerWithOnlyOneStream; rejectsInvalidFps; rejectsNonBooleanLoop; enforcesTitleUnicodeLimitsAndUtf8; rejectsAliasMediaAndEscapeOutsideBase; rejectsMissingFieldsAndUnknownCatalogKeys |
| Deployment coerente e limites | deploymentRejectsRoleAndEndpointsDifferentFromTopology; deploymentRejectsWrongBootstrapContact; deploymentRejectsMissingAndDuplicateProfiles; deploymentDetectsCollisionWithBootstrapPort; rejectsOversizedNodeAndStreamLists; deploymentRejectsNodeAbsentFromAllowedGraph |

Não há transporte/relógio falso necessário para executar o loader: a API é
offline. Os @TempDir e ficheiros sintéticos pertencem aos testes existentes.
Quatro streams configuradas não comprovam quatro conteúdos JPEG reais.

## V-004 — Casos localizados em MessageCodecTest

| Parte do oráculo | Métodos existentes |
|---|---|
| Golden bytes e todos os tipos | goldenVectors; matchesIndependentFixturesBothDirections; allMessageNumbersHaveIndependentVectors |
| Leituras parciais, concatenação e EOF | readsOneByteAtATimeWithoutDependingOnAvailable; consecutiveMessagesRemainSeparate; eofAtBoundaryDiffersFromEveryTruncatedFrame |
| Prefixos/limites antes de alocação e corpo incompleto | rejectsMalformedTcpFixtures; unsignedOversizedLengthRejectedBeforeBodyRead; fullLengthBoundsAndIncompleteFields |
| u32/UUID/UTF-8 e limites de listas/IDs | unsigned32LeasesAndUuidBitsRoundTrip; rejectsLeaseOutsideUnsigned32Domain; strictUtf8AndCodePointLimits; validatesIdsAndMaximumNeighborCount; validatesPathsButDefersRoutingContext |
| Instância/papéis e saída inválida | endpointInstanceAndRolesFollowWireContract; invalidModelsDoNotEmitPartialFrames; callerOwnsStreamLifetimeAndFlush |
| Erros, cópias e contrato corrigido de descoberta | errorCodesAndUnknownRelatedTypeRoundTrip; payloadListsAreDefensivelyCopied; bootstrapReplyContainsOnlyConfiguredNeighbors |
| Integração configuração/codec | phaseOneConfigurationCanProduceAndRecoverControlFrames |

goldenVectors contém 17 vetores aceites para 16 tipos ativos: o vetor adicional
é a variante UTF-8 de ANNOUNCE. O manifesto declara 17 TCP aceites e 12 rejeitados.
Código 3, lease antigo em REGISTER_OK e instância viva no endpoint da resposta
são explicitamente rejeitados nos testes atuais. Esta inspeção não executa o codec.

## V-005 — Casos localizados em TransportTest

| Parte do oráculo | Métodos existentes |
|---|---|
| Full-duplex e concorrência | concurrentSendersPreserveFullDuplexFramesAndPerSenderOrder |
| Framing no TCP real | readsIndependentFixtureByteByByteThenConcatenatedFrames; truncatedPeerClosesOnceAndListenerAcceptsReplacement; truncatedBodyAndOversizedPrefixAreIsolated |
| Bind/dial, admissão e cancelamento | occupiedPortAndRefusedDialDoNotLeaveWorkers; boundedConnectionAdmissionAndCancellation; inboundAdmissionRejectsExcessPeerWithoutKillingListener |
| Peer lento, overflow e isolamento | slowPeerOverflowClosesOnlySaturatedConnection; invalidOutboundMessageDoesNotCloseHealthyConnection |
| Shutdown e reconexão | shutdownReleasesPartialReaderIdleWriterAndAcceptPort; simultaneousLocalCloseEmitsOneEventAndReconnectGetsNewId; shutdownWithQueuedWritesToNonReadingPeerReclaimsBothWorkers; shutdownRacingOutboundDialsCompletesAllFuturesAndReleasesReservations |
| Falhas de callbacks e prazo de terminação | failingCallbackIsLocalizedAndShutdownWorksFromCallback; failingOpenAndClosedCallbacksDoNotRetainConnectionSlots; blockedCallbackMakesShutdownTimeoutExplicitAndCanThenTerminate; simultaneousShutdownFromTwoCallbacksDoesNotJoinEachOther |
| Limites e endpoints resolvidos | validatesLimitsAndRejectsUnresolvedEndpoints |

A classe usa @Timeout(15), esperas limitadas e portas efémeras. Alguns callbacks
deliberadamente bloqueados testam a deteção do incumprimento do contrato; não
são recomendação para handlers reais. TCP real é necessário ao V-005, pelo
que substituí-lo por um transporte falso eliminaria parte do próprio oráculo.
Relógios falsos de monitorização/árvores pertencem aos IMP correspondentes.

## V-ADD — Verificações localizadas no script

- Requisitos/aliases/critérios: linhas 93–118; ADR/matriz/IMP e parâmetros:
  120–177. Essas verificações leem ADD e os índices derivados.
- Unidades, histórias e correspondência única: 185–215; campos/etapas,
  identidade/critérios de bolt: 216–263; DAG, gates e bloqueios: 264–281.
- Fixtures binário/hex/hash e parsing XML: 284–294; referências/âncoras:
  295–326; snapshot opcional: 328–332.
- O snapshot identifica as fontes; não é aprovação nem restauração de estado
  antigo após uma mudança legítima. Parsing de XML não comprova sessão CORE.

## Lacunas e trabalho corretamente fora deste IMP

Não foi identificada por leitura uma lacuna de interface/caso exigido que
justifique alterar as fundações ou os testes. Permanecem pendentes de prova
o build/POM efetivo, execução JUnit, integridade/offsets das fixtures, V-ADD
e o teste manual. Pendência de execução não é teste aprovado.

RuntimeConfiguration e NodeConfiguration.runtime() ainda não existem: são
trabalho de IMP-002/TASK-032. Connection.sendAndFlush ainda não existe: pertence
a IMP-003/TASK-006. O bootstrap é comentário, a CLI não tem main e as classes
de teste BootstrapServerTest/NeighborManagerTest/RoutingServiceTest/StreamForwarderTest
são vazias. Estes são serviços/testes futuros; não comprovam funcionalidade
nem impedem verificar a base existente. Não se propõe nova ADR ou alterar testes.

## Teste manual a executar na validação

Roteiro proposto, ainda não executado, após o novo clean verify:

1. Usar JDK 21/JShell com classpath target/classes, a partir de Projeto/, e
   guardar comandos/saída em experiments/construction/001-base-validada/.
2. Carregar os cinco perfis reais com loadDeployment. Conferir cinco nós,
   duas origens, quatro streams/UUID e vizinhanças, sem abrir sockets.
3. Ler uma fixture TCP independente e confirmar os campos e a equivalência
   dos bytes reencodificados, sem alterar o ficheiro esperado.
4. Criar dois Transport com loopback e portas efémeras. Usar callbacks que
   só publicam em filas limitadas, estabelecer a ligação e trocar PROBE_PING
   e PROBE_PONG. Conferir mensagens/requestId/probeId efetivamente recebidos;
   ACCEPTED isoladamente não comprova receção. Todas as esperas têm prazo.
5. Fechar os dois transportes em finally, confirmar awaitTermination e reservas
   libertadas; tentar reutilizar a porta após fecho e encerrar o JShell.
6. Guardar resultados e limites reais. É um smoke manual das APIs disponíveis,
   não uma demonstração de handshake, cálculo de RTT, CLI, vídeo ou CORE.

A regressão completa terá log novo; o total de 149 mantém-se histórico até à
nova execução. Fechar o bolt só após critérios, verificações, revisão humana
e integração exigidas. O checkpoint 2 foi aprovado; parar no checkpoint 3 antes
de executar a validação ou o teste manual.
