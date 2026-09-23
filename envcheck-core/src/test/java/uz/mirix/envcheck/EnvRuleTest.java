package uz.mirix.envcheck;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvRuleTest {
    @Test
    void rejectsInvalidConstraintRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> EnvRule.builder("VALUE").minLength(10).maxLength(2).build());
        assertThrows(IllegalArgumentException.class,
                () -> EnvRule.builder("VALUE").min(10).max(2).build());
    }

    @Test
    void rejectsDuplicateRuleNames() {
        EnvRule first = EnvRule.builder("DB_URL").build();
        EnvRule duplicate = EnvRule.builder("DB_URL").property("database.url").build();

        assertThrows(IllegalArgumentException.class,
                () -> EnvSchema.builder().rule(first).rule(duplicate));
    }

    @Test
    void preservesAllowedValueOrderForDeterministicReports() {
        EnvRule rule = EnvRule.builder("APP_ENV")
                .allowedValues("dev", "staging", "prod")
                .build();

        assertEquals(List.of("dev", "staging", "prod"), List.copyOf(rule.allowedValues()));
    }
}
