# Retoma M07 — Implement pendente

Base main `9c5ad094e58313622a5a695e259cd91b15a71a72`, entrada limpa.
Aplicação Q-C-38 autorizada exclusivamente pelo utilizador; sem staging/commit/push.

- [Entrada e hashes](entry.json), [proveniência](entry-provenance.json).
- [Patch Q-C-38](q-c-38-proposed.patch), [aplicação](q-c-38-application.json).
- [Negativo Maven M07: 3/2/0/0](red-maven/result.json), [log](red-maven/maven.log).
- [M07 verde: 6/0/0/0](green-m07/result.json).
- [ActivationTest: 31/0/0/0](activation-class/result.json).
- [Regressão incremental: 1328/0/0/0, 18 suites](regression/result.json),
  [comando/ambiente/hashes](regression/start.json), [log](regression/maven.log).
  XML/TXT de todas as suites copiados para regression, com deploys CLI/TCP reais.
- [Preservação: 229 outros src e 431 evidências anteriores](preservation.json).

Negativos anteriores preservados: red (compilação CLI), red-core (classloader),
red-core-02 (M07 funcional em core isolado, sem CLI), red-maven/launch-note.json
(cwd inválido antes de Maven). Não equivalem à regressão integral.

Causa/correção e hashes no walkthrough, secção 11. C1 terminal não é reinstalado
pelo ACK tardio; C2 e lease intactos; callback/continuação retêm reserva física;
revisão antiga não altera substituta. Q-C-36/37 e testes históricos preservados.

target é saída de execução separada da entrega. Não foi gerado JAR final nem
executado clean verify. Implement pendente; V-014 consolidado, Test, manual
humano, revisão final Waheed e integração pendentes.

V-ADD executado nesta retoma: 1 erro de trailing whitespace preexistente em
docs/style/CODESTYLE.md, byte a byte igual a HEAD. Q-C-39 aberta; ficheiro e
validador preservados, V-ADD não aprovado. Ver relatório e v-add-limitation.json
na pasta da evidência.
