import java.nio.file.*;
import java.util.*;
import pt.uminho.esr.ott.config.*;
try { var profiles = ConfigurationLoader.loadDeployment(List.of(Path.of("/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/005-rotas/manual-profiles/BOOT.properties"), Path.of("/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/005-rotas/manual-profiles/R1.properties"), Path.of("/Users/maria/Documentos/Francisco/ESR/ESR---TP2/experiments/construction/005-rotas/manual-profiles/R2.properties"))); System.out.println("MANUAL_DEPLOYMENT_VALID profiles="+profiles.size()); } catch (Exception e) { System.out.println("MANUAL_DEPLOYMENT_INVALID "+e); }
/exit
