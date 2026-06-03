package org.hyf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Entry point. Wires the pieces together:
 * get a connection from Database, hand it to LibraryRepository,
 * then print what fetchData() returns.
 */
public class Main {

    public static void main(String[] args) {
        // The connection is opened once here and shared with the repository.
        try (Connection conn = Database.getConnection()) {
            System.out.println("Connected! " + conn.getCatalog());

            LibraryRepository repo = new LibraryRepository(conn);

            repo.insertData();
            System.out.println("\nData inserted.\n");

            List<String> borrowings = repo.fetchData();
            System.out.println("Current borrowings (member -> book):");
            for (String row : borrowings) {
                System.out.println("  " + row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
