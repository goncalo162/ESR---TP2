# Apontamentos do checkpoint para retomar mais tarde

Este documento reúne os pontos anotados no checkpoint e o que deve ser tratado posteriormente na implementação e no relatório. As secções 1–3 preservam as propostas e dúvidas originais. As decisões CP-01–03 foram aprovadas pelo utilizador em 03-11-2026 e estão esclarecidas na secção 5; não representam implementação ou testes executados.

## 1. Avaliar a identificação por IP no bootstrap

Foi colocada a possibilidade de o bootstrap identificar o nó pelo **endereço IP, em vez de pelo ID do nó**.

**A tratar:**

- Esclarecer se o IP seria obtido da ligação recebida ou enviado pelo próprio nó.
- Avaliar como esse endereço seria associado à lista de vizinhos configurados.
- Verificar as consequências para vários processos no mesmo IP e para nós com várias interfaces.
- Comparar esta opção com a identificação por ID e registar a decisão e a justificação.

**Dúvida original, resolvida em CP-01:** manter a consulta e a identidade por nodeId; não adotar consulta por IP nem substituir a identidade global.

## 2. Explicar os compromissos técnicos no relatório

Foi salientado que é necessário **compreender e explicar os trade-offs das escolhas feitas**: o que cada escolha permite ganhar, que custos introduz e que limitações mantém.

Não é necessário centrar esta discussão em valores numéricos específicos, mas os compromissos devem ficar explícitos no relatório.

**A tratar:**

- Para cada decisão relevante, indicar a motivação, a vantagem e o custo ou limitação.
- Explicar em que situações a escolha funciona bem e em quais pode ser menos adequada.
- Relacionar a explicação com o comportamento do projeto, evitando apenas enumerar tecnologias ou parâmetros.

Como sugestões para desenvolver este ponto, podem discutir-se o custo das mensagens de controlo face à rapidez de deteção de falhas e a rapidez de adaptação das rotas face à estabilidade. Estes exemplos não são novos comentários atribuídos ao checkpoint.

## 3. Extras a avaliar antecipadamente

Os apontamentos originais identificam os temas seguintes como extras. Antes de decidir a implementação, é necessário confirmar o âmbito de cada um face aos requisitos e ao que já está previsto no projeto.

### 3.1. Falha de um nó e posterior regresso

Foi referido como extra o cenário em que **um nó falha e é novamente adicionado à rede**.

**A tratar:** esclarecer o comportamento que se pretende demonstrar: reiniciar o nó, restabelecer as ligações aos vizinhos e permitir que volte a participar na distribuição.

**Dúvida original, resolvida em CP-02:** “adicionar novamente” refere-se ao regresso de um nó já configurado. Entrada tardia de um ID já previsto também permanece no âmbito; IDs desconhecidos não são admitidos.

### 3.2. Criar novas arestas após uma falha catastrófica

Foi apontada a possibilidade de **criar novas ligações na overlay após uma falha catastrófica**.

**A tratar:** definir o cenário de falha e avaliar como os nós poderiam estabelecer novas vizinhanças para recuperar conectividade quando as ligações existentes deixassem de ser suficientes. Esclarecer como seriam descobertos e escolhidos os novos vizinhos.

**Dúvida original, resolvida em CP-03:** nesta versão recuperam-se ligações e caminhos já previstos no grafo; novas vizinhanças fora do grafo ficam adiadas como extra.

### 3.3. Escolher os extras antes da fase final

Foi recomendado **analisar antecipadamente os extras que poderão ser implementados**.

**A tratar:** comparar o benefício, o esforço e as dependências de cada proposta; escolher quais avançam e definir como serão demonstradas, tendo em conta o trabalho obrigatório ainda pendente.

## 4. Lista de acompanhamento

- [x] Esclarecer e avaliar a proposta de identificação por IP no bootstrap — CP-01.
- [ ] Incluir no relatório os compromissos das principais decisões técnicas.
- [x] Clarificar o âmbito do cenário de falha e regresso de um nó — CP-02.
- [x] Clarificar o significado de criar arestas após uma falha catastrófica — CP-03.
- [x] Selecionar antecipadamente os extras — novas vizinhanças adiadas; regresso configurado já pertence ao plano.

## 5. Decisões aprovadas — 03-11-2026

O utilizador decidiu adotar as três recomendações da revisão Inception.
Esta aprovação resolve as escolhas de identidade e âmbito; não é uma aprovação
de código, resultados experimentais ou de todos os artefactos de Inception.
Referência normativa: [ADD, secção 13](../add.md#13-questões-em-aberto-e-changelog).
Análise e alternativas: [revisão do checkpoint](../memory-bank/intents/001-ott-streaming/checkpoint-feedback-review.md).

| ID | Decisão aceite | Justificação e limite |
|---|---|---|
| CP-01 | Manter nodeId como identidade estável e chave da consulta inicial ao bootstrap | Distingue processos no mesmo IP e não depende da interface escolhida. Mantém REGISTER/REGISTER_OK e validação de identidade/endpoints. |
| CP-02 | Demonstrar falha e regresso/entrada tardia de nós já configurados | Comportamento já planeado em TASK-007/016, não um novo extra. IDs fora da configuração continuam excluídos. |
| CP-03 | Recuperar ligações permitidas e escolher alternativas configuradas; adiar novas vizinhanças | Evita acrescentar descoberta e alteração dinâmica do grafo. Sem caminho válido até à origem, usa-se UNAVAILABLE. |

### Identidade por IP e leitura do envelope

O IP remoto está disponível no socket antes de ler um pedido aplicacional.
Um protocolo em que a simples ligação desencadeasse a resposta por IP poderia
dispensar REGISTER, desde que o endereço identificasse inequivocamente um nó.
Essa alternativa exigiria resolver processos no mesmo IP e múltiplas interfaces.
No transporte atual, mudar apenas a chave da pesquisa não elimina a leitura:
o pedido é descodificado antes de chegar ao handler do bootstrap.

A escolha aprovada conserva o pedido explícito, o envelope v1 e nodeId. O IP
serve como endereço de rede; não substitui a identidade lógica. O custo de
interpretar o pedido inicial é aceite; não foi medido um ganho de desempenho
que justifique alterar este contrato.

### Regresso de nós e alternativas após falha

Um nó reiniciado mantém nodeId, cria uma nova instanceId e volta a consultar o
bootstrap, pois perdeu a lista volátil de vizinhos. Um processo que continua
vivo e conserva essa lista pode reconectar diretamente com bootstrap desligado.
Reconstruir rotas e ramos exige caminhos atuais e confirmação; o regresso de
um relay não força uma troca imediata de parent. Sem upstream válido tenta-se
uma alternativa; com um upstream válido aplica-se a política de histerese.

Criar uma ligação que não esteja no grafo, mesmo entre IDs conhecidos, fica
para trabalho futuro. A escolha não promete recuperar uma partição sem caminho
nem acrescenta recuperação de falha exclusiva UDP com TCP saudável.

### Seguimento no artigo

TASK-029 continua responsável por explicar ganhos, custos, alternativas e
limitações destas escolhas, com evidência do comportamento implementado.
A tarefa de escrever/rever o artigo permanece pendente; a checklist acima
assinala apenas o fecho das decisões de planeamento.
