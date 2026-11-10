# IMP-005 — justificação concreta das substituições em NeighborManager

Comparação contra a base operacional 90cded6. Não foi removida API histórica,
nenhum teste histórico foi editado e o corpo inteiro de send() de aplicação é
idêntico. As 14 linhas removidas no numstat são linhas substituídas pelos guardas
e integrações abaixo; nenhum comportamento antigo foi eliminado fora da nova
transição lifecycle autorizada. A autorização de Q-C-09 não reabre IMP-004.

| Local / linhas antigas substituídas | Critério que exige a adaptação |
|---|---|
| start: 1 guarda | L08/M15: após novo pedido de shutdown não criar serviços nem reviver o nó |
| command: 1 guarda + 1 linha de despacho | L08: novos comandos e os ainda na fila devolvem fechado sem enviar; send() permanece intacto |
| post: 1 assinatura + 1 guarda | L04/L06/L08: bloquear eventos normais, mas manter conclusões de flush e timer lifecycle admissíveis durante drenagem |
| callback de bind: 1 guarda | L08: bind tardio não inicia descoberta depois de fechar admissões |
| live(Attempt): 1 expressão | L08/L09: DNS/dial/HELLO tardios não promovem sessão após pedido de paragem |
| retry e retryDiscovery: 2 guardas | L08/M06: sem novas tentativas na drenagem, sem mudar política dos nós sobreviventes |
| requestClose: 1 início de relógio | L12/M12/Q-C-10: conservar o início original em vez de renovar o budget após notificação |
| requestClose sem owner: 1 linha | L01/L10: concluir relatório mesmo antes de start, sem abrir recursos ou deixar future pendente |
| ownerStopped: 1 início de relógio | L10/L12: paragem do owner não renova o orçamento, inclusive quando o relógio inicial é zero |
| cleanup: 1 conclusão | L07/L10: requestClose preemptivo, falha fatal, timer rejeitado ou owner parado concluem também o relatório lifecycle |
| close: 1 cálculo de restante | L12/M12: usar o budget originalmente oferecido, limitado pela configuração, após drenagem |

As 14 substituições somam 1+2+2+1+1+2+1+1+1+1+1. Adições incluem a flag
atómica de admissão, estado limitado do relatório no owner e um único ticket de
notificação. O cap do relatório reutiliza o grau máximo 63 existente; valida-o
antes da cópia. Não há scheduler por peer, nova Connection, Transport ou fila
ilimitada. Flush usa Channel.sendAndFlush já existente, sem alterar o transporte.

O stub do checkpoint anterior que lançava UnsupportedOperationException também
foi substituído pelo comportamento agora autorizado; não fazia parte da base
histórica. A emissão de NODE_LEAVE não passa por send() nem altera o seu filtro 7–17.
Q-C-05/Q-C-06, ADR aceites, bytes v1 e caps permanecem. Ver diff e hashes no pacote
de Implement; os 47 testes históricos de vizinhança passaram na regressão de 979.
