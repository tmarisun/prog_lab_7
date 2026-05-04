package org.example.server.cmdd;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

public class RemoveByIdCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        Long id = request.getId();
        if (id == null) {
            return CommandResponse.fail("ID is required for removal");
        }

        try {
            boolean removed = service.removeById(id, request.getAuthenticatedUserId());
            if (removed) {
                return CommandResponse.ok("Removed city with ID: " + id);
            }
            return CommandResponse.fail("City not found or not owned by you: " + id);
        } catch (Exception e) {
            return CommandResponse.fail("Remove error: " + e.getMessage());
        }
    }
}