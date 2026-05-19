package org.example.client;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.AppConfig;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.net.protocol.WireCodec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TCP-клиент: одно подключение на одну команду (запрос → ответ → закрытие).
 */
public class ClientNetworkChannel {

    private static final Logger log = LogManager.getLogger(ClientNetworkChannel.class);

    private final String host;
    private final int port;
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int SO_TIMEOUT_MS = 30_000;

    public ClientNetworkChannel() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.host = AppConfig.get(dotenv, "SERVER_HOST", "localhost");
        this.port = Integer.parseInt(AppConfig.get(dotenv, "SERVER_PORT", "5234"));
        log.info("Клиент: сервер {}:{}", host, port);
    }

    public CommandResponse send(CommandRequest request) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
                socket.setSoTimeout(SO_TIMEOUT_MS);
                socket.setTcpNoDelay(true);

                writeRequest(socket, request);
                return readResponse(socket);

            } catch (Exception e) {
                log.warn("Запрос к {}:{} не удался (попытка {}/3): {}", host, port, attempt, e.toString());
                if (attempt == 3) {
                    if (isConnectionRefused(e)) {
                        return CommandResponse.fail(MessageKeys.CONNECTION_REFUSED, host, port);
                    }
                    return CommandResponse.fail(MessageKeys.SERVER_UNAVAILABLE_DETAIL, rootCauseMessage(e));
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return CommandResponse.fail(MessageKeys.SERVER_UNAVAILABLE);
    }

    private void writeRequest(Socket socket, CommandRequest request) throws Exception {
        byte[] payload = WireCodec.encode(request);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    private CommandResponse readResponse(Socket socket) throws Exception {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        int len = in.readInt();
        if (len <= 0 || len > 16 * 1024 * 1024) {
            throw new IllegalStateException("invalid response length: " + len);
        }
        byte[] body = in.readNBytes(len);
        if (body.length != len) {
            throw new IllegalStateException(MessageKeys.SERVER_CONNECTION_CLOSED);
        }
        return WireCodec.decodeResponse(body);
    }

    private static boolean isConnectionRefused(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof java.net.ConnectException) {
                return true;
            }
            String m = t.getMessage();
            if (m != null && m.contains("Connection refused")) {
                return true;
            }
        }
        return false;
    }

    private static String rootCauseMessage(Throwable e) {
        Throwable c = e;
        while (c.getCause() != null) {
            c = c.getCause();
        }
        String msg = c.getMessage();
        if (msg != null && !msg.isBlank()) {
            return msg;
        }
        return c.getClass().getSimpleName();
    }
}
