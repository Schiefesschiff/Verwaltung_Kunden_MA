package org.verwaltung.verwaltung_kunden_ma.database_connection;

import org.verwaltung.verwaltung_kunden_ma.PersonDatas.PersonData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the {@code mitarbeiter} table.
 * <p>
 * Provides CRUD operations and navigation methods for employees.
 * All operations use JDBC with the provided {@link SQLConnector}.
 */
public class EmployeesDAO
{
    private final SQLConnector connector;

    /**
     * Creates a new EmployeesDAO.
     *
     * @param connector the SQLConnector providing database connections
     */
    public EmployeesDAO(SQLConnector connector)
    {
        this.connector = connector;
    }

    /**
     * Finds an employee by ID.
     *
     * @param id employee number
     * @return the {@link PersonData} or {@code null} if not found
     * @throws SQLException if a database error occurs
     */
    public PersonData findById(int id) throws SQLException {
        String sql = "SELECT * FROM mitarbeiter WHERE mitarbeiternummer = ?";

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPerson(rs);
            }
        }
        return null;
    }


    /**
     * Returns all employees ordered by ID.
     *
     * @return list of employees (never {@code null})
     * @throws SQLException if a database error occurs
     */
    public List<PersonData> findAll() throws SQLException
    {
        String sql = "SELECT * FROM mitarbeiter ORDER BY mitarbeiternummer";
        List<PersonData> list = new ArrayList<>();

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery())
        {
            while (rs.next())
            {
                list.add(mapResultSetToPerson(rs));
            }
        }
        return list;
    }

    /**
     * Finds the next employee (with a higher ID).
     *
     * @param currentId current employee number
     * @return next employee or {@code null} if none exists
     * @throws SQLException if a database error occurs
     */
    public PersonData findNextById(int currentId) throws SQLException {
        String sql = """
        SELECT *
        FROM mitarbeiter
        WHERE mitarbeiternummer > ?
        ORDER BY mitarbeiternummer ASC
        LIMIT 1
        """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToPerson(rs) : null;
            }
        }
    }

    /**
     * Finds the previous employee (with a lower ID).
     *
     * @param currentId current employee number
     * @return previous employee or {@code null} if none exists
     * @throws SQLException if a database error occurs
     */
    public PersonData findPreviousById(int currentId) throws SQLException {
        String sql = """
        SELECT *
        FROM mitarbeiter
        WHERE mitarbeiternummer < ?
        ORDER BY mitarbeiternummer DESC
        LIMIT 1
        """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToPerson(rs) : null;
            }
        }
    }

    /**
     * Finds the next employee or wraps around to the first one.
     *
     * @param currentId current employee number
     * @return next or first employee
     * @throws SQLException if a database error occurs
     */
    public PersonData findNextByIdCircular(int currentId) throws SQLException {
        PersonData next = findNextById(currentId);
        return (next != null) ? next : findFirst();
    }

    /**
     * Finds the previous employee or wraps around to the last one.
     *
     * @param currentId current employee number
     * @return previous or last employee
     * @throws SQLException if a database error occurs
     */
    public PersonData findPreviousByIdCircular(int currentId) throws SQLException {
        PersonData prev = findPreviousById(currentId);
        return (prev != null) ? prev : findLast();
    }

    /**
     * Returns the employee with the smallest ID.
     *
     * @return first employee or {@code null} if table is empty
     * @throws SQLException if a database error occurs
     */
    public PersonData findFirst() throws SQLException {
        String sql = """
        SELECT *
        FROM mitarbeiter
        ORDER BY mitarbeiternummer ASC
        LIMIT 1
        """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? mapResultSetToPerson(rs) : null;
        }
    }

    /**
     * Returns the employee with the largest ID.
     *
     * @return last employee or {@code null} if table is empty
     * @throws SQLException if a database error occurs
     */
    public PersonData findLast() throws SQLException {
        String sql = """
        SELECT *
        FROM mitarbeiter
        ORDER BY mitarbeiternummer DESC
        LIMIT 1
        """;
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? mapResultSetToPerson(rs) : null;
        }
    }

    /**
     * Inserts a new employee into the database.
     *
     * @param p the employee to insert
     * @throws SQLException if insertion fails
     */
    public void insert(PersonData p) throws SQLException {
        String sql = "INSERT INTO mitarbeiter " +
                "(mitarbeiternummer, vorname, nachname, strasse, plz, ort, telefon, email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, p.getId());
            stmt.setString(2, p.getFirstName());
            stmt.setString(3, p.getLastName());
            stmt.setString(4, p.getStreet());
            stmt.setString(5, p.getPlz());
            stmt.setString(6, p.getPlace());
            stmt.setString(7, p.getPhone());
            stmt.setString(8, p.getEmail());

            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an employee by ID.
     *
     * @param id the employee number to delete
     * @throws SQLException if a database error occurs
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM mitarbeiter WHERE mitarbeiternummer = ?";

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a row of the {@code mitarbeiter} table into a {@link PersonData}.
     *
     * @param rs a ResultSet positioned at a row
     * @return the mapped employee data
     * @throws SQLException if column access fails
     */
    private PersonData mapResultSetToPerson(ResultSet rs) throws SQLException {
        PersonData p = new PersonData(
                rs.getString("vorname"),
                rs.getString("nachname"),
                rs.getString("strasse"),
                rs.getString("plz"),
                rs.getString("ort"),
                rs.getString("telefon"),
                rs.getString("email")
        );
        p.setId(rs.getInt("mitarbeiternummer"));
        return p;
    }
}
