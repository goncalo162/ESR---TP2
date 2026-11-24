---
unit: "005-video"
bolt: "011-video"
stage: "test"
status: "complete"
---
# IMP-017 — Checkpoint Test: Test/G-MEDIA técnicos aprovados,LOCAL-04 aberto

## Construção fixada e resultados reais

Implement aprovado;Test autorizado. Reexecução sobre os quatro ficheiros finais,
sem reobtenção/conversão/alteração de binários ou do manifesto de media.
**22687/22687 JPEGs** descodificados com ImageIO/JDK21 headless,sem avisos;
**16/16** casos de fronteira/erro passaram;6 pares de ficheiros/sequências de pixels
distintos. Inventários Test coincidem byte a byte com os de Implement.
[Consolidação/logs/inventários](../../../experiments/construction/011-video/test-01/README.md),
[manifesto aprovado preservado](../../../experiments/media/manifest.json).

| Alias/origem | Registos | Bytes | JPEG min..max |
|---|---:|---:|---:|
| A/S1 |500|4269893|4970..13967|
| B/S1 |5965|111255866|1602..41769|
| C/S2 |8880|167319819|1602..66091|
| D/S2 |7342|176974550|1938..56322|

Total459820128 bytes. A coincide com hash histórico,sem reencodar. B/C/D completos,
créditos retidos,640×360/FPS10/q5 da preparação;nenhum FPS de reprodução medido.
Manifesto é o snapshot aprovado em Implement e fica byte-idêntico;os campos de
estado históricos nele não são reescritos para aparentar aprovações posteriores.
O estado corrente é documentado aqui e na consolidação Test.

## V-020-A

- [x] Associação A/B→S1,C/D→S2,quatro outputs e hashes/manifesto/proveniência conferidos.
- [x] Todos os prefixos de cinco bytes ASCII,L1..99999,payloads/offsets/EOF/bytes totais conferidos.
- [x] JPEGs com estrutura integral,dimensões limitadas antes de ImageIO,descodificação um frame de cada vez,readers/streams libertados.
- [x] Fronteiras:JPEG válido99999,EOF limpo entre registos,truncaturas,não decimal,zero,corrupto,bytes extra e seis dígitos rejeitados.
- [x] Seis comparações de hash de ficheiro e sequência de pixels,proveniência distinta e amostras visuais do agente em Implement.
- [x] Confirmação visual humana de quatro conteúdos e créditos;B ilegível,C/D legíveis nas amostras,limitação registada.
- [x] Test/G-MEDIA aprovados no âmbito técnico pelo utilizador,aceitando a limitação conhecida B sem aprovar legibilidade/atribuição/redistribuição.

99999 é limite do perfil de ficheiro,não o cap UDP2MiB do codec fechado.
Testes/inventários independentes do futuro VideoSource;sem alterar/implementar Java
sob Projeto/src. Classes do auditor compiladas apenas em pasta temporária.
Não executar Maven por inferência para dados sem alteração Java;1537 testes de
IMP-016 são históricos,não contagem atual de V-020-A ou nova regressão.

## Manual humano e gates

[Manual humano conferido](../../../experiments/construction/011-video/manual-verification-01/README.md):
Francisco confirmou execução pessoal,13 checks/7 etapas,quatro conteúdos distintos,
WRAPPER_EXIT=0/PASSED=true. SHELL_EXIT=0 confirmado pelo utilizador,não pelo log.
Tentativa completa20261124T153130Z-fdf1c313 e parcial20261124T152418Z-403a6f63
preservadas com HTML/imagens/logs originais. Parcial:9 checks/3 etapas,nota vazia,
WRAPPER_EXIT=1/PASSED=false;não equivale a aprovação nem foi apagada.

**B:créditos presentes mas ilegíveis**,nota «nao dava para ler as letras».
C/D legíveis nas amostras,sem inferir legibilidade de todo o texto/filme.
MANUAL_PASS conclui o guião de inspeção,não aprova legibilidade ou redistribuição.
[Avaliação LOCAL-04 e opções não aplicadas](../../../experiments/construction/011-video/manual-verification-01/B-readability-assessment.md):
V-020-A técnico e distinção dos conteúdos demonstrados;ADD não fixa limiar de letras
para esse oráculo,mas a limitação de créditos fica explícita para decisão humana
e G-MEDIA-DISTRIBUTION/TASK-030. Não se presume atribuição adequada por mera presença.
Recomendado manter a versão/hashes para aprovação técnica com limitação conhecida,
tratando créditos/condições finais na TASK-030. Se for exigida correção no vídeo,
propor edição de B adequada e nova validação/manual/revisão,sem alterar por inferência.

**Test e G-MEDIA aprovados no âmbito técnico**,com limitação B explicitamente aceite pelo utilizador.
[Confirmação/hash](../../../experiments/construction/011-video/review-package-01/technical-approval.json).
LOCAL-04 permanece aberto para TASK-030;legibilidade/atribuição/redistribuição não aprovadas.
G-MEDIA-DISTRIBUTION aberto,A sem licença audiovisual específica conhecida;
revisão final/integração pendentes. Sem novo build/auditoria/manual:media e manifesto
não mudaram. LOCAL-03 opção1:cópia/hash para Projeto/media posterior,não instalada;
perfis atuais não são anunciados como prontos.

## Volume e integração posterior

[Plano do volume real](../../../experiments/construction/011-video/test-01/integration-size-plan.md):
considerar459820128 bytes de media mais documentação/evidência. Nenhuma escolha
Git LFS,armazenamento externo,ponteiros ou alteração de conteúdos por inferência.
Revisão final e integração pendentes;nenhum staging/commit/push/fecho.
Parte A não fecha TASK-020 inteira nem as partes B/C. Unidade/fase em progresso.
V-ADD/documentos e preservação ligados na evidência. IDs locais,demo e incrementos
fechados intactos;sem alterações de binários,manifesto,Java,perfis,POM ou fixtures.

## Pacote para revisão final e integração

[Resumo/diffs/allowlist/plano](../../../experiments/construction/011-video/review-package-01/README.md).
Revisão final ainda não confirmada,integração pendente;bolt e TASK-020 pai abertos.
Limites de publicação dos binários registados,sem escolher LFS/externo/modificar dados.

## Armazenamento LFS autorizado posteriormente

Sem alterar media/manifesto ou repetir manual/auditoria,estratégia LFS aprovada e
aplicada exatamente aos quatro ficheiros. [Validação local e efeitos de instalação](../../../experiments/construction/011-video/lfs-implementation-01/README.md):
4/4 objetos materializados e hashes reais/ZIP temporário de dados conferidos,
index/HEAD intactos,sem rede LFS ou uploads. Git passa a esperar ponteiros no
index futuro e bytes reais no working tree/cache/entrega,conforme
[novo pacote](../../../experiments/construction/011-video/lfs-review-package-01/README.md).
Este resultado não executa nem aprova um Codigo.zip completo final.
LOCAL-04/G-MEDIA-DISTRIBUTION/revisão final/integração continuam pendentes.
