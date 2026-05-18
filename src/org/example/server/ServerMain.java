package org.example.server;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.AppConfig;
import org.example.db.CityRepository;
import org.example.db.Database;
import org.example.db.DatabaseCreator;
import org.example.db.SchemaInitializer;
import org.example.db.UserRepository;

import java.sql.Connection;

public class ServerMain {

    private static final Logger log = LogManager.getLogger(ServerMain.class);

    public static void main(String[] args) throws Exception {
        log.info("Запуск сервера: инициализация конфигурации");
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        int port;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        } else {
            port = Integer.parseInt(AppConfig.get(dotenv, "SERVER_PORT", "5234"));
        }
        log.info("Параметры запуска: порт прослушивания TCP = {}", port);

        log.info("Проверка/создание базы приложения (если настроено)");
        DatabaseCreator.tryCreateApplicationDatabase(dotenv);

        Database database = new Database(dotenv);
        log.info("Подключение к СУБД: {} , пользователь JDBC: {}", database.getJdbcUrlForLogging(), database.getUser());
        try (Connection c = database.getConnection()) {
            log.info("Соединение с PostgreSQL установлено, применение схемы");
            new SchemaInitializer().ensureSchema(c);
            log.info("Схема БД готова");
        } catch (Exception e) {
            log.error("Ошибка PostgreSQL при старте: {}. Проверьте PG_HOST, PG_DATABASE, PG_USER и доступ СУБД.",
                    e.getMessage(), e);
            throw e;
        }

        UserRepository userRepository = new UserRepository(database);
        CityRepository cityRepository = new CityRepository(database);
        ServerCollectionService service = new ServerCollectionService(cityRepository);
        service.loadFromDatabase();

        ServerCommandProcessor processor = new ServerCommandProcessor(service, userRepository);
        log.info("TCP-сервер готов к приёму подключений на порту {}", port);
        new ServerConnectionAcceptor(port, processor).start();
    }
}
