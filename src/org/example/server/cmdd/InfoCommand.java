package org.example.server.cmdd;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

public class InfoCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        return CommandResponse.ok(MessageKeys.INFO, service.collectionSize());
    }
}
