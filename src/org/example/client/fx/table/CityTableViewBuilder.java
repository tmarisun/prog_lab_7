package org.example.client.fx.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.client.fx.i18n.I18n;
import org.example.data.City;
import org.example.data.Climate;
import org.example.data.Government;
import org.example.data.StandardOfLiving;

import java.util.ArrayList;
import java.util.List;

public final class CityTableViewBuilder {

    private CityTableViewBuilder() {}

    public static TableView<City> createTable() {
        TableView<City> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(buildColumns());
        return table;
    }

    public static void refreshHeaders(TableView<City> table) {
        for (int i = 0; i < CityTableColumn.values().length && i < table.getColumns().size(); i++) {
            CityTableColumn col = CityTableColumn.values()[i];
            table.getColumns().get(i).setText(I18n.get(col.getHeaderKey()));
        }
    }

    private static List<TableColumn<City, ?>> buildColumns() {
        List<TableColumn<City, ?>> cols = new ArrayList<>();
        for (CityTableColumn def : CityTableColumn.values()) {
            cols.add(column(def));
        }
        return cols;
    }

    private static TableColumn<City, String> column(CityTableColumn def) {
        TableColumn<City, String> col = new TableColumn<>(I18n.get(def.getHeaderKey()));
        col.setCellValueFactory(cell -> {
            City city = cell.getValue();
            if (city == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(formatCell(def, city));
        });
        col.setComparator(null);
        return col;
    }

    private static String formatCell(CityTableColumn def, City city) {
        if (def == CityTableColumn.ID) {
            if (city.getId() != null) {
                return String.valueOf(city.getId());
            }
            return "";
        }
        if (def == CityTableColumn.NAME) {
            return nullToEmpty(city.getName());
        }
        if (def == CityTableColumn.COORD_X) {
            if (city.getCoordinates() != null) {
                return I18n.formatNumber(city.getCoordinates().getX());
            }
            return "";
        }
        if (def == CityTableColumn.COORD_Y) {
            if (city.getCoordinates() != null) {
                return I18n.formatNumber(city.getCoordinates().getY());
            }
            return "";
        }
        if (def == CityTableColumn.CREATION_DATE) {
            return I18n.formatDate(city.getCreationDate());
        }
        if (def == CityTableColumn.AREA) {
            return I18n.formatNumber(city.getArea());
        }
        if (def == CityTableColumn.POPULATION) {
            return I18n.formatNumber(city.getPopulation());
        }
        if (def == CityTableColumn.METERS) {
            return String.valueOf(city.getMetersAboveSeaLevel());
        }
        if (def == CityTableColumn.CLIMATE) {
            return enumLabel(city.getClimate(), "climate.");
        }
        if (def == CityTableColumn.GOVERNMENT) {
            return enumLabel(city.getGovernment(), "government.");
        }
        if (def == CityTableColumn.STANDARD_OF_LIVING) {
            return enumLabel(city.getStandardOfLiving(), "standardOfLiving.");
        }
        if (def == CityTableColumn.GOVERNOR) {
            if (city.getGovernor() != null) {
                return I18n.formatDate(city.getGovernor().birthday());
            }
            return "";
        }
        if (def == CityTableColumn.OWNER) {
            return nullToEmpty(city.getOwnerLogin());
        }
        return "";
    }

    private static String enumLabel(Enum<?> value, String prefix) {
        if (value == null) {
            return "";
        }
        return I18n.get(prefix + value.name());
    }

    private static String nullToEmpty(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }
}
