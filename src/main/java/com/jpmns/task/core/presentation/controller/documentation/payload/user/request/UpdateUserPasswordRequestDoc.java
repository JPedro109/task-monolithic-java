package com.jpmns.task.core.presentation.controller.documentation.payload.user.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateUserPasswordRequest", description = "Dados para atualização da senha do usuário autenticado")
public interface UpdateUserPasswordRequestDoc {

    @Schema(description = "Senha atual do usuário, necessária para confirmar a identidade", example = "senha@123")
    String currentPassword();

    @Schema(description = "Nova senha desejada. Mínimo de 8 caracteres.", example = "novaSenha@456", minLength = 8)
    String newPassword();
}
