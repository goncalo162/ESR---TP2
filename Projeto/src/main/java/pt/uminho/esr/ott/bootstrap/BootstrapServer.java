/*
 * O bootstrap serve apenas para o arranque dos nós: fornece a cada nó a lista
 * dos seus vizinhos adjacentes na rede overlay.
 * Após obter essa lista, o nó comunica diretamente com os seus vizinhos.
 * A lista provém apenas da configuração, incluindo vizinhos offline; pedidos
 * repetidos não criam registos, leases, renovações ou estado de disponibilidade.
 * O bootstrap não recebe avisos de saída nem monitoriza os nós. Identidades
 * de execução e duplicados são verificados diretamente no handshake dos vizinhos.
 * O bootstrap não intermedeia a comunicação entre nós nem reencaminha
 * mensagens de controlo ou fluxos multimédia.
 */
