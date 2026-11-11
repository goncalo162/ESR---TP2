# IMP-034 — revisão para aprovação: TCP uniforme v2

Registo de 2026-11-11T17:47:08Z. [Design](ddd-02-technical-design.md), [ADR-034 Proposta](../../../add.md#adr-034),
[revisão ADR](adr-034-review.md), [interfaces](../../../docs/07-API-AND-INTERFACES.md#extra-01-control-v2)
e [migração dos testes](test-contract-migration.md) atualizados pela decisão do utilizador.

Um programa/versão de JAR em todos os nós; todo o TCP v2, incluindo bootstrap,
handshake, mensagens normais e descoberta. Um MessageCodec/ControlMessage,
18 tipos explícitos, 3 rejeitado; sem runtime v1, fallback ou modo opcional.
Bootstrap só consulta inicial, resposta completa antes de EOF, sem membership;
UDP OTT1/v1 de vídeo e as suas 16 fixtures permanecem inalterados.

FIFO/framing/caps/flush/identidade/unicidade/shutdown conservados. Descoberta
obrigatória/Q>=2 offline, cache 128x4, TTL local 60 s, proveniência/gerações e
retenção após morte preservadas. Rejeição remota é local, sem FAILED global ou
fecho da CLI/sessões saudáveis; D11a e novo D11b cobrem isolamento/bootstrap.

## Âmbito e arquivo

Proposta de 29 caminhos: retirar ControlFrame e incluir BootstrapServer,
BootstrapServerTest, recursos de protocolo, Projeto/docs/protocolos.md e
scripts/validate-planning.py. Os fontes de Projeto/ continuam congelados.
IMP-034 só aprende/retém; reparação permanece em IMP-035/V-034, não iniciado.

[Arquivo v1](closure-walkthrough.md#limpeza-autorizada)
com 45 fixtures originais, contrato e inventário de hashes; tar SHA-256
6ef0dfea7984340f62e79951f15f63d5fc3fb6001d8eadc6cb86bc2cfc764437.
O [registo de entrada](closure-walkthrough.md#limpeza-autorizada)
preserva documentos anteriores, 163 hashes de Projeto/ e 126 evidências anteriores.
As aprovações OTD1/execuções parciais e revisões mistas continuam históricas,
sem reclassificar falhas ou resultados como validação v2.

## Evidência e limites desta revisão

[50 fixtures TCP v2 propostas](../../../experiments/construction/034-overlay-extra/protocol-v2-proposal/README.md),
com 22 aceites, 23 rejeições e cinco rejeições de conteúdo condicionais à sessão.
Gerador por campos literais e auditor Python separado, sem codec Java nem
alteração de byte de versão de v1. [Auditoria offline](../../../experiments/construction/034-overlay-extra/protocol-v2-proposal/audit.json)
confere os 18 tipos e os campos/negativos. Não é execução de Java, TCP ou V-033.

[V-ADD desta revisão](closure-walkthrough.md#limpeza-autorizada),
[preservação/estado](closure-walkthrough.md#limpeza-autorizada)
e [diff contra a entrada](closure-walkthrough.md#limpeza-autorizada)
distinguem esta mudança dos diffs anteriores. V-ADD ainda verifica 45 recursos
originais congelados; os 50 propostos são auditados separadamente.

ADR-034 continua Proposta; G-EXTRA-ADR pendente de aprovação de Design/ADR/contratos/
âmbito e migração de testes. Não continuar Implement/Test/fecho. Regressão JDK 21,
TCP/CLI reais, manual, revisão final e integração continuam por executar/concluir.
Sem commits/push ou outro IMP.
