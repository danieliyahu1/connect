package com.connect.connector.exception.handler;

import com.connect.connector.exception.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.connect.connector.constants.ConnectorServiceConstants.ERROR;
import static com.connect.connector.constants.ConnectorServiceConstants.MESSAGE;

@RestControllerAdvice
public class ConnectorExceptionHandler {

    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String errorConstant, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(ERROR, errorConstant);
        errorResponse.put(MESSAGE, message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ExistingConnectorException.class)
    public ResponseEntity<Map<String, String>> handleExistingConnectorException(ExistingConnectorException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, "Existing Connector", ex.getMessage());
    }

    @ExceptionHandler(ConnectorNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleConnectorNotFoundException(ConnectorNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Connector Not Found", ex.getMessage());
    }

    @ExceptionHandler(ImageIndexOutOfBoundException.class)
    public ResponseEntity<Map<String, String>> handleImageIndexOutOfBoundException(ImageIndexOutOfBoundException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Image Index Out of Bound", ex.getMessage());
    }

    @ExceptionHandler(ProfilePictureRequiredException.class)
    public ResponseEntity<Map<String, String>> handleProfilePictureRequiredException(ProfilePictureRequiredException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Profile Picture Required", ex.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleImageNotFoundException(ImageNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Image Not Found", ex.getMessage());
    }

    @ExceptionHandler(InvalidProfileUrlException.class)
    public ResponseEntity<Map<String, String>> handleInvalidProfileUrlException(InvalidProfileUrlException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Profile URL", ex.getMessage());
    }

    @ExceptionHandler(ConnectorSocialMediaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleConnectorSocialMediaNotFoundException(ConnectorSocialMediaNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Connector Social Media Not Found", ex.getMessage());
    }

    @ExceptionHandler(InvalidImageOrderException.class)
    public ResponseEntity<Map<String, String>> handleInvalidImageOrderException(InvalidImageOrderException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Image Order", ex.getMessage());
    }

    @ExceptionHandler(ExistingSocialMediaPlatformException.class)
    public ResponseEntity<Map<String, String>> handleExistingSocialMediaPlatformException(ExistingSocialMediaPlatformException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, "Existing Social Media Platform", ex.getMessage());
    }

    @ExceptionHandler(ExistingImageException.class)
    public ResponseEntity<Map<String, String>> handleExistingImageException(ExistingImageException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, "Existing Image", ex.getMessage());
    }

    @ExceptionHandler(MediaStorageSignatureGenerationException.class)
    public ResponseEntity<Map<String, String>> handleSignatureGenerationException(MediaStorageSignatureGenerationException ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Signature Generation Failed", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Invalid input");

        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", message);
    }

    @ExceptionHandler(IllegalEnumException.class)
    public ResponseEntity<Map<String, String>> handleIllegalEnumException(IllegalEnumException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Illegal Enum Value", ex.getMessage());
    }
}
