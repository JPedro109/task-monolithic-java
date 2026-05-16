package com.jpmns.task.core.application.usecase.task;

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
import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.application.usecase.task.implementation.DeleteTaskUseCaseImpl;
import com.jpmns.task.shared.fixture.TaskFixture;
import com.jpmns.task.shared.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class DeleteTaskUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private DeleteTaskUseCaseImpl useCase;

    @Test
    @DisplayName("Should delete a task successfully")
    void shouldDeleteTaskSuccessfully() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var userId = task.getUserId();
        var input = new DeleteTaskInputDTO(taskId.asString(), userId.asString());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        useCase.execute(input);

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    @DisplayName("Should throw when task is not found")
    void shouldThrowWhenTaskNotFound() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var userId = task.getUserId();
        var input = new DeleteTaskInputDTO(taskId.asString(), userId.asString());

        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw when user does not own the task")
    void shouldThrowWhenUserDoesNotOwnTask() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var user = UserFixture.aUser();
        var userId = user.getId();
        var input = new DeleteTaskInputDTO(taskId.asString(), userId.asString());

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(TaskAccessDeniedException.class);

        verify(taskRepository, never()).deleteById(any());
    }
}
