# Correção — Bootstrap exclusivamente para localização inicial dos vizinhos

Data: 27-10-2026. Autorização: pedido explícito do utilizador para corrigir Markdown e código existente. Revisão de partida: `4105429a76f67a5acf0579db8719b0ba2460b549`. Preservados os ficheiros locais da revisão anterior: docs/15-PROJECT-PROGRESS.md e experiments/phase-01-readiness-review.md. Não foi iniciado o transporte, o servidor bootstrap, CORE ou outra tarefa da fase 2.

## Papel definido

1. O nó consulta o bootstrap identificando-se.
2. O bootstrap valida a consulta contra a configuração e devolve a localização dos vizinhos configurados, incluindo os offline.
3. A conexão de consulta termina após a resposta completa. Comunicação, handshake, saída, reconexão, métricas, rotas e vídeo são tratados diretamente pelos nós.

Não existe inscrição de nós ativos, lease, renovação, heartbeat, monitorização, aviso de saída ou arbitragem de instâncias no bootstrap. Pedidos repetidos ou concorrentes não criam estado e devolvem a mesma lista configurada. O requerente não altera o grafo nem os endpoints configurados. Após descoberta bem-sucedida, não existem consultas periódicas; retentar o pedido inicial sem resposta é diferente de renovar um registo.

## Alterações concretas

- Arquitetura, modelo, requisitos, interfaces, guia da IA, testes planeados, fases/tarefas e plano histórico alinhados com [ADR-013](../docs/12-RISKS-AND-DECISIONS.md).
- TASK-006 passa a consulta da configuração, sem NodeRegistry ou expiração de membros. TASK-007/009 exigem demonstrar reconexão direta com bootstrap parado; TASK-008 envia NODE_LEAVE apenas a vizinhos diretos. Estas tarefas continuam TODO.
- Removido MessageType.REGISTER_RENEW; o código 3 fica retirado/reservado, sem renumerar os restantes. Há agora 16 tipos ativos, com IDs 1, 2 e 4–17.
- Substituído ControlMessage.RegistrationResult(leaseMs, neighbors) por NeighborList(neighbors). REGISTER_OK não tem prazo nem estado de disponibilidade. O seu payload é apenas count u16 seguido dos Endpoints configurados, todos com instanceId zero. O codec rejeita uma instância viva nesta resposta.
- REGISTER/REGISTER_OK mantêm os nomes e IDs: significam consulta/resposta, não registo duradouro. O Endpoint do requerente permite verificar correspondência com a configuração; não será guardado ou usado para atualizar vizinhos.
- Removido apenas o placeholder vazio bootstrap/NodeRegistry.java. BootstrapServer.java continua apenas comentado, com o âmbito clarificado; não foi criada implementação de rede.
- Mantidos os leases de subscrição ACTIVATE/ACTIVATE_ACK/SUBSCRIPTION_RENEW e a monitorização entre vizinhos, que não pertencem ao bootstrap.
- Sem alterações ao POM, aos perfis de configuração ou aos XML CORE.

## Contrato e fixtures

O contrato corrigido continua identificado como v1: ainda não existe runtime de transporte ou sistema externo implantado. A antiga resposta REGISTER_OK com lease é incompatível e não tem fallback. Uma implementação futura deve utilizar exclusivamente o contrato atual de [07](../docs/07-API-AND-INTERFACES.md).

As alterações das fixtures foram calculadas independentemente do encoder Java, com literais hexadecimais para envelope, count e Endpoint. A contagem de vizinhos começa imediatamente após o envelope, no offset 41, em tcp-register-ok; o seu instanceId ocupa os offsets 47–62 e é todo zero.

| Fixture | Resultado da correção |
|---|---|
| tcp-register-ok.bin/.hex | Removidos quatro bytes de lease, instanceId do vizinho zero; frame 78 bytes, corpo 74 |
| SHA-256 de tcp-register-ok.bin | `467e3d6a5ca8c806bb9ec57c08de7e0a967294acfb37283f6d750a171af6b55e` |
| Antiga tcp-register-renew | Renomeada tcp-invalid-retired-type; os bytes são idênticos, mas o código 3 tem agora resultado obrigatório reject |
| Restantes fixtures | Bytes preservados; as 16 UDP não sofreram alterações |

Inventário continua com 45 pares: 29 TCP, agora 17 aceites (16 tipos + variante UTF-8) e 12 rejeitados; 16 UDP, com quatro aceites e 12 rejeitados. Manifesto e README atualizados, sem gerar os expected bytes com o codec testado. As evidências originais V-002/V-004 conservam os resultados da sua execução com notas explícitas sobre esta revisão.

## Validação executada

A partir de Projeto/:

```bash
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -o -Dstyle.color=never clean package
```

Código 0, BUILD SUCCESS, terminado em 27-10-2026 às 14:34:31 UTC:

| Suite | Testes | Falhas | Erros | Ignorados |
|---|---:|---:|---:|---:|
| ConfigurationLoaderTest | 65 | 0 | 0 | 0 |
| MessageCodecTest | 66 | 0 | 0 | 0 |
| Total | 131 | 0 | 0 | 0 |

Relatórios XML em Projeto/target/surefire-reports/. JAR gerado: Projeto/target/ott-streaming-1.0-SNAPSHOT.jar. Java 21/Maven e versões fixadas mantidos.

A contagem total coincide com a anterior, mas os casos mudaram: desapareceu a renovação das duas parametrizações de mensagens aceites; a mesma mensagem entrou nos casos inválidos e foi acrescentado um teste específico da resposta de bootstrap. Os testes verificam bytes de referência, código 3 inválido, ausência do lease, UUID zero no vizinho, rejeição do layout antigo com lease e rejeição de vizinho com instância viva. Casos de limites/imutabilidade e integração em memória foram adaptados ao payload NeighborList. Testes de domínio u32 continuam a verificar leases das subscrições, não de registo.

Esta execução comprova configuração e contrato/codec locais. Não comprova resposta de servidor real, ausência de tráfego periódico no runtime, reconexão com bootstrap parado ou comunicação entre processos; essas validações estão explicitamente previstas em V-006/007/009 e ainda não foram executadas.

Revisão final de integridade: os 45 pares coincidem com o manifesto/hex; em comparação com HEAD, apenas tcp-register-ok altera bytes e a antiga renovação mantém os seus bytes sob o nome de caso inválido. Os 16 casos UDP estão intactos. Verificadas 1476 referências relativas a ficheiros, sem caminhos em falta, e correspondência exata de V-006/007/009 entre tarefas e estratégia de testes. As tarefas conservam as 13 secções obrigatórias e estados corretos. git diff --check passou; CORE, POM, código config e perfis permanecem sem alterações. Os manifests de compilação já versionados em target foram regenerados automaticamente pelo Maven; não representam funcionalidades novas.

## Estado e próxima tarefa

PHASE-01/TASK-001–004 permanecem DONE; PHASE-02/TASK-005–009 permanecem TODO. Nenhum requisito global parcialmente implementado passou a DONE. Próxima tarefa: [TASK-005](../docs/tasks/PHASE-02/TASK-005.md). A correção não inicia a sua execução.
