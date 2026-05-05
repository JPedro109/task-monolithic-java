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
import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.application.usecase.task.implementation.MarkTaskAsFinishedUseCaseImpl;
import com.jpmns.task.core.fixture.TaskFixture;
import com.jpmns.task.core.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class MarkTaskAsFinishedUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private MarkTaskAsFinishedUseCaseImpl useCase;

    @Test
    @DisplayName("Should mark a task as finished successfully")
    void shouldMarkTaskAsFinishedSuccessfully() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var userId = task.getUserId();
        var input = new MarkTaskAsFinishedInputDTO(taskId.asString(), userId.asString());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        useCase.execute(input);

        assertThat(task.getFinished()).isTrue();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Should throw when task is not found")
    void shouldThrowWhenTaskNotFound() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var userId = task.getUserId();
        var input = new MarkTaskAsFinishedInputDTO(taskId.asString(), userId.asString());

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
        var input = new MarkTaskAsFinishedInputDTO(taskId.asString(), userId.asString());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(TaskAccessDeniedException.class);

        verify(taskRepository, never()).save(any());
    }
}
