# IMP-002 — Test aprovado; fecho ainda pendente

Implement aprovado explicitamente pelo utilizador, com registo em
2026-11-03T22:55:46Z. Autorizado Test completo e manual reproduzível; parar antes
de fecho/commit/push ou outro IMP. Test aprovado explicitamente pelo utilizador,
com registo em 2026-11-04T10:19:06Z, incluindo os 824 testes e a execução pessoal
por Luis em 2026-11-04T10:15:14Z. Test concluído; [fecho preparado](closure-walkthrough.md)
para revisão, com G-REVIEW-032 pendente. Bolt in-progress/test, completed=null.

## Resultado atual e versão validada

**clean verify passou: 824 testes, zero falhas, zero erros e zero skips.**
São 740 de configuração (65 anteriores + 675 novos), 66 de codec e 18 de TCP
real. Os totais foram extraídos dos XML novos após clean, sem contar relatórios
das execuções incrementais. Não é reutilização dos 149 históricos de IMP-001.

O manual executado pelo agente passou com **25 CHECK_PASS e MANUAL_PASS**,
operador Codex. Luis repetiu pessoalmente o manual em 2026-11-04T10:15:14Z:
25 CHECK_PASS e MANUAL_PASS no [log separado](../../../experiments/construction/019-overlay/test-manual-luis.log),
saída Java 0 confirmada pelo utilizador. [Proveniência pessoal](../../../experiments/construction/019-overlay/test-manual-luis.json).
O log do agente foi mantido; esta repetição não designa o revisor distinto.

Base Git: Denji/HEAD 6bef7425a1f9237f9f744f26f3aa7e6067778025, com as alterações
locais revistas de IMP-002. Não há commit deste incremento nem integração das
alterações atuais em main. Os quatro hashes de código são os mesmos da última
execução verde de Implement, antes/depois do build completo; nenhuma fundação
ou teste foi alterado durante Test.

SHA-256 do JAR produzido e usado no manual:
`b13a0bd6c928badfb0fd37cdb44d2e9699f683120b096490b60eed1a4ad43760`.
Os [metadados do build](../../../experiments/construction/019-overlay/test-clean-verify-command.json)
guardam também os hashes dos quatro ficheiros e de cada XML Surefire novo.

## Build completo, ambiente e TCP real

Comando executado a partir de Projeto/:

```sh
env JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -ntp clean verify
```

Execução em 2026-11-03T22:56:23Z–22:56:28Z, código 0, BUILD SUCCESS. JDK
21.0.12.1 observado nos três XML e no manual. O processo foi autorizado fora da
sandbox para permitir os sockets loopback exigidos pelos testes de transporte;
não se substituiu TCP por mocks nem se alteraram os testes para contornar o ambiente.

| Suite atual | Testes | Falhas | Erros | Skips | Relatório novo |
|---|---:|---:|---:|---:|---|
| ConfigurationLoaderTest | 740 | 0 | 0 | 0 | [XML configuração](../../../experiments/construction/019-overlay/test-clean-verify-surefire/TEST-pt.uminho.esr.ott.config.ConfigurationLoaderTest.xml) |
| MessageCodecTest | 66 | 0 | 0 | 0 | [XML codec](../../../experiments/construction/019-overlay/test-clean-verify-surefire/TEST-pt.uminho.esr.ott.protocol.MessageCodecTest.xml) |
| TransportTest | 18 | 0 | 0 | 0 | [XML TCP](../../../experiments/construction/019-overlay/test-clean-verify-surefire/TEST-pt.uminho.esr.ott.transport.TransportTest.xml) |
| Total | 824 | 0 | 0 | 0 | [Log completo](../../../experiments/construction/019-overlay/test-clean-verify.log) |

O relatório TCP cobre troca full-duplex intacta, fecho/reconexão, saturação,
callbacks e shutdown/terminação conforme os 18 casos existentes. O runner de
evidência copiou os XML/txt da diretoria criada por este clean verify; não contou
os relatórios antigos restantes de seleções incrementais. POM/provider/versões
e testes de transporte mantêm a base aprovada; sem dependência ou ferramenta nova.

## Consolidação D01–D17 / V-032

O [walkthrough de Implement](implementation-walkthrough.md#correspondência-d01d17)
identifica os métodos concretos; o [resumo atual](../../../experiments/construction/019-overlay/test-results.json)
confere os 740 casos de configuração, os 65 antigos e as contagens por método.
Aqui os estados referem a execução completa atual, não a aprovação do fecho.

| Caso | Prova atual | Resultado técnico |
|---|---|---|
| D01 | Cinco perfis e 30 defaults por nó, em JUnit e no manual | Passou |
| D02 | 93 combinações chave/papel e accessors corretos; 30 overrides no manual | Passou |
| D03 | 58 fronteiras e sete alpha válidos; conversões exatas/combinações impossíveis | Passou |
| D04 | 298 casos de inteiros inválidos/caps, léxico alpha, underflow e espaços/leading zeros | Passou |
| D05 | Limites reduzíveis na carga e construção direta; fonte/filas/caches/montagens | Passou |
| D06 | Overrides aplicáveis e 54 rejeições por papel, inclusive valor igual ao default | Passou |
| D07 | Unknown/duplicadas literal/escapada e papel inválido; testes anteriores preservados | Passou |
| D08 | Quatro relações, igualdades permitidas/proibidas e chave de diagnóstico | Passou |
| D09 | Defaults+overrides verificados como conjunto, sem perda de valores | Passou |
| D10 | Erros runtime/legados de grupos diferentes agregados; cinco chaves também no manual | Passou |
| D11 | Operando inválido não fabrica erro cruzado; outra relação continua avaliada | Passou |
| D12 | 125 entradas inválidas diretas, grupos/Duration nulos e relações; listas imutáveis | Passou |
| D13 | Construtor de 13 argumentos e canónico 14, runtime não nulo/cópias defensivas; manual | Passou |
| D14 | Quatro overrides chegam ao record Transport.Options, sem instanciar Transport | Passou em JUnit e manual |
| D15 | Deployment falha inteiro, parâmetros por nó, escopo perfil/topologia/catálogo | Passou |
| D16 | Propriedades obrigatórias e paths fora do cwd não mascarados por defaults | Passou |
| D17 | 824 casos completos, incluindo TCP real, 45 fixtures intactas e manual do agente | Prova executada e Test aprovado, incluindo repetição pessoal; revisão distinta/integração pendentes |

V-032 tem evidência técnica atual de defaults/overrides/domínios/papéis/relações,
agregação/imutabilidade e consumo offline. A prova D17 que faltava em Implement
foi executada no ambiente disponível e aceite em Test, com repetição pessoal;
isso não satisfaz por inferência o gate de
revisor distinto nem a integração exigida para DONE. Não marcar tarefa/bolt
concluídos apenas porque o build passou.

## Manual executado e repetição pessoal

Em 2026-11-03T22:58:30Z–22:58:31Z, o agente executou o JAR verificado através de
um launcher Java source-file experimental, fora de Maven/JUnit. Código 0,
25 CHECK_PASS, MANUAL_PASS; [log](../../../experiments/construction/019-overlay/test-manual.log)
e [comando/hashes](../../../experiments/construction/019-overlay/test-manual-command.json).

O manual verifica os 30 defaults em B/S1/S2/R1/C1, quatro streams configuradas,
os 30 overrides e o construtor anterior. O consumidor recebe queue=7, connections=13,
connect=1111 ms e shutdown=2222 ms. A configuração inválida devolve numa exceção
as chaves control.port, control.queue.capacity, monitor.ewma.alpha,
routing.candidate.ttl.ms e video.reassembly.max.frames; nenhum nó parcial.
Também rejeita expoente em alpha/override BOOTSTRAP proibido, verifica imutabilidade,
preserva perfis originais e remove os temporários. Nenhum socket de aplicação é aberto.

Comandos e roteiro para o utilizador:
[manual-runtime.md](../../../experiments/construction/019-overlay/manual-runtime.md).
O último argumento identifica o operador real. **A repetição pessoal foi realizada
por Luis e aceite na aprovação de Test**. O [log pessoal](../../../experiments/construction/019-overlay/test-manual-luis.log)
está preservado byte a byte, com os metadados da execução separados dos do agente.
O roteiro arquivado conserva as instruções anteriores à repetição; não se pede
nova execução pessoal para registar esta aprovação.

## Hashes, preservação e diff

[Auditoria atual das 45 fixtures](../../../experiments/construction/019-overlay/test-fixture-audit.json):
bytes/tamanho/SHA-256 e equivalência .bin/.hex conferidos, sem regeneração.
O código/revisões/relatórios anteriores mantêm os hashes; o JAR/XML/manual têm
hashes próprios desta execução. O snapshot documental mantém rastreabilidade das
alterações autorizadas, sem restaurar versões antigas ou enfraquecer o validador.

O diff de aplicação/testes continua limitado aos quatro ficheiros e às duas
linhas originais justificadas em Implement. O diff desta etapa contém documentação
e evidência/roteiro manual, sem editar código de aplicação. As duas listas
geradas em Projeto/target/ são apresentadas separadamente; não são mudanças
autorais, não foram restauradas para fingir hashes intactos e não há commit.

Índice reproduzível de [hashes](../../../experiments/construction/019-overlay/test-hashes.json)
e [conferência de preservação/estados](../../../experiments/construction/019-overlay/test-preservation.json).
Diffs: [etapa Test](../../../experiments/construction/019-overlay/test-checkpoint.diff),
[cumulativo do bolt](../../../experiments/construction/019-overlay/bolt-through-test.diff),
[launcher/roteiro experimental](../../../experiments/construction/019-overlay/test-manual-artifacts.diff)
e [listas geradas](../../../experiments/construction/019-overlay/test-final-generated.diff).
O [diff de código +664/−2](../../../experiments/construction/019-overlay/implement-code.diff)
permanece o aprovado em Implement. [V-ADD atual](../../../experiments/construction/019-overlay/test-validation.md).

## Limitações e paragem

TCP foi real, em loopback; não demonstra CORE, processos de overlay separados,
vídeo, GUI, RTT/rotas ou recuperação automática UDP-only. O manual prova o consumo
do record de opções, não o wiring dos componentes futuros. Quatro UUID de streams
configurados não provam quatro conteúdos preparados/reproduzidos.

Resultados de Test aprovados explicitamente pelo utilizador, com repetição
pessoal comprovada. Test concluído em stages_completed; bolt/história/unidade/
TASK-032 continuam em progresso. Pendentes: revisor humano distinto designado,
revisão final e integração antes de DONE. [Fecho preparado para revisão](closure-walkthrough.md).
Sem fecho, commit, push ou outro IMP.
