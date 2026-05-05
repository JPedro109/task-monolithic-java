package com.jpmns.task.core.application.usecase.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import com.jpmns.task.core.fixture.TaskFixture;

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
}
