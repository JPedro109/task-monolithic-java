package com.jpmns.task.core.fixture;

import com.jpmns.task.core.domain.task.TaskEntity;

public final class TaskFixture {

    private static final String DEFAULT_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901";
    private static final String DEFAULT_USER_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901";
    private static final String DEFAULT_TASK_NAME = "Buy groceries";
    private static final boolean DEFAULT_FINISHED = false;

    private TaskFixture() { }

    public static TaskEntity aTask() {
        return new TaskEntity(DEFAULT_ID, DEFAULT_USER_ID, DEFAULT_TASK_NAME, DEFAULT_FINISHED);
    }

    public static TaskEntity aTaskWithName(String taskName) {
        return new TaskEntity(DEFAULT_ID, DEFAULT_USER_ID, taskName, DEFAULT_FINISHED);
    }
}
