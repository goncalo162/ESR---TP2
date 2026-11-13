# IMP-008 / 021-rotas / TASK-011 — fecho autorizado

Registo 2026-11-13T18:09:37Z. Implementação revista integrada em main:
`3b7aa745cac62b71ac8ce7e036656bf7ab21692f`. Prova de192 ficheiros/hashes, árvore
`7788f5229bc8d98fb12a6c78d0b99ab9be684557` e V-ADD0/78 sobre a cópia extraída desse commit.
[Prova estruturada](../../../experiments/construction/021-rotas/integration-proof.json)
e [log V-ADD do commit](../../../experiments/construction/021-rotas/integration-commit-v-add.log).

## Critérios cumpridos

- V-011/D01–D21:35 casos de domínio, custos/desempate conhecidos, quatro streams,
  alternativas e snapshots imutáveis; fonte/epoch/sessão/TTL/limites/overflow.
- Clean verify JDK21:1211/0falhas/0erros/0ignorados,14XML,saída0;
  [resultado](../../../experiments/construction/021-rotas/test-build-01/result.json).
- JAR364704bytes,SHA256366210cf8c7656ad2948ae1c842796632085aaa4f09afcedcd00a49db0a0ecee.
- Manual humano Luis43 CHECK_PASS/MANUAL_PASS/Java0,passed=true,JVM terminada,
  fontes/JAR iguais. [Report original preservado](../../../experiments/construction/021-rotas/test-manual-runs/20261113T174838Z-y23kpe_b/report.json),
  SHA25647b712bb518ddc2fb5f975f8af218fd9b76987cfd174d5a3c238bcc5ea11a9d1.
- Test aprovado e revisão final Luis confirmada, substituindo Waheed. Não se
  inventa revisão de Waheed. [Aceitação](../../../experiments/construction/021-rotas/test-acceptance.json),
  [revisão](../../../experiments/construction/021-rotas/luis-final-review-confirmation.json),
  [autorização de fecho/commits/push](../../../experiments/construction/021-rotas/closure-authorization.json).
- Integração da versão revista comprovada; D22 cumprido. Não se repetiu build/manual
  porque os fontes/JAR/relatório humano no commit são os mesmos validados e revistos.

## Cascata de fecho

Cinco etapas aceites, bolt complete/completed real; história complete/implemented=true;
TASK-011 DONE. Índices, progresso e unidade atualizados. Dez bolts complete,
25 planned, nenhum em execução. Unidade003-rotas e PHASE-03 continuam in-progress.
006-rotas apenas blocks=false por dependências completas; permanece planned,
started/current_stage/completed null, sem iniciar outro IMP.

## Preservação e publicação

Seis caminhos de código +851/−0; nenhum teste anterior enfraquecido ou fonte mudado
após Implement. Raw logs/XML/ZIPs de fontes e manual humano preservados. Atributos
locais da pasta de evidência mantêm os espaços exigidos por patches/logs brutos;
a verificação normal de whitespace continua nos fontes/Markdown/auxiliares.
Tentativas de preparação malsucedidas preservadas, sem ocultar ou normalizar bytes.

Target gerado, backup pré-clean e classes temporárias dos manuais não integram
os commits. JAR validado é evidência explícita fora de target. EXTRA-02 preexistente
permanece no disco e fora da integração: quatro caminhos e dois blocos partilhados.
Sem dependência, wire/cap, transporte, CLI, rede integrada ou vídeo novos por efeito
deste fecho. Sem alegação de GUI/CORE, entrega ou submissão.

Commit de implementação acima já criado; este documento integra o commit de fecho
posterior. O seu hash e resultado do push main só serão apresentados depois da
execução efetiva, sem hash autorreferente ou force push.
