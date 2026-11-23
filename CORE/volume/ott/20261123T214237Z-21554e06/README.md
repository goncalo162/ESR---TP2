# Deployment no CORE — cenario2 (run 20261123T151525Z-1b1daa64)

1. No CORE GUI, abrir a topologia `/home/goncalom/Documents/Uni/4.º ano/1.º semestre/ESR/ESR---TP2/CORE/topologies/cenario2.xml` e confirmar IPs (manifest.json → deviceIp).
2. Copiar esta pasta para `CORE/volume/ott/cenario2/` (volume montado em /volume).
3. No host de S1 (10.0.0.20): arrancar o bootstrap (comando em manifest.json → BOOT).
4. Em cada nó: `cd /volume/ott/cenario2 && <comando do manifest.json para o nó>`.
5. Validar com a CLI real (`OttApplication`) ou com o adaptador DemoNode (subscribe/trace/snapshot).
6. Recolher logs em experiments/ segundo docs/09-TESTING-STRATEGY.md.

Gerado por prepare_core.py; ver demo/GUIA-DEMO.md para o guia completo.
