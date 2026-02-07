package es.superstrellaa.storagemanager.api;

import es.superstrellaa.storagemanager.api.data.RowData;
import es.superstrellaa.storagemanager.api.schema.TableSchema;
import es.superstrellaa.storagemanager.internal.TableExecutor;
import es.superstrellaa.storagemanager.internal.cache.WriteCache;

import java.util.List;
import java.util.Map;

/**
 * API principal para interactuar con StorageManager.
 *
 * Por defecto, todas las operaciones de escritura usan un sistema de caché
 * que agrupa operaciones y las ejecuta en lotes cada 5 segundos.
 *
 * Para datos críticos que necesitas guardar inmediatamente, usa los métodos *Immediate
 * o llama a flush() manualmente.
 */
public final class StorageManager {

    /**
     * Registra una tabla en la base de datos
     */
    public static void registerTable(TableSchema schema) {
        TableExecutor.createTable(schema);
    }

    /**
     * Inserta datos (modo async con caché, recomendado para la mayoría de casos)
     */
    public static void insert(String table, RowData data) {
        TableExecutor.insert(table, data, false);
    }

    /**
     * Inserta datos inmediatamente sin usar caché (para datos críticos)
     */
    public static void insertImmediate(String table, RowData data) {
        TableExecutor.insert(table, data, true);
    }

    /**
     * Selecciona datos de una tabla.
     * Automáticamente hace flush de la caché antes de leer.
     */
    public static List<RowData> select(String table, Map<String, Object> where) {
        return TableExecutor.select(table, where);
    }

    /**
     * Elimina datos (modo async con caché)
     */
    public static void delete(String table, Map<String, Object> where) {
        TableExecutor.delete(table, where, false);
    }

    /**
     * Elimina datos inmediatamente sin usar caché
     */
    public static void deleteImmediate(String table, Map<String, Object> where) {
        TableExecutor.delete(table, where, true);
    }

    /**
     * Fuerza el guardado de todas las operaciones pendientes de una tabla específica
     */
    public static void flush(String table) {
        WriteCache.getInstance().flushTable(table);
    }

    /**
     * Fuerza el guardado de todas las operaciones pendientes de todas las tablas
     */
    public static void flushAll() {
        WriteCache.getInstance().flushAll();
    }

    private StorageManager() {}
}