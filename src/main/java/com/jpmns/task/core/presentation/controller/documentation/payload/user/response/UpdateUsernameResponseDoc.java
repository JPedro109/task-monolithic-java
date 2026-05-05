package com.jpmns.task.core.presentation.controller.documentation.payload.user.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateUsernameResponse", description = "Dados do usuário após atualização do username")
public interface UpdateUsernameResponseDoc {

    @Schema(description = "Identificador único do usuário (UUID)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    String id();

    @Schema(description = "Novo username do usuário", example = "joao_silva_novo")
    String username();
}
