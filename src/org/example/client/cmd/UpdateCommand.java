package org.example.client.cmd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;

public class UpdateCommand implements ClientCommand {
    @Override
    public void execute(String arg, CommandRequest request) throws Exception {
        if (arg == null || arg.trim().isEmpty()) {
            throw new IllegalArgumentException("Нужен id: update <id>");
        }
        String[] parts = arg.trim().split("\\s+");
        request.setType(CommandType.UPDATE);
        try {
            request.setId(Long.parseLong(parts[0]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id должен быть числом");
        }
        if (parts.length > 1) {
            throw new IllegalArgumentException("Лишние аргументы. Использование: update <id> (поля — с консоли)");
        }
        City city = CityInputHelper.readCity();
        if (city == null) {
            throw new IllegalArgumentException("Ввод города отменён или некорректен");
        }
        request.setCity(city);
    }
}
