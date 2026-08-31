package com.jpmns.task.core.application.usecase.user;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.UserOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.user.UserEntity;

@Service
public class GetUserByIdUseCase {

    private final UserRepository userRepository;

    public GetUserByIdUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserOutputDTO execute(GetUserByIdInputDTO input) {
        var idValue = IdValueObject.of(input.id()).getValueOrThrow();

        var user = userRepository.findById(idValue).orElseThrow(UserNotFoundException::new);

        return toOutput(user);
    }

    private UserOutputDTO toOutput(UserEntity user) {
        return new UserOutputDTO(
                user.getId().asString(),
                user.getUsername().asString(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
