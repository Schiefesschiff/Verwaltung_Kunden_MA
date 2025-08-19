package org.verwaltung.verwaltung_kunden_ma.database_connection;

import org.verwaltung.verwaltung_kunden_ma.PersonDatas.ExternalEmployeesData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExternalEmployeesDAO {
    private final SQLConnector connector;

    public ExternalEmployeesDAO(SQLConnector connector) {
        this.connector = connector;
    }

    /* -------------------- READ -------------------- */

    public ExternalEmployeesData findById(int id) throws SQLException {
        String sql = """
            SELECT em.mitarbeiternummer, em.vorname, em.nachname, em.strasse, em.plz, em.ort,
                   em.telefon, em.email, em.firma_id, f.name AS firma_name
            FROM externe_mitarbeiter em
            JOIN firmen f ON f.firma_id = em.firma_id
            WHERE em.mitarbeiternummer = ?
            """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<ExternalEmployeesData> findAll() throws SQLException {
        String sql = """
            SELECT em.mitarbeiternummer, em.vorname, em.nachname, em.strasse, em.plz, em.ort,
                   em.telefon, em.email, em.firma_id, f.name AS firma_name
            FROM externe_mitarbeiter em
            JOIN firmen f ON f.firma_id = em.firma_id
            ORDER BY em.mitarbeiternummer
            """;
        List<ExternalEmployeesData> list = new ArrayList<>();
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public ExternalEmployeesData findNextById(int currentId) throws SQLException {
        String sql = """
            SELECT em.mitarbeiternummer, em.vorname, em.nachname, em.strasse, em.plz, em.ort,
                   em.telefon, em.email, em.firma_id, f.name AS firma_name
            FROM externe_mitarbeiter em
            JOIN firmen f ON f.firma_id = em.firma_id
            WHERE em.mitarbeiternummer > ?
            ORDER BY em.mitarbeiternummer ASC
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

    public ExternalEmployeesData findPreviousById(int currentId) throws SQLException {
        String sql = """
            SELECT em.mitarbeiternummer, em.vorname, em.nachname, em.strasse, em.plz, em.ort,
                   em.telefon, em.email, em.firma_id, f.name AS firma_name
            FROM externe_mitarbeiter em
            JOIN firmen f ON f.firma_id = em.firma_id
            WHERE em.mitarbeiternummer < ?
            ORDER BY em.mitarbeiternummer DESC
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

    public ExternalEmployeesData findNextByIdCircular(int currentId) throws SQLException {
        ExternalEmployeesData next = findNextById(currentId);
        return (next != null) ? next : findFirst();
    }

    public ExternalEmployeesData findPreviousByIdCircular(int currentId) throws SQLException {
        ExternalEmployeesData prev = findPreviousById(currentId);
        return (prev != null) ? prev : findLast();
    }

    public ExternalEmployeesData findFirst() throws SQLException {
        String sql = """
            SELECT em.mitarbeiternummer, em.vorname, em.nachname, em.strasse, em.plz, em.ort,
                   em.telefon, em.email, em.firma_id, f.name AS firma_name
            FROM externe_mitarbeiter em
            JOIN firmen f ON f.firma_id = em.firma_id
            ORDER BY em.mitarbeiternummer ASC
            LIMIT 1
            """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        }
    }

    public ExternalEmployeesData findLast() throws SQLException {
        String sql = """
            SELECT em.mitarbeiternummer, em.vorname, em.nachname, em.strasse, em.plz, em.ort,
                   em.telefon, em.email, em.firma_id, f.name AS firma_name
            FROM externe_mitarbeiter em
            JOIN firmen f ON f.firma_id = em.firma_id
            ORDER BY em.mitarbeiternummer DESC
            LIMIT 1
            """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        }
    }

    /* -------------------- WRITE -------------------- */

    /** Insert, wenn du NUR den Firmennamen im Modell hast. */
    public void insert(ExternalEmployeesData p) throws SQLException {
        try (Connection conn = connector.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int companyId = getOrCreateCompanyId(conn, p.getCompany()); // FK besorgen
                String sql = """
                    INSERT INTO externe_mitarbeiter
                    (mitarbeiternummer, vorname, nachname, strasse, plz, ort, telefon, email, firma_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, p.getId());
                    stmt.setString(2, p.getFirstName());
                    stmt.setString(3, p.getLastName());
                    stmt.setString(4, p.getStreet());
                    stmt.setString(5, p.getPlz());
                    stmt.setString(6, p.getPlace());
                    stmt.setString(7, p.getPhone());
                    stmt.setString(8, p.getEmail());
                    stmt.setInt(9, companyId);
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
        String sql = "DELETE FROM externe_mitarbeiter WHERE mitarbeiternummer = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /* -------------------- Helpers -------------------- */

    /** Holt die firma_id zu name; legt sie an, wenn sie nicht existiert. */
    private int getOrCreateCompanyId(Connection conn, String companyName) throws SQLException {
        // 1) versuchen zu finden
        try (PreparedStatement s = conn.prepareStatement(
                "SELECT firma_id FROM firmen WHERE name = ?")) {
            s.setString(1, companyName);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        // 2) sonst anlegen (UNIQUE auf name vorausgesetzt)
        try (PreparedStatement s = conn.prepareStatement(
                "INSERT INTO firmen(name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, companyName);
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException dup) {
            // Parallel angelegt? Dann nochmal selektieren.
            try (PreparedStatement s2 = conn.prepareStatement(
                    "SELECT firma_id FROM firmen WHERE name = ?")) {
                s2.setString(1, companyName);
                try (ResultSet rs2 = s2.executeQuery()) {
                    if (rs2.next()) return rs2.getInt(1);
                }
            }
            throw dup;
        }
        throw new SQLException("Konnte firma_id nicht ermitteln für: " + companyName);
    }

    /** Mapper: baut ExternalEmployeesData inkl. Company-Name aus Join-Spalte 'firma_name'. */
    private ExternalEmployeesData mapRow(ResultSet rs) throws SQLException {
        ExternalEmployeesData p = new ExternalEmployeesData(
                rs.getString("vorname"),
                rs.getString("nachname"),
                rs.getString("strasse"),
                rs.getString("plz"),
                rs.getString("ort"),
                rs.getString("telefon"),
                rs.getString("email"),
                rs.getString("firma_name") // Klartextname aus JOIN
        );
        p.setId(rs.getInt("mitarbeiternummer"));
        return p;
    }
}
