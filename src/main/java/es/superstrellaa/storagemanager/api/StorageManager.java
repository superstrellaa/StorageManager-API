package es.superstrellaa.storagemanager.api;

import es.superstrellaa.storagemanager.api.data.RowData;
import es.superstrellaa.storagemanager.api.schema.TableSchema;
import es.superstrellaa.storagemanager.internal.TableExecutor;

import java.util.List;
import java.util.Map;

public final class StorageManager {

    public static void registerTable(TableSchema schema) {
        TableExecutor.createTable(schema);
    }

    public static void insert(String table, RowData data) {
        TableExecutor.insert(table, data);
    }

    public static List<RowData> select(String table, Map<String, Object> where) {
        return TableExecutor.select(table, where);
    }

    public static void delete(String table, Map<String, Object> where) {
        TableExecutor.delete(table, where);
    }

    private StorageManager() {}
}
