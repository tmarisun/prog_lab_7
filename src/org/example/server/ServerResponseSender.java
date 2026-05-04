package org.example.server;

import org.example.net.protocol.CommandResponse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ServerResponseSender {
    public void send(OutputStream output, CommandResponse response) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(response);
        }
        byte[] bytes = bos.toByteArray();
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeInt(bytes.length);
        dos.write(bytes);
        dos.flush();
    }
}

