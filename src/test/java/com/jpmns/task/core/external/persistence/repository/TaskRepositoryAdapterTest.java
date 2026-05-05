package com.jpmns.task.core.external.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.external.persistence.dao.TaskJpaDao;
import com.jpmns.task.core.external.persistence.model.TaskJpaModel;
import com.jpmns.task.core.fixture.TaskFixture;
import com.jpmns.task.core.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class TaskRepositoryAdapterTest {

    @Mock
    private TaskJpaDao jpaRepository;

    @InjectMocks
    private TaskRepositoryAdapter adapter;

    private TaskJpaModel buildTaskModel() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var taskName = task.getTaskName();
        var taskFinished = task.getFinished();
        var user = UserFixture.aUser();
        var userId = user.getId();

        return new TaskJpaModel(
                UUID.fromString(taskId.asString()),
                UUID.fromString(userId.asString()),
                taskName.asString(),
                taskFinished,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("Should save a task and return the persisted domain entity")
    void shouldSaveTask() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var taskName = task.getTaskName();
        var model = buildTaskModel();

        when(jpaRepository.save(any())).thenReturn(model);

        var result = adapter.save(task);

        assertThat(result).isNotNull();
        assertThat(result.getId().asString()).isEqualTo(taskId.asString());
        assertThat(result.getTaskName().asString()).isEqualTo(taskName.asString());
        verify(jpaRepository).save(any());
    }

    @Test
    @DisplayName("Should find a task by id and return the domain entity")
    void shouldFindTaskById() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var id = IdValueObject.of(taskId.asString()).getValue();
        var model = buildTaskModel();

        when(jpaRepository.findById(UUID.fromString(taskId.asString()))).thenReturn(Optional.of(model));

        var result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId().asString()).isEqualTo(taskId.asString());
    }

    @Test
    @DisplayName("Should return empty Optional when task is not found by id")
    void shouldReturnEmptyWhenTaskNotFoundById() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var id = IdValueObject.of(taskId.asString()).getValue();

        when(jpaRepository.findById(any())).thenReturn(Optional.empty());

        var result = adapter.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all tasks by userId and return domain entities")
    void shouldFindAllTasksByUserId() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var id = IdValueObject.of(userId.asString()).getValue();
        var model = buildTaskModel();

        when(jpaRepository.findAllByUserId(UUID.fromString(userId.asString()))).thenReturn(List.of(model));

        var result = adapter.findAllByUserId(id);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUserId().asString()).isEqualTo(userId.asString());
    }

    @Test
    @DisplayName("Should return empty list when user has no tasks")
    void shouldReturnEmptyListWhenUserHasNoTasks() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var id = IdValueObject.of(userId.asString()).getValue();

        when(jpaRepository.findAllByUserId(any())).thenReturn(List.of());

        var result = adapter.findAllByUserId(id);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should delete a task by id")
    void shouldDeleteTaskById() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var id = IdValueObject.of(taskId.asString()).getValue();

        doNothing().when(jpaRepository).deleteById(any());

        adapter.deleteById(id);

        verify(jpaRepository).deleteById(UUID.fromString(taskId.asString()));
    }
}
