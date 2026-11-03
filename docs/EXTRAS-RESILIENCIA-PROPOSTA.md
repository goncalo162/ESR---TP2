# Extras para seleção em reunião — resiliência, protocolos e operação

## Estatuto e objetivo

Proposta preparada a partir do enunciado, do ADD e das TASK-006, TASK-007 e TASK-016. Não altera decisões aceites, contratos, atribuições ou estados de implementação. Os nomes de componentes abaixo são fronteiras conceptuais propostas, não APIs já disponíveis.

Objetivo: escolher os extras e definir os seus contratos antes de implementar os componentes afetados, reduzindo alterações futuras. Não é possível garantir ausência de alterações ao código para funcionalidades ainda não especificadas. Configuração só consegue selecionar comportamentos que tenham sido implementados e testados.

O regresso de um nó configurado, reconexão, recuperação por uma rota alternativa e retoma da intenção de consumo já estão previstos nas TASK-007 e TASK-016. A criação de novas vizinhanças e a admissão de identidades não previstas ultrapassam o desenho atual por grafo configurado. Uma partição da rede IP não pode ser reparada apenas criando ligações aplicacionais.

## Comparação para a reunião

**Grupo A — extras técnicos:** alteram algoritmos, decisões distribuídas, protocolo ou garantias de entrega. Devem demonstrar uma melhoria de comportamento perante uma condição adversa.

**Grupo B — extras de apresentação e operação:** tornam a solução mais fácil de observar ou utilizar. Podem exigir programação significativa, mas, no âmbito descrito, não acrescentam mecanismos de recuperação ou entrega. São os extras mais superficiais face ao objetivo técnico desta proposta.

A classificação seguinte é uma estimativa de impacto e esforço relativo, não uma previsão de horas nem de nota. Nenhum extra está selecionado.

| ID | Extra técnico | Profundidade | Dificuldade | Principal alteração | Evidência a comparar |
|---|---|---|---|---|---|
| T01 | Reparação automática de vizinhanças | Alta | Alta | Negociação distribuída de novas arestas | Recuperação e estabilidade após falhas simultâneas |
| T02 | Admissão de identidades novas | Alta | Alta | Política de admissão e propagação de candidatos | Entrada sem reinícios e rejeição de admissões inválidas |
| T03 | Recuperação de falha exclusiva UDP | Alta | Alta | Deteção por stream e recuperação coordenada | Interrupção de vídeo e falsos alarmes |
| T04 | Drenagem com migração antecipada | Alta | Alta | Coordenação da retirada e ativação de alternativas | Perdas/interrupção face a retirada abrupta |
| T05 | Quarentena e reintegração | Média/alta | Média | Histórico de falhas e política de seleção | Mudanças de rota e tempo até reintegração |
| T06 | Bootstrap redundante | Média | Média | Descoberta com failover e revisão de configuração | Arranque com falha e rejeição de configuração incompatível |
| T07 | Admissão por capacidade | Alta | Alta | Reservas concorrentes por ramo | Sobrecarga e qualidade dos consumidores existentes |
| T08 | Réplicas de origem com continuidade | Muito alta | Muito alta | Identidade de conteúdo, sessões e posição de reprodução | Interrupção e salto temporal após perda da origem |
| T09 | Descoberta distribuída com anti-entropia | Alta | Alta | Troca limitada e reconciliação de candidatos | Convergência, tráfego e comportamento sob churn |
| T10 | Recuperação de fragmentos por redundância | Alta | Alta | Codec UDP com paridade e reconstrução limitada | Frames recuperados, atraso e banda adicional |

T06 é uma melhoria de disponibilidade técnica com alcance menor do que T01 ou T08; não deve ser apresentada como tendo a mesma complexidade distribuída. T01 com uma única ligação de reserva predefinida também é uma versão mais simples do que negociação entre vários candidatos.

## Grupo A — Extras técnicos

### T01 — Reparação automática das vizinhanças

**Cenário:** R1 perde os seus vizinhos úteis; R2 continua alcançável por IP, mas não existia uma aresta R1–R2.

**Extensão:** procurar candidatos e negociar uma nova ligação para recuperar conectividade ou redundância. Mudar o upstream para um vizinho existente continua a ser recuperação de rotas; este extra modifica a overlay.

**Definir antes de implementar:**

- Fonte dos candidatos: recomenda-se começar com um conjunto de reserva configurado, distinto das ligações iniciais. Descoberta distribuída pode ser uma extensão posterior.
- Autorização nos dois extremos; um endereço conhecido não implica permissão de ligação.
- Grau desejado, máximo aceite e limite de tentativas simultâneas. O grau desejado não é uma garantia se faltarem candidatos disponíveis.
- Aceitação explícita pelo recetor, resolução de propostas simultâneas e unicidade por par. A regra atual do iniciador por menor ID tem de ser integrada com a negociação, incluindo o caso de o nó maior descobrir primeiro o candidato.
- Prazo, backoff/jitter, permanência mínima e critérios para retirar uma aresta de reparação quando os vizinhos originais regressam.

**Prova:** remover um relay, observar uma aresta que não existia inicialmente e recuperar a stream. Depois repor o relay e verificar ausência de ligações duplicadas, ciclos na árvore e alterações incessantes.

**Impacto:** política de vizinhanças permitidas, configuração, NeighborManager e negociação de controlo. O bootstrap mantém apenas descoberta inicial.

**Desafio técnico principal:** várias reparações simultâneas podem escolher o mesmo candidato e ultrapassar o seu limite de vizinhos. A versão avançada tem de coordenar aceitação/cancelamento, suportar mensagens atrasadas e convergir após pedidos concorrentes. Medir uma nova aresta em funcionamento não demonstra, por si só, tolerância a esta concorrência.

### T02 — Admissão de um nó novo sem reiniciar a rede

**Cenário:** introduzir R7, cuja identidade não estava na topologia inicial, mantendo os restantes processos em execução.

**Extensão:** um nó conhecido admite R7 segundo uma política explícita e permite que ele participe nas vizinhanças e nos anúncios.

**Definir antes de implementar:**

- Quem pode admitir identidades novas e quais papéis são permitidos.
- Se a primeira versão exige configuração prévia de R7 como candidato autorizado. Nesse caso existe entrada dinâmica, mas não admissão de uma identidade completamente desconhecida.
- Descoberta inicial, validação de endpoints, tratamento de ID duplicado e limite de admissões.
- Como disseminar candidatos de forma limitada, se necessário: validade, tamanho máximo, deduplicação e limite de propagação.
- Como retirar uma autorização e o que acontece após reinício. Nenhum destes estados pertence implicitamente ao bootstrap atual.

**Prova:** iniciar R7 com o contacto inicial e os dados de admissão definidos; estabelecer vizinhanças, aprender streams e transportar vídeo sem reiniciar outros nós. Tentar também um nó não autorizado.

**Impacto:** elevado; altera a política atual de identidades e grafo permitidos. Implementar depois da reparação com candidatos de reserva.

### T03 — Recuperação de falhas exclusivas do vídeo

**Cenário:** o TCP mantém-se ligado, mas os datagramas UDP deixam de chegar num ramo.

**Extensão:** observar progresso do vídeo e iniciar recuperação quando existe procura confirmada e emissão esperada. O ADD atual aceita esta falha como limitação na ADR-025; implementar a extensão exige rever essa decisão.

**Definir antes de implementar:**

- Saúde TCP e progresso UDP como sinais separados, associados à stream, sessão e upstream atuais.
- Período inicial de tolerância, prazo sem progresso e confirmação antes de mudar de caminho.
- Evidência para distinguir falha do ramo de origem sem emissão. Se não for possível distinguir, limitar tentativas e reportar a incerteza.
- Alternativas já tentadas, intervalo de recuperação e proteção contra o regresso imediato ao caminho defeituoso.
- Reutilização do mecanismo de ativação/ACK; não criar um segundo mecanismo independente de troca de upstream.

**Prova:** bloquear apenas o UDP de um ramo, manter TCP saudável, recuperar por alternativa e medir a interrupção. Validar também ausência de espectadores e interrupção na origem, para evitar recuperações em ciclo.

**Impacto:** observações de streaming, eventos de saúde e política de recuperação; mensagens novas se forem necessárias confirmações entre nós.

### T04 — Saída planeada com drenagem de tráfego

**Cenário:** retirar um relay para manutenção sem provocar uma falha abrupta aos seus consumidores.

**Extensão:** o relay anuncia a intenção de saída, deixa de aceitar nova procura e permite preparar caminhos alternativos antes de fechar.

**Definir antes de implementar:**

- Estado de drenagem separado de nó falhado; novas admissões e ativações recebem uma resposta explícita.
- Propagação da indisponibilidade futura aos nós afetados, para não voltarem a escolher percursos que atravessam o relay em drenagem.
- Prazo máximo, confirmação das migrações e política quando não existe alternativa: aguardar até ao prazo ou cancelar a manutenção.
- O processo pode falhar durante a drenagem; nesse caso continua aplicável a recuperação normal.

**Prova:** ordenar manutenção, confirmar migração e só depois fechar o relay. Comparar interrupção e perdas com a mesma remoção abrupta.

**Impacto:** ciclo de vida, anúncios/ativação e mensagens de controlo. Fechar sockets de forma limpa, por si só, não realiza este extra.

### T05 — Quarentena e reintegração gradual de nós instáveis

**Cenário:** um relay liga e desliga repetidamente, provocando recuperações e mudanças sucessivas.

**Extensão:** detetar reincidência, evitar temporariamente o nó para novos caminhos e voltar a avaliá-lo após estabilidade observada.

**Definir antes de implementar:**

- Janela de observação, número de incidentes, duração limitada da quarentena e condições de saída.
- Estado associado ao ID lógico; mudar instanceId após cada reinício não deve apagar automaticamente o histórico. Eventos antigos continuam a ser filtrados pela instância/ligação.
- Se a quarentena é por vizinho ou por stream: uma falha UDP numa stream não prova falha total do nó.
- Política para o único caminho disponível: indisponibilidade explícita ou tentativa excecional controlada.

**Prova:** reiniciar repetidamente um relay e mostrar estabilidade pelo caminho alternativo; depois deixá-lo estável e demonstrar reintegração sem intervenção manual.

**Impacto:** histórico de saúde e seleção de candidatos. É diferente da histerese de custo e do backoff de reconexão já previstos.

### T06 — Arranque resistente à indisponibilidade do bootstrap

**Cenário:** o bootstrap principal está desligado quando arranca um processo novo ou reiniciado.

**Extensão recomendada:** vários contactos bootstrap equivalentes, consultados com prazos limitados. Cada um continua a responder apenas a partir de configuração, sem membership nem escolha de rotas.

**Definir antes de implementar:**

- Ordem das tentativas, prazo total e comportamento quando todos falham.
- Mesma revisão de topologia e política em todas as réplicas; definir rejeição de respostas incompatíveis, em vez de combinar silenciosamente listas.
- Configuração local da revisão esperada ou mecanismo explícito de atualização. A revisão não é um estado de nós vivos.
- A lista de contactos não pode depender exclusivamente do bootstrap que está indisponível.

**Prova:** desligar o principal e iniciar um nó através do segundo; testar uma réplica com configuração desatualizada.

**Impacto:** configuração e cliente de descoberta; possível extensão do contrato para identificar a revisão. Persistir uma cache de vizinhos seria uma proposta adicional, com problemas próprios de validade e revogação.

### T07 — Admissão de ramos segundo capacidade do relay

**Cenário:** o caminho de menor RTT passa num relay que já não consegue suportar mais replicação de vídeo.

**Extensão:** recusar ou redirecionar nova procura quando a capacidade de saída é insuficiente, preservando os consumidores existentes.

**Definir antes de implementar:**

- Primeiro passo verificável: orçamento de saída configurado e estimativa documentada do débito de cada stream; não inferir capacidade apenas do RTT.
- Contabilização por aresta de saída e stream. Vários consumidores a jusante da mesma aresta não implicam várias cópias nesse enlace.
- Reserva provisória durante ativação, confirmação, libertação em cancelamento/timeout e revisão quando o débito observado muda.
- Erro explícito de capacidade e tentativa limitada por outra rota. O orçamento é uma política de admissão, não garantia de largura de banda disponível na rede IP.

**Prova:** saturar o orçamento de um relay, adicionar um consumidor e demonstrar utilização de alternativa sem interromper os anteriores.

**Impacto:** ativação, estado de reservas e seleção de alternativas. Evitar começar por uma fórmula que misture RTT e carga sem unidades ou validação.

**Desafio técnico principal:** duas ativações concorrentes não podem consumir ambas o mesmo orçamento livre. Reservas provisórias têm de expirar e ser canceláveis, sem libertar uma reserva nova por causa de um ACK antigo. Testar a falha a meio da ativação e confirmar que não deixa capacidade permanentemente ocupada.

### T08 — Continuidade perante falha do servidor de origem

**Cenário:** o servidor que publica um vídeo falha; outro servidor possui uma réplica do mesmo conteúdo.

**Extensão:** descobrir outra origem do mesmo conteúdo e continuar a reprodução. As quatro streams distintas obrigatórias mantêm-se; réplicas são adicionais.

**Definir antes de implementar:**

- Identidade do conteúdo separada da origem, da stream e da sessão de transmissão.
- Correspondência e versão das réplicas, incluindo hash e perfil de reprodução.
- Se o resultado esperado é reiniciar o vídeo ou continuar numa posição equivalente. Continuidade temporal exige um mecanismo explícito de posição/alinhamento; trocar apenas o endereço não chega.
- Seleção da réplica, indisponibilidade parcial e rejeição de frames/ACK da sessão antiga.

**Prova:** retirar a origem ativa, recuperar noutra réplica e medir interrupção e salto temporal. Repetir com réplica incompatível, que deve ser rejeitada.

**Impacto:** elevado; catálogo, identidade, anúncios, fontes e player. Não confundir com escolher outra rota até à mesma origem, já previsto.

### T09 — Descoberta distribuída de candidatos e anti-entropia

**Cenário:** a rede muda e os contactos de reserva locais deixam de ser suficientes. Os nós aprendem candidatos através de outros participantes, sem depender de um controlador permanente.

**Extensão:** trocar periodicamente subconjuntos limitados de candidatos e reconciliar informação divergente. Anti-entropia significa aqui permitir que trocas posteriores corrijam diferenças entre as listas locais, depois de atrasos ou interrupções. Saber de um candidato não confirma que está vivo nem concede autorização.

**Definir antes de implementar:**

- Registos com identidade, endpoint, proveniência, versão e validade; definir quem pode atualizar cada registo e como validar a informação.
- Política para conflitos, reinícios, expiração e retirada. Uma recomendação antiga não deve ressuscitar indefinidamente um contacto retirado.
- Tamanho máximo das listas/mensagens, frequência das trocas, seleção de pares e proteção contra propagação repetida.
- Separação entre lista de candidatos e ligações ativas: aprender cem contactos não implica abrir cem sockets.
- Medidas de diversidade para evitar que todos escolham os mesmos vizinhos e concentrem o risco de falha.

**Prova:** iniciar nós com conhecimento parcial, introduzir entradas/saídas e uma interrupção temporária de ligações. Após restabelecer conectividade, medir convergência das informações relevantes, memória limitada e bytes de controlo. Verificar rejeição/expiração de candidatos inválidos ou antigos.

**Impacto:** novo mecanismo distribuído de descoberta e mensagens de controlo. Pode fornecer candidatos a T01 e T02, mas não substitui a política de admissão nem o path vector das streams.

### T10 — Recuperação de fragmentos por redundância

**Cenário:** um frame fica inutilizável por perda de um fragmento, apesar de os restantes terem chegado dentro do prazo.

**Extensão delimitada:** acrescentar um fragmento de paridade XOR a cada grupo de fragmentos de dados. Se faltar exatamente um fragmento de dados e a paridade estiver disponível, reconstruí-lo antes de expirar o frame. Esta versão não promete recuperar várias perdas no mesmo grupo.

**Definir antes de implementar:**

- Identificação do grupo, quantidade de fragmentos, comprimentos originais e regras de padding para comprimentos diferentes.
- Paridade com o mesmo limite de datagrama/MTU; contabilizar headers e o aumento de tráfego.
- Limites de memória/CPU, expiração, duplicados e validação de índices/comprimentos antes de alocar buffers.
- Execução na origem e no cliente; manter os relays a reencaminhar os fragmentos segundo a árvore, sem descodificar JPEG.
- Versão/capacidade de protocolo explícita. Clientes sem suporte não podem interpretar paridade como dados de vídeo.

**Prova:** comparar sem/com redundância para as mesmas condições, incluindo perda isolada e perdas em rajada. Medir frames apresentados, reconstruções, prazo de apresentação, memória e banda adicional. Mostrar também o cenário em que a redundância não recupera a perda ou piora a congestão.

**Impacto:** codec UDP, fragmentação, fonte e reconstituição de frames. É um extra distinto de suportar perda/reordenação com descarte de frames incompletos, já previsto na base.

## Grupo B — Extras de apresentação e operação

Estas opções podem apoiar a defesa, mas devem ser votadas separadamente dos mecanismos do grupo A. Não substituir os testes nem apresentar uma funcionalidade já exigida como extra.

| ID | Extra complementar | Âmbito proposto | O que não acrescenta por si só |
|---|---|---|---|
| S01 | Visualização gráfica da overlay | Mostrar ligações, árvores por stream e mudanças a partir de snapshots | Não descobre vizinhos nem recupera falhas |
| S02 | Interface de diagnóstico mais rica | Pesquisa de nós, filtros, histórico e detalhe de métricas | Não melhora a seleção ou a entrega se as políticas forem iguais |
| S03 | Lançador de cenários | Selecionar perfis e iniciar/parar os processos com uma interface conveniente | Não equivale a admissão dinâmica distribuída nem a reparação da overlay |
| S04 | Reprodução visual de uma execução | Percorrer uma linha temporal de eventos já registados | Não demonstra comportamento em tempo real sem uma execução real correspondente |
| S05 | Personalização do player | Organização do catálogo, atalhos e apresentação de estado | Não melhora perdas, capacidade ou recuperação |

Logs, comandos reproduzíveis, resultados medidos, seleção/reprodução de streams e testes CORE pertencem ao trabalho já esperado. O extra nestas propostas é a conveniência ou apresentação adicional. Experiências comparativas rigorosas são trabalho técnico de avaliação; não se tornam superficiais por produzirem gráficos. Em S01/S04, a componente complementar é a representação desses dados.

## Fronteiras a especificar antes do código afetado

| Fronteira conceptual proposta | Responsabilidade | Limite |
|---|---|---|
| Descoberta de candidatos | Obter endpoints iniciais/de reserva ou aprendidos | Conhecer não significa autorizar |
| Política de admissão | Autorizar identidades e ligações | O handshake continua a validar ambos os extremos |
| Gestão das ligações | Ligar, validar, deduplicar e reconectar | Não escolhe árvores de vídeo |
| Observações de saúde | Publicar falhas/progresso com origem e validade | Não chamar RTT a perda UDP ou capacidade |
| Política de recuperação | Escolher a ação perante um incidente | Uma troca usa sempre ativação e confirmação coerentes |
| Estado publicado | Expor snapshots imutáveis e revisões | Um único proprietário decide cada estado mutável |

Prever estas fronteiras não significa implementar já todas as estratégias nem criar um sistema genérico de plugins. Reutilizar as separações existentes entre overlay, monitoring, routing e streaming.

### Contratos e invariantes comuns

- Separar identidade lógica, endereço de contacto, instância de execução e ligação. IP não deve substituir indiscriminadamente estas identidades.
- Distinguir candidatos conhecidos, vizinhos autorizados, ligações confirmadas, rotas candidatas e ramos ativos.
- Permitir ciclos na overlay; impedir ciclos nos caminhos e nas árvores ativas.
- Associar eventos e respostas à identidade/instância/ligação e revisão relevantes, rejeitando estado antigo.
- Definir quem pode alterar cada estado e como serializar alterações concorrentes.
- Limitar caches, filas, ligações, negociações, retries e tarefas agendadas. Dar prazo e cancelamento a operações pendentes.
- Preservar bytes e fixtures v1 existentes. Se os extras exigirem mensagens/campos novos, especificar uma versão/extensão e compatibilidade antes de os adicionar; um parâmetro de configuração não altera o protocolo.
- Validar opções à entrada. Uma opção sem implementação deve falhar explicitamente, não fingir suporte.
- Usar relógio monotónico para intervalos e relógio controlável nos testes. Registar motivo, estado anterior/novo e duração da recuperação.
- Nenhum extra deve transformar implicitamente o bootstrap em controlador permanente.

## Seleção em reunião e preparação

As opções abaixo são alternativas para discussão; não representam seleção ou atribuição:

| Foco possível | Extras a discutir | Razão técnica |
|---|---|---|
| Overlay autónoma | T01 + T09; T02 como extensão | Aprender candidatos, reparar o grafo e admitir participantes |
| Continuidade do serviço | T03 + T04; T05 como complemento | Recuperar o vídeo e preparar retiradas sem instabilidade recorrente |
| Entrega sob recursos limitados | T07 + T10 | Gerir admissão e avaliar recuperação de perdas sem ignorar o custo da redundância |
| Disponibilidade das origens | T08; T06 como complemento de arranque | Tratar falhas em origens e no contacto inicial, que são problemas distintos |

Preferir um foco coerente e limitar a seleção à capacidade da equipa. Por exemplo, T10 aumenta o tráfego que T07 tem de contabilizar; T08 pode alterar identidades usadas por vários outros componentes. Não somar as propostas como se fossem independentes.

### Ficha a preencher para cada candidato

| Campo | Decisão da reunião |
|---|---|
| ID e variante exata | Por preencher |
| Estado: selecionado / reserva / rejeitado / precisa de estudo | Por preencher |
| Problema e melhoria além da base obrigatória | Por preencher |
| Limites do comportamento prometido | Por preencher |
| Cenário CORE e métricas de sucesso | Por preencher |
| Mensagens, estados e contratos afetados | Por preencher |
| Dependências e incompatibilidades com outros extras | Por preencher |
| Autor, revisor e esforço estimado pela equipa | Por preencher |
| Decisões a fechar antes da primeira tarefa afetada | Por preencher |

Não marcar todos os candidatos como selecionados por defeito. Registar primeiro a escolha e as exclusões; depois produzir a especificação e o impacto nas tarefas dos extras selecionados.

Escolher um conjunto finito antes de desenvolver. Para cada extra selecionado, fechar: cenário/limites, origem da informação, máquina de estados, mensagens/erros, parâmetros, proprietário de estado e prova CORE com critérios de sucesso. A seguir avaliar impacto no ADD, tarefas e dependências; só então implementar incrementalmente.

### Onde o planeamento é necessário primeiro

| Trabalho planeado | Extras que precisam de ser considerados antes |
|---|---|
| TASK-032 — configuração operacional | Parâmetros dos extras selecionados; sem aceitar opções ainda sem comportamento |
| TASK-006 — descoberta inicial | Lista de bootstraps e revisão de configuração, se selecionadas |
| TASK-007 — vizinhanças | Candidatos de reserva, admissão, reparação, drenagem e quarentena |
| Descoberta de candidatos, se selecionada | T09 antes de o consumir como fonte de candidatos de T01/T02 |
| TASK-010–012 — saúde, rotas e anúncios | Eventos de saúde e retirada/drenagem; identidade de conteúdo se houver réplicas |
| TASK-014–017 — ativação e recuperação | Capacidade, recuperação UDP e coordenação das trocas |
| TASK-019–024 — vídeo | Progresso por sessão; continuidade temporal se houver réplicas; grupos/paridade de T10 |

Rever esta proposta não exige implementar agora componentes de fases futuras. Exige fechar os contratos que atravessam essas fases e testar cada incremento quando as dependências estiverem disponíveis.
