package com.jpmns.task.core.application.usecase.user.implementation;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.port.security.PasswordEncoder;
import com.jpmns.task.core.application.usecase.user.dto.input.CreateUserInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.CreateUserOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException;
import com.jpmns.task.core.application.usecase.user.interfaces.CreateUserUseCase;
import com.jpmns.task.core.domain.user.UserEntity;

@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCaseImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CreateUserOutputDTO execute(CreateUserInputDTO input) {
        var encodedPassword = passwordEncoder.encode(input.password());
        var user = new UserEntity(UUID.randomUUID().toString(), input.username(), encodedPassword);

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        var saved = userRepository.save(user);

        return new CreateUserOutputDTO(saved.getId().asString(), saved.getUsername().asString());
    }
}
