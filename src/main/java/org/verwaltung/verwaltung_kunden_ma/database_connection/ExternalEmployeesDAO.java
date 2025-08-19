package org.verwaltung.verwaltung_kunden_ma.database_connection;

import org.verwaltung.verwaltung_kunden_ma.PersonDatas.ExternalEmployeesData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the {@code externe_mitarbeiter} table.
 * <p>
 * Provides CRUD operations and navigation methods for external employees.
 * Uses JDBC via the provided {@link SQLConnector} to query and update the database.
 */
public class ExternalEmployeesDAO {
    private final SQLConnector connector;

    /**
     * Creates a new DAO for external employees.
     *
     * @param connector the SQLConnector used to obtain database connections
     */
    public ExternalEmployeesDAO(SQLConnector connector) {
        this.connector = connector;
    }

    /**
     * Finds an external employee by ID.
     *
     * @param id employee number
     * @return the employee or {@code null} if not found
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Returns all external employees ordered by ID.
     *
     * @return list of employees (never {@code null})
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Finds the next external employee (with a higher ID).
     *
     * @param currentId current employee number
     * @return next employee or {@code null} if none exists
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Finds the previous external employee (with a lower ID).
     *
     * @param currentId current employee number
     * @return previous employee or {@code null} if none exists
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Finds the next external employee or wraps around to the first one.
     *
     * @param currentId current employee number
     * @return next or first employee
     * @throws SQLException if a database error occurs
     */
    public ExternalEmployeesData findNextByIdCircular(int currentId) throws SQLException {
        ExternalEmployeesData next = findNextById(currentId);
        return (next != null) ? next : findFirst();
    }

    /**
     * Finds the previous external employee or wraps around to the last one.
     *
     * @param currentId current employee number
     * @return previous or last employee
     * @throws SQLException if a database error occurs
     */
    public ExternalEmployeesData findPreviousByIdCircular(int currentId) throws SQLException {
        ExternalEmployeesData prev = findPreviousById(currentId);
        return (prev != null) ? prev : findLast();
    }

    /**
     * Returns the external employee with the smallest ID.
     *
     * @return first employee or {@code null} if none exist
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Returns the external employee with the largest ID.
     *
     * @return last employee or {@code null} if none exist
     * @throws SQLException if a database error occurs
     */
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

    /**
     * Inserts a new external employee into the database.
     * <p>
     * If the company name does not yet exist in {@code firmen},
     * a new entry is created and its ID is used as foreign key.
     *
     * @param p the employee to insert
     * @throws SQLException if the insert fails
     */
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

    /**
     * Deletes an external employee by ID.
     *
     * @param id the employee number
     * @throws SQLException if the delete fails
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM externe_mitarbeiter WHERE mitarbeiternummer = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }


    /**
     * Resolves a company name to its {@code firma_id}.
     * <p>
     * If the company does not exist, a new row is inserted.
     *
     * @param conn active connection
     * @param companyName name of the company
     * @return the company ID
     * @throws SQLException if the lookup or insert fails
     */
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

    /**
     * Maps a {@link ResultSet} row to an {@link ExternalEmployeesData} object.
     *
     * @param rs result set positioned at a row
     * @return mapped employee data
     * @throws SQLException if column access fails
     */
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
