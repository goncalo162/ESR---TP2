# NeighborDown — evidência anterior a IMP-011 preservada

Preservação em 2026-11-17T17:03:23Z, por instrução do utilizador.
Não foram repetidos testes Java durante Design. Os logs foram copiados
byte a byte de /tmp, conservando o conteúdo e os mtimes originais.
[manifest.json](manifest.json) contém origem, dimensão e SHA-256 das cópias.

1 - **[verify-unrestricted.log](verify-unrestricted.log)**: verify com Java 21
e sockets locais autorizados, terminado em 2026-11-17T14:04:33Z. 1296 testes,
1 falha, 0 erros, 0 ignorados. Falhou a asserção na linha 96 de
OverlayNodeMonitoringTest.realTcpUsesSameOwnerV2ProbesAndEofInvalidatesCurrentLink.
2 - **[monitoring-recheck.log](monitoring-recheck.log)**: repetição isolada
terminada em 2026-11-17T14:05:11Z; 1 teste, 1 falha, 0 erros, 0 ignorados,
na mesma asserção. Não comprova a causa nem que toda a monitorização falhe.
3 - **[verify-sandbox-denied.log](verify-sandbox-denied.log)**: primeira execução
restrita, terminada em 2026-11-17T14:03:48Z; 1296 testes, 78 erros de sockets,
0 falhas e 0 ignorados. Não classificar estes erros como defeito do produto.

## Proveniência e limites

O manifesto regista o checkout observado ao preservar, não um hash de JAR
recuperado dos ensaios. Não se inventam binários, XML ou artefactos de subprocessos
que estes logs não contêm. As cópias verificadas deixam de depender de /tmp;
não houve commit ou publicação por efeito desta preservação.

**Causa: não estabelecida.** Não atribuir a falha a uma corrida, ao logger,
à CPU ou ao ambiente apenas pela asserção. Preservar o resultado negativo mesmo
que futuras repetições passem. Não alterar/esquecer o teste para obter verde.

## Investigação a executar numa etapa que permita código

1 - Reconstituir a identidade da Session esperada e a sequência de eventos
efetivamente observada, distinguindo snapshot de monitor, entrega ao observador
e terminação física. Estas são perguntas de investigação, não causa afirmada.
2 - Reproduzir primeiro sem mudanças, com o teste e prazo originais, guardando
novos logs/XML/contexto em diretório diferente e identificando JDK/versão.
3 - Confrontar a asserção com os contratos aprovados e a ordem efetiva. Se a
evidência for insuficiente, declarar isso; não criar um resultado verde fictício.
4 - Se uma correção exigir teste histórico ou fundação fora dos quatro caminhos
de IMP-011, apresentar evidência/impacto e obter autorização específica antes
de a aplicar. Não existe essa autorização nesta sessão de Design.
5 - Antes de aceitar Implement/Test, consolidar a investigação e a regressão
pertinente, com preservação dos negativos. Esta é uma pendência técnica existente,
não um gate externo adicional nem reabertura automática de uma tarefa DONE.
