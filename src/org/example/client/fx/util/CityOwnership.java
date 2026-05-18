package org.example.client.fx.util;

import org.example.data.City;

public final class CityOwnership {

    private CityOwnership() {}

    public static boolean isOwnedByUser(City city, String login) {
        if (city == null || login == null || login.isBlank()) {
            return false;
        }
        return login.equals(city.getOwnerLogin());
    }
}
