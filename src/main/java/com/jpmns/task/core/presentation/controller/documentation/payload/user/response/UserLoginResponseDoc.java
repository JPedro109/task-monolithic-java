package com.jpmns.task.core.presentation.controller.documentation.payload.user.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserLoginResponse", description = "Par de tokens JWT gerado após autenticação bem-sucedida")
public interface UserLoginResponseDoc {

    @Schema(description = "Token de acesso de curta duração. Envie no header Authorization: Bearer <accessToken>", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.abc123")
    String accessToken();

    @Schema(description = "Token de renovação de longa duração. Use para obter um novo accessToken quando ele expirar.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.xyz789")
    String refreshToken();
}
