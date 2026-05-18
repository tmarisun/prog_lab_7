package org.example.client.cmd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;

public class InsertAtCommand implements ClientCommand {
    @Override
    public void execute(String arg, CommandRequest request) throws Exception {
        request.setType(CommandType.INSERT_AT);

        // In append-only mode index is ignored; parse it only for backward compatibility.
        if (arg != null && !arg.trim().isEmpty()) {
            String[] parts = arg.trim().split("\\s+");
            if (parts.length > 1) {
                throw new IllegalArgumentException("Использование: insert_at [индекс] (индекс опционален и игнорируется)");
            }
            try {
                request.setIndex(Integer.parseInt(parts[0]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Индекс должен быть целым числом");
            }
        }

        City city = CityInputHelper.readCity();
        if (city == null) {
            throw new IllegalArgumentException("Ввод города отменён или некорректен");
        }
        request.setCity(city);
    }
}
