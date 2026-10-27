# Denji — Implementação e revisões

Data de distribuição: 27-10-2026. Este documento atribui responsabilidade futura; não altera estados nem atesta trabalho executado. As TASK-001–004/PHASE-01 já estão DONE e não são redistribuídas retroativamente. Os estados vivos pertencem aos ficheiros TASK e a [15 — Progresso](../15-PROJECT-PROGRESS.md).

## 1. Papel e ponto de partida

Composição da aplicação, diagnóstico, cliente/reprodução e integração. Coordena marcos, evidências, terceiro cenário CORE, análise final e defesa; os outros dois fornecem os dados das suas áreas.

Implementações principais: **10 tarefas**. Revisões principais: **7 tarefas**. A contagem não mede esforço; ajustar atribuições explicitamente nos documentos se a equipa redistribuir trabalho.

TASK-008 aguarda TASK-003/006/007 integradas. Agora pode preparar a matriz de CLI, arranque/encerramento e reconexão e estudar como rever TASK-007. Não iniciar código de TASK-008 ou cliente/vídeo antecipadamente.

Não avançar para a próxima tarefa própria só por ser o menor número da sua lista. Exigir todas as dependências integradas e o fecho da fase anterior. Se não há tarefa pronta, preparar casos/revisões/documentação da fase atual; não escrever código de tarefas dependentes ou futuras para contornar o plano.

## 2. Leitura obrigatória e regras partilhadas

Ler [coordenação e matriz completa](00-COORDINATION.md), [guia da IA](../AI-IMPLEMENTATION-GUIDE.md), [progresso](../15-PROJECT-PROGRESS.md), [roadmap](../08-IMPLEMENTATION-ROADMAP.md), [arquitetura](../04-ARCHITECTURE.md), [modelo](../06-DATA-MODEL.md), [contratos](../07-API-AND-INTERFACES.md), [ADR-013 e decisões](../12-RISKS-AND-DECISIONS.md) e [Definition of Done](../14-DEFINITION-OF-DONE.md). Depois ler a tarefa atual na íntegra, os requisitos relacionados e a sua fase.

A evidência anterior da fase 1 é [131 testes aprovados](../../experiments/bootstrap-discovery-correction.md); não prova rede/CORE/vídeo nem substitui a execução necessária na sua tarefa. Atribuições não autorizam enviar mensagens externas ou submeter artefactos automaticamente.

## 3. Lista completa de implementações atribuídas

| Ordem própria | Tarefa | Fase | Dependências a integrar antes | Revisor principal |
|---:|---|---|---|---|
| 1 | [TASK-008](../tasks/PHASE-02/TASK-008.md) — Compor o processo overlay e encerrar de forma previsível | [PHASE-02](../phases/PHASE-02-OVERLAY.md) | [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-006](../tasks/PHASE-02/TASK-006.md) (Fillmore), [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed) | [Waheed](WAHEED.md) |
| 2 | [TASK-009](../tasks/PHASE-02/TASK-009.md) — Preparar evidência do primeiro marco intermédio | [PHASE-02](../phases/PHASE-02-OVERLAY.md) | [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji) | [Fillmore](FILLMORE.md) |
| 3 | [TASK-013](../tasks/PHASE-03/TASK-013.md) — Diagnosticar percursos pela tabela de rotas | [PHASE-03](../phases/PHASE-03-ROUTING.md) | [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore) | [Fillmore](FILLMORE.md) |
| 4 | [TASK-018](../tasks/PHASE-04/TASK-018.md) — Preparar o segundo marco com árvores e adaptação | [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md) | [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji), [TASK-013](../tasks/PHASE-03/TASK-013.md) (Denji), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed), [TASK-017](../tasks/PHASE-04/TASK-017.md) (Fillmore) | [Waheed](WAHEED.md) |
| 5 | [TASK-022](../tasks/PHASE-05/TASK-022.md) — Reconstituir frames com buffers e prazo limitados | [PHASE-05](../phases/PHASE-05-STREAMING.md) | [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed), [TASK-021](../tasks/PHASE-05/TASK-021.md) (Waheed) | [Waheed](WAHEED.md) |
| 6 | [TASK-023](../tasks/PHASE-05/TASK-023.md) — Apresentar catálogo e vídeo numa janela Swing | [PHASE-05](../phases/PHASE-05-STREAMING.md) | [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-022](../tasks/PHASE-05/TASK-022.md) (Denji) | [Fillmore](FILLMORE.md) |
| 7 | [TASK-024](../tasks/PHASE-05/TASK-024.md) — Integrar duas origens e quatro streams ponta a ponta | [PHASE-05](../phases/PHASE-05-STREAMING.md) | [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji), [TASK-020](../tasks/PHASE-05/TASK-020.md) (Fillmore), [TASK-021](../tasks/PHASE-05/TASK-021.md) (Waheed), [TASK-022](../tasks/PHASE-05/TASK-022.md) (Denji), [TASK-023](../tasks/PHASE-05/TASK-023.md) (Denji) | [Waheed](WAHEED.md) |
| 8 | [TASK-027](../tasks/PHASE-06/TASK-027.md) — Validar cenário complexo com duas origens e cinco clientes | [PHASE-06](../phases/PHASE-06-CORE-VALIDATION.md) | [TASK-026](../tasks/PHASE-06/TASK-026.md) (Fillmore) | [Fillmore](FILLMORE.md) |
| 9 | [TASK-028](../tasks/PHASE-06/TASK-028.md) — Recolher métricas e avaliar resultados e limites | [PHASE-06](../phases/PHASE-06-CORE-VALIDATION.md) | [TASK-025](../tasks/PHASE-06/TASK-025.md) (Waheed), [TASK-026](../tasks/PHASE-06/TASK-026.md) (Fillmore), [TASK-027](../tasks/PHASE-06/TASK-027.md) (Denji) | [Fillmore](FILLMORE.md) |
| 10 | [TASK-031](../tasks/PHASE-07/TASK-031.md) — Preparar defesa e checklist de submissão humana | [PHASE-07](../phases/PHASE-07-DELIVERY.md) | [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji), [TASK-030](../tasks/PHASE-07/TASK-030.md) (Waheed) | [Waheed](WAHEED.md) |

A ordem acima é uma fila pessoal; entre duas tarefas próprias podem existir tarefas de outros membros que é obrigatório aguardar. O revisor verifica também os critérios do ficheiro TASK e não apenas os pontos resumidos aqui.

## 4. O que entregar e quando pedir revisão

### TASK-008 — Compor o processo overlay e encerrar de forma previsível

- **Especificação completa:** [TASK-008](../tasks/PHASE-02/TASK-008.md); fase [PHASE-02](../phases/PHASE-02-OVERLAY.md).
- **Iniciar depois de:** [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-006](../tasks/PHASE-02/TASK-006.md) (Fillmore), [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Permitir iniciar e terminar cada papel manualmente, sem streaming antecipado.
- **Âmbito indicado na tarefa:** Existentes: app/OttApplication.java, overlay/OverlayNode.java, client/StreamingClient.java como ponto de composição futuro; planeado: app/LifecycleTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Compor os serviços já integrados, sem reimplementar transporte ou bootstrap. Garantir CLI coerente, estado serial e shutdown limitado; NODE_LEAVE só vai para vizinhos, nunca para bootstrap.
- **Validação obrigatória:** V-008: subprocessos locais iniciam por CLI, mostram vizinhos e terminam; configuração inválida não abre porto; interrupção e erro de um peer não deixam processo preso.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-009](../tasks/PHASE-02/TASK-009.md), [TASK-010](../tasks/PHASE-03/TASK-010.md), [TASK-013](../tasks/PHASE-03/TASK-013.md), [TASK-024](../tasks/PHASE-05/TASK-024.md).
- **Pontos que o revisor deve comprovar:**
  - CLI/papel/configuração inválidos falham claramente sem iniciar serviços indevidos.
  - Encerramento termina threads/sockets e só notifica vizinhos diretos.

### TASK-009 — Preparar evidência do primeiro marco intermédio

- **Especificação completa:** [TASK-009](../tasks/PHASE-02/TASK-009.md); fase [PHASE-02](../phases/PHASE-02-OVERLAY.md).
- **Iniciar depois de:** [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Conseguir demonstrar construção da overlay e explicar protocolo de controlo.
- **Âmbito indicado na tarefa:** Planeados: experiments/milestone-1/README.md, configurações e roteiro; existentes: docs/15 e docs/12. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Recolher evidência real do primeiro marco, incluindo reconexão com bootstrap parado após descoberta. Confirmar com a equipa a situação das instruções docentes, sem inventar datas ou resultados de rotas/vídeo.
- **Validação obrigatória:** V-009: outro elemento consegue repetir a demonstração e identificar consulta inicial, handshake, saída e reconexão nos logs; os nós já descobertos reconectam com bootstrap parado, sem tráfego periódico para ele.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-010](../tasks/PHASE-03/TASK-010.md), [TASK-018](../tasks/PHASE-04/TASK-018.md), [TASK-031](../tasks/PHASE-07/TASK-031.md).
- **Pontos que o revisor deve comprovar:**
  - Outro elemento repete a demonstração nos comandos registados.
  - Logs distinguem consulta inicial, HELLO, saída e reconexão, sem alegar routing/streaming.

### TASK-013 — Diagnosticar percursos pela tabela de rotas

- **Especificação completa:** [TASK-013](../tasks/PHASE-03/TASK-013.md); fase [PHASE-03](../phases/PHASE-03-ROUTING.md).
- **Iniciar depois de:** [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Verificar o caminho escolhido sem depender de vídeo ou ativação de árvore.
- **Âmbito indicado na tarefa:** Existentes: app/OttApplication.java, routing/RoutingService.java; planeado: routing/RouteTraceTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Implementar diagnóstico que segue a tabela real e termina por resposta ou timeout. Não ativar subscrições. A direção cliente→origem é a opção atual do plano; explicitar a diferença da sugestão servidor→destinos do enunciado.
- **Validação obrigatória:** V-013: duas rotas alternativas produzem visited correto; NO_ROUTE e TTL são explícitos; não há ativação nem saída de vídeo após trace.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-014](../tasks/PHASE-04/TASK-014.md), [TASK-018](../tasks/PHASE-04/TASK-018.md).
- **Pontos que o revisor deve comprovar:**
  - Caminho/logs correspondem às tabelas; NO_ROUTE, TTL e timeout terminam de forma explícita.
  - Trace não cria ativação nem envio de vídeo; direção do diagnóstico é documentada.

### TASK-018 — Preparar o segundo marco com árvores e adaptação

- **Especificação completa:** [TASK-018](../tasks/PHASE-04/TASK-018.md); fase [PHASE-04](../phases/PHASE-04-FLOW-MANAGEMENT.md).
- **Iniciar depois de:** [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji), [TASK-013](../tasks/PHASE-03/TASK-013.md) (Denji), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed), [TASK-017](../tasks/PHASE-04/TASK-017.md) (Fillmore), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Demonstrar gestão de fluxos e protocolo de controlo antes da integração final.
- **Âmbito indicado na tarefa:** Planeados: experiments/milestone-2/README.md, logs e sequência de falhas; existentes: docs/15 e docs/12. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Produzir demonstração repetível de ativação, poda, falha e adaptação. Distinguir dados sintéticos de diagnóstico de vídeo real; recolher contributos dos dois autores de routing e confirmar expectativas do marco.
- **Validação obrigatória:** V-018: reprodução independente da ativação/poda/failover usando logs e snapshots; revisão do material contra os critérios de gestão de fluxos e controlo do PDF.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-019](../tasks/PHASE-05/TASK-019.md), [TASK-024](../tasks/PHASE-05/TASK-024.md), [TASK-031](../tasks/PHASE-07/TASK-031.md).
- **Pontos que o revisor deve comprovar:**
  - Outro elemento reproduz logs e snapshots de ativação/poda/failover.
  - Limites do marco e situação docente ficam explícitos; dados sintéticos não são apresentados como vídeo.

### TASK-022 — Reconstituir frames com buffers e prazo limitados

- **Especificação completa:** [TASK-022](../tasks/PHASE-05/TASK-022.md); fase [PHASE-05](../phases/PHASE-05-STREAMING.md).
- **Iniciar depois de:** [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed), [TASK-021](../tasks/PHASE-05/TASK-021.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Recuperar frames completos sob reordenação e descartar incompletos sem crescer memória.
- **Âmbito indicado na tarefa:** Existente: client/StreamingClient.java; planeados: client/FrameAssembler.java e FrameAssemblerTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Reconstruir com limite de montagens e deadline inalterável por duplicados. Validar metadados, reordenação, soma de bytes e mudança de época; não permitir crescimento ilimitado ou bloqueio por frame perdido.
- **Validação obrigatória:** V-022: permutações determinísticas recuperam frame; perda expira; duplicado não aumenta memória nem deadline; flood não supera oito montagens; epoch novo limpa buffers. O teste de JPEG corrompido pertence ao decoder/player em V-023.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-023](../tasks/PHASE-05/TASK-023.md), [TASK-024](../tasks/PHASE-05/TASK-024.md).
- **Pontos que o revisor deve comprovar:**
  - Reordenação/duplicados não corrompem frame nem prolongam deadline.
  - Perdas expiram, montagens ficam limitadas e nova época limpa estado antigo.

### TASK-023 — Apresentar catálogo e vídeo numa janela Swing

- **Especificação completa:** [TASK-023](../tasks/PHASE-05/TASK-023.md); fase [PHASE-05](../phases/PHASE-05-STREAMING.md).
- **Iniciar depois de:** [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-022](../tasks/PHASE-05/TASK-022.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Permitir escolher, ver e parar uma stream com feedback de disponibilidade.
- **Âmbito indicado na tarefa:** Existente: client/VideoPlayer.java e StreamingClient.java; planeados: client/VideoPlayerTest.java e roteiro de teste GUI. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Descodificar fora do EDT e apresentar imagens prontas em Swing. Limitar a fila visual, ligar play/stop às subscrições e tratar fecho da janela. Headless não demonstra reprodução visual.
- **Validação obrigatória:** V-023: testar presenter e frame sink sem GUI; na bancada gráfica verificar vídeo, seleção e fecho manualmente, registando imagem/roteiro. Ausência de display é erro explicativo, não sucesso GUI.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-024](../tasks/PHASE-05/TASK-024.md).
- **Pontos que o revisor deve comprovar:**
  - Rede/descodificação não correm no EDT e a fila visual é limitada.
  - Vídeo foi visto numa janela, incluindo seleção/stop/fecho; headless não é aceite como substituto da GUI.

### TASK-024 — Integrar duas origens e quatro streams ponta a ponta

- **Especificação completa:** [TASK-024](../tasks/PHASE-05/TASK-024.md); fase [PHASE-05](../phases/PHASE-05-STREAMING.md).
- **Iniciar depois de:** [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji), [TASK-020](../tasks/PHASE-05/TASK-020.md) (Fillmore), [TASK-021](../tasks/PHASE-05/TASK-021.md) (Waheed), [TASK-022](../tasks/PHASE-05/TASK-022.md) (Denji), [TASK-023](../tasks/PHASE-05/TASK-023.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Obter versão integrada com todas as funcionalidades principais antes do CORE.
- **Âmbito indicado na tarefa:** Existentes: app/OttApplication.java, OverlayNode.java, StreamingServer.java, StreamingClient.java e config/*.properties; planeado: integração MultiStreamIntegrationTest.java. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Coordenar a integração com contributos de Waheed (transporte/replicação) e Fillmore (routing/fontes). Verificar duas origens, quatro UUID/conteúdos, consumidores partilhados, isolamento e GUI real; atribuir correções ao autor do componente.
- **Validação obrigatória:** V-024: duas origens anunciam e fornecem quatro vídeos; sinks confirmam stream/session/conteúdo; partilha e isolamento são medidos; GUI reproduz pelo menos uma amostra de cada UUID com evidência.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-025](../tasks/PHASE-06/TASK-025.md).
- **Pontos que o revisor deve comprovar:**
  - Duas origens anunciam quatro conteúdos reproduzíveis e clientes partilham corretamente o tronco.
  - Troca play/stop, isolamento e erro de uma fonte têm evidência; contratos não foram alterados silenciosamente.
- **Contributo obrigatório dos três:** fornecer dados/revisão da própria área; o responsável coordena e compila o resultado, não assume sozinho a implementação/avaliação dos restantes.

### TASK-027 — Validar cenário complexo com duas origens e cinco clientes

- **Especificação completa:** [TASK-027](../tasks/PHASE-06/TASK-027.md); fase [PHASE-06](../phases/PHASE-06-CORE-VALIDATION.md).
- **Iniciar depois de:** [TASK-026](../tasks/PHASE-06/TASK-026.md) (Fillmore), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Demonstrar a configuração final mínima e partilha com mais clientes.
- **Âmbito indicado na tarefa:** Existente: CORE/topologies/cenario3.xml; planeados: perfis .properties que mapeiam os processos para os equipamentos existentes e experiments/scenario-3/. Não há novo XML obrigatório. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Reutilizar cenario3.xml com mapeamento explícito de duas origens e cinco clientes. Os nomes dos equipamentos não obrigam o papel Java. Comprovar quatro conteúdos, partilha e isolamento entre fontes.
- **Validação obrigatória:** V-027: quatro conteúdos identificáveis são recebidos de duas origens; cinco clientes funcionam dentro da capacidade configurada; falhas de uma origem não corrompem a outra; partilha no tronco tem evidência.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-028](../tasks/PHASE-06/TASK-028.md).
- **Pontos que o revisor deve comprovar:**
  - Duas origens, quatro conteúdos e cinco clientes foram realmente observados.
  - Capacidade, partilha e isolamento têm evidência, sem alterar o XML por conveniência não justificada.

### TASK-028 — Recolher métricas e avaliar resultados e limites

- **Especificação completa:** [TASK-028](../tasks/PHASE-06/TASK-028.md); fase [PHASE-06](../phases/PHASE-06-CORE-VALIDATION.md).
- **Iniciar depois de:** [TASK-025](../tasks/PHASE-06/TASK-025.md) (Waheed), [TASK-026](../tasks/PHASE-06/TASK-026.md) (Fillmore), [TASK-027](../tasks/PHASE-06/TASK-027.md) (Denji), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Quantificar eficiência e experiência observada com experiências repetíveis.
- **Âmbito indicado na tarefa:** Planeados: experiments/*/RUN-ID/manifest.md, metrics.csv, capturas selecionadas e resumo-results.md. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Cruzar capturas, contadores e eventos, com condições/repetições identificadas. Distinguir perda de datagrama, frame incompleto e frame não apresentado. Analisar também resultados negativos e limites de capacidade.
- **Validação obrigatória:** V-028: auditoria cruzada entre capturas, contadores e eventos de rota; tabela reproduzível de condições/resultados; análise distingue perda de datagrama, frame incompleto e frame não mostrado.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Fillmore](FILLMORE.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Fillmore](FILLMORE.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: [TASK-029](../tasks/PHASE-07/TASK-029.md).
- **Pontos que o revisor deve comprovar:**
  - Tabelas derivam de dados brutos rastreáveis com unidades e condições.
  - Resultados desfavoráveis/limites e erro de medição não são omitidos ou confundidos com garantias.
- **Contributo obrigatório dos três:** fornecer dados/revisão da própria área; o responsável coordena e compila o resultado, não assume sozinho a implementação/avaliação dos restantes.

### TASK-031 — Preparar defesa e checklist de submissão humana

- **Especificação completa:** [TASK-031](../tasks/PHASE-07/TASK-031.md); fase [PHASE-07](../phases/PHASE-07-DELIVERY.md).
- **Iniciar depois de:** [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji), [TASK-030](../tasks/PHASE-07/TASK-030.md) (Waheed), mais fase anterior DONE; sem exceções implícitas.
- **Resultado:** Concluir preparação de defesa e orientar submissão no grupo correto.
- **Âmbito indicado na tarefa:** Planeados: report/defense.md e delivery/submission-checklist.md. Caminhos Java são relativos às raízes main/test indicadas na TASK; confirmar o estado real antes de editar, incluindo placeholders.
- **Atenção principal:** Preparar defesa dos três membros e confirmar que os ficheiros são os validados. Preparar checklist não equivale a submissão: só concluir após confirmação humana do upload no grupo correto, sem expor credenciais.
- **Validação obrigatória:** V-031: ensaio cronometrado reproduz tópicos de avaliação; revisão dos dois nomes e grupo; confirmação humana documenta upload correto sem exposição de credenciais.
- **Revisão inicial de contrato:** antes de alterar APIs partilhadas, rever com [Waheed](WAHEED.md) e o consumidor afetado; registar decisão em 07/12 se mudar o contrato.
- **Revisão final:** pedir a [Waheed](WAHEED.md) após testes/evidência e antes de integrar, marcar DONE ou desbloquear: fecho da fase/projeto, segundo a Definition of Done.
- **Pontos que o revisor deve comprovar:**
  - Os três elementos explicam decisões e demonstram funcionalidades da versão validada.
  - Confirmação humana do upload identifica grupo/artefactos corretos; checklist sem upload não dá DONE.
- **Contributo obrigatório dos três:** fornecer dados/revisão da própria área; o responsável coordena e compila o resultado, não assume sozinho a implementação/avaliação dos restantes.

## 5. Lista completa das revisões que lhe cabem

Iniciar a revisão de interface antes do código consumidor e a revisão final quando o autor entregar o diff, os testes e a evidência. **Concluir antes do merge/DONE e do início dos consumidores abaixo.** Rever a versão final após correções; não aprovar apenas uma versão anterior.

| Tarefa a rever | Autor | Finalizar revisão antes de desbloquear |
|---|---|---|
| [TASK-007](../tasks/PHASE-02/TASK-007.md) — Gerir handshake, unicidade e reconexão de vizinhos | [Waheed](WAHEED.md) | [TASK-008](../tasks/PHASE-02/TASK-008.md), [TASK-010](../tasks/PHASE-03/TASK-010.md), [TASK-012](../tasks/PHASE-03/TASK-012.md) |
| [TASK-012](../tasks/PHASE-03/TASK-012.md) — Difundir anúncios e construir catálogo e rotas | [Fillmore](FILLMORE.md) | [TASK-013](../tasks/PHASE-03/TASK-013.md), [TASK-014](../tasks/PHASE-04/TASK-014.md), [TASK-016](../tasks/PHASE-04/TASK-016.md), [TASK-023](../tasks/PHASE-05/TASK-023.md) |
| [TASK-015](../tasks/PHASE-04/TASK-015.md) — Podar ramos, renovar leases e remover procura abandonada | [Fillmore](FILLMORE.md) | [TASK-016](../tasks/PHASE-04/TASK-016.md), [TASK-018](../tasks/PHASE-04/TASK-018.md), [TASK-021](../tasks/PHASE-05/TASK-021.md) |
| [TASK-019](../tasks/PHASE-05/TASK-019.md) — Definir codec UDP e fragmentação de frames | [Waheed](WAHEED.md) | [TASK-020](../tasks/PHASE-05/TASK-020.md), [TASK-021](../tasks/PHASE-05/TASK-021.md), [TASK-022](../tasks/PHASE-05/TASK-022.md) |
| [TASK-020](../tasks/PHASE-05/TASK-020.md) — Ler fontes MJPEG e controlar ritmo de publicação | [Fillmore](FILLMORE.md) | [TASK-024](../tasks/PHASE-05/TASK-024.md) |
| [TASK-025](../tasks/PHASE-06/TASK-025.md) — Preparar CORE e comprovar replicação no cenário de quatro nós | [Waheed](WAHEED.md) | [TASK-026](../tasks/PHASE-06/TASK-026.md), [TASK-028](../tasks/PHASE-06/TASK-028.md) |
| [TASK-029](../tasks/PHASE-07/TASK-029.md) — Escrever artigo com protocolos e avaliação crítica | [Fillmore](FILLMORE.md) | [TASK-030](../tasks/PHASE-07/TASK-030.md) |

Para cada revisão, ler a especificação e critérios, analisar o diff e reproduzir as validações relevantes; usar também os pontos específicos na secção de implementação do documento do autor. Pedir correções concretas, sem alterar diretamente o trabalho do autor ou aprovar resultados não observados. Registar aceitação e evidência no handoff do ficheiro TASK; o autor corrige e volta a pedir revisão.

## 6. Passagem de trabalho e integração

Aplicar o protocolo de [coordenação](00-COORDINATION.md): uma branch por tarefa, autor responsável pelos próprios testes, revisor distinto e integração antes de disponibilizar dependências. Mudanças de routing/transport/app partilhados exigem coordenação com o responsável da tarefa em curso. A lista de revisões não autoriza implementar simultaneamente uma segunda tarefa no mesmo checkout.

No final de cada sessão registar no ficheiro TASK: revisão/branch de base, alterações, testes/comandos/resultados, critérios pendentes, contrato alterado, responsável pelo próximo passo e estado da revisão. Na integração atualizar apenas as linhas afetadas em 15 e a evidência pertinente em 13; preservar handoffs de outras pessoas.

Se os testes passam mas a revisão/integração está pendente, manter IN PROGRESS com a situação explícita. A espera normal por uma dependência mantém uma tarefa futura TODO. Usar BLOCKED para um impedimento real e explicar a condição de resolução. Não marcar requisito transversal DONE porque a sua parcela foi concluída.

## 7. Prompt para uma sessão de implementação

Substituir TASK-XXX pelo ID próprio já desbloqueado; este modelo não inicia tarefas automaticamente.

```text
Sou Denji. Segue docs/team/DENJI.md e docs/team/00-COORDINATION.md.
Executa exclusivamente TASK-XXX, lendo o seu ficheiro em docs/tasks/ e todos os contratos referenciados.
Antes de editar, verifica o estado do repositório, as dependências integradas e o gate da fase.
Se a tarefa não estiver desbloqueada, indica a dependência em falta sem a contornar.
Preserva alterações existentes e implementa apenas o âmbito da tarefa, incluindo os seus testes.
Regista comandos/resultados reais e prepara o diff/handoff para o revisor atribuído.
Mantém IN PROGRESS enquanto revisão ou integração estiverem pendentes.
Não avances automaticamente para outras tarefas nem submetas artefactos externamente.
```
