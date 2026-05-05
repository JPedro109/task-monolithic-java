package com.jpmns.task.core.presentation.controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jpmns.task.core.application.usecase.user.dto.input.CreateUserInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUsernameInputDTO;
import com.jpmns.task.core.application.usecase.user.interfaces.CreateUserUseCase;
import com.jpmns.task.core.application.usecase.user.interfaces.DeleteUserUseCase;
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUserPasswordUseCase;
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUsernameUseCase;
import com.jpmns.task.core.presentation.controller.common.resolver.AuthenticatedUserResolver;
import com.jpmns.task.core.presentation.controller.documentation.UserControllerDoc;
import com.jpmns.task.core.presentation.controller.payload.user.request.CreateUserRequest;
import com.jpmns.task.core.presentation.controller.payload.user.request.UpdateUserPasswordRequest;
import com.jpmns.task.core.presentation.controller.payload.user.request.UpdateUsernameRequest;
import com.jpmns.task.core.presentation.controller.payload.user.response.CreateUserResponse;
import com.jpmns.task.core.presentation.controller.payload.user.response.UpdateUsernameResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerDoc {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final CreateUserUseCase createUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UpdateUserPasswordUseCase updateUserPasswordUseCase;
    private final UpdateUsernameUseCase updateUsernameUseCase;

    public UserController(CreateUserUseCase createUserUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          UpdateUserPasswordUseCase updateUserPasswordUseCase,
                          UpdateUsernameUseCase updateUsernameUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.updateUserPasswordUseCase = updateUserPasswordUseCase;
        this.updateUsernameUseCase = updateUsernameUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        LOGGER.info("Creating user - request: {}", request);

        var dto = new CreateUserInputDTO(request.username(), request.password());
        var output = createUserUseCase.execute(dto);

        var response = CreateUserResponse.of(output);

        LOGGER.info("Creating user - response: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteUser() {
        LOGGER.info("Deleting user");

        var userId = AuthenticatedUserResolver.getUserId();
        var dto = new DeleteUserInputDTO(userId);

        deleteUserUseCase.execute(dto);

        LOGGER.info("User deleted successfully");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdateUserPasswordRequest request) {
        LOGGER.info("Updating user password - request: {}", request);

        var userId = AuthenticatedUserResolver.getUserId();
        var dto = new UpdateUserPasswordInputDTO(userId, request.currentPassword(), request.newPassword());

        updateUserPasswordUseCase.execute(dto);

        LOGGER.info("User password updated successfully");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/username")
    public ResponseEntity<UpdateUsernameResponse> updateUsername(@Valid @RequestBody UpdateUsernameRequest request) {
        LOGGER.info("Updating username - request: {}", request);

        var userId = AuthenticatedUserResolver.getUserId();

        var dto = new UpdateUsernameInputDTO(userId, request.newUsername());
        var output = updateUsernameUseCase.execute(dto);

        var response = UpdateUsernameResponse.of(output);

        LOGGER.info("Updating username - response: {}", response);
        return ResponseEntity.ok(response);
    }
}
