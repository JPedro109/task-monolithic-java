package com.jpmns.task.core.application.usecase.user.implementation;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.UserRepository;
import com.jpmns.task.core.application.port.security.Token;
import com.jpmns.task.core.application.usecase.user.dto.input.RefreshUserTokenInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.RefreshUserTokenOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.interfaces.RefreshUserTokenUseCase;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class RefreshUserTokenUseCaseImpl implements RefreshUserTokenUseCase {

    private final Token token;
    private final UserRepository userRepository;

    public RefreshUserTokenUseCaseImpl(Token tokenProvider, UserRepository userRepository) {
        this.token = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public RefreshUserTokenOutputDTO execute(RefreshUserTokenInputDTO input) {
        var decodeTokenDto = token.tokenValidation(input.refreshToken());

        var userIdResult = IdValueObject.of(decodeTokenDto.sub());
        if (userIdResult.isFail()) {
            throw new UserNotFoundException();
        }

        var user = userRepository.findById(userIdResult.getValue()).orElseThrow(UserNotFoundException::new);

        var newAccessToken = token.generateAccessToken(user.getId().asString());
        var newRefreshToken = token.generateRefreshToken(user.getId().asString());

        return new RefreshUserTokenOutputDTO(newAccessToken, newRefreshToken);
    }
}
