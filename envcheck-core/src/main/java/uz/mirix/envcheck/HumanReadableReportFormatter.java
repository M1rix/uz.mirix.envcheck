package uz.mirix.envcheck;

import java.util.Comparator;

public final class HumanReadableReportFormatter {
    public String format(ValidationResult result){ StringBuilder out=new StringBuilder(); out.append(result.valid()?"Application configuration valid":"Application configuration invalid").append(System.lineSeparator()).append(System.lineSeparator()); int width=result.checks().stream().map(EnvCheckResult::name).mapToInt(String::length).max().orElse(0); for(EnvCheckResult check:result.checks()){ if(check.valid()){String marker=check.present()?"✓":"○";String value=check.present()?check.displayValue():"not set (optional)";out.append(String.format("%s %-"+width+"s  %s%n",marker,check.name(),value));} else {String message=check.violations().stream().sorted(Comparator.comparing(v->v.code().ordinal())).map(EnvViolation::message).findFirst().orElse("invalid");out.append(String.format("✗ %-"+width+"s  %s%n",check.name(),message));}} if(!result.valid()) out.append(System.lineSeparator()).append("Application startup aborted."); return out.toString().stripTrailing(); }
}
