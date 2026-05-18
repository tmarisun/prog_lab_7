package org.example.client.fx.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Ограничение ввода в полях формы города.
 */
public final class FormFieldFilters {

    private FormFieldFilters() {}

    /** Дробное число (координаты, площадь). */
    public static void decimal(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String t = change.getControlNewText();
            if (t.isEmpty() || t.matches("-?\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));
    }

    /** Целое неотрицательное (население). */
    public static void positiveInteger(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String t = change.getControlNewText();
            if (t.isEmpty() || t.matches("\\d*")) {
                return change;
            }
            return null;
        }));
    }

    /** Целое со знаком (высота над уровнем моря). */
    public static void integer(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String t = change.getControlNewText();
            if (t.isEmpty() || t.matches("-?\\d*")) {
                return change;
            }
            return null;
        }));
    }
}
