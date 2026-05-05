package com.jpmns.task.core.presentation.controller.documentation.payload.user.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RefreshTokenResponse", description = "Novo par de tokens JWT gerado após renovação bem-sucedida")
public interface RefreshTokenResponseDoc {

    @Schema(description = "Novo token de acesso de curta duração", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.newAccess")
    String accessToken();

    @Schema(description = "Novo token de renovação. O token anterior é invalidado após o uso.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.newRefresh")
    String refreshToken();
}
