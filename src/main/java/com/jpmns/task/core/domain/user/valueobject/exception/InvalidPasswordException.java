package com.jpmns.task.core.domain.user.valueobject.exception;

import com.jpmns.task.core.domain.common.exception.DomainException;

public class InvalidPasswordException extends DomainException {

    public InvalidPasswordException() {
        super("Password must be at least 8 characters long");
    }
}
