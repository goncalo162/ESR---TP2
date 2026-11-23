# Plano proposto — aguarda autorização

1. Revalidar ramo imp-016-udp,HEAD/main b6de848,estado local,hashes do manifesto e
   exclusões. Se main mudar ou aparecer trabalho alheio,não integrar por inferência:
   identificar diferenças/conflitos e reapresentar o plano.
2. Após autorização explícita,fazer staging apenas dos caminhos de allowlist.txt.
   Conferir conjunto exato do index,blobs/SHA-256,numstat/diff e ausência de target/,
   demo/ e quaisquer alterações alheias antes de criar o commit.
3. Criar um commit local em imp-016-udp com mensagem proposta:
   `feat(IMP-016/TASK-019): adicionar codec UDP OTT1 e evidencia validada`.
   O grupo IMP-016-LOCAL-02 é separado no inventário e patch; contém só o template.
   Não introduzir alterações adicionais ao código aprovado ou aos originais.
4. Extrair o commit para cópia temporária,conferir allowlist/hashes/JAR,repetir
   V-ADD nessa cópia e apresentar hash/prova do conteúdo efetivamente cometido.
5. Integração posterior em main,apenas por autorização explícita. Com main ainda
   na base b6de848,o commit permite fast-forward. Revalidar primeiro refs/estado
   e preservar trabalho alheio. Se main avançar,rever divergências e o método
   de integração antes de qualquer operação. Sem force,push ou histórico reescrito.
6. Comprovar que a versão revista e o JAR aprovados estão efetivamente integrados
   em main; apresentar hash e prova. Bolt continua aberto até essa comprovação.
   Fecho documental/DONE só por autorização posterior,separado do commit funcional.

Test1537/0/0/0,22 XML,V-ADD,manual Francisco24 e revisão final Denji já cumpridos.
Sem novo build/manual enquanto código/JAR mantiverem os hashes aprovados.
Sem gates externos adicionais. Nenhum destes passos de staging/commit/integração
foi executado nesta autorização de preparação.
