package com.jpmns.task.core.application.usecase.user.implementation;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.port.security.PasswordEncoder;
import com.jpmns.task.core.application.port.security.Token;
import com.jpmns.task.core.application.usecase.user.dto.input.UserLoginInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.UserLoginOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException;
import com.jpmns.task.core.application.usecase.user.interfaces.UserLoginUseCase;
import com.jpmns.task.core.domain.user.valueobject.UsernameValueObject;

@Service
public class UserLoginUseCaseImpl implements UserLoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Token token;

    public UserLoginUseCaseImpl(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                Token tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.token = tokenProvider;
    }

    @Override
    public UserLoginOutputDTO execute(UserLoginInputDTO input) {
        var usernameValueOrError = UsernameValueObject.of(input.username());
        if (usernameValueOrError.isFail()) {
            throw usernameValueOrError.getError();
        }

        var usernameValue = usernameValueOrError.getValue();

        var user = userRepository.findByUsername(usernameValue)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(input.password(), user.getPassword().asString())) {
            throw new InvalidCredentialsException();
        }

        var accessToken = token.generateAccessToken(user.getId().asString());
        var refreshToken = token.generateRefreshToken(user.getId().asString());
        return new UserLoginOutputDTO(accessToken, refreshToken);
    }
}
