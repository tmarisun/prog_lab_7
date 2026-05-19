package org.example.client.fx.dialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.util.Callback;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.client.fx.i18n.I18n;
import org.example.client.fx.util.CityOwnership;
import org.example.data.*;
import org.example.validate.CoordinatesValidator;
import org.example.validate.InputValidator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Диалог ввода / редактирования города.
 * При ошибке окно не закрывается — данные в полях сохраняются.
 */
public final class CityFormDialog {

    private CityFormDialog() {
    }

    /** @return город или {@code null}, если отмена или ошибка */
    public static City showAddDialog() {
        return showDialog(null);
    }

    /** @return город или {@code null} */
    public static City showEditDialog(City existing, String currentLogin) {
        if (!CityOwnership.isOwnedByUser(existing, currentLogin)) {
            new Alert(Alert.AlertType.WARNING, I18n.get("error.notOwner"), ButtonType.OK).showAndWait();
            return null;
        }
        return showDialog(existing);
    }

    private static City showDialog(City existing) {
        boolean edit = existing != null;
        Dialog<City> dialog = new Dialog<>();
        if (edit) {
            dialog.setTitle(I18n.get("dialog.edit.title"));
        } else {
            dialog.setTitle(I18n.get("dialog.add.title"));
        }
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (edit) {
            okButton.setText(I18n.get("dialog.edit.ok"));
        } else {
            okButton.setText(I18n.get("dialog.add.ok"));
        }
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL))
                .setText(I18n.get("dialog.add.cancel"));

        TextField nameField = new TextField();
        TextField xField = new TextField();
        TextField yField = new TextField();
        TextField areaField = new TextField();
        TextField populationField = new TextField();
        TextField metersField = new TextField("0");

        ComboBox<Climate> climateBox = enumCombo(Climate.values(), "climate.");
        ComboBox<Government> governmentBox = enumCombo(Government.values(), "government.");
        ComboBox<StandardOfLiving> solBox = enumCombo(StandardOfLiving.values(), "standardOfLiving.");

        CheckBox hasGovernor = new CheckBox(I18n.get("field.hasGovernor"));
        DatePicker birthdayPicker = new DatePicker();
        birthdayPicker.setDisable(true);
        hasGovernor.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean selected) {
                birthdayPicker.setDisable(selected == null || !selected);
            }
        });

        if (edit) {
            fillFields(existing, nameField, xField, yField, areaField, populationField, metersField,
                    climateBox, governmentBox, solBox, hasGovernor, birthdayPicker);
        } else {
            climateBox.getSelectionModel().clearSelection();
            solBox.getSelectionModel().clearSelection();
        }

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: #c0392b;");
        errorLabel.setMaxWidth(420);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        GridPane grid = buildGrid(nameField, xField, yField, areaField, populationField, metersField,
                climateBox, governmentBox, solBox, hasGovernor, birthdayPicker);
        VBox root = new VBox(8, grid, errorLabel);
        dialog.getDialogPane().setContent(root);

        final City[] validated = new City[1];

        // Не закрывать диалог, если данные неверные
        okButton.addEventFilter(ActionEvent.ACTION, new javafx.event.EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                try {
                    City built = buildCity(nameField, xField, yField, areaField, populationField, metersField,
                            climateBox, governmentBox, solBox, hasGovernor, birthdayPicker, edit);
                    if (edit) {
                        built.setId(existing.getId());
                        built.setCreationDate(existing.getCreationDate());
                        built.setOwnerLogin(existing.getOwnerLogin());
                        built.setOwnerUserId(existing.getOwnerUserId());
                    }
                    validated[0] = built;
                } catch (IllegalArgumentException ex) {
                    showFormError(errorLabel, formatValidationError(ex));
                    event.consume();
                }
            }
        });

        dialog.setResultConverter(new Callback<ButtonType, City>() {
            @Override
            public City call(ButtonType btn) {
                if (btn == ButtonType.OK) {
                    return validated[0];
                }
                return null;
            }
        });

        Optional<City> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    private static String formatValidationError(IllegalArgumentException ex) {
        if (ex instanceof NumberFormatException) {
            return I18n.get("error.invalidNumber");
        }
        String raw = ex.getMessage();
        if (raw != null && raw.contains("cannot be negative")) {
            return I18n.get("error.coordNegative");
        }
        if (raw != null && !raw.isBlank()) {
            return raw;
        }
        return I18n.get("error.formInvalid");
    }

    private static void showFormError(Label errorLabel, String message) {
        if (message != null && !message.isBlank()) {
            errorLabel.setText(message);
        } else {
            errorLabel.setText(I18n.get("error.formInvalid"));
        }
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private static void fillFields(
            City city,
            TextField nameField, TextField xField, TextField yField, TextField areaField,
            TextField populationField, TextField metersField,
            ComboBox<Climate> climateBox, ComboBox<Government> governmentBox,
            ComboBox<StandardOfLiving> solBox,
            CheckBox hasGovernor, DatePicker birthdayPicker
    ) {
        nameField.setText(city.getName());
        if (city.getCoordinates() != null) {
            xField.setText(String.valueOf(city.getCoordinates().getX()));
            yField.setText(String.valueOf(city.getCoordinates().getY()));
        }
        areaField.setText(String.valueOf(city.getArea()));
        populationField.setText(String.valueOf(city.getPopulation()));
        metersField.setText(String.valueOf(city.getMetersAboveSeaLevel()));
        climateBox.setValue(city.getClimate());
        governmentBox.setValue(city.getGovernment());
        solBox.setValue(city.getStandardOfLiving());
        if (city.getGovernor() != null && city.getGovernor().birthday() != null) {
            hasGovernor.setSelected(true);
            birthdayPicker.setValue(city.getGovernor().birthday().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }
    }

    private static GridPane buildGrid(
            TextField nameField, TextField xField, TextField yField, TextField areaField,
            TextField populationField, TextField metersField,
            ComboBox<Climate> climateBox, ComboBox<Government> governmentBox,
            ComboBox<StandardOfLiving> solBox,
            CheckBox hasGovernor, DatePicker birthdayPicker
    ) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        int row = 0;
        grid.add(new Label(I18n.get("col.name")), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label(I18n.get("col.x")), 0, row);
        grid.add(xField, 1, row++);
        grid.add(new Label(I18n.get("col.y")), 0, row);
        grid.add(yField, 1, row++);
        grid.add(new Label(I18n.get("col.area")), 0, row);
        grid.add(areaField, 1, row++);
        grid.add(new Label(I18n.get("col.population")), 0, row);
        grid.add(populationField, 1, row++);
        grid.add(new Label(I18n.get("col.metersAboveSeaLevel")), 0, row);
        grid.add(metersField, 1, row++);
        grid.add(new Label(I18n.get("col.climate")), 0, row);
        grid.add(climateBox, 1, row++);
        grid.add(new Label(I18n.get("col.government")), 0, row);
        grid.add(governmentBox, 1, row++);
        grid.add(new Label(I18n.get("col.standardOfLiving")), 0, row);
        grid.add(solBox, 1, row++);
        grid.add(hasGovernor, 0, row, 2, 1);
        row++;
        grid.add(new Label(I18n.get("field.governorBirthday")), 0, row);
        grid.add(birthdayPicker, 1, row);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        return grid;
    }

    private static <E extends Enum<E>> ComboBox<E> enumCombo(E[] values, String keyPrefix) {
        ComboBox<E> box = new ComboBox<>();
        box.getItems().addAll(values);
        box.setCellFactory(listView -> new EnumListCell<>(keyPrefix));
        box.setButtonCell(new EnumListCell<>(keyPrefix));
        return box;
    }

    private static class EnumListCell<E extends Enum<E>> extends ListCell<E> {
        private final String keyPrefix;

        EnumListCell(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        @Override
        protected void updateItem(E item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(I18n.get(keyPrefix + item.name()));
            }
        }
    }

    private static City buildCity(
            TextField nameField, TextField xField, TextField yField, TextField areaField,
            TextField populationField, TextField metersField,
            ComboBox<Climate> climateBox, ComboBox<Government> governmentBox,
            ComboBox<StandardOfLiving> solBox,
            CheckBox hasGovernor, DatePicker birthdayPicker,
            boolean edit
    ) {
        requireNonEmpty(xField, I18n.get("col.x"));
        requireNonEmpty(yField, I18n.get("col.y"));
        requireNonEmpty(areaField, I18n.get("col.area"));
        requireNonEmpty(populationField, I18n.get("col.population"));
        requireNonEmpty(metersField, I18n.get("col.metersAboveSeaLevel"));

        City city = new City();
        city.setName(InputValidator.validateName(nameField.getText().trim()));

        float x = InputValidator.validateX(parseFloat(xField.getText().trim()));
        double y = InputValidator.validateY(parseDouble(yField.getText().trim()));
        Coordinates coords = new Coordinates(x, y);
        CoordinatesValidator.validateCoordinates(coords);
        city.setCoordinates(coords);

        double area = parseDouble(areaField.getText().trim());
        InputValidator.validateArea(area);
        city.setArea(area);

        int pop = parseInt(populationField.getText().trim());
        InputValidator.validatePopulation(pop);
        city.setPopulation(pop);

        city.setMetersAboveSeaLevel(parseInt(metersField.getText().trim()));
        city.setClimate(climateBox.getValue());
        Government gov = governmentBox.getValue();
        if (gov == null) {
            throw new IllegalArgumentException(I18n.get("error.governmentRequired"));
        }
        city.setGovernment(gov);
        if (solBox.getValue() == null) {
            throw new IllegalArgumentException(I18n.get("error.standardOfLivingRequired"));
        }
        city.setStandardOfLiving(solBox.getValue());

        if (hasGovernor.isSelected()) {
            LocalDate ld = birthdayPicker.getValue();
            if (ld == null) {
                throw new IllegalArgumentException(I18n.get("error.birthdayRequired"));
            }
            city.setGovernor(new Human(InputValidator.validateBirthday(ld.toString())));
        } else {
            city.setGovernor(null);
        }

        if (!edit) {
            city.setCreationDate(new Date());
            city.setId(null);
        }
        return city;
    }

    private static float parseFloat(String text) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(I18n.get("error.invalidNumber"));
        }
    }

    private static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(I18n.get("error.invalidNumber"));
        }
    }

    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(I18n.get("error.invalidNumber"));
        }
    }

    private static void requireNonEmpty(TextField field, String fieldLabel) {
        if (field.getText() == null || field.getText().isBlank()) {
            throw new IllegalArgumentException(I18n.format("error.fieldEmpty", fieldLabel));
        }
    }
}
