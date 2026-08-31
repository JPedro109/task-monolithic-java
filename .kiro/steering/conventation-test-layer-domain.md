---
inclusion: fileMatch
fileMatchPattern: "**/src/test/**/domain/**"
---

# Testes Unitários de Value Objects

Os testes de **Value Objects** têm como objetivo garantir que todas as regras de validação e invariantes do objeto sejam respeitadas.

Cada Value Object deve possuir uma classe de teste dedicada, validando tanto cenários válidos quanto inválidos.

---

## Estrutura

Os testes devem permanecer na mesma organização lógica do Value Object.

Exemplo:

```text
src/test/java/
└── domain/
    └── sample/
        └── valueobject/
            ├── SampleNameValueObjectTest.java
            └── SampleValueObjectTest.java
```

---

## Convenções

As seguintes regras devem ser respeitadas:

- Cada Value Object deve possuir sua própria classe de teste.
- Todos os métodos públicos devem ser testados.
- Devem ser testados cenários de sucesso e de falha.
- Testes devem validar o objeto retornado e não apenas a ausência de exceções.
- Quando o Value Object utilizar `Result<T>`, devem ser testados tanto `Result.ok(...)` quanto `Result.fail(...)`.
- Fixtures devem ser utilizadas sempre que houver reutilização de dados de teste.

---

## Cenários obrigatórios

Todo Value Object deve possuir, no mínimo, testes para:

- Criação com dados válidos.
- Criação com dados inválidos.
- Valores nulos (quando aplicável).
- Valores vazios (quando aplicável).
- Valores fora dos limites definidos.
- Valores exatamente nos limites permitidos.

---

## ✔ Correto

```java
class SampleNameValueObjectTest {

    @Test
    @DisplayName("Should create a valid SampleNameValueObject")
    void shouldCreateValueObjectWhenValueIsValid() {
        var sampleName = "Sample";

        var result = SampleNameValueObject.of(sampleName);

        assertTrue(result.isSuccess());
        assertEquals(sampleName, result.getValue().value());
    }

    @ParameterizedTest
    @DisplayName("Should fail for null, empty, too short, too long or invalid character sample names")
    @NullAndEmptySource
    void shouldFailForInvalidSampleName() {
        var invalidSampleName = "";

        var result = SampleNameValueObject.of(invalidSampleName);

        assertTrue(result.isFail());
        assertInstanceOf(DomainException.class, result.getError());
    }

}
```

---

## ✔ Correto

```java
@Test
@DisplayName("Should fail for null to SampleValueObject")
void shouldReturnFailureWhenValueIsNull() {
    var sampleName = "Sample";

    var result = SampleNameValueObject.of(sampleName);

    assertTrue(result.isFail());

}
```

---

## ❌ Incorreto

```java
@Test
void shouldCreate() {
    SampleNameValueObject.of("Sample");
}
```

---

## ❌ Incorreto

```java
@Test
void shouldThrowException() {
    assertThrows(Exception.class, () -> SampleNameValueObject.of(""));
}
```

---

## Boas práticas

Sempre que possível:

- Um cenário por teste.
- Utilizar nomes descritivos para os métodos de teste.
- Validar explicitamente o conteúdo do `Result`.
- Evitar múltiplos cenários no mesmo teste.
- Evitar dependência entre testes.
- Manter os testes rápidos e determinísticos.

---

## Resumo das Convenções

Todo teste de Value Object deve respeitar os seguintes princípios:

- Cada Value Object possui sua própria classe de teste.
- Cenários válidos e inválidos devem ser testados.
- Todo `Result.ok(...)` e `Result.fail(...)` deve ser validado.
- Um cenário por teste.
- Testes devem validar explicitamente o resultado obtido.
- Fixtures devem ser utilizadas quando houver reutilização de dados.

# Testes Unitários de Entidades

Os testes de **Entidades** têm como objetivo garantir que as regras de negócio implementadas na camada Domain sejam executadas corretamente.

Cada entidade deve possuir uma classe de teste dedicada, validando a criação do objeto, suas invariantes e todos os comportamentos expostos publicamente.

As entidades devem ser testadas isoladamente, sem dependências de infraestrutura, banco de dados ou contexto do Spring.

---

## Estrutura

Os testes devem permanecer organizados conforme a entidade.

Exemplo:

```text
src/test/java/
└── domain/
    └── sample/
        ├── SampleEntityTest.java
        └── fixture/
```

---

## Convenções

As seguintes regras devem ser respeitadas:

- Cada entidade deve possuir sua própria classe de teste.
- Todos os métodos públicos devem ser testados.
- Todos os comportamentos da entidade devem ser validados.
- Toda regra de negócio implementada na entidade deve possuir testes.
- Utilizar Fixtures para construção dos objetos.
- Nunca instanciar entidades diretamente nos testes.
- Os testes devem utilizar AssertJ para todas as asserções.
- Cada teste deve validar um único cenário.
- Os testes devem seguir o padrão AAA (Arrange → Act → Assert).
- Todo método deve possuir `@DisplayName`.

---

## Cenários obrigatórios

Toda entidade deve possuir, no mínimo, testes para:

- Criação com dados válidos.
- Criação com dados inválidos.
- Execução dos comportamentos públicos.
- Alteração de estado.
- Validação das invariantes.
- Exceções lançadas pela entidade.

---

## ✔ Correto

```java
class SampleEntityTest {

    @Test
    @DisplayName("Should create sample when data is valid")
    void shouldCreateSampleWhenDataIsValid() {
        var id = "1";
        var name = "1";
        var value = "1";

        var sample = new SampleEntity(id, name, value);

        var idValueObject = sample.getId();
        var nameValueObject = sample.getName();
        var valueValueObject = sample.getValue();
        assertThat(idValueObject).isNotNull();
        assertThat(idValueObject.asString()).isEqualTo("1");
        assertThat(nameValueObject.asString()).isEqualTo("Sample");
        assertThat(valueValueObject.asString()).isEqualTo("Value");
    }
}
```

---

## ✔ Correto

```java
@Test
@DisplayName("Should update value when new value is valid")
void shouldUpdateValueWhenNewValueIsValid() {
    var sample = SampleEntityFixture.aSample();
    var newValue = SampleValueObject.of("Updated Value").getValueOrThrow();

    sample.updateValue(newValue);

    assertThat(sample.getValue()).isEqualTo(newValue);
}
```

---

## ✔ Correto

```java
@Test
@DisplayName("Should throw when value is invalid")
void shouldThrowWhenValueIsInvalid() {
    assertThatThrownBy(() ->
            new SampleEntity(
                    "00000000-0000-0000-0000-000000000001",
                    "Sample",
                    ""
            )
    ).isInstanceOf(DomainException.class);
}
```

---

## ❌ Incorreto

```java
@Test
@DisplayName("Create sample")
void shouldCreateSample() {

    var sample = new SampleEntity(
            "1",
            "Sample",
            "Value"
    );
}
```

---

## ❌ Incorreto

```java
@Test
@DisplayName("Create sample")
void shouldCreateSample() {
    SampleEntityFixture.aSample();
}
```

---

## ❌ Incorreto

```java
@SpringBootTest
class SampleEntityTest { }
```

---

## Boas práticas

Sempre que possível:

- Reutilizar Fixtures.
- Validar explicitamente o estado da entidade.
- Testar apenas comportamentos públicos.
- Não acessar atributos privados por reflexão.
- Evitar múltiplos cenários no mesmo teste.
- Utilizar nomes descritivos para os métodos de teste.
- Manter os testes independentes entre si.

---

## Resumo das Convenções

Todo teste de Entidade deve respeitar os seguintes princípios:

- Cada entidade possui sua própria classe de teste.
- Apenas a camada Domain deve ser testada.
- Utilizar Fixtures para criação dos objetos.
- Nunca instanciar entidades diretamente.
- Utilizar AssertJ para todas as validações.
- Todo método deve possuir `@DisplayName`.
- Um cenário por teste.
- Todos os comportamentos públicos devem ser cobertos.
- Os testes devem ser rápidos, determinísticos e independentes.
