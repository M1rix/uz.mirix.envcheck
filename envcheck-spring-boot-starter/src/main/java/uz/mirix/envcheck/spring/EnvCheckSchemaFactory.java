package uz.mirix.envcheck.spring;

import uz.mirix.envcheck.EnvRule;
import uz.mirix.envcheck.EnvSchema;
import uz.mirix.envcheck.SecretDetector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class EnvCheckSchemaFactory {
    EnvSchema create(EnvCheckProperties properties, String[] activeProfiles) {
        Set<String> profiles = new HashSet<>(Arrays.asList(activeProfiles));
        EnvSchema.Builder schema = EnvSchema.builder();

        for (EnvCheckProperties.Variable config : properties.getVariables()) {
            if (!appliesToProfile(config, profiles)) {
                continue;
            }

            String name = requireName(config.getName());
            EnvRule.Builder rule = EnvRule.builder(name)
                    .required(config.isRequired())
                    .allowBlank(config.isAllowBlank())
                    .type(config.getType())
                    .sensitive(config.getSensitive() != null ? config.getSensitive() : SecretDetector.isSensitive(name));

            if (config.getProperty() != null && !config.getProperty().isBlank()) rule.property(config.getProperty());
            if (config.getMinLength() != null) rule.minLength(config.getMinLength());
            if (config.getMaxLength() != null) rule.maxLength(config.getMaxLength());
            if (config.getPattern() != null && !config.getPattern().isBlank()) rule.pattern(config.getPattern());
            if (config.getAllowedValues() != null && !config.getAllowedValues().isEmpty()) rule.allowedValues(config.getAllowedValues());
            if (config.getMin() != null) rule.min(config.getMin());
            if (config.getMax() != null) rule.max(config.getMax());
            schema.rule(rule.build());
        }

        return schema.build();
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Every envcheck.variables entry requires a non-blank name");
        }
        return name;
    }

    private boolean appliesToProfile(EnvCheckProperties.Variable variable, Set<String> activeProfiles) {
        return variable.getProfiles() == null
                || variable.getProfiles().isEmpty()
                || variable.getProfiles().stream().anyMatch(activeProfiles::contains);
    }
}
