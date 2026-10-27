# Coordenação — Waheed, Fillmore e Denji

Data: 27-10-2026. Base observada: revisão 2e58ef5, PHASE-01/TASK-001–004 DONE; PHASE-02–07/TASK-005–031 TODO. Esta distribuição é documentação de responsabilidades e revisão. Não inicia implementação, não altera dependências ou critérios técnicos, não muda estados e não atribui retroativamente autoria à fase 1.

## 1. Documentos individuais e fontes de verdade

- [Waheed](WAHEED.md): transporte, vizinhança, monitorização, recuperação, UDP/replicação, cenário 1 e pacote.
- [Fillmore](FILLMORE.md): bootstrap de consulta, routing/ativação/adaptação, fontes, cenário 2 e artigo.
- [Denji](DENJI.md): composição, diagnóstico, marcos, cliente, integração, cenário 3, análise e defesa.

Os ficheiros TASK continuam a especificação detalhada e têm agora responsável/revisor. Os ficheiros pessoais são filas e orientações, não substituem critérios. [15 — Progresso](../15-PROJECT-PROGRESS.md) e os estados dos TASK são a fonte de estado vivo; a matriz abaixo é o retrato da distribuição. Não manter três cópias independentes do progresso.

Ler [guia da IA](../AI-IMPLEMENTATION-GUIDE.md), [roadmap](../08-IMPLEMENTATION-ROADMAP.md), [contratos](../07-API-AND-INTERFACES.md), [decisões](../12-RISKS-AND-DECISIONS.md), [testes](../09-TESTING-STRATEGY.md) e [Definition of Done](../14-DEFINITION-OF-DONE.md). Atribuições equilibram áreas e revisão; número de tarefas não é estimativa de horas. Se houver redistribuição, atualizar esta matriz, ambos os documentos pessoais e metadados TASK em conjunto.

## 2. Matriz completa das 31 tarefas

| Tarefa | Fase | Responsável principal | Revisor principal | Dependências explícitas | Estado na distribuição |
|---|---|---|---|---|---|
| [TASK-001](../tasks/PHASE-01/TASK-001.md) — Validar a base Java e configurar testes | 01 | Base anterior: autoria não atribuída | Leitura conjunta; não repetir execução por esta divisão | nenhuma | DONE |
| [TASK-002](../tasks/PHASE-01/TASK-002.md) — Fixar contratos, fixtures e decisões pendentes | 01 | Base anterior: autoria não atribuída | Leitura conjunta; não repetir execução por esta divisão | [TASK-001](../tasks/PHASE-01/TASK-001.md) (base concluída) | DONE |
| [TASK-003](../tasks/PHASE-01/TASK-003.md) — Carregar e validar configurações de nós, grafo e streams | 01 | Base anterior: autoria não atribuída | Leitura conjunta; não repetir execução por esta divisão | [TASK-001](../tasks/PHASE-01/TASK-001.md) (base concluída), [TASK-002](../tasks/PHASE-01/TASK-002.md) (base concluída) | DONE |
| [TASK-004](../tasks/PHASE-01/TASK-004.md) — Codificar e descodificar mensagens de controlo v1 | 01 | Base anterior: autoria não atribuída | Leitura conjunta; não repetir execução por esta divisão | [TASK-002](../tasks/PHASE-01/TASK-002.md) (base concluída), [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída) | DONE |
| [TASK-005](../tasks/PHASE-02/TASK-005.md) — Estabelecer transporte TCP full-duplex com recursos limitados | 02 | [Waheed](WAHEED.md) | [Fillmore](FILLMORE.md) | [TASK-004](../tasks/PHASE-01/TASK-004.md) (base concluída) | TODO |
| [TASK-006](../tasks/PHASE-02/TASK-006.md) — Fornecer localização inicial dos vizinhos configurados | 02 | [Fillmore](FILLMORE.md) | [Waheed](WAHEED.md) | [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-005](../tasks/PHASE-02/TASK-005.md) (Waheed) | TODO |
| [TASK-007](../tasks/PHASE-02/TASK-007.md) — Gerir handshake, unicidade e reconexão de vizinhos | 02 | [Waheed](WAHEED.md) | [Denji](DENJI.md) | [TASK-005](../tasks/PHASE-02/TASK-005.md) (Waheed), [TASK-006](../tasks/PHASE-02/TASK-006.md) (Fillmore) | TODO |
| [TASK-008](../tasks/PHASE-02/TASK-008.md) — Compor o processo overlay e encerrar de forma previsível | 02 | [Denji](DENJI.md) | [Waheed](WAHEED.md) | [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-006](../tasks/PHASE-02/TASK-006.md) (Fillmore), [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed) | TODO |
| [TASK-009](../tasks/PHASE-02/TASK-009.md) — Preparar evidência do primeiro marco intermédio | 02 | [Denji](DENJI.md) | [Fillmore](FILLMORE.md) | [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji) | TODO |
| [TASK-010](../tasks/PHASE-03/TASK-010.md) — Medir RTT e publicar falhas de vizinhança | 03 | [Waheed](WAHEED.md) | [Fillmore](FILLMORE.md) | [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed), [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji) | TODO |
| [TASK-011](../tasks/PHASE-03/TASK-011.md) — Modelar candidatos, custos e snapshots por stream | 03 | [Fillmore](FILLMORE.md) | [Waheed](WAHEED.md) | [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed) | TODO |
| [TASK-012](../tasks/PHASE-03/TASK-012.md) — Difundir anúncios e construir catálogo e rotas | 03 | [Fillmore](FILLMORE.md) | [Denji](DENJI.md) | [TASK-007](../tasks/PHASE-02/TASK-007.md) (Waheed), [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore) | TODO |
| [TASK-013](../tasks/PHASE-03/TASK-013.md) — Diagnosticar percursos pela tabela de rotas | 03 | [Denji](DENJI.md) | [Fillmore](FILLMORE.md) | [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore) | TODO |
| [TASK-014](../tasks/PHASE-04/TASK-014.md) — Ativar árvores com ACK e procura agregada | 04 | [Fillmore](FILLMORE.md) | [Waheed](WAHEED.md) | [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore), [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-013](../tasks/PHASE-03/TASK-013.md) (Denji) | TODO |
| [TASK-015](../tasks/PHASE-04/TASK-015.md) — Podar ramos, renovar leases e remover procura abandonada | 04 | [Fillmore](FILLMORE.md) | [Denji](DENJI.md) | [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore) | TODO |
| [TASK-016](../tasks/PHASE-04/TASK-016.md) — Recuperar árvores após falha de upstream | 04 | [Waheed](WAHEED.md) | [Fillmore](FILLMORE.md) | [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore) | TODO |
| [TASK-017](../tasks/PHASE-04/TASK-017.md) — Adaptar rota por RTT com histerese e troca confirmada | 04 | [Fillmore](FILLMORE.md) | [Waheed](WAHEED.md) | [TASK-010](../tasks/PHASE-03/TASK-010.md) (Waheed), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed) | TODO |
| [TASK-018](../tasks/PHASE-04/TASK-018.md) — Preparar o segundo marco com árvores e adaptação | 04 | [Denji](DENJI.md) | [Waheed](WAHEED.md) | [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji), [TASK-013](../tasks/PHASE-03/TASK-013.md) (Denji), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed), [TASK-017](../tasks/PHASE-04/TASK-017.md) (Fillmore) | TODO |
| [TASK-019](../tasks/PHASE-05/TASK-019.md) — Definir codec UDP e fragmentação de frames | 05 | [Waheed](WAHEED.md) | [Denji](DENJI.md) | [TASK-002](../tasks/PHASE-01/TASK-002.md) (base concluída), [TASK-004](../tasks/PHASE-01/TASK-004.md) (base concluída), [TASK-011](../tasks/PHASE-03/TASK-011.md) (Fillmore), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji) | TODO |
| [TASK-020](../tasks/PHASE-05/TASK-020.md) — Ler fontes MJPEG e controlar ritmo de publicação | 05 | [Fillmore](FILLMORE.md) | [Denji](DENJI.md) | [TASK-002](../tasks/PHASE-01/TASK-002.md) (base concluída), [TASK-003](../tasks/PHASE-01/TASK-003.md) (base concluída), [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed) | TODO |
| [TASK-021](../tasks/PHASE-05/TASK-021.md) — Reencaminhar e replicar datagramas pela árvore ativa | 05 | [Waheed](WAHEED.md) | [Fillmore](FILLMORE.md) | [TASK-014](../tasks/PHASE-04/TASK-014.md) (Fillmore), [TASK-015](../tasks/PHASE-04/TASK-015.md) (Fillmore), [TASK-016](../tasks/PHASE-04/TASK-016.md) (Waheed), [TASK-017](../tasks/PHASE-04/TASK-017.md) (Fillmore), [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed) | TODO |
| [TASK-022](../tasks/PHASE-05/TASK-022.md) — Reconstituir frames com buffers e prazo limitados | 05 | [Denji](DENJI.md) | [Waheed](WAHEED.md) | [TASK-019](../tasks/PHASE-05/TASK-019.md) (Waheed), [TASK-021](../tasks/PHASE-05/TASK-021.md) (Waheed) | TODO |
| [TASK-023](../tasks/PHASE-05/TASK-023.md) — Apresentar catálogo e vídeo numa janela Swing | 05 | [Denji](DENJI.md) | [Fillmore](FILLMORE.md) | [TASK-012](../tasks/PHASE-03/TASK-012.md) (Fillmore), [TASK-022](../tasks/PHASE-05/TASK-022.md) (Denji) | TODO |
| [TASK-024](../tasks/PHASE-05/TASK-024.md) — Integrar duas origens e quatro streams ponta a ponta | 05 | [Denji](DENJI.md) | [Waheed](WAHEED.md) | [TASK-008](../tasks/PHASE-02/TASK-008.md) (Denji), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji), [TASK-020](../tasks/PHASE-05/TASK-020.md) (Fillmore), [TASK-021](../tasks/PHASE-05/TASK-021.md) (Waheed), [TASK-022](../tasks/PHASE-05/TASK-022.md) (Denji), [TASK-023](../tasks/PHASE-05/TASK-023.md) (Denji) | TODO |
| [TASK-025](../tasks/PHASE-06/TASK-025.md) — Preparar CORE e comprovar replicação no cenário de quatro nós | 06 | [Waheed](WAHEED.md) | [Denji](DENJI.md) | [TASK-024](../tasks/PHASE-05/TASK-024.md) (Denji) | TODO |
| [TASK-026](../tasks/PHASE-06/TASK-026.md) — Demonstrar alternativas e adaptação no cenário de seis nós | 06 | [Fillmore](FILLMORE.md) | [Waheed](WAHEED.md) | [TASK-025](../tasks/PHASE-06/TASK-025.md) (Waheed) | TODO |
| [TASK-027](../tasks/PHASE-06/TASK-027.md) — Validar cenário complexo com duas origens e cinco clientes | 06 | [Denji](DENJI.md) | [Fillmore](FILLMORE.md) | [TASK-026](../tasks/PHASE-06/TASK-026.md) (Fillmore) | TODO |
| [TASK-028](../tasks/PHASE-06/TASK-028.md) — Recolher métricas e avaliar resultados e limites | 06 | [Denji](DENJI.md) | [Fillmore](FILLMORE.md) | [TASK-025](../tasks/PHASE-06/TASK-025.md) (Waheed), [TASK-026](../tasks/PHASE-06/TASK-026.md) (Fillmore), [TASK-027](../tasks/PHASE-06/TASK-027.md) (Denji) | TODO |
| [TASK-029](../tasks/PHASE-07/TASK-029.md) — Escrever artigo com protocolos e avaliação crítica | 07 | [Fillmore](FILLMORE.md) | [Denji](DENJI.md) | [TASK-028](../tasks/PHASE-06/TASK-028.md) (Denji) | TODO |
| [TASK-030](../tasks/PHASE-07/TASK-030.md) — Empacotar código e verificar reprodução da entrega | 07 | [Waheed](WAHEED.md) | [Fillmore](FILLMORE.md) | [TASK-029](../tasks/PHASE-07/TASK-029.md) (Fillmore) | TODO |
| [TASK-031](../tasks/PHASE-07/TASK-031.md) — Preparar defesa e checklist de submissão humana | 07 | [Denji](DENJI.md) | [Waheed](WAHEED.md) | [TASK-009](../tasks/PHASE-02/TASK-009.md) (Denji), [TASK-018](../tasks/PHASE-04/TASK-018.md) (Denji), [TASK-030](../tasks/PHASE-07/TASK-030.md) (Waheed) | TODO |

Todas as 27 tarefas futuras têm exatamente um responsável e um revisor diferente. Quem implementa também escreve/executa os testes e prepara evidência; o revisor reproduz/verifica os resultados relevantes. Denji não é o único responsável por testes, integração ou relatório. Waheed e Fillmore participam na TASK-024/028/029/031 com os dados e explicações dos seus componentes.

## 3. Ordem real de execução e fecho de fases

Só iniciar uma tarefa quando todas as suas dependências estiverem DONE **e integradas numa base comum**, e a fase anterior estiver DONE. Um fornecedor a trabalhar numa branch não é ainda uma dependência disponível. Uma fase mantém IN PROGRESS até todos os seus critérios/testes de integração estarem verificados.

| Fase | Ordem de implementação que respeita o plano | Coordena o fecho; revisão dos três |
|---|---|---|
| 02 | 005 Waheed → 006 Fillmore → 007 Waheed → 008 Denji → 009 Denji | Denji |
| 03 | 010 Waheed → 011 Fillmore → 012 Fillmore → 013 Denji | Fillmore |
| 04 | 014 Fillmore → 015 Fillmore → 016 Waheed → 017 Fillmore → 018 Denji | Fillmore |
| 05 | 019 Waheed → 020 Fillmore e 021 Waheed em paralelo; 022 Denji após 021; 023 Denji após 022; 024 Denji após 020/021/022/023 | Denji |
| 06 | 025 Waheed → 026 Fillmore → 027 Denji → 028 Denji | Waheed |
| 07 | 029 Fillmore → 030 Waheed → 031 Denji, incluindo confirmação humana da entrega | Denji |

Na fase 05, a TASK-022 não precisa esperar pela TASK-020 se 019/021 já estiverem integradas. TASK-024 espera por ambas as frentes completas. Nas restantes fases, a cadeia de dependências atual é essencialmente sequencial. Distribuir autores não elimina estas dependências nem garante três implementações simultâneas.

~~~mermaid
flowchart LR
  T19["019 · Waheed: codec UDP"] --> T20["020 · Fillmore: fontes MJPEG"]
  T19 --> T21["021 · Waheed: replicação"]
  T21 --> T22["022 · Denji: reconstrução"]
  T22 --> T23["023 · Denji: reprodução"]
  T20 --> T24["024 · Denji + equipa: integração"]
  T21 --> T24
  T23 --> T24
~~~

O desenho mostra as dependências internas relevantes da fase 05, não dispensa os pré-requisitos adicionais das TASK nem o gate PHASE-04 DONE.

## 4. O que cada pessoa pode fazer agora

| Pessoa | Trabalho disponível na entrada da fase 2 | O que aguarda |
|---|---|---|
| Waheed | Ler TASK-005, acordar eventos/API de transporte com Fillmore e cenários com Denji; implementar/testar 005 quando iniciar a construção | Não antecipar 007, 010 ou UDP |
| Fillmore | Rever o contrato proposto para 005; preparar casos de consulta bootstrap repetida/concorrente/offline, respostas sem lease e rejeição de operações indevidas | Implementação 006 aguarda 005 integrada |
| Denji | Preparar matriz de testes de HELLO, arranque/encerramento e reconexão com bootstrap parado; preparar a revisão de 007 e cenários CLI de 008 | Implementação 008 aguarda 006/007 integradas |

Preparação significa leitura, desenho dos casos, revisão de interfaces e notas de trabalho; não é implementar antecipadamente uma TASK que aguarda dependências, nem executar CORE/vídeo. Pode ser registada em notas da própria TASK sem mudar o seu estado para IN PROGRESS. Revisões de tarefas prontas podem decorrer enquanto outro membro implementa uma tarefa independente. Não modificar concorrencialmente os mesmos ficheiros do checkout.

O plano atual foi preservado. Para obter mais paralelismo de produção (por exemplo codec UDP antes do routing ou tabela com métricas simuladas antes de monitorização real), é necessária uma revisão explícita de tarefas/gates e dos testes de integração. Este documento não concede essas exceções nem sugere marcar DONE código com dependências fictícias.

## 5. Quatro momentos de revisão por tarefa

1. **Antes da implementação que define/consome uma interface:** autor, revisor e consumidor afetado confirmam mensagens, unidades, ownership de estado, eventos e tratamento de erros. Mudança de contrato atualiza 07/12/tarefas/testes antes do consumidor depender dela. Uma revisão documental não comprova runtime.
2. **Após implementar e testar:** autor entrega diff/branch, base exata, âmbito, comandos/resultados e checklist; o revisor indicado analisa os critérios e repete a validação pertinente. Não há autoaprovação. Se houver correções, manter a tarefa IN PROGRESS e rever o novo diff.
3. **Antes do DONE e da próxima dependência:** revisão aceite sobre a versão final, integração na base comum e verificação de regressão proporcionada. Dependentes só começam sobre a versão integrada. Um teste unitário verde não substitui validação de sockets, GUI ou CORE quando exigida.
4. **No fecho da fase:** coordenador reúne os três, verifica entregável, critérios da fase, evidência, estado das tarefas e pendências. A equipa confirma que não existem conflitos de contrato nem requisitos parcialmente cumpridos apresentados como completos.

Review de código não substitui a confirmação humana de upload exigida na TASK-031. Nenhum membro deve contactar docente ou submeter externamente por mera atribuição neste plano.

## 6. Ramos, ficheiros partilhados e integração

- Uma branch por tarefa, com nomes sugeridos task-005-waheed, task-006-fillmore, task-008-denji. Estes nomes são convenção proposta, não branches criadas por esta documentação.
- Antes de iniciar, sincronizar a base comum, verificar git status, preservar alterações e ler a tarefa/requisitos. Não criar commits ou publicar alterações de outra pessoa como seus. Cada tarefa tem um único autor principal.
- Revisão pode usar PR ou diff local; não depende de serviço remoto. Ao aceitar, o revisor regista a revisão exata; o autor integra pela prática de Git acordada pela equipa, sem forçar histórico nem sobrescrever alterações.
- Não misturar alterações de duas pessoas no mesmo checkout. Usar clones/worktrees separados quando houver trabalho paralelo real; esta documentação não os cria.
- RoutingService/Route/RoutingTable: Fillmore coordena as interfaces; Waheed altera-os na 016 e Fillmore revê antes de 017. 014/015/016/017 não são implementadas em paralelo sobre esses ficheiros.
- OttApplication/OverlayNode/StreamingClient: Denji coordena composição; Waheed/Fillmore entregam interfaces e corrigem os seus componentes. Mudanças de integração não justificam reescrever módulos já aceites.
- Contratos/fixtures/POM/configuração partilhados: combinar a alteração com os consumidores; não renumerar códigos ou regenerar expected bytes com o encoder em teste para fazer passar testes.
- Alterações de estado em TASK são feitas pelo autor; notas de aceitação incluem o revisor. Para 15/13, integrar apenas as linhas afetadas com edição coordenada, sem apagar handoffs de outros. Preferir manter logs detalhados em experiments e resumo em 15.
- Conflitos são resolvidos pelo autor da tarefa com o responsável do componente e nova revisão; não escolher versões inteiras de ficheiros sem inspeção.

## 7. Evidência e critérios para integrar

O autor executa a validação V-XXX e testes afetados da TASK. O revisor comprova os seus resultados, limites e critérios. Após integrar, verificar o build comum e as regressões relevantes. Repetir cenários adicionais apenas se mudanças/falhas justificarem; não deixar todos os testes para Denji nem para o fim.

Na base atual, o comando foi validado no host a partir de Projeto/ com este JDK instalado:

```bash
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -o -Dstyle.color=never clean package
```

Em outra máquina, selecionar o JDK 21 local e confirmar java/mvn -version; o caminho acima não é universal. -o exige cache Maven já preparada; a falta de dependências offline não comprova erro de código e não autoriza mudar versões do POM. Comandos concretos de testes futuros vêm das TASK e serão confirmados durante a implementação. O build Java não substitui os roteiros GUI/CORE nem um resultado de compilação prova funcionalidade.

Para TASK-005–031, DONE exige revisão aceite e integração na base comum, além da [Definition of Done](../14-DEFINITION-OF-DONE.md). Estes requisitos não reabrem a fase 1 já concluída. A tarefa pode ter implementação local pronta e continuar IN PROGRESS enquanto espera revisão/integração. Uma tarefa que aguarda normalmente a sua dependência continua TODO; BLOCKED exige impedimento real descrito.

Modelo para a entrega do autor e a aceitação do revisor, no handoff do ficheiro TASK:

```text
TASK / autor / revisor:
Base e branch ou revisão exata:
Objetivo e ficheiros alterados:
Dependências integradas e gate da fase:
Contratos alterados e decisão registada, se aplicável:
Validação V-XXX, comandos, versões e resultados reais:
Critérios cumpridos / pendências / limitações:
Revisão: por fazer | alterações pedidas | aceite, com revisor/data/revisão exata:
Integração: ainda não realizada | revisão integrada e verificação executada:
Atualizações em TASK / 15 / 13 e próxima tarefa desbloqueada:
```

Os termos da linha Revisão descrevem a revisão, não acrescentam novos estados de tarefa. Os estados de tarefa continuam TODO, IN PROGRESS, BLOCKED e DONE. As notas documentais de preparação não precisam de testes unitários artificiais.

## 8. Critérios transversais que os três preservam

- Java 21/Maven e contratos atuais; 16 tipos de controlo ativos, código 3 retirado/reservado.
- Bootstrap apenas consulta inicial de localizações configuradas; sem registo vivo, lease de registo, renovação, sondas ou aviso de saída. Leases de subscrição de streams pertencem ao routing.
- Estratégia 1 da Etapa 2: anúncios periódicos por origem/stream, custo RTT acumulado, caminhos sem ciclos e ativação pelo cliente; nenhum vídeo sem procura.
- CORE/topologies/cenario1/2/3.xml são reutilizados. Perfis loopback não substituem IPs reais ou aceitação em CORE; não criar topologias alternativas por defeito.
- Duas origens, pelo menos dois vídeos distintos por origem, UUID/épocas e estado isolados por stream. Fontes e preparação dos conteúdos continuam gate de TASK-020/030.
- Leitura parcial TCP, buffers/filas limitados, expiração, concorrência e shutdown têm testes nas tarefas relevantes. Não afirmar que GUI/CORE/entrega foram verificados se não foram.
- TASK-024/028/029/031 são trabalho coordenado: os três têm de compreender comunicação, routing, streaming e evidências para a defesa.

## 9. Decisões pendentes e redistribuição

Datas/conteúdo dos marcos, recursos finais de vídeo/redistribuição, runtime/GUI/volumes CORE e dados de entrega mantêm os gates em 12/15. O responsável da tarefa informa a equipa e regista impacto/condição de resolução; não inventa aprovação docente, prazos ou resultados.

Qualquer redistribuição ou revisão para mais paralelismo deve manter um autor principal e um revisor distinto, rever dependências/gates/ficheiros partilhados, atualizar os três documentos e não introduzir ciclos. O trabalho só fica concluído quando a evidência e a integração o justificarem.

## 10. Validação desta distribuição

Verificação documental em 27-10-2026: 31 tarefas na matriz, 27 tarefas futuras com responsável/revisor diferentes e filas pessoais de implementação/revisão consistentes com os metadados TASK. Estados, dependências e especificações técnicas das 31 tarefas preservados face à revisão 2e58ef5; apenas metadados de atribuição foram acrescentados às futuras. Grafo de dependências sem ciclos; TASK-005 é a única implementação inicial desbloqueada.

Conferidas 2191 referências relativas a ficheiros em docs/ e experiments/, sem destinos em falta, e estrutura obrigatória das TASK preservada. Os 168 ficheiros existentes sob Projeto/ e CORE/ considerados na verificação, excluindo target, mantêm os hashes e não foram adicionados ficheiros de implementação. git diff --check passou. Não foram executados testes Java ou experiências CORE nesta distribuição documental; a evidência de 131 testes citada nos documentos pertence à execução anterior da fase 1.
