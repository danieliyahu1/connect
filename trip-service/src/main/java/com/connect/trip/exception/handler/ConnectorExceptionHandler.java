package com.connect.trip.exception.handler;

import com.connect.trip.exception.TripNotFoundException;
import com.connect.trip.model.Trip;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ConnectorExceptionHandler {

        @ExceptionHandler(TripNotFoundException.class)
        public ResponseEntity<Map<String, String>> handleExistingConnectorException(TripNotFoundException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Trip Not Found");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
}