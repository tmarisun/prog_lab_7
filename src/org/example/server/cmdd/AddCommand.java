package org.example.server.cmdd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

public class AddCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        City city = request.getCity();
        if (city == null) {
            return CommandResponse.fail(MessageKeys.CITY_REQUIRED);
        }
        try {
            City added = service.add(city, request.getAuthenticatedUserId(), request.getLogin());
            CommandResponse response = CommandResponse.ok(MessageKeys.ADDED, added.getId());
            response.getCities().add(added);
            return response;
        } catch (Exception e) {
            return CommandResponse.fail(MessageKeys.ADD_FAILED, e.getMessage());
        }
    }
}
