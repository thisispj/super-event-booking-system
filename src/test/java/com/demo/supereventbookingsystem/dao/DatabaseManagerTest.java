package com.demo.supereventbookingsystem.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        databaseManager = DatabaseManager.getInstance();
    }

    @Test
    void testEncryptPassword_ValidPassword() {
        String password = "password123";
        String encrypted = databaseManager.encryptPassword(password);
        assertNotNull(encrypted);
        assertTrue(encrypted.length() == 64);
        assertNotEquals(password, encrypted);
    }

    @Test
    void testEncryptPassword_EmptyPassword() {
        String password = "";
        String encrypted = databaseManager.encryptPassword(password);
        assertNotNull(encrypted);
        assertEquals(64, encrypted.length()); // SHA-256 of empty string is still 64 chars
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", encrypted);
    }
}