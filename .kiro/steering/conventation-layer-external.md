---
inclusion: fileMatch
fileMatchPattern: "**/src/main/**/external/**"
---

# Convenções da Camada External

A camada **External** é responsável por implementar todas as integrações com tecnologias externas utilizadas pela aplicação.

Seu objetivo é isolar detalhes de infraestrutura da camada de domínio, mantendo a arquitetura desacoplada e seguindo o princípio da inversão de dependência.

Toda dependência de frameworks, bibliotecas, serviços externos ou infraestrutura deve permanecer exclusivamente nesta camada.

---

# Responsabilidades

A camada External é responsável por:

- Implementar portas definidas pela camada Application.
- Integrar com bibliotecas externas.
- Integrar com serviços externos.
- Integrar com mecanismos de autenticação e autorização.
- Integrar com serviços de mensageria.
- Integrar com serviços de armazenamento.
- Integrar com serviços de cache.
- Adaptar modelos da aplicação para tecnologias externas.

A camada External nunca deve implementar regras de negócio.

---

# Fluxo de Dependências

A camada External depende da camada Application para implementar suas portas.

```text
Presentation
        ↓
Application
        ↑
External
        ↓
Frameworks / Banco / APIs / Bibliotecas
```

A camada Domain nunca possui dependência da camada External.

---

# Adaptadores

Todo componente da camada External deve representar um adaptador entre a aplicação e uma tecnologia externa.

Sempre que uma nova integração for necessária, o fluxo deve ser:

1. Definir a porta na camada Application.
2. Implementar a porta na camada External.
3. Registrar o adaptador como Bean do Spring.
4. Criar testes para o adaptador.

## ✔ Correto

```java
public interface SampleGateway {

    SampleResponse send(SampleRequest request);
}
```

```java
@Component
public class SampleGatewayAdapter implements SampleGateway {

    @Override
    public SampleResponse send(SampleRequest request) { }
}
```

---

# Organização

Os componentes devem ser organizados conforme sua responsabilidade.

Exemplo:

```text
external/
├── persistence/
├── security/
├── messaging/
├── storage/
├── cache/
├── client/
└── configuration/
```

Novos módulos podem ser criados sempre que uma nova responsabilidade surgir.

---

# Configurações

Toda configuração relacionada às tecnologias externas deve permanecer nesta camada.

As seguintes regras devem ser respeitadas:

- Nunca acessar propriedades diretamente.
- Utilizar `@ConfigurationProperties`.
- Centralizar configurações por responsabilidade.
- Não implementar regras de negócio.

## ✔ Correto

```java
@ConfigurationProperties(prefix = "sample.client")
public record SampleClientProperties(
        String url,
        Duration timeout
) { }
```

---

# Exceções

Toda exceção proveniente de bibliotecas externas e que tem valor para as regras de negócio do domínio deve ser traduzida antes de atravessar os limites da camada External as demais exceções não precisam ser traduzidas.

A camada Domain nunca deve conhecer exceções de bibliotecas ou frameworks.

## ✔ Correto

```java
try {
    client.send(request);
} catch (ExternalClientException ex) {
    throw new SampleUnavailableException();
}
```

---

# Resumo das Convenções

- Toda integração externa pertence à camada External.
- Todo adaptador deve implementar uma porta da camada Application.
- A camada External nunca implementa regras de negócio.
- Toda exceção de infraestrutura deve ser traduzida.
- Configurações devem utilizar `@ConfigurationProperties`.
- Cada responsabilidade deve possuir seu próprio pacote.
- Toda implementação de persistência deve seguir o documento de Convenções de Persistência.
