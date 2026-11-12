---
stage: design
bolt: "035-overlay-extra"
revision: "02-direct-repair"
status: "approved"
---
# IMP-035 — âmbito aprovado de 21 caminhos

Os 21 caminhos foram aprovados explicitamente pelo utilizador e atualizados
no [ADD](../../../add.md#imp-035). Esta aprovação substitui o âmbito de oito
caminhos; a proposta anterior de30 não foi aceite.
[Design revisto](ddd-02-technical-design.md) e [ADR-035 Aceite](adr-035-proposal.md).
Na etapa de aprovação de âmbito não houve alterações Java. A autorização posterior
da revisão ADR, registada em 2026-11-11T23:39:43Z, permite só Implement nestes21 caminhos.

## Caminhos já previstos

1. overlay/OverlayRecovery.java — política de isolamento/seleção/retry e snapshots.
2. overlay/NeighborManager.java — REPAIR_HELLO20/ACK na mesma ligação, PairSlot e desempate.
3. overlay/OverlayNode.java — composição no mesmo owner e recoveryStatus imutável.
4. app/OttApplication.java — comando recovery limitado; neighbors/quit/EOF preservados.
5. overlay/OverlayRecoveryTest.java — D01–D20 e M01–M18 de política/reparação.
6. overlay/NeighborManagerTest.java — simultâneos, identidade, sessão única e máscaras3.
7. app/OttApplicationTest.java — CLI/JVM real e isolamento de falhas.
8. experiments/overlay-recovery/ — roteiro de processos/SIGKILL e evidência futura.

## Ampliação aprovada

9. overlay/NeighborDiscovery.java — compor o tick100ms existente com Recovery, sem GRANT/wire novo.
10. overlay/NeighborDiscoveryTest.java — preservar provas de aprendizagem e justificar novo comportamento e2e.
11. protocol/MessageType.java — adicionar apenas20, conservar1/2/4..19 e3 rejeitado.
12. protocol/ControlMessage.java — permitir o Hello existente para20, sem novo payload/records de prova.
13. protocol/MessageCodec.java — mesmo parser/HELLO para20 e capacidades3, sem decoder alternativo.
14. protocol/MessageCodecTest.java —19 tipos e comparação com fixtures independentes.
15. overlay/NeighborManagerLifecycleTest.java — handshake20/perdedores/shutdown e máscaras3.
16. bootstrap/BootstrapServerTest.java —20 continua pedido inválido ao bootstrap; migrar máscaras de inputs.
17. transport/TransportTest.java —20 usa sendAndFlush/FIFO normal; cobrança física e quotas da descoberta mantidas.
18. Projeto/src/test/resources/protocol/ —12 fixtures novas/quatro de máscara migradas; snapshot/UDP intactos.
19. scripts/validate-planning.py — validar conjunto revisto e hashes preservados.
20. Projeto/README.md — roteiro, estado executado e comando recovery quando implementados.
21. Projeto/docs/protocolos.md — documentar contrato aprovado depois da revisão/implementação.

Java segue os pacotes main/test pt/uminho/esr/ott do ADD. Não alterar um ficheiro
só por constar do âmbito: o delta tem de ser exigido pelo contrato/teste.
Se a conferência técnica exigir outro caminho, apresentar a divergência antes
de intervir. Não alterar configuração/POM/dependências ou outro IMP por inferência.

## Retirado da proposta ativa

Não criar RepairMessage/GRANT/INTENT/RESULT/BIND, registry/permissões/provas,
Connection.terminated, dial de retorno ou tipo21..23. Sem alterações a
Connection/Transport, CandidateCache, DiscoveryMessage/DiscoveryCodec ou ao
formato de SNAPSHOT19. Cache128x4/TTL60s, retenção e fonte da QUERY permanecem.
A chamada de Recovery usa o tick/owner existentes; nenhum listener/pool novo.

## Contratos de Design aprovados; revisão ADR pendente

ADR-035 Proposta substitui parte de ADR-019/033/034: qualquer isolado inicia20,
menor originator apenas para saídas concorrentes ainda pendentes, ACTIVE nunca
substituída; admissão unilateral cooperativa; capacidades3 e19 tipos. Os textos
Aceites e o catálogo não são reescritos como se a proposta estivesse aprovada.
G-EXTRA-ADR próprio de IMP-035 ainda depende da aprovação formal da revisão
ADR-035 antes de Implement. O âmbito não requer nova confirmação nesta fase.
