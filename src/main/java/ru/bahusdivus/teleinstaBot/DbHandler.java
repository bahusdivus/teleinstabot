package ru.bahusdivus.teleinstaBot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

class DbHandler {

    private static DbHandler instance = null;
    private HikariDataSource ds;
    private SessionFactory sessions;

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

                Configuration cfg = new Configuration()
                        .addAnnotatedClass(ru.bahusdivus.teleinstaBot.User.class)
                        .addAnnotatedClass(ru.bahusdivus.teleinstaBot.UserTask.class)
                        .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect")
                        .setProperty("hibernate.show_sql", "true")
                        .setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
                        .setProperty("hibernate.hikari.minimumIdle", "5")
                        .setProperty("hibernate.hikari.maximumPoolSize", "10")
                        .setProperty("hibernate.hikari.idleTimeout", "30000")
                        .setProperty("hibernate.hikari.dataSourceClassName", "com.mysql.cj.jdbc.MysqlDataSource")
                        .setProperty("hibernate.hikari.dataSource.url", jdbcUrl)
                        .setProperty("hibernate.hikari.dataSource.user", properties.getProperty("db.user"))
                        .setProperty("hibernate.hikari.dataSource.password", properties.getProperty("db.password"));


                sessions = cfg.buildSessionFactory();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void createDB() {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE if not exists users (userId int AUTO_INCREMENT, instId text, chatId bigint, taskTaken timestamp, taskComplite timestamp, PRIMARY KEY (userId));");
            statement.execute("CREATE TABLE if not exists task (taskId int AUTO_INCREMENT, ownerId int, postId text, isLikeRequired boolean, commentRequiredLength int, comment text, created timestamp, PRIMARY KEY (taskId));");
            statement.execute("CREATE TABLE if not exists tasklist (userId int, taskId int);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    User getUserByChatId(Long chatId) {
        try(Session session = sessions.openSession()) {
            return session.createNativeQuery("SELECT * FROM users WHERE chatId = '" + chatId + "';", User.class).uniqueResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    int saveUser(User user) {
        Session session = sessions.openSession();
        if (user.getId() != 0) {
            session.beginTransaction();
            session.createNativeQuery("UPDATE users SET instId = '" + user.getInstId() + "', taskTaken = " + user.getTaskTaken() + ", taskComplite = " + user.getTaskComplite() + "  WHERE userId = " + user.getId()).executeUpdate();
            session.getTransaction().commit();
            return user.getId();
        } else {
            session.beginTransaction();
            session.createNativeQuery("INSERT INTO users (instId, chatId, taskTaken, taskComplite) VALUES ('" + user.getInstId() + "', " + user.getChatId() + ", TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 day)), TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 day)))").executeUpdate();
            int lastInsertId = ((BigInteger) session.createSQLQuery("SELECT LAST_INSERT_ID()").uniqueResult()).intValue();
            session.getTransaction().commit();
            return lastInsertId;
        }
    }

    ArrayList<UserTask> getTaskListLast24Hours(int id) {
        String query = "SELECT task.* FROM task LEFT JOIN tasklist ON tasklist.taskId = task.taskId " +
                            "WHERE task.created > TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 day)) " +
                            "AND (tasklist.userId IS NULL OR tasklist.userId <> " + id + ")" +
                            "AND task.ownerId <> " + id;
        return getTaskList(query);
    }

    ArrayList<UserTask> getTaskListLast7(int id) {
        String query = "SELECT task.* FROM (SELECT * FROM task WHERE ownerId <> " + id +
                " ORDER BY created DESC LIMIT 7) AS task " +
                "LEFT JOIN tasklist ON tasklist.taskId = task.taskId " +
                "WHERE (tasklist.userId IS NULL OR tasklist.userId <> " + id + ")";
        return getTaskList(query);
    }

    private ArrayList<UserTask> getTaskList(String query) {
        try(Session session = sessions.openSession()) {
            return  (ArrayList<UserTask>) session.createNativeQuery(query, UserTask.class).list();
        } catch (NoResultException e) {
            return null;
        }
    }

    void compliteTask(int userId, int taskId) {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO tasklist (userId, taskId) VALUES (" + userId + ", " + taskId + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    int saveTask(UserTask task) {
        Session session = sessions.openSession();
        session.beginTransaction();
        session.saveOrUpdate(task);
        session.flush();
        session.getTransaction().commit();
        return task.getId();
    }

    void deleteUser(int id) {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM users WHERE userId = " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void deleteTask(int id) {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM task WHERE taskId = " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
