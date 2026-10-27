# ESR — Serviço OTT de multimédia

Trabalho prático de Engenharia de Serviços em Rede: entrega de vídeo através de uma rede overlay aplicacional, com servidores de conteúdos, nós de encaminhamento e clientes. O CORE é a bancada de emulação de rede.

## Documentação

- [Enunciado](docs/ENUNCIADO.md)
- [Índice da documentação](docs/00-README.md)
- [Arquitetura](docs/04-ARCHITECTURE.md)
- [Ambiente de desenvolvimento](docs/11-SETUP-AND-ENVIRONMENT.md)
- [Progresso e tarefas](docs/15-PROJECT-PROGRESS.md)

## Compilar e testar

Pré-requisitos: Java 21 e Maven.

```sh
cd Projeto
mvn clean verify
```

## Utilizar o CORE

A partir da pasta `CORE/`:

```sh
sh setup.sh
./core-gui
```

- `CORE/volume/`: ficheiros partilhados com o ambiente CORE.
- `CORE/topologies/`: topologias de rede para os cenários de teste.
