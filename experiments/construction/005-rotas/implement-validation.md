# IMP-007 — validação do checkpoint Implement

HEAD/base 3dedf007f52f43f53eaf254bef7c25e35f986e5a, main; índice vazio, nenhum commit/push.
Regressão04 mvn test:1176/0falhas/0erros/0ignorados,exit0;52monitoring+11integração.
V-ADD:exit0 (implement-v-add.log); git diff --check:exit0.

Diff contra base com documentos/testes novos sem staging:
- Cinco caminhos de implementação: +1462/-39.
- Q-C-25, só teste histórico aprovado: +33/-1.
- Java autorizado total6: +1495/-40.
- Acumulado documental/fontes: 23 ficheiros,+3325/-61.
Exclui experiments/ (inventário separado) e target/ (outputs gerados).
Git numstat bruto conservado separadamente: não inclui untracked e inclui outputs
tracked target; por isso não é confundido com o diff de entrega.

SHA256: 228 ficheiros tracked protegidos de Projeto fora de target/
e allowlist preservados desde Model/base. Fora da allowlist documental/Java e
target, todos os hashes da entrada Implement preservados, incluindo evidência
anterior. Model/Design/ADR byte a byte intactos. Prefixo de construction-duvidas
anterior a Q-C-25 tem SHA256 igual à entrada: Q-C-23/24 e questões antigas intactas.
AppTest mudou exclusivamente pela cadeia autorizada de patches com hashes antes/
depois e patch SHA256. Nenhum outro teste histórico alterado; NeighborManager/send,
wire/caps/config/transportes/fixtures intactos. Fechos e unidade002 intactos.

Sem clean verify, JAR novo, manual humano ou revisão final do código nesta etapa.
Fillmore reviu Design. M22 e fecho pendentes; Implement para aprovação, Test não
iniciado. XML/TXT completos, log/profil/exit CLI e ambiente em experiments/.
