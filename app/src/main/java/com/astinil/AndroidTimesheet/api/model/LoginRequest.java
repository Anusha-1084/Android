package com.astinil.AndroidTimesheet.api.model;

public class LoginRequest {
    public String username;
    public String password;

    public LoginRequest(String u, String p){
        username = u; password = p;
    }
}
