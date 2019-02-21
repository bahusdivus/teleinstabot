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
            statement.execute("CREATE TABLE if not exists task (id int AUTO_INCREMENT, ownerId int, postId text, isLikeRequired boolean, commentRequiredLength int, comment text, created timestamp, PRIMARY KEY (id));");
            statement.execute("CREATE TABLE if not exists taskList (id int AUTO_INCREMENT, userId int, taskId int, PRIMARY KEY (id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    ArrayList<UserTask> getTaskList(int id) {
        ArrayList<UserTask> list = new ArrayList<>();
        try (Statement statement = this.connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT task.* FROM task LEFT JOIN tasklist ON tasklist.taskId = task.id " +
                            "WHERE task.created > DATE_SUB(NOW(), INTERVAL 1 day) " +
                            "AND (tasklist.userId IS NULL OR tasklist.userId <> " + id + ")" +
                            "AND task.ownerId <> " + id);
            while (resultSet.next()) {
                list.add(new UserTask(resultSet.getInt("id"),
                        resultSet.getInt("ownerId"),
                        resultSet.getString("postId"),
                        resultSet.getBoolean("isLikeRequired"),
                        resultSet.getInt("commentRequiredLength"),
                        resultSet.getString("comment"),
                        resultSet.getTimestamp("created")
                ));
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    void compliteTask(int userId, int taskId) {
        try (Statement statement = this.connection.createStatement()) {
            statement.execute("INSERT INTO tasklist (userid, taskid) VALUES (" + userId + ", " + taskId + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
