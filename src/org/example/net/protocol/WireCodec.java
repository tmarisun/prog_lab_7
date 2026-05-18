package org.example.net.protocol;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Протокол: {@code int length} + тело. Новый формат — префикс {@code LAB1} + JSON;
 * старый JAR на Helios — чистая Java-сериализация (чтение с клиента).
 */
public final class WireCodec {

    private static final byte[] MAGIC = {'L', 'A', 'B', '1'};

    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private WireCodec() {
    }

    public static boolean isJsonWire(byte[] body) {
        return body.length >= MAGIC.length
                && body[0] == MAGIC[0]
                && body[1] == MAGIC[1]
                && body[2] == MAGIC[2]
                && body[3] == MAGIC[3];
    }

    public static byte[] encodeJson(Object value) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(MAGIC);
        JSON.writeValue(bos, value);
        return bos.toByteArray();
    }

    /** Java ObjectOutputStream (старый JAR на Helios). */
    public static byte[] encodeJava(Serializable value) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(value);
        }
        return bos.toByteArray();
    }

    public static CommandRequest decodeRequest(byte[] body) throws Exception {
        if (isJsonWire(body)) {
            return JSON.readValue(body, 4, body.length - 4, CommandRequest.class);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(body))) {
            return (CommandRequest) ois.readObject();
        }
    }

    public static CommandResponse decodeResponse(byte[] body) throws Exception {
        if (isJsonWire(body)) {
            return JSON.readValue(body, 4, body.length - 4, CommandResponse.class);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(body))) {
            return (CommandResponse) ois.readObject();
        }
    }

}
