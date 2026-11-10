# IMP-005 — matriz de oráculos no checkpoint Implement

Todos os casos técnicos abaixo foram executados na regressão de Implement
981/zero falhas/erros/ignorados. Os 32 testes lifecycle e 46 CLI são novos;
os restantes 903 são regressão histórica intacta. XML em
[implement-xml](../../../experiments/construction/020-overlay/terminal-fix-xml/).
M18 e a consolidação formal de Test permanecem pendentes, sem aprovação inferida.

## Lifecycle — NeighborManagerLifecycleTest

| ID | Prova executada |
|---|---|
| L01 | reason/duração/null/overflow inválidos sem factories; budget zero e fecho antes de start, sem reviver |
| L02 | aviso só para sessão direta confirmada, sem bootstrap/HELLO incompleto; envelope v1 e instância reais |
| L03 | send(Leave) continua INVALID_MESSAGE sem fechar sessão saudável; envio histórico regressado |
| L04 | future pendente até confirmação local write+flush; não é ACK |
| L05 | future de escrita falhado, canal fechado e overflow síncrono fecham sem dial novo |
| L06 | prazo de metade do budget vence flush bloqueado, fecha e ignora confirmação tardia |
| L07 | chamadas repetidas/concorrentes mantêm future/razão/prazo e um aviso; requestClose preempta |
| L08 | comandos novos/na fila não enviam; retry e dial tardio não ressuscitam serviço |
| L09 | callbacks de sessão antiga não notificam/apagam substituição; connectionId e instanceId preservados |
| L10 | owner rejeita/para antes de executar, timer rejeita e consumidor falha: future e recursos terminam |
| L11 | 63 sessões, um timer, relatório limitado/cópia defensiva; tempo na fila, cap e wrap do relógio |
| L12 | TCP real recebe Leave reason 1 v1 e EOF; reason 0/1 em fake/TCP; borrowed owner e budget não renovado em close |

## Processo — OttApplicationTest

| ID | Prova / estado |
|---|---|
| M01 | parser/config inválidos e ajuda sem factory; JVM inválida não ocupa porto configurado |
| M02 | gramática dos quatro papéis e quatro JVMs reais BOOTSTRAP/SERVER/RELAY/CLIENT |
| M03 | serviço do papel validado, bootstrap separado e nó embutido observado num único owner que termina fisicamente |
| M04 | neighbors durante descoberta/sem bootstrap disponível; READY não depende de todos os peers online |
| M05 | três JVMs overlay convergem com duas sessões cada, neighbors mostra ACTIVE sem duplicados |
| M06 | bootstrap retirado após descoberta; sessão TCP real reconecta com nova instância sem bootstrap |
| M07 | peer sai normalmente ou morre abruptamente; sobreviventes mantêm PID e reconectam ao reinício |
| M08 | ERROR ACTIVE verdadeiro observado na CLI sem matar nó; Diagnostic/falhas locais observáveis |
| M09 | bind ocupado real/erro de factory dão 3 e rollback; perfil inválido dá 2 |
| M10 | EOF/quit/sinal real e interrupção do coordenador terminam; flag de interrupção restaurada após cleanup; L07 cobre corrida/repetição |
| M11 | Leave real de saída reason 0 no peer TCP; L01–L12 cobrem sessões/flush/deadline/preempção |
| M12 | clock falso mede consumo de 1000 ms antes da espera, que recebe apenas 2000 de 3000; L12 não renova budget em close |
| M13 | stdin aberto/bloqueado, entrada que falha, stdout fechado/bloqueado; probe grava joins físicos antes de sair da JVM |
| M14 | linha 4097 rejeitada sem virar comando, log truncado até 4096, saturação real de pipes com saída 3; filas usam caps configurados |
| M15 | close antes de start não revive; owner termina; close no callback serial não espera por si e liberta o porto |
| M16 | catalog/play explicam indisponibilidade; neighbors/quit continuam utilizáveis, sem simular streaming |
| M17 | regressão Maven test de 981; clean verify novo obrigatório em Test, ainda não executado |
| M18 | roteiro humano preparado; manual humano antes de aprovar Test, revisão final Waheed e integração ainda pendentes |

V-008 exercitado com JVMs distintas e sockets reais; fakes não substituíram esses
cenários. Os logs por processo, perfis e exits reais estão no
[índice de subprocessos](../../../experiments/construction/020-overlay/implement-subprocess-results.json).
O status do SO para sinal/kill é registado tal como ocorreu, sem o converter em zero.
A prova de joins usa a execução real da consola e pipes de descritor num processo
com serviço falso controlado; os cenários dos quatro papéis usam serviços reais.
Esta distinção impede apresentar a morte da JVM como prova de cleanup.

Os perfis de execução técnica usam loopback e não demonstram GUI/CORE, vídeo,
medidas de FPS/latência ou aprovação humana. Stage Test não foi iniciado.

## Correção terminal — Implement reapresentado

Registada em 2026-11-10T18:39:44Z. A última mensagem é SHUTDOWN com phase=closing-output,
sem código final. ExecutionResult/status do processo só são definidos depois
do fecho/join do writer; não se escreve novamente depois de fechar a saída.
Dois testes novos reproduzem falha/bloqueio no cleanup, verificam saída 3,
join físico, ausência de anúncio prematuro e orçamento global sem renovação.
Vermelho: 2 falhas; verde: 2 sucessos; regressão atual: 981/zero falhas/erros/ignorados.
[Revisão da correção](terminal-fix-review.md). Test continua não iniciado;
clean verify/manual humano/revisão Waheed/integração pendentes.
