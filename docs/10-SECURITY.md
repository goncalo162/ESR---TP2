# Segurança proporcional ao protótipo

O enunciado não exige autenticação de utilizadores, TLS, proteção de dados pessoais nem operação pública na Internet. A solução proposta é um protótipo em laboratório controlado; a allowlist de nós serve consistência da topologia e não comprova identidade criptográfica. Não inventar um sistema de contas ou declarar confidencialidade que TCP/UDP simples não oferecem.

## Fronteiras e ameaças relevantes

Entradas não fiáveis: CLI, ficheiros de configuração, frame TCP, datagrama UDP, metadados de vídeo e bytes MJPEG. Riscos concretos: comprimento exagerado provoca alocação, flooding causa carga, nó com ID duplicado confunde estado, pacote de vizinho errado injeta vídeo e path de ficheiro permite leitura não prevista. Acesso local ao laboratório é pressuposto; spoofing numa rede hostil não é resolvido pelo protocolo proposto.

## Controlos previstos

| Área | Regra e tarefa responsável |
|---|---|
| Configuração | Validar propriedades, limites, endpoints e grafo antes de sockets; TASK-003 |
| TCP | Framing máximo 64 KiB, UTF-8 estrito, enums/listas limitados e leitor/escritor únicos; TASK-004/005 |
| Descoberta inicial | Bootstrap valida apenas a consulta face à configuração; não guarda disponibilidade nem arbitra instâncias vivas; TASK-006 |
| Admissão entre vizinhos | HELLO com identidade/instância e validação da allowlist; um canal por par; sem intervenção do bootstrap; TASK-007 |
| Duplicados | Rejeitar identidade já viva, invalidar callbacks de conexão velha e correlacionar requests/revisions; TASK-007/014/015 |
| Flooding | Vetor de caminho, validade, máximo de IDs, cache e rate limit; TASK-012 |
| Subscrições | Aceitar apenas mensagens do child associado, com StreamKey, epoch e revisão corretos; lease e timeout; TASK-014/015 |
| Vídeo UDP | Endpoint do upstream confirmado, epoch/stream ativas, magic/tamanhos/índices validados; TASK-019/021 |
| Memória e CPU | Filas limitadas, dedup/cache com expiração, oito montagens por stream e deadline; TASK-021/022 |
| Ficheiros de media | Paths apenas em configuração local; resolver dentro de media.base e rejeitar escape, incluindo symlinks após normalização real; TASK-020 |
| Erros/logs | Erro externo com código e detalhe curto; stack trace local limitado; não logar payload de vídeo ou credenciais; TASK-008/024 |
| Entrega | Não incluir segredos, caches ou credenciais Blackboard; confirmar recursos redistribuíveis; TASK-030/031 |

A validação de endpoint UDP reduz erros/injeção acidental dentro do laboratório; não substitui integridade autenticada. Não adicionar criptografia parcial ou chaves hardcoded. Se o âmbito passar a rede não confiável, registar decisão e rever autenticação de nós, integridade e confidencialidade antes da exposição.

## Ambiente CORE e privilégios

A bancada existente usa container privilegiado e partilha X11/volume. Estes privilégios pertencem ao emulador, não são necessários ao processo Java em geral. Limitar volumes ao necessário e usar autorização X11 restrita à bancada. Evitar alargar permissões gráficas globalmente. O plano não executa Docker nem altera as permissões do host.

Validar versões e origem de dependências e exemplos em TASK-001/020; não executar scripts descarregados para interpretar um vídeo. Não colocar credenciais do Blackboard em .properties, logs, chat ou ZIP. O upload final é manual pela equipa, com confirmação sem dados sensíveis.

## Verificação

Os testes de limites/identidade estão incluídos em V-003/004/005/006/007/012/014/015/019/021/022. A extração da entrega em V-030 verifica paths seguros e conteúdo apropriado. Um teste negativo deve provar rejeição e ausência de alteração do estado válido dos outros nós/fluxos. Nenhum resultado destes testes é alegado neste planeamento.
