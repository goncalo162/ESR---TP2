---
stage: design
bolt: "035-overlay-extra"
created: "2026-11-11T22:35:39Z"
revision: "02-direct-repair"
status: "approved"
---
# Design revisto — reparação na ligação iniciada pelo isolado

**Design/Q-C-22/21 caminhos aprovados em registo de 2026-11-11T23:28:20Z;
revisão ADR/ADR-035 aprovadas em registo de 2026-11-11T23:39:43Z.**
G-EXTRA-ADR satisfeito para IMP-035; só Implement autorizado, parar antes de Test.
O contrato abaixo é o desenho aprovado; o mapa de provas atual consta do
[walkthrough](implementation-walkthrough.md), sem presumir a validação completa.

A proposta anterior de permissões/registry/retorno foi retirada da proposta
ativa e preservada, com hashes, em experiments/construction/035-overlay-extra/.
Não foi implementada. A regra antiga de menor ID é alterada apenas por proposta
de [ADR-035](adr-035-proposal.md), não por edição silenciosa da ADR-019 Aceite.
Na etapa Design, Java não foi lido/analisado/alterado; a revisão ADR confere
agora as APIs reais, sem modificar os fontes.

## 1. Comportamento e cenários preservados

Q-C-20 mantém procura autónoma só quando zero vizinhos ACTIVE. Um nó saudável
pode admitir o isolado sem perder as sessões existentes. Cache/proveniência/
TTL aprendidos antes das falhas continuam a origem exclusiva da seleção local.

- S–R1/R2–C: após morrer R1 conserva-se R2 e não se procura nova aresta. Após
  morrer R2, S/C isolados podem iniciar a reparação um para o outro; fica S–C.
- S–R1–R2/R3–C: a primeira morte conserva a alternativa. Depois da segunda,
  C inicia REPAIR_HELLO para R1; R1 responde no mesmo TCP e conserva S–R1,
  incluindo connectionId. Resultado S–R1–C.
- Variantes com IDs invertidos: o isolado maior também inicia e completa nessa
  ligação. O saudável não faz dial de retorno nem tem de ficar isolado.

Manter mesmos processos/instanceIds sobreviventes e bootstrap parado. Sem
candidato válido, publicar isolamento e backoff limitado. Morte silenciosa sem
EOF/RST depende da deteção futura do IMP-007. Controlo reparado não comprova
routing, caminho de stream, vídeo, FPS ou recuperação da rede IP underlay.

## 2. Mínimo de protocolo e confiança do laboratório

Propor **um tipo novo, 20 REPAIR_HELLO**, com o payload Hello existente. Usa o
mesmo requestId, Endpoint, supportedMask/requiredMask e parser/envelope TCP v2.
A resposta é **6 HELLO_OK**, com requestId correlacionado, ou ERROR17 já existente
quando o contexto permite responder. Não criar GRANT/INTENT/RESULT/BIND, nova
permissão, novo ACK, listener, porta, writer ou codec.

Zero tipos novos implicaria sobrecarregar HELLO5 sem distinguir a política do
grafo inicial da admissão reparadora, ou acrescentar um campo a todos os HELLO.
Um tipo separa explicitamente a finalidade, mantendo o payload e as garantias
comuns. O próprio handshake expressa a intenção; não há pré-handshake separado.

**Admissão unilateral mínima proposta para o laboratório cooperativo:**

1. A origem só escolhe endpoint não-self, AVAILABLE, com pelo menos um claim
   válido na cache128x4/TTL60 s. Regista sourceKey/geração/queryStarted/expiração
   nos seus logs/evidência. Revalida isolamento, endpoint, conflito e TTL antes
   de enfileirar e antes de publicar a sessão. Não obtém catálogo ou prova nova
   após morrerem os anunciantes.
2. O destino não exige cache recíproca. Valida versão/tipo, senderId igual ao
   Endpoint.nodeId, senderInstance não zero igual à do Endpoint, papel overlay,
   host/portas lexicais, capacidades, requestId e limites. Se o ID já é configurado,
   exige o mesmo endpoint configurado; se é novo, não pode colidir com o próprio
   nó, bootstrap ou endpoint de outro peer configurado/admitido. Comparar TCP/UDP
   nos namespaces corretos. Não inferir identidade pelo IP remoto ou fazer DNS
   para validar a identidade.
3. O destino admite o pedido válido no laboratório, com estado volátil limitado,
   sem verificar uma permissão emitida por si. A prova de aprendizagem é local
   à origem; **o destino não comprova que o emissor aprendeu antes da falha**.
   Isso é a simplificação de confiança proposta, não uma garantia criptográfica.
4. Um HELLO5 de ID fora do grafo continua rejeitado pela regra normal. Um
   REPAIR_HELLO20 formalmente válido pode introduzir esse ID conforme a nova
   política cooperativa. Não alegar que se rejeita toda autoafirmação maliciosa
   com campos válidos; a ADR-021 já assume laboratório sem autenticação forte.

Retirar permissões não retira os controlos de identidade, unicidade e recursos.
Se a revisão exigir comprovação da introdução pelo destino, essa é uma política
mais forte e requer mecanismo próprio; não atribuir essa propriedade ao tipo20.

## 3. Revisão proposta da ADR-019

Regra normal mantida: para arestas configuradas, só o menor nodeId inicia HELLO5.
Exceção proposta: para reparar isolamento com candidato aprendido válido,
qualquer extremo pode iniciar REPAIR_HELLO20. **Não exigir que seja o menor**.
O recetor responde e o mesmo TCP completa HELLO_OK/flush, sem dial de retorno.

O menor ID passa a ser desempate de tentativas concorrentes ainda pendentes,
não uma condição universal para iniciar a reparação. Sessão ACTIVE nunca é
substituída para fazer prevalecer a ordem de IDs. Aplicar a exceção de admissão
fora do grafo da ADR-033/REQ-025 explicitamente na ADR-035 Proposta.

### 3.1. Eleição de tentativa e slot do par

Preservar a barreira de associação dos dials da base: opened antes do future
não prova direção inbound. Um Context UNCLASSIFIED retém eventos nos limites
B/R até associação por token de dial/connectionId; a eleição só usa contextos
com direção e execução confirmadas. Reservar a saída antes do DNS impede que
esse atraso esconda uma saída concorrente. Cancelamento lógico não é término
físico do dial/resolver.

O owner serial mantém um PairSlot por par canónico de IDs. Cada tentativa tem
originatorId (emissor do HELLO), requestId, connectionId, sender/peer instanceIds,
needEpoch quando autónoma e estado. O contexto escolhido e os contextos a fechar
são distintos; um close/callback do perdedor não pode remover o escolhido.

1. Se já há ACTIVE para o par, conservar essa sessão. Rejeitar nova tentativa
   com DUPLICATE_ID, independentemente de originator menor/maior ou nova instância.
2. Uma tentativa de saída só pode nascer se o par não tem uma admissão escolhida
   ou outra saída corrente. Reservar o slot no owner **antes de resolver/dialar**.
3. Ao chegar um HELLO válido, se existe uma saída local concorrente para esse
   mesmo par e ambos os contextos ainda são pendentes, escolher o contexto cujo
   **originatorId é lexicograficamente menor**, com a comparação de IDs da base.
   Ambos os extremos escolhem a mesma direção: A→B se A<B.
4. Fechar/cancelar logicamente o perdedor, preservando a cobrança física até
   terminação real. O vencedor usa o TCP que já iniciou/recebeu; não redialar
   apenas para aplicar o desempate.
5. Se não há saída concorrente, reservar e admitir a ligação recebida, mesmo
   iniciada pelo ID maior. Esse slot bloqueia uma nova saída local para o par.
6. Duplicata do mesmo originator não abre nova eleição por UUID ou tempo remoto:
   manter o requestId/contexto corrente e rejeitar a duplicata. Não reutilizar
   requestIds; eventos/ACK antigos não adotam slot livre nem trocam o escolhido.
7. Escolher o contexto antes de enfileirar HELLO_OK e manter barreiras de flush
   e retenção B/R existentes. Só o escolhido pode publicar NeighborUp. Ao passar
   a ACTIVE, congela-se a eleição; uma tentativa tardia não provoca NeighborDown.

O desempate é determinístico para **saídas sobrepostas**. Não promete uma direção
fixa quando uma ligação foi admitida antes de existir a outra tentativa; nesse
caso o slot escolhido/ACTIVE prevalece. A classificação não usa uma janela
arbitrária, menor RTT, relógio remoto ou «último socket substitui o primeiro».
Um nó nunca começa uma segunda saída para um par cuja entrada já reservou.

Se C/S ambos iniciam, a direção do menor vence enquanto pendentes. Se apenas
S isolado inicia para C saudável e S>C, a direção S→C é válida e mantém-se.
Ambos os casos terminam com uma sessão por par. Contextos perdedores podem existir
transitoriamente até fecharem; todos contam em C e não são duas sessões ACTIVE.

## 4. Sequência única de handshake

```mermaid
sequenceDiagram
    participant A as Isolado (ID menor ou maior)
    participant B as Candidato aprendido / destino
    Note over A: Cache válida e zero ACTIVE; reservar tentativa
    A->>B: Dial controlPort + REPAIR_HELLO20
    Note over B: Validar identidade, endpoint, capacidades e limites; escolher PairSlot
    B-->>A: HELLO_OK6 correlacionado na mesma ligação
    Note over A,B: Barreiras de write/flush e contexto corrente
    Note over A,B: Uma sessão ACTIVE; sessões saudáveis de B preservadas
```

Não há chamada adicional a BIND, espera de retorno, ACK de intenção, permissão
anterior nem promoção entre dois protocolos. A origem publica ACTIVE após a sua
escrita HELLO e receção ACK válidas; o recetor segue a confirmação de escrita
HELLO_OK já existente. Send=ACCEPTED e fila vazia não são flush/ACK remoto.

Se a origem entretanto ganha outro vizinho, perde o claim ou cancela antes do
commit local, retira a tentativa e fecha esse contexto. O recetor pode observar
o fecho após ter escrito ACK, tal como no handshake anterior; não inventar uma
confirmação distribuída nova. Isso não altera as outras sessões saudáveis.

## 5. Contrato binário proposto

- Um único codec/envelope TCP version2; body37..65536 e u32 big-endian, UTF-8
  estrito, tipo3 retirado. Total proposto **19 tipos ativos: 1,2,4..20**.
- Novo20 tem o mesmo payload de HELLO5: Endpoint seguido de supportedMask u32
  e requiredMask u32. Sem bytes de intenção, token, endpoint destinatário,
  provas de introdução ou timestamps remotos. Corpos HELLO5/20:72..386.
- HELLO_OK6 mantém payload selectedMask u32 e corpo41..72; ERROR17 mantém
  code/relatedType/detail. ERROR correlacionado pode ter relatedType20, sem
  acrescentar códigos ErrorCode. Não responder ERROR a ERROR.
- Propor capacidades obrigatórias DISCOVERY=1 e REPAIR=2, portanto
  supported=required=selected=3 em HELLO5/20/ACK6; bits restantes zero. Não
  negociar subconjunto nem admitir apenas descoberta. É delta explícito de
  ADR-034 para aprovação, preservando os tamanhos/estruturas dos tipos5/6.
- HELLO_OK é validado contra o peer/endpoint esperado do dial, o requestId e a
  instância não zero do handshake corrente. Instância observada na cache é pista;
  o handshake confirma a execução real, sem misturar callbacks de outra conexão.
- Bootstrap/REGISTER e restantes payloads, DISCOVERY_QUERY18/SNAPSHOT19 e
  UDP OTT1 permanecem byte a byte. Snapshot não ganha provas/flags; cache e
  DiscoveryMessage/DiscoveryCodec não precisam de novo formato/API.
- Cap32768 continua apenas para descoberta18/19. REPAIR_HELLO20 é handshake
  limitado pelo formato72..386 e framing global, não tráfego periódico extra.
  Tamanho/UTF-8/EOF/limites continuam validados antes de alocações incompatíveis.

Quatro fixtures existentes migram apenas nas máscaras: tcp-hello e tcp-hello-ok
1→3; tcp-invalid-capability-not-required usa supported3/required0 e reserved
usa supported7/required3 para continuar a testar bit reservado4. Missing0/0
conserva bytes. Restantes46TCP e16UDP mantidos, incluindo **todos os snapshots**.

Propor12 fixtures novas independentes: positivas REPAIR_HELLO min/max; dez
negativas para only-discovery, obrigatória ausente, bit reservado, requestId0,
truncamento de Endpoint, role inválido, comprimento pequeno, comprimento acima
do cap global, tipo21 desconhecido e duplicata rejeitada no owner. Esta última
é wire válido; não rotular erro de estado como erro do codec. Alvo78 fixtures:
50TCP (quatro migradas)+12TCP novas+16UDP. Nenhuma criada neste Design.

## 6. Recursos, prazos e reconexão

Conservar cache128x4, TTL60 s desde QUERY, retenção após morte do anunciante,
gerações e D07e. Apenas consultar/revalidar snapshots existentes; não escrever
provas/claims artificiais ou prolongar prazos para concluir um handshake.

- Uma tentativa autónoma de reparação por nó, uma saída por par, até duas
  admissões recebidas pendentes e até min(16,C) entradas dinâmicas. Configurados
  mantêm tabela<=63; grau ACTIVE total<=min(63,C), C<=64. Tabelas distintas não
  permitem somar sockets acima de C. Falta de espaço produz recusa/retry, nunca
  remoção de S–R1 para abrir espaço.
- Antes de validação não criar estado Peer permanente por ID desconhecido.
  Contextos de pré-handshake/retidos respeitam C/B/R existentes. A eleição só
  arbitra os contextos que puderam ser admitidos; se o transporte recusa antes
  de ler HELLO por falta de slot, o resultado é capacidade/retry, não uma eleição
  fictícia nem recuperação garantida. Ensaiar esse caso separadamente. Perdedor da
  eleição continua a consumir reserva até os workers/dial/DNS terminarem.
  Não adicionar Connection.terminated(), pool, writer, listener ou registry.
- REPAIR_HELLO usa **sendAndFlush normal de handshake**, tal como HELLO5, numa
  ligação nova sem tráfego normal concorrente. HELLO_OK/ERROR na mesma FIFO.
  Não usar trySendExtension ou reserva da descoberta para contornar a política.
  Descoberta18/19 preserva margemQ>=2, um extra enfileirado+um em escrita,
  quatro tickets físicos e regras take→inFlight. Q1 genérico/bootstrap preservado.
- Selecionar candidatos AVAILABLE, não-self e não configurados que exijam nova
  aresta; sort por nodeId/SourceKey e round-robin. Até quatro claims por candidato;
  escolher endpoint coerente e revalidar TTL. Sem RTT ainda não implementado.
- Backoff usa overlay.retry.initial/max/jitter existentes (500/5000ms/10% defaults),
  teto incluindo jitter e aritmética verificada. Estados de cooldown limitados
  aos128 IDs atuais da cache, sem timer/dial por candidato. Falhar um candidato
  não impede tentar outro elegível. Sem candidato, não abrir socket/DNS/bootstrap.
- DNS/bind e dial conservam control.connectTimeout separados; HELLO usa
  overlay.helloTimeout, incluindo espera de flush e retenção. Orçamento total
  local proposto **10s** por tentativa, limitado pelo prazo do claim e pelos
  subprazos. Não renovar por frame duplicada. Não é SLA de recuperar em10s.
- Usar um tick100ms coalescido partilhado no owner para trabalho de Recovery;
  integrar chamada ao tick já existente, sem outro executor/timer físico.
  O registo de claims mantém a origem e os seus callbacks/limites anteriores.
- Reconexão configurada e reparação usam o mesmo owner/reservas. Primeiro
  NeighborUp válido satisfaz necessidade local; cancelar commits autónomos
  obsoletos. Não cancelar uma admissão recebida só porque o destino tem S ACTIVE.
  Tratar cuidadosamente o needEpoch local, distinto do contexto remoto recebido.

## 7. Identidade, isolamento e shutdown

Recusar20 antes de ACTIVE com identidade/endpoint/capacidade inválidos, campo
truncado, framing inválido ou contexto fora de estado. O recetor saudável não
precisa de isolamento. Peer já configurado mantém comparação de endpoint; ID
ACTIVE não pode ser substituído por instância nova ou endpoint autoafirmado.
Uma rejeição segura pode enviar ERROR17 correlacionado e fechar após flush;
envelope inseguro só fecha/diagnostica. A exceção é de finalidade20, não uma
permissão geral para admitir HELLO5 desconhecido.

Chaves de validação: par, requestId, connectionId, instanceIds e contexto/epoch
correntes. Callback de perdedor/retirado só trata os recursos próprios. Um EOF
antigo não apaga a sessão vencedora. Não associar callbacks só pelo nodeId.
Snapshot de descoberta inválida corrente conserva a recuperação de conteúdo e
D07e: tokens/query/gerações antigos não alteram query/cache/watermark/TTL/Down.

Shutdown impede novas admissões/dials, cancela lógica e usa prazo global
restante, sem acrescentar10s ao shutdown3s. Owner/callback não faz joins nem
espera por sendAndFlush. Reservas físicas só saem quando os workers terminam,
conforme infraestrutura documentada; worker preso é timeout explícito, não
sucesso fabricado. Falha/rejeição de peer ou cache vazia não causa FAILED global,
interrompe CLI ou fecha sessão saudável. Saturação crítica do owner mantém o
tratamento operacional aceite; não converter falha de infraestrutura em sucesso.

## 8. Contratos de integração e assinaturas propostas

Assinaturas conceptuais para conferir em ADR, sem analisar Java nesta etapa:

```java
OverlayRecovery(NodeConfiguration config, UUID instanceId, LongSupplier clock,
                Supplier<UUID> requestIds, RecoveryPorts ports);
void onNeighborUp(Neighbor.Session current);
void onNeighborDown(Neighbor.Session current);
void maintain(long now);
boolean isCurrent(RepairAttempt attempt);
void onAttemptTerminal(RepairAttempt attempt, RecoveryReason reason);
void requestClose(long remainingNanos);
RecoveryStatus snapshot();
record RepairAttempt(UUID requestId, UUID instanceId, long needEpoch,
                     CandidateCache.SourceKey source, String targetId) {}
interface RecoveryPorts {
    Map<String, Neighbor.Session> active();
    List<CandidateCache.CandidateView> candidates(long now);
    boolean beginRepair(RepairAttempt attempt, ControlMessage.Endpoint target,
                        long remainingNanos);
    void cancelRepair(RepairAttempt attempt);
    void publish(RecoveryStatus immutable);
}
```

NeighborManager implementa as portas e possui PairSlot, dial, Context, HELLO20/
ACK/flush e reservas. Nenhum socket é exposto a Recovery. Todas as mutações no
SerialOwner; snapshots imutáveis publicados para leitura. NeedEpoch monotónico
sem wrap; esgotamento é explícito. IDs de teste determinísticos não são tuning.

OverlayNode.recoveryStatus() é leitura de RecoveryStatus imutável publicado,
com fase/reason, ACTIVE count, candidato opcional e retry relativo. Sem conteúdo
bruto da cache ou proveniência ilimitada. A CLI ganha recovery com uma linha
+até oito candidatos/motivos; conserva neighbors/quit/EOF/--help. Não imprimir
stack traces na rede nem afirmar que existe uma rota/vídeo só por ter sessão.

## 9. Âmbito proposto 8→21 e testes existentes

[Âmbito revisto](scope-change-proposal.md). Esta revisão retira as alterações
propostas a Connection/Transport, permissões/RepairMessage, CandidateCache,
DiscoveryMessage/DiscoveryCodec e provas/GRANT. Os21 caminhos estão aprovados e refletidos no ADD. O início de Implement
continua dependente da aprovação formal da revisão ADR.

NeighborDiscovery só fornece o tick comum à política, sem alterar wire, cache
ou cadência de consultas. Os testes de aprendizagem isolada mantêm os seus
oráculos. Nos e2e que esperavam ausência de reparação da aplicação IMP-034,
justificar a mudança de resultado por requisito novo; continuar a provar
aprendizagem/retention/TTL e separar «Discovery não cria aresta» de «aplicação
agora repara». Não introduzir flag off, apagar ou enfraquecer testes.

## 10. D01–D20 para M01–M18 / V-034 (oráculos aprovados)

| Caso | Oráculo para Implement/Test | M |
|---|---|---|
| D01 | Um codec/version2/19 tipos; máscaras3;78 fixtures alvo; tipo3/versões inválidas; snapshots/UDP byte a byte. | M01 |
| D02 | ZeroACTIVE inicia necessidade; primeira morte com alternativa não procura; sem cache não há dial. | M02/M07 |
| D03 | S–C após mortes sequenciais reais de R1/R2, mesmos sobreviventes e bootstrap parado. | M03/M18 |
| D04 | S–R1–C após R2/R3, mesmo connectionId S–R1. | M04/M18 |
| D05 | Isolado maior inicia20 e completa na mesma conexão; saudável responde6 sem dial de retorno/cache recíproca. | M05 |
| D06 | Isolado menor: mesmo caminho20/6; um dial, uma sessão, mesmo connectionId do pedido aoACTIVE. | M05/M14 |
| D07 | Origem só usa claim válido antes de enviar/commit; TTL/CONFLICT/self/ausência/candidato indisponível e logs de proveniência. | M07/M08/M11 |
| D08 | Endpoint/sender/instance/role/caps inválidos recusados; HELLO5 desconhecido continua rejeitado;20 válido unilateral admitido segundo confiança de laboratório. | M06 |
| D09 | Saídas reservadas antes de DNS/dial; opened antes do future mantém barreira UNCLASSIFIED/B/R; variar HELLO/flush e eleger menor originator enquanto pendentes. | M09/M17 |
| D10 | Entrada reservada antes da segunda saída impede essa saída; ACTIVE de maior ID nunca é trocada por tentativa tardia do menor. | M09/M13 |
| D11 | Duplicata do mesmo originator/requestId/contexto e instância antiga não substitui corrente; close do perdedor não gera Down do vencedor. | M12 |
| D12 | Nova instância/endpoint enquanto IDACTIVE e conexão configurada concorrente: manter saudável e unicidade, correlação por conexão. | M06/M13 |
| D13 | Cache128x4/TTL60/grau63/C/dinâmica16/admissões2, losers ainda físicos e filas B/R; sem caps ampliados. | M10 |
| D14 | sendAndFlush de20/6/ERROR, Q2/overflow normal, quota/tickets da descoberta intactos; release só físico. | M10/M16 |
| D15 | Clock falso de TTL/handshake/10s/backoff/jitter, justiça de candidatos, callbacks tardios sem renovar prazo/claim. | M08/M11/M17 |
| D16 | D07e integral em18/19 com sessão reparada: token/query/geração antigos não Down nem terminam consulta. | M12 |
| D17 | Aresta reparada só anunciada depois de Up, sem transformar hearsay em adjacência. | M14 |
| D18 | CLI/recovery úteis com peer rejeitado; bootstrap rejeita20 e continua só inicial; sem efeito global. | M01/M15 |
| D19 | Shutdown com dials/handshakes/perdedores pendentes usa restante global; terminação presa explícita, sem joins no owner. | M16 |
| D20 | TCP real, JDK21 clean verify/XML/hashes, manual SIGKILL e repetição humana; revisão/integração reais. | M17/M18 |

Injetar clock, fonte de requestId, resolver/transport/writer/flush e SerialOwner
manual. Sinais físicos de fakes só terminam reservas quando o teste os liberta,
não ao cancelar future. Sem criar API pública nova de terminação. TCP real
comprova conexão/ACK/EOF e o mesmo connectionId, não um future falso.

## 11. Evidência futura e checkpoint

Roteiro em experiments/overlay-recovery/ após autorização de Implement: mesmo
JAR/hash, snapshots antes das falhas e logs de origem/proveniência, bootstrap
parado, PIDs inequívocos, SIGKILL sequencial e nenhuma aresta após primeira morte,
aresta única após segunda. Verificar ambos os sentidos de IDs e simultâneos,
sem matar sobreviventes, depois shutdown/portas/processos. Repetição humana
antes de aprovar Test. Loopback não substitui CORE/vídeo.

Nada foi executado neste Design: V-034, codec Java novo, regressão e manual
continuam pendentes. Os1065 testes/53checks do pré-requisito são históricos.

Revisão atual: [Q-C-22](../../../docs/construction-duvidas.md#q-c-22--simplificação-da-reparação-na-mesma-ligação),
[ADR-035 Proposta](adr-035-proposal.md), [auditoria offline](../../../experiments/construction/035-overlay-extra/design-02-wire-audit.json),
[V-ADD](../../../experiments/construction/035-overlay-extra/design-02-v-add-03.log),
[diff desta revisão](../../../experiments/construction/035-overlay-extra/design-02-vs-design-01.diff)
e [conferência final](../../../experiments/construction/035-overlay-extra/design-02-validation.json).

**Design concluído; revisão ADR em curso.** A aprovação de Design/Q-C-22/âmbito
não é aprovação formal da ADR-035 nem autorização de Implement.
Sem Java/Maven/Test/fecho/commits/push ou outro IMP.

## Aprovação e handoff para ADR

Registo de 2026-11-11T23:28:20Z. Design/Q-C-22/21 caminhos aprovados explicitamente.
[Revisão ADR](adr-review.md) confronta APIs reais e formaliza substituição
parcial de019/033/034 em ADR-035 Proposta. Sem Java/Maven/Implement/Test.

## Concretizações de Implement

RepairAttempt acrescenta geração/queryStarted/ttl/selectedAt ao esboço conceptual
para fixar a autorização original. Um evento RepairStarted transporta esse record
imutável uma vez por tentativa local admitida, sem dump da cache. RecoveryStatus
permanece uma vista limitada. São concretizações do registo de proveniência
exigido em D07, sem alteração wire/política/âmbito.
