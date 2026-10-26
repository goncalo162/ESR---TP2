# Roadmap de implementação

Sete fases seguem dependências reais: formatos/configuração antes da rede; vizinhança antes de métricas/rotas; rotas antes de subscrições; árvores antes de vídeo integrado; integração local antes de CORE; evidências antes de relatório/entrega. Todas começam TODO. Há 31 tarefas com um oráculo V-XXX por tarefa.

## Fases e entregáveis

| Fase | Nome | Tarefas | Gate de entrada | Resultado verificável / gate de saída |
|---|---|---|---|---|
| [PHASE-01](phases/PHASE-01-FOUNDATION.md) | Fundações, configuração e protocolo | TASK-001–TASK-004 | nenhum | Configuração e mensagens de controlo validadas por testes locais, sem necessidade de CORE. |
| [PHASE-02](phases/PHASE-02-OVERLAY.md) | Overlay, bootstrap e ciclo de vida | TASK-005–TASK-009 | PHASE-01 DONE | Três ou mais nós trocam HELLO, entram e saem e reconectam; bootstrap não intermedeia tráfego. |
| [PHASE-03](phases/PHASE-03-ROUTING.md) | Monitorização e descoberta de rotas | TASK-010–TASK-013 | PHASE-02 DONE | Rotas para várias streams, alternativas sem ciclos e trace até à origem. |
| [PHASE-04](phases/PHASE-04-FLOW-MANAGEMENT.md) | Árvores de distribuição e adaptação | TASK-014–TASK-018 | PHASE-03 DONE | Dois consumidores partilham ativação, remoção de um preserva o outro, falha troca upstream e segundo marco fica reproduzível. |
| [PHASE-05](phases/PHASE-05-STREAMING.md) | Vídeo, replicação e reprodução | TASK-019–TASK-024 | PHASE-04 DONE | Reprodução real das quatro streams, ramos independentes e tráfego de dados condicionado pela tabela ativa. |
| [PHASE-06](phases/PHASE-06-CORE-VALIDATION.md) | Integração e avaliação em CORE | TASK-025–TASK-028 | PHASE-05 DONE | Manifestos, logs, capturas e resultados reais dos três cenários; as metas são aprovadas ou limitações registadas. |
| [PHASE-07](phases/PHASE-07-DELIVERY.md) | Relatório, empacotamento e defesa | TASK-029–TASK-031 | PHASE-06 DONE | PDF e ZIP com nomes corretos, reprodução após extração limpa e checklist de submissão com confirmação humana. |

O gate de saída inclui a [DoD](14-DEFINITION-OF-DONE.md) da fase. Cada ficheiro de fase identifica requisitos, tarefas, riscos e validações. Um requisito transversal pode começar numa fase e só ser aceite noutra; consultar [13](13-REQUIREMENTS-TRACEABILITY.md).

## Ordem e dependências

~~~mermaid
flowchart TD
  P1[01 Configuração e protocolo] --> P2[02 Overlay e bootstrap]
  P2 --> P3[03 Monitorização e rotas]
  P3 --> P4[04 Subscrições e adaptação]
  P4 --> P5[05 Vídeo e reprodução]
  P5 --> P6[06 Validação CORE]
  P6 --> P7[07 Artigo e entrega]
  P2 --> M1[Marco intermédio 1]
  P4 --> M2[Marco intermédio 2]
~~~

| Tarefa | Fase | Responsabilidade | Dependências explícitas | Validação |
|---|---|---|---|---|
| [TASK-001](tasks/PHASE-01/TASK-001.md) | PHASE-01 | Validar a base Java e configurar testes | nenhuma | V-001 |
| [TASK-002](tasks/PHASE-01/TASK-002.md) | PHASE-01 | Fixar contratos, fixtures e decisões pendentes | TASK-001 | V-002 |
| [TASK-003](tasks/PHASE-01/TASK-003.md) | PHASE-01 | Carregar e validar configurações de nós, grafo e streams | TASK-001, TASK-002 | V-003 |
| [TASK-004](tasks/PHASE-01/TASK-004.md) | PHASE-01 | Codificar e descodificar mensagens de controlo v1 | TASK-002, TASK-003 | V-004 |
| [TASK-005](tasks/PHASE-02/TASK-005.md) | PHASE-02 | Estabelecer transporte TCP full-duplex com recursos limitados | TASK-004 | V-005 |
| [TASK-006](tasks/PHASE-02/TASK-006.md) | PHASE-02 | Implementar registo bootstrap e descoberta configurada | TASK-003, TASK-005 | V-006 |
| [TASK-007](tasks/PHASE-02/TASK-007.md) | PHASE-02 | Gerir handshake, unicidade e reconexão de vizinhos | TASK-005, TASK-006 | V-007 |
| [TASK-008](tasks/PHASE-02/TASK-008.md) | PHASE-02 | Compor o processo overlay e encerrar de forma previsível | TASK-003, TASK-006, TASK-007 | V-008 |
| [TASK-009](tasks/PHASE-02/TASK-009.md) | PHASE-02 | Preparar evidência do primeiro marco intermédio | TASK-008 | V-009 |
| [TASK-010](tasks/PHASE-03/TASK-010.md) | PHASE-03 | Medir RTT e publicar falhas de vizinhança | TASK-007, TASK-008, TASK-009 | V-010 |
| [TASK-011](tasks/PHASE-03/TASK-011.md) | PHASE-03 | Modelar candidatos, custos e snapshots por stream | TASK-003, TASK-010 | V-011 |
| [TASK-012](tasks/PHASE-03/TASK-012.md) | PHASE-03 | Difundir anúncios e construir catálogo e rotas | TASK-007, TASK-010, TASK-011 | V-012 |
| [TASK-013](tasks/PHASE-03/TASK-013.md) | PHASE-03 | Diagnosticar percursos pela tabela de rotas | TASK-008, TASK-012 | V-013 |
| [TASK-014](tasks/PHASE-04/TASK-014.md) | PHASE-04 | Ativar árvores com ACK e procura agregada | TASK-011, TASK-012, TASK-013 | V-014 |
| [TASK-015](tasks/PHASE-04/TASK-015.md) | PHASE-04 | Podar ramos, renovar leases e remover procura abandonada | TASK-010, TASK-014 | V-015 |
| [TASK-016](tasks/PHASE-04/TASK-016.md) | PHASE-04 | Recuperar árvores após falha de upstream | TASK-012, TASK-014, TASK-015 | V-016 |
| [TASK-017](tasks/PHASE-04/TASK-017.md) | PHASE-04 | Adaptar rota por RTT com histerese e troca confirmada | TASK-010, TASK-016 | V-017 |
| [TASK-018](tasks/PHASE-04/TASK-018.md) | PHASE-04 | Preparar o segundo marco com árvores e adaptação | TASK-009, TASK-013, TASK-015, TASK-016, TASK-017 | V-018 |
| [TASK-019](tasks/PHASE-05/TASK-019.md) | PHASE-05 | Definir codec UDP e fragmentação de frames | TASK-002, TASK-004, TASK-011, TASK-018 | V-019 |
| [TASK-020](tasks/PHASE-05/TASK-020.md) | PHASE-05 | Ler fontes MJPEG e controlar ritmo de publicação | TASK-002, TASK-003, TASK-019 | V-020 |
| [TASK-021](tasks/PHASE-05/TASK-021.md) | PHASE-05 | Reencaminhar e replicar datagramas pela árvore ativa | TASK-014, TASK-015, TASK-016, TASK-017, TASK-019 | V-021 |
| [TASK-022](tasks/PHASE-05/TASK-022.md) | PHASE-05 | Reconstituir frames com buffers e prazo limitados | TASK-019, TASK-021 | V-022 |
| [TASK-023](tasks/PHASE-05/TASK-023.md) | PHASE-05 | Apresentar catálogo e vídeo numa janela Swing | TASK-012, TASK-022 | V-023 |
| [TASK-024](tasks/PHASE-05/TASK-024.md) | PHASE-05 | Integrar duas origens e quatro streams ponta a ponta | TASK-008, TASK-018, TASK-020, TASK-021, TASK-022, TASK-023 | V-024 |
| [TASK-025](tasks/PHASE-06/TASK-025.md) | PHASE-06 | Preparar CORE e comprovar replicação no cenário de quatro nós | TASK-024 | V-025 |
| [TASK-026](tasks/PHASE-06/TASK-026.md) | PHASE-06 | Demonstrar alternativas e adaptação no cenário de seis nós | TASK-025 | V-026 |
| [TASK-027](tasks/PHASE-06/TASK-027.md) | PHASE-06 | Validar cenário complexo com duas origens e cinco clientes | TASK-026 | V-027 |
| [TASK-028](tasks/PHASE-06/TASK-028.md) | PHASE-06 | Recolher métricas e avaliar resultados e limites | TASK-025, TASK-026, TASK-027 | V-028 |
| [TASK-029](tasks/PHASE-07/TASK-029.md) | PHASE-07 | Escrever artigo com protocolos e avaliação crítica | TASK-028 | V-029 |
| [TASK-030](tasks/PHASE-07/TASK-030.md) | PHASE-07 | Empacotar código e verificar reprodução da entrega | TASK-029 | V-030 |
| [TASK-031](tasks/PHASE-07/TASK-031.md) | PHASE-07 | Preparar defesa e checklist de submissão humana | TASK-009, TASK-018, TASK-030 | V-031 |

TASK-001 → ... → TASK-031 é uma ordem total válida, sem obrigar trabalho paralelo. Dentro de uma fase pode escolher-se uma tarefa independente pronta; o gate anterior mantém-se. Não avançar para vídeo para esconder problemas de rotas. A primeira tarefa é [TASK-001](tasks/PHASE-01/TASK-001.md).

## Marcos e calendário

M1 = TASK-009, prova de overlay/controlo; M2 = TASK-018, prova de rotas/árvores/adaptação. São conteúdos propostos para as duas avaliações de 5%; confirmar datas e expectativas com o docente. Se M2 exigir vídeo, deslocar a demonstração para depois de TASK-024 e atualizar o gate, sem afirmar que vídeo já existe.

O PDF prevê oito aulas ao longo de nove semanas. Como hipótese de organização: semana 1 para fundamentos, 2 para overlay/M1, 3 para rotas, 4–5 para gestão de fluxos/M2, 6–7 para vídeo, 8 para CORE e 9 para entrega. Não são datas oficiais nem estimativas garantidas; rever após TASK-001 e disponibilidade da equipa. Recolher notas para relatório ao longo das fases, embora o artigo final dependa de resultados reais.

## Critérios globais para progressão

Concluir testes no momento de cada tarefa, preservar evidências e atualizar [15](15-PROJECT-PROGRESS.md). Falha de uma meta técnica implica análise; falta de um requisito obrigatório impede fecho. Gates externos de vídeo, turno/grupo e submissão estão identificados em [12](12-RISKS-AND-DECISIONS.md). Não existe trabalho de produção autorizado nesta fase de planeamento.
