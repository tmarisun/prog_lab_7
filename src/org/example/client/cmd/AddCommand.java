package org.example.client.cmd;

import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandType;

public class AddCommand implements ClientCommand {
    @Override
    public void execute(String arg, CommandRequest request) throws Exception {
        request.setType(CommandType.ADD);
        City city = CityInputHelper.readCity();
        if (city == null) {
            throw new IllegalArgumentException("Ввод города отменён или некорректен");
        }
        request.setCity(city);
    }
}
