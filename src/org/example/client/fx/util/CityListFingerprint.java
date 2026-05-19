package org.example.client.fx.util;

import org.example.data.City;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Сравнение списков городов для polling (без полного SHOW diff). */
public final class CityListFingerprint {

    private CityListFingerprint() {}

    public static String of(List<City> cities) {
        List<City> sorted = new ArrayList<>();
        for (City c : cities) {
            if (c.getId() != null) {
                sorted.add(c);
            }
        }
        sorted.sort(Comparator.comparing(City::getId));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            City c = sorted.get(i);
            sb.append(c.getId());
            sb.append('|');
            sb.append(nullSafe(c.getName()));
            sb.append('|');
            sb.append(c.getPopulation());
            sb.append('|');
            sb.append(c.getArea());
            sb.append('|');
            sb.append(nullSafe(c.getOwnerLogin()));
        }
        return sb.toString();
    }

    private static String nullSafe(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }
}
