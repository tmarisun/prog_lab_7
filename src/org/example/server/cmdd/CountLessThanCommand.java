package org.example.server.cmdd;

import org.example.data.StandardOfLiving;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

public class CountLessThanCommand implements ServerCommandHandler {
    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        String arg = request.getArg();
        if (arg == null || arg.isEmpty()) {
            return CommandResponse.fail("StandardOfLiving value is required");
        }

        try {
            StandardOfLiving val = StandardOfLiving.valueOf(arg.toUpperCase());
            long count = service.countLessThan(val);
            return CommandResponse.ok("Count less than " + val + ": " + count);
        } catch (IllegalArgumentException e) {
            return CommandResponse.fail("Invalid StandardOfLiving value: " + arg);
        }
    }
}