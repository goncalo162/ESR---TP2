# Apontamentos do checkpoint para retomar mais tarde

Este documento reúne os pontos anotados no checkpoint e o que deve ser tratado posteriormente na implementação e no relatório. As propostas abaixo precisam de avaliação; não representam alterações já implementadas nem decisões fechadas.

## 1. Avaliar a identificação por IP no bootstrap

Foi colocada a possibilidade de o bootstrap identificar o nó pelo **endereço IP, em vez de pelo ID do nó**.

**A tratar:**

- Esclarecer se o IP seria obtido da ligação recebida ou enviado pelo próprio nó.
- Avaliar como esse endereço seria associado à lista de vizinhos configurados.
- Verificar as consequências para vários processos no mesmo IP e para nós com várias interfaces.
- Comparar esta opção com a identificação por ID e registar a decisão e a justificação.

**Ponto em aberto:** confirmar se a intenção é apenas mudar a consulta ao bootstrap ou também alterar a identidade usada pelos nós no restante sistema.

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

**Ponto em aberto:** confirmar se “adicionar novamente” significa o regresso de um nó já configurado ou a introdução de um nó novo.

### 3.2. Criar novas arestas após uma falha catastrófica

Foi apontada a possibilidade de **criar novas ligações na overlay após uma falha catastrófica**.

**A tratar:** definir o cenário de falha e avaliar como os nós poderiam estabelecer novas vizinhanças para recuperar conectividade quando as ligações existentes deixassem de ser suficientes. Esclarecer como seriam descobertos e escolhidos os novos vizinhos.

**Ponto em aberto:** confirmar se a proposta implica criar vizinhanças que não estavam configuradas ou apenas recuperar ligações já previstas.

### 3.3. Escolher os extras antes da fase final

Foi recomendado **analisar antecipadamente os extras que poderão ser implementados**.

**A tratar:** comparar o benefício, o esforço e as dependências de cada proposta; escolher quais avançam e definir como serão demonstradas, tendo em conta o trabalho obrigatório ainda pendente.

## 4. Lista de acompanhamento

- [ ] Esclarecer e avaliar a proposta de identificação por IP no bootstrap.
- [ ] Incluir no relatório os compromissos das principais decisões técnicas.
- [ ] Clarificar o âmbito do cenário de falha e regresso de um nó.
- [ ] Clarificar o significado de criar arestas após uma falha catastrófica.
- [ ] Selecionar antecipadamente os extras a implementar e a demonstrar.
