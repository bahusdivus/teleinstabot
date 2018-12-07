package ru.bahusdivus.teleinstaBot;

import java.io.*;
import java.sql.*;
import java.util.*;

class DbHandler {

    private static DbHandler instance = null;
    private Connection connection;

    static synchronized DbHandler getInstance() {
        if (instance == null)
            instance = new DbHandler();
        return instance;
    }

    private DbHandler() {
        try (InputStream reader = this.getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (reader != null) {
                Properties properties = new Properties();
                properties.load(reader);
                String jdbcUrl = properties.getProperty("db.link") + properties.getProperty("db.db") + "?useLegacyDatetimeCode=false&serverTimezone=UTC";
                try {
                    this.connection = DriverManager.getConnection(jdbcUrl, properties.getProperty("db.user"), properties.getProperty("db.password"));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void createDB() {
        try (Statement statement = this.connection.createStatement()) {
            statement.execute("CREATE TABLE if not exists users (id int AUTO_INCREMENT, instId text, chatId bigint, taskTaken timestamp, taskComplite timestamp, PRIMARY KEY (id));");
            statement.execute("CREATE TABLE if not exists task (id int AUTO_INCREMENT, ownerId int, postId text, isLikeRequired boolean, commentRequeredLenght int, comment text, created timestamp, PRIMARY KEY (id));");
            statement.execute("CREATE TABLE if not exists taskList (id int AUTO_INCREMENT, userId int, taskId int, PRIMARY KEY (id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Back up function
    /*
    public Boolean makeBackup (int n) {
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("backup to backup" + n + ".s3db");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    */

    User getUserByChatId(Long chatId) {
        try (Statement statement = this.connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE chatId = '" + chatId + "';");
            if (resultSet.next()) {
                return new User(resultSet.getInt("id"),
                        resultSet.getString("instId"),
                        resultSet.getLong("chatId"),
                        resultSet.getTimestamp("taskTaken"),
                        resultSet.getTimestamp("taskComplite")
                );
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    void saveUser(User user) {
        int id = user.getId();
        try (Statement statement = this.connection.createStatement()) {
            if (id != 0) {
                statement.execute("UPDATE users SET instId = '" + user.getInstId() + "', taskTaken = " + user.getTaskTaken() + ", taskComplite = " + user.getTaskComplite() + "  WHERE id = " + id);
            } else {
                statement.execute("INSERT INTO users (instId, chatId) VALUES ('" + user.getInstId() + "', " + user.getChatId() + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}