package org.example.validate;

import org.example.data.City;
import java.io.*;

/**
 * Валидирует объект {@link City}, делегируя проверки {@link InputValidator} и {@link CoordinatesValidator}.
 * Выбрасывает {@link IllegalArgumentException} при нарушении контракта полей.
 * @see InputValidator
 * @see CoordinatesValidator
 */

public class CityValidator {

    public static void validateCity(City city) throws IllegalArgumentException {
        InputValidator.validateId(city.getId());
        InputValidator.validateName(city.getName());
        InputValidator.validateArea(city.getArea());
        InputValidator.validatePopulation(city.getPopulation());
        InputValidator.validateNotNull(city.getCoordinates(), "Coordinates");
        InputValidator.validateNotNull(city.getCreationDate(), "Creation date");
        InputValidator.validateNotNull(city.getGovernment(), "Government");
        CoordinatesValidator.validateCoordinates(city.getCoordinates());
    }

}