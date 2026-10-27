package pt.uminho.esr.ott.config;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

/** Metadados locais: os bytes do vídeo só serão lidos na TASK-020. */
public record StreamConfiguration(Path sourceFile, UUID streamId, String serverId,
                                  String title, Path mediaFile, int fps, boolean loop) {
    public StreamConfiguration {
        Objects.requireNonNull(sourceFile);
        Objects.requireNonNull(streamId);
        Objects.requireNonNull(serverId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(mediaFile);
    }
}
