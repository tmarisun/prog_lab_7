package org.example.service;

import org.example.data.*;
import org.example.validate.CoordinatesValidator;
import org.example.validate.InputValidator;

import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.function.Supplier;

import static org.example.validate.InputValidator.*;

/**
 * Ввод и проверка полей города с консоли.
 */

public class CityReader {

    public static Scanner scanner = new Scanner(System.in);
    private static final int MAX_ATTEMPTS = 100;

    public static void setScanner(Scanner scanner) {
        CityReader.scanner = scanner;
    }

    public static City readCity() throws IllegalArgumentException {
        Scanner scanner = CityReader.scanner;
        City city = new City();

        city.setName(retry("Entering city data", () -> readName(scanner)));
        city.setCoordinates(retry("Enter the coordinates", () -> {
            float x = readCoordinateX(scanner);
            double y = readCoordinateY(scanner);
            Coordinates coordinates = new Coordinates(x, y);
            CoordinatesValidator.validateCoordinates(coordinates);
            return coordinates;
        }));
        city.setArea(retry(null, () -> readDoubleArea(scanner)));
        city.setPopulation(retry(null, () -> readIntPollution(scanner)));
        city.setClimate(retry(null, () -> readEnumClimate(scanner)));
        city.setMetersAboveSeaLevel(retry(null, () -> readSeaLevel(scanner)));
        city.setGovernment(retry(null, () -> readEnumGovernment(scanner)));
        city.setStandardOfLiving(retry(null, () -> readEnumStandard(scanner)));
        city.setGovernor(retry(null, () -> readGovernor(scanner)));

        city.setCreationDate(new Date());
        city.setId(null);

        System.out.println("The data is accepted!");
        return city;
    }

    private static Human readGovernor(Scanner scanner) {
        if (!readYesNo(scanner)) {
            return null;
        }
        Date birthday = retry(null, () -> readDate(scanner));
        return new Human(birthday);
    }

    private static <T> T retry(String prompt, Supplier<T> action) {
        for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
            if (prompt != null && !prompt.isBlank()) {
                System.out.println(prompt + " (attempt " + (attempts + 1) + ") ===");
            }
            try {
                return action.get();
            } catch (IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
            }
        }
        throw new IllegalArgumentException("Too many failed attempts");
    }

    private static String readName(Scanner scanner) throws IllegalArgumentException {
        System.out.print("Enter the name of the city: ");
        String input = scanner.nextLine();
        return validateName(input);
    }

    private static float readCoordinateX(Scanner scanner) throws IllegalArgumentException {
        System.out.print("  X: ");
        String line = scanner.nextLine();
        try {
            return validateX(Float.parseFloat(line));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Type Error!");
        }
    }

    private static int readSeaLevel(Scanner scanner) throws IllegalArgumentException {
        System.out.print("Enter the height above sea level: ");
        String line = scanner.nextLine();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Type Error!");
        }
    }

    private static double readCoordinateY(Scanner scanner) throws IllegalArgumentException {
        System.out.print("  Y: ");
        String line = scanner.nextLine();
        try {
            return validateY(Double.parseDouble(line));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Type Error!");
        }
    }

    private static Integer readIntPollution(Scanner scanner) throws IllegalArgumentException {
        System.out.print("Enter the city's population: ");
        String line = scanner.nextLine();
        try {
            Integer population = Integer.parseInt(line);
            validatePopulation(population);
            return population;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Type Error!");
        }
    }

    private static double readDoubleArea(Scanner scanner) throws IllegalArgumentException {
        System.out.print("Enter the area of the city: ");
        String line = scanner.nextLine();
        try {
            double ar = Double.parseDouble(line);
            InputValidator.validateArea(ar);
            return ar;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Type Error!");
        }
    }

    private static Climate readEnumClimate(Scanner scanner) throws IllegalArgumentException {
        System.out.println("Available values: " + Arrays.toString(Climate.values()));
        System.out.print("Select the climate (Enter to skip): ");
        String climateInput = scanner.nextLine();

        return InputValidator.validateEnum(
                climateInput.isEmpty() ? null : climateInput,
                Climate.class,
                "climate",
                false
        );
    }

    private static Government readEnumGovernment(Scanner scanner) throws IllegalArgumentException {
        System.out.println("Available values: " + Arrays.toString(Government.values()));
        System.out.println("Choose a form of government: ");
        String govInput = scanner.nextLine();

        return InputValidator.validateEnum(
                govInput,
                Government.class,
                "government",
                true
        );
    }

    private static StandardOfLiving readEnumStandard(Scanner scanner) throws IllegalArgumentException {
        System.out.println("Available values: " + Arrays.toString(StandardOfLiving.values()));
        System.out.print("Select the standard of living (Enter to skip): ");
        String input = scanner.nextLine();

        return InputValidator.validateEnum(
                input.isEmpty() ? null : input,
                StandardOfLiving.class,
                "StandardOfLiving",
                false
        );

    }

    private static boolean readYesNo(Scanner scanner) {
        System.out.print("Add a governor? (y/n): ");
        String input = scanner.nextLine();
        return input.equalsIgnoreCase("y")
                || input.equalsIgnoreCase("yes");
    }

    private static Date readDate(Scanner scanner) throws IllegalArgumentException {
        System.out.print("Enter your birthday (yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss): ");
        return InputValidator.validateBirthday(scanner.nextLine());
    }


}

