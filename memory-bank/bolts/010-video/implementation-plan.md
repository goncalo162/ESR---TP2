---
unit: "005-video"
bolt: "010-video"
stage: "plan"
status: "complete"
---
# IMP-016 — Plan formal: codec UDP OTT1 e fragmentação

Proposta tecnicamente aceite pelo utilizador, incluindo API mínima, imutabilidade,
validação antes de alocar e oráculos V-019. **Plan formal aprovado pelo utilizador**. Primeiro passo de Implement aprovado; comportamentos autorizados posteriormente
pelo utilizador nos mesmos dois ficheiros. Implement aprovado posteriormente e Test autorizado pelo utilizador. Tipo Simple: Plan → Implement → Test.
Base operacional b6de8488f6dc99e3bf490d4cb88b31840089cb30; ramo autorizado
imp-016-udp. Main mantém o apontador da base. Sem staging/commits/push.

## 1. Objetivo, fontes e dependências

Converter um frame opaco em datagramas OTT1 limitados e validar um fragmento sem
abrir sockets, ler media, descodificar JPEG ou emitir vídeo. É codec puro, não
reassemblagem, receção/autorização de rede, player ou integração de streaming.

[ADD/IMP-016](../../../add.md#imp-016), secções2/5/6/8–12; [specs](../../../specs.md),
[intent](../../intents/001-ott-streaming/requirements.md),
[unidade](../../intents/001-ott-streaming/units/005-video/unit-brief.md),
[história](../../intents/001-ott-streaming/units/005-video/stories/001-definir-codec-udp-e-fragmentacao-de-frames.md),
[TASK-019](../../../docs/tasks/PHASE-05/TASK-019.md),
[contratos UDP/5 e perfil/7](../../../docs/07-API-AND-INTERFACES.md),
[testes/V-019](../../../docs/09-TESTING-STRATEGY.md),
[DoD](../../../docs/14-DEFINITION-OF-DONE.md) e progresso15.
Código real inspecionado: StreamPacket placeholder de0 bytes, sem referências
Java a consumir a sua API; StreamPacketTest ausente. MessageCodec/ProtocolException,
RuntimeConfiguration/POM e fixtures existentes lidos, sem alterações.

Dependência IMP-015/009-arvores complete e história implemented=true; TASK-018 DONE,
unidade004/PHASE-04 complete/DONE. Fecho reconciliado em009 e progresso15 referencia
integração1ee8db6 e confirmação humana. Não reabrir esses incrementos.
DEMO-001 é apoio separado, não dependência/gate de PHASE-05, por instrução explícita.
O seu handoff antigo não prevalece sobre a autorização atual para este Plan.

ADR-002/006/007/009/021/024 reutilizadas; preservar ADR-026 (JUnit só em testes,
sem nova dependência runtime,CI,linter,JaCoCo ou limiar percentual). Sem nova ADR
ou alteração de wire/política/hard caps. Decisão aceite inviável exige parar a
parte afetada e seguir ADD11; não há incompatibilidade material identificada aqui.

## 2. Allowlist Java futura e coordenação

Somente na etapa Implement, após autorização:

1 - `Projeto/src/main/java/pt/uminho/esr/ott/streaming/StreamPacket.java`.
2 - `Projeto/src/test/java/pt/uminho/esr/ott/streaming/StreamPacketTest.java`.

Sem StreamPacketCodec.java ou StreamPacketCodecTest.java adicionais. A nomenclatura
legada de TASK-019/6 é clarificada por este Plan/ADD, sem alargar o âmbito.
Manter todas as fixtures e testes históricos; vetores adicionais necessários
ficam como literais independentes dentro do teste autorizado.

Colega não usa este diretório, conforme confirmação do utilizador. Ramo
imp-016-udp criado por autorização explícita; sem alteração do ref main,
checkout/reset do trabalho do colega ou integração inferida. demo/, respetivo
HANDOFF/bolt e experiências de checkpoint permanecem intactos. Atualizações de
estado aqui pertencem só ao IMP-016; blocos históricos da demo são preservados.
Numeração global continua não coordenada: usar apenas IMP-016-LOCAL-01/02,
sem reservar Q-C globais. Não contactar pessoas ou presumir acordo do colega.

## 3. API mínima tecnicamente aceite

StreamPacket como record imutável: streamId UUID,sessionId UUID,frameSeq long,
mediaTimeUs long,frameLength long,fragmentIndex int,fragmentCount int,payload byte[].
Cópia defensiva do payload na entrada e na saída; não guardar input reutilizável
de receção. frameLength u32 é lido para long antes da validação/conversão.

Assinaturas conceptuais aceites, a implementar no mesmo ficheiro:

- `encode(StreamPacket)` → byte[] do datagrama completo.
- `decode(byte[])` → StreamPacket de um datagrama exato.
- `decode(byte[], offset, length)` → StreamPacket da região declarada.
- `fragment(streamId, sessionId, frameSeq, mediaTimeUs, byte[])` → List<StreamPacket> imutável.

[PRESSUPOSTO] Erros explícitos de formato reutilizam ProtocolException existente
como diagnóstico local; isto não cria/envia ERROR de TCP, não decide política de
rede e não modifica ProtocolException/ErrorCode. Valores de programação inválidos
no construtor/argumentos são rejeitados antes das cópias. Não usar igualdade
referencial de arrays como oráculo de conteúdo; testes comparam bytes/campos.

Codec sem estado, threads, relógios, schedulers, filas, caches ou sockets.
Não exige shutdown próprio; ownership serial/contexto ativo dos nós existentes
não é modificado. Não introduzir ObjectInputStream ou dependências runtime.

## 4. Bytes fixos UDP v1, separados do controlo TCP v2

Tudo big-endian. Cada buffer representa o datagrama UDP de aplicação, sem prefixo
de framing TCP e sem contar IP/UDP. TCP permanece v2/19 tipos/código3 rejeitado;
UDP OTT1 permanece **version1**,flags0,header64,payload≤1136,total≤1200.
Uma configuração não altera estes bytes/caps.

| Offset | Bytes | Campo/invariante |
|---:|---:|---|
| 0 | 4 | magic ASCII OTT1 |
| 4 | 1 | version=1 |
| 5 | 1 | flags=0; reservadas rejeitadas |
| 6 | 2 | headerLength u16=64 |
| 8 | 16 | streamId UUID; todos os bits preservados |
| 24 | 16 | sessionId UUID=serverEpoch |
| 40 | 8 | frameSeq i64 não negativo |
| 48 | 8 | mediaTimeUs i64 não negativo, timeline relativa |
| 56 | 4 | frameLength u32,1..2097152 |
| 60 | 2 | fragmentIndex u16,0..count−1 |
| 62 | 2 | fragmentCount u16,1..1847 |
| 64 | variável | payload exato,1..1136 |

O codec valida sintaxe, não autentica UUID/endpoint nem determina sessão ativa.
Comparação com HELLO/upstream/época e monotonias entre datagramas pertencem aos
consumidores posteriores. Um datagrama não cria procura/rota ou renova lease.
mediaTimeUs não é UTC/latência unilateral. JPEG é opaco; bytes sintéticos válidos
não precisam de ser uma imagem. Fonte de cinco dígitos tem cap99999 em IMPs
posteriores; codec aceita até2MiB, sem confundir limites de container e protocolo.

## 5. Fragmentação, offsets e ordem de validação

Com L já validado entre1 e2097152:
count=1+(L−1)/1136; offset=index×1136 calculado com aritmética verificada;
expectedPayload=min(1136,L−offset). count tem de coincidir exatamente com a
fórmula. Todos os fragmentos exceto o último têm1136; último tem resto positivo,
incluindo1136 quando L é múltiplo. No máximo2MiB:1847 fragmentos,último payload96.
Identidades/seq/mediaTime/frameLength/count são comuns, índice0..count−1.

Decoder:

1 - Validar array/região sem overflow e comprimento65..1200 antes de ler campos.
    Backing array maior é permitido só no overload com uma região válida;
    encode/decode de datagrama inteiro nunca ignora bytes extra.
2 - Ler magic,versão,flags,header e escalares com acessos limitados à região.
3 - Converter unsigned corretamente; validar seq/time,frameLength,count,index,
    count calculado,offset e payload/tamanho total exatos,sem wrap/truncagem.
4 - Só depois criar o valor/cópia de payload limitada a1136. Nunca alocar um
    frame/buffer com comprimento escolhido por metadados ainda não validados.

Encoder valida valor e tamanho exato antes de criar datagrama limitado a1200.
Fragmentação valida IDs,seq/time e tamanho real do array antes da lista/payloads;
lista≤1847 e soma dos payloads≤2MiB. Sem fila ilimitada, montagens ou buffers por
frame declarado em decode. Revisão de código confirma a ordem; rejeição de campos
forjados é prova adicional, não uma medição de heap inventada.

## 6. V-019 — oráculos planeados, ainda não executados em Java

16 vetores UDP preservados,4 positivos e12 negativos. Ler os binários/hex e
manifesto independentes; expectativas nunca geradas pelo encoder de produção.
Cada grupo abaixo é um conjunto de casos, não promessa de contagem JUnit.

| ID local | Exercício/asserções |
|---|---|
| U01 | Quatro positivos: decode confere todos os campos/payloads e encode do modelo esperado coincide com bin/hex golden, byte a byte |
| U02 | Doze negativos existentes rejeitados pelas condições documentadas; nenhum byte/expected alterado para obter verde |
| U03 | Offsets0..64,big-endian,UUID com bits altos preservados; seq/time0 e extremos válidos; literais adicionais independentes no teste |
| U04 | Tamanhos de frame1,1135,1136,1137,2272,2273,99999,2097151,2097152; rejeitar0 e2097153; máximo1847/último96 |
| U05 | Recombinação independente de payloads recupera exatamente o frame,padrão sintético conhecido,sem bytes perdidos/duplicados; datagramas≤1200 |
| U06 | Campos hostis: u32 elevados/overflow,count0/incoerente/index fora da gama,seq/time negativos,versão2,flags/header inválidos,payload curto/extra |
| U07 | Overload de região com prefixo/sufixo e offset não nulo; limites e overflow de offset/length; array maior não muda cap da região |
| U08 | Cópias defensivas nos dois sentidos e lista imutável; inputs/outputs mutados não alteram o pacote; rejeição antes de cópias dependentes de metadados |

Round-trip apenas complementar. Fronteiras de fragmentação/UUID/campos não são
substituídas por round-trip. Sem JPEG real,frame assembler,socket ou relógio/
transporte falso desnecessários num codec puro. Protocolos TCP/UDP testados como
versões distintas, sem tocar MessageCodec/fixtures TCP.

## 7. Sequência e condições de fecho futuras

1 - Plan formal: guardar este artefacto,estado/evidência e patch autorizado;
    executar apenas V-ADD/auditoria documental,preservar diff/hashes; parar.
2 - Implement só após aprovação/autorização: tipos e testes significativos,
    demonstração de falhas adequadas e comportamentos incrementais nos dois
    ficheiros,sem enfraquecer históricos. Reapresentar Implement antes de Test.
3 - Test só após autorização: V-019 executado,regressão JDK21 e clean verify,
    logs/XML/JAR/hash e manual humano reproduzível com evidência/operador.
4 - Fecho apenas com testes/validações,manual humano,revisão final Denji e
    integração comprovados,conforme autorização do utilizador/DoD. Sem gates
    externos adicionais no IMP-016. Sem alegar GUI/CORE/vídeo entregue por codec.

Os1459 testes registados na base/DEMO/fechos anteriores são históricos,não testes
atuais do IMP-016. Não foi executado Maven,JUnit ou Java nesta etapa.

## 8. Registo local e patch autorizado

IMP-016-LOCAL-01 — coordenação: utilizador confirmou colega fora deste diretório
e autorizou ramo imp-016-udp na base b6de848. Separação local satisfeita;
numeração global continua por coordenar. Apenas IDs locais,sem reserva Q-C.

IMP-016-LOCAL-02 — ligação fictícia no template DDD: patch exato de uma linha
aplicado por autorização explícita: representar o destino fictício como caminho
literal em backticks,no exemplo JWT da linha551. Não altera validador/âmbito,
não cria ADR fictícia e não altera wire. **RESOLVIDA** após V-ADD pós-patch
com0 erros; negativo anterior preservado. [Patch/evidência](../../../experiments/construction/010-video/plan-01/README.md).

Plan aprovado. O utilizador aprovou depois tipos/testes vermelhos e autorizou
os comportamentos nos dois ficheiros. Implement aprovado; parar no checkpoint Test, manual humano pendente; sem staging/commits/push. Main preservado na base.
