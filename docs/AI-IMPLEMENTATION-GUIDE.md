# Guia para a IA implementadora

## Autorização e fonte de verdade

Este plano não autoriza iniciar a construção durante a fase de planeamento. Quando o utilizador pedir implementação, executar uma tarefa de cada vez e respeitar o PDF. Ler [00](00-README.md) e [01](01-PROJECT-OVERVIEW.md), depois [02](02-REQUIREMENTS.md), [12](12-RISKS-AND-DECISIONS.md), [04](04-ARCHITECTURE.md), [06](06-DATA-MODEL.md) e [07](07-API-AND-INTERFACES.md). Não assumir que a documentação histórica é uma implementação existente.

## Antes de cada tarefa

1. Ler [15](15-PROJECT-PROGRESS.md), a fase em [08](08-IMPLEMENTATION-ROADMAP.md), a tarefa e os requisitos em [13](13-REQUIREMENTS-TRACEABILITY.md).
2. Inspecionar o repositório real, instruções AGENTS.md aplicáveis, git status e alterações do utilizador. Comparar com a auditoria; não apagar trabalho existente.
3. Confirmar que todas as dependências estão DONE e que os artefactos/testes que as justificam existem. Para TASK-005–031, confirmar também revisão aceite e integração na base comum; uma branch em desenvolvimento não é dependência disponível. Uma marca DONE desatualizada exige revisão.
4. Verificar gates em docs/12 e as decisões/contratos citados. Diferenciar exigência do enunciado, hipótese, escolha técnica e sugestão.
5. Consultar a [coordenação da equipa](team/00-COORDINATION.md) e o documento pessoal de Waheed/Fillmore/Denji. Respeitar a tarefa explicitamente pedida e a atribuição; numa escolha delegada, selecionar o menor ID próprio em TODO com dependências integradas e gate cumprido. Se existir IN PROGRESS da mesma pessoa, rever handoff e retomar primeiro. A primeira tarefa histórica foi TASK-001, já DONE; não a repetir por causa da distribuição.
6. Marcar a tarefa IN PROGRESS no próprio ficheiro e em docs/15. Registar início, âmbito e validações planeadas, sem mudar as restantes para DONE.

## Durante a implementação

Implementar apenas a responsabilidade da tarefa atual; não criar arquitetura ou funcionalidades futuras por antecipação. Uma interface planeada não significa que o componente já existe. Usar fakes nas fronteiras ainda não implementadas, explicitando-os nos testes; não usá-los como prova de integração real.

Regra explícita do utilizador (ADR-013): bootstrap serve exclusivamente a localização inicial dos vizinhos pela configuração. Não implementar registo vivo, leases/renovações, heartbeats, monitorização ou avisos de saída no bootstrap; código 3 foi retirado. Saídas, identidades de execução, reconexão e métricas pertencem aos vizinhos; leases de subscrição das streams permanecem no routing.

Respeitar proprietário de estado, snapshots, limites, unidades, IDs/epochs e contratos de bytes. Testes devem verificar comportamento com oráculos independentes, incluindo os casos de erro da tarefa. Não adicionar dependências runtime sem razão e ADR. Evitar alterações ao Docker/underlay fora das tarefas próprias. Não substituir CORE por localhost para aceitação final.

Uma alteração legítima de desenho deve atualizar ADR, modelo, interfaces, tarefa, testes e rastreabilidade antes de afetar tarefas futuras. Se contradizer o enunciado, parar a decisão afetada e pedir esclarecimento; a IA não pode dispensar requisitos obrigatórios. Não enviar mensagens à equipa/docente nem submeter artefactos externamente por inferência.

## Depois de cada tarefa

Executar verificações relevantes, guardar comandos, ambiente e resultados reais. Verificar todos os critérios e [14](14-DEFINITION-OF-DONE.md). Se falhar, investigar dentro do âmbito; sem evidência não marcar DONE. Testes não executáveis no ambiente ficam pendentes e explicados.

Se cumprir, marcar DONE no ficheiro e em docs/15, ligar evidência em docs/13 e indicar próxima tarefa desbloqueada. Um requisito com várias tarefas só fica satisfeito quando todas e respetivas validações estiverem concluídas; estados parciais não equivalem a DONE. Registar alterações de decisão e repetir apenas verificações afetadas por mudanças novas.

Para TASK-005–031, obter revisão do membro indicado no ficheiro TASK sobre a versão final e integrar na base comum antes de marcar DONE. Se o código local passa mas revisão/integração estão pendentes, manter IN PROGRESS com handoff explícito. Uma tarefa futura que aguarda normalmente a dependência continua TODO. Não modificar os mesmos ficheiros em paralelo no mesmo checkout, nem implementar tarefas atribuídas a outra pessoa sob pretexto de desbloquear a própria sem redistribuição documentada. O autor cria/executa os seus testes; o revisor verifica a evidência e resultados relevantes. A regra não reabre TASK-001–004.

## Bloqueios

| Situação | Procedimento |
|---|---|
| Dependência não implementada | Não contornar com estado fictício; voltar à dependência ou escolher tarefa efetivamente independente |
| Requisito ambíguo | Consultar PDF e docs/12; se hipótese existente serve e não é bloqueante, preservá-la; se muda obrigação, perguntar e bloquear a decisão afetada |
| Recurso docente indisponível | Registar evidência de falha e requisito concreto; TASK-020 bloqueada até formato real ou alternativa discutida com docente |
| Decisão técnica em falta | Propor a solução mínima, alternativas e impacto em ADR; só pedir decisão humana se necessária para cumprir fonte de verdade |
| Teste falha | Preservar log, reproduzir e corrigir causa; não apagar teste ou relaxar critério para obter verde |
| Arquitetura precisa de alteração | Fazer avaliação de impacto em requisitos/contratos/DAG; documentar antes de prosseguir; não reescrever todo o projeto automaticamente |
| Tarefa contraria o PDF | PDF prevalece; interromper a parte contraditória, registar e corrigir o plano ou pedir esclarecimento |
| CORE/GUI indisponível | Continuar verificações independentes, mas conservar aceitação CORE/GUI pendente |
| Turno/grupo/prazo desconhecido | Pedir à equipa quando necessário; não inventar valores para TASK-030/031 |

BLOCKED inclui motivo, evidência, condição de desbloqueio, responsável e impacto. Não marcar todas as tarefas dependentes BLOCKED em massa; mantê-las TODO com dependência visível. Trabalho independente só avança se isso não contornar o gate da fase e ficar documentado.

## Retomar numa sessão nova

Ler 00, 15, este guia, 12, a fase atual e o ficheiro da tarefa em curso. Consultar 02/04/06/07 apenas nas partes relevantes e verificar código/evidência. Não confiar em memória de conversas anteriores nem em target/. Confirmar git diff antes de editar. O menor ID pronto em 15 é sugestão; as dependências reais continuam a mandar.

Ao terminar uma sessão, acrescentar em docs/15 um handoff com:

~~~text
Data e revisão do repositório:
Fase e tarefa atual / estado:
Ficheiros alterados e objetivo:
Requisitos e decisões aplicados:
Comandos executados e resultados (ou NÃO EXECUTADO):
Critérios já verificados e ainda pendentes:
Bloqueios e condição de desbloqueio:
Próxima ação concreta e ficheiro a abrir:
Alterações do utilizador a preservar:
~~~

Manter passagem curta e factual, ligando logs extensos em vez de os copiar. Uma sessão interrompida deixa a tarefa IN PROGRESS; a seguinte revê o estado antes de continuar. Não criar commits, uploads ou mensagens externas apenas porque figuram como possibilidade neste guia; seguir a autorização real do utilizador.
