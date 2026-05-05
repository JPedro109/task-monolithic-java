package com.jpmns.task.core.domain.user.valueobject.exception;

import com.jpmns.task.core.domain.common.exception.DomainException;

public class InvalidUsernameException extends DomainException {

    public InvalidUsernameException() {
        super("Username must be between 3 and 50 characters and contain only letters, numbers or underscores");
    }
}
