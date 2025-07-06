package com.connect.connector.enums.util;

public class EnumUtil {

    public static <E extends Enum<E>> E getEnumFromDisplayName(Class<E> enumClass, String displayName) {
        String normalized = displayName
                .toUpperCase()
                .replaceAll("[-\\s]", "_"); // replace dash or space with underscore

        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown enum name: " + displayName);
        }
        catch (NullPointerException e) {
            throw new IllegalArgumentException("Display name is invalid: " + displayName);
        }
    }
}
