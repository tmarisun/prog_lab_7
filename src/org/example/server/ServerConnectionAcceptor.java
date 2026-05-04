package org.example.server;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class ServerConnectionAcceptor {
    private final int port;
    private final ServerRequestReader requestReader = new ServerRequestReader();
    private final ServerResponseSender responseSender = new ServerResponseSender();
    private final ServerCommandProcessor processor;
    private final int poolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
    private final ExecutorService readExecutor = Executors.newFixedThreadPool(poolSize);
    private final ExecutorService processExecutor = Executors.newFixedThreadPool(poolSize);
    private final ForkJoinPool responsePool =
            new ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors()));

    public ServerConnectionAcceptor(int port, ServerCommandProcessor processor) {
        this.port = port;
        this.processor = processor;
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port, 512)) {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Server started on port " + port);
            System.out.println("Server console: help (данные в PostgreSQL)");

            while (true) {
                processServerConsoleInput(consoleReader);
                Socket socket = serverSocket.accept();
                readExecutor.submit(() -> readAndDispatch(socket));
            }
        }
    }

    private void readAndDispatch(Socket socket) {
        try {
            CommandRequest request = requestReader.read(socket.getInputStream());
            processExecutor.submit(() -> {
                CommandResponse response;
                try {
                    response = processor.process(request);
                } catch (Throwable t) {
                    response = CommandResponse.fail("Server internal error: " + t.getMessage());
                }
                CommandResponse toSend = response;
                responsePool.execute(() -> sendAndClose(socket, toSend));
            });
        } catch (Exception e) {
            System.out.println("Read error: " + e.getMessage());
            closeQuietly(socket);
        }
    }

    private void sendAndClose(Socket socket, CommandResponse response) {
        try {
            responseSender.send(socket.getOutputStream(), response);
        } catch (Exception e) {
            System.err.println("Send error: " + e.getMessage());
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
                CommandResponse response = processor.processServerConsoleCommand(line);
                if (response != null) {
                    System.out.println("[SERVER] " + response.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Server console error: " + e.getMessage());
        }
    }
}
