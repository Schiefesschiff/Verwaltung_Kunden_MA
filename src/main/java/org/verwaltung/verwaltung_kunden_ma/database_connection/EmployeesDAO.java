package org.verwaltung.verwaltung_kunden_ma.database_connection;

import org.verwaltung.verwaltung_kunden_ma.PersonDatas.PersonData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeesDAO
{
    private final SQLConnector connector;

    public EmployeesDAO(SQLConnector connector)
    {
        this.connector = connector;
    }

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

    // NÄCHSTER (strict > currentId). Gibt null zurück, wenn es keinen größeren gibt.
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

    // VORHERIGER (strict < currentId). Gibt null zurück, wenn es keinen kleineren gibt.
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

    // OPTIONAL: „circular“ – springt zum ersten/letzten, wenn es keinen nächsten/vorherigen gibt.
    public PersonData findNextByIdCircular(int currentId) throws SQLException {
        PersonData next = findNextById(currentId);
        return (next != null) ? next : findFirst();
    }

    public PersonData findPreviousByIdCircular(int currentId) throws SQLException {
        PersonData prev = findPreviousById(currentId);
        return (prev != null) ? prev : findLast();
    }

    // Erster/Letzter nach mitarbeiternummer (nützlich für UI-Buttons „<<“/„>>“)
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

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM mitarbeiter WHERE mitarbeiternummer = ?";

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

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
