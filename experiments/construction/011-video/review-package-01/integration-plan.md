# Plano posterior de integração — revisão final e autorização pendentes

1. Revisor final previsto Denji examina a versão técnica aprovada,quatro binários/
   manifesto e documentação/evidência desta allowlist. A revisão não foi confirmada.
   Manter LOCAL-04/G-MEDIA-DISTRIBUTION abertos para TASK-030,sem converter aceitação
   de Test em aprovação de legibilidade/atribuição/redistribuição.
2. Após confirmação da revisão final,vinculá-la aos hashes dos quatro ficheiros e
   manifesto,atualizar pacote/allowlist/manifesto e validar referências/V-ADD sobre
   cópia exata candidata. Sem repetir preparação/manual/auditoria se os dados não mudarem.
3. Considerar459820128 bytes de media,mais evidência/documentação e espaço para
   index/objetos/cópia de verificação. Volume Git/pack não medido;não estimar compressão
   ou prometer capacidade do destino por inferência. Os hashes aprovados são imutáveis.
4. Restrição concreta de publicação:origin é GitHub,B/C/D excedem100MiB por ficheiro;
   [fonte oficial](https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-large-files-on-github).
   Este pacote com os binários brutos **não** é prometido como publicável por Git normal
   nesse origin. Isto é uma restrição do transporte remoto,não novo gate de Test/G-MEDIA.
   Não configurar Git LFS,externo,releases,ponteiros,dividir binários,alterar duração/
   resolução/qualidade/créditos ou reescrever histórico por inferência.
5. Pedir autorização concreta para commit/integração local,distinguindo-a de uma
   estratégia futura de publicação. Proposta,se o utilizador autorizar integração
   exclusivamente local dos ficheiros brutos:commit local em main,sobre a base a
   reconferir,com mensagem `feat(IMP-017/TASK-020-A): preparar quatro conteudos MJPEG e evidencia validada`.
   Nenhum ramo é criado nesta preparação. A forma de publicação desses ficheiros no
   origin continua decisão explícita pendente;se o objetivo incluir publicar o
   histórico,rever essa decisão antes do commit adequado,sem migração posterior inferida.
6. Antes de staging/commit autorizados,reconferir refs/estado/trabalho alheio e
   hashes,caminhos/modes contra a allowlist final;staging apenas explícito desses
   caminhos,excluindo target/,demo/ e alterações alheias. Conferir staged diff e
   bytes dos blobs antes do commit. Nenhum destes passos foi executado agora.
7. Depois de commit autorizado,extrair o commit,conferir conteúdo/hashes,dados e
   V-ADD na cópia;apresentar prova de integração local. Se for adotado outro ramo
   por autorização futura,integrá-lo em main só pelo método expressamente aprovado,
   após conferir avanço local/remoto/conflitos. Sem merge/rebase/checkout/reset
   que prejudique trabalho alheio ou force de histórico.
8. Bolt/história ficam abertos até revisão final e integração comprovadas;fecho
   documental só mediante autorização posterior. Uma parte não fecha TASK-020.
   LOCAL-03:cópia dos recursos para Projeto/media e conferência de hashes pertencem
   à composição posterior,não realizadas pela integração documental deste pacote.

Nesta autorização:apenas pacote/revisão/plano,sem staging/commits/push/fecho,
sem escolha de estratégia de armazenamento e sem alterações de conteúdo.
