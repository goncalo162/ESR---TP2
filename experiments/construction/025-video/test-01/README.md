# IMP-018 / 025-video — Evidência Test

Implement aprovado; Test em curso, não submetido para aprovação.
[Relatório](../../../../memory-bank/bolts/025-video/ddd-03-test-report.md).

- [Clean verify](clean-verify/result.json): JDK21.0.13.1,1582/0/0/0,23 XML,
  saída0,43.624s.1537 históricos+45 novos, sem alterar código aprovado.
- [JAR](clean-verify/ott-streaming-1.0-SNAPSHOT.jar), SHA-256
  `f9a3cb9cad6166d13e244a772398179731952fd8eb8ff0efe7b16a6f67b9787f`; [checksum](clean-verify/ott-streaming-1.0-SNAPSHOT.jar.sha256).
- [Consolidação M01–M14/V-020-B](consolidation.json): oráculos locais passam;
  prova dos quatro conteúdos completos pendente, não substituída por fixtures.
- [Media](media-availability.json): quatro ponteiros e nenhum objeto local;
  nenhum download/materialização. Hash/count/EOF/cleanup integrais não executados.
- [Manual de sete etapas](../manual-instructions.md): preparado, humano não
  executado. Ensaio atual: saída2/pending_full_media. Negativo inicial do
  lançador JShell e controlo de classpath preservados nas runs separadas.
- [V-ADD](v-add-after-patch.md):0 erros após o patch de3 paths autorizado,
  [aplicação exata](v-add-proposal/application.json). Validador e demo preservados.

[Entrada/hashes](entry-state.json), [inspeção do JAR](jar-inspection.json) e
[metadados gerados preservados](generated-build-metadata.json). Os2 registos
compilados de target alterados pelo build foram repostos à entrada, com as
versões geradas guardadas; não se repôs código ou mexeu no index.

16MiB apenas raster destino. R17 inicial permanece com causa desconhecida;
a integração com métricas controladas não a demonstra. Sem streaming integrado,
pacing/loop/emissão/player, fecho, commits/push ou IMP-019. Manual aplicável,
revisão Denji e integração mantêm-se obrigatórios; TASK-020 só apósA/B/C.
