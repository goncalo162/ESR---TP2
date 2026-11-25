# IMP-018 — Manual humano do leitor local

Atualização 2026-11-25T17:06:50Z: a execução pessoal de Luis terminou negativa e permanece
preservada. O utilizador delegou depois a repetição em Codex e autorizou
commit/fecho se correto; a run automática passou nos quatro conteúdos completos.
[Autorização](test-02/acceptance-and-review.json). Não se declara manual humano
concluído; o roteiro abaixo continua disponível para repetição pessoal facultativa,
sem tornar essa repetição um novo gate após a alteração expressa do utilizador.


Estado: **preparado, não executado por humano**. Sete etapas em terminal,
Java21 e Python3, sem dependências novas, navegador, GUI, CORE ou nós OTT.
Executa o leitor contido no JAR novo, sem pacing, loop, emissão ou player.
A confirmação deste guião não aprova Test, revisão Denji ou integração.

## Construção fixada e condição dos conteúdos

JAR: `test-01/clean-verify/ott-streaming-1.0-SNAPSHOT.jar`.
SHA-256: `f9a3cb9cad6166d13e244a772398179731952fd8eb8ff0efe7b16a6f67b9787f`.
[Registo fixado](test-manual-tools/approved-build.json).
O runner rejeita outro hash e JDK diferente de21; não recompila ou altera Java.

Os quatro ficheiros completos têm de ser fornecidos localmente com estes hashes:

| Conteúdo | SHA-256 aprovado | Bytes | Frames |
|---|---|---|---|
| A | `ca461cb1edce18efb355794ffb7d52f73dc7ce4c03422224894eb0fc550b0cca` | 4269893 | 500 |
| B | `ceb6497674bde7210bff2162b07e4305a74b33ed2b5a381b3e973d30fc837c5f` | 111255866 | 5965 |
| C | `d2a050894ced76969b2bafae669532141dccf770fa2352e9ac4d0a4374bed3ab` | 167319819 | 8880 |
| D | `c47ef9b41f05904e1bb6657424e2d88fe504e0925c0dffbdee85c58bf8c8e4cc` | 176974550 | 7342 |

Os paths da aplicação continuam como ponteiros LFS. Os quatro binários completos
foram agora recuperados do Git LFS remoto para `test-02/media/`, com hashes e
tamanhos iguais aos aprovados; sem alterar recursos/configuração da aplicação.
[Prova integral atual](test-02/full-content-consolidation.json):500/5965/8880/7342
frames conferidos pelo novo leitor no JAR fixado, EOF e fecho único da entrada.
A run automática não substitui a execução humana das sete etapas.
Os ensaios anteriores parciais/negativos permanecem intactos.

## Comando pessoal

Na raiz do repositório, exemplo para Luis nesta máquina:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
python3 -B experiments/construction/025-video/test-manual-tools/run-checks.py \
  --kind human --operator Luis \
  --media-dir experiments/construction/025-video/test-02/media
manual_exit=$?
printf 'SHELL_EXIT=%s\n' "$manual_exit"
```

O diretório deve conter `a.Mjpeg`, `b.Mjpeg`, `c.Mjpeg`, `d.Mjpeg` completos.
Em alternativa, indicar paths existentes individualmente, por exemplo
`--media A=/caminho/a.Mjpeg --media B=/caminho/b.Mjpeg`, mantendo os restantes
no media-dir. Não copiar/descarregar conteúdos por este runner. Noutro sistema,
indicar o Java21 instalado com `--jdk-home /caminho/do/jdk21`.
`--jar` só aceita um ficheiro com o mesmo SHA-256 fixado.

O modo human exige terminal real; não enviar respostas por pipe. Ler as saídas
antes de responder `sim`; qualquer `nao` preserva resultado incompleto/falha.
`--kind rehearsal` e `--kind automated` são execuções técnicas sem confirmações
humanas e nunca ficam registadas como manual humano concluído.

## Sete etapas

1. Conferir Java21 e SHA-256 do JAR apresentados; confirmar construção correta.
2. Inspecionar dois JPEGs independentes lidos pelo novo JAR: índices0/1,
   offsets0/689, bytes originais, EOF e estadoEND. Não é vídeo reproduzido.
3. Inspecionar os cinco negativos: INVALID_PREFIX, TRUNCATED_PREFIX,
   TRUNCATED_BODY, FRAME_LIMIT e INVALID_JPEG. Confirmar rejeição e primário
   estável, sem recuperação silenciosa ou frame parcial.
4. Confirmar que alterar bytes() não altera Frame e fechar uma sessão não
   avança/fecha a segunda sessão aberta sobre o mesmo ficheiro.
5. Inspecionar A/B/C/D completos: hash/bytes aprovados antes de abrir, JPEGs
   efetivamente descodificados pelo reader real, comparação de cada Frame com
   a auditoria independente aprovada, contagem, hash de todos os bytes que o
   VideoSource consumiu, EOF/END, entrada fechada exatamente uma vez e close
   repetido sem nova tentativa. Uma única pendência impede manual completo.
6. Confirmar CLOSED_USE após close explícito e EOF vazio na fronteira; inspecionar
   o controlo do verificador com apenas duas fixtures. VS_FULL_PASS frames=2
   neste controlo **não** é prova de qualquer um dos quatro conteúdos completos.
7. Reconhecer a fronteira: apenas leitura local;16MiB de raster destino não
   limita toda a memória do decoder/JVM. Sem playback/pacing/emissão; R17 inicial
   permanece sem causa estabelecida. Fecho de TASK-020 só apósA/B/C.

## Como a prova integral observa a implementação real

O script JShell usa a classe do JAR fixado. Nos testes de paths, erros e sessões
usa open/nextFrame/state/close públicos. Na leitura integral usa por reflexão
**apenas o porto de ownership já aprovado de três argumentos**, fromOwnedInput,
para envolver Files.newInputStream com contadores/hash. Não injeta ReaderFactory
nem stub de ImageReader: seleção e decode JPEG são os da produção aprovada.
O decorador não simula bytes nem fecha a entrada antes do VideoSource.

VS_FULL_PASS dos conteúdos regista o SHA-256 consumido, frames e bytes; verifica
cada JPEG contra o JSONL histórico cujo hash também é conferido. O fecho da
entrada física tem contador e resultado. O descarte transitório do reader/raster
é exercitado em M10 dos testes JUnit; não é contado separadamente por frame
na passagem integral. Não confundir essas duas observações.

JShell recebe o JAR no classpath de compilação e no da própria JVM, execução
local/headless; não há sockets da aplicação. Preferências e fixtures ficam
apenas na nova pasta da run. `-J-Xmx256m` é uma opção do processo do verificador,
**não** uma garantia de consumo total16MiB ou alteração da configuração OTT.

## Resultados a devolver

Cada execução cria test-manual-runs/<ID-novo>/, sem substituir anteriores.
report.json guarda operador/kind/respostas, comandos, hashes, estado e logs;
manual.log guarda diálogo e resultados, com logs JShell separados.

- Saída0: checks técnicos completos. Só kind=human com sete confirmações e
  manual_human_complete=true comprova execução pessoal deste guião.
- Saída2: pendente por conteúdos completos ausentes/LFS; não é PASS do manual
  nem validação integral de V-020-B. Etapa5 não pede confirmação falsa.
- Saída1: falha técnica/recusa; guardar negativo, sem alterar código aprovado.
- Saída130: interrupção, relatório parcial; nenhuma aprovação presumida.

Devolver caminho de report.json, SHELL_EXIT real e observações. O ensaio de
Codex não substitui a repetição pessoal. Não pedir aprovação de Test antes
da prova dos quatro conteúdos completos e do manual humano aplicável.
