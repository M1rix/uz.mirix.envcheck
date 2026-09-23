package uz.mirix.envcheck;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvCheckTest {
    @Test
    void validatesTypedConfigurationAndConstraints() {
        EnvSchema schema = EnvSchema.builder()
                .rule(EnvRule.builder("DB_URL").type(EnvType.URL).build())
                .rule(EnvRule.builder("PORT").type(EnvType.PORT).min(1024).max(9000).build())
                .rule(EnvRule.builder("TIMEOUT").type(EnvType.DURATION).build())
                .rule(EnvRule.builder("MAX_UPLOAD").type(EnvType.SIZE).build())
                .rule(EnvRule.builder("MODE").allowedValues("dev", "prod").build())
                .build();

        ValidationResult result = EnvCheck.validate(schema, ValueResolver.fromMap(Map.of(
                "DB_URL", "https://db.internal.example",
                "PORT", "8080",
                "TIMEOUT", "5s",
                "MAX_UPLOAD", "10MiB",
                "MODE", "prod")));

        assertTrue(result.valid());
        assertEquals(5, result.validCount());
    }

    @Test
    void returnsAllViolationsInsteadOfStoppingAtFirstOne() {
        EnvSchema schema = EnvSchema.builder()
                .rule(EnvRule.builder("DB_URL").type(EnvType.URL).build())
                .rule(EnvRule.builder("JWT_SECRET").minLength(32).sensitive(true).build())
                .rule(EnvRule.builder("PORT").type(EnvType.PORT).build())
                .build();

        ValidationResult result = EnvCheck.validate(schema, ValueResolver.fromMap(Map.of(
                "JWT_SECRET", "short",
                "PORT", "70000")));

        assertFalse(result.valid());
        assertEquals(3, result.invalidCount());
        assertTrue(result.violations().stream().anyMatch(v -> v.code() == ViolationCode.MISSING));
        assertTrue(result.violations().stream().anyMatch(v -> v.code() == ViolationCode.TOO_SHORT));
        assertTrue(result.violations().stream().anyMatch(v -> v.code() == ViolationCode.INVALID_TYPE));
    }

    @Test
    void neverLeaksSensitiveValueIntoFormattedReport() {
        String secret = "super-secret-token-value";
        EnvSchema schema = EnvSchema.builder()
                .rule(EnvRule.builder("API_TOKEN").sensitive(true).minLength(64).build())
                .build();

        ValidationResult result = EnvCheck.validate(schema, ValueResolver.fromMap(Map.of("API_TOKEN", secret)));
        String report = new HumanReadableReportFormatter().format(result);

        assertFalse(report.contains(secret));
        assertEquals("[REDACTED]", result.checks().get(0).displayValue());
    }

    @Test
    void sensitiveRulesDoNotExposeAllowedValuesOrCustomMessages() {
        String secret = "current-secret";
        EnvSchema schema = EnvSchema.builder()
                .rule(EnvRule.builder("API_TOKEN")
                        .sensitive(true)
                        .allowedValues("expected-secret")
                        .validateWith(value -> Optional.of("invalid token: " + value))
                        .build())
                .build();

        ValidationResult result = EnvCheck.validate(schema, ValueResolver.fromMap(Map.of("API_TOKEN", secret)));
        String report = new HumanReadableReportFormatter().format(result);

        assertFalse(report.contains(secret));
        assertFalse(report.contains("expected-secret"));
        assertFalse(report.contains("invalid token"));
        assertTrue(report.contains("value is not allowed"));
    }

    @Test
    void optionalMissingValueIsValid() {
        EnvSchema schema = EnvSchema.builder()
                .rule(EnvRule.builder("OPTIONAL_FEATURE").optional().build())
                .build();

        ValidationResult result = EnvCheck.validate(schema, key -> Optional.empty());

        assertTrue(result.valid());
        assertFalse(result.checks().get(0).present());
    }

    @Test
    void supportsCustomValidators() {
        EnvSchema schema = EnvSchema.builder()
                .rule(EnvRule.builder("TENANT")
                        .validateWith(value -> value.startsWith("t_") ? Optional.empty() : Optional.of("must start with t_"))
                        .build())
                .build();

        ValidationResult result = EnvCheck.validate(schema, ValueResolver.fromMap(Map.of("TENANT", "wrong")));
        assertEquals(ViolationCode.CUSTOM, result.violations().get(0).code());
        assertEquals("must start with t_", result.violations().get(0).message());
    }
}
