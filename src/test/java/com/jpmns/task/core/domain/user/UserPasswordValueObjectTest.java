package com.jpmns.task.core.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.jpmns.task.core.domain.user.valueobject.UserPasswordValueObject;

class UserPasswordValueObjectTest {

    @Test
    @DisplayName("Should create a valid UserPasswordValueObject with exactly 8 characters")
    void shouldCreateValidPasswordWithMinLength() {
        var password = "12345678";

        var result = UserPasswordValueObject.of(password);

        assertThat(result.isFail()).isFalse();
        assertThat(result.getValue().asString()).isEqualTo(password);
    }

    @Test
    @DisplayName("Should create a valid UserPasswordValueObject with more than 8 characters")
    void shouldCreateValidPasswordWithMoreThanMinLength() {
        var password = "raw-password";

        var result = UserPasswordValueObject.of(password);

        assertThat(result.isFail()).isFalse();
        assertThat(result.getValue().asString()).isEqualTo(password);
    }

    @Test
    @DisplayName("Should accept an encoded bcrypt password")
    void shouldAcceptEncodedBcryptPassword() {
        var encoded = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        var result = UserPasswordValueObject.of(encoded);

        assertThat(result.isFail()).isFalse();
        assertThat(result.getValue().asString()).isEqualTo(encoded);
    }

    @Test
    @DisplayName("Should fail when password is null")
    void shouldFailWhenPasswordIsNull() {
        var result = UserPasswordValueObject.of(null);

        assertThat(result.isFail()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Should fail when password has fewer than 8 characters")
    @ValueSource(strings = {"", "1", "1234567"})
    void shouldFailWhenPasswordIsTooShort(String password) {
        var result = UserPasswordValueObject.of(password);

        assertThat(result.isFail()).isTrue();
    }
}
