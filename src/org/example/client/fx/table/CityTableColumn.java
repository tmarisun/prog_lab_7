package org.example.client.fx.table;

import org.example.data.City;
import org.example.data.Climate;
import org.example.data.Coordinates;
import org.example.data.Government;
import org.example.data.Human;
import org.example.data.StandardOfLiving;

import java.util.Comparator;
import java.util.Date;
import java.util.function.Function;

/**
 * Поля таблицы для фильтрации и сортировки.
 */
public enum CityTableColumn {
    ID("col.id", City::getId, Comparator.comparing(City::getId, Comparator.nullsLast(Comparator.naturalOrder()))),
    NAME("col.name", City::getName, Comparator.comparing(City::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))),
    COORD_X("col.x", CityTableColumn::coordX, Comparator.comparing(CityTableColumn::coordX, Comparator.nullsLast(Comparator.naturalOrder()))),
    COORD_Y("col.y", CityTableColumn::coordY, Comparator.comparing(CityTableColumn::coordY, Comparator.nullsLast(Comparator.naturalOrder()))),
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
    GOVERNOR("col.governor", CityTableColumn::governorText,
            Comparator.comparing(CityTableColumn::governorText, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))),
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
        if (ascending) {
            return comparator;
        }
        return comparator.reversed();
    }

    private static Float coordX(City city) {
        Coordinates coords = city.getCoordinates();
        if (coords != null) {
            return coords.getX();
        }
        return null;
    }

    private static Double coordY(City city) {
        Coordinates coords = city.getCoordinates();
        if (coords != null) {
            return coords.getY();
        }
        return null;
    }

    private static String governorText(City city) {
        Human governor = city.getGovernor();
        if (governor != null) {
            return governor.toString();
        }
        return null;
    }
}
