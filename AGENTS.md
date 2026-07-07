# AGENTS.md

## Purpose

This repository is a small Spring showcase, not a production service. Keep changes
targeted, preserve the demonstration focus, and avoid broad refactors unless they
are required for the requested task.

## Stack

- Spring Boot 4.1.0
- Spring Framework 7 sample code
- Java 26 toolchain (preview features enabled - see `spotlight` package's use
  of structured concurrency)
- Gradle with Kotlin DSL
- JPA / Hibernate with H2

## Project Layout

- `src/main/java/com/example/showcase`
  - `catalog`: sample JPA entity, repository, DTOs, controller, data seeding
  - `order`: sample controller and service for resilience features
  - `client`: HTTP interface client example
  - `config`: MVC and HTTP client configuration
- `src/main/resources/application.yml`: application configuration
- `src/test/java/com/example/showcase`: Spring Boot test coverage

## Working Rules

- Treat this as a feature showcase. Each class exists to demonstrate a specific
  Spring 7 / Boot 4.1 capability.
- Prefer small, explicit implementations over abstraction-heavy designs.
- Follow existing package boundaries and naming patterns.
- Preserve package-level `package-info.java` nullness configuration when adding
  new packages.
- Keep comments brief and only where they clarify framework-specific behavior.

## Build And Test

- Preferred commands:
  - `gradle build`
  - `gradle test`
  - `gradle bootRun`
- There is currently no Gradle wrapper in the repo.

## Build And Run Record

These are the steps already used successfully in this workspace.

1. Install Gradle locally if it is missing. This workspace was built with
   Gradle `9.6.1`.
2. Run `gradle build`.
3. If `WebConfig` fails to compile against Spring `7.0.8`, use
   `HandlerTypePredicate.forAssignableType(BookController.class)` instead of
   `forControllerTypes(...)`.
4. Start the application with `gradle bootRun`.
5. Verify startup on `http://localhost:8080`.
6. Smoke test:
   - `http://localhost:8080/api/v1/books`
   - `http://localhost:8080/api/v2/books`
   - `http://localhost:8080/orders/external-lookup/1`
   - `http://localhost:8080/h2-console`
7. Stop the background process on Windows with `Stop-Process -Id <pid>`.

## Known Pitfalls

- **API versioning is applied to every request, not just `BookController`.**
  `WebMvcConfigurer.configureApiVersioning` wires a single, application-wide
  `ApiVersionStrategy` - it is not scoped by the `HandlerTypePredicate` used
  for the path prefix. Configuring it with `usePathSegment(1)` and
  `setVersionRequired(true)` breaks every non-`/api/...` endpoint (they get
  400s because their first path segment isn't a valid version). Scope version
  *resolution* with the `usePathSegment(int, Predicate<RequestPath>)`
  overload (e.g. only treat paths starting with `/api/` as versioned) and set
  `setVersionRequired(false)`; `BookController` still effectively requires a
  version because `v{version}` is baked into its path prefix. See
  `WebConfig`.
- **H2 console needs an explicit dependency in Boot 4.1.** Autoconfiguration
  is modularized, and `H2ConsoleAutoConfiguration` is no longer pulled in by
  `spring-boot-starter-data-jpa`. Add
  `runtimeOnly("org.springframework.boot:spring-boot-h2console")` or
  `/h2-console` 404s even with `spring.h2.console.enabled: true` set.

## Change Guidance

- For API changes, keep the versioned controller behavior intact unless the task
  explicitly changes the public contract.
- For resilience examples, keep annotation usage aligned with current Spring
  resilience APIs in this repo.
- For persistence changes, prefer minimal schema/entity adjustments that do not
  obscure the example.
- Add or update tests when behavior changes.

## Before Finishing

- Summarize the functional change and list any commands you could not run.
- Call out assumptions when a change depends on Spring 7 / Boot 4.1 APIs that
  have not been re-verified after the latest edit.
