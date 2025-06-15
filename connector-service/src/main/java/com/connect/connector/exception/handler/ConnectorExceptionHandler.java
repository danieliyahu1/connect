package com.connect.connector.exception.handler;

import com.connect.connector.exception.*;
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

        @ExceptionHandler(ConnectorNotFoundException.class)
        public ResponseEntity<Map<String, String>> handleConnectorNotFoundException(ConnectorNotFoundException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Connector Not Found");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        @ExceptionHandler(ImageIndexOutOfBoundException.class)
        public ResponseEntity<Map<String, String>> handleImageIndexOutOfBoundException(ImageIndexOutOfBoundException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Image Index Out of Bound");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(ProfilePictureRequiredException.class)
        public ResponseEntity<Map<String, String>> handleProfilePictureRequiredException(ProfilePictureRequiredException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Profile Picture Required");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(ImageNotFoundException.class)
        public ResponseEntity<Map<String, String>> handleImageNotFoundException(ImageNotFoundException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Image Not Found");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
}
