# Controlo TCP v2 e dados UDP OTT1

Contrato corrente: [ADD/ADR-034](../../add.md#adr-034), [interfaces](../../docs/07-API-AND-INTERFACES.md) e [Design IMP-034](../../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md). Um programa/versão de JAR em todos os processos, um codec TCP operacional (MessageCodec), sem runtime TCP v1 ou fallback.

## Envelope TCP

u32 big-endian bodyLength, seguido do corpo completo de 37..65536 bytes. Envelope: version u8=2, type u8, requestId UUID (16), senderId string u16+1..32 bytes ASCII, senderInstance UUID (16). UTF-8 estrito e limites antes de alocar; EOF só é limpo antes do prefixo. O envelope com ID de um byte tem 37 bytes; o payload do tipo continua obrigatório. Descoberta tem cap adicional de corpo 32768.

| Código | Tipo |
|---|---|
| 1 / 2 | REGISTER / REGISTER_OK |
| 4 | NODE_LEAVE |
| 5 / 6 | HELLO / HELLO_OK |
| 7 / 8 | PROBE_PING / PROBE_PONG |
| 9 / 10 | STREAM_ANNOUNCE / STREAM_WITHDRAW |
| 11 / 12 / 13 / 14 | ACTIVATE / ACTIVATE_ACK / DEACTIVATE / SUBSCRIPTION_RENEW |
| 15 / 16 | ROUTE_TRACE / TRACE_RESULT |
| 17 | ERROR |
| 18 / 19 | DISCOVERY_QUERY / DISCOVERY_SNAPSHOT |

18 tipos com códigos explícitos; tipo 3 retirado/rejeitado. Os códigos do payload ERROR mantêm o contrato anterior (incluindo UNAUTHORIZED_NEIGHBOR=3); relatedType é o octeto bruto do tipo relacionado. Nunca responder ERROR a ERROR. O codec representar routing não significa que o serviço de routing esteja implementado.

## Bootstrap e vizinhos

REGISTER leva Endpoint e REGISTER_OK só a lista configurada com instâncias zero, sem leases/membership. A conexão de consulta inicial v2 fecha depois de write/flush completos, com prazo. O bootstrap só admite REGISTER; depois do sucesso os nós não voltam a consultá-lo por falhas de vizinhos.

HELLO leva Endpoint+supportedMask u32+requiredMask u32; HELLO_OK leva selectedMask u32. Exigir supported=required=selected=1 e bits reservados zero. Validar identidade, endpoint/grafo, instância, requestId e iniciador menor nodeId, preservando unicidade e as barreiras de flush. Cada par tem um TCP persistente, uma FIFO/writer e um frame em escrita. Descoberta e mensagens normais partilham esse canal.

## Aprendizagem limitada

QUERY/SNAPSHOT levam helloRequestId, recipientId e recipientInstance. SNAPSHOT acrescenta geração positiva crescente por sessão e até 63 endpoints diretos ACTIVE. Cache volátil: 128 IDs, quatro fontes por ID, TTL local 60 s desde a admissão da QUERY. Não retransmitir hearsay, não carregar catálogo global; a morte do anunciante conserva claims até expirar. Identidade da instância anunciada é pista, sem autenticação criptográfica.

Descoberta obrigatória em todo overlay, Q>=2 offline. Admissão extra deixa uma posição livre para tráfego normal, no máximo um extra enfileirado e um em escrita por ligação, quatro tickets físicos globais até terminação. BACKPRESSURED não fecha o controlo. Mantêm-se ritmos, prazos e limites do Design.

Anúncio completo inválido de sessão atual pode ser rejeitado atomicamente sem alterar cache/geração/TTL, seguido de mensagem normal v2 na mesma ligação. Framing, truncamento e identidade incompatível são terminais no handle afetado. Tentativa inválida tem diagnóstico/estado limitado por peer e retry v2/backoff existentes; não causa FAILED global, FATAL, saída da CLI ou queda das sessões saudáveis. Shutdown fecha admissões, conserva o prazo global e não confunde cancelamento de future com terminação física.

## UDP e arquivo

Vídeo mantém OTT1/version 1, cabeçalho 64, total <=1200, payload <=1136 e restantes caps. O [arquivo TCP v1](../../memory-bank/bolts/034-overlay-extra/closure-walkthrough.md#limpeza-autorizada) é histórico. As [fixtures operacionais](../src/test/resources/protocol/README.md) são 50 TCP v2 e 16 UDP originais; os vetores esperados são independentes do codec Java.

IMP-034 apenas aprende/retém candidatos. IMP-035 trata reparação após falhas sob checkpoint próprio; não se alega continuidade de vídeo neste incremento.
