package org.example.server.cmdd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

import java.util.List;

public class FilterByGovernorCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        String governorName = request.getArg();
        if (governorName == null || governorName.isEmpty()) {
            return CommandResponse.fail(MessageKeys.GOVERNOR_ARG_REQUIRED);
        }

        List<City> list = service.filterByGovernor(governorName);
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        CommandResponse response = CommandResponse.ok(MessageKeys.FILTERED_GOVERNOR, governorName);
        response.setCities(list);
        return response;
    }
}
