package org.example.client.fx.canvas;

import javafx.scene.paint.Color;

/**
 * Стабильный цвет по логину владельца (разные пользователи — разные цвета).
 */
public final class OwnerColors {

    private OwnerColors() {}

    public static Color forOwner(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.isBlank()) {
            return Color.GRAY;
        }
        int hash = Math.abs(ownerLogin.hashCode());
        double hue = (hash % 360);
        return Color.hsb(hue, 0.65, 0.85, 1.0);
    }
}
