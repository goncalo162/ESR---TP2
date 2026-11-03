---
unit: "001-base-validada"
intent: "001-ott-streaming"
created: "2026-11-03T21:03:35Z"
updated: "2026-11-03T22:00:19Z"
---

# Registo Construction — 001-base-validada

- **2026-11-03T21:03:35Z**: bolt 001-base-validada iniciado em plan, após leitura
  e comunicação do estado ao utilizador. Plano preparado para o checkpoint 1;
  nenhuma etapa aprovada/concluída e nenhum build/teste executado.
- Base: ramo Denji, HEAD 4fe6791178828e72ea48c71d46146f9c8cd4c6f9. Alterações
  preexistentes de Inception preservadas na baseline e no diff da evidência.
- **2026-11-03T21:08:48Z**: aprovação explícita do utilizador registada para
  checkpoint 1 e Q-C-01, exclusivamente para IMP-001. Plan concluído; implement
  iniciado apenas para a inspeção de interfaces e mapeamento dos testes do
  checkpoint 2. Parar nesse checkpoint, apresentando lacunas antes de alterar
  fundações ou testes. Regressão com log novo e teste manual adequado continuam
  obrigatórios na validação. Nenhum build/teste executado nesta continuação.
- **2026-11-03T21:13:13Z**: interfaces/records/enums conferidos por leitura;
  mapeados testes existentes e verificações para V-001–005/V-ADD. Não foi
  identificada lacuna que justifique alterar fundações/testes. Nenhuma suite,
  build, V-ADD ou teste manual executados. Os 163 ficheiros da baseline de
  Projeto/ mantêm os hashes; staged e unstaged diffs dessa pasta vazios.
- **2026-11-03T21:16:53Z**: aprovação explícita do checkpoint 2 registada.
  Autorizado somente preparar implementation-walkthrough e diffs no checkpoint 3,
  distinguindo alterações do bolt e as preexistentes. Manter verificações
  de execução pendentes e parar antes de build, regressão, V-ADD ou teste manual.
  O fecho continua sujeito a critérios, verificações, revisão humana e integração.
- **2026-11-03T21:22:32Z**: aprovação explícita do checkpoint 3 registada.
  Implement concluído e test iniciado. Autorizados apenas V-001–005/V-ADD,
  fixtures/POM/provider, clean verify com JDK 21 e teste manual proposto.
  Apresentar lacunas antes de alterar fundações/testes; parar no checkpoint 4,
  sem fechar o bolt. Resultados ainda por executar neste registo de arranque.
- **2026-11-03T21:24:58Z**: clean verify com JDK 21 concluído, BUILD SUCCESS,
  149 testes, zero falhas/erros/skips: configuração 65, codec 66, transporte 18.
  A primeira tentativa na sandbox teve 18 erros de sockets proibidos; log e XML
  preservados. Repetição fora da sandbox autorizada, sem mudar código/testes.
  POM efetivo/provider/engine conferidos nos artefactos dessa execução.
- **2026-11-03T21:27:23Z**: auditoria independente das 45 fixtures aprovada,
  incluindo hash/hex/tamanhos/offsets e contratos TCP/UDP. Corrigida apenas uma
  suposição de offset variável no auxiliar de evidência; bytes existentes intactos.
- **2026-11-03T21:29:44Z**: teste manual configuração/codec/TCP/shutdown concluído
  em JShell/JDK 21, 22 CHECK_PASS e MANUAL_PASS. A tentativa com engine local
  falhou ao carregar uma classe antes dos checks; engine padrão executou o mesmo
  roteiro. Não é execução GUI/CORE nem revisão humana independente.
- **2026-11-03T21:32:04Z**: relatório de validação preparado para revisão do
  checkpoint 4; V-ADD aprovado. POM/fontes/fixtures/testes preservados; 162/163
  hashes rastreados de Projeto/ iguais, com única diferença na lista gerada
  createdFiles.lst de target/ após o build. Nenhuma lacuna exige intervir nas
  fundações. Bolt in-progress/test; história/unidade não concluídas.
- **2026-11-03T21:40:56Z**: aprovação explícita do checkpoint 4 e dos resultados
  registada; autorizado apenas checkpoint 5. Test concluído e critérios técnicos
  cumpridos. Commit/push e integração não autorizados/presumidos.
- **2026-11-03T21:42:27Z**: mecanismo de fecho ensaiado em cópias temporárias.
  Script instalado sem fs-extra/js-yaml e serialização incompatível com o subset
  local; adaptador de evidência usa só Node builtin e preserva a cascata instalada.
  Ensaio aprovado; nenhum fecho aplicado ao repositório real. Ver Q-C-02.
- **2026-11-03T21:42:46Z**: checkpoint 5 preparado para revisão, com estados,
  progresso/rastreabilidade, evidência e diff. TASK-001–005 continuam DONE;
  nenhuma tarefa seguinte iniciada. Integração dos documentos locais ainda pendente.
- **2026-11-03T21:51:13Z**: aprovação explícita do fecho de IMP-001 e autorização
  para commits separados de Inception/IMP-001, integração em main e push de
  Denji/main para origin, sem force push. Parar antes de resolver conflitos ou
  divergência remota. Preservado o log de execução por Luis, com 22 CHECK_PASS
  e MANUAL_PASS em 2026-11-03T21:42:26Z; SHA-256 e origem guardados na evidência.
  Excluir Projeto/target/ dos commits. Nenhum novo IMP autorizado.
- **2026-11-03T21:53:59Z**: Inception commit b603dc1dcef01a6cd38d0b89ee43695e686d5f49;
  20 ficheiros exatamente iguais aos hashes anteriores ao bolt.
- **2026-11-03T21:55:48Z**: IMP-001 revisto no commit
  d65f94740ef95ffc67d312f7fbccac7575c86f7a, com documentação/evidência e log de Luis.
  Nenhum ficheiro de Projeto/ nos dois commits; target local preservado/excluído.
- **2026-11-03T21:56:28Z**: integração em main comprovada por fast-forward;
  main=Denji=d65f94740ef95ffc67d312f7fbccac7575c86f7a e ancestralidade confirmada.
  V-ADD/snapshot sobre a versão integrada passou; nenhum conflito/divergência remota.
- **2026-11-03T21:56:48Z**: mecanismo validado executado no repositório real/main,
  código 0. Bolt/história/unidade complete; história implemented=true e current_stage
  null no bolt. As outras seis unidades e os outros 32 bolts não foram concluídos.
  Log real e metadados preservados. Fecho aprovado pelo utilizador, sem nova inferência.
- **2026-11-03T22:00:19Z**: V-ADD final detetou dois metadados derivados a alinhar: marcador
  GENERATED do índice (estado complete mantido separadamente) e blocks=false em
  019-overlay após concluir a sua única dependência. Corrigidos sem alterar o
  validador; 019-overlay continua planned, started=null, sem novo IMP iniciado.
  Relatório da primeira tentativa preservado; sem lacuna de fundações/testes.
- Próxima ação autorizada: publicar o fecho em Denji/main para origin sem force,
  após validação final. Não iniciar outro IMP. O resultado efetivo dos pushes
  será confirmado pelas referências remotas e comunicado depois da execução.

Plano: [implementation-plan.md](../../../../bolts/001-base-validada/implementation-plan.md).
Checkpoint 2: [interfaces e testes](../../../../bolts/001-base-validada/interfaces-and-test-map.md).
Checkpoint 3: [walkthrough](../../../../bolts/001-base-validada/implementation-walkthrough.md).
Checkpoint 4: [validação](../../../../bolts/001-base-validada/test-walkthrough.md).
Checkpoint 5: [preparação do fecho](../../../../bolts/001-base-validada/closure-walkthrough.md).
Evidência: experiments/construction/001-base-validada/.
