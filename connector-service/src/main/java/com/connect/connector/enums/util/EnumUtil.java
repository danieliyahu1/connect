package com.akatsuki.connector.enums.util;

import com.akatsuki.connector.exception.IllegalEnumException;

public class EnumUtil {

    public static <E extends Enum<E>> E getEnumFromDisplayName(Class<E> enumClass, String displayName) throws IllegalEnumException {
        String normalized = displayName
                .toUpperCase()
                .replaceAll("[-\\s]", "_"); // replace dash or space with underscore

        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalEnumException("Unknown enum name: " + displayName);
        }
        catch (NullPointerException e) {
            throw new IllegalEnumException("Display name is invalid: " + displayName);
        }
    }
}
