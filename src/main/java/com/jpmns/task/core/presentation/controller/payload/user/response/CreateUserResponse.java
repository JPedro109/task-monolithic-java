package com.jpmns.task.core.presentation.controller.payload.user.response;

import com.jpmns.task.core.application.usecase.user.dto.output.CreateUserOutputDTO;
import com.jpmns.task.core.presentation.controller.documentation.payload.user.response.CreateUserResponseDoc;

public record CreateUserResponse(
        String id,
        String username
) implements CreateUserResponseDoc {

    public static CreateUserResponse of(CreateUserOutputDTO dto) {
        return new CreateUserResponse(dto.id(), dto.username());
    }
}
