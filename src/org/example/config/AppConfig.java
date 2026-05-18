package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Читает настройки из .env и из переменных окружения процесса.
 */
public final class AppConfig {

    private AppConfig() {}

    public static String get(Dotenv dotenv, String key, String defaultValue) {
        if (dotenv != null) {
            String v = dotenv.get(key);
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        return defaultValue;
    }

    public static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
