package org.example.server.cmdd;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

public class ShowCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        var cities = service.getSortedByName();
        CommandResponse response = CommandResponse.ok(MessageKeys.SHOW_OK);
        response.setCities(cities);
        return response;
    }
}
