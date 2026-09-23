# Changelog

## 0.1.0-SNAPSHOT

- Framework-independent `envcheck-core` with zero runtime dependencies.
- Spring Boot 4.1.1 early-startup integration via `EnvironmentPostProcessor`.
- Java 17 baseline with CI matrix for Java 17, 21 and 25.
- String, integer, long, boolean, URL, URI, host, port, duration and data-size types.
- Required/optional, blank, length, regex, allow-list and numeric range constraints.
- Custom programmatic validators and pluggable `ValueResolver` sources.
- Profile-scoped Spring rules and Spring-property aliases.
- Binding-safe declarative Spring schema using explicit variable names.
- Aggregate validation so a single startup reports all broken configuration values.
- Automatic common-secret detection plus explicit `sensitive` rules.
- Sensitive diagnostics redact values, allow-lists and custom validator messages.
- Duplicate rule rejection and deterministic allow-list ordering.
- Structured validation results and human-readable Spring Boot failure analysis.
