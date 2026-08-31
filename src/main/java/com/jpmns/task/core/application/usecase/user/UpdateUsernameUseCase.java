package com.jpmns.task.core.application.usecase.user;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class UpdateUsernameUseCase {

    private final UserRepository userRepository;

    public UpdateUsernameUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UpdateUsernameOutputDTO execute(UpdateUsernameInputDTO input) {
        var userIdValue = IdValueObject.of(input.userId()).getValueOrThrow();

        var user = userRepository.findById(userIdValue).orElseThrow(UserNotFoundException::new);

        user.updateUsername(input.newUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        var saved = userRepository.save(user);

        return new UpdateUsernameOutputDTO(saved.getId().asString(), saved.getUsername().asString());
    }
}
