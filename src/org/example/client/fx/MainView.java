package org.example.client.fx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.client.fx.canvas.CityCanvasPane;
import org.example.client.fx.dialog.CitiesTableDialog;
import org.example.client.fx.dialog.CityFormDialog;
import org.example.client.fx.dialog.ExtraCommandsDialogs;
import org.example.client.fx.i18n.I18n;
import org.example.client.fx.i18n.SupportedLocale;
import org.example.client.fx.util.CityListFingerprint;
import org.example.client.fx.util.CityOwnership;
import org.example.client.fx.util.FxMessages;
import org.example.client.fx.util.FxTasks;
import org.example.client.service.CommandService;
import org.example.data.City;
import org.example.net.protocol.CommandResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Главное окно: координатная плоскость; таблица — по «Показать»; polling; все команды.
 */
public class MainView extends BorderPane {

    private static final Duration POLL_INTERVAL = Duration.seconds(2.5);

    private final CommandService commandService;
    private final Runnable onLogout;

    private final List<City> masterList = new ArrayList<>();
    private final Map<String, Consumer<String>> boundTexts = new LinkedHashMap<>();

    private Label userLabel;
    private Label statusLabel;
    private Label languageCaption;
    private ComboBox<SupportedLocale> languageBox;
    private Button showButton;
    private Button addButton;
    private Button updateButton;
    private Button removeButton;
    private Button helpButton;
    private MenuButton moreButton;
    private CheckBox autoRefreshBox;
    private Button logoutButton;
    private Label canvasTitleLabel;
    private CityCanvasPane cityCanvas;
    private City selectedOnCanvas;

    private Timeline pollTimeline;
    private String lastFingerprint = "";

    public MainView(CommandService commandService, Runnable onLogout) {
        this.commandService = commandService;
        this.onLogout = onLogout;
        build();
        I18n.addLocaleChangeListener(loc -> {
            applyTexts();
            refreshCanvas(false);
        });
        applyTexts();
        setStatus(I18n.get("status.ready"));
        refreshCanvas(false);
    }

    private void build() {
        setPadding(new Insets(10));

        languageCaption = new Label();
        languageBox = createLanguageBox();
        userLabel = new Label();
        logoutButton = new Button();
        logoutButton.setOnAction(e -> {
            stopPolling();
            commandService.logout();
            onLogout.run();
        });

        HBox top = new HBox(12, languageCaption, languageBox, userLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(userLabel, Priority.ALWAYS);
        userLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox topRight = new HBox(10, logoutButton);
        topRight.setAlignment(Pos.CENTER_RIGHT);
        setTop(new BorderPane(top, null, topRight, null, null));

        showButton = new Button();
        addButton = new Button();
        updateButton = new Button();
        removeButton = new Button();
        helpButton = new Button();
        moreButton = new MenuButton();
        autoRefreshBox = new CheckBox();

        showButton.setOnAction(e -> onShowTable());
        addButton.setOnAction(e -> onAdd());
        updateButton.setOnAction(e -> onUpdate());
        removeButton.setOnAction(e -> onRemove());
        helpButton.setOnAction(e -> onHelp());
        buildMoreMenu();
        autoRefreshBox.selectedProperty().addListener((o, was, on) -> {
            if (on) {
                startPolling();
            } else {
                stopPolling();
            }
        });

        HBox toolbar = new HBox(8, showButton, addButton, updateButton, removeButton, helpButton, moreButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox pollRow = new HBox(8, autoRefreshBox);
        pollRow.setAlignment(Pos.CENTER_LEFT);

        canvasTitleLabel = new Label();
        cityCanvas = new CityCanvasPane();
        cityCanvas.setOnCitySelected(city -> {
            selectedOnCanvas = city;
            cityCanvas.setSelectedCityId(city.getId());
        });

        VBox center = new VBox(8, toolbar, pollRow, canvasTitleLabel, cityCanvas);
        VBox.setVgrow(cityCanvas, Priority.ALWAYS);
        setCenter(center);

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        setBottom(statusLabel);

        bindText("language.label", languageCaption::setText);
        bindText("button.show", showButton::setText);
        bindText("button.add", addButton::setText);
        bindText("button.update", updateButton::setText);
        bindText("button.remove", removeButton::setText);
        bindText("button.help", helpButton::setText);
        bindText("button.more", moreButton::setText);
        bindText("poll.auto", autoRefreshBox::setText);
        bindText("button.logout", logoutButton::setText);
        bindText("canvas.title", canvasTitleLabel::setText);
    }

    private void buildMoreMenu() {
        MenuItem infoItem = new MenuItem();
        MenuItem clearItem = new MenuItem();
        MenuItem countItem = new MenuItem();
        MenuItem filterGovItem = new MenuItem();
        MenuItem printItem = new MenuItem();
        MenuItem addIfMaxItem = new MenuItem();
        MenuItem insertAtItem = new MenuItem();

        bindMenuText("menu.info", infoItem::setText);
        bindMenuText("menu.clear", clearItem::setText);
        bindMenuText("menu.countLess", countItem::setText);
        bindMenuText("menu.filterGovernor", filterGovItem::setText);
        bindMenuText("menu.printField", printItem::setText);
        bindMenuText("menu.addIfMax", addIfMaxItem::setText);
        bindMenuText("menu.insertAt", insertAtItem::setText);

        infoItem.setOnAction(e -> ExtraCommandsDialogs.runInfo(commandService, this::setStatus));
        clearItem.setOnAction(e -> ExtraCommandsDialogs.runClear(commandService, this::setStatus, this::onCollectionCleared));
        countItem.setOnAction(e -> ExtraCommandsDialogs.runCountLessThan(commandService, this::setStatus));
        filterGovItem.setOnAction(e -> ExtraCommandsDialogs.runFilterByGovernor(commandService, this::setStatus));
        printItem.setOnAction(e -> ExtraCommandsDialogs.runPrintFieldAscending(commandService, this::setStatus));
        addIfMaxItem.setOnAction(e -> ExtraCommandsDialogs.runAddIfMax(commandService, this::setStatus, this::reloadFromServerQuiet));
        insertAtItem.setOnAction(e -> ExtraCommandsDialogs.runInsertAt(commandService, this::setStatus, this::reloadFromServerQuiet));

        moreButton.getItems().addAll(infoItem, clearItem, countItem, filterGovItem, printItem, addIfMaxItem, insertAtItem);
    }

    private void bindMenuText(String key, Consumer<String> setter) {
        boundTexts.put(key, setter);
    }

    private ComboBox<SupportedLocale> createLanguageBox() {
        ComboBox<SupportedLocale> box = new ComboBox<>();
        box.getItems().addAll(SupportedLocale.values());
        box.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(SupportedLocale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNativeDisplayName());
            }
        });
        box.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SupportedLocale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNativeDisplayName());
            }
        });
        box.getSelectionModel().select(SupportedLocale.RU);
        box.setOnAction(e -> {
            SupportedLocale sel = box.getSelectionModel().getSelectedItem();
            if (sel != null) {
                I18n.setSupportedLocale(sel);
            }
        });
        return box;
    }

    private void bindText(String key, Consumer<String> setter) {
        boundTexts.put(key, setter);
    }

    private void applyTexts() {
        boundTexts.forEach((k, s) -> s.accept(I18n.get(k)));
        refreshMoreMenuTexts();
        refreshUserLabel();
        languageBox.requestLayout();
    }

    private void refreshMoreMenuTexts() {
        if (moreButton.getItems().size() >= 7) {
            moreButton.getItems().get(0).setText(I18n.get("menu.info"));
            moreButton.getItems().get(1).setText(I18n.get("menu.clear"));
            moreButton.getItems().get(2).setText(I18n.get("menu.countLess"));
            moreButton.getItems().get(3).setText(I18n.get("menu.filterGovernor"));
            moreButton.getItems().get(4).setText(I18n.get("menu.printField"));
            moreButton.getItems().get(5).setText(I18n.get("menu.addIfMax"));
            moreButton.getItems().get(6).setText(I18n.get("menu.insertAt"));
        }
    }

    private void refreshUserLabel() {
        userLabel.setText(I18n.format("user.current", commandService.getLogin()));
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private String formatLoadedStatus(int count) {
        String base = I18n.format("status.loaded", count);
        if (count > 20) {
            return base + " " + I18n.get("status.dbNotEmptyHint");
        }
        return base;
    }

    private void onShowTable() {
        loadCitiesThenShowTable();
    }

    private void loadCitiesThenShowTable() {
        setStatus(I18n.get("status.connecting"));
        showButton.setDisable(true);
        FxTasks.runAsync(
                commandService::show,
                response -> {
                    showButton.setDisable(false);
                    if (response.isSuccess()) {
                        applyServerList(commandService.citiesFrom(response), false);
                        setStatus(formatLoadedStatus(masterList.size()));
                        CitiesTableDialog.show(
                                masterList,
                                commandService,
                                commandService.getLogin(),
                                this::setStatus,
                                () -> refreshCanvas(false)
                        );
                    } else {
                        setStatus(FxMessages.fromResponse(response));
                    }
                },
                err -> {
                    showButton.setDisable(false);
                    setStatus(FxMessages.fromError(err));
                }
        );
    }

    private void reloadFromServerQuiet() {
        FxTasks.runAsync(
                commandService::show,
                response -> {
                    if (response.isSuccess()) {
                        applyServerList(commandService.citiesFrom(response), true);
                        setStatus(I18n.format("status.loaded", masterList.size()));
                    } else {
                        setStatus(FxMessages.fromResponse(response));
                    }
                },
                err -> setStatus(FxMessages.fromError(err))
        );
    }

    private void applyServerList(List<City> fromServer, boolean animate) {
        masterList.clear();
        masterList.addAll(fromServer);
        lastFingerprint = CityListFingerprint.of(masterList);
        refreshCanvas(animate);
    }

    private void onCollectionCleared() {
        String login = commandService.getLogin();
        masterList.removeIf(c -> CityOwnership.isOwnedByUser(c, login));
        lastFingerprint = CityListFingerprint.of(masterList);
        if (selectedOnCanvas != null && CityOwnership.isOwnedByUser(selectedOnCanvas, login)) {
            selectedOnCanvas = null;
        }
        refreshCanvas(false);
    }

    private void refreshCanvas(boolean animate) {
        cityCanvas.setCities(new ArrayList<>(masterList), animate);
        if (selectedOnCanvas != null && selectedOnCanvas.getId() != null) {
            cityCanvas.setSelectedCityId(selectedOnCanvas.getId());
        }
    }

    private void onAdd() {
        var cityOpt = CityFormDialog.showAddDialog();
        if (cityOpt.isEmpty()) {
            return;
        }
        setStatus(I18n.get("status.connecting"));
        City toAdd = cityOpt.get();
        FxTasks.runAsync(
                () -> commandService.add(toAdd),
                response -> handleAddResponse(response, toAdd),
                err -> setStatus(FxMessages.fromError(err))
        );
    }

    private void onUpdate() {
        if (selectedOnCanvas == null || selectedOnCanvas.getId() == null) {
            setStatus(I18n.get("error.noSelection"));
            return;
        }
        var updatedOpt = CityFormDialog.showEditDialog(selectedOnCanvas, commandService.getLogin());
        if (updatedOpt.isEmpty()) {
            return;
        }
        City updated = updatedOpt.get();
        long id = selectedOnCanvas.getId();
        setStatus(I18n.get("status.connecting"));
        FxTasks.runAsync(
                () -> commandService.update(id, updated),
                response -> handleUpdateResponse(response, updated),
                err -> setStatus(FxMessages.fromError(err))
        );
    }

    private void onRemove() {
        if (selectedOnCanvas == null || selectedOnCanvas.getId() == null) {
            setStatus(I18n.get("error.noSelection"));
            return;
        }
        City selected = selectedOnCanvas;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18n.get("confirm.remove.title"));
        confirm.setHeaderText(null);
        confirm.setContentText(I18n.format("confirm.remove.message", selected.getId(), selected.getName()));
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }
        setStatus(I18n.get("status.connecting"));
        FxTasks.runAsync(
                () -> commandService.removeById(selected.getId()),
                response -> handleRemoveResponse(response, selected.getId()),
                err -> setStatus(FxMessages.fromError(err))
        );
    }

    private void onHelp() {
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle(I18n.get("button.help"));
        help.setHeaderText(null);
        help.setContentText(I18n.get("help.commands"));
        help.getButtonTypes().setAll(ButtonType.OK);
        help.showAndWait();
    }

    private void handleAddResponse(CommandResponse response, City submitted) {
        if (!response.isSuccess()) {
            setStatus(FxMessages.fromResponse(response));
            return;
        }
        City added = response.getCities().isEmpty() ? submitted : response.getCities().get(0);
        masterList.add(added);
        lastFingerprint = CityListFingerprint.of(masterList);
        selectedOnCanvas = added;
        refreshCanvas(true);
        setStatus(FxMessages.fromResponse(response));
    }

    private void handleUpdateResponse(CommandResponse response, City updated) {
        if (!response.isSuccess()) {
            setStatus(FxMessages.fromResponse(response));
            return;
        }
        for (int i = 0; i < masterList.size(); i++) {
            if (updated.getId() != null && updated.getId().equals(masterList.get(i).getId())) {
                masterList.set(i, updated);
                break;
            }
        }
        lastFingerprint = CityListFingerprint.of(masterList);
        selectedOnCanvas = updated;
        refreshCanvas(false);
        cityCanvas.setSelectedCityId(updated.getId());
        setStatus(FxMessages.fromResponse(response));
    }

    private void handleRemoveResponse(CommandResponse response, long removedId) {
        if (!response.isSuccess()) {
            setStatus(FxMessages.fromResponse(response));
            return;
        }
        masterList.removeIf(c -> c.getId() != null && c.getId() == removedId);
        lastFingerprint = CityListFingerprint.of(masterList);
        selectedOnCanvas = null;
        refreshCanvas(false);
        setStatus(FxMessages.fromResponse(response));
    }

    private void startPolling() {
        stopPolling();
        pollOnce();
        pollTimeline = new Timeline(new KeyFrame(POLL_INTERVAL, e -> pollOnce()));
        pollTimeline.setCycleCount(Timeline.INDEFINITE);
        pollTimeline.play();
    }

    private void stopPolling() {
        if (pollTimeline != null) {
            pollTimeline.stop();
            pollTimeline = null;
        }
    }

    private void pollOnce() {
        FxTasks.runAsync(
                commandService::show,
                response -> {
                    if (!response.isSuccess()) {
                        return;
                    }
                    List<City> fromServer = commandService.citiesFrom(response);
                    String fp = CityListFingerprint.of(fromServer);
                    if (fp.equals(lastFingerprint)) {
                        return;
                    }
                    boolean animate = !masterList.isEmpty();
                    applyServerList(fromServer, animate);
                    setStatus(I18n.format("status.pollUpdated", masterList.size()));
                },
                err -> { /* тихо при polling */ }
        );
    }
}
