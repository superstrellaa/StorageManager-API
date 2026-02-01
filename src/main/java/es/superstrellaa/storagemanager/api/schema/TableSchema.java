package es.superstrellaa.storagemanager.api.schema;

import java.util.ArrayList;
import java.util.List;

public final class TableSchema {

    private final String name;
    private final List<Column> columns;
    private final List<String> primaryKeys;

    private TableSchema(String name, List<Column> columns, List<String> primaryKeys) {
        this.name = name;
        this.columns = columns;
        this.primaryKeys = primaryKeys;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {

        private final String name;
        private final List<Column> columns = new ArrayList<>();
        private final List<String> primaryKeys = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder column(String name, ColumnType type) {
            columns.add(new Column(name, type, false));
            return this;
        }

        public Builder column(String name, ColumnType type, boolean notNull) {
            columns.add(new Column(name, type, notNull));
            return this;
        }

        public Builder primaryKey(String... keys) {
            primaryKeys.addAll(List.of(keys));
            return this;
        }

        public TableSchema build() {
            return new TableSchema(name, columns, primaryKeys);
        }
    }
}
