package com.jpmns.task.core.domain.common.valueobject;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class IdValueObjectTest {

    @Test
    @DisplayName("Should create a valid IdValueObject from a UUID string")
    void shouldCreateValidId() {
        var uuid = UUID.randomUUID().toString();

        var result = IdValueObject.of(uuid);

        assertThat(result.isFail()).isFalse();
        assertThat(result.getValue().asString()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should accept an uppercase UUID string")
    void shouldAcceptUpperCaseUUID() {
        var uuid = UUID.randomUUID().toString().toUpperCase();

        var result = IdValueObject.of(uuid);

        assertThat(result.isFail()).isFalse();
    }

    @ParameterizedTest
    @DisplayName("Should fail for null, empty or malformed UUID strings")
    @NullAndEmptySource
    @ValueSource(strings = {
            "not-a-uuid",
            "12345678-1234-1234-1234-12345678901",
            "12345678-1234-1234-1234-1234567890123",
            "12345678_1234_1234_1234_123456789012",
            "gggggggg-gggg-gggg-gggg-gggggggggggg"
    })
    void shouldFailForInvalidId(String id) {
        var result = IdValueObject.of(id);

        assertThat(result.isFail()).isTrue();
    }
}
