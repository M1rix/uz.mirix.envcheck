package uz.mirix.envcheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class EnvSchema {
    private final List<EnvRule> rules;

    private EnvSchema(List<EnvRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<EnvRule> rules() {
        return rules;
    }

    public static final class Builder {
        private final List<EnvRule> rules = new ArrayList<>();
        private final Set<String> names = new HashSet<>();

        public Builder rule(EnvRule rule) {
            EnvRule value = Objects.requireNonNull(rule, "rule");
            if (!names.add(value.name())) {
                throw new IllegalArgumentException("Duplicate EnvCheck rule name: " + value.name());
            }
            rules.add(value);
            return this;
        }

        public Builder rules(Collection<EnvRule> rules) {
            Objects.requireNonNull(rules, "rules").forEach(this::rule);
            return this;
        }

        public EnvSchema build() {
            return new EnvSchema(rules);
        }
    }
}
