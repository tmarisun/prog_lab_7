package org.example.client.fx.i18n;

import java.util.Locale;

/**
 * Локали задания: ru, sk, hu, en-IN.
 */
public enum SupportedLocale {
    RU(new Locale("ru"), "Русский"),
    SK(new Locale("sk"), "Slovenčina"),
    HU(new Locale("hu"), "Magyar"),
    EN_IN(Locale.forLanguageTag("en-IN"), "English (India)");

    private final Locale locale;
    /** Название на самом языке — не зависит от текущей локали интерфейса. */
    private final String nativeDisplayName;

    SupportedLocale(Locale locale, String nativeDisplayName) {
        this.locale = locale;
        this.nativeDisplayName = nativeDisplayName;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getNativeDisplayName() {
        return nativeDisplayName;
    }
}
