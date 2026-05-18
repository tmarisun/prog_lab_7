package org.example.server.cmdd;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

public class RemoveByIdCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        Long id = request.getId();
        if (id == null) {
            return CommandResponse.fail(MessageKeys.ID_REQUIRED);
        }
        try {
            boolean removed = service.removeById(id, request.getAuthenticatedUserId());
            if (removed) {
                return CommandResponse.ok(MessageKeys.REMOVED, id);
            }
            return CommandResponse.fail(MessageKeys.NOT_OWNED, id);
        } catch (Exception e) {
            return CommandResponse.fail(MessageKeys.REMOVE_FAILED, e.getMessage());
        }
    }
}
