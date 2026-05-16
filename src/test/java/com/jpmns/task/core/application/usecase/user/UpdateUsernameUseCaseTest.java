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
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException;
import com.jpmns.task.core.application.usecase.user.implementation.UpdateUsernameUseCaseImpl;
import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.shared.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UpdateUsernameUseCaseTest {

    private static final String NEW_USERNAME = "new_username";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUsernameUseCaseImpl useCase;

    @Test
    @DisplayName("Should update username successfully")
    void shouldUpdateUsernameSuccessfully() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var input = new UpdateUsernameInputDTO(userId.asString(), NEW_USERNAME);
        var savedUser = UserFixture.aUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(savedUser);

        var output = useCase.execute(input);

        assertThat(output.id()).isEqualTo(savedUser.getId().asString());
        assertThat(output.username()).isEqualTo(savedUser.getUsername().asString());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw when user ID is invalid")
    void shouldThrowWhenUserIdIsInvalid() {
        var invalidUserId = "not-a-valid-uuid";
        var input = new UpdateUsernameInputDTO(invalidUserId, NEW_USERNAME);

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
        var input = new UpdateUsernameInputDTO(userId.asString(), NEW_USERNAME);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when new username is already taken")
    void shouldThrowWhenUsernameAlreadyExists() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var input = new UpdateUsernameInputDTO(userId.asString(), NEW_USERNAME);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(any())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}
