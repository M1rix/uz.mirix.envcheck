package uz.mirix.envcheck;

import java.util.Objects;

@SuppressWarnings("serial")
public class EnvCheckException extends RuntimeException {
    private final ValidationResult result;
    public EnvCheckException(ValidationResult result){ super(new HumanReadableReportFormatter().format(result)); this.result=Objects.requireNonNull(result,"result"); }
    public ValidationResult result(){return result;}
}
