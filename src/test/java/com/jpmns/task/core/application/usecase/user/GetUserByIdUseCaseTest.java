package com.jpmns.task.core.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.implementation.GetUserByIdUseCaseImpl;
import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.shared.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class GetUserByIdUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserByIdUseCaseImpl useCase;

    @Test
    @DisplayName("Should return user output with all fields when user is found")
    void shouldReturnUserOutputWhenUserIsFound() {
        var user = UserFixture.aUser();
        var userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var output = useCase.execute(new GetUserByIdInputDTO(userId.asString()));

        assertThat(output.id()).isEqualTo(userId.asString());
        assertThat(output.username()).isEqualTo(user.getUsername().asString());
        assertThat(output.password()).isEqualTo(user.getPassword().asString());
        assertThat(output.createdAt()).isNotNull();
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should include password in the output")
    void shouldIncludePasswordInOutput() {
        var user = UserFixture.aUser();
        var userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var output = useCase.execute(new GetUserByIdInputDTO(userId.asString()));

        assertThat(output.password()).isEqualTo(user.getPassword().asString());
        assertThat(output).hasNoNullFieldsOrPropertiesExcept("updatedAt");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowWhenUserNotFound() {
        var user = UserFixture.aUser();
        var userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetUserByIdInputDTO(userId.asString())))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw DomainException when id is not a valid UUID")
    void shouldThrowWhenIdIsInvalid() {
        assertThatThrownBy(() -> useCase.execute(new GetUserByIdInputDTO("not-a-uuid")))
                .isInstanceOf(DomainException.class);
    }
}
