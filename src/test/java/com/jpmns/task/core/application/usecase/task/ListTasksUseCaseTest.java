package com.jpmns.task.core.application.usecase.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO;
import com.jpmns.task.core.application.usecase.task.implementation.ListTasksUseCaseImpl;
import com.jpmns.task.shared.fixture.TaskFixture;
import com.jpmns.task.shared.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class ListTasksUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ListTasksUseCaseImpl useCase;

    @Test
    @DisplayName("Should return all tasks for a given user")
    void shouldReturnAllTasksForUser() {
        var taskOne = TaskFixture.aTask();
        var taskTwo = TaskFixture.aTask();
        var taskThree = TaskFixture.aTask();
        var tasks = List.of(taskOne, taskTwo, taskThree);
        var userId = taskOne.getUserId();

        when(taskRepository.findAllByUserId(userId)).thenReturn(tasks);

        var output = useCase.execute(new ListTasksInputDTO(userId.asString()));

        assertThat(output).hasSize(3);
        assertThat(output.getFirst().id()).isEqualTo(taskOne.getId().asString());
        assertThat(output.getFirst().userId()).isEqualTo(taskOne.getUserId().asString());
        assertThat(output.getFirst().taskName()).isEqualTo(taskOne.getTaskName().asString());
        assertThat(output.getFirst().finished()).isFalse();
        assertThat(output.getFirst().createdAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return an empty list when user has no tasks")
    void shouldReturnEmptyListWhenUserHasNoTasks() {
        var user = UserFixture.aUser();
        var userId = user.getId();

        when(taskRepository.findAllByUserId(userId)).thenReturn(List.of());

        var output = useCase.execute(new ListTasksInputDTO(userId.asString()));

        assertThat(output).isEmpty();
    }
}
