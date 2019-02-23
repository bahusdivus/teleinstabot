package ru.bahusdivus.teleinstaBot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

class DbHandler {

    private static DbHandler instance = null;
    private HikariDataSource ds;

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
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(properties.getProperty("db.user"));
                config.setPassword(properties.getProperty("db.password"));
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                ds = new HikariDataSource(config);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void createDB() {
        try (Statement statement = ds.getConnection().createStatement()) {
            statement.execute("CREATE TABLE if not exists users (id int AUTO_INCREMENT, instId text, chatId bigint, taskTaken timestamp, taskComplite timestamp, PRIMARY KEY (id));");
            statement.execute("CREATE TABLE if not exists task (id int AUTO_INCREMENT, ownerId int, postId text, isLikeRequired boolean, commentRequiredLength int, comment text, created timestamp, PRIMARY KEY (id));");
            statement.execute("CREATE TABLE if not exists tasklist (id int AUTO_INCREMENT, userId int, taskId int, PRIMARY KEY (id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    User getUserByChatId(Long chatId) {
        try (Statement statement = ds.getConnection().createStatement()) {
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
        try (Statement statement = ds.getConnection().createStatement()) {
            if (id != 0) {
                statement.execute("UPDATE users SET instId = '" + user.getInstId() + "', taskTaken = " + user.getTaskTaken() + ", taskComplite = " + user.getTaskComplite() + "  WHERE id = " + id);
            } else {
                statement.execute("INSERT INTO users (instId, chatId, taskTaken, taskComplite) VALUES ('" + user.getInstId() + "', " + user.getChatId() + ", DATE_SUB(NOW(), INTERVAL 1 day), DATE_SUB(NOW(), INTERVAL 1 day))");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ArrayList<UserTask> getTaskList(int id) {
        ArrayList<UserTask> list = new ArrayList<>();
        try (Statement statement = ds.getConnection().createStatement()) {
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
            if (list.size() > 0) {
                return list;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    void compliteTask(int userId, int taskId) {
        try (Statement statement = ds.getConnection().createStatement()) {
            statement.execute("INSERT INTO tasklist (userid, taskid) VALUES (" + userId + ", " + taskId + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void saveTask(UserTask task) {
        try (Statement statement = ds.getConnection().createStatement()) {
            statement.execute("INSERT INTO task (ownerId, postId, isLikeRequired, commentRequiredLength, comment, created) VALUES (" +
                    task.getOwnerId() + ", " +
                    "'" + task.getPostId() + "', " +
                    task.isLikeRequired() + ", " +
                    task.getCommentRequiredLength() + "," +
                    "'" + task.getComment() + "', " +
                    task.getCreated() + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
