package uz.mirix.envcheck.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.mock.env.MockEnvironment;
import uz.mirix.envcheck.EnvCheckException;
import uz.mirix.envcheck.ViolationCode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvCheckEnvironmentPostProcessorTest {
    private final EnvCheckEnvironmentPostProcessor processor = new EnvCheckEnvironmentPostProcessor();

    @Test
    void abortsStartupWhenRequiredValueIsMissingAndPreservesDisplayName() {
        MockEnvironment environment = environment()
                .withProperty("envcheck.variables[0].name", "DB_URL")
                .withProperty("envcheck.variables[0].type", "URL");

        EnvCheckException error = assertThrows(EnvCheckException.class,
                () -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));

        assertEquals("DB_URL", error.result().checks().get(0).name());
        assertEquals(ViolationCode.MISSING, error.result().violations().get(0).code());
    }

    @Test
    void validatesSpringPropertyThroughDisplayAlias() {
        MockEnvironment environment = environment()
                .withProperty("envcheck.variables[0].name", "DB_URL")
                .withProperty("envcheck.variables[0].property", "spring.datasource.url")
                .withProperty("envcheck.variables[0].type", "URI")
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/app");

        assertDoesNotThrow(() -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));
    }

    @Test
    void skipsProfileSpecificRuleWhenProfileIsInactive() {
        MockEnvironment environment = environment()
                .withProperty("envcheck.variables[0].name", "PAYMENT_API_URL")
                .withProperty("envcheck.variables[0].type", "URL")
                .withProperty("envcheck.variables[0].profiles[0]", "prod");
        environment.setActiveProfiles("dev");

        assertDoesNotThrow(() -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));
    }

    @Test
    void warnModeDoesNotAbortStartup() {
        MockEnvironment environment = environment()
                .withProperty("envcheck.fail-fast", "false")
                .withProperty("envcheck.variables[0].name", "JWT_SECRET")
                .withProperty("envcheck.variables[0].min-length", "32")
                .withProperty("JWT_SECRET", "short");

        assertDoesNotThrow(() -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));
    }

    @Test
    void rejectsUnnamedRules() {
        MockEnvironment environment = environment()
                .withProperty("envcheck.variables[0].type", "URL");

        assertThrows(IllegalArgumentException.class,
                () -> processor.postProcessEnvironment(environment, new SpringApplication(Object.class)));
    }

    private MockEnvironment environment() {
        MockEnvironment environment = new MockEnvironment();
        ConfigurationPropertySources.attach(environment);
        return environment;
    }
}
