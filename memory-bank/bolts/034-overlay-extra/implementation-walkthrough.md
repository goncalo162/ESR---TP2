---
stage: implement
bolt: "034-overlay-extra"
created: "2026-11-11T19:03:16Z"
status: "ready-for-review"
---
# IMP-034 — TCP v2 uniforme e aprendizagem de candidatos

**Implement pronto para revisão. Parar antes de Test/fecho.** Aprovação explícita
de Design/ADR-034/29 caminhos/migração registada em 2026-11-11T17:55:50Z;
ADR-034 Aceite e G-EXTRA-ADR satisfeito só para IMP-034. Base Git integrada:
17c1ca27673930bfbf82078522059a243d96ccdb, ramo Denji. Sem commits/push.

**Correção D07e posterior:** a versão atual passou 142 testes incrementais afetados;
os 1062 abaixo são a execução anterior. Ver a secção final desta revisão.

Todo o controlo TCP agora usa v2: bootstrap, handshake, mensagens normais e
aprendizagem. Um ControlMessage/MessageCodec operacional, 18 tipos explícitos
(1,2,4..19), código 3 retirado, sem codec runtime v1/OTD1, fallback ou flag para
desligar descoberta. IMP-034 aprende/retém candidatos; IMP-035 continua planeado,
com checkpoint próprio para admitir/reparar ligações. Não foi implementado vídeo.

## Alterações e resultado

- Codec: framing/UTF-8/UUID/strings/endpoint/buffer comuns, APIs existentes em v2;
  HELLO(Endpoint+1+1)/ACK(1), QUERY/SNAPSHOT como payloads e readFrame tipado.
  Framing global 37..65536 e cap extra 32768 antes de alocar, sem ressincronização.
- Configuração: todo overlay exige Q>=2 antes de recursos, ausência/true redundante
  obrigatórios, false inválido com erros agregados. Construtores 13/14 preservados.
  Preflight conjunto rejeita grau conhecido maior que C; genérico/bootstrap Q=1 válido.
- Bootstrap: consulta imutável/offline em v2, instâncias zero na lista, resposta
  única após write/flush completo e fecho limitado; não responde ERROR a ERROR.
- Transporte: uma FIFO Q+inFlight/writer, margem M=1, quota enfileirada extra=1
  incluindo take→inFlight, quatro tickets físicos comuns, BACKPRESSURED não fecha.
  Cancelar cópia de future/expirar lógica não liberta ticket de escrita prematuramente.
- Vizinhos: capacidade obrigatória antes de Up, estado/razão limitados por peer,
  retry/backoff existentes e rejeição local sem FAILED/FATAL/saída da CLI ou queda
  das sessões saudáveis; correlação de sessão/handle/instância/token preservada.
- Aprendizagem: snapshots dos vizinhos diretos ACTIVE, sem hearsay/catálogo global,
  cache 128x4, TTL local 60 s desde QUERY, proveniência e geração por sessão.
  Morte do anunciante conserva claims; conflito/expiração não inventam um caminho.
- Lifecycle: mesmo owner e worker de deadlines. Tick físico único/coalescido,
  callbacks curtos e jobs cancelados contam até terminarem. A corrida descoberta/
  shutdown observada em teste existente foi corrigida: rejeitar admissões de
  aplicação durante drenagem sem fechar antes de NODE_LEAVE escrever/flush.

## Mapa D01–D12 / V-033 e garantias mantidas

| Oráculo | Implementação e testes incrementais | Limite/pendência |
|---|---|---|
| D01/a–e | ConfigurationLoaderTest (741 casos), quatro casos de configuração de NeighborDiscoveryTest, MessageCodecTest (79); construtores, Q/false agregados, grau/C, 18 tipos e v1 rejeitado. | Nenhum socket para configuração inválida; assinatura anterior preservada. |
| D02/a–d | NeighborManagerTest.D11aBadPeerDoesNotStopHealthySessionOrCandidateLearning, D02cConfirmedRejectionSurvivesTemporaryAbsenceAndBackoffThenValidRetry; codec negativos de máscara/versão e testes anteriores de handshake migrados. | Falha corrente por peer, último motivo confirmado separado da ausência transitória; sem modo base. |
| D03 | NeighborDiscoveryTest.D03D05_realTcpLearnsSAndCWithoutBootstrapAndRetainsAfterRelayLoss e teste com relógio falso. | S–R1/R2–C, bootstrap parado após arranque; nós embutidos/TCP real. |
| D04 | D04_realTcpLearnsR1AndRetainsHealthySR1SessionAfterR2R3Loss. | S–R1–R2/R3–C; S–R1 conserva connectionId; sem R1–C nova. |
| D05 | D03D05_learnSourceViaDirectRelayRetainAfterDeathAndExpireFromQueryStart e CandidateCacheTest.D05. | A origem do prazo é QUERY; expiração exata em clock falso, não 60 s medidos de vídeo. |
| D06 | CandidateCacheTest limites/wrap/conflitos/fontes; NeighborDiscoveryTest geração velha/nova/MAX, callbacks e watermark; DiscoveryCodecTest tamanho máximo 19674. | Fixture de fronteira injeta geração MAX por reflexão em teste, sem propriedade runtime. |
| D07/a–e | 26 DiscoveryCodecTest: cinco fixtures de conteúdo, 13 negativos terminais, UTF-8/truncamento/identidade/cap; sequência rejeição→PING v2→snapshot; transporte real e callback antigo no gestor. | readFrame só retém envelope/motivo, sem corpo/lista inválidos. Owner valida sessão antes de recuperar. |
| D08/a–f | TransportTest.D08_Q2MarginAndTakePublicationWindowPreserveCommonFifo, lastFreeSlot, concorrência Q2/3/256, tickets físicos; NeighborDiscoveryTest ritmo global, 2 consultas/2 respostas, cooldown, sem hearsay. | A margem não é promessa de burst/latência; overflow normal mantém testes anteriores Q1. |
| D09 | Cache vazia/expirada e ausência de aprendizagem não causam dial; grafos reais conservam candidatos sem criar arestas. | Reparação/deteção de máquina silenciosa/fluxos/vídeo são incrementos posteriores. |
| D10 | Ticket de writer/periodic worker não reutilizado antes de terminação física; tick coalescido; quatro consultas públicas; callbacks antigos; novo teste do shutdown durante admissão. | Deadline/shutdown e observador/owner saturado mantêm provas anteriores. |
| D11/a–b | TCP real: dois grafos, seis ordens/arranque simultâneo/reconexão anteriores, 36 TransportTest, 20 BootstrapServerTest; OttApplicationTest.D11a e testes CLI existentes em JVM separada. | Peer inválido com saudável ACTIVE e descoberta/CLI úteis; bootstrap v2 com flush antes EOF; sem CORE/GUI. |
| D12 | 1062 testes incrementais JDK21, hashes/XML/CLI preservados, V-ADD operacional+arquivo e diff. | clean verify completo, manual, revisão final e integração continuam por executar/concluir na etapa Test/fecho. |

## Migração e cada remoção nas fundações

[Plano aprovado e aplicado](test-contract-migration.md). Não se apagou nenhum
método de teste anterior da baseline integrada: foram migrados nomes/expectativas
v1/contagem/payloads e acrescentados casos. Os quatro testes específicos OTD1 do
protótipo interrompido foram substituídos pelos testes de canal único/fila/quota;
ficam integralmente no arquivo/entrada, sem os apresentar como testes da baseline.

| Remoção/substituição | Justificação e garantia preservada |
|---|---|
| ControlMessage default 1→2; enum 16→18; HELLO EndpointPayload e ACK Empty substituídos por payloads tipados | Mudança explícita aprovada do contrato, com os números anteriores, 3 rejeitado, modelos/cópias defensivas e vetores independentes. Empty conserva nome Java como modelo antigo, sem tipo de sucesso associado. |
| MessageCodec read/payload branches | Um parser de framing e payloads v2; nenhuma aceitação v1 nem segundo decoder. EOF/limites/UTF-8 e não escrever modelo inválido preservados. 50 fixtures independentes cobertas pelo Java, sem fabricar esperado pelo encoder. |
| Connection reader direto read→readFrame; PendingWrite/flush bookkeeping estendidos | Entrega de rejeição tipada no mesmo reader; fila, writer, confirmação fora dos locks, overflow normal, cancelamento e terminação mantidos nos 31 testes anteriores + cinco novos. |
| NeighborManager HELLO/ACK/envelope/application dispatch | V2/capacidade e dispatch interno 18/19; base de identidade/iniciador/unicidade/B/R/retentativas preservada. Não reescrever DNS/dial/reservas ou introduzir off-graph. |
| Q1 em perfil overlay e teste D20 | Requisito obrigatório rejeita offline Q1; D20 usa Q2/B2 e provoca overflow à terceira frame, preservando a prova. Q1 genérico/bootstrap continua exercitado. |
| Testes que esperavam version=1 ou version=2 inválida | NODE_LEAVE também migra para v2; versão antiga passa a negativo. Mesmas garantias de flush, EOF, classes/códigos de erro, ordens e limites. |
| Bootstrap ERROR como pedido | Continua rejeitado; fechar sem resposta cumpre a regra aprovada de não responder ERROR a ERROR. Matriz de todos os 17 outros tipos mantém a rejeição; 16 casos continuam com erro correlacionado. Red real preservado. |
| 29 pares TCP v1 retirados dos recursos operacionais | Copiados/verificados no arquivo antes da remoção; 50 pares v2 independentes em protocol/v2, manifesto com 66 total. UDP 16 intacto, POM e perfis main intactos. |
| DiscoveryOptions/OTD1 facades/listener/dial/codec autónomo do protótipo | Escolhas substituídas pela ADR-034; arquivo conserva código/fixtures/testes/logs anteriores. Não é remoção de API de fundação: construtores 13/14 e APIs operacionais base preservam nomes e passam a v2. |

[PRESSUPOSTO] A ajuda de D11a é --help existente, exercitada numa JVM separada;
interativamente mantêm-se neighbors/quit, sem alterar OttApplication.java fora
dos 29 caminhos. Não há novo comando de candidatos: existe API assíncrona
candidates(). Namespaces de TCP e UDP são comparados separadamente, como na
validação de configuração anterior; retirar TCP/dataPort OTD1 não pode criar
uma colisão fictícia entre transportes diferentes.

## Execuções reais, incluindo falhas preservadas

| Execução | Saída | Resultado/alcance |
|---|---:|---|
| [v2-implement-red-01](../../../experiments/construction/034-overlay-extra/v2-implement-red-01.log) | 1 | 3 testes; 2 falhas; 1 erros |
| [v2-implement-red-02](../../../experiments/construction/034-overlay-extra/v2-implement-red-02.log) | 1 | 4 testes; 3 falhas; 0 erros |
| [v2-implement-green-01](../../../experiments/construction/034-overlay-extra/v2-implement-green-01.log) | 1 | compilação falhou; sem testes executados |
| [v2-implement-green-02](../../../experiments/construction/034-overlay-extra/v2-implement-green-02.log) | 1 | compilação falhou; sem testes executados |
| [v2-implement-green-03](../../../experiments/construction/034-overlay-extra/v2-implement-green-03.log) | 0 | 116 testes; 0 falhas; 0 erros |
| [v2-implement-green-04](../../../experiments/construction/034-overlay-extra/v2-implement-green-04.log) | 1 | compilação falhou; sem testes executados |
| [v2-implement-green-05](../../../experiments/construction/034-overlay-extra/v2-implement-green-05.log) | 1 | compilação falhou; sem testes executados |
| [v2-implement-green-06](../../../experiments/construction/034-overlay-extra/v2-implement-green-06.log) | 1 | 1031 testes; 1 falhas; 52 erros |
| [v2-implement-green-07](../../../experiments/construction/034-overlay-extra/v2-implement-green-07.log) | 1 | 1031 testes; 3 falhas; 1 erros |
| [v2-implement-unit-08](../../../experiments/construction/034-overlay-extra/v2-implement-unit-08.log) | 0 | 935 testes; 0 falhas; 0 erros |
| [v2-implement-transport-09](../../../experiments/construction/034-overlay-extra/v2-implement-transport-09.log) | 0 | 133 testes; 0 falhas; 0 erros |
| [v2-implement-learning-10](../../../experiments/construction/034-overlay-extra/v2-implement-learning-10.log) | 0 | 95 testes; 0 falhas; 0 erros |
| [v2-implement-cli-11](../../../experiments/construction/034-overlay-extra/v2-implement-cli-11.log) | 1 | 66 testes; 1 falhas; 0 erros |
| [v2-implement-shutdown-12](../../../experiments/construction/034-overlay-extra/v2-implement-shutdown-12.log) | 0 | 80 testes; 0 falhas; 0 erros |
| [v2-implement-red-13](../../../experiments/construction/034-overlay-extra/v2-implement-red-13.log) | 1 | 2 testes; 2 falhas; 0 erros |
| [v2-implement-incremental-14](../../../experiments/construction/034-overlay-extra/v2-implement-incremental-14.log) | 0 | 1062 testes; 0 falhas; 0 erros |

red-01 prova versão/codec ausentes; red-02 prova opcionalidade/recursos OTD1 ainda
errados. As falhas de compilação green-01/02/04/05 são erros de implementação,
sem alegar red comportamental. green-06 regista 52 erros de sockets na sandbox e
uma expectativa de migração; green-07 fora da sandbox encontrou três falhas e
um erro de migração. unit-08 foi seleção sem sockets; transport-09 e learning-10
incluem TCP real. cli-11 preserva a falha de shutdown do teste v008 existente;
shutdown-12 e o conjunto final validam a correção sem relaxar o oráculo.
red-13 regista duas falhas reais: grau/C no preflight e ERROR recebido no bootstrap.

**Execução incremental da primeira apresentação (histórica):** [resultado/hashes](../../../experiments/construction/034-overlay-extra/v2-implement-incremental-14-result.json),
1062 testes, zero falhas/erros/skips; dez relatórios Surefire da própria invocação.
[Inventário dos logs da CLI](../../../experiments/construction/034-overlay-extra/v2-implement-incremental-14-cli-inventory.json)
preserva 109 ficheiros gerados nessa janela, separados de target/. Não somar
relatórios antigos. O runner inicial copiava todos os XML de target (podem conter
outras invocações); a contagem inicial vem do log. Desde 09 há XML só das classes
executadas e hashes que confirmam fontes estáveis durante a execução.

## Diffs, hashes e validação documental

- [Contra HEAD integrado — código/recursos/docs de Projeto](../../../experiments/construction/034-overlay-extra/v2-implement-project-vs-head.diff):
  4330 adições/766 remoções textuais,
  79 binários; [numstat](../../../experiments/construction/034-overlay-extra/v2-implement-project-vs-head.numstat).
- [Desde a aprovação v2 — mesmo âmbito](../../../experiments/construction/034-overlay-extra/v2-implement-project-vs-entry.diff):
  4077 adições/1282 remoções textuais,
  79 binários; [numstat](../../../experiments/construction/034-overlay-extra/v2-implement-project-vs-entry.numstat).
- [Documentação/índices/validador desde a entrada](../../../experiments/construction/034-overlay-extra/v2-implement-docs-vs-entry.diff)
  separa esta implementação das revisões documentais/Inception anteriores.
- [git diff --check](../../../experiments/construction/034-overlay-extra/v2-implement-diff-check.log) e
  [manifesto de conferência](../../../experiments/construction/034-overlay-extra/v2-implement-validation.json) confirmam âmbito,
  preservação das 338 evidências anteriores, POM/perfis main/UDP, fontes validadas
  e ausência de outro IMP. target/ é gerado e fica fora dos patches de Projeto.
- [V-ADD atual](../../../experiments/construction/034-overlay-extra/v2-implement-v-add-02.log) verifica 66 fixtures operacionais,
  50 referências independentes e arquivo de 45 originais; os hashes históricos
  de planning-source-manifest.files não foram reescritos para simular aprovação.

Na primeira apresentação, código/testes/configuração/fixtures eram os mesmos da invocação 14;
README/protocolos locais foram atualizados depois, sem alteração executável.
O JAR histórico validado continua preservado, não é um novo JAR v2 nem prova
desta versão. O diff fonte inclui ficheiros novos/não rastreados e binários;
o numstat global de ficheiros rastreados contém alterações preexistentes e
é guardado separadamente, sem as atribuir todas a esta etapa.

## Pendências e paragem obrigatória

Aguardar revisão de Implement. Não iniciar Test, clean verify completo/manual,
fecho, commits/push ou outro IMP por inferência. V-033 como conjunto de fecho
continua parcial (D12/manual/regressão atual); recolher evidência nova na etapa
Test. Revisão humana final e integração antes de DONE. IMP-035 mantém aprovação
própria de intenção/admissão/reparação; a cache disponível não é reparação feita.

## Correção D07e — correlação antiga na ligação atual

Registo de 2026-11-11T19:18:38Z. Revisão humana pediu duas correções antes de aprovar Implement.
Não muda o Design/ADR: aplica as regras já aprovadas de identidade antes de
correlação e descarte de token/geração antigos sem afetar a consulta atual.

- NeighborDiscovery.identity agora verifica sender/recipient e instâncias,
  separadamente do token HELLO. Identidade incompatível continua terminal.
  Token antigo na sessão atual retorna descarte tratado, em QUERY/SNAPSHOT válidas
  e em RejectedExtraContent; só o diagnóstico STALE é atualizado. Não fecha o
  controlo nem altera query, ritmos/cooldown, cache, watermark ou TTL.
- Geração <= watermark só regista STALE e retorna, antes de avaliar o prazo
  dessa mensagem. A consulta mantém o prazo original e pode receber a resposta
  válida posterior; os mecanismos normais de timeout continuam ativos.
- Três testes novos determinísticos em NeighborManagerTest cobrem quatro formas
  de token antigo, geração inferior/igual e oito negativos de sender/recipient.
  Os dois testes positivos reproduziram as falhas antes da correção. Confirmam
  cache/proveniência intactas após o descarte, aceitação de geração 5 no mesmo
  requestId, origem exata do TTL na consulta original, refresh sem cooldown,
  mesmo connectionId, uma única NeighborUp, zero NeighborDown e nenhum dial novo.
  QUERY antiga/rejeitada não impede a resposta à QUERY legítima seguinte.
  Nenhum método de teste anterior foi removido ou enfraquecido.

Execuções novas, preservando integralmente os relatórios anteriores:

| Evidência | Resultado real |
|---|---|
| [red-01](../../../experiments/construction/034-overlay-extra/d07e-correlation-red-01.log) | 3 testes, 2 falhas: canal fechado pelo token e resposta válida ignorada após geração antiga; negativo de identidade passou. |
| [green-02](../../../experiments/construction/034-overlay-extra/d07e-correlation-green-02.log) | 3 testes novos passaram após a correção. |
| [incremental-03](../../../experiments/construction/034-overlay-extra/d07e-correlation-incremental-03.log) | 142 testes, zero falhas, cinco erros de sockets proibidos na sandbox; preservado como execução incompleta. |
| [incremental-04](../../../experiments/construction/034-overlay-extra/d07e-correlation-incremental-04.log) | 142 testes, zero falhas/erros/skips, JDK 21 e TCP local autorizado. |

[Resultado, XML executados e hashes](../../../experiments/construction/034-overlay-extra/d07e-correlation-incremental-04-result.json).
A seleção atual inclui NeighborManagerTest (54), NeighborDiscoveryTest (20),
NeighborManagerLifecycleTest (33), CandidateCacheTest (9) e DiscoveryCodecTest (26).
Não somar estes 142 aos 1062 anteriores para alegar uma regressão completa atual.

[Diff exclusivo da correção](../../../experiments/construction/034-overlay-extra/d07e-correlation-correction.diff),
[numstat](../../../experiments/construction/034-overlay-extra/d07e-correlation-correction.numstat),
[diff atual de Projeto contra HEAD](../../../experiments/construction/034-overlay-extra/d07e-correlation-project-vs-head.diff),
[V-ADD atual](../../../experiments/construction/034-overlay-extra/d07e-correlation-v-add.log) e
[conferência final](../../../experiments/construction/034-overlay-extra/d07e-correlation-validation.json).
Só dois ficheiros Java mudaram desde a revisão anterior: NeighborDiscovery.java
(identidade/descarte) e NeighborManagerTest.java (helpers/três testes novos).
Bytes/fixtures/configuração/POM/UDP e JAR histórico permanecem intactos. Os 595
ficheiros de evidência já presentes à entrada desta correção foram preservados
por hash. target/ fica excluído dos diffs fonte.

Implement volta a revisão, sem avanço a Test, clean verify, manual, fecho,
commits/push ou IMP-035. Continuam necessários regressão completa atual,
manual humano antes da aprovação de Test, revisão final e integração.

## Aprovação de Implement e entrada em Test

Registo de 2026-11-11T19:31:01Z. O utilizador aprovou Implement e as correções D07e.
[Relatório Test atual](ddd-03-test-report.md): clean verify JDK 21 com 1065
testes aprovados; manual do agente com 53 CHECK_PASS, MANUAL_PASS e saída 0.
A repetição humana de Luis e a aprovação de Test continuam pendentes, assim
como revisão final e integração. Os estados de paragem anteriores são histórico.
Sem commits/push ou início do IMP-035.

## Atualização de fecho — Q-C-19

2026-11-11T19:42:24Z: Luis repetiu o manual V-033 com 53 checks/saída 0 e pediu revisão final, integração e fecho. A revisão técnica está concluída. Por instrução explícita foram retirados os arquivos TCP v1 e a variante abandonada; as referências anteriores a retenção de arquivos são históricas. Negativos de versão TCP v1 e todo UDP OTT1 continuam exigidos. [Estado e limpeza atuais](closure-walkthrough.md).
