package com.heidelberg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mariadbcdc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.sql.Connection;
import java.sql.ConnectionBuilder;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    @Value("${mariadb.host:localhost}")
    private String host;
    @Value("${mariadb.user}")
    private String user;
    @Value("${mariadb.password}")
    private String password;
    @Value("${mariadb.port:3306}")
    private int port;

    private static Map<String, String> map(DataRow dataRow) {
        Map<String, String> data = new HashMap<>();
        List<String> columnNames = dataRow.getColumnNames();
        for (int i = 0; i < dataRow.getColumnCount(); i++) {
            data.put(columnNames.get(i), dataRow.getString(i));
        }
        return data;
    }

    private static Event map(RowChangedData data) {
        String table = data.getTable();
        ChangeType type = data.getType();
        DataRow dataRow = data.getDataRow();
        String database = data.getDatabase();
        Map<String, String> dataAfter = map(dataRow);
        DataRow dataRowBeforeUpdate = data.getDataRowBeforeUpdate();
        Map<String, String> dataBefore = null;
        if (dataRowBeforeUpdate != null) {
            dataBefore = map(dataRowBeforeUpdate);
        }

        Event event = new Event();
        event.setSchema(database);
        event.setTable(table);
        event.setType(type);
        event.setDataAfter(dataAfter);
        event.setDataBefore(dataBefore);
        return event;
    }

    private void waitForDatabase() {
        for (int i = 0; i < 10; i++) {
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(
                        String.format("jdbc:mariadb://%s:%d/?user=%s&password=%s", host, port, user, password));
                return;
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e1) {
                    // Ignore
                }
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    private void listen() {
        final ObjectMapper mapper = new ObjectMapper();
        MariadbCdcConfig config = new MariadbCdcConfig(
                host, // host
                port, // port
                user, // user for cdc
                password, // password
                "bin.pos"); // bin position trace file
        JdbcColumnNamesGetter columnNamesGetter = new JdbcColumnNamesGetter(
                host, // host
                port, // port
                user, // cdc user
                password); // bin position trace file

        MariadbCdc cdc = new MariadbCdc(config, columnNamesGetter);
        cdc.setMariadbCdcListener(new MariadbCdcListener.BaseListener() {
            @Override
            public void onDataChanged(List<RowChangedData> list) {
                for (var item : list) {
                    Event event = map(item);
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

    @Bean
    public CommandLineRunner runner() {
        return new CommandLineRunner() {

            @Override
            public void run(String... args) throws Exception {
                waitForDatabase();
                listen();
            }

        };
    }

    public static void main(String[] args) {
        SpringApplication.run(Test.class, args);
    }
}
