package com.astinil.AndroidTimesheet.api.model;

public class ApiResponse<T> {

    public int code;
    public boolean success;
    public String message;
    public T data;

    public ApiResponse() {}

    public ApiResponse(int code, boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
