# Validação — DEMO-001

- Baseline e final: `cd Projeto && mvn -o -B -ntp clean verify`, JDK21.
  Copiar log, XML Surefire e hash/JAR em pastas novas. Referência: 1459 testes;
  nenhum número é inferido como resultado atual. Código aprovado congelado.
- Compilar auxiliares com `javac --release 21 -cp <JAR>` fora de Projeto.
- `./demo/run.sh cenario1 --auto` e `cenario2 --auto`: observar condições
  com prazo, sem inventar amostras nem reexecutar aleatoriamente até verde.
  Falha gera FAIL e conserva a run. Após um ajuste fundamentado, iniciar uma
  nova série de três por cenário sobre a mesma versão das ferramentas.
- Exercitar CLI real no mesmo perfil/JAR: neighbors/recovery/routes/catalog/
  metrics/trace/play/quit. Esperar condições de catálogo sem substituir valores
  ou chamar a implementação de CLI de "simulada".
- Conferir invariantes da confirmação/publicação, mensagens ACTIVATE/ACK e
  DEACTIVATE, instâncias/sessões novas após morte e intenção local persistente.
- Verificar SIGKILL de JVM própria; sobreviventes com PID inalterado; bootstrap
  terminado durante a reparação; retorno do bootstrap só para JVM reiniciada.
- Atrasar frames TCP originais, observar mudanças dos sampleCount/custos de
  NetworkMonitor; qualificar por P=20%, K=3 e D=5s existentes. Sem onMetrics
  injetado, tc/sudo ou clock falso. Registar STARTED/SUCCESS e revisão publicada.
- Verificar saída dos processos e bind das portas antes do PASS. Interrupção
  normal/SIGTERM também limpa filhos; SIGKILL do controlador não é recuperável
  por shutdown hook e fica documentado.
- Comparar hashes de todas as fontes/testes/CORE/scripts anteriores e preservar
  V-018 (44/44), negativos anteriores e limitação R17.
