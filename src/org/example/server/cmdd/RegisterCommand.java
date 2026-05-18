package org.example.server.cmdd;

import org.example.db.UserRepository;
import org.example.net.protocol.CommandRequest;
import org.example.net.protocol.CommandResponse;
import org.example.net.protocol.MessageKeys;
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
                return CommandResponse.fail(MessageKeys.REGISTER_ARGS);
            }
            if (!userRepository.register(request.getLogin(), request.getPassword())) {
                return CommandResponse.fail(MessageKeys.REGISTER_EXISTS);
            }
            return CommandResponse.ok(MessageKeys.REGISTER_OK);
        } catch (IllegalArgumentException e) {
            return CommandResponse.failRaw(e.getMessage());
        } catch (Exception e) {
            return CommandResponse.fail(MessageKeys.REGISTER_FAILED, e.getMessage());
        }
    }
}
