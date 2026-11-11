# IMP-034 — Migração dos testes para TCP exclusivamente v2

Plano proposto em 2026-11-11T17:47:08Z; não aplicado aos testes Java/recursos de Projeto/.
[Design](ddd-02-technical-design.md) · [ADR-034](../../../add.md#adr-034).
Programa/JAR únicos, sem codec/runtime v1, fallback ou modo opcional. Arquivo
de v1 preserva proveniência; não manter decoder antigo para o fazer passar.

## Regra de migração e independência do oráculo

Os [50 vetores TCP v2](../../../experiments/construction/034-overlay-extra/protocol-v2-proposal/README.md)
são construídos a partir de campos literais do contrato, sem encoder Java nem
cópia/alteração de um byte de v1. Um auditor Python separado verifica campos,
framing e hashes; não é execução do Java nem prova de sessões/recuperação real.
Os valores esperados nos testes serão literais/modelos documentados, nunca
MessageCodec.encode seguido do seu próprio decode como única prova.

| Família de testes | Migração necessária | Garantias preservadas |
|---|---|---|
| MessageCodecTest.goldenVectors / readsFragmentedStream / consecutiveMessages | Mesmos 16 significados originais em v2, HELLO com Endpoint+1+1 e ACK com 1; adicionar 18/19 e vetores extra. | Bytes contra fixtures independentes, EOF limpo, leitura fragmentada/concatenada, rejeição de bytes finais, UTF-8/Unicode/listas/cópias defensivas. |
| MessageCodecTest.rejectsMalformedTcpFixtures / truncamentos em cada offset | Negativos de conteúdo usam envelope v2 válido para atingir a causa certa; versão inválida usa 1/0/3/255. Código 3 continua negativo separado. | Mesmas classes/códigos EOFException/INVALID_MESSAGE/RESOURCE_LIMIT/UNSUPPORTED_VERSION e rejeição antes de alocar; não obter verde por rejeitar tudo por versão. |
| MessageCodecTest.enums / invalidModelsDoNotEmitPartialFrames | Esperar 18 valores explícitos (1,2,4..19); o antigo negativo version=2 passa a version=1/3; APIs/construtor de cinco argumentos emitem só v2. | Códigos anteriores preservados, 3 rejeitado, modelo inválido não escreve bytes, limites e domínio de payload. |
| endpointInstanceAndRolesFollowWireContract / offsets | Adaptar payload Hello/HelloOk e offsets declarados; offsets do Endpoint dentro de HELLO até ao role mantêm-se, máscaras seguem depois. | Instância zero apenas na lista configurada, papéis/portas/host válidos, min/max de IDs e strings, máscaras fixas e reservados zero. |
| BootstrapServerTest e cliente bootstrap em NeighborManagerTest | Fixtures/pares artificiais REGISTER/REGISTER_OK/ERROR v2; acrescentar pedido/resposta v1 negativos. | Grafo imutável, consultas offline, sem membership/leases; instâncias zero, correlação/identidade, uma resposta, escrita completa antes de EOF, deadlines/saturação/recursos e shutdown. Rejeitar pedido não mata bootstrap; cliente nunca tenta versão antiga. |
| NeighborManagerTest / LifecycleTest | Bootstrap e HELLO/ACK/ERROR/NODE_LEAVE/frames normais v2; atualizar fixtures de sessão positiva. | Seis ordens/arranque simultâneo/TCP real, menor iniciador, unicidade, duas barreiras, retenção B/R, callbacks antigos/runToken/connectionId/instanceId, backoff e terminação física. |
| TransportTest | Mensagens artificiais de todos os casos passam a v2 e payload correto, sem CodecPolicy/transportes por versão. | Q=1 genérico, fila única/FIFO/frame em escrita, concorrência, overflow normal, send vs flush, cancelamento, callbacks fora de locks, fechamento local e shutdown com prazo. |
| OttApplicationTest | Peers positivos usam mesmo contrato v2; manter comandos e responder queries válidas. | CLI/EOF/quit, reconexão com nova instância e sobreviventes, sem FATAL global por input inválido. D11a mantém A ACTIVE e aprendizagem/CLI úteis enquanto B é rejeitado. |
| ConfigurationLoaderTest / construtores 13/14 | Retirar expectativa de extra opcional, false/Q=1 overlay são erros agregados offline; genérico/bootstrap Q=1 preservado. | Defaults numéricos, overrides, validação/cópias defensivas/assinaturas, zero sockets/owner/workers antes de configuração válida. |
| NeighborDiscoveryTest / CandidateCacheTest / DiscoveryCodecTest OTD1 parciais | Migrar só enquadramento/dispatch para ControlMessage/MessageCodec v2; remover teste específico de canal OTD1 com justificação. | Cache 128x4, TTL local 60 s, proveniência/gerações/conflitos/retenção após morte, clocks falsos, callbacks antigos, limites de frequência/filas/timers/tickets. |
| Novo readFrame / D07 / D08 | SNAPSHOT inválida completa seguida de PROBE_PING v2 válida; depois SNAPSHOT válida no mesmo TCP. | Rejeição atómica sem renovar cache/watermark/TTL, framing/truncamento/identidade terminais, margem Q>=2, quatro tickets até terminação física e FIFO comum. |
| Recursos protocol/manifest.json / README | Após aprovação copiar 50 TCP v2 propostas para o conjunto operacional, rever manifesto/nomes conforme parser dos testes e manter 16 UDP intactas (alvo 66). | Bin/hex/hashes/campos esperados auditados; 45 fixtures originais no arquivo, sem reescrever evidências. V-ADD atual ainda verifica os 45 recursos congelados, não prova Java v2. |
| Testes/fixtures UDP OTT1 | Sem migração de versão por este IMP. | OTT1/version 1, cabeçalho 64, total <=1200, payload <=1136 e restantes caps/negativos intactos. |

## Sequência proposta e checkpoint

Introduzir primeiro novos oráculos negativos e red real; não fabricar falhas
nas fundações. Migrar por partes codec/payloads, bootstrap/cliente, transporte/
handshake, descoberta e CLI, executando os testes incrementais autorizados só
depois da aprovação. Não apagar testes, @Disable/excluir testes Maven, aumentar
timeouts para ocultar corridas ou reduzir ordens/casos. Justificar cada remoção
de pressuposto v1/OTD1 e manter correspondência entre garantia antiga e teste v2.
Preservar relatórios anteriores; novos totais só após execução da versão validada.
Reexecutar regressão JDK 21 e manual do contrato completo antes de Test/fecho.

IMP-034 aprende candidatos; IMP-035/V-034 cria novas arestas após falhas, em
checkpoint próprio. A migração não implementa routing/vídeo nem suporte a JARs
antigos. Design/ADR-034/contratos/âmbito/migração para aprovação; Java suspenso.

## Manual futuro do contrato completo (a preparar após aprovação)

Usar o mesmo JAR validado no bootstrap e em todos os nós, guardando o hash.
Confirmar REGISTER/REGISTER_OK/ERROR v2 e fecho após resposta completa; iniciar
vizinhos com HELLO/ACK v2, confirmar mensagens normais e descoberta v2 no mesmo
TCP persistente. Parar bootstrap após arranque e confirmar neighbors/help e
aprendizagem úteis. Injetar frames negativos (sem executar JAR antigo), mantendo
um peer saudável ACTIVE; observar rejeição local, zero Up inválido e zero Down
saudável/FATAL. Injetar anúncio completo inválido e depois ping/snapshot válidos
v2 na mesma sessão; verificar cache/geração/TTL intactos na rejeição. Confirmar
quit/EOF, flush e shutdown com prazo, portas livres e nenhum processo pendente.
Executar/recolher logs reais antes de aprovação de Test, sem alegar reparação
(IMP-035) ou continuidade de vídeo. O roteiro executável ainda não foi criado.

## Aplicação em Implement

A migração acima foi aplicada nos ficheiros aprovados. 1062 testes incrementais
passaram; logs/XML/hashes no walkthrough. Os 50 vetores TCP operacionais são
cópias byte a byte da proposta independente aprovada; os 16 UDP preservam
bytes e expectativas. V-ADD agora verifica 66 operacionais e 45 arquivados.

D20 do NeighborManager passou a Q=2/B=2 e testa overflow na terceira frame,
mantendo a prova de retenção limitada; Q=1 overlay é negativo offline e Q=1
genérico/bootstrap mantém testes. NODE_LEAVE passou a v2 sem alterar flush/EOF.
O antigo negativo version=2 passou a version=1. Bootstrap continua a rejeitar
os 17 outros tipos; ERROR recebido fecha sem resposta, conforme a regra
aprovada de não responder ERROR a ERROR. O teste mantém a rejeição e os demais
16 casos com resposta correlacionada, sem enfraquecer identidade/limites.

[PRESSUPOSTO] A ajuda referida em D11a é a invocação --help já existente.
A CLI interativa mantém neighbors/quit; não foi criado comando help nem
alterado OttApplication.java fora do âmbito. O cenário executa --help e
verifica neighbors em processos saudáveis durante a rejeição do peer.

Conteúdo extra completo inválido conserva cache/watermark/TTL e a ligação;
framing/identidade continuam terminais. O caso de shutdown que falhou no
incremental foi corrigido no código, com novo teste, sem alargar timeout nem
relaxar a exigência de NODE_LEAVE antes do fecho. Test/manual/integração pendentes.

## Atualização de fecho — Q-C-19

2026-11-11T19:42:24Z: Luis repetiu o manual V-033 com 53 checks/saída 0 e pediu revisão final, integração e fecho. A revisão técnica está concluída. Por instrução explícita foram retirados os arquivos TCP v1 e a variante abandonada; as referências anteriores a retenção de arquivos são históricas. Negativos de versão TCP v1 e todo UDP OTT1 continuam exigidos. [Estado e limpeza atuais](closure-walkthrough.md).
