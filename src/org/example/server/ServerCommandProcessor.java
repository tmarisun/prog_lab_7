package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.HelpFormatter;
import org.example.db.UserRepository;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.CommandType;
import org.example.net.protocol.MessageKeys;
import org.example.server.cmdd.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServerCommandProcessor {

    private static final Logger log = LogManager.getLogger(ServerCommandProcessor.class);

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

        handlers.put(CommandType.INFO, new InfoCommand());

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

        handlers.put(CommandType.EXIT, (s, r) -> CommandResponse.ok(MessageKeys.CLIENT_CLOSED));
    }

    public CommandResponse process(CommandRequest req) {
        try {
            CommandType type = req.getType();
            log.info("Обработка запроса: тип={}", type);
            if (type == null) {
                log.warn("Отклонён запрос: пустой тип команды");
                return CommandResponse.fail(MessageKeys.EMPTY_COMMAND);
            }

            if (type == CommandType.REGISTER) {
                log.info("Регистрация пользователя: логин={}", req.getLogin());
                CommandResponse r = registerCommand.execute(service, req);
                log.info("Регистрация завершена: success={}", r.isSuccess());
                return r;
            }

            CommandResponse authError = authenticateAndAttachUser(req);
            if (authError != null) {
                log.warn("Аутентификация не пройдена для логина={}: {}", req.getLogin(), authError.getMessage());
                return authError;
            }

            log.info("Аутентификация успешна, userId={}, выполнение команды {}", req.getAuthenticatedUserId(), type);
            CommandResponse out = dispatch(type, req);
            log.info("Команда {} выполнена: success={}", type, out.isSuccess());
            return out;

        } catch (Exception e) {
            log.error("Внутренняя ошибка при обработке запроса: {}", e.getMessage(), e);
            return CommandResponse.fail(MessageKeys.INTERNAL_ERROR, e.getMessage());
        }
    }

    private CommandResponse authenticateAndAttachUser(CommandRequest req) throws Exception {
        if (req.getLogin() == null || req.getLogin().isBlank() || req.getPassword() == null) {
            return CommandResponse.fail(MessageKeys.AUTH_REQUIRED);
        }

        Optional<Long> userId = userRepository.authenticate(req.getLogin(), req.getPassword());
        if (userId.isEmpty()) {
            return CommandResponse.fail(MessageKeys.AUTH_FAILED);
        }
        req.setAuthenticatedUserId(userId.get());
        return null;
    }

    private CommandResponse dispatch(CommandType type, CommandRequest req) {
        ServerCommandHandler handler = handlers.get(type);
        if (handler == null) {
            return CommandResponse.fail(MessageKeys.UNSUPPORTED_COMMAND, type);
        }
        return handler.execute(service, req);
    }

    public CommandResponse processServerConsoleCommand(String line) {
        if (line == null || line.length() == 0) {
            return null;
        }
        String command = line.toLowerCase();
        try {
            if ("help".equals(command)) {
                log.info("Консоль сервера: выполнена команда help");
                return CommandResponse.ok("Консоль сервера: help — краткая справка; данные только в PostgreSQL.");
            }
            log.warn("Консоль сервера: неизвестная команда «{}»", line);
            return CommandResponse.fail("Unknown server command");
        } catch (Exception e) {
            log.error("Ошибка консольной команды сервера: {}", e.getMessage(), e);
            return CommandResponse.fail("Server command error: " + e.getMessage());
        }
    }
}
