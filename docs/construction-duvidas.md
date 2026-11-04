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
- Estado: resolvida; ensaio em cópia temporária e fecho real aplicado após
  aprovação/integração em 2026-11-03T21:56:48Z. [Saída real](../experiments/construction/001-base-validada/completion-real.log).
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

## Q-C-03 — Gramática decimal de alpha

- Estado: resolvida; aprovação explícita do utilizador registada em
  2026-11-03T22:25:30Z, juntamente com a aprovação de Design do IMP-002.
- Âmbito: monitor.ewma.alpha, bolt 019-overlay / TASK-032. O
  [ADD 6.3](../add.md#63-parâmetros-operacionais-opcionais) exige representação
  decimal com ponto e valor finito em (0,1]; a gramática exata é o detalhe
  especificado no [Design, secção 4](../memory-bank/bolts/019-overlay/ddd-02-technical-design.md#4-unidades-gramática-e-conversões-verificadas).
- Decisão aprovada: depois de strip, exigir correspondência da string inteira
  com `(?:[0-9]+(?:\.[0-9]+)?|\.[0-9]+)`. Aceitar, por exemplo, `1`, `1.0`,
  `0.2` e `.2`; os exemplos continuam sujeitos ao domínio numérico.
- Rejeitar expoente, hexadecimal, sufixo d/f, vírgula, sinal, unidades, valor
  vazio e decimal sem dígitos depois do ponto, como `1.`. Após o teste lexical,
  usar Double.parseDouble, exigir Double.isFinite e 0 < alpha ≤ 1; overflow
  não finito e underflow para zero são inválidos. Não arredondar/clamp nem
  aplicar o default 0.2 a um valor presente inválido.
- Erros de propriedades entram na ConfigurationException agregada com fonte e
  chave; criação direta do record também mantém finitude e domínio. A ausência
  da chave usa o default existente. Os casos D03/D04/D10/D12 de V-032 verificam
  fronteiras, gramática, agregação e construção direta quando forem executados.
- Impacto: esclarece a representação de uma propriedade nova; preserva valor
  default, papel, domínio de ADR-016, bytes v1, hard caps e perfis existentes.
  Sem substituição de ADR Aceite, dependência ou âmbito adicional; não requer
  ADR nova. A revisão das cinco ADR está em
  [adr-review](../memory-bank/bolts/019-overlay/adr-review.md).
- A aprovação abrange Design/gramática e autoriza somente a etapa ADR nesta
  continuação; não declara implementação, testes, revisão distinta ou integração.
