package org.example.client.fx.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.client.fx.i18n.I18n;
import org.example.client.fx.table.CityTableColumn;
import org.example.client.fx.table.CityTablePipeline;
import org.example.client.fx.table.CityTableViewBuilder;
import org.example.client.fx.util.FxMessages;
import org.example.client.fx.util.FxTasks;
import org.example.client.service.CommandService;
import org.example.data.City;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Таблица городов (SHOW или результат filter_by_governor).
 */
public final class CitiesTableDialog {

    private CitiesTableDialog() {}

    public static void show(List<City> masterList, CommandService commands, String userLogin,
                            Consumer<String> status, Runnable onDataChanged) {
        open(masterList, commands, userLogin, status, onDataChanged, true, I18n.get("table.title"));
    }

    public static void showReadOnly(List<City> cities, String title) {
        open(new ArrayList<>(cities), null, null, msg -> {}, () -> {}, false, title);
    }

    private static void open(
            List<City> masterList,
            CommandService commands,
            String userLogin,
            Consumer<String> status,
            Runnable onDataChanged,
            boolean allowEdit,
            String title
    ) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ObservableList<City> tableItems = FXCollections.observableArrayList();
        TableView<City> table = CityTableViewBuilder.createTable();
        table.setItems(tableItems);

        ComboBox<CityTableColumn> filterColumnBox = new ComboBox<>();
        filterColumnBox.getItems().addAll(CityTableColumn.values());
        filterColumnBox.getSelectionModel().select(CityTableColumn.NAME);

        TextField filterValueField = new TextField();
        filterValueField.setPromptText("…");

        ComboBox<CityTableColumn> sortColumnBox = new ComboBox<>();
        sortColumnBox.getItems().addAll(CityTableColumn.values());
        sortColumnBox.getSelectionModel().select(CityTableColumn.ID);

        CheckBox sortAscendingBox = new CheckBox();
        sortAscendingBox.setSelected(true);

        Label filterColumnLabel = new Label(I18n.get("filter.column"));
        Label sortColumnLabel = new Label(I18n.get("sort.column"));
        sortAscendingBox.setText(I18n.get("sort.ascending"));

        Runnable refreshTable = () -> {
            CityTableColumn filterCol = filterColumnBox.getSelectionModel().getSelectedItem();
            CityTableColumn sortCol = sortColumnBox.getSelectionModel().getSelectedItem();
            boolean asc = sortAscendingBox.isSelected();
            List<City> view = CityTablePipeline.apply(
                    masterList,
                    filterCol,
                    filterValueField.getText(),
                    sortCol,
                    asc
            );
            tableItems.setAll(view);
            table.refresh();
        };

        filterColumnBox.setOnAction(e -> refreshTable.run());
        sortColumnBox.setOnAction(e -> refreshTable.run());
        sortAscendingBox.selectedProperty().addListener((o, a, b) -> refreshTable.run());
        filterValueField.textProperty().addListener((o, a, b) -> refreshTable.run());

        refreshTable.run();

        HBox filterRow = new HBox(8,
                filterColumnLabel, filterColumnBox, filterValueField,
                sortColumnLabel, sortColumnBox, sortAscendingBox);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_LEFT);

        if (allowEdit && commands != null) {
            Button editButton = new Button(I18n.get("button.update"));
            editButton.setOnAction(e -> {
                City selected = table.getSelectionModel().getSelectedItem();
                if (selected == null || selected.getId() == null) {
                    status.accept(I18n.get("error.noSelection"));
                    return;
                }
                var updatedOpt = CityFormDialog.showEditDialog(selected, userLogin);
                if (updatedOpt.isEmpty()) {
                    return;
                }
                City updated = updatedOpt.get();
                FxTasks.runAsync(
                        () -> commands.update(selected.getId(), updated),
                        response -> {
                            status.accept(FxMessages.fromResponse(response));
                            if (response.isSuccess()) {
                                replaceInMasterList(masterList, updated);
                                refreshTable.run();
                                onDataChanged.run();
                            }
                        },
                        err -> status.accept(FxMessages.fromError(err))
                );
            });
            buttons.getChildren().add(editButton);
        }

        VBox content = new VBox(10, filterRow, buttons, table);
        content.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPrefHeight(480);
        table.setPrefWidth(920);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(960, 560);
        dialog.showAndWait();
    }

    private static void replaceInMasterList(List<City> masterList, City updated) {
        for (int i = 0; i < masterList.size(); i++) {
            if (updated.getId() != null && updated.getId().equals(masterList.get(i).getId())) {
                masterList.set(i, updated);
                return;
            }
        }
    }
}
