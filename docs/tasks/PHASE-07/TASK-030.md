# TASK-030 — Empacotar código e verificar reprodução da entrega

## 1. Identificação

- ID: TASK-030
- Fase: [PHASE-07 — Relatório, empacotamento e defesa](../../phases/PHASE-07-DELIVERY.md)
- Prioridade: P0
- Estado: TODO
- Responsável: [Waheed](../../team/WAHEED.md).
- Revisor: [Fillmore](../../team/FILLMORE.md), antes de integrar e marcar DONE.
- Dependências: [TASK-029](../PHASE-07/TASK-029.md).
- Requisitos relacionados: [RC-003](../../02-REQUIREMENTS.md#rc-003), [RC-004](../../02-REQUIREMENTS.md#rc-004), [RC-005](../../02-REQUIREMENTS.md#rc-005), [RP-008](../../02-REQUIREMENTS.md#rp-008).

## 2. Objetivo

Preparar artefactos com nomes corretos que outro elemento consegue utilizar.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: PDF e ZIP com nomes corretos, reprodução após extração limpa e checklist de submissão com confirmação humana. A responsabilidade específica é empacotar código e verificar reprodução da entrega. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Não substituir dependências reais por componentes fictícios na demonstração de integração.

## 5. Especificação técnica

Obter turno PL e número do grupo, sem placeholders no ficheiro de entrega. Incluir fontes, POM, configurações, topologias, instruções e recursos redistribuíveis indispensáveis; documentar obtenção dos restantes. Excluir target antigo, caches e capturas volumosas não necessárias. Extrair ZIP para diretório temporário limpo, seguir README, compilar e executar testes e uma demonstração mínima. Atualizar links dos README para este plano e distinguir comandos validados dos restantes.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existentes: README.md e Projeto/README.md; planeados: delivery/ESR-TP2-PLx-Gy-Rel.pdf e ...-Codigo.zip com x/y reais.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Inventariar conteúdo e confirmar licença/disponibilidade de media.
- [ ] Gerar ZIP e copiar PDF final com nomes do enunciado.
- [ ] Extrair para diretório limpo e validar build/testes/execução.
- [ ] Registar hashes, conteúdo e resultado da reprodução.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

Grupo desconhecido é gate para naming final, não preencher com PL1/G1 inventados. Media fora do ZIP exige instruções e acesso verificado, caso contrário a reprodução falha.

## 10. Testes necessários

V-030: arquivo abre sem paths absolutos/escape; extração limpa passa instruções e testes; PDF abre e continua até 12 páginas; nomes e hashes correspondem ao manifesto.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] PDF e ZIP usam turno/grupo reais.
- [ ] Entrega é reproduzível a partir da extração.
- [ ] Não há segredos, caches ou dependências locais não documentadas.
- [ ] Validação V-030 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Executar esta tarefa numa unidade de trabalho; se surgir decisão que afete tarefas seguintes, registá-la antes de avançar. Não alargar o objetivo para compensar uma dependência incompleta.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
