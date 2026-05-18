package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.net.protocol.WireCodec;

import java.io.DataInputStream;
import java.io.InputStream;

public class ServerRequestReader {

    private static final Logger log = LogManager.getLogger(ServerRequestReader.class);

    public ParsedRequest read(InputStream input) throws Exception {
        DataInputStream dis = new DataInputStream(input);
        int len = dis.readInt();
        log.info("Десериализация запроса: размер тела по протоколу = {} байт", len);
        byte[] body = new byte[len];
        dis.readFully(body);
        boolean jsonWire = WireCodec.isJsonWire(body);
        return new ParsedRequest(WireCodec.decodeRequest(body), jsonWire);
    }
}
