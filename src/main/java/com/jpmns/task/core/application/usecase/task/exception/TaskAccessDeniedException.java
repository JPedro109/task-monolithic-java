package com.jpmns.task.core.application.usecase.task.exception;

public class TaskAccessDeniedException extends RuntimeException {

    public TaskAccessDeniedException() {
        super("Access denied to this task");
    }
}
