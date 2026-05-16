package com.jpmns.task.core.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.port.security.PasswordEncoder;
import com.jpmns.task.core.application.usecase.user.dto.input.CreateUserInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException;
import com.jpmns.task.core.application.usecase.user.implementation.CreateUserUseCaseImpl;
import com.jpmns.task.shared.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserUseCaseImpl useCase;

    @Test
    @DisplayName("Should create a user successfully")
    void shouldCreateUserSuccessfully() {
        var user = UserFixture.aUser();
        var username = user.getUsername();
        var password = user.getPassword();
        var input = new CreateUserInputDTO(username.asString(), password.asString());
        var savedUser = UserFixture.aUser();

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password.asString())).thenReturn(password.asString());
        when(userRepository.save(any())).thenReturn(savedUser);

        var output = useCase.execute(input);

        assertThat(output.username()).isEqualTo(username.asString());
        assertThat(output.id()).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("Should throw when username already exists")
    void shouldThrowWhenUsernameAlreadyExists() {
        var user = UserFixture.aUser();
        var username = user.getUsername();
        var password = user.getPassword();
        var input = new CreateUserInputDTO(username.asString(), password.asString());

        when(userRepository.existsByUsername(username)).thenReturn(true);
        when(passwordEncoder.encode(password.asString())).thenReturn(password.asString());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}
