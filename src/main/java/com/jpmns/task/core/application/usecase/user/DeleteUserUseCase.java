package com.jpmns.task.core.application.usecase.user;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(DeleteUserInputDTO input) {
        var userIdValue = IdValueObject.of(input.userId()).getValueOrThrow();

        userRepository.findById(userIdValue).orElseThrow(UserNotFoundException::new);

        userRepository.deleteById(userIdValue);
    }
}
