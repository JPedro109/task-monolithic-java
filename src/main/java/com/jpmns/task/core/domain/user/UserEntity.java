package com.jpmns.task.core.domain.user;

import java.time.Instant;
import java.util.List;

import com.jpmns.task.core.domain.common.abstracts.Entity;
import com.jpmns.task.core.domain.user.valueobject.UserPasswordValueObject;
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject;

public class UserEntity extends Entity {

    private UsernameValueObject username;
    private UserPasswordValueObject password;
    private final Instant updatedAt;

    public UserEntity(String id, String username, String password, Instant createdAt, Instant updatedAt) {
        super(id, createdAt);

        var usernameResult = UsernameValueObject.of(username);
        var passwordResult = UserPasswordValueObject.of(password);

        var results = List.of(usernameResult, passwordResult);
        validateOrThrow(results);

        this.username = usernameResult.getValue();
        this.password = passwordResult.getValue();
        this.updatedAt = updatedAt;
    }

    public UserEntity(String id, String username, String password) {
        this(id, username, password, null, null);
    }

    public UsernameValueObject getUsername() {
        return username;
    }

    public UserPasswordValueObject getPassword() {
        return password;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateUsername(String username) {
        this.username = UsernameValueObject.of(username).getValueOrThrow();
    }

    public void updatePassword(String encodedPassword) {
        this.password = UserPasswordValueObject.of(encodedPassword).getValueOrThrow();
    }
}
