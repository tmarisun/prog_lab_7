package org.example.client;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.CommandType;
import org.example.service.CityReader;

import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        CityReader.setScanner(scanner);

        ClientNetworkChannel network = new ClientNetworkChannel();
        ClientCommandParser parser = new ClientCommandParser();
        ClientSession session = new ClientSession();

        System.out.println("Клиент запущен. Сначала: register <логин> <пароль> или login <логин> <пароль>");
        System.out.println("Команды help — справка.");
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line.length() == 0) {
                continue;
            }

            String trimmed = line;
            if (trimmed.toLowerCase().startsWith("login ")) {
                String rest = trimmed.substring(6);
                int sp = rest.indexOf(' ');
                if (sp < 0) {
                    System.out.println("Формат: login <логин> <пароль>");
                    continue;
                }
                String user = rest.substring(0, sp);
                String pass = rest.substring(sp + 1);
                if (user.isEmpty() || pass.isEmpty()) {
                    System.out.println("Логин и пароль не должны быть пустыми");
                    continue;
                }
                session.set(user, pass);
                System.out.println("Учётные данные сохранены для этой сессии.");
                continue;
            }

            CommandRequest req = parser.parse(line);
            if (req == null) {
                String parseError = parser.getLastError();
                System.out.println("Ошибка ввода: " + (parseError != null ? parseError : "неизвестная"));
                continue;
            }

            if (req.getType() == CommandType.EXIT) {
                System.out.println("Клиент завершён.");
                break;
            }

            if (req.getType() != CommandType.REGISTER && !session.isPresent()) {
                System.out.println("Нужна авторизация: login <логин> <пароль> или register ...");
                continue;
            }

            if (req.getType() != CommandType.REGISTER) {
                session.applyTo(req);
            }

            CommandResponse resp = network.send(req);
            printResponse(resp);

            if (resp.isSuccess() && req.getType() == CommandType.REGISTER) {
                session.set(req.getLogin(), req.getPassword());
            }
        }
    }

    private static void printResponse(CommandResponse resp) {
        System.out.println();
        System.out.println(resp.getMessage());
        for (City city : resp.getCities()) {
            System.out.println(city);
        }
        System.out.println();
    }
}
