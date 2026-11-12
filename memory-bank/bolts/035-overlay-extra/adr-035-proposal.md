---
stage: adr
bolt: "035-overlay-extra"
adr: "ADR-035"
status: "Aceite"
replaces_parts: ["ADR-019", "ADR-033", "ADR-034"]
---
# ADR-035 — reparação direta na ligação iniciada pelo isolado

**Estado: Aceite.** Aprovação explícita do utilizador registada em
2026-11-11T23:39:43Z, incluindo revisão ADR, contratos e substituição parcial
ADR-019/033/034. G-EXTRA-ADR satisfeito para IMP-035. Só Implement autorizado;
parar antes de Test. O nome histórico deste ficheiro preserva os links existentes.

## Contexto e drivers

REQ-025 exige S–C após R1/R2 e S–R1–C após R2/R3 com S–R1 preservada, mesmos
sobreviventes, bootstrap parado e candidatos aprendidos antes das falhas.
Q-C-20 fixa zeroACTIVE como gatilho. O utilizador pediu que o isolado possa
iniciar e completar a reparação na mesma ligação mesmo tendo o maior nodeId,
evitar dial de retorno e justificar o mínimo de admissão no laboratório.

## Decisão aceite

1. Manter HELLO5/grafo e iniciador menor para a reconexão normal configurada.
2. Acrescentar20 REPAIR_HELLO, com o payload Hello existente, para uma tentativa
   local motivada por isolamento e cache válida. O isolado inicia qualquer que
   seja a ordem de IDs. O destino saudável pode admitir e responder6 HELLO_OK
   no mesmo TCP, sem procurar reciprocamente ou abrir ligação de retorno.
3. Antes de dial, reservar a tentativa no owner. Se há duas saídas sobrepostas,
   preferir o menor originatorId enquanto pendentes. Entrada já escolhida impede
   lançar outra saída. Duplicata do mesmo originator não troca o requestId atual.
   ACTIVE é imutável perante tentativa tardia: rejeitar duplicata, sem Down da
   sessão saudável. Callbacks do perdedor só terminam os recursos próprios.
4. Admitir unilateralmente no laboratório cooperativo: origem verifica/regista
   aprendizagem/proveniência/TTL; destino valida campos, identidade/endpoint,
   capacidades, colisões e quotas. **O destino não comprova a aprendizagem da
   origem.** Não emitir permissões ou exigir cache recíproca. Não atribuir a
   esta política defesa contra um processo malicioso que imite campos válidos.
5. Um codec TCP v2,19 tipos1/2/4..20 e3 rejeitado. HELLO5/20 e ACK6 exigem
   máscaras3 (DISCOVERY|REPAIR), sem subconjunto/fallback. Alteram-se quatro
   fixtures de máscara e somam-se12 independentes; alvo78. Restantes46TCP/
   16UDP mantidos, incluindo snapshots/gerações/TTL e bootstrap.
6. Manter mesma FIFO/sendAndFlush/barreiras, identidade/instâncias/correlação,
   isolamento local, limites C/B/R/Q e shutdown com prazo. Contextos perdedores
   continuam cobrados até terminação física; nenhuma API nova de terminação.

## Alcance da substituição aceite

- ADR-019: exceção ao iniciador menor e admissão por grafo **só para finalidade20**;
  desempate menor apenas quando há saídas concorrentes pendentes. HELLO5 conserva
  restrições e uma sessão ACTIVE por par continua obrigatória.
- ADR-033 residual: concretizar nova aresta por aprendizagem válida na origem e
  confiança cooperativa no destino, sem nova permissão verificável remotamente.
- ADR-034:18→19 tipos, máscaras1→3, mesmo codec/versão; sem alterar formato dos
  snapshots ou acrescentar provas. Sem fallback ou suporte operacional a JAR antigo.
- ADR-013/014/015/017/018/020/021: bootstrap inicial, recursos/flush/owner/identidade/
  falhas locais e pressuposto do laboratório mantêm-se. UDP OTT1 intocável.

## Alternativas consideradas

- Menor ID universal e retorno: rejeitado pelo pedido atual e exige coordenação/
  fecho/retorno que não são necessários para sessão única.
- Permissões prévias, registry e quatro mensagens: permitem ao destino verificar
  uma introdução, mas acrescentam política/estado/expiração e wire fora da
  confiança cooperativa escolhida nesta proposta mínima. Retirados, não implementados.
- Cache recíproca: pode impedir a reparação unilateral pedida.
- Zero tipos novos: mistura HELLO configurado com admissão fora do grafo ou exige
  outro discriminador no payload de todos os HELLO; um código dedicado é mais explícito.
- Substituir ACTIVE para impor menor ID: provoca Down desnecessário, contra
  isolamento de falhas e preservação das sessões saudáveis.
- «Primeiro socket ganha» sem slot/correlação: não resolve duas saídas realmente
  concorrentes; a regra proposta elege direção comum e protege o contexto escolhido.

## Consequências e limites

A proposta reduz a troca a HELLO20/ACK6 e usa as APIs de transporte existentes.
As políticas de cache/anúncios não mudam. A origem assume a verificação da
proveniência; o destino não ganha autenticação ou conhecimento do grafo global.
Uma direção diferente pode vencer se já estava admitida antes da segunda saída;
não se promete impor menor ID depois de ACTIVE. Falta de candidato/recurso/
alcançabilidade mantém isolamento/retry, sem sucesso ou SLA fabricado.

## Como validar e aprovar

[Design D01–D20 / M01–M18](ddd-02-technical-design.md) e
[âmbito aprovado21](scope-change-proposal.md). Conferir APIs na revisão ADR,
validar fixtures novas/máscaras e negatividade de HELLO5 desconhecido, testar
ordens de saídas/ACK/flush/cancelamento, mesmo connectionId sem retorno,
sessões saudáveis, limites e terminação física. V-034, regressão atual/manual
SIGKILL/revisão final/integração antes de DONE.

Política, contratos, âmbito e revisão ADR aprovados explicitamente.
ADD/catálogo registam ADR-035 Aceite; as cláusulas não substituídas de019/033/034
mantêm-se. Implement não autoriza Test/fecho/commits/push.

## Formalização desta revisão

Registo de 2026-11-11T23:28:20Z. [Revisão ADR e conferência das APIs reais](adr-review.md).
A política cooperativa e o âmbito já estão aprovados; este checkpoint pede
apenas validação formal da ADR/substituição parcial antes de Implement.

## Aprovação formal

Registo de 2026-11-11T23:39:43Z. ADR-035/revisão/substituição parcial aprovadas explicitamente.
G-EXTRA-ADR satisfeito para IMP-035; só Implement, parar antes de Test.
