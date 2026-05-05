package com.jpmns.task.core.presentation.controller.documentation.payload.user.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateUserRequest", description = "Dados para criação de um novo usuário")
public interface CreateUserRequestDoc {

    @Schema(description = "Nome de usuário único na plataforma. Entre 3 e 50 caracteres.", example = "joao_silva", minLength = 3, maxLength = 50)
    String username();

    @Schema(description = "Senha do usuário. Mínimo de 8 caracteres.", example = "senha@123", minLength = 8)
    String password();
}
