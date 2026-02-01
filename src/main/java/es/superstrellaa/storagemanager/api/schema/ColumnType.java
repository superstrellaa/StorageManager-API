package es.superstrellaa.storagemanager.api.schema;

public enum ColumnType {
    INTEGER("INTEGER"),
    REAL("REAL"),
    TEXT("TEXT"),
    BLOB("BLOB");

    private final String sql;

    ColumnType(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
