# Revisão local — ordem de validação e âmbito

StreamPacket.java substitui apenas stubs aprovados: construtor/accessor para U08;
encode/decode para U01–U03/U06/U07; fragmentação para U04/U05/U08.
Não há substituição de lógica de incrementos anteriores.

A região é limitada a65..1200 antes de criar uma vista ByteBuffer; subtração
buffer.length-offset impede overflow. Header e escalares são lidos exclusivamente
nessa vista. frameLength u32 é convertido para long; frame/count/index/payload
são validados antes da cópia. Não existe alocação de frame no decoder.
Objetos UUID/vistas têm tamanho fixo e não dependem de comprimento declarado.
O construtor valida antes do clone. Encode usa apenas o payload interno já
validado e imutável, alocando no máximo1200. Fragmentação verifica IDs/contadores/
array antes de lista/payload; no máximo1847 fragmentos e2MiB de payload retido.
As cópias temporárias de um fragmento são limitadas a1136; não se alega medição de heap.

Nenhum socket, fila, relógio, executor, cache ou lifecycle foi introduzido.
ProtocolException é apenas diagnóstico local, sem envio de ERROR TCP.
Nenhuma alteração a wire/caps/fixtures ou comportamento dos restantes componentes.
O record mantém igualdade padrão de arrays: oráculos comparam campos/bytes,
sem introduzir um contrato adicional de igualdade de conteúdo.

Testes existentes mantidos integralmente; adicionados dois testes de nulos e
região exata. Expectativas são literais/fixtures independentes, não round-trip.
Sem revisão final humana presumida: Denji ainda pendente.
