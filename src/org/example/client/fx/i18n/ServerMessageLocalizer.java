package org.example.client.fx.i18n;

import org.example.net.protocol.MessageKeys;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Перевод сообщений сервера и сети (ключи {@code response.*} или устаревший англ./рус. текст).
 */
public final class ServerMessageLocalizer {

    private static final Map<String, String> LEGACY_EXACT = Map.ofEntries(
            Map.entry("Server unavailable", MessageKeys.SERVER_UNAVAILABLE),
            Map.entry("empty credentials", MessageKeys.EMPTY_CREDENTIALS),
            Map.entry("Updated", MessageKeys.UPDATED),
            Map.entry("Insertion failed", MessageKeys.INSERT_FAIL),
            Map.entry("Added (value is max)", MessageKeys.ADD_IF_MAX_OK),
            Map.entry("Not added: value is not greater than max", MessageKeys.ADD_IF_MAX_FAIL),
            Map.entry("Inserted (append mode: index ignored)", MessageKeys.INSERT_OK),
            Map.entry("Требуются логин и пароль в каждом запросе", MessageKeys.AUTH_REQUIRED),
            Map.entry("Неверный логин или пароль", MessageKeys.AUTH_FAILED),
            Map.entry("Удалены все ваши объекты из коллекции", MessageKeys.CLEARED),
            Map.entry("Регистрация успешна. Выполните login на клиенте для дальнейшей работы.", MessageKeys.REGISTER_OK),
            Map.entry("Пользователь с таким логином уже существует", MessageKeys.REGISTER_EXISTS)
    );

    private ServerMessageLocalizer() {}

    public static String localize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String trimmed = raw.trim();

        String legacyKey = LEGACY_EXACT.get(trimmed);
        if (legacyKey != null) {
            return I18n.get(legacyKey);
        }

        if (trimmed.startsWith("response.")) {
            return decodeKeyMessage(trimmed);
        }

        String fromPattern = matchLegacyPatterns(trimmed);
        if (fromPattern != null) {
            return fromPattern;
        }

        return trimmed;
    }

    private static String decodeKeyMessage(String encoded) {
        int pipe = encoded.indexOf('|');
        if (pipe < 0) {
            return translateKey(encoded);
        }
        String key = encoded.substring(0, pipe);
        String[] rest = encoded.substring(pipe + 1).split("\\|", -1);
        return I18n.format(key, (Object[]) rest);
    }

    private static String translateKey(String key) {
        try {
            return I18n.get(key);
        } catch (Exception e) {
            return key;
        }
    }

    private static String matchLegacyPatterns(String msg) {
        if (msg.startsWith("Server unavailable:")) {
            return I18n.format("response.server.unavailableDetail",
                    msg.substring("Server unavailable:".length()).trim());
        }
        if (msg.equals("Server closed connection")) {
            return I18n.get(MessageKeys.SERVER_CONNECTION_CLOSED);
        }
        if (msg.startsWith("connect timeout") || msg.contains("connect timeout")) {
            return I18n.get("response.network.connectTimeout");
        }
        if (msg.startsWith("read timeout") || msg.contains("read timeout")) {
            return I18n.get("response.network.readTimeout");
        }
        if (msg.startsWith("write timeout") || msg.contains("write timeout")) {
            return I18n.get("response.network.writeTimeout");
        }
        Matcher m;

        m = Pattern.compile("^Added city with id (\\d+)$").matcher(msg);
        if (m.matches()) {
            return I18n.format(MessageKeys.ADDED, m.group(1));
        }
        m = Pattern.compile("^Failed to add city: (.+)$").matcher(msg);
        if (m.matches()) {
            return I18n.format(MessageKeys.ADD_FAILED, m.group(1));
        }
        m = Pattern.compile("^City not found or not owned by you: (\\d+)$").matcher(msg);
        if (m.matches()) {
            return I18n.format(MessageKeys.NOT_OWNED, m.group(1));
        }
        m = Pattern.compile("^Removed city with ID: (\\d+)$").matcher(msg);
        if (m.matches()) {
            return I18n.format(MessageKeys.REMOVED, m.group(1));
        }
        m = Pattern.compile("^Count less than ([A-Z_]+): (\\d+)$").matcher(msg);
        if (m.matches()) {
            return I18n.format(MessageKeys.COUNT_LESS, m.group(1), m.group(2));
        }
        m = Pattern.compile("^Filtered by governor: (.+)$").matcher(msg);
        if (m.matches()) {
            return I18n.format(MessageKeys.FILTERED_GOVERNOR, m.group(1));
        }
        if (msg.startsWith("City payload is required")) {
            return I18n.get(MessageKeys.CITY_REQUIRED);
        }
        if (msg.startsWith("ID is required")) {
            return I18n.get(MessageKeys.ID_REQUIRED);
        }
        if (msg.startsWith("StandardOfLiving value is required")) {
            return I18n.get(MessageKeys.SOL_REQUIRED);
        }
        if (msg.startsWith("Invalid StandardOfLiving value:")) {
            return I18n.format(MessageKeys.SOL_INVALID, msg.substring(msg.indexOf(':') + 1).trim());
        }
        if (msg.startsWith("Governor name argument is required")) {
            return I18n.get(MessageKeys.GOVERNOR_ARG_REQUIRED);
        }
        if (msg.startsWith("Ошибка очистки:")) {
            return I18n.format(MessageKeys.CLEAR_FAILED, msg.substring("Ошибка очистки:".length()).trim());
        }
        if (msg.startsWith("Ошибка регистрации:")) {
            return I18n.format(MessageKeys.REGISTER_FAILED, msg.substring("Ошибка регистрации:".length()).trim());
        }
        return null;
    }

    public static String localizeResponse(org.example.net.protocol.CommandResponse response) {
        if (response == null) {
            return "";
        }
        return localize(response.getMessage());
    }
}
