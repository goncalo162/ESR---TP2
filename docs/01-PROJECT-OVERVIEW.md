# Visão geral do projeto

## Contexto e problema

Trabalho Prático n.º 2 de Engenharia de Serviços em Rede, MEI, Universidade do Minho, 2026/2027. O enunciado prevê oito aulas ao longo de nove semanas; não fornece datas de entrega nem das avaliações intermédias. Uma solução unicast direta repetiria a mesma stream do servidor para cada cliente. O protótipo deve partilhar segmentos comuns de um percurso e replicar os pacotes apenas nos pontos de ramificação da overlay.

## Objetivos e resultado esperado

Entregar uma aplicação executada manualmente em máquinas IP, com papéis de servidor, relay ou cliente. Os servidores e clientes participam na overlay. Um serviço de bootstrap fornece vizinhos, sem transportar vídeo ou decidir centralmente as rotas. A solução final terá pelo menos quatro streams distintas provenientes de dois servidores e será avaliada principalmente em CORE, incluindo falhas e alterações de atraso. O relatório, até 12 páginas em formato de artigo, descreve desenho, protocolos, implementação futura, testes e apreciação crítica.

## Atores

- Operador/equipa: prepara a underlay, configura vizinhos e conteúdos, inicia e termina processos e recolhe evidências.
- Cliente/utilizador: escolhe uma stream, observa reprodução, termina a subscrição e pode escolher outra.
- Docente/avaliador: acompanha dois marcos intermédios e verifica solução, relatório e defesa.
- Sistemas externos: CORE fornece emulação IP; os recursos docentes fornecem exemplos e movie.Mjpeg, cuja disponibilidade ainda tem de ser confirmada.

## Fluxos de utilização

1. O operador inicia o bootstrap e depois servidores, relays e clientes em qualquer ordem suportada por tentativas de reconexão limitadas.
2. Cada nó conhece o contacto bootstrap, recebe a lista permitida e estabelece canais TCP bidirecionais com vizinhos disponíveis.
3. Servidores anunciam metadados de cada stream. Anúncios constroem candidatos a rotas e catálogo sem enviar vídeo.
4. Um cliente escolhe um UUID de stream. A ativação segue o percurso inverso até à fonte; a fonte passa a enviar e os relays replicam conforme as subscrições.
5. A saída de um cliente remove apenas o seu ramo. A última saída desativa a transmissão nesse segmento.
6. Falha ou degradação do upstream desencadeia seleção de alternativa e nova ativação, se existir caminho. Uma partição ou perda da única fonte produz indisponibilidade explícita, sem prometer recuperação impossível.

## Âmbito e limites

Obrigatório: overlay dinâmica, tabela por fluxo, múltiplos clientes, caminhos alternativos, monitorização, métricas de rede, dois servidores com dois vídeos cada, testes principalmente CORE, relatório e artefactos com os nomes pedidos.

Escolhas deste plano: MJPEG, TCP/UDP separados, bootstrap configurado, GUI mínima Swing, quatro UUID distintos e subscrição sob pedido. As etapas do PDF são sugestões, não tecnologias obrigatórias. O objetivo genérico áudio/vídeo/texto é interpretado como entrega multimédia concretizada por vídeo, por ser a modalidade explicitamente exigida na lista mínima; ver H-01 em [12](12-RISKS-AND-DECISIONS.md).

Fora do âmbito: streaming adaptativo HTTP, CDN pública, transcodificação, contas, faturação, aplicações móveis, armazenamento distribuído, descoberta arbitrária na Internet, NAT traversal, replicação de um mesmo vídeo entre fontes, recuperação de pacotes com FEC/ARQ, garantia formal de QoS ou novos requisitos de áudio/texto sem esclarecimento. São exclusões de âmbito proposto e não dispensas de requisitos explícitos.

## Auditoria inicial, 26-10-2026

Revisão observacional do commit 3c2c22d5b72e62b73b914c11b08f2cf9714f62db, sem executar a aplicação:

| Caminho | Observação confirmada | Consequência |
|---|---|---|
| [Projeto/pom.xml](../Projeto/pom.xml) | Java release 21; compiler-plugin 3.13.0; sem dependências de testes | Preservar Java/Maven; configurar testes em TASK-001 |
| Projeto/src/main/java | 28 ficheiros vazios e BootstrapServer.java com sete linhas de comentário | Não existe implementação funcional demonstrada |
| Projeto/src/test/java | Cinco ficheiros de teste vazios | Nenhuma cobertura comprovada |
| Projeto/src/main/resources/config | Sete ficheiros vazios | Não há configuração utilizável |
| Projeto/src/test/resources/topologies | Três JSON vazios | Fixtures por criar |
| Projeto/target | Artefactos antigos presentes | Não provam compilação ou qualidade atual |
| [CORE/container/Dockerfile](../CORE/container/Dockerfile) | Imagem base CORE 9.2.1; JAVA_HOME fixo amd64; pacote JDK headless | Validar runtime, arquitetura e suporte GUI |
| [CORE/docker-compose.yml](../CORE/docker-compose.yml) | Container privilegiado; volume partilhado em /volume | Evitar carregar serviços desnecessários; planeamento não inicia Docker |
| CORE/topologies/cenario1.xml | Quatro equipamentos; intermediário é router | São quatro equipamentos underlay, não prova de quatro processos overlay |
| CORE/topologies/cenario2.xml | Seis equipamentos, incluindo três routers | Mapear processos separadamente ou criar terminais para relays |
| CORE/topologies/cenario3.xml | 20 equipamentos e 31 links; um equipamento chamado Servidor | Nome não garante dois servidores de conteúdos; criar segundo terminal no cenário de entrega |
| Projeto/docs/*.md | Plano anterior e três documentos vazios | Documento antigo contém escolhas que não são obrigações do PDF |

Os suportes specsmd src/flows/aidlc/agents/inception-agent.md, .specsmd/aidlc/memory-bank.yaml e .specsmd/skills/inception não foram encontrados no repositório nem no diretório da skill. Este plano usa os conceitos de requisitos, unidades, histórias e incrementos, mas não afirma ter criado ou validado artefactos nativos specsmd. Os ficheiros de fase equivalem aos incrementos de construção; as tarefas contêm o objetivo observável e a aceitação de cada história técnica.
