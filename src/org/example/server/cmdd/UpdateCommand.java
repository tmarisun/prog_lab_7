package org.example.server.cmdd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

public class UpdateCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        City city = request.getCity();
        Long id = request.getId();

        if (id == null) {
            return CommandResponse.fail(MessageKeys.ID_REQUIRED);
        }

        try {
            if (city == null) {
                if (service.canUpdate(id, request.getAuthenticatedUserId())) {
                    return CommandResponse.ok(MessageKeys.CAN_UPDATE);
                }
                return CommandResponse.fail(MessageKeys.NOT_OWNED, id);
            }
            boolean updated = service.update(id, city, request.getAuthenticatedUserId());
            if (updated) {
                return CommandResponse.ok(MessageKeys.UPDATED);
            }
            return CommandResponse.fail(MessageKeys.NOT_OWNED, id);
        } catch (Exception e) {
            return CommandResponse.fail(MessageKeys.UPDATE_FAILED, e.getMessage());
        }
    }
}
