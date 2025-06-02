package com.connect.auth.exception;

public class InvalidAccessTokenException extends Exception {
    public InvalidAccessTokenException(String message) {
        super(message);
    }
}
