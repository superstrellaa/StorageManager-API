package es.superstrellaa.storagemanager.internal;

import es.superstrellaa.storagemanager.StorageManagerAPI;
import es.superstrellaa.storagemanager.api.data.RowData;
import es.superstrellaa.storagemanager.api.schema.Column;
import es.superstrellaa.storagemanager.api.schema.TableSchema;
import es.superstrellaa.storagemanager.internal.cache.WriteCache;
import org.jetbrains.annotations.ApiStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@ApiStatus.Internal
public final class TableExecutor {

    /**
     * Crea una tabla según el esquema proporcionado
     */
    public static void createTable(TableSchema schema) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ")
                .append(schema.getName())
                .append(" (");

        List<String> columnDefs = new ArrayList<>();

        for (Column column : schema.getColumns()) {
            StringBuilder def = new StringBuilder();
            def.append(column.name())
                    .append(" ")
                    .append(column.type().getSql());

            if (column.notNull()) {
                def.append(" NOT NULL");
            }

            columnDefs.add(def.toString());
        }

        if (!schema.getPrimaryKeys().isEmpty()) {
            columnDefs.add(
                    "PRIMARY KEY (" + String.join(", ", schema.getPrimaryKeys()) + ")"
            );
        }

        sql.append(String.join(", ", columnDefs));
        sql.append(");");

        execute(sql.toString());
    }

    /**
     * Inserta datos usando la caché (modo async, más rápido)
     */
    public static void insert(String table, RowData data) {
        insert(table, data, false);
    }

    /**
     * Inserta datos con opción de bypass de caché
     * @param immediate si es true, escribe inmediatamente a DB sin usar caché
     */
    public static void insert(String table, RowData data, boolean immediate) {
        if (immediate) {
            insertImmediate(table, data);
        } else {
            WriteCache.getInstance().queueInsert(table, data);
        }
    }

    /**
     * Inserción inmediata sin caché (para datos críticos)
     */
    private static void insertImmediate(String table, RowData data) {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");

        data.values().forEach((k, v) -> {
            columns.add(k);
            values.add("?");
        });

        String sql = "INSERT OR REPLACE INTO " + table +
                " (" + columns + ") VALUES (" + values + ");";

        try (PreparedStatement stmt = SQLiteBackend.getConnection().prepareStatement(sql)) {

            int index = 1;
            for (Object value : data.values().values()) {
                stmt.setObject(index++, value);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert into table " + table, e);
        }
    }

    /**
     * SELECT siempre lee directamente de la DB
     * Antes de leer, hace flush de la tabla para asegurar que los datos estén actualizados
     */
    public static List<RowData> select(String table, Map<String, Object> where) {
        // Flush antes de leer para tener datos actualizados por si acaso
        WriteCache.getInstance().flushTable(table);

        List<RowData> results = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table);

        if (!where.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(buildWhereClause(where));
        }

        sql.append(";");

        try (PreparedStatement stmt = SQLiteBackend.getConnection().prepareStatement(sql.toString())) {

            bindWhere(stmt, where);

            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            while (rs.next()) {
                RowData row = new RowData();

                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.set(meta.getColumnName(i), rs.getObject(i));
                }

                results.add(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to select from table " + table, e);
        }

        return results;
    }

    /**
     * Elimina usando caché por defecto
     */
    public static void delete(String table, Map<String, Object> where) {
        delete(table, where, false);
    }

    /**
     * Elimina con opción de inmediatez
     */
    public static void delete(String table, Map<String, Object> where, boolean immediate) {
        if (where.isEmpty()) {
            throw new IllegalArgumentException("DELETE without WHERE is not allowed");
        }

        if (immediate) {
            deleteImmediate(table, where);
        } else {
            WriteCache.getInstance().queueDelete(table, where);
        }
    }

    private static void deleteImmediate(String table, Map<String, Object> where) {
        String sql = "DELETE FROM " + table +
                " WHERE " + buildWhereClause(where) + ";";

        try (PreparedStatement stmt = SQLiteBackend.getConnection().prepareStatement(sql)) {

            bindWhere(stmt, where);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete from table " + table, e);
        }
    }

    private static String buildWhereClause(Map<String, Object> where) {
        StringJoiner joiner = new StringJoiner(" AND ");
        where.keySet().forEach(k -> joiner.add(k + " = ?"));
        return joiner.toString();
    }

    private static void bindWhere(PreparedStatement stmt, Map<String, Object> where)
            throws SQLException {

        int index = 1;
        for (Object value : where.values()) {
            stmt.setObject(index++, value);
        }
    }

    private static void execute(String sql) {
        try (Statement stmt = SQLiteBackend.getConnection().createStatement()) {
            stmt.execute(sql);
            StorageManagerAPI.LOGGER.debug("Executed SQL: {}", sql);
        } catch (SQLException e) {
            throw new RuntimeException("SQL execution failed", e);
        }
    }

    private TableExecutor() {}
}