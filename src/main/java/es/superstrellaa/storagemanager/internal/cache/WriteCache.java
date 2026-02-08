package es.superstrellaa.storagemanager.internal.cache;

import es.superstrellaa.storagemanager.StorageManagerAPI;
import es.superstrellaa.storagemanager.api.data.RowData;
import es.superstrellaa.storagemanager.internal.SQLiteBackend;
import org.jetbrains.annotations.ApiStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
public final class WriteCache {

    private static final long FLUSH_INTERVAL_MS = 5000;
    private static final int BATCH_SIZE = 100;

    private final Map<String, List<PendingOperation>> pendingOps = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "StorageManager-WriteCache");
        t.setDaemon(true);
        return t;
    });

    private static WriteCache instance;

    private WriteCache() {}

    public static WriteCache getInstance() {
        if (instance == null) {
            instance = new WriteCache();
        }
        return instance;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                this::flushAll,
                FLUSH_INTERVAL_MS,
                FLUSH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
        StorageManagerAPI.LOGGER.info("WriteCache started with {}ms flush interval", FLUSH_INTERVAL_MS);
    }

    /**
     * Encola una operación de inserción para ser ejecutada más tarde
     */
    public void queueInsert(String table, RowData data) {
        pendingOps.computeIfAbsent(table, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new PendingOperation(OperationType.INSERT, data));

        // Si hay muchas operaciones pendientes, hacer flush inmediato no vaya que pete o algo random
        if (pendingOps.get(table).size() >= BATCH_SIZE) {
            flushTable(table);
        }
    }

    /**
     * Encola una operación de eliminación
     */
    public void queueDelete(String table, Map<String, Object> where) {
        pendingOps.computeIfAbsent(table, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new PendingOperation(OperationType.DELETE, where));
    }

    /**
     * Hace flush de todas las operaciones pendientes de una tabla específica
     */
    public void flushTable(String table) {
        List<PendingOperation> ops = pendingOps.remove(table);
        if (ops == null || ops.isEmpty()) {
            return;
        }

        synchronized (ops) {
            try {
                SQLiteBackend.getConnection().setAutoCommit(false);

                for (PendingOperation op : ops) {
                    if (op.type == OperationType.INSERT) {
                        executeInsert(table, op.data);
                    } else if (op.type == OperationType.DELETE) {
                        executeDelete(table, op.whereClause);
                    }
                }

                SQLiteBackend.getConnection().commit();
                StorageManagerAPI.LOGGER.debug("Flushed {} operations for table {}", ops.size(), table);

            } catch (SQLException e) {
                try {
                    SQLiteBackend.getConnection().rollback();
                } catch (SQLException ex) {
                    StorageManagerAPI.LOGGER.error("Failed to rollback transaction", ex);
                }
                throw new RuntimeException("Failed to flush operations for table " + table, e);
            } finally {
                try {
                    SQLiteBackend.getConnection().setAutoCommit(true);
                } catch (SQLException e) {
                    StorageManagerAPI.LOGGER.error("Failed to restore auto-commit", e);
                }
            }
        }
    }

    /**
     * Hace flush de todas las tablas
     */
    public void flushAll() {
        Set<String> tables = new HashSet<>(pendingOps.keySet());
        if (tables.isEmpty()) {
            return;
        }

        StorageManagerAPI.LOGGER.debug("Flushing {} tables...", tables.size());
        for (String table : tables) {
            flushTable(table);
        }
    }

    /**
     * Detiene el scheduler y hace flush final
     */
    public void shutdown() {
        StorageManagerAPI.LOGGER.info("Shutting down WriteCache...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Flush final de todo lo pendiente por si acaso
        flushAll();
        StorageManagerAPI.LOGGER.info("WriteCache shutdown complete");
    }

    private void executeInsert(String table, RowData data) throws SQLException {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");

        data.values().forEach((k, v) -> {
            columns.add(k);
            placeholders.add("?");
        });

        String sql = "INSERT OR REPLACE INTO " + table +
                " (" + columns + ") VALUES (" + placeholders + ")";

        try (PreparedStatement stmt = SQLiteBackend.getConnection().prepareStatement(sql)) {
            int index = 1;
            for (Object value : data.values().values()) {
                stmt.setObject(index++, value);
            }
            stmt.executeUpdate();
        }
    }

    private void executeDelete(String table, Map<String, Object> where) throws SQLException {
        StringJoiner whereClause = new StringJoiner(" AND ");
        where.keySet().forEach(k -> whereClause.add(k + " = ?"));

        String sql = "DELETE FROM " + table + " WHERE " + whereClause;

        try (PreparedStatement stmt = SQLiteBackend.getConnection().prepareStatement(sql)) {
            int index = 1;
            for (Object value : where.values()) {
                stmt.setObject(index++, value);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Operación pendiente en la caché
     */
    private static class PendingOperation {
        final OperationType type;
        final RowData data;
        final Map<String, Object> whereClause;

        PendingOperation(OperationType type, RowData data) {
            this.type = type;
            this.data = data;
            this.whereClause = null;
        }

        PendingOperation(OperationType type, Map<String, Object> whereClause) {
            this.type = type;
            this.data = null;
            this.whereClause = whereClause;
        }
    }

    private enum OperationType {
        INSERT, DELETE
    }
}