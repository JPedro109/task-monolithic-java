package com.jpmns.task.core.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.port.security.Token;
import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto;
import com.jpmns.task.core.application.usecase.user.dto.input.RefreshUserTokenInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.implementation.RefreshUserTokenUseCaseImpl;
import com.jpmns.task.core.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class RefreshUserTokenUseCaseTest {

    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";
    private static final String INVALID_USER_ID = "not-a-valid-uuid";

    @Mock
    private Token token;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshUserTokenUseCaseImpl useCase;

    @Test
    @DisplayName("Should refresh tokens successfully when refresh token is valid")
    void shouldRefreshTokensSuccessfully() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var input = new RefreshUserTokenInputDTO(REFRESH_TOKEN);

        when(token.tokenValidation(REFRESH_TOKEN)).thenReturn(new DecodeTokenDto(userId.asString()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(token.generateAccessToken(userId.asString())).thenReturn(NEW_ACCESS_TOKEN);
        when(token.generateRefreshToken(userId.asString())).thenReturn(NEW_REFRESH_TOKEN);

        var output = useCase.execute(input);

        assertThat(output.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
        assertThat(output.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
        verify(token).generateAccessToken(userId.asString());
        verify(token).generateRefreshToken(userId.asString());
    }

    @Test
    @DisplayName("Should throw when token subject is not a valid user ID")
    void shouldThrowWhenTokenSubjectIsInvalidUserId() {
        var input = new RefreshUserTokenInputDTO(REFRESH_TOKEN);

        when(token.tokenValidation(REFRESH_TOKEN)).thenReturn(new DecodeTokenDto(INVALID_USER_ID));

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.any());
        verify(token, never()).generateAccessToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Should throw when user is not found in the repository")
    void shouldThrowWhenUserNotFound() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var input = new RefreshUserTokenInputDTO(REFRESH_TOKEN);

        when(token.tokenValidation(REFRESH_TOKEN)).thenReturn(new DecodeTokenDto(userId.asString()));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(UserNotFoundException.class);

        verify(token, never()).generateAccessToken(org.mockito.ArgumentMatchers.any());
        verify(token, never()).generateRefreshToken(org.mockito.ArgumentMatchers.any());
    }
}
