# Vetores operacionais — TCP v2 e UDP OTT1

66 fixtures: 50 TCP v2 independentes e 16 UDP OTT1/version 1 preservadas. Bin/hex, tamanhos, SHA-256 e expectativas estão em manifest.json. Os vetores TCP encontram-se em v2/; as fixtures UDP mantêm os nomes e bytes anteriores.

TCP: prefixo u32 big-endian, corpo 37..65536, envelope version=2/type/requestId/senderId/senderInstance; tipos 1,2,4..19 e código 3 rejeitado. HELLO contém Endpoint+supported/required=1, HELLO_OK selected=1. Bootstrap também é v2 e continua só consulta inicial. Não existe decoder/encoder runtime v1 ou fallback.

Os 50 vetores TCP foram construídos fora do codec Java a partir dos campos literais do Design, auditados por um leitor Python independente e aprovados pelo utilizador. Não os regenerar com o encoder de produção para obter testes verdes. MessageCodecTest compara bytes e modelos esperados em ambos os sentidos; DiscoveryCodecTest separa erros terminais de conteúdo extra rejeitado.

22 vetores positivos, 23 negativos terminais e cinco de conteúdo extra. reject-content exige adicionalmente validação de identidade/sessão atual no owner; a fixture isolada não prova autorização ou continuidade da ligação. Negativos de campos mantêm version=2 para testar a causa indicada; casos de versão errada são separados.

UDP: OTT1, versão 1, cabeçalho 64, total <=1200, payload <=1136; inalterado. Os dados sintéticos não provam JPEG, GUI ou vídeo real.

Os arquivos TCP v1 foram retirados por decisão explícita Q-C-19. V-ADD verifica os 50 vetores TCP v2 independentes e os 16 UDP OTT1 face à base integrada; não substitui os testes Java/manual. Os testes negativos de rejeição da versão TCP v1 são parte do contrato v2.
