package com.connect.connector.exception.handler;

import com.connect.connector.exception.ExistingConnectorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ConnectorExceptionHandler {

        @ExceptionHandler(ExistingConnectorException.class)
        public ResponseEntity<Map<String, String>> handleExistingConnectorException(ExistingConnectorException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Existing Connector");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
}
