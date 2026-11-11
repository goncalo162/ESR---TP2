---
stage: model
bolt: "034-overlay-extra"
created: "2026-11-11T12:11:34Z"
updated: "2026-11-11T15:43:23Z"
---
# Model — EXTRA-01, aprendizagem por divulgação de vizinhos

O utilizador escolheu anúncios dos vizinhos diretos com conservação limitada
dos candidatos aprendidos. Esta decisão substitui o catálogo inicial de
endpoints. O bootstrap continua a dar apenas os vizinhos de arranque.
[REQ-025](../../../specs.md#req-025) · [ADR-033 proposta](../../../add.md#adr-033).

## Fronteiras e sequência

IMP-034 aprende e conserva candidatos. IMP-035 usa essa cache para reparar
ligações. Ambos precedem 005-rotas. A escolha da rota por fluxo e a prova
de vídeo pertencem à integração posterior de routing/streaming.

No primeiro grafo, R1 ou R2 divulgam a C o seu vizinho S enquanto estão vivos.
Depois de morrerem os dois relays, a cache permite tentar S–C.
No segundo grafo, R2 ou R3 divulgam R1 a C. Depois de morrerem R2/R3, C
pode pedir R1–C, enquanto R1 conserva S. A reparação é assimétrica.
Os testes devem mostrar que os candidatos foram aprendidos antes das mortes.

## Entidades e invariantes

- Anúncio imutável com snapshot limitado de vizinhos diretos confirmados.
  Só é recebido de uma sessão autorizada e a identidade é correlacionada
  com instanceId/connectionId do anunciante.
- Candidato com nodeId, endpoint, papel, origem do anúncio, geração e
  instantes locais de aprendizagem/expiração. Informação sobre a instância
  observada pelo anunciante é uma pista; o novo handshake valida a instância.
- A cache começa sem endpoints globais pré-carregados. Exclui o próprio nó,
  filtra duplicados e trata endpoints incompatíveis segundo política a
  aprovar. Limites de candidatos, fontes, filas e timers são explícitos.
- A morte de quem anunciou um candidato não apaga esse candidato por si.
  O prazo local continua válido. A reparação pode falhar porque o candidato
  já morreu. A informação recebida não garante disponibilidade.
- O TTL usa relógio monotónico local. Mensagens antigas, de sessões
  substituídas ou com geração obsoleta não renovam indefinidamente entradas.
  Expiração e retenção têm testes com relógio controlado.
- A divulgação inicial cobre apenas os vizinhos diretos ativos de quem
  anuncia. Entradas apenas aprendidas na cache não são retransmitidas como
  se fossem ligações diretas. Uma nova sessão confirmada passa a ser uma
  adjacência real e pode constar de snapshots futuros.
- Admissões, cache, snapshots e callbacks são coordenados pelo executor
  serial do nó. Envio e conclusão não bloqueiam o owner. Shutdown fecha
  admissões e termina os recursos dentro do orçamento restante.

## Protocolo e decisões de Design

Os 16 tipos ativos v1 não oferecem esta operação de divulgação. Design
deve especificar mensagens próprias, enquadramento, negociação ou isolamento
da extensão, limites e compatibilidade. Não se atribui um código novo nem se
reutiliza REGISTER_OK ou uma mensagem de routing para esconder a operação.
O codec v1, código 3 rejeitado, fixtures e hard caps conservam o contrato.
O Design tem de identificar a integração da extensão com o transporte e
provar que os chamadores e sessões base conservam o comportamento. Os
ficheiros de transporte e os testes afetados constam do âmbito proposto
no IMP-034.

A confiança nos endpoints aprendidos, a frequência de anúncios, a geração,
o TTL, a capacidade e a política perante conflito aguardam Design.
Trabalha-se no laboratório controlado. A aprovação da funcionalidade não
constitui aprovação de um formato binário concreto.
[ADR-033](../../../add.md#adr-033) permanece Proposta até essa revisão.

## Oráculos V-033 da aprendizagem

| Caso | Prova necessária |
|---|---|
| D01 | Todos os nós overlay exigem descoberta; conservar bytes/fixtures v1 e Q=1 genérico/bootstrap sem modo de nó desativado. |
| D02 | Anúncio só é aceite numa sessão confirmada e correlacionada. |
| D03 | C aprende S através de R1/R2, sem ter o endpoint de S em configuração. |
| D04 | C aprende R1 através de R2/R3, sem o endpoint de R1 pré-configurado em C. |
| D05 | Cache retém o candidato depois da morte de quem o anunciou até ao prazo próprio. |
| D06 | Relógio falso comprova expiração, limites e ausência de renovação por anúncio obsoleto. |
| D07 | Listas repetidas, self, endpoints inválidos ou conflitantes têm tratamento explícito e limitado. |
| D08 | Anúncios divulgam só adjacências diretas confirmadas, com frequência e filas limitadas. |
| D09 | Ausência de aprendizagem antes das mortes fica visível como cache sem candidato utilizável. |
| D10 | Shutdown, callbacks antigos e falhas de envio não deixam timers, sockets ou futures retidos. |
| D11 | Peer incompatível é rejeitado explicitamente, sem ACTIVE_BASE operacional; formatos v1 usados preservados no TCP comum, sem canal separado. |
| D12 | Regressão atual e testes da extensão executados na validação, com logs e hashes. |

## Handoff para IMP-035

V-034 exige S–C após a morte de R1/R2 e R1–C após a morte de R2/R3,
com mesmos sobreviventes, bootstrap parado, uma sessão por par e
preservação da ligação S–R1 no caso assimétrico.
A política de gatilho e iniciativa tem de permitir que só C precise
da reparação. Falhas, expiração e candidatos indisponíveis devem produzir
estado explícito e tentativas limitadas. O ensaio inicial mata processos
abruptamente; desaparecimento silencioso de máquina fica ligado às sondas
do IMP-007. Vídeo pela aresta criada é prova posterior das TASK-016/024/026.

Model aprovado explicitamente pelo utilizador; aprovação registada em 2026-11-11T12:41:00Z.
[Design proposto](ddd-02-technical-design.md) concretiza os pontos acima, sem
aprovação implícita do protocolo/ADR-033. V-033 e V-034 continuam por executar.

## Mudança de requisito operacional

A decisão posterior do utilizador torna descoberta obrigatória em todo nó
overlay, por todos os construtores/loaders/perfis. Não há ativação opcional ou
ACTIVE_BASE. Cache, proveniência, TTL e os dois cenários permanecem;
[Design](ddd-02-technical-design.md) e [migração dos testes](test-contract-migration.md)
concretizam os ajustes operacionais para revisão. IMP-034 aprende; IMP-035 repara.
