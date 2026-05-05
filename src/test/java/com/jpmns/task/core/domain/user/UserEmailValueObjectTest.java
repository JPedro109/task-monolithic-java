package com.jpmns.task.core.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.jpmns.task.core.domain.user.valueobject.UserEmailValueObject;

class UserEmailValueObjectTest {

    @Test
    @DisplayName("Should create a valid UserEmailValueObject")
    void shouldCreateValidEmail() {
        var email = "user@example.com";

        var result = UserEmailValueObject.of(email);

        assertThat(result.isFail()).isFalse();
        assertThat(result.getValue().asString()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should accept an email with subdomain")
    void shouldAcceptEmailWithSubdomain() {
        var email = "user@mail.example.com";

        var result = UserEmailValueObject.of(email);

        assertThat(result.isFail()).isFalse();
    }

    @Test
    @DisplayName("Should accept an email with plus sign")
    void shouldAcceptEmailWithPlusSign() {
        var email = "user+tag@example.com";

        var result = UserEmailValueObject.of(email);

        assertThat(result.isFail()).isFalse();
    }

    @ParameterizedTest
    @DisplayName("Should fail for null, empty or malformed email addresses")
    @NullAndEmptySource
    @ValueSource(strings = {
            "notanemail",
            "missing@dot",
            "@nodomain.com",
            "spaces @domain.com"
    })
    void shouldFailForInvalidEmail(String email) {
        var result = UserEmailValueObject.of(email);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getError().getErrors()).contains("Invalid email format");
    }
}
