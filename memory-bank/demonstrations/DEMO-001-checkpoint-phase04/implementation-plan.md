# Plano — DEMO-001

1. Ler progresso, README operacional, bolts concluídos, enunciado e V-018.
   Guardar hashes de fontes/CORE/scripts e executar clean verify/JDK21 inicial.
2. Criar `demo/run.sh`, `demo/src/demo/{CheckpointDemo,DelayProxy,Json}.java`,
   `demo/src/pt/uminho/esr/ott/overlay/DemoNode.java` e `demo/dashboard.html`.
   Consultar [estilo](../../../docs/style/CODESTYLE.md); compilar ferramentas
   separadamente contra o JAR, nunca em Projeto/src.
3. Criar os descritores `demo/config/cenario1.properties`, `cenario2.properties`
   e `cenario3.properties`, a partir das figuras do PDF. Gerar perfis reais
   node/bootstrap/topology/streams por execução e validá-los conjuntamente.
4. Confirmar CLI de produção e observação por snapshots. Reutilizar C1–C4:
   agregação/isolamento, renovação, custo real, falha/recuperação e poda.
5. Preservar tentativas negativas e diagnóstico. Só alterar ferramentas de
   demo por causas identificadas; qualquer necessidade funcional é apresentada
   separadamente, sem reabrir fases concluídas por inferência.
6. Executar três runs consecutivas de cada cenário obrigatório e tentar onze
   nós. Guardar manifestos (commit/JAR/JDK/config), snapshots, frames, PIDs,
   sinais/saídas, eventos e resultado de limpeza. Conferir não simulação.
7. Clean verify final; comparar regressão/fontes/CORE com baseline. Entregar
   `demo/DEMO.md`, `demo/MATRIX.md`, relatórios Implement/Test e progresso.
   Fazer commits pequenos na Denji e conferir working tree limpo. Sem push.

O pedido atual autoriza os incrementos de preparação/validação deste bolt de
apoio. A aceitação da ferramenta pelo agente não é apresentada como execução
humana nem aprovação docente ou de fases funcionais futuras.
