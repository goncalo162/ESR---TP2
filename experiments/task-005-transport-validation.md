# V-005 — Transporte TCP full-duplex

Data: 27-10-2026. Revisão de partida: `ebb825b` (árvore limpa). TASK-005 DONE; PHASE-02 IN PROGRESS. Implementados Transport/Connection e os testes de transporte TCP, respeitando o contrato de bootstrap exclusivamente para descoberta (ADR-013).

## Ambiente e execução

macOS 14.6.1 x86_64; Eclipse Temurin 21.0.13+9; Apache Maven 3.9.9; JUnit Jupiter 5.13.4/Surefire 3.5.4 existentes. Sem dependências novas.

```sh
export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$HOME/.local/apache-maven-3.9.9/bin:$PATH"
cd Projeto
mvn -B -ntp clean verify
```

Executado com permissões alargadas para bind/TCP loopback. [Log integral desta execução](task-005-build.log).

```text
TransportTest:            Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
ConfigurationLoaderTest: Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
MessageCodecTest:        Tests run: 66, Failures: 0, Errors: 0, Skipped: 0
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 7.244 s
Finished at: 2026-10-27T14:53:33Z
```

## Oráculos executados

[TransportTest.java](../Projeto/src/test/java/pt/uminho/esr/ott/transport/TransportTest.java) usa TCP real em loopback, portas efémeras, filas de eventos, futures/latches e deadlines. Sem sleeps arbitrários; a libertação de slots é observada por polling com deadline e espera curta.

| Caso | Evidência verificada |
|---|---|
| Quatro produtores, dois por sentido | 160 mensagens intactas, sem perda/duplicação, ordem por produtor e UUID correto |
| Fixture independente | `tcp-probe-ping.bin` byte a byte e dois frames concatenados produzem três mensagens intactas |
| Prefixo truncado/reconexão | TRUNCATED_FRAME único; listener aceita novo UUID; EOF limpo distinto |
| Corpo truncado/prefixo excessivo | TRUNCATED_FRAME/PROTOCOL_ERROR sem mensagem entregue nem morte do listener |
| Porta ocupada/dial recusado | Erro controlado, sem eventos de ligação fictícia; workers terminam; dial após shutdown falha |
| Admissão outbound/cancelamento | Limite de uma reserva rejeita dial adicional; fecho liberta slot; cancelamento seguro |
| Peer sem leitura/overflow | Fila limitada a uma mensagem; OUTPUT_OVERFLOW só no peer saturado; ligação saudável entrega mensagem |
| Shutdown em accept/leitura parcial | Reader, writer ocioso e listener terminam; porta reutilizável |
| Fecho concorrente/reconexão | Evento único por ligação; novo UUID e mensagem entregue após novo dial explícito |
| Saída inválida | INVALID_MESSAGE sem fechar ligação; envio válido seguinte funciona |
| Falha de callback/shutdown no callback | Falha localizada, terminação externa confirmada sem auto-join |
| Admissão inbound | Peer excedente recebe EOF sem open; listener aceita após libertar slot |
| Shutdown com escrita pendente | Peer não lê; 200 mensagens grandes aceites; fila pendente limpa e workers/sockets libertados |
| Shutdown concorrente com dials | 32 tentativas, cancelamento solicitado em metade; futures terminais, reservas zero e open/closed correspondentes |
| Opções/endpoints | Rejeição de capacidades zero/excessivas, timeout zero e endpoint não resolvido |

Os testes chamam close/awaitTermination para confirmar workers terminados; verificam reservas zero nos cenários de escrita/dial e rebind do listener. A mensagem grande usa NeighborList atual sem lease, com UUID dos endpoints a zero; não repõe RegistrationResult/REGISTER_RENEW retirados.

## Implementação e decisões

- [Transport](../Projeto/src/main/java/pt/uminho/esr/ott/transport/Transport.java): listener, dial assíncrono com timeout, reservas, cancelamento e shutdown.
- [Connection](../Projeto/src/main/java/pt/uminho/esr/ott/transport/Connection.java): UUID por conexão, codec existente, leitor/escritor únicos, fila, resultados de send e evento terminal único.
- Defaults: 64 reservas incluindo dials, 256 frames por saída, connect 2 s e deadline externo de shutdown 3 s; opções permitem limites menores. Frame máximo de 65540 bytes com prefixo. Pode existir um frame em escrita e serializações dos chamadores; não foi medido pico de memória.
- Threads virtuais com número limitado por admissão; sem executor de fila ilimitada. Slots mantidos até ambos os workers I/O saírem.
- Callbacks e continuations síncronas de connect devem retornar rapidamente. Close em callback só inicia shutdown para evitar joins circulares; confirmar terminação externamente. Timeout de shutdown é erro operacional explícito.
- send ACCEPTED significa enfileiramento, não entrega/ACK. Close imediato descarta fila; confirmar escrita completa antes de fechar resposta na futura TASK-006. Nenhum handler bootstrap foi implementado aqui.
- [API](../docs/07-API-AND-INTERFACES.md), ADR-014 em [decisões](../docs/12-RISKS-AND-DECISIONS.md), [rastreabilidade](../docs/13-REQUIREMENTS-TRACEABILITY.md).

## Limitações e próxima ação

TCP real, mas dentro da mesma JVM de testes. Sem processos independentes, bootstrap, handshake, allowlist operacional, manutenção automática de vizinhos, rotas, UDP, vídeo ou CORE demonstrados. Reconexão é dial explícito. Socket.connect aplica timeout, mas não foi ensaiada uma rede que descarte SYN silenciosamente. Callbacks permanentemente bloqueados não são suportados como funcionamento normal.

RF-003/RNF-004/RP-006 permanecem IN PROGRESS por dependerem de outras tarefas/aceitação integrada. Próxima TASK-006 TODO: descoberta inicial de vizinhos configurados, sem membership/lease/renovação no bootstrap, conforme ADR-013. Código/fixtures atuais, enunciado e CORE/topologies preservados. Documentação atualizada, incluindo READMEs, setup e notas para relatório.
