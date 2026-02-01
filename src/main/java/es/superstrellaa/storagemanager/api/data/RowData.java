package es.superstrellaa.storagemanager.api.data;

import java.util.HashMap;
import java.util.Map;

public final class RowData {

    private final Map<String, Object> values = new HashMap<>();

    public RowData set(String column, Object value) {
        values.put(column, value);
        return this;
    }

    public Map<String, Object> values() {
        return values;
    }
}
