package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DbHandlerTest {

    @Test
    void getInstance(){
        DbHandler db = DbHandler.getInstance();
    }

    @Test
    void createDB() {
        DbHandler db = DbHandler.getInstance();
        db.createDB();
    }
}