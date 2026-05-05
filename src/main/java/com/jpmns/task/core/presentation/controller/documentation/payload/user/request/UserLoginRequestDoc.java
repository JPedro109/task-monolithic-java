package com.jpmns.task.core.presentation.controller.documentation.payload.user.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserLoginRequest", description = "Credenciais para autenticação do usuário")
public interface UserLoginRequestDoc {

    @Schema(description = "Nome de usuário cadastrado na plataforma", example = "joao_silva")
    String username();

    @Schema(description = "Senha do usuário", example = "senha@123")
    String password();
}
