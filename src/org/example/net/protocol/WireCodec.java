package org.example.net.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * TCP-тело: Java-сериализация ({@link ObjectOutputStream} / {@link ObjectInputStream}).
 * Не путать с колонками {@code coordinates_json} в PostgreSQL — там другой JSON (Jackson в {@link org.example.db.CityRepository}).
 */
public final class WireCodec {

    private WireCodec() {
    }

    public static byte[] encode(Serializable value) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(value);
        }
        return bos.toByteArray();
    }

    public static CommandRequest decodeRequest(byte[] body) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(body))) {
            return (CommandRequest) ois.readObject();
        }
    }

    public static CommandResponse decodeResponse(byte[] body) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(body))) {
            return (CommandResponse) ois.readObject();
        }
    }
}
