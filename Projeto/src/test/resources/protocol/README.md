# Fixtures normativas do protocolo OTT v1

Criadas na TASK-002 como dados de referência, antes de existir um codec da aplicação. Contrato: [docs/07](../../../../../docs/07-API-AND-INTERFACES.md); modelo: [docs/06](../../../../../docs/06-DATA-MODEL.md); revisão e recursos: [V-002](../../../../../experiments/task-002-contract-review.md).

## Conteúdo e utilização futura

Cada caso tem um ficheiro .bin com os bytes efetivos e um .hex com os mesmos bytes em hexadecimal, em linhas de até 16 bytes. [manifest.json](manifest.json) contém comprimento, SHA-256, resultado esperado, fase de validação, valores interpretados ou motivo de rejeição. A fonte normativa do resultado é o contrato e a interpretação documentada, não o código que vier a ser testado.

Há 45 casos: 29 TCP (17 aceites, incluindo os 16 tipos ativos e uma variante UTF-8; 12 rejeitados) e 16 UDP (quatro aceites e 12 rejeitados). Os bytes foram escritos a partir de literais e comprimentos calculados separadamente, sem importar/usar encoder da aplicação. Foram revistos por uma inspeção temporária independente de offsets, tamanhos, hashes e fronteiras. A criação original na TASK-002 não executou codec; o codec/testes foram implementados depois, na TASK-004.

Correção do bootstrap por instrução do utilizador, ADR-013: REGISTER_OK não contém lease e devolve apenas endpoints configurados, todos com instanceId zero. O código 3 foi retirado: a antiga fixture de renovação é agora [tcp-invalid-retired-type](tcp-invalid-retired-type.hex), com bytes preservados e resultado obrigatório reject. A revisão da fixture REGISTER_OK foi calculada por literais de envelope/count/Endpoint, sem usar o codec Java. [Motivo, compatibilidade e validação](../../../../../experiments/bootstrap-discovery-correction.md).

TASK-004 deverá consumir os casos TCP: comparar encoding com bytes fixos, verificar os campos do decoding e rejeitar as entradas malformadas. TASK-019 deverá consumir os casos UDP. Testes de integração, autorização, caminhos/leases e JPEG real pertencem às respetivas tarefas. Não substituir os expected bytes por bytes gerados pelo codec testado nem reescrever estas fixtures só para obter testes verdes.

Uma mudança normativa exige atualizar contrato, manifesto, .bin/.hex e ADR, justificando a alteração. A mera divergência da implementação não demonstra erro da fixture.

## O que significa aceitar ou rejeitar

- accept/control-codec: formato, limites e campos válidos, sem afirmar que o emissor fez handshake ou tem autorização para essa mensagem.
- accept/udp-codec: cabeçalho, comprimentos e fragmentação válidos. Payload é sintético: AA, ABC ou AB repetido e CD. Não é vídeo JPEG para reproduzir, pelo que não demonstra RF-015 nem serve como ficheiro MJPEG.
- reject/framing: tamanho declarado fora de 37..65536; rejeição antes de alocação orientada pelo prefixo.
- reject/framing-eof: o fim do ficheiro simula EOF antes de completar prefixo/corpo. Numa conexão aberta deve aguardar os restantes bytes até timeout/EOF, não rejeitar apenas por uma leitura parcial.
- reject/control-codec ou udp-codec: valor, enumeração, estrutura ou limite inválido, com motivo específico no manifesto.

Os textos de reason no manifesto identificam a causa de rejeição nos testes, não novos códigos de erro na rede. O tipo ERROR da rede e os códigos 1–12 estão em docs/07. Não exigir resposta de rede a um frame cujo envelope não foi validado. Uma fixture pode violar mais de um invariante; o teste deve demonstrar rejeição pelo limite relevante, sem depender de qual exceção interna é lançada primeiro.

## Identidades dos exemplos

| Campo | Valor |
|---|---|
| requestId | 00112233-4455-6677-8899-aabbccddeeff |
| senderInstance / instanceId do Endpoint S1 | 10213243-5465-7687-98a9-bacbdcedfe0f |
| streamId A | 00000000-0000-4000-8000-000000000001 |
| serverEpoch / sessionId | deadbeef-0000-4000-8000-000000000001 |
| subscriptionId | fedcba98-7654-4321-8765-0123456789ab |
| traceId | 11223344-5566-4788-99aa-bbccddeeff00 |
| nodeIds | S1 origem, C1 cliente, C para o mínimo de um carácter, B para bootstrap |
| Endpoint de exemplo | S1, 10.0.0.1, TCP 5000, UDP 5001, SERVER=1 |

Os casos são independentes; não constituem uma sessão de rede. Identidades repetidas, IP e portas de exemplo não são configurações do CORE nem uma alegação de processos existentes. Os 16 bytes UUID preservam todos os bits, inclusive octetos acima de 7F. Os valores de exemplo não restringem a geração de UUID em execução.

## TCP: limites e offsets anotados

Todas as posições abaixo são offsets de bytes no ficheiro .bin, contando o prefixo de quatro bytes. O prefixo contém apenas o comprimento do corpo. Envelope mínimo: 1+1+16+(2+1)+16=37 bytes; ficheiro mínimo=41 bytes.

### HELLO_OK mínimo

[tcp-hello-ok-min.hex](tcp-hello-ok-min.hex) / [binário](tcp-hello-ok-min.bin): 41 bytes, prefixo 00000025 (=37).

| Offset | Tamanho | Interpretação |
|---:|---:|---|
| 0 | 4 | length=37 |
| 4 | 1 | version=1 |
| 5 | 1 | type=6, HELLO_OK |
| 6 | 16 | requestId |
| 22 | 2 | tamanho de senderId=1 |
| 24 | 1 | C, ASCII 43 |
| 25 | 16 | senderInstance |
| 41 | 0 | payload vazio; EOF limpo |

### HELLO com Endpoint

[tcp-hello.hex](tcp-hello.hex) / [binário](tcp-hello.bin): 77 bytes, prefixo 00000049 (=73). Envelope de S1: 38 bytes; Endpoint: 35 bytes.

| Offset | Tamanho | Interpretação |
|---:|---:|---|
| 22 | 4 | comprimento 2 e senderId S1 |
| 26 | 16 | senderInstance |
| 42 | 4 | comprimento 2 e Endpoint.nodeId S1 |
| 46 | 16 | Endpoint.instanceId |
| 62 | 2 | comprimento do host=8 |
| 64 | 8 | 10.0.0.1 |
| 72 | 2 | 5000, bytes 13 88 |
| 74 | 2 | 5001, bytes 13 89 |
| 76 | 1 | SERVER=1 |

REGISTER tem o mesmo payload, mas type=1; é uma consulta inicial sem inscrição ou atualização do endpoint. REGISTER_OK usa emissor B e contém apenas count=1 e o Endpoint configurado de S1, obrigatoriamente com instanceId zero. Não inclui lease nem disponibilidade. A identidade da instância no envelope é a do emissor imediato; o UUID zero aqui refere-se ao vizinho devolvido, não ao envelope.

REGISTER_OK corrigido tem 78 bytes: prefixo=74, envelope de 37 bytes, count u16 no offset 41, Endpoint.nodeId nos offsets 43–46, instanceId zero nos offsets 47–62, host/portas/papel nos offsets 63–77. O payload começa diretamente pelo count, sem os quatro bytes de lease do layout antigo. SHA-256: 467e3d6a5ca8c806bb9ec57c08de7e0a967294acfb37283f6d750a171af6b55e.

### STREAM_ANNOUNCE e UTF-8

[tcp-stream-announce.hex](tcp-stream-announce.hex) / [binário](tcp-stream-announce.bin): 106 bytes, prefixo 00000066 (=102). Payload de 64 bytes: StreamInfo=42, announceSeq=8, costMicros=8, path=6.

| Offset | Tamanho | Interpretação |
|---:|---:|---|
| 42 | 16 | streamId |
| 58 | 4 | serverId S1 com prefixo |
| 62 | 3 | title A: comprimento 1 + ASCII 41 |
| 65 | 1 | codec MJPEG=1 |
| 66 | 2 | fps=10 |
| 68 | 16 | serverEpoch |
| 84 | 8 | announceSeq=1 |
| 92 | 8 | costMicros=0 na fonte |
| 100 | 2 | path.count=1 |
| 102 | 4 | S1 com prefixo de comprimento |

[tcp-stream-announce-utf8.hex](tcp-stream-announce-utf8.hex) substitui o título A por 🎬 (U+1F3AC): um ponto de código Unicode, quatro bytes UTF-8 (F0 9F 8E AC), logo prefixo do título 0004 e comprimento do corpo 105 (00000069). Os campos seguintes deslocam-se três bytes. Este exemplo distingue UTF-8 comum de modified UTF-8: writeUTF codificaria o par de surrogates em seis bytes e produziria prefixo 0006, que não corresponde à fixture. Contar caracteres por pontos de código, não por unidades UTF-16.

### ACTIVATE

[tcp-activate.hex](tcp-activate.hex) / [binário](tcp-activate.bin): 112 bytes, prefixo 0000006C (=108). Emissor C1 e payload de 70 bytes.

| Offset | Tamanho | Interpretação |
|---:|---:|---|
| 42 | 16 | streamId |
| 58 | 16 | serverEpoch |
| 74 | 16 | subscriptionId |
| 90 | 8 | revision=1 |
| 98 | 4 | leaseMs=6000 (00001770) |
| 102 | 2 | expectedPath.count=2 |
| 104 | 4 | S1 com prefixo |
| 108 | 4 | C1 com prefixo |

ACK identifica a mesma subscrição/revisão, com grantedLeaseMs=6000; DEACTIVATE não tem lease nem path. SUBSCRIPTION_RENEW usa o mesmo payload de ACTIVATE, com type=14.

### ERROR

[tcp-error-no-route.hex](tcp-error-no-route.hex) / [binário](tcp-error-no-route.bin): 55 bytes, prefixo 00000033 (=51). Payload: 0006 = NO_ROUTE, 0B = ACTIVATE, 0008 = oito bytes do detalhe no route. Não codificar stack traces ou caminhos locais no detalhe.

## UDP: offsets e fragmentos

A posição 0 é o magic; não há prefixo TCP. Cabeçalho de 64 bytes: 4+1+1+2+16+16+8+8+4+2+2. Payload máximo=1200−64=1136.

| Offset | Tamanho | Campo |
|---:|---:|---|
| 0 | 4 | OTT1, bytes 4F 54 54 31 |
| 4 | 1 | version=1 |
| 5 | 1 | flags=0 |
| 6 | 2 | headerLength=64 |
| 8 | 16 | streamId |
| 24 | 16 | sessionId, igual ao serverEpoch |
| 40 | 8 | frameSeq não negativo |
| 48 | 8 | mediaTimeUs não negativo |
| 56 | 4 | frameLength |
| 60 | 2 | fragmentIndex |
| 62 | 2 | fragmentCount |
| 64 | variável | payload |

- [udp-min](udp-min.hex): frame de 1 byte AA, seq/timestamp=0, datagrama de 65 bytes.
- [udp-small](udp-small.hex): frame sintético ABC, seq=7, mediaTimeUs=10000, datagrama de 67 bytes.
- [udp-split-first](udp-split-first.hex): primeiro fragmento de frame de 1137 bytes, seq=8, mediaTimeUs=40000; count=2, index=0, 1136 bytes AB, datagrama=1200 bytes.
- [udp-split-last](udp-split-last.hex): mesmo frame, index=1, um byte CD, datagrama=65 bytes.

Concatenação dos payloads split-first/split-last deve produzir exatamente AB repetido 1136 vezes e CD, total 1137 bytes. São valores fixos de teste, não um algoritmo de reassemblagem implementado. O máximo de 2 MiB requer ceil(2097152/1136)=1847 fragmentos; não foram criados ficheiros de 2 MiB para esta revisão.

## Inventário de casos

Os nomes ligam os .hex legíveis; o .bin correspondente e os campos esperados estão no manifesto.

| Caso | Transporte | Bytes | Resultado e etapa |
|---|---|---:|---|
| [tcp-register](tcp-register.hex) | TCP | 77 | accept / control-codec |
| [tcp-register-ok](tcp-register-ok.hex) | TCP | 78 | accept / control-codec |
| [tcp-invalid-retired-type](tcp-invalid-retired-type.hex) | TCP | 42 | reject / control-codec |
| [tcp-node-leave](tcp-node-leave.hex) | TCP | 43 | accept / control-codec |
| [tcp-hello](tcp-hello.hex) | TCP | 77 | accept / control-codec |
| [tcp-hello-ok-min](tcp-hello-ok-min.hex) | TCP | 41 | accept / control-codec |
| [tcp-probe-ping](tcp-probe-ping.hex) | TCP | 50 | accept / control-codec |
| [tcp-probe-pong](tcp-probe-pong.hex) | TCP | 50 | accept / control-codec |
| [tcp-stream-announce](tcp-stream-announce.hex) | TCP | 106 | accept / control-codec |
| [tcp-stream-withdraw](tcp-stream-withdraw.hex) | TCP | 82 | accept / control-codec |
| [tcp-activate](tcp-activate.hex) | TCP | 112 | accept / control-codec |
| [tcp-activate-ack](tcp-activate-ack.hex) | TCP | 102 | accept / control-codec |
| [tcp-deactivate](tcp-deactivate.hex) | TCP | 98 | accept / control-codec |
| [tcp-subscription-renew](tcp-subscription-renew.hex) | TCP | 112 | accept / control-codec |
| [tcp-route-trace](tcp-route-trace.hex) | TCP | 97 | accept / control-codec |
| [tcp-trace-result](tcp-trace-result.hex) | TCP | 84 | accept / control-codec |
| [tcp-error-no-route](tcp-error-no-route.hex) | TCP | 55 | accept / control-codec |
| [tcp-stream-announce-utf8](tcp-stream-announce-utf8.hex) | TCP | 109 | accept / control-codec |
| [tcp-invalid-short-prefix](tcp-invalid-short-prefix.hex) | TCP | 3 | reject / framing-eof |
| [tcp-invalid-length-small](tcp-invalid-length-small.hex) | TCP | 40 | reject / framing |
| [tcp-invalid-length-large](tcp-invalid-length-large.hex) | TCP | 4 | reject / framing |
| [tcp-invalid-truncated-body](tcp-invalid-truncated-body.hex) | TCP | 40 | reject / framing-eof |
| [tcp-invalid-version](tcp-invalid-version.hex) | TCP | 41 | reject / control-codec |
| [tcp-invalid-type](tcp-invalid-type.hex) | TCP | 41 | reject / control-codec |
| [tcp-invalid-utf8](tcp-invalid-utf8.hex) | TCP | 41 | reject / control-codec |
| [tcp-invalid-trailing-byte](tcp-invalid-trailing-byte.hex) | TCP | 42 | reject / control-codec |
| [tcp-invalid-negative-probe](tcp-invalid-negative-probe.hex) | TCP | 50 | reject / control-codec |
| [tcp-invalid-role](tcp-invalid-role.hex) | TCP | 77 | reject / control-codec |
| [tcp-invalid-path-count](tcp-invalid-path-count.hex) | TCP | 267 | reject / control-codec |
| [udp-min](udp-min.hex) | UDP | 65 | accept / udp-codec |
| [udp-small](udp-small.hex) | UDP | 67 | accept / udp-codec |
| [udp-split-first](udp-split-first.hex) | UDP | 1200 | accept / udp-codec |
| [udp-split-last](udp-split-last.hex) | UDP | 65 | accept / udp-codec |
| [udp-invalid-short-header](udp-invalid-short-header.hex) | UDP | 63 | reject / udp-codec |
| [udp-invalid-no-payload](udp-invalid-no-payload.hex) | UDP | 64 | reject / udp-codec |
| [udp-invalid-magic](udp-invalid-magic.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-version](udp-invalid-version.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-flags](udp-invalid-flags.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-header-length](udp-invalid-header-length.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-index](udp-invalid-index.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-count](udp-invalid-count.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-zero-frame](udp-invalid-zero-frame.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-large-frame](udp-invalid-large-frame.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-negative-seq](udp-invalid-negative-seq.hex) | UDP | 67 | reject / udp-codec |
| [udp-invalid-datagram-size](udp-invalid-datagram-size.hex) | UDP | 1201 | reject / udp-codec |

## Estado e limites da revisão

V-002 conferiu originalmente pares binário/hex, tamanhos, hashes, cobertura dos então 17 tipos, exemplos de campos, rejeições de comprimento/enum/UTF-8, offsets UDP e a fronteira de fragmentação de 1137 bytes. A verificação de referências locais e do manifesto fez parte da revisão. Não se executou codec, JUnit ou streaming nessa tarefa; o codec foi criado na TASK-004. O inventário atual e a revisão normativa posterior constam no início deste documento e em ADR-013.

Os scripts usados para gerar/inspecionar testemunhos são temporários em /tmp e não pertencem ao projeto. Este diretório contém dados de teste e documentação; não contém encoder, decoder, leitor de vídeo ou lógica de produção.
