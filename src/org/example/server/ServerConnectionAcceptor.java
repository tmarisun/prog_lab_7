package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnectionAcceptor {

    private static final Logger log = LogManager.getLogger(ServerConnectionAcceptor.class);

    private final int port;
    private final ServerRequestReader requestReader = new ServerRequestReader();
    private final ServerResponseSender responseSender = new ServerResponseSender();
    private final ServerCommandProcessor processor;
    private final int poolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
    private final ExecutorService workerPool = Executors.newFixedThreadPool(poolSize);

    public ServerConnectionAcceptor(int port, ServerCommandProcessor processor) {
        this.port = port;
        this.processor = processor;
    }

    public void start() throws Exception {
        log.info("Инициализация пула обработчиков: размер = {}", poolSize);
        try (ServerSocket serverSocket = new ServerSocket(port, 512)) {
            log.info("Серверный сокет открыт, ожидание подключений на порту {} (backlog 512)", port);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Сервер слушает порт : " + port);
            System.out.println("Для помощи введите : help");

            while (true) {
                processServerConsoleInput(consoleReader);
                Socket socket = serverSocket.accept();
                log.info("Получено новое TCP-подключение: remote={} local={}",
                        socket.getRemoteSocketAddress(), socket.getLocalSocketAddress());
                workerPool.submit(() -> handleClient(socket));
            }
        }
    }

    private void handleClient(Socket socket) {
        String remote = String.valueOf(socket.getRemoteSocketAddress());
        try {
            ParsedRequest parsed = requestReader.read(socket.getInputStream());
            CommandRequest request = parsed.request();
            log.info("Запрос получен от {}: тип команды={} логин={}",
                    remote,
                    request != null && request.getType() != null ? request.getType() : "null",
                    request != null && request.getLogin() != null ? request.getLogin() : "-");

            CommandResponse response = processor.process(request);

            log.info("Ответ сформирован для {}: success={}",
                    remote, response != null && response.isSuccess());
            if (sendAndClose(socket, response, parsed.jsonWire())) {
                log.info("Ответ отправлён по сети, сокет {} закрыт", remote);
            } else {
                log.warn("Ответ не был отправлён (ошибка записи), сокет {} закрыт", remote);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки клиента {}: {}", remote, e.getMessage(), e);
            closeQuietly(socket);
        }
    }

    private boolean sendAndClose(Socket socket, CommandResponse response, boolean jsonWire) {
        try {
            responseSender.send(socket.getOutputStream(), response, jsonWire);
            return true;
        } catch (Exception e) {
            log.error("Ошибка отправки ответа клиенту {}: {}", socket.getRemoteSocketAddress(), e.getMessage(), e);
            return false;
        } finally {
            closeQuietly(socket);
        }
    }

    private static void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void processServerConsoleInput(BufferedReader consoleReader) {
        try {
            while (consoleReader.ready()) {
                String line = consoleReader.readLine();
                log.info("Ввод с консоли сервера: {}", line);
                CommandResponse response = processor.processServerConsoleCommand(line);
                if (response != null) {
                    log.info("[консоль сервера] {}", response.getMessage());
                    System.out.println("[SERVER] " + response.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Ошибка чтения консоли сервера: {}", e.getMessage());
        }
    }
}
