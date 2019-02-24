package ru.bahusdivus.teleinstaBot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
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
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("useLocalSessionState", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");
                config.addDataSourceProperty("cacheResultSetMetadata", "true");
                config.addDataSourceProperty("cacheServerConfiguration", "true");
                config.addDataSourceProperty("elideSetAutoCommits", "true");
                config.addDataSourceProperty("maintainTimeStats", "false");
                ds = new HikariDataSource(config);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void createDB() {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE if not exists users (id int AUTO_INCREMENT, instId text, chatId bigint, taskTaken timestamp, taskComplite timestamp, PRIMARY KEY (id));");
            statement.execute("CREATE TABLE if not exists task (id int AUTO_INCREMENT, ownerId int, postId text, isLikeRequired boolean, commentRequiredLength int, comment text, created timestamp, PRIMARY KEY (id));");
            statement.execute("CREATE TABLE if not exists tasklist (id int AUTO_INCREMENT, userId int, taskId int, PRIMARY KEY (id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    User getUserByChatId(Long chatId) {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
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

    int saveUser(User user) {
        int id = user.getId();
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            if (id != 0) {
                statement.execute("UPDATE users SET instId = '" + user.getInstId() + "', taskTaken = " + user.getTaskTaken() + ", taskComplite = " + user.getTaskComplite() + "  WHERE id = " + id);
                return id;
            } else {
                statement.executeUpdate("INSERT INTO users (instId, chatId, taskTaken, taskComplite) VALUES ('" + user.getInstId() + "', " + user.getChatId() + ", TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 day)), TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 day)))",
                        Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                } else throw new SQLException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    ArrayList<UserTask> getTaskListLast24Hours(int id) {
        String query = "SELECT task.* FROM task LEFT JOIN tasklist ON tasklist.taskId = task.id " +
                            "WHERE task.created > TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 day)) " +
                            "AND (tasklist.userId IS NULL OR tasklist.userId <> " + id + ")" +
                            "AND task.ownerId <> " + id;
        return getTaskList(query);
    }

    ArrayList<UserTask> getTaskListLast7(int id) {
        String query = "SELECT task.* FROM task LEFT JOIN tasklist ON tasklist.taskId = task.id " +
                            "WHERE (tasklist.userId IS NULL OR tasklist.userId <> " + id + ")" +
                            "AND task.ownerId <> " + id +
                            " ORDER BY task.created DESC LIMIT 7";
        return getTaskList(query);
    }

    private ArrayList<UserTask> getTaskList(String query) {
        ArrayList<UserTask> list = new ArrayList<>();
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
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
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO tasklist (userid, taskid) VALUES (" + userId + ", " + taskId + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    int saveTask(UserTask task) {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO task (ownerId, postId, isLikeRequired, commentRequiredLength, comment, created) VALUES (" +
                    task.getOwnerId() + ", " +
                    "'" + task.getPostId() + "', " +
                    task.isLikeRequired() + ", " +
                    task.getCommentRequiredLength() + "," +
                    "'" + task.getComment() + "', '" +
                    task.getCreated() + "')", Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else throw new SQLException();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    void deleteUser(int id) {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM users WHERE id = " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void deleteTask(int id) {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM task WHERE id = " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
