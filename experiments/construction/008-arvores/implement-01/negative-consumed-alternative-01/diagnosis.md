# R10 — alternativa já consumida voltava a fornecer crédito

Negativo controlado: A tentada, seleção muda para B, B consome as tentativas
restantes e falha; tick(T) emite quarta ativação sem evento novo. Um teste,
uma falha esperada3/observado4, zero erros; XML/log/teste preservados.

Tree.used era preenchido apenas no início da ronda, permanecendo A mesmo
quando a tentativa seguinte já consumia B. Ao terminar, blockRecoveryProof
bloqueava A; reconcileRecovery voltava a tratar B como ramo novo.
Correção: capturar a prova efetivamente usada ao preparar cada tentativa e
consumir o crédito correspondente, sem reiniciar T/A/cadência. Não alterar
asserções, prazos, APIs externas ou históricos. A prova posterior ANN(B,G3)
continua a permitir rearme legítimo; o mesmo B/G2 não reabre por timer.
