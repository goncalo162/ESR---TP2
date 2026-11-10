# IMP-004 — Implement para revisão

Etapa ADR aprovada explicitamente pelo utilizador e registada no bolt. Implement
está preparado para o checkpoint humano; não está aprovado nem concluído no
frontmatter. Bolt/TASK-007/história/unidade continuam in-progress; completed=null.
Não houve commit, push, integração ou início de outro IMP nesta execução.

## Âmbito e base

Base aprovada: `26aaa4ae6775e9c78e1d7658472ccaca0b376427`. HEAD de entrada:
`06dc037`, ramo real `main`. A alteração externa de BootstrapServer já estava
integrada em HEAD. Modificações locais de ferramentas/.gitignore preexistentes
foram preservadas. Alterações geradas em target/ não fazem parte do código revisto.

| Ficheiro de fonte, contra a base aprovada | + | − | Proveniência |
|---|---:|---:|---|
| overlay/Neighbor.java | 33 | 0 | IMP-004 |
| overlay/NeighborManager.java | 953 | 0 | IMP-004 |
| overlay/NeighborManagerTest.java | 759 | 0 | IMP-004 |
| bootstrap/BootstrapServer.java | 482 | 142 | alteração externa de legibilidade + reposição autorizada separadamente |

O incremento IMP-004 soma +1745/−0 nos três ficheiros. O acumulado Java inclui
BootstrapServer separadamente: +2227/−142. [Diff de fontes completo](../../../experiments/construction/003-overlay/implementation-source.diff)
e [numstat](../../../experiments/construction/003-overlay/implementation-source.diff.numstat).

## Correção externa — Q-C-07

A alteração externa tinha removido EventLoop e RealDeadlines, ainda referenciadas:
[compilação inicial](../../../experiments/construction/003-overlay/implement-entry-compile.log)
falhou com quatro erros. A autorização posterior do utilizador permitiu repor
exclusivamente estas duas classes da base aprovada, preservando a formatação restante.
[Patch da reposição contra HEAD](../../../experiments/construction/003-overlay/bootstrap-authorized-restoration.diff).

A [comparação estática](../../../experiments/construction/003-overlay/bootstrap-restored-ast.json)
com javac encontrou as mesmas 102 declarações normalizadas, ignorando comentários,
espaços, ordem de declarações e delimitadores de blocos. Inspeção dos blocos
adicionais: contêm instruções/loops inalterados, sem nova declaração ou mudança da
ordem de inicialização de campos. Os 19 testes existentes de bootstrap passaram
na regressão final. Esta evidência suporta equivalência de comportamento com a
base aprovada **após a reposição**; o estado externo incompleto anterior não era
uma alteração só de legibilidade. Não se modificou a política bootstrap.

## Implementação

Neighbor e Session são records validados; os snapshots e a lista desejada são
imutáveis. Configuração/instância são verificadas antes de criar recursos.
Não foram alterados POM, loader, codec, enums wire, Transport ou Connection.

NeighborManager fornece descoberta inicial REGISTER, validação integral da lista,
HELLO/HELLO_OK, uma sessão por aresta e reconexão direta pelo menor ID lexical.
Up depende da escrita/flush e da confirmação pertinente; frames de aplicação
retidos só são entregues depois de Up. ERROR segue Q-C-05 por fase, sem resposta
ERROR a ERROR e com envelope/payload/sessão íntegros em Received.

O domínio pertence ao SerialOwner próprio ou emprestado. I/O, DNS, agenda e callbacks
oferecem eventos sem bloquear. São limitados os eventos, hooks, contextos, tokens,
retenção B/R, timers e jobs físicos. DNS do sistema permanece contabilizado se
ignorar interrupção; o timeout de shutdown não é apresentado como terminação física.
A fonte DNS injetável é interna, para testar esse caso sem resolver hosts remotos.

A classificação de canais aguarda a barreira dos dials nativos pendentes, sem
inferir direção pelo IP ou pelo primeiro callback. Não se cancela o future nativo
para retirar individualmente uma tentativa; handles tardios são fechados. Prazos
usam relógio monotónico, subtração modular e Q-C-06 para resolução/bind. Shutdown
solicita fecho físico e aplica um budget restante aos awaits; o owner emprestado
permanece com o chamador.

Durante os ensaios TCP, um timeout de reinício expôs uma corrida com o EOF esperado
do bootstrap. Corrigido o descarte de uma resposta completa já capturada quando o
socket físico fecha antes do processamento no owner. O teste determinístico de
REGISTER_OK→EOF cobre a corrida. Pela mesma ordenação, um ERROR ACTIVE capturado
antes de Closed chega ao consumidor antes de Down. Não se autoriza Up num canal
fechado. Todos os logs intermédios, incluindo falhas, foram preservados.

## Testes e evidência reais

Operador: agente Codex. Java Temurin 21.0.13 e Maven 3.9.9. JUnit em test scope,
sem dependência runtime, linter, CI, JaCoCo ou limiar de cobertura novos.

- [Red inicial](../../../experiments/construction/003-overlay/implement-red.log):
  três testes, duas falhas de asserção por validação da instância/fecho ausentes;
  sem erro de compilação. Esse red prova estes oráculos iniciais, não toda a suite.
- Testes incrementais com owner/relógio/agenda/transporte falsos; os casos TCP e
  os testes anteriores de transporte/bootstrap executaram sockets reais locais.
- Uma execução TCP na sandbox falhou em bind com Operation not permitted; não foi
  contada como prova. A repetição fora da sandbox foi autorizada pela ferramenta.
- [Regressão final `mvn -B -ntp test`](../../../experiments/construction/003-overlay/implement-regression-final.log):
  **903 testes, zero falhas, erros ou ignorados**. Distribuição: configuração 740,
  codec 66, transporte 31, bootstrap 19, vizinhança 47.
- [XML preservados](../../../experiments/construction/003-overlay/implement-regression-xml/)
  e [auditoria de preservação](../../../experiments/construction/003-overlay/implementation-preservation.json):
  151 ficheiros restantes de Projeto/ preservados contra a base; os três ficheiros
  do IMP e a correção externa são as únicas fontes diferentes. Q-C-05/Q-C-06 e o
  Design aprovado, incluindo as linhas D01–D22, permanecem intactos.

A mensagem stderr «Bootstrap control processing failed» no log final pertence ao
teste existente que provoca saturação; a suite BootstrapServerTest termina com
19 testes sem falhas. Não é omitida nem convertida num resultado diferente.

## D01–D22 — ligação aos casos executados e aos gates

IDs/nome de teste têm prefixo dNN em NeighborManagerTest. Uma função JUnit pode
conter vários estímulos; os IDs do desenho não são uma contagem de testes.

| Oráculos aprovados | Prova incremental |
|---|---|
| D01 | instância zero, perfil/ID, endpoint/owner inválidos; factories apenas em start |
| D02 | rejeições permanentes/transitórias, requestId errado, canal estranho, resposta completa antes do EOF |
| D03 | lista defensiva, próprio/duplicados/colisões/instância viva, grau 63/64 e grau superior ao cap efetivo |
| D04 | contador fake de consultas e reconexão TCP após bootstrap encerrado; processo reiniciado descobre novamente |
| D05 | menor ID faz dial; seis ordens de três nós e arranque simultâneo em TCP |
| D06 | host/porta/papel/ID/instância/direção incompatíveis, sem DNS do HELLO |
| D07 | HELLO_OK inválido, ERROR inesperado/correlacionado, rejeição HELLO/HELLO_OK e flush tardio |
| D08 | HELLO_OK antes de Written; retenção até B e Up antes do drain FIFO |
| D09 | ID vivo duplicado não substitui sessão saudável |
| D10 | nova instanceId/conexão; Close/ERROR e disconnect da sessão anterior não apagam a nova |
| D11 | todos os códigos ERROR e relatedType 0/3/17/255 intactos; envio valida identidade/sessão; identidade errada isolada; Received antes de Down no EOF |
| D12 | limite exato, fila, wrap de nanoTime, resolução/bind tardios |
| D13 | jitter nos extremos de 100%, crescimento saturado até Integer.MAX_VALUE, reset após ACTIVE |
| D14 | Open antes/depois de DialResult, HELLO forjado e barreira de outro dial; espera não renova prazo |
| D15 | dial nativo retirado não cancelado, sucesso tardio fechado; DNS bloqueado mantém reserva e impede duplicação |
| D16 | NODE_LEAVE corrente/repetido produz um Down e conserva aresta |
| D17 | B e R globais/overflow; 65 jobs físicos e rejeição do seguinte; timer em execução cancelado conserva permissão |
| D18 | fila saturada sem novo slot para Stop, consumidor que lança exceção, rejeição de agenda/I/O limitada |
| D19 | Stop em resolução/bind, owner externo parado, awaits com tempo restante, timeout explícito e close idempotente; TCP verifica fecho/reutilização |
| D20 | runtime efetivo/defaults/caps menores e owner emprestado continua utilizável por outro componente |
| D21 / V-007 | TCP real local: três nós, ordens, desconhecido rejeitado, reconexão sem bootstrap e reinício com nova instanceId |
| D22 | regressão incremental executada; clean verify/JAR/manifesto e manual reproduzível/humano ficam para Test; revisão final/integração pendentes |

Esta matriz liga os casos críticos às decisões; não alega enumerar todas as
intercalações possíveis de threads. Não há GUI, CORE, vídeo ou JVMs CLI demonstrados.
V-007 tem prova automatizada local; a consolidação/aceitação de Test ainda não ocorreu.

## Checkpoint

Parar para validar Implement. Só Model, Design e ADR constam de stages_completed.
A próxima etapa autorizável é Test, com `mvn -B -ntp clean verify`, log novo,
JAR/hash da versão construída e manual embutível reproduzível para repetição humana.
Nenhum JAR antigo é apresentado como produto desta implementação.
O fecho continua dependente de Test/V-007, revisão final humana e integração.

## Limitação da validação documental — Q-C-08

`python3 scripts/validate-planning.py` foi executado e falhou com três erros
em exemplos de ferramentas preexistentes de `.specsmd/`, não alterados nesta
execução. [Log](../../../experiments/construction/003-overlay/implement-planning-validation.log)
e [Q-C-08](../../../docs/construction-duvidas.md#q-c-08--validação-global-inclui-exemplos-de-ferramentas-preexistentes).
Não se declara V-ADD aprovado; a correção desses exemplos não foi inferida
do âmbito Java autorizado. A regressão de 903 testes é um resultado separado.

## Aprovação posterior

Implement aprovado explicitamente pelo utilizador; registo em 2026-11-10T16:17:06Z.
Autorizada apenas Test. O estado anterior para revisão é histórico.
