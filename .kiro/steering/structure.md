# Estrutura do Projeto

## Layout do código-fonte

```
src/main/java/com/jpmns/task/
├── TaskApplication.java              # Ponto de entrada do Spring Boot
├── configuration/                    # Configurações de framework (não cobertas pelo JaCoCo)
│   ├── security/SecurityConfig.java  # Cadeia de filtros do Spring Security
│   ├── swagger/SwaggerConfig.java    # OpenAPI / Swagger UI
│   └── tracing/OtelBaggageConfig.java
├── core/
│   ├── domain/                       # Lógica de negócio pura — sem dependências de framework
│   │   ├── common/
│   │   │   ├── abstracts/Entity.java # Entidade base (id + createdAt + validateOrThrow)
│   │   │   ├── exception/DomainException.java
│   │   │   └── valueobject/IdValueObject.java
│   │   ├── task/
│   │   │   ├── TaskEntity.java
│   │   │   └── valueobject/TaskNameValueObject.java
│   │   └── user/
│   │       ├── UserEntity.java
│   │       └── valueobject/  (UserEmailValueObject, UsernameValueObject, UserPasswordValueObject)
│   ├── application/                  # Casos de uso e interfaces de porta
│   │   ├── port/
│   │   │   ├── persistence/repository/  # TaskRepository, UserRepository (interfaces)
│   │   │   └── security/               # Token, PasswordEncoder (interfaces)
│   │   └── usecase/
│   │       ├── task/
│   │       │   ├── interfaces/       # Uma interface por caso de uso
│   │       │   ├── implementation/   # Implementações com @Service
│   │       │   ├── dto/input/        # DTOs de entrada (records)
│   │       │   └── dto/output/       # DTOs de saída (records)
│   │       └── user/                 # Mesma estrutura que task
│   ├── external/                     # Adaptadores de infraestrutura
│   │   ├── persistence/
│   │   │   ├── dao/                  # Interfaces Spring Data JPA (TaskJpaDao, UserJpaDao)
│   │   │   ├── model/                # Modelos @Entity do JPA (TaskJpaModel, UserJpaModel)
│   │   │   ├── mapper/               # Classes de mapeamento estático (domínio ↔ modelo JPA)
│   │   │   └── repository/           # Adaptadores @Repository implementando interfaces de porta
│   │   └── security/
│   │       ├── filter/JwtAuthenticationFilter.java
│   │       ├── service/UserDetailsServiceImpl.java
│   │       ├── PasswordEncoderAdapter.java
│   │       └── TokenAdapter.java
│   └── presentation/                 # Camada HTTP
│       └── controller/
│           ├── AuthController.java
│           ├── TaskController.java
│           ├── UserController.java
│           ├── documentation/        # Anotações @Operation do Swagger (separadas dos controllers)
│           ├── payload/              # Classes record de Request/Response
│           └── common/
│               ├── handler/GlobalExceptionHandler.java
│               ├── filter/           # Filtros Servlet
│               └── resolver/AuthenticatedUserResolver.java
└── shared/
    └── type/Result.java              # Result<T, E> genérico para validação de value objects
```

## Regras de arquitetura (Clean Architecture)

- O **Domínio** não possui nenhuma dependência de Spring/JPA. Entidades e value objects são Java puro.
- **Value objects** são criados via factory estática `of(...)` que retorna `Result<VO, DomainException>`. Nunca instancie diretamente.
- **Casos de uso** são definidos como interfaces em `usecase/.../interfaces/` e implementados em `usecase/.../implementation/`. Controllers dependem apenas da interface.
- **Interfaces de porta** (`TaskRepository`, `Token`, `PasswordEncoder`) ficam em `application/port/` e são implementadas por adaptadores em `external/`. As camadas de domínio e aplicação nunca importam de `external/`.
- **Mappers** são classes utilitárias estáticas sem estado. Traduzem entre entidades de domínio e modelos JPA (ou DTOs).
- **Controllers** implementam uma interface `*ControllerDoc` que concentra todas as anotações Swagger, mantendo a classe do controller limpa.
- **`AuthenticatedUserResolver`** é o único ponto de extração do ID do usuário autenticado a partir do `SecurityContext`.

## Layout de testes

```
src/test/java/com/jpmns/task/
├── core/
│   ├── application/usecase/   # Testes unitários de casos de uso (Mockito, sem contexto Spring)
│   ├── controller/            # Testes unitários de controllers (slice MockMvc)
│   ├── domain/                # Testes unitários de entidades e value objects
│   ├── external/              # Testes unitários de adaptadores e mappers
│   └── fixture/               # TaskFixture, UserFixture — construtores de dados de teste compartilhados
├── integration/               # Testes de integração completos (Testcontainers PostgreSQL)
│   ├── common/
│   │   ├── abstracts/IntegrationTestBase.java  # Classe base: @SpringBootTest + MockMvc
│   │   ├── container/PostgresContainerConfig.java
│   │   └── sql/SqlCreateSeed.java              # Anotação: popula e limpa o BD por teste
│   ├── AuthIntegrationTest.java
│   ├── TaskIntegrationTest.java
│   └── UserIntegrationTest.java
└── shared/security/
    └── WithJwtTokenMock.java  # Anotação para injetar um principal JWT mockado nos testes
```

## Convenções principais

- **Nomenclatura**: `PascalCase` para tipos, `camelCase` para métodos/campos, `UPPER_SNAKE_CASE` para constantes, pacotes em letras minúsculas.
- **Imports**: imports estáticos primeiro, depois agrupados `java → javax → jakarta → org → com`, ordenados alfabeticamente, sem wildcards.
- **Formatação**: indentação de 4 espaços, sem tabs, máximo de 120 caracteres por linha, chaves sempre obrigatórias, chave de abertura na mesma linha.
- **Logging**: use `Logger` do SLF4J (nunca `System.out`/`System.err`/`printStackTrace()`). Logue em nível `INFO` na entrada e saída dos métodos de controller.
- **IDs**: sempre strings `UUID` na fronteira do domínio; objetos `UUID` nos modelos JPA.
- **Schema do banco**: gerenciado exclusivamente pelo Flyway. Nunca use `ddl-auto: create/update`. Novas migrações seguem o padrão `V{n}__{descricao}.sql`.
- **Checkstyle**: aplicado em todo build. Classes de documentação (`**/documentation/**`) são excluídas.
