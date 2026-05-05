package com.jpmns.task.core.presentation.controller.payload.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.CreateUserRequestDoc;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8) String password
) implements CreateUserRequestDoc {
    @Override
    public String toString() {
        return "CreateUserRequest{username='" + username + "', password='[PROTECTED]'}";
    }
}
