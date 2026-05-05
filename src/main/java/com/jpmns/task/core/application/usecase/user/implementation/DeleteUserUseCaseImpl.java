package com.jpmns.task.core.application.usecase.user.implementation;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.interfaces.DeleteUserUseCase;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DeleteUserInputDTO input) {
        var userIdValueOrError = IdValueObject.of(input.userId());
        if (userIdValueOrError.isFail()) {
            throw userIdValueOrError.getError();
        }

        var userIdValue = userIdValueOrError.getValue();

        if (userRepository.findById(userIdValue).isEmpty()) {
            throw new UserNotFoundException();
        }

        userRepository.deleteById(userIdValue);
    }
}
