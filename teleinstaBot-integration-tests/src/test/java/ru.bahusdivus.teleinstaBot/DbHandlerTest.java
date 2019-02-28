package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

class DbHandlerTest {

    @Test
    void getInstance_returnNotNull(){
        DbHandler db = DbHandler.getInstance();
        Assertions.assertNotNull(db);
    }

    @Test
    void saveUser_newUser_returnsAIid() {
        DbHandler dbHandler = DbHandler.getInstance();
        int id = dbHandler.saveUser(new User("@testUser", 1L));
        Assertions.assertNotEquals(-1, id);
        dbHandler.deleteUser(id);
    }

    @Test
    void saveUser_newUser_savesUserCorrectly() {
        DbHandler dbHandler = DbHandler.getInstance();
        User user = new User("@testUser", 10L);
        int id = dbHandler.saveUser(user);
        User assertUser = dbHandler.getUserByChatId(10L);
        Assertions.assertEquals(user, assertUser);
        dbHandler.deleteUser(id);
    }

    @Test
    void deleteUser_deleteUserCorrectly() {
        DbHandler dbHandler = DbHandler.getInstance();
        int id = dbHandler.saveUser(new User("@testUser", 10L));
        Assertions.assertNotEquals(-1, id);
        dbHandler.deleteUser(id);
        Assertions.assertNull(dbHandler.getUserByChatId(10L));
    }

    @Test
    void saveTask_newTask_returnsAIid() {
        DbHandler dbHandler = DbHandler.getInstance();
        int id = dbHandler.saveTask(new UserTask(1, "a", true, 1, "", new Timestamp(System.currentTimeMillis())));
        Assertions.assertNotEquals(-1, id);
        dbHandler.deleteTask(id);
    }
}