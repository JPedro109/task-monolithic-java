package com.jpmns.task.core.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject;

class UsernameValueObjectTest {

    @Test
    @DisplayName("Should create a valid UsernameValueObject")
    void shouldCreateValidUsername() {
        var username = "john_doe";

        var result = UsernameValueObject.of(username);

        assertThat(result.isFail()).isFalse();
        assertThat(result.getValue().asString()).isEqualTo(username);
    }

    @Test
    @DisplayName("Should accept a username with exactly 3 characters")
    void shouldAcceptMinimumLength() {
        var username = "abc";

        var result = UsernameValueObject.of(username);

        assertThat(result.isFail()).isFalse();
    }

    @Test
    @DisplayName("Should accept a username with exactly 50 characters")
    void shouldAcceptMaximumLength() {
        var username = "a".repeat(50);

        var result = UsernameValueObject.of(username);

        assertThat(result.isFail()).isFalse();
    }

    @Test
    @DisplayName("Should accept an alphanumeric username with underscore")
    void shouldAcceptAlphanumericWithUnderscore() {
        var username = "User_123";

        var result = UsernameValueObject.of(username);

        assertThat(result.isFail()).isFalse();
    }

    @Test
    @DisplayName("Should be equal when two instances have the same value")
    void shouldBeEqualWhenSameValue() {
        var a = UsernameValueObject.of("john_doe").getValue();
        var b = UsernameValueObject.of("john_doe").getValue();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when two instances have different values")
    void shouldNotBeEqualWhenDifferentValue() {
        var a = UsernameValueObject.of("john_doe").getValue();
        var b = UsernameValueObject.of("jane_doe").getValue();

        assertThat(a).isNotEqualTo(b);
    }

    @ParameterizedTest
    @DisplayName("Should fail for null, empty, too short, too long or invalid character usernames")
    @NullAndEmptySource
    @ValueSource(strings = {
            "ab",
            "has space",
            "invalid-char!",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
    void shouldFailForInvalidUsername(String username) {
        var result = UsernameValueObject.of(username);

        assertThat(result.isFail()).isTrue();
    }
}
