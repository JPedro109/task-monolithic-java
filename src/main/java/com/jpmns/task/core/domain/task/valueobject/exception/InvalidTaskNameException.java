package com.jpmns.task.core.domain.task.valueobject.exception;

import com.jpmns.task.core.domain.common.exception.DomainException;

public class InvalidTaskNameException extends DomainException {

    public InvalidTaskNameException() {
        super("Task name must be between 1 and 255 characters");
    }
}
