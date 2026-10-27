# Fase 2 — Material incremental para o relatório

Estado em 27-10-2026: PHASE-02 IN PROGRESS; TASK-005 DONE e TASK-006–009 TODO. Este documento reúne a implementação e validação do transporte TCP; a overlay completa e o relatório final continuam pendentes. O material anterior da fase 1 mantém-se nas [notas próprias](PHASE-01-REPORT-NOTES.md).

## Implementação disponível

Transport abre o listener TCP e estabelece ligações assíncronas com timeout. Connection identifica cada ligação por UUID e integra o codec v1, com leitor e escritor exclusivos. Eventos open/message/closed são ordenados por ligação; ligações diferentes podem produzir callbacks em paralelo. Callbacks devem publicar eventos sem bloquear.

send serializa e enfileira, devolvendo aceitação/erro, sem esperar por entrega/ACK. Fila default de 256 frames; overflow fecha só a ligação saturada. Admissão de até 64 reservas, incluindo tentativas pendentes. Threads virtuais Java 21 não dispensam estes limites explícitos. Nenhuma dependência runtime nova.

Shutdown fecha listener/sockets, limpa filas e termina workers, com deadline global default de 3 s para chamadas externas. Chamado por callback, inicia encerramento sem join; a terminação é confirmada externamente. Capacidades e prazos são decisões técnicas, não números impostos no PDF. Bootstrap permanece exclusivamente para descoberta inicial conforme ADR-013, sem implementação de handler nesta tarefa.

## Testes e resultados

Executados 15 testes com sockets TCP reais em loopback na mesma JVM: quatro produtores simultâneos, fixture independente, framing parcial/concatenado, recusa/porta ocupada, mensagens inválidas, peer lento, overflow, admissão, cancelamento, reconexão explícita e shutdown durante I/O/dial.

`mvn -B -ntp clean verify` passou **146 testes**, sem falhas/erros/skips: 65 configuração, 66 codec e 15 transporte. macOS 14.6.1 x86_64, Temurin 21.0.13+9 e Maven 3.9.9. [Evidência](../experiments/task-005-transport-validation.md), [log do build](../experiments/task-005-build.log).

## Texto factual reutilizável

O plano de controlo utiliza ligações TCP bidirecionais com enquadramento binário v1. Cada ligação possui leitor e escritor exclusivos, fila de saída limitada e identidade própria para correlacionar eventos ao longo de reconexões. O estabelecimento é assíncrono e as falhas de formato ou saturação são tratadas localmente, preservando as restantes ligações. O encerramento liberta I/O pendente, sockets e workers. A validação do transporte acrescentou 15 testes aos 131 testes de configuração e codec, totalizando 146 casos aprovados num build limpo.

## Limites e continuação

Sem bootstrap, handshake, autorização por grafo, unicidade por vizinho, retries automáticos ou CLI. Reconexão é explícita. Não foram demonstrados processos JVM independentes, CORE, rotas, UDP ou streaming. Não foi ensaiado timeout de connect com descarte silencioso de SYN; não há medição de desempenho, escalabilidade ou qualidade de vídeo.

RF-003/RNF-004/RP-006 parcialmente cobertos, conforme [rastreabilidade](13-REQUIREMENTS-TRACEABILITY.md). Próxima: [TASK-006](tasks/PHASE-02/TASK-006.md), apenas descoberta inicial dos vizinhos, ainda TODO.
