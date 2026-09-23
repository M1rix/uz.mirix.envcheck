package uz.mirix.envcheck.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;
import uz.mirix.envcheck.EnvCheckException;
import uz.mirix.envcheck.ViolationCode;

import static org.junit.jupiter.api.Assertions.*;

class EnvCheckEnvironmentPostProcessorTest {
    private final EnvCheckEnvironmentPostProcessor processor = new EnvCheckEnvironmentPostProcessor();

    @Test
    void abortsStartupWhenRequiredValueIsMissing() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("envcheck.variables.DB_URL.type", "URL");

        EnvCheckException error = assertThrows(EnvCheckException.class,
                () -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));

        assertEquals(ViolationCode.MISSING, error.result().violations().get(0).code());
    }

    @Test
    void validatesSpringPropertyThroughDisplayAlias() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("envcheck.variables.DB_URL.property", "spring.datasource.url")
                .withProperty("envcheck.variables.DB_URL.type", "URL")
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/app");

        assertDoesNotThrow(() -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));
    }

    @Test
    void skipsProfileSpecificRuleWhenProfileIsInactive() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("envcheck.variables.PAYMENT_API_URL.type", "URL")
                .withProperty("envcheck.variables.PAYMENT_API_URL.profiles[0]", "prod");
        environment.setActiveProfiles("dev");

        assertDoesNotThrow(() -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));
    }

    @Test
    void warnModeDoesNotAbortStartup() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("envcheck.fail-fast", "false")
                .withProperty("envcheck.variables.JWT_SECRET.min-length", "32")
                .withProperty("JWT_SECRET", "short");

        assertDoesNotThrow(() -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));
    }
}
