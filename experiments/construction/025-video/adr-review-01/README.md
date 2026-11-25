# IMP-018 / 025-video — revisão ADR preparada

Design/guardas aprovados; apenas revisão ADR autorizada.
[Revisão para aprovação](../../../../memory-bank/bolts/025-video/adr-review.md).
Sem impedimento de API/âmbito identificado;sem nova ADR arquitetural.
A01–A08 concretizam a implementação futura nos dois caminhos autorizados.

**Precisões apresentadas antes de código:**A05 distingue ausência de provider
retornada pela factory de retorno inválido deImageReader comdestino explícito;
o último é inesperado,não corrupção JPEG checked. A06 inclui cleanup de
imagem extra devolvida indevidamente:teto conservador6 recursos/secundários
nesse ramo,sem mudar caps do raster ou mascarar o primeiro erro.
As precisões e a revisão permanecem por aprovar;nenhum patch Java foi aplicado.

## Inspeção real, sem executar o leitor

[Fontes/API do projeto](project-api-inspection.json):VideoSource.java0 bytes,
sem consumidoresJava;VideoSourceTest inexistente. StreamConfiguration,
NodeConfiguration,RuntimeConfiguration eConfigurationLoader já fornecem dados
e limites necessários,sem alteração de configuração/POM/protocolo.

JDK local21.0.13.1:assinaturas por javap em[jdk21-public-apis.txt](jdk21-public-apis.txt)
e[jdk21-support-apis.txt](jdk21-support-apis.txt). O src.zip local não existe;
sem instalação. Bytecode do providerUnix confirma NOFOLLOW_LINKS em
[jdk21-unix-open-options.txt](jdk21-unix-open-options.txt);classeJPEGImageReader
inspecionada em[jdk21-jpeg-reader-bytecode.txt](jdk21-jpeg-reader-bytecode.txt).
Javap é inspeção de classes,não compilação ou execução deVideoSource.

O scanner ainda não existe:foi conferida a sua especificação de fronteiras
comT.81 e a relação comImageIO,não declarado runtime PASS. Não foi aberto/
descodificado qualquer vídeo ou executado M01–M14/V-020-B. Os22687 registos
aprovados usados no Design continuam a ser a evidência dimensional anterior.
16MiB são apenas o raster de destino,não consumo total da JVM/provider.

## Estado e limites

[Verificação de âmbito](verification.json):fontes inspecionados inalterados,
remoção da demo preservada,Model/Design como únicas etapas concluídas.
[Validação documental](v-add.md):mesmos6 erros,zero novos;V-ADD não aprovada.
[Seis pendências](../design-01/v-add-pendencias.md) obrigatórias antes do fecho,
sem corrigir templates/repor demo/remover referências por inferência.

Sem Implement,alteraçãoJava,compilação/teste/leitor,Maven,commits,push ou outro
IMP. Fecho:regressãoJDK21,manual humano aplicável,Denji e integração;sem gates
externos adicionais. TASK-020 só fecha apósA/B/C;LOCAL-03/04,distribuição eR17
mantêm as limitações anteriores. Não há aprovação deImplement/Test nesta revisão.
