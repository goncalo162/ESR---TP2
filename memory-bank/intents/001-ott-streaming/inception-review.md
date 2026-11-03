# Revisão Inception — 001-ott-streaming

Revisão técnica: 2026-11-03T14:28:37Z. Fecho aprovado pelo utilizador:
2026-11-03T20:54:11Z. Estado: Inception concluído para o âmbito atual.
Não foram identificadas lacunas impeditivas; CP-01–03 estão aprovadas e
incorporadas. A instrução explícita para fechar a fase aprova os artefactos
revistos, sem executar Construction ou antecipar a escolha dos extras.

## Âmbito e fontes

Conferidos requisitos, contexto, sete briefs, índice e 33 histórias, 33 bolts,
tipos de bolt, dependências e rastreabilidade. A [especificação](../../../specs.md)
contém 40 entradas: 24 REQ, cinco NFR, nove CON e dois INT. Os 35 requisitos
históricos RF/RNF/RC/RP mantêm aliases e critérios; a diferença de contagens
resulta da consolidação, não de requisitos perdidos.

O [ADD](../../../add.md) governa a arquitetura; os artefactos specsmd organizam
a execução do mesmo projeto Java 21/Maven. As unidades são agrupamentos de
trabalho e não serviços ou deployables independentes.

## Contexto e fluxos

Atores: operador/equipa, espectador e docente. Sistemas e recursos externos:
CORE/rede IP, ficheiros de configuração e media, display Swing e Blackboard
para a submissão humana final. JDK/Maven apoiam construção e avaliação.

Bootstrap fornece localização inicial de vizinhos configurados. TCP transporta
handshake, métricas, anúncios e controlo das árvores; UDP transporta fragmentos
JPEG nos ramos confirmados. Os clientes reconstituem frames e apresentam-nos
no EDT. Logs, capturas e métricas sustentam a avaliação e o artigo.

## Unidades, histórias e bolts

| Unidade | Objetivo | Histórias/bolts | Bolts na ordem de planeamento |
|---|---|---|---|
| 001-base-validada | Verificar a base herdada e executar regressão | 1/1 | 001-base-validada |
| 002-overlay | Parametrização, descoberta, vizinhos, processo e primeiro marco | 5/5 | 019-overlay, 002-overlay, 003-overlay, 020-overlay, 004-overlay |
| 003-rotas | RTT, candidatos, anúncios, catálogo e diagnóstico | 4/4 | 005-rotas, 021-rotas, 006-rotas, 022-rotas |
| 004-arvores | Ativação, leases, poda, recuperação e adaptação | 5/5 | 007-arvores, 023-arvores, 008-arvores, 024-arvores, 009-arvores |
| 005-video | Codec, media, leitor, emissão, encaminhamento, cliente e integração | 8/8 | 010-video, 011-video, 025-video, 026-video, 012-video, 027-video, 013-video, 028-video |
| 006-core | Preparar ambiente e medir partilha, recuperação, adaptação e escala | 6/6 | 014-core, 029-core, 015-core, 030-core, 016-core, 031-core |
| 007-entrega | Artigo, pacote reproduzível, defesa e registo de submissão | 4/4 | 017-entrega, 018-entrega, 032-entrega, 033-entrega |

As [unidades](units.md), o [índice de histórias](../../story-index.md) e o
[plano de bolts](../../../docs/18-CONSTRUCTION-BOLTS.md) permitem inspecionar
cada artefacto e as suas dependências. A tabela não substitui requires_bolts.

## Verificações e resultado

- Todos os requisitos canónicos têm proprietário primário único e cobertura
  direta nos metadados das histórias; não há requisitos sem história.
- Há correspondência única entre 33 IMP, 33 histórias e 33 bolts. Os critérios
  dos IMP estão presentes nas histórias e nos bolts, com validações associadas.
- Os grafos de TASK, unidades e bolts não têm ciclos. Dependências de tarefas
  compostas e gates de fase estão preservados; uma parte não conclui o pai.
- Os 14 bolts DDD cobrem parametrização, estado distribuído e processamento;
  os 19 simples cobrem a base, composição, codec definido, GUI, integração,
  experiências e entrega. As etapas correspondem aos tipos locais.
- Todos os bolts continuam planned, sem etapas ou timestamps de execução.
- O validador confirmou 26 ADR, matriz 40/40, referências locais/âncoras,
  45 fixtures, parsing de três XML CORE e 219 hashes de fontes importadas.
  A [evidência desta execução](../../../experiments/inception-review-validation-2026-11-03.md)
  regista o comando e zero erros.

## Limites e pendências de execução

Não foram executados Maven, testes Java, GUI ou sessões CORE nesta revisão.
Os 149 testes continuam a ser evidência histórica. A análise de fixtures e XML
não demonstra reprodução de vídeo, reachability ou convergência reais.

Os gates de media/proveniência, GUI, CORE, avaliações, dados finais de entrega,
revisão da TASK-032 e confirmação de submissão permanecem nas tarefas próprias.
Não constituem lacunas que impeçam concluir o planeamento ou verificar a base.

Mantêm-se as escolhas já registadas: falha exclusiva UDP com TCP saudável não
tem recuperação automática; recuperação por alternativa exige caminho até à
mesma origem; não há failover de conteúdo entre origens. Não se acrescentam
CI, linter ou limiar percentual global de cobertura.

## Atualização após os apontamentos do checkpoint

O [relatório do checkpoint](../../../docs/checkpoint_REPORT.md) preserva os
apontamentos e regista agora CP-01–03 como aprovadas pelo utilizador. A
[revisão complementar](checkpoint-feedback-review.md) esclarece as decisões:
manter nodeId/REGISTER na consulta inicial (CP-01), validar regresso e entrada
tardia de nós já configurados (CP-02), e adiar novas vizinhanças fora do grafo
como extra (CP-03). O ADD 1.0.1 incorpora as clarificações em ADR-013/019 e secção 13.

Regresso de nós configurados e análise de trade-offs no artigo já estão
planeados. Novas vizinhanças fora do grafo e IDs desconhecidos não estão
cobertos pelos 33 bolts atuais. As escolhas aprovadas confirmam o âmbito
existente, sem acrescentar requisitos, ADR ou bolts.

As propostas CP-01–03 estão resolvidas pela escolha explícita do utilizador.
O utilizador aprovou também o fecho final dos artefactos do âmbito atual;
os gates de execução continuam nas tarefas responsáveis.

[Validação após a incorporação das decisões](../../../experiments/checkpoint-decisions-validation-2026-11-03.md):
zero erros, contagens e DAG preservados, 220 hashes conferidos. O relatório
conserva o contexto da incorporação das CP; a validação atual do fecho está abaixo.

[Validação documental do fecho](../../../experiments/inception-closure-validation-2026-11-03.md):
zero erros, sete unidades prontas e 33 bolts planeados, sem ciclos ou referências
quebradas. Não foram executados testes Java, GUI, CORE ou Construction.

## Fecho e continuidade

O checkpoint 3 está concluído pela aprovação explícita do fecho. O intent fica
pronto para Construction; a execução do primeiro bolt será uma ação posterior.
O primeiro bolt é 001-base-validada; depois segue 019-overlay/TASK-032, antes
de 002-overlay/TASK-006.

Os extras serão definidos numa retoma futura de Inception, quando forem
pedidos, com requisitos/ADR/histórias/bolts próprios e integração antes da
entrega que os inclua. O âmbito atual continua com 33 bolts; não se presume
uma exclusão definitiva de extras do trabalho nem funcionalidades adicionais
aprovadas agora. O fecho está registado no [inception-log](inception-log.md).
