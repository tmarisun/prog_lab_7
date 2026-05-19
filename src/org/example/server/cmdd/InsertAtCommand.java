package org.example.server.cmdd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

public class InsertAtCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        City city = request.getCity();
        if (city == null) {
            return CommandResponse.fail(MessageKeys.CITY_REQUIRED);
        }

        try {
            int index;
            if (request.getIndex() == null) {
                index = -1;
            } else {
                index = request.getIndex();
            }
            boolean inserted = service.insertAt(index, city, request.getAuthenticatedUserId(), request.getLogin());
            if (inserted) {
                return CommandResponse.ok(MessageKeys.INSERT_OK);
            }
            return CommandResponse.fail(MessageKeys.INSERT_FAIL);
        } catch (Exception e) {
            return CommandResponse.fail(MessageKeys.INSERT_FAIL, e.getMessage());
        }
    }
}
