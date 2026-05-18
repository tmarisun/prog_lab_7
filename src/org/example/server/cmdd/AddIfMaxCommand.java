package org.example.server.cmdd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

public class AddIfMaxCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        City city = request.getCity();
        if (city == null) {
            return CommandResponse.fail(MessageKeys.CITY_REQUIRED);
        }

        try {
            boolean added = service.addIfMax(city, request.getAuthenticatedUserId(), request.getLogin());
            if (added) {
                return CommandResponse.ok(MessageKeys.ADD_IF_MAX_OK);
            }
            return CommandResponse.fail(MessageKeys.ADD_IF_MAX_FAIL);
        } catch (Exception e) {
            return CommandResponse.fail(MessageKeys.ADD_FAILED, e.getMessage());
        }
    }
}
