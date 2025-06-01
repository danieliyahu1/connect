package com.connect.auth.exception.handler;

import com.connect.auth.exception.PasswordNotMatchException;
import com.connect.auth.exception.InvalidRefreshTokenException;
import com.connect.auth.exception.UnauthorizedException;
import com.connect.auth.exception.UserExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, String>> handleRefreshTokenNotFoundException(InvalidRefreshTokenException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Refresh Token Not Found");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(WrongThreadException.class)
    public ResponseEntity<Map<String, String>> handleWrongThreadException(WrongThreadException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Wrong Thread");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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
}
