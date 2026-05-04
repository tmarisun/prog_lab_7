package org.example.server.cmdd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

public class AddCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        City city = request.getCity();
        if (city == null) {
            return CommandResponse.fail("City payload is required");
        }
        try {
            City added = service.add(city, request.getAuthenticatedUserId(), request.getLogin());
            return CommandResponse.ok("Added city with id " + added.getId());
        } catch (Exception e) {
            return CommandResponse.fail("Failed to add city: " + e.getMessage());
        }
    }
}