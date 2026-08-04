---
inclusion: fileMatch
fileMatchPattern: "**/src/main/**/external/persistence/**"
---

# Convenções de Persistência

A camada de **Persistência** é responsável por armazenar e recuperar dados da aplicação.

Seu objetivo é adaptar o modelo de domínio ao mecanismo de persistência utilizado, mantendo o domínio completamente desacoplado dos detalhes técnicos.

Nesta arquitetura, a implementação da persistência deve estar obrigatoriamente localizada na camada **External**, por representar um detalhe de infraestrutura e uma implementação concreta das portas definidas pela camada Application.

Este guia define um padrão único para organização da persistência e deve ser seguido por todos os projetos que adotarem esta arquitetura.

O uso do **Java Persistence API (JPA)** é obrigatório para implementação da camada de persistência.

# Estrutura

A organização recomendada é:

```text
infrastructure/
└── persistence/
    ├── dao/
    ├── mapper/
    ├── model/
    └── repository/
```

Cada componente possui uma responsabilidade específica e não deve assumir responsabilidades pertencentes aos demais.

---

# DAO

DAOs representam a implementação de acesso aos dados utilizando o mecanismo de persistência adotado.

Nesta arquitetura, todos os DAOs devem utilizar **Spring Data JPA**.

As seguintes regras devem ser respeitadas:

- Devem ser interfaces.
- Devem estender um repositório do Spring Data.
- Não devem conter regras de negócio.
- Devem trabalhar exclusivamente com Models.
- Não devem ser utilizados diretamente pelas camadas Application ou Presentation.

## ✔ Correto

```java
public interface SampleJpaDaoextends JpaRepository<SampleJpaModel, UUID> {

    Optional<SampleJpaModel> findByName(String name);
}
```

## ❌ Incorreto

```java
public class SampleJpaDao { }
```

---

# Repository

Repositories representam os adaptadores responsáveis por implementar as portas de persistência definidas na camada Application.

Seu papel é converter objetos do domínio em modelos de persistência e delegar as operações ao DAO.

As seguintes regras devem ser respeitadas:

- Devem implementar exclusivamente interfaces da camada Application.
- Devem depender apenas do DAO e dos Mappers.
- Devem trabalhar com Entidades do domínio.
- Não devem implementar regras de negócio.
- Não devem expor Models para outras camadas.

## ✔ Correto

```java
@Repository
public class SampleRepositoryImpl implements SampleRepository {

    private final SampleJpaDao dao;

    public SampleRepositoryImpl(SampleJpaDao dao) {
        this.dao = dao;
    }

    @Override
    public SampleEntity save(SampleEntity entity) {
        var model = SampleMapper.toModel(entity);

        var saved = dao.save(model);

        return SampleMapper.toEntity(saved);
    }
}
```

## ❌ Incorreto

```java
@Repository
public class SampleRepositoryImpl implements SampleRepository {

    @Override
    public SampleJpaModel save(
            SampleEntity entity
    ) {
        ...
    }
}
```

---

# Mapper

Mappers são responsáveis exclusivamente pela conversão entre objetos do domínio e modelos de persistência.

As seguintes regras devem ser respeitadas:

- Não devem possuir regras de negócio.
- Devem realizar apenas conversões.
- Devem converter Domain ↔ Model.
- Devem permanecer stateless.
- Devem possuir métodos estáticos quando não houver necessidade de estado.

## ✔ Correto

```java
public final class SampleMapper {

    private SampleMapper() { }

    public static SampleJpaModel toModel(SampleEntity entity) {
        return new SampleJpaModel(
                entity.getId().value(),
                entity.getName().value()
        );
    }

    public static SampleEntity toEntity(SampleJpaModel model) {
        return new SampleEntity(
                model.getId(),
                model.getName()
        );
    }
}
```

## ❌ Incorreto

```java
public class SampleMapper {

    public SampleEntity toEntity(SampleJpaModel model) {
        repository.save(...);
    }
}
```

---

# Model

Models representam exclusivamente a estrutura persistida no banco de dados.

Eles não representam objetos do domínio.

As seguintes regras devem ser respeitadas:

- Devem utilizar anotações JPA.
- Devem representar apenas a estrutura do banco.
- Não devem conter regras de negócio.
- Não devem ser utilizados fora da camada de persistência.
- Não devem ser expostos para Application ou Presentation.

## ✔ Correto

```java
@Entity
@Table(name = "samples")
public class SampleJpaModel {

    @Id
    private String id;
    @Column(nullable = false)
    private String name;
}
```

## ❌ Incorreto

```java
@Entity
public class SampleJpaModel {

    public void activate() {
        ...
    }
}
```

```java
@Entity
public class SampleJpaModel {

    public boolean canExecuteBusinessRule() {
        ...
    }
}
```

---

# Queries

Consultas são operações responsáveis exclusivamente pela recuperação de dados.

Seu objetivo é fornecer as informações necessárias para a camada Application, sem implementar regras de negócio.

As seguintes regras devem ser respeitadas:

- Consultas simples devem ser implementadas diretamente nos DAOs através dos recursos do Spring Data JPA.
- Consultas derivadas pelo nome do método devem ser priorizadas sempre que atenderem ao requisito.
- Consultas mais complexas podem utilizar `@Query`, `Specification` ou `Criteria API`, conforme o padrão adotado pelo projeto.
- Consultas nativas (`nativeQuery`) devem ser utilizadas apenas quando não houver alternativa viável utilizando JPA.
- Toda consulta deve retornar Models da camada de persistência.
- Conversões para objetos do domínio devem ocorrer exclusivamente no Repository através dos Mappers.
- Consultas não devem implementar regras de negócio.
- Consultas não devem ser executadas diretamente pela camada Application.

## ✔ Correto

```java
public interface SampleJpaDaoextends JpaRepository<SampleJpaModel,String> {

    Optional<SampleJpaModel> findByName(
            String name
    );

    boolean existsByName(
            String name
    );
}
```

```java
public interface SampleJpaDaoextends JpaRepository<SampleJpaModel, String> {

    @Query("""
        select s
        from SampleJpaModel s
        where s.createdAt >= :createdAt
    """)
    List<SampleJpaModel> findAllCreatedAfter(
            Instant createdAt
    );
}
```

## ✔ Correto

```java
@Override
public Optional<SampleEntity> findByName(
        SampleNameValueObject name
) {

    return dao.findByName(name.value())
            .map(SampleMapper::toEntity);
}
```

## ❌ Incorreto

```java
@Service
public class CreateSampleUseCaseImpl implements CreateSampleUseCase {

    @PersistenceContext
    private EntityManager entityManager;
}
```

# Fluxo de Persistência

Toda operação de persistência deve seguir o fluxo abaixo:

```text
Application
      ↓
Repository (Adapter)
      ↓
Mapper
      ↓
DAO
      ↓
JPA
      ↓
Banco de Dados
```

O fluxo inverso deve seguir o mesmo princípio:

```text
Banco de Dados
      ↓
JPA
      ↓
DAO
      ↓
Mapper
      ↓
Repository
      ↓
Application
```

---

# Banco de Dados

A camada de persistência deve abstrair completamente a tecnologia utilizada pelo banco de dados.

As seguintes regras devem ser respeitadas:

- Nenhuma camada fora da Infrastructure deve conhecer detalhes do banco.
- Consultas devem permanecer encapsuladas nos DAOs.
- Toda comunicação deve ocorrer através dos Repositories.
- O banco de dados nunca deve ser acessado diretamente por casos de uso.

---

# Migrações

Toda alteração estrutural do banco de dados deve ser realizada através de ferramentas de migração.

As seguintes regras devem ser respeitadas:

- Alterações manuais no banco não são permitidas.
- Scripts devem ser versionados.
- Migrações devem ser executadas automaticamente durante o processo de implantação.
- Cada migração deve representar uma única alteração estrutural.

---

# IDs

A geração e persistência de identificadores deve respeitar as regras do domínio.

As seguintes regras devem ser respeitadas:

- IDs do domínio devem permanecer encapsulados em Value Objects.
- Models devem armazenar apenas o valor persistido.
- Conversões entre Value Objects e tipos persistidos devem ocorrer exclusivamente nos Mappers.

## ✔ Correto

```java
public static SampleJpaModel toModel(SampleEntity entity) {

    return new SampleJpaModel(
            entity.getId().value(),
            entity.getName().value()
    );
}
```

## ❌ Incorreto

```java
@Entity
public class SampleJpaModel {

    @Embedded
    private SampleIdValueObject id;
}
```

---

# Dependências

A camada de Persistência pode depender de:

- Domain;
- Application;
- Spring Data JPA;
- JPA;
- Hibernate.

Não é permitido depender de:

- Presentation.

---

# Resumo das Convenções

Toda implementação da camada de Persistência deve respeitar os seguintes princípios:

- O uso de **JPA** é obrigatório.
- DAOs devem utilizar Spring Data JPA.
- Repositories implementam exclusivamente portas da camada Application.
- Models representam apenas a estrutura persistida.
- Mappers realizam exclusivamente conversões entre Domain e Model.
- Nenhuma regra de negócio deve ser implementada nesta camada.
- Models nunca devem ser expostos para fora da Infrastructure.
- Toda persistência deve passar pelo fluxo **Repository → Mapper → DAO → JPA → Banco de Dados**.
