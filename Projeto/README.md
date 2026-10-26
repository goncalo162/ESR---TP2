# ESR - Serviço Over the Top (OTT) para Entrega de Multimédia

Trabalho Prático Nº 2 da unidade curricular de Engenharia de Serviços em Rede (ESR) - Mestrado em Engenharia Informática, Universidade do Minho (2026/2027).

## 📄 Documentação

- **[Plano Detalhado de Implementação e Ordem de Execução](docs/plano-implementacao.md)**
- **[Especificação da Arquitetura](docs/arquitetura.md)**
- **[Especificação dos Protocolos de Comunicação](docs/protocolos.md)**
- **[Cenários de Teste no CORE](docs/cenarios-teste.md)**

---

## 🚀 Como Compilar e Executar

### Pré-requisitos
- Java 21+
- Apache Maven 3.8+
- Docker e Docker Compose (para ambiente CORE emulado)

### Compilação
```bash
cd Projeto
mvn clean package
```

### Execução dos Componentes
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

