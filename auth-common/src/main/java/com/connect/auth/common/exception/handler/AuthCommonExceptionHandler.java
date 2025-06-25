package com.connect.auth.common.exception.handler;

import com.connect.auth.common.exception.AuthCommonInvalidAccessTokenException;
import com.connect.auth.common.exception.AuthCommonInvalidTokenException;
import com.connect.auth.common.exception.AuthCommonUnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AuthCommonExceptionHandler {
    @ExceptionHandler(AuthCommonUnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(AuthCommonUnauthorizedException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AuthCommonInvalidAccessTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAccessTokenException(AuthCommonInvalidAccessTokenException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid Access Token");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AuthCommonInvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTokenException(AuthCommonInvalidTokenException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid Token");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
