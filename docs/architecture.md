# EnvCheck architecture

EnvCheck keeps one hard boundary: **the validation engine does not know that Spring exists**.

## Modules

- `envcheck-core` — schema, rules, value resolution, type/constraint validation, structured results and reporting.
- `envcheck-spring-boot-starter` — binds `envcheck.*`, resolves values from Spring's `Environment`, executes validation during environment post-processing and exposes a Spring Boot failure analysis.

## Startup lifecycle

```text
ConfigData loaded
      ↓
Spring Environment assembled
      ↓
EnvCheckEnvironmentPostProcessor
      ↓
Bind envcheck schema
      ↓
Resolve configured values
      ↓
EnvCheck core validation
      ↓
valid ───────────────→ continue Spring startup
invalid + fail-fast → EnvCheckException → FailureAnalyzer → abort
invalid + warn      → print report → continue
```

The post-processor intentionally runs at `LOWEST_PRECEDENCE`: all normal Spring Boot config data should be present before validation, while validation still happens before bean creation.

## Security invariant

Values classified as sensitive never enter a human-readable report. Sensitivity can be explicit or inferred from common secret-like names.

## Extension points

Core callers can supply any `ValueResolver` including environment variables, maps, system properties, Vault adapters or test fixtures. Rules can attach `CustomValidator` functions without modifying the validation engine.
