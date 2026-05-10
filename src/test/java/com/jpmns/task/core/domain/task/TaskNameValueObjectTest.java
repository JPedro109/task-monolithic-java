package com.jpmns.task.core.domain.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.jpmns.task.core.domain.task.valueobject.TaskNameValueObject;

class TaskNameValueObjectTest {

    @Test
    @DisplayName("Should create a valid TaskNameValueObject")
    void shouldCreateValidTaskName() {
        var name = "Buy groceries";

        var result = TaskNameValueObject.of(name);

        assertThat(result.isFail()).isFalse();
        assertThat(result.getValue().asString()).isEqualTo(name);
    }

    @Test
    @DisplayName("Should accept a name with exactly 255 characters")
    void shouldAcceptExactMaxLength() {
        var name = "a".repeat(255);

        var result = TaskNameValueObject.of(name);

        assertThat(result.isFail()).isFalse();
    }

    @Test
    @DisplayName("Should accept a single character name")
    void shouldAcceptSingleCharacterName() {
        var name = "X";

        var result = TaskNameValueObject.of(name);

        assertThat(result.isFail()).isFalse();
    }

    @Test
    @DisplayName("Should fail when name exceeds 255 characters")
    void shouldFailWhenNameExceedsMaxLength() {
        var name = "a".repeat(256);

        var result = TaskNameValueObject.of(name);

        assertThat(result.isFail()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Should fail for null, empty or blank names")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldFailForBlankOrNullName(String name) {
        var result = TaskNameValueObject.of(name);

        assertThat(result.isFail()).isTrue();
    }
}
