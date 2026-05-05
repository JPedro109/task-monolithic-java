package com.jpmns.task.core.fixture;

import com.jpmns.task.core.domain.user.UserEntity;

public final class UserFixture {

    private static final String DEFAULT_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String DEFAULT_USERNAME = "john_doe";
    private static final String DEFAULT_PASSWORD = "password";

    private UserFixture() { }

    public static UserEntity aUser() {
        return new UserEntity(DEFAULT_ID, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }
}
