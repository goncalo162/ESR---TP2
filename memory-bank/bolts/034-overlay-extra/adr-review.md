> Histórico da variante OTD1 interrompida. A aprovação foi registada em
> 2026-11-11T13:57:14Z. Agora aplica-se a revisão proposta em ADR-034 e no
> Design atual, com G-EXTRA-ADR reaberto; este relatório não autoriza código.

# IMP-034 — Revisão ADR para aprovação

**Etapa ADR em revisão; Implement não iniciado.** O utilizador aprovou
explicitamente Design e as escolhas de Q-C-17. Aprovação registada na entrada
de ADR em 2026-11-11T13:38:55Z; este é o instante do registo, não uma data
atribuída à mensagem humana. Base: `17c1ca27673930bfbf82078522059a243d96ccdb`.

[Design aprovado](ddd-02-technical-design.md) ·
[ADR-033 Proposta](../../../add.md#adr-033) ·
[Contrato OTD1 em revisão](../../../docs/07-API-AND-INTERFACES.md#extra-01--contrato-otd1-proposto) ·
[Q-C-17 aprovada em Design](../../../docs/construction-duvidas.md#q-c-17--design-da-descoberta-extra-proposta).

## 1. Conclusão e limites da revisão

ADR-033, Design e interfaces são coerentes com as escolhas aprovadas para
IMP-034. A revisão identifica uma cobertura documental a explicitar: a única
chave adicional, extra.discovery.enabled, é uma exceção proposta à lista de
chaves de ADR-010/016, limitada à extensão. Foi explicitada na ADR-033, sem
alterar a política aprovada em Design ou o texto das ADR aceites.

Não foi identificada decisão aceite inviável nem necessidade de ADR-034.
ADR-033 continua **Proposta**, cobrindo as escolhas novas para aprovação
formal neste checkpoint. ADR-003 continua **Substituída** por ADR-013. As
25 ADR aceites e a entrada substituída conservam o conteúdo de referência.

Esta conclusão é documental/estática. Não prova comportamento implementado,
autenticação, terminação física ou execução de V-033. O Java, os testes e o
JAR histórico permanecem na versão de entrada. Não se reexecutou a regressão
de 981 testes. V-033/D01–D12, regressão atual, manual, revisão humana final e
integração continuam necessários para fechar.

## 2. Matriz de conformidade com as decisões existentes

| Decisão do ADD | Conferência e alcance da ADR-033 | Prova futura |
|---|---|---|
| ADR-002 — TCP/UDP | Sessões vizinhas de controlo continuam persistentes. OTD1 é consulta TCP curta separada; UDP OTT1 mantém framing e dataPort. | D01/D11/D12 |
| ADR-007 — Binário e dependências | OTD1 tem codec próprio, UTF-8 estrito, códigos explícitos; não usa ObjectInputStream ou dependência runtime. | D07/D11/D12 |
| ADR-010/016 — Configuração | Proposta explícita de uma chave extra, booleana e desativada por default; só nos três papéis overlay. Preservar validação estrita, erros agregados, duas assinaturas anteriores e as 30 chaves/defaults. | D01/D07/D12 |
| ADR-013 — Bootstrap inicial | REGISTER_OK e grafo do bootstrap não mudam. Não há catálogo global, membership ou consulta após falhas. | D03/D04/D09/D11 |
| ADR-014 — Recursos TCP | Variante de codec tipada, C partilhado entre controlo e descoberta; dials/terminação física conservam reservas; fila limitada e callbacks não bloqueiam. | D08/D10/D11 |
| ADR-015 — Write/flush | Resposta OTD1 só fecha após flush ou prazo. Reutilizar a disciplina de fila/frame em escrita/futures fora de locks. O sucesso continua local, não ACK remoto. | D08/D10/D11 |
| ADR-017 — Owner serial | Sessões, gerações, cache e consultas pertencem ao mesmo owner. Discovery recebe owner emprestado; não o fecha. Saturação de eventos é falha observada. | D02/D06/D10 |
| ADR-018 — Estado/identidade | Cache só em memória, instanceId observada é pista; token HELLO, conexão e consulta delimitam o contexto. Não ordenar UUIDs ou restaurar estado após reinício. | D02/D05/D06/D10 |
| ADR-019 / CP-02/03 | IMP-034 aprende candidatos, sem os admitir como vizinhos. Base continua limitada ao grafo e menor nodeId. Exceção futura de arestas aprendidas só em IMP-035 com contrato próprio aprovado. | D01/D03/D04/D09 e V-034 posterior |
| ADR-020 — Falhas/shutdown | Falhas de formato/flush/capacidade afetam o canal extra; razões são diagnósticos locais, sem ERROR OTD1. Prazo global não é renovado por componente. | D07/D08/D10/D11 |
| ADR-024 — Rastreabilidade | Um IMP, os 15 caminhos já previstos na secção 8, V-033 e gates explícitos. Não iniciar reparação, routing ou outro bolt. | V-ADD/diff/hashes e D12 |
| ADR-026 — Validação | Maven/JUnit e validador existentes; sem CI, linter, JaCoCo ou limiar novo. Evidência de execução só conta depois de recolhida. | D12/Test/manual |

## 3. Coerência dos contratos afetados

As secções indicadas pertencem ao Design aprovado; a secção OTD1 de interfaces
é o resumo do mesmo contrato. O ADD/ADR-033 remete para o detalhe binário e
não cria um segundo formato concorrente.

| Aspeto conferido | Regra comum | Referência do Design |
|---|---|---|
| Isolamento | TCP/dataPort só OTD1; TCP/controlPort só v1; UDP/dataPort só OTT1. Extra desligado não cria recursos. | 2 e 4 |
| Deploy misto | Sem fallback para controlo. dataPort=controlPort remoto ou colisão TCP conhecida impede dial. Falha do extra conserva a sessão base. | 2 e D11 |
| Envelope | Big-endian, u32 bodyLength, magic OTD1, version 1, kind QUERY=1/SNAPSHOT=2, queryId, token HELLO e identidade/instância dos dois extremos. | 4 |
| Tamanhos | QUERY 76..138; SNAPSHOT vazio 86..148; endpoint máximo 310; SNAPSHOT máximo 19678; cap externo do corpo 32768. Prefixo de quatro bytes excluído. | 4 e D07 |
| Snapshot | Até 63 vizinhos diretos ACTIVE, sem hearsay/TTL remoto; roles explícitos e validação completa antes da mutação. | 4, 6 e D08 |
| Confiança | Token HELLO comum aos extremos correlaciona sessão; não autentica criptograficamente. connectionId é local, não é um valor comum no fio. | 3 e D02 |
| Consulta corrente | Fonte ACTIVE e correspondência de queryId/canal/instâncias/token/runToken, antes dos prazos. Resposta velha não altera geração/cache da sessão nova. | 3, 5 e 7 |
| Geração | Positiva e estritamente crescente por sessão emissora; sem ordenar gerações entre sessões ou permitir wrap. | 5 e D06 |
| Retenção | TTL de 60 s desde o início monotónico local da QUERY. Down da fonte conserva claims até ao prazo próprio. Omissão, replay e sessão velha não rejuvenescem claims. | 5, 6 e D05/D06 |
| Cache/conflitos | 128 IDs × quatro fontes; configuração local autoritativa, self ignorado e conflitos indisponíveis; sem desalojar claims válidos por saturação. | 6 e D06/D07 |
| Frequência | Elegibilidade por peer 5 s; saída global uma query/250 ms; entrada por sessão uma/s; pausa 30 s após falha. Diagnóstico limitado por motivo. | 5 e 7, D08 |
| Recursos | C<=64 partilhado; até quatro sockets/dials extra (2+2); fila de um frame mais o frame em escrita; jobs físicos comuns limitados. | 7 e D08/D10 |
| API/conclusões | Facadas tipadas separadas, records/cópias defensivas, consulta pública limitada e assíncrona; flush não é ACK e cancelamento não é fim físico. | 7 e 8 |
| Shutdown | Admissões fechadas, tick cancelado e recursos aguardados fora do owner/locks dentro do restante orçamento global. Worker preso significa timeout explícito. | 7 e D10 |

Os valores acima foram reconferidos no documento; não são medições. A contagem
binária é uma conferência aritmética, sem execução do codec novo nesta etapa.
Não se envia um novo tipo no namespace v1 nem se altera o código 3 rejeitado,
o corpo v1 37..65536, o cabeçalho UDP de 64 bytes, total <=1200 ou payload <=1136.

## 4. Confiança, retenção e admissão

Uma sessão base ACTIVE autoriza a consulta ao seu vizinho, com validação de
identidades e token. O anúncio pode continuar a estar errado ou desatualizado;
o laboratório não passa a ter TLS, assinatura, quorum ou autenticação de
endpoints. Um observador do fio pode conhecer o token. Não se resolve esta
limitação com requestId aleatório, comparação de IP remoto ou nova propriedade.

Na entrada da SNAPSHOT, verificar primeiro contexto e prazo no owner; só depois
aceitar generation e claims. Um Down antigo não elimina a sessão nova. O Down
corrente retira a autorização para novos anúncios nessa sessão, mas não elimina
as proveniências válidas já guardadas. Expiração local e retirada de autorização
são operações diferentes. Não há timers por claim nem tombstones ilimitados.

Endpoint unívoco torna o candidato AVAILABLE, sem o transformar em vizinho.
Conflito torna-o indisponível enquanto as proveniências divergirem; configuração
local não é substituída por publicidade. Múltiplas instâncias observadas no mesmo
endpoint não são ordenadas por UUID nem escolhidas como identidade atual.

V-033 demonstra aprendizagem recíproca nos dois grafos antes das falhas, retenção
após a morte do anunciante e expiração. S–C/R1–C, autorização dos dois lados,
intenção de reparação, corridas de admissão e preservação de S–R1 pertencem ao
IMP-035/V-034. O gate desse incremento exige os seus contratos revistos; esta
aprovação de Design não os fornece por antecipação.

## 5. Contabilização física e integração aditiva

| Recurso | Regra que a implementação terá de cumprir |
|---|---|
| Reservas TCP | Um Reservations por OverlayNode de capacidade C. Todos os accepts, dials de controlo/bootstrap e consultas extra usam-no; não há C independente por listener. |
| Quota da descoberta | Até quatro reservas físicas (duas por direção), dentro de C. Retirar contexto lógico ou concluir/cancelar future não devolve a reserva física. |
| Worker de conexão | Devolver reserva apenas depois da saída física do dial sem conexão ou dos dois workers da conexão. Comparar o handle exato ao libertar. |
| Writer | Uma fila por canal extra, capacidade 1 mais inFlight. Flush, fecho e falha decidem cada future uma vez; publicar conclusões fora dos monitores. |
| DNS/bind | Usar o serviço limitado existente: um worker, até 65 jobs físicos totais, incluindo em terminação. Até dois jobs da descoberta dentro desse limite, sem outro pool. Host ocupado pode recusar consulta; não é descoberta garantida. |
| Tick | Uma tarefa periódica física extra, com no máximo um evento pendente; cancelamento/shutdown aguardam terminação real. Mantêm-se os limites dos timers base. |
| Eventos | Capacidade existente do owner, até quatro contextos físicos extra e snapshots limitados. Overflow de fila de canal fecha o canal; impossibilidade de publicar cleanup crítico usa a falha terminal do owner, sem CallerRuns ou fila alternativa. |
| Encerramento | Quotas/reservas continuam ocupadas até fim físico; prazo global restante cobre ambos os componentes, I/O e owner do nó. O componente emprestado não fecha o owner. |

A inspeção estática do adaptador existente confirma o limite de um worker e
65 jobs DNS/bind como ponto de integração; ainda não existe prova executada de
partilha com a extensão. A fachada de resolver/bind e o overload de orçamento
TCP pertencem aos ficheiros NeighborManager/Transport já autorizados. Não há
razão identificada para alterar outra fundação ou criar dependência runtime.

Se a implementação revelar que a partilha exige violar API/cap/decisão aceite
ou ultrapassar os caminhos autorizados, aplicar ADD 11: evidência e impacto,
paragem da parte afetada e aprovação antes de intervir. Não resolver a lacuna
com um segundo orçamento, libertação antecipada ou testes enfraquecidos.

## 6. Decisão submetida ao checkpoint

- **Aprovados pelo utilizador:** Model, Design e escolhas de Q-C-17.
- **Para aprovação agora:** esta revisão ADR, ADR-033 no ADD e contratos
  afetados de descoberta/configuração/transporte definidos nas secções 2–8 do
  Design e na secção OTD1 de interfaces.
- **G-EXTRA-ADR do IMP-034:** pendente dessa aprovação formal; não foi fechado
  por inferência da aprovação de Design.
- **ADR-033:** permanece Proposta; a política de aprendizagem está consolidada,
  a reparação futura continua sujeita ao contrato do IMP-035.
- **Etapa atual:** ADR, com Model/Design em stages_completed; Implement/Test,
  commits/push, revisão final e integração não foram executados.

A evidência documental é arquivada em experiments/construction/034-overlay-extra,
com hashes, diffs desta etapa e cumulativos e execução nova de V-ADD. Os relatórios
de Model/Design permanecem preservados. No fecho continuam obrigatórios
V-033/D01–D12, regressão atual, manual, revisão humana e integração.

## 7. Evidência documental desta etapa

[V-ADD executado](closure-walkthrough.md#limpeza-autorizada):
saída 0, zero erros, 45 fixtures verificadas. `git diff --check` passou.
[Conferência de hashes/estado](closure-walkthrough.md#limpeza-autorizada)
regista os 157 ficheiros de Projeto/ e SHA do JAR histórico preservados.
[Diff desta etapa](closure-walkthrough.md#limpeza-autorizada)
e [diff cumulativo contra a base](closure-walkthrough.md#limpeza-autorizada)
separam esta revisão das alterações de planeamento já presentes à entrada.
O campo Base do relatório V-ADD é a revisão histórica do manifesto; HEAD real
continua 17c1ca2 e é registado separadamente na conferência. Não se reescreve
o snapshot histórico para aparentar uma validação diferente.
