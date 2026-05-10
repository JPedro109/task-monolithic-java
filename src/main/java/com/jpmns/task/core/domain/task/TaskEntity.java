package com.jpmns.task.core.domain.task;

import java.time.Instant;
import java.util.List;

import com.jpmns.task.core.domain.common.abstracts.Entity;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.task.valueobject.TaskNameValueObject;

public class    TaskEntity extends Entity {

    private TaskNameValueObject taskName;
    private boolean finished;
    private final IdValueObject userId;
    private final Instant updatedAt;

    public TaskEntity(String id,
                      String userId,
                      String taskName,
                      Boolean finished,
                      Instant createdAt,
                      Instant updatedAt) {
        super(id, createdAt);

        var userIdResult = IdValueObject.of(userId);
        var taskNameResult = TaskNameValueObject.of(taskName);
        validateOrThrow(List.of(userIdResult, taskNameResult));

        this.userId = userIdResult.getValue();
        this.taskName = taskNameResult.getValue();
        this.finished = finished;
        this.updatedAt = updatedAt;
    }

    public TaskEntity(String id, String userId, String taskName, Boolean finished) {
        this(id, userId, taskName, finished, null, null);
    }

    public TaskNameValueObject getTaskName() {
        return taskName;
    }

    public IdValueObject getUserId() {
        return userId;
    }

    public boolean getFinished() {
        return finished;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateTaskName(String taskName) {
        var taskNameResult = TaskNameValueObject.of(taskName);

        validateOrThrow(List.of(taskNameResult));

        this.taskName = taskNameResult.getValue();
    }

    public void markAsFinished() {
        this.finished = true;
    }
}
