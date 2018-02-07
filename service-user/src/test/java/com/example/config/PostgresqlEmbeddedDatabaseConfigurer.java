package com.example.config;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.util.ClassUtils;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.yandex.qatools.embed.postgresql.EmbeddedPostgres.cachedRuntimeConfig;

public class PostgresqlEmbeddedDatabaseConfigurer implements EmbeddedDatabaseConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresqlEmbeddedDatabaseConfigurer.class.getName());
    private static final AtomicInteger CONNEXION_COUNTER = new AtomicInteger();

    private static EmbeddedDatabaseConfigurer configurerInstance;

    private final Class<? extends Driver> driverClass;

    private PostgresProcess postgresProcess;

    private PostgresqlEmbeddedDatabaseConfigurer(Class<? extends Driver> driverClass) {
        this.driverClass = driverClass;
    }

    /**
     * Get PostgresqlEmbeddedDatabaseConfigurer instance
     */
    @SuppressWarnings("unchecked")
    static synchronized EmbeddedDatabaseConfigurer getInstance() throws ClassNotFoundException {
        if (configurerInstance == null) {
            configurerInstance = new PostgresqlEmbeddedDatabaseConfigurer((Class<? extends Driver>)
                    ClassUtils.forName("org.postgresql.Driver",
                            PostgresqlEmbeddedDatabaseConfigurer.class.getClassLoader()));
        }
        return configurerInstance;
    }

    @Override
    public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
        try {

            //Increment connexionCounter
            CONNEXION_COUNTER.incrementAndGet();

            PostgresConfig config;

            if (postgresProcess == null || !postgresProcess.isProcessRunning()) {
                PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter
                        .getInstance(buildCachedRuntimeConfig());
                config = PostgresConfig.defaultWithDbName(databaseName, "dummy_user", "dummy_password");
                PostgresExecutable exec = runtime.prepare(config);
                postgresProcess = exec.start();

            } else {
                // postgresProcess already running
                config = postgresProcess.getConfig();
            }

            String url = String.format("jdbc:postgresql://%s:%s/%s",
                    config.net().host(),
                    config.net().port(),
                    config.storage().dbName()

            );

            LOGGER.info("Configure DB, driver = {}, url = {}", driverClass.getName(), url);

            properties.setDriverClass(driverClass);
            properties.setUrl(url);
            properties.setUsername(config.credentials().username());
            properties.setPassword(config.credentials().password());

        } catch (IOException ex) {
            //Decrement when connexion fail
            CONNEXION_COUNTER.decrementAndGet();
            LOGGER.warn("Could not start Postgresql embedded database : {}", ex.getLocalizedMessage());
        }
    }

    @Override
    public void shutdown(DataSource dataSource, String databaseName) {
        //Stop postgres process when all connexions are closed
        if (CONNEXION_COUNTER.decrementAndGet() <= 0) {
            try {
                dataSource.getConnection().close();
                postgresProcess.stop();
            } catch (SQLException e) {
                LOGGER.warn("Could not stop Postgresql embedded database : {}", e.getLocalizedMessage());
            }
        }
    }

    private static IRuntimeConfig buildCachedRuntimeConfig() throws IOException {
        Path embeddedPostgresCachedRuntimeConfigPath = Paths.get(System.getProperty("java.io.tmpdir") + "/cached.embedded.postgres");
        if (!Files.exists(embeddedPostgresCachedRuntimeConfigPath)) {
            Files.createDirectory(embeddedPostgresCachedRuntimeConfigPath);
        }
        LOGGER.info("Loading config from path '{}'", embeddedPostgresCachedRuntimeConfigPath);
        return cachedRuntimeConfig(embeddedPostgresCachedRuntimeConfigPath);
    }
}
