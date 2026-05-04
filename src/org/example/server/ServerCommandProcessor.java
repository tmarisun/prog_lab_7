package org.example.server;

import org.example.HelpFormatter;
import org.example.db.UserRepository;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.CommandType;
import org.example.server.cmdd.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServerCommandProcessor {
    private final ServerCollectionService service;
    private final UserRepository userRepository;
    private final RegisterCommand registerCommand;
    private final Map<CommandType, ServerCommandHandler> handlers = new HashMap<>();

    public ServerCommandProcessor(ServerCollectionService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
        this.registerCommand = new RegisterCommand(userRepository);
        registerHandlers();
    }

    private void registerHandlers() {
        handlers.put(CommandType.HELP,
                new SimpleServerCommand(CommandType.HELP, s -> HelpFormatter.serverHelpMessage()));

        handlers.put(CommandType.INFO,
                new SimpleServerCommand(CommandType.INFO, ServerCollectionService::info));

        handlers.put(CommandType.CLEAR, new ClearCommand());

        handlers.put(CommandType.ADD, new AddCommand());
        handlers.put(CommandType.ADD_IF_MAX, new AddIfMaxCommand());
        handlers.put(CommandType.UPDATE, new UpdateCommand());
        handlers.put(CommandType.INSERT_AT, new InsertAtCommand());
        handlers.put(CommandType.REMOVE_BY_ID, new RemoveByIdCommand());

        handlers.put(CommandType.SHOW, new ShowCommand());
        handlers.put(CommandType.FILTER_BY_GOVERNOR, new FilterByGovernorCommand());
        handlers.put(CommandType.COUNT_LESS_THAN_STANDARD_OF_LIVING, new CountLessThanCommand());
        handlers.put(CommandType.PRINT_FIELD_ASCENDING_STANDARD_OF_LIVING, new PrintFieldAscendingCommand());

        handlers.put(CommandType.EXIT, (s, r) -> CommandResponse.ok("Client closed"));
    }

    public CommandResponse process(CommandRequest req) {
        try {
            CommandType type = req.getType();
            if (type == null) {
                return CommandResponse.fail("Empty command type");
            }

            if (type == CommandType.REGISTER) {
                return registerCommand.execute(service, req);
            }

            if (req.getLogin() == null || req.getLogin().isBlank()
                    || req.getPassword() == null) {
                return CommandResponse.fail("Требуются логин и пароль в каждом запросе");
            }

            Optional<Long> userId = userRepository.authenticate(req.getLogin(), req.getPassword());
            if (userId.isEmpty()) {
                return CommandResponse.fail("Неверный логин или пароль");
            }
            req.setAuthenticatedUserId(userId.get());

            ServerCommandHandler handler = handlers.get(type);
            if (handler == null) {
                return CommandResponse.fail("Unsupported command: " + type);
            }

            return handler.execute(service, req);

        } catch (Exception e) {
            return CommandResponse.fail("Server internal error: " + e.getMessage());
        }
    }

    public CommandResponse processServerConsoleCommand(String line) {
        if (line == null || line.length() == 0) {
            return null;
        }
        String command = line.toLowerCase();
        try {
            if ("help".equals(command)) {
                return CommandResponse.ok("Консоль сервера: help — краткая справка; данные только в PostgreSQL.");
            }
            return CommandResponse.fail("Unknown server command");
        } catch (Exception e) {
            return CommandResponse.fail("Server command error: " + e.getMessage());
        }
    }
}
