package com.jpmns.task.core.presentation.controller.payload.user.response;

import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO;
import com.jpmns.task.core.presentation.controller.documentation.payload.user.response.UpdateUsernameResponseDoc;

public record UpdateUsernameResponse(
        String id,
        String username
) implements UpdateUsernameResponseDoc {

    public static UpdateUsernameResponse of(UpdateUsernameOutputDTO dto) {
        return new UpdateUsernameResponse(dto.id(), dto.username());
    }
}
