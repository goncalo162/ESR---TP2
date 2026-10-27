# Definition of Done

O estado DONE exige evidência. Documentação, um ficheiro vazio, compilação com zero testes ou presença de artefacto antigo não provam comportamento implementado. Não existe declaração de conformidade final com base apenas neste plano.

## Tarefa

- Dependências em DONE, ou exceção de execução justificada e aprovada que não viole requisito; nunca fingir que um pré-requisito existe.
- Para TASK-005–031: revisor indicado aceita a versão final e alterações estão integradas na base comum antes de DONE; dependências de outras branches só contam após integração. Registar autor/revisor, versão e evidência segundo a [coordenação](team/00-COORDINATION.md). Esta regra não reabre a fase 1 já concluída.
- Objetivo e todos os critérios da tarefa cumpridos; alterações limitadas ao seu âmbito.
- Testes/verificações previstos executados e resultados reais registados. V-001 pode não conter testes funcionais; essa exceção é explícita e não se estende às tarefas seguintes.
- Casos limite relevantes cobertos, erros controlados e recursos libertados. Não introduzir regressões nas funcionalidades afetadas.
- Contratos, decisões e comandos atualizados se a implementação os alterou.
- Estado no ficheiro da tarefa e em docs/15 coerente; requisitos e evidências em docs/13 atualizados. Sem bloqueios ocultos.

Uma tarefa documental usa revisão/verificação de artefactos, não testes unitários artificiais. TASK-031 requer confirmação humana da submissão para DONE; preparar checklist não a conclui.

## Funcionalidade

Todos os requisitos associados têm componentes implementados, tarefas DONE e oráculos executados. Percurso nominal e erros essenciais verificados. Configuração e operação documentadas. Para vídeo, sink headless valida transporte/reassemblagem, mas a reprodução exige janela e conteúdo identificável. Para recuperação, a prova identifica caminho alternativo até à mesma origem, sem pressupor conteúdo replicado.

## Fase

Todas as suas tarefas concluídas, entregável verificável, teste de integração da fase executado e critérios específicos no ficheiro de fase cumpridos. O estado intermédio deve ser utilizável dentro do seu âmbito. Não antecipar fases seguintes para alegar a conclusão da fase atual. Há um ponto de retoma, riscos pendentes e próxima tarefa registados.

## Solução tecnicamente concluída

- RF-001–015, RNF-001–005 e RC-001 satisfeitos e com evidências; recomendações adotadas implementadas ou revista a decisão sem afetar obrigações.
- Dois processos de servidor, cada um com pelo menos dois vídeos distintos, quatro UUID, clientes simultâneos, árvore sem cópias redundantes no tronco em estado estável.
- Rotas por fluxo, alternativas, RTT real e adaptação a falhas/condições; sem caminho, indisponibilidade explícita.
- Vídeo reproduzido, pacing/buffers limitados e erros/perdas tratados; metas numéricas avaliadas com limitações transparentes.
- Testes incrementais e cenários CORE relevantes executados; relatório de evidências inclui resultados negativos e apreciação crítica.

## Projeto entregue

Além da conclusão técnica: artigo até 12 páginas, ZIP/RAR permitido com código e instruções reproduzíveis, nomes com turno/grupo reais, marcos intermédios e defesa preparados conforme docente. A equipa confirma upload na troca de ficheiros do grupo Blackboard e a versão submetida é a validada. Requisitos de entrega RC-002–007 só passam a satisfeitos com os seus artefactos e confirmações.

Datas não fornecidas, recurso não obtido, teste não executado e upload não confirmado são pendências, nunca sucesso presumido. Nenhuma submissão é feita neste planeamento.
