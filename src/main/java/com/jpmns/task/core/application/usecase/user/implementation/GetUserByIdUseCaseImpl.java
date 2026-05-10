package com.jpmns.task.core.application.usecase.user.implementation;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.UserOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.interfaces.GetUserByIdUseCase;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.user.UserEntity;

@Service
public class GetUserByIdUseCaseImpl implements GetUserByIdUseCase {

    private final UserRepository userRepository;

    public GetUserByIdUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserOutputDTO execute(GetUserByIdInputDTO input) {
        var idValueOrError = IdValueObject.of(input.id());
        if (idValueOrError.isFail()) {
            throw idValueOrError.getError();
        }

        var idValue = idValueOrError.getValue();

        var user = userRepository.findById(idValue).orElseThrow(UserNotFoundException::new);

        return toOutput(user);
    }

    private UserOutputDTO toOutput(UserEntity user) {
        return new UserOutputDTO(
                user.getId().asString(),
                user.getUsername().asString(),
                user.getPassword().asString(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
