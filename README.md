# EnvCheck

[![CI](https://github.com/M1rix/uz.mirix.envcheck/actions/workflows/ci.yml/badge.svg)](https://github.com/M1rix/uz.mirix.envcheck/actions/workflows/ci.yml)
[![](https://jitpack.io/v/M1rix/uz.mirix.envcheck.svg)](https://jitpack.io/#M1rix/uz.mirix.envcheck)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

**Fail fast. Explain why.**

EnvCheck is a lightweight Java library for validating environment variables and application configuration **before your application starts serving traffic**.

Instead of discovering broken configuration through a `NullPointerException`, a failed database connection, or the first production request, EnvCheck aborts startup with a readable report:

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

## Why EnvCheck?

Configuration failures are usually simple problems with expensive symptoms:

- a required environment variable is missing;
- a secret is too short;
- a URL is malformed;
- a port is outside the valid range;
- a production-only integration was not configured;
- a Spring property exists but contains an invalid value.

EnvCheck makes those failures explicit at startup.

```text
one problem
one dependency
near-zero configuration
no external infrastructure
immediate feedback
```

## Requirements

- **Java 17+**
- Spring module: **Spring Boot 4.1.x**

CI verifies the project on Java **17, 21 and 25**.

## Modules

| Module | Purpose |
| --- | --- |
| `envcheck-core` | Framework-independent validation engine with zero runtime dependencies. |
| `envcheck-spring-boot-starter` | Spring Boot integration that validates configuration after ConfigData is loaded and before beans are created. |

Most Spring Boot applications only need `envcheck-spring-boot-starter`; it brings the core module transitively.

---

# Installation

EnvCheck is distributed through **JitPack**.

Release used below: **`v0.1.0`**.

Because EnvCheck is a multi-module repository, JitPack exposes individual modules using the group:

```text
com.github.M1rix.uz.mirix.envcheck
```

## Maven — Spring Boot starter

Add JitPack as a repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add EnvCheck:

```xml
<dependency>
    <groupId>com.github.M1rix.uz.mirix.envcheck</groupId>
    <artifactId>envcheck-spring-boot-starter</artifactId>
    <version>v0.1.0</version>
</dependency>
```

## Gradle Kotlin DSL — Spring Boot starter

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.M1rix.uz.mirix.envcheck:envcheck-spring-boot-starter:v0.1.0")
}
```

## Gradle Groovy DSL — Spring Boot starter

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.M1rix.uz.mirix.envcheck:envcheck-spring-boot-starter:v0.1.0'
}
```

## Core only

Use this when you do not need Spring Boot integration:

### Maven

```xml
<dependency>
    <groupId>com.github.M1rix.uz.mirix.envcheck</groupId>
    <artifactId>envcheck-core</artifactId>
    <version>v0.1.0</version>
</dependency>
```

### Gradle

```kotlin
implementation("com.github.M1rix.uz.mirix.envcheck:envcheck-core:v0.1.0")
```

> JitPack builds a tagged version from source on the first request, so the first dependency resolution for a new release may take longer than subsequent downloads.

---

# Spring Boot quick start

Add the starter dependency. No Java configuration is required.

Define the configuration contract in `application.yml`:

```yaml
envcheck:
  variables:
    - name: DB_URL
      type: uri

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

Every declared variable is **required by default**.

EnvCheck resolves values through Spring's `Environment`, so normal Spring property precedence still applies: environment variables, command-line arguments, system properties, `application.yml`, profile-specific configuration, and other Spring property sources.

Rules use an explicit `name` instead of relying on a map key. This keeps identifiers such as `JWT_SECRET` and `DB_URL` stable even with Spring relaxed binding.

## Validate a Spring property

A rule does not have to read an environment variable with the same name.

```yaml
envcheck:
  variables:
    - name: DATABASE_URL
      property: spring.datasource.url
      type: uri
```

EnvCheck displays `DATABASE_URL` in the report while resolving the value from:

```text
spring.datasource.url
```

This is useful when you want stable operational names without changing your application's Spring property names.

## Profile-specific rules

Validate configuration only when selected profiles are active:

```yaml
envcheck:
  variables:
    - name: PAYMENT_API_URL
      type: url
      profiles: [prod, staging]
```

The rule is ignored when neither `prod` nor `staging` is active.

## Warn mode

Fail-fast behavior is enabled by default.

During migration, you can temporarily print errors without aborting startup:

```yaml
envcheck:
  fail-fast: false
```

## Disable EnvCheck

```yaml
envcheck:
  enabled: false
```

---

# Supported types

| Type | Examples |
| --- | --- |
| `string` | `production`, `tenant-42` |
| `integer` | `4`, `128` |
| `long` | `9223372036854775807` |
| `boolean` | `true`, `false` |
| `url` | `https://api.example.com` |
| `uri` | `jdbc:postgresql://localhost:5432/app` |
| `host` | `localhost`, `redis.internal`, `10.0.0.12` |
| `port` | `8080` |
| `duration` | `250ms`, `5s`, `10m`, `2h`, `1d`, `PT30S` |
| `size` | `500B`, `10KB`, `64MiB`, `2GB` |

Ports are validated against the range `1..65535`.

---

# Constraints

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

Supported rule properties:

| Property | Purpose |
| --- | --- |
| `name` | Stable name shown in EnvCheck reports. |
| `property` | Alternative Spring property key used to resolve the value. |
| `required` | Whether the value must exist. Defaults to `true`. |
| `allow-blank` | Allow an existing but blank value. |
| `type` | Typed validation. |
| `min-length` | Minimum string length. |
| `max-length` | Maximum string length. |
| `pattern` | Regular-expression constraint. |
| `allowed-values` | Allow-list of accepted values. |
| `min` | Minimum numeric value. |
| `max` | Maximum numeric value. |
| `profiles` | Activate the rule only for selected Spring profiles. |
| `sensitive` | Prevent the value and sensitive diagnostics from appearing in reports. |

EnvCheck evaluates all rules in one pass. A broken deployment therefore reports the complete configuration problem set instead of forcing repeated restart/fix/restart cycles.

---

# Secret safety

Sensitive configuration must not end up in logs.

EnvCheck automatically infers sensitivity for names containing common markers such as:

```text
PASSWORD
SECRET
TOKEN
API_KEY
PRIVATE_KEY
CREDENTIAL
AUTH
COOKIE
```

For unusual names, mark the rule explicitly:

```yaml
envcheck:
  variables:
    - name: SIGNING_MATERIAL
      sensitive: true
      min-length: 32
```

Sensitive values are represented as:

```text
[REDACTED]
```

EnvCheck also avoids exposing potentially sensitive allow-list values and custom-validator messages for rules marked as sensitive.

---

# Framework-independent API

`envcheck-core` can be used without Spring.

```java
EnvSchema schema = EnvSchema.builder()
    .rule(EnvRule.builder("DB_URL")
        .type(EnvType.URI)
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

## Validate any source

```java
ValidationResult result = EnvCheck.validate(
    schema,
    ValueResolver.fromMap(config)
);
```

## Compose sources

```java
ValueResolver resolver = ValueResolver.composite(
    ValueResolver.systemProperties(),
    ValueResolver.systemEnvironment()
);

ValidationResult result = EnvCheck.validate(schema, resolver);
```

## Custom validators

```java
EnvRule tenant = EnvRule.builder("TENANT")
    .validateWith(value -> value.startsWith("t_")
        ? Optional.empty()
        : Optional.of("must start with t_"))
    .build();
```

---

# How startup validation works

For Spring Boot applications the flow is intentionally early:

```text
SpringApplication
      │
      ▼
ConfigData loaded
      │
      ▼
EnvCheck EnvironmentPostProcessor
      │
      ├── configuration valid ──► continue startup
      │
      └── configuration invalid ─► abort with EnvCheck report
      │
      ▼
ApplicationContext / beans
```

EnvCheck does not wait for a datasource, HTTP client, cache, message broker, or application bean to fail first.

See [`docs/architecture.md`](docs/architecture.md) for the internal design.

---

# Design guarantees

- **Zero runtime dependencies** in `envcheck-core`.
- **No external infrastructure**.
- **No agent or background process**.
- All configured rules are evaluated in one pass.
- Sensitive values do not enter the human-readable report.
- Spring validation runs after ConfigData is available and before bean creation.
- Duplicate rule names are rejected.
- Rule output is deterministic.
- Removing EnvCheck does not require architectural changes to the application.

---

# Build from source

```bash
git clone https://github.com/M1rix/uz.mirix.envcheck.git
cd uz.mirix.envcheck
mvn -B -ntp verify
```

Install locally:

```bash
mvn -B -ntp clean install
```

---

# Release through JitPack

EnvCheck releases are built directly from Git tags by JitPack.

For `v0.1.0`:

```bash
git tag v0.1.0
git push origin v0.1.0
```

Then open:

```text
https://jitpack.io/#M1rix/uz.mirix.envcheck/v0.1.0
```

JitPack will build and expose both modules for dependency resolution.

---

# License

Licensed under the [Apache License 2.0](LICENSE).
