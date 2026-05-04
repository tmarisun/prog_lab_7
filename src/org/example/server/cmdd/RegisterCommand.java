package org.example.server.cmdd;

import org.example.db.UserRepository;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.server.ServerCollectionService;

public class RegisterCommand implements ServerCommandHandler {

    private final UserRepository userRepository;

    public RegisterCommand(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CommandResponse execute(ServerCollectionService service, CommandRequest request) {
        try {
            if (request.getLogin() == null || request.getLogin().isBlank()
                    || request.getPassword() == null || request.getPassword().isEmpty()) {
                return CommandResponse.fail("Укажите логин и пароль: register <login> <password>");
            }
            if (!userRepository.register(request.getLogin(), request.getPassword())) {
                return CommandResponse.fail("Пользователь с таким логином уже существует");
            }
            return CommandResponse.ok("Регистрация успешна. Выполните login на клиенте для дальнейшей работы.");
        } catch (IllegalArgumentException e) {
            return CommandResponse.fail(e.getMessage());
        } catch (Exception e) {
            return CommandResponse.fail("Ошибка регистрации: " + e.getMessage());
        }
    }
}
