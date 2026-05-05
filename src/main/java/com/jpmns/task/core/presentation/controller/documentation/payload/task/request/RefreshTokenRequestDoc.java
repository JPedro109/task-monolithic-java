package com.jpmns.task.core.presentation.controller.documentation.payload.task.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RefreshTokenRequest", description = "Token de renovação para geração de um novo par de tokens JWT")
public interface RefreshTokenRequestDoc {

    @Schema(description = "Refresh token válido obtido no login ou na última renovação", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.xyz789")
    String refreshToken();
}
