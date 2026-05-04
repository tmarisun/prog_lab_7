package org.example.client.cmd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;

public class InsertAtCommand implements ClientCommand {
    @Override
    public void execute(String arg, CommandRequest request) throws Exception {
        if (arg == null || arg.trim().isEmpty()) {
            throw new IllegalArgumentException("Нужен индекс: insert_at <индекс>");
        }
        String[] parts = arg.trim().split("\\s+");
        request.setType(CommandType.INSERT_AT);
        try {
            request.setIndex(Integer.parseInt(parts[0]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Индекс должен быть целым числом");
        }
        if (parts.length > 1) {
            throw new IllegalArgumentException("Лишние аргументы. Использование: insert_at <индекс> (город — с консоли)");
        }
        City city = CityInputHelper.readCity();
        if (city == null) {
            throw new IllegalArgumentException("Ввод города отменён или некорректен");
        }
        request.setCity(city);
    }
}
