package org.example.client.cmd;

import org.example.data.City;
import org.example.service.CityReader;

public final class CityInputHelper {

    private CityInputHelper() {
    }

    public static City readCity() throws Exception {
        return CityReader.readCity();
    }
}
