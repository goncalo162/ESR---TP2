# Auditoria dos artefactos antes dos bolts

Data: 2026-11-02T15:02:08Z. Base Git: `85e1b33ff0a1de69116dfdf3e7ee2d2352cbf936`. Âmbito: rever/corrigir a documentação e preparar
bolts por fases para o **Construction**, conforme esclarecimento do utilizador.
Não se implementa a aplicação nem se executam os bolts nesta preparação.

## Inventário e conclusão

- 35 requisitos, sete fases, 31 tarefas e 31 validações existentes.
- TASK-001–005 DONE; 26 restantes TODO. Os estados são conservados nos TASK/docs/15.
- Configuração, codec e transporte TCP implementados; bootstrap contém apenas um
  comentário; serviços de overlay, monitorização, routing e vídeo ainda faltam.
- O último log de 27-10 regista 149 testes (65+66+18). Não se reexecutou Maven.
- Conferidos os 45 pares binário/hex, tamanhos/SHA-256 do manifesto e o parsing
  dos três XML CORE. Isso não testa aplicação, IP, convergência, GUI ou vídeo.
- Faltavam artefactos specsmd: esquema/contexto local, requirements importados,
  system-context, unit briefs, histórias, índice e instâncias de bolt.
- O relatório final é trabalho futuro em TASK-029. As notas incrementais não são
  resultados de uma aplicação integrada.

## Correções anteriores à criação dos bolts

| Achado | Correção | Efeito |
|---|---|---|
| ADR-013/014 citadas sem secções próprias | Consolidar decisões já adotadas em docs/12 | Preservar semântica/bytes existentes |
| send só confirma fila, mas descoberta exige resposta antes de EOF | ADR-015 em docs/12, API planeada em 07, TASK-006 e V-006 | Extensão assíncrona de escrita/flush, com prazo e regressão |
| Parágrafos ADR-013 repetidos | Remover duplicações em 00/07 | Orientação atual sem repetição |
| Roadmap apontava de novo a TASK-001 | Indicar TASK-006 após verificar a base | Não repetir trabalho concluído |
| S3 da estratégia fixava cinco relays | Alinhar com TASK-027, exemplo de duas origens/três relays/cinco clientes | Reutilizar os dez PCs do XML existente |
| Faltava entrada reproduzível para Construction | Criar esquema, standards e tipos de bolt locais | Etapas e provas explícitas sem inventar infraestrutura |

## Pendências externas e gate responsável

| Gate | Tarefas | Informação/artefacto em falta | Prova de fecho |
|---|---|---|---|
| G-MEDIA | TASK-020 | Nenhum vídeo final no repositório; B/C/D só identificados | Obtenção/preparação, JPEG/formato válidos, hashes e conteúdo distinto |
| G-MEDIA-DISTRIBUTION | TASK-030 | Créditos/condições da versão incluída | Proveniência e inclusão/obtenção reproduzível |
| G-GUI | TASK-023/024 | Reprodução numa janela real | Conteúdo identificável; headless não a substitui |
| G-CORE | TASK-025–028 | Runtime, JDK, GUI, IP e cenários reais | Manifesto, mapa e medições nos XML existentes |
| G-ASSESSMENTS | TASK-009/018/031 | Datas/conteúdo docente | Informação real ou pendência explícita; sem calendário inventado |
| G-DELIVERY | TASK-030/031 | PL, grupo e prazo reais | Nomes finais e checklist com dados fornecidos |
| G-SUBMISSION | TASK-031 | Upload pela equipa | Confirmação humana da versão entregue |

São gates de execução nas tarefas certas, não requisitos desconhecidos que
impeçam planear os bolts. Não converter todas as tarefas futuras em BLOCKED.
Não foram feitos downloads de vídeos, instalação, contactos externos ou
submissão. A seleção/preparação de media já pertence ao âmbito de TASK-020.

## Decisões preservadas

Java 21/Maven; biblioteca padrão no runtime; controlo TCP; vídeo UDP próprio;
bootstrap só inicial; path vector e soma RTT EWMA; um parent confirmado por
stream; ACK/revisões/leases entre vizinhos; MJPEG de cinco dígitos + JPEG;
snapshots e filas limitadas; sem failover de conteúdo entre origens.

## Critérios da nova organização

Cada trabalho futuro tem história, TASK, V-XXX, critérios, dependências,
ordem interna, outputs e evidência. Unidades são agrupamentos de trabalho
no mesmo projeto Java, sem criar serviços/pacotes de produção artificiais.
Revisões humanas atribuídas e integração permanecem exigidas pela DoD.

Validação reproduzível: `python3 scripts/validate-planning.py --check-snapshot`.
Consultar a [evidência](../experiments/inception-planning-validation.md).

Controlos negativos do verificador: [rejeições confirmadas do validador](../experiments/inception-validator-controls.md), em cópia temporária e sem alterar as fontes.


## Reconciliação posterior pelo ADD

As contagens anteriores registam a primeira preparação. A [reconciliação atual](19-ADD-RECONCILIATION.md)
incorpora 1A/2A/3B/4A: 40 entradas, 26 ADR e 33 incrementos, incluindo TASK-032.
O ADD é a arquitetura normativa; o estado de implementação não foi promovido.
