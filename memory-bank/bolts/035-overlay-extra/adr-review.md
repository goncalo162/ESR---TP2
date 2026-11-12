---
stage: adr
bolt: "035-overlay-extra"
status: "approved"
created: "2026-11-11T23:28:20Z"
---
# Revisão ADR — IMP-035 / reparação direta

**Revisão aprovada.** Registo da aprovação explícita em 2026-11-11T23:39:43Z.
ADR-035 Aceite/substituição parcial019/033/034; G-EXTRA-ADR satisfeito para IMP-035.
Model/Design/ADR concluídos. Apenas Implement autorizado, parar antes de Test.
A análise abaixo foi feita antes do código; a evidência dessa revisão permanece intacta.

## Decisão formalizada e substituição parcial

[ADR-035 no ADD](../../../add.md#adr-035) e
[registo detalhado](adr-035-proposal.md). Os 21 caminhos estão aprovados no
[IMP-035](../../../add.md#imp-035) e no [catálogo](../../architecture-traceability.json),
sem autorização de alterações Java nesta fase.

| Decisão anterior | Delta aceite em ADR-035 | Garantias que se mantêm |
|---|---|---|
| ADR-019: só menor ID inicia e grafo autoriza HELLO | Para finalidade20, qualquer isolado inicia com candidato aprendido e completa na mesma ligação. Menor originatorId desempata só saídas concorrentes ainda pendentes. | HELLO5/grafo/iniciador menor no caso configurado; correlação por conexão/execução, uma sessão por par e ACTIVE nunca substituída por tentativa tardia. |
| ADR-033 residual: extra de novas arestas com admissão própria | Admitir unilateralmente no laboratório: origem verifica cache/proveniência/TTL, destino verifica handshake/limites sem comprovar aprendizagem. | Cache128x4, TTL60s desde QUERY, fontes/gerações, retenção após anunciante morrer, sem catálogo ou bootstrap de reparação. |
| ADR-034:18 tipos e máscaras1 | Um tipo20 REPAIR_HELLO com payload Hello existente;19 tipos, máscaras3 em HELLO5/20/ACK6; mesmo codec/version2. | Framing37..65536, UTF-8/EOF/limites, código3 rejeitado, payloads restantes/snapshots/UDP intocados, sem runtime antigo/fallback. |

Os estados Aceites anteriores não são mudados para Substituída integralmente.
A nova ADR identifica exatamente as cláusulas afetadas e mantém os estados
históricos até à aprovação formal da revisão. Não reabrir IMP-034 DONE nem
atribuir a este registo execução do contrato novo.

## Conformidade com as restantes decisões

- ADR-007/009/012: sem dependência runtime nova; um codec e fixtures independentes.
  Quatro fixtures de máscara serão migradas,12 novas, alvo78; atualmente66.
  As restantes46TCP/16UDP, incluindo snapshots, mantêm bytes. Tipo20 não mistura
  finalidade reparadora com HELLO5 nem reutiliza3; não é nova propriedade.
- ADR-013/015: bootstrap imutável só inicial; não recebe reparação e rejeita20.
  Send=ACCEPTED continua enfileiramento; sucesso sendAndFlush é write+flush,
  não receção remota. A resposta6/ERROR usa mesma conexão com prazo.
- ADR-014/017: mesmos Transport/FIFO/owner, DNS fora do owner e reservas físicas.
  Não há listener/pool/timer físico novo, nem Connection.terminated. Perdedor
  mantém cobrança até terminação real; callbacks são curtos e fora de locks.
- ADR-018/020: comparar requestId/connectionId/instanceIds/contexto/needEpoch;
  preservar saudável, descartar callbacks antigos e não causar FAILED global
  por rejeição de peer. Shutdown usa orçamento restante, sem joins no owner.
- ADR-021: laboratório cooperativo, sem prova criptográfica da aprendizagem no
  destino. Esta política foi aceite explicitamente; não a apresentar como
  defesa contra autoafirmações maliciosas válidas no formato.
- ADR-024/026: um IMP, âmbito21 aprovado e índices derivados coerentes. Nenhuma
  CI/linter/JaCoCo/dependência ou cobertura percentual nova; V-034 real antes de DONE.

## Conferência estática com APIs e código reais

Esta fase pode analisar fontes; nenhum fonte foi alterado. As fontes conferidas
são as da base22622c1, com todos os206 hashes não gerados/JAR preservados.
Não é teste Java nem prova de que a reparação já funciona.

| Superfície conferida | Evidência atual | Integração exigida dentro dos21 caminhos |
|---|---|---|
| [ControlMessage](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/ControlMessage.java), MessageType/MessageCodec | Hello(Endpoint,long,long), HelloOk(long) existem; payload imutável, v2 e máscaras1 fixas no codec/gestor. | Reutilizar Hello para20; migrar validação/máscaras3 e enum/codec, sem RepairMessage nem campos de prova. |
| [Connection.sendAndFlush](../../../Projeto/src/main/java/pt/uminho/esr/ott/transport/Connection.java) | Proíbe apenas18/19 na API normal e usa o encoder/FIFO; fecha/falha com limites. | Tipo20 usa caminho normal de handshake quando codec o reconhecer. Não alterar Connection/Transport. TransportTest cobre novo input e invariantes. |
| [Transport.connect e reservas](../../../Projeto/src/main/java/pt/uminho/esr/ott/transport/Transport.java) | Reserva socket antes do dial; cancelamento fecha socket; conexão só liberta após ambos workers terminarem. | Preservar reserva física/barreiras; nenhuma contabilidade nova que liberte pelo future lógico. |
| [NeighborManager.handle/hello](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborManager.java) | Entrada aceita só5, lookup de peer configurado, menor ID; capacidade1. application() cobre7..19. | Separar ramo20 para admissão cooperativa, não relaxar HELLO5. Não entregar20 como mensagem normal de uma sessão ACTIVE. |
| beginOutbound/ERROR/drain | Pedido normal usa5 e há relatedType5/6 fixo nas janelas de erro; requestId é gerado no início do HELLO. | Contexto guarda finalidade/pedido original5 ou20; correlacionar ERROR com o tipo real e manter barreira de flush. Pedido20 conserva o ID da tentativa reservada antes do dial. |
| dialResult/UNCLASSIFIED/nativeDials | Associação ao handle ocorre antes de retirar token da barreira; frames retidos têm B/R. | Reservar PairSlot antes de DNS, não classificar opened como inbound cedo; manter associação mesmo para perdedores lógicos até resultado/cleanup próprio. |
| activate/finish/retire/phaseSlotCancel | Proteções p.context==c/p.session e timers partilhados por Peer já existem. | Eleger antes de ACK/Up; retirar perdedor sem cancelar slot/timer do vencedor; fechar callback antigo não apaga nova sessão. |
| [NeighborDiscovery](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/NeighborDiscovery.java) | Tick100ms único/coalescido e cache.snapshot no owner; tokens/gerações antigos descartados. | Compor maintain de Recovery no mesmo tick, sem segundo job. Revalidar cache internamente, sem esperar pela API assíncrona pública no owner. |
| [CandidateCache](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/CandidateCache.java) | CandidateView.endpoint opcional; sources imutáveis com SourceKey/generation/queryStarted/ttl/observedInstance. | Consumir e congelar os dados da seleção sem mudar a API/cache; atualização posterior não prolonga prazo original da tentativa. |
| [OverlayNode](../../../Projeto/src/main/java/pt/uminho/esr/ott/overlay/OverlayNode.java) | SerialOwner emprestado ao gestor, APIs de snapshot/candidates/discoveryStatus e shutdown global. | Compor Recovery no owner existente e publicar recoveryStatus imutável; CLI limitada, sem futures bloqueantes. |

### Cuidados vinculativos para implementação e oráculos

1. D09/D10: duas saídas correntes elegem mesmo originator menor; entrada já
   escolhida impede lançar a oposta. ACTIVE bloqueia eleição tardia. Com falta
   de slot antes de ler HELLO, observar capacidade/retry, não eleição fictícia.
2. D11/D12/D19: não aplicar indiscriminadamente retire/phaseSlotCancel ao Peer
   partilhado: só cancelar o que pertence ao Attempt/Context perdedor. Manter
   requestId/connectionId/instanceIds/epoch até filtrar eventos atrasados.
3. D13/D14: dinâmicos não entram na política ilimitada de retries configurados.
   Reconexão de aresta configurada conserva o fluxo; aresta dinâmica caída volta
   a seleção de Recovery e só tenta com cache válida. Usar tick comum para
   espera/backoff/expiração, sem um timer extra por candidato/peer dinâmico.
   Não aumentar o orçamento de timers C+configurados+1 somando arbitrariamente
   todos os dinâmicos ao número de configurados. Contextos físicos usam C.
4. D07/D15: fixar geração/sourceKey/queryStarted/TTL e endpoint da seleção e o
   orçamento10s. Ler snapshot mais recente não renova o prazo original; conflito,
   isolamento resolvido ou expiração impedem commit local. ACK/flush tardio não
   prolonga autorização. Uma sessão já ACTIVE não depende do TTL do claim.
5. D01/D08/D18: capacidades3 obrigatórias nos dois caminhos; os restantes payloads
   existentes, snapshots e UDP mantêm formato. Invalid HELLO5 fora do grafo
   continua negativo;20 válido pode introduzir ID conforme política aprovada.
6. D20: testes de lógica com clock/transporte/escrita controlados e TCP real;
   mortes reais de processos/mesmos sobreviventes nos dois grafos e manual
   humano antes de aprovação de Test, sem substituir por close/EOF embutido.

Estas são concretizações das obrigações aprovadas, não alterações adicionais de
wire/âmbito/política. Não foi identificada decisão aceite inviável ou necessidade
de outro caminho nesta análise estática. Viabilidade sob todas as interleavings
continua por demonstrar em Implement/Test; se surgir conflito material, parar
parte afetada com evidência e revisão conforme ADD11, sem alterar fundações
fora de âmbito ou enfraquecer testes.

## Estado do gate e paragem

Aprovação explícita de Model/Design/Q-C-22/21 caminhos e desta revisão ADR.
ADR-035 Aceite; G-EXTRA-ADR satisfeito para IMP-035. Implement em curso;
[walkthrough](implementation-walkthrough.md) para revisão antes de Test.

[Entrada e hashes](../../../experiments/construction/035-overlay-extra/adr-entry.json),
[V-ADD](../../../experiments/construction/035-overlay-extra/adr-v-add-02.log),
[diff desta etapa](../../../experiments/construction/035-overlay-extra/adr-vs-design.diff)
e [conferência final](../../../experiments/construction/035-overlay-extra/adr-validation.json).

V-034, regressão atual JDK21, fixtures novas, manual humano com mortes reais,
revisão humana final e integração continuam obrigatórios e pendentes. Sem Java,
Maven, manual, commits, push, fecho ou início de outro IMP nesta etapa.
