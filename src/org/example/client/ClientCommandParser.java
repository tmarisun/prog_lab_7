package org.example.client;

import lombok.Getter;
import org.example.client.cmd.*;
import org.example.data.City;
import org.example.data.StandardOfLiving;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;
import org.example.service.CityReader;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ClientCommandParser {

    private String lastError;
    private final Map<String, ClientCommand> commands = new HashMap<>();

    public ClientCommandParser() {
        registerCommands();
    }

    private void registerCommands() {
        commands.put("register", (arg, req) -> {
            if (arg == null || arg.isBlank()) {
                throw new IllegalArgumentException("Формат: register <логин> <пароль>");
            }
            String trimmed = arg.trim();
            int sp = trimmed.indexOf(' ');
            if (sp < 0) {
                throw new IllegalArgumentException("Формат: register <логин> <пароль>");
            }
            String login = trimmed.substring(0, sp).trim();
            String password = trimmed.substring(sp + 1).trim();
            if (login.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Логин и пароль не должны быть пустыми");
            }
            req.setType(CommandType.REGISTER);
            req.setLogin(login);
            req.setPassword(password);
        });

        commands.put("help", new SimpleCommand(CommandType.HELP));
        commands.put("info", new SimpleCommand(CommandType.INFO));
        commands.put("show", new SimpleCommand(CommandType.SHOW));
        commands.put("clear", new SimpleCommand(CommandType.CLEAR));
        commands.put("exit", new SimpleCommand(CommandType.EXIT));
        commands.put("print_field_ascending_standard_of_living",
                new SimpleCommand(CommandType.PRINT_FIELD_ASCENDING_STANDARD_OF_LIVING));

        commands.put("remove_by_id", new RemoveByIdCommand());
        commands.put("count_less_than_standard_of_living", new CountLessThanCommand());
        commands.put("filter_by_governor", new FilterByGovernorCommand());
        commands.put("add", new AddCommand());
        commands.put("add_if_max", new AddIfMaxCommand());
        commands.put("insert_at", new InsertAtCommand());
        commands.put("update", new UpdateCommand());
    }

    public CommandRequest parse(String line) {
        lastError = null;

        if (line == null || line.trim().isEmpty()) {
            lastError = "Пустая строка";
            return null;
        }

        String[] parts = line.trim().split("\\s+", 2);
        String cmdName = parts[0].toLowerCase();
        String arg;
        if (parts.length > 1) {
            arg = parts[1];
        } else {
            arg = null;
        }

        ClientCommand command = commands.get(cmdName);

        if (command == null) {
            lastError = "Неизвестная команда: " + cmdName + " (введите help)";
            return null;
        }

        CommandRequest req = new CommandRequest();
        try {
            command.execute(arg, req);
            return req;
        } catch (Exception e) {
            lastError = e.getMessage();
            return null;
        }
    }
}
