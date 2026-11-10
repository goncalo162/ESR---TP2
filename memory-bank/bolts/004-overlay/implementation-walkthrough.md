---
stage: implement
bolt: 004-overlay
created: "2026-11-10T22:50:50Z"
---
# IMP-006 — Implement para revisão

O Plan foi aprovado pelo utilizador. O roteiro, os perfis e os auxiliares
da demonstração estão preparados. Q-C-14 mantém pendente a execução do
cenário nesta bancada, onde a captura e o corte seletivo exigem permissões
adicionais. [Roteiro do primeiro marco](../../../experiments/milestone-1/README.md).

## Ficheiros preparados

- [x] `experiments/milestone-1/README.md` — preparação, sequência, observações,
  correlação da captura com os logs e limpeza.
- [x] `experiments/milestone-1/profiles/` — configuração dos quatro papéis,
  grafo e catálogo usados pelo JAR de referência.
- [x] `experiments/milestone-1/bench.py` — manifesto, logs de JVM, marcas UTC
  e monotónicas, preflight e corte de um 4-tuple confirmado como sessão própria.
- [x] `experiments/milestone-1/Inspect.java` — valida perfis e descodifica
  exportações binárias por direção com o codec do JAR aprovado.
- [x] `experiments/milestone-1/preflight/` — resultados reais do diagnóstico
  de permissões e das verificações offline.

## Decisões e limites

A janela de observação com BOOT ativo é de 30 segundos. A duração foi
escolhida para observar dois períodos de referência de 15 segundos e detetar
consultas frequentes mesmo com fase desconhecida. É um parâmetro do ensaio,
independente dos retries dos vizinhos. A captura abrange a paragem de BOOT e
a reconexão. A conclusão sobre tráfego aplica-se às janelas efetivamente
capturadas e requer uma captura completa. A janela substitui a justificação
anterior baseada no backoff.

O corte só avança após uma sondagem positiva com um socket descartável, uma
captura iniciada e a identificação do 4-tuple nos dois sentidos. O helper
confirma os PIDs, os instantes de início das JVMs e o socket selecionado.
Um erro de `ss -K` ou um socket antigo ainda presente impede tratar o corte
como bem-sucedido. O roteiro mantém a abordagem aprovada. As ferramentas
Linux e o leitor de captura são pré-requisitos da bancada.

## Evidência real e estado

O JDK 21 validou os quatro perfis. O leitor descodificou as fixtures
REGISTER, REGISTER_OK, HELLO, HELLO_OK e NODE_LEAVE e rejeitou um frame
truncado. Foram verificados também o manifesto, as marcas de cronologia e
a sintaxe dos exemplos Bash.
[Resultados offline](../../../experiments/milestone-1/preflight/offline-checks.json).

No anfitrião, a captura sem privilégios foi recusada e `sudo -n` pediu
autenticação. A seleção de um socket TCP descartável próprio foi inequívoca.
O kernel recusou o corte e o socket continuou a transmitir, apesar de
`ss -K` devolver saída 0. [Diagnóstico](../../../experiments/milestone-1/preflight/permissions-host.json) ·
[Q-C-14](../../../docs/construction-duvidas.md#q-c-14--permissões-da-bancada-linux-para-v-009).

A execução técnica da overlay, a captura do cenário e V-009 estão pendentes.
Test requer a repetição por outro elemento com logs e captura identificados.
G-ASSESSMENTS permanece pendente de data e conteúdo oficiais. Para fechar
a TASK-009 é preciso ligar V-009 ao progresso, registar a revisão final
de Fillmore e integrar a versão revista.

## Aprovação de Implement

Registo de 2026-11-10T23:09:30Z. O utilizador aprovou Implement com a verificação
da identidade e do estado do processo `tcpdump` imediatamente antes de
`ss -K`. Os testes com processos descartáveis confirmaram a recusa
quando a captura termina durante a seleção. Os artefactos da revisão
foram arquivados com os hashes de origem.
[Diff](../../../experiments/construction/004-overlay/capture-guard-review/capture-guard.diff) ·
[Resultados](../../../experiments/construction/004-overlay/capture-guard-review/results.log) ·
[Manifesto](../../../experiments/construction/004-overlay/capture-guard-review/archive.json).
O ensaio técnico e a repetição são acompanhados no [registo de Test](test-walkthrough.md).
