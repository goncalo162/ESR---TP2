# ESR - Serviço Over the Top (OTT) para Entrega de Multimédia

Trabalho Prático Nº 2 da unidade curricular de Engenharia de Serviços em Rede (ESR) - Mestrado em Engenharia Informática, Universidade do Minho (2026/2027).

## Estado atual

TASK-001–005 concluídas; PHASE-02 IN PROGRESS. Configuração, codec e transporte TCP implementados, com dials assíncronos, filas limitadas, eventos e shutdown. Build com 149 testes aprovados (65 configuração, 66 codec, 18 transporte). Sockets loopback reais na mesma JVM; ainda sem overlay entre processos/streaming. Bootstrap previsto só para descoberta inicial, conforme ADR-013.

## Documentação

- [Enunciado](../docs/ENUNCIADO.md)
- [Plano e índice](../docs/00-README.md)
- [Arquitetura](../docs/04-ARCHITECTURE.md)
- [Protocolos e APIs](../docs/07-API-AND-INTERFACES.md)
- [Progresso e TASK-006](../docs/15-PROJECT-PROGRESS.md)
- [Ambiente e testes](../docs/11-SETUP-AND-ENVIRONMENT.md)
- [V-005](../experiments/task-005-transport-validation.md)

---

## 🚀 Como Compilar e Executar

### Pré-requisitos
- Java 21+
- Apache Maven 3.8+
- Docker e Docker Compose (para ambiente CORE emulado)

### Compilação
```bash
mvn clean package
```

### Testes

A partir de Projeto/:

```sh
mvn clean verify
mvn -Dtest=TransportTest test
```

Os testes TCP precisam de permissão para sockets locais; relatórios em target/surefire-reports/.

### Execução futura dos componentes

Comandos planeados: OttApplication ainda não tem main funcional. A composição pertence à TASK-008; execução de vídeo depende da integração posterior. TASK-005 não implementou bootstrap, handshake ou streaming.

```bash
# Iniciar Bootstrap Server
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role BOOTSTRAP --config src/main/resources/config/bootstrap.properties

# Iniciar Servidor de Conteúdos 1
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role SERVER --config src/main/resources/config/server-1.properties

# Iniciar Nó de Encaminhamento (Relay)
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role RELAY --config src/main/resources/config/relay.properties

# Iniciar Cliente de Streaming
java -cp target/ott-streaming-1.0-SNAPSHOT.jar pt.uminho.esr.ott.app.OttApplication --role CLIENT --config src/main/resources/config/client.properties
```

