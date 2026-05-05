package com.jpmns.task.core.application.port.security;

import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto;

public interface Token {

    String generateAccessToken(String sub);

    String generateRefreshToken(String sub);

    DecodeTokenDto tokenValidation(String token);
}
