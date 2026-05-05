package com.jpmns.task.core.application.usecase.user.implementation;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.port.security.PasswordEncoder;
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDTO;
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUserPasswordUseCase;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class UpdateUserPasswordUseCaseImpl implements UpdateUserPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdateUserPasswordUseCaseImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void execute(UpdateUserPasswordInputDTO input) {
        var userIdValueOrError = IdValueObject.of(input.userId());
        if (userIdValueOrError.isFail()) {
            throw userIdValueOrError.getError();
        }

        var userIdValue = userIdValueOrError.getValue();
        var user = userRepository.findById(userIdValue).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(input.currentPassword(), user.getPassword().asString())) {
            throw new InvalidCredentialsException();
        }

        var encodedNewPassword = passwordEncoder.encode(input.newPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);
    }
}
