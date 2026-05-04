package org.example.client.cmd;

import org.example.data.StandardOfLiving;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;

public class CountLessThanCommand implements ClientCommand {
    @Override
    public void execute(String arg, CommandRequest request) throws Exception {
        if (arg == null || arg.trim().isEmpty()) {
            throw new IllegalArgumentException("StandardOfLiving value is required");
        }
        request.setType(CommandType.COUNT_LESS_THAN_STANDARD_OF_LIVING);
        // Преобразуем строку в Enum и сохраняем имя
        request.setArg(StandardOfLiving.valueOf(arg.trim().toUpperCase()).name());
    }
}