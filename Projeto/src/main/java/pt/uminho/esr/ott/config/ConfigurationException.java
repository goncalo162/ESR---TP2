package pt.uminho.esr.ott.config;

import java.util.List;
import java.util.stream.Collectors;

/** A carga falha como um todo: não devolve uma configuração parcial. */
public final class ConfigurationException extends Exception {
    private final List<ConfigurationProblem> problems;

    public ConfigurationException(List<ConfigurationProblem> problems) {
        super(problems.stream().map(ConfigurationProblem::toString)
                .collect(Collectors.joining(System.lineSeparator())));
        this.problems = List.copyOf(problems);
    }

    public List<ConfigurationProblem> problems() {
        return problems;
    }
}
