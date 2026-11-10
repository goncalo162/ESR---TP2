# Q-C-08 — correção mínima para aprovação

**Proposta preparada, não aplicada ao repositório.** Test continua por aprovar;
V-ADD não está aprovado e Q-C-08 continua aberta.

## Alterações concretas

1. `.specsmd/aidlc/skills/inception/bolt-plan.md`: na secção 6, mudar o fence
   exterior de `markdown` para quatro backticks e fechá-lo depois dos dois
   blocos interiores `text`. O conteúdo dos diagramas fica intacto.
2. `.specsmd/aidlc/skills/operations/deploy.md`: na secção 6, usar quatro backticks
   no fence exterior do exemplo de histórico e fechá-lo após o bloco interior
   do comando de rollback. Conteúdo e checkpoints ficam intactos.
3. `.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md`:
   manter o caminho fictício JWT como texto de exemplo e substituir o link
   inexistente por um link explicitamente rotulado ADR template para
   `ddd-construction-bolt/adr-template.md`, que já existe. Não criar uma ADR
   fictícia nem converter o exemplo numa decisão do projeto OTT.

Total incremental contra os ficheiros locais atuais: **+5/−3 linhas**.
O patch foi gerado sobre o conteúdo de trabalho, incluindo as alterações locais
preexistentes; não restaura versões de HEAD nem substitui os ficheiros completos.
[Patch exato](q-c-08-proposed.patch) · [numstat](q-c-08-proposed.diff.numstat).
Os três inputs foram preservados com hashes e cópias em
[q-c-08-proposal-inputs.json](q-c-08-proposal-inputs.json). Antes da aplicação,
conferir esses hashes; se houver alteração concorrente, reapresentar o patch.

## Ensaio isolado, sem aprovação global inferida

[Resultado real](q-c-08-preview-result.json): o mesmo validador, sem alterações,
reproduziu os três erros numa cópia isolada. Após aplicar apenas o patch nessa
cópia, terminou com saída 0 e zero erros. **3688 ligações/âncoras foram verificadas
antes e depois**, sem reduzir o alcance ou acrescentar exclusões ao validador.
[Antes](q-c-08-preview-before.log) · [Depois](q-c-08-preview-after.log).

O snapshot não copiou `.git`, `.aws` nem saídas `target`; reproduziu exatamente
os três erros conhecidos antes do patch. As árvores verificadas pelo validador
foram mantidas. [Script reproduzível](q-c-08-preview.py). Os ficheiros reais de
ferramentas e `scripts/validate-planning.py` permaneceram intactos.
Este é um ensaio da proposta: não é execução aprovada de V-ADD no repositório.

Após aprovação: conferir hashes, aplicar apenas este patch, executar o validador
original no repositório e guardar um log novo. Só então avaliar a resolução de
Q-C-08; Test continuará dependente da aprovação explícita do utilizador.
Sem fecho, commits ou push. Revisão humana final e integração pendentes.

## Diff proposto

```diff
--- a/.specsmd/aidlc/skills/inception/bolt-plan.md
+++ b/.specsmd/aidlc/skills/inception/bolt-plan.md
@@ -158,7 +158,7 @@
 
 Establish execution order based on dependencies:
 
-```markdown
+````markdown
 ## Bolt Sequence
 
 ```text
@@ -181,6 +181,7 @@
                    [payment-service]
 
 ```
+````
 
 ### 7. Create Bolt Instance Files (CRITICAL)
 
--- a/.specsmd/aidlc/skills/operations/deploy.md
+++ b/.specsmd/aidlc/skills/operations/deploy.md
@@ -92,7 +92,7 @@
 
 Append to `deployment/history.md`:
 
-```markdown
+````markdown
 ---
 version: {version}
 environment: {env}
@@ -118,6 +118,7 @@
 ```text
 deploy --unit="{unit}" --env="{env}" --version="{prev-version}"
 ```
+````
 
 ---
 
--- a/.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md
+++ b/.specsmd/aidlc/templates/construction/bolt-types/ddd-construction-bolt.md
@@ -548,7 +548,7 @@
 ## Relevant Prior Decisions
 
 Found {n} ADR(s) that may apply to this bolt:
-- ADR-001: Use JWT for Authentication → [View](bolts/001-auth-service/adr-001-jwt-auth.md)
+- ADR-001: Use JWT for Authentication → example path: `bolts/001-auth-service/adr-001-jwt-auth.md` ([ADR template](ddd-construction-bolt/adr-template.md))
 
 These decisions may constrain or guide your approach. Proceed? (y/n)
 ```
```
