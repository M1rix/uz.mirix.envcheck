package uz.mirix.envcheck;

import java.util.Optional;

@FunctionalInterface
public interface CustomValidator {
    Optional<String> validate(String value);
}
