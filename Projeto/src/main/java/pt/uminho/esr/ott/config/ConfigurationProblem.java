package pt.uminho.esr.ott.config;

import java.nio.file.Path;
import java.util.Objects;

public record ConfigurationProblem(Path file, String key, String message) {
    public ConfigurationProblem {
        Objects.requireNonNull(file);
        Objects.requireNonNull(key);
        Objects.requireNonNull(message);
    }

    @Override
    public String toString() {
        return file + " [" + key + "]: " + message;
    }
}
