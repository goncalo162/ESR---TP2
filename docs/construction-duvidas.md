# Dúvidas de Construction

## Q-C-01 — Testes primeiro no IMP-001

- Registada: 2026-11-03T21:03:35Z.
- Estado: resolvida; adaptação aprovada exclusivamente para IMP-001.
- Âmbito: IMP-001, bolt/unidade 001-base-validada.
- Evidência: [IMP-001](../add.md#imp-001) exige verificar TASK-001–005 DONE sem
  reimplementar; o pedido desta execução exige interfaces e testes primeiro,
  com testes a falhar antes de implementar. Não há nova funcionalidade ou API
  prevista neste incremento. O tipo Simple e a DoD excluem código/testes artificiais.
- Proposta: no checkpoint 2 auditar assinaturas/records/enums e correspondência
  entre os testes existentes e V-001–005/V-ADD; não acrescentar ou provocar uma
  falha artificial só para imitar um ciclo de funcionalidade nova. Executar a
  regressão com evidência nova na validação autorizada. Qualquer lacuna real
  deve ser apresentada ao utilizador antes de alterar fundações/testes.
- Impacto: nenhuma mudança de bytes, arquitetura, requisitos ou dependências;
  adapta o procedimento de testes primeiro à natureza de verificação de IMP-001.
- Aprovação: explícita pelo utilizador nesta sessão, registada em
  2026-11-03T21:08:48Z. Apenas conferir interfaces e mapear testes no checkpoint 2,
  apresentando lacunas antes de alterar fundações/testes e parando para revisão.
  Não se autoriza omitir a regressão com log novo ou o teste manual na validação.
  Não altera o procedimento dos restantes IMP nem requer nova ADR.

## Q-C-02 — Compatibilidade do mecanismo de fecho

- Registo do diagnóstico/ensaio: 2026-11-03T21:42:27Z; apenas IMP-001/checkpoint 5.
- Estado: resolvida tecnicamente em cópia temporária; fecho real ainda não aplicado.
- O script local não existe; o instalado em /home/luis/.specsmd/aidlc/scripts/bolt-complete.cjs
  falha por falta de fs-extra. Também não está disponível js-yaml. A inspeção
  do serializer mostra YAML genérico, incompatível com os valores JSON do
  parser local; não se alterou esse parser nem se instalaram dependências.
- Solução de ferramenta: [preload](../experiments/construction/001-base-validada/bolt-complete-json-frontmatter.cjs)
  que fornece apenas as quatro operações fs-extra utilizadas a partir de Node
  builtin e load/dump do subset local, preservando a lógica/cascata instalada.
- [Ensaio isolado](../experiments/construction/001-base-validada/completion-tool-compatibility.json):
  execução nativa falha antes de escrever; com adaptação, código 0, metadados
  aceites pelo parser local, bolt/história/unidade completos só na cópia,
  intent e os outros 32 bolts preservados. Sem fecho real ou história editada
  manualmente. A primeira tentativa do auxiliar de ensaio assumia a presença
  das dependências; o diagnóstico e o log foram preservados.
- Impacto: nenhum no runtime Java, wire, âmbito ou ADR; é adaptação de ferramenta
  ao esquema já existente. Não dispensa revisão final nem integração, exigidas
  pelo utilizador/DoD antes do estado complete.
