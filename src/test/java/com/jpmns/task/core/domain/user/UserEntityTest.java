package com.jpmns.task.core.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.shared.fixture.UserFixture;

class UserEntityTest {

    @Test
    @DisplayName("Should create a user with valid data")
    void shouldCreateUserWithValidData() {
        var id = UUID.randomUUID().toString();
        var username = "username";
        var password = "password";

        var user = new UserEntity(id, username, password);

        assertThat(user.getId().asString()).isEqualTo(id);
        assertThat(user.getUsername().asString()).isEqualTo(username);
        assertThat(user.getPassword().asString()).isEqualTo(password);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update the username")
    void shouldUpdateUsername() {
        var user = UserFixture.aUser();
        var username = "newusername";

        user.updateUsername(username);

        assertThat(user.getUsername().asString()).isEqualTo(username);
    }

    @Test
    @DisplayName("Should update the password")
    void shouldUpdatePassword() {
        var user = UserFixture.aUser();
        var password = "new-password";

        user.updatePassword(password);

        assertThat(user.getPassword().asString()).isEqualTo(password);
    }

    @Test
    @DisplayName("Should throw when username is too short")
    void shouldThrowWhenUsernameIsInvalid() {
        var id = UUID.randomUUID().toString();
        var username = "ab";
        var password = "password";

        assertThatThrownBy(() -> new UserEntity(id, username, password))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when password is null")
    void shouldThrowWhenPasswordIsNull() {
        var id = UUID.randomUUID().toString();
        var username = "username";
        String password = null;

        assertThatThrownBy(() -> new UserEntity(id, username, password))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw with two errors when both username and password are invalid")
    void shouldThrowWhenBothFieldsAreInvalid() {
        var id = UUID.randomUUID().toString();
        var shortUsername = "ab";
        String nullPassword = null;

        assertThatThrownBy(() -> new UserEntity(id, shortUsername, nullPassword))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    var errors = ((DomainException) ex).getErrors();
                    assertThat(errors).hasSize(2);
                });
    }

    @Test
    @DisplayName("Should throw when id is null")
    void shouldThrowWhenIdIsNull() {
        String nullId = null;
        var username = "username";
        var password = "password";

        assertThatThrownBy(() -> new UserEntity(nullId, username, password))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when id is not a valid UUID")
    void shouldThrowWhenIdIsNotUUID() {
        var id = "not-a-uuid";
        var username = "username";
        var password = "password";

        assertThatThrownBy(() -> new UserEntity(id, username, password))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    var errors = ((DomainException) ex).getErrors();
                    assertThat(errors).contains("Id is not in format UUID");
                });
    }

    @Test
    @DisplayName("Should throw when updating with an invalid username")
    void shouldThrowWhenUpdatingWithInvalidUsername() {
        var user = UserFixture.aUser();
        var username = "ab";

        assertThatThrownBy(() -> user.updateUsername(username))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when updating with a null username")
    void shouldThrowWhenUpdatingWithNullUsername() {
        var user = UserFixture.aUser();
        String nullUsername = null;

        assertThatThrownBy(() -> user.updateUsername(nullUsername))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should throw when updating with a null password")
    void shouldThrowWhenUpdatingWithNullPassword() {
        var user = UserFixture.aUser();
        String nullPassword = null;

        assertThatThrownBy(() -> user.updatePassword(nullPassword))
                .isInstanceOf(DomainException.class);
    }
}
