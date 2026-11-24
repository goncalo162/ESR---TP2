# IMP-017 / 011-video — Checkpoint Test,manual humano pendente

Implement aprovado;Test autorizado. Sem staging/commits/push/fecho.
[Test walkthrough](../../../../memory-bank/bolts/011-video/test-walkthrough.md),
[manifesto de media aprovado e preservado](../../../media/manifest.json).

## Validação nova sobre ficheiros finais

[Consolidação](consolidation.json),[auditorias](full-audit-results.json):
22687/22687 JPEGs,sem avisos,ImageIO/JDK21 headless,um frame/imagem de cada vez.
A500,B5965,C8880,D7342;459820128 bytes. Os inventários coincidem por hash com os
anteriores de Implement,sem alterar binários ou o manifesto aprovado.
A/B/C/D-audit.log,A/B/C/D-command.json e A/B/C/D-frames.jsonl guardam comandos,
exits,offsets/comprimentos/hashes JPEG/pixels/dimensões/decoder de todos os registos.
[Auditor recém-compilado](audit-tool.json),[compilação](audit-compile.log).
Sem mudanças a Java de produção/teste ou dependência runtime.

[16/16 fronteiras/negativos](negative-results.json),
[proveniência das mesmas expectativas](negative-run-provenance.json):apenas paths
de saída/classpath adaptados,casos não enfraquecidos. Prefixos parciais,não decimal,
zero,truncaturas/corrupto/extra/seis dígitos rejeitados;EOF limpo e JPEG válido99999
aceites. Não modificar recursos reais para negativos.
[Distinção dos seis pares](distinctness.json):hashes de ficheiros e de sequências de
pixels diferentes;proveniência e observações visuais do agente preservadas em
../implement-01/distinctness.json. Confirmação humana ainda pendente.

Limite99999 do perfil cinco dígitos separado do cap UDP2MiB. FPS10 é configuração/
preparação,não FPS entregue/apresentado medido.1537 testes Java de IMP-016 são
históricos,não nova regressão nem contagem deste auditor de dados. Sem Maven por inferência.

## Manual preparado,sem execução humana

[Instruções completas](../manual-instructions.md),
[configuração de hashes](../test-manual-tools/approved-media.json),
[verificação técnica do auxiliar](manual-tools-check.json).
Sete etapas pessoais,13 checks,118 amostras de JPEGs originais em página local.
Teste técnico preparou apenas um pack temporário,verificou hashes/referências e
apagou-o;sem browser,diálogo humano ou MANUAL_PASS. Não há ainda execução humana.
Legibilidade classificada explicitamente legivel/parcial/ilegivel e notas obrigatórias;
MANUAL_PASS apenas conclui o guião,não aprova legibilidade integral,Test ou redistribuição.
Este manual Python não executa JVM;nenhum JAVA_EXIT é inventado.

## Gates,composição e volume

G-MEDIA aguarda manual/aceitação da validação final. G-MEDIA-DISTRIBUTION aberto,
licença audiovisual de A não inferida;condições/créditos finais pertencem TASK-030.
LOCAL-03 opção1 explícita:cópia com hash para Projeto/media só na composição
posterior;perfis atuais não anunciados prontos. [Volume/plano posterior](integration-size-plan.md):
459820128 bytes de media mais manifesto/evidência,sem escolha de Git LFS,externo,
ponteiros ou modificação de conteúdos por inferência.

[V-ADD](v-add.md),[preservação](preservation.json),[diff](cumulative.diff),
[numstat](cumulative.numstat),[manifesto do checkpoint](manifest.json).
Test por aprovar;manual humano,revisão final e integração pendentes. Parte A não
fecha TASK-020 inteira. Unidade/fase em progresso,IDs locais,demo/incrementos
fechados preservados. Binários/manifesto/Java/perfis/POM/fixtures intactos.
