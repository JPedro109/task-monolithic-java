package com.jpmns.task.core.domain.user.valueobject;

import java.util.regex.Pattern;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.core.domain.user.valueobject.exception.InvalidUserEmailException;
import com.jpmns.task.shared.type.Result;

public class UserEmailValueObject {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String email;

    private UserEmailValueObject(String email) {
        this.email = email;
    }

    public static Result<UserEmailValueObject, DomainException> of(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return Result.fail(new InvalidUserEmailException());
        }

        return Result.success(new UserEmailValueObject(email));
    }

    public String asString() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UserEmailValueObject other)) {
            return false;
        }

        return asString().equals(other.asString());
    }

    @Override
    public int hashCode() {
        return asString().hashCode();
    }
}
