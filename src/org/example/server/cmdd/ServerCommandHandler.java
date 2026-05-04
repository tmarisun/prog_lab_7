package org.example.server.cmdd;

import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

public interface ServerCommandHandler {
    /**
     * Выполняет команду и возвращает ответ клиенту.
     * @param service Сервис для работы с коллекцией
     * @param request Запрос от клиента
     * @return Ответ для клиента
     */
    CommandResponse execute(ServerCollectionService service, CommandRequest request);
}