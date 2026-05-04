package org.example.server;

import org.example.net.protocol.CommandRequest;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class ServerRequestReader {
    public CommandRequest read(InputStream input) throws Exception {
        DataInputStream dis = new DataInputStream(input);
        int len = dis.readInt();
        byte[] body = new byte[len];
        dis.readFully(body);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(body))) {
            return (CommandRequest) ois.readObject();
        }
    }
}

