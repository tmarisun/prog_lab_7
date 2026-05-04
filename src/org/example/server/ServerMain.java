package org.example.server;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.config.AppConfig;
import org.example.db.CityRepository;
import org.example.db.Database;
import org.example.db.DatabaseCreator;
import org.example.db.SchemaInitializer;
import org.example.db.UserRepository;

import java.sql.Connection;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        int port = args.length >= 1
                ? Integer.parseInt(args[0])
                : Integer.parseInt(AppConfig.get(dotenv, "SERVER_PORT", "5555"));

        DatabaseCreator.tryCreateApplicationDatabase(dotenv);

        Database database = new Database(dotenv);
        System.out.println("Подключение к " + database.getJdbcUrlForLogging() + " как " + database.getUser());
        try (Connection c = database.getConnection()) {
            new SchemaInitializer().ensureSchema(c);
        } catch (Exception e) {
            System.err.println("Ошибка PostgreSQL: " + e.getMessage());
            System.err.println("Проверьте PG_HOST, PG_DATABASE, PG_USER и что СУБД принимает подключение без пароля для этого пользователя.");
            throw e;
        }

        UserRepository userRepository = new UserRepository(database);
        CityRepository cityRepository = new CityRepository(database);
        ServerCollectionService service = new ServerCollectionService(cityRepository);
        service.loadFromDatabase();

        ServerCommandProcessor processor = new ServerCommandProcessor(service, userRepository);
        System.out.println("TCP-сервер слушает порт " + port + " (подключайте клиентов к хосту helios и этому порту)");
        new ServerConnectionAcceptor(port, processor).start();
    }
}
