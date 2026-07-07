# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

This repository is a small Spring Boot 4.1 / Spring Framework 7.0.8 showcase, not a
production service or a template. Each class exists to demonstrate one specific new
framework feature in isolation. Keep changes targeted, preserve the demonstration
focus, and avoid broad refactors unless required for the requested task.

## Stack

- Spring Boot 4.1.0 / Spring Framework 7.0.8
- Java 26 toolchain with preview features enabled (required for structured
  concurrency / `ScopedValue` in the `spotlight` package)
- `spring.threads.virtual.enabled: true` (`application.yml`) - Tomcat's request
  threads and `@Async` both run on virtual threads app-wide
- Gradle with Kotlin DSL (`build.gradle.kts`) - **no Gradle wrapper is committed**,
  so a local Gradle install (this workspace uses `9.6.1`) is required
- Spring Data JPA / Hibernate ORM 7 with H2 (in-memory)

## Build, Test, Run

```bash
gradle build
gradle test
gradle bootRun
```

- Running the built jar directly (rather than via `gradle bootRun`) requires the
  `--enable-preview` JVM flag or `BookSpotlightService` fails to load.
- Smoke test once running (port `8080`):
  ```bash
  curl localhost:8080/api/v1/books
  curl localhost:8080/api/v2/books
  curl "localhost:8080/api/v2/books/batches?size=2"
  curl localhost:8080/orders/external-lookup/1
  curl localhost:8080/java26/books/1/spotlight
  curl localhost:8080/runtime/thread-info
  ```
- H2 console: `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:mem:showcase`.
- On Windows, stop a background run with `Stop-Process -Id <pid>`.

## Architecture

Source lives under `src/main/java/com/example/showcase`, split into feature packages
that each demonstrate one capability:

| Package | Demonstrates | Key files |
|---|---|---|
| `catalog` | JPA 3.2 / Hibernate 7 entity + API versioning + Java 24 Stream Gatherers (`GET /books/batches`) | `Book.java`, `BookController.java`, `BookDtos.java`, `BookRepository.java`, `BookDataSeeder.java` |
| `client` | HTTP Interface Client (`@ImportHttpServices` + `@GetExchange`) | `CatalogLookupClient.java`, `CatalogStubController.java` (in-app stub so the demo works offline) |
| `config` | MVC + HTTP client wiring | `WebConfig.java` (API versioning), `HttpServiceClientConfig.java` |
| `order` | Core resilience annotations (`@Retryable`, `@ConcurrencyLimit`) | `OrderService.java`, `OrderController.java` |
| `spotlight` | Java 26 structured concurrency + `ScopedValue` + `Joiner.onTimeout()` (JEP 525), composing local JPA data and an upstream HTTP call into one response | `BookSpotlightService.java`, `BookSpotlightController.java` |
| `runtime` | Virtual threads app-wide (`spring.threads.virtual.enabled`) | `ThreadInfoController.java` - reports whether the request-handling thread is virtual |

Application configuration is in `src/main/resources/application.yml`; tests live in
`src/test/java/com/example/showcase`, mirroring the main package layout.

Cross-cutting notes:

- **API versioning is applied to every request, not just `BookController`.**
  `WebConfig` (`WebMvcConfigurer.configureApiVersioning`) wires a single,
  application-wide `ApiVersionStrategy` - it is not scoped by the
  `HandlerTypePredicate` used for the path prefix. Version *resolution* is scoped
  with `usePathSegment(int, Predicate<RequestPath>)` (only `/api/...` paths) with
  `setVersionRequired(false)`; `BookController` still effectively requires a
  version because `v{version}` is baked into its path prefix (`/api/v1/books` vs
  `/api/v2/books`, same controller, different `@GetMapping(version=...)` methods
  and DTOs).
- **`@EnableResilientMethods`** (on `ShowcaseApplication`) is required to activate
  both `@Retryable` and `@ConcurrencyLimit` in `OrderService`.
- **`@Retryable` here is `org.springframework.resilience.annotation.Retryable`**
  (Spring Core's built-in resilience support), not the old
  `org.springframework.retry.annotation.Retryable` (Spring Retry project). The
  attribute is `maxRetries`, not `maxAttempts` - `maxRetries = 4` means up to 5
  total invocations. Don't mix imports between the two; they look almost
  identical but use different attribute names for the same concepts.
- **JSpecify null safety** is per-package: `@NullMarked` in each package's
  `package-info.java` is not inherited by sub-packages, so every package needs
  its own `package-info.java`. `Book.isbn` is `@Nullable` as the example.
- **`BookSpotlightService`** composes a local JPA lookup and a call to
  `OrderService.lookupExternalCatalogEntry` concurrently via
  `StructuredTaskScope` (virtual threads), carrying request-local context through
  a `ScopedValue` instead of a `ThreadLocal`.
- **H2 console needs an explicit dependency in Boot 4.1.** Autoconfiguration is
  modularized, so `spring-boot-starter-data-jpa` no longer pulls in
  `H2ConsoleAutoConfiguration`; it's declared via
  `runtimeOnly("org.springframework.boot:spring-boot-h2console")` in
  `build.gradle.kts`. Without it `/h2-console` 404s even with
  `spring.h2.console.enabled: true` set.
- **`HttpServiceClientConfig`'s self-referencing stub base URL breaks under
  `@SpringBootTest(webEnvironment = RANDOM_PORT)`.** It resolves
  `${server.port:8080}` at bean-creation time; under a random port that property
  is still `0` in the `Environment` (Boot only exposes the real bound port
  afterwards, via `local.server.port` / `@LocalServerPort`, not by rewriting
  `server.port`). All three `RestTestClient` integration test classes therefore
  use `webEnvironment = DEFINED_PORT` (fixed port 8080, consistent with how the
  rest of the repo assumes port 8080) instead of `RANDOM_PORT`.
- **`BookSpotlightService`'s `SpotlightJoiner`** reimplements
  `Joiner.awaitAllSuccessfulOrThrow()`'s cancel-on-first-failure semantics from
  scratch (that built-in joiner is a `final`, package-private class - it can't be
  extended) purely to add Java 26's `Joiner.onTimeout()` hook (JEP 525, default
  method, default body throws the unchecked `StructuredTaskScope.TimeoutException`).
  Overriding it turns a spotlight-assembly timeout into an accurate 504 Gateway
  Timeout instead of falling through to the generic 502 Bad Gateway that
  `adaptFailure` produces for unrecognized failures.

## Working Rules

- Prefer small, explicit implementations over abstraction-heavy designs.
- Follow existing package boundaries and naming patterns.
- Preserve package-level `package-info.java` nullness configuration when adding
  new packages.
- Keep comments brief and only where they clarify framework-specific behavior.
- For API changes, keep the versioned controller behavior intact unless the task
  explicitly changes the public contract.
- For resilience examples, keep annotation usage aligned with the current Spring
  resilience APIs used in this repo (see the `@Retryable` note above).
- For persistence changes, prefer minimal schema/entity adjustments that don't
  obscure the example.
- Add or update tests when behavior changes.
- Before finishing: summarize the functional change, list any commands you could
  not run, and call out assumptions when a change depends on Spring 7 / Boot 4.1
  APIs that haven't been re-verified after the latest edit.
