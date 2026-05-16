package com.jpmns.task.core.external.persistence.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.jpmns.task.core.domain.task.TaskEntity;
import com.jpmns.task.core.domain.user.UserEntity;
import com.jpmns.task.core.external.persistence.model.TaskJpaModel;
import com.jpmns.task.core.external.persistence.model.UserJpaModel;
import com.jpmns.task.shared.fixture.TaskFixture;
import com.jpmns.task.shared.fixture.UserFixture;

@DataJpaTest
@DisplayName("TaskJpaDao Tests")
class TaskJpaDaoTest {

    @Autowired
    private TaskJpaDao taskJpaDao;

    @Autowired
    private UserJpaDao userJpaDao;

    private UUID userId;

    @BeforeEach
    void setUp() {
        var user = UserFixture.aUser();
        var model = buildUser(user);
        userJpaDao.save(model);
        userId = model.getId();
    }

    private UserJpaModel buildUser(UserEntity user) {
        var userId = user.getId();
        var username = user.getUsername();
        var password = user.getPassword();
        var createdAt = user.getCreatedAt();

        return new UserJpaModel(
                UUID.fromString(userId.asString()),
                username.asString(),
                password.asString(),
                createdAt,
                null
        );
    }

    private TaskJpaModel buildTask(TaskEntity task, UUID userId) {
        var taskId = task.getId();
        var taskName = task.getTaskName();
        var finished = task.getFinished();
        var createdAt = task.getCreatedAt();

        return new TaskJpaModel(
                UUID.fromString(taskId.asString()),
                userId,
                taskName.asString(),
                finished,
                createdAt,
                null
        );
    }

    @Test
    @DisplayName("Should save a task and return it with populated timestamps")
    void shouldSaveTask() {
        var task = TaskFixture.aTask();
        var model = buildTask(task, userId);

        var saved = taskJpaDao.save(model);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(model.getId());
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getTaskName()).isEqualTo(task.getTaskName().asString());
        assertThat(saved.isFinished()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should find a task by id after saving")
    void shouldFindTaskById() {
        var task = TaskFixture.aTask();
        var model = buildTask(task, userId);
        taskJpaDao.save(model);

        var found = taskJpaDao.findById(model.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(model.getId());
        assertThat(found.get().getTaskName()).isEqualTo(task.getTaskName().asString());
    }

    @Test
    @DisplayName("Should return empty Optional when task id does not exist")
    void shouldReturnEmptyWhenTaskNotFoundById() {
        var found = taskJpaDao.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when user has no tasks")
    void shouldReturnEmptyListWhenUserHasNoTasks() {
        var otherUserId = UUID.randomUUID();

        var tasks = taskJpaDao.findAllByUserId(otherUserId);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("Should delete a task by id")
    void shouldDeleteTaskById() {
        var task = TaskFixture.aTask();
        var model = buildTask(task, userId);

        taskJpaDao.save(model);
        taskJpaDao.deleteById(model.getId());

        var found = taskJpaDao.findById(model.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update task name when saving an existing task")
    void shouldUpdateTaskNameWhenSavingExistingTask() {
        var task = TaskFixture.aTask();
        var model = buildTask(task, userId);

        taskJpaDao.save(model);
        model.setTaskName("Updated name");

        taskJpaDao.save(model);

        var found = taskJpaDao.findById(model.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTaskName()).isEqualTo("Updated name");
    }

    @Test
    @DisplayName("Should mark a task as finished when updating")
    void shouldMarkTaskAsFinished() {
        var task = TaskFixture.aTask();
        var model = buildTask(task, userId);

        taskJpaDao.save(model);
        model.setFinished(true);
        taskJpaDao.save(model);

        var found = taskJpaDao.findById(model.getId());

        assertThat(found).isPresent();
        assertThat(found.get().isFinished()).isTrue();
    }

    @Test
    @DisplayName("Should throw when saving a task with a non-existent user_id")
    void shouldThrowWhenSavingTaskWithNonExistentUserId() {
        var task = TaskFixture.aTask();
        var orphanModel = buildTask(task, UUID.randomUUID());

        assertThatThrownBy(() -> {
            taskJpaDao.save(orphanModel);
            taskJpaDao.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should cascade delete tasks when the owner user is deleted")
    void shouldCascadeDeleteTasksWhenUserIsDeleted() {
        var task = TaskFixture.aTask();
        taskJpaDao.save(buildTask(task, userId));
        taskJpaDao.flush();
        userJpaDao.deleteById(userId);
        userJpaDao.flush();

        var tasks = taskJpaDao.findAllByUserId(userId);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("Should find all saved tasks")
    void shouldFindAllTasks() {
        var task = TaskFixture.aTask();
        taskJpaDao.save(buildTask(task, userId));

        var all = taskJpaDao.findAll();

        assertThat(all).hasSizeGreaterThanOrEqualTo(1);
    }
}
