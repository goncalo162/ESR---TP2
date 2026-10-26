# Riscos, hipóteses e decisões

## Estado da informação

Não há bloqueio à elaboração deste plano nem ao início futuro de TASK-001. Existem dependências que bloquearão tarefas específicas caso não sejam resolvidas a tempo. Todas as tarefas começam TODO, incluindo as que têm gates; BLOCKED só será usado durante execução quando a condição efetivamente impedir trabalho.

O enunciado tem precedência sobre documentação antiga. Escolhas deste plano podem ser revistas por ADR, desde que continuem a satisfazer as obrigações. Uma hipótese não equivale a resposta do docente.

## Ambiguidades e questões a resolver

| ID | Informação / hipótese assumida | Impacto, responsável e gate |
|---|---|---|
| H-01 | O objetivo menciona áudio/vídeo/texto; a lista mínima exige vídeo. Hipótese: não são exigidos três subsistemas multimédia separados | Planear vídeo, sem áudio/texto novos. TASK-002 verifica se há instrução docente adicional. Se os três forem exigidos, rever requisitos e tarefas antes de implementar essa extensão |
| H-02 | Fonte/formatos docentes e movie.Mjpeg ainda não inspecionados | TASK-002 verifica acesso cedo; TASK-020 exige inspeção real ou alternativa discutida com docente. Bloqueia leitor/reprodução se faltar recurso; não bloqueia TCP/rotas |
| H-03 | X vizinhos não tem valor único; três é exemplo | Configurar grau por nó, folhas com 1, relays com mais. Não prometer X links ativos se vizinhos ainda não existem; mostrar desejado/ativo. TASK-003/007 |
| H-04 | Um servidor/fluxo no exemplo contradiz mínimo de dois servidores × dois fluxos | Aplicar mínimo explícito e modelar tudo por stream desde início. Não é necessário esperar esclarecimento para cumprir o mínimo mais abrangente |
| H-05 | Não há limiares numéricos de atraso, FPS, recuperação, N ou banda | Usar metas técnicas de docs/09 e medir. Não chamar incumprimento do PDF à mera falha de uma meta proposta |
| H-06 | Turno PL, grupo, prazos e datas de avaliações não fornecidos | TASK-009/018 confirma avaliações; TASK-030 exige x/y reais; TASK-031 exige prazo e confirmação da submissão. Não inventar nomes finais ou datas |
| H-07 | Não se especifica se servidores alojam o mesmo conteúdo | Quatro UUID distintos, dois vídeos diferentes por origem; não há failover de conteúdo entre fontes. Falha de S1 torna A/B indisponíveis, preserva C/D |
| H-08 | Formato artigo obrigatório, LNCS recomendado | Adotar LNCS e contar todas as páginas até 12. Sem autorização não excluir referências ou anexos da contagem |
| H-09 | Figura 1 e cenários são sugestões, topologias XML são underlay | Criar cópias de cenários com terminais e mapear explicitamente overlay. Uso de routers para apps é possível no CORE, mas adota-se recomendação de terminais |
| H-10 | Não há esquema de protocolo, exigência RTP ou de JSON | Usar contratos próprios de docs/07. Se a equipa docente exigir compatibilidade externa, rever contrato antes da TASK-019 |

Perguntas para quando for necessário: há instrução docente adicional sobre H-01/H-10? Onde obter os exemplos e vídeos autorizados se o URL não estiver disponível? Quais são turno, grupo e datas reais? Estas perguntas não têm de parar o planeamento; os gates acima evitam afirmar decisões ou recursos confirmados sem evidência.

## ADR-001 — Manter Java 21 e Maven

Estado: adotada no plano. Contexto: linguagem é livre no PDF, mas o repositório já prepara pacote Java/POM release 21. Decisão: manter essa base e usar biblioteca padrão, JUnit para testes. Alternativas: Python simplifica prototipagem, mas descarta preparação sem benefício obrigatório. Consequências: verificar JDK dentro de CORE, suporte java.desktop e runtime não headless para a demonstração visual. Responsáveis: TASK-001/025.

## ADR-002 — TCP de controlo e UDP de dados

Estado: adotada no plano. Contexto: o PDF permite TCP ou UDP e exige vídeo com objetivo de tempo real. Decisão: ligações TCP persistentes para sinalização e datagramas UDP limitados para vídeo. Alternativa: tudo TCP é mais simples de transportar, mas retransmissão/filas podem propagar atraso a clientes; tudo UDP exige fiabilidade de controlo própria. Consequências: dois endpoints por nó, fragmentação e descarte de frames, nenhum compromisso de entrega sem perdas. Reavaliar MTU e filas por evidência, sem converter o protocolo a RTP por nome. TASK-004/005/019/021/022.

## ADR-003 — Bootstrap por grafo permitido

Estado: adotada no plano. Decisão: estratégia 2 sugerida, registo e vizinhos definidos num ficheiro simétrico. Estado das conexões e rotas permanece distribuído. Alternativas: vizinhos manuais na CLI complicam arranque, descoberta totalmente dinâmica introduz complexidade desnecessária. Consequência: grafo não muda arbitrariamente durante execução; nós configurados podem entrar/sair. Novos IDs exigem edição/recarregamento planeado do bootstrap e nova consulta dos afetados, fora do fluxo básico. O requisito de entrada dinâmica é satisfeito por processos configurados que entram sem reiniciar a overlay. Bootstrap indisponível não interrompe nós ligados, mas impede descoberta inicial de novos processos. TASK-006/007/008.

## ADR-004 — Anúncios com path vector e métrica RTT

Estado: adotada no plano. Decisão: anúncio periódico por fluxo/origem, candidatos por vizinho, vetor de caminho, soma de RTT EWMA e desempate por saltos/ID. Alternativas: contagem de saltos não reage a congestão/atraso com a mesma informação; link-state global e Dijkstra são mais complexos. Um booleano seen por anúncio eliminaria uma melhoria tardia e não é suficiente. Consequências: paths e cache limitados, anúncio de melhoria, prefixo validado em ACTIVATE e histerese em mudanças de parent. RTT TCP não é medição de banda disponível nem atraso exato de vídeo. TASK-010–017.

## ADR-005 — Árvores sob pedido com leases e ACK

Estado: adotada no plano. Decisão: um parent confirmado por fluxo, conjunto de downstream e consumidores locais, ACK assíncrono, IDs/revisões e leases. Alternativa: contar clientes finais a montante duplica tráfego e simplificação sem leases deixa ramos após crash. Consequências: ativação provisória, poda após última procura, tratamento de ACK/cancelamento tardio e período transitório de troca. Não há eleição de fonte nem subscrição global central. TASK-014–017.

## ADR-006 — MJPEG simples e pacote próprio

Estado: adotada com gate de formato H-02. Decisão: seguir a estratégia sugerida de frames independentes e reprodução Swing; vídeo é opaco nos relays. Não declarar RTP/RTSP: cabeçalho de 64 bytes proposto é protocolo próprio. Alternativas: RTP completo exige especificação/compatibilidade que o PDF não impõe; codecs interframe e FFmpeg aumentam dependências. Consequências: banda mais alta, fragmentação, perda de um fragmento descarta frame, quatro fontes distintas e formato de ficheiro confirmado antes do leitor. TASK-019–024.

## ADR-007 — Propriedades e binário sem dependências runtime

Estado: adotada no plano. Decisão: configuração .properties e codec DataInputStream/ByteBuffer; memória apenas para estado operacional. Alternativa: JSON com biblioteca externa é válida, mas não necessária; o JSON vazio existente não impõe contrato. Consequências: topology.properties planeado, strings UTF-8 com comprimentos explícitos e sem Java ObjectInputStream. Alteração face ao documento antigo é registada, não migração de ficheiro funcional. TASK-002–004.

## ADR-008 — Testes incrementais e CORE na aceitação

Estado: adotada no plano. Decisão: unitários e integração desde a primeira fase; três cenários CORE após integração local; relatório baseado em dados reais. Alternativa: deixar tudo para final esconde falhas de protocolo. Consequência: marcos intermédios são demonstrações com escopo explícito e não substituem testes finais. Tarefas de relatório/submissão não produzem funcionalidades novas.

## Registo de riscos

| Risco | Probabilidade / impacto | Mitigação e evidência |
|---|---|---|
| Recurso docente indisponível | média / alta | TASK-002 verifica cedo; TASK-020 bloqueia leitor até formato real ou alternativa aprovada |
| Quatro aliases do mesmo vídeo | média / alta | Conteúdo/hash/inspeção visual por origem em V-020/024/027 |
| Loop na alteração de parent | média / alta | Path vector + prefixo na ativação, candidatos válidos, revision e testes V-012/014/016 |
| Supressão de rota melhor pelo flooding | média / alta | Reenvio de melhoria e fixture de chegada tardia V-012 |
| Oscilação por RTT | alta / média | EWMA, histerese e permanência; V-017/026 |
| Ramos fantasma | média / alta | Leases, cancelamento idempotente e callbacks por conexão; V-015 |
| Congestão/frames muito grandes | alta / média | 1200 bytes, limites, descarte, bitrate medido e capacidade calculada; V-019/022/028 |
| Runtime CORE/GUI falha | média / alta | Verificar JDK, arquitetura, display e volume em TASK-025; testes headless não substituem GUI |
| Confundir underlay e overlay | alta / alta | Terminais, mapa explícito, falhas separadas; V-025/026 |
| Bootstrap como ponto único de descoberta | média / média | Nós ligados continuam; retries e diagnóstico; redundância não é requisito |
| Fonte única por stream morre | média / alta | Estado UNAVAILABLE explícito, isolamento entre fontes; não prometer failover de conteúdo |
| Prazo e duas avaliações desconhecidos | alta / alta | Confirmar calendário e reservar marcos; sequência por dependências, sem datas inventadas |
| Entrega irreproduzível | média / alta | ZIP extraído limpo, dependências/documentação e verificação de media; V-030 |
| Desvio entre documentos | média / alta | Rever requisitos, ADR, contratos, tarefas e rastreabilidade em cada alteração |

## Diferenças face ao plano antigo

Não tratar Java 21, RTP, GUI específica, FPS=25, LNCS ou X≥3 como obrigações do enunciado. LNCS é recomendação, limite de páginas é obrigatório. O suporte multi-stream é modelado desde a configuração, não acrescentado a tabelas com chave única no fim. O pacote próprio tem fragmentação explícita. A alternativa é guardada por vizinho e a troca de upstream inclui ACK/poda. A leitura de movie.Mjpeg fica dependente dos bytes reais, em vez de presumir um container.
