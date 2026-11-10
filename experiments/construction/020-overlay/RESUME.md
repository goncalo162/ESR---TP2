# Ponto de retoma — IMP-005 / 020-overlay / TASK-008

Validação interrompida por pedido explícito do utilizador, registado em 2026-11-10T19:05:26Z.
Guardar WIP local; não fazer push nem iniciar outro IMP.

## Estado preservado

- Base operacional anterior ao WIP: 90cded6b8d321cfcbe369dd4f8dbed9a01ddf430,
  main; referência histórica 2ea748efd61ba5bdea355d2b5a02de6f6bd9a8c5.
- Model/Plan aprovados; Implement preparado e reapresentado após correção terminal.
  Não inferir nova aprovação de Implement/Test pela preparação do manual ou WIP.
- Bolt in-progress, current_stage=implement, stages_completed apenas Plan;
  completed=null. História in-progress sem implemented=true; TASK-008 IN PROGRESS.
- clean verify executado: 981 testes, zero falhas/erros/ignorados, saída 0.
- JAR de evidência: verified-jar/ott-streaming-1.0-SNAPSHOT.jar.
  SHA-256: 303facafc148b96bd2cc0d98ea6afc7a773a50cb2b49d1d5c6c6c4c481de5b52.
  A cópia de evidência fica no WIP; target/ fica excluído.
- Q-C-09–12, send(), testes históricos, wire/caps e fecho de IMP-004 preservados.

## Manual humano parcial — não aprovado

Há oito ficheiros pessoais em manual-Francisco-execucao-01/, quatro logs e quatro
.exit dos arranques iniciais. Preservados sem edição; hashes e saídas reais em
[wip-personal-evidence.json](wip-personal-evidence.json). Os quatro registos
JAVA_EXIT/TEE_EXIT existentes são 0; isso não comprova todos os passos do roteiro,
nem aprovação humana global. Não se declara MANUAL_PASS ou cleanup global.
A leitura de processos não estava disponível na sandbox; o agente não terminou
processos nem infere o seu estado atual.

## Retoma

1. Ler este ponto, [roteiro](manual-instructions.md),
   [última revisão de Implement](../../../memory-bank/bolts/020-overlay/terminal-fix-review.md)
   e estado do bolt. Conferir git status e hashes antes de alterar qualquer coisa.
2. Conferir com Francisco quais passos já foram executados e os logs pessoais,
   sem reescrever/aprovar a execução parcial por inferência.
3. Conferir JAR/hash. Se target/ faltar após checkout, a cópia em verified-jar/
   é o JAR já validado: comparar SHA-256 antes de a copiar para target/. Não alegar
   novo clean verify por fazer essa cópia. Se código/JAR mudar, revalidar a versão.
4. Reconfirmar portas antes de novos arranques; não sobrescrever os rótulos
   bootstrap-01/server-01/relay-01/client-01. Para repetir passos, usar rótulos
   novos (-02/-03), pois o helper recusa sobrescrever logs.
5. Aguardar conclusão pessoal de Francisco e evidência/resultado explícitos.
   Depois continuam aprovação de Test, revisão humana final de Waheed e integração
   da versão revista. Não marcar bolt/TASK DONE antes de cumprir os critérios.

Sem gates externos adicionais. O WIP é apenas preservação de trabalho e evidência,
não entrega, revisão aprovada ou integração final do incremento.
