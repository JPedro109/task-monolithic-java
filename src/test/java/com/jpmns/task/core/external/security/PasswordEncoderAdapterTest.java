package com.jpmns.task.core.external.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordEncoderAdapterTest {

    private PasswordEncoderAdapter passwordEncoderAdapter;

    @BeforeEach
    void setUp() {
        passwordEncoderAdapter = new PasswordEncoderAdapter(new BCryptPasswordEncoder());
    }

    @Test
    @DisplayName("Should encode a raw password and return a non-null hash")
    void shouldEncodeRawPassword() {
        var raw = "mySecret123";

        var encoded = passwordEncoderAdapter.encode(raw);

        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEqualTo(raw);
    }

    @Test
    @DisplayName("Should produce different hashes for the same password on each call")
    void shouldProduceDifferentHashesForSamePassword() {
        var raw = "mySecret123";

        var first = passwordEncoderAdapter.encode(raw);
        var second = passwordEncoderAdapter.encode(raw);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Should return true when raw password matches the encoded one")
    void shouldReturnTrueWhenPasswordMatches() {
        var raw = "mySecret123";
        var encoded = passwordEncoderAdapter.encode(raw);

        var result = passwordEncoderAdapter.matches(raw, encoded);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when raw password does not match the encoded one")
    void shouldReturnFalseWhenPasswordDoesNotMatch() {
        var raw = "mySecret123";
        var wrong = "wrongPassword";
        var encoded = passwordEncoderAdapter.encode(raw);

        var result = passwordEncoderAdapter.matches(wrong, encoded);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when comparing against an empty string")
    void shouldReturnFalseWhenComparingAgainstEmptyString() {
        var raw = "mySecret123";
        var encoded = passwordEncoderAdapter.encode(raw);

        var result = passwordEncoderAdapter.matches("", encoded);

        assertThat(result).isFalse();
    }
}
