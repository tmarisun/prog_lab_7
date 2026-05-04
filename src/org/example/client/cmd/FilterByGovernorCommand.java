package org.example.client.cmd;

import org.example.client.cmd.ClientCommand;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;

public class FilterByGovernorCommand implements ClientCommand {
    @Override
    public void execute(String arg, CommandRequest request) throws Exception {
        if (arg == null || arg.trim().isEmpty()) {
            throw new IllegalArgumentException("Governor name is required");
        }
        request.setType(CommandType.FILTER_BY_GOVERNOR);
        // Сохраняем имя губернатора как аргумент
        request.setArg(arg.trim());
    }
}