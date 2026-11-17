# IMP-011 / TASK-014 — integração local comprovada

Commit main `37fa863ed416017e733e692f9565899fb61285ae`, pai `9c5ad094e58313622a5a695e259cd91b15a71a72`.
Mensagem: `feat(IMP-011/TASK-014): integrar ativacao confirmada e evidencia validada`. Sem push, fecho documental ou DONE.

- [Autorização Test/integração e preflight](authorization-and-preflight.json).
- [Staging conferido contra os568 caminhos/hash](staged-verification.json),
  [diff staged](staged.diff), [numstat](staged.numstat).
- [Commit e extração verificados](commit-verification.json),
  [caminhos integrados](commit-name-status.txt), [numstat do commit](commit.numstat),
  [log do commit](commit.log).
- [V-ADD da cópia extraída: zero erros](v-add-extracted-commit.md),
  [comando/hash/resultado](v-add-result.json).

568 caminhos =555 adicionados+13 modificados. Todos os567 hashes de payload e
o hash do próprio manifesto coincidem com o aprovado. As232 fontes e os oito
caminhos Java revistos por Waheed continuam iguais a Test; JAR SHA-256
`f923f5b766f4ddcc3871c6339710a4a0e87a609aa6cb5ce1e6620c73ccc55314` igual ao manual humano Francisco24 e clean verify1328/0/0/0.
Não houve necessidade de repetir regressão/manual, pois não houve mudança de código.

Q-C-36/37 byte a byte herdadas de HEAD; Q-C-38 exclusivamente a lambda do leitor,
Q-C-39 exclusivamente whitespace das seis linhas. Zero alteração target no commit;
os nove caminhos target já versionados são herdados da base. As duas alterações
locais target preservadas não foram staged nem integradas. Nenhuma alteração
alheia integrada. Índice vazio após commit.

[git diff --cached --check](staged-whitespace-check.log) devolveu2 por166 diagnósticos
exclusivamente nos artefactos brutos de evidência (logs/XML/TXT/diffs/patches e
relatório com linha final vazia); [classificação](staged-whitespace-classification.json).
Esses bytes fazem parte do manifesto aprovado e foram preservados. Não se
normalizaram logs/negativos nem se alterou ou enfraqueceu o validador. V-ADD
na cópia extraída passou sem erros.

O pacote aprovado conserva snapshots de Test/plano anteriores à última
autorização. A aprovação de Test e autorização de integração recebidas depois
estão registadas nesta prova, fora da allowlist imutável. Estes novos artefactos
pós-commit estão locais e não staged; poderão acompanhar o fecho documental
separadamente autorizado. Bolt/história/TASK não foram fechados.

Falta apenas autorização e execução do fecho documental; não iniciar outro IMP.
