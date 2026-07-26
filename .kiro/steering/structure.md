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
- **Value objects** são criados via factory estática `of(...)` que retorna `Result<VO>`. O construtor é sempre `private`; nunca instancie diretamente fora da própria classe.
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
- **Toda exceção originada em infraestrutura externa** (bibliotecas de terceiros, JPA, JWT, etc.) que seja relevante para a regra de negócio deve ser capturada no adaptador correspondente e relançada como uma exceção de domínio/aplicação. Por exemplo, qualquer exceção da biblioteca JJWT que indique um token inválido deve ser convertida para InvalidTokenException. Dessa forma, as camadas de domínio e aplicação nunca dependem diretamente de exceções de frameworks ou bibliotecas externas. Se a exceção da infraestrutura não influencia a regra de negócio e apenas representa uma falha técnica inesperada, ela não precisa ser convertida para uma exceção de domínio. Nesses casos, a exceção pode ser propagada para o tratamento global de erros, sendo considerada um erro inesperado..
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

### Ordenação de membros de uma classe

Siga sempre esta ordem dentro de qualquer classe:

1. Constantes (`static final`)
2. Campos de instância
3. Construtores
4. Métodos públicos
5. Métodos protegidos
6. Métodos privados

Métodos estáticos públicos (factories, utilitários da API pública) ficam junto aos métodos públicos. Métodos estáticos privados (helpers internos) ficam junto aos métodos privados, no final.

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

### DTOs e conversão de dados

O fluxo de dados entre camadas segue uma direção única, com tipos distintos em cada fronteira:

```
Request (payload) → InputDTO (usecase) → Entity (domain) → OutputDTO (usecase) → Response (payload)
```

- **Controller → Use Case**: o controller monta o `InputDTO` manualmente a partir dos campos do `Request`, nunca passa o `Request` diretamente ao use case.
- **Use Case → Controller**: o use case retorna um `OutputDTO`; o controller converte para `Response` via factory estática `Response.of(outputDto)`.
- **Use Case → Domain**: o use case instancia a entidade diretamente via construtor público. O `InputDTO` carrega apenas strings/primitivos.
- **Use Case → Repository → Domain**: mappers estáticos fazem a conversão entre entidade de domínio e modelo JPA dentro dos adapters.

## Convenções da camada domain

### Entidade base (`Entity`)

Toda entidade de domínio estende `Entity`. O construtor da classe base recebe `id` (String) e `createdAt` (Instant), valida o ID via `IdValueObject.of(id)` e atribui `Instant.now()` quando `createdAt` for `null`. O campo `id` é `private final IdValueObject`; `createdAt` é `private final Instant`.

As subclasses nunca acessam `id` diretamente — usam sempre `getId()`.

```java
public abstract class Entity {

    private final IdValueObject id;
    private final Instant createdAt;

    public Entity(String id, Instant createdAt) {
        this.id = IdValueObject.of(id).getValueOrThrow();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }
}
```

O método `validateOrThrow(List<Result<?>> results)` é `protected` e coleta todos os `Result` com falha, extrai as `DomainException`s e lança uma `DomainException` agregada via `DomainException.with(errors)`. Subclasses o chamam no construtor após criar todos os value objects.

### Estrutura de entidade de domínio

Entidades seguem este esquema:

1. **Campos mutáveis**: declarados sem `final` (podem ser atualizados por métodos de domínio).
2. **Campos imutáveis (exceto `id` e `createdAt`)**: declarados com `final`.
3. **Construtor completo**: recebe todos os campos como primitivos/strings, cria os value objects, chama `validateOrThrow`, atribui os campos.
4. **Construtor de conveniência**: delega ao construtor completo passando `null` para `createdAt` e `updatedAt` quando não há esses dados disponíveis (ex: criação nova).
5. **Métodos de negócio** (`update*`, `markAs*`): recebem primitivos/strings, recriam o value object via `of(...).getValueOrThrow()` e atualizam o campo.

```java
public class TaskEntity extends Entity {

    private TaskNameValueObject taskName;  // mutável
    private boolean finished;             // mutável
    private final IdValueObject userId;   // imutável após criação
    private final Instant updatedAt;      // imutável após criação

    // Construtor completo
    public TaskEntity(String id, String userId, String taskName,
                      Boolean finished, Instant createdAt, Instant updatedAt) {
        super(id, createdAt);

        var userIdResult = IdValueObject.of(userId);
        var taskNameResult = TaskNameValueObject.of(taskName);

        var results = List.of(userIdResult, taskNameResult);
        validateOrThrow(results);

        this.userId = userIdResult.getValue();
        this.taskName = taskNameResult.getValue();
        this.finished = finished;
        this.updatedAt = updatedAt;
    }

    // Construtor de conveniência (nova entidade)
    public TaskEntity(String id, String userId, String taskName, Boolean finished) {
        this(id, userId, taskName, finished, null, null);
    }

    // Método de negócio
    public void updateTaskName(String taskName) {
        this.taskName = TaskNameValueObject.of(taskName).getValueOrThrow();
    }
}
```

### Value objects

Cada value object segue este contrato:

- Construtor `private` — instanciação exclusiva via `of(...)`.
- Factory estática `of(String value)` retorna `Result<VO>`: `Result.fail(new InvalidXxxException())` quando inválido, `Result.success(new XxxValueObject(value))` quando válido.
- O valor primitivo é exposto por um método de conversão correspondente ao seu tipo, como `asString()`, `asInt()`, `asLong()`, etc. Nunca use getValue().
- Sobrescrevem `equals` e `hashCode` com base no valor retornado pelo método de conversão correspondente (`asString()`, `asInt()`, `asLong()`, etc.).
- Regras de validação ficam dentro do `of(...)` — `null`, formato, tamanho, padrão de regex, etc.

```java
public class UsernameValueObject {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    private final String username;

    private UsernameValueObject(String username) { this.username = username; }

    public static Result<UsernameValueObject> of(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            return Result.fail(new InvalidUsernameException());
        }

        return Result.success(new UsernameValueObject(username));
    }

    public String asString() { return username; }
}
```

### `DomainException`

Classe base de todas as exceções de domínio. Subclasses de value object e entidade estendem diretamente `DomainException` com mensagem fixa no construtor:

```java
public class InvalidTaskNameException extends DomainException {
    public InvalidTaskNameException() {
        super("Task name must not be blank and must have at most 255 characters");
    }
}
```

Nunca lance `RuntimeException` ou `IllegalArgumentException` no domínio — sempre uma subclasse de `DomainException`.

## Convenções da camada application

### Interfaces de porta (`port/`)

As interfaces de porta definem o contrato entre a camada de aplicação e a infraestrutura. Ficam em `application/port/` e são organizadas por categoria:

- `port/persistence/repository/` — interfaces de repositório (`TaskRepository`, `UserRepository`). Assinaturas trabalham exclusivamente com tipos do domínio: `IdValueObject`, `UsernameValueObject`, `TaskEntity`, `UserEntity`. Nunca expõem tipos JPA.
- `port/security/` — interfaces de serviços de segurança (`Token`, `PasswordEncoder`). Trabalham com `String`s e com o DTO de porta `DecodeTokenDto`.
- `port/security/dto/` — DTOs usados nas assinaturas das interfaces de porta de segurança (ex: `DecodeTokenDto`). São records simples sem anotações de framework.
- `port/security/exception/` — exceções lançadas pelas interfaces de porta de segurança (ex: `InvalidTokenException`). Subclasses de `RuntimeException`; não estendem `DomainException`.

```java
public interface TaskRepository {
    TaskEntity save(TaskEntity task);
    Optional<TaskEntity> findById(IdValueObject id);
    List<TaskEntity> findAllByUserId(IdValueObject userId);
    void deleteById(IdValueObject id);
}

public interface Token {
    String generateAccessToken(String sub);
    String generateRefreshToken(String sub);
    DecodeTokenDto tokenValidation(String token);
}
```

### Implementações de caso de uso (`usecase/.../implementation/`)

- Anotadas com `@Service`, implementam a interface correspondente.
- Uma classe por caso de uso, sufixo `Impl` (ex: `CreateTaskUseCaseImpl`).
- Dependências injetadas via construtor como campos `private final`.
- O método `execute` é sempre anotado com `@Override` e segue as fases descritas em [Separação de fases lógicas](#separação-de-fases-lógicas).
- Use cases sem retorno declaram `void` na interface e não retornam nada no `execute`.
- Use cases que retornam lista declaram `List<OutputDTO>` e convertem cada entidade com `stream().map(this::toOutput).toList()`.

Estrutura completa de um use case de criação (sem busca por ID):

```java
@Service
public class CreateTaskUseCaseImpl implements CreateTaskUseCase {

    private final TaskRepository taskRepository;

    public CreateTaskUseCaseImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskOutputDTO execute(CreateTaskInputDTO input) {
        var task = new TaskEntity(UUID.randomUUID().toString(), input.userId(), input.taskName(), false);
        var saved = taskRepository.save(task);

        return toOutput(saved);
    }

    private TaskOutputDTO toOutput(TaskEntity task) {
        return new TaskOutputDTO(
                task.getId().asString(),
                task.getUserId().asString(),
                task.getTaskName().asString(),
                task.getFinished(),
                task.getCreatedAt()
        );
    }
}
```

Estrutura completa de um use case de atualização (com busca por ID, verificação de ownership e mutação):

```java
@Service
public class UpdateTaskUseCaseImpl implements UpdateTaskUseCase {

    private final TaskRepository taskRepository;

    public UpdateTaskUseCaseImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskOutputDTO execute(UpdateTaskInputDTO input) {
        var taskIdValue = IdValueObject.of(input.taskId()).getValueOrThrow();

        var task = taskRepository.findById(taskIdValue).orElseThrow(TaskNotFoundException::new);

        var userIsOwnerTask = task.getUserId().asString().equals(input.userId());
        if (!userIsOwnerTask) {
            throw new TaskAccessDeniedException();
        }

        task.updateTaskName(input.taskName());
        var saved = taskRepository.save(task);

        return toOutput(saved);
    }

    private TaskOutputDTO toOutput(TaskEntity task) { ... }
}
```

Estrutura completa de um use case de exclusão (sem retorno, com ownership):

```java
@Service
public class DeleteTaskUseCaseImpl implements DeleteTaskUseCase {

    private final TaskRepository taskRepository;

    public DeleteTaskUseCaseImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void execute(DeleteTaskInputDTO input) {
        var taskIdValue = IdValueObject.of(input.taskId()).getValueOrThrow();

        var task = taskRepository.findById(taskIdValue).orElseThrow(TaskNotFoundException::new);

        var userIsOwnerTask = task.getUserId().asString().equals(input.userId());
        if (!userIsOwnerTask) {
            throw new TaskAccessDeniedException();
        }

        taskRepository.deleteById(taskIdValue);
    }
}
```

### Exceções de caso de uso (`usecase/.../exception/`)

Exceções específicas de cada domínio de caso de uso ficam em `exception/` ao lado de `interfaces/` e `implementation/`. Estendem `RuntimeException` diretamente (não `DomainException`) e carregam uma mensagem fixa:

```java
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException() {
        super("Task not found");
    }
}
```

Exceções de porta (`port/security/exception/`) seguem o mesmo padrão.



Sempre que o input do use case contiver um campo que será usado **isoladamente** (sem instanciar uma entidade completa) — como um ID para busca ou um campo que será atualizado individualmente —, valide com `isFail()` antes de prosseguir e lance a exceção de domínio retornada pelo próprio `Result`. Nunca use o value object sem antes verificar o resultado.

```java
var taskIdValue = taskIdValueOrError.getValue().getValueOrThrow;
```

**Exceção — instanciação de entidade completa**: quando todos os campos necessários estão disponíveis e a entidade será criada via construtor, **não** valide os value objects manualmente. O construtor já chama `validateOrThrow` internamente e lança `DomainException` automaticamente. A validação manual nesse caso é redundante.

```java
// CORRETO — entidade valida internamente, não repita a validação
var user = new UserEntity(UUID.randomUUID().toString(), input.username(), input.password());

// INCORRETO — validação duplicada, desnecessária antes da instanciação completa
var usernameValueOrError = UsernameValueObject.of(input.username()).getValueOrThrow;
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

## Convenções da camada external

A camada `external` é o único lugar onde infraestrutura pode existir. Toda integração com tecnologia externa — banco de dados, biblioteca de JWT, encoder de senha, fila de mensagens, etc. — deve ser implementada aqui como um adaptador.

**Exceção — camada de apresentação (HTTP, GraphQL, CLI, scheduler)**: mecanismos de entrada da aplicação não são infraestrutura de suporte e por isso **não** pertencem a `external`. Tudo que representa uma forma de acesso à aplicação — controllers HTTP, resolvers GraphQL, comandos CLI, schedulers — pertence à camada `presentation`. Veja as convenções correspondentes em [Convenções da camada presentation](#convenções-da-camada-presentation).

Cada adaptador **obrigatoriamente** implementa uma interface de porta definida em `application/port/`. Nunca crie uma classe de infraestrutura sem uma interface correspondente em `port/`. As camadas de domínio e aplicação nunca importam nada de `external/` — a dependência flui sempre de fora para dentro.

```
application/port/security/Token.java          ← interface (domínio/aplicação conhecem isso)
external/security/TokenAdapter.java           ← implementação (só external conhece JJWT)

application/port/persistence/repository/TaskRepository.java   ← interface
external/persistence/repository/TaskRepositoryAdapter.java    ← implementação (só external conhece JPA)
```

Se uma nova necessidade de infraestrutura surgir (ex: envio de e-mail, cache, storage), o fluxo é sempre:
1. Criar a interface de porta em `application/port/` com o contrato orientado ao domínio.
2. Implementar o adaptador em `external/` usando a tecnologia concreta.
3. Registrar o adaptador como bean Spring (`@Component`, `@Repository`, etc.).

### Adaptadores de persistência

#### Modelos JPA (`*JpaModel`)

- Anotados com `@Entity` e `@Table(name = "...")`.
- Campos `UUID` para IDs (não `String`) — a conversão é feita nos mappers com `UUID.fromString(...)` / `.toString()`.
- `@Id` sem geração automática (`@GeneratedValue`) — o ID vem do domínio.
- `@CreationTimestamp` e `@UpdateTimestamp` do Hibernate para `createdAt` e `updatedAt`.
- Colunas imutáveis após criação recebem `updatable = false`.
- Possuem construtor padrão sem argumentos (exigido pelo JPA) e construtor com todos os campos.
- Campos mutáveis (ex: `taskName`, `finished`) expõem setters; campos imutáveis expõem apenas getters.

```java
@Entity
@Table(name = "tasks")
public class TaskJpaModel {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(nullable = false)
    private boolean finished;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TaskJpaModel() { }

    public TaskJpaModel(UUID id, UUID userId, String taskName, boolean finished, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.taskName = taskName;
        this.finished = finished;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getTaskName() { return taskName; }
    public boolean isFinished() { return finished; }

    // setters apenas para campos mutáveis
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public void setFinished(boolean finished) { this.finished = finished; }
}
```

#### DAOs Spring Data JPA (`*JpaDao`)

- Estendem `JpaRepository<Model, UUID>`.
- Declaram apenas os métodos de query adicionais necessários (ex: `findAllByUserId`). Métodos padrão do `JpaRepository` são usados diretamente.
- Sem anotações `@Query` a não ser que a query derivada não seja possível.

```java
public interface TaskJpaDao extends JpaRepository<TaskJpaModel, UUID> {

    List<TaskJpaModel> findAllByUserId(UUID userId);
}
```

#### Adaptadores do Repository (`*RepositoryAdapter`)

- Anotados com `@Repository`, implementam a interface de porta correspondente (`TaskRepository`, `UserRepository`).
- Recebem o DAO Spring Data JPA (`*JpaDao`) via construtor.
- Toda operação converte domínio → modelo com `Mapper.toModel(entity)` antes de persistir, e modelo → domínio com `Mapper.toDomain(model)` ao retornar.

```java
@Repository
public class TaskRepositoryAdapter implements TaskRepository {

    private final TaskJpaDao jpaRepository;

    public TaskRepositoryAdapter(TaskJpaDao jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TaskEntity save(TaskEntity task) {
        var model = TaskMapper.toModel(task);
        var saved = jpaRepository.save(model);
        return TaskMapper.toDomain(saved);
    }

    @Override
    public Optional<TaskEntity> findById(IdValueObject id) {
        var parsedId = UUID.fromString(id.asString());
        return jpaRepository.findById(parsedId).map(TaskMapper::toDomain);
    }
}
```

#### Mappers (`*Mapper`)

- Classes utilitárias estáticas: construtor `private`, todos os métodos `public static`.
- `toModel(Entity entity)` — converte domínio para JPA, chamando `UUID.fromString(entity.getId().asString())` para IDs.
- `toDomain(Model model)` — converte JPA para domínio, chamando `model.getId().toString()` para IDs.
- Nunca adicionam lógica de negócio ou validação.

```java
public class TaskMapper {

    private TaskMapper() { }

    public static TaskJpaModel toModel(TaskEntity entity) {
        return new TaskJpaModel(
                UUID.fromString(entity.getId().asString()),
                UUID.fromString(entity.getUserId().asString()),
                entity.getTaskName().asString(),
                entity.getFinished(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static TaskEntity toDomain(TaskJpaModel model) {
        return new TaskEntity(
                model.getId().toString(),
                model.getUserId().toString(),
                model.getTaskName(),
                model.isFinished(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}
```

### Adaptadores de segurança

**`TokenAdapter`** (`@Component`, implementa `Token`):
- Recebe `SecurityConfigProperties` via construtor e extrai `jwt.secret()`, `jwt.accessTokenExpirationMs()`, `jwt.refreshTokenExpirationMs()`.
- Toda exceção da biblioteca JJWT é capturada no `catch (Exception e)` de `tokenValidation` e relançada como `InvalidTokenException`. Nunca deixa exceções de JJWT escapar para fora do adaptador.
- `generateAccessToken` e `generateRefreshToken` delegam para um método privado `buildToken(sub, expirationMs, tokenType)`.
- Loga em nível `ERROR` quando o token é inválido.

**`PasswordEncoderAdapter`** (`@Component`, implementa `PasswordEncoder`):
- Recebe `BCryptPasswordEncoder` (bean registrado em `SecurityConfig`) via construtor.
- Delega `encode` e `matches` diretamente ao `BCryptPasswordEncoder` sem lógica adicional.

**`JwtAuthenticationFilter`** (`@Component`, estende `OncePerRequestFilter`):
- Extrai o token do header `Authorization: Bearer <token>`.
- Se o header estiver ausente ou sem prefixo `Bearer `, segue a cadeia sem autenticar.
- Chama `token.tokenValidation(jwt)` — se lançar exceção, segue a cadeia sem autenticar (o endpoint protegido devolverá 401).
- Se o `sub` for válido e o `SecurityContext` estiver vazio, chama `getUserByIdUseCase.execute(input)` e popula o `SecurityContext` com `UsernamePasswordAuthenticationToken` tendo o `userId` como principal e lista de authorities vazia.

## Convenções da camada presentation

A camada `presentation` concentra tudo que representa uma forma de acesso à aplicação. Independentemente do protocolo ou mecanismo, qualquer ponto de entrada deve estar aqui:

- **HTTP** — controllers REST (`@RestController`), filtros Servlet, handlers de exceção
- **GraphQL** — resolvers e tipos de entrada/saída
- **CLI** — comandos de linha de comando
- **Scheduler** — tarefas agendadas (`@Scheduled`)

Cada mecanismo de entrada fica em seu próprio subpacote dentro de `presentation/` (ex: `controller/`, `scheduler/`). Nunca coloque pontos de entrada em `external/` — essa camada é exclusiva para adaptadores de infraestrutura de suporte (banco, JWT, etc.).

### Estrutura dos controllers

- Anotados com `@RestController` e `@RequestMapping` com o path base.
- Implementam a interface `*ControllerDoc` correspondente — nenhuma anotação do SpringDoc no corpo do controller.
- Logger declarado como `private static final Logger LOGGER = LoggerFactory.getLogger(XxxController.class)`.
- Todas as dependências (interfaces de caso de uso) injetadas via construtor.
- `AuthenticatedUserResolver.getUserId()` é o único ponto de extração do ID do usuário autenticado. Nunca acesse o `SecurityContext` diretamente nos controllers.

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

### `GlobalExceptionHandler`

- Anotado com `@RestControllerAdvice`.
- Único ponto de mapeamento de exceções para respostas HTTP. Nenhum controller captura exceções.
- Retorna `ProblemDetail` (Problem Details RFC 7807) via `ProblemDetail.forStatusAndDetail(status, message)`.
- Todo handler loga em nível `ERROR` com `LOGGER.error("...: {}", ex.getMessage(), ex)`.

### `AuthenticatedUserResolver`

Classe utilitária estática (sem instância) que lê o `principal` do `SecurityContext`:

- `getUserId()` — lança `IllegalArgumentException` se não autenticado. Use em todos os endpoints protegidos.
- `getUserIdOrNull()` — retorna `null` se não autenticado. Use apenas em endpoints opcionalmente autenticados.

O principal no `SecurityContext` é sempre uma `String` com o UUID do usuário, populada pelo `JwtAuthenticationFilter`.

## Convenções de documentação (Swagger / OpenAPI)

Toda a documentação da API é declarada fora dos controllers, em interfaces e classes dedicadas dentro de `presentation/controller/documentation/`. Os controllers permanecem limpos — sem nenhuma anotação do SpringDoc.

### `*ControllerDoc` — documentação de endpoints

- Cada controller possui uma interface `*ControllerDoc` correspondente em `documentation/` (ex: `TaskControllerDoc`, `AuthControllerDoc`).
- A interface é anotada com `@Tag(name = "...", description = "...")` e `@RequestMapping` com o path base do controller.
- Endpoints que exigem autenticação recebem `@SecurityRequirement(name = "bearerAuth")` na interface (ou no método, quando apenas alguns endpoints do controller são protegidos).
- Cada método da interface declara exatamente uma anotação `@Operation` e uma `@ApiResponses`.

**`@Operation`** deve conter:
- `summary`: título curto do endpoint (ex: `"Criar nova tarefa"`).
- `description`: descrição em HTML com `<p>`, `<ul>`, `<li>`, `<code>` e `<blockquote>` quando necessário. Sempre mencione o header de autenticação em endpoints protegidos.
- `requestBody` (quando aplicável): com `mediaType = "application/json"`, `schema` apontando para a classe de request e ao menos dois `@ExampleObject` — um válido e um inválido.
- `parameters` (quando aplicável): para path variables, com `name`, `description`, `required = true` e `example`.

**`@ApiResponses`** deve cobrir todos os status HTTP possíveis para o endpoint:
- Sucesso (`2xx`): com `schema` e ao menos um `@ExampleObject` com corpo representativo.
- Erros de validação (`400`): quando o endpoint aceita `@RequestBody`.
- Não autenticado (`401`): em todo endpoint protegido.
- Acesso negado (`403`): quando há verificação de ownership.
- Não encontrado (`404`): quando o use case pode lançar `*NotFoundException`.
- Conflito (`409`): quando há verificação de unicidade.
- Erro interno (`500`): sempre presente, com exemplo padrão.

Para respostas sem corpo (`204 No Content`), omita o `content` na `@ApiResponse`.

```java
@Tag(name = "Tasks", description = "Gerenciamento de tarefas — criação, listagem, atualização, exclusão e conclusão")
@RequestMapping("/api/v1/tasks")
@SecurityRequirement(name = "bearerAuth")
public interface TaskControllerDoc {

    @Operation(
            summary = "Criar nova tarefa",
            description = """
                    <p>Cria uma nova tarefa associada ao usuário autenticado.</p>
                    <p>A tarefa é criada com o status <code>finished: false</code> por padrão.</p>
                    <p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateTaskRequest.class),
                            examples = {
                                    @ExampleObject(name = "Tarefa válida", value = """
                                            {"taskName": "Estudar Spring Boot"}
                                            """),
                                    @ExampleObject(name = "Nome em branco (inválido)", value = """
                                            {"taskName": ""}
                                            """)
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class),
                            examples = @ExampleObject(name = "Tarefa criada", value = """
                                    {
                                      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                                      "taskName": "Estudar Spring Boot",
                                      "finished": false
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Não autenticado", value = """
                                    {"status": 401, "detail": "Invalid or expired token"}
                                    """))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Erro interno", value = """
                                    {"status": 500, "detail": "Internal server error"}
                                    """)))
    })
    ResponseEntity<TaskResponse> createTask(@Valid @org.springframework.web.bind.annotation.RequestBody CreateTaskRequest request);
}
```

### `*RequestDoc` / `*ResponseDoc` — documentação de payloads

- Cada record de request ou response implementa uma interface `*Doc` correspondente em `documentation/payload/{domínio}/request/` ou `.../response/`.
- A interface é anotada com `@Schema(name = "NomeDaClasse", description = "...")`.
- Cada método da interface declara `@Schema` com `description`, `example` e, quando aplicável, `minLength` / `maxLength`.
- O record de request/response implementa a interface — as anotações `@Schema` dos métodos são herdadas automaticamente pelo SpringDoc.
- Campos sensíveis (senha, token) devem ter `example` com valor fictício (nunca omitir o exemplo).

```java
// Interface de documentação
@Schema(name = "CreateTaskRequest", description = "Dados para criação de uma nova tarefa")
public interface CreateTaskRequestDoc {

    @Schema(
            description = "Nome da tarefa. Não pode ser vazio e deve ter no máximo 255 caracteres.",
            example = "Estudar Spring Boot",
            maxLength = 255)
    String taskName();
}

// Record que implementa a interface
public record CreateTaskRequest(
        @NotBlank
        @Size(max = 255)
        String taskName()
) implements CreateTaskRequestDoc { }
```

### Regras gerais de documentação

- **Checkstyle é excluído** para toda a pasta `**/documentation/**` — anotações longas do SpringDoc são permitidas sem restrição de linha.
- Nunca adicione anotações do SpringDoc (`@Operation`, `@ApiResponse`, `@Schema`, etc.) diretamente nos controllers ou nos records de request/response. Toda documentação pertence às interfaces `*Doc`.
- Exemplos de corpo de resposta de erro devem seguir o formato Problem Details (RFC 7807): campos `type`, `title`, `status`, `detail`.
- Os nomes dos `@ExampleObject` devem ser descritivos e em português, indicando o cenário representado (ex: `"Credenciais válidas"`, `"Username muito curto (inválido)"`).

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

### Testes unitários de value objects

Cobrem value objects (`*ValueObject`) — sem contexto Spring, sem Mockito.

Uma classe de teste por value object, sem anotações de extensão:

- Happy path: criação com valor válido, verificar `isFail()` é `false` e `getValue().asString()` retorna o valor esperado.
- Boundary cases: valores nos limites (mínimo, máximo, exato).
- Falhas: `null`, vazio, em branco, fora do limite, formato inválido — use `@ParameterizedTest` com `@NullAndEmptySource` e `@ValueSource` quando os casos forem similares.

```java
@Test
@DisplayName("Should create a valid TaskNameValueObject")
void shouldCreateValidTaskName() {
    var name = "Buy groceries";

    var result = TaskNameValueObject.of(name);

    assertThat(result.isFail()).isFalse();
    assertThat(result.getValue().asString()).isEqualTo(name);
}

@ParameterizedTest
@DisplayName("Should fail for null, empty or blank names")
@NullAndEmptySource
@ValueSource(strings = {"   "})
void shouldFailForBlankOrNullName(String name) {
    var result = TaskNameValueObject.of(name);

    assertThat(result.isFail()).isTrue();
}
```

### Testes unitários de entities

Cobrem entidades de domínio (`*Entity`) — sem contexto Spring, sem Mockito.

Uma classe de teste por entidade, sem anotações de extensão:

- Happy path: construção com dados válidos, verificar todos os campos via getters.
- Métodos de negócio: cada `update*` e `markAs*` tem ao menos um cenário de sucesso e um de falha.
- Falhas de construção: IDs inválidos, campos obrigatórios nulos/vazios/fora do limite — assert em `DomainException`.
- Para construção direta (sem fixture), declare cada campo como variável separada antes de instanciar.

```java
@Test
@DisplayName("Should throw when task name is empty")
void shouldThrowWhenTaskNameIsBlank() {
    var id = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var finished = false;
    var emptyTaskName = "";

    assertThatThrownBy(() -> new TaskEntity(id, userId, emptyTaskName, finished))
            .isInstanceOf(DomainException.class);
}

@Test
@DisplayName("Should update the task name")
void shouldUpdateTaskName() {
    var task = TaskFixture.aTask();
    var taskName = "Buy groceries";

    task.updateTaskName(taskName);

    assertThat(task.getTaskName().asString()).isEqualTo(taskName);
}
```

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

### Testes de persistência

Cobrem os três componentes da camada `external/persistence/`: DAOs, mappers e repository adapters. Cada um tem sua própria estratégia de teste.

#### Testes de DAO (`@DataJpaTest`)

- Usam `@DataJpaTest` — sobe apenas o slice JPA, sem contexto Spring completo.
- Usam H2 em memória (`testImplementation("com.h2database:h2")`): o objetivo é verificar queries e mapeamentos JPA de forma rápida e isolada, sem Testcontainers.
- Dependências (`*JpaDao`) são injetadas com `@Autowired`.
- Constantes para IDs desconhecidos ficam como `private static final UUID` na classe de teste — nunca crie UUIDs aleatórios inline.
- Use `@BeforeEach` para popular dados de pré-requisito (ex: salvar um `UserJpaModel` antes de testar `TaskJpaDao`).
- Construa os modelos JPA via métodos auxiliares privados (`buildTask(...)`, `buildUser(...)`) para evitar repetição.
- Cenários obrigatórios: `save`, `findById` (encontrado e não encontrado), `findAll*` (com resultado e lista vazia), `deleteById`, atualização via `save` de modelo existente, violação de integridade referencial.

```java
@DataJpaTest
@DisplayName("TaskJpaDao Tests")
class TaskJpaDaoTest {

    private static final UUID UNKNOWN_TASK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private TaskJpaDao taskJpaDao;

    @Autowired
    private UserJpaDao userJpaDao;

    @BeforeEach
    void setUp() {
        var user = UserFixture.aUser();
        userJpaDao.save(buildUser(user));
    }

    @Test
    @DisplayName("Should return empty Optional when task id does not exist")
    void shouldReturnEmptyWhenTaskNotFoundById() {
        var found = taskJpaDao.findById(UNKNOWN_TASK_ID);

        assertThat(found).isEmpty();
    }
}
```

#### Testes de mapper

- Sem anotações de extensão — são classes Java puras.
- Um teste por direção de mapeamento: `toModel` e `toDomain`.
- Verificar que todos os campos são mapeados corretamente, incluindo conversão de `UUID` ↔ `String`.
- Casos adicionais para campos que possam ter comportamentos distintos (ex: preservação de valor específico).

```java
@Test
@DisplayName("Should map a TaskEntity to a TaskJpaModel correctly")
void shouldMapEntityToModel() {
    var task = TaskFixture.aTask();
    var taskId = task.getId();
    var model = TaskMapper.toModel(task);

    assertThat(model.getId().toString()).isEqualTo(taskId.asString());
    assertThat(model.getTaskName()).isEqualTo(task.getTaskName().asString());
}
```

#### Testes de repository adapter

- Use `@ExtendWith(MockitoExtension.class)` — o DAO é mockado com `@Mock`; o adapter com `@InjectMocks`.
- O objetivo é verificar que o adapter converte corretamente domínio ↔ modelo e delega ao DAO.
- Construa os modelos JPA via método auxiliar privado `buildXxxModel()` na classe de teste.
- Cenários obrigatórios espelham as operações da interface de porta: `save`, `findById` (presente e vazio), `findAll*`, `deleteById`.

```java
@ExtendWith(MockitoExtension.class)
class TaskRepositoryAdapterTest {

    @Mock
    private TaskJpaDao jpaRepository;

    @InjectMocks
    private TaskRepositoryAdapter adapter;

    @Test
    @DisplayName("Should save a task and return the persisted domain entity")
    void shouldSaveTask() {
        var task = TaskFixture.aTask();
        var model = buildTaskModel();

        when(jpaRepository.save(any())).thenReturn(model);

        var result = adapter.save(task);

        assertThat(result.getId().asString()).isEqualTo(task.getId().asString());
        verify(jpaRepository).save(any());
    }
}
```

### Testes de integração

Cobrem adaptadores de infraestrutura que **não** são persistência — como `TokenAdapter`, `PasswordEncoderAdapter` e qualquer outro adaptador de `external/` que integre com biblioteca de terceiros. Esses testes instanciam o adaptador diretamente (sem contexto Spring) e verificam o comportamento real da integração.

- Sem `@ExtendWith` — o adaptador é instanciado manualmente no `@BeforeEach`.
- Dependências externas reais são usadas (ex: `BCryptPasswordEncoder`, `SecurityConfigProperties` construído manualmente).
- Constantes de configuração (segredos, expirations) são declaradas como `private static final` na classe de teste.
- Cenários obrigatórios para `Token`: geração de access token, geração de refresh token, validação com subject correto, token expirado, token malformado, token assinado com segredo diferente.
- Cenários obrigatórios para `PasswordEncoder`: encode retorna hash diferente do raw, hashes distintos para a mesma senha, `matches` retorna `true` para senha correta e `false` para senha errada.

```java
class TokenAdapterTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars!!";
    private static final long ACCESS_EXPIRATION_MS = 900_000L;
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    private TokenAdapter tokenAdapter;

    @BeforeEach
    void setUp() {
        tokenAdapter = new TokenAdapter(buildProperties(SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS));
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is expired")
    void shouldThrowWhenTokenIsExpired() {
        var expiredAdapter = new TokenAdapter(buildProperties(SECRET, -1L, -1L));
        var user = UserFixture.aUser();
        var sub = user.getId().asString();
        var token = expiredAdapter.generateAccessToken(sub);

        assertThatThrownBy(() -> tokenAdapter.tokenValidation(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    private SecurityConfigProperties buildProperties(String secret, long accessMs, long refreshMs) {
        return new SecurityConfigProperties(new SecurityConfigProperties.Jwt(secret, accessMs, refreshMs));
    }
}
```

### Testes E2E

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
        @DisplayName("Should return 400 when taskName is blank")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn400WhenTaskNameIsBlank() throws Exception {
            perform("")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            perform("My first task")
                    .andExpect(status().isUnauthorized());
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
