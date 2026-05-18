package org.example.client.fx.table;

import org.example.data.City;
import org.example.data.Climate;
import org.example.data.Government;
import org.example.data.StandardOfLiving;

import java.util.Comparator;
import java.util.Date;
import java.util.function.Function;

/**
 * Поля таблицы для фильтрации и сортировки (Streams API).
 */
public enum CityTableColumn {
    ID("col.id", City::getId, Comparator.comparing(City::getId, Comparator.nullsLast(Comparator.naturalOrder()))),
    NAME("col.name", City::getName, Comparator.comparing(City::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))),
    COORD_X("col.x",
            c -> c.getCoordinates() != null ? c.getCoordinates().getX() : null,
            Comparator.comparing(c -> c.getCoordinates() != null ? c.getCoordinates().getX() : null,
                    Comparator.nullsLast(Comparator.naturalOrder()))),
    COORD_Y("col.y",
            c -> c.getCoordinates() != null ? c.getCoordinates().getY() : null,
            Comparator.comparing(c -> c.getCoordinates() != null ? c.getCoordinates().getY() : null,
                    Comparator.nullsLast(Comparator.naturalOrder()))),
    CREATION_DATE("col.creationDate", City::getCreationDate,
            Comparator.comparing(City::getCreationDate, Comparator.nullsLast(Comparator.comparing(Date::getTime)))),
    AREA("col.area", City::getArea,
            Comparator.comparing(City::getArea, Comparator.nullsLast(Comparator.naturalOrder()))),
    POPULATION("col.population", City::getPopulation, Comparator.comparingInt(City::getPopulation)),
    METERS("col.metersAboveSeaLevel", City::getMetersAboveSeaLevel, Comparator.comparingInt(City::getMetersAboveSeaLevel)),
    CLIMATE("col.climate", City::getClimate,
            Comparator.comparing(City::getClimate, Comparator.nullsLast(Comparator.comparing(Enum::name)))),
    GOVERNMENT("col.government", City::getGovernment,
            Comparator.comparing(City::getGovernment, Comparator.nullsLast(Comparator.comparing(Enum::name)))),
    STANDARD_OF_LIVING("col.standardOfLiving", City::getStandardOfLiving,
            Comparator.comparing(City::getStandardOfLiving, Comparator.nullsLast(Comparator.comparing(Enum::name)))),
    GOVERNOR("col.governor",
            c -> c.getGovernor() != null ? c.getGovernor().toString() : null,
            Comparator.comparing(c -> c.getGovernor() != null ? c.getGovernor().toString() : null,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))),
    OWNER("col.owner", City::getOwnerLogin,
            Comparator.comparing(City::getOwnerLogin, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

    private final String headerKey;
    private final Function<City, Object> extractor;
    private final Comparator<City> comparator;

    CityTableColumn(String headerKey, Function<City, Object> extractor, Comparator<City> comparator) {
        this.headerKey = headerKey;
        this.extractor = extractor;
        this.comparator = comparator;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public Object valueOf(City city) {
        return extractor.apply(city);
    }

    public String displayValue(City city) {
        Object v = valueOf(city);
        if (v == null) {
            return "";
        }
        if (v instanceof Climate climate) {
            return climate.name();
        }
        if (v instanceof Government government) {
            return government.name();
        }
        if (v instanceof StandardOfLiving sol) {
            return sol.name();
        }
        return String.valueOf(v);
    }

    public Comparator<City> comparator(boolean ascending) {
        return ascending ? comparator : comparator.reversed();
    }
}
