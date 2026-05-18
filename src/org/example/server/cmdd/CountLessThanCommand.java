package org.example.server.cmdd;

import org.example.data.StandardOfLiving;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
import org.example.server.ServerCollectionService;

public class CountLessThanCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        String arg = request.getArg();
        if (arg == null || arg.isEmpty()) {
            return CommandResponse.fail(MessageKeys.SOL_REQUIRED);
        }

        try {
            StandardOfLiving val = StandardOfLiving.valueOf(arg.toUpperCase());
            long count = service.countLessThan(val);
            return CommandResponse.ok(MessageKeys.COUNT_LESS, val.name(), count);
        } catch (IllegalArgumentException e) {
            return CommandResponse.fail(MessageKeys.SOL_INVALID, arg);
        }
    }
}
