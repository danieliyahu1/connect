package com.akatsuki.trip.enums.util;

import com.akatsuki.trip.exception.IllegalEnumException;

public class EnumUtil {

    public static <E extends Enum<E>> E getEnumFromDisplayName(Class<E> enumClass, String displayName) throws IllegalEnumException {
        String normalized = displayName
                .toUpperCase()
                .replaceAll("[-\\s]", "_"); // replace dash or space with underscore

        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalEnumException("Unknown enum name: " + normalized + "\n Unknown display name: " + displayName);
        }
        catch (NullPointerException e) {
            throw new IllegalEnumException("Display name is invalid: " + displayName);
        }
    }
}
