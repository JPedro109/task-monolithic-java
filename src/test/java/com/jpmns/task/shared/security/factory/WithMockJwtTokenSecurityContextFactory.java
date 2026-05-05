package com.jpmns.task.shared.security.factory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.jpmns.task.shared.security.WithJwtTokenMock;

@SuppressWarnings("NullableProblems")
public class WithMockJwtTokenSecurityContextFactory
        implements WithSecurityContextFactory<WithJwtTokenMock> {

    @Override
    public SecurityContext createSecurityContext(WithJwtTokenMock annotation) {
        var httpStatus = annotation.httpStatus();

        if (httpStatus == HttpStatus.UNAUTHORIZED) {
            return unauthorizedContext();
        }

        if (httpStatus == HttpStatus.FORBIDDEN) {
            return forbiddenContext(annotation.sub());
        }

        return authenticatedContext(annotation.sub());
    }

    private SecurityContext unauthorizedContext() {
        return SecurityContextHolder.createEmptyContext();
    }

    private SecurityContext forbiddenContext(String sub) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        var authentication = new UsernamePasswordAuthenticationToken(sub, null);

        context.setAuthentication(authentication);
        return context;
    }

    private SecurityContext authenticatedContext(String sub) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        var authentication = new UsernamePasswordAuthenticationToken(
                sub,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        context.setAuthentication(authentication);
        return context;
    }
}
