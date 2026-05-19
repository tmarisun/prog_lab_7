package org.example.validate;

import org.example.data.City;
import org.example.db.CityRepository;
import java.io.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Утилитный класс для валидации полей {@link City} и связанных типов.
 * Все методы статические, выбрасывают {@link IllegalArgumentException} при нарушении контракта.
 * @see CityValidator
 * @see CoordinatesValidator
 */

public class InputValidator {

    public static void validateUniqueIds(CityRepository cityRepository) throws Exception {
        cityRepository.validateUniqueIdsInDatabase();
    }

    // === Coordinate Constraints ===
    private static final float MIN_X = 0;
    private static final double MIN_Y = 0;
    private static final float MAX_X = 959;
    private static final double MAX_Y = 613.0;

    // === Numeric Constraints ===
    private static final double MIN_AREA = 0.0;
    private static final int MIN_POPULATION = 0;
    private static final long MIN_ID = 0;

    //-------------------------------

    public static void validateId(Long id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (id <= MIN_ID) {
            throw new IllegalArgumentException("ID must be greater than 0 (received: " + id + ")");
        }
    }

    //----------------

    public static Float validateX(Float x) throws IllegalArgumentException {
        if (x == null) {
            throw new IllegalArgumentException("X coordinate cannot be empty");
        }
        if (x < MIN_X) {
            throw new IllegalArgumentException(
                    "X coordinate cannot be negative (received: " + x + ")"
            );
        }
        if (x > MAX_X) {
            throw new IllegalArgumentException(
                    "X coordinate cannot exceed " + MAX_X + " (received: " + x + ")"
            );
        }
        return x;
    }

    public static Double validateY(Double y) throws IllegalArgumentException {
        if (y == null) {
            throw new IllegalArgumentException("Y coordinate cannot be empty");
        }
        if (y < MIN_Y) {
            throw new IllegalArgumentException(
                    "Y coordinate cannot be negative (received: " + y + ")"
            );
        }
        if (y > MAX_Y) {
            throw new IllegalArgumentException(
                    "Y coordinate cannot exceed " + MAX_Y + " (received: " + y + ")"
            );
        }
        return y;
    }

    //-----------------------------------------

    public static void validateCoordinates(float x, double y) throws IllegalArgumentException {
        validateX(x);
        validateY(y);
    }

    //------------------------------------------


    public static void validatePopulation(Integer population) throws IllegalArgumentException {
        if (population == null) {
            throw new IllegalArgumentException("Population cannot be empty");
        }
        if (population <= MIN_POPULATION) {
            throw new IllegalArgumentException("Population must be greater than 0 (received: " + population + ")");
        }
    }


    //-----------------------------

    public static void validateArea(Double area) throws IllegalArgumentException {
        if (area == null) {
            throw new IllegalArgumentException("Area cannot be null");
        }
        if (area <= MIN_AREA) {
            throw new IllegalArgumentException("Area must be greater than 0 (received: " + area + ")");
        }
    }

    //-----------------------------------------


    public static String validateName(String name) throws IllegalArgumentException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return name;
    }

    //-----------------------

    public static java.util.Date validateBirthday(String dateString) throws IllegalArgumentException {
        if (dateString == null || dateString.length() == 0) {
            throw new IllegalArgumentException("Birthday cannot be empty");
        }

        String s = dateString;
        String[] patterns = {"yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss"};
        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(pattern);
                format.setLenient(false);
                return format.parse(s);
            } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Invalid birthday format. Expected: yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss");
    }


    //---------------

    public static <T extends Enum<T>> T validateEnum(
            String value,
            Class<T> enumClass,
            String fieldName,
            boolean required) throws IllegalArgumentException {

        if (value == null || value.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(fieldName + " cannot be empty");
            }
            return null;
        }

        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ". Valid values");
        }
    }


    //-------------------------------

    public static void validateNotNull(Object obj, String fieldName) throws IllegalArgumentException {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }


}