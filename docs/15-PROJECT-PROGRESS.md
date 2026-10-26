# Progresso do projeto

Data de criação do plano: 26-10-2026. Estado global da implementação: IN PROGRESS. TASK-001 e TASK-002 concluídas por pedido explícito do utilizador; TASK-003–031 continuam TODO. Estados permitidos: TODO, IN PROGRESS, BLOCKED, DONE.

**Próxima tarefa recomendada:** [TASK-003](tasks/PHASE-01/TASK-003.md), ainda TODO e não iniciada. TASK-001/002 estão DONE, com [baseline](../experiments/build-baseline.md) e [V-002](../experiments/task-002-contract-review.md). Os gates de media/entrega não bloqueiam TASK-003.

## Fases

| Fase | Tarefas | Estado | Dependência / gate |
|---|---|---|---|
| [PHASE-01](phases/PHASE-01-FOUNDATION.md) | TASK-001–TASK-004 | IN PROGRESS | TASK-001/002 autorizadas e concluídas |
| [PHASE-02](phases/PHASE-02-OVERLAY.md) | TASK-005–TASK-009 | TODO | PHASE-01 DONE |
| [PHASE-03](phases/PHASE-03-ROUTING.md) | TASK-010–TASK-013 | TODO | PHASE-02 DONE |
| [PHASE-04](phases/PHASE-04-FLOW-MANAGEMENT.md) | TASK-014–TASK-018 | TODO | PHASE-03 DONE |
| [PHASE-05](phases/PHASE-05-STREAMING.md) | TASK-019–TASK-024 | TODO | PHASE-04 DONE |
| [PHASE-06](phases/PHASE-06-CORE-VALIDATION.md) | TASK-025–TASK-028 | TODO | PHASE-05 DONE |
| [PHASE-07](phases/PHASE-07-DELIVERY.md) | TASK-029–TASK-031 | TODO | PHASE-06 DONE |

## Tarefas

| ID | Objetivo | Dependências | Estado | Validação / evidência |
|---|---|---|---|---|
| [TASK-001](tasks/PHASE-01/TASK-001.md) | Validar a base Java e configurar testes | nenhuma | DONE | V-001: EXECUTADA; [baseline](../experiments/build-baseline.md); zero testes funcionais |
| [TASK-002](tasks/PHASE-01/TASK-002.md) | Fixar contratos, fixtures e decisões pendentes | TASK-001 | DONE | V-002: EXECUTADA; [contratos, fixtures e recursos](../experiments/task-002-contract-review.md) |
| [TASK-003](tasks/PHASE-01/TASK-003.md) | Carregar e validar configurações de nós, grafo e streams | TASK-001, TASK-002 | TODO | V-003: NÃO EXECUTADO |
| [TASK-004](tasks/PHASE-01/TASK-004.md) | Codificar e descodificar mensagens de controlo v1 | TASK-002, TASK-003 | TODO | V-004: NÃO EXECUTADO |
| [TASK-005](tasks/PHASE-02/TASK-005.md) | Estabelecer transporte TCP full-duplex com recursos limitados | TASK-004 | TODO | V-005: NÃO EXECUTADO |
| [TASK-006](tasks/PHASE-02/TASK-006.md) | Implementar registo bootstrap e descoberta configurada | TASK-003, TASK-005 | TODO | V-006: NÃO EXECUTADO |
| [TASK-007](tasks/PHASE-02/TASK-007.md) | Gerir handshake, unicidade e reconexão de vizinhos | TASK-005, TASK-006 | TODO | V-007: NÃO EXECUTADO |
| [TASK-008](tasks/PHASE-02/TASK-008.md) | Compor o processo overlay e encerrar de forma previsível | TASK-003, TASK-006, TASK-007 | TODO | V-008: NÃO EXECUTADO |
| [TASK-009](tasks/PHASE-02/TASK-009.md) | Preparar evidência do primeiro marco intermédio | TASK-008 | TODO | V-009: NÃO EXECUTADO |
| [TASK-010](tasks/PHASE-03/TASK-010.md) | Medir RTT e publicar falhas de vizinhança | TASK-007, TASK-008, TASK-009 | TODO | V-010: NÃO EXECUTADO |
| [TASK-011](tasks/PHASE-03/TASK-011.md) | Modelar candidatos, custos e snapshots por stream | TASK-003, TASK-010 | TODO | V-011: NÃO EXECUTADO |
| [TASK-012](tasks/PHASE-03/TASK-012.md) | Difundir anúncios e construir catálogo e rotas | TASK-007, TASK-010, TASK-011 | TODO | V-012: NÃO EXECUTADO |
| [TASK-013](tasks/PHASE-03/TASK-013.md) | Diagnosticar percursos pela tabela de rotas | TASK-008, TASK-012 | TODO | V-013: NÃO EXECUTADO |
| [TASK-014](tasks/PHASE-04/TASK-014.md) | Ativar árvores com ACK e procura agregada | TASK-011, TASK-012, TASK-013 | TODO | V-014: NÃO EXECUTADO |
| [TASK-015](tasks/PHASE-04/TASK-015.md) | Podar ramos, renovar leases e remover procura abandonada | TASK-010, TASK-014 | TODO | V-015: NÃO EXECUTADO |
| [TASK-016](tasks/PHASE-04/TASK-016.md) | Recuperar árvores após falha de upstream | TASK-012, TASK-014, TASK-015 | TODO | V-016: NÃO EXECUTADO |
| [TASK-017](tasks/PHASE-04/TASK-017.md) | Adaptar rota por RTT com histerese e troca confirmada | TASK-010, TASK-016 | TODO | V-017: NÃO EXECUTADO |
| [TASK-018](tasks/PHASE-04/TASK-018.md) | Preparar o segundo marco com árvores e adaptação | TASK-009, TASK-013, TASK-015, TASK-016, TASK-017 | TODO | V-018: NÃO EXECUTADO |
| [TASK-019](tasks/PHASE-05/TASK-019.md) | Definir codec UDP e fragmentação de frames | TASK-002, TASK-004, TASK-011, TASK-018 | TODO | V-019: NÃO EXECUTADO |
| [TASK-020](tasks/PHASE-05/TASK-020.md) | Ler fontes MJPEG e controlar ritmo de publicação | TASK-002, TASK-003, TASK-019 | TODO | V-020: NÃO EXECUTADO |
| [TASK-021](tasks/PHASE-05/TASK-021.md) | Reencaminhar e replicar datagramas pela árvore ativa | TASK-014, TASK-015, TASK-016, TASK-017, TASK-019 | TODO | V-021: NÃO EXECUTADO |
| [TASK-022](tasks/PHASE-05/TASK-022.md) | Reconstituir frames com buffers e prazo limitados | TASK-019, TASK-021 | TODO | V-022: NÃO EXECUTADO |
| [TASK-023](tasks/PHASE-05/TASK-023.md) | Apresentar catálogo e vídeo numa janela Swing | TASK-012, TASK-022 | TODO | V-023: NÃO EXECUTADO |
| [TASK-024](tasks/PHASE-05/TASK-024.md) | Integrar duas origens e quatro streams ponta a ponta | TASK-008, TASK-018, TASK-020, TASK-021, TASK-022, TASK-023 | TODO | V-024: NÃO EXECUTADO |
| [TASK-025](tasks/PHASE-06/TASK-025.md) | Preparar CORE e comprovar replicação no cenário de quatro nós | TASK-024 | TODO | V-025: NÃO EXECUTADO |
| [TASK-026](tasks/PHASE-06/TASK-026.md) | Demonstrar alternativas e adaptação no cenário de seis nós | TASK-025 | TODO | V-026: NÃO EXECUTADO |
| [TASK-027](tasks/PHASE-06/TASK-027.md) | Validar cenário complexo com duas origens e cinco clientes | TASK-026 | TODO | V-027: NÃO EXECUTADO |
| [TASK-028](tasks/PHASE-06/TASK-028.md) | Recolher métricas e avaliar resultados e limites | TASK-025, TASK-026, TASK-027 | TODO | V-028: NÃO EXECUTADO |
| [TASK-029](tasks/PHASE-07/TASK-029.md) | Escrever artigo com protocolos e avaliação crítica | TASK-028 | TODO | V-029: NÃO EXECUTADO |
| [TASK-030](tasks/PHASE-07/TASK-030.md) | Empacotar código e verificar reprodução da entrega | TASK-029 | TODO | V-030: NÃO EXECUTADO |
| [TASK-031](tasks/PHASE-07/TASK-031.md) | Preparar defesa e checklist de submissão humana | TASK-009, TASK-018, TASK-030 | TODO | V-031: NÃO EXECUTADO |

## Gates e bloqueios

| Informação pendente | Tarefa responsável | Condição de desbloqueio | Estado inicial |
|---|---|---|---|
| Recurso MJPEG e preparação dos quatro conteúdos | TASK-020 / TASK-030 | Perfil/hash docente confirmados; preparar B/C/D e verificar redistribuição | Acesso/formato confirmados em TASK-002; gates futuros de preparação/entrega |
| Instruções docentes adicionais sobre áudio/texto ou compatibilidade de protocolo | H-01/H-10 | Utilizador respondeu sem indicações adicionais conhecidas | Hipóteses mantidas, sem confirmação formal docente |
| Datas/conteúdo das duas avaliações | TASK-009 / TASK-018 | Informação docente e roteiro alinhado | Pendente |
| Runtime/GUI/volumes CORE | TASK-025 | Verificação no ambiente real | Pendente |
| Turno, grupo e prazo | TASK-030 / TASK-031 | Informação real fornecida pela equipa | Pendente |
| Upload Blackboard | TASK-031 | Confirmação humana depois de artefactos validados | Não realizado |

## Histórico de decisões

| Data | Decisão | Referência |
|---|---|---|
| 26-10-2026 | Planeamento apenas, sem desenvolvimento; Java preparado ainda não funcional | 01 / auditoria |
| 26-10-2026 | Mínimo explícito: dois servidores com dois vídeos cada | H-04 / RF-012–013 |
| 26-10-2026 | Java/Maven, TCP/UDP, bootstrap, path vector/RTT e árvore com ACK/leases | ADR-001–007 em 12 |
| 26-10-2026 | Testes incrementais, CORE na aceitação e sete fases com 31 tarefas | ADR-008 / 08 |

## Evidências da sessão de planeamento

Leitura integral do PDF por extração de texto e inspeção visual da figura 1 e dos cenários na p. 5, comparação SHA-256 das cópias, inspeção de ficheiros do repositório e revisão dos contratos/estrutura documental. A validação documental está em [16](16-PLAN-VALIDATION.md). Não há build, testes Java, experiência CORE, vídeo reproduzido nem submissão realizados por este planeamento.

## Passagem inicial de contexto

- Revisão observada: 3c2c22d5b72e62b73b914c11b08f2cf9714f62db.
- Tarefa atual: nenhuma IN PROGRESS; todas TODO.
- Ficheiros alterados por esta sessão: apenas Markdown em docs/.
- Decisões: ver 12; recursos de vídeo e dados de entrega continuam pendentes.
- Próxima ação: após autorização de construção, abrir TASK-001 e inspecionar novamente o estado real do repositório.
- Preservar: CORE, Projeto e documentação histórica não foram modificados pelo plano.

Acrescentar novos handoffs seguindo [o guia](AI-IMPLEMENTATION-GUIDE.md). Não substituir resultados anteriores por afirmações genéricas de sucesso.

## Execução da TASK-001 — 26-10-2026

- Estado: DONE. PHASE-01 permanece IN PROGRESS; TASK-002–031 continuam TODO.
- Revisão de partida: 04d02fef9f4646266de0fca428ca4d680597ba36; árvore inicialmente limpa.
- Alterações: Projeto/pom.xml, .gitignore, experiments/build-baseline.md e documentação de estado/ambiente. Não se reorganizou Projeto/ nem se alterou Projeto/src.
- Decisões: Java 21 e Maven preservados; Jupiter 5.13.4, Surefire 3.5.4 e versões dos plugins do ciclo clean/package fixadas. failIfNoTests=false é transitório até TASK-003.
- Validação: POM efetivo, clean package e test com sucesso; engine/âmbito de testes conferidos. Zero .class e zero testes funcionais, sem relatórios Surefire de casos de teste.
- Bloqueio resolvido: cache Maven só de leitura no sandbox; execução com permissões alargadas autorizada resolveu os downloads. Sem bloqueios atuais da TASK-001.
- Evidência: [baseline, comandos e resultados reais](../experiments/build-baseline.md).
- Requisito RP-006: IN PROGRESS, com base de testes concluída e restantes comportamentos pendentes. Nenhum requisito funcional é marcado DONE.
- Próxima ação: ler TASK-002 quando a sua execução for pedida; não foi iniciada nesta sessão.
- Preservar: fontes, configurações, testes placeholders e CORE sem alterações. Artefactos target regenerados pelo build não são funcionalidades implementadas.

## Execução da TASK-002 — 26-10-2026

- Estado: DONE; PHASE-01 continua IN PROGRESS e TASK-003–031 em TODO.
- Partida: revisão 04d02fef9f4646266de0fca428ca4d680597ba36 com alterações locais da TASK-001 preservadas.
- Novos artefactos: 45 pares .bin/.hex, manifesto e README em Projeto/src/test/resources/protocol; revisão em experiments/task-002-contract-review.md.
- Decisões: ADR-009; mínimos/limites e identidade TCP/UDP explícitos; serverEpoch comum às streams da mesma origem, com estado por StreamKey; perfil MJPEG confirmado de cinco dígitos ASCII + JPEG.
- Recursos: ZIP obtido em /tmp por curl autorizado, hashes e 500 frames inspecionados; três cópias representam um vídeo. A/B e C/D identificados, HEAD 200 das fontes B/C/D, ainda por preparar em TASK-020.
- Utilizador: sem indicações adicionais conhecidas sobre áudio/texto ou RTP/RTSP; H-01/H-10 continuam explícitas. Não foi contactado docente ou terceiro.
- Validação V-002: inspeção independente de tamanhos/offsets/hex/binário/hash, 22 casos aceites e 23 rejeitados quanto ao formato; nenhum codec, JUnit, build ou streaming executado nesta tarefa.
- RNF-005: IN PROGRESS; especificação/fixtures não concluem implementação nem avaliação crítica final. RP-006 conserva o progresso da TASK-001.
- Bloqueios atuais da TASK-002: nenhum. Gates futuros: preparação dos quatro media e condições de distribuição.
- Próxima ação: ler TASK-003 quando a execução for pedida; não foi iniciada. Preservar as alterações anteriores e fontes Java/config/POM/CORE.
- Evidência: [contratos e recursos](../experiments/task-002-contract-review.md), [fixtures](../Projeto/src/test/resources/protocol/README.md).
