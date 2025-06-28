package com.connect.trip.enums.util;

public class EnumUtil {

    public static <E extends Enum<E>> E fromDisplayName(Class<E> enumClass, String displayName) {
        String normalized = displayName
                .toUpperCase()
                .replaceAll("[-\\s]", "_"); // replace dash or space with underscore

        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown enum name: " + displayName);
        }
    }
}
