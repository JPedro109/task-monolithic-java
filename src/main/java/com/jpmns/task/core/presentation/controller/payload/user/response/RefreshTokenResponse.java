package com.jpmns.task.core.presentation.controller.payload.user.response;

import com.jpmns.task.core.application.usecase.user.dto.output.RefreshUserTokenOutputDTO;
import com.jpmns.task.core.presentation.controller.documentation.payload.user.response.RefreshTokenResponseDoc;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) implements RefreshTokenResponseDoc {

    public static RefreshTokenResponse of(RefreshUserTokenOutputDTO dto) {
        return new RefreshTokenResponse(dto.accessToken(), dto.refreshToken());
    }

    @Override
    public String toString() {
        return "RefreshTokenResponse{accessToken='[PROTECTED]', refreshToken='[PROTECTED]'}";
    }
}
