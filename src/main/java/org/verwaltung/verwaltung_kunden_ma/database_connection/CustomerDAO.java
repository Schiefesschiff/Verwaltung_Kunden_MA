package org.verwaltung.verwaltung_kunden_ma.database_connection;

import org.verwaltung.verwaltung_kunden_ma.PersonDatas.CustomerData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the {@code kunden} table.
 * <p>
 * Provides CRUD operations for customers, including navigation helpers
 * (next/previous, circular) and automatic handling of the industry
 * ({@code branche}) relationship.
 */
public class CustomerDAO
{
    private final SQLConnector connector;

    /**
     * Creates a new {@link CustomerDAO}.
     *
     * @param connector the SQL connector providing database connections
     */
    public CustomerDAO(SQLConnector connector)
    {
        this.connector = connector;
    }

    /* -------------------- READ -------------------- */

    /**
     * Finds a customer by ID.
     *
     * @param id the customer number
     * @return the matching {@link CustomerData}, or {@code null} if not found
     * @throws SQLException if a database access error occurs
     */
    public CustomerData findById(int id) throws SQLException
    {
        String sql = """
                SELECT k.kundennummer, k.vorname, k.nachname, k.strasse, k.plz, k.ort,
                       k.telefon, k.email, k.branche_id, b.name AS branche_name
                FROM kunden k
                JOIN branchen b ON b.branche_id = k.branche_id
                WHERE k.kundennummer = ?
                """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery())
            {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Retrieves all customers from the database ordered by ID.
     *
     * @return list of all customers (never {@code null})
     * @throws SQLException if a database access error occurs
     */
    public List<CustomerData> findAll() throws SQLException
    {
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
             ResultSet rs = stmt.executeQuery())
        {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Finds the customer record with the next higher ID.
     *
     * @param currentId the current customer number
     * @return the next {@link CustomerData}, or {@code null} if none exists
     * @throws SQLException if a database access error occurs
     */
    public CustomerData findNextById(int currentId) throws SQLException
    {
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
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, currentId);
            try (ResultSet rs = stmt.executeQuery())
            {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Finds the customer record with the next lower ID.
     *
     * @param currentId the current customer number
     * @return the previous {@link CustomerData}, or {@code null} if none exists
     * @throws SQLException if a database access error occurs
     */
    public CustomerData findPreviousById(int currentId) throws SQLException
    {
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
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, currentId);
            try (ResultSet rs = stmt.executeQuery())
            {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Finds the next customer by ID, or wraps around to the first one if none is found.
     *
     * @param currentId the current customer number
     * @return the next customer, or the first one if wrap-around occurred
     * @throws SQLException if a database access error occurs
     */
    public CustomerData findNextByIdCircular(int currentId) throws SQLException
    {
        CustomerData next = findNextById(currentId);
        return (next != null) ? next : findFirst();
    }

    /**
     * Finds the previous customer by ID, or wraps around to the last one if none is found.
     *
     * @param currentId the current customer number
     * @return the previous customer, or the last one if wrap-around occurred
     * @throws SQLException if a database access error occurs
     */
    public CustomerData findPreviousByIdCircular(int currentId) throws SQLException
    {
        CustomerData prev = findPreviousById(currentId);
        return (prev != null) ? prev : findLast();
    }

    /**
     * Returns the customer with the smallest ID.
     *
     * @return the first customer, or {@code null} if no customers exist
     * @throws SQLException if a database access error occurs
     */
    public CustomerData findFirst() throws SQLException
    {
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
             ResultSet rs = stmt.executeQuery())
        {
            return rs.next() ? mapRow(rs) : null;
        }
    }

    /**
     * Returns the customer with the largest ID.
     *
     * @return the last customer, or {@code null} if no customers exist
     * @throws SQLException if a database access error occurs
     */
    public CustomerData findLast() throws SQLException
    {
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
             ResultSet rs = stmt.executeQuery())
        {
            return rs.next() ? mapRow(rs) : null;
        }
    }


    /**
     * Inserts a new customer into the database.
     * <p>
     * If the referenced industry (branche) does not yet exist, it will be created automatically.
     *
     * @param c the customer to insert
     * @throws SQLException if the insert fails or the branche cannot be resolved
     */
    public void insert(CustomerData c) throws SQLException
    {
        try (Connection conn = connector.getConnection())
        {
            conn.setAutoCommit(false);
            try
            {
                int brancheId = getOrCreateBrancheId(conn, c.getIndustry());
                String sql = """
                        INSERT INTO kunden
                        (kundennummer, vorname, nachname, strasse, plz, ort, telefon, email, branche_id)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement stmt = conn.prepareStatement(sql))
                {
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
            } catch (SQLException ex)
            {
                conn.rollback();
                throw ex;
            } finally
            {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Deletes a customer by ID.
     *
     * @param id the customer number to delete
     * @throws SQLException if a database access error occurs
     */
    public void delete(int id) throws SQLException
    {
        String sql = "DELETE FROM kunden WHERE kundennummer = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /* -------------------- Helpers -------------------- */

    /**
     * Looks up the {@code branche_id} for the given industry name.
     * If the industry does not exist, it is created.
     *
     * @param conn        the active SQL connection
     * @param brancheName the industry name
     * @return the branche ID
     * @throws SQLException if the branche could not be resolved or created
     */
    private int getOrCreateBrancheId(Connection conn, String brancheName) throws SQLException
    {
        // 1) versuchen zu finden
        try (PreparedStatement s = conn.prepareStatement(
                "SELECT branche_id FROM branchen WHERE name = ?"))
        {
            s.setString(1, brancheName);
            try (ResultSet rs = s.executeQuery())
            {
                if (rs.next()) return rs.getInt(1);
            }
        }
        // 2) sonst anlegen
        try (PreparedStatement s = conn.prepareStatement(
                "INSERT INTO branchen(name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS))
        {
            s.setString(1, brancheName);
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys())
            {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException dup)
        {
            // Falls parallel angelegt: erneut selektieren
            try (PreparedStatement s2 = conn.prepareStatement(
                    "SELECT branche_id FROM branchen WHERE name = ?"))
            {
                s2.setString(1, brancheName);
                try (ResultSet rs2 = s2.executeQuery())
                {
                    if (rs2.next()) return rs2.getInt(1);
                }
            }
            throw dup;
        }
        throw new SQLException("Konnte branche_id nicht ermitteln für: " + brancheName);
    }

    /**
     * Maps a {@link ResultSet} row into a {@link CustomerData} object.
     *
     * @param rs the result set positioned at a valid row
     * @return a populated {@link CustomerData} instance
     * @throws SQLException if column access fails
     */
    private CustomerData mapRow(ResultSet rs) throws SQLException
    {
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
