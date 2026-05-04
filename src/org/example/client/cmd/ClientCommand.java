package org.example.client.cmd;

import org.example.net.protocol.CommandRequest;

public interface ClientCommand {
    /**
     * Заполняет объект CommandRequest данными из строки аргументов.
     * @param arg Аргументы после имени команды (может быть null)
     * @param request Объект запроса для заполнения
     * @throws Exception Если аргументы некорректны
     */
    void execute(String arg, CommandRequest request) throws Exception;
}