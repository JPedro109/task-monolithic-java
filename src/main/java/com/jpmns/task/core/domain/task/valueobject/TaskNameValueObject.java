package com.jpmns.task.core.domain.task.valueobject;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.core.domain.task.valueobject.exception.InvalidTaskNameException;
import com.jpmns.task.shared.type.Result;

public class TaskNameValueObject {

    private static final int MAX_LENGTH = 255;

    private final String name;

    private TaskNameValueObject(String name) {
        this.name = name;
    }

    public static Result<TaskNameValueObject, DomainException> of(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_LENGTH) {
            return Result.fail(new InvalidTaskNameException());
        }

        return Result.success(new TaskNameValueObject(name));
    }

    public String asString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TaskNameValueObject other)) {
            return false;
        }

        return asString().equals(other.asString());
    }

    @Override
    public int hashCode() {
        return asString().hashCode();
    }
}
