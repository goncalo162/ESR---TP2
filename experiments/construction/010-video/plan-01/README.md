# IMP-016 / 010-video — Plan formal por aprovar

[Plan](../../../../memory-bank/bolts/010-video/implementation-plan.md),ramo
imp-016-udp,base `b6de8488f6dc99e3bf490d4cb88b31840089cb30`. Main permanece nesse ref,sem commits acrescentados.
Proposta tecnicamente aceite; gravação/preservação e patch de uma linha autorizados.
Plan não concluído,stages_completed=[],Implement/Test não autorizados.

- [Entrada e hashes](entry.json), [preservação](preservation.json), [estado](checkpoint-status.json).
- [Diff acumulado contra a base](cumulative.diff), [numstat](cumulative.numstat).
- [Patch exato IMP-016-LOCAL-02](template-link.patch):1 acréscimo/1 remoção,no template.
- [V-ADD negativo anterior](v-add-before-patch.md), [pós-patch:0 erros](v-add-after-patch.md),
  [V-ADD final](v-add-final.md),sem alterar validador ou reduzir alcance.
- [Auditoria documental de fixtures](fixture-audit.json):16 UDP OTT1,4 expectativas
  positivas/12 negativas,hashes/bin-hex coerentes. Não é execução de V-019/Java.
- [Manifesto do pacote](manifest.json),sem hash de si próprio.

235 ficheiros src e380
ficheiros de demo/apoio preservados por hash. POM,ADD,specs,Q-C globais,fixtures,
validador,target e bloco histórico da demo em progresso15 intactos. DEMO-001 não
é dependência nem bloqueio PHASE-05. Identificadores IMP-016-LOCAL apenas.
Sem Java/sockets/emissão de vídeo/novas dependências,testes Java,staging,commits/push.
Fecho futuro exige V-019,regressão,manual humano,revisão e integração,sem gates externos.
