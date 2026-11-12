# Reparação da overlay — IMP-035

Implement preparado para revisão; Test ainda não autorizado. Os comandos de
execução manual abaixo são o plano da próxima etapa, sem resultados atribuídos.

## Evidência incremental

Desde a raiz, com JDK21 e sockets locais autorizados:

```sh
cd Projeto
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -ntp -Dtest=OverlayRecoveryTest,NeighborManagerTest,NeighborManagerLifecycleTest,NeighborDiscoveryTest,TransportTest,BootstrapServerTest,OttApplicationTest,MessageCodecTest,DiscoveryCodecTest test
mvn -B -ntp -Dtest=CandidateCacheTest test
```

Logs/XML/hashes das execuções reais em experiments/construction/035-overlay-extra/.
D03/D04 do teste usam nós embutidos e TCP real com close explícito; não são
SIGKILL nem manual humano. Um teste de writer retém um worker sem o libertar
por interrupção para verificar reserva física/shutdown, libertando-o no finally.

## Roteiro a executar em Test

Preparar duas execuções com diretórios novos, operador identificado, instantes
reais, comandos, logs separados por processo, PIDs, instanceIds e hash do mesmo
JAR novo em todos os nós. O JAR histórico do IMP-034 é apenas evidência preservada.
Só construir/validar a regressão completa depois de aprovado Implement.

| Ensaio | Grafo inicial | Após primeira morte | Após segunda morte |
|---|---|---|---|
| A | S–R1–C; S–R2–C | R1 morto: alternativa R2, sem S–C | R2 morto: formar S–C |
| B | S–R1–R2–C; S–R1–R3–C | R2 morto: alternativa R3, sem R1–C | R3 morto: formar R1–C, conservar S–R1 |

1. Criar perfis locais isolados para cada grafo. Bootstrap leva topology.file com
   grafo simétrico; SERVER, RELAY e CLIENT levam respetivos IDs/portas/roles e
   o mesmo bootstrap. Configurar catálogo de streams válido para SERVER. Usar
   portas livres, respeitar Q≥2 e todos os limites. Não partilhar um perfil mutável
   entre execuções. Os perfis existentes em Projeto/src/main/resources/config/
   mostram a gramática, mas o seu grafo não é nenhum destes dois ensaios.
2. Arrancar BOOTSTRAP e cada nó em terminal próprio com o mesmo JAR:
   `java -cp "$JAR" pt.uminho.esr.ott.app.OttApplication --role "$ROLE" --config "$PROFILE"`.
   Guardar STARTED/READY e confirmar neighbors; o servidor aqui não emite vídeo.
3. Antes das falhas, comprovar aprendizagem por anúncios dos relays. Os eventos
   RepairStarted posteriores identificam a seleção por sourceKey, geração,
   queryStarted/TTL e requestId. Não preencher resultados antes de os observar.
4. Parar bootstrap por quit; manter sobreviventes originais. Em cada nó consultar
   neighbors/recovery. Registar a sessão S–R1 do ensaio B antes das mortes.
5. Identificar o PID exato de cada relay pelos logs STARTED e cmdline/perfil.
   Executar `kill -KILL "$RELAY_PID"` só sobre o processo do ensaio. Não usar
   pkill global. Após primeira morte esperar NeighborDown e confirmar alternativa
   preservada, sem reparação prematura. Repetir para o segundo relay depois da
   primeira observação, dentro do TTL60s da aprendizagem necessária.
6. Confirmar a aresta nova por NeighborUp/neighbors nos dois extremos, mesmo
   connectionId por sessão e sobreviventes com PIDs/instanceIds inalterados.
   No ensaio B, conferir S–R1 byte a byte. Correlacionar RepairStarted com a
   aprendizagem anterior, sem consulta nova ao bootstrap. Não confundir snapshot
   em cache com sessão ACTIVE. O caso sem candidato deve mostrar isolamento.
7. Exercitar neighbors/recovery enquanto há rejeições/retries. Encerrar
   sobreviventes por quit/EOF, conferir saídas, ausência de processos pendentes
   e portas livres. Registar insucesso/limitações, não ajustar a posteriori.
8. Luis repete o manual antes da aprovação de Test. V-034, regressão completa,
   revisão final e integração são pendências próprias. A ligação de controlo
   reparada não prova routing, recuperação de stream ou vídeo.

## Fixtures independentes

reference-v2/build-reference-fixtures.py usa campos literais com struct/uuid,
sem importar codec Java. A referência tem62TCP: quatro vetores de máscaras
migrados,46 preservados e12 novos. UDP16 permanece intacto. Não regenerar com
produção para fazer passar testes. O validador confere a referência anterior,
os deltas autorizados e a correspondência da cópia operacional.
