# IMP-001 — Plano de verificação da base herdada

Preparado em 2026-11-03T21:03:35Z. Checkpoint 1 e Q-C-01 aprovados pelo utilizador,
com registo em 2026-11-03T21:08:48Z. Checkpoint 2 aprovado pelo utilizador,
com registo em 2026-11-03T21:16:53Z. Checkpoint 3 aprovado pelo utilizador,
com registo em 2026-11-03T21:22:32Z; validação autorizada apenas até ao checkpoint 4.
Bolt/unidade: 001-base-validada. Tipo: simple-construction-bolt.
Etapas locais: Plan → Implement → Test, com os cinco checkpoints explícitos
do utilizador. O plano não autoriza automaticamente o checkpoint seguinte.

Inspeção do checkpoint 2 aprovada: [interfaces e mapeamento dos testes](interfaces-and-test-map.md).
O [walkthrough](implementation-walkthrough.md) e os diffs foram aprovados no
checkpoint 3. Autorizada a validação, incluindo build, regressão e teste manual;
parar no checkpoint 4, sem fechar o bolt ou alterar fundações/testes por inferência.

Validação executada e apresentada no [relatório do checkpoint 4](test-walkthrough.md):
149 testes aprovados, 45 fixtures, V-ADD sem erros e teste manual com 22 checks.
O utilizador aprovou esses resultados, com registo em 2026-11-03T21:40:56Z,
e autorizou preparar o checkpoint 5. O fecho foi depois aprovado, integrado em
main e aplicado em 2026-11-03T21:56:48Z: [relatório final](closure-walkthrough.md).
Commits e push foram autorizados explicitamente. As descrições
de estado inicial e verificações previstas abaixo documentam o plano aprovado.

## Estado observado e fontes

Inception concluído; unidade inicialmente ready e bolt inicialmente planned,
sem artefactos de execução. Não existem dependências de bolt/unidade. TASK-001,
002, 003, 004 e 005 estão DONE nos documentos das tarefas e em docs/15; os seus
resultados anteriores não substituem esta regressão futura.

Ramo atual: Denji. HEAD: 4fe6791178828e72ea48c71d46146f9c8cd4c6f9.
Há 19 ficheiros rastreados modificados e um relatório não rastreado do fecho
de Inception, anteriores a este arranque. A árvore Projeto/ não tem diff contra
HEAD. Guardados [baseline e hashes](../../../experiments/construction/001-base-validada/baseline.json)
e [diff preexistente](../../../experiments/construction/001-base-validada/pre-existing.diff)
antes das alterações de arranque, para distinguir a evidência deste bolt.

[PRESSUPOSTO] --bolt 001 identifica o único prefixo 001, isto é, 001-base-validada,
cujo incremento é IMP-001 e cuja unidade é 001-base-validada. O placeholder
{RAMO} não foi preenchido: conservar Denji sem criar/trocar ramos.

O [ADD](../../../add.md) presente é v1.0.2, já aprovado no fecho de Inception;
v1.0.1 referido no pedido é a versão anterior. As clarificações e o fecho não
introduzem mudanças nos bytes ou no objetivo de IMP-001. Foram consultados
enunciado, specs, intent/história/bolt, secções 2/5/6/8/9/10/11/12 do ADD,
contratos 07 (3–7), testes 09, DoD 14, progresso 15 e a base Java/testes.

## Objetivo e limites

Aplicar [IMP-001](../../../add.md#imp-001): confirmar a base DONE sem a
reimplementar, conferir 45 fixtures e os 16 tipos ativos e guardar regressão
atual. A base contém configuração, codec e transporte; bootstrap é comentário,
CLI/overlay/vídeo são trabalho futuro. A ausência desses serviços não é falha
deste incremento. RuntimeConfiguration e sendAndFlush pertencem aos IMP seguintes.

Na inspeção inicial não foi identificada divergência material que exija mudar
uma ADR ou os componentes herdados. A execução ainda não foi validada. Qualquer
falha futura que exija alterar as fundações interrompe a parte afetada para
registo em docs/construction-duvidas.md e decisão do utilizador; ADR novas,
se necessárias, usam ADR-033 em diante.

## Ficheiros e entregáveis

Os alvos de inspeção da secção 8 do ADD são Projeto/pom.xml, config/, protocol/,
transport/ e Projeto/src/test/. Não está prevista alteração de produção, POM,
fixtures ou testes existentes para um incremento de verificação.

Criar/manter na pasta deste bolt: implementation-plan.md (este documento),
implementation-walkthrough.md e test-walkthrough.md nas etapas respetivas;
bolt.md regista somente etapas realmente executadas/aprovadas.

Manter unit-brief.md e construction-log.md da unidade. Na aprovação de fecho,
sincronizar a história, memory-bank/story-index.md e docs/13–15, sem declarar
requisitos integrados nem reabrir TASK-001–005. O mecanismo de fecho exige
inspeção antes de execução: não existe script local bolt-complete.cjs; existe
o instalado em /home/luis/.specsmd/aidlc/scripts/bolt-complete.cjs. A sua execução
no checkpoint 5 depende de verificar compatibilidade com este esquema local,
sem sobrescrever índices/estados por assumir o template genérico.

Evidência nova fica em experiments/construction/001-base-validada/: baseline,
diffs, ambiente/comandos, relatório documental, POM efetivo quando aplicável,
log completo do build e resumo extraído dos relatórios Surefire novos.
Não produzir walkthroughs/resultados antes das respetivas execuções.

## Oráculos e verificações previstas

| Oráculo | Verificação |
|---|---|
| V-001 | POM efetivo, release 21, JUnit Jupiter/Surefire, failIfNoTests=true e build limpo |
| V-002 | 45 fixtures binário/hex/hash, offsets/limites e proveniência da evidência histórica; não presumir novo download nem execução de media |
| V-003 | ConfigurationLoaderTest: perfis, IDs/portas/papéis, erro agregado, paths fora do cwd, imutabilidade e quatro streams configuradas |
| V-004 | MessageCodecTest: golden bytes, 16 tipos, rejeição de 3/layout antigo, UTF-8, truncatura/concatenação e limites antes da alocação |
| V-005 | TransportTest: TCP loopback real, concorrência, limites, peer lento, cancelamento/fecho e terminação dos recursos |
| V-ADD | scripts/validate-planning.py: estrutura, rastreabilidade, critérios, referências e DAG; snapshot é referência histórica, não impedimento a mudanças legítimas de estado |

JDK por omissão: 25.0.4.1. JDK 21.0.12.1 disponível em
/usr/lib/jvm/java-21-openjdk-amd64; Maven 3.6.3 confirmado com esse JDK.
Selecionar JAVA_HOME apenas no processo de construção e registar o ambiente.
Comando de validação, a partir de Projeto/:

```sh
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -ntp clean verify
```

O total de 149 testes é histórico; contar os relatórios Surefire produzidos
pela nova execução e registar falhas/skips. Não alegar GUI, CORE, vídeo ou
processos distribuídos a partir dos testes de configuração/codec/loopback.

## ADR e gates

ADR aplicáveis no bolt: 001, 002, 007, 008, 009, 010, 012, 014, 020, 021,
024 e 026. As restrições de bootstrap/bytes continuam a seguir ADR-013.
Sem dependências runtime novas, CI/linter/JaCoCo ou cobertura percentual nova.
Nenhum gate externo adicional em IMP-001. Testes de transporte precisam de TCP
local disponível; impedimentos do ambiente são reportados como tal, sem sucesso
inventado. Não abrir serviços CORE ou obter media final neste incremento.

Os checkpoints do utilizador exigem validação humana; a revisão atribuída e a
integração histórica de TASK-005 não são aprovadas novamente por inferência.
O fecho deste bolt não substitui revisões de código ou gates dos próximos IMP.

## Sequência e pontos de paragem

1. Estado/plano: apresentar este plano, diff e Q-C-01; parar sem testes.
2. Autorizado: conferir assinaturas/records/enums e cobertura dos testes
   existentes. A adaptação à base DONE em Q-C-01 foi aprovada exclusivamente
   para IMP-001; apresentar lacunas reais antes de alterar fundações ou testes
   e parar para validar no checkpoint 2.
3. Produzir walkthrough da verificação autorizada, mostrar git diff --numstat
   e distinguir as alterações preexistentes; parar antes da validação final.
4. Executar a validação documental e mvn -B -ntp clean verify com log novo,
   apresentar resultados reais e limitações; executar também um teste manual
   adequado à base de configuração/codec/transporte e parar antes do fecho.
5. Após aprovação: atualizar estados/evidência coerentes e apresentar diff.
   Commit/push só mediante autorização explícita; não iniciar outro IMP.
