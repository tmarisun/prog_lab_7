package org.example.client.fx.dialog;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.client.fx.i18n.I18n;
import org.example.client.fx.util.CityOwnership;
import org.example.client.fx.util.FormFieldFilters;
import org.example.data.*;
import org.example.validate.CoordinatesValidator;
import org.example.validate.InputValidator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Диалог ввода / редактирования города.
 */
public final class CityFormDialog {

    private CityFormDialog() {}

    public static Optional<City> showAddDialog() {
        return showDialog(null, null);
    }

    public static Optional<City> showEditDialog(City existing, String currentLogin) {
        if (!CityOwnership.isOwnedByUser(existing, currentLogin)) {
            new Alert(Alert.AlertType.WARNING, I18n.get("error.notOwner"), ButtonType.OK).showAndWait();
            return Optional.empty();
        }
        return showDialog(existing, currentLogin);
    }

    private static Optional<City> showDialog(City existing, String currentLogin) {
        boolean edit = existing != null;
        Dialog<City> dialog = new Dialog<>();
        dialog.setTitle(I18n.get(edit ? "dialog.edit.title" : "dialog.add.title"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText(I18n.get(edit ? "dialog.edit.ok" : "dialog.add.ok"));
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL))
                .setText(I18n.get("dialog.add.cancel"));

        TextField nameField = new TextField();
        TextField xField = new TextField();
        TextField yField = new TextField();
        TextField areaField = new TextField();
        TextField populationField = new TextField();
        TextField metersField = new TextField("0");

        FormFieldFilters.decimal(xField);
        FormFieldFilters.decimal(yField);
        FormFieldFilters.decimal(areaField);
        FormFieldFilters.positiveInteger(populationField);
        FormFieldFilters.integer(metersField);

        ComboBox<Climate> climateBox = enumCombo(Climate.values(), "climate.");
        ComboBox<Government> governmentBox = enumCombo(Government.values(), "government.");
        ComboBox<StandardOfLiving> solBox = enumCombo(StandardOfLiving.values(), "standardOfLiving.");

        CheckBox hasGovernor = new CheckBox(I18n.get("field.hasGovernor"));
        DatePicker birthdayPicker = new DatePicker();
        birthdayPicker.setDisable(true);
        hasGovernor.selectedProperty().addListener((o, old, sel) -> birthdayPicker.setDisable(!sel));

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
        root.setPadding(new Insets(0));
        dialog.getDialogPane().setContent(root);

        final City[] validated = new City[1];

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
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
                String msg = ex.getMessage();
                if (ex instanceof NumberFormatException) {
                    msg = I18n.get("error.invalidNumber");
                }
                showFormError(errorLabel, msg);
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? validated[0] : null);

        return dialog.showAndWait();
    }

    private static void showFormError(Label errorLabel, String message) {
        errorLabel.setText(message != null && !message.isBlank() ? message : I18n.get("error.formInvalid"));
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
        box.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18n.get(keyPrefix + item.name()));
            }
        });
        box.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18n.get(keyPrefix + item.name()));
            }
        });
        return box;
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

        float x = InputValidator.validateX(Float.parseFloat(xField.getText().trim()));
        double y = InputValidator.validateY(Double.parseDouble(yField.getText().trim()));
        Coordinates coords = new Coordinates(x, y);
        CoordinatesValidator.validateCoordinates(coords);
        city.setCoordinates(coords);

        double area = Double.parseDouble(areaField.getText().trim());
        InputValidator.validateArea(area);
        city.setArea(area);

        int pop = Integer.parseInt(populationField.getText().trim());
        InputValidator.validatePopulation(pop);
        city.setPopulation(pop);

        city.setMetersAboveSeaLevel(Integer.parseInt(metersField.getText().trim()));
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

    private static void requireNonEmpty(TextField field, String fieldLabel) {
        if (field.getText() == null || field.getText().isBlank()) {
            throw new IllegalArgumentException(I18n.format("error.fieldEmpty", fieldLabel));
        }
    }
}
