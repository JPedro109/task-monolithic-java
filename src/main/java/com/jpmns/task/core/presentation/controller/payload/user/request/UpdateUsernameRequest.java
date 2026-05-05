package com.jpmns.task.core.presentation.controller.payload.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.jpmns.task.core.presentation.controller.documentation.payload.user.request.UpdateUsernameRequestDoc;

public record UpdateUsernameRequest(
        @NotBlank @Size(min = 3, max = 50) String newUsername
) implements UpdateUsernameRequestDoc {
    @Override
    public String toString() {
        return "UpdateUsernameRequest{newUsername='" + newUsername + "'}";
    }
}
