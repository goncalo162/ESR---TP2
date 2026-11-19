# IMP-012 — evidência incremental de Implement

Base main/b273b258a4642f94f8614ae00f060a46318d77a5. Comportamentos autorizados
nos três ficheiros; Implement por aceitar, Test não autorizado. Sem staging/commit/push.

- [Resultado final](regression-02-result.json):1379/0/0/0,20 XML;51 testes de leases,
  incluindo TCP real e24 grupos L01–L24. [Log](regression-02.log).
- XML finais em `regression-02-xml/`; regressão anterior1375 em `regression-01-xml/`.
- [Preservação](preservation.json), [hashes observados durante a execução](regression-02-observation.json).
- [Diff Java](java.diff), [numstat Java](java.numstat); target excluído do diff de entrega.
- Negativos `attempt-01.log/.xml`, `attempt-02.log/-lease.xml/-activation.xml`,
  `expanded-01.log/.xml` e compilações `expanded-02.log`/`expanded-03.log`.
- Verdes `attempt-03.log`, `expanded-04.log/.xml`, `expanded-05.log/.xml`,
  `boundaries-01.log/.xml`, `regression-01.log` e final `regression-02.log`.
- [Walkthrough anterior preservado](prior-walkthrough.md.txt). Evidências anteriores
  em `../implement-interfaces-01/` não foram alteradas.

Comando final: `JAVA_HOME=/Users/maria/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home mvn -B -ntp -f Projeto/pom.xml test`.
JDK21.0.13+9/Maven3.9.9; testes TCP/CLI fora da sandbox após bind recusado.
Não houve clean verify/JAR/manual humano/revisão Denji/integração nesta etapa.
TCP é loopback e não demonstra vídeo/GUI/CORE. Portas verificadas: seis TCP de
controlo; nenhuma medição UDP/vídeo reivindicada. Poda best effort mantém fallback
pelo último grant remoto finito; não há meta7s medida.

V-ADD: [resultado sem erros](v-add-02.md). Negativos documentais
v-add-initial.md/v-add-final.md preservados; correção exclusiva do formato UTC
em updated do bolt. Validador intacto.
