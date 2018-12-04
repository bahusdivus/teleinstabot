package ru.bahusdivus.teleinstaBot;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

class DbHandler {

    private static DbHandler instance = null;
    private Connection connection;

    static synchronized DbHandler getInstance() throws SQLException {
        if (instance == null)
            instance = new DbHandler();
        return instance;
    }

    private DbHandler() throws SQLException {
        try (InputStream reader = this.getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (reader != null) {
                Properties properties = new Properties();
                properties.load(reader);
                //String jdbcUrl = String.format("jdbc:mysql://35.226.35.187/%s?cloudSqlInstance=%s&useSSL=false", "telegrambot1db", "telegrambot1");
                String jdbcUrl = properties.getProperty("db.link") + properties.getProperty("db.db");
                this.connection = DriverManager.getConnection(jdbcUrl, properties.getProperty("db.user"), properties.getProperty("db.password"));
                System.out.println("Connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
