# IMP-002 — Etapa ADR: revisão do Design aprovado

Design e gramática de alpha aprovados explicitamente pelo utilizador, com registo
em 2026-11-03T22:25:30Z. Autorizado apenas rever ADR-007/010/016/024/026 e registar
Q-C-03. Revisão ADR aprovada pelo utilizador em 2026-11-03T22:33:00Z;
[Implement e Test aprovados](ddd-03-test-report.md), com 824 testes completos e
execução pessoal por Luis/25 checks confirmados. Esta análise ADR é documental;
resultados e [fecho preparado](closure-walkthrough.md) mantêm G-REVIEW-032 pendente.

Fontes: [ADD secção 5](../../../add.md#5-registo-de-decisões),
[ADD 6.3](../../../add.md#63-parâmetros-operacionais-opcionais),
[IMP-002](../../../add.md#imp-002), [Model](ddd-01-domain-model.md),
[Design aprovado](ddd-02-technical-design.md) e
[TASK-032](../../../docs/tasks/PHASE-02/TASK-032.md).

## Resultado

As cinco ADR continuam Aceites e são reutilizadas. Não foi identificada divergência
material entre o desenho aprovado, as decisões e as APIs inspecionadas. Não é
necessária ADR nova nem substituição de uma decisão aceite. O detalhe lexical
novo tem aprovação explícita em [Q-C-03](../../../docs/construction-duvidas.md#q-c-03--gramática-decimal-de-alpha);
o domínio de alpha continua finito e em (0,1], com default 0.2.

| Decisão | Conformidade do desenho | Prova futura/limite |
|---|---|---|
| [ADR-007](../../../add.md#adr-007) — propriedades UTF-8 e binário sem dependências runtime | Mantém o reader UTF-8 estrito e Properties com deteção de duplicadas; só acrescenta chaves no perfil do nó. Não usa JSON externo, serialização da rede ou biblioteca nova | D04/D07/D15/D16; POM/fixtures/codec preservados. Nenhum Java executado nesta etapa |
| [ADR-010](../../../add.md#adr-010) — carga estrita/imutável | Sete records e Duration imutáveis; construtores guardam domínio/relações; loader agrega file/key/message, rejeita unknown/duplicada/inaplicável e não publica carga parcial | D01/D06–D13/D15/D16. Retorno agregado do loader é distinto de erro de programação em construção direta |
| [ADR-016](../../../add.md#adr-016) — opções centrais/overrides | Exatamente 30 chaves/sete grupos, defaults/caps/papéis/quatro relações do ADD. Dezasseis durações com milissegundos exatos e conversões limitadas; alpha conforme Q-C-03 | D01–D06/D08–D15. Consumo offline por Transport.Options; wiring real continua nos IMP responsáveis |
| [ADR-024](../../../add.md#adr-024) — ADD mestre/IMP atómico | Apenas IMP-002/TASK-032 no bolt 019; dependência IMP-001 complete e integrada. Quatro ficheiros autorizados para implementação futura; índices/registos sem reabrir fundações | V-ADD e preservação de hashes agora; V-032/revisão/integração futuras. Nenhum avanço para IMP-003/bolt 002 |
| [ADR-026](../../../add.md#adr-026) — qualidade com ferramentas existentes | JUnit/Maven/validador existentes, D01–D17 especificados e regressão/manual pendentes; sem CI, linter, JaCoCo, dependência runtime ou limiar percentual novos | Cobrir critérios/casos críticos com resultados reais. Os 149 testes de IMP-001 não contam como execução atual deste incremento |

## Q-C-03 e razão para não criar nova ADR

Q-C-03 regista a gramática aprovada, não uma nova opção arquitetural. A carga
operacional ainda será implementada; não existe representação anterior de alpha
aceite pelo loader que esteja a ser removida. A string inteira, após strip, deve
ser decimal com dígitos e ponto conforme o Design; expoente/hex/sufixos/vírgula/
sinal/unidades não fazem parte dessa representação. O parsing continua obrigado
a rejeitar não finitos, zero, valores acima de 1 e underflow para zero, agregando
o erro da propriedade. Não se usa validação lexical para dispensar a numérica.

O detalhe não altera default, aplicabilidade por papel, limites, mensagem de rede
ou requisito; concretiza ADR-016/010 com autorização explícita. ADR-033 em diante
ficam reservadas para eventual decisão nova, não são criadas para renumerar ou
duplicar este esclarecimento. O ADD e as ADR aceites não foram modificados.

## Salvaguardas a manter antes e durante a implementação autorizada

- **Compatibilidade:** acrescentar runtime no fim do record e manter o overload
  público anterior de 13 argumentos, delegando nos defaults. Não remover accessors
  ou nomes Java existentes. A alteração de reflection/equals pelo novo componente
  já está explícita no Design aprovado; não afeta o protocolo binário v1.
- **Erros agregados:** parsing inválido não recebe default, não publica grupo/nó
  parcial nem dispara uma relação com sentinela inválida. A validação dos records
  é segunda defesa; não substitui a agregação da API de carga. Role inválido não
  é tratado como overlay; chave inaplicável é rejeitada mesmo igual ao default.
- **Unidades/caps:** Duration limitado antes de converter, sem truncar fração;
  inteiros/percentagens/alpha nos domínios aprovados. Fronteira escalar impossível
  com as relações resulta em erro de relação, sem aumentar caps para fazer passar
  um caso. Preservar TCP 37..65536, 16 tipos/código 3 rejeitado, UDP 1200/64/1136,
  2 MiB/frame e restantes limites protegidos do ADD.
- **Âmbito e consumo:** a prova D14 instancia apenas o record real Transport.Options
  no ConfigurationLoaderTest autorizado, com sentinelas diferentes dos defaults.
  Não instancia Transport, abre sockets ou cria adapter de produção. Não afirma
  wiring de OttApplication/bootstrap nem consumo dos serviços ainda por implementar.
- **Testes:** manter todos os testes existentes, inclusive chaves inválidas atuais.
  Depois da autorização, os novos casos falham por APIs/comportamentos ausentes;
  não fabricar falhas em fundações. V-032, regressão completa com log novo e teste
  manual adequado continuam obrigatórios, com resultados atuais e limitações.

Estas salvaguardas já decorrem do desenho/ADR; não são novas dependências ou gates.
Se o código real revelar inviabilidade durante Implement, parar a parte afetada,
registar evidência/impacto e propor a revisão necessária antes de intervir.

## Checkpoint ADR e condições de fecho

Revisão ADR aprovada e incluída em stages_completed. Na etapa ADR não houve
alterações em Projeto/ ou execução Java; o trabalho posterior autorizado está no
walkthrough de Implement e no relatório Test posterior, agora aprovado com
repetição pessoal confirmada. Bolt in-progress/test, aguardando G-REVIEW-032 e
revisão do fecho preparado. Nenhum DONE ou commit/push foi autorizado.

G-REVIEW-032 permanece pendente: designar revisor humano distinto do autor,
executar V-032/regressão/manual, obter revisão final e integrar antes de DONE.
A aprovação de Design/alpha não identifica esse revisor nem equivale a revisão
da implementação. Não fechar bolt/história/unidade nem iniciar outro IMP.
Apresentar esta reutilização para revisão e parar antes de Implement/Test.
