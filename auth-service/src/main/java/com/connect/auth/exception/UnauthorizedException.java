package com.connect.auth.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) {
        super(message);
    }
}
