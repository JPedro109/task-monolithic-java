package com.jpmns.task.core.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThatCode;
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
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.implementation.UpdateUserPasswordUseCaseImpl;
import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.core.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UpdateUserPasswordUseCaseTest {

    private static final String NEW_PASSWORD = "new_password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserPasswordUseCaseImpl useCase;

    @Test
    @DisplayName("Should update password successfully when current password matches")
    void shouldUpdatePasswordSuccessfully() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var currentPassword = user.getPassword().asString();
        var encodedNewPassword = "encoded_new_password";
        var input = new UpdateUserPasswordInputDTO(userId.asString(), currentPassword, NEW_PASSWORD);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, currentPassword)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(encodedNewPassword);
        when(userRepository.save(user)).thenReturn(user);

        assertThatCode(() -> useCase.execute(input)).doesNotThrowAnyException();

        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw when user ID is invalid")
    void shouldThrowWhenUserIdIsInvalid() {
        var invalidUserId = "not-a-valid-uuid";
        var currentPassword = "current";
        var input = new UpdateUserPasswordInputDTO(invalidUserId, currentPassword, NEW_PASSWORD);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(DomainException.class);

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when user is not found")
    void shouldThrowWhenUserNotFound() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var currentPassword = "current";
        var input = new UpdateUserPasswordInputDTO(userId.asString(), currentPassword, NEW_PASSWORD);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when current password does not match")
    void shouldThrowWhenCurrentPasswordDoesNotMatch() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var currentPassword = user.getPassword().asString();
        var wrongPassword = "wrong_password";
        var input = new UpdateUserPasswordInputDTO(userId.asString(), wrongPassword, NEW_PASSWORD);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, currentPassword)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
