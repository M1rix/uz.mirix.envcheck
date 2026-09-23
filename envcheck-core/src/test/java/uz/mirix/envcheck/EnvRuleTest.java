package uz.mirix.envcheck;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvRuleTest {
    @Test
    void rejectsInvalidConstraintRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> EnvRule.builder("VALUE").minLength(10).maxLength(2).build());
        assertThrows(IllegalArgumentException.class,
                () -> EnvRule.builder("VALUE").min(10).max(2).build());
    }
}
