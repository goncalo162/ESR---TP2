import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import pt.uminho.esr.ott.config.ConfigurationLoader;
import pt.uminho.esr.ott.protocol.MessageCodec;

class Inspect {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("profiles <directory> | frames <raw TCP direction>");
        Path input = Path.of(args[1]);
        switch (args[0]) {
            case "profiles" -> {
                var profiles = List.of("bootstrap.properties", "N0.properties", "N1.properties", "N2.properties");
                for (var config : ConfigurationLoader.loadDeployment(profiles.stream().map(input::resolve).toList())) {
                    System.out.printf("PROFILE node=%s role=%s TCP=%d%n", config.nodeId(), config.role(), config.controlPort());
                }
            }
            case "frames" -> {
                if (Files.size(input) > 1_048_576) throw new IllegalArgumentException("TCP export exceeds 1 MiB");
                try (var stream = Files.newInputStream(input)) {
                    for (int index = 1; ; index++) {
                        var frame = MessageCodec.read(stream);
                        if (frame.isEmpty()) {
                            if (index == 1) throw new IllegalArgumentException("Empty TCP direction");
                            break;
                        }
                        var message = frame.orElseThrow();
                        System.out.printf("%d %s requestId=%s sender=%s instanceId=%s payload=%s%n",
                                index, message.type(), message.requestId(), message.senderId(),
                                message.senderInstance(), message.payload());
                    }
                }
            }
            default -> throw new IllegalArgumentException("Unknown mode " + args[0]);
        }
    }
}
