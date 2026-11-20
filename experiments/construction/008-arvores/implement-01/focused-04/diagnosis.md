# Negativos preservados e correções da fixture

116 testes: duas falhas novas, históricos verdes; logs/XML e fontes preservados.

R18Recovered: o último tick chegava a8s, instante exato de expiração do ANN
inicial (TTL8s). Não havia seleção elegível para renovar, apesar do grant ainda
vivo. Acrescentado ANN novo legítimo antes do tick; nenhuma asserção ou prazo
alterado. A barreira de incompatibilidade antiga continua retirada ao ACTIVATE novo.

R17 TCP: o ticket inicial terminou ERROR, mas a execução não registou a razão
ou a sequência de métricas. Causa desse resultado inicial não estabelecida;
não se atribui ao transporte ou à política de recuperação nem se repete até verde.
A fixture anterior usava monitor/RTT reais e verificava apenas presença de seleção.
A fixture nova usa TCP/handshake/manager/reparação reais com métricas controladas
constantes e espera o prefixo determinístico completo antes do pedido inicial.
Conserva SUCCESS e todas as restantes asserções/prazos; não pede retry humano.
Esta mudança é exclusivamente em RecoveryTest, não nos históricos/fundações.
As integrações históricas continuam a testar NetworkMonitor real sem alterações.
