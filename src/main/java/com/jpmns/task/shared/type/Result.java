package com.jpmns.task.shared.type;

public class Result<T> {

    private final T value;
    private final RuntimeException error;
    private final boolean success;

    private Result(T value, RuntimeException error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }

    public static <T> Result<T> fail(RuntimeException error) {
        return new Result<>(null, error, false);
    }

    public boolean isFail() {
        return !success;
    }

    public T getValue() {
        if (!success) {
            throw new IllegalStateException("The result is an error, value does not exist");
        }

        return value;
    }

    public T getValueOrThrow() {
        if (!success) {
            throw error;
        }

        return value;
    }

    public RuntimeException getError() {
        if (success) {
            throw new IllegalStateException("The result is a success, error does not exist");
        }

        return error;
    }
}
