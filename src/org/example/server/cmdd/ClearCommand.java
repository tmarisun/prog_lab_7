package org.example.server.cmdd;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

public class ClearCommand implements ServerCommandHandler {

    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        try {
            service.clearForUser(request.getAuthenticatedUserId());
            return CommandResponse.ok("Удалены все ваши объекты из коллекции");
        } catch (Exception e) {
            return CommandResponse.fail("Ошибка очистки: " + e.getMessage());
        }
    }
}
