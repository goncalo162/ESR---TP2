# IMP-018 / 025-video — Design para revisão

Model eIMP-018-LOCAL-01/02 aprovados;apenas Design autorizado.
[Design proposto](../../../../memory-bank/bolts/025-video/ddd-02-technical-design.md).
API open/nextFrame/close e tipos aninhados;ownership e erro primário;
readbulk=0 falha imediatamente;guardas antes do decode;cleanup por recurso.

[Dimensões derivadas da auditoria aprovada](approved-audit-dimensions.json):
22687 registos com hashes verificados,A384×288/B-C-D640×360. Nenhum vídeo
reaberto/descodificado ou fonte Java lido/analisado. Guardas propostas4096
por dimensão/4194304pixels/raster destino16MiB;comB=99999,os quatro conteúdos
cabem. Overrides menores continuam respeitados. Compatibilidade de dimensões
não equivale a um teste do futuro leitor,parser ou decoder integrado.

[Scope](scope-verification.json) e [entrada](entry-state.json): fontes/CORE
inalterados,remoções da demo preservadas. Apenas documentação/metadados;
sem Implement,pacing/loop/emissão,configuração/protocolo,Java/Maven,commits/push.
M01–M14/V-020-B continuam por executar;nenhuma aprovação deDesign/ADR/Test.

[Validação global atual](v-add.md):mesmos seis erros do Model,zero novos.
[Pendências duradouras](v-add-pendencias.md) a resolver **antes do fecho**;
**V-ADD não aprovada**. Dois fences de templates e quatro destinos removidos
da demo não foram corrigidos por inferência. Links locais e whitespace do
novo Design passaram;isso não muda o resultado global.

Fecho exige testes/regressãoJDK21,manual humano aplicável,Denji e integração.
TASK-020 apenas fecha apósA/B/C. Sem gates externos adicionais;LOCAL-03/04,
G-MEDIA-DISTRIBUTION e R17 mantêm as limitações já registadas.
