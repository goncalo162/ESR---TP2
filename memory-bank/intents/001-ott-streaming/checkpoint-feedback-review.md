# Revisão dos apontamentos do checkpoint

Data: 2026-11-03T14:37:38Z. Intent: 001-ott-streaming.
Fonte: [checkpoint_REPORT.md](../../../docs/checkpoint_REPORT.md).
Atualização: 2026-11-03T14:50:15Z. Estado: CP-01–03 aprovadas pelo utilizador
nesta sessão, por escolha explícita das três recomendações. As análises abaixo
preservam as alternativas avaliadas; as decisões aceites constam de cada secção.

## Conclusão para o fecho de Inception

O documento registou propostas por avaliar, não resultados de testes nem uma
ordem docente definitiva. As três escolhas de identidade/âmbito foram resolvidas:
manter nodeId, validar regresso/entrada tardia de nós configurados e adiar novas
vizinhanças. O [ADD, secção 13](../../../add.md#13-questões-em-aberto-e-changelog)
regista as aprovações sem alterar o âmbito de implementação existente.

A aprovação resolve CP-01–03; a revisão final dos restantes artefactos de
Inception mantém-se pendente. Conservam-se 40 entradas, 26 ADR e 33
histórias/IMP/bolts. A aprovação não representa execução ou revisão de código.

## CP-01 — Identificação por IP no bootstrap

O apontamento admite três interpretações que não devem ser confundidas:

1. Manter a consulta por nodeId e os endpoints configurados, como no plano atual.
2. Procurar a configuração pelo IP observado na ligação, conservando nodeId
   como identidade interna no restante sistema.
3. Substituir nodeId por IP em todo o sistema.

O [contrato de TASK-006](../../../docs/tasks/PHASE-02/TASK-006.md) verifica
senderId/Endpoint.nodeId e configuração imutável. As ADR-018/019 do
[ADD](../../../add.md) usam nodeId para identidade estável, grafo e iniciativa
de conexão; instanceId distingue execuções. Assim, mudar apenas a pesquisa do
bootstrap é uma alteração menor que substituir a identidade global, mas ambas
precisam de decisão explícita e análise de compatibilidade.

A API existente [Connection.remoteAddress](../../../Projeto/src/main/java/pt/uminho/esr/ott/transport/Connection.java)
permite observar o endereço remoto da ligação. O
[Endpoint do protocolo](../../../Projeto/src/main/java/pt/uminho/esr/ott/protocol/ControlMessage.java)
transporta separadamente nodeId, host e portas. Disponibilidade da API não
equivale a implementação ou aprovação da consulta por IP.

Compromissos identificados na alternativa por IP:

- Vários processos podem usar o mesmo IP com portas de escuta diferentes;
  o IP isolado não identifica inequivocamente esses processos.
- Um nó com várias interfaces pode estabelecer a consulta com um IP diferente
  do endpoint publicitado. É necessário definir os aliases aceites e o mapeamento.
- A porta de origem da consulta TCP não deve ser confundida com a porta de
  escuta de controlo configurada do nó.
- Um IP declarado na mensagem e um IP observado no socket têm origens
  diferentes; é necessário escolher qual orienta a consulta e como tratar
  ausência, ambiguidade ou divergência com a configuração.
- Deve definir-se o comportamento em perfis loopback e nos cenários CORE,
  preservando os contratos existentes ou planeando a sua migração.

**Decisão aprovada CP-01:** conservar nodeId como identidade e chave de consulta
nesta versão, com REGISTER/REGISTER_OK e validação dos endpoints configurados.
Não adotar pesquisa por IP nem resposta desencadeada apenas pela ligação TCP.
O IP pode ser observado sem ler o pedido, mas o fluxo atual descodifica o
envelope antes do handler; mudar só a pesquisa não evita esse trabalho. Um
protocolo sem REGISTER seria uma alternativa distinta e exigiria mapeamento
inequívoco IP/nó. Aceita-se o custo da consulta explícita, sem ganho de
desempenho medido que justifique a mudança. Não se atribui ao docente uma
exigência que o apontamento não comprova.

**Responsabilidades mantidas:** Fillmore e Waheed são os responsáveis/revisores
já atribuídos à TASK-006. A identidade global também afeta os consumidores de
configuração, handshake e routing. Esta indicação não redistribui tarefas.

## CP-02 — Falha e posterior regresso de um nó

O [enunciado](../../../docs/ENUNCIADO.md) já exige entrada/saída dinâmica de
processos. O plano inclui entrada tardia e reconexão em
[TASK-007/V-007](../../../docs/tasks/PHASE-02/TASK-007.md), e recuperação após
regresso em [TASK-016/V-016](../../../docs/tasks/PHASE-04/TASK-016.md).
O [guia do checkpoint](../../../docs/CHECKPOINT-1-PREPARACAO.md) também explica
a recuperação de R2 e a posterior escolha de caminho.

Importa distinguir:

- Reiniciar um nó configurado: mantém nodeId, cria nova instanceId, consulta
  inicialmente o bootstrap e reconstrói ligações, métricas, rotas e procura.
- Iniciar pela primeira vez um nó já previsto no grafo: é entrada tardia no
  âmbito atual, mesmo que informalmente se lhe chame um nó novo.
- Admitir um ID que não conste da configuração carregada: é uma extensão de
  admissão/configuração, excluída do âmbito atual do ADD.

O processo que continua vivo e conserva a lista pode reconectar diretamente
com bootstrap desligado. Um processo reiniciado perdeu a lista volátil e
depende novamente da descoberta inicial. Não prometer reinício sem bootstrap.
O regresso de um relay não implica troca imediata de parent: primeiro é preciso
um caminho válido, medições e confirmação; aplicar histerese se o ramo atual
continuar válido. Um reinício da origem cria uma nova época, sem réplica do
mesmo conteúdo noutra fonte.

**Decisão aprovada CP-02:** o cenário do checkpoint abrange regresso/entrada
tardia de nós já configurados. É validação do âmbito existente em TASK-007/016,
não uma nova funcionalidade extra. O roteiro deve demonstrar falha, nova
instância e recuperação. Admissão de IDs desconhecidos continua fora do âmbito.

**Responsabilidades mantidas:** Waheed/Denji na TASK-007 e Waheed/Fillmore
na TASK-016, segundo as atribuições já existentes.

## CP-03 — Novas arestas e seleção antecipada de extras

Selecionar uma rota alternativa ou reconectar uma aresta já permitida não é
criar uma nova vizinhança. O plano atual admite essas recuperações e usa
UNAVAILABLE quando não há caminho válido até à origem.

Criar uma aresta que não conste do grafo, mesmo entre dois IDs já configurados,
altera o contrato de vizinhança imutável. A aceitação de IDs desconhecidos é
uma extensão adicional. Uma ligação overlay nova também exige conectividade
IP entre os endpoints; não repara por si só uma partição da rede de suporte.

Numa eventual adoção futura deste extra seriam necessários: cenário de falha reproduzível,
descoberta de candidatos alcançáveis, autorização das novas relações, política
de escolha/limite X, iniciativa e unicidade, atualização consistente das
vizinhanças e testes de concorrência, ciclos e convergência. O mecanismo não
deve transformar implicitamente o bootstrap num controlador de membership.

**Decisão aprovada CP-03:** a criação de vizinhanças fora do grafo fica adiada
como extra/trabalho futuro. Nesta versão recuperam-se ligações permitidas e
demonstram-se alternativas configuradas. Sem caminho válido usa-se UNAVAILABLE.
O extra teria benefício potencial de recuperar conectividade overlay perdida,
mas esforço e dependências elevados sobre descoberta, handshake e routing.
Uma futura adoção exige revisão de requisitos/ADR/testes e decomposição própria;
os 33 bolts atuais não o incluem.

Esta escolha resolve também a seleção antecipada de extras pedida no ponto
3.3 dos apontamentos: identificar explicitamente o que entra e o que fica
adiado, em vez de decidir apenas na fase final.

**Aprovação registada:** escolha explícita do utilizador nesta sessão. Não se
presume revisão individual de código por qualquer membro da equipa.

## Compromissos técnicos no artigo

O ponto 2 do checkpoint já está contemplado em
[TASK-029](../../../docs/tasks/PHASE-07/TASK-029.md), que exige escrever
trade-offs e análise crítica; Fillmore é responsável e Denji revisor.
O ADD contém alternativas e consequências por ADR. Não há uma nova escolha
arquitetural necessária para aceitar este objetivo de documentação.

Ao executar a TASK-029, explicar motivação, ganho, custo/limite e condições de
validade, relacionando-os com a solução efetiva. Exemplos adequados: frequência
das sondas versus custo de controlo; rapidez de adaptação versus estabilidade;
identidade estável versus dependência de endereços; MJPEG simples versus banda
e descarte de frames; grafo configurado versus capacidade de criar alternativas.
Os exemplos adicionais são sugestões desta revisão, não comentários atribuídos
ao docente. A discussão qualitativa não elimina as medições já exigidas.

## Outras decisões da equipa e momento de resolução

| Assunto | Estado identificado | Momento necessário |
|---|---|---|
| CP-01–03 | Aprovadas pelo utilizador em 03-11-2026; incorporadas no ADD e no relatório | Resolvidas; não aguardam nova escolha |
| Revisão final dos artefactos | Pendente no inception-log | Depois de incorporar as escolhas do checkpoint |
| Revisor humano da TASK-032 | Equipa ainda deve designar pessoa distinta do autor | Antes do DONE da TASK-032; não impede rever/verificar a base |
| Media final e condições de distribuição | Gates G-MEDIA/G-MEDIA-DISTRIBUTION | TASK-020 e TASK-030 |
| Ambiente e provas GUI/CORE | Gates G-GUI/G-CORE | Nas tarefas de execução correspondentes |
| Datas/conteúdo docente e dados de entrega | Gates G-ASSESSMENTS/G-DELIVERY | Antes de fechar os marcos e a entrega afetados |
| Upload da versão final | Gate G-SUBMISSION | Confirmação humana para fechar TASK-031/último bolt |

As atribuições e gates foram conferidos em
[coordenação](../../../docs/team/00-COORDINATION.md),
[TASK-032](../../../docs/tasks/PHASE-02/TASK-032.md) e ADD, secções 10/13.
As escolhas anteriores 1A/2A/3B/4A continuam aprovadas. CP-01–03 confirmam os
contratos existentes; não exigem novas ADR nem substituem as decisões aceites.

## Próxima ação concreta

CP-01–03 estão resolvidas e documentadas; retomar a revisão final dos artefactos
de Inception. O primeiro bolt preparado continua 001-base-validada, seguido
de 019-overlay/TASK-032. Não foram executados bolts, testes Java, GUI ou CORE
para registar estas decisões.
