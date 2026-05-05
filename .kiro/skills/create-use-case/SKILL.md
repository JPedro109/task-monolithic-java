---
name: create-use-case
description: Gera um novo caso de uso completo seguindo o padrão estrito da aplicação (interface, DTOs, implementação e testes unitários)
inclusion: manual
---

# Skill: Criar Novo Caso de Uso

Quando ativada, esta skill guia a criação de um caso de uso completo seguindo o padrão Clean Architecture do projeto.

## O que será criado

Para um caso de uso `{Domain}/{UseCaseName}`, os seguintes arquivos devem ser gerados:

```
src/main/java/com/jpmns/task/core/application/usecase/{domain}/
├── interfaces/
│   └── {UseCaseName}UseCase.java
├── dto/
│   ├── input/
│   │   └── {UseCaseName}InputDTO.java
│   └── output/
│       └── {OutputName}OutputDTO.java          ← só se ainda não existir para o domínio
├── exception/
│   └── {SomeException}.java                    ← só se o caso de uso precisar de novas exceções
└── implementation/
    └── {UseCaseName}UseCaseImpl.java

src/test/java/com/jpmns/task/core/application/usecase/{domain}/
└── {UseCaseName}UseCaseTest.java
```

---

## Regras obrigatórias

### Geral
- Nunca criar controller, payload HTTP ou endpoint — casos de uso são independentes da camada de apresentação.
- Nunca importar nada de `external/` ou `presentation/` nas camadas `domain` ou `application`.
- Nunca usar `System.out`, `System.err` ou `printStackTrace()`. Usar SLF4J `Logger` quando necessário.
- Indentação: 4 espaços. Linha máxima: 120 caracteres. Chaves sempre obrigatórias, abertura na mesma linha.
- Imports: estáticos primeiro, depois agrupados `java → javax → jakarta → org → com`, ordenados alfabeticamente, sem wildcards.

### Interface
- Fica em `usecase/{domain}/interfaces/`.
- Declara apenas o método `execute({Input}InputDTO input)` com o tipo de retorno adequado.
- Sem anotações Spring.

```java
package com.jpmns.task.core.application.usecase.task.interfaces;

import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO;

public interface CreateTaskUseCase {

    TaskOutputDTO execute(CreateTaskInputDTO input);
}
```

### DTOs de Input
- São `record`s em `usecase/{domain}/dto/input/`.
- Todos os campos são `String` (ou tipos primitivos simples) — nunca value objects ou entidades de domínio.
- Sem anotações de validação Bean Validation nos DTOs de input do use case (validação ocorre dentro da implementação via value objects).

```java
package com.jpmns.task.core.application.usecase.task.dto.input;

public record CreateTaskInputDTO(String userId, String taskName) { }
```

### DTOs de Output
- São `record`s em `usecase/{domain}/dto/output/`.
- Reutilize o output DTO existente do domínio se já houver um adequado (ex: `TaskOutputDTO` para qualquer use case de task).
- Só crie um novo se o retorno for estruturalmente diferente.

```java
package com.jpmns.task.core.application.usecase.task.dto.output;

import java.time.Instant;

public record TaskOutputDTO(String id, String userId, String taskName, boolean finished, Instant createdAt) { }
```

### Implementação
- Fica em `usecase/{domain}/implementation/`, anotada com `@Service`.
- Implementa a interface do use case.
- Recebe dependências (repositórios, ports) via construtor — nunca `@Autowired` em campo.
- Valida IDs e value objects usando `IdValueObject.of(...)` e o respectivo `ValueObject.of(...)`, verificando `isFail()` e lançando a exceção de domínio se inválido.
- Converte entidade para output DTO em um método privado `toOutput(Entity entity)`.
- Nunca expõe entidades de domínio fora da implementação.

```java
package com.jpmns.task.core.application.usecase.task.implementation;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO;
import com.jpmns.task.core.application.usecase.task.interfaces.CreateTaskUseCase;
import com.jpmns.task.core.domain.task.TaskEntity;

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

### Exceções de domínio do use case
- Ficam em `usecase/{domain}/exception/`.
- Estendem a exceção base adequada do projeto (ex: `DomainException` ou `RuntimeException` conforme o padrão existente).
- Consulte as exceções já existentes (`TaskNotFoundException`, `TaskAccessDeniedException`) antes de criar novas.

### Testes unitários
- Ficam em `src/test/java/com/jpmns/task/core/application/usecase/{domain}/`.
- Usam `@ExtendWith(MockitoExtension.class)` — sem contexto Spring.
- Dependências são `@Mock`; a implementação é `@InjectMocks`.
- Usam os fixtures existentes (`TaskFixture`, `UserFixture`) para construir dados de teste — nunca instanciar entidades inline nos testes.
- Cada método de teste tem `@DisplayName` descritivo em inglês.
- Cobrir obrigatoriamente:
  - Cenário de sucesso (happy path)
  - Cenário de entidade não encontrada (quando aplicável)
  - Cenário de acesso negado / ownership (quando aplicável)
  - Cenário de input inválido (quando aplicável)
- Usar `assertThat` do AssertJ e `assertThatThrownBy` para exceções.
- Verificar interações com `verify(mock).method(...)` e `verify(mock, never()).method(...)`.

```java
package com.jpmns.task.core.application.usecase.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.UpdateTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.application.usecase.task.implementation.UpdateTaskUseCaseImpl;
import com.jpmns.task.core.fixture.TaskFixture;
import com.jpmns.task.core.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UpdateTaskUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private UpdateTaskUseCaseImpl useCase;

    @Test
    @DisplayName("Should update the task name successfully")
    void shouldUpdateTaskNameSuccessfully() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var userId = task.getUserId();
        var taskName = "Updated task name";
        var updatedTask = TaskFixture.aTaskWithName(taskName);
        var input = new UpdateTaskInputDTO(taskId.asString(), userId.asString(), taskName);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(updatedTask);

        var output = useCase.execute(input);

        assertThat(output.taskName()).isEqualTo(taskName);
        assertThat(output.userId()).isEqualTo(userId.asString());
        assertThat(output.finished()).isFalse();
        assertThat(output.createdAt()).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Should throw when task is not found")
    void shouldThrowWhenTaskNotFound() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var userId = task.getUserId();
        var input = new UpdateTaskInputDTO(taskId.asString(), userId.asString(), "Updated task name");

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when user does not own the task")
    void shouldThrowWhenUserDoesNotOwnTask() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var input = new UpdateTaskInputDTO(taskId.asString(), userId.asString(), "Updated task name");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(TaskAccessDeniedException.class);

        verify(taskRepository, never()).save(any());
    }
}
```

---

## Checklist antes de finalizar

- [ ] Interface criada em `interfaces/` sem anotações Spring
- [ ] Input DTO é um `record` com campos `String`/primitivos
- [ ] Output DTO reutilizado ou criado como `record` em `dto/output/`
- [ ] Implementação anotada com `@Service`, injeção via construtor
- [ ] Value objects validados com `isFail()` antes de usar
- [ ] Método privado `toOutput(...)` para conversão entidade → DTO
- [ ] Exceções novas criadas em `exception/` se necessário
- [ ] Teste com `@ExtendWith(MockitoExtension.class)`, sem Spring context
- [ ] Happy path coberto
- [ ] Cenários de erro cobertos (not found, access denied, invalid input)
- [ ] `@DisplayName` em todos os métodos de teste
- [ ] Fixtures usados para construção de dados de teste
- [ ] Imports ordenados corretamente (estáticos primeiro, depois agrupados)
- [ ] Nenhuma linha ultrapassa 120 caracteres
