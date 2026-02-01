package es.superstrellaa.storagemanager.internal;

import es.superstrellaa.storagemanager.StorageManagerAPI;
import es.superstrellaa.storagemanager.api.data.RowData;
import es.superstrellaa.storagemanager.api.schema.Column;
import es.superstrellaa.storagemanager.api.schema.TableSchema;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public final class TableExecutor {

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

    public static void insert(String table, RowData data) {
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

    public static List<RowData> select(String table, Map<String, Object> where) {
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

    public static void delete(String table, Map<String, Object> where) {
        if (where.isEmpty()) {
            throw new IllegalArgumentException("DELETE without WHERE is not allowed");
        }

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
