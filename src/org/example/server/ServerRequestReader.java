package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.WireCodec;

import java.io.DataInputStream;
import java.io.InputStream;

/**
 * Читает запрос: длина (int) + тело (Java-сериализация).
 */
public class ServerRequestReader {

    private static final Logger log = LogManager.getLogger(ServerRequestReader.class);

    public CommandRequest read(InputStream input) throws Exception {
        DataInputStream dis = new DataInputStream(input);
        int len = dis.readInt();
        log.info("Десериализация запроса: размер тела = {} байт", len);
        byte[] body = new byte[len];
        dis.readFully(body);
        return WireCodec.decodeRequest(body);
    }
}
