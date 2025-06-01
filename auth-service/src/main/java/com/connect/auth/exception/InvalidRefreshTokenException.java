package com.connect.auth.exception;

public class InvalidRefreshTokenException extends Exception {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
