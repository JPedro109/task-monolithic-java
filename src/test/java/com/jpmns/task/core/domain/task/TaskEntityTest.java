package com.jpmns.task.core.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.core.fixture.TaskFixture;

class TaskEntityTest {

    @Test
    @DisplayName("Should create a task with valid data")
    void shouldCreateTaskWithValidData() {
        var id = UUID.randomUUID().toString();
        var userId = UUID.randomUUID().toString();

        var task = new TaskEntity(id, userId, "Buy groceries", false);

        assertThat(task.getId().asString()).isEqualTo(id);
        assertThat(task.getUserId().asString()).isEqualTo(userId);
        assertThat(task.getTaskName().asString()).isEqualTo("Buy groceries");
        assertThat(task.getFinished()).isFalse();
        assertThat(task.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should mark a task as finished")
    void shouldMarkTaskAsFinished() {
        var task = TaskFixture.aTask();

        task.markAsFinished();

        assertThat(task.getFinished()).isTrue();
    }

    @Test
    @DisplayName("Should update the task name")
    void shouldUpdateTaskName() {
        var task = TaskFixture.aTask();

        task.updateTaskName("New name");

        assertThat(task.getTaskName().asString()).isEqualTo("New name");
    }

    @Test
    @DisplayName("Should preserve the userId after updating the task name")
    void shouldPreserveUserIdAfterUpdate() {
        var task = TaskFixture.aTask();
        var userId = task.getUserId();

        task.updateTaskName("Updated name");

        assertThat(task.getUserId().asString()).isEqualTo(userId.asString());
    }

    @Test
    @DisplayName("Should throw when task name is blank")
    void shouldThrowWhenTaskNameIsBlank() {
        var id = UUID.randomUUID().toString();
        var userId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> new TaskEntity(id, userId, "  ", false))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when task name is null")
    void shouldThrowWhenTaskNameIsNull() {
        var id = UUID.randomUUID().toString();
        var userId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> new TaskEntity(id, userId, null, false))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when task name exceeds 255 characters")
    void shouldThrowWhenTaskNameExceedsMaxLength() {
        var id = UUID.randomUUID().toString();
        var userId = UUID.randomUUID().toString();
        var longName = "a".repeat(256);

        assertThatThrownBy(() -> new TaskEntity(id, userId, longName, false))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when task id is not a valid UUID")
    void shouldThrowWhenIdIsNotUUID() {
        var userId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> new TaskEntity("not-a-uuid", userId, "Buy groceries", false))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    var errors = ((DomainException) ex).getErrors();
                    assertThat(errors).contains("Id is not in format UUID");
                });
    }

    @Test
    @DisplayName("Should throw when userId is not a valid UUID")
    void shouldThrowWhenUserIdIsNotUUID() {
        var id = UUID.randomUUID().toString();

        assertThatThrownBy(() -> new TaskEntity(id, "not-a-uuid", "Buy groceries", false))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    var errors = ((DomainException) ex).getErrors();
                    assertThat(errors).contains("Id is not in format UUID");
                });
    }

    @Test
    @DisplayName("Should throw when updating task name with a blank value")
    void shouldThrowWhenUpdatingWithBlankName() {
        var task = TaskFixture.aTask();

        assertThatThrownBy(() -> task.updateTaskName(""))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when updating task name with null")
    void shouldThrowWhenUpdatingWithNullName() {
        var task = TaskFixture.aTask();

        assertThatThrownBy(() -> task.updateTaskName(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when updating task name with a value exceeding 255 characters")
    void shouldThrowWhenUpdatingWithNameExceedingMaxLength() {
        var task = TaskFixture.aTask();
        var longName = "a".repeat(256);

        assertThatThrownBy(() -> task.updateTaskName(longName))
                .isInstanceOf(DomainException.class);
    }
}
