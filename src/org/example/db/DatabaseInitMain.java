package org.example.db;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;

/** Создаёт таблицы users и cities в схеме PostgreSQL (без запуска TCP-сервера). */
public final class DatabaseInitMain {

    private static final Logger log = LogManager.getLogger(DatabaseInitMain.class);

    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Database database = new Database(dotenv);
        log.info("Подключение: {} , пользователь: {}, схема: {}",
                database.getJdbcUrlForLogging(), database.getUser(), database.getSchema());
        try (Connection c = database.getConnection()) {
            new SchemaInitializer().ensureSchema(c);
            log.info("Таблицы users и cities готовы в схеме {}", database.getSchema());
        }
    }

    private DatabaseInitMain() {}
}
