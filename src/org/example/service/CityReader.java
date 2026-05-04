package org.example.service;

import org.example.data.*;
import org.example.validate.CoordinatesValidator;
import org.example.validate.InputValidator;

import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import static org.example.validate.InputValidator.*;

/**
 * Ввод и проверка полей города с консоли.
 */

public class CityReader {

    public static Scanner scanner = new Scanner(System.in);

    public static void setScanner(Scanner scanner) {
        CityReader.scanner = scanner;
    }

    public static City readCity() throws IllegalArgumentException {
        Scanner scanner = CityReader.scanner;
        final int MAX_ATTEMPTS = 100;
        int attempts = 0;
        City city = new City();

        while(attempts < MAX_ATTEMPTS) {
            System.out.println("Entering city data (attempt " + (attempts + 1) + ") ===");
            try {
                city.setName(readName(scanner));
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }
        }

        while(attempts < MAX_ATTEMPTS) {
            System.out.println("Enter the coordinates:");
            try {
                float x = readCoordinateX(scanner);
                double y = readCoordinateY(scanner);
                Coordinates coordinates = new Coordinates(x, y);
                CoordinatesValidator.validateCoordinates(coordinates);
                city.setCoordinates(coordinates);
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }
        }

        while(attempts < MAX_ATTEMPTS) {
            try {
                double area = readDoubleArea(scanner);
                InputValidator.validateArea(area);
                city.setArea(area);
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }
        }

        while(attempts < MAX_ATTEMPTS) {
            try{
                int population = readIntPollution(scanner);
                InputValidator.validatePopulation(population);
                city.setPopulation(population);
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }
        }

        while(attempts < MAX_ATTEMPTS){
            try{
                Climate climate = readEnumClimate(scanner);
                city.setClimate(climate);
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }
        }

        while(attempts < MAX_ATTEMPTS){
            try{
                int metersAboveSeaLevel = readSeaLevel(scanner);
                city.setMetersAboveSeaLevel(metersAboveSeaLevel);
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }
        }

        while(attempts < MAX_ATTEMPTS){
            try {
                Government government = readEnumGovernment(scanner);
                city.setGovernment(government);
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }

        }

        while(attempts < MAX_ATTEMPTS){
            try{
                StandardOfLiving standardOfLiving = readEnumStandard(scanner);
                city.setStandardOfLiving(standardOfLiving);
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }
        }

        while(attempts < MAX_ATTEMPTS){
            try{
                Human governor = null;
                if (readYesNo(scanner)) {
                    int date = 0;
                    while(date < MAX_ATTEMPTS){
                        try{
                            java.util.Date birthday = readDate(scanner);
                            governor = new Human(birthday);
                            break;
                        }
                        catch(IllegalArgumentException e) {
                            System.err.println("Error validation: " + e.getMessage());
                            System.out.println("Try to enter the date again.\n");
                            date++;
                        }
                    }

                    if (governor == null) {
                        throw new IllegalArgumentException("Too many failed attempts for birthday");
                    }
                }

                city.setGovernor(governor);
                break;
            }
            catch(IllegalArgumentException e) {
                System.err.println("Error validation: " + e.getMessage());
                System.out.println("Try to enter the data again.\n");
                attempts++;
            }
        }
        // creationDate генерируется автоматически — локальное время системы в момент создания объекта
        city.setCreationDate(new Date());

        // ID and creationDate are assigned on server side for network mode.
        city.setId(0L);

        if(attempts == MAX_ATTEMPTS) {
            System.out.println("You've used a lot of input attempts, repeat after 10 minutes.");
            Runtime.getRuntime().exit(0);
        }

        System.out.println("The data is accepted!");
        return city;
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

