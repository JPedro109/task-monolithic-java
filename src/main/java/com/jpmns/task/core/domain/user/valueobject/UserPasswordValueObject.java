package com.jpmns.task.core.domain.user.valueobject;

import com.jpmns.task.core.domain.user.valueobject.exception.InvalidPasswordException;
import com.jpmns.task.shared.type.Result;

public class UserPasswordValueObject {

    private final String password;

    private UserPasswordValueObject(String password) {
        this.password = password;
    }

    private static final int MIN_LENGTH = 8;

    public static Result<UserPasswordValueObject> of(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return Result.fail(new InvalidPasswordException());
        }

        return Result.success(new UserPasswordValueObject(password));
    }

    public String asString() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UserPasswordValueObject other)) {
            return false;
        }

        return asString().equals(other.asString());
    }

    @Override
    public int hashCode() {
        return asString().hashCode();
    }
}
