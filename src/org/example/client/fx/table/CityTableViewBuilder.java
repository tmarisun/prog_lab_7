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
        return switch (def) {
            case ID -> city.getId() != null ? String.valueOf(city.getId()) : "";
            case NAME -> nullToEmpty(city.getName());
            case COORD_X -> city.getCoordinates() != null ? I18n.formatNumber(city.getCoordinates().getX()) : "";
            case COORD_Y -> city.getCoordinates() != null ? I18n.formatNumber(city.getCoordinates().getY()) : "";
            case CREATION_DATE -> I18n.formatDate(city.getCreationDate());
            case AREA -> I18n.formatNumber(city.getArea());
            case POPULATION -> I18n.formatNumber(city.getPopulation());
            case METERS -> String.valueOf(city.getMetersAboveSeaLevel());
            case CLIMATE -> enumLabel(city.getClimate(), "climate.");
            case GOVERNMENT -> enumLabel(city.getGovernment(), "government.");
            case STANDARD_OF_LIVING -> enumLabel(city.getStandardOfLiving(), "standardOfLiving.");
            case GOVERNOR -> city.getGovernor() != null ? I18n.formatDate(city.getGovernor().birthday()) : "";
            case OWNER -> nullToEmpty(city.getOwnerLogin());
        };
    }

    private static String enumLabel(Enum<?> value, String prefix) {
        return value == null ? "" : I18n.get(prefix + value.name());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
