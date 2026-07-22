# Stack Tecnológica

## Core

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 |
| Banco de Dados | PostgreSQL 15 |
| Migrações | Flyway (scripts versionados em `src/main/resources/db/migration/`) |
| ORM | Spring Data JPA / Hibernate |
| Segurança | Spring Security + JWT via JJWT 0.12.6 |
| Documentação de API | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle 8 com Kotlin DSL (`build.gradle.kts`) |

## Observabilidade

- OpenTelemetry (Spring Boot starter)
- Micrometer + OTLP registry
- Spring Boot Actuator (expõe `health`, `info`, `metrics`)
- Prometheus + Grafana via Docker Compose

## Testes

| Ferramenta | Finalidade |
|------------|-----------|
| JUnit 5 | Testes unitários e de integração |
| Mockito | Mocking em testes unitários (`@ExtendWith(MockitoExtension.class)`) |
| Testcontainers (PostgreSQL) | BD real para testes de integração |
| Spring MockMvc | Testes de integração na camada HTTP |
| JaCoCo | Cobertura de código (mínimo de 85%, verificado no build) |
| Checkstyle 10.21.4 | Análise estática (zero avisos permitidos) |

## Comandos Comuns

```bash
# Executar a aplicação (requer PostgreSQL rodando ou stack Docker no ar)
./gradlew bootRun

# Executar todos os testes (requer Docker para Testcontainers)
./gradlew test

# Executar testes + verificação de cobertura + Checkstyle
./gradlew check

# Subir infraestrutura (PostgreSQL, Prometheus, Grafana, OTEL Collector)
cd docker && docker compose up -d

# Visualizar relatório de cobertura (após rodar os testes)
# build/reports/jacoco/test/html/index.html
```

## Configurações Principais

Toda a configuração de runtime está em `src/main/resources/application.yaml` e é controlada por variáveis de ambiente:

| Variável | Padrão | Observações |
|----------|--------|-------------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `task` | Conexão com o PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | |
| `JWT_SECRET` | `change-me-...` | Deve ter ≥ 32 caracteres; sempre substitua em produção |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` (15 min) | |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 dias) | |
| `ENABLE_OTLP_COLLECTOR` | `false` | Defina como `true` para enviar métricas |
| `OTLP_COLLECTOR_URL` | — | Obrigatório quando OTLP estiver habilitado |

Os testes de integração usam o perfil Spring `integration-test` (`src/test/resources/application-integration-test.yaml`) e sobem automaticamente uma instância PostgreSQL via Testcontainers.
