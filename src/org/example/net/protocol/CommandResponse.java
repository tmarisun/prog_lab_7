package org.example.net.protocol;

import lombok.Getter;
import lombok.Setter;
import org.example.data.City;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CommandResponse implements Serializable {
    private boolean success;
    private String message;

    @Setter
    private List<City> cities = new ArrayList<>();

    public static CommandResponse ok(String message) {
        CommandResponse r = new CommandResponse();
        r.success = true;
        r.message = message;
        return r;
    }

    public static CommandResponse fail(String message) {
        CommandResponse r = new CommandResponse();
        r.success = false;
        r.message = message;
        return r;
    }

}

