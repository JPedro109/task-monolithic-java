package com.jpmns.task.configuration.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public class SecurityConfigProperties {

    private final Jwt jwt;

    public SecurityConfigProperties(Jwt jwt) {
        this.jwt = jwt;
    }

    public Jwt jwt() {
        return jwt;
    }

    public record Jwt(
            String secret,
            long accessTokenExpirationMs,
            long refreshTokenExpirationMs
    ) { }
}
