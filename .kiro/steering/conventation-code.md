---
inclusion: always
---

# Convenções de Código

# Nomenclatura

A nomenclatura dos componentes deve ser consistente, descritiva e refletir claramente sua responsabilidade.

As seguintes convenções devem ser adotadas:

- Classes devem utilizar **PascalCase**.
- Métodos e variáveis devem utilizar **camelCase**.
- Constantes devem utilizar **UPPER_SNAKE_CASE**.
- Pacotes devem utilizar apenas letras minúsculas.
- Interfaces devem representar comportamentos ou contratos.
- Interfaces **não devem** utilizar o prefixo `I`.
- Implementações concretas de interfaces devem utilizar o sufixo `Impl`.
- Classes concretas devem possuir nomes que representem claramente sua responsabilidade.

Evite abreviações desnecessárias e nomes genéricos que não expressem claramente a responsabilidade do componente.

### ✔ Correto

```java
public interface CreateUserUseCase {}

public class CreateUserUseCaseImpl {}

public class UserRepositoryImpl {}

public interface PasswordEncoder {}

public class JwtTokenProviderImpl {}
```

### ❌ Incorreto

```java
public class UserManager {}

public class Helper {}

public class Util {}

public class Processor {}
```

---

# Constantes

Valores reutilizados ou que representem regras estáticas devem ser declarados como constantes.

Não é permitido utilizar valores literais ("magic numbers" ou "magic strings") quando seu significado puder ser representado por uma constante nomeada.

### ✔ Correto

```java
private static final int MAX_USERNAME_LENGTH = 50;
```

### ❌ Incorreto

```java
if (username.length() > 50) {
    ...
}
```

---

# Uso do var

Sempre que possível, utilizar a palavra reservada var para inferência de tipo:

### ✔ Correto

```java
var name = "john"
```

### ❌ Incorreto

```java
String name = "john"
```

# Formatação

Todo o código deve seguir um único padrão de formatação.

A formatação deve garantir:

- Consistência visual;
- Facilidade de leitura;
- Padronização entre todos os projetos.

A formatação deve ser automatizada por ferramentas apropriadas, evitando diferenças de estilo entre desenvolvedores.

Além da formatação automática, o código deve ser organizado em **blocos lógicos**, utilizando linhas em branco para separar etapas distintas de uma operação.

Cada bloco deve representar uma fase claramente identificável da execução, como:

- Validação;
- Conversão de dados;
- Execução da regra de negócio;
- Persistência;
- Publicação de eventos;
- Retorno do resultado.

### ✔ Correto

```java
public User create(CreateUserInput input) {
    validate(input);

    User user = mapper.toEntity(input);

    repository.save(user);

    eventPublisher.publish(new UserCreatedEvent(user));

    return mapper.toOutput(user);
}
```

### ❌ Incorreto

```java
public User create(CreateUserInput input) {
    validate(input);
    User user = mapper.toEntity(input);
    repository.save(user);
    eventPublisher.publish(new UserCreatedEvent(user));
    return mapper.toOutput(user);
}
```

### ✔ Outro exemplo

```java
public void authenticate(LoginInput input) {
    User user = findUser(input.email());

    validatePassword(user, input.password());

    Token token = tokenProvider.generate(user);

    audit(user);

    return token;
}
```
---

# Ordenação dos Membros

Os membros de uma classe devem seguir uma ordem lógica e consistente.

A ordem recomendada é:

1. Constantes;
2. Campos;
3. Construtor;
4. Métodos públicos;
5. Métodos protegidos;
6. Métodos privados.

Métodos auxiliares devem permanecer próximos dos métodos que os utilizam, mas sempre métodos privados devem ficar em baixo dos públicos.

### ✔ Correto

```java
public class UserService {
    private static final int MAX_ATTEMPTS = 3;

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User findById(UUID id) {
        validate(id);

        return repository.findById(id);
    }

    private void validate(UUID id) {
        ...
    }
}
```

---

# Organização dos Métodos

Cada método deve possuir apenas uma responsabilidade.

Sempre que possível:

- Métodos públicos devem representar operações de alto nível.
- Detalhes de implementação devem ser extraídos para métodos privados.
- Métodos devem ser pequenos e objetivos.
- Um método não deve executar múltiplas responsabilidades.

### ✔ Correto

```java
public User execute(CreateUserInput input) {
    validate(input);

    User user = createUser(input);

    save(user);

    return user;
}

private void validate(CreateUserInput input) {
    ...
}

private User createUser(CreateUserInput input) {
    ...
}

private void save(User user) {
    ...
}
```

### ❌ Incorreto

```java
public User execute(CreateUserInput input) {

    // 150 linhas contendo:
    // validação
    // criação
    // persistência
    // envio de e-mail
    // auditoria
    // logging
}
```

---


# Injeção de Dependência

Toda dependência entre componentes deve ser realizada por meio de injeção de dependência.

As seguintes regras devem ser respeitadas:

- A injeção deve ocorrer exclusivamente pelo construtor.
- Dependências obrigatórias devem ser imutáveis (`final`).
- Instanciações diretas (`new`) devem ser evitadas para componentes gerenciados pela aplicação.
- Dependências devem ser representadas por contratos sempre que possível.

### ✔ Correto

```java
@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCaseImpl(
            UserRepository repository,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }
}
```

### ❌ Incorreto

```java
@Service
public class CreateUserUseCaseImpl {
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;
}
```

### ❌ Incorreto

```java
public class CreateUserUseCaseImpl {
    private final UserRepository repository = new UserRepositoryImpl();
}
```

---


# Logging

Logs devem registrar informações relevantes para auditoria, monitoramento e diagnóstico da aplicação.

As seguintes práticas devem ser adotadas:

- Registrar apenas eventos relevantes para o funcionamento da aplicação.
- Utilizar o nível de log apropriado (`TRACE`, `DEBUG`, `INFO`, `WARN` ou `ERROR`).
- Utilizar mensagens objetivas e descritivas.
- Nunca registrar dados sensíveis, como senhas, tokens, chaves ou informações pessoais.
- Nunca utilizar logs como mecanismo de controle de fluxo.
- Nunca adicionar mensagens redundantes ou duplicadas.
- Nunca registrar identificadores de rastreamento (Trace ID, Span ID, Request ID, etc.), pois essas informações já são propagadas automaticamente pelo Baggage Field.
- Nunca registrar a mesma exceção em múltiplas camadas da aplicação.
- Sempre utilizar logs parametrizados, evitando concatenação de strings.
- Registrar exceções apenas no ponto em que elas forem efetivamente tratadas ou onde houver contexto relevante a ser acrescentado.
- Logs de operações devem ser realizados na camada de Presentation, registrando as entradas (requests) e saídas (responses) quando apropriado.
- Logs de exceções devem ser realizados exclusivamente pelo Global Exception Handler.
- A única exceção ocorre quando uma exceção de infraestrutura precisa ser traduzida para uma exceção da aplicação ou do domínio. Nesse caso, a exceção original deve ser registrada antes da tradução.
- Utilizar exclusivamente o Logger do SLF4J para registro de logs.
- Não utilizar `System.out`, `System.err` ou `printStackTrace()`.

### ✔ Correto

```java
log.info("Creating user - request: {}", request);
```

```java
log.info("Creating user - response: {}", response);
```

```java
log.error("Resource not found: {}", ex.getMessage(), ex);
```

### ❌ Incorreto

```java
log.info(
    "User {} authenticated using password {}",
    username,
    password
);
```

```java
log.info(
    "User {} authenticated using password {}",
    username,
    password
);
```

```java
log.info("User created: " + username);
```

```java
log.info(
    "RequestId={} TraceId={} User authenticated.",
    requestId,
    traceId
);
```

- A instância do logger sempre deve ser criada via uma constante estática privada.

### Exemplo

```java
@RestController
@RequestMapping("/samples")
public class SampleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
}
```

---

# Configurações

As configurações da aplicação devem permanecer centralizadas e desacopladas da lógica de negócio.

As seguintes regras devem ser respeitadas:

- Configurações devem ser externalizadas.
- Valores configuráveis não devem ser codificados diretamente na aplicação.
- Configurações devem ser agrupadas conforme sua responsabilidade.
- Configurações tipadas devem utilizar `@ConfigurationProperties`.
- Classes de configuração devem representar apenas um grupo específico de propriedades.
- Regras de negócio não devem depender diretamente da origem das configurações.
- Componentes devem acessar apenas as configurações necessárias para sua responsabilidade.
- Classes de configuração sempre devem ter o sufixo `ConfigProperties`.

### ✔ Correto

```java
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        Duration expiration,
        String issuer
) {
}
```

```java
@Service
public class JwtTokenProvider {
    private final JwtProperties properties;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }
}
```

### ❌ Incorreto

```java
public class JwtTokenProvider {
    private static final String ISSUER = "My Application";
    private static final long EXPIRATION = 86400000L;
}
```

### ❌ Incorreto

```java
@Service
public class JwtTokenProvider {
    @Value("${security.jwt.expiration}")
    private long expiration;
    @Value("${security.jwt.issuer}")
    private String issuer;

}
```

# Imports

As instruções de importação devem permanecer organizadas e conter apenas dependências utilizadas pela classe.

As seguintes regras devem ser respeitadas:

- Imports não utilizados devem ser removidos.
- Imports curinga (`*`) não são permitidos.
- A organização dos imports deve seguir o padrão definido pela ferramenta de formatação adotada pelo projeto.

### ✔ Correto

```java
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
```

### ❌ Incorreto

```java
import java.util.*;
```

# Análise Estática

Todo o código deve ser validado por ferramentas de análise estática antes de sua integração ao projeto.

As seguintes regras devem ser respeitadas:

- O projeto deve possuir uma configuração padronizada de análise estática.
- Todo o código deve estar em conformidade com as regras definidas por essa configuração.
- Violações não devem ser ignoradas ou desabilitadas sem justificativa documentada.
- Alterações no conjunto de regras devem ser revisadas e aprovadas pela equipe.

A configuração da ferramenta de análise estática faz parte da arquitetura do projeto e deve ser compartilhada entre todos os desenvolvedores.
