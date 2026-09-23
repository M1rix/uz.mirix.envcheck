# Contributing

1. Keep `envcheck-core` framework-independent.
2. Every validator change requires tests for valid, invalid and secret/redaction behavior where applicable.
3. Do not add infrastructure requirements to the core library.
4. Run `mvn -B -ntp verify` before opening a pull request.
5. Keep public API additions small; EnvCheck is intentionally a focused library, not a configuration framework.
