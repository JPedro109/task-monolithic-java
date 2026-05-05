# Project Structure

## Source layout

```
src/main/java/com/jpmns/task/
├── TaskApplication.java              # Spring Boot entry point
├── configuration/                    # Framework config (not covered by JaCoCo)
│   ├── security/SecurityConfig.java  # Spring Security filter chain
│   ├── swagger/SwaggerConfig.java    # OpenAPI / Swagger UI
│   └── tracing/OtelBaggageConfig.java
├── core/
│   ├── domain/                       # Pure business logic — no framework dependencies
│   │   ├── common/
│   │   │   ├── abstracts/Entity.java # Base entity (id + createdAt + validateOrThrow)
│   │   │   ├── exception/DomainException.java
│   │   │   └── valueobject/IdValueObject.java
│   │   ├── task/
│   │   │   ├── TaskEntity.java
│   │   │   └── valueobject/TaskNameValueObject.java
│   │   └── user/
│   │       ├── UserEntity.java
│   │       └── valueobject/  (UserEmailValueObject, UsernameValueObject, UserPasswordValueObject)
│   ├── application/                  # Use cases and port interfaces
│   │   ├── port/
│   │   │   ├── persistence/repository/  # TaskRepository, UserRepository (interfaces)
│   │   │   └── security/               # Token, PasswordEncoder (interfaces)
│   │   └── usecase/
│   │       ├── task/
│   │       │   ├── interfaces/       # One interface per use case
│   │       │   ├── implementation/   # @Service implementations
│   │       │   ├── dto/input/        # Input DTOs (records)
│   │       │   └── dto/output/       # Output DTOs (records)
│   │       └── user/                 # Same structure as task
│   ├── external/                     # Infrastructure adapters
│   │   ├── persistence/
│   │   │   ├── dao/                  # Spring Data JPA interfaces (TaskJpaDao, UserJpaDao)
│   │   │   ├── model/                # JPA @Entity models (TaskJpaModel, UserJpaModel)
│   │   │   ├── mapper/               # Static mapper classes (domain ↔ JPA model)
│   │   │   └── repository/           # @Repository adapters implementing port interfaces
│   │   └── security/
│   │       ├── filter/JwtAuthenticationFilter.java
│   │       ├── service/UserDetailsServiceImpl.java
│   │       ├── PasswordEncoderAdapter.java
│   │       └── TokenAdapter.java
│   └── presentation/                 # HTTP layer
│       └── controller/
│           ├── AuthController.java
│           ├── TaskController.java
│           ├── UserController.java
│           ├── documentation/        # Swagger @Operation annotations (separate from controllers)
│           ├── payload/              # Request/Response record classes
│           └── common/
│               ├── handler/GlobalExceptionHandler.java
│               ├── filter/           # Servlet filters
│               └── resolver/AuthenticatedUserResolver.java
└── shared/
    └── type/Result.java              # Generic Result<T, E> for value object validation
```

## Architecture rules (Clean Architecture)

- **Domain** has zero Spring/JPA dependencies. Entities and value objects are plain Java.
- **Value objects** are created via a static `of(...)` factory that returns `Result<VO, DomainException>`. Never instantiate directly.
- **Use cases** are defined as interfaces in `usecase/.../interfaces/` and implemented in `usecase/.../implementation/`. Controllers depend only on the interface.
- **Port interfaces** (`TaskRepository`, `Token`, `PasswordEncoder`) live in `application/port/` and are implemented by adapters in `external/`. The domain and application layers never import from `external/`.
- **Mappers** are static utility classes with no state. They translate between domain entities and JPA models (or DTOs).
- **Controllers** implement a `*ControllerDoc` interface that holds all Swagger annotations, keeping the controller class clean.
- **`AuthenticatedUserResolver`** is the single point for extracting the authenticated user ID from the `SecurityContext`.

## Test layout

```
src/test/java/com/jpmns/task/
├── core/
│   ├── application/usecase/   # Unit tests for use cases (Mockito, no Spring context)
│   ├── controller/            # Unit tests for controllers (MockMvc slice)
│   ├── domain/                # Unit tests for entities and value objects
│   ├── external/              # Unit tests for adapters and mappers
│   └── fixture/               # TaskFixture, UserFixture — shared test data builders
├── integration/               # Full-stack integration tests (Testcontainers PostgreSQL)
│   ├── common/
│   │   ├── abstracts/IntegrationTestBase.java  # Base class: @SpringBootTest + MockMvc
│   │   ├── container/PostgresContainerConfig.java
│   │   └── sql/SqlCreateSeed.java              # Annotation: seeds + cleans DB per test
│   ├── AuthIntegrationTest.java
│   ├── TaskIntegrationTest.java
│   └── UserIntegrationTest.java
└── shared/security/
    └── WithJwtTokenMock.java  # Annotation to inject a mock JWT principal in tests
```

## Key conventions

- **Naming**: `PascalCase` for types, `camelCase` for methods/fields, `UPPER_SNAKE_CASE` for constants, lowercase packages.
- **Imports**: static imports first, then grouped `java → javax → jakarta → org → com`, alphabetically sorted, no wildcards.
- **Formatting**: 4-space indentation, no tabs, max 120-char lines, braces always required, opening brace on same line.
- **Logging**: use SLF4J `Logger` (never `System.out`/`System.err`/`printStackTrace()`). Log at `INFO` on entry and exit of controller methods.
- **IDs**: always `UUID` strings at the domain boundary; `UUID` objects in JPA models.
- **Database schema**: managed exclusively by Flyway. Never use `ddl-auto: create/update`. New migrations follow `V{n}__{description}.sql`.
- **Checkstyle**: enforced on every build. Documentation classes (`**/documentation/**`) are excluded.
