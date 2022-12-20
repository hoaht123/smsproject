package com.example.smsapi.model;

public class LoginResponse {
    private Account account;
    private String token;
    private String message;

    public LoginResponse(Account account, String token, String message) {
        this.account = account;
        this.token = token;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
