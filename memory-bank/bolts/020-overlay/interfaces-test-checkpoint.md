# IMP-005 — checkpoint de interfaces/tipos e testes inicialmente vermelhos

✅ Plan aprovado pelo utilizador. ⏳ Implement limitado a este primeiro passo;
comportamentos não implementados. [ ] Test e fecho não iniciados.

## Decisões e base

Q-C-10/11/12 registam decisões aprovadas de reserva de metade do budget,
gramática CLI e buffers locais, sem as atribuir ao enunciado. Q-C-09 continua
resolvida quanto ao âmbito autorizado, com prova técnica pendente.
Base operacional/HEAD preservado: 90cded6b8d321cfcbe369dd4f8dbed9a01ddf430.
Referência histórica: 2ea748efd61ba5bdea355d2b5a02de6f6bd9a8c5.

## Interfaces introduzidas

1 - **NeighborManager**: requestShutdown(int, Duration), LeaveOutcome,
    LeaveAttempt e ShutdownReport. Records com cópia defensiva/null checks.
    Novo método lança UnsupportedOperationException de comportamento pendente.
    O restante ficheiro é exatamente o anterior, incluindo send() de aplicação.
2 - **OttApplication**: Invocation, constantes dos buffers aprovados, parse,
    run/main e portas internas ServiceFactory/RunningService/relógio injetado.
    Parser, execução e main são stubs explícitos sem serviços/cleanup fictícios.
3 - **OverlayNode**: construtor sem arranque, start/snapshot/diagnostics,
    requestShutdown/awaitTermination/close como contratos ainda sem comportamento.
4 - **Testes novos**: OttApplicationTest e NeighborManagerLifecycleTest.
    Reutilizam fakes históricos de owner/transport/timers sem os editar.
    Relógio falso controla flush, deadline, repetição e admissões; JVMs reais
    verificam os oráculos iniciais help/erro de configuração, ainda vermelhos.

Não existem novos tipos wire ou alterações de framing/caps/política de send.
Não há implementação da drenagem, composição, parser ou I/O neste checkpoint.

## Execuções reais

- `mvn -B -ntp -Dtest=NeighborManagerLifecycleTest,OttApplicationTest test`:
  compilação bem-sucedida, exit 1 esperado; 50 testes, 47 falhas, zero erros,
  zero ignorados. Lifecycle: 22/19 falhas/3 sucessos; CLI: 28/28 falhas.
  Todos os XML das falhas contêm UnsupportedOperationException dos stubs;
  subprocessos retornam 1 por esse motivo, em vez dos futuros 0/2.
- `mvn -B -ntp -Dtest=NeighborManagerTest test`: repetição autorizada fora da
  sandbox, exit 0; 47 testes, zero falhas/erros/ignorados. A tentativa na sandbox
  teve dois erros SocketException Operation not permitted; foi preservada.
- `python3 scripts/validate-planning.py`: exit 0, zero erros; validador intacto.
- `git diff --check`: sem erros.

[Resultado por suite/caso](../../../experiments/construction/020-overlay/interfaces-test-results.json),
[log vermelho confirmado](../../../experiments/construction/020-overlay/interfaces-red-confirmed.log),
[regressão histórica de vizinhança](../../../experiments/construction/020-overlay/interfaces-historical-neighbors-unrestricted.log),
[V-ADD](../../../experiments/construction/020-overlay/interfaces-v-add.log) e
[XML](../../../experiments/construction/020-overlay/interfaces-xml/).

As tentativas iniciais de montagem/compilação foram corrigidas antes do vermelho
confirmado: caminho do novo teste CLI e nome PROBE_PING existente. Logs preliminares
preservados não contam como testes vermelhos pelas razões certas. A primeira
validação documental rejeitou a serialização YAML de stages_completed; corrigida
para o formato JSON inline já usado no projeto, sem alterar o validador.

## Cobertura atual e trabalho ainda pendente

Os testes iniciais exercitam reason/budget inválidos, zero/pre-start, sessões
confirmadas versus bootstrap/HELLO, filtro de send, flush/falha/timeout/repetição,
preempção, comandos/retries tardios, sessão substituída, owner/timer rejeitados,
tempo na fila e owner emprestado; grammar/help/config/EOF/quit/erro local e limites.
Esta execução não demonstra esses comportamentos: os testes novos estão vermelhos.

Ainda faltam ampliar/provar os oráculos de grau máximo/caps e todas as variantes
L01–L12, teste TCP real do novo Leave, concorrência/overflows, bootstrap/owner
compostos e todos os papéis, pipes bloqueados/falhados e orçamento de I/O.
V-008 completo com bootstrap e múltiplos nós em subprocessos, M01–M18, regressão
completa atual/clean verify, manual humano antes de aprovar Test, revisão final
de Waheed e integração permanecem obrigatórios. Os 903 testes do IMP-004 são
históricos; os 47 atuais de vizinhança não são regressão completa.
Sem gates externos adicionais. Não afirmar GUI/CORE, JAR novo ou manual executado.

## Diff, preservação e checkpoint

[Diff acumulado contra 90cded6](../../../experiments/construction/020-overlay/interfaces-vs-operational-base.diff),
[numstat acumulado](../../../experiments/construction/020-overlay/interfaces-vs-operational-base.numstat),
[numstat só dos cinco ficheiros de código](../../../experiments/construction/020-overlay/interfaces-code.numstat)
e [auditoria](../../../experiments/construction/020-overlay/interfaces-preservation.json).
Código: +449/−0 nos cinco ficheiros. Testes/fixtures históricos, ferramentas,
fecho de IMP-004 e listas tracked de target preservados; listas de compilação
alteradas pela execução foram repostas aos bytes conferidos da entrada.
XML copiados para evidência fora de target. Sem staging, commits ou push.

**Parar aqui para validação do checkpoint antes de implementar comportamentos.**
Não marcar Implement completo, história implemented=true, TASK-008 DONE ou bolt
complete. A unidade permanece em progresso. Não iniciar outro IMP.
