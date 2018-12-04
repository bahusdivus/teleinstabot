package ru.bahusdivus.teleinstaBot;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DbHandlerTest {

    @Test
    void getInstance() throws SQLException {
        DbHandler db = DbHandler.getInstance();
    }
}