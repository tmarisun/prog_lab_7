package org.example.client.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.example.client.fx.i18n.I18n;
import org.example.client.fx.i18n.SupportedLocale;
import org.example.client.fx.util.FxMessages;
import org.example.client.fx.util.FxTasks;
import org.example.client.service.CommandService;
import org.example.net.protocol.CommandResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Экран входа и регистрации.
 */
public class LoginView extends BorderPane {

    private final CommandService commandService;
    private final Runnable onLoginSuccess;

    private final Map<String, Consumer<String>> boundTexts = new LinkedHashMap<>();
    private Label titleLabel;
    private Label usernameCaption;
    private Label passwordCaption;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;
    private Label statusLabel;
    private Label languageCaption;
    private ComboBox<SupportedLocale> languageBox;

    public LoginView(CommandService commandService, Runnable onLoginSuccess) {
        this.commandService = commandService;
        this.onLoginSuccess = onLoginSuccess;
        build();
        I18n.addLocaleChangeListener(loc -> applyTexts());
        applyTexts();
    }

    private void build() {
        setPadding(new Insets(16));

        languageCaption = new Label();
        languageBox = createLanguageBox();
        HBox top = new HBox(10, languageCaption, languageBox);
        top.setAlignment(Pos.CENTER_RIGHT);
        setTop(top);

        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        usernameCaption = new Label();
        passwordCaption = new Label();
        usernameField = new TextField();
        passwordField = new PasswordField();

        loginButton = new Button();
        registerButton = new Button();
        loginButton.setOnAction(e -> doLogin());
        registerButton.setOnAction(e -> doRegister());

        HBox buttons = new HBox(10, loginButton, registerButton);
        buttons.setAlignment(Pos.CENTER);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);
        form.add(usernameCaption, 0, 0);
        form.add(usernameField, 1, 0);
        form.add(passwordCaption, 0, 1);
        form.add(passwordField, 1, 1);
        form.add(buttons, 0, 2, 2, 1);

        statusLabel = new Label();
        statusLabel.setWrapText(true);

        javafx.scene.layout.VBox center = new javafx.scene.layout.VBox(16, titleLabel, form, statusLabel);
        center.setAlignment(Pos.CENTER);
        setCenter(center);

        bindText("login.title", titleLabel::setText);
        bindText("login.username", usernameCaption::setText);
        bindText("login.password", passwordCaption::setText);
        bindText("button.login", loginButton::setText);
        bindText("button.register", registerButton::setText);
        bindText("language.label", languageCaption::setText);
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
        languageBox.requestLayout();
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private void doLogin() {
        String login = usernameField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isEmpty()) {
            setStatus(I18n.get("error.emptyLogin"));
            return;
        }
        setStatus(I18n.get("status.connecting"));
        loginButton.setDisable(true);
        FxTasks.runAsync(
                () -> commandService.login(login, password),
                response -> {
                    loginButton.setDisable(false);
                    handleAuthResponse(response, true);
                },
                err -> {
                    loginButton.setDisable(false);
                    setStatus(FxMessages.fromError(err));
                }
        );
    }

    private void doRegister() {
        String login = usernameField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isEmpty()) {
            setStatus(I18n.get("error.emptyLogin"));
            return;
        }
        setStatus(I18n.get("status.connecting"));
        registerButton.setDisable(true);
        FxTasks.runAsync(
                () -> commandService.register(login, password),
                response -> {
                    registerButton.setDisable(false);
                    if (response.isSuccess()) {
                        setStatus(FxMessages.fromResponse(response));
                    } else {
                        setStatus(FxMessages.fromResponse(response));
                    }
                },
                err -> {
                    registerButton.setDisable(false);
                    setStatus(FxMessages.fromError(err));
                }
        );
    }

    private void handleAuthResponse(CommandResponse response, boolean fromLogin) {
        if (response.isSuccess()) {
            setStatus(I18n.get(fromLogin ? "msg.login.ok" : "msg.register.ok"));
            onLoginSuccess.run();
        } else {
            setStatus(FxMessages.fromResponse(response));
        }
    }
}
