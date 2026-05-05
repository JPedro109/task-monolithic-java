package com.jpmns.task.core.domain.common.exception;

import java.util.List;

public class DomainException extends RuntimeException {

    private final List<String> errors;

    protected DomainException(String message) {
        super(message);

        this.errors = List.of(message);
    }

    private DomainException(List<DomainException> causes) {
        super("Found " + causes.size() + " domain errors");

        this.errors = causes.stream()
                .map(Throwable::getMessage)
                .toList();
    }

    public List<String> getErrors() {
        return errors;
    }

    public static DomainException with(List<DomainException> causes) {
        return new DomainException(causes);
    }
}
