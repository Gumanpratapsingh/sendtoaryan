package com.guman.bbc_backend.auth;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String message;
    private boolean newUser;
}