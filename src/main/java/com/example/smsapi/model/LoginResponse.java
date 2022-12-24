package com.example.smsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    @JsonProperty("account")
    private Account account;
    @JsonProperty("token")
    private String token;
    @JsonProperty("message")
    private String message;

    public LoginResponse() {
    }

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
