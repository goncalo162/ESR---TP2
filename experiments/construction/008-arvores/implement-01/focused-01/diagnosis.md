# Negativos iniciais preservados

Focused-01: 23 testes, 1 falha e 1 erro. Asserções/prazos preservados.

R09: filho suspenso é removido em stepChild após expiração, mas detach não
reativa a passagem de Tree; sem callback/evento posterior fica Tree sem procura.
Correção no RoutingService autorizado: reavaliar trees no passe limitado após detach.

R13: diagnóstico-r13-01 reproduz snapshot antigo capturedAt=0/recoveries vazio
antes/depois de tick(T), enquanto ActivationLoad mantém Tree1/Local1/envios2.
O método admit chama drive sem qualificador dentro de ActivationEngine: executa
apenas a passagem interna, sem publish/schedule do RoutingService. Quando não
há callback novo (capacidade cheia), não existe wake que publique/arme o prazo.
Correção no RoutingService autorizado: chamar RoutingService.this.drive na
admissão. Conserva reservas físicas/limites, torna prazo e projeção observáveis.
Não aumentar prazos, remover asserções, alterar históricos ou fundações.
Instrumentação temporária só no novo RecoveryTest preservada no diagnóstico;
retirada depois de recolher esta evidência, mantendo todas as asserções originais.
