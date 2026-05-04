package org.example.server.cmdd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

import java.util.List;

public class FilterByGovernorCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        String governorName = request.getArg();
        if (governorName == null || governorName.isEmpty()) {
            return CommandResponse.fail("Governor name argument is required");
        }

        List<City> list = service.filterByGovernor(governorName);
        // Сортируем результат по имени, как было в оригинале
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        CommandResponse response = CommandResponse.ok("Filtered by governor: " + governorName);
        response.setCities(list);
        return response;
    }
}