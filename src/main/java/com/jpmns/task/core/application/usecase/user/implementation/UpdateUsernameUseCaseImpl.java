package com.jpmns.task.core.application.usecase.user.implementation;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException;
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUsernameUseCase;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class UpdateUsernameUseCaseImpl implements UpdateUsernameUseCase {

    private final UserRepository userRepository;

    public UpdateUsernameUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UpdateUsernameOutputDTO execute(UpdateUsernameInputDTO input) {
        var userIdValueOrError = IdValueObject.of(input.userId());
        if (userIdValueOrError.isFail()) {
            throw userIdValueOrError.getError();
        }

        var userIdValue = userIdValueOrError.getValue();
        var user = userRepository.findById(userIdValue).orElseThrow(UserNotFoundException::new);
        user.updateUsername(input.newUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        var saved = userRepository.save(user);

        return new UpdateUsernameOutputDTO(saved.getId().asString(), saved.getUsername().asString());
    }
}
