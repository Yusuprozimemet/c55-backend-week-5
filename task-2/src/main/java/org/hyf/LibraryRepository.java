package org.hyf;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LibraryRepository {

    private final Connection conn;

    public LibraryRepository(Connection conn) {
        this.conn = conn;
    }

    public void insertData() throws SQLException {
        // --- publishers (ids 1, 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO publishers (publisher_name) VALUES (?)")) {
            ps.setString(1, "O'Reilly");
            ps.executeUpdate();
            ps.setString(1, "Addison-Wesley");
            ps.executeUpdate();
        }

        // --- members (ids 1, 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO members (member_name, member_email, member_phone, member_address) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "Tom");
            ps.setString(2, "tom@example.com");
            ps.setString(3, "+31645678901");
            ps.setString(4, "Biltstraat 88, Utrecht");
            ps.executeUpdate();
            ps.setString(1, "Sam");
            ps.setString(2, "sam@example.com");
            ps.setString(3, "+31612345678");
            ps.setString(4, "Oudegracht 10, Utrecht");
            ps.executeUpdate();
        }

        // --- authors (ids 1, 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO authors (author_name) VALUES (?)")) {
            ps.setString(1, "Douglas Crockford");
            ps.executeUpdate();
            ps.setString(1, "Joshua Bloch");
            ps.executeUpdate();
        }

        // --- tags (ids 1, 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tags (tag_name) VALUES (?)")) {
            ps.setString(1, "javascript");
            ps.executeUpdate();
            ps.setString(1, "java");
            ps.executeUpdate();
        }

        // --- books (ids 1, 2; publisher_id -> publishers 1, 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books (book_isbn, book_title, publisher_id) VALUES (?, ?, ?)")) {
            ps.setString(1, "9780596517748");
            ps.setString(2, "JavaScript: The Good Parts");
            ps.setInt(3, 1);
            ps.executeUpdate();
            ps.setString(1, "9780134685991");
            ps.setString(2, "Effective Java");
            ps.setInt(3, 2);
            ps.executeUpdate();
        }

        // --- cards (member_id -> members 1, 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO cards (card_number, card_issued_on, card_expires_on, member_id) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, "C-1004");
            ps.setDate(2, Date.valueOf("2023-11-20"));
            ps.setDate(3, Date.valueOf("2026-11-20"));
            ps.setInt(4, 1);
            ps.executeUpdate();
            ps.setString(1, "C-1001");
            ps.setDate(2, Date.valueOf("2024-01-10"));
            ps.setDate(3, Date.valueOf("2027-01-10"));
            ps.setInt(4, 2);
            ps.executeUpdate();
        }

        // --- copies (ids 1, 2; book_id -> books 1, 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO copies (copy_barcode, shelf_code, book_id) VALUES (?, ?, ?)")) {
            ps.setString(1, "BC-0011");
            ps.setString(2, "B-03");
            ps.setInt(3, 1);
            ps.executeUpdate();
            ps.setString(1, "BC-0001");
            ps.setString(2, "A-01");
            ps.setInt(3, 2);
            ps.executeUpdate();
        }

        // --- loans (member_id + copy_id) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO loans (member_id, copy_id, borrowed_at, due_date, returned_at, fine_eur) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, 1);
            ps.setInt(2, 1);
            ps.setTimestamp(3, Timestamp.valueOf("2025-12-02 08:08:00"));
            ps.setTimestamp(4, Timestamp.valueOf("2025-12-23 08:08:00"));
            ps.setTimestamp(5, Timestamp.valueOf("2025-12-13 08:08:00"));
            ps.setBigDecimal(6, new BigDecimal("0.00"));
            ps.executeUpdate();
            ps.setInt(1, 2);
            ps.setInt(2, 2);
            ps.setTimestamp(3, Timestamp.valueOf("2026-03-09 03:18:00"));
            ps.setTimestamp(4, Timestamp.valueOf("2026-03-30 03:18:00"));
            ps.setTimestamp(5, Timestamp.valueOf("2026-03-28 03:18:00"));
            ps.setBigDecimal(6, new BigDecimal("1.25"));
            ps.executeUpdate();
        }

        // --- book_authors bridge (book 1 + author 1, book 2 + author 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)")) {
            ps.setInt(1, 1);
            ps.setInt(2, 1);
            ps.executeUpdate();
            ps.setInt(1, 2);
            ps.setInt(2, 2);
            ps.executeUpdate();
        }

        // --- book_tags bridge (book 1 + tag 1, book 2 + tag 2) ---
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO book_tags (book_id, tag_id) VALUES (?, ?)")) {
            ps.setInt(1, 1);
            ps.setInt(2, 1);
            ps.executeUpdate();
            ps.setInt(1, 2);
            ps.setInt(2, 2);
            ps.executeUpdate();
        }
    }

    /**
     * Fetches all borrowings and returns them as a list.
     * Uses joins across loans -> members -> copies -> books.
     */
    public List<String> fetchData() throws SQLException {
        String sql =
                "SELECT m.member_name, b.book_title, c.copy_barcode, l.borrowed_at, l.returned_at " +
                "FROM loans l " +
                "JOIN members m ON m.member_id = l.member_id " +
                "JOIN copies  c ON c.copy_id   = l.copy_id " +
                "JOIN books   b ON b.book_id   = c.book_id " +
                "ORDER BY l.borrowed_at";

        List<String> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString("member_name")
                        + " -> " + rs.getString("book_title")
                        + " (copy " + rs.getString("copy_barcode") + ")"
                        + ", borrowed " + rs.getTimestamp("borrowed_at")
                        + ", returned " + rs.getTimestamp("returned_at"));
            }
        }
        return result;
    }
}
