# Estratégia de testes e experiências futuras

Este documento define validações a executar durante a construção. **Não há testes da aplicação executados neste planeamento.** Cada V-XXX corresponde à validação principal de TASK-XXX; pode conter vários testes automatizados e verificações manuais. Não confundir existência de teste, execução, sucesso e evidência preservada.

## Camadas e momento

| Camada | Quando | Método |
|---|---|---|
| Validação documental | Planeamento e mudanças de contrato | Ligações, requisitos, DAG, schemas e offsets |
| Build e testes unitários | Desde PHASE-01 | JUnit Jupiter, fixtures independentes, relógio/scheduler falso, sem rede externa |
| Integração local | Desde PHASE-02 | TCP/UDP loopback em portos efémeros, timeouts limitados, teardown que verifica recursos |
| Protocolo e máquinas de estado | PHASE-03/04 | Mensagens repetidas/atrasadas, eventos em ordem variável e caminhos de custo conhecido |
| Streaming | PHASE-05 | Fixtures pequenas de formato confirmado, fragmentação, perda/reordenação e sink headless |
| Funcional GUI | PHASE-05 e CORE | Janela real com conteúdo identificável; registo manual, sem afirmar que headless prova renderização |
| End-to-end CORE | PHASE-06 | Três cenários, falhas, alteração de atraso, concorrência de clientes e captura |
| Aceitação e entrega | PHASE-07 | PDF até 12 páginas, nomes, extração limpa, build, ensaio e confirmação de upload |

Não deixar testes para o fim. Cada tarefa que altera comportamento inclui testes dos invariantes que introduz. Não exigir percentagem de cobertura arbitrária; verificar os requisitos e ramos de erro relevantes. Não escrever testes que apenas repetem o código, como calcular expected bytes com o próprio encoder.

## Casos críticos

- Configuração: propriedades ausentes/desconhecidas/duplicadas, UUID repetido, dois vídeos por origem, simetria do grafo, paths relativos, endpoints e limites.
- TCP: leitura de um byte, várias mensagens concatenadas, frame truncado, cliente lento, dois envios concorrentes e encerramento durante I/O.
- Vizinhança: ordens diferentes de arranque, duplicado, peer não permitido, ligação antiga morre depois da nova, reconexão e lease de registo.
- Rotas: primeiro anúncio mais caro que segundo, empate, TTL/path repetido, epoch nova/antiga, candidato expirado e custo com overflow.
- Procura: dois consumidores no mesmo ramo, repetição de ACTIVATE, ACK atrasado após stop, cancelamento antigo, saída do último e lease expirado.
- Adaptação: morte de relay, caminho alternativo inválido, partição, regresso, melhoria ruidosa junto do limiar e nova ativação falhada.
- UDP: fronteiras de tamanho, índices trocados, duplicados, origem/epoch inválida, perda, frame incompleto, saturação e reset da sessão.
- Vídeo: recurso real confirmado, ficheiro truncado, EOF, loop, pacing, quatro conteúdos distintos, cliente lento e JPEG corrompido.
- Segurança: todos os limites de [07](07-API-AND-INTERFACES.md), sem alocações orientadas por comprimento não validado; rejeitar peer fora do grafo e path recebido como media.

## Cenários e oráculos

S1 de quatro nós mede partilha: S→R→C1/C2. S2 de seis nós mede caminhos alternativos: S→R1/R2→R3→C1/C2. S3 de duas origens, cinco relays e cinco clientes, inspirado na figura 1, valida quatro streams e escala. Configurações detalhadas em TASK-025–027 e mapeamento a endereços reais criado nessas tarefas.

Para uma janela estável e sem perda configurada, agrupar datagramas por (streamId, sessionId, frameSeq, fragmentIndex) em cada ligação lógica. No segmento comum cada chave deve ocorrer uma vez na direção de dados, mesmo com dois ou cinco clientes a jusante. Comparar janela de sequências comuns aos pontos de captura; excluir warm-up, troca de rota e final de captura. Não somar a mesma transmissão observada em interfaces underlay distintas como redundância da overlay.

Para isolamento, parar um consumidor de A e confirmar que outro A e consumidores B/C/D continuam; parar origem S1 torna A/B indisponíveis e preserva C/D. Quatro nomes a apontar ao mesmo ficheiro não demonstram RF-013. Registar hash e inspeção visual; para dois conteúdos com diferenças mínimas, usar evidência de frames distintos, não só hash de container.

## Medições e metas técnicas

Parâmetros iniciais: 10 s warm-up, 60 s medição e três repetições por condição, salvo limitação documentada. Baseline deve ter capacidade por ligação superior à soma dos fluxos e overhead que a atravessam; calcular bitrate real dos ficheiros antes de impor banda no CORE. Perfis sugeridos: atraso adicional de 0/20/80 ms por link selecionado e perda de 0%/1%. Registar unidade aceite pela versão real do CORE e sentido aplicado. Não assumir que soma de RTT equivale ao atraso unidirecional de reprodução.

| Métrica | Recolha e critério |
|---|---|
| Cópias no tronco | Exatamente uma por chave em estado estável e perfil sem perda; critério funcional de RF-011 |
| FPS completos / mostrados | Frames reconstruídos / frames entregues à janela por intervalo; separar descarte de decode e GUI |
| Eficiência | Bytes de vídeo por ligação lógica e número de clientes; tronco não multiplica por N para a mesma stream |
| Tempo de ativação | Pedido local até primeiro frame completo; medido no mesmo cliente |
| Interrupção após falha | Último frame completo antes da falha até primeiro após recuperação; relacionar com evento de falha conhecido |
| Mudança de parent | Quantidade, motivo, custos e instante; ausência de oscilação em cenário estável |
| RTT | Sonda/pong no mesmo nó; EWMA e idade; não confundir com perda de datagramas |
| Recursos | Heap/RSS e CPU por nó, filas máximas, sockets/threads após stop |
| Overhead | Bytes de controlo e sondas separados dos bytes de vídeo |

Metas propostas, a calibrar: ativação local e em CORE sem congestão até 5 s; recuperação com alternativa até 10 s; FPS mostrado pelo menos 90% do configurado no baseline sem perda e com capacidade suficiente; nenhuma troca por custo após convergência em 60 s de condições fixas; oito montagens/stream e filas dentro dos máximos; última poda em até 7 s após desaparecimento do consumidor. Estas são metas de engenharia, não números do enunciado. Se falharem, investigar, ajustar com justificação ou declarar limitação; não transformar falha de um requisito obrigatório em simples tuning.

Perda de datagramas mede-se por chaves esperadas/observadas nos pontos adequados, descontando perda da própria captura. Frames incompletos e frames descartados por atraso são métricas diferentes. Não inferir latência unidirecional a partir de mediaTimeUs; só medir se houver referência comum verificada e erro de sincronização documentado.

## Evidências e execução

Manifesto por RUN-ID: data, commit/hash do JAR, versões Java/CORE, máquina/recursos, topologia, mapa de processos, configurações/hashes, vídeos/hashes, condições de atraso/banda/perda, comandos, seed, cronologia das falhas, dados brutos e resultados. Manter métricas com unidades e origem. Repetições produzem mediana e intervalo; p95 apenas com amostras suficientes e método explícito. Guardar limitações e falhas.

Comandos de teste são propostos em [11](11-SETUP-AND-ENVIRONMENT.md); os nomes concretos das classes de testes são estabelecidos pelas tarefas. Usar portos efémeros nos testes locais e não iniciar Docker para testes unitários. Sem GUI ou sem CORE, assinalar o teste que falta; não o marcar como aprovado.

## Esquema proposto das evidências

metrics.csv usa cabeçalho estável: run_id, sample_time_utc, elapsed_ms, node_id, stream_id, direction, neighbor_id, metric_name, metric_value, unit. direction é ingress, egress, local ou control; neighbor_id vazio apenas para métricas locais. Exemplos de metric_name: video_bytes, video_datagrams, frames_complete, frames_shown, frame_timeout_drop, rtt_ewma_us, queue_max_depth, route_switch_count. Contadores acumulados devem ser convertidos em deltas por intervalo na análise, conservando os valores brutos.

O manifesto liga os ficheiros de configuração e respetivos hashes, captures e logs. Eventos de rota incluem custo anterior/novo e motivo. Um resultado sem dados brutos suficientes para repetir a conclusão é limitação da experiência, não prova positiva.

## Catálogo de validações

A lista seguinte especifica oráculos de aceitação e está ligada a [13](13-REQUIREMENTS-TRACEABILITY.md). Todos começam como NÃO EXECUTADO.

### V-001 — Validar a base Java e configurar testes

V-001: inspeção do POM efetivo e build limpo com release 21; verificar engine escolhido. Zero testes nesta base não valida funcionalidades; repetir descoberta real em V-003.

Responsável: [TASK-001](tasks/PHASE-01/TASK-001.md). Requisitos: RP-006. Estado: EXECUTADA, com sucesso em 26-10-2026. [Evidência](../experiments/build-baseline.md): POM efetivo, build limpo e engine verificados; zero testes funcionais. Descoberta/execução de testes reais continua pendente para V-003.

### V-002 — Fixar contratos, fixtures e decisões pendentes

V-002: revisão independente dos comprimentos, offsets, exemplos de identidade e tabela de requisitos; evidência de acesso ou falha de recurso, sem execução de streaming.

Responsável: [TASK-002](tasks/PHASE-01/TASK-002.md). Requisitos: RNF-005. Estado: EXECUTADA com sucesso em 26-10-2026. [Evidência](../experiments/task-002-contract-review.md): 45 pares binário/hex, 17 tipos TCP, offsets/limites UDP, recurso docente obtido/hash/perfil confirmados e quatro conteúdos identificados. Não foi executado codec, JUnit ou streaming.

### V-003 — Carregar e validar configurações de nós, grafo e streams

V-003: fixtures válidas/invalidas com diretório temporário, paths fora do cwd, ID de um carácter e quatro streams; Maven reporta testes executados e falha perante um requisito de configuração violado.

Responsável: [TASK-003](tasks/PHASE-01/TASK-003.md). Requisitos: RF-001, RF-004, RF-012, RF-013, RP-001, RP-006. Estado: NÃO EXECUTADO.

### V-004 — Codificar e descodificar mensagens de controlo v1

V-004: golden bytes para HELLO/ANNOUNCE/ACTIVATE/ERROR, round-trip dos 17 tipos, leituras parciais, concatenação e rejeições sem alocação não limitada.

Responsável: [TASK-004](tasks/PHASE-01/TASK-004.md). Requisitos: RNF-005, RP-006. Estado: NÃO EXECUTADO.

### V-005 — Estabelecer transporte TCP full-duplex com recursos limitados

V-005: dois endpoints loopback com portas efémeras trocam mensagens simultâneas intactas; parar um gera evento único; reconexão e saturação não deixam threads/sockets abertos.

Responsável: [TASK-005](tasks/PHASE-02/TASK-005.md). Requisitos: RF-003, RNF-004, RP-006. Estado: NÃO EXECUTADO.

### V-006 — Implementar registo bootstrap e descoberta configurada

V-006: registos recebem só os vizinhos definidos; lease expira com relógio falso; novo arranque pode recuperar identidade após expiração; tráfego de vizinhos não passa pelo bootstrap.

Responsável: [TASK-006](tasks/PHASE-02/TASK-006.md). Requisitos: RF-002, RF-003, RF-004. Estado: NÃO EXECUTADO.

### V-007 — Gerir handshake, unicidade e reconexão de vizinhos

V-007: três nós iniciados em ordens diferentes convergem para o grafo esperado sem duplicados; novo instanceId após reinício não é apagado por callback antigo; vizinho desconhecido é rejeitado.

Responsável: [TASK-007](tasks/PHASE-02/TASK-007.md). Requisitos: RF-002, RF-003, RF-004, RNF-004, RP-006. Estado: NÃO EXECUTADO.

### V-008 — Compor o processo overlay e encerrar de forma previsível

V-008: subprocessos locais iniciam por CLI, mostram vizinhos e terminam; configuração inválida não abre porto; interrupção e erro de um peer não deixam processo preso.

Responsável: [TASK-008](tasks/PHASE-02/TASK-008.md). Requisitos: RF-001, RF-002, RNF-003, RP-008. Estado: NÃO EXECUTADO.

### V-009 — Preparar evidência do primeiro marco intermédio

V-009: outro elemento consegue repetir a demonstração e identificar registo, handshake, saída e reconexão nos logs.

Responsável: [TASK-009](tasks/PHASE-02/TASK-009.md). Requisitos: RNF-005, RC-007. Estado: NÃO EXECUTADO.

### V-010 — Medir RTT e publicar falhas de vizinhança

V-010: relógio falso confirma EWMA, reset após nova instância, perda de três sondas e ausência de falso positivo por um timeout; EOF invalida imediatamente.

Responsável: [TASK-010](tasks/PHASE-03/TASK-010.md). Requisitos: RF-003, RF-008, RF-014, RP-006. Estado: NÃO EXECUTADO.

### V-011 — Modelar candidatos, custos e snapshots por stream

V-011: custos e desempate calculados com fixtures de resultados conhecidos; quatro streams não interferem; alternativa é preservada e snapshot não pode ser alterado por consumidor.

Responsável: [TASK-011](tasks/PHASE-03/TASK-011.md). Requisitos: RF-005, RF-006, RF-014, RP-006. Estado: NÃO EXECUTADO.

### V-012 — Difundir anúncios e construir catálogo e rotas

V-012: grafo em losango e grafo cíclico convergem para custo esperado; segunda chegada melhor é aceite; quatro streams aparecem; retirar uma origem não remove streams da outra; flooding estabiliza.

Responsável: [TASK-012](tasks/PHASE-03/TASK-012.md). Requisitos: RF-005, RF-006, RF-009, RF-012, RF-014, RP-001. Estado: NÃO EXECUTADO.

### V-013 — Diagnosticar percursos pela tabela de rotas

V-013: duas rotas alternativas produzem visited correto; NO_ROUTE e TTL são explícitos; não há ativação nem saída de vídeo após trace.

Responsável: [TASK-013](tasks/PHASE-03/TASK-013.md). Requisitos: RF-010, RP-004, RP-008. Estado: NÃO EXECUTADO.

### V-014 — Ativar árvores com ACK e procura agregada

V-014: dois clientes em tronco comum geram uma subscrição no tronco; ACK atrasado/cancelado não ativa; prefixo incorreto gera PATH_CHANGED; streams independentes mantêm estados separados.

Responsável: [TASK-014](tasks/PHASE-04/TASK-014.md). Requisitos: RF-005, RF-007, RF-011, RP-002, RP-006. Estado: NÃO EXECUTADO.

### V-015 — Podar ramos, renovar leases e remover procura abandonada

V-015: sair um de dois consumidores mantém tronco ativo; sair o último remove; relógio falso elimina lease órfão; cancelamento velho não remove ramo novo.

Responsável: [TASK-015](tasks/PHASE-04/TASK-015.md). Requisitos: RF-002, RF-005, RF-007, RF-009, RF-011, RNF-003, RP-002. Estado: NÃO EXECUTADO.

### V-016 — Recuperar árvores após falha de upstream

V-016: cortar upstream num losango leva a caminho alternativo sem ciclo; sem alternativa termina UNAVAILABLE; regresso permite recuperar; streams da outra origem continuam.

Responsável: [TASK-016](tasks/PHASE-04/TASK-016.md). Requisitos: RF-002, RF-006, RF-009, RNF-003. Estado: NÃO EXECUTADO.

### V-017 — Adaptar rota por RTT com histerese e troca confirmada

V-017: séries temporais predeterminadas não oscilam junto do limiar; melhoria sustentada troca; falha troca sem esperar permanência; troca falhada mantém parent antigo válido.

Responsável: [TASK-017](tasks/PHASE-04/TASK-017.md). Requisitos: RF-009, RF-014, RNF-001, RNF-003. Estado: NÃO EXECUTADO.

### V-018 — Preparar o segundo marco com árvores e adaptação

V-018: reprodução independente da ativação/poda/failover usando logs e snapshots; revisão do material contra os critérios de gestão de fluxos e controlo do PDF.

Responsável: [TASK-018](tasks/PHASE-04/TASK-018.md). Requisitos: RNF-005, RC-007. Estado: NÃO EXECUTADO.

### V-019 — Definir codec UDP e fragmentação de frames

V-019: fixtures de bytes confirmam offsets 0–64; recombinação manual dos payloads recupera exatamente o frame; nenhum datagrama excede 1200 bytes; inputs inválidos são rejeitados.

Responsável: [TASK-019](tasks/PHASE-05/TASK-019.md). Requisitos: RF-010, RF-015, RNF-001, RP-003, RP-006. Estado: NÃO EXECUTADO.

### V-020 — Ler fontes MJPEG e controlar ritmo de publicação

V-020: fixture confirmada com dois frames produz bytes esperados; ficheiro truncado falha de forma localizada; relógio falso confirma pacing/ausência de envio sem procura; quatro conteúdos são identificáveis.

Responsável: [TASK-020](tasks/PHASE-05/TASK-020.md). Requisitos: RF-012, RF-013, RF-015, RNF-001, RP-003. Estado: NÃO EXECUTADO.

### V-021 — Reencaminhar e replicar datagramas pela árvore ativa

V-021: fonte→relay→dois sinks recebe uma cópia por fragmento no tronco e duas nas saídas; origem não autorizada/epoch velha não segue; saturar uma saída não bloqueia controlo nem outras streams.

Responsável: [TASK-021](tasks/PHASE-05/TASK-021.md). Requisitos: RF-007, RF-010, RF-011, RF-015, RNF-001, RNF-002, RP-006. Estado: NÃO EXECUTADO.

### V-022 — Reconstituir frames com buffers e prazo limitados

V-022: permutações determinísticas recuperam frame; perda expira; duplicado não aumenta memória nem deadline; flood não supera oito montagens; epoch novo limpa buffers. O teste de JPEG corrompido pertence ao decoder/player em V-023.

Responsável: [TASK-022](tasks/PHASE-05/TASK-022.md). Requisitos: RF-015, RNF-001, RNF-003, RP-003, RP-006. Estado: NÃO EXECUTADO.

### V-023 — Apresentar catálogo e vídeo numa janela Swing

V-023: testar presenter e frame sink sem GUI; na bancada gráfica verificar vídeo, seleção e fecho manualmente, registando imagem/roteiro. Ausência de display é erro explicativo, não sucesso GUI.

Responsável: [TASK-023](tasks/PHASE-05/TASK-023.md). Requisitos: RF-015, RP-001, RP-003. Estado: NÃO EXECUTADO.

### V-024 — Integrar duas origens e quatro streams ponta a ponta

V-024: duas origens anunciam e fornecem quatro vídeos; sinks confirmam stream/session/conteúdo; partilha e isolamento são medidos; GUI reproduz pelo menos uma amostra de cada UUID com evidência.

Responsável: [TASK-024](tasks/PHASE-05/TASK-024.md). Requisitos: RF-007, RF-012, RF-013, RF-015, RP-001, RP-008. Estado: NÃO EXECUTADO.

### V-025 — Preparar CORE e comprovar replicação no cenário de quatro nós

V-025: no estado estável comparar chaves (stream,session,seq,index) por ligação lógica; S→R tem uma cópia, R→C1 e R→C2 uma cada; depois do último stop não há datagramas de vídeo após o prazo de poda.

Responsável: [TASK-025](tasks/PHASE-06/TASK-025.md). Requisitos: RF-011, RNF-004, RC-001, RP-005, RP-008. Estado: NÃO EXECUTADO.

### V-026 — Demonstrar alternativas e adaptação no cenário de seis nós

V-026: três repetições por condição; medir fim do vídeo antes e primeiro frame após falha; snapshots e logs mostram parent alterado; jitter pequeno não gera oscilações; um consumidor sair não interrompe o outro.

Responsável: [TASK-026](tasks/PHASE-06/TASK-026.md). Requisitos: RF-006, RF-009, RF-014, RNF-003, RC-001, RP-005. Estado: NÃO EXECUTADO.

### V-027 — Validar cenário complexo com duas origens e cinco clientes

V-027: quatro conteúdos identificáveis são recebidos de duas origens; cinco clientes funcionam dentro da capacidade configurada; falhas de uma origem não corrompem a outra; partilha no tronco tem evidência.

Responsável: [TASK-027](tasks/PHASE-06/TASK-027.md). Requisitos: RF-007, RF-012, RF-013, RNF-002, RC-001, RP-005. Estado: NÃO EXECUTADO.

### V-028 — Recolher métricas e avaliar resultados e limites

V-028: auditoria cruzada entre capturas, contadores e eventos de rota; tabela reproduzível de condições/resultados; análise distingue perda de datagrama, frame incompleto e frame não mostrado.

Responsável: [TASK-028](tasks/PHASE-06/TASK-028.md). Requisitos: RF-008, RF-011, RNF-001, RNF-002, RNF-005, RC-001, RP-008. Estado: NÃO EXECUTADO.

### V-029 — Escrever artigo com protocolos e avaliação crítica

V-029: revisão por checklist do enunciado; correspondência de figuras/tabelas com dados; contagem de páginas do PDF até 12; protocolo no artigo corresponde aos bytes e comportamento implementados.

Responsável: [TASK-029](tasks/PHASE-07/TASK-029.md). Requisitos: RNF-005, RC-002, RC-003, RP-007. Estado: NÃO EXECUTADO.

### V-030 — Empacotar código e verificar reprodução da entrega

V-030: arquivo abre sem paths absolutos/escape; extração limpa passa instruções e testes; PDF abre e continua até 12 páginas; nomes e hashes correspondem ao manifesto.

Responsável: [TASK-030](tasks/PHASE-07/TASK-030.md). Requisitos: RC-003, RC-004, RC-005, RP-008. Estado: NÃO EXECUTADO.

### V-031 — Preparar defesa e checklist de submissão humana

V-031: ensaio cronometrado reproduz tópicos de avaliação; revisão dos dois nomes e grupo; confirmação humana documenta upload correto sem exposição de credenciais.

Responsável: [TASK-031](tasks/PHASE-07/TASK-031.md). Requisitos: RNF-005, RC-005, RC-006, RC-007. Estado: NÃO EXECUTADO.
