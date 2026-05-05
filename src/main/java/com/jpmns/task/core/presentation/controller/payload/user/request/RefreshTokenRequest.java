package com.jpmns.task.core.presentation.controller.payload.user.request;

import jakarta.validation.constraints.NotBlank;

import com.jpmns.task.core.presentation.controller.documentation.payload.task.request.RefreshTokenRequestDoc;

public record RefreshTokenRequest(
        @NotBlank String refreshToken
) implements RefreshTokenRequestDoc {
    @Override
    public String toString() {
        return "RefreshTokenRequest{refreshToken='[PROTECTED]'}";
    }
}
