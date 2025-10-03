package com.akatsuki.auth.exception.handler;

import com.akatsuki.auth.common.exception.AuthCommonInvalidRefreshTokenException;
import com.akatsuki.auth.common.exception.AuthCommonUnauthorizedException;
import com.akatsuki.auth.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(AuthCommonUnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(AuthCommonUnauthorizedException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UnsupportedProviderException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedProviderException(UnsupportedProviderException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unsupported Provider");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthCommonInvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, String>> handleRefreshTokenNotFoundException(AuthCommonInvalidRefreshTokenException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Refresh Token Not Found");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UserExistException.class)
    public ResponseEntity<Map<String, String>> handleUserExistException(UserExistException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "User Already Exists");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<Map<String, String>> handlePasswordNotMatchException(PasswordNotMatchException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Password Mismatch");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

//    @ExceptionHandler(InvalidTokenException.class)
//    public ResponseEntity<Map<String, String>> handleInvalidTokenException(InvalidTokenException ex) {
//        Map<String, String> errorResponse = new HashMap<>();
//        errorResponse.put("error", "Invalid Token");
//        errorResponse.put("message", ex.getMessage());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
//    }

    @ExceptionHandler(WrongProviderException.class)
    public ResponseEntity<Map<String, String>> handleWrongProviderException(WrongProviderException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Wrong Provider");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
