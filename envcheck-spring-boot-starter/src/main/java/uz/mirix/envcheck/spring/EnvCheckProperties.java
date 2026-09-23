package uz.mirix.envcheck.spring;

import uz.mirix.envcheck.EnvType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EnvCheckProperties {
    private boolean enabled = true;
    private boolean failFast = true;
    private List<Variable> variables = new ArrayList<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isFailFast() { return failFast; }
    public void setFailFast(boolean failFast) { this.failFast = failFast; }
    public List<Variable> getVariables() { return variables; }
    public void setVariables(List<Variable> variables) {
        this.variables = variables == null ? new ArrayList<>() : new ArrayList<>(variables);
    }

    public static class Variable {
        private String name;
        private String property;
        private boolean required = true;
        private boolean allowBlank;
        private Boolean sensitive;
        private EnvType type = EnvType.STRING;
        private Integer minLength;
        private Integer maxLength;
        private String pattern;
        private List<String> allowedValues = new ArrayList<>();
        private BigDecimal min;
        private BigDecimal max;
        private List<String> profiles = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getProperty() { return property; }
        public void setProperty(String property) { this.property = property; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public boolean isAllowBlank() { return allowBlank; }
        public void setAllowBlank(boolean allowBlank) { this.allowBlank = allowBlank; }
        public Boolean getSensitive() { return sensitive; }
        public void setSensitive(Boolean sensitive) { this.sensitive = sensitive; }
        public EnvType getType() { return type; }
        public void setType(EnvType type) { this.type = type; }
        public Integer getMinLength() { return minLength; }
        public void setMinLength(Integer minLength) { this.minLength = minLength; }
        public Integer getMaxLength() { return maxLength; }
        public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        public List<String> getAllowedValues() { return allowedValues; }
        public void setAllowedValues(List<String> allowedValues) { this.allowedValues = allowedValues == null ? new ArrayList<>() : new ArrayList<>(allowedValues); }
        public BigDecimal getMin() { return min; }
        public void setMin(BigDecimal min) { this.min = min; }
        public BigDecimal getMax() { return max; }
        public void setMax(BigDecimal max) { this.max = max; }
        public List<String> getProfiles() { return profiles; }
        public void setProfiles(List<String> profiles) { this.profiles = profiles == null ? new ArrayList<>() : new ArrayList<>(profiles); }
    }
}
