package org.example.client.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.client.fx.i18n.I18n;
import org.example.client.fx.i18n.SupportedLocale;
import org.example.client.service.CommandService;

/**
 * Точка входа JavaFX-клиента. Запуск: {@code ./gradlew runFxClient}
 * <p>
 * Сначала экран входа ({@link LoginView}), затем главное окно ({@link MainView}).
 * С сервером общается только {@link org.example.client.service.CommandService}.
 */
public class ClientFxApp extends Application {

    private CommandService commandService;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        I18n.setSupportedLocale(SupportedLocale.RU);
        this.commandService = new CommandService();

        showLogin();
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.show();
    }

    private void showLogin() {
        LoginView loginView = new LoginView(commandService, this::showMain);
        Scene scene = new Scene(loginView, 480, 340);
        primaryStage.setTitle(I18n.get("login.title"));
        primaryStage.setScene(scene);
    }

    private void showMain() {
        MainView mainView = new MainView(commandService, this::showLogin);
        Scene scene = new Scene(mainView, 1100, 680);
        primaryStage.setTitle(I18n.get("app.title"));
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
