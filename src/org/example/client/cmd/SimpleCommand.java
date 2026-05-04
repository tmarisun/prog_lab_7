package org.example.client.cmd;

import org.example.client.cmd.ClientCommand;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;

public class SimpleCommand implements ClientCommand {
    private final CommandType type;

    public SimpleCommand(CommandType type) {
        this.type = type;
    }

    @Override
    public void execute(String arg, CommandRequest request) {
        if (arg != null && !arg.trim().isEmpty()) {
            throw new IllegalArgumentException("Command '" + type.name() + "' does not accept arguments");
        }
        request.setType(type);
    }
}