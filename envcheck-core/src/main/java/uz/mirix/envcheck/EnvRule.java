package uz.mirix.envcheck;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class EnvRule {
    private final String name;
    private final String propertyKey;
    private final boolean required;
    private final boolean allowBlank;
    private final boolean sensitive;
    private final EnvType type;
    private final Integer minLength;
    private final Integer maxLength;
    private final Pattern pattern;
    private final Set<String> allowedValues;
    private final BigDecimal min;
    private final BigDecimal max;
    private final List<CustomValidator> customValidators;

    private EnvRule(Builder builder) {
        this.name = requireText(builder.name, "name");
        this.propertyKey = builder.propertyKey == null ? name : requireText(builder.propertyKey, "propertyKey");
        this.required = builder.required;
        this.allowBlank = builder.allowBlank;
        this.sensitive = builder.sensitive;
        this.type = Objects.requireNonNull(builder.type, "type");
        this.minLength = nonNegative(builder.minLength, "minLength");
        this.maxLength = nonNegative(builder.maxLength, "maxLength");
        if (minLength != null && maxLength != null && minLength > maxLength) {
            throw new IllegalArgumentException("minLength cannot be greater than maxLength");
        }
        this.pattern = builder.pattern;
        this.allowedValues = Collections.unmodifiableSet(new LinkedHashSet<>(builder.allowedValues));
        this.min = builder.min;
        this.max = builder.max;
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min cannot be greater than max");
        }
        this.customValidators = List.copyOf(builder.customValidators);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static Integer nonNegative(Integer value, String field) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(field + " must be >= 0");
        }
        return value;
    }

    public static Builder builder(String name) { return new Builder(name); }
    public String name() { return name; }
    public String propertyKey() { return propertyKey; }
    public boolean required() { return required; }
    public boolean allowBlank() { return allowBlank; }
    public boolean sensitive() { return sensitive; }
    public EnvType type() { return type; }
    public Integer minLength() { return minLength; }
    public Integer maxLength() { return maxLength; }
    public Pattern pattern() { return pattern; }
    public Set<String> allowedValues() { return allowedValues; }
    public BigDecimal min() { return min; }
    public BigDecimal max() { return max; }
    public List<CustomValidator> customValidators() { return customValidators; }

    public static final class Builder {
        private final String name;
        private String propertyKey;
        private boolean required = true;
        private boolean allowBlank;
        private boolean sensitive;
        private EnvType type = EnvType.STRING;
        private Integer minLength;
        private Integer maxLength;
        private Pattern pattern;
        private final Set<String> allowedValues = new LinkedHashSet<>();
        private BigDecimal min;
        private BigDecimal max;
        private final List<CustomValidator> customValidators = new ArrayList<>();

        private Builder(String name) { this.name = name; }

        public Builder property(String value) { propertyKey = value; return this; }
        public Builder required(boolean value) { required = value; return this; }
        public Builder optional() { required = false; return this; }
        public Builder allowBlank(boolean value) { allowBlank = value; return this; }
        public Builder sensitive(boolean value) { sensitive = value; return this; }
        public Builder type(EnvType value) { type = Objects.requireNonNull(value, "type"); return this; }
        public Builder minLength(int value) { minLength = value; return this; }
        public Builder maxLength(int value) { maxLength = value; return this; }
        public Builder pattern(String value) { pattern = Pattern.compile(Objects.requireNonNull(value, "regex")); return this; }
        public Builder allowedValues(Collection<String> values) { allowedValues.addAll(Objects.requireNonNull(values, "values")); return this; }
        public Builder allowedValues(String... values) { allowedValues.addAll(List.of(Objects.requireNonNull(values, "values"))); return this; }
        public Builder min(Number value) { min = new BigDecimal(Objects.requireNonNull(value, "min").toString()); return this; }
        public Builder max(Number value) { max = new BigDecimal(Objects.requireNonNull(value, "max").toString()); return this; }
        public Builder validateWith(CustomValidator value) { customValidators.add(Objects.requireNonNull(value, "validator")); return this; }
        public EnvRule build() { return new EnvRule(this); }
    }
}
