package org.example.net.protocol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.data.City;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CommandResponse implements Serializable {

    /** UID развёрнутого на Helios JAR (Java-сериализация, только чтение). */
    private static final long serialVersionUID = -3564839609325213382L;

    private boolean success;
    private String message;
    private List<City> cities = new ArrayList<>();

    public static CommandResponse ok(String messageKey, Object... args) {
        CommandResponse r = new CommandResponse();
        r.success = true;
        r.message = encode(messageKey, args);
        return r;
    }

    public static CommandResponse fail(String messageKey, Object... args) {
        CommandResponse r = new CommandResponse();
        r.success = false;
        r.message = encode(messageKey, args);
        return r;
    }

    public static CommandResponse okRaw(String text) {
        CommandResponse r = new CommandResponse();
        r.success = true;
        r.message = text;
        return r;
    }

    public static CommandResponse failRaw(String text) {
        CommandResponse r = new CommandResponse();
        r.success = false;
        r.message = text;
        return r;
    }

    static String encode(String messageKey, Object... args) {
        if (messageKey == null) {
            return "";
        }
        if (args == null || args.length == 0) {
            return messageKey;
        }
        StringBuilder sb = new StringBuilder(messageKey);
        for (Object arg : args) {
            sb.append('|').append(arg == null ? "" : arg);
        }
        return sb.toString();
    }
}
