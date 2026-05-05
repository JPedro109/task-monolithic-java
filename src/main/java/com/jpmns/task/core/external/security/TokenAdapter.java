package com.jpmns.task.core.external.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jpmns.task.core.application.port.security.Token;
import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto;
import com.jpmns.task.core.application.port.security.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenAdapter implements Token {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAdapter.class);

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public TokenAdapter(@Value("${security.jwt.secret}") String secret,
                        @Value("${security.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
                        @Value("${security.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    public String generateAccessToken(String sub) {
        return buildToken(sub, accessTokenExpirationMs, TOKEN_TYPE_ACCESS);
    }

    @Override
    public String generateRefreshToken(String sub) {
        return buildToken(sub, refreshTokenExpirationMs, TOKEN_TYPE_REFRESH);
    }

    @Override
    public DecodeTokenDto tokenValidation(String token) {
        try {
            var claims = parseClaims(token);

            return new DecodeTokenDto(claims.getSubject());
        } catch (Exception e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException();
        }
    }

    private String buildToken(String sub, long expirationMs, String tokenType) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(sub)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
