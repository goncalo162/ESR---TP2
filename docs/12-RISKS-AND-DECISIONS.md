# Riscos, hipóteses e decisões

> Arquitetura normativa: [add.md](../add.md). Requisitos consolidados: [specs.md](../specs.md). Este documento mantém detalhe técnico/proveniência e deve ser interpretado pela precedência do ADD; não é uma autoridade arquitetural concorrente.

## Estado da informação

Estado atualizado em 02-11-2026: TASK-001–005 estão DONE; TASK-006–031 continuam TODO. Não há bloqueio documental ao planeamento da continuação. Existem dependências que bloquearão tarefas específicas caso não sejam resolvidas a tempo; BLOCKED só será usado durante execução quando a condição efetivamente impedir trabalho. A próxima implementação é TASK-006, após verificar a base existente.

O enunciado tem precedência sobre documentação antiga. Escolhas deste plano podem ser revistas por ADR, desde que continuem a satisfazer as obrigações. Uma hipótese não equivale a resposta do docente.

## Ambiguidades e questões a resolver

| ID | Informação / hipótese assumida | Impacto, responsável e gate |
|---|---|---|
| H-01 | O objetivo menciona áudio/vídeo/texto; a lista mínima exige vídeo. Hipótese mantida: não são exigidos três subsistemas separados | TASK-002: utilizador respondeu «Sem indicações adicionais conhecidas». Mantém-se vídeo; isto não é confirmação formal do docente. Rever plano se surgir instrução adicional |
| H-02 | TASK-002 confirmou GET 200 do ZIP e formato de movie.Mjpeg: cinco dígitos ASCII + JPEG; 500 frames, cópias idênticas | [Hashes e quatro conteúdos identificados](../experiments/task-002-contract-review.md). TASK-020 prepara/valida os quatro media; TASK-030 verifica créditos/redistribuição, não explícitos no ZIP docente. Não bloqueia TASK-003 |
| H-03 | X vizinhos não tem valor único; três é exemplo | Configurar grau por nó, folhas com 1, relays com mais. Não prometer X links ativos se vizinhos ainda não existem; mostrar desejado/ativo. TASK-003/007 |
| H-04 | Um servidor/fluxo no exemplo contradiz mínimo de dois servidores × dois fluxos | Aplicar mínimo explícito e modelar tudo por stream desde início. Não é necessário esperar esclarecimento para cumprir o mínimo mais abrangente |
| H-05 | Não há limiares numéricos de atraso, FPS, recuperação, N ou banda | Usar metas técnicas de docs/09 e medir. Não chamar incumprimento do PDF à mera falha de uma meta proposta |
| H-06 | Turno PL, grupo, prazos e datas de avaliações não fornecidos | TASK-009/018 confirma avaliações; TASK-030 exige x/y reais; TASK-031 exige prazo e confirmação da submissão. Não inventar nomes finais ou datas |
| H-07 | Não se especifica se servidores alojam o mesmo conteúdo | Quatro UUID distintos, dois vídeos diferentes por origem; não há failover de conteúdo entre fontes. Falha de S1 torna A/B indisponíveis, preserva C/D |
| H-08 | Formato artigo obrigatório, LNCS recomendado | Adotar LNCS e contar todas as páginas até 12. Sem autorização não excluir referências ou anexos da contagem |
| H-09 | Figura 1 e cenários são sugestões, topologias XML são underlay | Reutilizar os XML já existentes e mapear explicitamente os processos. Routers Linux podem alojar relays no CORE; terminais são recomendados pelo PDF, sem obrigar a recriar os cenários. Variantes apenas por necessidade demonstrada |
| H-10 | Não há esquema obrigatório, exigência RTP ou JSON; utilizador sem indicações adicionais conhecidas em TASK-002 | Contratos próprios v1 mantidos e clarificados com fixtures. Se surgir imposição docente de compatibilidade, rever antes da implementação afetada, sem chamar RTP ao pacote próprio |

Questões atualizadas pela TASK-002: H-01/H-10 foram consultadas com o utilizador, sem indicações adicionais conhecidas, e o URL docente esteve acessível nesta execução. Continuam pendentes a preparação/licenciamento dos media finais e turno, grupo e datas reais. Estas perguntas não têm de parar o planeamento; os gates acima evitam afirmar decisões ou recursos confirmados sem evidência.

## ADR-001 — Manter Java 21 e Maven

Estado: adotada no plano. Contexto: linguagem é livre no PDF, mas o repositório já prepara pacote Java/POM release 21. Decisão: manter essa base e usar biblioteca padrão, JUnit para testes. Alternativas: Python simplifica prototipagem, mas descarta preparação sem benefício obrigatório. Consequências: verificar JDK dentro de CORE, suporte java.desktop e runtime não headless para a demonstração visual. Responsáveis: TASK-001/025.

## ADR-002 — TCP de controlo e UDP de dados

Estado: adotada no plano. Contexto: o PDF permite TCP ou UDP e exige vídeo com objetivo de tempo real. Decisão: ligações TCP persistentes para sinalização e datagramas UDP limitados para vídeo. Alternativa: tudo TCP é mais simples de transportar, mas retransmissão/filas podem propagar atraso a clientes; tudo UDP exige fiabilidade de controlo própria. Consequências: dois endpoints por nó, fragmentação e descarte de frames, nenhum compromisso de entrega sem perdas. Reavaliar MTU e filas por evidência, sem converter o protocolo a RTP por nome. TASK-004/005/019/021/022.

## ADR-003 — Bootstrap por grafo permitido

Estado: Substituída por ADR-013 no ADD; esta descrição conserva a proveniência da escolha e a sua correção. Decisão: estratégia 2 sugerida, apenas consulta inicial dos vizinhos definidos num ficheiro simétrico. Não há inscrição de membros, registo vivo, lease, renovação, monitorização ou avisos de saída no bootstrap. Estado das conexões e rotas permanece distribuído. Alternativas: vizinhos manuais na CLI ou descoberta totalmente dinâmica. Consequência: grafo não muda arbitrariamente durante execução; nós configurados podem entrar/sair. Novos IDs exigem edição/recarregamento planeado da configuração e nova consulta dos afetados, fora do fluxo básico. O requisito de entrada dinâmica é satisfeito por processos configurados que entram sem reiniciar a overlay. Bootstrap indisponível não interrompe nós que já obtiveram a lista, incluindo a sua reconexão direta, mas impede descoberta inicial de novos processos. TASK-006/007/008.

## ADR-004 — Anúncios com path vector e métrica RTT

Estado: adotada no plano. Decisão: anúncio periódico por fluxo/origem, candidatos por vizinho, vetor de caminho, soma de RTT EWMA e desempate por saltos/ID. Alternativas: contagem de saltos não reage a congestão/atraso com a mesma informação; link-state global e Dijkstra são mais complexos. Um booleano seen por anúncio eliminaria uma melhoria tardia e não é suficiente. Consequências: paths e cache limitados, anúncio de melhoria, prefixo validado em ACTIVATE e histerese em mudanças de parent. RTT TCP não é medição de banda disponível nem atraso exato de vídeo. TASK-010–017.

## ADR-005 — Árvores sob pedido com leases e ACK

Estado: adotada no plano. Decisão: um parent confirmado por fluxo, conjunto de downstream e consumidores locais, ACK assíncrono, IDs/revisões e leases. Alternativa: contar clientes finais a montante duplica tráfego e simplificação sem leases deixa ramos após crash. Consequências: ativação provisória, poda após última procura, tratamento de ACK/cancelamento tardio e período transitório de troca. Não há eleição de fonte nem subscrição global central. TASK-014–017.

## ADR-006 — MJPEG simples e pacote próprio

Estado: adotada; perfil de ficheiro confirmado em TASK-002, preparação dos restantes conteúdos e redistribuição ainda com gates H-02. Decisão: seguir a estratégia sugerida de frames independentes e reprodução Swing; vídeo é opaco nos relays. Não declarar RTP/RTSP: cabeçalho de 64 bytes proposto é protocolo próprio. Alternativas: RTP completo exige especificação/compatibilidade que o PDF não impõe; codecs interframe e FFmpeg aumentam dependências. Consequências: banda mais alta, fragmentação, perda de um fragmento descarta frame, quatro fontes distintas e formato de ficheiro confirmado antes do leitor. TASK-019–024.

## ADR-007 — Propriedades e binário sem dependências runtime

Estado: adotada no plano. Decisão: configuração .properties e codec DataInputStream/ByteBuffer; memória apenas para estado operacional. Alternativa: JSON com biblioteca externa é válida, mas não necessária; o JSON vazio existente não impõe contrato. Consequências: topology.properties planeado, strings UTF-8 com comprimentos explícitos e sem Java ObjectInputStream. Alteração face ao documento antigo é registada, não migração de ficheiro funcional. TASK-002–004.

## ADR-008 — Testes incrementais e CORE na aceitação

Estado: adotada no plano. Decisão: unitários e integração desde a primeira fase; três cenários CORE após integração local; relatório baseado em dados reais. Alternativa: deixar tudo para final esconde falhas de protocolo. Consequência: marcos intermédios são demonstrações com escopo explícito e não substituem testes finais. Tarefas de relatório/submissão não produzem funcionalidades novas.

## Registo de riscos

| Risco | Probabilidade / impacto | Mitigação e evidência |
|---|---|---|
| Recurso docente voltar a ficar indisponível ou media final por preparar | média / alta | TASK-002 confirmou acesso/hash/perfil; repetir obtenção quando necessário. TASK-020 prepara os restantes conteúdos e valida bytes/JPEG; TASK-030 trata distribuição |
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

## ADR-009 — Fixar contratos e perfil MJPEG antes dos codecs

Estado: adotada na TASK-002, 26-10-2026. Contexto: ainda não existe codec de produção; os comprimentos e os exemplos têm de poder servir de oráculo independente. Decisão: corpo mínimo TCP de 37 bytes, UDP com 64 bytes de cabeçalho e 1136 de payload máximo, restrições de identidade/enumeração explícitas, 45 pares binário/hex com manifesto e resultados esperados. Separar validade de formato de autorização/estado operacional.

Corrigida uma contradição do invariante 1 de 06: a época pertence ao arranque do servidor e pode ser comum às suas streams; o estado de cada StreamKey continua isolado. Perfil do recurso docente confirmado em bytes e exemplos: cinco dígitos ASCII + L bytes JPEG, limite de 99999 neste perfil, distinto do máximo UDP de 2 MiB. Não se implementou leitor/codec.

Alternativa rejeitada: produzir golden bytes com o próprio encoder ou presumir que .Mjpeg significa JPEG sem prefixos. Consequências: TASK-004/019 consomem fixtures fixas; TASK-020 usa o perfil confirmado e prepara os conteúdos distintos identificados. [Revisão, comandos, hashes e gates](../experiments/task-002-contract-review.md).

## ADR-010 — Carga estrita e validação conjunta da configuração

Estado: adotada na TASK-003, 27-10-2026. Decisão: usar Reader UTF-8 e sintaxe Properties com deteção de chaves duplicadas depois dos escapes; rejeitar desconhecidas e inaplicáveis ao papel. Usar records e cópias imutáveis. Erros contêm ficheiro/propriedade e são agregados numa exceção sem estado parcial. Não preencher portas implicitamente: os perfis têm endpoints explícitos para tornar colisões verificáveis.

load(path, expectedRole) valida um nó e ficheiros dependentes; loadDeployment(paths) valida o conjunto completo contra o grafo e verifica UUID globalmente únicos/associados à origem. Sem conhecer os outros perfis, a carga individual não afirma unicidade global. A validação offline usa apenas sintaxe e normalização lexical de hosts/paths, sem DNS, sockets ou bytes dos vídeos. Reachability, symlinks reais e conteúdos são gates posteriores. Limitar grafo a 64 IDs e catálogos a 256 streams por origem; estes limites de implementação não são números impostos pelo PDF.

O enum NodeRole foi preenchido nesta tarefa por ser necessário à configuração tipada, sem implementar OverlayNode ou bootstrap de rede. Os exemplos locais têm cinco perfis, duas origens com dois UUID cada, grafo simétrico e portas distintas. A partir desta tarefa Surefire usa failIfNoTests=true; a cópia do POM sem testes falhou como esperado. [Resultados e 65 testes](../experiments/task-003-configuration-validation.md).

## ADR-011 — Reutilizar as topologias CORE existentes

Estado: adotada após esclarecimento do utilizador em 27-10-2026. Os XML cenario1/2/3 em CORE/topologies são a base de integração. O topology.properties criado na TASK-003 é um perfil local de teste com loopback, não substitui nem importa esses XML. CORE lê o XML para criar equipamentos/links/IP; a aplicação lê propriedades para definir papéis e vizinhanças overlay.

A proposta inicial de criar ott-cenario*.xml por defeito foi retirada. Reutilizar os equipamentos/IP existentes, incluindo relays nos routers Linux quando adequado à bancada; só propor adaptação mínima se uma limitação concreta impedir um requisito. Um PC chamado Cliente pode alojar outro papel Java, incluindo a segunda origem. TASK-025–027 foram ajustadas, mantendo-se TODO. Nenhum XML foi alterado.

Verificação estática dos XML: parsing concluído, IDs únicos, referências dos links válidas e grafo conectado nos três ficheiros. Cenario1 tem quatro equipamentos, cenario2 seis, cenario3 20 devices mais cinco networks/switches (25 equipamentos no total). Isto não valida o arranque, IP routing, reachability ou streaming no CORE; essas verificações continuam pendentes.

## ADR-012 — Codec independente do transporte e fecho da fase 1

Estado: adotada na TASK-004, 27-10-2026. O contrato original tinha 17 tipos; após ADR-013 tem 16 tipos ativos, com o código 3 retirado. IDs explícitos, payloads tipados/imutáveis e helpers de Endpoint/StreamInfo no pacote protocol. Configuração e codec partilham NodeRole, com mapeamento de rede explícito. Reusar payloads de layout igual sem implementar handlers.

Decisão: read(InputStream) sinaliza EOF limpo com Optional.empty; truncatura de transporte é EOFException; corpo recebido com campos inválidos é ProtocolException. Prefixo e u32 são unsigned; validar comprimentos antes de alocar e saída antes de escrever no stream do chamador. Utilizar UTF-8 estrito, nunca modified UTF-8. O chamador mantém flush/close e serialização dos envios.

O codec valida formato/domínio, não autorização, ciclos ou ativação de rotas. As fixtures permanecem independentes e inalteradas. Validação final: 65 testes de configuração + 66 de codec/integração local, todos aprovados. PHASE-01 fecha a base técnica; o transporte da Etapa 0 e a overlay da Etapa 1 do enunciado continuam nas fases seguintes. [V-004](../experiments/task-004-codec-validation.md), [material para relatório](PHASE-01-REPORT-NOTES.md). Nenhum XML CORE foi alterado.

## ADR-013 — Bootstrap exclusivamente para descoberta inicial

Estado: decisão do utilizador de 27-10-2026, já refletida no contrato e codec; registo consolidado em 02-11-2026. Contexto: referências a esta decisão existiam sem uma secção própria neste índice.

Decisão: REGISTER/REGISTER_OK são consulta/resposta do grafo imutável configurado. Devolver também vizinhos offline, com instanceId zero. Não guardar disponibilidade, exclusividade de IDs vivos, leases, renovações, sondas ou avisos de saída no bootstrap. Código 3 retirado; rejeitar os bytes do antigo REGISTER_OK com lease. A descoberta inicial termina depois de a resposta ser completamente escrita. Vizinhança, instâncias, reconexão e falhas pertencem aos pares diretos; leases de streams pertencem ao routing.

Alternativa rejeitada: registo central de membros com renovação periódica. Consequências: nós que conservam a lista reconectam sem bootstrap; processos sem lista inicial dependem dele. Esta decisão não implementa BootstrapServer ou NeighborManager. [Correção e revalidação do codec](../experiments/bootstrap-discovery-correction.md). TASK-006/007/008/009 continuam por executar.

## ADR-014 — Transporte TCP assíncrono com recursos limitados

Estado: implementada e revista em TASK-005, 27-10-2026; registo consolidado em 02-11-2026 a partir da evidência existente. Decisão: um leitor e um escritor por conexão, fila limitada de saída, dials assíncronos, callbacks ordenados por ligação e shutdown com prazo. Defaults: 256 frames por saída, 64 reservas incluindo dials, connect de 2 s e shutdown externo de 3 s. Threads virtuais não eliminam a necessidade destes limites.

send=ACCEPTED confirma apenas enfileiramento. Overflow fecha a ligação saturada; close pode descartar a fila. Não há ACK aplicacional, retries, handshake ou fecho gracioso de resposta garantidos nesta API. Alternativa rejeitada: escritas de rede bloqueantes nos handlers ou filas ilimitadas. [18 testes TCP e 149 no build histórico](../experiments/task-005-transport-validation.md). A nova preparação documental não executa novamente estes testes.

## ADR-015 — Confirmar escrita da resposta antes de fechar a descoberta

Estado: decisão de integração adotada para TASK-006 no planeamento de 02-11-2026; ainda não implementada. Contexto: TASK-006 exige resposta completa seguida de EOF, mas a API atual só confirma enfileiramento e o close imediato pode perder a resposta.

Decisão: acrescentar em TASK-006 uma operação assíncrona de confirmação de escrita/flush, concluída pelo escritor exclusivo depois de escrever o frame completo. O bootstrap encadeia a conclusão com o fecho e aplica prazo limitado a consultas/peers lentos; nunca bloqueia o callback de leitura. Falha, cancelamento, overflow ou shutdown têm de terminar as confirmações pendentes sem as deixar suspensas. O ADD fixa a API Connection.sendAndFlush(ControlMessage): CompletableFuture<Void>.

Não equiparar fila vazia a escrita concluída, nem escrita/flush a entrega remota ou ACK aplicacional. Manter a semântica de send e a compatibilidade dos bytes v1. V-006 deve incluir resposta completa seguida de EOF e falhas durante escrita; repetir os testes de transporte afetados. Esta extensão pertence a TASK-006 e não reabre TASK-005 nem inventa um resultado de validação.


## Consolidação no ADD — 02-11-2026

O [ADD](../add.md) mantém ADR-001–015 e acrescenta ADR-016–026. Este ficheiro
é proveniência histórica, não o local para criar decisões concorrentes.
1A: specs consolidado; 2A: configuração operacional em TASK-032;
3B: limitação de recuperação UDP-only; 4A: sem ferramentas/limiar percentual novos.
Nova decisão ou substituição é registada no ADD e no seu índice.
