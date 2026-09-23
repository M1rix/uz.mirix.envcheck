package uz.mirix.envcheck;

import java.util.*;

public final class EnvSchema {
    private final List<EnvRule> rules;
    private EnvSchema(List<EnvRule> rules){this.rules=List.copyOf(rules);} public static Builder builder(){return new Builder();} public List<EnvRule> rules(){return rules;}
    public static final class Builder { private final List<EnvRule> rules=new ArrayList<>(); public Builder rule(EnvRule rule){rules.add(Objects.requireNonNull(rule));return this;} public Builder rules(Collection<EnvRule> rules){rules.forEach(this::rule);return this;} public EnvSchema build(){return new EnvSchema(rules);} }
}
