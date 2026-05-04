package org.example.server.cmdd;

import org.example.HelpFormatter;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.CommandType;
import org.example.server.ServerCollectionService;
import org.example.server.cmdd.ServerCommandHandler;

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
            String result = action.apply(service);
            return CommandResponse.ok(result);
        } catch (Exception e) {
            return CommandResponse.fail("Error executing " + type + ": " + e.getMessage());
        }
    }
}