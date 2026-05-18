package org.example.client.fx.i18n;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Локализованные строки из {@code i18n/messages_*.properties}.
 * Смена языка без перезапуска — через {@link #setLocale(Locale)} и слушатели.
 */
public final class I18n {

    private static final String BUNDLE_BASE = "i18n.messages";
    private static final List<Consumer<Locale>> LISTENERS = new ArrayList<>();

    private static Locale locale = SupportedLocale.RU.getLocale();
    private static ResourceBundle bundle = loadBundle(locale);

    private I18n() {}

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(Locale newLocale) {
        if (newLocale == null) {
            return;
        }
        locale = newLocale;
        bundle = loadBundle(locale);
        Locale.setDefault(locale);
        for (Consumer<Locale> listener : List.copyOf(LISTENERS)) {
            listener.accept(locale);
        }
    }

    public static void setSupportedLocale(SupportedLocale supported) {
        setLocale(supported.getLocale());
    }

    public static void addLocaleChangeListener(Consumer<Locale> listener) {
        LISTENERS.add(listener);
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }

    /** Локализованный текст ответа сервера или сообщения об ошибке сети. */
    public static String localizeServer(String rawOrEncoded) {
        return ServerMessageLocalizer.localize(rawOrEncoded);
    }

    public static String formatNumber(double value) {
        return NumberFormat.getNumberInstance(locale).format(value);
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
        return df.format(date);
    }

    public static String formatNow() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale)
                .format(Date.from(now.toInstant()));
    }

    private static ResourceBundle loadBundle(Locale loc) {
        return ResourceBundle.getBundle(BUNDLE_BASE, loc);
    }
}
