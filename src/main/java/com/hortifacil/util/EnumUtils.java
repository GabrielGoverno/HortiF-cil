package com.hortifacil.util;

public class EnumUtils {
    public static <E extends Enum<E>> E enumFromString(Class<E> enumClass, String value) {
        return Enum.valueOf(enumClass, value.toLowerCase());
    }
}
