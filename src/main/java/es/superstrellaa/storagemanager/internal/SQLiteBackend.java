package es.superstrellaa.storagemanager.internal;

import es.superstrellaa.storagemanager.StorageManagerAPI;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@ApiStatus.Internal
public final class SQLiteBackend {

    private static Connection connection;

    public static void init() {
        try {
            Path dbPath = StoragePaths.getDatabasePath();
            Files.createDirectories(dbPath.getParent());

            String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
            connection = DriverManager.getConnection(url);

            applyPragmas();

            StorageManagerAPI.LOGGER.info("SQLite initialized at {}", dbPath);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLite", e);
        }
    }

    private static void applyPragmas() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=NORMAL;");
            stmt.execute("PRAGMA foreign_keys=ON;");
            stmt.execute("PRAGMA temp_store=MEMORY;");
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            StorageManagerAPI.LOGGER.error("Error closing SQLite connection", e);
        }
    }

    private SQLiteBackend() {}
}
