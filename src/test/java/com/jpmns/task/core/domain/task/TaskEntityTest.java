package com.jpmns.task.core.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.shared.fixture.TaskFixture;

class TaskEntityTest {

    private static final String VALID_ID = "00000000-0000-0000-0000-000000000001";
    private static final String VALID_USER_ID = "00000000-0000-0000-0000-000000000002";

    @Test
    @DisplayName("Should create a task with valid data")
    void shouldCreateTaskWithValidData() {
        var id = VALID_ID;
        var userId = VALID_USER_ID;
        var taskName = "Buy groceries";
        var finished = false;

        var task = new TaskEntity(id, userId, taskName, finished);

        assertThat(task.getId().asString()).isEqualTo(id);
        assertThat(task.getUserId().asString()).isEqualTo(userId);
        assertThat(task.getTaskName().asString()).isEqualTo(taskName);
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
        var taskName = "Buy groceries";

        task.updateTaskName(taskName);

        assertThat(task.getTaskName().asString()).isEqualTo(taskName);
    }

    @Test
    @DisplayName("Should preserve the userId after updating the task name")
    void shouldPreserveUserIdAfterUpdate() {
        var task = TaskFixture.aTask();
        var userId = task.getUserId();
        var updateTaskName = "Updated task name";

        task.updateTaskName(updateTaskName);

        assertThat(task.getUserId().asString()).isEqualTo(userId.asString());
    }

    @Test
    @DisplayName("Should throw when task name is empty")
    void shouldThrowWhenTaskNameIsBlank() {
        var id = VALID_ID;
        var userId = VALID_USER_ID;
        var finished = false;
        var emptyTaskName = "";

        assertThatThrownBy(() -> new TaskEntity(id, userId, emptyTaskName, finished))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when task name is null")
    void shouldThrowWhenTaskNameIsNull() {
        var id = VALID_ID;
        var userId = VALID_USER_ID;
        var finished = false;
        String nullTaskName = null;

        assertThatThrownBy(() -> new TaskEntity(id, userId, nullTaskName, finished))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when task name exceeds 255 characters")
    void shouldThrowWhenTaskNameExceedsMaxLength() {
        var id = VALID_ID;
        var userId = VALID_USER_ID;
        var longName = "a".repeat(256);
        var finished = false;

        assertThatThrownBy(() -> new TaskEntity(id, userId, longName, finished))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when task id is not a valid UUID")
    void shouldThrowWhenIdIsNotUUID() {
        var userId = VALID_USER_ID;
        var invalidTaskId = "not-a-uuid";
        var taskName = "Buy groceries";
        var finished = false;

        assertThatThrownBy(() -> new TaskEntity(invalidTaskId, userId, taskName, finished))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    var errors = ((DomainException) ex).getErrors();
                    assertThat(errors).contains("Id is not in format UUID");
                });
    }

    @Test
    @DisplayName("Should throw when userId is not a valid UUID")
    void shouldThrowWhenUserIdIsNotUUID() {
        var id = VALID_ID;
        var taskName = "Buy groceries";
        var invalidUserId = "not-a-uuid";
        var finished = false;

        assertThatThrownBy(() -> new TaskEntity(id, invalidUserId, taskName, finished))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when updating task name with a blank value")
    void shouldThrowWhenUpdatingWithBlankName() {
        var task = TaskFixture.aTask();
        var emptyTaskName = "";

        assertThatThrownBy(() -> task.updateTaskName(emptyTaskName))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when updating task name with null")
    void shouldThrowWhenUpdatingWithNullName() {
        var task = TaskFixture.aTask();
        String nullTaskName = null;

        assertThatThrownBy(() -> task.updateTaskName(nullTaskName))
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
