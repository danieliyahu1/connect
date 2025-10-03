package com.akatsuki.trip.exception.handler;

import com.akatsuki.trip.exception.InvalidDateException;
import com.akatsuki.trip.exception.OverlapTripException;
import com.akatsuki.trip.exception.IllegalEnumException;
import com.akatsuki.trip.exception.TripNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class TripExceptionHandler {

    @ExceptionHandler(TripNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleExistingConnectorException(TripNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Trip Not Found");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(OverlapTripException.class)
    public ResponseEntity<Map<String, String>> handleExistingTripException(OverlapTripException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Trip Overlap Detected");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalEnumException.class)
    public ResponseEntity<Map<String, String>> handleIllegalEnumException(IllegalEnumException ex)
    {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Illegal destination");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDateException(InvalidDateException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid Date");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}