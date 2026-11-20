# IMP-013 — integração e fecho autorizados

Test/manual Luis aprovados; Fillmore aprovou sem alterações, confirmado pelo
utilizador. Commit/push autorizados. Base local e remota main/Denji40592da.

1. Conferir manual/hashes/revisão, preparar allowlist exata de três ficheiros Java,
   documentação e toda a evidência008-arvores. Excluir Projeto/target e alterações
   alheias; preservar outputs locais sem restaurar ou apagar.
2. Exportar a base e sobrepor apenas allowlist, validar referências/V-ADD na cópia.
   Stage explícito dos caminhos, conferir hashes e diff antes do commit de código.
3. Commit em Denji e avanço rápido de main para essa mesma árvore aprovada;
   exportar commit, conferir bytes/paths, exclusões/negativos e V-ADD.
4. Após prova de integração local, marcar bolt/história/TASK-016 complete/DONE,
   preservar unidade/fase em progresso e outros IMP não iniciados. Commit
   documental com a prova anterior, mantendo hashes históricos imutáveis.
5. Avançar ambas as branches e fazer push normal/atómico, sem force. Conferir
   refs remotas e apresentar commits publicados. Não inventar publicação se
   houver rejeição externa; resolver apenas dentro da autorização.

R17 inicial permanece sem causa estabelecida. Mesmos1419 testes,20 XML,
manual Luis28 e JAR81779be388778fd7dfacba5957e74ccffe5da45e86a403ea1ec5626f52c8f49d.
Nenhum novo código/build/manual, gate externo ou outro IMP.

## Fecho após integração comprovada

Registo 2026-11-20T16:23:16Z: Test/manual Luis7/28 e revisão Fillmore aprovados;
integração main 447ae4bf68bf96329bd1a0c7e16ed20df06bc55b comprovada.
Bolt/história complete,implemented=true,TASK-016 DONE; unidade/fase em progresso.
Negativos e limitação R17 preservados,sem causa estabelecida. Nenhum outro IMP
iniciado;024 apenas desimpedido. Commit/push autorizados,sem Java/build/manual novo.
[Fecho](closure-walkthrough.md).
