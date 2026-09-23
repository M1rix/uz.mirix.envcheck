package uz.mirix.envcheck.spring;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import uz.mirix.envcheck.EnvCheck;
import uz.mirix.envcheck.EnvCheckException;
import uz.mirix.envcheck.EnvSchema;
import uz.mirix.envcheck.HumanReadableReportFormatter;
import uz.mirix.envcheck.ValidationResult;

import java.util.Optional;

public final class EnvCheckEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final String PREFIX = "envcheck";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        EnvCheckProperties properties = Binder.get(environment)
                .bind(PREFIX, Bindable.of(EnvCheckProperties.class))
                .orElseGet(EnvCheckProperties::new);

        if (!properties.isEnabled() || properties.getVariables().isEmpty()) {
            return;
        }

        EnvSchema schema = new EnvCheckSchemaFactory().create(properties, environment.getActiveProfiles());
        ValidationResult result = EnvCheck.validate(schema,
                key -> Optional.ofNullable(environment.getProperty(key)));

        if (result.valid()) {
            return;
        }

        if (properties.isFailFast()) {
            throw new EnvCheckException(result);
        }

        System.err.println(new HumanReadableReportFormatter().format(result));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
