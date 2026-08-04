---
inclusion: fileMatch
fileMatchPattern: "**/src/main/**/application/**"
---

# Convenções da Camada Application

A camada **Application** é responsável por orquestrar os casos de uso da aplicação.

Ela coordena a execução das regras de negócio da camada Domain e realiza validações ou decisões que dependam de recursos externos.

Regras que possam ser implementadas exclusivamente pelo domínio não devem ser implementadas nesta camada.

---

# Estrutura

A organização da camada Application deve refletir os casos de uso da aplicação.

A estrutura recomendada é:

```text
application/
├── port/
│   ├── persistence/
│   │   └── repository/
│   └── security/
└── usecase/
    └── sample/
        ├── interfaces/
        ├── implementation/
        ├── dto/
        │   ├── input/
        │   └── output/
        └── exception/
```

As seguintes regras devem ser respeitadas:

- Cada contexto deve possuir seu próprio pacote.
- Cada caso de uso deve possuir sua própria interface.
- Implementações devem permanecer em `implementation`.
- DTOs devem permanecer separados entre entrada e saída.
- Portas devem permanecer organizadas por responsabilidade.

---

# Casos de Uso

Casos de uso representam operações executadas pela aplicação.

Cada caso de uso deve representar apenas uma responsabilidade.

As seguintes regras devem ser respeitadas:

- Todo caso de uso deve possuir uma interface.
- Toda implementação deve implementar sua respectiva interface.
- Implementações devem utilizar o sufixo `Impl`.
- Cada caso de uso deve executar apenas um fluxo da aplicação.
- Casos de uso não devem conter regras de infraestrutura.
- Casos de uso não devem depender da camada Presentation.

## ✔ Correto

```java
public interface CreateSampleUseCase {
    CreateSampleOutput execute(CreateSampleInput input);
}
```

```java
@Service
public class CreateSampleUseCaseImpl implements CreateSampleUseCase {
    ...
}
```

## ❌ Incorreto

```java
@Service
public class SampleService {
    public void create(...) {}
    public void update(...) {}
    public void delete(...) {}
}
```

---

# DTOs

DTOs representam exclusivamente os dados trafegados entre camadas.

Não representam regras de negócio.

As seguintes regras devem ser respeitadas:

- Devem ser imutáveis.
- Devem utilizar `record`.
- Não devem possuir validações.
- Não devem possuir regras de negócio.
- Não devem possuir dependência de frameworks.

## ✔ Correto

```java
public record CreateSampleInput(
        String name,
        String value
) { }
```

```java
public record CreateSampleOutput(
        String id,
        String name
) { }
```

## ❌ Incorreto

```java
public class CreateSampleInput {
    @NotBlank
    private String name;
}
```

---

# Portas

Portas representam contratos entre a camada Application e componentes externos.

Toda comunicação com infraestrutura deve ocorrer através de portas.

As seguintes regras devem ser respeitadas:

- Portas devem ser interfaces.
- Portas devem representar comportamento.
- Não devem possuir implementação.
- Devem permanecer organizadas por responsabilidade.

## ✔ Correto

```java
public interface SampleRepository {
    void save(SampleEntity entity);
}
```

```java
public interface PasswordEncoder {
    String encode(String value);
}
```

## ❌ Incorreto

```java
public class SampleRepository { }
```

---

# Serviços

A camada Application não deve possuir serviços genéricos.

O comportamento deve ser representado através de casos de uso.

Caso um componente seja reutilizado por múltiplos casos de uso, ele deve possuir uma responsabilidade claramente definida.

## ✔ Correto

```java
@Service
public class CreateSampleUseCaseImpl implements CreateSampleUseCase {
    ...
}
```

## ❌ Incorreto

```java
@Service
public class SampleService {
    public void create(){}
    public void update(){}
    public void delete(){}
}
```

---

# Fluxo de Dados

A camada Application é responsável por coordenar o fluxo entre as portas e o domínio.

O fluxo recomendado é:

```text
Input DTO
      ↓
Caso de Uso
      ↓
Value Objects
      ↓
Entidade
      ↓
Porta
      ↓
Output DTO
```

As seguintes regras devem ser respeitadas:

- O caso de uso recebe um DTO de entrada.
- O caso de uso cria os Value Objects necessários.
- O caso de uso instancia a entidade.
- O caso de uso utiliza apenas portas para acessar infraestrutura.
- O retorno deve ocorrer através de um DTO de saída.

## ✔ Correto

```java
@Override
public CreateSampleOutput execute(
        CreateSampleInput input
) {
    var entity = new SampleEntity(
            UUID.randomUUID().toString(),
            input.name(),
            input.value()
    );

    repository.save(entity);

    return new CreateSampleOutput(
            entity.getId().asString(),
            entity.getName().asString()
    );
}
```

---

# Conversões

A camada Application é responsável pela conversão entre DTOs e objetos do domínio.

As seguintes regras devem ser respeitadas:

- DTOs nunca devem atravessar para a camada Domain.
- Entidades nunca devem ser utilizadas diretamente pela camada Presentation.
- Conversões devem permanecer centralizadas no caso de uso ou em componentes específicos de mapeamento quando houver reutilização.

## ✔ Correto

```java
var entity = new SampleEntity(input.id(), input.name(), input.value());
```

```java
return new CreateSampleOutput(entity.getId().value(), entity.getName().value());
```

## ❌ Incorreto

```java
repository.save(input);
```

```java
return entity;
```

---

# Dependências

A camada Application pode depender apenas da camada Domain e de contratos definidos pela própria Application.

Não é permitido depender de:

- Controllers;
- Requests;
- Responses;
- Models de persistência;
- Repositórios concretos;
- Frameworks de persistência;
- Componentes da camada Infrastructure.

---

# Resumo das Convenções

Toda implementação da camada Application deve respeitar os seguintes princípios:

- Cada caso de uso representa uma única operação da aplicação.
- Todo caso de uso deve possuir interface e implementação.
- Implementações devem utilizar o sufixo `Impl`.
- DTOs devem utilizar `record`.
- DTOs não possuem regras de negócio.
- Toda comunicação externa deve ocorrer através de portas.
- A camada Application coordena o fluxo da aplicação, executa validações que dependam de recursos externos e orquestra a execução das regras de negócio da camada Domain.
- Entidades pertencem ao Domain.
- Componentes da Infrastructure nunca devem ser acessados diretamente.
- O retorno dos casos de uso deve ocorrer através de DTOs de saída.
