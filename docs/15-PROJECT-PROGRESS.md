# Progresso do projeto

Data de criação do plano: 26-10-2026. Estado global da implementação: IN PROGRESS. PHASE-01 e TASK-001–004 concluídas por pedido explícito do utilizador; TASK-005 concluída; PHASE-02 base DONE após validação da equipa, com extensão EXTRA-01 IN PROGRESS antes da fase 3. PHASE-03–07 e TASK-010–031 continuam TODO. TASK-009 DONE após Test, revisão final e integração. TASK-008 DONE após Test, revisão final e integração; TASK-007 DONE; TASK-006 DONE; TASK-032 DONE, após Test aprovado, revisão final por Luis e integração em main. Estados permitidos: TODO, IN PROGRESS, BLOCKED, DONE.

**Utilizador da distribuição histórica:** [Fillmore](team/FILLMORE.md). Nesta revisão de IMP-002, o utilizador/revisor humano é Luis. TASK-006 está concluída após TASK-032, com revisão atribuída a Waheed confirmada pelo utilizador. TASK-007 está DONE após Test/manual humano, revisão final de Denji aprovada e integração local em main (8db7b78). Na TASK-005, Fillmore é o revisor atribuído e Waheed o responsável previsto na distribuição.

**Última tarefa concluída:** [TASK-033](tasks/PHASE-02/TASK-033.md), DONE. IMP-034/V-033: 1065 testes, manual Luis/53 checks, Test e revisão técnica final aceites, versão f650be02f2af10b95328f5b50c3d6941e2e75c15 integrada em main local. [Fecho](../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md).

Histórico anterior à TASK-005 — revisão de prontidão em 27-10-2026: [implementação, build repetido e pendências](../experiments/phase-01-readiness-review.md). Os 131 testes voltaram a passar e não foi identificado bloqueio ao início de TASK-005; PHASE-02 permanece TODO.

Histórico anterior à TASK-005 — correção posterior, por pedido explícito do utilizador: **bootstrap exclusivamente para localização inicial dos vizinhos**. Contrato/código/fixtures e tarefas futuras corrigidos por ADR-013; sem registo vivo, leases, renovações ou avisos de saída no bootstrap. [Evidência atual](../experiments/bootstrap-discovery-correction.md): 131 testes aprovados, 16 tipos ativos, código 3 retirado. PHASE-01 permanece DONE; PHASE-02 não foi iniciada.

Handoff histórico da correção: partir da revisão 4105429a76f67a5acf0579db8719b0ba2460b549 com as notas locais da revisão de prontidão preservadas. Alterados apenas contrato/codec/testes/fixtures e documentação necessária; removido o placeholder vazio NodeRegistry. Build clean package offline com Java 21 passou em 27-10-2026 às 14:34:31 UTC. Sem bloqueios; abrir TASK-005 quando pedida a sua implementação. Os resultados originais das tarefas conservam-se como histórico, com notas apontando para ADR-013.

Distribuição de trabalho: [coordenação e matriz completa](team/00-COORDINATION.md); [Waheed](team/WAHEED.md), [Fillmore](team/FILLMORE.md) e [Denji](team/DENJI.md). A TASK-005 tem Waheed como responsável atribuído e Fillmore como revisor atribuído. TASK-006, atribuída a Fillmore e com Waheed como revisor, está DONE após TASK-032 e confirmação da revisão pelo utilizador. As atribuições não substituem a evidência técnica nem alteram os estados das tarefas. Estados continuam exclusivamente nos TASK e nesta tabela; os documentos pessoais são filas de trabalho, não cópias do progresso.

Handoff da distribuição (27-10-2026, base 2e58ef5): criados quatro Markdown em docs/team/, metadados nas 27 TASK futuras e ligações/regras de revisão nos documentos gerais/fases. Validação documental confirmou cobertura, atribuições sem auto-revisão, dependências sem ciclos e referências válidas; código/CORE preservados. Não foram executados build/testes Java nesta alteração exclusivamente documental. Não há implementação em curso por efeito desta distribuição.

## Fases

Estado Construction atual — IMP-001: Plan/Implement/Test concluídos e resultados
do checkpoint 4 aprovados pelo utilizador, com registo em 2026-11-03T21:40:56Z.
[Checkpoint 5 aprovado e concluído](../memory-bank/bolts/001-base-validada/closure-walkthrough.md)
em 2026-11-03T21:56:48Z, após integração da versão revista em main por fast-forward
(d65f94740ef95ffc67d312f7fbccac7575c86f7a). IMP-001 DONE; bolt/história/unidade
001-base-validada complete, com implemented=true na história. A verificação
não reimplementou TASK-001–005 nem promove requisitos globais a DONE.
Incremento anterior fechado: IMP-002/019-overlay/TASK-032, DONE em 2026-11-04T10:33:12Z, com cinco
etapas concluídas e Test aprovado explicitamente em 2026-11-04T10:19:06Z (registo).
824 testes completos aprovados; execução pessoal por Luis em 2026-11-04T10:15:14Z,
25 CHECK_PASS/MANUAL_PASS e saída Java 0 confirmados. [Fecho concluído](../memory-bank/bolts/019-overlay/closure-walkthrough.md).
G-REVIEW-032 cumprido: revisão final por Luis, distinta da autoria do agente Codex,
e integração em main do commit 09a068f68eca473451e1e80a045c1cf9cf15a1b2.
Fecho pela cascata specsmd após integração. Nenhum outro IMP iniciado.
Código/JAR/XML/execução do agente preservados; log pessoal de Luis separado.

Último incremento fechado: IMP-003/002-overlay/TASK-006, DONE em 2026-11-04T14:18:23Z, iniciado
em 2026-11-04T10:40:58Z. Model aprovado em 2026-11-04T10:50:52Z, incluindo Q-C-04.
[Design preparado para revisão](../memory-bank/bolts/002-overlay/ddd-02-technical-design.md)
em 2026-11-04T11:04:53Z, aprovado em 2026-11-04T11:25:40Z, incluindo requestClose/close compatível.
[Revisão ADR preparada](../memory-bank/bolts/002-overlay/adr-review.md) em 2026-11-04T11:29:33Z:
conformidade com 013/014/015/017/020, alterações internas justificadas, limites e
M01–M18 preservados; sem inviabilidade/divergência material identificada nesta inspeção.
Revisão ADR aprovada em 2026-11-04T11:37:45Z; Implement autorizado e [preparado](../memory-bank/bolts/002-overlay/implementation-walkthrough.md)
em 2026-11-04T12:08:01Z. Cinco ficheiros +1226/−35; 150 hashes restantes e os 18 testes
anteriores de transporte preservados. Último incremento: 856 testes (740+66+31+19),
zero falhas/erros/skips no incremento. Implement aprovado em 2026-11-04T12:18:01Z.
[Relatório Test para revisão](../memory-bank/bolts/002-overlay/ddd-03-test-report.md): clean verify
em 2026-11-04T12:18:10Z, 856 testes/zero falhas; manual Codex em 2026-11-04T12:22:53Z,
34 CHECK_PASS/MANUAL_PASS/saída 0. M01–M18/V-006 com execução técnica local.
Os 155 hashes estão congelados durante Test; listas target/ geradas separadas.
Test aprovado, com registo em 2026-11-04T14:05:14Z, incluindo execução pessoal por Luis
em 2026-11-04T13:58:36Z–13:58:38Z: 34 CHECK_PASS/MANUAL_PASS/JAVA_EXIT=0, mesmo JAR.
[Log pessoal preservado](../experiments/construction/002-overlay/test-manual-luis.log),
separado do agente. [Fecho concluído](../memory-bank/bolts/002-overlay/closure-walkthrough.md).
Revisão final atribuída a Waheed confirmada pelo utilizador — «fecha o bolt, a revisão esta feita» —
com registo em 2026-11-04T14:18:23Z. Versão revista 03345e9e1ab56ec04302676cca6fce32d168223d integrada em main antes do fecho.
Cascata specsmd: bolt/história complete, implemented=true; TASK-006 DONE.
Unidade mantém in-progress; 003-overlay/IMP-004 continua planned, nenhum outro IMP iniciado.

Histórico do início: IMP-004/003-overlay/TASK-007, IN PROGRESS em Model, iniciado em
2026-11-10T14:01:42Z, base Denji 26aaa4ae6775e9c78e1d7658472ccaca0b376427.
[Modelo concreto](../memory-bank/bolts/003-overlay/ddd-01-domain-model.md) preparado em 2026-11-10T14:11:43Z:
entidades/estados, descoberta inicial, um canal confirmado por par, direção do
iniciador, identidade UDP após handshake, reconexão direta e M01–M22 planeados.
155 hashes de Projeto/ preservados; três ficheiros autorizados vazios. Não há
execução V-007/build/regressão/manual nova. Model ainda não aprovado; parar antes
de Design/ADR/Implement/Test. Revisor final atribuído Denji; revisão e integração
antes de DONE. Nenhum outro IMP iniciado nesta continuação, sem commits/push.

[Resultados de IMP-001](../memory-bank/bolts/001-base-validada/test-walkthrough.md):
149 testes aprovados (65+66+18), 45 fixtures, 16 tipos ativos, V-ADD sem erros
e 22 checks manuais. Execução por Luis: [log original preservado](../experiments/construction/001-base-validada/manual-smoke-luis.log),
22 CHECK_PASS e MANUAL_PASS. Naquele incremento, POM/fontes/configuração/fixtures/testes intactos;
a lista gerada createdFiles.lst mudou apenas localmente em Projeto/target/ e
foi excluída dos commits. Inception e IMP-001 em commits separados, com
[proveniência](../experiments/construction/001-base-validada/inception-commit-provenance.json)
e [integração comprovada](../experiments/construction/001-base-validada/integration-proof.json).
DoD preservada; sem GUI/CORE, deploy ou upload presumidos.

| Fase | Tarefas | Estado | Dependência / gate |
|---|---|---|---|
| [PHASE-01](phases/PHASE-01-FOUNDATION.md) | TASK-001–TASK-004 | DONE | quatro tarefas e critérios da fase concluídos |
| [PHASE-02](phases/PHASE-02-OVERLAY.md) | TASK-005–TASK-009, TASK-032 e EXTRA-01/TASK-033–034 | IN PROGRESS na extensão. [Base concluída](phases/PHASE-02-CLOSURE-REVIEW.md) | PHASE-01 DONE |
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
| [TASK-032](tasks/PHASE-02/TASK-032.md) | Parametrização operacional validada | TASK-003, TASK-005 | DONE | V-032: 824 testes e manual Luis/25 checks; revisão final por Luis e versão revista integrada em main |
| [TASK-006](tasks/PHASE-02/TASK-006.md) | Fornecer localização inicial dos vizinhos configurados | TASK-003, TASK-005, TASK-032 | DONE | V-006: 856 testes; manuais Codex/Luis, 34 checks/saída 0 cada; revisão final confirmada pelo utilizador, versão revista integrada em main e bolt complete |
| [TASK-007](tasks/PHASE-02/TASK-007.md) | Gerir handshake, unicidade e reconexão de vizinhos | TASK-005, TASK-006 | DONE | V-007/Test aprovados; clean verify 903 testes, manual Francisco 49 checks; revisão Denji e integração local 8db7b78; bolt complete |
| [TASK-008](tasks/PHASE-02/TASK-008.md) | Compor o processo overlay e encerrar de forma previsível | TASK-003, TASK-006, TASK-007 | DONE | V-008 e clean verify 981/zero falhas, manual de Luis e verificação complementar, V-ADD aprovado, revisão Waheed confirmada pelo utilizador e integração em main |
| [TASK-009](tasks/PHASE-02/TASK-009.md) | Preparar evidência do primeiro marco intermédio | TASK-008 | DONE | V-009 técnico aceite em Test, dispensa Q-C-15, revisão final confirmada e integração em main. [Evidência](../memory-bank/bolts/004-overlay/test-walkthrough.md) |
| [TASK-033 — Extra A](tasks/PHASE-02/TASK-033.md) | Aprender candidatos através dos vizinhos | TASK-009 | DONE | V-033: 1065 testes; manual Luis/53 checks; Test e revisão técnica aceites; integração em main local f650be0 |
| [TASK-034 — Extra B](tasks/PHASE-02/TASK-034.md) | Reparar ligações com a cache aprendida | TASK-033 | IN PROGRESS | Design aprovado; revisão ADR para aprovação. V-034 por executar; precede 005-rotas |
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


## Preparação de bolts Construction — 02-11-2026

Utilizador pediu revisão/correção de artefactos e bolts faseados com validação;
confirmou Construction como executor. [Auditoria](17-INCEPTION-AUDIT.md) e
[guia](18-CONSTRUCTION-BOLTS.md): sete unidades, 27 histórias e 18 bolts planeados.
TASK-001–005 continuam DONE; TASK-006–031 TODO. Nenhum bolt iniciado, build/teste
Java/CORE ou revisão humana nova presumidos. Os 149 testes são evidência histórica.

Consolidadas ADR-013/014; extensão de escrita planeada em TASK-006/ADR-015;
repetições removidas e cenário S3/indicação de arranque corrigidos. Código,
fixtures, XML e distribuição/revisores preservados. Próxima ação de construção
preparada: 001-base-validada; primeiro código novo continua TASK-006 após a base.


## Consolidação ADD e replaneamento — 02-11-2026

Respostas do utilizador 1A/2A/3B/4A incorporadas em [specs](../specs.md) e
[ADD](../add.md). O ADD é a arquitetura normativa para Construction, com 26 ADR,
40 entradas, sete unidades e 33 tarefas IMP/bolts/histórias; os 18 IDs anteriores
conservam-se. A contagem de 18 na preparação anterior é histórica.

TASK-001–005 continuam DONE. TASK-006–031 e a nova TASK-032 continuam TODO;
nenhum IMP/bolt iniciado. Primeiro: 001-base-validada; primeiro código novo:
019-overlay/TASK-032; depois 002-overlay/TASK-006.
TASK-020/025/026/031 têm partes e só fecham após todas elas. Deteção automática
continua TCP; UDP-only é limitação documentada. Sem ferramentas de qualidade novas.
Código Java, fixtures e XML preservados; build Java/GUI/CORE não executados aqui.

## Ajuste de Model do IMP-004 — ERROR (17)

Preparado em 2026-11-10T14:24:46Z, por pedido explícito de revisão: tratamento de ERROR na
[descoberta/handshake/ACTIVE](../memory-bank/bolts/003-overlay/ddd-01-domain-model.md#8-autorização-error-17-e-entrega-ao-consumidor),
com requestId/relatedType/sessão preservados e sem ERROR de resposta. M02/M07/M11
ampliados. [Q-C-05](construction-duvidas.md#q-c-05--receção-de-error-no-imp-004) pendente de decisão sobre
concretizações de retry e resposta inesperada. TASK-007/bolt/história continuam
em Model para revisão; nenhum Design/Java/build/V-007/commit/push iniciado.

## Continuação do IMP-004 — Model/Q-C-05 aprovados, Design para revisão

Aprovação explícita registada em 2026-11-10T14:48:50Z, incluindo todas as decisões da tabela
Q-C-05. [Design preparado](../memory-bank/bolts/003-overlay/ddd-02-technical-design.md) em 2026-11-10T15:00:14Z:
contratos novos nos três ficheiros autorizados, owner serial reutilizável, limites
concretos de retenção/DNS/dial e D01–D22. As propostas Q-C-05 e a revisão Model
anteriores estão superadas pela aprovação; Design/ADR/Implement/Test não aprovados.
155 ficheiros de Projeto/ preservados, sem build/testes/manuais novos ou outro IMP.
V-007, regressão/manual, revisão final atribuída a Denji e integração pendentes.
Parar antes de ADR/Implement/Test, sem commits/push ou fecho.

## Continuação do IMP-004 — Design/Q-C-06 aprovados, ADR para revisão

Aprovação explícita registada em 2026-11-10T15:13:50Z: prazo próprio resolução/bind igual
a control.connectTimeout, antes da fila e separado do dial TCP. Q-C-06 resolvida.
[Revisão ADR](../memory-bank/bolts/003-overlay/adr-review.md) preparada em 2026-11-10T15:20:09Z:
compatível no desenho, sem decisão aceite inviável/nova ADR necessária identificada.
Owner próprio/emprestado, reservas físicas, B/R, dials e shutdown explícito
revistos; Q-C-05 e D01–D22 mantidos. 155 ficheiros de Projeto/ preservados.
Só Model/Design concluídos; parar antes de Implement/Test. V-007/regressão/manual,
revisão final atribuída a Denji e integração pendentes. Sem Java/build/testes,
commits/push/fecho ou outro IMP nesta etapa.

## IMP-004 — Implement para checkpoint

ADR explicitamente aprovada; Q-C-07 resolvida com reposição externa autorizada
de EventLoop/RealDeadlines. [Implement para checkpoint](../memory-bank/bolts/003-overlay/implementation-walkthrough.md)
preparado em 2026-11-10T16:12:36Z. Regressão incremental: `mvn -B -ntp test`,
903 testes (740 configuração +66 codec +31 TCP +19 bootstrap +47 vizinhança),
zero falhas/erros/ignorados. V-007 tem execução automatizada TCP local;
Q-C-05/Q-C-06 e D01–D22 preservados. Três ficheiros do IMP +1745/−0;
BootstrapServer externo +482/−142 separado, 151 ficheiros restantes preservados.
Implement ainda aguarda aprovação; Test/clean verify/JAR/manual não iniciados,
revisão final atribuída a Denji e integração pendentes. TASK/bolt/história/unidade
continuam em progresso, sem implemented=true, commits/push ou fecho.

Validação documental global desta continuação: falhou com três erros em exemplos
preexistentes de `.specsmd/`, sem alteração desses ficheiros. Q-C-08 regista
a pendência; V-ADD não aprovado. A regressão Java de 903 testes é independente.

## IMP-004 — Test preparado, aceitação pendente

Implement aprovado explicitamente pelo utilizador; [relatório Test](../memory-bank/bolts/003-overlay/ddd-03-test-report.md)
preparado em 2026-11-10T16:26:35Z. Clean verify atual: 903 testes (47 de vizinhança),
zero falhas/erros/ignorados. Manual interativo executado pelo agente Codex no
JAR verificado: 49 CHECK_PASS, MANUAL_PASS/CLEANUP_COMPLETE, saída Java 0.
XML/JAR/hash/perfis/log e instruções de repetição pessoal preservados em
experiments/construction/003-overlay/. Código aprovado e ferramentas intactos
durante Test. V-007/D01–D22 têm consolidação técnica local, com limites explícitos.
Q-C-08 continua aberta: V-ADD reprovado com três erros, sem alteração de ferramentas
ou redução do alcance. Test ainda não aprovado; revisão final humana e integração
pendentes. TASK/bolt/história/unidade permanecem em progresso, sem implemented=true,
commits/push/fecho ou GUI/CORE presumidos.

## Checkpoint Test mantido aberto — 2026-11-10T16:29:38Z

O utilizador confirmou que conferiu a evidência técnica, mas manteve Test
por aprovar até executar o manual humano. Não há execução humana registada
por esta confirmação. Checkpoint aberto, current_stage=test, completed=null;
Test não entra em stages_completed. Q-C-08 continua por resolver e V-ADD
continua reprovado. Sem gates externos adicionais; critérios/validações,
revisão humana final e integração continuam exigidos antes do fecho.
Não fechar o bolt nem fazer commits/push.

## Manual humano aprovado — Test ainda aberto

Registo de conferência em 2026-11-10T16:38:44Z.
Manual humano executado e aprovado por Francisco, confirmado pelo utilizador.
Conferência do log original: 49 CHECK_PASS distintos, zero CHECK_FAIL,
MANUAL_PASS CHECKS=49 e CLEANUP_COMPLETE; hash do JAR coincide com o clean verify
e os cinco hashes de configuração coincidem com os ficheiros da execução.
JAVA_EXIT=0 é confirmação explícita do utilizador; esse marcador não consta
do log original do driver, que foi preservado sem edição.
Test continua por aprovar até tratar V-ADD. Q-C-08 continua aberta; correção
mínima preparada e ensaiada apenas em cópia isolada, não aplicada às ferramentas.
Sem fecho/commits/push; revisão humana final e integração continuam pendentes.

[Log Francisco](../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log) · [Conferência](../experiments/construction/003-overlay/manual-Francisco-evidence.json) · [Proposta Q-C-08](../experiments/construction/003-overlay/q-c-08-proposal.md).

## Test para aprovação — Q-C-08 resolvida

Estado registado em 2026-11-10T16:41:30Z. Patch Q-C-08 aplicado exclusivamente
como autorizado: [diff +5/−3](../experiments/construction/003-overlay/q-c-08-applied.diff), três ficheiros, restante conteúdo
local preservado. [V-ADD real](../experiments/construction/003-overlay/q-c-08-v-add-applied.log) passou: saída 0, zero erros;
validador/alcance intactos. Q-C-08 resolvida. As anteriores referências a
V-ADD reprovado são históricas. Código Java/testes/configurações sem alteração;
clean verify de 903 testes e JAR/hash/XML preservados. Manual humano Francisco
cumprido e aprovado, 49 checks/Java 0. [Checkpoint Test atualizado](../memory-bank/bolts/003-overlay/ddd-03-test-report.md).
Test ainda por aprovar, current_stage=test, completed=null; não há fecho,
commits/push ou nova integração. Revisão humana final e integração pendentes.

## IMP-004 — Test aprovado; fecho pendente

Test aprovado explicitamente pelo utilizador, registo em 2026-11-10T16:44:29Z.
903 testes do clean verify aprovados, manual humano de Francisco cumprido
(49 checks/Java 0) e V-ADD aprovado; Q-C-08 resolvida.
[Resumo/diff para revisão humana final](../memory-bank/bolts/003-overlay/final-review-summary.md).
As cinco etapas estão concluídas; bolt in-progress, current_stage=null,
completed=null. TASK/história/unidade continuam em progresso, sem implemented=true
ou DONE. Não há gates externos adicionais; faltam revisão humana final e
integração. Não executar o fecho nem fazer commits/push nesta autorização.

## Revisão Denji pendente — plano de integração

Registo em 2026-11-10T16:50:53Z.
Plano de integração preparado para aprovação, sem staging/commits/push.
Não foi encontrada confirmação registada da revisão humana final atribuída
a Denji; essa revisão mantém-se pendente. Aprovação de Test e manual Francisco
não a substituem. Bolt permanece in-progress/completed=null até revisão e integração.
A lista proposta exclui target/ e alterações locais alheias. Q-C-08 fica
separada como patch/evidência: integrar os três ficheiros de ferramentas completos
importaria conteúdo local anterior alheio (dois não tracked e um substituído
por versão genérica). Essa importação não é proposta nem autorizada.

[Plano/lista exata](../memory-bank/bolts/003-overlay/integration-plan.md).

## Revisão final Denji aprovada — integração por autorizar

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Registo da confirmação](../experiments/construction/003-overlay/denji-final-review-confirmation.json).

## Estado atual — IMP-004 fechado

Registo documental em 2026-11-10T17:12:15Z.
IMP-004/003-overlay/TASK-007 fechado documentalmente por autorização explícita
do utilizador. Integração local comprovada em main: `8db7b78`
(`8db7b78950e444f3c2917844617be4b9b236ec0f`), com 145 caminhos e hashes conferidos.
Test aprovado: clean verify/903 testes, manual humano Francisco/49 checks/Java 0
confirmado pelo utilizador, V-ADD aprovado; revisão final de Denji aprovada sem
alterações pendentes. Q-C-08 resolvida e limitada a patch/evidência na integração.
Bolt complete; história complete/implemented=true; TASK-007 DONE. A unidade
002-overlay mantém in-progress: 020-overlay e 004-overlay continuam planned,
sem início de outro IMP. Fecho documental sem alteração Java ou repetição do manual.
Sem push ou alegação de integração remota/entrega do projeto.

[Fecho documental](../memory-bank/bolts/003-overlay/closure-walkthrough.md). As referências anteriores a estados
pendentes são históricas; não descrevem o estado atual. Não se promovem
requisitos globais, PHASE-02, unidade ou intent a concluídos por este fecho.

## IMP-005 — checkpoint Model, sem implementação

[Modelo preparado](../memory-bank/bolts/020-overlay/ddd-01-domain-model.md) em 2026-11-10T17:31:12Z. Dependências
001-base-validada/003-overlay/019-overlay complete, presentes na base pedida
2ea748e. Checkout 90cded6 preservado; Projeto/ e ADD iguais à base. O tipo
continua Simple; apenas preparação Model de Plan, current_stage=plan,
stages_completed=[], completed=null. Q-C-09 regista a lacuna NODE_LEAVE
na API pública, sem alargar os três ficheiros do ADD ou alterar fundações.
Plan não concluído; parar no checkpoint Model. V-008/regressão/manual/revisão
Waheed/integração pendentes. Sem Java/build/testes, commits/push ou outro IMP.

## IMP-005 — Model aceite e Plan para aprovação

Registado em 2026-11-10T17:38:56Z por decisão explícita do utilizador. Model aceite como preparação
de Plan Simple; Q-C-09 resolvida quanto ao âmbito: extensão mínima de lifecycle
em NeighborManager e teste próprio, sem mudar send() de aplicação ou reabrir IMP-004.
Base operacional 90cded6; referência histórica 2ea748e. Plan preparado e ainda
por aprovar; current_stage=plan, stages_completed=[], sem Implement/Test iniciados.
V-008 com subprocessos reais, regressão, manual humano antes de aprovar Test,
revisão final de Waheed e integração continuam exigidos; sem gates externos adicionais.
Sem Java, staging, commits/push, DONE ou início de outro IMP.

## IMP-005 — Plan aprovado; Implement limitado ao checkpoint interfaces/testes

Registo em 2026-11-10T17:46:21Z. Plan aprovado explicitamente pelo utilizador, incluindo
Q-C-10 (budget), Q-C-11 (gramática) e Q-C-12 (buffers locais). current_stage=implement;
apenas Plan consta em stages_completed. Interfaces/tipos e testes inicialmente
vermelhos preparados; comportamentos novos continuam pendentes. Test não iniciado.
V-008 completo/L01–L12/M01–M18, regressão completa atual, manual humano antes de
aprovar Test, revisão final Waheed e integração continuam exigidos, sem gates
externos adicionais. Bolt aberto, história não implementada, TASK-008 IN PROGRESS.
Sem commits/push, DONE ou início de outro IMP.

## IMP-005 — Implement preparado para aprovação

Registo em 2026-11-10T18:16:24Z. O utilizador aprovou interfaces/testes vermelhos e autorizou
comportamentos nos cinco ficheiros do Plan. Implement preparado, ainda por aprovar;
current_stage=implement, só Plan em stages_completed. Regressão atual Maven test:
979 testes, zero falhas/erros/ignorados, com 44 CLI e 32 lifecycle novos, TCP e
subprocessos reais. Test não iniciado; clean verify e manual humano antes da sua
aprovação continuam pendentes, tal como revisão final Waheed e integração.
Q-C-09–12 preservadas; send() e testes históricos intactos, sem alteração wire/caps
ou dependências. Não há gates externos adicionais. Bolt aberto, história ainda
não implementada, TASK-008 IN PROGRESS e unidade in-progress. Sem commits/push
ou outro IMP.

## IMP-005 — correção terminal no checkpoint Implement

Registo em 2026-11-10T18:39:44Z. Retirado anúncio prematuro STOPPED/exit: última mensagem
passa a progresso SHUTDOWN/closing-output, sem código final. Resultado terminal
só após conhecer o fecho do writer. Dois testes novos reproduzidos primeiro
vermelhos e depois verdes; regressão Maven test atual: 981 testes, zero falhas,
erros ou ignorados. Budget global, send(), manager, wire/caps e testes históricos
preservados. Implement reapresentado, por aprovar; Test não iniciado. Sem staging,
commits/push ou DONE. clean verify, manual humano, revisão Waheed e integração
continuam pendentes, sem gates externos adicionais.

## Validação interrompida — WIP e ponto de retoma

Registo em 2026-11-10T19:05:26Z, por pedido explícito do utilizador. Pausa da validação;
manual humano parcial, não aprovado, e Test por aprovar. Quatro logs pessoais
iniciais/ficheiros de saída preservados, sem inferir roteiro concluído.
clean verify já executado: 981/zero falhas/erros/ignorados. Ponto de retoma em
experiments/construction/020-overlay/RESUME.md. Guardar WIP local só deste
incremento/evidência; target/ e alterações alheias excluídos; sem push.
Bolt permanece in-progress/current_stage=implement/completed=null; só Plan em
stages_completed. História não implementada, TASK-008 IN PROGRESS. Revisão final
Waheed e integração continuam pendentes; sem gates externos adicionais ou DONE.

## IMP-005 — Implement aprovado; Test para revisão

Aprovação de Implement registada em 2026-11-10T22:05:26Z. O [relatório de Test](../memory-bank/bolts/020-overlay/test-walkthrough.md)
consolida V-008, clean verify (981 testes, zero falhas/erros/ignorados), o manual
de Luis e a verificação complementar do agente, com as sondagens preliminares
preservadas. Test aguarda aprovação. A revisão final de Waheed e a integração
da versão revista continuam pendentes.

V-ADD desta sessão: saída 1 por ligação quebrada para um template local de
`.specsmd/`, reproduzida no estado de entrada. A regressão Java passou.

## IMP-005 — Test aprovado e fecho preparado

Registo em 2026-11-10T22:22:56Z. O utilizador aprovou Test, incluindo V-008,
o manual de Luis, a verificação complementar e V-ADD após Q-C-13.
Waheed tem a revisão final atribuída. A integração e o fecho dependem dessa
revisão. [Pacote para revisão final](../memory-bank/bolts/020-overlay/closure-walkthrough.md).

## IMP-005 — Fecho concluído

Registo de 2026-11-10T22:30:30Z. Revisão final atribuída a Waheed dada como concluída
por confirmação do utilizador. A versão revista `8c30b04` foi integrada
por fast-forward em main. Bolt e história concluídos, TASK-008 DONE.
[Registo de fecho](../memory-bank/bolts/020-overlay/closure-walkthrough.md).

## IMP-006 — Plan para revisão

Registo de 2026-11-10T22:38:06Z. As dependências estão concluídas e integradas
na base `89d0201`. O Plan define o roteiro do marco, a captura e a repetição
por outro elemento. V-009 continua por executar e a informação docente
sobre a primeira avaliação está pendente. [Plan do IMP-006](../memory-bank/bolts/004-overlay/implementation-plan.md).

## IMP-006 — Plan aprovado, Implement para revisão

Registo de 2026-11-10T22:50:50Z. O utilizador aprovou Plan e autorizou Implement.
O roteiro, os perfis e os auxiliares estão preparados. Q-C-14 regista as
permissões da bancada ainda por confirmar para a captura e o corte TCP.
O ensaio da overlay e a repetição de V-009 por outro elemento estão pendentes.
A data e o conteúdo da avaliação aguardam informação docente.
[Implement do IMP-006](../memory-bank/bolts/004-overlay/implementation-walkthrough.md).

## IMP-006 — Implement aprovado, Test em execução

Registo de 2026-11-10T23:09:30Z. O utilizador aprovou Implement incluindo a guarda
do processo de captura. A sondagem inicial do agente falhou. A sondagem
de Luis na bancada Linux local passou para captura e corte.
Test aguarda a evidência de V-009 e a repetição por outro elemento.
[Registo de Test](../memory-bank/bolts/004-overlay/test-walkthrough.md).

## IMP-006 — Fecho concluído

Registo de 2026-11-10T23:27:50Z. Test foi aceite com o ensaio técnico de V-009
e a repetição foi dispensada em Q-C-15. A revisão atribuída a Fillmore
foi dada como concluída por indicação do utilizador. A versão revista
`cc478d1` foi integrada em main. Bolt e história completos,
TASK-009 DONE. A data e o conteúdo da avaliação aguardam informação docente.
[Registo de fecho](../memory-bank/bolts/004-overlay/closure-walkthrough.md).

## PHASE-02 — Fecho após validação da equipa

Em 2026-11-10T23:39:09Z foi registada a confirmação do utilizador sobre a validação
da equipa. PHASE-02 DONE, com as seis tarefas integradas em `223ea6f`.
O ensaio de V-009 e a regressão arquivada de V-008 sustentam o fecho.
G-ASSESSMENTS mantém a data e o conteúdo da avaliação pendentes.
PHASE-03 está TODO, com Model de 005-rotas como ponto de retoma.
[Dossier de fecho](phases/PHASE-02-CLOSURE-REVIEW.md).

## EXTRA-01 — Reconstrução dinâmica antes de 005-rotas

Registo de 2026-11-11T12:11:34Z. Novo [IMP-034 / 034-overlay-extra](../memory-bank/bolts/034-overlay-extra/bolt.md)
em Model. A base fechada de PHASE-02 mantém os resultados; a extensão
TASK-033 está IN PROGRESS. V-033 por executar. Catálogo inicial aprovado
e continuidade real de vídeo adiada para routing/streaming por decisão
do utilizador. 005-rotas aguarda a conclusão e integração deste extra.

## EXTRA-01 — Descoberta entre vizinhos e dois bolts

Registo de 2026-11-11T12:26:03Z. O utilizador substituiu o catálogo inicial por
divulgação limitada dos vizinhos diretos e cache aprendida com prazo.
034-overlay-extra aprende candidatos; 035-overlay-extra repara ligações.
Ambos precedem 005-rotas. ADR-033 e a extensão de protocolo são propostas
para Design. V-033/V-034 por executar.

## IMP-034 — Design para revisão

Registo de 2026-11-11T12:49:29Z. Model aprovado explicitamente; apenas Design autorizado.
[Design](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md):
isolamento TCP/dataPort OTD1, vínculo ao HELLO, geração por sessão, TTL 60 s,
cache limitada e conflitos, recursos comuns e callbacks correlacionados.
[Q-C-17](construction-duvidas.md#q-c-17--design-da-descoberta-extra-proposta)
e ADR-033/contratos estão propostos, G-EXTRA-ADR pendente. IMP-034 continua
IN PROGRESS em Design, sem Java novo, execução V-033/regressão/manual, revisão
final ou integração. IMP-035/V-034 e 005-rotas não foram iniciados.

## IMP-034 — Revisão ADR para aprovação

Registo de 2026-11-11T13:44:19Z. O utilizador aprovou Design e Q-C-17, autorizando apenas
ADR. [Revisão preparada](../memory-bank/bolts/034-overlay-extra/adr-review.md):
coerência de isolamento OTD1, token HELLO como correlação, TTL/retenção e
recursos físicos partilhados; exceção da chave extra explicitada em ADR-033.
ADR-033 permanece Proposta e G-EXTRA-ADR exige aprovação formal da ADR e
contratos afetados antes de Implement. current_stage=adr; só Model/Design
concluídos. V-033/D01–D12, regressão atual, manual, revisão humana final e
integração pendentes. IMP-035 e 005-rotas não iniciados.

## IMP-034 — Implement interrompido; revisão do transporte

Registo de 2026-11-11T14:26:58Z. O utilizador mandou usar o TCP persistente de controlo e
parar OTD1. [Design revisto](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md)
e ADR-034 propostos; G-EXTRA-ADR reaberto. Aprovações anteriores ficam históricas.
163 ficheiros de Projeto/ e 87 evidências anteriores preservados no
[arquivo](../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md#limpeza-autorizada).
Houve execução parcial anterior, não conclusão: 763 testes selecionados aprovados;
última execução de transporte com 58 testes e uma falha num caso novo; compilação
posterior sem testes passou. Isto não valida o novo transporte e não fecha V-033.
Esta revisão só altera documentos; nenhum código/teste/target foi continuado.
IMP-035/005-rotas não iniciados; sem commits, push ou integração.

## IMP-034 — margem e falhas da ADR-034 em revisão

Registo de 2026-11-11T14:51:37Z. [Design ajustado](../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md):
margem M=1, quota enfileirada E(Q), Q=1 em base sem descoberta, quatro tickets
globais mantidos. Conteúdo extra completo de sessão atual pode ser rejeitado
atomicamente sem fecho; framing/truncamento/identidade incompatível continuam
terminais. Oráculos D07/D08/D11 ampliados, ainda não executados. ADR-034 permanece
Proposta, G-EXTRA-ADR pendente. Código/evidências congelados, sem outro IMP.

## IMP-034 — extra obrigatório, sem degradação automática

Registo de 2026-11-11T15:15:24Z. O utilizador rejeitou Q=1/incompatibilidade passarem a base.
Design/ADR-034/configuração revistos: normal todos overlay true, Q>=2 offline,
supported=required=1 e seleção obrigatória. Base/API antigos preservam Q=1.
Erro/fase requerida explícitos, sem fallback; margem e rejeição de conteúdo
mantidas. Novo preflight normal e atualização futura de Projeto/README.md no
âmbito. Oráculos reforçados, não executados; G-EXTRA-ADR pendente. Sem Java,
commits/push ou outro IMP.

## IMP-034 — requisito intrínseco em todos os caminhos

Registo de 2026-11-11T15:35:59Z. Utilizador retirou a ativação opcional e ACTIVE_BASE operacional.
Design/ADR-034/REQ-025 aplicam descoberta e Q>=2 a todos os nós overlay, loaders,
construtores e perfis; ausência de chave não desliga, false é erro. Bootstrap
mantém lookup e UDP mantém vídeo. [Migração dos testes justificada](../memory-bank/bolts/034-overlay-extra/test-contract-migration.md)
sem mudanças executadas: preservar bytes/testes v1 usados e invariantes; adaptar
pressupostos opcionais. AppTest incluído no âmbito futuro para o peer artificial.
Gate pendente, sem Java/commits/push; IMP-034 aprende e IMP-035 repara depois.

## IMP-034 — Design/ADR com JAR único e isolamento por peer

Registo de 2026-11-11T17:22:59Z. Utilizador clarificou programa único/mesma versão em todos os
nós. Descoberta obrigatória, sem suporte operacional a versões antigas/base/
fallback. Corrigida a consequência proposta de FAILED global: input inválido
rejeita só a tentativa, com diagnóstico/estado limitado por peer; CLI, sessões
saudáveis e aprendizagem continuam. D02b–d/D11a incluem retry/backoff e distinção
de ausência temporária, com A ACTIVE enquanto B é rejeitado.
Design/ADR-034/contratos revistos para aprovação; ADR-034 Proposta, gate pendente.
Sem novos testes Java/manuais ou retoma de código; fontes e evidência preservadas.
IMP-034 aprende/retém, IMP-035 repara depois. Nenhum outro IMP iniciado.

## IMP-034 — revisão de TCP integralmente v2

Registo de 2026-11-11T17:47:08Z. Decisão explícita: TCP exclusivamente v2 em bootstrap,
handshake, mensagens normais e descoberta; um contrato/codec, 18 tipos explícitos,
código 3 rejeitado, sem runtime v1/fallback. Arquivo v1 e fixtures v2 independentes
preparados; migração dos testes justificada com garantias preservadas. Bootstrap
continua só inicial, UDP OTT1 inalterado, descoberta obrigatória e isolamento por
peer/CLI saudável mantidos; cache/TTL/proveniência/shutdown conservados.
IMP-034 aprende, IMP-035 repara depois. Design/ADR-034/âmbito para aprovação;
Implement suspenso, sem execução Java, commits, push ou outro IMP.

## Aprovação e retoma de Implement — TCP v2

Registo de 2026-11-11T17:55:50Z; instante do registo, não data atribuída à mensagem humana.
O utilizador aprovou explicitamente Design revisto, ADR-034, âmbito dos 29
caminhos e migração justificada dos testes/recursos para TCP exclusivamente v2.
ADR-034 Aceite: substitui o contrato TCP v1 e as propostas OTD1/mista/ativação
opcional afetadas; v1/fixtures ficam arquivados, UDP OTT1 permanece inalterado.
G-EXTRA-ADR satisfeito para IMP-034; formalizada revisão ADR e retoma apenas
Implement. Um contrato/codec v2 no bootstrap e vizinhos, descoberta obrigatória,
com framing/caps/identidade/unicidade/FIFO/flush/isolamento/shutdown e cache/TTL/
proveniência preservados. Testes incrementais com logs reais autorizados.
Parar para revisão de Implement antes de Test/fecho; V-033, regressão atual,
manual, revisão humana e integração continuam exigidos. IMP-035 mantém checkpoint
próprio; sem commits/push ou início de outro IMP.

## IMP-034 — Implement pronto para revisão

Registo de 2026-11-11T18:57:34Z. ADR-034 Aceite e G-EXTRA-ADR satisfeito apenas para IMP-034.
Controlo TCP exclusivamente v2 implementado no bootstrap, handshake, mensagens
normais e descoberta obrigatória; um codec/FIFO/writer, sem canal OTD1/runtime
v1 ou fallback. Cache/TTL/proveniência, isolamento e shutdown preservados.
Conjunto incremental JDK 21: 1062 testes, zero falhas/erros; inclui TCP local e
JVM da CLI. Corrigida a corrida descoberta/shutdown detetada por teste existente,
com novo oráculo controlado; falhas/sondagens anteriores mantidas.
66 fixtures operacionais (50 TCP v2 independentes e 16 UDP intactas); arquivo
v1 de 45 fixtures verificado separadamente. Ver walkthrough do bolt para logs,
XML, hashes, diff e mapa D01–D12; não confundir teste incremental com clean verify.
Pendente aprovação humana de Implement antes de Test. V-033 completo, clean
verify/regressão atual, manual reproduzido, revisão final e integração continuam
necessários para fecho. Sem commits/push ou outro IMP; IMP-035 continua planeado.

## IMP-034 — correção de correlação D07e

Registo de 2026-11-11T19:18:38Z. Corrigidos o descarte de token HELLO antigo
e de geração antiga, preservando a sessão e a consulta corrente. 142 testes
incrementais afetados passaram com JDK 21/TCP local; os 1062 da apresentação
anterior ficam como histórico. [Walkthrough, red/green e evidência](../memory-bank/bolts/034-overlay-extra/implementation-walkthrough.md).
Implement aguarda revisão. Test/clean verify/manual humano/revisão final/integração
continuam pendentes; sem DONE, commits/push ou início de IMP-035.

## Estado atual — IMP-034 em Test para repetição humana

Registo de 2026-11-11T19:31:01Z. Implement aprovado; clean verify JDK 21 executado
com 1065 testes, zero falhas/erros/skips. Manual Codex: 53 CHECK_PASS,
MANUAL_PASS, saída Java 0, incluindo TTL real. [Relatório/evidência](../memory-bank/bolts/034-overlay-extra/ddd-03-test-report.md).
Repetição pessoal de Luis, aprovação de Test, revisão final e integração
pendentes. Bolt/história/unidade in-progress, sem DONE/commits/push/IMP-035.

## IMP-034 — Test aceite e revisão final

2026-11-11T19:42:24Z: manual humano Luis/53 checks, MANUAL_PASS e saída 0, mesmo JAR. Test aceite pelo pedido explícito de revisão/integração/fecho. Revisão técnica final do agente validador concluída; nenhuma revisão atribuída a Fillmore/Waheed. Q-C-19 autoriza retirar os arquivos TCP v1; TCP v2/UDP OTT1 e evidências finais preservados. Integração local pendente. [Fecho](../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md).

## IMP-034 — fecho atual

2026-11-11T19:45:51Z: TASK-033 DONE, bolt/história complete após integração local f650be02f2af10b95328f5b50c3d6941e2e75c15. [Prova](../experiments/construction/034-overlay-extra/closure-integration.json). Test/manual humano e revisão técnica concluídos, Q-C-19 aplicada; sem push. Sete bolts completos no projeto, 28 planned. IMP-035 não iniciado, unidade/PHASE-02/REQ-025 continuam em curso.

## IMP-035 — Model para revisão

Registo de 2026-11-11T22:22:21Z. Apenas Model iniciado por autorização do utilizador.
Pré-requisito IMP-034 complete e integrado localmente na base 22622c1.
Q-C-20: reparação autónoma só ao ficar isolado (zero ACTIVE), atendimento
possível por peer saudável. Descoberta obrigatória/TCP v2, sem modo opcional.
[Model e M01–M18 propostos](../memory-bank/bolts/035-overlay-extra/ddd-01-domain-model.md). G-EXTRA-ADR próprio pendente: contrato
de intenção/admissão e eventual âmbito adicional exigem revisão antes de código.
Model não aprovado; V-034 não executado, sem Java/Maven/manual/commits/push.
Parar antes de Design. Unidade/REQ-025/extra continuam em curso; 005-rotas não iniciado.

## IMP-035 — Model aprovado; Design para revisão

Registo de 2026-11-11T22:41:27Z. O utilizador aprovou Model e clarificou que o destino
pode estabelecer a ligação pedida pelo isolado, conservando sessões saudáveis.
[Design proposto/Q-C-21](../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md) concretiza intenção/admissão unilateral,
permissões anteriores às falhas, promoção/retorno pelo menor ID, limites e
D01–D24. Âmbito8→30 e minuta para futuraADR-035 ainda propostos; o contrato
integrado/206 hashes de Projeto/JAR continuam intactos.
Só Model concluído, current_stage=design. Gate próprio, Design/ADR/Implement/
V-034/manual/revisão final/integração pendentes. Sem Java/Maven/commits/push.

## IMP-035 — Design direto simplificado para revisão

Registo de 2026-11-11T23:17:55Z. Revisão pedida pelo utilizador: isolado inicia e completa
na mesma ligação mesmo com ID maior, sem retorno. [Design/Q-C-22](../memory-bank/bolts/035-overlay-extra/ddd-02-technical-design.md)
propõe um tipo20/ACK6, admissão cooperativa sem permissões/registry e eleição
do menor originator só entre saídas pendentes; ACTIVE nunca substituída.
Âmbito8→21/ADR-035/contratos ainda propostos; versão anterior preservada nas
evidências. Só Model concluído; nenhum Java/Maven/V-034/Implement/Test/commit/push.

## IMP-035 — revisão ADR para aprovação antes de Implement

Registo de 2026-11-11T23:28:20Z. Model/Design concluídos; Design revisto, Q-C-22/
política cooperativa e21 caminhos aprovados explicitamente. [RevisãoADR](../memory-bank/bolts/035-overlay-extra/adr-review.md)
formaliza ADR-035 Proposta e substituição parcial019/033/034, mesma ligação e
ACTIVE preservada. APIs reais conferidas sem alteração de fontes.
Só revisão ADR autorizada; aprovação formalADR/G-EXTRA-ADR ainda pendentes.
V-034/regressão/manual humano com mortes reais/revisão final/integração obrigatórios.
Sem Implement/Test/fecho/commits/push/outro IMP.

## IMP-035 — Implement para revisão antes de Test

Registo de 2026-11-12T00:04:01Z. Revisão ADR/ADR-035/substituição parcial019/033/034 aprovadas
explicitamente; ADR-035 Aceite e G-EXTRA-ADR satisfeito para IMP-035.
[Walkthrough, mapa D01–D20 e evidência incremental](../memory-bank/bolts/035-overlay-extra/implementation-walkthrough.md).
Reparação20/6 no mesmo TCP v2, candidatos aprendidos, menor originator só pendente,
ACTIVE preservada e quotas existentes. 78 fixtures operacionais, snapshots/UDP intactos.
Sem clean verify ou manual humano de mortes reais nesta etapa; V-034 completo,
regressão, revisão final e integração pendentes. Não marcar DONE nem iniciar Test.
Sem commits, push ou outro IMP; 005-rotas continua bloqueado.

## IMP-035 — confirmação concorrente do grau corrigida

Registo 2026-11-12T00:23:42Z. [Complemento Implement/D13](../experiments/construction/035-overlay-extra/implement-degree-walkthrough.md):
ACTIVE≤min(C,63) validado na confirmação comum de HELLO5/20, sem Up do excedente
ou remoção dos saudáveis. Oito ordens de flush/ACK e retenção física com64 sockets;
175 testes incrementais afetados passaram. Evidências anteriores preservadas.
Sem passagem a Test, commits/push ou fecho. V-034 completo/regressão/manual humano
com mortes reais/revisão final/integração permanecem pendentes.

## IMP-035 — Test à espera de repetição humana

Registo 2026-11-12T00:39:35Z. Implement aprovado; [Test](<../memory-bank/bolts/035-overlay-extra/ddd-03-test-report.md>) executou clean verify
JDK21/1113 testes sem falhas e preservou11 XML/JAR novo. Manual Codex68
CHECK_PASS/MANUAL_PASS: JVM por nó, mortes SIGKILL sequenciais, bootstrap
parado, S–C/R1–C únicos, sobreviventes originais e S–R1 preservada. Duas tentativas
incompletas mantidas. Execução pessoal de Luis/aprovação de Test/revisão final/
integração pendentes. Sem DONE/commits/push ou início de outro IMP.

## IMP-035 — Test aprovado; integração autorizada

Registo 2026-11-12T10:35:23Z. Luis aprovou Test/1113 e o manual humano20261112T102931Z-pr_nynth:
66 CHECK_PASS/MANUAL_PASS, passed=true/no_processes_pending=true. Execução
humana anterior preservada; nenhuma repetição. Cinco etapas concluídas.
[Resumo para revisão humana final](<../memory-bank/bolts/035-overlay-extra/final-review-summary.md>). Commit/main autorizados;
revisão humana final da implementação e fecho pendentes. Bolt/história/unidade
continuam in-progress, sem implemented=true/DONE ou início de005-rotas.
