package uz.mirix.envcheck.spring;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import uz.mirix.envcheck.EnvCheckException;
import uz.mirix.envcheck.HumanReadableReportFormatter;

public final class EnvCheckFailureAnalyzer extends AbstractFailureAnalyzer<EnvCheckException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, EnvCheckException cause) {
        String description = new HumanReadableReportFormatter().format(cause.result());
        String action = "Set or correct the listed configuration values, then restart the application. "
                + "If a value is intentionally optional, configure required: false for that EnvCheck rule.";
        return new FailureAnalysis(description, action, cause);
    }
}
