package org.example.client.fx.util;

import org.example.client.fx.i18n.I18n;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;

/**
 * Локализованные статусы для JavaFX.
 */
public final class FxMessages {

    private FxMessages() {}

    public static String fromResponse(CommandResponse response) {
        if (response == null) {
            return "";
        }
        return I18n.localizeServer(response.getMessage());
    }

    public static String fromError(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return I18n.get(MessageKeys.SERVER_UNAVAILABLE);
        }
        return I18n.localizeServer(error.getMessage());
    }
}
