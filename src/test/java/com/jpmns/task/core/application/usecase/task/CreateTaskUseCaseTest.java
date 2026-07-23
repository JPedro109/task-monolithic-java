package com.jpmns.task.core.application.usecase.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.implementation.CreateTaskUseCaseImpl;
import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.shared.fixture.TaskFixture;

@ExtendWith(MockitoExtension.class)
class CreateTaskUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CreateTaskUseCaseImpl useCase;

    @Test
    @DisplayName("Should create a task successfully")
    void shouldCreateTaskSuccessfully() {
        var savedTask = TaskFixture.aTask();
        var taskName = savedTask.getTaskName();
        var userId = savedTask.getUserId();
        var input = new CreateTaskInputDTO(userId.asString(), taskName.asString());

        when(taskRepository.save(any())).thenReturn(savedTask);

        var output = useCase.execute(input);

        assertThat(output.taskName()).isEqualTo(taskName.asString());
        assertThat(output.userId()).isEqualTo(userId.asString());
        assertThat(output.finished()).isFalse();
        assertThat(output.id()).isNotNull();
        assertThat(output.createdAt()).isNotNull();
        verify(taskRepository).save(any());
    }

    @Test
    @DisplayName("Should throw when task name is blank")
    void shouldThrowWhenTaskNameIsBlank() {
        var task = TaskFixture.aTask();
        var userId = task.getUserId();
        var input = new CreateTaskInputDTO(userId.asString(), "");

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(DomainException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when user id is not a valid UUID")
    void shouldThrowWhenUserIdIsInvalid() {
        var input = new CreateTaskInputDTO("not-a-valid-uuid", "Buy groceries");

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(DomainException.class);

        verify(taskRepository, never()).save(any());
    }
}
