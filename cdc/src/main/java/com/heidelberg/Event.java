package com.heidelberg;

import mariadbcdc.ChangeType;

import java.util.Map;

public class Event {
    private String table;
    private String schema;
    private ChangeType type;
    private Map<String, String> dataBefore;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public Map<String, String> getDataBefore() {
        return dataBefore;
    }

    public void setDataBefore(Map<String, String> dataBefore) {
        this.dataBefore = dataBefore;
    }

    public Map<String, String> getDataAfter() {
        return dataAfter;
    }

    public void setDataAfter(Map<String, String> dataAfter) {
        this.dataAfter = dataAfter;
    }

    private Map<String, String> dataAfter;
}
