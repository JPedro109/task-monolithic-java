package com.jpmns.task.core.external.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jpmns.task.core.application.port.security.exception.InvalidTokenException;

class TokenAdapterTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars!!";
    private static final long ACCESS_EXPIRATION_MS = 900_000L;
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    private TokenAdapter tokenAdapter;

    @BeforeEach
    void setUp() {
        tokenAdapter = new TokenAdapter(SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("Should generate a non-null access token for a given subject")
    void shouldGenerateAccessToken() {
        var sub = UUID.randomUUID().toString();

        var token = tokenAdapter.generateAccessToken(sub);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("Should generate a non-null refresh token for a given subject")
    void shouldGenerateRefreshToken() {
        var sub = UUID.randomUUID().toString();

        var token = tokenAdapter.generateRefreshToken(sub);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("Should generate different tokens for access and refresh")
    void shouldGenerateDifferentTokensForAccessAndRefresh() {
        var sub = UUID.randomUUID().toString();

        var accessToken = tokenAdapter.generateAccessToken(sub);
        var refreshToken = tokenAdapter.generateRefreshToken(sub);

        assertThat(accessToken).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Should validate a valid access token and return the correct subject")
    void shouldValidateAccessTokenAndReturnSubject() {
        var sub = UUID.randomUUID().toString();
        var token = tokenAdapter.generateAccessToken(sub);

        var decoded = tokenAdapter.tokenValidation(token);

        assertThat(decoded).isNotNull();
        assertThat(decoded.sub()).isEqualTo(sub);
    }

    @Test
    @DisplayName("Should validate a valid refresh token and return the correct subject")
    void shouldValidateRefreshTokenAndReturnSubject() {
        var sub = UUID.randomUUID().toString();
        var token = tokenAdapter.generateRefreshToken(sub);

        var decoded = tokenAdapter.tokenValidation(token);

        assertThat(decoded).isNotNull();
        assertThat(decoded.sub()).isEqualTo(sub);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is malformed")
    void shouldThrowWhenTokenIsMalformed() {
        var malformed = "this.is.not.a.valid.jwt";

        assertThatThrownBy(() -> tokenAdapter.tokenValidation(malformed))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is empty")
    void shouldThrowWhenTokenIsEmpty() {
        assertThatThrownBy(() -> tokenAdapter.tokenValidation(""))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is signed with a different secret")
    void shouldThrowWhenTokenSignedWithDifferentSecret() {
        var otherAdapter = new TokenAdapter(
                "another-secret-key-must-be-at-least-32-chars!",
                ACCESS_EXPIRATION_MS,
                REFRESH_EXPIRATION_MS
        );
        var sub = UUID.randomUUID().toString();
        var token = otherAdapter.generateAccessToken(sub);

        assertThatThrownBy(() -> tokenAdapter.tokenValidation(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is expired")
    void shouldThrowWhenTokenIsExpired() {
        var expiredAdapter = new TokenAdapter(SECRET, -1L, -1L);
        var sub = UUID.randomUUID().toString();
        var token = expiredAdapter.generateAccessToken(sub);

        assertThatThrownBy(() -> tokenAdapter.tokenValidation(token))
                .isInstanceOf(InvalidTokenException.class);
    }
}
