package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DbHandlerTest {

    @Test
    void getInstance(){
        DbHandler db = DbHandler.getInstance();
        Assertions.assertNotNull(db);
    }

    @Test
    void createDB() {
        DbHandler db = DbHandler.getInstance();
        Assertions.assertDoesNotThrow(() -> db.createDB());
    }
}