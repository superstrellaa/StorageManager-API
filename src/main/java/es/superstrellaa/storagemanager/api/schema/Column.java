package es.superstrellaa.storagemanager.api.schema;

public record Column(
        String name,
        ColumnType type,
        boolean notNull
) {}
