package com.jpmns.task.core.external.security.filter;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jpmns.task.core.application.port.security.Token;
import com.jpmns.task.core.application.port.security.dto.DecodeTokenDto;
import com.jpmns.task.core.application.usecase.user.dto.input.GetUserByIdInputDTO;
import com.jpmns.task.core.application.usecase.user.interfaces.GetUserByIdUseCase;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final Token token;
    private final GetUserByIdUseCase getUserByIdUseCase;

    public JwtAuthenticationFilter(Token tokenProvider, GetUserByIdUseCase getUserByIdUseCase) {
        this.token = tokenProvider;
        this.getUserByIdUseCase = getUserByIdUseCase;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = authHeader.substring(BEARER_PREFIX.length());

        DecodeTokenDto decodeTokenDto;

        try {
            decodeTokenDto = this.token.tokenValidation(token);
        } catch (Exception ex) {
            filterChain.doFilter(request, response);
            return;
        }

        var sub = decodeTokenDto.sub();

        if (sub != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var input = new GetUserByIdInputDTO(sub);
            var user = getUserByIdUseCase.execute(input);

            var authentication = new UsernamePasswordAuthenticationToken(user.id(),
                    null,
                    Collections.emptyList());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
