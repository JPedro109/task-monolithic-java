---
inclusion: always
---

# Convenções de testes

# Layout

```text
src/test/java/
├── application/                  # Testes unitários dos casos de uso (Mockito, sem contexto Spring)
├── domain/                       # Testes unitários de entidades, value objects e serviços de domínio
├── external/                     # Testes de integração dos adaptadores de infraestrutura (repositories, mappers, segurança, integrações)
├── presentation/                 # Testes unitários dos controllers (MockMvc/WebMvcTest)
├── integration/                  # Testes de integração dos controllers
│   ├── common/
│   │   ├── abstract/
│   │   │   └── IntegrationTestBase.java
│   │   ├── container/
│   │   │   └── PostgresContainerConfig.java
│   │   └── sql/
│   │       └── SqlCreateSeed.java
│   └── SampleIntegrationTest.java
└── shared/
    ├── fixture/
    │   ├── SampleEntityFixture.java
    └── security/
        └── WithJwtTokenMock.java
```

Todos os testes de integração dos controllers, devem herdar da classe abstrata em src/test/java/integration/common/container, a classe abstrata deve ser essa:

```java
@SpringBootTest(classes = TaskApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(PostgresContainerConfig.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;
}
```

Quaisquer configurações de test container devem ser criados em src/test/java/integration/common/container, uma classe por dependência, exemplo:

```java
@SuppressWarnings("resource")
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("task_test")
                .withUsername("test")
                .withPassword("test");
    }
}
```

Quaisquer configurações de seeds para testes devem ficar em src/test/java/integration/common/container, exemplo:

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SqlGroup({
        @Sql(
                scripts = {
                        "classpath:/sql/insert-user.sql",
                        "classpath:/sql/insert-task.sql"
                },
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        @Sql(
                scripts = "classpath:/sql/cleanup.sql",
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
})
public @interface SqlCreateSeed { }
```


# Geral

- Use ou crie fixtures existentes fixtures para construir dados de teste. Nunca instancie entidades de domínio inline dentro dos testes. Nunca use valores aleatórios (`UUID.randomUUID()`, `Math.random()`, etc.) — sempre fixture.
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

# Fixtures

Fixtures são responsáveis por centralizar a criação de objetos utilizados durante os testes.

Seu objetivo é evitar duplicação de código, facilitar a leitura dos testes e manter um único ponto de manutenção para dados de teste.

Todos os fixtures compartilhados devem permanecer no pacote `shared.fixture`.

As seguintes regras devem ser respeitadas:

- Devem ser classes utilitárias.
- Não devem possuir estado.
- Devem possuir construtor privado.
- Devem disponibilizar métodos estáticos para criação dos objetos.
- Devem fornecer valores padrão válidos para todos os atributos obrigatórios.
- Devem permitir a criação rápida de objetos válidos para diferentes cenários de teste.
- Devem ser reutilizados por todos os testes da aplicação sempre que possível.

Estrutura recomendada:

```text
shared/
└── fixture/
    ├── SampleEntityFixture.java
```

## ✔ Correto

```java
public final class SampleEntityFixture {
    private static final String DEFAULT_ID ="00000000-0000-0000-0000-000000000001";
    private static final String DEFAULT_NAME = "Sample";
    private static final String DEFAULT_VALUE = "Value";

    private SampleEntityFixture() { }

    public static SampleEntity aSample() {
        return new SampleEntity(
                DEFAULT_ID,
                DEFAULT_NAME,
                DEFAULT_VALUE
        );
    }
}
```

## ✔ Utilização

```java
@Test
void shouldCreateSample() {
    var sample = SampleEntityFixture.aSample();
    ...
}
```

## ❌ Incorreto

```java
@Test
void shouldCreateSample() {
    var sample = new SampleEntity(
            "00000000-0000-0000-0000-000000000001",
            "Sample",
            "Value"
    );
    ...
}
```

# WireMock

Toda integração HTTP com sistemas externos deve ser testada utilizando **WireMock**.

O objetivo é simular o comportamento de APIs externas de forma determinística, eliminando dependências de ambientes externos durante a execução dos testes.

As seguintes regras devem ser respeitadas:

- Nunca realizar chamadas HTTP para ambientes reais durante os testes.
- Toda API externa deve possuir uma classe responsável por centralizar seus stubs.
- Os testes nunca devem utilizar `stubFor(...)` diretamente.
- Cada método da classe de stub deve representar um cenário de teste.
- Os stubs devem validar método HTTP, URL, parâmetros, headers e corpo da requisição sempre que aplicável.
- Cada teste deve configurar apenas os stubs necessários para o cenário em execução.
- Devem existir cenários de sucesso, erro, timeout e indisponibilidade do serviço.
- Nunca utilizar Mockito para simular clientes HTTP concretos.

Estrutura recomendada:

```text
src/test/java/
└── integration/
    └── common/
        ├── container/
        │   └── WireMockConfig.java
        └── wiremock/
            ├── SampleApiStub.java
```

Cada integração HTTP deve possuir sua própria classe de stubs.

## ✔ Correto

```java
public final class SampleApiStub {

    private SampleApiStub() { }

    public static void sampleExists(String id) {
        stubFor(get(urlEqualTo("/samples/" + id))
                .willReturn(okJson("""
                        {
                            "exists": true
                        }
                        """)));
    }

    public static void sampleDoesNotExist(String id) {
        stubFor(get(urlEqualTo("/samples/" + id))
                .willReturn(okJson("""
                        {
                            "exists": false
                        }
                        """)));
    }

    public static void serviceUnavailable() {
        stubFor(get(urlPathMatching("/samples/.*"))
                .willReturn(serverError()));
    }

    public static void timeout() {
        stubFor(get(urlPathMatching("/samples/.*"))
                .willReturn(aResponse()
                        .withFixedDelay(5000)
                        .withStatus(200)));
    }

}
```

## ✔ Utilização

```java
@Test
@DisplayName("Should create sample when it does not exist")
void shouldCreateSampleWhenItDoesNotExist() throws Exception {

    SampleApiStub.sampleDoesNotExist("1");

    perform().andExpect(status().isCreated());
}
```

```java
@Test
@DisplayName("Should return conflict when sample already exists")
void shouldReturnConflictWhenSampleAlreadyExists() throws Exception {

    SampleApiStub.sampleExists("1");

    perform().andExpect(status().isConflict());
}
```

```java
@Test
@DisplayName("Should return service unavailable when external service is unavailable")
void shouldReturnServiceUnavailableWhenExternalServiceIsUnavailable() throws Exception {

    SampleApiStub.serviceUnavailable();

    perform().andExpect(status().isServiceUnavailable());
}
```

## ❌ Incorreto

```java
@Test
void shouldCreateSample() throws Exception {

    stubFor(get(urlEqualTo("/samples/1"))
            .willReturn(okJson("""
                    {
                        "exists": false
                    }
                    """)));

    perform();
}
```

O uso de `stubFor(...)` diretamente nos testes dificulta a reutilização, aumenta a duplicação de código e espalha detalhes de implementação do WireMock pelos testes.

## ❌ Incorreto

```java
when(sampleApiClient.exists(any()))
        .thenReturn(false);
```

- Clientes HTTP concretos nunca devem ser simulados com Mockito. 
- O comportamento da integração deve ser exercitado utilizando WireMock.
