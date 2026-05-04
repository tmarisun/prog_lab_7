package org.example.server.cmdd;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

public class ShowCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        // Получаем отсортированную коллекцию
        var cities = service.getSortedByName();

        CommandResponse response = CommandResponse.ok("Collection displayed");
        response.setCities(cities);
        return response;
    }
}