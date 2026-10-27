# Validação final do plano

Revisão de 26-10-2026. O objeto desta validação é a documentação e a sua cobertura planeada, não a aplicação. A análise usou o enunciado integral de cinco páginas, inspeção visual da figura 1 e dos cenários, confirmação de identidade das cópias do PDF e observação do repositório.

> Registo histórico de 26-10-2026. Estado atual: TASK-001–005 DONE, PHASE-02 IN PROGRESS, 146 testes aprovados. [Progresso](15-PROJECT-PROGRESS.md), [V-005](../experiments/task-005-transport-validation.md).

## Resultados estruturais

- 56 ficheiros Markdown não vazios, sete fases e 31 tarefas.
- 35 requisitos: 15 funcionais, cinco não funcionais, sete restrições e oito recomendações/decisões adotadas.
- Todas as 27 obrigações identificadas e as oito recomendações têm tarefas e validação; nenhuma tarefa órfã.
- 31 validações únicas V-001–031, todas em estado NÃO EXECUTADO para a aplicação.
- 1301 referências relativas verificadas, incluindo âncoras de requisitos: nenhuma referência quebrada.
- Grafos de dependências de tarefas e requisitos sem ciclos; ordem TASK-001–031 válida e gates de fase alcançáveis transitivamente.
- Os 31 ficheiros de tarefa e os sete de fase contêm as 13 secções exigidas; critérios e estado TODO presentes.
- Rastreabilidade, progresso, requisitos por tarefa e dependências conferidos contra os identificadores documentados.
- Cabeçalhos e limites conferidos por aritmética: envelope mínimo 37 bytes, UDP 64 bytes, payload máximo 1136 bytes e até 1847 fragmentos/frame.
- Alterações no repositório limitadas a Markdown em docs/; nenhum ficheiro versionado existente modificado.

Foram também consultadas fontes oficiais de Java, Maven, CORE e livro para capacidades usadas em 03. A verificação automática de caminhos cobre referências locais; não afirma disponibilidade de todos os URLs externos nem do ZIP docente.

## Revisão crítica

| Questão | Resultado documental |
|---|---|
| Requisitos mínimos completos | Os dez pontos mínimos da p. 2–3 e o objetivo de vídeo estão desdobrados em RF-001–015; qualidade e entrega em RNF/RC |
| Obrigações versus sugestões | Identificadas por natureza em 02; Java, transportes separados, MJPEG, GUI e LNCS não são declarados obrigatórios pelo enunciado |
| Contradição de servidor/fluxo único | Modelo desde início para múltiplas origens/streams, cumprindo o mínimo explícito de dois servidores × dois vídeos |
| Arquitetura e ciclos | Overlay aplicacional, path vector, validação de prefixo, ACK/leases, alternativas e troca confirmada com histerese |
| Protocolo inequívoco | Tipos TCP, framing mínimo de 37 bytes, ordem/tipos de campos, erros; UDP de 64 bytes com fragmentação até 1200 bytes de aplicação |
| Falhas e limites | Partição e morte da única fonte produzem indisponibilidade; recuperação só prometida se existir caminho para a mesma fonte |
| Dependências e fases | Primeiro configuração/contrato, depois overlay, métricas/rotas, árvores, vídeo, CORE e entrega; gates explícitos, sem ciclos |
| Atomização | 31 responsabilidades com ficheiros, pré-requisitos, passos, casos limite, validação e aceitação; tarefas de integração só após componentes |
| Testes incrementais | Catálogo V-001–031 ligado a tarefas/requisitos; CORE e GUI exigem evidência real, não substituível por headless |
| Fidelidade ao repositório | Java quase todo vazio, testes/configurações vazios e artefactos antigos identificados; nenhum componente considerado funcional por existir ficheiro |
| CORE e figura 1 | Underlay distinguida da overlay; cenários copiados com terminais e mapeamento, alternativa do cenário 2 explicitada, cenário 3 com segunda origem |
| Vídeos distintos | Perfis por servidor, UUID e verificação de conteúdo; aliases do mesmo vídeo não satisfazem dois fluxos distintos por servidor |
| Relatório e submissão | Artigo até 12 páginas, ZIP permitido, nomes com x/y reais e confirmação de upload manual no grupo Blackboard |
| Retoma por IA | Guia, progresso TODO, handoff e regra de selecionar menor tarefa pronta, sem contexto de chat obrigatório |

## Ajustes resultantes da revisão

Corrigidas duas omissões de V-XXX na rastreabilidade e a contagem inicial dos placeholders Java: são 28 ficheiros vazios e um ficheiro apenas com comentário. Uniformizado o comprimento mínimo do envelope TCP em 37 bytes e explicitado bind UDP ao endpoint publicitado em nós multihomed. Separados perfis de streams por origem; a validação de JPEG corrompido pertence ao decoder/player, não à montagem de bytes. A variante proposta do cenário de seis nós está identificada. Os componentes e critérios de fecho das fases foram particularizados.

## Pendências e alcance da conclusão

A estrutura do plano tem cobertura para todas as obrigações identificadas. A execução continua toda em TODO. Recursos MJPEG reais, instruções docentes adicionais, datas, runtime/GUI CORE, turno/grupo e upload continuam pendentes nos gates de [12](12-RISKS-AND-DECISIONS.md) e [15](15-PROJECT-PROGRESS.md). Estas dependências não bloqueiam TASK-001; podem bloquear as tarefas responsáveis se não forem resolvidas a tempo.

Não foram executados build Maven, testes Java, reprodução de vídeo, CORE, instalação ou submissão. As validações automáticas desta sessão foram scripts temporários de inspeção da documentação, fora do código do projeto; não são testes funcionais. Não foram alterados ficheiros de produção nem configuração do ambiente. Os suportes auxiliares specsmd ausentes não são declarados criados ou validados.

Primeira tarefa futura: [TASK-001](tasks/PHASE-01/TASK-001.md), após pedido de implementação. A [matriz](13-REQUIREMENTS-TRACEABILITY.md) e o [guia](AI-IMPLEMENTATION-GUIDE.md) permitem seguir e retomar o plano.
