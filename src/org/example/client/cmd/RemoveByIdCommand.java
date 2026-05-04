package org.example.client.cmd;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;

public class RemoveByIdCommand implements ClientCommand {
    @Override
    public void execute(String arg, CommandRequest request) throws Exception {
        if (arg == null || arg.trim().isEmpty()) {
            throw new IllegalArgumentException("ID is required");
        }
        request.setType(CommandType.REMOVE_BY_ID);
        request.setId(Long.parseLong(arg.trim()));
    }
}