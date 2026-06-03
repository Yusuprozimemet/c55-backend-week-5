package org.hyf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/library_db";
    private static final String USER = "hyfuser";
    private static final String PASSWORD = "hyfpassword";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
