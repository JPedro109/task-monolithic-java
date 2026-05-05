package com.jpmns.task.core.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import com.jpmns.task.core.application.port.security.PasswordEncoder;
import com.jpmns.task.core.application.port.security.Token;
import com.jpmns.task.core.application.usecase.user.dto.input.UserLoginInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException;
import com.jpmns.task.core.application.usecase.user.implementation.UserLoginUseCaseImpl;
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject;
import com.jpmns.task.core.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserLoginUseCaseTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Token tokenProvider;

    @InjectMocks
    private UserLoginUseCaseImpl useCase;

    @Test
    @DisplayName("Should login successfully and return access and refresh tokens")
    void shouldLoginSuccessfully() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var username = user.getUsername();
        var password = user.getPassword();
        var input = new UserLoginInputDTO(username.asString(), password.asString());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password.asString(), password.asString())).thenReturn(true);
        when(tokenProvider.generateAccessToken(userId.asString())).thenReturn(ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(userId.asString())).thenReturn(REFRESH_TOKEN);

        var output = useCase.execute(input);

        assertThat(output.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(output.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Should throw when user is not found")
    void shouldThrowWhenUserNotFound() {
        var username = UsernameValueObject.of("unknown").getValue();
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UserLoginInputDTO(username.asString(), "pass")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw when password does not match")
    void shouldThrowWhenPasswordDoesNotMatch() {
        var user = UserFixture.aUser();
        var username = user.getUsername();
        var password = user.getPassword();
        var wrongPassword = "wrong-password";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, password.asString())).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new UserLoginInputDTO(username.asString(), wrongPassword)))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenProvider, never()).generateAccessToken(any());
    }
}
