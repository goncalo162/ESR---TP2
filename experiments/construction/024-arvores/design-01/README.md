# IMP-014 — Design para revisão

Registo de aprovação Model/Q-C-45/46:2026-11-21T14:21:52Z. Autorizada apenas
a etapa Design. [Design proposto](../../../../memory-bank/bolts/024-arvores/ddd-02-technical-design.md),
[decisões](../../../../docs/construction-duvidas.md) e metadados reconciliados.
Só Model concluído; Design não aprovado, história planned/não implementada.
Totais:15 bolts complete,1 in-progress,19 planned; nenhum outro IMP iniciado.

O Design concretiza identidades M/A e saturação, comparação inteira exacta,
âncora D e revalidação, confirmação atómica seguida de poda capturada,
renegociação dos filhos e coordenação14/11/recuperação. T/A e reservas físicas
partilhados, sem libertação antecipada. Falha efectiva não espera histerese
ou backoff de qualidade. Âmbito futuro continua nos três caminhos do ADD.

`authorization.json` regista a autorização e o SHA-256 de
`approved-model-input.tar.gz`. O arquivo conserva todos os18 documentos/artefactos
identificados pelo manifesto model-01 antes de actualizar estados de aprovação,
mais o próprio manifesto. Os hashes foram conferidos antes da transição.
O directório model-01 permanece intacto; os seus hashes documentais descrevem
essa versão histórica arquivada, não os documentos correntes já actualizados.

`verification.json` e `v-add.md` preservam resultados documentais/âmbito novos;
`document-hashes.sha256` identifica esta entrega, sem substituir hashes antigos.
V-ADD é validação estrutural/documental/fixtures; não é V-017 nem teste Java.

Sem fontes lidas/analisadas/alteradas, Maven, revisão ADR, Implement/Test,
commits ou push. As APIs reais **não foram conferidas nesta etapa**; os pontos
de compatibilidade constam da secção11 do Design para revisão ADR futura,
apenas depois de autorizada. Qualquer impedimento/ampliação será apresentado
antes de alterar código. Testes/asserções/prazos históricos preservados.

V-017/S01–S18 **não executados**. Fecho exige testes/regressão JDK21, manual
humano **antes de aprovar Test**, revisão final **Waheed** e integração.
Sem gate externo adicional, fecho ou início de IMP-015.

R17 inicial de IMP-013 continua **sem causa estabelecida**: integração com
métricas controladas não explica o negativo histórico. Logs, negativos,
sucessos, manual, JAR e hashes anteriores são preservados.
