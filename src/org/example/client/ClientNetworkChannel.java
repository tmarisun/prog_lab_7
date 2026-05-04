package org.example.client;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.config.AppConfig;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeoutException;

public class ClientNetworkChannel {
    private final String host;
    private final int port;
    private static final long CONNECT_TIMEOUT_MS = 0;
    private static final long IO_TIMEOUT_MS = 0;

    public ClientNetworkChannel() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.host = AppConfig.get(dotenv, "SERVER_HOST", "localhost");
        this.port = Integer.parseInt(AppConfig.get(dotenv, "SERVER_PORT", "5555"));
    }

    public CommandResponse send(CommandRequest request) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try (SocketChannel channel = SocketChannel.open()) {
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(host, port));
                long connectStart = System.currentTimeMillis();
                while (!channel.finishConnect()) {
                    if (System.currentTimeMillis() - connectStart > CONNECT_TIMEOUT_MS) {
                        throw new TimeoutException("connect timeout");
                    }
                    Thread.sleep(50);
                }

                byte[] payload = serialize(request);
                ByteBuffer out = ByteBuffer.allocate(4 + payload.length);
                out.putInt(payload.length);
                out.put(payload);
                out.flip();
                writeFully(channel, out, IO_TIMEOUT_MS);

                ByteBuffer lenBuf = ByteBuffer.allocate(4);
                readFully(channel, lenBuf, IO_TIMEOUT_MS);
                lenBuf.flip();
                int len = lenBuf.getInt();
                ByteBuffer body = ByteBuffer.allocate(len);
                readFully(channel, body, IO_TIMEOUT_MS);
                return deserialize(body.array());
            } catch (Exception e) {
                if (attempt == 3) {
                    return CommandResponse.fail("Server unavailable: " + e.getMessage());
                }
                try { Thread.sleep(300); } catch (InterruptedException ignored) { }
            }
        }
        return CommandResponse.fail("Server unavailable");
    }

    private byte[] serialize(CommandRequest request) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(request);
        }
        return bos.toByteArray();
    }

    private CommandResponse deserialize(byte[] bytes) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (CommandResponse) ois.readObject();
        }
    }

    private static void readFully(SocketChannel channel, ByteBuffer buffer, long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read > 0) {
                continue;
            }
            if (read < 0) {
                throw new IllegalStateException("Server closed connection");
            }
            if (System.currentTimeMillis() - start > timeoutMs) {
                throw new TimeoutException("read timeout");
            }
            Thread.sleep(20);
        }
    }

    private static void writeFully(SocketChannel channel, ByteBuffer buffer, long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        while (buffer.hasRemaining()) {
            int written = channel.write(buffer);
            if (written > 0) {
                continue;
            }
            if (System.currentTimeMillis() - start > timeoutMs) {
                throw new TimeoutException("write timeout");
            }
            Thread.sleep(20);
        }
    }
}

