# IMP-011 / TASK-014 — plano de integração local, para aprovação

Test reapresentado, ainda não aprovado. Manual humano Francisco e revisão final
Waheed cumpridos por confirmação explícita do utilizador, sem alterações pendentes.
[Prova e hashes da versão](manual-and-review-verification.json).

Base a conferir antes de qualquer ação: main/9c5ad094e58313622a5a695e259cd91b15a71a72.
A implementação original já está em HEAD; este pacote integra a correção M07,
as exceções adicionais autorizadas e os registos/evidências ainda locais.

## Conteúdo e separação

- IMP-011/M07: RoutingService.java (+3) e ActivationTest.java (+66); demais
  caminhos de ativação já em HEAD, hashes vinculados à revisão.
- Q-C-36/37: patches de monitor/handshake já em HEAD, byte a byte preservados;
  não se reaplicam nem se amplia a autorização.
- Q-C-38: exclusivamente lambda this::read → () -> read(input) em OttApplication.
- Q-C-39: exclusivamente espaços finais das seis linhas de CODESTYLE.md.
- Documentação de aprovação/progresso/Test e todas as evidências locais deste
  bolt: negativos, XML/logs, JAR congelado, ferramentas, manual do agente e
  manual humano; confirmações/hashes de revisão e prova desta simulação.

A [allowlist exata de568 ficheiros](allowlist.txt) e o [manifesto de hashes](manifest.json)
estão nesta pasta. Nenhum caminho de
target ou alteração alheia é elegível. Os classfiles de auxiliares guardados
nas execuções de evidência não são Projeto/target nem código de produção.

## Passos após autorização explícita

1. Conferir ramo/HEAD, índice vazio, caminhos/hash de cada ficheiro do manifesto
   e ausência de mudanças no código/JAR revistos; parar perante divergência.
2. Staging explícito exclusivamente dos caminhos da allowlist; conferir staged
   diff/numstat, conteúdo e hashes, garantindo ausência de target/alterações
   alheias e Q-C-38/39 exatamente limitadas aos patches autorizados.
3. Criar apenas commit local de integração em main, com mensagem proposta
   `feat(IMP-011/TASK-014): integrar ativacao confirmada e evidencia validada`.
4. Extrair o commit para cópia independente e conferir conteúdo/hashes, JAR e
   V-ADD sobre essa cópia. Apresentar hash/prova antes de qualquer DONE.
5. Fecho documental só em autorização posterior, mantendo unidade/PHASE em
   progresso enquanto existir trabalho pendente. Sem push ou outro IMP.

A integração não requer repetir testes/manual enquanto o código/JAR mantiverem
os hashes aprovados; qualquer mudança material exige nova validação pertinente.
Não há gates externos adicionais. Nada deste plano foi staged/committed.

## Verificação preparada

Manual/revisão vinculados ao mesmo JAR SHA-256
`f923f5b766f4ddcc3871c6339710a4a0e87a609aa6cb5ce1e6620c73ccc55314`
e aos hashes dos oito caminhos Java. Todos os232 ficheiros src permanecem iguais
a Test; nenhuma regressão/manual repetida nesta confirmação documental.

V-ADD da árvore local e da cópia de HEAD+allowlist passou com zero erros;
[relatório da simulação](v-add-simulation.md), [proveniência](simulation.json) e
[comparação exata](simulation-exact.json). A cópia foi feita por git archive,
sem usar o índice. O primeiro lançamento parou antes do validador por argumento
Python de extração não suportado; nota preservada, sem mudar o validador.

Há nove caminhos de target já versionados em HEAD: herdados da base, não
removidos por esta autorização. As duas alterações locais de target ficam fora
da allowlist e de qualquer staging/commit proposto. Nenhuma alteração alheia
foi identificada. Os resultados desta preparação não aprovam Test/integração.

Pedido de decisão: aprovar Test com manual/revisão agora cumpridos e autorizar,
se pretendido, o staging explícito dos568 caminhos e commit local de integração
segundo estes passos. Sem push; DONE/fecho documental ficam para autorização
posterior à prova do commit integrado.
