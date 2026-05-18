package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.WireCodec;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class ServerResponseSender {

    private static final Logger log = LogManager.getLogger(ServerResponseSender.class);

    public void send(OutputStream output, CommandResponse response, boolean jsonWire) throws Exception {
        byte[] bytes = jsonWire ? WireCodec.encodeJson(response) : WireCodec.encodeJava(response);
        log.info("Сериализация ответа: формат={}, размер тела = {} байт",
                jsonWire ? "JSON" : "Java", bytes.length);
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeInt(bytes.length);
        dos.write(bytes);
        dos.flush();
    }
}
