package com.connect.auth.exception;

public class RefreshTokenNotFoundException extends Exception {
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
