package com.jpmns.task.core.presentation.controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jpmns.task.core.application.usecase.user.dto.input.RefreshUserTokenInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.input.UserLoginInputDTO;
import com.jpmns.task.core.application.usecase.user.interfaces.RefreshUserTokenUseCase;
import com.jpmns.task.core.application.usecase.user.interfaces.UserLoginUseCase;
import com.jpmns.task.core.presentation.controller.documentation.AuthControllerDoc;
import com.jpmns.task.core.presentation.controller.payload.user.request.RefreshTokenRequest;
import com.jpmns.task.core.presentation.controller.payload.user.request.UserLoginRequest;
import com.jpmns.task.core.presentation.controller.payload.user.response.RefreshTokenResponse;
import com.jpmns.task.core.presentation.controller.payload.user.response.UserLoginResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDoc {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserLoginUseCase userLoginUseCase;
    private final RefreshUserTokenUseCase refreshUserTokenUseCase;

    public AuthController(UserLoginUseCase userLoginUseCase,
                          RefreshUserTokenUseCase refreshUserTokenUseCase) {
        this.userLoginUseCase = userLoginUseCase;
        this.refreshUserTokenUseCase = refreshUserTokenUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        LOGGER.info("User login - request: {}", request);

        var dto = new UserLoginInputDTO(request.username(), request.password());

        var output = userLoginUseCase.execute(dto);
        var response = UserLoginResponse.of(output);

        LOGGER.info("User login - response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LOGGER.info("Refreshing user token - request: {}", request);

        var dto = new RefreshUserTokenInputDTO(request.refreshToken());
        var output = refreshUserTokenUseCase.execute(dto);

        var response = RefreshTokenResponse.of(output);

        LOGGER.info("Refreshing user token - response: {}", response);
        return ResponseEntity.ok(response);
    }
}
