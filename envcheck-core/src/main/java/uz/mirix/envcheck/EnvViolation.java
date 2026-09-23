package uz.mirix.envcheck;

import java.util.Objects;

public record EnvViolation(String name, ViolationCode code, String message) {
    public EnvViolation {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }
}
