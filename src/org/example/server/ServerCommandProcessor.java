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

import java.util.Optional;

/**
 * Проверка логина и выбор команды по типу (простой switch).
 */
public class ServerCommandProcessor {

    private static final Logger log = LogManager.getLogger(ServerCommandProcessor.class);

    private final ServerCollectionService service;
    private final UserRepository userRepository;
    private final RegisterCommand registerCommand;
    private final InfoCommand infoCommand = new InfoCommand();
    private final ClearCommand clearCommand = new ClearCommand();
    private final AddCommand addCommand = new AddCommand();
    private final AddIfMaxCommand addIfMaxCommand = new AddIfMaxCommand();
    private final UpdateCommand updateCommand = new UpdateCommand();
    private final InsertAtCommand insertAtCommand = new InsertAtCommand();
    private final RemoveByIdCommand removeByIdCommand = new RemoveByIdCommand();
    private final ShowCommand showCommand = new ShowCommand();
    private final FilterByGovernorCommand filterByGovernorCommand = new FilterByGovernorCommand();
    private final CountLessThanCommand countLessThanCommand = new CountLessThanCommand();
    private final PrintFieldAscendingCommand printFieldAscendingCommand = new PrintFieldAscendingCommand();
    private final SimpleServerCommand helpCommand = new SimpleServerCommand(
            CommandType.HELP, s -> HelpFormatter.serverHelpMessage());

    public ServerCommandProcessor(ServerCollectionService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
        this.registerCommand = new RegisterCommand(userRepository);
    }

    public CommandResponse process(CommandRequest req) {
        try {
            CommandType type = req.getType();
            log.info("Обработка запроса: тип={}", type);
            if (type == null) {
                return CommandResponse.fail(MessageKeys.EMPTY_COMMAND);
            }

            if (type == CommandType.REGISTER) {
                return registerCommand.execute(service, req);
            }

            CommandResponse authError = authenticateAndAttachUser(req);
            if (authError != null) {
                return authError;
            }

            log.info("Аутентификация успешна, userId={}", req.getAuthenticatedUserId());
            return executeCommand(type, req);

        } catch (Exception e) {
            log.error("Внутренняя ошибка: {}", e.getMessage(), e);
            return CommandResponse.fail(MessageKeys.INTERNAL_ERROR, e.getMessage());
        }
    }

    private CommandResponse authenticateAndAttachUser(CommandRequest req) throws Exception {
        if (req.getLogin() == null || req.getLogin().isBlank() || req.getPassword() == null) {
            return CommandResponse.fail(MessageKeys.AUTH_REQUIRED);
        }

        Optional<Long> userIdOpt = userRepository.authenticate(req.getLogin(), req.getPassword());
        if (userIdOpt.isEmpty()) {
            return CommandResponse.fail(MessageKeys.AUTH_FAILED);
        }
        req.setAuthenticatedUserId(userIdOpt.get());
        return null;
    }

    private CommandResponse executeCommand(CommandType type, CommandRequest req) {
        switch (type) {
            case HELP:
                return helpCommand.execute(service, req);
            case INFO:
                return infoCommand.execute(service, req);
            case CLEAR:
                return clearCommand.execute(service, req);
            case ADD:
                return addCommand.execute(service, req);
            case ADD_IF_MAX:
                return addIfMaxCommand.execute(service, req);
            case UPDATE:
                return updateCommand.execute(service, req);
            case INSERT_AT:
                return insertAtCommand.execute(service, req);
            case REMOVE_BY_ID:
                return removeByIdCommand.execute(service, req);
            case SHOW:
                return showCommand.execute(service, req);
            case FILTER_BY_GOVERNOR:
                return filterByGovernorCommand.execute(service, req);
            case COUNT_LESS_THAN_STANDARD_OF_LIVING:
                return countLessThanCommand.execute(service, req);
            case PRINT_FIELD_ASCENDING_STANDARD_OF_LIVING:
                return printFieldAscendingCommand.execute(service, req);
            case EXIT:
                return CommandResponse.ok(MessageKeys.CLIENT_CLOSED);
            default:
                return CommandResponse.fail(MessageKeys.UNSUPPORTED_COMMAND, type);
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
