package com.jpmns.task.core.presentation.controller.documentation.payload.user.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateUsernameRequest", description = "Dados para atualização do username do usuário autenticado")
public interface UpdateUsernameRequestDoc {

    @Schema(description = "Novo username desejado. Entre 3 e 50 caracteres e deve ser único na plataforma.", example = "joao_silva_novo", minLength = 3, maxLength = 50)
    String newUsername();
}
