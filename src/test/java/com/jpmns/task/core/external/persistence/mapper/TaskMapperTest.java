package com.jpmns.task.core.external.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jpmns.task.core.external.persistence.model.TaskJpaModel;
import com.jpmns.task.core.fixture.TaskFixture;
import com.jpmns.task.core.fixture.UserFixture;

class TaskMapperTest {

    @Test
    @DisplayName("Should map a TaskEntity to a TaskJpaModel correctly")
    void shouldMapEntityToModel() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var taskName = task.getTaskName();
        var userId = task.getUserId();

        var model = TaskMapper.toModel(task);

        assertThat(model).isNotNull();
        assertThat(model.getId().toString()).isEqualTo(taskId.asString());
        assertThat(model.getUserId().toString()).isEqualTo(userId.asString());
        assertThat(model.getTaskName()).isEqualTo(taskName.asString());
        assertThat(model.isFinished()).isFalse();
        assertThat(model.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should map a TaskJpaModel to a TaskEntity correctly")
    void shouldMapModelToDomain() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var taskName = task.getTaskName();
        var taskFinished = task.getFinished();
        var user = UserFixture.aUser();
        var userId = user.getId();

        var model = new TaskJpaModel(
                UUID.fromString(taskId.asString()),
                UUID.fromString(userId.asString()),
                taskName.asString(),
                taskFinished,
                Instant.now(),
                Instant.now()
        );

        var entity = TaskMapper.toDomain(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId().asString()).isEqualTo(taskId.asString());
        assertThat(entity.getUserId().asString()).isEqualTo(userId.asString());
        assertThat(entity.getTaskName().asString()).isEqualTo(taskName.asString());
        assertThat(entity.getFinished()).isFalse();
    }

    @Test
    @DisplayName("Should preserve task name when mapping from model to domain")
    void shouldPreserveTaskNameWhenMappingToDomain() {
        var task = TaskFixture.aTask();
        var taskId = task.getId();
        var taskFinished = task.getFinished();
        var user = UserFixture.aUser();
        var userId = user.getId();
        var customName = "Custom task name";

        var model = new TaskJpaModel(
                UUID.fromString(taskId.asString()),
                UUID.fromString(userId.asString()),
                customName,
                taskFinished,
                Instant.now(),
                Instant.now()
        );

        var entity = TaskMapper.toDomain(model);

        assertThat(entity.getTaskName().asString()).isEqualTo(customName);
    }
}
