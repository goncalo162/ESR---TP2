---
stage: adr
bolt: "034-overlay-extra"
created: "2026-11-11T17:22:59Z"
status: "approved"
updated: "2026-11-11T17:47:08Z"
---
# IMP-034 — Revisão pendente de Design/ADR-034

Revisão documental concluída para apresentação; aprovação humana ainda pendente.
Não conclui a etapa ADR nem autoriza retomar Implement.
[ADR-034 Proposta](../../../add.md#adr-034) ·
[Design revisto](ddd-02-technical-design.md) ·
[Interfaces](../../../docs/07-API-AND-INTERFACES.md#extra-01-control-v2) ·
[Q-C-18](../../../docs/construction-duvidas.md#q-c-18--descoberta-no-canal-de-controlo).

O utilizador fixa um único programa e a mesma versão do JAR em todos os nós.
SERVER/RELAY/CLIENT exigem descoberta; não desenvolver compatibilidade operacional
com versões antigas, modo opcional ou fallback. A validação de versão,
identidade, endpoint e capacidade permanece no handshake. Inputs incompatíveis
são negativos de protocolo, não outra modalidade de execução.

| Decisão | Conformidade proposta / prova pendente |
|---|---|
| ADR-007 e contrato incorporado nas ADR-009/012 | Um codec/contrato TCP v2, 18 tipos explícitos, 3 rejeitado; arquivo v1, fixtures v2 independentes. Framing/caps/semântica mantidos; deixa de haver runtime TCP v1. D01e/D07/D11. |
| ADR-010/016 | Configuração obrigatória em todo overlay; ausência exige, true redundante, false/Q=1 agregados offline; genérico/bootstrap Q=1 conservado. D01a–d/D08a. |
| ADR-013 | Bootstrap só consulta inicial v2; sem catálogo vivo, aprendizagem ou reparação no bootstrap. D03/D04/D09. |
| ADR-014/015 | TCP persistente/FIFO/writer únicos, margem M=1, quatro tickets físicos, reservas até terminação real e conclusões fora dos locks. D08a–f/D10. |
| ADR-017/018 | Estado/cache no owner serial, sessão/runToken/requestId/connectionId/instanceId correntes; callback velho não altera novo estado. D02c–d/D06/D10. |
| ADR-019 / CP-02/03 | IMP-034 aprende sem admitir arestas fora do grafo; reparação/admissão permanecem no IMP-035/V-034 e checkpoint próprio. |
| ADR-020 | Rejeição remota afeta só a tentativa, sem NeighborUp, FAILED global, failure(), FATAL, fim da CLI ou queda das sessões saudáveis. Peer state limitado, backoff existente. D02b–d/D11a. Falha local fatal real conserva tratamento original. |
| ADR-024/026 | Mesmo IMP, âmbito proposto de 29 caminhos incluindo bootstrap/testes/recursos, sem runtime novo/CI/linter. Migração de pressupostos dos testes justificada, preservando garantias de framing/limites/FIFO/flush/identidade/shutdown durante a mudança de bytes TCP. V-ADD é documental; execução Java ainda pendente. |
| ADR-033 residual | Cache 128×4, TTL local 60 s, proveniência, gerações, retenção após morte e callbacks conservados. ADR-034 substitui transporte/ativação opcional e migra todo o contrato TCP para v2, sem rever as políticas de cache. |

A proposta anterior de mensagens normais/bootstrap v1 com descoberta v2 foi
substituída pela decisão explícita de TCP v2 uniforme. Remover ControlFrame e
políticas runtime por versão; conservar ControlMessage e MessageCodec únicos.
Incluir BootstrapServer/BootstrapServerTest, recursos/README de protocolo,
documentação local e validador no âmbito. D01e/D11b preservam a consulta imutável,
instâncias zero, flush antes de EOF, limites e shutdown; não mudam a ADR-013.
UDP OTT1 continua v1. Arquivo v1 e 50 vetores v2 independentes preparados/auditados
offline; nenhuma execução Java. [Migração de testes](test-contract-migration.md)
preserva causas reais dos negativos, códigos, Unicode/limites, identidades,
ordens, unicidade, FIFO/flush/deadlines e terminação física.

O erro da proposta anterior que fazia um input de peer autorizado provocar
FAILED global foi corrigido, em vez de alterar a ADR-020 aceite. Limites e
backoff estão na secção 4.1 do Design: até 63 peers configurados, uma tentativa
e um retry por peer, índice saturado, C<=64/deadlines físicos; não há negociação
de outro programa nem histórico ilimitado. D11a exige peer A ACTIVE e B
rejeitado com CLI e descoberta de A úteis; depois B só pode entrar pelo mesmo
handshake válido obrigatório. A ausência temporária não prova incompatibilidade.

G-EXTRA-ADR continua pendente de aprovação de Design/ADR-034/contratos revistos
e migração proposta dos testes. ADR-034 continua Proposta; Model é a única
etapa concluída no contrato atual. OTD1/código parcial/evidências preservados.
V-033, regressão JDK 21, TCP/CLI reais, manual, revisão e integração continuam
por executar/concluir. Sem commits, push ou início de IMP-035/outro IMP.

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

## Revisão formal concluída e resultado de Implement

Design/ADR-034/29 caminhos/migração foram aprovados explicitamente pelo
utilizador; aprovação registada em 2026-11-11T17:55:50Z. ADR-034 Aceite,
G-EXTRA-ADR satisfeito para IMP-034. As referências anteriores a Proposta,
pendência de gate e proibição de Java neste relatório descrevem a entrada
histórica, substituída por essa aprovação.

Implement segue o contrato v2 único e conserva ADR-013/014/015/017/018/019/020.
Não surgiu necessidade de ADR nova ou substituição adicional: versão TCP/
ativação/transporte foram as mudanças aprovadas; reparação mantém IMP-035.
Ver walkthrough para resultados incrementais e correspondência com as garantias
das fundações; esta revisão não aprova Test/fecho/integração.

## Atualização de fecho — Q-C-19

2026-11-11T19:42:24Z: Luis repetiu o manual V-033 com 53 checks/saída 0 e pediu revisão final, integração e fecho. A revisão técnica está concluída. Por instrução explícita foram retirados os arquivos TCP v1 e a variante abandonada; as referências anteriores a retenção de arquivos são históricas. Negativos de versão TCP v1 e todo UDP OTT1 continuam exigidos. [Estado e limpeza atuais](closure-walkthrough.md).
