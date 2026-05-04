package org.example.server.cmdd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

public class UpdateCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        City city = request.getCity();
        Long id = request.getId();

        if (id == null || city == null) {
            return CommandResponse.fail("ID and City payload are required");
        }

        try {
            boolean updated = service.update(id, city, request.getAuthenticatedUserId());
            if (updated) {
                return CommandResponse.ok("Updated");
            }
            return CommandResponse.fail("City not found or not owned by you: " + id);
        } catch (Exception e) {
            return CommandResponse.fail("Update error: " + e.getMessage());
        }
    }
}