# spring7-showcase

Minimal Spring Boot 4.1 / Spring Framework 7.0.8 sample, Java 26 (with preview
features enabled), Gradle (Kotlin DSL). Not a template for a real service - each
piece exists to demonstrate one specific new feature in isolation.

This repository has been build-verified locally in this workspace with Gradle `9.6.1`
and Java `26.0.1`. Running the built jar directly (rather than via `gradle bootRun`)
requires the `--enable-preview` JVM flag or `BookSpotlightService` fails to load.

## What's in here

| Feature | Where | Notes |
|---|---|---|
| JPA 3.2 / Hibernate 7 entity | `catalog/Book.java` | Standard entity; Hibernate ORM 7.1/7.2 is now the JPA provider under Framework 7, native Hibernate support moved to `org.springframework.orm.jpa.hibernate` |
| JSpecify null safety | `package-info.java` in each package, `@Nullable` on `Book.isbn` | `@NullMarked` is per-package, not inherited by sub-packages - each package needs its own `package-info.java` |
| API Versioning (path-segment) | `config/WebConfig.java`, `catalog/BookController.java` | `GET /api/v1/books` vs `GET /api/v2/books`, same controller, different `@GetMapping(version=...)` methods, different response DTOs |
| HTTP Interface Client | `client/CatalogLookupClient.java`, `config/HttpServiceClientConfig.java` | `@ImportHttpServices` + `@GetExchange` - no manual `HttpServiceProxyFactory` wiring. Defaults to an in-app stub (`client/CatalogStubController.java`) so the demo runs without internet access; swap `catalog.lookup.base-url` to point at a real upstream service |
| Core resilience: `@Retryable` | `order/OrderService.java` | Note: the attribute is `maxRetries`, not `maxAttempts` (that's the old Spring Retry project's annotation) - `maxRetries = 4` means up to 5 total invocations |
| Core resilience: `@ConcurrencyLimit` | `order/OrderService.java` | Caps concurrent callers into the method; matters more with virtual threads since there's no implicit thread-pool ceiling |
| `@EnableResilientMethods` | `ShowcaseApplication.java` | Required to activate both annotations above |

## Running it

No Gradle wrapper is bundled, so use a local Gradle installation or generate the
wrapper yourself.

```bash
gradle build
gradle bootRun
```

## Local build and deploy steps used here

These are the exact steps already used successfully in this workspace and can be
repeated later.

1. Install Gradle locally if it is missing. This workspace was built with
   Gradle `9.6.1`.
2. Run `gradle build`.
3. For Spring `7.0.8`, make sure
   `HandlerTypePredicate.forAssignableType(BookController.class)` is used in
   `WebConfig`; `forControllerTypes(...)` does not compile here.
4. Start the app with `gradle bootRun`.
5. Verify that Tomcat starts on port `8080`.
6. Smoke test the application endpoints:

```bash
curl localhost:8080/api/v1/books
curl localhost:8080/api/v2/books
curl localhost:8080/orders/external-lookup/1
```

7. Open the H2 console at `http://localhost:8080/h2-console`.
   JDBC URL: `jdbc:h2:mem:showcase`
8. Stop the app with `Stop-Process -Id <pid>` if it is running in the background.

## Things to sanity-check against your actual Boot 4.1 BOM

- `spring-boot-starter-parent` / plugin version `4.1.0` - confirm this matches the
  latest patch when you build.
- `org.jspecify:jspecify:1.0.0` - Boot 4 brings JSpecify in transitively for
  Spring's own code; pinning it explicitly here just makes the dependency visible.
- The `@Retryable` attribute set (`maxRetries`, `delay`, `multiplier`, `maxDelay`,
  `jitter`, `includes`) is from `org.springframework.resilience.annotation.Retryable`,
  not `org.springframework.retry.annotation.Retryable` (the old Spring Retry
  project). Don't mix imports between the two - they look almost identical but
  use different attribute names for the same concepts.
