package com.heidelberg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mariadbcdc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        final String user = "root";
        final String password = "mariadb1";
        final String host = "localhost";
        final ObjectMapper mapper = new ObjectMapper();
        MariadbCdcConfig config = new MariadbCdcConfig(
                host, // host
                3306, // port
                user, // user for cdc
                password, // password
                "bin.pos"); // bin position trace file
        JdbcColumnNamesGetter columnNamesGetter = new JdbcColumnNamesGetter(
                host, // host
                3306, // port
                user, // cdc user
                password); // bin position trace file

        MariadbCdc cdc = new MariadbCdc(config, columnNamesGetter);
        cdc.setMariadbCdcListener(new MariadbCdcListener.BaseListener() {
            @Override
            public void onDataChanged(List<RowChangedData> list) {
                for (var item : list) {
                    String table = item.getTable();
                    ChangeType type = item.getType();
                    DataRow dataRow = item.getDataRow();
                    String database = item.getDatabase();
                    logger.info("Table {}, type {}, database {}", table, type, database);
                    List<String> columnNames = dataRow.getColumnNames();
                    Map<String, String> dataAfter = new HashMap<>();
                    for (int i = 0; i < dataRow.getColumnCount(); i++) {
                        logger.info("\tdata {}: {}", columnNames.get(i), dataRow.getString(i));
                        dataAfter.put(columnNames.get(i), dataRow.getString(i));
                    }
                    DataRow dataRowBeforeUpdate = item.getDataRowBeforeUpdate();
                    Map<String, String> dataBefore = null;
                    if (dataRowBeforeUpdate != null) {
                        dataBefore = new HashMap<>();
                        columnNames = dataRowBeforeUpdate.getColumnNames();
                        for (int i = 0; i < dataRowBeforeUpdate.getColumnCount(); i++) {
                            logger.info("\tdata before {}: {}", columnNames.get(i), dataRowBeforeUpdate.getString(i));
                            dataBefore.put(columnNames.get(i), dataRowBeforeUpdate.getString(i));
                        }
                    }

                    Event event =  new Event();
                    event.setSchema(database);
                    event.setTable(table);
                    event.setType(type);
                    event.setDataAfter(dataAfter);
                    event.setDataBefore(dataBefore);
                    try {
                        String json = mapper.writeValueAsString(event);
                        logger.info(json);
                    } catch (JsonProcessingException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        });
        cdc.start();
    }
}
