---
stage: test
bolt: 004-overlay
created: "2026-11-10T23:09:30Z"
---
# IMP-006 — Test aprovado

Implement foi aprovado e os resultados da correção da guarda de captura
foram preservados. Em 2026-11-10T23:25:25Z, o utilizador aprovou o fecho de Test
com a evidência do ensaio técnico validado e dispensou a repetição por outro
elemento. A decisão limita-se a este checkpoint.
[Roteiro](../../../experiments/milestone-1/README.md) ·
[Aprovação de Implement](implementation-walkthrough.md).

## Sondagens de permissões

| Operador | Sondagem | Resultado |
|---|---|---|
| Codex | [Sondagem inicial](../../../experiments/milestone-1/runs/20261110T230236Z-1oy34xz9/permission-probe.json) | Captura e corte recusados por falta de autenticação no terminal |
| Luis | [Sondagem autorizada](../../../experiments/milestone-1/runs/20261110T230622Z-pax81tgv/permission-probe.json) | Captura e corte passaram. Um pacote no PCAP e o socket selecionado desapareceu |

Q-C-14 está resolvida para a bancada autenticada de Luis. O terminal do
agente continua a exigir autenticação própria. As sondagens usaram sockets
descartáveis e pertencem ao preflight de V-009.

## Execuções de V-009

| Execução | Operador | Evidência e resultado |
|---|---|---|
| Tentativa técnica inicial | Codex, com lançamento no terminal autorizado de Luis | INCOMPLETE. O controlador esgotou a espera pelo fecho da captura. [Resultado](../../../experiments/milestone-1/runs/20261110T230909Z-iaa88lnv/analysis/result.json) · [Log da captura](../../../experiments/milestone-1/runs/20261110T230909Z-iaa88lnv/capture.log) |
| Ensaio técnico validado | Codex, com lançamento no terminal autorizado de Luis | PASS. [Resultado](../../../experiments/milestone-1/runs/20261110T231137Z-9917llvm/analysis/result.json) · [Captura](../../../experiments/milestone-1/runs/20261110T231137Z-9917llvm/control.pcap) · [Ações e logs](../../../experiments/milestone-1/runs/20261110T231137Z-9917llvm/technical-actions.jsonl) |
| Repetição independente | — | Dispensada pelo utilizador em Q-C-15 |

A primeira tentativa produziu 112 pacotes legíveis. As estatísticas chegaram
depois de expirar a espera do controlador. A tentativa conserva o resultado
INCOMPLETE. O controlador de Test passou a sinalizar diretamente o processo
`tcpdump` identificado com pidfd e a aguardar o fecho. Os auxiliares
aprovados em Implement conservam os hashes arquivados.

O ensaio validado registou 89 pacotes no PCAP, saída 0 da captura e zero perdas
reportadas pelo kernel. O contador do filtro foi 224. Os oráculos de controlo
usam as ligações e os frames reconstituídos no PCAP, enquanto os estados de
saída e portas livres provêm dos logs e da sondagem final.
[Frames descodificados](../../../experiments/milestone-1/runs/20261110T231137Z-9917llvm/analysis/frames.json).

- Três pares REGISTER/REGISTER_OK correlacionados por requestId. As respostas
  de N1 e N2 incluem N0 enquanto SERVER ainda está ausente.
- Três pares HELLO/HELLO_OK iniciais. Um quarto par na nova ligação N0–N1,
  após a saída de BOOT, conserva as instâncias. Os logs confirmam os mesmos
  processos e novos connectionIds apenas nessa aresta.
- Dois NODE_LEAVE de N2 com motivo 0 e `NeighborDown` por LEAVE nos
  sobreviventes. As quatro JVM saíram com código 0 e as sete portas ficaram
  disponíveis.
- Zero pacotes para a porta de BOOT nos 30,030 segundos observados com BOOT
  ativo e desde a saída de BOOT até à marca de reconexão. A conclusão
  aplica-se a essas duas janelas no PCAP.

O roteiro manual e o controlador estão disponíveis para reprodução. A prova
aceite é a execução técnica validada. A repetição por outra pessoa ficou
dispensada por decisão do utilizador e não consta como executada.
[Q-C-15](../../../docs/construction-duvidas.md#q-c-15--evidência-aceite-no-fecho-de-v-009).
[Manifesto da evidência](../../../experiments/construction/004-overlay/test-evidence.json).
G-ASSESSMENTS mantém a data e o conteúdo da avaliação pendentes de
informação docente.

[Fecho com revisão e integração](closure-walkthrough.md).
