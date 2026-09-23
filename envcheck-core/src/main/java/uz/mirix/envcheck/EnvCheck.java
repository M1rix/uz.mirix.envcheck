package uz.mirix.envcheck;

import java.util.Objects;

public final class EnvCheck {
    private EnvCheck() {}
    public static ValidationResult validate(EnvSchema schema, ValueResolver resolver){ return new EnvValidator().validate(Objects.requireNonNull(schema,"schema"),Objects.requireNonNull(resolver,"resolver")); }
    public static ValidationResult validateEnvironment(EnvSchema schema){ return validate(schema,ValueResolver.systemEnvironment()); }
    public static ValidationResult validateEnvironmentOrThrow(EnvSchema schema){ ValidationResult r=validateEnvironment(schema); if(!r.valid()) throw new EnvCheckException(r); return r; }
    public static ValidationResult validateOrThrow(EnvSchema schema, ValueResolver resolver){ ValidationResult r=validate(schema,resolver); if(!r.valid()) throw new EnvCheckException(r); return r; }
}
