package com.jpmns.task.core.domain.common.valueobject.exception;

import com.jpmns.task.core.domain.common.exception.DomainException;

public class InvalidIdValueObjectException extends DomainException {
    public InvalidIdValueObjectException() {
        super("Id is not in format UUID");
    }
}
