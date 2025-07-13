package com.connect.connector.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access= AccessLevel.PRIVATE)
public class ConnectorServiceConstants {

    // cloudinary constants
    public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    public static final String KEY_FOLDER = "folder";
    public static final String KEY_PUBLIC_ID = "public_id";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String SYMBOL_DELIMITER = "&";
    public static final String SYMBOL_ASSIGNMENT = "=";

    // image service constants
    public static final int GALLERY_MIN_INDEX = 0;
    public static final int GALLERY_MAX_INDEX = 4;
    public static final int GALLERY_MAX_SIZE = GALLERY_MAX_INDEX + 1;

    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
}
