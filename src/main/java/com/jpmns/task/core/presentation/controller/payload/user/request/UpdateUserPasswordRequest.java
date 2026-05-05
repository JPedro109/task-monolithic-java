package com.jpmns.task.core.presentation.controller.payload.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.UpdateUserPasswordRequestDoc;

public record UpdateUserPasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8) String newPassword
) implements UpdateUserPasswordRequestDoc {
    @Override
    public String toString() {
        return "UpdateUserPasswordRequest{currentPassword='[PROTECTED]', newPassword='[PROTECTED]'}";
    }
}
