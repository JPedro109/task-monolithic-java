package com.jpmns.task.core.domain.user.valueobject.exception;

import com.jpmns.task.core.domain.common.exception.DomainException;

public class InvalidUserEmailException extends DomainException {

    public InvalidUserEmailException() {
        super("Invalid email format");
    }
}
