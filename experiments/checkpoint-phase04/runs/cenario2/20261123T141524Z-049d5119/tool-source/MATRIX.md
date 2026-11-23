# Funcionalidade → demonstração → evidência

Esta matriz é uma base para o artigo, não uma declaração de avaliação CORE ou
vídeo. "Previsto" exige execução na versão final da ferramenta. Runs FAIL e
limitações são preservadas no [handoff](../memory-bank/demonstrations/DEMO-001-checkpoint-phase04/HANDOFF.md).

| Funcionalidade concluída | Onde observar | Teste/evidência e limite |
|---|---|---|
| Configuração e papéis | Arranque, validation.log, perfis/PIDs | V-003; ConfigurationLoader real antes dos processos; JAR único |
| TCP full-duplex/v2/handshake | wire.jsonl: HELLO/HELLO_OK e mensagens em ambos os sentidos | V-005/V-007; TCP e JVMs reais, sem UDP |
| Descoberta obrigatória e ACTIVE | Discovery status, neighbors, frames18/19 | V-033 e V-034; cache e capabilities observadas, sem v1 |
| Cache e reparação sem bootstrap | Quarto passo: candidatos antes da morte, bootstrap parado, RepairStarted/REPAIR_HELLO | [Manual V-034 com mortes reais](../experiments/overlay-recovery/README.md); cenário1 do protótipo já passou |
| Reconexão/reinício | Quinto passo, novo PID/instance e ACTIVE | V-007; nova JVM volta a consultar bootstrap inicial |
| Isolamento/falha real | SIGKILL de relay/cliente, NeighborDown | V-034 e V-016; V-018/C3 anterior fecha TRANSPORT numa JVM, não prova SIGKILL |
| RTT periódico/falha vizinhança | metrics.links, sampleCount/cost/state, frames7/8 | V-010; proxy atrasa tráfego real; resultado atual da adaptação ainda pendente |
| Anúncios, catálogo e path-vector | Catálogo, seleção, origem/época/custo/candidatos | V-011/V-012; seleção INACTIVE separada da autorização forwarding |
| Trace e alternativas | trace-before/after.json e candidatos; CLI real | V-013/V-017; preservar NO_ROUTE inicial da CLI, causa ainda não estabelecida |
| Ativação com ACK e intenção local | Primeiro next, localConsumers/forwarding, tipos11/12 | V-014; V-018/C2 reutilizado; resultado inicial do ticket não é estado vivo da árvore |
| Agregação | Dois clientes, um downstream no servidor, um ACTIVATE inicial do tronco | V-018/C2; oráculo wire e snapshot; cenário1 já demonstrado no protótipo |
| Renovação e leases | Segundo next, tipo14 e ACK após duração inicial6s | V-015/L01–L24; não confundir SUBSCRIPTION_RENEW com ACTIVATE |
| Remoção/poda seletiva | Sexto next: sai C1, C2 permanece; últimoA sai e B fica | V-015 e V-018/C2; DEACTIVATE real |
| Procura abandonada | Morte real de C2 sem cancelar B, remoção do ramo | V-015/V-016; remoção por falha TCP, expiração isolada fica na evidência histórica |
| Recuperação upstream | Quarto next: forwarding muda com o mesmo PID/token | V-016/R01–R18 e V-018/C3; R17 inicial sem causa estabelecida |
| Histerese/troca confirmada | Terceiro next cenário2: pulso, atraso sustentado, STARTED/SUCCESS e revisão | V-017/S01–S18 e V-018/C4. Demonstração nova com RTT real ainda por concluir/validar; V-018 anterior usa custos controlados |
| Streams/clientes simultâneos | C1=A, C2=A+B; cancelarA não afeta B | V-018/C1 e C2. Dois servidores/quatro consumidores têm evidência anterior44/44; cenários1/2 do enunciado têm um servidor, dois fluxos |
| Limites, concorrência, shutdown | result.json, portas reutilizáveis e regressão | 1459 testes de baseline; cenários de saturação/ordem/clock controlado são testes, não simulação apresentada como runtime |
| Slot de dados | production-cli.log: NOT_IMPLEMENTED play; banner do HTML | Nenhuma alegação de funcionalidade UDP/MJPEG/player/vídeo |
| Figura1/11 nós | cenario3, grafo O1–O11 | Descritor criado; ainda não executado. Não é execução CORE |

Fontes principais: [V-018 e 44/44 anteriores](../experiments/milestone-2/README.md),
[regressão e fecho PHASE-04](../docs/phases/PHASE-04-CLOSURE-REVIEW.md),
[oráculos V-017](../memory-bank/bolts/024-arvores/ddd-03-test-report.md).
