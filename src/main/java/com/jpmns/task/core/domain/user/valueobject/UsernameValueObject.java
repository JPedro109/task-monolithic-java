package com.jpmns.task.core.domain.user.valueobject;

import java.util.regex.Pattern;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.core.domain.user.valueobject.exception.InvalidUsernameException;
import com.jpmns.task.shared.type.Result;

public class UsernameValueObject {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    private final String username;

    private UsernameValueObject(String username) {
        this.username = username;
    }

    public static Result<UsernameValueObject, DomainException> of(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            return Result.fail(new InvalidUsernameException());
        }

        return Result.success(new UsernameValueObject(username));
    }

    public String asString() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UsernameValueObject other)) {
            return false;
        }

        return asString().equals(other.asString());
    }

    @Override
    public int hashCode() {
        return asString().hashCode();
    }
}
