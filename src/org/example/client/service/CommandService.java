package org.example.client.service;

import org.example.client.ClientNetworkChannel;
import org.example.client.ClientSession;
import org.example.data.City;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.CommandType;
import org.example.net.protocol.MessageKeys;

import java.util.List;

/**
 * Вызовы сервера для GUI (обёртка над {@link ClientNetworkChannel}).
 */
public class CommandService {

    private final ClientNetworkChannel channel = new ClientNetworkChannel();
    private final ClientSession session = new ClientSession();

    public ClientSession getSession() {
        return session;
    }

    public boolean isLoggedIn() {
        return session.isPresent();
    }

    public String getLogin() {
        return session.isPresent() ? session.getLogin() : null;
    }

    /**
     * Вход: сохраняем учётные данные; проверка — лёгкой командой {@link #info()}.
     */
    public CommandResponse login(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isEmpty()) {
            return CommandResponse.fail(MessageKeys.EMPTY_CREDENTIALS);
        }
        session.set(login.trim(), password);
        CommandResponse check = info();
        if (!check.isSuccess()) {
            session.clear();
        }
        return check;
    }

    public void logout() {
        session.clear();
    }

    public CommandResponse register(String login, String password) {
        CommandRequest req = new CommandRequest();
        req.setType(CommandType.REGISTER);
        req.setLogin(login != null ? login.trim() : null);
        req.setPassword(password);
        return channel.send(req);
    }

    public CommandResponse show() {
        return send(CommandType.SHOW, null);
    }

    public CommandResponse info() {
        return send(CommandType.INFO, null);
    }

    public CommandResponse help() {
        return send(CommandType.HELP, null);
    }

    public CommandResponse removeById(long id) {
        CommandRequest req = authenticated(CommandType.REMOVE_BY_ID, null);
        req.setId(id);
        return channel.send(req);
    }

    public CommandResponse update(long id, City city) {
        CommandRequest req = authenticated(CommandType.UPDATE, null);
        req.setId(id);
        req.setCity(city);
        return channel.send(req);
    }

    public CommandResponse add(City city) {
        CommandRequest req = authenticated(CommandType.ADD, null);
        req.setCity(city);
        return channel.send(req);
    }

    public CommandResponse clear() {
        return send(CommandType.CLEAR, null);
    }

    public CommandResponse countLessThanStandardOfLiving(String arg) {
        return send(CommandType.COUNT_LESS_THAN_STANDARD_OF_LIVING, arg);
    }

    public CommandResponse filterByGovernor(String arg) {
        return send(CommandType.FILTER_BY_GOVERNOR, arg);
    }

    public CommandResponse printFieldAscendingStandardOfLiving() {
        return send(CommandType.PRINT_FIELD_ASCENDING_STANDARD_OF_LIVING, null);
    }

    public CommandResponse addIfMax(City city) {
        CommandRequest req = authenticated(CommandType.ADD_IF_MAX, null);
        req.setCity(city);
        return channel.send(req);
    }

    public CommandResponse insertAt(City city, Integer index) {
        CommandRequest req = authenticated(CommandType.INSERT_AT, null);
        req.setCity(city);
        req.setIndex(index);
        return channel.send(req);
    }

    public List<City> citiesFrom(CommandResponse response) {
        return response != null && response.getCities() != null
                ? response.getCities()
                : List.of();
    }

    private CommandResponse send(CommandType type, String arg) {
        return channel.send(authenticated(type, arg));
    }

    private CommandRequest authenticated(CommandType type, String arg) {
        CommandRequest req = new CommandRequest();
        req.setType(type);
        req.setArg(arg);
        if (session.isPresent()) {
            session.applyTo(req);
        }
        return req;
    }
}
