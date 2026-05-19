package org.example.net.protocol;

/**
 * Ключи сообщений для {@link CommandResponse}. Клиент переводит через i18n.
 */
public final class MessageKeys {

    private MessageKeys() {}

    public static final String SERVER_UNAVAILABLE = "response.server.unavailable";
    public static final String SERVER_UNAVAILABLE_DETAIL = "response.server.unavailableDetail";
    public static final String CONNECTION_REFUSED = "response.server.connectionRefused";
    public static final String SERVER_CONNECTION_CLOSED = "response.server.connectionClosed";

    public static final String AUTH_REQUIRED = "response.auth.required";
    public static final String AUTH_FAILED = "response.auth.failed";
    public static final String EMPTY_COMMAND = "response.server.emptyCommand";
    public static final String INTERNAL_ERROR = "response.server.internalError";
    public static final String UNSUPPORTED_COMMAND = "response.server.unsupportedCommand";
    public static final String EXECUTE_ERROR = "response.server.executeError";

    public static final String CITY_REQUIRED = "response.city.required";
    public static final String ID_REQUIRED = "response.id.required";
    public static final String NOT_OWNED = "response.city.notOwned";
    public static final String ADDED = "response.city.added";
    public static final String ADD_FAILED = "response.city.addFailed";
    public static final String UPDATED = "response.city.updated";
    public static final String UPDATE_FAILED = "response.city.updateFailed";
    public static final String REMOVED = "response.city.removed";
    public static final String REMOVE_FAILED = "response.city.removeFailed";
    public static final String CLEARED = "response.collection.cleared";
    public static final String CLEAR_FAILED = "response.collection.clearFailed";

    public static final String COUNT_LESS = "response.count.lessThan";
    public static final String SOL_REQUIRED = "response.sol.required";
    public static final String SOL_INVALID = "response.sol.invalid";
    public static final String GOVERNOR_ARG_REQUIRED = "response.governor.argRequired";
    public static final String FILTERED_GOVERNOR = "response.governor.filtered";

    public static final String ADD_IF_MAX_OK = "response.addIfMax.ok";
    public static final String ADD_IF_MAX_FAIL = "response.addIfMax.fail";
    public static final String INSERT_OK = "response.insert.ok";
    public static final String INSERT_FAIL = "response.insert.fail";

    public static final String REGISTER_OK = "response.register.ok";
    public static final String REGISTER_EXISTS = "response.register.exists";
    public static final String REGISTER_ARGS = "response.register.args";
    public static final String REGISTER_FAILED = "response.register.failed";

    public static final String EMPTY_CREDENTIALS = "response.auth.emptyCredentials";
    public static final String CLIENT_CLOSED = "response.client.closed";

    public static final String SHOW_OK = "response.show.ok";
    public static final String INFO = "response.info";
    public static final String PRINT_FIELD = "response.printField";
    public static final String CAN_UPDATE = "response.city.canUpdate";
}
