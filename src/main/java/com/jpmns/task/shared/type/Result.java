package com.jpmns.task.shared.type;

public class Result<T, E> {

    private final T value;
    private final E error;
    private final boolean success;

    private Result(T value, E error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }

    public static <T, E> Result<T, E> success(T value) {
        return new Result<>(value, null, true);
    }

    public static <T, E> Result<T, E> fail(E error) {
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

    public E getError() {
        if (success) {
            throw new IllegalStateException("The result is a success, error does not exist");
        }

        return error;
    }
}
