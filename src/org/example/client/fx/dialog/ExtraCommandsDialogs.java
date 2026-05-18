package org.example.client.fx.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.client.fx.i18n.I18n;
import org.example.client.fx.util.FxMessages;
import org.example.client.service.CommandService;
import org.example.data.City;
import org.example.data.StandardOfLiving;
import org.example.net.protocol.CommandResponse;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Диалоги для команд info, clear, count_less_than, filter_by_governor, print_field_ascending, add_if_max, insert_at.
 */
public final class ExtraCommandsDialogs {

    private ExtraCommandsDialogs() {}

    public static void runInfo(CommandService commands, Consumer<String> status) {
        runAsyncStatus(commands::info, status);
    }

    public static void runClear(CommandService commands, Consumer<String> status, Runnable onCleared) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm.clear.title"));
        confirm.setHeaderText(null);
        confirm.setContentText(I18n.get("confirm.clear.message"));
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        var answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.YES) {
            return;
        }
        org.example.client.fx.util.FxTasks.runAsync(
                commands::clear,
                response -> {
                    status.accept(FxMessages.fromResponse(response));
                    if (response.isSuccess() && onCleared != null) {
                        onCleared.run();
                    }
                },
                err -> status.accept(FxMessages.fromError(err))
        );
    }

    public static void runCountLessThan(CommandService commands, Consumer<String> status) {
        ComboBox<StandardOfLiving> box = new ComboBox<>();
        box.getItems().addAll(StandardOfLiving.values());
        box.getSelectionModel().selectFirst();
        Dialog<StandardOfLiving> dialog = new Dialog<>();
        dialog.setTitle(I18n.get("dialog.countLess.title"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox boxPane = new VBox(8, new Label(I18n.get("dialog.countLess.prompt")), box);
        boxPane.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(boxPane);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? box.getValue() : null);
        Optional<StandardOfLiving> chosen = dialog.showAndWait().flatMap(Optional::ofNullable);
        if (chosen.isEmpty()) {
            return;
        }
        StandardOfLiving sol = chosen.get();
        runAsyncStatus(() -> commands.countLessThanStandardOfLiving(sol.name()), status);
    }

    public static void runFilterByGovernor(CommandService commands, Consumer<String> status) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(I18n.get("dialog.filterGovernor.title"));
        dialog.setHeaderText(null);
        dialog.setContentText(I18n.get("dialog.filterGovernor.prompt"));
        Optional<String> text = dialog.showAndWait();
        if (text.isEmpty() || text.get().isBlank()) {
            return;
        }
        runAsyncResponse(() -> commands.filterByGovernor(text.get().trim()), response -> {
            status.accept(FxMessages.fromResponse(response));
            if (response.isSuccess() && response.getCities() != null && !response.getCities().isEmpty()) {
                CitiesTableDialog.showReadOnly(response.getCities(), I18n.get("dialog.filterGovernor.resultTitle"));
            }
        });
    }

    public static void runPrintFieldAscending(CommandService commands, Consumer<String> status) {
        runAsyncResponse(commands::printFieldAscendingStandardOfLiving, response -> showResultDialog(response, status));
    }

    public static void runAddIfMax(CommandService commands, Consumer<String> status, Runnable onChanged) {
        var cityOpt = CityFormDialog.showAddDialog();
        if (cityOpt.isEmpty()) {
            return;
        }
        org.example.client.fx.util.FxTasks.runAsync(
                () -> commands.addIfMax(cityOpt.get()),
                response -> {
                    status.accept(FxMessages.fromResponse(response));
                    if (response.isSuccess() && onChanged != null) {
                        onChanged.run();
                    }
                },
                err -> status.accept(FxMessages.fromError(err))
        );
    }

    public static void runInsertAt(CommandService commands, Consumer<String> status, Runnable onChanged) {
        TextInputDialog indexDialog = new TextInputDialog("0");
        indexDialog.setTitle(I18n.get("dialog.insertAt.title"));
        indexDialog.setContentText(I18n.get("dialog.insertAt.index"));
        Optional<String> indexStr = indexDialog.showAndWait();
        if (indexStr.isEmpty()) {
            return;
        }
        int index;
        try {
            index = Integer.parseInt(indexStr.get().trim());
        } catch (NumberFormatException e) {
            status.accept(I18n.get("error.invalidIndex"));
            return;
        }
        var cityOpt = CityFormDialog.showAddDialog();
        if (cityOpt.isEmpty()) {
            return;
        }
        org.example.client.fx.util.FxTasks.runAsync(
                () -> commands.insertAt(cityOpt.get(), index),
                response -> {
                    status.accept(FxMessages.fromResponse(response));
                    if (response.isSuccess() && onChanged != null) {
                        onChanged.run();
                    }
                },
                err -> status.accept(FxMessages.fromError(err))
        );
    }

    private static void runAsyncStatus(
            java.util.function.Supplier<CommandResponse> call,
            Consumer<String> status
    ) {
        runAsyncResponse(call, response -> status.accept(FxMessages.fromResponse(response)));
    }

    private static void runAsyncResponse(
            java.util.function.Supplier<CommandResponse> call,
            java.util.function.Consumer<CommandResponse> onResponse
    ) {
        org.example.client.fx.util.FxTasks.runAsync(
                call::get,
                onResponse,
                err -> onResponse.accept(CommandResponse.fail(err.getMessage()))
        );
    }

    private static void showResultDialog(CommandResponse response, Consumer<String> status) {
        String text = FxMessages.fromResponse(response);
        status.accept(text);
        if (!response.isSuccess()) {
            return;
        }
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(12);
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(I18n.get("dialog.result.title"));
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
