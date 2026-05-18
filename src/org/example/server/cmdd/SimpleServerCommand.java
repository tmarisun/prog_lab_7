package org.example.server.cmdd;

import org.example.HelpFormatter;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.CommandType;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

import java.util.function.Function;

public class SimpleServerCommand implements ServerCommandHandler {
    private final CommandType type;
    private final Function<ServerCollectionService, String> action;

    public SimpleServerCommand(CommandType type, Function<ServerCollectionService, String> action) {
        this.type = type;
        this.action = action;
    }

    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        try {
            if (type == CommandType.HELP) {
                return CommandResponse.okRaw(HelpFormatter.serverHelpMessage());
            }
            String result = action.apply(service);
            return CommandResponse.okRaw(result);
        } catch (Exception e) {
            return CommandResponse.fail(MessageKeys.EXECUTE_ERROR, type, e.getMessage());
        }
    }
}
