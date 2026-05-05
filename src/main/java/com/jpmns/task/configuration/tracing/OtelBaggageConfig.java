package com.jpmns.task.configuration.tracing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

@Configuration
@ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "false", matchIfMissing = false)
public class OtelBaggageConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        var baggagePropagator = W3CBaggagePropagator.getInstance();
        var traceContextPropagator = W3CTraceContextPropagator.getInstance();

        var propagator = TextMapPropagator.composite(
                traceContextPropagator,
                baggagePropagator
        );

        var tracerProvider = SdkTracerProvider.builder()
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(propagator))
                .build();
    }
}
