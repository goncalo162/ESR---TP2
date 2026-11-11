# Fixtures propostas — controlo TCP uniforme v2

Referências independentes aprovadas para TCP v2. Os mesmos vetores foram comparados pelo codec Java na regressão de 1065 testes.

O gerador build-reference-fixtures.py escreve campos literais (UUID, strings, inteiros e listas) com Python struct, sem importar código Java, ler fixtures v1 ou trocar um byte do seu cabeçalho. O auditor separado audit-reference-fixtures.py não importa o gerador: percorre campos, limites, versões e payloads, confere bin/hex/hashes e os resultados declarados. A independência é em relação ao codec de produção; continua necessária revisão humana dos vetores e comparação com Java após aprovação.

Contrato: [Design](../../../../memory-bank/bolts/034-overlay-extra/ddd-02-technical-design.md), [interfaces](../../../../docs/07-API-AND-INTERFACES.md#extra-01-control-v2). Dados/expectativas em [manifest.json](manifest.json); resultado da auditoria offline em [audit.json](audit.json).

50 vetores TCP: 22 aceites, 23 rejeitados terminalmente e cinco rejeições de conteúdo, cobrindo todos os 18 tipos (1,2,4..19). reject-content é condicional à identidade/sessão corrente e autorização confirmadas pelo owner; o auditor offline não comprova essa condição. Inputs de sessão errada, clocks, FIFO, CLI e write/flush exigem os testes D02/D07/D08/D11 futuros.

```sh
python3 experiments/construction/034-overlay-extra/protocol-v2-proposal/audit-reference-fixtures.py
```

O gerador é ferramenta de preparação do oráculo, não parte do runtime; não regenerar fixtures automaticamente nos testes a partir do encoder. Os binários e os campos esperados ficam estáticos e sujeitos a revisão. Após aprovação, migrar os recursos TCP em Projeto/src/test/resources/protocol/ e o manifesto; manter as 16 fixtures UDP OTT1/version 1 byte a byte. O alvo é 66 fixtures operacionais (50 TCP v2 + 16 UDP). Os arquivos TCP v1 foram retirados no fecho a pedido explícito de Luis (Q-C-19), sem suporte runtime v1. O manifesto UDP de referência mantém só os 16 vetores de vídeo da base integrada.

Este ensaio de referência não executa Java/Maven, TCP, GUI, CORE, vídeo ou reparação. IMP-034 aprende; IMP-035 repara depois.

| Vetor | Bytes | Resultado esperado |
|---|---:|---|
| [tcp-register](fixtures/tcp-register.hex) | 77 | accept / codec |
| [tcp-register-ok](fixtures/tcp-register-ok.hex) | 78 | accept / codec |
| [tcp-node-leave](fixtures/tcp-node-leave.hex) | 43 | accept / codec |
| [tcp-node-leave-min](fixtures/tcp-node-leave-min.hex) | 42 | accept / codec |
| [tcp-hello](fixtures/tcp-hello.hex) | 85 | accept / codec |
| [tcp-hello-ok](fixtures/tcp-hello-ok.hex) | 45 | accept / codec |
| [tcp-probe-ping](fixtures/tcp-probe-ping.hex) | 50 | accept / codec |
| [tcp-probe-pong](fixtures/tcp-probe-pong.hex) | 50 | accept / codec |
| [tcp-stream-announce](fixtures/tcp-stream-announce.hex) | 106 | accept / codec |
| [tcp-stream-announce-utf8](fixtures/tcp-stream-announce-utf8.hex) | 109 | accept / codec |
| [tcp-stream-withdraw](fixtures/tcp-stream-withdraw.hex) | 82 | accept / codec |
| [tcp-activate](fixtures/tcp-activate.hex) | 112 | accept / codec |
| [tcp-activate-ack](fixtures/tcp-activate-ack.hex) | 102 | accept / codec |
| [tcp-deactivate](fixtures/tcp-deactivate.hex) | 98 | accept / codec |
| [tcp-subscription-renew](fixtures/tcp-subscription-renew.hex) | 112 | accept / codec |
| [tcp-route-trace](fixtures/tcp-route-trace.hex) | 97 | accept / codec |
| [tcp-trace-result](fixtures/tcp-trace-result.hex) | 84 | accept / codec |
| [tcp-error-no-route](fixtures/tcp-error-no-route.hex) | 55 | accept / codec |
| [tcp-error-discovery](fixtures/tcp-error-discovery.hex) | 64 | accept / codec |
| [tcp-discovery-query](fixtures/tcp-discovery-query.hex) | 77 | accept / codec |
| [tcp-discovery-snapshot-empty](fixtures/tcp-discovery-snapshot-empty.hex) | 87 | accept / codec |
| [tcp-discovery-snapshot](fixtures/tcp-discovery-snapshot.hex) | 122 | accept / codec |
| [tcp-invalid-short-prefix](fixtures/tcp-invalid-short-prefix.hex) | 3 | reject / framing |
| [tcp-invalid-length-small](fixtures/tcp-invalid-length-small.hex) | 40 | reject / framing |
| [tcp-invalid-length-large](fixtures/tcp-invalid-length-large.hex) | 4 | reject / framing |
| [tcp-invalid-length-unsigned](fixtures/tcp-invalid-length-unsigned.hex) | 4 | reject / framing |
| [tcp-invalid-truncated-body](fixtures/tcp-invalid-truncated-body.hex) | 41 | reject / framing |
| [tcp-invalid-version-0](fixtures/tcp-invalid-version-0.hex) | 45 | reject / version |
| [tcp-invalid-version-1](fixtures/tcp-invalid-version-1.hex) | 45 | reject / version |
| [tcp-invalid-version-3](fixtures/tcp-invalid-version-3.hex) | 45 | reject / version |
| [tcp-invalid-version-255](fixtures/tcp-invalid-version-255.hex) | 45 | reject / version |
| [tcp-invalid-retired-type](fixtures/tcp-invalid-retired-type.hex) | 50 | reject / codec |
| [tcp-invalid-type](fixtures/tcp-invalid-type.hex) | 42 | reject / codec |
| [tcp-invalid-identity-utf8](fixtures/tcp-invalid-identity-utf8.hex) | 45 | reject / identity |
| [tcp-invalid-trailing-byte](fixtures/tcp-invalid-trailing-byte.hex) | 46 | reject / codec |
| [tcp-invalid-negative-probe](fixtures/tcp-invalid-negative-probe.hex) | 50 | reject / codec |
| [tcp-invalid-role](fixtures/tcp-invalid-role.hex) | 77 | reject / codec |
| [tcp-invalid-path-count](fixtures/tcp-invalid-path-count.hex) | 231 | reject / codec |
| [tcp-invalid-capability-not-required](fixtures/tcp-invalid-capability-not-required.hex) | 85 | reject / handshake |
| [tcp-invalid-capability-missing](fixtures/tcp-invalid-capability-missing.hex) | 85 | reject / handshake |
| [tcp-invalid-capability-reserved](fixtures/tcp-invalid-capability-reserved.hex) | 85 | reject / handshake |
| [tcp-invalid-selection](fixtures/tcp-invalid-selection.hex) | 45 | reject / handshake |
| [tcp-snapshot-generation-zero](fixtures/tcp-snapshot-generation-zero.hex) | 87 | reject-content / content |
| [tcp-snapshot-duplicate](fixtures/tcp-snapshot-duplicate.hex) | 157 | reject-content / content |
| [tcp-snapshot-invalid-role](fixtures/tcp-snapshot-invalid-role.hex) | 122 | reject-content / content |
| [tcp-snapshot-invalid-count](fixtures/tcp-snapshot-invalid-count.hex) | 87 | reject-content / content |
| [tcp-snapshot-trailing](fixtures/tcp-snapshot-trailing.hex) | 88 | reject-content / content |
| [tcp-snapshot-truncated-endpoint](fixtures/tcp-snapshot-truncated-endpoint.hex) | 90 | reject / codec |
| [tcp-snapshot-field-length](fixtures/tcp-snapshot-field-length.hex) | 89 | reject / codec |
| [tcp-discovery-over-cap](fixtures/tcp-discovery-over-cap.hex) | 32845 | reject / codec |
