package org.example.client.fx.table;

import org.example.data.City;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Фильтрация и сортировка коллекции городов через Streams API.
 */
public final class CityTablePipeline {

    private CityTablePipeline() {}

    public static List<City> apply(
            List<City> source,
            CityTableColumn filterColumn,
            String filterText,
            CityTableColumn sortColumn,
            boolean ascending
    ) {
        Stream<City> stream = source.stream();

        if (filterColumn != null && filterText != null && !filterText.isBlank()) {
            String needle = filterText.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(city -> matches(city, filterColumn, needle));
        }

        if (sortColumn != null) {
            Comparator<City> cmp = sortColumn.comparator(ascending);
            stream = stream.sorted(cmp);
        }

        return stream.collect(Collectors.toList());
    }

    private static boolean matches(City city, CityTableColumn column, String needle) {
        String haystack = column.displayValue(city).toLowerCase(Locale.ROOT);
        return haystack.contains(needle);
    }
}
