---
inclusion: fileMatch
fileMatchPattern: "**/src/test/**/persistence/**"
---

# Testes de Persistência

Os testes da camada de **Persistência** têm como objetivo garantir que os componentes responsáveis pelo armazenamento, recuperação e conversão de dados funcionem corretamente.

Nesta arquitetura, a camada de persistência é composta por três componentes principais:

- DAO;
- Mapper;
- Repository.

Cada componente possui responsabilidades distintas e, consequentemente, estratégias de teste diferentes.

- **Mappers** devem ser testados através de **testes unitários**, por não possuírem dependências externas.
- **DAOs** devem ser testados através de **testes de integração com `@DataJpaTest`**, pois dependem do JPA e de um banco de dados.
- **Repositories** devem ser testados através de **testes unitários com Mockito**, validando a orquestração entre DAO e Mapper de forma isolada.

---

## Estrutura

Os testes devem permanecer organizados conforme o componente testado.

Exemplo:

```text
src/test/java/
└── external/
    └── persistence/
        ├── dao/
        │   └── SampleJpaDaoTest.java
        ├── mapper/
        │   └── SampleMapperTest.java
        └── repository/
            └── SampleRepositoryImplTest.java
```

---

## Convenções

As seguintes regras devem ser respeitadas:

- Todo DAO deve possuir testes com `@DataJpaTest`.
- Todo Mapper deve possuir testes unitários.
- Todo Repository deve possuir testes unitários com Mockito.
- Cada classe deve testar exclusivamente um componente.
- Fixtures devem ser utilizadas para construção dos objetos.
- Os testes devem utilizar AssertJ.
- Os testes devem ser organizados em cenários de sucesso, corner cases e exceções.

---

## Testes de DAO com @DataJpaTest

Os testes de DAO têm como objetivo validar o comportamento das consultas realizadas pelo Spring Data JPA.

Devem garantir que consultas, filtros e operações de persistência funcionem corretamente utilizando `@DataJpaTest`, que configura automaticamente um banco de dados embarcado para testes.

## Convenções

- Devem utilizar `@DataJpaTest`.
- Devem injetar o DAO com `@Autowired`.
- Não devem herdar de `IntegrationTestBase`.
- Devem validar métodos derivados do Spring Data.
- Devem validar consultas utilizando `@Query`.
- Devem validar operações de persistência.
- Devem validar operações de atualização.
- Devem validar operações de remoção.
- Não devem testar regras de negócio.

### Cenários obrigatórios

- Buscar por ID.
- Buscar por atributos.
- Exists.
- Save.
- Delete.
- Consultas customizadas.

## ✔ Correto

```java
@DataJpaTest
@DisplayName("SampleJpaDao Tests")
class SampleJpaDaoTest {

    @Autowired
    private SampleJpaDao dao;

    @Test
    @DisplayName("Should find sample by name")
    void shouldFindSampleByName() {
        var model = dao.findByName("Sample");

        assertThat(model)
                .isPresent();
    }
}
```

---

## Testes Unitários de Mapper

Os testes de Mapper têm como objetivo garantir a correta conversão entre Domain e Persistência.

Nenhum Mapper deve possuir lógica além da conversão dos objetos.

## Convenções

- Não devem utilizar Spring.
- Não devem utilizar banco de dados.
- Devem validar conversão Domain → Model.
- Devem validar conversão Model → Domain.
- Devem validar todos os atributos.
- Devem validar conversão de IDs.
- Devem validar conversão de Value Objects.

### Cenários obrigatórios

- Conversão para Model.
- Conversão para Entity.
- Conversão de IDs.
- Conversão de Value Objects.

## ✔ Correto

```java
class SampleMapperTest {

    @Test
    @DisplayName("Should convert entity to model")
    void shouldConvertEntityToModel() {
        var entity = SampleEntityFixture.aSample();

        var model = SampleMapper.toModel(entity);

        assertThat(model.getId()).isEqualTo(entity.getId().value());
        assertThat(model.getName()).isEqualTo(entity.getName().value());
    }
}
```

---

## Testes Unitários de Repository

Os testes de Repository têm como objetivo garantir que o adaptador orquestre corretamente as conversões (Mapper) e delegações (DAO).

Devem validar que o Repository converte entidades do domínio para models, delega ao DAO e retorna o resultado convertido de volta para o domínio.

## Convenções

- Devem utilizar `@ExtendWith(MockitoExtension.class)`.
- Devem simular o DAO utilizando `@Mock`.
- Devem instanciar o Repository com `@InjectMocks`.
- Não devem utilizar contexto Spring.
- Não devem utilizar banco de dados.
- Devem validar operações de persistência (delegação ao DAO).
- Devem validar operações de consulta (delegação ao DAO).
- Devem validar que os resultados retornados foram corretamente convertidos pelo Mapper.
- Devem validar a implementação da porta da Application.
- Não devem testar regras de negócio.

### Cenários obrigatórios

- Save.
- FindById.
- FindAll.
- Exists.
- Delete.
- Retorno vazio (Optional.empty / lista vazia).

## ✔ Correto

```java
@ExtendWith(MockitoExtension.class)
class SampleRepositoryAdapterTest {

    @Mock
    private SampleJpaDao jpaRepository;

    @InjectMocks
    private SampleRepositoryAdapter adapter;

    @Test
    @DisplayName("Should save sample and return the persisted domain entity")
    void shouldSaveSample() {
        var sample = SampleEntityFixture.aSample();
        var model = buildSampleModel();

        when(jpaRepository.save(any())).thenReturn(model);

        var result = adapter.save(sample);

        assertThat(result).isNotNull();
        assertThat(result.getId().asString()).isEqualTo(sample.getId().asString());
        verify(jpaRepository).save(any());
    }
}
```

---

## Boas práticas

Sempre que possível:

- Utilizar Fixtures.
- Validar todos os atributos convertidos.
- Validar os cenários positivos e negativos.
- Um cenário por teste.
- Reutilizar Seeds quando necessário.
- Evitar dependência entre testes.
- Validar apenas a responsabilidade do componente em teste.

---

## Resumo das Convenções

Toda implementação da camada de Persistência deve respeitar os seguintes princípios:

- Todo DAO deve possuir testes com `@DataJpaTest`.
- Todo Mapper deve possuir testes unitários.
- Todo Repository deve possuir testes unitários com Mockito.
- DAOs devem utilizar `@DataJpaTest` e `@Autowired`.
- Repositories devem utilizar `@ExtendWith(MockitoExtension.class)`, `@Mock` e `@InjectMocks`.
- Mappers devem ser testados isoladamente.
- Utilizar Fixtures para criação dos objetos.
- Utilizar AssertJ para todas as validações.
- Todo método deve possuir `@DisplayName`.
- Os testes devem ser rápidos, determinísticos e independentes.
