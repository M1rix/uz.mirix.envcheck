# EnvCheck

**Fail fast. Explain why.**

EnvCheck is a lightweight Java library that validates environment variables and application configuration before your application starts serving traffic.

```text
Application configuration invalid

✗ DB_URL          missing
✓ REDIS_HOST      localhost
✗ JWT_SECRET      too short (min 32)
✓ S3_BUCKET       aurum-files
✗ PAYMENT_API_URL malformed URL

Application startup aborted.
```

No agent. No collector. No external service. No runtime dashboard.

## Modules

| Artifact | Purpose |
| --- | --- |
| `envcheck-core` | Framework-independent schema and validation engine. Zero runtime dependencies. |
| `envcheck-spring-boot-starter` | Spring Boot integration that validates after config data is loaded and before beans are created. |

Baseline: **Java 17+**. The Spring starter is compiled against **Spring Boot 4.1.1**.

## Install locally

The first public Maven Central release is not published yet. Install the snapshot locally:

```bash
git clone https://github.com/M1rix/uz.mirix.envcheck.git
cd uz.mirix.envcheck
mvn -B -ntp clean install
```

```xml
<dependency>
    <groupId>uz.mirix</groupId>
    <artifactId>envcheck-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Spring Boot: zero-code setup

```yaml
envcheck:
  variables:
    - name: DB_URL
      type: url

    - name: REDIS_HOST
      type: host
      required: false

    - name: JWT_SECRET
      type: string
      min-length: 32
      sensitive: true

    - name: S3_BUCKET
      pattern: "^[a-z0-9.-]{3,63}$"

    - name: PAYMENT_API_URL
      type: url
      profiles: [prod, staging]
```

Every declared variable is required by default. EnvCheck resolves values through Spring's `Environment`, so normal Spring precedence still applies. Rules use an explicit `name` rather than a map key so names such as `JWT_SECRET` are not changed by Spring relaxed map binding.

### Validate a Spring property

```yaml
envcheck:
  variables:
    - name: DATABASE_URL
      property: spring.datasource.url
      type: uri
```

The report uses the stable display name `DATABASE_URL`, while the value is resolved from `spring.datasource.url`.

### Warn mode

Fail-fast is the default. During migration you can temporarily keep startup alive:

```yaml
envcheck:
  fail-fast: false
```

Disable EnvCheck completely when needed:

```yaml
envcheck:
  enabled: false
```

## Types

`string`, `integer`, `long`, `boolean`, `url`, `uri`, `host`, `port`, `duration`, `size`.

Examples:

- URL: `https://api.example.com`
- URI: `jdbc:postgresql://localhost:5432/app`, `https://api.example.com`
- duration: `250ms`, `5s`, `10m`, `2h`, `1d`, `PT30S`
- size: `500B`, `10KB`, `64MiB`, `2GB`
- port: `1..65535`

## Constraints

```yaml
envcheck:
  variables:
    - name: JWT_SECRET
      min-length: 32
      max-length: 256

    - name: WORKER_COUNT
      type: integer
      min: 1
      max: 64

    - name: APP_ENV
      allowed-values: [dev, staging, prod]

    - name: TENANT_ID
      pattern: '^t_[a-z0-9]+$'
```

Supported: `name`, `property`, `required`, `allow-blank`, `type`, `min-length`, `max-length`, `pattern`, `allowed-values`, `min`, `max`, `profiles`, `sensitive`.

## Secret safety

Sensitive values are never put into the human-readable report. The Spring starter also infers sensitivity for names containing common secret markers such as `PASSWORD`, `SECRET`, `TOKEN`, `API_KEY`, `PRIVATE_KEY`, `CREDENTIAL`, `AUTH` and `COOKIE`.

If an unusual variable contains a secret, mark it explicitly:

```yaml
envcheck:
  variables:
    - name: SIGNING_MATERIAL
      sensitive: true
      min-length: 32
```

## Framework-independent API

```java
EnvSchema schema = EnvSchema.builder()
    .rule(EnvRule.builder("DB_URL")
        .type(EnvType.URL)
        .build())
    .rule(EnvRule.builder("JWT_SECRET")
        .minLength(32)
        .sensitive(true)
        .build())
    .rule(EnvRule.builder("WORKERS")
        .type(EnvType.INTEGER)
        .min(1)
        .max(64)
        .build())
    .build();

EnvCheck.validateEnvironmentOrThrow(schema);
```

Validate any source:

```java
ValidationResult result = EnvCheck.validate(schema, ValueResolver.fromMap(config));
```

Compose sources:

```java
ValueResolver resolver = ValueResolver.composite(
    ValueResolver.systemProperties(),
    ValueResolver.systemEnvironment()
);
```

Custom validation:

```java
EnvRule tenant = EnvRule.builder("TENANT")
    .validateWith(value -> value.startsWith("t_")
        ? Optional.empty()
        : Optional.of("must start with t_"))
    .build();
```

## Design guarantees

- zero runtime dependencies in `envcheck-core`;
- all rules are evaluated in one pass, so one restart shows the full broken-config list;
- sensitive values never enter the human-readable report;
- Spring validation runs after ConfigData is available and before bean creation;
- no external infrastructure;
- removing EnvCheck does not change application architecture.

See [`docs/architecture.md`](docs/architecture.md).

## Verify

```bash
mvn -B -ntp verify
```

CI runs the reactor on Java 17, 21 and 25.

## License

Apache License 2.0.
