package org.verwaltung.verwaltung_kunden_ma.database_connection;

import org.verwaltung.verwaltung_kunden_ma.PersonDatas.CustomerData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private final SQLConnector connector;

    public CustomerDAO(SQLConnector connector) {
        this.connector = connector;
    }

    /* -------------------- READ -------------------- */

    public CustomerData findById(int id) throws SQLException {
        String sql = """
            SELECT k.kundennummer, k.vorname, k.nachname, k.strasse, k.plz, k.ort,
                   k.telefon, k.email, k.branche_id, b.name AS branche_name
            FROM kunden k
            JOIN branchen b ON b.branche_id = k.branche_id
            WHERE k.kundennummer = ?
            """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<CustomerData> findAll() throws SQLException {
        String sql = """
            SELECT k.kundennummer, k.vorname, k.nachname, k.strasse, k.plz, k.ort,
                   k.telefon, k.email, k.branche_id, b.name AS branche_name
            FROM kunden k
            JOIN branchen b ON b.branche_id = k.branche_id
            ORDER BY k.kundennummer
            """;
        List<CustomerData> list = new ArrayList<>();
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public CustomerData findNextById(int currentId) throws SQLException {
        String sql = """
            SELECT k.kundennummer, k.vorname, k.nachname, k.strasse, k.plz, k.ort,
                   k.telefon, k.email, k.branche_id, b.name AS branche_name
            FROM kunden k
            JOIN branchen b ON b.branche_id = k.branche_id
            WHERE k.kundennummer > ?
            ORDER BY k.kundennummer ASC
            LIMIT 1
            """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public CustomerData findPreviousById(int currentId) throws SQLException {
        String sql = """
            SELECT k.kundennummer, k.vorname, k.nachname, k.strasse, k.plz, k.ort,
                   k.telefon, k.email, k.branche_id, b.name AS branche_name
            FROM kunden k
            JOIN branchen b ON b.branche_id = k.branche_id
            WHERE k.kundennummer < ?
            ORDER BY k.kundennummer DESC
            LIMIT 1
            """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public CustomerData findNextByIdCircular(int currentId) throws SQLException {
        CustomerData next = findNextById(currentId);
        return (next != null) ? next : findFirst();
    }

    public CustomerData findPreviousByIdCircular(int currentId) throws SQLException {
        CustomerData prev = findPreviousById(currentId);
        return (prev != null) ? prev : findLast();
    }

    public CustomerData findFirst() throws SQLException {
        String sql = """
            SELECT k.kundennummer, k.vorname, k.nachname, k.strasse, k.plz, k.ort,
                   k.telefon, k.email, k.branche_id, b.name AS branche_name
            FROM kunden k
            JOIN branchen b ON b.branche_id = k.branche_id
            ORDER BY k.kundennummer ASC
            LIMIT 1
            """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        }
    }

    public CustomerData findLast() throws SQLException {
        String sql = """
            SELECT k.kundennummer, k.vorname, k.nachname, k.strasse, k.plz, k.ort,
                   k.telefon, k.email, k.branche_id, b.name AS branche_name
            FROM kunden k
            JOIN branchen b ON b.branche_id = k.branche_id
            ORDER BY k.kundennummer DESC
            LIMIT 1
            """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        }
    }

    /* -------------------- WRITE -------------------- */

    /** Insert, wenn du im Modell nur den Branchen-Namen hast. */
    public void insert(CustomerData c) throws SQLException {
        try (Connection conn = connector.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int brancheId = getOrCreateBrancheId(conn, c.getIndustry());
                String sql = """
                    INSERT INTO kunden
                    (kundennummer, vorname, nachname, strasse, plz, ort, telefon, email, branche_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, c.getId());
                    stmt.setString(2, c.getFirstName());
                    stmt.setString(3, c.getLastName());
                    stmt.setString(4, c.getStreet());
                    stmt.setString(5, c.getPlz());
                    stmt.setString(6, c.getPlace());
                    stmt.setString(7, c.getPhone());
                    stmt.setString(8, c.getEmail());
                    stmt.setInt(9, brancheId);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM kunden WHERE kundennummer = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /* -------------------- Helpers -------------------- */

    /** Holt branche_id zu Name; legt sie an, wenn nicht vorhanden. */
    private int getOrCreateBrancheId(Connection conn, String brancheName) throws SQLException {
        // 1) versuchen zu finden
        try (PreparedStatement s = conn.prepareStatement(
                "SELECT branche_id FROM branchen WHERE name = ?")) {
            s.setString(1, brancheName);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        // 2) sonst anlegen
        try (PreparedStatement s = conn.prepareStatement(
                "INSERT INTO branchen(name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, brancheName);
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException dup) {
            // Falls parallel angelegt: erneut selektieren
            try (PreparedStatement s2 = conn.prepareStatement(
                    "SELECT branche_id FROM branchen WHERE name = ?")) {
                s2.setString(1, brancheName);
                try (ResultSet rs2 = s2.executeQuery()) {
                    if (rs2.next()) return rs2.getInt(1);
                }
            }
            throw dup;
        }
        throw new SQLException("Konnte branche_id nicht ermitteln für: " + brancheName);
    }

    /** Mapper: baut CustomerData inkl. Branchen-Name aus Join-Spalte 'branche_name'. */
    private CustomerData mapRow(ResultSet rs) throws SQLException {
        CustomerData c = new CustomerData(
                rs.getString("vorname"),
                rs.getString("nachname"),
                rs.getString("strasse"),
                rs.getString("plz"),
                rs.getString("ort"),
                rs.getString("telefon"),
                rs.getString("email"),
                rs.getString("branche_name") // Klartextname aus JOIN
        );
        c.setId(rs.getInt("kundennummer"));
        return c;
    }
}
