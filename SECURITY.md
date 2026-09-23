# Security policy

Please do not publish potential secret-leak issues with real credentials or tokens. Reproduce with synthetic values only.

The core security invariant is that a rule marked `sensitive` must never expose its raw value through the human-readable formatter or Spring Boot failure analysis. Any regression violating that invariant should be treated as high priority.
