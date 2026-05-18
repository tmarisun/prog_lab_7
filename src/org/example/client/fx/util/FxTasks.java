package org.example.client.fx.util;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class FxTasks {

    private FxTasks() {}

    public static <T> void runAsync(Callable<T> background, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return background.call();
            }
        };
        task.setOnSucceeded(e -> {
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });
        task.setOnFailed(e -> {
            if (onError != null) {
                onError.accept(task.getException());
            }
        });
        new Thread(task, "fx-background").start();
    }

    public static void runOnFx(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
