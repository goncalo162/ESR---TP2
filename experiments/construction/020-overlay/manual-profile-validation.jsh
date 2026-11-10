import java.nio.file.*;
import java.util.*;
import pt.uminho.esr.ott.config.*;
class ManualProfileValidation {
    public static void main(String[] args) throws Exception {
        var base = Path.of(args[0]);
        var configs = ConfigurationLoader.loadDeployment(List.of(base.resolve("bootstrap.properties"), base.resolve("N0.properties"), base.resolve("N1.properties"), base.resolve("N2.properties")));
        for (var c : configs) System.out.println("PROFILE_VALID node=" + c.nodeId() + " role=" + c.role() + " control=" + c.controlPort() + " data=" + c.dataPort());
        System.out.println("DEPLOYMENT_VALID profiles=" + configs.size());
    }
}
