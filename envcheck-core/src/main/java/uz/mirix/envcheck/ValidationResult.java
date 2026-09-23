package uz.mirix.envcheck;

import java.util.List;
import java.util.Objects;

public final class ValidationResult {
    private final List<EnvCheckResult> checks;
    private final List<EnvViolation> violations;
    public ValidationResult(List<EnvCheckResult> checks) {
        this.checks = List.copyOf(Objects.requireNonNull(checks, "checks"));
        this.violations = this.checks.stream().flatMap(check -> check.violations().stream()).toList();
    }
    public List<EnvCheckResult> checks() { return checks; }
    public List<EnvViolation> violations() { return violations; }
    public boolean valid() { return violations.isEmpty(); }
    public int validCount() { return (int) checks.stream().filter(EnvCheckResult::valid).count(); }
    public int invalidCount() { return checks.size() - validCount(); }
}
