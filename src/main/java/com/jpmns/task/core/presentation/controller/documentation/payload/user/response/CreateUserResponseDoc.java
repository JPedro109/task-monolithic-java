package com.jpmns.task.core.presentation.controller.documentation.payload.user.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateUserResponse", description = "Dados do usuário recém-criado")
public interface CreateUserResponseDoc {

    @Schema(description = "Identificador único do usuário (UUID)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    String id();

    @Schema(description = "Username escolhido pelo usuário", example = "joao_silva")
    String username();
}
