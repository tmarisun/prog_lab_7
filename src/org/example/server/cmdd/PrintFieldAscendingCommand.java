package org.example.server.cmdd;

import org.example.data.StandardOfLiving;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

import java.util.List;

public class PrintFieldAscendingCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        List<StandardOfLiving> values = service.getStandardsAscending();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(values.get(i).name());
        }

        return CommandResponse.ok(MessageKeys.PRINT_FIELD, builder.toString());
    }
}
