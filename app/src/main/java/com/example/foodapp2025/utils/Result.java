package com.example.foodapp2025.utils;

import androidx.annotation.Nullable;

public class Result<T> {
    public enum Status {
        SUCCESS, ERROR, LOADING
    }

    private final Status status;
    @Nullable
    private final T data;
    @Nullable
    private final String message;
    @Nullable
    private final Throwable error;

    private Result(Status status, @Nullable T data, @Nullable String message, @Nullable Throwable error) {
        this.status = status;
        this.data = data;
        this.message = message != null ? message : "";
        this.error = error;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, data, "Login successful", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(Status.ERROR, null, message, null);
    }

    public static <T> Result<T> error(String message, Throwable error) {
        return new Result<>(Status.ERROR, null, message, error);
    }

    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null, null);
    }

    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

    @Override
    public String toString() {
        return "Result{" +
                "status=" + status +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", error=" + error +
                '}';
    }
}
