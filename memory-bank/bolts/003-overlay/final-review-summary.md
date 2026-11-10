# IMP-004 / TASK-007 — resumo para revisão humana final

O manager obtém a lista inicial de vizinhos, confirma uma conexão por par permitido
e reconecta diretamente após perda TCP, mesmo com bootstrap encerrado. Sessões
antigas não autorizam comandos sobre uma conexão/instância nova. Endpoints só são
publicados após handshake; ERROR ACTIVE chega intacto ao consumidor responsável.

**Test foi aprovado explicitamente pelo utilizador.** Clean verify de 903 testes,
manual humano de Francisco e V-ADD aprovados; Q-C-08 resolvida. A revisão final de Denji também foi aprovada, conforme confirmação explícita
do utilizador; a integração continua pendente. O bolt permanece
`in-progress`, com cinco etapas concluídas, `current_stage=null` e `completed=null`.
TASK-007/história/unidade continuam em progresso, sem `implemented=true` ou DONE.

## Âmbito e alterações

| Grupo | Ficheiros | Delta |
|---|---|---|
| IMP-004 | Neighbor.java, NeighborManager.java, NeighborManagerTest.java | +1745/−0 |
| Externo ao IMP | BootstrapServer.java: legibilidade anterior + reposição autorizada de EventLoop/RealDeadlines | +482/−142 contra a base aprovada |
| Q-C-08 | bolt-plan.md, deploy.md e template ddd-construction-bolt.md | +5/−3 contra o conteúdo local anterior à correção |

Os três ficheiros do IMP introduzem records/snapshots imutáveis, descoberta,
allowlist, handshake com confirmação de escrita, iniciador pelo menor nodeId,
unicidade, backoff/jitter, correlação por geração/connectionId/instanceId,
retenção/filas/trabalho físico limitados e shutdown com prazo. O dono serial pode
ser próprio ou emprestado. As dobras para relógio, transporte, resolução e agenda
ficam aninhadas; não houve dependência runtime, formato wire ou hard cap novo.
Q-C-05/Q-C-06 e D01–D22 foram preservados. Não se implementou CLI, routing ou UDP.

A reposição externa foi autorizada após detetar que a alteração de legibilidade
removera classes ainda usadas. A comparação estática de 102 declarações
normalizadas e os 19 testes de bootstrap suportam equivalência com a base após
reposição; não se afirma que o estado externo incompleto era equivalente.

Q-C-08 corrige dois fences exteriores e uma ligação fictícia, mantendo o caminho
como exemplo e ligando ao template ADR existente. O patch autorizado foi
aplicado exatamente; alterações locais preexistentes e o validador foram preservados.

## Evidência revista e aprovada em Test

- [Clean verify](../../../experiments/construction/003-overlay/test-clean-verify-20261110T161706Z.log):
  903 testes, zero falhas/erros/ignorados: 740 configuração, 66 codec, 31 transporte,
  19 bootstrap e 47 vizinhança. [XML/JAR](../../../experiments/construction/003-overlay/test-artifacts-20261110T161706Z/).
- [Manual do agente](../../../experiments/construction/003-overlay/manual-codex-20261110T161706Z/manual.log):
  49 checks e processo Java 0, separado do manual humano.
- [Manual humano Francisco](../../../experiments/construction/003-overlay/manual-Francisco-execucao-01/manual.log):
  49 CHECK_PASS, MANUAL_PASS CHECKS=49, CLEANUP_COMPLETE; JAVA_EXIT=0 confirmado
  explicitamente pelo utilizador. Esse marcador não está no log do driver.
  [Conferência/hash/configurações](../../../experiments/construction/003-overlay/manual-Francisco-evidence.json).
- [V-ADD após Q-C-08](../../../experiments/construction/003-overlay/q-c-08-v-add-final.log):
  saída 0 e zero erros, com o validador original. A atualização documental desta
  aprovação também tem [log próprio](../../../experiments/construction/003-overlay/final-review-v-add.log).
- [Relatório Test](ddd-03-test-report.md): V-007/D01–D22, limites, proveniência e
  aprovação de Test; [registo da aprovação](../../../experiments/construction/003-overlay/test-approval.json).

JAR SHA-256 usado pelos manuais e preservado com os XML:

```text
1d56888b8d3db1ca8d12d45621e14783c45bb7f0b7c8b4a0d0945deb77644d17
```

V-007 inclui seis ordens e arranque simultâneo de três nós, desconhecido rejeitado,
reinício com instância nova e reconexão com bootstrap parado. Fakes controlam as
janelas de flush, callbacks tardios, relógio e limites físicos; TCP real local e
manuais complementam esses casos. Não é enumeração exaustiva das intercalações,
prova de GUI/CORE/vídeo, teste de performance ou demonstração da CLI final.

## Diff para o revisor

Base aprovada do incremento: `26aaa4ae6775e9c78e1d7658472ccaca0b376427`.
HEAD local de entrada: `06dc037578421bbf2a2e26b3ee463793d4f3e587`, ramo `main`.
A versão revista tem alterações locais não commitadas: HEAD sozinho não a identifica.
Os hashes do [índice do pacote](../../../experiments/construction/003-overlay/final-review-index.json)
identificam os ficheiros exatos. Nenhum branch/commit/push foi criado nesta autorização.

1. [Fontes contra a base aprovada](../../../experiments/construction/003-overlay/final-review-source.diff)
   e [numstat](../../../experiments/construction/003-overlay/final-review-source.diff.numstat):
   quatro ficheiros, com bootstrap externo identificado separadamente.
2. [Patch mínimo Q-C-08 aplicado](../../../experiments/construction/003-overlay/final-review-q-c-08.diff)
   e [numstat](../../../experiments/construction/003-overlay/final-review-q-c-08.diff.numstat):
   contra o conteúdo local congelado antes da correção, incluindo as alterações
   locais que já existiam. Não representa a importação completa de ferramentas.
3. [Documentação contra a base aprovada](../../../experiments/construction/003-overlay/final-review-documents.diff)
   e [numstat](../../../experiments/construction/003-overlay/final-review-documents.diff.numstat):
   inclui os três novos documentos de Implement/Test/resumo ainda não tracked,
   sem usar git add. Os dados brutos de execução permanecem nos ficheiros de evidência,
   identificados no índice; não se apresentam como parte de um commit já preparado.

As alterações preexistentes de `.gitignore`, configurações de ferramentas e outros
ficheiros fora destes grupos foram preservadas e não são atribuídas ao IMP-004.

## Pontos da revisão e condições de fecho

Rever a autorização de sessões e comparação de identidades; confirmação de escrita
antes de Up; entrega de ERROR e ordem Received/Down no EOF; classificação antes do
resultado nativo do dial; contagem de recursos físicos; owner emprestado e budget
global de shutdown. A limitação de DNS nativo que ignora interrupção é explícita:
o serviço conserva a reserva e reporta timeout, sem afirmar terminação inexistente.

Autor do código: agente Codex. Operador do manual humano: Francisco. Revisor final
atribuído: Denji, aprovado por confirmação do utilizador; não confundir aprovação do manual/Test com essa
revisão. A integração da versão revista na base comum continua pendente.
Não há gates externos adicionais. Falta **integração autorizada e comprovada**
para poder tratar DONE. Não executar cascata de fecho nem fazer commits/push nesta autorização.

## Confirmação posterior — revisão final aprovada

O utilizador confirmou explicitamente que Denji realizou e aprovou a revisão
humana final desta versão, sem alterações pendentes. Confirmação registada em
2026-11-10T17:00:44Z; data de execução da revisão não fornecida e não inventada.
Código permanece idêntico ao aprovado em Test; manual humano de Francisco
já cumprido, sem repetição. Q-C-08 resolvida, Test/V-ADD aprovados.
Falta integração autorizada/comprovada para poder tratar DONE. Sem staging,
commits/push, integração ou fecho nesta autorização.

[Confirmação de Denji](../../../experiments/construction/003-overlay/denji-final-review-confirmation.json)
e [plano atualizado](integration-plan.md). As referências anteriores a revisão
pendente são históricas; não representam este estado atual.
