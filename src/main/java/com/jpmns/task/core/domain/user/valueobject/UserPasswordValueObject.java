package com.jpmns.task.core.domain.user.valueobject;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.core.domain.user.valueobject.exception.InvalidPasswordException;
import com.jpmns.task.shared.type.Result;

public class UserPasswordValueObject {

    private final String password;

    private UserPasswordValueObject(String password) {
        this.password = password;
    }

    public static Result<UserPasswordValueObject, DomainException> of(String password) {
        if (password == null) {
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
