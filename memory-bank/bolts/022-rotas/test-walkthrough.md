---
stage: test
bolt: "022-rotas"
created: "2026-11-14T17:45:23Z"
status: "approved-complete"
---
# IMP-010 — Test aprovado e bolt fechado

✅ Plan/Implement/Test aprovados; manual humano Luis executado e revisão final
Luis confirmada em substituição de Fillmore. Versão revista integrada em main; bolt e fase 3 fechados. Os registos anteriores abaixo conservam o histórico das etapas.

## Construção e código aprovado

Dentro de Projeto/, comando real:

```sh
mvn -B -ntp -Dott.trace.evidence=/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/022-rotas/test-build-01/cli clean verify
```

**Exit0:1296 testes,0 falhas/0 erros/0 ignorados;17 XML novos.** Os1256 testes
históricos e40 de RouteTraceTest foram executados nesta construção, não herdados
de relatórios anteriores. JDK21.0.13/Maven3.9.9. [Log](../../../experiments/construction/022-rotas/test-build-01/maven.log),
[resultado/suites](../../../experiments/construction/022-rotas/test-build-01/result.json),
[XML de trace](../../../experiments/construction/022-rotas/test-build-01/TEST-pt.uminho.esr.ott.routing.RouteTraceTest.xml).
Os17 XML têm mtime posterior ao início preservado; clean removeu os antigos.

Novo [JAR fixo](../../../experiments/construction/022-rotas/test-build-01/ott-streaming-1.0-SNAPSHOT.jar):
436556bytes,238 classes major65, nenhum Test.class; todas as classes empacotadas
iguais às classes do build. SHA-256:

```text
5f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52
```

Todas as entradas de Projeto/src e POM conferidas antes/depois contra o
checkpoint aprovado de Implement: **nenhuma alteração Java em Test**, incluindo
os quatro caminhos autorizados, testes históricos, NeighborManager/send(),
configuração, protocolo/TCPv2/19tipos/código3 rejeitado, framing/caps e UDP.
[Prova e quatro hashes](../../../experiments/construction/022-rotas/test-preservation.json).
target/ gerado separado da entrega; JAR e XML foram copiados para experiments/.

## V-013 / T01–T18 — consolidação técnica

[Mapa com nomes dos testes](../../../experiments/construction/022-rotas/test-oracle-map.json).
Os IDs seguintes são grupos de oráculos, não18 testes presumidos adicionais.

| Oráculo | Evidência atual e alcance |
|---|---|
| T01 | Fonte local [S], nenhum envio diagnóstico remoto ou procura |
| T02 | Duas alternativas; cada salto lê a própria tabela, sem inverter path antigo; losango CLI/TCP real |
| T03 | Retorno pela Session guardada mesmo após seleção mudar; conexão de jusante substituta não recebe resposta velha |
| T04 | UNKNOWN_STREAM versus NO_ROUTE para binding sem RTT/expirado/sem candidato; CLI real |
| T05 | TTL0/1/32, origem no último nó, path32/33, ciclo/IDs/prefixo inválidos; TIMEOUT10/relatedType15/TTL_EXHAUSTED |
| T06 | Relógio falso: fila consome budget;2999ms pendente/3000ms terminal, sem novo envio de pedido expirado |
| T07 | Resultado antes de ACCEPTED; ACCEPTED não é ACK; CLOSED/OVERFLOW/null/exceção e callbacks tardios |
| T08 | Mesmo instanceId/nova connectionId e nova instanceId; epoch/Down antigo e sessões de retorno isoladas |
| T09 | requestId/traceId/stream/prefixo/sender errados ignorados; ERROR relacionado15 correlacionado e restantes eventos preservados |
| T10 | Duplicação/cancelamento/timeout não renovam prazo nem criam retry ou segundo terminal |
| T11 | Local4,por Session4,global64/S64; reserva física sobrevive a timeout, intenção de resposta limitada, sem evicção protegida |
| T12 | Wake/fila owner/scheduler limitados; recusa real do owner continua fatal e cleanup corre; até2 jobs |
| T13 | Estado de procura/active/pending/downstream intacto; sem11–14; observadores/eventos históricos reexecutados |
| T14 | CLI/ajuda/UUID/snapshots/títulos e linhas4096;256 entradas cedem a quit e segundo cursor BUSY; limite antes de copiar |
| T15 | JVMs reais BOOT/S/R1/R2/C, TCP loopback, trace/alternativa/erros e códigos/cleanup preservados |
| T16 | Falha TCP durante trace pendente com origem suspensa e relay morto, retorno explícito; reconexão por fakes; PEER_EOF assertado no TransportTest |
| T17 | Cleanup com future pendente, callback realmente em execução e job retido; prazo global existente e liberação física; falha do relógio sem reserva órfã |
| T18 | clean verify1296/0/0/0,17XML,JAR/hash novos,código aprovado intacto,manifestos e roteiro humano sobre JAR fixo |

Não inferir FIN/RST de SIGKILL: o ensaio CLI observa NeighborDown/TRANSPORT;
classificação PEER_EOF é a prova de transporte separada. UDP sentinela prova
zero datagramas apenas no intervalo observado, não captura CORE ou performance.
Não se mediram FPS/delay unidirecional. Perfis têm fps25 de configuração,
não taxa de vídeo entregue. Os testes reais usam classes recém-compiladas;
o ensaio técnico do roteiro abaixo usa **exclusivamente o JAR fixo**.

## Falha histórica preservada — limitação sem causa estabelecida

Na regressão02 de Implement, o teste histórico
m13PhysicalIoJoinsAreProvedBeforeJvmExitWithBlockedOutput não observou a JVM
terminar dentro dos6s. stderr estava vazio e não havia resultado final antes do
cleanup do teste. [Log/XML/artefactos preservados](../../../experiments/construction/022-rotas/implement-behavior/regression-02/result.json).
A causa **não está estabelecida**; não atribuir CPU/logger/ambiente sem prova.

O mesmo teste e prazos inalterados passaram na repetição isolada, regressão03
e neste clean verify. **Não reapareceu em Test.** Não houve aumento de prazos,
exclusão ou enfraquecimento de testes. Se reaparecer, investigar antes de
apresentar Test para aprovação. O sucesso atual não apaga a execução falhada.

## Roteiro humano e ensaios técnicos separados

[Roteiro completo](../../../experiments/construction/022-rotas/manual-instructions.md):
seis terminais(controlo+cinco nós),preparação com operador real/JDK21,perfis/portas,
logs por processo e comandos, PID/saída/SIGKILL intencional, hashes, watcher UDP
com intervalo,portas reutilizáveis e report.json novo. Não executar play.
TCP20880..20884 e UDP20981..20984 foram usados com sucesso no ensaio técnico;
prepare volta a conferir a disponibilidade, não promete reserva entre terminais.

- Primeiro [ensaio agent](../../../experiments/construction/022-rotas/manual-runs/20261114T173427Z-ni76vufu/agent-driver-error.json):
  pressupunha NO_ROUTE após sair ambos os relays com S ainda vivo. IMP-035
  reparou C–S e o JAR devolveu corretamente [C,S]. Resultado falhado do roteiro
  preservado, não falha do produto nem motivo para alterar uma fundação fechada.
- [Correção da cena](../../../experiments/construction/022-rotas/manual-observation-fix.json):
  sair de S antes do último relay; mantém NO_ROUTE, reparação e prazos, sem Java,
  JAR ou configuração diferente. O roteiro explica essa ordem explicitamente.
- [Ensaio técnico final kind=agent](../../../experiments/construction/022-rotas/manual-runs/20261114T174144Z-am0q0t63/report.json):
  **34 CHECK_PASS,MANUAL_PASS**, quatro Java0 e um SIGKILL intencional
  (return=-9/sinal9/shell137),cinco processos recolhidos sem forced_by_wrapper,
  watcher0datagramas no intervalo,cinco TCP/quatro UDP disponíveis e hashes
  estáveis. [Resumo separado](../../../experiments/construction/022-rotas/manual-agent-result.json).

**Não é manual humano.** Nenhuma pessoa executante atribuída/inventada e nenhuma
aprovação humana de Test inferida. O operador humano tem de gerar RUN novo,
executar/verificar e confirmar report.json/resultados reais. Auxiliar e perfis
não autenticam a identidade declarada. Fonte/JAR/auxiliar são conferidos por hash.

## Checkpoint

V-ADD documental atual: [relatório](../../../experiments/construction/022-rotas/test-v-add.md).
Provas/índice: experiments/construction/022-rotas/test-build-01/ e test-preservation.json.
Q-C-32/33 e decisões anteriores preservadas; sem nova ADR/gate/dependência.
Plan/Implement aprovados; current_stage=test; Test não aprovado, completed=null.
Faltam **manual humano → aprovação Test → revisão final Fillmore → integração**.
Sem staging/commits/push/fecho, implemented=true ou início de outro IMP.


## IMP-010 — pausa da validação e WIP autorizado

Registo 2026-11-14T17:53:17Z. Utilizador interrompeu a validação e autorizou commit WIP local
limitado ao incremento/documentação/evidência,sem target/ ou alterações alheias.
Plan/Implement aprovados; Test técnico1296/0/0/0,JAR/hash preservados.
Manual humano e aprovação Test pendentes; ensaios agent/parciais preservados e
não substituem execução humana. Falha histórica sem causa estabelecida mantém-se.
current_stage=test,status=in-progress,completed=null; só Plan/Implement concluídos.
TASK-013/história/unidade/fase em progresso; sem implemented=true/DONE/complete.
Sem push/revisão final presumida/integração final/novo IMP. Retoma:
experiments/construction/022-rotas/RESUME.md.


## IMP-010 — Test e revisão final aprovados; integração do fecho pendente

Registo 2026-11-15T16:31:56Z. Utilizador aprovou Test, incluindo o manual humano de Luis
20261115T160738Z-me3p9b08:34 CHECK_PASS,passed=true,quatro Java0 e um SIGKILL
intencional; cinco JVM recolhidas sem cleanup forçado,zero UDP no intervalo,
portas livres e233 hashes conferidos. Report original e todos os logs preservados.
Luis confirmou revisão final e aprovação da versão, substituindo Fillmore,
incluindo os NO_ROUTE anteriores às falhas e a falha histórica m13 de shutdown.
Ambas as observações são preservadas; não se alega disponibilidade contínua nem
causa exata de cada retirada ou da falha histórica. Clean verify1296/0/0/0 e JAR
5f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52 mantêm-se
inalterados; não houve nova execução Java. Test concluído,bolt ainda in-progress
até integração desta evidência e cascata autorizada. Fecho de bolt/PHASE-03,
commits e push autorizados; nenhum outro IMP iniciado.
Aprovação: experiments/construction/022-rotas/closure/test-acceptance.json.
Revisão: experiments/construction/022-rotas/closure/final-review-confirmation.json.

Manual humano: [report original](../../../experiments/construction/022-rotas/manual-runs/20261115T160738Z-me3p9b08/report.json),
SHA2569f9497f36e53a769c6f90e6e835feb1b0a72b0e4fc22c36298bce0760183ea5f.
[Análise separada dos NO_ROUTE](../../../experiments/construction/022-rotas/manual-human-review-20261115.json):
no log C,retiradas da geração127 por R2/R1 precedem os dois últimos NO_ROUTE
e são seguidas dos anúncios128. O log de receção não prova sozinho a ordem
exata de processamento no owner nem o motivo de cada retirada. Política
withdraw/geração nova aprovada em Q-C-29; limitações explicitamente aceites.
A falha histórica m13 mantém-se documentada na secção própria, sem causa inventada.


## IMP-010 / TASK-013 — fecho de bolt e PHASE-03 após integração

Registo 2026-11-15T16:36:52Z. Test e manual humano Luis aprovados explicitamente pelo utilizador;
revisão final Luis confirmada, substituindo Fillmore. Versão revista integrada
em main 3349d575ae38c772b38496cc08bc1d44ded764ff, antes de marcar DONE. Prova:
experiments/construction/022-rotas/closure/integration-proof.json.
1296 testes/0falhas/0erros/0ignorados,17XML preservados; manual Luis34 checks,
passed=true,cinco JVM recolhidas,zero UDP no intervalo e hashes intactos.
JAR SHA2565f51dddc6482496f18c772f414788995ae53706511601712c3d0babcd1f5fe52.
NO_ROUTE antes das falhas e falha histórica m13 de shutdown conservados;
retiradas127/anúncios128 observados não provam a causa de cada retirada;
a causa histórica de m13 permanece desconhecida. Sem disponibilidade contínua
alegada nem alteração de fontes/testes/prazos para obter aprovação.
Bolt022 e história complete/implemented=true,TASK-013 DONE,unidade003-rotas
complete e PHASE-03 DONE. V-010–V-013 e demonstração integrada ligadas no fecho;
quatro streams/duas origens são prova controlada automatizada,losango humano
usa dois streams. Sem alegar vídeo,GUI,CORE ou medição de desempenho.
Doze bolts complete,23 planned,nenhum em execução.007-arvores apenas
desimpedido; PHASE-04 e restantes fases não iniciadas. Commits e push autorizados;
o resultado de publicação só será declarado após execução e conferência remota.
