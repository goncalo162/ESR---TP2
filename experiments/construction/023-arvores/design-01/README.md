# IMP-012 / 023-arvores — checkpoint Design

[Design para aprovação](../../../../memory-bank/bolts/023-arvores/ddd-02-technical-design.md).
Model aprovado, Q-C-40–42 reconfirmadas pelo utilizador; apenas Design autorizado.
Base main/b273b258a4642f94f8614ae00f060a46318d77a5; alterações locais de Model
preservadas, sem checkout/reset/limpeza. Nenhuma alteração Java/teste Java.

[Entrada/hashes](entry.json), [preservação](preservation.json),
[V-ADD inicial: zero erros](v-add-initial.md). Relatório final, diff/numstat
contra a base e manifesto dos artefactos guardados nesta pasta.

Propostas concretas: poda captura maior revisão submetida da geração retirada
e exige match exato remoto, com lease como fallback; ACK/ACCEPTED antigos não
alteram substituta. Período P=min(renew,G/3) constante por grant, prazo cortado
pelo restante, uma operação pendente e ausência de catch-up. Recursos C/D/L/H/B/J
partilhados, duas fases reais de conclusão física e shutdown único.

Allowlist Java de três caminhos; nenhuma API/wire pública, CLI, transporte ou
dependência novos. Mapa L01–L24/V-015 por executar, não contagem de testes.
Grants/quotas podem impedir continuidade: expiração segura, sem metas inventadas.

Parar antes de ADR/Implement; Design por aprovar. Sem staging/commits/push/outro
IMP. Fecho futuro depende de V-015/regressão atual/manual humano/revisão Denji/
integração, sem gates externos adicionais.

[Diff cumulativo Model+Design contra a base](cumulative.diff),
[numstat](cumulative.numstat), [resumo do diff](diff-summary.json),
[V-ADD final](v-add-final.md). A evidência de Model anterior não foi sobrescrita.
