# Progresso do projeto

Data de criação do plano: 26-10-2026. Estado global da implementação: IN PROGRESS. PHASE-01 e TASK-001–004 concluídas por pedido explícito do utilizador; TASK-005 concluída; PHASE-02 IN PROGRESS. PHASE-03–07 e TASK-006–031 continuam TODO. Estados permitidos: TODO, IN PROGRESS, BLOCKED, DONE.

**Utilizador desta sessão:** [Fillmore](team/FILLMORE.md). A próxima implementação da tua fila é TASK-006; a revisão atribuída é de Waheed. Na TASK-005, Fillmore é o revisor atribuído e Waheed o responsável previsto na distribuição.

**Próxima tarefa recomendada:** [TASK-006](tasks/PHASE-02/TASK-006.md), ainda TODO. TASK-005 DONE com [18 testes TCP e 149 testes no build completo](../experiments/task-005-transport-validation.md). PHASE-01 está DONE: configuração e codec testados com [131 casos aprovados](../experiments/task-004-codec-validation.md). Material para relatório: [fase 1](PHASE-01-REPORT-NOTES.md) e [fase 2/TASK-005](PHASE-02-REPORT-NOTES.md).

Histórico anterior à TASK-005 — revisão de prontidão em 27-10-2026: [implementação, build repetido e pendências](../experiments/phase-01-readiness-review.md). Os 131 testes voltaram a passar e não foi identificado bloqueio ao início de TASK-005; PHASE-02 permanece TODO.

Histórico anterior à TASK-005 — correção posterior, por pedido explícito do utilizador: **bootstrap exclusivamente para localização inicial dos vizinhos**. Contrato/código/fixtures e tarefas futuras corrigidos por ADR-013; sem registo vivo, leases, renovações ou avisos de saída no bootstrap. [Evidência atual](../experiments/bootstrap-discovery-correction.md): 131 testes aprovados, 16 tipos ativos, código 3 retirado. PHASE-01 permanece DONE; PHASE-02 não foi iniciada.

Handoff histórico da correção: partir da revisão 4105429a76f67a5acf0579db8719b0ba2460b549 com as notas locais da revisão de prontidão preservadas. Alterados apenas contrato/codec/testes/fixtures e documentação necessária; removido o placeholder vazio NodeRegistry. Build clean package offline com Java 21 passou em 27-10-2026 às 14:34:31 UTC. Sem bloqueios; abrir TASK-005 quando pedida a sua implementação. Os resultados originais das tarefas conservam-se como histórico, com notas apontando para ADR-013.

Distribuição de trabalho: [coordenação e matriz completa](team/00-COORDINATION.md); [Waheed](team/WAHEED.md), [Fillmore](team/FILLMORE.md) e [Denji](team/DENJI.md). A TASK-005 tem Waheed como responsável atribuído e Fillmore como revisor atribuído. A próxima tarefa é TASK-006, atribuída a Fillmore, com Waheed como revisor. As atribuições não substituem a evidência técnica nem alteram os estados das tarefas. Estados continuam exclusivamente nos TASK e nesta tabela; os documentos pessoais são filas de trabalho, não cópias do progresso.

Handoff da distribuição (27-10-2026, base 2e58ef5): criados quatro Markdown em docs/team/, metadados nas 27 TASK futuras e ligações/regras de revisão nos documentos gerais/fases. Validação documental confirmou cobertura, atribuições sem auto-revisão, dependências sem ciclos e referências válidas; código/CORE preservados. Não foram executados build/testes Java nesta alteração exclusivamente documental. Não há implementação em curso por efeito desta distribuição.

## Fases

| Fase | Tarefas | Estado | Dependência / gate |
|---|---|---|---|
| [PHASE-01](phases/PHASE-01-FOUNDATION.md) | TASK-001–TASK-004 | DONE | quatro tarefas e critérios da fase concluídos |
| [PHASE-02](phases/PHASE-02-OVERLAY.md) | TASK-005–TASK-009 | IN PROGRESS | PHASE-01 DONE |
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
| [TASK-003](tasks/PHASE-01/TASK-003.md) | Carregar e validar configurações de nós, grafo e streams | TASK-001, TASK-002 | DONE | V-003: EXECUTADA; [65 testes, build e controlo negativo](../experiments/task-003-configuration-validation.md) |
| [TASK-004](tasks/PHASE-01/TASK-004.md) | Codificar e descodificar mensagens de controlo v1 | TASK-002, TASK-003 | DONE | V-004: EXECUTADA; [66 casos do codec e 131 no build](../experiments/task-004-codec-validation.md) |
| [TASK-005](tasks/PHASE-02/TASK-005.md) | Estabelecer transporte TCP full-duplex com recursos limitados | TASK-004 | DONE | [V-005: 18 testes TCP, 149 no build](../experiments/task-005-transport-validation.md) |
| [TASK-006](tasks/PHASE-02/TASK-006.md) | Fornecer localização inicial dos vizinhos configurados | TASK-003, TASK-005 | TODO | V-006: NÃO EXECUTADO |
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
| 27-10-2026 | Por pedido explícito: bootstrap apenas localização inicial; sem membership/renovação, código 3 retirado, NeighborList sem lease | ADR-013 / [correção e 131 testes](../experiments/bootstrap-discovery-correction.md) |

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

## Execução da TASK-003 — 27-10-2026

- Partida: dffb4fc2c45715ef99ebcf7b0a7e948de70b83f4, árvore inicialmente limpa. Revalidados build e 45 fixtures das duas dependências; ambas completas segundo os respetivos critérios.
- Estado: TASK-003 DONE; PHASE-01 continua IN PROGRESS; TASK-004–031 TODO.
- Alterações: loader, modelos/erros imutáveis em config, enum NodeRole, propriedades de exemplo e catálogos separados por origem, testes ConfigurationLoaderTest; POM exige testes.
- Contrato: carga UTF-8 estrita por perfil e por conjunto; sem DNS/sockets/leitura de vídeos; paths relativos ao ficheiro/media.base; ownership e UUID global por conjunto validado. Decisão ADR-010 e docs/07 atualizados.
- Resultado real: clean package com Java 21.0.12.1/Maven 3.6.3 executou 65 testes, zero falhas/erros/skips. Provider JUnit Platform carregado. POM vazio em /tmp falhou com No tests to run!, como esperado.
- Permissões: provider em falta resolvido por descarga autorizada; build final passou offline. Nenhum bloqueio atual.
- Requisitos associados: IN PROGRESS, com configuração/testes locais como evidência parcial; não declarar servidores, overlay ou streaming funcionais.
- Preservar: artefactos da TASK-002, CORE e evidências históricas sem alterações. Os vídeos continuam por preparar na TASK-020.
- Próxima ação: TASK-004 quando solicitada; não foi iniciada. [Evidência detalhada](../experiments/task-003-configuration-validation.md).

## Esclarecimento sobre CORE — 27-10-2026

O utilizador reforçou o uso das topologias existentes. Registada ADR-011: reutilizar CORE/topologies/cenario1.xml, cenario2.xml e cenario3.xml; os perfis loopback da TASK-003 são exemplos locais de validação. Retirada a criação obrigatória de variantes nas TASK-025–027. Verificação apenas estrutural dos XML, sem alterar ficheiros CORE ou iniciar serviços. Nenhuma tarefa nova foi iniciada; TASK-004 permanece a próxima.

## Execução da TASK-004 e fecho da fase 1 — 27-10-2026

- Partida: b67a1a719369746d7ee9a572b5ceda577f2368a8, árvore limpa. Dependências e dados persistidos presentes.
- Estado: TASK-001–004 e PHASE-01 DONE; PHASE-02 e TASK-005 continuam TODO, sem implementação nesta sessão.
- Código: 17 tipos de controlo, modelos imutáveis, encode/decode/read/write com framing, UTF-8, limites e erros; sem sockets/handlers.
- Testes: golden bytes/decoding das 29 fixtures TCP, partial reads/EOF/concatenação e integração de configuração→mensagens em memória. As 45 fixtures e os XML CORE foram preservados.
- Resultado final: Java 21.0.12.1/Maven 3.6.3, clean package com 131 testes (65+66), zero falhas/erros/skips; sem bloqueios.
- Critérios da fase: quatro tarefas com evidência, configuração/codec integrados localmente, suite anterior sem regressão e documentação/rastreabilidade atualizadas.
- Requisitos globais: permanecem IN PROGRESS; rede, rotas, vídeo, testes CORE e artigo/PDF final ainda pendentes.
- Relatório: formatos de controlo e detalhes da base implementada/testes já têm material factual; arquitetura/interações completas e resultados de rede permanecem parciais. [Notas](PHASE-01-REPORT-NOTES.md).
- Próxima ação: TASK-005 quando solicitada, seguindo o guia. Preservar o uso das topologias existentes conforme ADR-011.
- Evidência: [V-004 e fecho](../experiments/task-004-codec-validation.md).

## Execução da TASK-005 — 27-10-2026

- Revisão de partida: ebb825b; árvore inicialmente limpa. TASK-001–005 DONE, PHASE-02 IN PROGRESS; TASK-006–031 TODO. Este estado substitui as recomendações anteriores de início da TASK-005, conservadas como histórico.
- Implementados Transport/Connection, 15 testes de transporte, evidência e documentação. Preservada ADR-013 de bootstrap exclusivamente para descoberta, codec corrigido e fixtures; transporte documentado em ADR-014.
- Java 21.0.13/Maven 3.9.9 e configuração VS Code já estavam instalados. Enunciado Markdown e três figuras presentes e verificados.
- `mvn -B -ntp clean verify`: 146 testes, zero falhas/erros/skips. Sockets loopback executados com permissões alargadas. [Log e limites](../experiments/task-005-transport-validation.md).
- RF-003/RNF-004/RP-006 parcialmente cobertos. Sem bloqueios atuais; todos os critérios TASK-005 verificados. CORE, JVMs separadas e streaming não demonstrados.
- READMEs, setup, estrutura, segurança, notas de relatório, tarefas e rastreabilidade alinhados. Próxima ação: abrir TASK-006 quando solicitada, sem membership/leases no bootstrap.
- Preservar correções documentais e código de descoberta existentes. Sem commits, uploads ou alterações CORE.

## Revisão da TASK-005 — 27-10-2026

Revisão de partida: ebe39f4; árvore inicialmente limpa. Revistos contratos, concorrência, admissão/cancelamento, codec, filas, eventos e shutdown. Não foi identificado defeito funcional confirmado que exigisse alterar Transport/Connection. Acrescentados três testes para erros em callbacks open/closed, timeout perante callback preso e shutdown simultâneo a partir de duas ligações.

`mvn -B -ntp clean verify`: 149 testes aprovados (65 configuração + 66 codec + 18 transporte), sem falhas/erros/skips. [Log da revisão](../experiments/task-005-review-build.log), [conclusões e limites](../experiments/task-005-transport-validation.md#revisão-da-task-005--27-10-2026). Totais documentais e referência ao TransportTest implementado atualizados. TASK-005 continua DONE; TASK-006 TODO. Mantém-se o limite de integração: send só confirma enfileiramento e close imediato pode descartar resposta pendente; o futuro bootstrap tem de confirmar escrita completa antes de fechar. CORE/JVMs separadas/timeout com descarte silencioso de SYN não foram ensaiados.


## Atualização documental — 27-10-2026

Alinhadas as notas da fase 1 com a revisão da TASK-005 (18 testes TCP, 149 no build). Corrigidas orientações de arranque nos documentos da equipa e a indicação da próxima tarefa: TASK-006, atribuída a Fillmore, com revisão atribuída a Waheed. A matriz da distribuição conserva o seu retrato histórico; estados vivos continuam nos TASK e neste documento. Removidas repetições de handoffs históricos. Alteração apenas documental; resultados de testes são os da revisão V-005, sem nova execução Java.
