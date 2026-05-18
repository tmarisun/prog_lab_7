package org.example.client.fx.util;

import org.example.data.City;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Сравнение списков городов для polling (без полного SHOW diff). */
public final class CityListFingerprint {

    private CityListFingerprint() {}

    public static String of(List<City> cities) {
        return cities.stream()
                .filter(c -> c.getId() != null)
                .sorted(Comparator.comparing(City::getId))
                .map(c -> c.getId()
                        + "|" + nullSafe(c.getName())
                        + "|" + c.getPopulation()
                        + "|" + c.getArea()
                        + "|" + nullSafe(c.getOwnerLogin()))
                .collect(Collectors.joining(";"));
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
