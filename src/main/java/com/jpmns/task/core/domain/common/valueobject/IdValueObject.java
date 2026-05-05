package com.jpmns.task.core.domain.common.valueobject;

import java.util.regex.Pattern;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.core.domain.common.valueobject.exception.InvalidIdValueObjectException;
import com.jpmns.task.shared.type.Result;

public class IdValueObject {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final String id;

    private IdValueObject(String id) {
        this.id = id;
    }

    public String asString() {
        return id;
    }

    public static Result<IdValueObject, DomainException> of(String id) {
        if (id == null || !UUID_PATTERN.matcher(id).matches()) {
            return Result.fail(new InvalidIdValueObjectException());
        }

        return Result.success(new IdValueObject(id));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdValueObject other)) {
            return false;
        }
        return asString().equals(other.asString());
    }

    @Override
    public int hashCode() {
        return asString().hashCode();
    }
}
