# AGENTS.md — Spring Boot 4.x (Java 25, Gradle)

## Purpose
This file defines the working rules for coding agents (e.g., Codex) in this repository. Follow it strictly to keep changes safe, reviewable, and consistent.

## Tech baseline
- Java: 25
- Framework: Spring Boot 3.x
- Build: Gradle (use wrapper)
- Tests: JUnit 5

## Ground rules (must follow)
- Keep changes small and scoped to the request. Avoid repo-wide refactors/renames/reformatting.
- Prefer existing patterns, packages, and utilities in this codebase over introducing new abstractions.
- Do not add/remove dependencies unless necessary; explain why and keep it minimal.
- Never modify or commit secrets (tokens, credentials, private keys). Use `.env.example` or docs if needed.
- If requirements are ambiguous, write a short plan (3–6 bullets) before changing code.
- Preserve public APIs and security behavior unless explicitly asked to change them.

## Commands (use Gradle wrapper)
Run from the repository root unless stated otherwise.

### Build & verification
- Build: `./gradlew build`
- Fast checks: `./gradlew test`
- Full checks (if configured): `./gradlew check`

### Running the app
- Run (if Spring Boot plugin configured): `./gradlew bootRun`

### Targeted testing
- Single test class:
  - `./gradlew test --tests com.example.SomeTest`
- Single test method:
  - `./gradlew test --tests com.example.SomeTest.someMethod`

## Project structure (typical)
- Production code: `src/main/java/...`
- Resources: `src/main/resources/...`
- Tests: `src/test/java/...`

## Spring Boot conventions
- Prefer constructor injection.
- Prefer `@ConfigurationProperties` for structured configuration.
- Avoid reading environment variables directly in business logic.
- Keep configuration explicit; avoid magic defaults unless already established in the repo.
- Use SLF4J logging; avoid excessive debug/trace logs and never log secrets.

## Testing conventions (JUnit 5)
- New/changed behavior requires tests.
- Prefer fast unit tests; add integration tests only when necessary.
- Keep tests deterministic:
  - Avoid `Thread.sleep`.
  - Prefer injectable `Clock`, stubs/mocks, or time control utilities.
- For Spring tests:
  - Prefer slice tests (`@WebMvcTest`, etc.) where possible.
  - Use `@SpringBootTest` only when needed.

## Change hygiene
- Keep diffs reviewable: avoid unrelated formatting changes.
- Update docs/config when behavior changes.
- Do not leave commented-out code, debug prints, or temporary endpoints.

## Definition of Done (before finishing)
- [ ] `./gradlew test` passes
- [ ] `./gradlew check` passes (if configured)
- [ ] Security behavior is not weakened unintentionally
- [ ] New/changed logic is covered by tests (including security tests when relevant)
- [ ] No secrets added; no sensitive info logged
- [ ] Changes are scoped, clean, and consistent with repo patterns

