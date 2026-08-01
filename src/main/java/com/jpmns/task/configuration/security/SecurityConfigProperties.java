package com.jpmns.task.configuration.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityConfigProperties(
        Jwt jwt
) {

    public record Jwt(
            String secret,
            long accessTokenExpirationMs,
            long refreshTokenExpirationMs
    ) { }
}
