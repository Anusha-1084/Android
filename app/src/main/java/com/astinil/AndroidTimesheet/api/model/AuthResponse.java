package com.astinil.AndroidTimesheet.api.model;

public class AuthResponse {
    public int code;
    public boolean success;
    public String message;
    public Data data;

    public static class Data {
        public String role;
        public String accessToken;
        public String refreshToken;
    }
}
