package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.WireCodec;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Отправляет ответ: длина (int) + тело (Java-сериализация).
 */
public class ServerResponseSender {

    private static final Logger log = LogManager.getLogger(ServerResponseSender.class);

    public void send(OutputStream output, CommandResponse response) throws Exception {
        byte[] bytes = WireCodec.encode(response);
        log.info("Ответ: размер тела = {} байт", bytes.length);
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeInt(bytes.length);
        dos.write(bytes);
        dos.flush();
    }
}
