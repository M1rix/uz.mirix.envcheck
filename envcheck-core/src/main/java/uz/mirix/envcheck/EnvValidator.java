package uz.mirix.envcheck;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

final class EnvValidator {
    private static final Pattern HOSTNAME = Pattern.compile("^(?=.{1,253}$)(?!-)(?:[a-zA-Z0-9-]{1,63}\\.)*[a-zA-Z0-9][a-zA-Z0-9-]{0,62}$");
    private static final Pattern IPV4 = Pattern.compile("^(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}$");
    private static final Pattern SIZE = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*(B|KB|MB|GB|TB|KIB|MIB|GIB|TIB)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SHORT_DURATION = Pattern.compile("^(\\d+)\\s*(MS|S|M|H|D)$", Pattern.CASE_INSENSITIVE);

    ValidationResult validate(EnvSchema schema, ValueResolver resolver) {
        List<EnvCheckResult> checks = new ArrayList<>();
        for (EnvRule rule : schema.rules()) {
            checks.add(validateRule(rule, resolver));
        }
        return new ValidationResult(checks);
    }

    private EnvCheckResult validateRule(EnvRule rule, ValueResolver resolver) {
        Optional<String> resolved = resolver.resolve(rule.propertyKey());
        if (resolved.isEmpty()) {
            List<EnvViolation> violations = rule.required()
                    ? List.of(new EnvViolation(rule.name(), ViolationCode.MISSING, "missing"))
                    : List.of();
            return new EnvCheckResult(rule.name(), rule.propertyKey(), false, rule.sensitive(), "", violations);
        }

        String value = resolved.get();
        List<EnvViolation> violations = new ArrayList<>();
        if (!rule.allowBlank() && value.isBlank()) {
            violations.add(new EnvViolation(rule.name(), ViolationCode.BLANK, "must not be blank"));
        }
        if (!value.isBlank()) {
            validateType(rule, value, violations);
            validateConstraints(rule, value, violations);
        }

        return new EnvCheckResult(
                rule.name(),
                rule.propertyKey(),
                true,
                rule.sensitive(),
                rule.sensitive() ? "[REDACTED]" : value,
                violations);
    }

    private void validateType(EnvRule rule, String value, List<EnvViolation> violations) {
        try {
            switch (rule.type()) {
                case STRING -> { }
                case INTEGER -> Integer.parseInt(value);
                case LONG -> Long.parseLong(value);
                case BOOLEAN -> {
                    if (!("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
                        throw new IllegalArgumentException();
                    }
                }
                case URI -> validateUri(value, false);
                case URL -> validateUri(value, true);
                case HOST -> {
                    if (!(IPV4.matcher(value).matches()
                            || HOSTNAME.matcher(value).matches()
                            || "localhost".equalsIgnoreCase(value))) {
                        throw new IllegalArgumentException();
                    }
                }
                case PORT -> {
                    int port = Integer.parseInt(value);
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException();
                    }
                }
                case DURATION -> parseDuration(value);
                case SIZE -> parseSize(value);
            }
        } catch (RuntimeException ex) {
            violations.add(new EnvViolation(rule.name(), ViolationCode.INVALID_TYPE, typeMessage(rule.type())));
        }
    }

    private void validateConstraints(EnvRule rule, String value, List<EnvViolation> violations) {
        if (rule.minLength() != null && value.length() < rule.minLength()) {
            violations.add(new EnvViolation(rule.name(), ViolationCode.TOO_SHORT, "too short (min " + rule.minLength() + ")"));
        }
        if (rule.maxLength() != null && value.length() > rule.maxLength()) {
            violations.add(new EnvViolation(rule.name(), ViolationCode.TOO_LONG, "too long (max " + rule.maxLength() + ")"));
        }
        if (rule.pattern() != null && !rule.pattern().matcher(value).matches()) {
            violations.add(new EnvViolation(rule.name(), ViolationCode.PATTERN_MISMATCH, "pattern mismatch"));
        }
        if (!rule.allowedValues().isEmpty() && !rule.allowedValues().contains(value)) {
            String message = rule.sensitive() ? "value is not allowed" : "must be one of " + rule.allowedValues();
            violations.add(new EnvViolation(rule.name(), ViolationCode.NOT_ALLOWED, message));
        }
        if (rule.min() != null || rule.max() != null) {
            try {
                BigDecimal numeric = numericValue(rule.type(), value);
                if (rule.min() != null && numeric.compareTo(rule.min()) < 0) {
                    violations.add(new EnvViolation(rule.name(), ViolationCode.TOO_SMALL, "must be >= " + rule.min().toPlainString()));
                }
                if (rule.max() != null && numeric.compareTo(rule.max()) > 0) {
                    violations.add(new EnvViolation(rule.name(), ViolationCode.TOO_LARGE, "must be <= " + rule.max().toPlainString()));
                }
            } catch (RuntimeException ignored) {
                // Type validation reports malformed numeric values. Avoid duplicate diagnostics here.
            }
        }
        for (CustomValidator validator : rule.customValidators()) {
            validator.validate(value).ifPresent(message -> violations.add(new EnvViolation(
                    rule.name(),
                    ViolationCode.CUSTOM,
                    rule.sensitive() ? "custom validation failed" : message)));
        }
    }

    private static BigDecimal numericValue(EnvType type, String value) {
        return switch (type) {
            case INTEGER, LONG, PORT -> new BigDecimal(value);
            case SIZE -> parseSize(value);
            case DURATION -> BigDecimal.valueOf(parseDuration(value).toMillis());
            default -> new BigDecimal(value);
        };
    }

    private static void validateUri(String value, boolean url) {
        URI uri = URI.create(value);
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException();
        }
        if (url && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) && uri.getHost() == null) {
            throw new IllegalArgumentException();
        }
    }

    private static Duration parseDuration(String value) {
        String text = value.trim();
        if (text.regionMatches(true, 0, "P", 0, 1)) {
            return Duration.parse(text.toUpperCase(Locale.ROOT));
        }
        var matcher = SHORT_DURATION.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        long amount = Long.parseLong(matcher.group(1));
        return switch (matcher.group(2).toUpperCase(Locale.ROOT)) {
            case "MS" -> Duration.ofMillis(amount);
            case "S" -> Duration.ofSeconds(amount);
            case "M" -> Duration.ofMinutes(amount);
            case "H" -> Duration.ofHours(amount);
            case "D" -> Duration.ofDays(amount);
            default -> throw new IllegalArgumentException();
        };
    }

    private static BigDecimal parseSize(String value) {
        var matcher = SIZE.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        BigDecimal amount = new BigDecimal(matcher.group(1));
        String unit = matcher.group(2) == null ? "B" : matcher.group(2).toUpperCase(Locale.ROOT);
        BigDecimal multiplier = switch (unit) {
            case "B" -> BigDecimal.ONE;
            case "KB" -> BigDecimal.valueOf(1_000L);
            case "MB" -> BigDecimal.valueOf(1_000_000L);
            case "GB" -> BigDecimal.valueOf(1_000_000_000L);
            case "TB" -> BigDecimal.valueOf(1_000_000_000_000L);
            case "KIB" -> BigDecimal.valueOf(1_024L);
            case "MIB" -> BigDecimal.valueOf(1_048_576L);
            case "GIB" -> BigDecimal.valueOf(1_073_741_824L);
            case "TIB" -> BigDecimal.valueOf(1_099_511_627_776L);
            default -> throw new IllegalArgumentException();
        };
        return amount.multiply(multiplier);
    }

    private static String typeMessage(EnvType type) {
        return switch (type) {
            case URL -> "malformed URL";
            case URI -> "malformed URI";
            case INTEGER -> "must be an integer";
            case LONG -> "must be a long integer";
            case BOOLEAN -> "must be true or false";
            case HOST -> "malformed host";
            case PORT -> "must be a port between 1 and 65535";
            case DURATION -> "malformed duration";
            case SIZE -> "malformed data size";
            case STRING -> "invalid string";
        };
    }
}
