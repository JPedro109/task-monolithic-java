package com.jpmns.task.core.application.usecase.user;

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
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.implementation.DeleteUserUseCaseImpl;
import com.jpmns.task.core.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserUseCaseImpl useCase;

    @Test
    @DisplayName("Should delete a user successfully")
    void shouldDeleteUserSuccessfully() {
        var user = UserFixture.aUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        useCase.execute(new DeleteUserInputDTO(user.getId().asString()));

        verify(userRepository).deleteById(user.getId());
    }

    @Test
    @DisplayName("Should throw when user is not found")
    void shouldThrowWhenUserNotFound() {
        var user = UserFixture.aUser();
        var userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeleteUserInputDTO(userId.asString())))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
