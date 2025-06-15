package com.connect.connector.exception;

public class ConnectorNotFoundException extends RuntimeException {
    public ConnectorNotFoundException(String message) {
        super(message);
    }
}
