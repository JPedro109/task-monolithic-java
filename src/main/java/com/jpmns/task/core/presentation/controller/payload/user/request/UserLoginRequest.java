package com.jpmns.task.core.presentation.controller.payload.user.request;

import jakarta.validation.constraints.NotBlank;

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.UserLoginRequestDoc;

public record UserLoginRequest(
        @NotBlank String username,
        @NotBlank String password
) implements UserLoginRequestDoc {
    @Override
    public String toString() {
        return "UserLoginRequest{username='" + username + "', password='[PROTECTED]'}";
    }
}
