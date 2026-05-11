package com.jpmns.task.core.presentation.controller.common.filter;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jpmns.task.core.presentation.controller.common.resolver.AuthenticatedUserResolver;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.context.Scope;

@Component
public class TracingContextFilter extends OncePerRequestFilter {

    private static final String HEADER_CORRELATION_ID = "correlation-id";

    private static final String MDC_USER_ID = "user-id";
    private static final String MDC_CORRELATION_ID = "correlation-id";
    private static final String MDC_PATH = "path";
    private static final String MDC_METHOD = "method";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var correlationId = request.getHeader(HEADER_CORRELATION_ID);

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        var baggageBuilder = Baggage.builder();

        if (!isApplicationRequest(request)) {
            populate(request, baggageBuilder, correlationId);
        }

        try (Scope ignored = baggageBuilder.build().makeCurrent()) {
            filterChain.doFilter(request, response);
        } finally {
            clear();
        }
    }

    private Boolean isApplicationRequest(HttpServletRequest request) {
        var uri = getUri(request);

        return uri.contains("swagger") || uri.contains("docs");
    }

    private void populate(HttpServletRequest request, BaggageBuilder baggageBuilder, String correlationId) {
        var userId = AuthenticatedUserResolver.getUserIdOrNull();
        if (userId != null && !userId.isBlank()) {
            MDC.put(MDC_USER_ID, userId);
            baggageBuilder.put(MDC_USER_ID, userId);
        }

        MDC.put(MDC_CORRELATION_ID, correlationId);
        baggageBuilder.put(MDC_CORRELATION_ID, correlationId);

        var uri = getUri(request);
        MDC.put(MDC_PATH, uri);
        baggageBuilder.put(MDC_PATH, uri);

        var method = getMethod(request);
        MDC.put(MDC_METHOD, method);
        baggageBuilder.put(MDC_METHOD, method);
    }

    private void clear() {
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_CORRELATION_ID);
        MDC.remove(MDC_PATH);
        MDC.remove(MDC_METHOD);
    }

    private String getUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private String getMethod(HttpServletRequest request) {
        return request.getMethod();
    }
}
