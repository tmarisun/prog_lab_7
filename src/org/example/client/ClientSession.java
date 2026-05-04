package org.example.client;

import org.example.net.protocol.CommandRequest;

public class ClientSession {
    private String login;
    private String password;

    public void set(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public boolean isPresent() {

        return login != null && !login.isBlank() && password != null;
    }

    public void applyTo(CommandRequest request) {
        request.setLogin(login);
        request.setPassword(password);
    }
}
