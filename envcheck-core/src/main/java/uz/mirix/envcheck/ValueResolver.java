package uz.mirix.envcheck;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@FunctionalInterface
public interface ValueResolver {
    Optional<String> resolve(String key);
    static ValueResolver fromMap(Map<String, ?> values) {
        Objects.requireNonNull(values, "values");
        return key -> Optional.ofNullable(values.get(key)).map(String::valueOf);
    }
    static ValueResolver systemEnvironment() { return key -> Optional.ofNullable(System.getenv(key)); }
    static ValueResolver systemProperties() { return key -> Optional.ofNullable(System.getProperty(key)); }
    static ValueResolver composite(ValueResolver... resolvers) {
        Objects.requireNonNull(resolvers, "resolvers");
        ValueResolver[] copy = resolvers.clone();
        return key -> {
            for (ValueResolver resolver : copy) {
                if (resolver == null) continue;
                Optional<String> value = resolver.resolve(key);
                if (value.isPresent()) return value;
            }
            return Optional.empty();
        };
    }
}
