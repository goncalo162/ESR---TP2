# Fase 1 — Material disponível para o relatório

Atualização posterior ao fecho original: o utilizador limitou o bootstrap à localização inicial de vizinhos (ADR-013). Há agora 16 tipos ativos; código 3 retirado, REGISTER_OK sem lease e apenas com vizinhos configurados/instâncias desconhecidas. Os 45 pares de fixtures incluem 29 TCP (17 aceites e 12 rejeitados) e 16 UDP. [Revalidação: 131 testes aprovados](../experiments/bootstrap-discovery-correction.md). As referências a 17 tipos e os resultados de fecho abaixo descrevem a versão original; o relatório final deve usar o contrato atual em 07 e esta atualização.

Estado em 27-10-2026: PHASE-01 concluída no âmbito de fundações, configuração e codec. Este documento identifica texto e evidência utilizáveis no artigo futuro; não é o relatório final nem declara toda a solução implementada.

Fonte principal: [enunciado](../ESR-TP2-26-27.pdf), em particular a estrutura recomendada do relatório na p. 5 e os critérios de avaliação na p. 4.

## 1. O que foi feito em cada tarefa

| Tarefa | Entrega efetiva | Evidência |
|---|---|---|
| TASK-001 | Build Java 21/Maven, JUnit Jupiter/Surefire configurados e versões fixadas | [Baseline](../experiments/build-baseline.md), sem testes funcionais na base inicial |
| TASK-002 | Revisão dos requisitos/formatos, 45 pares binário/hex e identificação de recursos de vídeo | [Revisão](../experiments/task-002-contract-review.md); perfil de movie.Mjpeg confirmado |
| TASK-003 | Configuração UTF-8 estrita, modelos imutáveis, validação de grafo/UUID/endpoints e perfis locais | [65 testes reais](../experiments/task-003-configuration-validation.md) |
| TASK-004 | Encoder/decoder dos 17 tipos de controlo, framing/limites/UTF-8 e erros controlados | [66 testes de codec/integração](../experiments/task-004-codec-validation.md) |

Build final: 131 testes, zero falhas, erros ou skips. Testes locais de configuração e serialização, incluindo integração entre os dois componentes; ainda sem conexões entre processos.

## 2. Cobertura da estrutura recomendada do artigo — p. 5

| Secção do relatório | Material já disponível | Limite atual |
|---|---|---|
| Introdução | Contexto OTT, partilha de tráfego e requisitos em 01/02 | Atualizar com o resultado real da solução completa no fim |
| Arquitetura da solução | Componentes e separação controlo/dados em 04; configuração e codec já implementados | Overlay, monitorização, rotas, árvores e reprodução ainda são desenho proposto |
| Especificação dos protocolos — formato das mensagens | Contrato v1 em 07, tipos 1–17, ordem/tamanhos, prefixo, UTF-8, enums e limites; bytes confirmados por fixtures | Formato UDP de vídeo tem especificação/fixtures, mas o seu codec ainda não foi implementado |
| Especificação dos protocolos — interações | Fluxos de registo, anúncio, ativação, poda e falha descritos no desenho | Ainda não há handlers/conexões que executem essas interações; não apresentar como teste de rede realizado |
| Implementação | Java 21, Maven/JUnit, config/records, DTO de mensagens, codec, escolhas de dependências e parâmetros | Não descrever transporte ou streaming como código concluído |
| Testes e resultados | Metodologia, fixtures independentes, 65+66 casos, erros/fronteiras, build e integração em memória | Faltam resultados CORE, tráfego, recuperação, FPS e avaliação integrada |
| Conclusões e trabalho futuro | Base testada, decisões/riscos e próximas fases identificados | Conclusões finais e apreciação crítica global só após experiências completas |

A estrutura de secções é recomendada pelo PDF. A obrigação final é entregar artigo com até 12 páginas; LNCS é recomendado. Nenhum PDF foi escrito ou validado nesta fase, e RC-002–006 continuam pendentes.

## 3. Ligação às etapas e avaliação do enunciado

- Pág. 3, Etapa 0: suporte à escolha de linguagem/transporte, preparação de ambiente e testes. A etapa também sugere cliente/servidor simples, que ainda não existe em rede; não declarar a etapa integralmente concluída.
- Pág. 3, Etapas 1/2: formatos de HELLO/registo, anúncios e controlo de rotas preparados e serializados. A construção da overlay e atualização de rotas permanecem futuras.
- Pág. 4, Etapa 4: recurso movie.Mjpeg inspecionado e seu formato de ficheiro conhecido. Não há transmissão/reprodução de vídeo.
- Pág. 4, avaliação: há contribuição para especificação conceptual, implementação e qualidade dos testes do protocolo aplicacional de controlo (item de 15%). Não atribuir os 15% como cumpridos: o protocolo ainda precisa de interações executadas e integração na rede.
- Pág. 5, relatório: a fase fornece sobretudo os formatos de mensagens, detalhes da base implementada e resultados de testes locais. Testes CORE e avaliação crítica completa ainda faltam.

Maven, JUnit, os números dos campos, o cabeçalho escolhido e a quantidade de testes são decisões técnicas, não imposições textuais do enunciado. A utilização das topologias CORE existentes foi mantida; os perfis loopback desta fase são apenas de desenvolvimento.

## 4. Texto factual reutilizável

O protótipo foi organizado em módulos de configuração, controlo e serviços de rede. Na primeira fase implementaram-se a carga de configuração e a representação binária das mensagens de controlo. A configuração valida o grafo de vizinhança e os metadados das origens, com erros associados ao ficheiro e à propriedade, sem iniciar comunicação. O codec v1 utiliza um prefixo de comprimento de quatro bytes, seguido de um corpo limitado entre 37 e 65536 bytes. As mensagens são tipadas, usam UTF-8 estrito e preservam os identificadores UUID. Leituras parciais são recompostas antes da interpretação; entradas fora dos limites e corpos malformados são rejeitados.

A validação incluiu testemunhos binários definidos antes do codec e casos de configuração inválida, truncatura, concatenação e limites. O build limpo executou 131 testes com sucesso. Estes resultados demonstram a base local de configuração e serialização, não o desempenho ou funcionamento da overlay em rede. A implementação do transporte, dos handlers distribuídos, das árvores de distribuição e do streaming permanece nas fases seguintes.

## 5. Próxima execução

No fecho da PHASE-01 a próxima tarefa era TASK-005. Atualização: TASK-005 DONE com transporte TCP, 15 novos testes e 146 no build. Próxima TASK-006 TODO. O material anterior descreve a fase 1; [notas incrementais da fase 2](PHASE-02-REPORT-NOTES.md). Retomar pelo [progresso](15-PROJECT-PROGRESS.md) e pelo [guia](AI-IMPLEMENTATION-GUIDE.md). Preservar os XML em CORE/topologies e as fixtures independentes. Não substituir observações locais por resultados CORE não executados.
