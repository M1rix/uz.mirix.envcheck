package uz.mirix.envcheck;

import java.util.List;
import java.util.Objects;

public record EnvCheckResult(String name, String propertyKey, boolean present, boolean sensitive, String displayValue, List<EnvViolation> violations) {
    public EnvCheckResult {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(propertyKey, "propertyKey");
        Objects.requireNonNull(displayValue, "displayValue");
        violations = List.copyOf(Objects.requireNonNull(violations, "violations"));
    }
    public boolean valid() { return violations.isEmpty(); }
}
