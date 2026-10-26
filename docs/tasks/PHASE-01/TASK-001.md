# TASK-001 — Validar a base Java e configurar testes

## 1. Identificação

- ID: TASK-001
- Fase: [PHASE-01 — Fundações, configuração e protocolo](../../phases/PHASE-01-FOUNDATION.md)
- Prioridade: P0
- Estado: TODO
- Dependências: nenhuma.
- Requisitos relacionados: [RP-006](../../02-REQUIREMENTS.md#rp-006).

## 2. Objetivo

Preparar build e engine de testes sem interpretar placeholders como trabalho concluído.

## 3. Contexto

Esta tarefa contribui para o resultado da fase: Configuração e mensagens de controlo validadas por testes locais, sem necessidade de CORE. A responsabilidade específica é validar a base java e configurar testes. Consultar [arquitetura](../../04-ARCHITECTURE.md) e [rastreabilidade](../../13-REQUIREMENTS-TRACEABILITY.md) para a ligação ao sistema completo.

## 4. Pré-requisitos

Concluir e verificar evidências das dependências acima. Confirmar o estado real dos ficheiros em [05](../../05-PROJECT-STRUCTURE.md) e os gates da fase em [12](../../12-RISKS-AND-DECISIONS.md). Ficheiros existentes podem estar vazios; os modelos e serviços planeados só ficam disponíveis após as suas tarefas. Esta é a primeira tarefa; não pressupõe funcionalidade Java implementada.

## 5. Especificação técnica

Confirmar Java 21 no host e no ambiente escolhido. Preservar coordenadas Maven e compiler-plugin existente. Selecionar e fixar versões de JUnit Jupiter e Surefire compatíveis, documentando a escolha. Durante esta tarefa a ausência de testes é aceite e registada; a partir de TASK-003 exigir descoberta de testes reais. Não escrever testes artificiais de classes vazias nem executar o launcher CORE.

Contrato normativo e limites: [07 — Interfaces](../../07-API-AND-INTERFACES.md). Modelo e invariantes: [06](../../06-DATA-MODEL.md). Aplicar apenas as partes relacionadas com esta tarefa, sem implementar funcionalidades posteriores.

## 6. Ficheiros e componentes envolvidos

Existente: Projeto/pom.xml e placeholders em Projeto/src; planeado: registo do build em experiments/build-baseline.md.

Os caminhos de pacotes Java acima são relativos a Projeto/src/main/java/pt/uminho/esr/ott ou, nos testes, Projeto/src/test/java/pt/uminho/esr/ott, salvo prefixo explícito. Caminhos marcados planeados não existem necessariamente. Confirmar antes de editar e preservar alterações alheias.

## 7. Passos de implementação

- [ ] Inspecionar git status, pom e ficheiros existentes; registar diferenças face à auditoria.
- [ ] Confirmar java -version e mvn -version; verificar o POM efetivo e fixar engine/plugins de teste.
- [ ] Executar build mínimo autorizado na futura construção e registar explicitamente número de testes, mesmo zero.
- [ ] Documentar comando de teste e caminho para relatórios Surefire, sem declarar cobertura.

## 8. Regras e restrições

Respeitar a natureza dos requisitos ligados: obrigações do PDF prevalecem; escolhas propostas só mudam com decisão registada. Usar o mínimo necessário e manter isolamento por stream. Não declarar testes executados sem resultados. Não adicionar funcionalidades, bibliotecas ou alterações de infraestrutura fora do objetivo. Rever [segurança](../../10-SECURITY.md) para as entradas e recursos afetados.

## 9. Casos limite e tratamento de erros

JDK diferente, ausência de rede para dependências, permissões de cache Maven e target antigo. Uma falha de download não justifica trocar linguagem.

## 10. Testes necessários

V-001: inspeção do POM efetivo e build limpo com release 21; verificar engine escolhido. Zero testes nesta base não valida funcionalidades; repetir descoberta real em V-003.

Detalhes gerais em [09](../../09-TESTING-STRATEGY.md). Estado inicial da validação: NÃO EXECUTADO. Guardar comandos, ambiente e resultado; verificações manuais devem identificar operador e evidência.

## 11. Critérios de aceitação

- [ ] Build base documentado com versões fixadas.
- [ ] Nenhuma classe vazia é marcada como implementada.
- [ ] Comandos reproduzíveis e ausência inicial de cobertura registados.
- [ ] Validação V-001 executada e evidência ligada no progresso.

## 12. Definition of Done

Todos os critérios acima satisfeitos, dependências verificadas, falhas relevantes resolvidas e [DoD](../../14-DEFINITION-OF-DONE.md) aplicável cumprida. Atualizar este estado, [15](../../15-PROJECT-PROGRESS.md) e [13](../../13-REQUIREMENTS-TRACEABILITY.md), distinguindo requisito parcialmente coberto de concluído. Nenhuma evidência de execução foi produzida por este ficheiro de planeamento.

## 13. Notas para a IA implementadora

Não implementar bootstrap ou transporte para conseguir um JAR aparentemente funcional.

Na retoma de sessão, seguir [o guia](../../AI-IMPLEMENTATION-GUIDE.md). Se houver bloqueio real, registar causa, condição de desbloqueio e próxima ação; não inventar comportamento para contornar obrigação do enunciado.
