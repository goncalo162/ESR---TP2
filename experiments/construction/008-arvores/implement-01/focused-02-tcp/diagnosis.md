# Prazo de fila — negativo histórico preservado

105 testes: uma falha em ActivationTest.m09QueueAndFirstTimeoutConsumeTheEntireRound,
zero erros TCP fora da sandbox. O mesmo teste/asserção/prazo permaneceu intacto.
A versão anterior conservava a intenção do pedido já expirado mas deixava
Tree.used/view/cadência por inicializar; reconcileRecovery tomava a primeira
leitura do candidato existente por novidade e emitia depois do prazo consumido.
Correção em admit: registar a ronda/visão esgotada e bloquear o watermark quando
a fila já consumiu T. Intenção mantém-se, mas só evento novo válido pode rearmar.
Focused-03 confirma a correção com os 107 testes e duas integrações TCP.
Focused-02 conserva separadamente as duas recusas de sockets pela sandbox.
