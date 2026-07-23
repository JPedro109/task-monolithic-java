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
│   │       │   ├── dto/input/        # DTOs de entrada (records sem anotações de framework)
│   │       │   ├── dto/output/       # DTOs de saída (records sem anotações de framework)
│   │       │   └── exception/        # Exceções de aplicação do domínio task
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
│           ├── documentation/        # Interfaces *ControllerDoc com anotações @Operation do Swagger
│           │   └── payload/          # Interfaces *Doc para payloads (anotações @Schema)
│           ├── payload/              # Classes record de Request/Response por domínio
│           │   ├── task/
│           │   │   ├── request/      # CreateTaskRequest, UpdateTaskRequest
│           │   │   └── response/     # TaskResponse
│           │   └── user/
│           │       ├── request/      # UserLoginRequest, CreateUserRequest, etc.
│           │       └── response/     # UserLoginResponse, RefreshTokenResponse, etc.
│           └── common/
│               ├── handler/GlobalExceptionHandler.java
│               ├── filter/           # Filtros Servlet (ex: TracingContextFilter)
│               └── resolver/AuthenticatedUserResolver.java
└── shared/
    └── type/Result.java              # Result<T, E> genérico para validação de value objects
```

## Regras de arquitetura (Clean Architecture)

- O **Domínio** não possui nenhuma dependência de Spring/JPA. Entidades e value objects são Java puro.
- **Value objects** são criados via factory estática `of(...)` que retorna `Result<VO, DomainException>`. O construtor é sempre `private`; nunca instancie diretamente fora da própria classe.
- **Value objects** expõem o valor primitivo via método `asString()`. Não há getter genérico `getValue()` no value object em si.
- **Casos de uso** são definidos como interfaces em `usecase/.../interfaces/` e implementados em `usecase/.../implementation/`. Controllers dependem apenas da interface.
- **Port interfaces** (`TaskRepository`, `Token`, `PasswordEncoder`) ficam em `application/port/` e são implementadas por adaptadores em `external/`. As camadas de domínio e aplicação nunca importam de `external/`.
- **Mappers** são classes utilitárias estáticas sem estado. Possuem construtor `private` e métodos `toModel()` (domínio → JPA) e `toDomain()` (JPA → domínio). Nunca adicionam lógica de negócio.
- **Use case implementations** Nunca retornam entidades de domínio diretamente.
- **Input DTOs** da camada de aplicação (`usecase/.../dto/input/`) são records simples, sem anotações de framework. Recebem apenas tipos primitivos ou strings — nunca value objects.
- **Output DTOs** da camada de aplicação (`usecase/.../dto/output/`) são records simples, sem anotações de framework. Contêm apenas tipos primitivos, strings e `Instant`.
- **Request payloads** (`presentation/controller/payload/.../request/`) são records com anotações Bean Validation (`@NotBlank`, `@Size`, etc.) e implementam a interface `*RequestDoc` correspondente. Sempre sobrescrevem `toString()` quando contêm dados sensíveis.
- **Response payloads** (`presentation/controller/payload/.../response/`) são records que implementam a interface `*ResponseDoc` correspondente e expõem uma factory estática `of(OutputDTO)` para conversão a partir do output do use case.
- **Controllers** implementam a interface `*ControllerDoc` que concentra todas as anotações Swagger, mantendo a classe do controller limpa. Dependem exclusivamente das interfaces de casos de uso.
- **`AuthenticatedUserResolver`** é o único ponto de extração do ID do usuário autenticado a partir do `SecurityContext`.
- **Toda exceção originada em infraestrutura externa** (bibliotecas de terceiros, JPA, JWT, etc.) deve ser capturada no adaptador correspondente e relançada como uma exceção do domínio/aplicação (ex: qualquer exceção da biblioteca JJWT é convertida para `InvalidTokenException`). As camadas de domínio e aplicação nunca devem depender de exceções de frameworks externos.
- **`GlobalExceptionHandler`** é o único ponto de mapeamento de exceções de domínio/aplicação para respostas HTTP. Nenhum controller trata exceções diretamente.

## Convenções principais

- **Nomenclatura**: `PascalCase` para tipos, `camelCase` para métodos/campos, `UPPER_SNAKE_CASE` para constantes, pacotes em letras minúsculas.
- **Sufixo `ConfigProperties`**: toda classe anotada com `@ConfigurationProperties` deve ter o sufixo `ConfigProperties`.
- **Imports**: imports estáticos primeiro, depois agrupados `java → javax → jakarta → org → com`, ordenados alfabeticamente, sem wildcards.
- **Formatação**: indentação de 4 espaços, sem tabs, máximo de 120 caracteres por linha, chaves sempre obrigatórias, chave de abertura na mesma linha.
- **Logging**: use `Logger` do SLF4J (nunca `System.out`/`System.err`/`printStackTrace()`). Logue em nível `INFO` na entrada e saída dos métodos de controller.
- **IDs**: sempre strings `UUID` na fronteira do domínio; objetos `UUID` nos modelos JPA.
- **Schema do banco**: gerenciado exclusivamente pelo Flyway. Nunca use `ddl-auto: create/update`. Novas migrações seguem o padrão `V{n}__{descricao}.sql`.
- **Checkstyle**: aplicado em todo build. Classes de documentação (`**/documentation/**`) são excluídas.

## Convenções de código

### Injeção de dependência via construtor

Toda dependência deve ser declarada como campo `private final` e injetada exclusivamente via construtor. Nunca use `@Autowired` em campo ou setter.

```java
@Service
public class CreateTaskUseCaseImpl implements CreateTaskUseCase {

    private final TaskRepository taskRepository;

    public CreateTaskUseCaseImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
```

### Separação de fases lógicas

Separe cada fase lógica de um método com **uma linha em branco**. Não insira linhas em branco dentro de uma mesma fase. Não agrupe instruções não relacionadas. Preserve o estilo do código ao redor.

Fases típicas de um use case:

```java
@Override
public TaskOutputDTO execute(UpdateTaskInputDTO input) {
    // 1. Preparação de entrada / validação
    var taskIdValueOrError = IdValueObject.of(input.taskId());
    if (taskIdValueOrError.isFail()) {
        throw taskIdValueOrError.getError();
    }

    // 2. Lógica de negócio / busca e autorização
    var taskIdValue = taskIdValueOrError.getValue();
    var task = taskRepository.findById(taskIdValue).orElseThrow(TaskNotFoundException::new);

    var userIsOwner = task.getUserId().asString().equals(input.userId());
    if (!userIsOwner) {
        throw new TaskAccessDeniedException();
    }

    // 3. Mutação no domínio
    task.updateTaskName(input.taskName());

    // 4. Persistência
    var saved = taskRepository.save(task);

    // 5. Mapeamento de resposta
    return toOutput(saved);
}
```

Fases típicas de um controller:

```java
@PostMapping
public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
    LOGGER.info("Creating task - request: {}", request);

    var userId = AuthenticatedUserResolver.getUserId();

    var dto = new CreateTaskInputDTO(userId, request.taskName());
    var output = createTaskUseCase.execute(dto);

    var response = TaskResponse.of(output);

    LOGGER.info("Creating task - response: {}", response);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Validação de value objects nas implementações de use case

Sempre que o input do use case contiver um campo que será usado **isoladamente** (sem instanciar uma entidade completa) — como um ID para busca ou um campo que será atualizado individualmente —, valide com `isFail()` antes de prosseguir e lance a exceção de domínio retornada pelo próprio `Result`. Nunca use o value object sem antes verificar o resultado.

```java
var taskIdValueOrError = IdValueObject.of(input.taskId());
if (taskIdValueOrError.isFail()) {
    throw taskIdValueOrError.getError();
}

var taskIdValue = taskIdValueOrError.getValue();
```

**Exceção — instanciação de entidade completa**: quando todos os campos necessários estão disponíveis e a entidade será criada via construtor, **não** valide os value objects manualmente. O construtor já chama `validateOrThrow` internamente e lança `DomainException` automaticamente. A validação manual nesse caso é redundante.

```java
// CORRETO — entidade valida internamente, não repita a validação
var user = new UserEntity(UUID.randomUUID().toString(), input.username(), input.password());

// INCORRETO — validação duplicada, desnecessária antes da instanciação completa
var usernameValueOrError = UsernameValueObject.of(input.username());
if (usernameValueOrError.isFail()) {
    throw usernameValueOrError.getError();
}
var user = new UserEntity(UUID.randomUUID().toString(), input.username(), input.password());
```

A validação manual é necessária em dois casos:
- Para usar um value object **antes** de instanciar a entidade (ex: verificar unicidade de username sem criar o objeto ainda).
- Para converter campos de IDs recebidos no `InputDTO` que serão usados como parâmetros de busca no repositório.

A conversão da entidade de domínio para `OutputDTO` deve sempre ser feita por um método privado `toOutput(Entity entity)` dentro da implementação. Nunca repita o mapeamento inline ou exponha entidades de domínio fora da implementação.

```java
private TaskOutputDTO toOutput(TaskEntity task) {
    return new TaskOutputDTO(
            task.getId().asString(),
            task.getUserId().asString(),
            task.getTaskName().asString(),
            task.getFinished(),
            task.getCreatedAt()
    );
}
```

### DTOs e conversão de dados

O fluxo de dados entre camadas segue uma direção única, com tipos distintos em cada fronteira:

```
Request (payload) → InputDTO (usecase) → Entity (domain) → OutputDTO (usecase) → Response (payload)
```

- **Controller → Use Case**: o controller monta o `InputDTO` manualmente a partir dos campos do `Request`, nunca passa o `Request` diretamente ao use case.
- **Use Case → Controller**: o use case retorna um `OutputDTO`; o controller converte para `Response` via factory estática `Response.of(outputDto)`.
- **Use Case → Domain**: o use case instancia a entidade diretamente via construtor público. O `InputDTO` carrega apenas strings/primitivos.
- **Use Case → Repository → Domain**: mappers estáticos fazem a conversão entre entidade de domínio e modelo JPA dentro dos adapters.

### `.toString()` customizado para dados sensíveis

Todo record de request ou response que contenha campos sensíveis (senha, token) **deve** sobrescrever `toString()` substituindo o valor por `'[PROTECTED]'`. Essa proteção garante que logs de entrada e saída dos controllers nunca exponham dados confidenciais.

```java
// Exemplo: UserLoginRequest.java
@Override
public String toString() {
    return "UserLoginRequest{username='" + username + "', password='[PROTECTED]'}";
}

// Exemplo: UserLoginResponse.java
@Override
public String toString() {
    return "UserLoginResponse{accessToken='[PROTECTED]', refreshToken='[PROTECTED]'}";
}
```

Campos considerados sensíveis: senhas (`password`, `currentPassword`, `newPassword`), tokens (`accessToken`, `refreshToken`) e qualquer credencial ou segredo.

### Agrupamento de ConfigurationProperties

Cada prefixo do `application.yaml` mapeado para uma classe Java deve usar `@ConfigurationProperties` e seguir as regras abaixo:

- O nome da classe deve ter o sufixo `ConfigProperties` (ex: `SecurityConfigProperties`, `JwtConfigProperties`).
- Todas as classes `@ConfigurationProperties` devem ser registradas centralmente com `@EnableConfigurationProperties` na classe principal da aplicação (`TaskApplication.java`), nunca espalhadas por classes de configuração individuais.
- Quando um prefixo contém subgrupos aninhados no YAML, crie uma classe interna estática `record` ou uma classe separada para o subgrupo (nunca achate tudo em uma única classe com nomes longos).

```java
@ConfigurationProperties(prefix = "security")
public class SecurityConfigProperties {

    private final Jwt jwt;

    public SecurityConfigProperties(Jwt jwt) {
        this.jwt = jwt;
    }

    public Jwt jwt() {
        return jwt;
    }

    // Subgrupo aninhado: security.jwt.*
    public record Jwt(
            String secret,
            long accessTokenExpirationMs,
            long refreshTokenExpirationMs
    ) { }
}

// Registro centralizado na classe principal:
@SpringBootApplication
@EnableConfigurationProperties(SecurityConfigProperties.class)
public class TaskApplication {

}
```

```yaml
# application.yaml
security:
  jwt:
    secret: ${JWT_SECRET:change-me-32-chars-minimum-value}
    access-token-expiration-ms: ${JWT_ACCESS_EXPIRATION_MS:900000}
    refresh-token-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
```

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

## Convenções de testes

### Geral

- Use os fixtures existentes (`TaskFixture`, `UserFixture`) para construir dados de teste. Nunca instancie entidades de domínio inline dentro dos testes. Nunca use valores aleatórios (`UUID.randomUUID()`, `Math.random()`, etc.) — sempre fixture.
- Todo método de teste deve ter `@DisplayName` com uma frase descritiva em inglês no formato `"Should [resultado esperado] when [condição]"`.
  - Exemplos: `"Should return 200 with tokens when credentials are valid"`, `"Should throw when task is not found"`, `"Should return 403 when user does not own the task"`.
- O nome do método é o `@DisplayName` em camelCase: `shouldReturn200WhenCredentialsAreValid`, `shouldThrowWhenTaskNotFound`.
- Use `assertThat` do AssertJ para asserções e `assertThatThrownBy` para verificar exceções.
- Verifique interações com `verify(mock).method(...)` e `verify(mock, never()).method(...)`.
- Testes devem ser ordenados: sucesso (happy path) primeiro, corner cases depois, exceções/erros por último.
- Use `@BeforeEach` e `@AfterEach` quando necessário para setup e teardown compartilhados.
- Siga o padrão **AAA (Arrange → Act → Assert)**, separando cada etapa com uma linha em branco, nunca utilize comentários para separação das etapas.
  - Dentro do `Arrange`, separe a criação de variáveis dos stubs `when(...)` com uma linha em branco:

```java
var user = UserFixture.aUser();
var username = user.getUsername();
var password = user.getPassword();
var input = new CreateUserInputDTO(username.asString(), password.asString());
var savedUser = UserFixture.aUser();

when(userRepository.existsByUsername(username)).thenReturn(false);
when(passwordEncoder.encode(password.asString())).thenReturn(password.asString());
when(userRepository.save(any())).thenReturn(savedUser);

var output = useCase.execute(input);

assertThat(output.username()).isEqualTo(username.asString());
assertThat(output.id()).isNotNull();
verify(userRepository).save(any());
```

- Sempre declare o fixture primeiro e depois extraia cada campo que for utilizar em variáveis separadas:

```java
var user = UserFixture.aUser();
var username = user.getUsername();
var password = user.getPassword();
```

- Cubra o máximo de cenários possível. Sempre siga a estrutura de layout definida para cada tipo de teste.

### Testes unitários de use cases

- Use `@ExtendWith(MockitoExtension.class)` — sem contexto Spring.
- Dependências são declaradas com `@Mock`; a implementação sob teste com `@InjectMocks`.
- Cenários obrigatórios para cada use case:
  - **Happy path**: fluxo principal de sucesso.
  - **Not found**: entidade não encontrada (quando o use case busca por ID).
  - **Access denied**: usuário não é dono do recurso (quando há verificação de ownership).
  - **Invalid input**: ID ou campo inválido que falha na criação do value object (quando aplicável).

```java
@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserUseCaseImpl useCase;

    @Test
    @DisplayName("Should create a user successfully")
    void shouldCreateUserSuccessfully() {
        var user = UserFixture.aUser();
        var username = user.getUsername();
        var password = user.getPassword();
        var input = new CreateUserInputDTO(username.asString(), password.asString());
        var savedUser = UserFixture.aUser();
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password.asString())).thenReturn(password.asString());
        when(userRepository.save(any())).thenReturn(savedUser);

        var output = useCase.execute(input);

        assertThat(output.username()).isEqualTo(username.asString());
        assertThat(output.id()).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("Should throw when username already exists")
    void shouldThrowWhenUsernameAlreadyExists() {
        var user = UserFixture.aUser();
        var username = user.getUsername();
        var password = user.getPassword();
        var input = new CreateUserInputDTO(username.asString(), password.asString());
        when(userRepository.existsByUsername(username)).thenReturn(true);
        when(passwordEncoder.encode(password.asString())).thenReturn(password.asString());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}
```

### Testes unitários de controllers

- Uma classe de teste por controller, com `@WebMvcTest(XxxController.class)`.
- Use `@Import` para incluir beans quando necessário.
- Dependências são declaradas com `@MockitoBean`.
- Uma `@Nested` class por endpoint, com `@DisplayName` indicando o método HTTP e o path (ex: `"POST /api/v1/auth/login"`).
- Cada classe nested tem seu próprio método privado `perform(...)` que encapsula a chamada MockMvc para aquele endpoint.

```java
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserLoginUseCase userLoginUseCase;

    @MockitoBean
    private Token token;

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("Should return 200 with tokens when credentials are valid")
        void shouldReturn200WhenCredentialsAreValid() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();
            var password = user.getPassword();
            var accessToken = "access-token";
            var refreshToken = "refresh-token";
            var output = new UserLoginOutputDTO(accessToken, refreshToken);
            when(userLoginUseCase.execute(any())).thenReturn(output);

            perform(username.asString(), password.asString())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(accessToken))
                    .andExpect(jsonPath("$.refreshToken").value(refreshToken));
        }

        @Test
        @DisplayName("Should return 401 when credentials are invalid")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();
            var wrongPassword = "wrong-password";
            when(userLoginUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

            perform(username.asString(), wrongPassword)
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String username, String password) throws Exception {
            var requestBody = """
                    {"username": "%s", "password": "%s"}
                    """.formatted(username, password);

            return mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }
}
```

### Testes de DAO (`@DataJpaTest`)

- Testes das interfaces Spring Data JPA (`*JpaDao`) usam `@DataJpaTest`, que sobe apenas o slice JPA sem o contexto completo do Spring.
- **Exceção permitida**: testes de DAO podem usar H2 em memória (dependência `testImplementation("com.h2database:h2")`), pois seu objetivo é verificar queries e mapeamentos JPA de forma rápida e isolada, sem necessidade de Testcontainers.
- Não use `@DataJpaTest` para testar lógica de negócio ou adaptadores de repositório — esses pertencem aos testes unitários com Mockito e aos testes de integração com Testcontainers, respectivamente.

### Testes de integração

- Uma classe por controller com o sufixo `IntegrationTest` (ex: `TaskIntegrationTest`), estendendo `IntegrationTestBase`.
- `IntegrationTestBase` fornece `MockMvc`, `@SpringBootTest`, `@AutoConfigureMockMvc`, perfil `integration-test` e Testcontainers PostgreSQL via `PostgresContainerConfig`. Sempre estenda-a — nunca configure essas infraestruturas manualmente.
- Sempre usar Testcontainers — nunca banco em memória ou mocks de persistência (exceto testes de DAO com `@DataJpaTest`, conforme seção acima).
- Uma `@Nested` class por endpoint, com `@DisplayName` indicando o método HTTP e o path.
- Cada classe nested tem seu próprio método privado `perform(...)` que encapsula a chamada MockMvc.
- Cada método de teste declara explicitamente se precisa de `@SqlCreateSeed` (para popular o banco) e/ou `@WithJwtTokenMock` (para autenticação). Não assuma nenhum estado prévio.

```java
@DisplayName("Task Integration Tests")
class TaskIntegrationTest extends IntegrationTestBase {

    @Nested
    @DisplayName("POST /api/v1/tasks")
    class CreateTask {

        @Test
        @DisplayName("Should return 201 with task data when input is valid")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn201WhenInputIsValid() throws Exception {
            var taskName = "My first task";

            perform(taskName)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.taskName").value(taskName))
                    .andExpect(jsonPath("$.finished").value(false));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            perform("My first task")
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when taskName is blank")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn400WhenTaskNameIsBlank() throws Exception {
            perform("")
                    .andExpect(status().isBadRequest());
        }

        private ResultActions perform(String taskName) throws Exception {
            var requestBody = """
                    {"taskName": "%s"}
                    """.formatted(taskName);

            return mockMvc.perform(post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }
}
```
